#!/usr/bin/env python3
"""Create a license-aware installer ZIP from validated, source-built payloads."""
from pathlib import Path
import hashlib,json,os,re,shutil,subprocess,zipfile
ROOT=Path(__file__).resolve().parents[1]
VERSION=json.loads((ROOT/'modpack/manifest.json').read_text(encoding='utf-8'))['version']
REPOSITORY=json.loads((ROOT/'PROJECT.json').read_text(encoding='utf-8')).get('public_repository','https://github.com/Flowfangfei/create-263-modpack-public')
RUNTIME_MODULE_COUNT=len(json.loads((ROOT/'manifests/runtime-modules.json').read_text(encoding='utf-8'))['modules'])
INSTALL_README=f'''# Create263 Pack {VERSION}

代码由GPT 6.1sol许愿式编程生成，请谨慎采用。仅进行了部分测试，实验性版本尚不稳定。后续将在实际存档中持续测试。

Code generated through wish-driven programming with GPT 6.1sol. Use with caution. Only partial testing has been performed; this experimental version is unstable. Testing will continue in actual worlds.

## 安装 / Installation

1. 解压全部文件，运行 `Install-Pack.cmd`，等待固定来源下载及 SHA-256 校验完成。Extract all files, run `Install-Pack.cmd`, and wait for the pinned downloads and checks.
2. 将 `output/Create263-Pack-v{VERSION}-HMCL.zip` 导入 HMCL，安装为新实例，选择 Java 25。Import the generated ZIP into HMCL as a new instance and select Java 25.
3. 使用新世界或已备份的存档副本试用。平台为 Minecraft 26.3 / NeoForge 26.3.0.22-beta。Use a new world or a backed-up copy. The platform is Minecraft 26.3 / NeoForge 26.3.0.22-beta.

需要代理时运行 `Install-Pack.cmd -Proxy <HTTP proxy URL>`。脚本不查找或复制现有世界，也不覆盖已有输出 ZIP。The script leaves existing saves alone and refuses to overwrite an existing output ZIP.

## 内容与验证 / Contents and validation

包含 {RUNTIME_MODULE_COUNT} 个直接运行 JAR。最新修复涵盖铁道机器人通风口、热气球、仓储思索模板，以及多模组思索的数据、渲染、动画和资源重载。Includes {RUNTIME_MODULE_COUNT} direct runtime JARs with fixes for Vent rendering, balloons, Storage Ponder templates, and cross-module Ponder data, rendering, animation and resource reload.

389 项场景回放、3458 个采样时点、38592 帧界面渲染通过，资源重载后另验 12 个场景。这是部分功能验证，不能代表所有玩法或长期存档安全。389 scene playbacks, 3458 sampled times and 38592 GUI frames passed, plus 12 scenes after resource reload. This is partial coverage and does not establish complete gameplay or long-term save safety.

完整说明：[中文](docs/PROJECT-README.md) / [English](docs/PROJECT-README.en.md)。测试证据：[Ponder audit](docs/PONDER-AUDIT-20261104.md)。

## 来源与许可 / Sources and licenses

各组件保留各自许可，见 [中文说明](docs/SOURCE-AND-LICENSES.md)、[English notes](docs/SOURCE-AND-LICENSES.en.md)、[第三方声明](THIRD_PARTY_NOTICES.md) 和 `LICENSES/`。部分源码由旧版公开源码结合目标发布包差异恢复，不是原作者提供的完整目标版本源码。Components retain their own licenses. Some recovered sources combine an older public baseline with newer release-binary differences; they are not an official complete source release for the target version.

航空学受限素材（包括跨命名空间资源）、Create 官方美术和单独授权音效从固定来源下载后在本地恢复，附件不包含它们。传动与连杆的 CC-BY-NC 和 PolyForm Shield 声明均保留。Restricted Aero assets across all namespaces, official Create artwork and separately licensed audio are fetched from pinned upstream sources and restored locally. Transmission & Linkage retains both notices.

[升级与测试](docs/MOD-UPGRADE-AND-TESTING.md) / [Upgrade and testing](docs/MOD-UPGRADE-AND-TESTING.en.md)

Source: {REPOSITORY}/tree/v{VERSION}
'''


