<!-- IFT3913-TACHE2-START -->
# IFT3913 — Tâche 2 — Nassim Barhoumi et Dina Andolsi

## Expérience EndianUtils

Classe : `org.apache.tika.io.EndianUtils`, module `tika-core`. Les tests originaux sont dans `tika-core/src/test/java/org/apache/tika/io/EndianUtilsTest.java`.

### Mesure initiale

PIT 1.25.9, connecteur JUnit 1.2.3, opérateurs `DEFAULTS` : 206 mutants, dont 36 tués, 16 survivants et 154 non couverts. Score global : **36 / 206 = 17,48 %**. Couverture des lignes PIT : 31 / 121 = 25,62 %. Rapport conservé : [HTML](rapports-pit/endian-before/index.html), [XML](rapports-pit/endian-before/mutations.xml). Aucun test généré ne faisait partie de cette mesure.

### Génération locale avec ChatUniTest

Environnement : Java 21, Maven 3.9.12, JUnit 6.1.3, Mac Apple M1 Pro avec 16 Go de RAM. Ollama 0.34.4 exécute **Qwen 2.5 Coder 7B**, quantification Q4_K_M, modèle `qwen2.5-coder:7b` (identifiant `dae161e27b0e`, licence Apache 2.0).

ChatUniTest Maven 2.1.1 est configuré dans le profil explicite `chatunitest-local`. Il appelle uniquement `http://127.0.0.1:11434/v1/chat/completions`. La valeur `ollama-local` est une clé fictive, pas un secret. Aucune génération n'est liée aux phases Maven : `mvn test` ne déclenche pas Ollama.

Cette version de ChatUniTest refuse les noms de modèles absents de son énumération. L'alias local `codeqwen:v1.5-chat` désigne ici **Qwen 2.5 Coder 7B et non Qwen 1.5**. Le fichier [Modelfile](chatunitest-local/Modelfile) fixe le modèle source et une fenêtre de contexte de 8192 tokens. Recréation :

```bash
ollama create codeqwen:v1.5-chat -f chatunitest-local/Modelfile
mvn -B -f tika-core/pom.xml -Pchatunitest-local test-compile \
  io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:class \
  -DselectClass=org.apache.tika.io.EndianUtils \
  -Drat.skip=true -Dcheckstyle.skip=true -Dossindex.skip=true
```

Paramètres : une tentative par méthode, au plus trois tours (génération initiale puis deux réparations), température 0,2, génération séquentielle, 6000 tokens de prompt et 2048 de réponse, fusion désactivée. Le compilateur/exécuteur interne du plugin utilise des dépendances JUnit alignées sur 6.1.3.

### Incidents et adaptations de configuration

1. Première tentative interrompue : les propositions utilisaient Mockito, absent de `tika-core`, et importaient incorrectement `BufferUnderrunException`. Ajout de `mockito-core` et `mockito-junit-jupiter` en portée test, versions 5.23.0 du parent. Vérification : 749 tests existants, zéro échec, zéro erreur, deux ignorés.
2. Deuxième tentative interrompue : l'import incorrect persistait après les réparations automatiques. Les prompts ne précisaient pas que l'exception est imbriquée dans `EndianUtils`.
3. Troisième tentative : ajout au prompt système du nom exact `org.apache.tika.io.EndianUtils.BufferUnderrunException`, de l'environnement Java/JUnit et d'une préférence pour de vrais flux en mémoire. Aucun oracle attendu n'a été fourni. Les templates proviennent des sources publiées de ChatUniTest Core 2.1.1 ; les adaptations sont conservées dans [le prompt système](chatunitest-local/prompts/initial_system.ftl).

Les journaux sont dans [chatunitest-local/logs](chatunitest-local/logs). Les historiques JSON contiennent les prompts et réponses du modèle. Les corrections de configuration ci-dessus sont distinctes des 11 corrections du code de test détaillées plus bas.

Attention observée dans cette version : ChatUniTest peut exporter un test et afficher « compile and execute successfully » même lorsque JUnit signale un échec d'assertion. Une validation Maven indépendante est donc nécessaire ; le message de ChatUniTest seul n'établit pas que les tests passent.

### Résultat brut de la troisième tentative

Génération achevée le 28 septembre 2026 en 23 min 11 s. **31 méthodes/surcharges ciblées**, 61 réponses du modèle, dont 30 tours de réparation automatique. **18 fichiers exportés** dans `tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/`. Les 13 autres méthodes n'ont pas produit de fichier exporté après trois tours. Leurs propositions et erreurs restent dans les historiques.

Validation Maven indépendante : **71 tests exécutés, 60 réussis, 11 échecs d'assertion, 0 erreur, 0 ignoré**. Les 18 fichiers exportés compilent, mais l'ensemble ne s'exécute donc **pas avec succès sans intervention manuelle**. Au moment de cette mesure brute, **aucune correction manuelle du code de test** n'avait été effectuée. La section suivante décrit les 11 corrections apportées ensuite ; ce nombre est distinct des 30 tours de réparation automatique tentés et des adaptations de configuration.

Les preuves sont conservées hors de `target` dans [attempt-03](chatunitest-local/attempt-03) : fichiers exportés originaux, prompts/réponses JSON, erreurs et rapports Surefire. Le [journal de validation](chatunitest-local/logs/validation-raw.log) contient le `BUILD FAILURE` attendu pour cette version brute.

Commande utilisée pour la validation initiale, sans génération ni appel à Ollama. Depuis les corrections ci-dessous, cette même commande exécute les copies de travail corrigées ; les versions brutes immuables restent dans `chatunitest-local/attempt-03/exported/` :

```bash
mvn -B -pl tika-core -Pchatunitest-verify test \
  '-Dtest=EndianUtils_*_Test' \
  -Drat.skip=true -Dcheckstyle.skip=true -Dossindex.skip=true -Dspotless.skip=true
```

Le profil `chatunitest-verify` ajoute `tika-core/chatunitest` aux sources de test Maven ; il ne déclenche aucune génération. Les tests corrigés restent dans ce dossier, et non dans `src/test/java`. Activer ce profil est donc nécessaire pour les compiler depuis un checkout neuf. Après la mesure brute initiale, les classes compilées générées avaient été retirées de `target/test-classes` ; elles sont à nouveau présentes depuis la validation corrigée. Une exécution sans `clean` peut donc retrouver ces classes même sans le profil : les comparaisons de mutation devront contrôler explicitement les tests sélectionnés.

| Fichier généré (sans extension) | Tests | Échecs |
|---|---:|---:|
| `EndianUtils_getIntBE_23_0_Test` | 4 | 0 |
| `EndianUtils_getIntLE_20_0_Test` | 4 | 0 |
| `EndianUtils_getIntLE_21_0_Test` | 4 | 0 |
| `EndianUtils_getLongLE_28_0_Test` | 4 | 0 |
| `EndianUtils_getShortBE_16_0_Test` | 5 | 1 |
| `EndianUtils_getShortBE_17_0_Test` | 4 | 1 |
| `EndianUtils_getShortLE_12_0_Test` | 6 | 0 |
| `EndianUtils_getShortLE_13_0_Test` | 4 | 0 |
| `EndianUtils_getUIntBE_26_0_Test` | 4 | 1 |
| `EndianUtils_getUIntBE_27_0_Test` | 3 | 1 |
| `EndianUtils_getUIntLE_24_0_Test` | 4 | 2 |
| `EndianUtils_getUIntLE_25_0_Test` | 3 | 1 |
| `EndianUtils_getUShortBE_18_0_Test` | 4 | 2 |
| `EndianUtils_getUShortBE_19_0_Test` | 5 | 0 |
| `EndianUtils_getUShortLE_14_0_Test` | 4 | 1 |
| `EndianUtils_getUShortLE_15_0_Test` | 4 | 0 |
| `EndianUtils_readShortLE_0_0_Test` | 4 | 1 |
| `EndianUtils_ubyteToInt_29_0_Test` | 1 | 0 |

### Comparaison qualitative des oracles

Les tests originaux utilisent des valeurs attendues précises, notamment `4294967280L` pour le décodage non signé, et des flux trop courts. Leur portée reste limitée à quatre méthodes. Dans `testReadUIntBE`, le cas de flux trop court appelle cependant `readUIntLE` : ce cas ne valide donc pas directement l'exception de la variante BE.

Les tests générés élargissent le nombre de méthodes exercées et proposent des assertions de valeurs ainsi que des cas de débordement de tableau. Mais plusieurs oracles confondent l'ordre des octets ou le décalage. Par exemple, dans sa version brute, `EndianUtils_getUShortBE_18_0_Test.testGetUShortBE` attendait 256 pour les octets `00 01` en big-endian. Le calcul indépendant `0 × 256 + 1 = 1` justifie la correction, sans prendre la sortie du programme comme oracle.

Autre défaut précis dans la version brute : `testReadShortLE_withBufferUnderrun` configurait Mockito avec `thenReturn(0x12)`. Mockito répète cette valeur aux lectures suivantes ; le flux simulé n'atteint donc jamais la fin attendue par `assertThrows`. L'intention du test est pertinente, mais ses données simulées ne réalisent pas cette intention.

### Corrections manuelles des tests exportés

**Périmètre et comptage.** Les 18 fichiers exportés ont été conservés, avec les mêmes 71 méthodes de test. Onze méthodes ont reçu chacune une correction sémantique, réparties dans neuf fichiers : dix changements de valeur attendue et un changement de données simulées. Aucune assertion n'a été supprimée ou remplacée par une vérification moins précise ; aucune méthode de production ni aucun test original n'a été modifié. Les treize méthodes sans fichier exporté ne sont pas réparées dans cette étape : leurs tentatives restent archivées et ne sont pas comptées parmi les 71 tests validés.

Les versions brutes se trouvent dans [attempt-03/exported](chatunitest-local/attempt-03/exported), les copies corrigées dans [tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io). Le [manifeste des corrections](chatunitest-local/corrections/manifest.json) enregistre, pour chacun des neuf fichiers, les remplacements exacts et les empreintes SHA-256 avant/après. Aucun nouvel appel au modèle n'a été effectué pendant cette correction. Les modifications ont été appliquées avec l'assistance de Codex ; elles ne proviennent pas des réparations automatiques de ChatUniTest/Ollama.

**Construction des oracles.** Les noms, Javadocs et signatures d'`EndianUtils` définissent la largeur et l'ordre des octets. Un `short` occupe deux octets ; un entier non signé `UInt` occupe quatre octets, même si son résultat Java est un `long`. L'offset est un indice de tableau commençant à zéro. Pour des octets interprétés comme non signés `b0, b1, …`, BE16 vaut `256 × b0 + b1`, LE16 vaut `b0 + 256 × b1`, BE32 vaut `2^24 × b0 + 2^16 × b1 + 2^8 × b2 + b3`, et LE32 vaut `b0 + 2^8 × b1 + 2^16 × b2 + 2^24 × b3`. Tous les résultats 16 bits corrigés ici sont positifs et inférieurs à 32768 : aucune conversion de signe n'intervient.

1. **`EndianUtils_getShortBE_16_0_Test.testGetShortBEWithOffset`**
   - Intention : lire un entier signé 16 bits BE à un offset non nul.
   - Données conservées : `01 00 02 03`, offset `1`. Les octets avant et après la fenêtre lue rendent visible un décalage incorrect.
   - Correction : `assertEquals(258, result)` devient `assertEquals(0x0002, result)`.
   - Oracle : les indices 1 et 2 contiennent `00 02`, donc `0 × 256 + 2 = 2`. Les octets aux indices 0 et 3 ne participent pas à la valeur.

2. **`EndianUtils_getShortBE_17_0_Test.testGetShortBE`**
   - Intention : vérifier l'ordre BE avec un offset explicite de zéro.
   - Données conservées : `00 01`, offset `0`. Ces deux octets distinguent BE (1) de LE (256).
   - Correction : `short expected = 0x0100` devient `short expected = 0x0001`.
   - Oracle : `0 × 256 + 1 = 1`. L'ancien attendu correspondait à l'ordre LE.

3. **`EndianUtils_getUIntBE_26_0_Test.testGetUIntBE`**
   - Intention : lire un entier non signé 32 bits au début d'un tableau plus long que quatre octets.
   - Données conservées : `00 00 00 00 00 00 00 01`, sans offset explicite. L'octet non nul final vérifie qu'une donnée au-delà du premier entier ne doit pas être incluse.
   - Correction : `assertEquals(1L, result)` devient `assertEquals(0L, result)`.
   - Oracle : seuls les indices 0 à 3 sont lus ; ils valent tous zéro. La valeur 1 à l'indice 7 appartient à une autre fenêtre de quatre octets.

4. **`EndianUtils_getUIntBE_27_0_Test.testGetUIntBE`**
   - Intention : respecter l'offset lors d'une lecture BE32.
   - Données conservées : `00 00 00 01 00 00 00 00`, offset `4`. La première fenêtre est non nulle, tandis que la fenêtre sélectionnée est nulle.
   - Correction : `long expected = 1L` devient `long expected = 0L`.
   - Oracle : les indices 4 à 7 contiennent `00 00 00 00`, donc le résultat vaut zéro. L'ancien oracle utilisait à tort la fenêtre des indices 0 à 3.

5. **`EndianUtils_getUIntLE_24_0_Test.testGetUIntLE`**
   - Intention : lire exactement quatre octets depuis le début en LE32, même lorsque le tableau en contient huit.
   - Données conservées : `00 00 00 00 00 00 00 01`, sans offset explicite. L'octet final sert de témoin hors de la fenêtre lue.
   - Correction : `assertEquals(1L, result)` devient `assertEquals(0L, result)`.
   - Oracle : les quatre premiers octets sont nuls ; LE32 vaut donc zéro. Le type de retour `long` ne transforme pas la lecture en une lecture de huit octets.

6. **`EndianUtils_getUIntLE_24_0_Test.testGetUIntLEWithOffset`**
   - Intention : respecter à la fois l'offset et la position de l'octet de poids fort en LE32.
   - Données conservées : `00 00 00 00 00 00 00 01`, offset `4`. La fenêtre lue est `00 00 00 01`.
   - Correction : `assertEquals(1L, result)` devient `assertEquals(0x01000000L, result)`.
   - Oracle : en LE, le quatrième octet est multiplié par `2^24` ; `0 + 0 + 0 + 1 × 16777216 = 16777216`. L'ancien attendu correspondait à une lecture BE de cette fenêtre.

7. **`EndianUtils_getUIntLE_25_0_Test.testGetUIntLE`**
   - Intention : sélectionner la fenêtre LE32 qui commence à l'offset demandé.
   - Données conservées : `00 00 00 01 00 00 00 00`, offset `4`. L'octet non nul précède la fenêtre sélectionnée.
   - Correction : `long expected = 1L` devient `long expected = 0L`.
   - Oracle : les indices 4 à 7 sont tous nuls. Le résultat ne doit dépendre d'aucun octet situé avant l'offset.

8. **`EndianUtils_getUShortBE_18_0_Test.testGetUShortBE`**
   - Intention : décoder deux octets en entier non signé BE16.
   - Données conservées : `00 01`. La disposition asymétrique distingue les deux ordres d'octets.
   - Correction : `assertEquals(256, result)` devient `assertEquals(0x0001, result)`.
   - Oracle : `0 × 256 + 1 = 1`. La valeur 256 aurait été correcte pour `01 00` en BE ou `00 01` en LE.

9. **`EndianUtils_getUShortBE_18_0_Test.testGetUShortBEWithOffset`**
   - Intention : décoder BE16 à partir d'un offset non nul.
   - Données conservées : `00 01 02 03`, offset `1`. La fenêtre `01 02` distingue une erreur d'offset d'une inversion d'ordre.
   - Correction : `assertEquals(256, result)` devient `assertEquals(0x0102, result)`.
   - Oracle : `1 × 256 + 2 = 258`. Le premier octet du tableau (`00`) est hors de la fenêtre lue.

10. **`EndianUtils_getUShortLE_14_0_Test.testGetUShortLEWithOffset`**
    - Intention : décoder LE16 en respectant l'offset.
    - Données conservées : `01 02 03 04`, offset `1`. Les quatre octets distincts rendent détectable un décalage d'une position.
    - Correction : `assertEquals(0x0403, result)` devient `assertEquals(0x0302, result)`.
    - Oracle : les indices 1 et 2 contiennent `02 03` ; `2 + 3 × 256 = 770`. L'ancien attendu, 1027, correspondait aux indices 2 et 3, donc à l'offset 2.

