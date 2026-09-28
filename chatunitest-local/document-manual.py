from pathlib import Path
from collections import Counter, defaultdict
import hashlib
import json
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent.parent
base = json.loads((ROOT / 'rapports-pit/comparison-endian.json').read_text())['mutants']
def key(m):
    return tuple(m[t] for t in ('mutatedClass','mutatedMethod','methodDescription','lineNumber','mutator')) + (tuple(m['indexes']),)
previous = {key(m):m for m in base}
final = []
for n in ET.parse(ROOT/'rapports-pit/endian-after-manual/mutations.xml').getroot():
    m = {t:n.findtext(t,'') for t in ('mutatedClass','mutatedMethod','methodDescription','lineNumber','mutator','description','killingTest')}
    m['indexes'] = [int(i.text) for i in n.findall('indexes/index')]
    old = previous[key(m)]
    assert m['description'] == old['description']
    m.update(id=old['id'], before_manual=old['status'], status=n.get('status'))
    final.append(m)
assert len(final) == 206 and {key(m) for m in final} == set(previous)
assert Counter(m['status'] for m in final) == {'KILLED':204,'SURVIVED':2}
new = sorted([m for m in final if m['status']=='KILLED' and m['before_manual']!='KILLED'], key=lambda m:m['id'])
assert len(new) == 101
assert all(m['status']=='KILLED' for m in final if m['before_manual']=='KILLED')
def test(m):
    assert '[class:org.apache.tika.io.EndianUtilsManualTest]' in m['killingTest']
    return re.search(r'\[method:([^]]+)\]',m['killingTest']).group(1).split('(')[0]
bytest=defaultdict(list)
for m in new: bytest[test(m)].append(m['id'])