def main():
    payload=ROOT/'artifacts/payload/mods'
    jars=sorted(payload.glob('*.jar'))
    expected={row['name'] for row in json.loads((ROOT/'manifests/runtime-modules.json').read_text(encoding='utf-8'))['modules']}
    if {p.name for p in jars}!=expected:raise ValueError('Payload mod set differs from runtime-modules.json')
    release=ROOT/('artifacts/installer-v'+VERSION);release.mkdir(parents=True,exist_ok=True)
    shutil.copytree(payload,release/'payload/mods',dirs_exist_ok=True)
    shutil.copytree(ROOT/'LICENSES',release/'LICENSES',dirs_exist_ok=True)
    shutil.copytree(ROOT/'manifests',release/'manifests',dirs_exist_ok=True)
    shutil.copytree(ROOT/'modpack',release/'modpack',dirs_exist_ok=True)
    shutil.copy2(ROOT/'THIRD_PARTY_NOTICES.md',release/'THIRD_PARTY_NOTICES.md')
    (release/'docs').mkdir(exist_ok=True)
    documents=['VALIDATION.md','GAMEPLAY-VALIDATION-26.3.md','PONDER-AUDIT-20261104.md','SOURCE-AND-LICENSES.md','SOURCE-AND-LICENSES.en.md','MOD-UPGRADE-AND-TESTING.md','MOD-UPGRADE-AND-TESTING.en.md']
    shutil.copy2(ROOT/'README.md',release/'docs/PROJECT-README.md')
    shutil.copy2(ROOT/'README.en.md',release/'docs/PROJECT-README.en.md')
    shutil.copytree(ROOT/'docs/assets',release/'docs/assets',dirs_exist_ok=True)
    for name in documents:
        shutil.copy2(ROOT/'docs'/name,release/'docs'/name)
    copies={ROOT/'README.md':release/'docs/PROJECT-README.md',ROOT/'README.en.md':release/'docs/PROJECT-README.en.md'}
    copies.update({ROOT/'docs'/name:release/'docs'/name for name in documents})
    for original_document,document in copies.items():
        def source_link(match):
            target=match.group(1)
            if '://' in target or target.startswith('#'):return match.group(0)
            relative=target.split('#',1)[0]
            original=(original_document.parent/relative).resolve()
            if not original.is_relative_to(ROOT.resolve()) or not original.exists():
                raise ValueError('Broken installer documentation source link: '+str(original_document)+': '+target)
            fragment='#'+target.split('#',1)[1] if '#' in target else ''
            mapped=copies.get(original)
            if mapped is None:
                candidate=release/original.relative_to(ROOT)
                if candidate.exists():mapped=candidate
            if mapped is not None:
                return ']('+Path(os.path.relpath(mapped,document.parent)).as_posix()+fragment+')'
            kind='tree' if original.is_dir() else 'blob'
            return ']('+f'{REPOSITORY}/{kind}/v{VERSION}/'+original.relative_to(ROOT).as_posix()+fragment+')'
        lines=[];fence=None
        for line in document.read_text(encoding='utf-8').splitlines(keepends=True):
            marker=re.match(r'^\s*(`{3,}|~{3,})',line)
            if marker:
                if fence is None:fence=marker[1][0]
                elif marker[1][0]==fence:fence=None
                lines.append(line)
            else:lines.append(line if fence else re.sub(r'\]\(([^)]+)\)',source_link,line))
        document.write_text(''.join(lines),encoding='utf-8')
    (release/'README.md').write_text(INSTALL_README,encoding='utf-8')
    shutil.copy2(ROOT/'scripts/Install-Pack.ps1',release/'Install-Pack.ps1')
    (release/'Install-Pack.cmd').write_bytes(b'@echo off\r\npowershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Install-Pack.ps1" %*\r\nif errorlevel 1 pause\r\n')
    hashes={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in jars}
    (release/'manifests/payload-sha256.json').write_text(json.dumps(hashes,indent=2)+'\n',encoding='utf-8')
    output=ROOT/'artifacts/releases';output.mkdir(parents=True,exist_ok=True)
    archive=output/f'Create263-Pack-v{VERSION}-installer.zip'
    with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as z:
        allowed=[release/name for name in ['README.md','THIRD_PARTY_NOTICES.md','Install-Pack.cmd','Install-Pack.ps1']]
        for folder in ['payload','LICENSES','manifests','modpack','docs']:
            allowed.extend(p for p in (release/folder).rglob('*') if p.is_file())
        for p in sorted(allowed):z.write(p,p.relative_to(release).as_posix())
    print(archive.relative_to(ROOT).as_posix(),hashlib.sha256(archive.read_bytes()).hexdigest())
if __name__=='__main__':main()
