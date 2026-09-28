from pathlib import Path
from collections import Counter, defaultdict
import hashlib
import json
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent.parent
BEFORE = ROOT / 'rapports-pit/endian-before/mutations.xml'
AFTER = ROOT / 'rapports-pit/endian-after-corrected/mutations.xml'

def parse(path):
    result = {}
    for node in ET.parse(path).getroot():
        fields = {name: node.findtext(name, '') for name in (
            'mutatedClass', 'mutatedMethod', 'methodDescription', 'lineNumber',
            'mutator', 'description', 'killingTest')}
        fields['indexes'] = [int(n.text) for n in node.findall('indexes/index')]
        fields['status'] = node.get('status')
        key = tuple(fields[name] for name in ('mutatedClass', 'mutatedMethod',
                    'methodDescription', 'lineNumber', 'mutator')) + (tuple(fields['indexes']),)
        assert key not in result, 'Identité de mutant dupliquée'
        result[key] = fields
    return result

b, a = parse(BEFORE), parse(AFTER)
assert set(a) == set(b), 'Les ensembles de mutants sont différents'
assert len(a) == 206
ordered = sorted(a, key=lambda k: (int(a[k]['lineNumber']), a[k]['mutatedMethod'],
                                  a[k]['methodDescription'], a[k]['mutator'], a[k]['indexes']))
rows = []
for i, k in enumerate(ordered, 1):
    m = dict(a[k], id=f'M{i:03d}', before=b[k]['status'])
    assert m['description'] == b[k]['description']
    rows.append(m)
new = [m for m in rows if m['status'] == 'KILLED' and m['before'] != 'KILLED']
survivors = [m for m in rows if m['status'] == 'SURVIVED']
uncovered = [m for m in rows if m['status'] == 'NO_COVERAGE']
assert (len(new), len(survivors), len(uncovered)) == (67, 21, 82)
assert all(m['before'] == 'NO_COVERAGE' for m in new)
assert sum(m['before'] == 'KILLED' and m['status'] == 'KILLED' for m in rows) == 36

def test_name(m):
    raw = m['killingTest']
    cls = re.search(r'\[class:([^]]+)\]', raw).group(1).split('.')[-1]
    method = re.search(r'\[method:([^]]+)\]', raw).group(1)
    return cls + '.' + method

