#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
# Copyright (C) 2026 Dimitrios Diamantis
"""Build Windows/Linux portable bundles and an Apple Silicon macOS DMG.
macOS host, Python 3.12+, JDK, Maven and mingw-w64 required. No publishing.
"""
import argparse
import json
import platform
import shutil
import struct
import subprocess
import sys
import tempfile
import tarfile
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path
from package_public import ROOT, assets, obtain, extract, checksums, digest
from package_release import write_sources


def run(*args):
    subprocess.run([str(a) for a in args], cwd=ROOT, check=True)


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--finish', action='store_true', help='Finish sources/checksums after a separately retried DMG build')
    args=parser.parse_args()
    if platform.system() != 'Darwin' or sys.version_info < (3, 12):
        raise SystemExit('Build on macOS with Python 3.12+.')
    for tool in ['java', 'javac', 'jpackage', 'x86_64-w64-mingw32-gcc', 'x86_64-w64-mingw32-windres']:
        if not shutil.which(tool): raise SystemExit('Missing build tool: ' + tool)
    version = ET.parse(ROOT/'pom.xml').getroot().findtext('{http://maven.apache.org/POM/4.0.0}version')
    prefix = 'managers-sidekick-' + version
    output = ROOT/'dist'/('release-' + version)
    if output.exists() and not args.finish: raise SystemExit('Preserve or move existing output first: ' + str(output))
    if platform.machine()!='arm64':raise SystemExit('This locked DMG build currently targets Apple Silicon only.')
    build_asset=json.loads((ROOT/'packaging/desktop-build-lock.json').read_text())
    build_archive=obtain(build_asset,False)
    manifest = assets()
    cached = {a['name']: obtain(a, False) for a in manifest}
    if args.finish:
        finish(output, prefix, manifest, cached, version)
        return
    run(sys.executable, ROOT/'scripts/package_release.py')
    output.mkdir(parents=True)
    with tempfile.TemporaryDirectory(prefix='sidekick-desktop-') as temporary:
        stage = Path(temporary)
        build_jdk=stage/'build-jdk';build_jdk.mkdir();extract(build_archive,build_jdk)
        jpackage=next(build_jdk.glob('*/Contents/Home/bin/jpackage'))
        icons = stage/'icons'
        run('java', '-Djava.awt.headless=true', '--class-path', ROOT/'target'/f'{prefix}.jar', ROOT/'scripts/ExportIcons.java', icons)
        pngs = [(n, (icons/f'{n}.png').read_bytes()) for n in [16,32,48,64,128,256]]
        offset = 6 + 16 * len(pngs); entries = []; payload = b''
        for size, data in pngs:
            entries.append(struct.pack('<BBBBHHII', size % 256, size % 256, 0, 0, 1, 32, len(data), offset))
            payload += data; offset += len(data)
        ico = icons/'sidekick.ico'; ico.write_bytes(struct.pack('<HHH',0,1,len(pngs))+b''.join(entries)+payload)
        chunks = b''
        for tag, size in [(b'ic07',128),(b'ic08',256),(b'ic09',512),(b'ic10',1024)]:
            data=(icons/f'{size}.png').read_bytes(); chunks += tag + struct.pack('>I',8+len(data)) + data
        icns=icons/'sidekick.icns';icns.write_bytes(b'icns'+struct.pack('>I',8+len(chunks))+chunks)
        extract(ROOT/'dist'/f'{prefix}-test.zip', stage)
        app=stage/prefix
        for name in ['start-windows.cmd','start-linux.sh']: (app/name).unlink()
        for language in ['', '.en']:
            (app/f'START-HERE{language}.md').write_text((ROOT/'docs'/f'DESKTOP{language}.md').read_text().replace('(DESKTOP', '(START-HERE'))
        def runtime(pattern, destination):
            asset=next(a for a in manifest if pattern in a['name'] and a['name'].endswith(('.zip','.tar.gz')))
            destination.mkdir();extract(cached[asset['name']],destination)
            roots=list(destination.iterdir())
            if len(roots)!=1:raise ValueError('Unexpected runtime layout')
            return roots[0], asset
        win, win_asset=runtime('jre_x64_windows_',stage/'win-runtime')
        shutil.move(str(win),app/'runtime')
        rc=stage/'launcher.rc'
        numbers=version.replace('.',',')+',0'
        rc.write_text(f'''#include <windows.h>
1 ICON "{ico.as_posix()}"
1 VERSIONINFO
FILEVERSION {numbers}
PRODUCTVERSION {numbers}
FILETYPE VFT_APP
BEGIN
 BLOCK "StringFileInfo"
 BEGIN
  BLOCK "040904B0"
  BEGIN
   VALUE "FileDescription", "Manager's Sidekick"
   VALUE "ProductName", "Manager's Sidekick"
   VALUE "ProductVersion", "{version}"
   VALUE "LegalCopyright", "Copyright (C) 2026 Dimitrios Diamantis"
  END
 END
 BLOCK "VarFileInfo"
 BEGIN
  VALUE "Translation", 0x409, 1200
 END
END
''')
        resource=stage/'launcher-res.o'
        run('x86_64-w64-mingw32-windres',rc,resource)
        run('x86_64-w64-mingw32-gcc','-Os','-fno-builtin','-fno-stack-protector','-nostdlib','-mwindows',
            '-Wl,--entry,mainCRTStartup',ROOT/'packaging/windows/launcher.c',resource,'-lkernel32','-luser32','-o',app/'managersSidekick.exe')
        (app/'RUNTIME-PROVENANCE.json').write_text(json.dumps(win_asset,indent=2)+'\n')
        (app/'SOURCE-CODE.txt').write_text(f'Matching full source bundle: {prefix}-all-sources.zip, supplied alongside this package.\n')
        (app/'SHA256SUMS.txt').write_text(checksums(app))
        with zipfile.ZipFile(output/f'{prefix}-windows-x64.zip','w',zipfile.ZIP_DEFLATED) as z:
            for p in sorted(app.rglob('*')):
                if p.is_file():z.write(p,f'{prefix}/{p.relative_to(app).as_posix()}')
        print('Windows portable EXE ZIP built.',flush=True)
        linux_stage=stage/'linux';linux_stage.mkdir()
        extract(ROOT/'dist'/f'{prefix}-test.zip',linux_stage)
        linux=linux_stage/prefix
        linux_runtime,linux_asset=runtime('jre_x64_linux_',stage/'linux-runtime')
        shutil.move(str(linux_runtime),linux/'runtime')
        (linux/'start-windows.cmd').unlink()
        (linux/'start-linux.sh').write_text('#!/bin/sh\nset -eu\nSIDEKICK_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)\ncd "$SIDEKICK_DIR"\nif [ -n "${SIDEKICK_FONT:-}" ]; then exec ./runtime/bin/java "-Dsidekick.font=$SIDEKICK_FONT" -jar managers-sidekick.jar; fi\nexec ./runtime/bin/java -jar managers-sidekick.jar\n')
        (linux/'start-linux.sh').chmod(0o755)
        for language in ['', '.en']:(linux/f'START-HERE{language}.md').write_text((ROOT/'docs'/f'DESKTOP{language}.md').read_text().replace('(DESKTOP', '(START-HERE'))
        (linux/'RUNTIME-PROVENANCE.json').write_text(json.dumps(linux_asset,indent=2)+'\n')
        shutil.copy2(app/'SOURCE-CODE.txt',linux/'SOURCE-CODE.txt')
        (linux/'SHA256SUMS.txt').write_text(checksums(linux))
        with tarfile.open(output/f'{prefix}-linux-x64.tar.gz','w:gz') as archive:archive.add(linux,arcname=prefix)
        print('Linux portable bundle built.',flush=True)
        # Only host-native macOS launchers; no misleading cross-architecture package.
        arch='arm64' if platform.machine()=='arm64' else 'x64'
        mac,mac_asset=runtime('jre_aarch64_mac_' if arch=='arm64' else 'jre_x64_mac_',stage/'mac-runtime')
        mac_input=stage/'mac-input';mac_input.mkdir()
        extract(ROOT/'dist'/f'{prefix}-test.zip',mac_input)
        source=mac_input/prefix
        for name in ['start-windows.cmd','start-linux.sh']: (source/name).unlink()
        for language in ['', '.en']:(source/f'START-HERE{language}.md').write_text((ROOT/'docs'/f'DESKTOP{language}.md').read_text().replace('(DESKTOP', '(START-HERE'))
        (source/'RUNTIME-PROVENANCE.json').write_text(json.dumps(mac_asset,indent=2)+'\n')
        (source/'SOURCE-CODE.txt').write_text(f'Matching full source bundle: {prefix}-all-sources.zip, supplied alongside this package.\n')
        images=output/'app-image';images.mkdir()
        run(jpackage,'--type','app-image','--name','ManagersSidekick','--app-version',version,
            '--vendor','nervZ','--input',source,'--main-jar','managers-sidekick.jar','--main-class','gr.sidekick.App',
            '--dest',images,'--runtime-image',mac/'Contents/Home','--icon',icns,
            '--mac-package-identifier','gr.sidekick.managerssidekick','--java-options','-Dsidekick.installed=true')
        run(jpackage,'--type','dmg','--name','ManagersSidekick','--app-version',version,
            '--app-image',images/'ManagersSidekick.app','--dest',output)
        (output/f'ManagersSidekick-{version}.dmg').rename(output/f'{prefix}-macos-{arch}.dmg')
    finish(output, prefix, manifest, cached, version)


