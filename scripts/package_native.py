#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
# Copyright (C) 2026 Dimitrios Diamantis
"""Build native packages on the target OS, bundling a jlink runtime via jpackage."""
import argparse
import hashlib
import platform
import shutil
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--type', choices=['app-image', 'dmg', 'pkg', 'exe', 'msi', 'deb', 'rpm'], default='app-image')
    args = parser.parse_args()
    system = platform.system()
    allowed = {'Darwin': {'app-image', 'dmg', 'pkg'}, 'Windows': {'app-image', 'exe', 'msi'}, 'Linux': {'app-image', 'deb', 'rpm'}}
    if args.type not in allowed.get(system, set()):
        parser.error('Build this package on its target operating system; cross-compilation is not supported.')
    tool = shutil.which('jpackage')
    if not tool:
        parser.error('Install a JDK with jpackage/jlink (21+) on the build machine.')
    subprocess.run([sys.executable, str(ROOT / 'scripts/package_release.py')], cwd=ROOT, check=True)
    version = ET.parse(ROOT / 'pom.xml').getroot().findtext('{http://maven.apache.org/POM/4.0.0}version')
    destination = ROOT / 'dist' / ('native-' + system.lower() + '-' + platform.machine() + '-' + version + '-' + args.type)
    if destination.exists() and any(destination.iterdir()):
        parser.error(f'Output already exists; move it aside first: {destination}')
    destination.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='sidekick-native-') as staging:
        stage = Path(staging)
        # Use the allowlisted portable release for notices and bilingual documentation.
        import zipfile
        with zipfile.ZipFile(ROOT / 'dist' / f'managers-sidekick-{version}-test.zip') as archive:
            archive.extractall(stage)
        source = stage / f'managers-sidekick-{version}'
        command = [tool, '--type', args.type, '--name', 'ManagersSidekick', '--app-version', version,
                   '--vendor', 'Managers Sidekick', '--input', str(source), '--main-jar', 'managers-sidekick.jar',
                   '--main-class', 'gr.sidekick.App', '--dest', str(destination),
                   '--add-modules', 'java.base,java.desktop,java.logging,java.xml,jdk.unsupported,jdk.charsets',
                   '--java-options', '-Dsidekick.installed=true']
        if system == 'Windows' and args.type != 'app-image':
            command += ['--win-per-user-install', '--win-menu', '--win-shortcut', '--win-dir-chooser',
                        '--win-upgrade-uuid', 'a5dcba42-f68f-4cca-a392-ab74bd4e7082']
        subprocess.run(command, cwd=ROOT, check=True)
    for artifact in destination.iterdir():
        if artifact.is_file():
            digest = hashlib.sha256(artifact.read_bytes()).hexdigest()
            artifact.with_name(artifact.name + '.sha256').write_text(digest + '  ' + artifact.name + '\n')
    print('Native output:', destination)
    print('Data folder: ~/ManagersSidekick/data (all OSes); no automatic import of portable data.')

if __name__ == '__main__':
    main()