oracles = {
 'EndianUtils_getIntBE_23_0_Test.testGetIntBE()':
 'Tableau `01 02 03 04`, offset 0 : `assertEquals(0x01020304, result)` (16909060). Les quatre octets distincts et non nuls contrôlent leur position et leur poids en BE32.',
 'EndianUtils_getIntLE_20_0_Test.testGetIntLE()':
 'Tableau `01 02 03 04`, sans offset : attendu `0x04030201` (67305985), somme des octets pondérés par 1, 256, 65536 et 16777216.',
 'EndianUtils_getIntLE_21_0_Test.testGetIntLE()':
 'Tableau `01 02 03 04 05 06 07 08`, offset 0 : attendu `0x04030201`. Seuls les quatre premiers octets contribuent à la valeur LE32.',
 'EndianUtils_getIntLE_20_0_Test.testGetIntLEWithTooShortArray()':
 'Tableau `01 02` : `assertThrows(ArrayIndexOutOfBoundsException.class, ...)`, car quatre octets sont nécessaires. Le mutant de l’incrément à la ligne 360 lit les indices 0, 1, 0, 1 au lieu de 0, 1, 2, 3 : aucune exception n’est alors levée, ce qui fait échouer l’oracle.',
 'EndianUtils_getLongLE_28_0_Test.testGetLongLE()':
 'Tableau `01 02 03 04 05 06 07 08`, offset 0 : attendu `0x0807060504030201L`. Chaque octet occupe sa position dans le résultat LE64 ; supprimer un tour, inverser un décalage ou changer la combinaison des octets altère cette valeur.',
 'EndianUtils_getShortBE_16_0_Test.testGetShortBE()':
 'Tableau `00 01`, sans offset : attendu 1. Ce résultat non nul détecte un retour forcé à zéro dans la surcharge appelée et dans celle à laquelle elle délègue.',
 'EndianUtils_getShortLE_12_0_Test.testGetShortLE()':
 'Tableau `12 34`, sans offset : attendu `0x3412` (13330), soit 18 + 52 × 256.',
 'EndianUtils_getShortLE_12_0_Test.testGetShortLEWithOffset()':
 'Tableau `00 12 34 56`, offset 1 : attendu `0x3412` (13330). La fenêtre `12 34` impose le bon offset et les bons poids ; le test traverse aussi `getUShortLE`.',
 'EndianUtils_getUIntBE_26_0_Test.testGetUIntBEWithOffset()':
 'Tableau `00 00 00 00 00 00 00 01`, offset 4 : attendu `1L`. Un retour zéro échoue ; remplacer le masque AND par OR produit `4294967295L`, également différent de 1.',
 'EndianUtils_getUIntLE_25_0_Test.testGetUIntLEWithZeroValues()':
 'Huit octets nuls, offset 0 : attendu `0L`. `0 & 0xFFFFFFFFL` vaut 0, mais `0 | 0xFFFFFFFFL` vaut 4294967295 : un oracle nul est pertinent pour ce mutant de masque.',
 'EndianUtils_getUIntLE_24_0_Test.testGetUIntLEWithOffset()':
 'Tableau `00 00 00 00 00 00 00 01`, offset 4 : attendu corrigé `0x01000000L` (16777216). Le retour forcé à zéro diffère de ce résultat non nul.',
 'EndianUtils_getUShortBE_18_0_Test.testGetUShortBE()':
 'Tableau `00 01` : attendu corrigé `0x0001`. Les retours zéro, le changement du masque, de l’offset ou de l’addition finale ne conservent pas ce résultat.',
 'EndianUtils_getUShortBE_19_0_Test.testGetUShortBEWithNegativeValues()':
 'Octets Java `(byte)0xFF` et `(byte)0xFE`, offset 0 : attendu `0xFFFE` (65534). Les valeurs non signées 255 et 254 donnent 255 × 256 + 254 ; remplacer `255 << 8` par `255 >> 8` ramène le résultat à 254.',
 'EndianUtils_getUShortLE_14_0_Test.testGetUShortLE()':
 'Tableau `01 02` : attendu `0x0201` (513), donc différent du zéro imposé par le mutant de retour de la surcharge sans offset.',
 'EndianUtils_readShortLE_0_0_Test.testReadShortLE_withValidData()':
 'Mockito renvoie successivement `0x12`, `0x34` : attendu `0x3412` (13330). La lecture délègue à `readUShortLE` ; des poids ou signes arithmétiques modifiés et les retours zéro font échouer cette égalité.',
 'EndianUtils_readShortLE_0_0_Test.testReadShortLE_withBufferUnderrun()':
 'Séquence corrigée `0x12`, `-1` : `assertThrows(BufferUnderrunException.class, ...)`. `18 | -1` vaut -1, tandis que `18 & -1` vaut 18 : le mutant AND masque la fin de flux. Supprimer le contrôle empêche aussi l’exception ; dans les deux cas, l’oracle échoue.',
 'EndianUtils_ubyteToInt_29_0_Test.testUbyteToInt()':
 'Assertions successives : 10 → 10, -10 → 246, `(byte)0xFF` → 255, 0 → 0. Dès la première assertion, AND→OR donne 255 au lieu de 10 ; le retour forcé à zéro donne 0 au lieu de 10.',
}
names = sorted({test_name(m) for m in new})
assert set(names) == set(oracles)
ids = {name: f'T{i:02d}' for i, name in enumerate(names, 1)}