11. **`EndianUtils_readShortLE_0_0_Test.testReadShortLE_withBufferUnderrun`**
    - Intention : exiger `BufferUnderrunException` lorsqu'un flux ne fournit qu'un octet pour une lecture de deux octets.
    - Données initiales : `when(mockInputStream.read()).thenReturn(0x12)`. Mockito répète le dernier résultat configuré ; les deux lectures retournaient donc `0x12`, ce qui ne simulait pas une fin de flux.
    - Correction : la séquence devient `thenReturn(0x12, -1)`. Le premier appel fournit un octet valide, le deuxième signale la fin du flux conformément au contrat d'`InputStream.read()`.
    - Oracle conservé : `assertThrows(BufferUnderrunException.class, ...)`. Le choix de `0x12` distingue bien un octet disponible de la sentinelle `-1`. L'exception est attendue parce que le deuxième octet requis est absent ; aucune valeur numérique de remplacement n'est acceptable.

**Limites conservées.** Les quatre corrections dont l'attendu devient zéro rétablissent des oracles exacts, mais ces cas ne détectent pas à eux seuls un mutant qui renvoie toujours zéro. Ils doivent être complétés par les autres tests à résultats non nuls et évalués par PIT. Les tests d'indices invalides sont utiles pour caractériser les exceptions, mais ne prouvent pas le bon ordre des octets. Plusieurs fichiers comportent des cas redondants ; ils ont été conservés pour ne pas modifier silencieusement la sortie du générateur au-delà des onze corrections recensées.

### Validation après correction

Validation locale réalisée le 28 septembre 2026, avec Java 21 et Maven, sans nouvelle génération :

| Exécution | Tests recensés | Réussis | Échecs | Erreurs | Ignorés | Résultat |
|---|---:|---:|---:|---:|---:|---|
| Tests générés bruts | 71 | 60 | 11 | 0 | 0 | `BUILD FAILURE` |
| Tests générés corrigés uniquement | 71 | 71 | 0 | 0 | 0 | `BUILD SUCCESS` |
| Tests originaux et générés corrigés | 820 | 818 | 0 | 0 | 2 | `BUILD SUCCESS` |

Les 820 tests correspondent aux 749 tests originaux, dont deux ignorés, et aux 71 tests générés. Le nombre de fichiers exportés reste 18 et les 71 annotations `@Test` sont conservées. La vérification des empreintes avant/après confirme exactement neuf fichiers modifiés et la conservation des originaux archivés.

Tests générés corrigés uniquement :

```bash
mvn -B -pl tika-core -Pchatunitest-verify test \
  '-Dtest=EndianUtils_*_Test' \
  -Drat.skip=true -Dcheckstyle.skip=true -Dossindex.skip=true -Dspotless.skip=true
```

Ensemble des tests de `tika-core`, avec les tests générés :

```bash
mvn -B -pl tika-core -Pchatunitest-verify test \
  -Drat.skip=true -Dcheckstyle.skip=true -Dossindex.skip=true
```

Preuves : [validation des 71 tests corrigés](chatunitest-local/logs/validation-corrected.log), [validation complète des 820 tests](chatunitest-local/logs/all-tests-after-corrections.log), [rapports Surefire des tests corrigés](chatunitest-local/corrections/surefire-reports), [bilan numérique](chatunitest-local/corrections/summary.json).

Ces commandes vérifient compilation et exécution ; les contrôles RAT, Checkstyle et OSS Index y sont désactivés, donc leur réussite n'est pas revendiquée. La validation complète n'a pas désactivé Spotless. La GitHub Action est configurée avec `chatunitest-verify` ; sa validation locale et la limite de vérification distante sont détaillées dans la section « Intégration continue ».

L'analyse de mutation avec ces **tests générés puis corrigés** est présentée dans la section suivante. Elle ne mesure pas une suite IA brute passant sans intervention. Les tests supplémentaires destinés aux mutants survivants sont documentés plus bas, séparément des corrections de tests générés.




<!-- PIT-COMPARISON-START -->
### Mutation après ajout des tests générés corrigés

Analyse exécutée le 28 septembre 2026. La suite comprend les tests originaux et les 71 tests générés **après les 11 corrections décrites ci-dessus**. À ce stade intermédiaire de la mesure, aucun test supplémentaire dédié aux mutants survivants n’avait été ajouté. Le score ne décrit donc ni les tests IA bruts seuls, ni une suite finale renforcée manuellement.

Commande exécutée depuis la racine :

```bash
mvn -B -pl tika-core -Pchatunitest-verify clean test-compile \
  org.pitest:pitest-maven:1.25.9:mutationCoverage \
  -DtargetClasses=org.apache.tika.io.EndianUtils \
  -Drat.skip=true -Dcheckstyle.skip=true -Dossindex.skip=true
```

Le `clean` élimine les anciennes classes compilées ; le profil ajoute explicitement les sources générées. PIT garde la même version 1.25.9, le connecteur JUnit 1.2.3, les opérateurs `DEFAULTS` et un thread. `MediaType` est exclue de cette commande. Aucun appel à Ollama n’est déclenché. Le rapport produit dans `tika-core/target/pit-reports` a été copié intégralement hors de `target`.

Preuves : [rapport HTML après](rapports-pit/endian-after-corrected/index.html), [XML après](rapports-pit/endian-after-corrected/mutations.xml), [journal Maven/PIT](chatunitest-local/logs/pit-after-corrected.log), [rapport initial](rapports-pit/endian-before/index.html).

| Mesure | Tests originaux | Originaux + générés corrigés |
|---|---:|---:|
| Mutants générés | 206 | 206 |
| `KILLED` | 36 | 103 |
| `SURVIVED` (exécutés mais non détectés) | 16 | 21 |
| `NO_COVERAGE` (non exécutés) | 154 | 82 |
| Autres statuts, dont erreurs et timeouts | 0 | 0 |
| Score global `KILLED / total` | 36/206 = 17,48 % | 103/206 = 50,00 % |
| Lignes couvertes par PIT | 31/121 = 25,62 % | 74/121 = 61,16 % |
| Force des tests parmi les mutants couverts | 36/52 = 69,23 % | 103/124 = 83,06 % |

**Gain : 67 mutants tués supplémentaires, soit +32,52 points de pourcentage.** Les tests ne détectent pas tous les mutants : 103 restent non tués (21 survivants et 82 non couverts). La hausse du nombre de survivants de 16 à 21 correspond à cinq mutants nouvellement exécutés, pas à une régression de mutants auparavant tués.

| Transition individuelle | Nombre |
|---|---:|
| `KILLED → KILLED` | 36 |
| `SURVIVED → SURVIVED` | 16 |
| `NO_COVERAGE → KILLED` | 67 |
| `NO_COVERAGE → SURVIVED` | 5 |
| `NO_COVERAGE → NO_COVERAGE` | 82 |

Les 16 survivants initiaux ne sont donc pas détectés par les tests ajoutés. Le gain porte exclusivement sur du code non couvert dans la première analyse.

#### Méthode de comparaison et attribution

Les 206 identités de mutants sont identiques dans les deux XML. La clé de comparaison combine la classe, la méthode, le descripteur JVM de surcharge, la ligne, le nom complet du mutateur et la liste des indices de bytecode. Les descriptions sont également vérifiées identiques. Une ligne peut contenir plusieurs mutations du même opérateur : les indices évitent de les confondre.

Les identifiants `M001` à `M206` ci-dessous sont des identifiants documentaires attribués par le [script de comparaison](chatunitest-local/compare-pit.py), pas des numéros fournis par PIT. Le [JSON de comparaison](rapports-pit/comparison-endian.json) conserve tous les champs XML, les deux statuts et les noms complets des tests. Reproduction : `python3 chatunitest-local/compare-pit.py`.

Le champ `killingTest` identifie le test enregistré par PIT pour tuer un mutant. Il ne prouve pas que ce test soit le seul capable de le tuer. Les explications ci-dessous sont déduites du code du test et de la mutation ; le statut `KILLED` et l’attribution du test proviennent du rapport mesuré. Aucun mutant n’a été supprimé du dénominateur.

#### Tests tueurs et oracles

Les 67 nouveaux mutants tués sont attribués à 17 méthodes de test générées. Chaque identifiant T renvoie à un test exact du package `org.apache.tika.io` et à ses données/oracles :

- **T01 — [EndianUtils_getIntBE_23_0_Test.testGetIntBE()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_getIntBE_23_0_Test.java)** : Tableau `01 02 03 04`, offset 0 : `assertEquals(0x01020304, result)` (16909060). Les quatre octets distincts et non nuls contrôlent leur position et leur poids en BE32.
- **T02 — [EndianUtils_getIntLE_20_0_Test.testGetIntLE()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_getIntLE_20_0_Test.java)** : Tableau `01 02 03 04`, sans offset : attendu `0x04030201` (67305985), somme des octets pondérés par 1, 256, 65536 et 16777216.
- **T03 — [EndianUtils_getIntLE_20_0_Test.testGetIntLEWithTooShortArray()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_getIntLE_20_0_Test.java)** : Tableau `01 02` : `assertThrows(ArrayIndexOutOfBoundsException.class, ...)`, car quatre octets sont nécessaires. Le mutant de l’incrément à la ligne 360 lit les indices 0, 1, 0, 1 au lieu de 0, 1, 2, 3 : aucune exception n’est alors levée, ce qui fait échouer l’oracle.
- **T04 — [EndianUtils_getIntLE_21_0_Test.testGetIntLE()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_getIntLE_21_0_Test.java)** : Tableau `01 02 03 04 05 06 07 08`, offset 0 : attendu `0x04030201`. Seuls les quatre premiers octets contribuent à la valeur LE32.
- **T05 — [EndianUtils_getLongLE_28_0_Test.testGetLongLE()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_getLongLE_28_0_Test.java)** : Tableau `01 02 03 04 05 06 07 08`, offset 0 : attendu `0x0807060504030201L`. Chaque octet occupe sa position dans le résultat LE64 ; supprimer un tour, inverser un décalage ou changer la combinaison des octets altère cette valeur.
- **T06 — [EndianUtils_getShortBE_16_0_Test.testGetShortBE()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_getShortBE_16_0_Test.java)** : Tableau `00 01`, sans offset : attendu 1. Ce résultat non nul détecte un retour forcé à zéro dans la surcharge appelée et dans celle à laquelle elle délègue.
- **T07 — [EndianUtils_getShortLE_12_0_Test.testGetShortLE()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_getShortLE_12_0_Test.java)** : Tableau `12 34`, sans offset : attendu `0x3412` (13330), soit 18 + 52 × 256.
- **T08 — [EndianUtils_getShortLE_12_0_Test.testGetShortLEWithOffset()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_getShortLE_12_0_Test.java)** : Tableau `00 12 34 56`, offset 1 : attendu `0x3412` (13330). La fenêtre `12 34` impose le bon offset et les bons poids ; le test traverse aussi `getUShortLE`.
- **T09 — [EndianUtils_getUIntBE_26_0_Test.testGetUIntBEWithOffset()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_getUIntBE_26_0_Test.java)** : Tableau `00 00 00 00 00 00 00 01`, offset 4 : attendu `1L`. Un retour zéro échoue ; remplacer le masque AND par OR produit `4294967295L`, également différent de 1.
- **T10 — [EndianUtils_getUIntLE_24_0_Test.testGetUIntLEWithOffset()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_getUIntLE_24_0_Test.java)** : Tableau `00 00 00 00 00 00 00 01`, offset 4 : attendu corrigé `0x01000000L` (16777216). Le retour forcé à zéro diffère de ce résultat non nul.
- **T11 — [EndianUtils_getUIntLE_25_0_Test.testGetUIntLEWithZeroValues()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_getUIntLE_25_0_Test.java)** : Huit octets nuls, offset 0 : attendu `0L`. `0 & 0xFFFFFFFFL` vaut 0, mais `0 | 0xFFFFFFFFL` vaut 4294967295 : un oracle nul est pertinent pour ce mutant de masque.
- **T12 — [EndianUtils_getUShortBE_18_0_Test.testGetUShortBE()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_getUShortBE_18_0_Test.java)** : Tableau `00 01` : attendu corrigé `0x0001`. Les retours zéro, le changement du masque, de l’offset ou de l’addition finale ne conservent pas ce résultat.
- **T13 — [EndianUtils_getUShortBE_19_0_Test.testGetUShortBEWithNegativeValues()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_getUShortBE_19_0_Test.java)** : Octets Java `(byte)0xFF` et `(byte)0xFE`, offset 0 : attendu `0xFFFE` (65534). Les valeurs non signées 255 et 254 donnent 255 × 256 + 254 ; remplacer `255 << 8` par `255 >> 8` ramène le résultat à 254.
- **T14 — [EndianUtils_getUShortLE_14_0_Test.testGetUShortLE()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_getUShortLE_14_0_Test.java)** : Tableau `01 02` : attendu `0x0201` (513), donc différent du zéro imposé par le mutant de retour de la surcharge sans offset.
- **T15 — [EndianUtils_readShortLE_0_0_Test.testReadShortLE_withBufferUnderrun()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_readShortLE_0_0_Test.java)** : Séquence corrigée `0x12`, `-1` : `assertThrows(BufferUnderrunException.class, ...)`. `18 | -1` vaut -1, tandis que `18 & -1` vaut 18 : le mutant AND masque la fin de flux. Supprimer le contrôle empêche aussi l’exception ; dans les deux cas, l’oracle échoue.
- **T16 — [EndianUtils_readShortLE_0_0_Test.testReadShortLE_withValidData()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_readShortLE_0_0_Test.java)** : Mockito renvoie successivement `0x12`, `0x34` : attendu `0x3412` (13330). La lecture délègue à `readUShortLE` ; des poids ou signes arithmétiques modifiés et les retours zéro font échouer cette égalité.
- **T17 — [EndianUtils_ubyteToInt_29_0_Test.testUbyteToInt()](tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/EndianUtils_ubyteToInt_29_0_Test.java)** : Assertions successives : 10 → 10, -10 → 246, `(byte)0xFF` → 255, 0 → 0. Dès la première assertion, AND→OR donne 255 au lieu de 10 ; le retour forcé à zéro donne 0 au lieu de 10.

#### Les 67 mutants nouvellement tués

Tous passent de `NO_COVERAGE` à `KILLED`. Les noms courts des mutateurs ci-dessous sont les suffixes exacts de leurs noms complets conservés dans le JSON/XML. Chaque ligne associe l’emplacement, l’opérateur, le test tueur et la raison de la détection.

