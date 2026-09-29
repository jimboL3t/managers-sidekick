#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
# Copyright (C) 2026 Dimitrios Diamantis
"""Build an upload directory with platform runtimes and matching upstream sources.
Requires Python 3.12+ (safe tar extraction), curl, Maven and a JDK 21+.
"""
import argparse
import hashlib
import html
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tarfile
import tempfile
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]
CACHE = ROOT / 'dist/release-cache'
LOCKS = ['runtime-lock.json', 'dependency-sources-lock.json', 'upstream-sources-lock.json']
PLATFORMS = {'windows-x64': 'jre_x64_windows_', 'linux-x64': 'jre_x64_linux_',
             'macos-arm64': 'jre_aarch64_mac_', 'macos-x64': 'jre_x64_mac_'}

def digest(path):
    with path.open('rb') as f:
        return hashlib.file_digest(f, 'sha256').hexdigest()

def assets():
    return [a for lock in LOCKS for a in json.loads((ROOT / 'packaging' / lock).read_text())['assets']]

def obtain(asset, download):
    path = CACHE / asset['name']
    if not path.exists() or digest(path) != asset['sha256']:
        if not download:
            raise SystemExit(f'Missing/incorrect upstream file: {path}. Run with --download.')
        part = path.with_name(path.name + '.part')
        subprocess.run(['curl', '--fail', '--location', '--retry', '2', '--max-time', '240',
                        '--silent', '--show-error', asset['url'], '-o', str(part)], check=True)
        if digest(part) != asset['sha256']:
            raise SystemExit(f'Checksum mismatch: {asset["name"]}')
        part.replace(path)
    return path

def extract(path, dest):
    if path.name.endswith('.zip'):
        with zipfile.ZipFile(path) as z:
            for name in z.namelist():
                resolved = (dest / name).resolve()
                if not resolved.is_relative_to(dest.resolve()):
                    raise ValueError('Unsafe archive path')
            z.extractall(dest)
    else:
        with tarfile.open(path) as t:
            t.extractall(dest, filter='data')