def reason(m):
    op = m['mutator'].split('.')[-1]
    test = test_name(m)
    desc = m['description']
    if 'withBufferUnderrun' in test:
        return 'La fin de flux ne déclenche plus l’exception exigée.'
    if 'WithTooShortArray' in test:
        return 'Les indices 0,1,0,1 restent valides : l’exception exigée disparaît.'
    if op == 'PrimitiveReturnsMutator':
        return 'Le zéro imposé diffère de l’attendu non nul.'
    if op == 'IncrementsMutator':
        return 'Un indice suivant recule : octet incorrect ou exception inattendue.'
    if op == 'ConditionalsBoundaryMutator':
        assert m['mutatedMethod'] == 'getLongLE'
        return 'Le passage de >= à > omet l’octet à l’offset 0 (valeur 01).'
    if op.startswith('RemoveConditionalMutator'):
        return 'La boucle ne s’exécute plus et laisse le résultat à zéro.'
    if m['mutatedMethod'] == 'getLongLE' and m['lineNumber'] == '446':
        return ('Départ j=-9 : la boucle est sautée, résultat zéro.' if 'addition with subtraction' in desc
                else 'Départ j=9 : accès hors du tableau de huit octets.')
    if m['mutatedMethod'] in ('getUIntBE', 'getUIntLE') and 'AND with OR' in desc:
        return 'Le masque OR force les 32 bits bas à 1 au lieu de préserver la valeur.'
    if 'AND with OR' in desc:
        return 'Le masque OR force des bits à 1 et altère l’octet décodé.'
    if 'OR with AND' in desc:
        return 'Le AND efface les bits au lieu d’assembler les octets.'
    if 'Shift Left with Shift Right' in desc:
        return 'Le décalage droit supprime le poids attendu de l’octet.'
    if m['lineNumber'] in ('292', '336'):
        return 'offset+1 devient offset-1 : mauvais octet ou indice négatif.'
    if 'addition with subtraction' in desc:
        return 'Une contribution d’octet non nulle est soustraite au lieu d’être ajoutée.'
    raise AssertionError(m)

lines = ['<!-- PIT-COMPARISON-START -->', '### Mutation après ajout des tests générés corrigés', '',
 'Analyse exécutée le 28 septembre 2026. La suite comprend les tests originaux et les 71 tests générés **après les 11 corrections décrites ci-dessus**. À ce stade intermédiaire de la mesure, aucun test supplémentaire dédié aux mutants survivants n’avait été ajouté. Le score ne décrit donc ni les tests IA bruts seuls, ni une suite finale renforcée manuellement.', '',
 'Commande exécutée depuis la racine :', '', '```bash',
 'mvn -B -pl tika-core -Pchatunitest-verify clean test-compile \\',
 '  org.pitest:pitest-maven:1.25.9:mutationCoverage \\',
 '  -DtargetClasses=org.apache.tika.io.EndianUtils \\',
 '  -Drat.skip=true -Dcheckstyle.skip=true -Dossindex.skip=true', '```', '',
 'Le `clean` élimine les anciennes classes compilées ; le profil ajoute explicitement les sources générées. PIT garde la même version 1.25.9, le connecteur JUnit 1.2.3, les opérateurs `DEFAULTS` et un thread. `MediaType` est exclue de cette commande. Aucun appel à Ollama n’est déclenché. Le rapport produit dans `tika-core/target/pit-reports` a été copié intégralement hors de `target`.', '',
 'Preuves : [rapport HTML après](rapports-pit/endian-after-corrected/index.html), [XML après](rapports-pit/endian-after-corrected/mutations.xml), [journal Maven/PIT](chatunitest-local/logs/pit-after-corrected.log), [rapport initial](rapports-pit/endian-before/index.html).', '',
 '| Mesure | Tests originaux | Originaux + générés corrigés |', '|---|---:|---:|',
 '| Mutants générés | 206 | 206 |', '| `KILLED` | 36 | 103 |',
 '| `SURVIVED` (exécutés mais non détectés) | 16 | 21 |',
 '| `NO_COVERAGE` (non exécutés) | 154 | 82 |',
 '| Autres statuts, dont erreurs et timeouts | 0 | 0 |',
 '| Score global `KILLED / total` | 36/206 = 17,48 % | 103/206 = 50,00 % |',
 '| Lignes couvertes par PIT | 31/121 = 25,62 % | 74/121 = 61,16 % |',
 '| Force des tests parmi les mutants couverts | 36/52 = 69,23 % | 103/124 = 83,06 % |', '',
 '**Gain : 67 mutants tués supplémentaires, soit +32,52 points de pourcentage.** Les tests ne détectent pas tous les mutants : 103 restent non tués (21 survivants et 82 non couverts). La hausse du nombre de survivants de 16 à 21 correspond à cinq mutants nouvellement exécutés, pas à une régression de mutants auparavant tués.', '',
 '| Transition individuelle | Nombre |', '|---|---:|',
 '| `KILLED → KILLED` | 36 |', '| `SURVIVED → SURVIVED` | 16 |',
 '| `NO_COVERAGE → KILLED` | 67 |', '| `NO_COVERAGE → SURVIVED` | 5 |',
 '| `NO_COVERAGE → NO_COVERAGE` | 82 |', '',
 'Les 16 survivants initiaux ne sont donc pas détectés par les tests ajoutés. Le gain porte exclusivement sur du code non couvert dans la première analyse.', '',
 '#### Méthode de comparaison et attribution', '',
 'Les 206 identités de mutants sont identiques dans les deux XML. La clé de comparaison combine la classe, la méthode, le descripteur JVM de surcharge, la ligne, le nom complet du mutateur et la liste des indices de bytecode. Les descriptions sont également vérifiées identiques. Une ligne peut contenir plusieurs mutations du même opérateur : les indices évitent de les confondre.', '',
 'Les identifiants `M001` à `M206` ci-dessous sont des identifiants documentaires attribués par le [script de comparaison](chatunitest-local/compare-pit.py), pas des numéros fournis par PIT. Le [JSON de comparaison](rapports-pit/comparison-endian.json) conserve tous les champs XML, les deux statuts et les noms complets des tests. Reproduction : `python3 chatunitest-local/compare-pit.py`.', '',
 'Le champ `killingTest` identifie le test enregistré par PIT pour tuer un mutant. Il ne prouve pas que ce test soit le seul capable de le tuer. Les explications ci-dessous sont déduites du code du test et de la mutation ; le statut `KILLED` et l’attribution du test proviennent du rapport mesuré. Aucun mutant n’a été supprimé du dénominateur.', '',
 '#### Tests tueurs et oracles', '',
 'Les 67 nouveaux mutants tués sont attribués à 17 méthodes de test générées. Chaque identifiant T renvoie à un test exact du package `org.apache.tika.io` et à ses données/oracles :', '']