| ID | Méthode, ligne, index bytecode | Mutateur et changement | Test | Pourquoi il le tue |
|---|---|---|---|---|
| M001 | `readShortLE(Ljava/io/InputStream;)S`, L45, i=6 | `PrimitiveReturnsMutator` : replaced short return with 0 for org/apache/tika/io/EndianUtils::readShortLE | T16 | Le zéro imposé diffère de l’attendu non nul. |
| M004 | `readUShortLE(Ljava/io/InputStream;)I`, L64, i=15 | `MathMutator` : Replaced bitwise OR with AND | T15 | La fin de flux ne déclenche plus l’exception exigée. |
| M005 | `readUShortLE(Ljava/io/InputStream;)I`, L64, i=16 | `RemoveConditionalMutator_ORDER_ELSE` : removed conditional - replaced comparison check with false | T15 | La fin de flux ne déclenche plus l’exception exigée. |
| M006 | `readUShortLE(Ljava/io/InputStream;)I`, L67, i=28 | `MathMutator` : Replaced Shift Left with Shift Right | T16 | Le décalage droit supprime le poids attendu de l’octet. |
| M007 | `readUShortLE(Ljava/io/InputStream;)I`, L67, i=30 | `MathMutator` : Replaced integer addition with subtraction | T16 | Une contribution d’octet non nulle est soustraite au lieu d’être ajoutée. |
| M008 | `readUShortLE(Ljava/io/InputStream;)I`, L67, i=31 | `PrimitiveReturnsMutator` : replaced int return with 0 for org/apache/tika/io/EndianUtils::readUShortLE | T16 | Le zéro imposé diffère de l’attendu non nul. |
| M139 | `getShortLE([B)S`, L259, i=6 | `PrimitiveReturnsMutator` : replaced short return with 0 for org/apache/tika/io/EndianUtils::getShortLE | T07 | Le zéro imposé diffère de l’attendu non nul. |
| M140 | `getShortLE([BI)S`, L270, i=7 | `PrimitiveReturnsMutator` : replaced short return with 0 for org/apache/tika/io/EndianUtils::getShortLE | T08 | Le zéro imposé diffère de l’attendu non nul. |
| M141 | `getUShortLE([B)I`, L280, i=6 | `PrimitiveReturnsMutator` : replaced int return with 0 for org/apache/tika/io/EndianUtils::getUShortLE | T14 | Le zéro imposé diffère de l’attendu non nul. |
| M142 | `getUShortLE([BI)I`, L291, i=7 | `MathMutator` : Replaced bitwise AND with OR | T08 | Le masque OR force des bits à 1 et altère l’octet décodé. |
| M143 | `getUShortLE([BI)I`, L292, i=14 | `MathMutator` : Replaced integer addition with subtraction | T08 | offset+1 devient offset-1 : mauvais octet ou indice négatif. |
| M144 | `getUShortLE([BI)I`, L292, i=17 | `MathMutator` : Replaced bitwise AND with OR | T08 | Le masque OR force des bits à 1 et altère l’octet décodé. |
| M145 | `getUShortLE([BI)I`, L293, i=23 | `MathMutator` : Replaced Shift Left with Shift Right | T08 | Le décalage droit supprime le poids attendu de l’octet. |
| M146 | `getUShortLE([BI)I`, L293, i=25 | `MathMutator` : Replaced integer addition with subtraction | T08 | Une contribution d’octet non nulle est soustraite au lieu d’être ajoutée. |
| M147 | `getUShortLE([BI)I`, L293, i=26 | `PrimitiveReturnsMutator` : replaced int return with 0 for org/apache/tika/io/EndianUtils::getUShortLE | T08 | Le zéro imposé diffère de l’attendu non nul. |
| M148 | `getShortBE([B)S`, L303, i=6 | `PrimitiveReturnsMutator` : replaced short return with 0 for org/apache/tika/io/EndianUtils::getShortBE | T06 | Le zéro imposé diffère de l’attendu non nul. |
| M149 | `getShortBE([BI)S`, L314, i=7 | `PrimitiveReturnsMutator` : replaced short return with 0 for org/apache/tika/io/EndianUtils::getShortBE | T06 | Le zéro imposé diffère de l’attendu non nul. |
| M150 | `getUShortBE([B)I`, L324, i=6 | `PrimitiveReturnsMutator` : replaced int return with 0 for org/apache/tika/io/EndianUtils::getUShortBE | T12 | Le zéro imposé diffère de l’attendu non nul. |
| M151 | `getUShortBE([BI)I`, L335, i=7 | `MathMutator` : Replaced bitwise AND with OR | T12 | Le masque OR force des bits à 1 et altère l’octet décodé. |
| M152 | `getUShortBE([BI)I`, L336, i=14 | `MathMutator` : Replaced integer addition with subtraction | T12 | offset+1 devient offset-1 : mauvais octet ou indice négatif. |
| M153 | `getUShortBE([BI)I`, L336, i=17 | `MathMutator` : Replaced bitwise AND with OR | T12 | Le masque OR force des bits à 1 et altère l’octet décodé. |
| M154 | `getUShortBE([BI)I`, L337, i=23 | `MathMutator` : Replaced Shift Left with Shift Right | T13 | Le décalage droit supprime le poids attendu de l’octet. |
| M155 | `getUShortBE([BI)I`, L337, i=25 | `MathMutator` : Replaced integer addition with subtraction | T12 | Une contribution d’octet non nulle est soustraite au lieu d’être ajoutée. |
| M156 | `getUShortBE([BI)I`, L337, i=26 | `PrimitiveReturnsMutator` : replaced int return with 0 for org/apache/tika/io/EndianUtils::getUShortBE | T12 | Le zéro imposé diffère de l’attendu non nul. |
| M157 | `getIntLE([B)I`, L347, i=6 | `PrimitiveReturnsMutator` : replaced int return with 0 for org/apache/tika/io/EndianUtils::getIntLE | T02 | Le zéro imposé diffère de l’attendu non nul. |
| M158 | `getIntLE([BI)I`, L359, i=9 | `IncrementsMutator` : Changed increment from 1 to -1 | T04 | Un indice suivant recule : octet incorrect ou exception inattendue. |
| M159 | `getIntLE([BI)I`, L359, i=12 | `MathMutator` : Replaced bitwise AND with OR | T04 | Le masque OR force des bits à 1 et altère l’octet décodé. |
| M160 | `getIntLE([BI)I`, L360, i=18 | `IncrementsMutator` : Changed increment from 1 to -1 | T03 | Les indices 0,1,0,1 restent valides : l’exception exigée disparaît. |
| M161 | `getIntLE([BI)I`, L360, i=21 | `MathMutator` : Replaced bitwise AND with OR | T04 | Le masque OR force des bits à 1 et altère l’octet décodé. |
| M162 | `getIntLE([BI)I`, L361, i=27 | `IncrementsMutator` : Changed increment from 1 to -1 | T04 | Un indice suivant recule : octet incorrect ou exception inattendue. |
| M163 | `getIntLE([BI)I`, L361, i=30 | `MathMutator` : Replaced bitwise AND with OR | T04 | Le masque OR force des bits à 1 et altère l’octet décodé. |
| M165 | `getIntLE([BI)I`, L362, i=39 | `MathMutator` : Replaced bitwise AND with OR | T04 | Le masque OR force des bits à 1 et altère l’octet décodé. |
| M166 | `getIntLE([BI)I`, L363, i=45 | `MathMutator` : Replaced Shift Left with Shift Right | T04 | Le décalage droit supprime le poids attendu de l’octet. |
| M167 | `getIntLE([BI)I`, L363, i=48 | `MathMutator` : Replaced Shift Left with Shift Right | T04 | Le décalage droit supprime le poids attendu de l’octet. |
| M168 | `getIntLE([BI)I`, L363, i=49 | `MathMutator` : Replaced integer addition with subtraction | T04 | Une contribution d’octet non nulle est soustraite au lieu d’être ajoutée. |
| M169 | `getIntLE([BI)I`, L363, i=52 | `MathMutator` : Replaced Shift Left with Shift Right | T04 | Le décalage droit supprime le poids attendu de l’octet. |
| M170 | `getIntLE([BI)I`, L363, i=53 | `MathMutator` : Replaced integer addition with subtraction | T04 | Une contribution d’octet non nulle est soustraite au lieu d’être ajoutée. |
| M171 | `getIntLE([BI)I`, L363, i=55 | `MathMutator` : Replaced integer addition with subtraction | T04 | Une contribution d’octet non nulle est soustraite au lieu d’être ajoutée. |
| M172 | `getIntLE([BI)I`, L363, i=56 | `PrimitiveReturnsMutator` : replaced int return with 0 for org/apache/tika/io/EndianUtils::getIntLE | T04 | Le zéro imposé diffère de l’attendu non nul. |
| M174 | `getIntBE([BI)I`, L385, i=9 | `IncrementsMutator` : Changed increment from 1 to -1 | T01 | Un indice suivant recule : octet incorrect ou exception inattendue. |
| M175 | `getIntBE([BI)I`, L385, i=12 | `MathMutator` : Replaced bitwise AND with OR | T01 | Le masque OR force des bits à 1 et altère l’octet décodé. |
| M176 | `getIntBE([BI)I`, L386, i=18 | `IncrementsMutator` : Changed increment from 1 to -1 | T01 | Un indice suivant recule : octet incorrect ou exception inattendue. |
| M177 | `getIntBE([BI)I`, L386, i=21 | `MathMutator` : Replaced bitwise AND with OR | T01 | Le masque OR force des bits à 1 et altère l’octet décodé. |
| M178 | `getIntBE([BI)I`, L387, i=27 | `IncrementsMutator` : Changed increment from 1 to -1 | T01 | Un indice suivant recule : octet incorrect ou exception inattendue. |
| M179 | `getIntBE([BI)I`, L387, i=30 | `MathMutator` : Replaced bitwise AND with OR | T01 | Le masque OR force des bits à 1 et altère l’octet décodé. |
| M181 | `getIntBE([BI)I`, L388, i=39 | `MathMutator` : Replaced bitwise AND with OR | T01 | Le masque OR force des bits à 1 et altère l’octet décodé. |
| M182 | `getIntBE([BI)I`, L389, i=45 | `MathMutator` : Replaced Shift Left with Shift Right | T01 | Le décalage droit supprime le poids attendu de l’octet. |
| M183 | `getIntBE([BI)I`, L389, i=48 | `MathMutator` : Replaced Shift Left with Shift Right | T01 | Le décalage droit supprime le poids attendu de l’octet. |
| M184 | `getIntBE([BI)I`, L389, i=49 | `MathMutator` : Replaced integer addition with subtraction | T01 | Une contribution d’octet non nulle est soustraite au lieu d’être ajoutée. |
| M185 | `getIntBE([BI)I`, L389, i=52 | `MathMutator` : Replaced Shift Left with Shift Right | T01 | Le décalage droit supprime le poids attendu de l’octet. |
| M186 | `getIntBE([BI)I`, L389, i=53 | `MathMutator` : Replaced integer addition with subtraction | T01 | Une contribution d’octet non nulle est soustraite au lieu d’être ajoutée. |
| M187 | `getIntBE([BI)I`, L389, i=55 | `MathMutator` : Replaced integer addition with subtraction | T01 | Une contribution d’octet non nulle est soustraite au lieu d’être ajoutée. |
| M188 | `getIntBE([BI)I`, L389, i=56 | `PrimitiveReturnsMutator` : replaced int return with 0 for org/apache/tika/io/EndianUtils::getIntBE | T01 | Le zéro imposé diffère de l’attendu non nul. |
| M190 | `getUIntLE([BI)J`, L411, i=12 | `MathMutator` : Replaced bitwise AND with OR | T11 | Le masque OR force les 32 bits bas à 1 au lieu de préserver la valeur. |
| M191 | `getUIntLE([BI)J`, L411, i=13 | `PrimitiveReturnsMutator` : replaced long return with 0 for org/apache/tika/io/EndianUtils::getUIntLE | T10 | Le zéro imposé diffère de l’attendu non nul. |
| M193 | `getUIntBE([BI)J`, L433, i=12 | `MathMutator` : Replaced bitwise AND with OR | T09 | Le masque OR force les 32 bits bas à 1 au lieu de préserver la valeur. |
| M194 | `getUIntBE([BI)J`, L433, i=13 | `PrimitiveReturnsMutator` : replaced long return with 0 for org/apache/tika/io/EndianUtils::getUIntBE | T09 | Le zéro imposé diffère de l’attendu non nul. |
| M195 | `getLongLE([BI)J`, L446, i=17 | `ConditionalsBoundaryMutator` : changed conditional boundary | T05 | Le passage de >= à > omet l’octet à l’offset 0 (valeur 01). |
| M196 | `getLongLE([BI)J`, L446, i=9 | `MathMutator` : Replaced integer addition with subtraction | T05 | Départ j=-9 : la boucle est sautée, résultat zéro. |
| M197 | `getLongLE([BI)J`, L446, i=11 | `MathMutator` : Replaced integer subtraction with addition | T05 | Départ j=9 : accès hors du tableau de huit octets. |
| M198 | `getLongLE([BI)J`, L446, i=17 | `RemoveConditionalMutator_ORDER_ELSE` : removed conditional - replaced comparison check with false | T05 | La boucle ne s’exécute plus et laisse le résultat à zéro. |
| M199 | `getLongLE([BI)J`, L447, i=22 | `MathMutator` : Replaced Shift Left with Shift Right | T05 | Le décalage droit supprime le poids attendu de l’octet. |
| M200 | `getLongLE([BI)J`, L448, i=31 | `MathMutator` : Replaced bitwise AND with OR | T05 | Le masque OR force des bits à 1 et altère l’octet décodé. |
| M201 | `getLongLE([BI)J`, L448, i=33 | `MathMutator` : Replaced bitwise OR with AND | T05 | Le AND efface les bits au lieu d’assembler les octets. |
| M202 | `getLongLE([BI)J`, L450, i=43 | `PrimitiveReturnsMutator` : replaced long return with 0 for org/apache/tika/io/EndianUtils::getLongLE | T05 | Le zéro imposé diffère de l’attendu non nul. |
| M203 | `ubyteToInt(B)I`, L461, i=5 | `MathMutator` : Replaced bitwise AND with OR | T17 | Le masque OR force des bits à 1 et altère l’octet décodé. |
| M204 | `ubyteToInt(B)I`, L461, i=6 | `PrimitiveReturnsMutator` : replaced int return with 0 for org/apache/tika/io/EndianUtils::ubyteToInt | T17 | Le zéro imposé diffère de l’attendu non nul. |

#### Les 21 mutants survivants