# Intent, test data and rationale, independent oracle / distinguishing behavior.
cases = {
 'testReadShortBEValues': (
  'Décoder un entier signé BE16 à partir d’un flux.',
  '`12 34` distingue les poids des deux octets ; `FF FE` vérifie l’interprétation signée.',
  '`0x1234` = 4660 pour le premier flux ; le motif 16 bits `0xFFFE` vaut -2 en complément à deux. Ce test traverse aussi readUShortBE.'),
 'testReadUnsignedShortBEValues': (
  'Vérifier directement la variante non signée BE16.',
  'Les mêmes flux `12 34` et `FF FE` distinguent la variante non signée de readShortBE.',
  'Résultats 4660 et 65534 : les octets représentent 18 × 256 + 52 et 255 × 256 + 254. PIT peut retenir le test précédent comme tueur des mêmes mutants ; aucune détection supplémentaire ne lui est attribuée dans ce passage.'),
 'testReadIntLEValues': (
  'Décoder LE32 signé sur un flux.',
  '`12 34 56 78` utilise quatre contributions distinctes non nulles ; `FE FF FF FF` exerce le signe.',
  'Attendus `0x78563412` et -2. Changer un décalage, soustraire une contribution ou retourner zéro viole ces valeurs calculées par les poids LE.'),
 'testReadIntBEValues': (
  'Décoder BE32 signé sur un flux.',
  '`12 34 56 78` impose quatre poids distincts ; `FF FF FF FE` encode une valeur négative.',
  'Attendus `0x12345678` et -2. Les octets sont pondérés par 2^24, 2^16, 2^8 et 1.'),
 'testReadLongLEValues': (
  'Décoder un entier LE64, y compris les positions au-delà de 32 bits.',
  '`01 02 03 04 05 06 07 08` donne un rôle observable à chacun des huit octets ; `FE FF FF FF FF FF FF FF` contrôle le signe.',
  'Attendus `0x0807060504030201L` et -2L. Le premier oracle est la somme des octets pondérés par 2^(8j), j de 0 à 7 ; les mutations des décalages et additions ne préservent pas cette somme.'),
 'testReadLongBEValues': (
  'Décoder un entier BE64, avec poids fort et signe.',
  '`01 02 03 04 05 06 07 08`, puis `FF FF FF FF FF FF FF FE`.',
  'Attendus `0x0102030405060708L` et -2L, calculés avec les poids 2^(8(7-j)). Les huit contributions non nulles distinguent les changements arithmétiques.'),
 'testFixedWidthZeroIsValid': (
  'Ne pas confondre des octets valides égaux à zéro avec une fin de flux.',
  'Pour chacune des onze fonctions listées ci-dessous, exactement le nombre requis d’octets `00`. Le OR de tous ces octets vaut zéro, frontière précise des contrôles de fin de flux.',
  'Attendu 0L sans exception pour chaque lecture. Remplacer le contrôle `< 0` par `<= 0` provoque une BufferUnderrunException injustifiée. Les courts et int sont promus en long uniquement pour partager l’assertion.'),
 'testFixedWidthTruncationAtEveryPosition': (
  'Refuser toute lecture incomplète sur un flux dont la fin est permanente.',
  'Pour une largeur n, tester chaque longueur de 0 à n-1 ; préfixe rempli de `12`, puis EOF de ByteArrayInputStream. Au total 44 cas pour onze lecteurs.',
  'Chaque appel doit lancer EndianUtils.BufferUnderrunException. Le type est spécifique, pas Exception générique. Les préfixes non nuls distinguent les contrôles supprimés et le dernier OR remplacé par AND ; les AND plus tôt peuvent être masqués par les EOF ultérieurs.'),
 'testIntBEArrayWithoutOffset': (
  'Exercer explicitement la surcharge getIntBE(byte[]), auparavant non couverte.',
  'Tableau `12 34 56 78`, sans argument offset. Les quatre octets non nuls empêchent un retour constant zéro de passer.',
  'Attendu `0x12345678`. Le test atteint la surcharge de délégation elle-même, pas seulement getIntBE(byte[], int).'),
 'testUnsignedArrayWrappersReturnNonZero': (
  'Exercer les deux surcharges sans offset getUIntBE/LE avec un résultat non nul et supérieur à Integer.MAX_VALUE.',
  'BE : `FE DC BA 98` ; LE : `98 BA DC FE`. Le bit 31 vaut 1, tandis que la valeur attendue reste positive dans le long.',
  'Attendu `0xFEDCBA98L` = 4275878552 pour les deux. Les mutants qui forcent le résultat de la surcharge sans offset à zéro sont alors détectés, contrairement aux données nulles de certains tests générés.'),
 'testGetUByteAtOffset': (
  'Lire un octet non signé à un offset donné.',
  'Tableau `55 80 FF 00`, offsets 1, 2 et 3. Le préfixe 55 révèle un mauvais indice ; 80 et FF représentent des byte Java négatifs.',
  'Attendus 128, 255 et 0. AND→OR donne 255 pour 80 au lieu de 128 ; un retour forcé à zéro échoue dès le premier cas.'),
 'testReadUE7ZeroTerminalPreservesNextByte': (
  'Accepter la valeur UE7 zéro et ne pas consommer l’octet suivant.',
  'Flux `00 55` : 00 est un terminal valide, 55 est une sentinelle hors de l’encodage.',
  'readUE7 doit retourner 0L ; la lecture suivante doit rendre 0x55. Ce cas protège le terminal nul sans préfixe de continuation.'),
 'testReadUE7RejectsPrematureEnd': (
  'Refuser une absence de valeur et une continuation sans octet suivant.',
  'Flux vide, puis flux réduit à `81`. Le bit haut de 81 annonce une continuation absente.',
  'IOException dans les deux cas, conformément au contrôle EOF de readUE7. Supprimer le contrôle final retournerait un accumulateur au lieu de signaler l’entrée incomplète.'),
 'testReadUE7SixBytePayload': (
  'Lire un encodage valide de six groupes de sept bits et préserver le flux suivant.',
  '`81 82 83 84 85 06 55` : cinq continuations, un terminal 06, puis la sentinelle 55. Les charges utiles 1 à 6 sont distinctes et non nulles.',
  'Attendu 34902966918L = 1×128^5 + 2×128^4 + 3×128^3 + 4×128^2 + 5×128 + 6 ; la lecture suivante donne 0x55.'),
 'testReadUE7SixByteLimitConsumesLookahead': (
  'Caractériser la limite existante de six groupes sur une entrée trop longue.',
  '`81 82 83 84 85 86 07 55` : le sixième groupe annonce encore une continuation. Le septième octet 07, suivi de 55, distingue l’ajout d’un groupe et la consommation du flux.',
  'Le code actuel accumule les six charges utiles 1 à 6 (34902966918L) et lit puis écarte 07 avant de sortir ; la lecture suivante rend 55. Une borne élargie ou un compteur décrémenté accumule aussi 07 et donne une autre valeur. Il s’agit d’un oracle de caractérisation tiré de la limite explicite du code, pas d’une exigence indépendante approuvant la troncature silencieuse.'),
 'testReadUE7ZeroAfterContinuation': (
  'Appliquer le poids du terminal nul après un préfixe non nul.',
  '`81 00 55` : contrairement au cas 00 seul, l’accumulateur vaut déjà 1 avant le terminal nul.',
  'Attendu 1×128 + 0 = 128L et sentinelle 55 intacte. La condition de boucle `> 0` quitte trop tôt avec 1 ; le contrôle final `<= 0` lance une exception injustifiée. Le zéro initial seul ne distinguait pas la première mutation.'),
 'testFixedWidthRejectsEofEvenIfFileGrows': (
  'Refuser une lecture qui a rencontré EOF, même si des octets apparaissent lors des lectures suivantes.',
  'Pour chaque lecteur de largeur n et position p de 0 à n-1, créer un fichier contenant p octets 12. Un FilterInputStream autour d’un vrai FileInputStream observe -1, ajoute ensuite n-p-1 octets 34 au fichier, puis retourne le -1 réellement lu. Les lectures suivantes voient les nouveaux octets. Il y a 44 cas, synchrones et sans attente ni thread concurrent ; @TempDir isole les fichiers.',
  'BufferUnderrunException reste attendue : l’un des n appels n’a pas fourni d’octet. Un OR garde le bit de signe de -1 ; un AND intermédiaire peut l’effacer avec un octet positif, et aucun EOF ultérieur ne le réintroduit. Ce scénario réel de fichier évolutif distingue les 22 AND précoces encore survivants sur les fichiers statiques tronqués. Il ne retourne ni valeur négative différente de -1 ni faux octet hors de 0..255.'),
}
source=ROOT/'tika-core/src/test/java/org/apache/tika/io/EndianUtilsManualTest.java'
names=re.findall(r'void (test\w+)\(',source.read_text())
assert set(names)==set(cases) and len(names)==17
ids={name:f'H{i:02d}' for i,name in enumerate(cases,1)}
parts=['<!-- MANUAL-TESTS-START -->','### Tests supplémentaires après analyse des mutants','',
 'Les tests de cette étape sont dans [EndianUtilsManualTest.java](tika-core/src/test/java/org/apache/tika/io/EndianUtilsManualTest.java), distincts des fichiers produits par ChatUniTest et des tests originaux. Ils ont été conçus et ajoutés avec l’assistance de Codex, **hors du pipeline ChatUniTest/Ollama** : les présenter comme des tests rédigés sans assistance IA serait inexact. Ils constituent ici la suite supplémentaire guidée par l’analyse des mutants.', '',
 'Aucune classe de production ni aucun test original n’a été modifié. Les 71 tests générés restent à leur état corrigé précédemment documenté. Les 17 nouveaux tests ne remplacent aucun test existant.', '',
 '#### Itérations observées', '',
 '- Premier passage : 15 tests supplémentaires, suite de 835 tests dont 2 ignorés, zéro échec/erreur. PIT tue 181/206 mutants (87,86 %), avec 25 survivants et aucun non couvert. [Rapport intermédiaire](rapports-pit/endian-manual-first-pass/index.html), [journal](chatunitest-local/logs/pit-manual-first-pass.log).',
 '- Deuxième passage : ajout de `testReadUE7ZeroAfterContinuation` et `testFixedWidthRejectsEofEvenIfFileGrows`. Suite de 837 tests dont 2 ignorés, zéro échec/erreur ; PIT tue 204/206 mutants (99,03 %), avec deux survivants et aucun non couvert.', '',
 'Le premier passage utilisait des flux statiques : dès EOF, toutes les lectures suivantes renvoyaient -1. Cela masquait plusieurs mutations OR→AND précoces : un OR ultérieur avec -1 rétablissait le contrôle d’erreur. Le test avec un fichier qui s’allonge retire précisément cette hypothèse. Le contrat de [InputStream.read()](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/InputStream.html#read()) fournit un octet entre 0 et 255 ou -1 à la fin du flux ; notre wrapper transmet la valeur réellement renvoyée par FileInputStream et modifie le fichier entre deux lectures. La détection a été mesurée avec ce test, et non déduite d’un mock arbitraire.', '',
 '#### Catalogue des 17 tests : intention, données et oracle', '',
 'Les tests collectifs de largeur fixe utilisent : readShortLE, readShortBE, readUShortLE et readUShortBE (2 octets) ; readUIntLE, readUIntBE, readIntLE, readIntBE et readIntME (4 octets) ; readLongLE et readLongBE (8 octets). Les cas de troncature couvrent donc 4×2 + 5×4 + 2×8 = 44 combinaisons. JUnit compte chaque boucle comme une seule méthode de test ; les messages d’assertion indiquent le lecteur et la longueur/position défaillante.', '']