for name in names:
    cls = name.split('.')[0]
    link = f'tika-core/chatunitest/tika-parent/tika-core/org/apache/tika/io/{cls}.java'
    lines += [f'- **{ids[name]} — [{name}]({link})** : {oracles[name]}']
lines += ['', '#### Les 67 mutants nouvellement tués', '',
          'Tous passent de `NO_COVERAGE` à `KILLED`. Les noms courts des mutateurs ci-dessous sont les suffixes exacts de leurs noms complets conservés dans le JSON/XML. Chaque ligne associe l’emplacement, l’opérateur, le test tueur et la raison de la détection.', '',
          '| ID | Méthode, ligne, index bytecode | Mutateur et changement | Test | Pourquoi il le tue |',
          '|---|---|---|---|---|']
for m in new:
    lines.append(f"| {m['id']} | `{m['mutatedMethod']}{m['methodDescription']}`, L{m['lineNumber']}, i={','.join(map(str,m['indexes']))} | `{m['mutator'].split('.')[-1]}` : {m['description']} | {ids[test_name(m)]} | {reason(m)} |")
lines += ['', '#### Les 21 mutants survivants', '',
 '| ID | Méthode et ligne | Mutateur, index et changement | Statut initial | Diagnostic |', '|---|---|---|---|---|']
