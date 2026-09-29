#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
# Copyright (C) 2026 Dimitrios Diamantis
"""Verify release archives and smoke-test the matching host runtime without GUI/user data."""
import argparse
import hashlib
import json
import platform
from pathlib import Path
import struct
import subprocess
import tarfile
import tempfile
import zipfile

ROOT=Path(__file__).resolve().parents[1]

def sha(data):return hashlib.sha256(data).hexdigest()

def main():
 parser=argparse.ArgumentParser();parser.add_argument('directory',type=Path);args=parser.parse_args();folder=args.directory.resolve();results={}
 for path in sorted(folder.glob('managers-sidekick-*')):
  if '-all-sources' in path.name:continue
  if path.suffix=='.zip':
   with zipfile.ZipFile(path) as z:files={n:z.read(n) for n in z.namelist() if not n.endswith('/')}
  else:
   with tarfile.open(path) as t:files={n.name:t.extractfile(n).read() for n in t if n.isfile()}
  assert files,path
  prefix=next(iter(files)).split('/')[0]+'/'
  for relative in ['LICENSE','COPYING.md','SOURCE-CODE.txt','RUNTIME-PROVENANCE.json','managers-sidekick.jar','docs/PUBLICATION.md','docs/PUBLICATION.en.md']:
   assert prefix+relative in files,(path,relative)
  assert not any('/data/' in n or '/.vscode/' in n or '/.git/' in n for n in files),path
  for line in files[prefix+'SHA256SUMS.txt'].decode().splitlines():
   expected,name=line.split('  ',1);assert sha(files[prefix+name])==expected,(path,name)
  candidates=[(n,b) for n,b in files.items() if n.endswith(('/bin/java','/bin/java.exe'))];assert len(candidates)==1
  name,binary=candidates[0]
  if 'windows-x64' in path.name:
   assert binary[:2]==b'MZ';pe=struct.unpack_from('<I',binary,0x3c)[0];assert struct.unpack_from('<H',binary,pe+4)[0]==0x8664
  elif 'linux-x64' in path.name:assert binary[:4]==b'\x7fELF' and struct.unpack_from('<H',binary,18)[0]==62
  else:
   assert binary[:4]==b'\xcf\xfa\xed\xfe';assert struct.unpack_from('<I',binary,4)[0]==(0x100000c if 'arm64' in path.name else 0x1000007)
  assert any('/legal/java.base/LICENSE' in n for n in files)
  results[path.name]={'archive':'passed','checksums':'passed','target_binary':'passed','desktop_gui':'not tested'}
  if platform.system()=='Darwin' and platform.machine()=='arm64' and 'macos-arm64' in path.name:
   with tempfile.TemporaryDirectory(prefix='sidekick-smoke-') as tmp:
    temp=Path(tmp)
    with tarfile.open(path) as t:t.extractall(temp,filter='data')
    jar=temp/prefix/'managers-sidekick.jar';classes=temp/'classes';classes.mkdir()
    subprocess.run(['javac','--release','21','-cp',str(jar),'-d',str(classes),str(ROOT/'scripts/ReleaseSmoke.java')],check=True)
    java=temp/name
    command=[str(java),'-Djava.awt.headless=true','-cp',str(classes)+':'+str(jar),'ReleaseSmoke',str(temp/'data')]
    result=subprocess.run(command,check=True,capture_output=True,text=True)
    results[path.name]['host_runtime_smoke']=result.stdout.strip();print(result.stdout.strip(),flush=True)
  print('Verified',path.name,flush=True)
 (folder/'VALIDATION.json').write_text(json.dumps(results,indent=2)+'\n')

if __name__=='__main__':main()