| ID | Méthode et ligne | Mutateur, index et changement | Statut initial | Diagnostic |
|---|---|---|---|---|
| M003 | `readUShortLE(Ljava/io/InputStream;)I`, L64 | `ConditionalsBoundaryMutator`, i=16 : changed conditional boundary | `NO_COVERAGE` | La frontière `< 0` devient `<= 0` ; aucun cas fourni ne lit deux octets nuls. Un flux `00 00` doit retourner zéro sans exception. |
| M015 | `readUIntLE(Ljava/io/InputStream;)J`, L92 | `ConditionalsBoundaryMutator`, i=30 : changed conditional boundary | `SURVIVED` | Le contrôle EOF nécessite des cas aux frontières (octets valides nuls et chaque position de troncature). Les entrées actuelles ne distinguent pas cette mutation. Pour readUIntBE, le cas court original appelle à tort readUIntLE. |
| M016 | `readUIntLE(Ljava/io/InputStream;)J`, L92 | `MathMutator`, i=25 : Replaced bitwise OR with AND | `SURVIVED` | Le contrôle EOF nécessite des cas aux frontières (octets valides nuls et chaque position de troncature). Les entrées actuelles ne distinguent pas cette mutation. Pour readUIntBE, le cas court original appelle à tort readUIntLE. |
| M017 | `readUIntLE(Ljava/io/InputStream;)J`, L92 | `MathMutator`, i=27 : Replaced bitwise OR with AND | `SURVIVED` | Le contrôle EOF nécessite des cas aux frontières (octets valides nuls et chaque position de troncature). Les entrées actuelles ne distinguent pas cette mutation. Pour readUIntBE, le cas court original appelle à tort readUIntLE. |
| M028 | `readUIntBE(Ljava/io/InputStream;)J`, L111 | `ConditionalsBoundaryMutator`, i=30 : changed conditional boundary | `SURVIVED` | Le contrôle EOF nécessite des cas aux frontières (octets valides nuls et chaque position de troncature). Les entrées actuelles ne distinguent pas cette mutation. Pour readUIntBE, le cas court original appelle à tort readUIntLE. |
| M029 | `readUIntBE(Ljava/io/InputStream;)J`, L111 | `MathMutator`, i=25 : Replaced bitwise OR with AND | `SURVIVED` | Le contrôle EOF nécessite des cas aux frontières (octets valides nuls et chaque position de troncature). Les entrées actuelles ne distinguent pas cette mutation. Pour readUIntBE, le cas court original appelle à tort readUIntLE. |
| M030 | `readUIntBE(Ljava/io/InputStream;)J`, L111 | `MathMutator`, i=27 : Replaced bitwise OR with AND | `SURVIVED` | Le contrôle EOF nécessite des cas aux frontières (octets valides nuls et chaque position de troncature). Les entrées actuelles ne distinguent pas cette mutation. Pour readUIntBE, le cas court original appelle à tort readUIntLE. |
| M031 | `readUIntBE(Ljava/io/InputStream;)J`, L111 | `MathMutator`, i=29 : Replaced bitwise OR with AND | `SURVIVED` | Le contrôle EOF nécessite des cas aux frontières (octets valides nuls et chaque position de troncature). Les entrées actuelles ne distinguent pas cette mutation. Pour readUIntBE, le cas court original appelle à tort readUIntLE. |
| M032 | `readUIntBE(Ljava/io/InputStream;)J`, L111 | `RemoveConditionalMutator_ORDER_ELSE`, i=30 : removed conditional - replaced comparison check with false | `SURVIVED` | Le contrôle EOF nécessite des cas aux frontières (octets valides nuls et chaque position de troncature). Les entrées actuelles ne distinguent pas cette mutation. Pour readUIntBE, le cas court original appelle à tort readUIntLE. |
| M065 | `readIntME(Ljava/io/InputStream;)I`, L168 | `ConditionalsBoundaryMutator`, i=30 : changed conditional boundary | `SURVIVED` | Le contrôle EOF nécessite des cas aux frontières (octets valides nuls et chaque position de troncature). Les entrées actuelles ne distinguent pas cette mutation. Pour readUIntBE, le cas court original appelle à tort readUIntLE. |
| M066 | `readIntME(Ljava/io/InputStream;)I`, L168 | `MathMutator`, i=25 : Replaced bitwise OR with AND | `SURVIVED` | Le contrôle EOF nécessite des cas aux frontières (octets valides nuls et chaque position de troncature). Les entrées actuelles ne distinguent pas cette mutation. Pour readUIntBE, le cas court original appelle à tort readUIntLE. |
| M067 | `readIntME(Ljava/io/InputStream;)I`, L168 | `MathMutator`, i=27 : Replaced bitwise OR with AND | `SURVIVED` | Le contrôle EOF nécessite des cas aux frontières (octets valides nuls et chaque position de troncature). Les entrées actuelles ne distinguent pas cette mutation. Pour readUIntBE, le cas court original appelle à tort readUIntLE. |
| M125 | `readUE7(Ljava/io/InputStream;)J`, L235 | `ConditionalsBoundaryMutator`, i=21 : changed conditional boundary | `SURVIVED` | Les tests existants décodent des valeurs valides sur 1 à 3 octets. Examiner séparément EOF, octet terminal nul, limite de six octets et incrément du compteur ; l’équivalence de ces mutants n’est pas établie ici. |
| M126 | `readUE7(Ljava/io/InputStream;)J`, L235 | `ConditionalsBoundaryMutator`, i=25 : changed conditional boundary | `SURVIVED` | Les tests existants décodent des valeurs valides sur 1 à 3 octets. Examiner séparément EOF, octet terminal nul, limite de six octets et incrément du compteur ; l’équivalence de ces mutants n’est pas établie ici. |
| M127 | `readUE7(Ljava/io/InputStream;)J`, L235 | `IncrementsMutator`, i=23 : Changed increment from 1 to -1 | `SURVIVED` | Les tests existants décodent des valeurs valides sur 1 à 3 octets. Examiner séparément EOF, octet terminal nul, limite de six octets et incrément du compteur ; l’équivalence de ces mutants n’est pas établie ici. |
| M136 | `readUE7(Ljava/io/InputStream;)J`, L246 | `ConditionalsBoundaryMutator`, i=64 : changed conditional boundary | `SURVIVED` | Les tests existants décodent des valeurs valides sur 1 à 3 octets. Examiner séparément EOF, octet terminal nul, limite de six octets et incrément du compteur ; l’équivalence de ces mutants n’est pas établie ici. |
| M137 | `readUE7(Ljava/io/InputStream;)J`, L246 | `RemoveConditionalMutator_ORDER_ELSE`, i=64 : removed conditional - replaced comparison check with false | `SURVIVED` | Les tests existants décodent des valeurs valides sur 1 à 3 octets. Examiner séparément EOF, octet terminal nul, limite de six octets et incrément du compteur ; l’équivalence de ces mutants n’est pas établie ici. |
| M164 | `getIntLE([BI)I`, L362 | `IncrementsMutator`, i=36 : Changed increment from 1 to -1 | `NO_COVERAGE` | Équivalence déduite du code : dernier `i++` dans `data[i++]`. La valeur d’indice utilisée est la même et i n’est plus lu ensuite. Le résultat/exception observable reste identique ; PIT le classe néanmoins SURVIVED. |
| M180 | `getIntBE([BI)I`, L388 | `IncrementsMutator`, i=36 : Changed increment from 1 to -1 | `NO_COVERAGE` | Équivalence déduite du code : dernier `i++` dans `data[i++]`. La valeur d’indice utilisée est la même et i n’est plus lu ensuite. Le résultat/exception observable reste identique ; PIT le classe néanmoins SURVIVED. |
| M189 | `getUIntLE([B)J`, L399 | `PrimitiveReturnsMutator`, i=6 : replaced long return with 0 for org/apache/tika/io/EndianUtils::getUIntLE | `NO_COVERAGE` | La surcharge sans offset n’est appelée qu’avec quatre premiers octets nuls dans ces nouveaux tests. Ajouter un résultat attendu non nul pour cette surcharge, sans se limiter à la surcharge avec offset. |
| M192 | `getUIntBE([B)J`, L421 | `PrimitiveReturnsMutator`, i=6 : replaced long return with 0 for org/apache/tika/io/EndianUtils::getUIntBE | `NO_COVERAGE` | La surcharge sans offset n’est appelée qu’avec quatre premiers octets nuls dans ces nouveaux tests. Ajouter un résultat attendu non nul pour cette surcharge, sans se limiter à la surcharge avec offset. |

Les deux incréments locaux ci-dessus sont considérés équivalents par analyse du code, mais restent inclus dans le score brut 103/206. Les autres diagnostics sont des pistes pour l’étape de tests supplémentaires, pas des preuves qu’un nouveau test a déjà tué ces mutants.

#### Les 82 mutants non couverts

Le statut `NO_COVERAGE` signifie que PIT n’a pas pu exercer le code muté avec la suite sélectionnée. Il ne s’agit pas d’une exception à ignorer ni d’une preuve d’équivalence. La liste ci-dessous couvre les 82 identifiants, regroupés par surcharge.

| Méthode (descripteur JVM) | Nombre | Mutants |
|---|---:|---|
| `getIntBE([B)I` | 1 | M173 |
| `getUByte([BI)S` | 2 | M205, M206 |
| `readIntBE(Ljava/io/InputStream;)I` | 12 | M053, M054, M055, M056, M057, M058, M059, M060, M061, M062, M063, M064 |
| `readIntLE(Ljava/io/InputStream;)I` | 12 | M041, M042, M043, M044, M045, M046, M047, M048, M049, M050, M051, M052 |
| `readLongBE(Ljava/io/InputStream;)J` | 24 | M101, M102, M103, M104, M105, M106, M107, M108, M109, M110, M111, M112, M113, M114, M115, M116, M117, M118, M119, M120, M121, M122, M123, M124 |
| `readLongLE(Ljava/io/InputStream;)J` | 24 | M077, M078, M079, M080, M081, M082, M083, M084, M085, M086, M087, M088, M089, M090, M091, M092, M093, M094, M095, M096, M097, M098, M099, M100 |
| `readShortBE(Ljava/io/InputStream;)S` | 1 | M002 |
| `readUShortBE(Ljava/io/InputStream;)I` | 6 | M009, M010, M011, M012, M013, M014 |

Les méthodes de lecture longue, plusieurs lectures signées et les variantes BE de lecture courte restent sans couverture de mutation. `getIntBE(byte[])` n’est pas exercée bien que `getIntBE(byte[], int)` le soit ; couvrir une surcharge ne garantit pas de couvrir la surcharge qui délègue. `getUByte` n’a pas produit de test exporté. Les treize échecs de génération contribuent à expliquer ces lacunes, sans établir une correspondance un pour un : un test peut atteindre indirectement plusieurs méthodes.

Cette comparaison intermédiaire s’arrête à la suite originaux + générés corrigés. L’étape suivante, réalisée et documentée ci-dessous, distingue les cas non couverts des survivants couverts et conserve un rapport PIT final séparé.
<!-- PIT-COMPARISON-END -->

<!-- MANUAL-TESTS-START -->
### Tests supplémentaires après analyse des mutants

Les tests de cette étape sont dans [EndianUtilsManualTest.java](tika-core/src/test/java/org/apache/tika/io/EndianUtilsManualTest.java), distincts des fichiers produits par ChatUniTest et des tests originaux. Ils ont été conçus et ajoutés avec l’assistance de Codex, **hors du pipeline ChatUniTest/Ollama** : les présenter comme des tests rédigés sans assistance IA serait inexact. Ils constituent ici la suite supplémentaire guidée par l’analyse des mutants.

Aucune classe de production ni aucun test original n’a été modifié. Les 71 tests générés restent à leur état corrigé précédemment documenté. Les 17 nouveaux tests ne remplacent aucun test existant.

#### Itérations observées

- Premier passage : 15 tests supplémentaires, suite de 835 tests dont 2 ignorés, zéro échec/erreur. PIT tue 181/206 mutants (87,86 %), avec 25 survivants et aucun non couvert. [Rapport intermédiaire](rapports-pit/endian-manual-first-pass/index.html), [journal](chatunitest-local/logs/pit-manual-first-pass.log).
- Deuxième passage : ajout de `testReadUE7ZeroAfterContinuation` et `testFixedWidthRejectsEofEvenIfFileGrows`. Suite de 837 tests dont 2 ignorés, zéro échec/erreur ; PIT tue 204/206 mutants (99,03 %), avec deux survivants et aucun non couvert.