for m in survivors:
    method = m['mutatedMethod']
    if method in ('getIntBE','getIntLE'):
        diagnosis = 'Équivalence déduite du code : dernier `i++` dans `data[i++]`. La valeur d’indice utilisée est la même et i n’est plus lu ensuite. Le résultat/exception observable reste identique ; PIT le classe néanmoins SURVIVED.'
    elif method in ('getUIntBE','getUIntLE'):
        diagnosis = 'La surcharge sans offset n’est appelée qu’avec quatre premiers octets nuls dans ces nouveaux tests. Ajouter un résultat attendu non nul pour cette surcharge, sans se limiter à la surcharge avec offset.'
    elif method == 'readUShortLE':
        diagnosis = 'La frontière `< 0` devient `<= 0` ; aucun cas fourni ne lit deux octets nuls. Un flux `00 00` doit retourner zéro sans exception.'
    elif method == 'readUE7':
        diagnosis = 'Les tests existants décodent des valeurs valides sur 1 à 3 octets. Examiner séparément EOF, octet terminal nul, limite de six octets et incrément du compteur ; l’équivalence de ces mutants n’est pas établie ici.'
    else:
        diagnosis = 'Le contrôle EOF nécessite des cas aux frontières (octets valides nuls et chaque position de troncature). Les entrées actuelles ne distinguent pas cette mutation. Pour readUIntBE, le cas court original appelle à tort readUIntLE.'
    lines.append(f"| {m['id']} | `{method}{m['methodDescription']}`, L{m['lineNumber']} | `{m['mutator'].split('.')[-1]}`, i={','.join(map(str,m['indexes']))} : {m['description']} | `{m['before']}` | {diagnosis} |")
lines += ['', 'Les deux incréments locaux ci-dessus sont considérés équivalents par analyse du code, mais restent inclus dans le score brut 103/206. Les autres diagnostics sont des pistes pour l’étape de tests supplémentaires, pas des preuves qu’un nouveau test a déjà tué ces mutants.', '',
 '#### Les 82 mutants non couverts', '',
 'Le statut `NO_COVERAGE` signifie que PIT n’a pas pu exercer le code muté avec la suite sélectionnée. Il ne s’agit pas d’une exception à ignorer ni d’une preuve d’équivalence. La liste ci-dessous couvre les 82 identifiants, regroupés par surcharge.', '',
 '| Méthode (descripteur JVM) | Nombre | Mutants |', '|---|---:|---|']
groups = defaultdict(list)
for m in uncovered: groups[(m['mutatedMethod'],m['methodDescription'])].append(m['id'])
for (method, descriptor), mids in sorted(groups.items()):
    lines.append(f"| `{method}{descriptor}` | {len(mids)} | {', '.join(mids)} |")
lines += ['', 'Les méthodes de lecture longue, plusieurs lectures signées et les variantes BE de lecture courte restent sans couverture de mutation. `getIntBE(byte[])` n’est pas exercée bien que `getIntBE(byte[], int)` le soit ; couvrir une surcharge ne garantit pas de couvrir la surcharge qui délègue. `getUByte` n’a pas produit de test exporté. Les treize échecs de génération contribuent à expliquer ces lacunes, sans établir une correspondance un pour un : un test peut atteindre indirectement plusieurs méthodes.', '',
 'Cette comparaison intermédiaire s’arrête à la suite originaux + générés corrigés. L’étape suivante, réalisée et documentée ci-dessous, distingue les cas non couverts des survivants couverts et conserve un rapport PIT final séparé.',
 '<!-- PIT-COMPARISON-END -->']

output = '\n'.join(lines) + '\n'
readme = ROOT / 'README.md'
text = readme.read_text()
start, end = '<!-- PIT-COMPARISON-START -->', '<!-- PIT-COMPARISON-END -->'
if start in text:
    lo, hi = text.index(start), text.index(end) + len(end)
    text = text[:lo] + output.rstrip() + text[hi:]
else:
    marker = '<!-- IFT3913-TACHE2-END -->'
    assert marker in text
    text = text.replace(marker, output + '\n' + marker)
readme.write_text(text)
summary = {
 'before': dict(Counter(m['before'] for m in rows)),
 'after': dict(Counter(m['status'] for m in rows)),
 'newly_killed': len(new),
 'transitions': {f'{x} -> {y}':n for (x,y),n in Counter((m['before'],m['status']) for m in rows).items()},
 'sha256_before_xml': hashlib.sha256(BEFORE.read_bytes()).hexdigest(),
 'sha256_after_xml': hashlib.sha256(AFTER.read_bytes()).hexdigest(),
 'mutants': rows,
}
(ROOT / 'rapports-pit/comparison-endian.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2)+'\n')
print(json.dumps({k:v for k,v in summary.items() if k!='mutants'},ensure_ascii=False,indent=2))