for name,(intent,data,oracle) in cases.items():
    mids=bytest.get(name,[])
    parts += [f'**{ids[name]} — `{name}`**', '',f'- Intention : {intent}',
              f'- Données et motivation : {data}',f'- Oracle et raison de la détection : {oracle}',
              f'- Nouveaux mutants tués attribués par PIT : {len(mids)}'+ (f" ({', '.join(mids)})." if mids else '. Aucun mutant supplémentaire ne lui est attribué dans ce rapport ; cela ne signifie pas que le test ne peut en tuer aucun.'),'']
parts += ['#### Résultats finaux de validation et de mutation','',
 '| Mesure | Originaux | + Générés corrigés | + 17 tests supplémentaires |',
 '|---|---:|---:|---:|',
 '| Mutants | 206 | 206 | 206 |', '| Tués | 36 | 103 | 204 |',
 '| Survivants | 16 | 21 | 2 |', '| Non couverts | 154 | 82 | 0 |',
 '| Score brut | 17,48 % | 50,00 % | **99,03 %** |',
 '| Lignes couvertes PIT | 31/121 | 74/121 | 120/121 |', '',
 '**101 mutants supplémentaires sont tués depuis l’étape générée corrigée**, soit +49,03 points. Les 103 mutants déjà tués restent tués. Les 82 non couverts deviennent tous tués ; parmi les 21 survivants, 19 sont tués et deux restent survivants. Aucun timeout, erreur d’exécution ou mutant non viable ne contribue au score.', '',
 'La suite complète recense **837 tests = 749 originaux + 71 générés corrigés + 17 supplémentaires** : 835 réussis, 2 ignorés, zéro échec et zéro erreur. [Journal des tests](chatunitest-local/logs/all-tests-with-manual.log), [rapport JUnit des 17 tests](chatunitest-local/manual/surefire-reports/TEST-org.apache.tika.io.EndianUtilsManualTest.xml).', '',
 'Le [rapport HTML final](rapports-pit/endian-after-manual/index.html), le [XML final](rapports-pit/endian-after-manual/mutations.xml) et le [journal PIT final](chatunitest-local/logs/pit-after-manual.log) sont distincts des mesures précédentes. Les commandes exécutées après modification des sources sont :','',
 '```bash',
 'mvn -B -pl tika-core -Pchatunitest-verify test \\',
 '  -Drat.skip=true -Dcheckstyle.skip=true -Dossindex.skip=true', '',
 'mvn -B -pl tika-core -Pchatunitest-verify test-compile \\',
 '  org.pitest:pitest-maven:1.25.9:mutationCoverage \\',
 '  -DtargetClasses=org.apache.tika.io.EndianUtils \\',
 '  -Drat.skip=true -Dcheckstyle.skip=true -Dossindex.skip=true', '```','',
 'Le plugin, ses opérateurs et la classe ciblée sont inchangés. Le `clean` avait été exécuté avant la mesure générée corrigée ; cette étape n’a ajouté qu’une classe de test et n’a supprimé/renommé aucun test. Le contrôle des 206 identités (méthode, surcharge JVM, ligne, opérateur et indices bytecode) confirme la même population dans les trois rapports. La compilation et les tests sont validés localement ; le workflow CI est configuré et validé localement ainsi que sur GitHub (voir la section « Intégration continue »).', '',
 '#### Attribution exacte des 101 nouvelles détections','',
 'Les identifiants M restent ceux de la comparaison précédente. H renvoie au catalogue ci-dessus, qui donne les données, l’oracle et le mécanisme de détection. `killingTest` est le test retenu par PIT, pas nécessairement le seul test capable de tuer le mutant. La [comparaison finale JSON](rapports-pit/comparison-endian-manual.json) conserve les noms complets et permet de vérifier chaque attribution. Reproduction de cette section : `python3 chatunitest-local/document-manual.py`.', '',
 '| Mutant | Méthode, ligne et index | Opérateur et transformation | Statut avant tests supplémentaires | Test tueur et oracle |',
 '|---|---|---|---|---|']
