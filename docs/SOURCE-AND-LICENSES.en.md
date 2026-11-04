# Source provenance, recovery and licensing

[中文](SOURCE-AND-LICENSES.md) · [Full component notices](../THIRD_PARTY_NOTICES.md) · [License texts](../LICENSES/)

This project combines independent mods and adaptation sources with local recovery, porting and integration changes. Each component keeps its own license. Public access to the repository does not turn PolyForm, noncommercial terms or reserved artwork into a single MIT grant or unrestricted open-source content.

## Authors and provenance records

Original authors and contributors are identified by the retained copyright headers, licenses and upstream notices. The Create code notices credit The Create Team / The Creators of Create; the Create: Adapted notice credits AlexSensey and contributors. The Aeronautics suite credits The Simulated Team / The Creators of Aeronautics. Other components retain their own notices in their source and license files. Local porting, reverse-engineering recovery and build work do not replace those credits or make this project the sole original author of the combined code.

The [source manifest](../manifests/sources.json) records repositories, pinned commits, import methods and per-file SHA-256 manifests. The [third-party license manifest](../manifests/third-party-licenses.json) records Registrate, compatibility code and referenced code. The [resource lock](../manifests/resources-lock.json) records separately acquired assets, fixed sources and hashes. Repository ownership, port maintenance and original copyright ownership are different roles; a GitHub account name alone does not establish ownership of every file.

An `upstream_head` identifies a provenance baseline. Some imports explicitly include a working-tree snapshot with uncommitted port changes. It does not assert that every current file equals that upstream commit, and no historical commits are invented for those snapshots.

## Differential recovery of Create 26.3

