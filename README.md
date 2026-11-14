
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


## Rickroll

### Implémentation

Nous avons créé une action réutilisable dans `.github/actions/rickroll/action.yml` pour afficher un Rickroll lorsqu'un test de Tika échoue.

L'action vérifie les rapports de tests Maven (Surefire et Failsafe) pour repérer les balises `<failure>` ou `<error>`. Si un échec est détecté, elle ajoute au résumé GitHub Actions un lien intitulé « Identifier la source du problème », qui mène à la vidéo Rickroll. Cette action est appelée dans `.github/workflows/main-jdk17-build.yml`, après les étapes Maven des jobs `build` et `e2e-tests`.

### Validation

Nous avons vérifié sur GitHub Actions que le Rickroll s'affiche dans le résumé après un échec Maven. [Exécution sur GitHub Actions](https://github.com/nass1379/tika/actions/runs/43544299763)

![Rickroll dans GitHub Actions](images/rickroll.png)




