
<!-- IFT3913-TACHE3-START -->
# Tâche 3

## Détection automatique des vulnérabilités avec Semgrep

### Implémentation

Nous avons ajouté un nouveau job `semgrep` dans le fichier `.github/workflows/main-jdk17-build.yml` pour détecter les nouvelles vulnérabilités dans le code de Tika. Ce job est indépendant et se trouve au même niveau que les jobs `build` et `e2e-tests`, sans modifier leur fonctionnement.

Le job `semgrep` exécute les étapes suivantes :

- **Récupération du code :** `actions/checkout@v6` récupère le dépôt. Nous utilisons `fetch-depth: 0` pour avoir accès à tout l'historique Git, nécessaire pour comparer les commits.
- **Choix du commit de référence :** l'étape `reference` détermine automatiquement le commit avec lequel les modifications seront comparées.
- **Installation :** la commande `pip install semgrep` installe Semgrep sur la machine utilisée par GitHub Actions.
- **Analyse :** l'option `--config p/java` utilise les règles publiques Java de Semgrep. L'option `--baseline-commit` permet de repérer les nouvelles vulnérabilités et `--error` fait échouer le job lorsqu'une nouvelle vulnérabilité est détectée.
- **Rapport :** les options `--json` et `--output` enregistrent les résultats dans `semgrep-resultat.json`. Le fichier est ensuite conservé comme artefact GitHub Actions sous le nom `rapport-semgrep`, même lorsque l'analyse échoue. L'option `--metrics=off` désactive l'envoi des métriques à Semgrep.

### Comparaison avec un commit de référence

Nous utilisons `--baseline-commit` pour détecter uniquement les nouvelles vulnérabilités. Le commit de référence correspond au commit précédent pour un push ou au commit de base pour une pull request. Si cette référence est indisponible, `git merge-base` permet de trouver un point de comparaison avec `main`.

### Validation

Nous avons vérifié le fonctionnement de Semgrep localement, puis sur GitHub Actions, afin de tester les deux situations : une analyse réussie et une analyse qui détecte une nouvelle vulnérabilité.

**Vérification locale**

Nous avons vérifié que Semgrep détecte les vulnérabilités dans chaque fichier Java modifié à l'aide de la commande suivante :

```bash
semgrep scan --config p/java --metrics=off chemin/du/fichier.java
```

**Aucune nouvelle vulnérabilité**

Nous avons vérifié que le job `semgrep` s'exécute correctement et se termine avec succès lorsqu'aucune nouvelle vulnérabilité n'est détectée. [Exécution réussie](https://github.com/nass1379/tika/actions/runs/43544299763/job/132291819827)

**Nouvelles vulnérabilités**

Lorsqu'un commit introduit une nouvelle vulnérabilité détectée par Semgrep, le job `semgrep` passe en échec avec un code de sortie 1. [Exécution en échec](https://github.com/nass1379/tika/actions/runs/43553274275/job/132320316336)


## Vulnérabilités

### Vulnérabilité 1 : hachage faible (MD5)

- **Type :** utilisation d'un algorithme de hachage cryptographique faible.
- **Choix du fichier :** `tika-core/src/main/java/org/apache/tika/utils/ProcessUtils.java`.  
  Cette classe regroupe des méthodes statiques liées à la gestion des processus dans Tika. Nous y avons ajouté une méthode indépendante, qui n'est pas appelée par les méthodes existantes. Leur comportement ne change donc pas.
- **Code ajouté :**  
  La méthode `insecureHash(String value)` utilise MD5 pour calculer l'empreinte d'une chaîne de caractères et retourne le résultat sous forme de tableau d'octets.
- **Pourquoi c'est vulnérable :**  
  MD5 est vulnérable aux collisions : deux données différentes peuvent produire la même empreinte. Son utilisation peut compromettre les mécanismes qui reposent sur la résistance aux collisions, comme certaines vérifications d'intégrité ou d'authenticité. SHA-256 serait préférable pour ces usages.
