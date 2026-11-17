<!-- IFT3913-TACHE3-START -->
# Tâche 3 — Semgrep et vulnérabilités

Nous avons ajouté sept vulnérabilités pédagogiques dans Tika, configuré GitHub Actions pour détecter les nouveaux problèmes et créé une action Rickroll lorsqu’un test échoue.

## Détection avec Semgrep

Le job `semgrep` du [workflow Java 17](.github/workflows/main-jdk17-build.yml) s’exécute sur les push et les pull requests vers `main` et `tache3`, indépendamment des tests Maven.

Nous utilisons **Semgrep CE 1.185.0** et un [instantané de 60 règles Java](rapports-semgrep/java-rules.yaml), récupéré le 16 novembre 2026. Figer ces versions permet de comparer les résultats avec les mêmes détecteurs.

Le workflow récupère tout l’historique Git avec `fetch-depth: 0`, puis choisit sa référence : le commit précédent pour un push, ou le commit de base pour une PR. Si la référence est vide ou nulle, il utilise le point commun avec `main`.

```bash
semgrep scan --config rapports-semgrep/java-rules.yaml --no-rewrite-rule-ids \
  --baseline-commit "$BASE" --metrics=off \
  --json --output semgrep-resultat.json --error
```

`--baseline-commit` limite les résultats aux nouvelles alertes. `--error` fait échouer le job lorsqu’il en trouve. Les identifiants des règles sont conservés avec `--no-rewrite-rule-ids`, et les métriques sont désactivées. Le rapport JSON est sauvegardé dans l’artefact `rapport-semgrep`, même en cas d’échec.

Nous avons vérifié les deux cas :

