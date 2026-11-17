#!/usr/bin/env python3
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""Compare findings using rule, repository path and source, ignoring shifted lines."""
import collections
import functools
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
BASE = 'b020dd1f9'
TRACKED = subprocess.check_output(['git', 'ls-files'], cwd=ROOT, text=True).splitlines()


def path_in_repo(raw):
    value = raw.replace('\\', '/')
    if value in TRACKED:
        return value
    matches = [p for p in TRACKED if value.endswith('/' + p)]
    if len(matches) != 1:
        raise ValueError(f'Cannot identify repository path: {raw}')
    return matches[0]


@functools.lru_cache(None)
def source(path, before):
    if before:
        return subprocess.check_output(['git', 'show', f'{BASE}:{path}'],
                                       cwd=ROOT, text=True).splitlines()
    return (ROOT / path).read_text().splitlines()


def findings(filename, before):
    report = json.loads((ROOT / filename).read_text())
    rows = []
    for finding in report['results']:
        path = path_in_repo(finding['path'])
        start, end = finding['start']['line'], finding['end']['line']
        # Semgrep CE redacts extra.lines and fingerprint as "requires login".
        # Read the actual versioned code instead of comparing those placeholders.
        snippet = '\n'.join(line.strip() for line in source(path, before)[start - 1:end])
        rows.append({'rule': finding['check_id'], 'path': path, 'line': start,
                     'source': snippet, 'severity': finding['extra']['severity']})
    return report, rows


def key(row):
    return row['rule'], row['path'], row['source']


def subtract(left, right):
    remaining = collections.Counter(key(row) for row in right)
    result = []
    for row in left:
        identity = key(row)
        if remaining[identity]:
            remaining[identity] -= 1
        else:
            result.append(row)
    return result


if __name__ == '__main__':
    original, archived = findings('semgrep-avant.json', True)
    before, old = findings('rapports-semgrep/avant-reproduit.json', True)
    after, new = findings('rapports-semgrep/apres.json', False)
    result = {
        'baseline_commit': BASE,
        'semgrep_version': after['version'],
        'archived_count': len(archived), 'before_count': len(old), 'after_count': len(new),
        'archived_baseline_matches_reproduction':
            collections.Counter(map(key, archived)) == collections.Counter(map(key, old)),
        'new_findings': subtract(new, old), 'removed_findings': subtract(old, new),
        'before_scan_warnings': before.get('errors', []),
        'after_scan_warnings': after.get('errors', [])
    }
    output = ROOT / 'rapports-semgrep/comparaison.json'
    output.write_text(json.dumps(result, indent=2, ensure_ascii=False) + '\n')
    print(f"Before: {len(old)}; after: {len(new)}; new: {len(result['new_findings'])}; "
          f"removed: {len(result['removed_findings'])}; "
          f"archived baseline matches: {result['archived_baseline_matches_reproduction']}")