- **Résultat Semgrep :**  
  Nous avons analysé `ProcessUtils.java` avec les règles Java (`p/java`). Semgrep a détecté l'utilisation de MD5 et l'a signalée comme une faiblesse de sécurité. [Run](https://github.com/nass1379/tika/actions/runs/43553274275/job/132320316336)

### Vulnérabilité 2 : injection SQL

- **Type :** injection SQL.
- **Choix du fichier :** `tika-core/src/main/java/org/apache/tika/metadata/Metadata.java`.  
  Cette classe sert à gérer les métadonnées des documents dans Tika. Ces valeurs peuvent provenir de documents non fiables, ce qui rend leur enregistrement dans une base de données pertinent pour illustrer une injection SQL.
- **Code ajouté :**  
  La méthode `saveToDatabase(Connection conn, String key)` permet d'enregistrer une métadonnée dans une base de données. Elle construit la requête SQL en concaténant directement la clé et sa valeur.
- **Pourquoi c'est vulnérable :**  
  La requête est construite à partir de valeurs qui pourraient provenir d'une source non fiable. Si l'une contient des caractères ou des instructions SQL malveillantes, elle pourrait modifier la requête exécutée. Un `PreparedStatement` avec des paramètres permettrait de séparer les données de la commande SQL et d'éviter ce problème.
- **Résultat Semgrep :**  
  Nous avons analysé `Metadata.java` avec `p/java`. L'injection SQL a été détectée (`1 finding`, `1 blocking`). [Run](https://github.com/nass1379/tika/actions/runs/43632586929/job/132562258032)

### Vulnérabilité 3 : XML External Entity (XXE)

- **Type :** XML External Entity (XXE).
- **Choix du fichier :** `tika-core/src/main/java/org/apache/tika/sax/OfflineContentHandler.java`.  
  Cette classe fait partie des composants de Tika liés au traitement du contenu XML avec SAX, ce qui en fait un endroit pertinent pour introduire une vulnérabilité liée au traitement XML.
- **Code ajouté :**  
  La méthode `parseXML(InputStream input)` analyse un document XML avec `DocumentBuilderFactory`, sans configurer le parseur pour bloquer les entités externes.
- **Pourquoi c'est vulnérable :**  
  Le parseur est créé sans configuration de sécurité contre les entités externes. Selon sa configuration, un document XML malveillant pourrait exploiter cette faiblesse pour tenter de lire des fichiers locaux ou d'accéder à des ressources internes. Il faudrait notamment désactiver les déclarations `DOCTYPE` et la résolution des entités externes.
- **Résultat Semgrep :**  
  Nous avons analysé `OfflineContentHandler.java` avec `p/java`. La vulnérabilité XXE a été détectée (`1 finding`, `1 blocking`). [Run](https://github.com/nass1379/tika/actions/runs/43635358793/job/132570853817)

### Vulnérabilité 4 :

### Vulnérabilité 5 :

### Vulnérabilité 6 :

### Vulnérabilité 7 :


## Rickroll

### Implémentation

Nous avons créé une action réutilisable dans `.github/actions/rickroll/action.yml` pour afficher un Rickroll lorsqu'un test de Tika échoue.

L'action vérifie les rapports de tests Maven (Surefire et Failsafe) pour repérer les balises `<failure>` ou `<error>`. Si un échec est détecté, elle ajoute au résumé GitHub Actions un lien intitulé « Identifier la source du problème », qui mène à la vidéo Rickroll. Cette action est appelée dans `.github/workflows/main-jdk17-build.yml`, après les étapes Maven des jobs `build` et `e2e-tests`.

### Validation

Nous avons vérifié sur GitHub Actions que le Rickroll s'affiche dans le résumé après un échec Maven. [Exécution sur GitHub Actions](https://github.com/nass1379/tika/actions/runs/43544299763)

![Rickroll dans GitHub Actions](images/rickroll.png)


## Comparaison des rapports Semgrep


---

#### Utilisation de l'intelligence artificielle

Claude et ChatGPT ont été utilisés pour aider à la rédaction et à la reformulation de certaines parties du README, ainsi que pour certains aspects de l'implémentation du Rickroll.