- **Sans nouvelle vulnérabilité :** [Semgrep passe](https://github.com/nass1379/tika/actions/runs/44128171110/job/134197591008) sur un commit de documentation et de configuration.
- **Avec de nouvelles vulnérabilités :** [Semgrep bloque la PR](https://github.com/nass1379/tika/actions/runs/44022291115/job/133834331939), avec quatre alertes et le code de sortie 1. Le [rapport conservé](rapports-semgrep/ci-pr1.json) contient les quatre ajouts attendus, sans erreur d’analyse.

La PR compare nos quatre derniers ajouts au code contenant déjà les trois premiers. La comparaison complète ci-dessous part, elle, du commit précédant les sept ajouts.

## Les sept vulnérabilités

### 1. Hachage faible : MD5

Dans [ProcessUtils.java](tika-core/src/main/java/org/apache/tika/utils/ProcessUtils.java), `insecureHash(String)` calcule une empreinte MD5. Nous avons choisi une méthode utilitaire indépendante pour ne pas modifier les traitements existants ; ce placement sert à l’expérience.

MD5 permet des collisions : deux contenus différents peuvent avoir la même empreinte. Il est donc inadapté aux vérifications exigeant une résistance aux collisions. SHA-256 serait préférable pour cet usage.

**Semgrep :** une nouvelle alerte `use-of-md5`. [Validation CI](https://github.com/nass1379/tika/actions/runs/43553274275/job/132320316336).

### 2. Injection SQL

Dans [Metadata.java](tika-core/src/main/java/org/apache/tika/metadata/Metadata.java), `saveToDatabase(Connection, String)` construit une requête en concaténant une clé et sa valeur. Ce fichier gère les métadonnées, qui peuvent provenir de documents non fiables.

Une valeur malveillante pourrait modifier la requête exécutée. Un `PreparedStatement` avec des paramètres éviterait de mélanger les données et les instructions SQL.

**Semgrep :** une nouvelle alerte `formatted-sql-string`. [Validation CI](https://github.com/nass1379/tika/actions/runs/43632586929/job/132562258032).

### 3. Entités XML externes : XXE

Dans [OfflineContentHandler.java](tika-core/src/main/java/org/apache/tika/sax/OfflineContentHandler.java), `parseXML(InputStream)` utilise `DocumentBuilderFactory` sans bloquer les entités externes. Cette classe traite du contenu XML, ce qui justifie le choix du fichier.

Selon la configuration du parseur, un document malveillant pourrait provoquer une lecture de fichiers locaux ou des requêtes vers des ressources internes. Il faudrait désactiver les déclarations `DOCTYPE` et la résolution des entités externes.

**Semgrep :** une nouvelle alerte `documentbuilderfactory-disallow-doctype-decl-missing`. [Validation CI](https://github.com/nass1379/tika/actions/runs/43635358793/job/132570853817).

### 4. Chiffrement AES en mode ECB

Dans [ProcessUtils.java](tika-core/src/main/java/org/apache/tika/utils/ProcessUtils.java), `insecureEncrypt(byte[], SecretKey)` utilise `Cipher.getInstance("AES/ECB/PKCS5Padding")`. Nous avons regroupé cet exemple avec celui du hachage ; le chiffrement n’est pas une responsabilité habituelle de cette classe.

Avec la même clé, des blocs clairs identiques produisent des blocs chiffrés identiques, ce qui révèle les répétitions. AES-GCM, avec un nonce unique, serait une meilleure option. MD5 et AES-ECB partagent le CWE-327, mais illustrent deux problèmes différents : les collisions et la fuite de motifs.

**Semgrep :** une nouvelle alerte `use-of-aes-ecb`. L’occurrence déjà présente dans `HwpTextExtractorV5` n’est pas comptée comme notre ajout.

### 5. Transmission sans TLS

Dans [NetworkParser.java](tika-core/src/main/java/org/apache/tika/parser/NetworkParser.java), `insecureSendDocument(String, int, byte[])` écrit le contenu d’un document dans une `Socket` TCP ordinaire. Cette classe échange déjà des documents avec des services distants.

Les données circulent sans chiffrement et pourraient être lues sur le réseau. La correction serait un transport TLS avec validation du certificat et de l’identité du serveur.

**Semgrep :** une nouvelle alerte `unencrypted-socket` (CWE-319). Le fichier contenait déjà une socket signalée : il passe donc de **une à deux occurrences**.

### 6. Désérialisation JSON non sûre

Dans [TikaObjectMapperFactory.java](tika-serialization/src/main/java/org/apache/tika/config/loader/TikaObjectMapperFactory.java), `insecureReadPolymorphicJson(String)` active `enableDefaultTyping()` avant `readValue(json, Object.class)`. Le choix du fichier est lié à son rôle de configuration de Jackson.

Une entrée non fiable peut sélectionner des classes à désérialiser. Si des classes exploitables sont présentes, cela peut entraîner des effets indésirables, voire une exécution de code. L’alerte seule ne prouve pas qu’une exploitation est possible avec les dépendances actuelles. Un type de données explicite ou une liste restrictive de types autorisés serait préférable.

**Semgrep :** une nouvelle alerte `jackson-unsafe-deserialization` (CWE-502).

### 7. Traversée de chemins

Dans [MetadataResource.java](tika-server/tika-server-core/src/main/java/org/apache/tika/server/core/resource/MetadataResource.java), `insecureReadMetadataFile(@PathParam("name") String)` construit `new File("metadata", name)` puis lit le fichier. Cette ressource serveur représente les métadonnées.

Un nom comme `../outside.txt` peut sortir du dossier prévu si le processus a accès au fichier. Il faudrait normaliser le chemin, vérifier qu’il reste dans le répertoire autorisé et gérer les liens symboliques.

**Semgrep :** une nouvelle alerte `jax-rs-path-traversal` (CWE-22). La méthode n’a pas d’annotation HTTP `GET` ou `POST` : elle illustre une lecture vulnérable si elle reçoit un nom non fiable, sans créer de nouvelle route accessible à distance.

Les quatre derniers exemples sont des méthodes indépendantes, sans appel ajouté aux parcours existants. Ils compilent et sont détectés statiquement ; nous n’avons pas exécuté de charge d’exploitation. Le [rapport CI](rapports-semgrep/ci-pr1.json) donne leurs identifiants de règles complets et leurs emplacements.

## Comparaison avant/après

Le commit de référence est **`b020dd1f9`**, avant les sept ajouts. Le [rapport initial de Dina](semgrep-avant.json) est conservé. Nous avons réanalysé cette version et le code final avec les mêmes versions de Semgrep et des règles, sur **1 535 fichiers Java**.

| Résultat | Nombre d’alertes |
|---|---:|
| Rapport initial et référence réanalysée | 11 |
| Après les sept ajouts | 18 |
| Nouvelles occurrences | 7 |
| Occurrences disparues | 0 |

Chacun des sept exemples ajoute une occurrence. Les 11 alertes préexistantes restent présentes. Les preuves sont disponibles dans les rapports [avant](rapports-semgrep/avant-reproduit.json), [après](rapports-semgrep/apres.json), [différentiel](rapports-semgrep/nouvelles.json) et la [comparaison détaillée](rapports-semgrep/comparaison.json).

Le [script de comparaison](scripts/compare_semgrep.py) utilise la règle, le chemin relatif et le code signalé, en conservant les occurrences multiples. Il normalise les chemins Windows et ignore les déplacements de lignes dus aux imports. Le code est lu dans les fichiers ou dans Git, car certains champs des rapports CE sont masqués par `requires login`.

Pour reproduire l’analyse, après installation de `semgrep==1.185.0` :

```bash
BASE_DIR=$(mktemp -d)
git archive b020dd1f9 | tar -x -C "$BASE_DIR"
semgrep scan --config rapports-semgrep/java-rules.yaml --no-rewrite-rule-ids \
  --metrics=off --json --output rapports-semgrep/avant-reproduit.json "$BASE_DIR"
semgrep scan --config rapports-semgrep/java-rules.yaml --no-rewrite-rule-ids \
  --metrics=off --json --output rapports-semgrep/apres.json .
python3 scripts/compare_semgrep.py
# Avec les ajouts commités : 7 nouvelles alertes, code de sortie 1 attendu.
semgrep scan --config rapports-semgrep/java-rules.yaml --no-rewrite-rule-ids \
  --baseline-commit b020dd1f9 --metrics=off --error \
  --json --output rapports-semgrep/nouvelles.json .
```

**Limites :** les deux scans ont le même avertissement de parsing partiel dans `PipesParsingHelper.java` et excluent deux fichiers de plus de 1 Mo. Les sept ajouts sont analysés sans cet avertissement. Un essai avant commit n’avait sélectionné qu’une partie des fichiers modifiés ; il est conservé comme [résultat intermédiaire](rapports-semgrep/differentiel-avant-commit.json). Le [contrôle après commit](rapports-semgrep/logs/differentiel-apres-commit.log) confirme bien les sept nouvelles alertes.

## Rickroll lorsqu’un test échoue

Notre [action réutilisable](.github/actions/rickroll/action.yml) est appelée après un échec Maven dans les jobs `build` et `e2e-tests`. Elle cherche les balises `<failure>` ou `<error>` dans les rapports Surefire et Failsafe. Si elle en trouve, elle ajoute au résumé GitHub Actions un lien « Identifier la source du problème » qui mène au Rickroll.

La validation comprend une [exécution GitHub Actions](https://github.com/nass1379/tika/actions/runs/43544299763) et [trois essais locaux](rapports-semgrep/rickroll-validation.json) : aucun Rickroll pour un test réussi, un Rickroll pour une erreur ou un échec de test.

![Rickroll dans GitHub Actions](images/rickroll.png)

## Résultats de validation

Sur le commit `bb94cd2a2`, avant la [fusion de la PR nº 1](https://github.com/nass1379/tika/pull/1), les vérifications suivantes ont réussi :

- [Build Linux Java 17](https://github.com/nass1379/tika/actions/runs/44128179619/job/134197621882) et [tests E2E](https://github.com/nass1379/tika/actions/runs/44128179619/job/134224888432).
- [Build Windows](https://github.com/nass1379/tika/actions/runs/44128179483) et [builds Windows avec deux locales](https://github.com/nass1379/tika/actions/runs/44128179732), après relance d’un téléchargement Maven interrompu.
- [Tests `tika-core`](https://github.com/nass1379/tika/actions/runs/44128179512), avec contrôles de licence et de style.

Seul le [job Semgrep de la PR](https://github.com/nass1379/tika/actions/runs/44128179619/job/134197621790) échoue volontairement : il détecte les quatre vulnérabilités ajoutées par rapport à sa base.

Les vérifications locales ont aussi confirmé la compilation des modules modifiés, les contrôles de style et **1 092 tests core/serialization sans échec ni erreur, dont quatre ignorés**. Les [journaux](rapports-semgrep/logs) conservent les résultats et leurs limites ; ces tests ne démontrent pas l’exploitation des méthodes ajoutées.

Deux problèmes d’infrastructure ont été corrigés pendant la validation :

- **MinIO :** l’image Quay renvoyait `401 Unauthorized`. Le [Dockerfile de test](tika-integration-tests/tika-pipes-s3-integration-tests/src/test/resources/Dockerfile.minio) construit maintenant la release officielle `RELEASE.2025-10-15T17-29-55Z` depuis les sources. Le test S3 passe localement et dans la CI. [Preuve locale](rapports-semgrep/minio-validation.json).
- **Apache RAT :** 528 signalements concernaient surtout les archives pédagogiques et les rapports générés. Nous avons ajouté les en-têtes aux scripts locaux et défini des exclusions ciblées pour les archives et les fichiers tiers, sans les modifier ni désactiver RAT. Le contrôle des 124 modules passe ; un fichier temporaire sans licence reste bien refusé. Une étape rapide vérifie désormais les licences de la racine avant le build. [Validation](rapports-semgrep/rat-validation.json).

## Utilisation de l’intelligence artificielle

Codex a aidé à préparer les quatre derniers exemples, le script de comparaison, les corrections CI et leur documentation. Claude et ChatGPT ont aussi aidé à la rédaction et à certains aspects du Rickroll. Les résultats ont été vérifiés avec Semgrep, Maven et GitHub Actions.