def checksums(directory):
    paths = sorted(p for p in directory.rglob('*') if p.is_file() and not p.is_symlink() and p.name != 'SHA256SUMS.txt')
    return ''.join(f'{digest(p)}  {p.relative_to(directory).as_posix()}\n' for p in paths)

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--download', action='store_true', help='Fetch locked upstream files over HTTPS')
    args = parser.parse_args()
    if sys.version_info < (3, 12):
        parser.error('Python 3.12+ is required for safe archive extraction.')
    CACHE.mkdir(parents=True, exist_ok=True)
    manifest = assets()
    for asset in manifest:
        obtain(asset, args.download)
    version = ET.parse(ROOT / 'pom.xml').getroot().findtext('{http://maven.apache.org/POM/4.0.0}version')
    output = ROOT / 'dist' / f'website-{version}'
    if output.exists():
        parser.error(f'Preserve or move existing output before rebuilding: {output}')
    subprocess.run([sys.executable, str(ROOT / 'scripts/package_release.py')], cwd=ROOT, check=True)
    output.mkdir()
    sources = output / 'sources';sources.mkdir()
    prefix = f'managers-sidekick-{version}'
    shutil.copy2(ROOT / 'dist' / f'{prefix}-source.zip', sources)
    source_assets = [a for a in manifest if 'jdk-sources_' in a['name'] or 'jre_' not in a['name'] or a['name'].endswith('.json')]
    for asset in source_assets:
        shutil.copy2(CACHE / asset['name'], sources)
    for name in ['LICENSE', 'COPYING.md']:
        shutil.copy2(ROOT / name, output)
    inventory = {'application': prefix, 'license': 'GPL-3.0-only', 'runtime': json.loads((ROOT/'packaging/runtime-lock.json').read_text()),
                 'upstream_assets': manifest, 'validation': {'macos-arm64': 'See VALIDATION.json for host runtime smoke results; GUI not tested', 'windows-x64': 'not executed on target OS',
                 'linux-x64': 'not executed on target OS', 'macos-x64': 'not executed on target CPU'}}
    (output/'RELEASE-INVENTORY.json').write_text(json.dumps(inventory, indent=2)+'\n')
    shutil.copytree(ROOT/'docs', output/'docs')
    releases = []
    for platform, pattern in PLATFORMS.items():
        runtime = next(a for a in manifest if pattern in a['name'] and a['name'].endswith(('.tar.gz','.zip')))
        with tempfile.TemporaryDirectory(prefix='sidekick-public-') as work:
            stage = Path(work)
            extract(ROOT / 'dist' / f'{prefix}-test.zip', stage)
            app = stage / prefix
            unpack = stage/'jre';unpack.mkdir();extract(CACHE/runtime['name'], unpack)
            roots = list(unpack.iterdir())
            if len(roots)!=1 or not roots[0].is_dir():
                raise ValueError('Unexpected runtime layout')
            shutil.move(str(roots[0]), app/'runtime')
            executables = list((app/'runtime').rglob('bin/java.exe' if platform.startswith('windows') else 'bin/java'))
            if len(executables)!=1:
                raise ValueError('Missing or ambiguous runtime executable')
            java = executables[0].relative_to(app).as_posix()
            if not list((app/'runtime').rglob('legal/java.base/LICENSE')):
                raise ValueError('Runtime license missing')
            (app/'RUNTIME-PROVENANCE.json').write_text(json.dumps(runtime, indent=2)+'\n')
            (app/'SOURCE-CODE.txt').write_text(f'Copyright (C) 2026 Dimitrios Diamantis. GPL-3.0-only.\n'
                f'The download page supplies {prefix}-all-sources.zip (application, libraries, Java and build scripts).\n'
                'Distributors: keep the full website sources/ directory and link it next to these binaries.\n'
                'Runtime license/third-party notices are preserved under runtime/. Library notices: licenses/.\n')
            # Remove generic launchers: platform packages always select their bundled Java.
            for name in ['start-linux.sh','start-windows.cmd']:
                (app/name).unlink()
            if platform.startswith('windows'):
                launcher = '@echo off\r\nsetlocal DisableDelayedExpansion\r\ncd /d "%~dp0"\r\nif errorlevel 1 goto failed\r\n'
                launcher += f'if defined SIDEKICK_FONT goto customfont\r\n"{java}" -jar "managers-sidekick.jar"\r\ngoto result\r\n:customfont\r\n"{java}" "-Dsidekick.font=%SIDEKICK_FONT%" -jar "managers-sidekick.jar"\r\n:result\r\nif errorlevel 1 goto failed\r\nexit /b 0\r\n:failed\r\necho Startup failed. See docs\\PUBLICATION.en.md.\r\npause\r\nexit /b 1\r\n'
                (app/'start-windows.cmd').write_bytes(launcher.encode('ascii'))
            else:
                name = 'start-macos.command' if platform.startswith('macos') else 'start-linux.sh'
                launcher = '#!/bin/sh\nset -eu\nSIDEKICK_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)\ncd "$SIDEKICK_DIR"\n'
                launcher += f'if [ -n "${{SIDEKICK_FONT:-}}" ]; then exec "./{java}" "-Dsidekick.font=$SIDEKICK_FONT" -jar managers-sidekick.jar; fi\nexec "./{java}" -jar managers-sidekick.jar\n'
                (app/name).write_text(launcher);(app/name).chmod(0o755)
            for language in ['', '.en']:
                text=(ROOT/'docs'/f'PUBLICATION{language}.md').read_text()
                (app/f'START-HERE{language}.md').write_text(text.replace('(PUBLICATION', '(docs/PUBLICATION'))
            (app/'SHA256SUMS.txt').write_text(checksums(app))
            filename=f'{prefix}-{platform}'
            if platform.startswith('windows'):
                artifact=output/(filename+'.zip')
                with zipfile.ZipFile(artifact,'w',zipfile.ZIP_DEFLATED) as z:
                    for path in sorted(app.rglob('*')):
                        if path.is_file():z.write(path,f'{prefix}/{path.relative_to(app).as_posix()}')
                with zipfile.ZipFile(artifact) as z:
                    if z.testzip():raise ValueError('Invalid output ZIP')
            else:
                artifact=output/(filename+'.tar.gz')
                with tarfile.open(artifact,'w:gz') as t:t.add(app,arcname=prefix)
            releases.append(artifact.name)
            print('Built',artifact.name,flush=True)
    subprocess.run([sys.executable, str(ROOT/'scripts/verify_public.py'), str(output)], cwd=ROOT, check=True)
    # One self-contained source download as well as browsable files for hosting.
    source_bundle=output/f'{prefix}-all-sources.zip'
    with zipfile.ZipFile(source_bundle,'w',zipfile.ZIP_STORED) as z:
        for path in sorted(sources.iterdir()):z.write(path,'sources/'+path.name)
        for name in ['LICENSE','COPYING.md','RELEASE-INVENTORY.json','VALIDATION.json']:z.write(output/name,name)
    links='\n'.join(f'<li><a href="{html.escape(name)}">{html.escape(name)}</a></li>' for name in releases)
    (output/'index.html').write_text(f'''<!doctype html><html lang="el"><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>Manager’s Sidekick {version}</title>
<style>body{{font:17px system-ui;max-width:880px;margin:3rem auto;padding:1rem;background:#161426;color:#eee}}a{{color:#bbaaff}}li{{margin:1rem 0}}</style>
<h1>Manager’s Sidekick {version}</h1><p>Δωρεάν / Free · GPL-3.0-only · Copyright © 2026 Dimitrios Diamantis</p>
<p>Φορητά πακέτα με Java / Portable packages with Java included. Extract the entire archive. Windows/Linux/Intel Mac desktop acceptance is pending.</p><ul>{links}</ul>
<p><a href="{source_bundle.name}">Πλήρες πακέτο πηγών εφαρμογής, βιβλιοθηκών και runtime / Application, library and runtime source bundle</a></p>
<p><a href="SHA256SUMS.txt">SHA-256 checksums</a> · <a href="LICENSE">GNU GPLv3</a> · <a href="COPYING.md">Copyright</a> · <a href="RELEASE-INVENTORY.json">Build inventory</a> · <a href="VALIDATION.json">Validation results</a></p>
<p>Unsigned distribution; OS desktop libraries/fonts are still needed. No login or cloud sync. User data is stored beside the extracted JAR.</p></html>''')
    (output/'SHA256SUMS.txt').write_text(checksums(output))
    print('Upload directory:',output)

if __name__=='__main__':main()