Le premier passage utilisait des flux statiques : dès EOF, toutes les lectures suivantes renvoyaient -1. Cela masquait plusieurs mutations OR→AND précoces : un OR ultérieur avec -1 rétablissait le contrôle d’erreur. Le test avec un fichier qui s’allonge retire précisément cette hypothèse. Le contrat de [InputStream.read()](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/InputStream.html#read()) fournit un octet entre 0 et 255 ou -1 à la fin du flux ; notre wrapper transmet la valeur réellement renvoyée par FileInputStream et modifie le fichier entre deux lectures. La détection a été mesurée avec ce test, et non déduite d’un mock arbitraire.

#### Catalogue des 17 tests : intention, données et oracle

Les tests collectifs de largeur fixe utilisent : readShortLE, readShortBE, readUShortLE et readUShortBE (2 octets) ; readUIntLE, readUIntBE, readIntLE, readIntBE et readIntME (4 octets) ; readLongLE et readLongBE (8 octets). Les cas de troncature couvrent donc 4×2 + 5×4 + 2×8 = 44 combinaisons. JUnit compte chaque boucle comme une seule méthode de test ; les messages d’assertion indiquent le lecteur et la longueur/position défaillante.

**H01 — `testReadShortBEValues`**

- Intention : Décoder un entier signé BE16 à partir d’un flux.
- Données et motivation : `12 34` distingue les poids des deux octets ; `FF FE` vérifie l’interprétation signée.
- Oracle et raison de la détection : `0x1234` = 4660 pour le premier flux ; le motif 16 bits `0xFFFE` vaut -2 en complément à deux. Ce test traverse aussi readUShortBE.
- Nouveaux mutants tués attribués par PIT : 4 (M002, M012, M013, M014).

**H02 — `testReadUnsignedShortBEValues`**

- Intention : Vérifier directement la variante non signée BE16.
- Données et motivation : Les mêmes flux `12 34` et `FF FE` distinguent la variante non signée de readShortBE.
- Oracle et raison de la détection : Résultats 4660 et 65534 : les octets représentent 18 × 256 + 52 et 255 × 256 + 254. PIT peut retenir le test précédent comme tueur des mêmes mutants ; aucune détection supplémentaire ne lui est attribuée dans ce passage.
- Nouveaux mutants tués attribués par PIT : 0. Aucun mutant supplémentaire ne lui est attribué dans ce rapport ; cela ne signifie pas que le test ne peut en tuer aucun.

**H03 — `testReadIntLEValues`**

- Intention : Décoder LE32 signé sur un flux.
- Données et motivation : `12 34 56 78` utilise quatre contributions distinctes non nulles ; `FE FF FF FF` exerce le signe.
- Oracle et raison de la détection : Attendus `0x78563412` et -2. Changer un décalage, soustraire une contribution ou retourner zéro viole ces valeurs calculées par les poids LE.
- Nouveaux mutants tués attribués par PIT : 7 (M046, M047, M048, M049, M050, M051, M052).

**H04 — `testReadIntBEValues`**

- Intention : Décoder BE32 signé sur un flux.
- Données et motivation : `12 34 56 78` impose quatre poids distincts ; `FF FF FF FE` encode une valeur négative.
- Oracle et raison de la détection : Attendus `0x12345678` et -2. Les octets sont pondérés par 2^24, 2^16, 2^8 et 1.
- Nouveaux mutants tués attribués par PIT : 7 (M058, M059, M060, M061, M062, M063, M064).

**H05 — `testReadLongLEValues`**

- Intention : Décoder un entier LE64, y compris les positions au-delà de 32 bits.
- Données et motivation : `01 02 03 04 05 06 07 08` donne un rôle observable à chacun des huit octets ; `FE FF FF FF FF FF FF FF` contrôle le signe.
- Oracle et raison de la détection : Attendus `0x0807060504030201L` et -2L. Le premier oracle est la somme des octets pondérés par 2^(8j), j de 0 à 7 ; les mutations des décalages et additions ne préservent pas cette somme.
- Nouveaux mutants tués attribués par PIT : 15 (M086, M087, M088, M089, M090, M091, M092, M093, M094, M095, M096, M097, M098, M099, M100).

**H06 — `testReadLongBEValues`**

- Intention : Décoder un entier BE64, avec poids fort et signe.
- Données et motivation : `01 02 03 04 05 06 07 08`, puis `FF FF FF FF FF FF FF FE`.
- Oracle et raison de la détection : Attendus `0x0102030405060708L` et -2L, calculés avec les poids 2^(8(7-j)). Les huit contributions non nulles distinguent les changements arithmétiques.
- Nouveaux mutants tués attribués par PIT : 15 (M110, M111, M112, M113, M114, M115, M116, M117, M118, M119, M120, M121, M122, M123, M124).

**H07 — `testFixedWidthZeroIsValid`**

- Intention : Ne pas confondre des octets valides égaux à zéro avec une fin de flux.
- Données et motivation : Pour chacune des onze fonctions listées ci-dessous, exactement le nombre requis d’octets `00`. Le OR de tous ces octets vaut zéro, frontière précise des contrôles de fin de flux.
- Oracle et raison de la détection : Attendu 0L sans exception pour chaque lecture. Remplacer le contrôle `< 0` par `<= 0` provoque une BufferUnderrunException injustifiée. Les courts et int sont promus en long uniquement pour partager l’assertion.
- Nouveaux mutants tués attribués par PIT : 9 (M003, M009, M015, M028, M041, M053, M065, M077, M101).

**H08 — `testFixedWidthTruncationAtEveryPosition`**

- Intention : Refuser toute lecture incomplète sur un flux dont la fin est permanente.
- Données et motivation : Pour une largeur n, tester chaque longueur de 0 à n-1 ; préfixe rempli de `12`, puis EOF de ByteArrayInputStream. Au total 44 cas pour onze lecteurs.
- Oracle et raison de la détection : Chaque appel doit lancer EndianUtils.BufferUnderrunException. Le type est spécifique, pas Exception générique. Les préfixes non nuls distinguent les contrôles supprimés et le dernier OR remplacé par AND ; les AND plus tôt peuvent être masqués par les EOF ultérieurs.
- Nouveaux mutants tués attribués par PIT : 12 (M010, M011, M031, M032, M044, M045, M056, M057, M084, M085, M108, M109).

**H09 — `testIntBEArrayWithoutOffset`**

- Intention : Exercer explicitement la surcharge getIntBE(byte[]), auparavant non couverte.
- Données et motivation : Tableau `12 34 56 78`, sans argument offset. Les quatre octets non nuls empêchent un retour constant zéro de passer.
- Oracle et raison de la détection : Attendu `0x12345678`. Le test atteint la surcharge de délégation elle-même, pas seulement getIntBE(byte[], int).
- Nouveaux mutants tués attribués par PIT : 1 (M173).

**H10 — `testUnsignedArrayWrappersReturnNonZero`**

- Intention : Exercer les deux surcharges sans offset getUIntBE/LE avec un résultat non nul et supérieur à Integer.MAX_VALUE.
- Données et motivation : BE : `FE DC BA 98` ; LE : `98 BA DC FE`. Le bit 31 vaut 1, tandis que la valeur attendue reste positive dans le long.
- Oracle et raison de la détection : Attendu `0xFEDCBA98L` = 4275878552 pour les deux. Les mutants qui forcent le résultat de la surcharge sans offset à zéro sont alors détectés, contrairement aux données nulles de certains tests générés.
- Nouveaux mutants tués attribués par PIT : 2 (M189, M192).

**H11 — `testGetUByteAtOffset`**

- Intention : Lire un octet non signé à un offset donné.
- Données et motivation : Tableau `55 80 FF 00`, offsets 1, 2 et 3. Le préfixe 55 révèle un mauvais indice ; 80 et FF représentent des byte Java négatifs.
- Oracle et raison de la détection : Attendus 128, 255 et 0. AND→OR donne 255 pour 80 au lieu de 128 ; un retour forcé à zéro échoue dès le premier cas.
- Nouveaux mutants tués attribués par PIT : 2 (M205, M206).

**H12 — `testReadUE7ZeroTerminalPreservesNextByte`**

- Intention : Accepter la valeur UE7 zéro et ne pas consommer l’octet suivant.
- Données et motivation : Flux `00 55` : 00 est un terminal valide, 55 est une sentinelle hors de l’encodage.
- Oracle et raison de la détection : readUE7 doit retourner 0L ; la lecture suivante doit rendre 0x55. Ce cas protège le terminal nul sans préfixe de continuation.
- Nouveaux mutants tués attribués par PIT : 0. Aucun mutant supplémentaire ne lui est attribué dans ce rapport ; cela ne signifie pas que le test ne peut en tuer aucun.

**H13 — `testReadUE7RejectsPrematureEnd`**

- Intention : Refuser une absence de valeur et une continuation sans octet suivant.
- Données et motivation : Flux vide, puis flux réduit à `81`. Le bit haut de 81 annonce une continuation absente.
- Oracle et raison de la détection : IOException dans les deux cas, conformément au contrôle EOF de readUE7. Supprimer le contrôle final retournerait un accumulateur au lieu de signaler l’entrée incomplète.
- Nouveaux mutants tués attribués par PIT : 1 (M137).

**H14 — `testReadUE7SixBytePayload`**

- Intention : Lire un encodage valide de six groupes de sept bits et préserver le flux suivant.
- Données et motivation : `81 82 83 84 85 06 55` : cinq continuations, un terminal 06, puis la sentinelle 55. Les charges utiles 1 à 6 sont distinctes et non nulles.
- Oracle et raison de la détection : Attendu 34902966918L = 1×128^5 + 2×128^4 + 3×128^3 + 4×128^2 + 5×128 + 6 ; la lecture suivante donne 0x55.
- Nouveaux mutants tués attribués par PIT : 0. Aucun mutant supplémentaire ne lui est attribué dans ce rapport ; cela ne signifie pas que le test ne peut en tuer aucun.

**H15 — `testReadUE7SixByteLimitConsumesLookahead`**

- Intention : Caractériser la limite existante de six groupes sur une entrée trop longue.
- Données et motivation : `81 82 83 84 85 86 07 55` : le sixième groupe annonce encore une continuation. Le septième octet 07, suivi de 55, distingue l’ajout d’un groupe et la consommation du flux.
- Oracle et raison de la détection : Le code actuel accumule les six charges utiles 1 à 6 (34902966918L) et lit puis écarte 07 avant de sortir ; la lecture suivante rend 55. Une borne élargie ou un compteur décrémenté accumule aussi 07 et donne une autre valeur. Il s’agit d’un oracle de caractérisation tiré de la limite explicite du code, pas d’une exigence indépendante approuvant la troncature silencieuse.
- Nouveaux mutants tués attribués par PIT : 2 (M126, M127).

**H16 — `testReadUE7ZeroAfterContinuation`**

- Intention : Appliquer le poids du terminal nul après un préfixe non nul.
- Données et motivation : `81 00 55` : contrairement au cas 00 seul, l’accumulateur vaut déjà 1 avant le terminal nul.
- Oracle et raison de la détection : Attendu 1×128 + 0 = 128L et sentinelle 55 intacte. La condition de boucle `> 0` quitte trop tôt avec 1 ; le contrôle final `<= 0` lance une exception injustifiée. Le zéro initial seul ne distinguait pas la première mutation.
- Nouveaux mutants tués attribués par PIT : 2 (M125, M136).

**H17 — `testFixedWidthRejectsEofEvenIfFileGrows`**

- Intention : Refuser une lecture qui a rencontré EOF, même si des octets apparaissent lors des lectures suivantes.
- Données et motivation : Pour chaque lecteur de largeur n et position p de 0 à n-1, créer un fichier contenant p octets 12. Un FilterInputStream autour d’un vrai FileInputStream observe -1, ajoute ensuite n-p-1 octets 34 au fichier, puis retourne le -1 réellement lu. Les lectures suivantes voient les nouveaux octets. Il y a 44 cas, synchrones et sans attente ni thread concurrent ; @TempDir isole les fichiers.
- Oracle et raison de la détection : BufferUnderrunException reste attendue : l’un des n appels n’a pas fourni d’octet. Un OR garde le bit de signe de -1 ; un AND intermédiaire peut l’effacer avec un octet positif, et aucun EOF ultérieur ne le réintroduit. Ce scénario réel de fichier évolutif distingue les 22 AND précoces encore survivants sur les fichiers statiques tronqués. Il ne retourne ni valeur négative différente de -1 ni faux octet hors de 0..255.
- Nouveaux mutants tués attribués par PIT : 22 (M016, M017, M029, M030, M042, M043, M054, M055, M066, M067, M078, M079, M080, M081, M082, M083, M102, M103, M104, M105, M106, M107).

#### Résultats finaux de validation et de mutation

| Mesure | Originaux | + Générés corrigés | + 17 tests supplémentaires |
|---|---:|---:|---:|
| Mutants | 206 | 206 | 206 |
| Tués | 36 | 103 | 204 |
| Survivants | 16 | 21 | 2 |
| Non couverts | 154 | 82 | 0 |
| Score brut | 17,48 % | 50,00 % | **99,03 %** |
| Lignes couvertes PIT | 31/121 | 74/121 | 120/121 |

**101 mutants supplémentaires sont tués depuis l’étape générée corrigée**, soit +49,03 points. Les 103 mutants déjà tués restent tués. Les 82 non couverts deviennent tous tués ; parmi les 21 survivants, 19 sont tués et deux restent survivants. Aucun timeout, erreur d’exécution ou mutant non viable ne contribue au score.

La suite complète recense **837 tests = 749 originaux + 71 générés corrigés + 17 supplémentaires** : 835 réussis, 2 ignorés, zéro échec et zéro erreur. [Journal des tests](chatunitest-local/logs/all-tests-with-manual.log), [rapport JUnit des 17 tests](chatunitest-local/manual/surefire-reports/TEST-org.apache.tika.io.EndianUtilsManualTest.xml).

Le [rapport HTML final](rapports-pit/endian-after-manual/index.html), le [XML final](rapports-pit/endian-after-manual/mutations.xml) et le [journal PIT final](chatunitest-local/logs/pit-after-manual.log) sont distincts des mesures précédentes. Les commandes exécutées après modification des sources sont :

```bash
mvn -B -pl tika-core -Pchatunitest-verify test \
  -Drat.skip=true -Dcheckstyle.skip=true -Dossindex.skip=true

mvn -B -pl tika-core -Pchatunitest-verify test-compile \
  org.pitest:pitest-maven:1.25.9:mutationCoverage \
  -DtargetClasses=org.apache.tika.io.EndianUtils \
  -Drat.skip=true -Dcheckstyle.skip=true -Dossindex.skip=true
```

Le plugin, ses opérateurs et la classe ciblée sont inchangés. Le `clean` avait été exécuté avant la mesure générée corrigée ; cette étape n’a ajouté qu’une classe de test et n’a supprimé/renommé aucun test. Le contrôle des 206 identités (méthode, surcharge JVM, ligne, opérateur et indices bytecode) confirme la même population dans les trois rapports. La compilation et les tests sont validés localement ; le workflow CI est configuré et validé localement ainsi que sur GitHub (voir la section « Intégration continue »).

#### Attribution exacte des 101 nouvelles détections

Les identifiants M restent ceux de la comparaison précédente. H renvoie au catalogue ci-dessus, qui donne les données, l’oracle et le mécanisme de détection. `killingTest` est le test retenu par PIT, pas nécessairement le seul test capable de tuer le mutant. La [comparaison finale JSON](rapports-pit/comparison-endian-manual.json) conserve les noms complets et permet de vérifier chaque attribution. Reproduction de cette section : `python3 chatunitest-local/document-manual.py`.

| Mutant | Méthode, ligne et index | Opérateur et transformation | Statut avant tests supplémentaires | Test tueur et oracle |
|---|---|---|---|---|
| M002 | `readShortBE(Ljava/io/InputStream;)S`, L58, i=6 | `PrimitiveReturnsMutator` : replaced short return with 0 for org/apache/tika/io/EndianUtils::readShortBE | `NO_COVERAGE` | H01 |
| M003 | `readUShortLE(Ljava/io/InputStream;)I`, L64, i=16 | `ConditionalsBoundaryMutator` : changed conditional boundary | `SURVIVED` | H07 |
| M009 | `readUShortBE(Ljava/io/InputStream;)I`, L73, i=16 | `ConditionalsBoundaryMutator` : changed conditional boundary | `NO_COVERAGE` | H07 |
| M010 | `readUShortBE(Ljava/io/InputStream;)I`, L73, i=15 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H08 |
| M011 | `readUShortBE(Ljava/io/InputStream;)I`, L73, i=16 | `RemoveConditionalMutator_ORDER_ELSE` : removed conditional - replaced comparison check with false | `NO_COVERAGE` | H08 |
| M012 | `readUShortBE(Ljava/io/InputStream;)I`, L76, i=28 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H01 |
| M013 | `readUShortBE(Ljava/io/InputStream;)I`, L76, i=30 | `MathMutator` : Replaced integer addition with subtraction | `NO_COVERAGE` | H01 |
| M014 | `readUShortBE(Ljava/io/InputStream;)I`, L76, i=31 | `PrimitiveReturnsMutator` : replaced int return with 0 for org/apache/tika/io/EndianUtils::readUShortBE | `NO_COVERAGE` | H01 |
| M015 | `readUIntLE(Ljava/io/InputStream;)J`, L92, i=30 | `ConditionalsBoundaryMutator` : changed conditional boundary | `SURVIVED` | H07 |
| M016 | `readUIntLE(Ljava/io/InputStream;)J`, L92, i=25 | `MathMutator` : Replaced bitwise OR with AND | `SURVIVED` | H17 |
| M017 | `readUIntLE(Ljava/io/InputStream;)J`, L92, i=27 | `MathMutator` : Replaced bitwise OR with AND | `SURVIVED` | H17 |
| M028 | `readUIntBE(Ljava/io/InputStream;)J`, L111, i=30 | `ConditionalsBoundaryMutator` : changed conditional boundary | `SURVIVED` | H07 |
| M029 | `readUIntBE(Ljava/io/InputStream;)J`, L111, i=25 | `MathMutator` : Replaced bitwise OR with AND | `SURVIVED` | H17 |
| M030 | `readUIntBE(Ljava/io/InputStream;)J`, L111, i=27 | `MathMutator` : Replaced bitwise OR with AND | `SURVIVED` | H17 |
| M031 | `readUIntBE(Ljava/io/InputStream;)J`, L111, i=29 | `MathMutator` : Replaced bitwise OR with AND | `SURVIVED` | H08 |
| M032 | `readUIntBE(Ljava/io/InputStream;)J`, L111, i=30 | `RemoveConditionalMutator_ORDER_ELSE` : removed conditional - replaced comparison check with false | `SURVIVED` | H08 |
| M041 | `readIntLE(Ljava/io/InputStream;)I`, L130, i=30 | `ConditionalsBoundaryMutator` : changed conditional boundary | `NO_COVERAGE` | H07 |
| M042 | `readIntLE(Ljava/io/InputStream;)I`, L130, i=25 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M043 | `readIntLE(Ljava/io/InputStream;)I`, L130, i=27 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M044 | `readIntLE(Ljava/io/InputStream;)I`, L130, i=29 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H08 |
| M045 | `readIntLE(Ljava/io/InputStream;)I`, L130, i=30 | `RemoveConditionalMutator_ORDER_ELSE` : removed conditional - replaced comparison check with false | `NO_COVERAGE` | H08 |
| M046 | `readIntLE(Ljava/io/InputStream;)I`, L133, i=42 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H03 |
| M047 | `readIntLE(Ljava/io/InputStream;)I`, L133, i=45 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H03 |
| M048 | `readIntLE(Ljava/io/InputStream;)I`, L133, i=46 | `MathMutator` : Replaced integer addition with subtraction | `NO_COVERAGE` | H03 |
| M049 | `readIntLE(Ljava/io/InputStream;)I`, L133, i=49 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H03 |
| M050 | `readIntLE(Ljava/io/InputStream;)I`, L133, i=50 | `MathMutator` : Replaced integer addition with subtraction | `NO_COVERAGE` | H03 |
| M051 | `readIntLE(Ljava/io/InputStream;)I`, L133, i=52 | `MathMutator` : Replaced integer addition with subtraction | `NO_COVERAGE` | H03 |
| M052 | `readIntLE(Ljava/io/InputStream;)I`, L133, i=53 | `PrimitiveReturnsMutator` : replaced int return with 0 for org/apache/tika/io/EndianUtils::readIntLE | `NO_COVERAGE` | H03 |
| M053 | `readIntBE(Ljava/io/InputStream;)I`, L149, i=30 | `ConditionalsBoundaryMutator` : changed conditional boundary | `NO_COVERAGE` | H07 |
| M054 | `readIntBE(Ljava/io/InputStream;)I`, L149, i=25 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M055 | `readIntBE(Ljava/io/InputStream;)I`, L149, i=27 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M056 | `readIntBE(Ljava/io/InputStream;)I`, L149, i=29 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H08 |
| M057 | `readIntBE(Ljava/io/InputStream;)I`, L149, i=30 | `RemoveConditionalMutator_ORDER_ELSE` : removed conditional - replaced comparison check with false | `NO_COVERAGE` | H08 |
| M058 | `readIntBE(Ljava/io/InputStream;)I`, L152, i=42 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H04 |
| M059 | `readIntBE(Ljava/io/InputStream;)I`, L152, i=45 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H04 |
| M060 | `readIntBE(Ljava/io/InputStream;)I`, L152, i=46 | `MathMutator` : Replaced integer addition with subtraction | `NO_COVERAGE` | H04 |
| M061 | `readIntBE(Ljava/io/InputStream;)I`, L152, i=49 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H04 |
| M062 | `readIntBE(Ljava/io/InputStream;)I`, L152, i=50 | `MathMutator` : Replaced integer addition with subtraction | `NO_COVERAGE` | H04 |
| M063 | `readIntBE(Ljava/io/InputStream;)I`, L152, i=52 | `MathMutator` : Replaced integer addition with subtraction | `NO_COVERAGE` | H04 |
| M064 | `readIntBE(Ljava/io/InputStream;)I`, L152, i=53 | `PrimitiveReturnsMutator` : replaced int return with 0 for org/apache/tika/io/EndianUtils::readIntBE | `NO_COVERAGE` | H04 |
| M065 | `readIntME(Ljava/io/InputStream;)I`, L168, i=30 | `ConditionalsBoundaryMutator` : changed conditional boundary | `SURVIVED` | H07 |
| M066 | `readIntME(Ljava/io/InputStream;)I`, L168, i=25 | `MathMutator` : Replaced bitwise OR with AND | `SURVIVED` | H17 |
| M067 | `readIntME(Ljava/io/InputStream;)I`, L168, i=27 | `MathMutator` : Replaced bitwise OR with AND | `SURVIVED` | H17 |
| M077 | `readLongLE(Ljava/io/InputStream;)J`, L191, i=58 | `ConditionalsBoundaryMutator` : changed conditional boundary | `NO_COVERAGE` | H07 |
| M078 | `readLongLE(Ljava/io/InputStream;)J`, L191, i=45 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M079 | `readLongLE(Ljava/io/InputStream;)J`, L191, i=47 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M080 | `readLongLE(Ljava/io/InputStream;)J`, L191, i=49 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M081 | `readLongLE(Ljava/io/InputStream;)J`, L191, i=51 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M082 | `readLongLE(Ljava/io/InputStream;)J`, L191, i=53 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M083 | `readLongLE(Ljava/io/InputStream;)J`, L191, i=55 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M084 | `readLongLE(Ljava/io/InputStream;)J`, L191, i=57 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H08 |
| M085 | `readLongLE(Ljava/io/InputStream;)J`, L191, i=58 | `RemoveConditionalMutator_ORDER_ELSE` : removed conditional - replaced comparison check with false | `NO_COVERAGE` | H08 |
| M086 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=71 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H05 |
| M087 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=75 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H05 |
| M088 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=76 | `MathMutator` : Replaced long addition with subtraction | `NO_COVERAGE` | H05 |
| M089 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=80 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H05 |
| M090 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=81 | `MathMutator` : Replaced long addition with subtraction | `NO_COVERAGE` | H05 |
| M091 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=85 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H05 |
| M092 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=86 | `MathMutator` : Replaced long addition with subtraction | `NO_COVERAGE` | H05 |
| M093 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=90 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H05 |
| M094 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=91 | `MathMutator` : Replaced long addition with subtraction | `NO_COVERAGE` | H05 |
| M095 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=94 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H05 |
| M096 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=96 | `MathMutator` : Replaced long addition with subtraction | `NO_COVERAGE` | H05 |
| M097 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=99 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H05 |
| M098 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=101 | `MathMutator` : Replaced long addition with subtraction | `NO_COVERAGE` | H05 |
| M099 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=104 | `MathMutator` : Replaced long addition with subtraction | `NO_COVERAGE` | H05 |
| M100 | `readLongLE(Ljava/io/InputStream;)J`, L195, i=105 | `PrimitiveReturnsMutator` : replaced long return with 0 for org/apache/tika/io/EndianUtils::readLongLE | `NO_COVERAGE` | H05 |
| M101 | `readLongBE(Ljava/io/InputStream;)J`, L217, i=58 | `ConditionalsBoundaryMutator` : changed conditional boundary | `NO_COVERAGE` | H07 |
| M102 | `readLongBE(Ljava/io/InputStream;)J`, L217, i=45 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M103 | `readLongBE(Ljava/io/InputStream;)J`, L217, i=47 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M104 | `readLongBE(Ljava/io/InputStream;)J`, L217, i=49 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M105 | `readLongBE(Ljava/io/InputStream;)J`, L217, i=51 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M106 | `readLongBE(Ljava/io/InputStream;)J`, L217, i=53 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M107 | `readLongBE(Ljava/io/InputStream;)J`, L217, i=55 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H17 |
| M108 | `readLongBE(Ljava/io/InputStream;)J`, L217, i=57 | `MathMutator` : Replaced bitwise OR with AND | `NO_COVERAGE` | H08 |
| M109 | `readLongBE(Ljava/io/InputStream;)J`, L217, i=58 | `RemoveConditionalMutator_ORDER_ELSE` : removed conditional - replaced comparison check with false | `NO_COVERAGE` | H08 |
| M110 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=71 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H06 |
| M111 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=75 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H06 |
| M112 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=76 | `MathMutator` : Replaced long addition with subtraction | `NO_COVERAGE` | H06 |
| M113 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=80 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H06 |
| M114 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=81 | `MathMutator` : Replaced long addition with subtraction | `NO_COVERAGE` | H06 |
| M115 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=85 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H06 |
| M116 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=86 | `MathMutator` : Replaced long addition with subtraction | `NO_COVERAGE` | H06 |
| M117 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=90 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H06 |
| M118 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=91 | `MathMutator` : Replaced long addition with subtraction | `NO_COVERAGE` | H06 |
| M119 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=94 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H06 |
| M120 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=96 | `MathMutator` : Replaced long addition with subtraction | `NO_COVERAGE` | H06 |
| M121 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=99 | `MathMutator` : Replaced Shift Left with Shift Right | `NO_COVERAGE` | H06 |
| M122 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=101 | `MathMutator` : Replaced long addition with subtraction | `NO_COVERAGE` | H06 |
| M123 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=104 | `MathMutator` : Replaced long addition with subtraction | `NO_COVERAGE` | H06 |
| M124 | `readLongBE(Ljava/io/InputStream;)J`, L221, i=105 | `PrimitiveReturnsMutator` : replaced long return with 0 for org/apache/tika/io/EndianUtils::readLongBE | `NO_COVERAGE` | H06 |
| M125 | `readUE7(Ljava/io/InputStream;)J`, L235, i=21 | `ConditionalsBoundaryMutator` : changed conditional boundary | `SURVIVED` | H16 |
| M126 | `readUE7(Ljava/io/InputStream;)J`, L235, i=25 | `ConditionalsBoundaryMutator` : changed conditional boundary | `SURVIVED` | H15 |
| M127 | `readUE7(Ljava/io/InputStream;)J`, L235, i=23 | `IncrementsMutator` : Changed increment from 1 to -1 | `SURVIVED` | H15 |
| M136 | `readUE7(Ljava/io/InputStream;)J`, L246, i=64 | `ConditionalsBoundaryMutator` : changed conditional boundary | `SURVIVED` | H16 |
| M137 | `readUE7(Ljava/io/InputStream;)J`, L246, i=64 | `RemoveConditionalMutator_ORDER_ELSE` : removed conditional - replaced comparison check with false | `SURVIVED` | H13 |
| M173 | `getIntBE([B)I`, L373, i=6 | `PrimitiveReturnsMutator` : replaced int return with 0 for org/apache/tika/io/EndianUtils::getIntBE | `NO_COVERAGE` | H09 |
| M189 | `getUIntLE([B)J`, L399, i=6 | `PrimitiveReturnsMutator` : replaced long return with 0 for org/apache/tika/io/EndianUtils::getUIntLE | `SURVIVED` | H10 |
| M192 | `getUIntBE([B)J`, L421, i=6 | `PrimitiveReturnsMutator` : replaced long return with 0 for org/apache/tika/io/EndianUtils::getUIntBE | `SURVIVED` | H10 |
| M205 | `getUByte([BI)S`, L472, i=7 | `MathMutator` : Replaced bitwise AND with OR | `NO_COVERAGE` | H11 |
| M206 | `getUByte([BI)S`, L472, i=9 | `PrimitiveReturnsMutator` : replaced short return with 0 for org/apache/tika/io/EndianUtils::getUByte | `NO_COVERAGE` | H11 |

#### Preuve d’équivalence des deux survivants

Les deux mutants restent officiellement `SURVIVED` dans PIT ; le moteur ne les a pas classés `EQUIVALENT`. Leur équivalence est une conclusion séparée obtenue par analyse du code :

| Mutant | Emplacement | Modification |
|---|---|---|
| M164 | `getIntLE([BI)I`, ligne 362, index 36 | Dernier `i++` remplacé par `i--` dans `int b3 = data[i++] & 0xFF` |
| M180 | `getIntBE([BI)I`, ligne 388, index 36 | Dernier `i++` remplacé par `i--` dans `int b3 = data[i++] & 0xFF` |

Dans les deux versions, le postfixe fournit **la même ancienne valeur de i** à l’accès `data[...]`. La valeur de b3 est donc identique pour toute entrée où l’accès réussit. Seule la nouvelle valeur de la variable locale i diffère. Or i n’est plus lue après cet accès : le retour dépend exclusivement de b0, b1, b2 et b3. La variable n’est ni partagée, ni retournée. Un éventuel dépassement arithmétique du int local ne lance pas d’exception en Java. Pour un tableau nul ou un indice invalide, l’accès utilise le même indice et échoue de la même manière. Les méthodes n’offrent donc aucun comportement observable permettant à un test fonctionnel de distinguer ces deux mutants.

Le résultat principal reste **204/206 = 99,03 %**, sans retirer de mutant du rapport. Si les deux équivalents démontrés sont exclus pour une mesure complémentaire explicitement ajustée, le score devient 204/(206−2) = 100 %. Ce chiffre ajusté n’est pas le score brut PIT, et ne prouve pas l’absence de défauts hors du jeu `DEFAULTS`. La couverture de lignes reste 120/121 ; aucun 100 % de lignes n’est revendiqué.

<!-- MANUAL-TESTS-END -->

### Intégration continue

Le workflow [Tache 2 - tika-core tests](.github/workflows/tache2-tika-core.yml) se déclenche à chaque `push`, à chaque `pull_request` et manuellement (`workflow_dispatch`). Il utilise Ubuntu 24.04, Temurin Java 21 et le wrapper Maven 3.9.12 du dépôt. Les permissions GitHub sont limitées à la lecture du contenu.

1. Installation de `tika-core` et de ses prérequis du réacteur avec les tests désactivés.
2. Nettoyage, compilation et exécution de tous les tests de `tika-core`, avec le profil `chatunitest-verify` qui ajoute les fichiers de `tika-core/chatunitest` aux sources de test. Les tests originaux et manuels de `src/test/java` restent inclus.
3. Conservation des rapports `tika-core/target/surefire-reports/` dans l'artefact `tika-core-surefire-reports` pendant 14 jours, même si les tests échouent, lorsque ces rapports existent.

Commandes exactes exécutées par le workflow :

```bash
./mvnw -B -ntp -pl tika-core -am install -DskipTests \
  -Drat.skip=true -Dcheckstyle.skip=true -Dossindex.skip=true
./mvnw -B -ntp -pl tika-core -Pchatunitest-verify clean test \
  -Drat.skip=true -Dcheckstyle.skip=true -Dossindex.skip=true
```

Aucune installation ni invocation d'Ollama, aucune génération ChatUniTest et aucune analyse PIT ne sont exécutées dans ce workflow. Le profil de génération `chatunitest-local` n'est pas activé. RAT, Checkstyle et OSS Index sont désactivés ; Spotless reste actif. Les autres workflows hérités d'Apache Tika ne sont pas modifiés.

**Vérification du 28 septembre 2026 :** les deux commandes exactes ci-dessus réussissent localement sur macOS avec Java 21. Après `clean`, Surefire compte **837 tests, 0 échec, 0 erreur et 2 ignorés**, dont les **71 tests générés corrigés** et les **17 tests manuels**. Journaux : [préparation](chatunitest-local/logs/ci-prerequisites-local.log) et [tests](chatunitest-local/logs/ci-tests-local.log). Le cache Maven local était déjà alimenté : cette vérification ne prouve pas une résolution depuis un cache vierge.

**Vérification distante réussie le 28 septembre 2026 :** le commit `d4288be12` a été publié sur `main`. Le [workflow GitHub Actions n° 36482829069](https://github.com/nass1379/tika/actions/runs/36482829069) a terminé avec succès sur Ubuntu 24.04 : préparation du réacteur, exécution des tests originaux, générés et manuels, puis sauvegarde des rapports Surefire. Le statut invalide de `gh auth status` n'empêchait pas Git de publier avec son accès existant ; aucune nouvelle authentification n'a été nécessaire. Cette preuve concerne le workflow « Tache 2 - tika-core tests », pas les autres workflows hérités du dépôt Apache. Le commit documentaire suivant porte `[skip ci]` pour éviter de relancer les builds pour cette seule mise à jour du compte rendu.

## Expérience MediaType 

Classe : `org.apache.tika.mime.MediaType`, module `tika-core`. Les tests existants spécifiques à cette classe sont dans [MediaTypeTest.java](tika-core/src/test/java/org/apache/tika/mime/MediaTypeTest.java).

### Environnement et préparation

Expérience réalisée sous Windows avec PowerShell, Temurin Java 21.0.9 et le wrapper Maven 3.9.12 du dépôt, sur la branche `mediatype`, à partir du commit `539ac7414649b1fc7351ef32d5837bc2546267ba`.

Ollama 0.34.4 est installé avec `qwen2.5-coder:7b` et l’alias `codeqwen:v1.5-chat`. La configuration de l’alias indique une fenêtre de contexte de 8192 tokens.

Preuves : [environnement](chatunitest-local/mediatype/logs/environment.log) et [préparation Maven](chatunitest-local/mediatype/logs/prerequisites.log).

### Mesure initiale avec les tests originaux

Analyse exécutée le 29 septembre 2026 avec PIT 1.25.9, le connecteur JUnit 1.2.3 et les opérateurs `DEFAULTS`. Seule `MediaType` est ciblée. Le profil `chatunitest-verify` n’est pas activé ; les tests générés ne font donc pas partie de cette mesure. Maven nettoie le dossier temporaire de compilation avant de recompiler le projet pour cette mesure initiale.

| Mesure | Résultat initial |
|---|---:|
| Mutants générés | 85 |
| `KILLED` | 60 |
| `SURVIVED` | 10 |
| `NO_COVERAGE` | 14 |
| `TIMED_OUT` | 1 |
| Score strict `KILLED / total` | 60/85 = **70,59 %** |
| Score incluant le timeout | 61/85 = **71,76 %** |
| Couverture des lignes PIT | 126/156 = **80,77 %** |

Le résumé PIT annonce « Killed 61 » en incluant le mutant `TIMED_OUT`. Nous distinguons ce timeout des 60 mutants classés `KILLED` ; sa cause est expliquée dans l’analyse des mutants restants.

Cette mesure justifie le choix de `MediaType` : sa couverture est inférieure à 100 %, dix mutants survivent aux tests existants et quatorze mutants ne sont pas couverts.

La génération a été lancée à l’échelle de la classe, sans sélection
manuelle de méthodes individuelles. Le rapport initial montre notamment
un mutant sans couverture dans `audio`, un cas sans couverture dans
`equals`, ainsi que des mutants survivants dans `parse` et dans
`isSimpleName`, appelée par le parseur. Ces lacunes motivent la génération
de cas supplémentaires sur les fabriques et le parsing.

D’autres méthodes ont également reçu des tests générés. Nous ne prétendons
pas que chacune était initialement sans couverture : l’objectif était
d’enrichir les tests de la classe et de mesurer leur apport avec PIT.

Commande exécutée depuis la racine du dépôt, dans PowerShell :

```powershell
.\mvnw.cmd -B -ntp -pl tika-core clean test-compile org.pitest:pitest-maven:1.25.9:mutationCoverage "-DtargetClasses=org.apache.tika.mime.MediaType" "-Drat.skip=true" "-Dcheckstyle.skip=true" "-Dossindex.skip=true" 2>&1 |
    Tee-Object -FilePath "chatunitest-local/mediatype/logs/pit-before.log"
```

Résultat : `BUILD SUCCESS`. Cette commande compile les tests et exécute l’analyse PIT ; elle ne constitue pas une exécution complète des tests par Surefire.

Le rapport a été copié hors de `target` avant toute nouvelle commande Maven. Preuves : [rapport HTML initial](rapports-pit/mediatype-before/index.html), [mutants XML](rapports-pit/mediatype-before/mutations.xml) et [journal Maven/PIT](chatunitest-local/mediatype/logs/pit-before.log).

### Préparation des prompts

Les templates du dépôt ont été copiés dans [mediatype/prompts](chatunitest-local/mediatype/prompts). La ligne du prompt système concernant l’exception imbriquée d’EndianUtils a été retirée, car elle ne concerne pas MediaType. Aucun résultat attendu ni oracle de test n’a été ajouté.

### Génération et sauvegarde des tests

La génération ChatUniTest s’est terminée le 29 septembre 2026 après 51 minutes, avec `BUILD SUCCESS`. Neuf fichiers de tests MediaType ont été exportés. Leur compilation et leurs résultats ont été vérifiés séparément avec Maven.

Les [tests générés originaux](chatunitest-local/mediatype/attempt-01/exported/) ont été archivés avant toute modification, avec leurs [empreintes SHA-256](chatunitest-local/mediatype/attempt-01/raw-tests-sha256.csv), les [fichiers du plugin](chatunitest-local/mediatype/attempt-01/plugin-output/), la [configuration Maven](chatunitest-local/mediatype/attempt-01/pom.xml) et les [prompts utilisés](chatunitest-local/mediatype/attempt-01/prompts/).

Le profil `chatunitest-mediatype` du [pom.xml](tika-core/pom.xml) cible MediaType et appelle Ollama localement avec l’alias `codeqwen:v1.5-chat`, basé sur `qwen2.5-coder:7b`. Paramètres : `testNumber=1`, trois tours maximum, température 0,2, génération séquentielle, fusion désactivée, limites de 6000 tokens pour le prompt et 2048 pour la réponse.

Commande exécutée depuis la racine du dépôt :

```powershell
.\mvnw.cmd -B -ntp -f tika-core/pom.xml -Pchatunitest-mediatype test-compile io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:class "-DselectClass=org.apache.tika.mime.MediaType" "-Drat.skip=true" "-Dcheckstyle.skip=true" "-Dossindex.skip=true" 2>&1 |
    Tee-Object -FilePath "chatunitest-local/mediatype/logs/generation-01.log"
```

Journal complet : [generation-01.log](chatunitest-local/mediatype/logs/generation-01.log).

### Contenu et limites des tests générés

Les neuf fichiers originaux sont accessibles dans
[exported](chatunitest-local/mediatype/attempt-01/exported/).
Le tableau décrit leurs 29 méthodes de test ; les corrections nécessaires
sont détaillées dans la section suivante.

| Fichier | Nombre de tests | Comportements vérifiés et limites |
|---|---:|---|
| `MediaType_audio_1_0_Test.java` | 1 | Fabrique audio avec sous-type simple, paramètres, valeur contenant un espace et sous-type vide. Les attentes initiales sur les guillemets et le sous-type vide étaient incorrectes. |
| `MediaType_compareTo_20_0_Test.java` | 1 | Égalité et ordre entre json et xml. L’oracle initial exigeait exactement -1 et 1 ; seul le signe est pertinent. Les différences de paramètres ne sont pas testées. |
| `MediaType_equals_18_0_Test.java` | 4 | Même objet, type différent, null et objet d’une autre classe. Les réponses booléennes sont pertinentes ; deux objets distincts mais égaux ne sont pas vérifiés ici. |
| `MediaType_hashCode_19_0_Test.java` | 1 | Même hash pour deux valeurs égales et hashes différents pour deux exemples précis. La première assertion vérifie le contrat ; la seconde n’est pas une propriété générale, car des collisions sont permises. |
| `MediaType_hasParameters_15_0_Test.java` | 2 | Map vide et map contenant un charset. Les oracles false/true distinguent directement les deux situations ; ils ne vérifient pas le contenu des paramètres. |
| `MediaType_image_2_0_Test.java` | 1 | Fabrique image avec png, sous-type vide et argument null. Les attentes initiales sur les deux derniers cas étaient incorrectes. |
| `MediaType_parse_7_0_Test.java` | 15 | Null, types simples et paramétrés, espaces, guillemets, paramètres multiples et identité des résultats. Plusieurs attentes initiales étaient incorrectes. Deux tests utilisent exactement l’entrée text/plain; et des assertions identiques, ce qui constitue une redondance. |
| `MediaType_text_3_0_Test.java` | 3 | Type simple, espaces autour du sous-type et charset. Les représentations attendues sont précises ; les entrées nulles et vides ne sont pas testées ici. |
| `MediaType_toString_17_0_Test.java` | 1 | Représentation application/json sans paramètres. L’oracle est précis mais couvre un seul cas simple. |

Les tests exportés ne couvrent pas toutes les méthodes ni toutes les
branches de MediaType. Leur réussite après correction ne suffit donc
pas à conclure à une couverture complète ; les mesures PIT quantifient
leur apport et les lacunes restantes.

### Vérification des tests générés originaux

Les neuf fichiers générés compilent. Leur exécution avec Maven, via le profil `chatunitest-verify`, donne **29 tests : 22 réussis, 7 échecs, aucune erreur et aucun test ignoré**. Maven termine avec `BUILD FAILURE`.

Les échecs concernent `audio`, `compareTo`, `image` et quatre tests de `parse`. Les assertions et les données ont été examinées avant correction. Le `BUILD SUCCESS` de la génération ChatUniTest ne garantit donc pas la réussite des tests exportés.

Preuves : [journal de validation](chatunitest-local/mediatype/logs/validation-raw.log) et [rapports Surefire originaux](chatunitest-local/mediatype/attempt-01/validation-raw/).

### Corrections et validation des tests générés

Les corrections ont été réalisées avec l’aide de ChatGPT/Codex, après lecture des tests et du code de `MediaType`. Quatre fichiers ont été modifiés ; les cinq autres sont inchangés. Aucun test n’a été supprimé ou désactivé.

**Comptage des corrections.** La comparaison des neuf fichiers [bruts](chatunitest-local/mediatype/attempt-01/exported/) avec leur [version corrigée](chatunitest-local/mediatype/corrections/version-01/) identifie **7 méthodes de test corrigées dans 4 fichiers**, sur 29 méthodes conservées. L’unité retenue est une méthode de test dont les données ou les assertions ont changé, et non une ligne modifiée ni une assertion individuelle. `testAudioMethod`, `testCompareTo` et `testImageMethod` comptent chacune pour une méthode, même lorsque plusieurs assertions ont été corrigées. Les quatre autres sont `testParse_InvalidType` (renommée `testParse_UnregisteredType`), `testParse_InvalidCharset` (renommée `testParse_UnknownCharsetPreserved`), `testParse_SpecialCharacters` et `testParse_ComplexCache` (renommée `testParse_ComplexTypesEqual`). Les renommages ne constituent pas des tests supplémentaires. Ces corrections manuelles sont distinctes des réparations automatiques tentées pendant la génération ChatUniTest.


| Test concerné | Correction et justification |
|---|---|
| `audio` | Retrait des guillemets attendus autour des valeurs simples ; le sous-type vide doit donner `null`. |
| `compareTo` | Vérification du signe du résultat plutôt que des valeurs exactes `-1` et `1`. |
| `image` | Le sous-type vide donne `null` ; l’argument Java `null` est concaténé en `"image/null"`. |
| `parse` — type non enregistré | `"invalid/type"` est accepté : le parseur ne vérifie pas l’inscription dans un registre. |
| `parse` — charset inconnu | La valeur `"invalid"` est conservée sans validation de l’encodage. |
| `parse` — paramètres | Remplacement de la virgule par un point-virgule dans les données du test. |
| `parse` — égalité | Remplacement de `assertSame` par `assertEquals` pour les types avec paramètres, qui ne sont pas mis en cache comme les types simples. |

La validation de cette version donne **29 tests réussis, aucun échec, aucune erreur et aucun test ignoré**, avec `BUILD SUCCESS`. Cette exécution cible uniquement les tests MediaType générés.

Preuves : [version corrigée sauvegardée](chatunitest-local/mediatype/corrections/version-01/), [rapports Surefire](chatunitest-local/mediatype/corrections/version-01/surefire-reports/) et [journal de validation](chatunitest-local/mediatype/logs/validation-corrected-01.log).

### Comparaison qualitative des oracles

**Comparaison avec les tests originaux.** Dans [MediaTypeTest.java](tika-core/src/test/java/org/apache/tika/mime/MediaTypeTest.java), `testBasics`, `testLowerCase` et `testTrim` comparent des représentations textuelles exactes : ils vérifient respectivement la construction, la normalisation de la casse et le retrait des espaces. `testQuote` exige l’échappement précis des caractères spéciaux. Ces oracles originaux sont plus discriminants qu’une simple assertion de non-nullité ; le test généré `testToString` ne vérifie qu’un cas simple `application/json`.

À l’inverse, le test original `testParseWithParams` vérifie le nombre et les noms des paramètres, mais pas leurs valeurs. Les tests générés `testParse_ComplexType` et `testParse_MultipleParameters` vérifient explicitement `UTF-8` et `1.0`, ce qui permet de détecter une mauvaise valeur malgré des clés correctes. Cet apport n’est toutefois pas entièrement nouveau : le test original `testParseWithParamsAndQuotedCharset` vérifie déjà les valeurs et compare notamment une map attendue complète pour le cas d’un charset unique.

Les tests générés ajoutent des cas comme l’entrée nulle et l’égalité avec un objet d’une autre classe. Ils présentent aussi des redondances : `testParse_EmptyParameters` et `testParse_SemicolonAtEnd` utilisent tous deux `text/plain;`, alors que le test original `testParseNoParamsWithSemi` traite déjà un type terminé par un point-virgule. Enfin, les attentes erronées sur les types non enregistrés, les charsets inconnus et l’identité des objets paramétrés montrent qu’un oracle IA précis peut malgré tout être incorrect ; les sept méthodes corrigées sont donc analysées séparément de l’apport en couverture.

Les oracles générés vérifient notamment les types, sous-types,
paramètres, représentations textuelles et relations d’égalité.
Ces assertions sont précises, mais plusieurs attentes initiales
étaient incorrectes : rejet d’un type non enregistré, validation
d’un charset inconnu ou identité supposée des objets avec paramètres.
Les vérifications de non-nullité seules apportent moins d’information
que les assertions sur les valeurs.

Les tests manuels ciblent davantage les comportements insuffisamment
vérifiés : filtrage des ensembles, non-modifiabilité, fusion et
remplacement des paramètres, préservation de l’objet de base et
caractères limites. Leurs oracles comparent des contenus attendus
explicitement construits ou vérifient une exception précise.

Les assertions d’identité du cache et de getBaseType(), ainsi que
l’accès privé par réflexion, vérifient le comportement actuel de
l’implémentation ; elles sont plus sensibles à une refonte interne.

### Mesure PIT après ajout des tests générés corrigés

L’analyse conserve la même classe cible et les mêmes opérateurs que la mesure initiale. Le profil `chatunitest-verify` ajoute les tests générés corrigés aux tests existants. Cette mesure a été réalisée avant l’ajout des tests manuels MediaType.

| Mesure | Tests existants | Avec tests générés corrigés |
|---|---:|---:|
| Mutants générés | 85 | 85 |
| `KILLED` | 60 | 68 |
| `SURVIVED` | 10 | 5 |
| `NO_COVERAGE` | 14 | 11 |
| `TIMED_OUT` | 1 | 1 |
| Score strict `KILLED / total` | 70,59 % | 80,00 % |
| Score incluant les timeouts | 71,76 % | 81,18 % |
| Couverture des lignes PIT | 126/156 = 80,77 % | 132/156 = 84,62 % |

Le nombre de mutants `KILLED` augmente de huit, soit un gain de 9,41 points de pourcentage du score strict. Le résumé PIT annonce 69 mutants détectés en incluant le timeout ; le XML distingue 68 `KILLED` et un `TIMED_OUT`. La comparaison des XML confirme cinq mutants auparavant survivants et trois auparavant sans couverture devenus KILLED. Les 60 mutants initialement KILLED le restent.

| Mutation devenue KILLED | Test détecteur | Pourquoi elle est détectée |
|---|---|---|
| `audio`, ligne 185 : retourner `null` | `testAudioMethod` | Les assertions exigent un objet et sa représentation attendue. |
| `equals`, ligne 420 : retourner `true` | `testEqualsWithDifferentClass` | Le test exige une réponse fausse pour un objet d’une autre classe. |
| `isSimpleName`, ligne 293, index 60 — `ConditionalsBoundaryMutator` | `MediaType_image_2_0_Test.testImageMethod` | La condition finale `name.length() > 0` devient `>= 0` : le sous-type vide est accepté par le chemin rapide. L’assertion `assertNull(MediaType.image(""))` distingue ce comportement. |
| `isSimpleName`, ligne 288, index 44 — `RemoveConditionalMutator_ORDER_ELSE` | `MediaType_image_2_0_Test.testImageMethod` | PIT supprime une comparaison de reconnaissance des caractères (`removed conditional - replaced comparison check with false`). Le test vérifie les fabriques pour `png`, le sous-type vide et l’argument Java `null`, avec des résultats explicites. Le XML attribue la détection à cette méthode de test ; il ne précise pas l’assertion ou l’exception qui a arrêté son exécution. |
| `isSimpleName`, ligne 293, index 68 — `returns.BooleanTrueReturnValsMutator` | `MediaType_image_2_0_Test.testImageMethod` | Le retour final est forcé à `true`, y compris pour un nom vide. `assertNull(MediaType.image(""))` détecte l’acceptation indue du sous-type vide. |
| `parse`, ligne 247 : supprimer le contrôle de nullité | `testParse_NullInput` | L’entrée `null` doit retourner `null` sans exception. |
| `parse`, ligne 265 : modifier la branche du cache | `testParse_SimpleCache` | Deux parsings du même type simple doivent retourner la même instance. |
| `parse`, ligne 277 : modifier une condition de reconnaissance | `testImageMethod` | Les assertions sur les résultats de `image("")` et `image(null)` distinguent le comportement modifié. |

Les noms de mutateurs ci-dessus sont les suffixes exacts du préfixe `org.pitest.mutationtest.engine.gregor.mutators.` dans le XML. Les indices sont les valeurs de `<indexes><index>`, pas des numéros de ligne. Les trois identités `isSimpleName` permettent ainsi de distinguer les deux mutations situées ligne 293.

Les tests générés corrigés ne détectent donc pas tous les mutants :
cinq survivent et onze ne sont pas couverts ; un autre provoque un timeout.

Preuves : [rapport HTML](rapports-pit/mediatype-generated-01/index.html), [mutants XML](rapports-pit/mediatype-generated-01/mutations.xml) et [journal PIT](chatunitest-local/mediatype/logs/pit-generated-01.log).

### Tests manuels ciblés

Huit tests ont été élaborés avec l’aide de ChatGPT/Codex dans [MediaTypeManualTest.java](tika-core/src/test/java/org/apache/tika/mime/MediaTypeManualTest.java), à partir des mutants restants et du code de `MediaType`. Ils sont séparés des tests générés.

| Test | Intention et données | Résultat attendu |
|---|---|---|
| `charsetBeforeMediaTypeIsPreserved` | Parser `"charset=UTF-8; text/plain"`. | Type `text`, sous-type `plain`, charset `UTF-8`. |
| `setOfMediaTypesRemovesDuplicatesAndNulls` | Fournir deux types, un doublon et `null`. | Exactement deux types distincts ; ensemble non modifiable. |
| `setOfStringsParsesAndFiltersInvalidValues` | Fournir deux types sous forme de chaînes, un doublon, `null` et `"invalid"`. | Exactement les deux types valides ; ensemble non modifiable. |
| `addingParametersToBaseTypePreservesValues` | Ajouter `charset=UTF-8` à `TEXT_PLAIN`. | Paramètre conservé et type de base `text/plain`. |
| `addingEmptyParametersPreservesExistingValues` | Ajouter une map vide à un type avec charset. | Valeur du type et paramètres conservés. |
| `addingParametersMergesAndOverridesValues` | Fusionner les paramètres en remplaçant le charset et en ajoutant une version. | Ancien format conservé, charset remplacé, version ajoutée ; objet de base inchangé. |
| `videoFactoryCreatesExpectedMediaType` | Appeler `video("mp4")`. | Type `video`, sous-type `mp4`, représentation `video/mp4`. |
| `baseTypeWithoutParametersReturnsSameInstance` | Appeler `getBaseType()` sur un objet créé sans paramètres. | La même instance est retournée ; cette assertion vérifie le comportement actuel du code. |

La validation ciblée donne **37 tests réussis : huit manuels et 29 générés corrigés**, sans échec, erreur ou test ignoré, avec `BUILD SUCCESS`.

Preuves : [version manuelle sauvegardée](chatunitest-local/mediatype/manual/version-01/MediaTypeManualTest.java), [rapports Surefire](chatunitest-local/mediatype/manual/version-01/surefire-reports/) et [journal de validation](chatunitest-local/mediatype/logs/validation-manual-01.log).

### Compléments manuels et bilan PIT

Deux tests supplémentaires ont porté la suite manuelle à dix tests :

| Test | Intention et données | Résultat attendu |
|---|---|---|
| `newlyParsedSimpleTypeEndingInZIsCached` | Parser deux fois un type simple dédié se terminant par `z`. | Même valeur et même instance retournée. |
| `simpleNameAcceptsBoundaryCharactersAndRejectsEmptyName` | Invoquer `isSimpleName` par réflexion avec les caractères limites acceptés et les caractères voisins exclus. | Acceptation de `a`, `z`, `0`, `9`, `-`, `+`, `.`, `_` ; rejet du nom vide, des majuscules, espaces et caractères hors limites. |

Le test du cache seul n’a pas amélioré le score PIT. Un cache déjà rempli peut éviter l’appel à `isSimpleName`. Le test par réflexion a permis de vérifier cette méthode indépendamment du cache ; il dépend toutefois de son nom et de sa visibilité privée actuels.

| Mesure | Tests existants | Avec générés corrigés | Avec dix tests manuels |
|---|---:|---:|---:|
| Mutants générés | 85 | 85 | 85 |
| `KILLED` | 60 | 68 | 81 |
| `SURVIVED` | 10 | 5 | 3 |
| `NO_COVERAGE` | 14 | 11 | 0 |
| `TIMED_OUT` | 1 | 1 | 1 |
| Score strict | 70,59 % | 80,00 % | 95,29 % |
| Score incluant le timeout | 71,76 % | 81,18 % | 96,47 % |
| Couverture des lignes PIT | 80,77 % | 84,62 % | 154/156 = 98,72 % |

Les versions manuelles 01 et 02 ont obtenu 79 mutants `KILLED`, la version 03 en a obtenu 80 et la version 04 en a obtenu 81. Le gain final est de 13 mutants `KILLED` par rapport à la suite avec tests générés corrigés, et de 21 par rapport à la mesure initiale.

### Analyse des mutants restants

Trois mutants ont été considérés comme équivalents pour les résultats observables via l’API publique, après examen du code :

| Méthode et ligne | Mutation | Justification |
|---|---|---|
| `parse`, ligne 257 | Remplacer le retour par `null`. | Cette instruction retourne déjà `null` lorsque la chaîne ne contient aucun `/`. |
| `union`, ligne 349 | Forcer le premier test de map vide à être faux. | Lorsque la première map est vide, la fusion avec la seconde conserve les mêmes entrées. |
| `union`, ligne 351 | Forcer le second test de map vide à être faux. | Lorsque la seconde map est vide, la copie de la première conserve les mêmes entrées. Le constructeur recopie les paramètres, ce qui masque la différence d’identité de la map intermédiaire. |

PIT conserve ces mutations au statut `SURVIVED` ; l’équivalence est une conclusion de notre analyse, pas un statut attribué automatiquement par PIT.

Le mutant `TIMED_OUT` dans `parseParameters`, ligne 305, transforme la condition de boucle `length() > 0` en `length() >= 0`. La boucle continue alors sur une chaîne vide sans progresser, ce qui explique le dépassement du délai.

Preuves : [version manuelle finale et rapports Surefire](chatunitest-local/mediatype/manual/version-04/), [journal de validation](chatunitest-local/mediatype/logs/validation-manual-04.log), [rapport PIT final](rapports-pit/mediatype-manual-04/index.html), [mutants XML](rapports-pit/mediatype-manual-04/mutations.xml) et [journal PIT](chatunitest-local/mediatype/logs/pit-manual-04.log).

### Motivation des données et justification des oracles manuels

Les résultats attendus ont été déterminés à partir du code de MediaType,
puis exprimés par des valeurs explicites dans les assertions.

| Test | Motivation des données et justification de l’oracle |
|---|---|
| `charsetBeforeMediaTypeIsPreserved` | Le charset placé avant le type exerce la branche de réorganisation du parseur ; les assertions vérifient que le type et le paramètre sont conservés. |
| `setOfMediaTypesRemovesDuplicatesAndNulls` | Deux types distincts, un doublon et `null` distinguent conservation, déduplication et filtrage ; l’ensemble attendu contient exactement les deux types valides. L’ajout doit lever une exception car le résultat est non modifiable. |
| `setOfStringsParsesAndFiltersInvalidValues` | Les chaînes valides, le doublon, `null` et une chaîne sans `/` exercent le parsing et le filtrage ; seuls les deux types valides doivent rester, dans un ensemble non modifiable. |
| `addingParametersToBaseTypePreservesValues` | Un paramètre unique sur un type sans paramètres exerce l’ajout initial ; la map attendue contient uniquement le charset fourni et le type de base reste text/plain. |
| `addingEmptyParametersPreservesExistingValues` | Une map ajoutée vide exerce le cas sans nouvelles entrées ; le charset existant et la valeur du type doivent rester inchangés. |
| `addingParametersMergesAndOverridesValues` | Une clé commune, une ancienne clé distincte et une nouvelle clé exercent remplacement et fusion ; la map attendue contient les trois valeurs et la map de l’objet initial reste inchangée. |
| `videoFactoryCreatesExpectedMediaType` | Le sous-type simple mp4 exerce la fabrique video, absente des fichiers générés exportés ; les composants et la représentation doivent être video/mp4. |
| `baseTypeWithoutParametersReturnsSameInstance` | Un type sans paramètres exerce le retour direct de getBaseType() ; assertSame vérifie l’identité prévue par cette branche du code. |
| `newlyParsedSimpleTypeEndingInZIsCached` | Un nom dédié terminé par z vise la borne supérieure des lettres minuscules ; deux appels identiques doivent conserver la valeur et l’identité via le cache. |
| `simpleNameAcceptsBoundaryCharactersAndRejectsEmptyName` | Les bornes a/z et 0/9, les signes autorisés et leurs voisins exclus distinguent les conditions de reconnaissance ; les valeurs booléennes attendues suivent les caractères autorisés par le code. |

### Validation complète de tika-core

L’exécution complète avec le profil `chatunitest-verify` a comptabilisé **876 tests, aucun échec, aucune erreur et deux tests ignorés**, avec `BUILD SUCCESS` et un code de sortie Maven égal à `0`.

Elle inclut les tests existants, les tests générés corrigés et les tests manuels des expériences EndianUtils et MediaType. MediaType ajoute 29 tests générés corrigés et dix tests manuels.

Preuves : [journal complet](chatunitest-local/mediatype/logs/validation-final.log) et [rapports Surefire sauvegardés](chatunitest-local/mediatype/validation-final/surefire-reports/).

### Validation GitHub Actions

Le workflow [Tache 2 - tika-core tests, exécution 36652594733](https://github.com/nass1379/tika/actions/runs/36652594733)
a réussi sur la branche `mediatype`, au commit `9e1f2ab`.

Les rapports Surefire confirment 876 tests, aucun échec, aucune erreur
et deux tests ignorés. Les 29 tests générés corrigés et les dix tests
manuels MediaType ont tous réussi, sans test ignoré.

### Correction des contrôles de licence et de style — 1er octobre 2026

Le [build général Java 17 du 30 septembre](https://github.com/nass1379/tika/actions/runs/36782556998/job/110116114035#step:5:500) a échoué au contrôle Apache RAT dans `tika-core` : 28 fichiers ajoutés ne portaient pas d’en-tête de licence reconnu. Il s’agissait des 18 fichiers générés EndianUtils, des neuf fichiers générés MediaType et de `MediaTypeManualTest.java`. Cet échec concernait bien les ajouts de l’expérience, même si leur exécution était réussie dans la CI dédiée, qui désactivait RAT.

Les 28 copies actives portent désormais l’en-tête Apache 2.0 du dépôt. Les imports statiques de `MediaTypeManualTest` sont explicites et sa fin de fichier a été corrigée pour Checkstyle. Aucun comportement de test, donnée ou assertion n’a été modifié. Les archives brutes, les versions corrigées sauvegardées et les rapports historiques sont conservés tels quels. Leurs empreintes décrivent ces versions historiques ; les fichiers actifs diffèrent désormais aussi par ces en-têtes et ajustements de forme. Cela n’ajoute aucune correction sémantique aux 11 méthodes EndianUtils et sept méthodes MediaType déjà recensées.

Le workflow dédié comporte maintenant une étape explicite avant la compilation :

```bash
./mvnw -B -ntp -pl tika-core apache-rat:check spotless:check checkstyle:check
```

Les commandes de build documentées plus haut gardent leurs options de désactivation pour éviter de répéter certains contrôles ; RAT et Checkstyle sont toutefois exécutés et bloquants dans cette étape distincte. OSS Index reste désactivé. Validation locale après correction : Apache RAT, Spotless et Checkstyle réussissent ; les 876 tests exécutés après nettoyage ne présentent aucun échec ni erreur, avec deux tests ignorés.

L’autre échec examiné, le [build Java 25 du 28 septembre](https://github.com/nass1379/tika/actions/runs/36482829178/job/109132392395), se situe dans `tika-pipes-s3-integration-tests` : `S3PipeIntegrationTest.setupMinio` rencontre une `ContainerLaunchException` au démarrage Docker Compose, précédée d’un délai dépassé lors de la récupération de `quay.io/minio/minio:latest`. Ce problème d’intégration S3 n’est pas corrigé par l’ajout des en-têtes. La réussite du workflow dédié ne signifie pas que tous les workflows généraux de Tika sont verts.

### Publication Docker sur le fork du cours

Le workflow `Docker snapshot - tika-server and tika-grpc` publie les images officielles `apache/tika` et `apache/tika-grpc` sur Docker Hub. Son [exécution du 1er octobre](https://github.com/nass1379/tika/actions/runs/36917347969/job/110554632939#step:9:9) a échoué à l’étape `Login to Docker Hub` avec `Username and password required`, faute des identifiants de publication dans notre fork.

Le premier job `gate` de [docker-snapshot.yml](.github/workflows/docker-snapshot.yml) est désormais conditionné par `github.repository == 'apache/tika'`. Sur `nass1379/tika`, ce job est ignoré, de même que le job de publication `build` qui en dépend. Aucun identifiant Docker Hub n’est nécessaire pour la tâche 2. Les anciennes exécutions échouées restent visibles dans l’historique.

Ce choix concerne uniquement le workflow de publication des snapshots Docker. Le workflow `Tache 2 - tika-core tests`, ses tests originaux, générés et manuels, et ses contrôles RAT, Spotless et Checkstyle restent actifs. Les autres workflows de tests restent également actifs. La publication Docker n’est pas demandée par l’énoncé ; le critère d’exécution porte sur la réussite des nouveaux tests dans GitHub Actions. Cette restriction ne corrige pas le problème distinct de démarrage Docker/MinIO du test d’intégration S3.

<!-- IFT3913-TACHE2-END -->

---

Welcome to Apache Tika  <https://tika.apache.org/>
=================================================

[![license](https://img.shields.io/github/license/apache/tika.svg?maxAge=2592000)](http://www.apache.org/licenses/LICENSE-2.0)
[![Jenkins](https://img.shields.io/jenkins/s/https/ci-builds.apache.org/job/Tika/job/tika-main-jdk17.svg?maxAge=3600)](https://ci-builds.apache.org/job/Tika/job/tika-main-jdk17/)
[![Jenkins tests](https://img.shields.io/jenkins/t/https/ci-builds.apache.org/job/Tika/job/tika-main-jdk17.svg?maxAge=3600)](https://ci-builds.apache.org/job/Tika/job/tika-main-jdk17/lastBuild/testReport/)
[![Maven Central](https://img.shields.io/maven-central/v/org.apache.tika/tika.svg?maxAge=86400)](http://search.maven.org/#search|ga|1|g%3A%22org.apache.tika%22)

Apache Tika(TM) is a toolkit for detecting and extracting metadata and structured text content from various documents using existing parser libraries.

Tika is a project of the [Apache Software Foundation](https://www.apache.org).

Apache Tika, Tika, Apache, the Apache feather logo, and the Apache Tika project logo are trademarks of The Apache Software Foundation.

Quick Start
===========

**Parse a file in Java:**

```java
import org.apache.tika.Tika;

Tika tika = new Tika();
String text = tika.parseToString(new File("document.pdf"));
System.out.println(text);
```

**From the command line:**

```bash
java -jar tika-app-*.jar --text document.pdf
```

**Maven dependency:**

```xml
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-parsers-standard-package</artifactId>
    <version>4.x.y</version>
    <type>pom</type>
</dependency>
```

Getting Started
===============
Pre-built binaries of Apache Tika standalone applications are available
from https://tika.apache.org/download.html . Pre-built binaries of all the
Tika jars can be fetched from Maven Central or your favourite Maven mirror.

**Tika 2.X and support for Java 8 reached End of Life (EOL) in April, 2025. 
See [Tika Roadmap 2.x, 3.x and beyond](https://cwiki.apache.org/confluence/display/TIKA/Tika+Roadmap+--+2.x%2C+3.x+and+Beyond).** 

Tika is based on **Java 17** and uses the [Maven 3](https://maven.apache.org) build system.
**N.B.** [Docker](https://www.docker.com/products/personal) is used for tests in tika-integration-tests. If Docker is not installed, those tests are skipped.

To build Tika from source, use the following command in the main directory:

    ./mvnw clean install

The Maven wrapper (`mvnw`) is included in the repository and will automatically download
the correct Maven version if needed. On Windows, use `mvnw.cmd` instead.

The build consists of a number of components, including a standalone runnable jar that you can use to try out Tika features. You can run it like this:

    java -jar tika-app/target/tika-app-*.jar --help


To build a specific project (for example, tika-server-standard):

    ./mvnw clean install -am -pl :tika-server-standard

If the ossindex-maven-plugin is causing the build to fail because a dependency
has now been discovered to have a vulnerability:

    ./mvnw clean install -Dossindex.skip


Faster Builds
=============

**Fast profile** - Use `-Pfast` to skip tests, checkstyle, and spotless:

    ./mvnw clean install -Pfast

**Parallel builds** - Add `-T1C` to build with 1 thread per CPU core:

    ./mvnw clean install -Pfast -T1C

**Maven Daemon (mvnd)** - Keeps a warm JVM running for 2-3x faster rebuilds:

```bash
# Install: https://github.com/apache/maven-mvnd
# macOS: brew install mvndaemon/tap/mvnd

# Use exactly like mvn
mvnd clean install -Pfast
mvnd test -pl :tika-core
```

**Combine both** for maximum speed during development:

    mvnd clean install -Pfast -T1C


Reproducible Builds
===================

Apache Tika supports [reproducible builds](https://reproducible-builds.org/). This means
that building the same source code with the same JDK version should produce
byte-for-byte identical artifacts, regardless of the build machine or time.

Key configuration:
- `project.build.outputTimestamp` is set in `tika-parent/pom.xml`
- All Maven plugins are configured to produce deterministic output

To verify the build plan supports reproducibility:

    ./mvnw artifact:check-buildplan

To verify two builds produce identical artifacts:

    ./mvnw clean install -DskipTests
    mv ~/.m2/repository/org/apache/tika tika-build-1
    ./mvnw clean install -DskipTests
    diff -r tika-build-1 ~/.m2/repository/org/apache/tika


Maven Dependencies
==================

Apache Tika provides *Bill of Material* (BOM) artifact to align Tika module versions and simplify version management. 
To avoid convergence errors in your own project, import this
bom or Tika's parent pom.xml in your dependency management section.

If you use Apache Maven:

```xml
<project>
  <dependencyManagement>
    <dependencies>
      <dependency>
       <groupId>org.apache.tika</groupId>
       <artifactId>tika-bom</artifactId>
       <version>4.x.y</version>
       <type>pom</type>
       <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <dependency>
      <groupId>org.apache.tika</groupId>
      <artifactId>tika-parsers-standard-package</artifactId>
      <type>pom</type>
      <!-- version not required since BOM included -->
    </dependency>
  </dependencies>
</project>
```

For Gradle:

```kotlin
dependencies {
  implementation(platform("org.apache.tika:tika-bom:4.x.y"))

  // version not required since bom (platform in Gradle terms)
  implementation("org.apache.tika:tika-parsers-standard-package@pom")
}
```

Migrating to 4.x
================
TBD

Contributing
============
See [CONTRIBUTING.md](CONTRIBUTING.md) and https://tika.apache.org/contribute.html

[![contributors](https://contributors-img.web.app/image?repo=apache/tika)](https://github.com/apache/tika/graphs/contributors)

Building from a Specific Tag
============================
Let's assume that you want to build the 3.0.1 tag:
```
0. Download and install hub.github.com
1. git clone https://github.com/apache/tika.git
2. cd tika
3. git checkout 3.0.1
4. ./mvnw clean install
```

If a new vulnerability has been discovered between the date of the
tag and the date you are building the tag, you may need to build with:

```
4. ./mvnw clean install -Dossindex.skip
```

If a local test is not working in your environment, please notify
 the project at dev@tika.apache.org. As an immediate workaround,
 you can turn off individual tests with e.g.:

```
4. ./mvnw clean install -Dossindex.skip -Dtest=\!UnpackerResourceTest#testPDFImages
```

License (see also LICENSE.txt)
==============================

Collective work: Copyright 2011 The Apache Software Foundation.

Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at

<https://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.

Apache Tika includes a number of subcomponents with separate copyright notices and license terms. Your use of these subcomponents is subject to the terms and conditions of the licenses listed in the LICENSE.txt file.

Export Control
==============

This distribution includes cryptographic software.  The country in which you currently reside may have restrictions on the import, possession, use, and/or re-export to another country, of encryption software.  BEFORE using any encryption software, please  check your country's laws, regulations and policies concerning the import, possession, or use, and re-export of encryption software, to  see if this is permitted.  See <http://www.wassenaar.org/> for more information.

The U.S. Government Department of Commerce, Bureau of Industry and Security (BIS), has classified this software as Export Commodity Control Number (ECCN) 5D002.C.1, which includes information security software using or performing cryptographic functions with asymmetric algorithms.  The form and manner of this Apache Software Foundation distribution makes it eligible for export under the License Exception ENC Technology Software Unrestricted (TSU) exception (see the BIS Export Administration Regulations, Section 740.13) for both object code and source code.

The following provides more details on the included cryptographic software:

Apache Tika uses the Bouncy Castle generic encryption libraries for extracting text content and metadata from encrypted PDF files.  See <http://www.bouncycastle.org/> for more details on Bouncy Castle.  

Mailing Lists
=============

* user@tika.apache.org - About using Tika
* dev@tika.apache.org - About developing Tika

Subscribe by sending a message to `{list}-subscribe@tika.apache.org`.

Issue Tracker
=============

https://issues.apache.org/jira/browse/TIKA

Security
========

See [SECURITY.md](SECURITY.md) and https://tika.apache.org/security.html