for m in new:
    parts.append(f"| {m['id']} | `{m['mutatedMethod']}{m['methodDescription']}`, L{m['lineNumber']}, i={','.join(map(str,m['indexes']))} | `{m['mutator'].split('.')[-1]}` : {m['description']} | `{m['before_manual']}` | {ids[test(m)]} |")
parts += ['', '#### Preuve d’équivalence des deux survivants', '',
 'Les deux mutants restent officiellement `SURVIVED` dans PIT ; le moteur ne les a pas classés `EQUIVALENT`. Leur équivalence est une conclusion séparée obtenue par analyse du code :', '',
 '| Mutant | Emplacement | Modification |', '|---|---|---|']
for m in sorted([x for x in final if x['status']=='SURVIVED'],key=lambda x:x['id']):
    assert m['mutatedMethod'] in ('getIntLE','getIntBE') and m['mutator'].endswith('IncrementsMutator')
    parts.append(f"| {m['id']} | `{m['mutatedMethod']}{m['methodDescription']}`, ligne {m['lineNumber']}, index {m['indexes'][0]} | Dernier `i++` remplacé par `i--` dans `int b3 = data[i++] & 0xFF` |")
parts += ['',
 'Dans les deux versions, le postfixe fournit **la même ancienne valeur de i** à l’accès `data[...]`. La valeur de b3 est donc identique pour toute entrée où l’accès réussit. Seule la nouvelle valeur de la variable locale i diffère. Or i n’est plus lue après cet accès : le retour dépend exclusivement de b0, b1, b2 et b3. La variable n’est ni partagée, ni retournée. Un éventuel dépassement arithmétique du int local ne lance pas d’exception en Java. Pour un tableau nul ou un indice invalide, l’accès utilise le même indice et échoue de la même manière. Les méthodes n’offrent donc aucun comportement observable permettant à un test fonctionnel de distinguer ces deux mutants.', '',
 'Le résultat principal reste **204/206 = 99,03 %**, sans retirer de mutant du rapport. Si les deux équivalents démontrés sont exclus pour une mesure complémentaire explicitement ajustée, le score devient 204/(206−2) = 100 %. Ce chiffre ajusté n’est pas le score brut PIT, et ne prouve pas l’absence de défauts hors du jeu `DEFAULTS`. La couverture de lignes reste 120/121 ; aucun 100 % de lignes n’est revendiqué.', '',
 '<!-- MANUAL-TESTS-END -->']
readme=ROOT/'README.md';text=readme.read_text();start='<!-- MANUAL-TESTS-START -->';end='<!-- MANUAL-TESTS-END -->';section='\n'.join(parts)+'\n'
if start in text:
    lo,hi=text.index(start),text.index(end)+len(end);text=text[:lo]+section.rstrip()+text[hi:]
else:
    text=text.replace('<!-- IFT3913-TACHE2-END -->',section+'\n<!-- IFT3913-TACHE2-END -->')
readme.write_text(text)
summary={'before_manual':dict(Counter(m['before_manual'] for m in final)),
         'final':dict(Counter(m['status'] for m in final)),
         'newly_killed':101,'manual_tests':17,
         'manual_source_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),
         'transitions':{f'{a} -> {b}':n for (a,b),n in Counter((m['before_manual'],m['status']) for m in final).items()},
         'mutants':sorted(final,key=lambda x:x['id'])}
(ROOT/'rapports-pit/comparison-endian-manual.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n')
print(json.dumps({k:v for k,v in summary.items() if k!='mutants'},ensure_ascii=False,indent=2))