def finish(output, prefix, manifest, cached, version):
    write_sources(version)
    shutil.copy2(ROOT/"quickStartGuide.pdf",output/"quickStartGuide.pdf")
    arch='arm64' if platform.machine()=='arm64' else 'x64'
    original=output/f'ManagersSidekick-{version}.dmg'
    if original.exists():original.rename(output/f'{prefix}-macos-{arch}.dmg')
    if not (output/f'{prefix}-macos-{arch}.dmg').is_file():raise SystemExit('DMG is not built yet.')
    # Preserve matching application, library and runtime sources. Native JDK launcher
    # provenance is recorded separately; public release requires its source audit.
    with zipfile.ZipFile(output/f'{prefix}-all-sources.zip','w',zipfile.ZIP_STORED) as z:
        z.write(ROOT/'dist'/f'{prefix}-source.zip',f'sources/{prefix}-source.zip')
        for a in manifest:
            if 'jdk-sources_' in a['name'] or 'jre_' not in a['name'] or a['name'].endswith('.json'):
                z.write(cached[a['name']],'sources/'+a['name'])
        for name in ['LICENSE','COPYING.md']:z.write(ROOT/name,name)
    provenance={'application':version,'mac_architecture':arch,'jpackage_build':json.loads((ROOT/'packaging/desktop-build-lock.json').read_text()),'status':'Published for desktop testing; native launcher and runtime use matching Temurin 21 source bundle','windows_execution':'pending target-host review','linux_execution':'pending target-host review','macos_validation':'headless smoke and package integrity; full installation/upgrade acceptance pending'}
    (output/'BUILD-REVIEW.json').write_text(json.dumps(provenance,indent=2)+'\n')
    for language in ['', '.en']:(output/f'START-HERE{language}.md').write_text((ROOT/'docs'/f'DESKTOP{language}.md').read_text().replace('(DESKTOP', '(START-HERE'))
    (output/'SHA256SUMS.txt').write_text(''.join(f'{digest(p)}  {p.name}\n' for p in sorted(output.iterdir()) if p.is_file() and p.name!='SHA256SUMS.txt'))
    print('Local review artifacts:',output)

if __name__=='__main__': main()