The Create 26.3 recovery used here starts from the public MC 26.2 / Adapted 0.996 source at [`30ec1245a5af80228699453c8e59f3285a6be074`](https://github.com/AlexSensey/create-adapted/tree/30ec1245a5af80228699453c8e59f3285a6be074). Two Adapted 1.31 release JARs were also compared at method level to narrow the 26.3 recovery scope:

| Input | Fixed source | SHA-256 |
| --- | --- | --- |
| MC 26.2 / Adapted 1.31, comparison reference | [CurseForge file 9075359](https://www.curseforge.com/minecraft/mc-mods/create-adapted/files/9075359) | `2489d8344a121c6751df05265abee4397d014986cacaf1a1b4bbbc61488a9dba` |
| MC 26.3 / Adapted 1.31, recovery target reference | [CurseForge file 9075351](https://www.curseforge.com/minecraft/mc-mods/create-adapted/files/9075351) | `4e8684618edeedae5dbfbe5ec8ba05d57c8c251cb34beb69160f53cbd97b4099` |

These inputs are documented in the original recovery workspace’s `RECOVERY.md`, `binary-diff.md` and `reference-manifest.json`; the input hashes have been checked again. This page preserves the fixed sources and digests without including raw decompiler caches or work records containing machine paths.

Recovery compared methods, instructions, symbols and exception handling in the release classes. Decompiled references and the actual 26.3 API contracts guided the changes merged into maintainable public source. The 26.2 / 1.31 JAR narrowed the comparison; no complete intermediate author source tree for that release was invented first.

The recovered version is an implementation reconstructed and maintained by this project, with subsequent modpack fixes. It is not presented as the author’s original 26.3 source and is not claimed to reproduce the reference binary byte for byte. Decompilation and differential analysis do not create permissions that the original rights holders did not grant. Code, third-party portions and artwork retain their respective licenses and credits. Provenance and static comparison do not replace build, runtime or gameplay verification.

## Component license scope

| Component | Pinned upstream source | License and provenance |
| --- | --- | --- |
| Create / Create: Adapted | [30ec1245a5af](https://github.com/AlexSensey/create-adapted/tree/30ec1245a5af80228699453c8e59f3285a6be074) | MIT; Create artwork is managed separately; internal NeoForge compatibility code is LGPL-2.1-only. [Text](../LICENSES/MIT-CREATE.txt) |
| Steam ’n’ Rails | [575ee893a984](https://github.com/zoom-packers/Create-Steam-n-Rails-Fly/tree/575ee893a9842998d1609329429c7e67495ddc7d) | LGPL-3.0-or-later, with the retained Apache-2.0 Quilt DataFixer notice. [Text](../LICENSES/LGPL-3.0-RAILWAYS.txt) |
| Sable | [113035f369cf](https://github.com/LyXith/sable-port/tree/113035f369cf737b4c8a8c2262c609939d5dab22) | PolyForm Shield 1.0.0, including its noncompete conditions; companion MIT and Rapier Apache-2.0 notices remain separate. [Text](../LICENSES/POLYFORM-SHIELD-SABLE.md) |
| Aeronautics / Simulated / Offroad | [943753a38209](https://github.com/LyXith/create-aeronautics-fly/tree/943753a38209dde0f24eaeacc89240772d9e5581) | MIT code; the specified assets directories of all three projects are All Rights Reserved, including files in other namespaces. [Text](../LICENSES/AERONAUTICS-CODE-AND-ASSETS.md) |
| Create Connected Adapted | [eca2561fc423](https://github.com/AlexSensey/create-connected-adapted/tree/eca2561fc423f9d024eed290b671025073d79c4c) | AGPL-3.0 with additional terms requiring an origin link and a distinct name and icon for modified versions. [Text](../LICENSES/AGPL-3.0-CONNECTED-WITH-ADDITIONAL-TERMS.txt) |
| Create Diesel Generators Adapted | [f5cff1bc288a](https://github.com/AlexSensey/create-diesel-generators-adapted/tree/f5cff1bc288a373bdb304c8bb312ebb3246c9556) | MIT code; engine_normal.ogg is separately subject to the Pixabay Content License. [Text](../LICENSES/MIT-DIESEL.txt) |
| Create Liquid Fuel | [7728b17e541c](https://github.com/vibeported-mc/CreateLiquidFuel/tree/7728b17e541c046012860987191e5026da3c8358) | The imported source is MIT, retaining Forsteri’s copyright; the 26.2 port branch is the adaptation reference. [Text](../LICENSES/MIT-LIQUID-FUEL.txt) |
| Create Ore Excavation | [724e18dd2c79](https://github.com/tom5454/Create-Ore-Excavation/tree/724e18dd2c792440976c0ec469d56d9f2c5e8411) | MIT, with Jere655’s 1.21.11 work-in-progress branch as an additional port reference. [Text](../LICENSES/MIT-ORE-EXCAVATION.txt) |
| Transmission & Linkage | [322c4182d059](https://github.com/userenxv/aeronautics-utility-objects/tree/322c4182d0593cde79fd7a5217dbb84283d9c65f) | LICENSE declares CC-BY-NC-4.0 and an additional file contains PolyForm Shield. Both are retained without treating either as a replacement; credited author: enxv233. [Text](../LICENSES/CC-BY-NC-4.0-TRANSMISSION-LINKAGE.txt) |
| Create Electro Energetics | [4828c3487a9f](https://github.com/george8188625/Create-Electro-Energetics/tree/4828c3487a9fc0703717b415eb07d005c7a48653) | MIT, retaining George VI’s copyright; combines the NeoForge feature source with a pinned Fabric 26.2 port reference. [Text](../LICENSES/SOURCE-create-electro-energetics-adapted-263-LICENSE) |
| Create Storage | [fc8ab27bde33](https://github.com/ausmez/create-storage-neo-forge/tree/fc8ab27bde33458b7edfc24cf9cd053caf5fa9a7) | The source includes GNU GPL v3 text; the distribution listing has declared GPL-3.0-or-later. This distinction is retained without replacing the original version terms. [Text](../LICENSES/SOURCE-create-storage-adapted-263-LICENSE) |
| Create Additional Logistics | [b96c27a72c51](https://github.com/KhloeLeclair/CreateAdditionalLogistics/tree/b96c27a72c51dd2e41b75d8945863774a68a0f6a) | MIT, retaining Khloe Leclair’s copyright and the cited Create notices. [Text](../LICENSES/SOURCE-create-additional-logistics-adapted-263-LICENSE-txt) |
| Create FluidLogistics | [28e0af76fe8f](https://github.com/yision1/CreateFluidLogistics/tree/28e0af76fe8f4300ba5cc7f28cc25691ff1ac5e3) | Own code is BSD-3-Clause; CDP LGPL-3.0-or-later and CreateFluidStuffs MIT portions retain their respective notices. [Text](../LICENSES/SOURCE-create-fluid-logistics-adapted-263-LICENSE-txt) |
| Create Dragon’s Plus | [56f7a4a9f2fc](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus/tree/56f7a4a9f2fc04fa643fd993f217094bd30f8275) | LGPL-3.0-or-later; combines a pinned NeoForge feature baseline with the Fly port reference and retains referenced MIT notices. [Text](../LICENSES/SOURCE-create-dragons-plus-adapted-263-LICENSE-txt) |
| Create Enchantment Industry | [7cb2e9f59d83](https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry/tree/7cb2e9f59d83c800b3dde4d0a9123cbcc083c9a5) | LGPL-3.0-or-later; combines a pinned NeoForge feature baseline with the Fly port reference and retains notices for referenced assets. [Text](../LICENSES/SOURCE-create-enchantment-industry-adapted-263-LICENSE-txt) |

Flywheel, Ponder and Catnip code licenses are retained in the [Flywheel](../modules/create-adapted-263/Flywheel/LICENSE.md) and [Ponder](../modules/create-adapted-263/Ponder/LICENSE) projects. Create’s internal `compat/neoforge263` classes derive from NeoForge 26.2.0.75 and retain Forge Development LLC and contributor notices under LGPL-2.1-only. Namespace relocation and API changes are described in the [compatibility notice](../LICENSES/CREATE-NeoForge-compat-NOTICE.txt). These internal classes are not official NeoForge APIs.

Registrate source is pinned to `MC1.21-1.3.0+67` under MPL-2.0. Original source is in [vendor](../vendor/registrate-MC1.21-1.3.0+67/); the archive digest and build transformations are recorded in the third-party license manifest. Source files, copyright headers and full license texts must accompany the applicable distribution.

Additional port references are pinned as well:

- Ore Excavation: [Jere655’s `d95850584d1c`](https://github.com/Jere655/Create-Ore-Excavation/tree/d95850584d1c6beb055ca1b50e510f0e1d500832).
- Electro Energetics: [Fabric 26.2 reference `1033dcb653a3`](https://github.com/cat4blep/Creaty-fly-Electro-Energetics/tree/1033dcb653a3dafcbd436f1da5bd109fbd8e6ac6).
- Dragon’s Plus: [Fly reference `27fd0e647e2e`](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus-Fly/tree/27fd0e647e2e1f644dce92aeba4027fe2dfb6414).
- Enchantment Industry: [Fly reference `2dcc94608cf7`](https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry-Fly/tree/2dcc94608cf7787ce6bc045fb191eba3f82ef676).
- CreateFluidStuffs portions cited by FluidLogistics: [license at `63f7f35689b5`](https://github.com/MoePus/create-fluidstuffs/blob/63f7f35689b5d73e0828a978db1b8ab2da25abe6/LICENSE). The original unfilled copyright-holder field is preserved; no owner is invented.

Diesel’s pinned public source properties identify 0.607, while the requested distribution identifies 0.608. The [source manifest](../manifests/sources.json) retains that distinction. The configuration-enum compatibility fix is local work, and the source and binary are not described as an exact pair. The two Transmission & Linkage license files and Storage’s GPL version-label distinction are likewise retained. This document provides no single commercial-use grant for the pack.

## Code and asset distribution boundaries

The Aeronautics [license at the pinned commit](https://github.com/LyXith/create-aeronautics-fly/blob/943753a38209dde0f24eaeacc89240772d9e5581/LICENSE.md) reserves all files under these three directories:

```text
aeronautics/src/main/resources/assets/
simulated/src/main/resources/assets/
offroad/src/main/resources/assets/
```

The boundary follows source directories, including files under namespaces such as `assets/minecraft/`, `assets/musicnotification/` and `assets/amendments/`. Moving a file, repackaging it, recovering code through reverse engineering, or releasing other code under MIT does not add distribution rights for these assets.

Official Create artwork is managed separately from the code license. This project uses an [external-assets edition](../modules/create-adapted-263/EXTERNAL_ASSETS.md) and retrieves a pinned official JAR through the resource lock rather than mirroring the artwork JAR as a source dependency. Diesel’s `engine_normal.ogg` remains subject to its [Pixabay notice](../LICENSES/DIESEL-THIRD-PARTY-NOTICES.md); it is not relicensed as MIT audio. Source and hash checks during installation do not change the original terms.

## Corresponding source and asset verification

The [development overview](DEVELOPMENT-OVERVIEW.en.md) explains source recovery, loader ports and dependency bridges. This page concentrates on provenance, attribution and license boundaries.

Asset filtering follows complete source directories and the [resource lock](../manifests/resources-lock.json). The early main-namespace filter missed eight cross-namespace files, including three PNGs; artwork terms inside old reference dependency JARs also require separate inspection. The current [publication policy](../scripts/publication_policy.py), [payload assembly](../scripts/package.py) and [source export](../scripts/source_archive.py) check these boundaries independently.

The installer excludes locked Aero resources, official Create artwork and separately licensed audio, restoring them locally from pinned sources. All 1593 Aero targets and the restored 18 JAR entry sets have been verified. Source manifests, recovery inputs and artifact digests establish traceability; see the [release record](RELEASE.md) for versions and coverage. Successful verification does not change original licenses.

Distribution also requires inspection of source archives, nested JARs, reachable history and older attachments. A deletion from the latest tree does not delete historical objects. Snapshot identity is recorded in the [source import manifest](../manifests/publication-source.json).

GNU GPL/LGPL/AGPL, MPL, MIT, BSD, PolyForm and CC-BY-NC terms apply separately. Distribution must preserve the relevant notices, modified-version provenance and corresponding source, and meet each component’s requirements. Integration or recovery work does not provide a basis for relicensing the entire collection under one license.
