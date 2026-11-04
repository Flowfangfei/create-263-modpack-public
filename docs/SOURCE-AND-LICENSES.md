# 源码来源、恢复方式与许可

[English](SOURCE-AND-LICENSES.en.md) · [组件完整声明](../THIRD_PARTY_NOTICES.md) · [许可文件](../LICENSES/)

本工程汇集多个独立模组及其适配源码，并包含本地恢复、移植和整合修改。各组件继续适用原有许可；仓库可被公开阅读，也不会因此使 PolyForm、非商业条款或保留权利的素材变成统一 MIT 或不受限制的开源内容。

## 作者与来源记录

原作者及贡献者以保留的版权头、许可证和上游声明为准。Create 代码声明列出 The Create Team / The Creators of Create，Create: Adapted 的适配声明列出 AlexSensey 与贡献者；Aeronautics 系列声明列出 The Simulated Team / The Creators of Aeronautics。其他组件的版权信息继续保留在对应源码和许可文件中。本工程的移植、逆向恢复和构建工作不替代这些署名，也不将整套代码标为本工程独立创作。

[来源清单](../manifests/sources.json)记录上游仓库、固定提交、导入方式和逐文件 SHA-256 清单；[第三方许可清单](../manifests/third-party-licenses.json)补充 Registrate、兼容代码和引用代码的来源。[资源锁](../manifests/resources-lock.json)记录单独获取的素材及其固定来源和 hash。仓库所有者、移植维护者和原始版权人是不同角色，不能只根据 GitHub 用户名推定版权归属。

清单中的 `upstream_head` 是来源基点。部分导入项明确为“包含未提交移植修改的工作树快照”，不能据此声称当前文件全部等同该上游提交，也不为这些快照补造历史提交。

## Create 26.3 的差分恢复

本工程使用的 Create 26.3 恢复版，以公开的 MC 26.2 / Adapted 0.996 源码提交 [`30ec1245a5af80228699453c8e59f3285a6be074`](https://github.com/AlexSensey/create-adapted/tree/30ec1245a5af80228699453c8e59f3285a6be074)为底座。另使用两份 Adapted 1.31 发布包进行方法级差分，缩小 26.3 的恢复范围：

| 输入 | 固定来源 | SHA-256 |
| --- | --- | --- |
| MC 26.2 / Adapted 1.31，比较参照 | [CurseForge 文件 9075359](https://www.curseforge.com/minecraft/mc-mods/create-adapted/files/9075359) | `2489d8344a121c6751df05265abee4397d014986cacaf1a1b4bbbc61488a9dba` |
| MC 26.3 / Adapted 1.31，恢复目标参照 | [CurseForge 文件 9075351](https://www.curseforge.com/minecraft/mc-mods/create-adapted/files/9075351) | `4e8684618edeedae5dbfbe5ec8ba05d57c8c251cb34beb69160f53cbd97b4099` |

以上输入来自原恢复工程的 `RECOVERY.md`、`binary-diff.md` 与 `reference-manifest.json` 记录，输入文件 hash 已重新核对。本页保留固定来源与摘要，不收录原始反编译缓存或含本机路径的工作记录。

恢复过程比较发布类的方法、指令、符号与异常处理结构，结合反编译参照和实际 26.3 API 契约，把所需差异合并到可维护的公开源码中。26.2 / 1.31 发布包用于缩小差异，没有先补造一套该版本的完整原作者源码。

这里的“恢复版”是本工程重建并继续维护的实现，包含随后为整合包编写的修复。它未被标为原作者提供的 26.3 原始源码，也不承诺与参照二进制逐字节相同。反编译或差分分析不会产生原权利人未授予的新许可证；代码、第三方引用与素材各自的许可和署名仍然适用。来源记录和静态对照也不代替构建、运行及玩法验证。

## 各组件的许可范围

| 组件 | 固定上游源码 | 许可与来源说明 |
| --- | --- | --- |
| Create / Create: Adapted | [30ec1245a5af](https://github.com/AlexSensey/create-adapted/tree/30ec1245a5af80228699453c8e59f3285a6be074) | MIT；Create 美术独立管理；内部 NeoForge 兼容代码另适用 LGPL-2.1-only。 [原文](../LICENSES/MIT-CREATE.txt) |
| Steam ’n’ Rails | [575ee893a984](https://github.com/zoom-packers/Create-Steam-n-Rails-Fly/tree/575ee893a9842998d1609329429c7e67495ddc7d) | LGPL-3.0-or-later；保留 Quilt DataFixer 的 Apache-2.0 声明。 [原文](../LICENSES/LGPL-3.0-RAILWAYS.txt) |
| Sable | [113035f369cf](https://github.com/LyXith/sable-port/tree/113035f369cf737b4c8a8c2262c609939d5dab22) | PolyForm Shield 1.0.0，有竞争用途限制；companion 的 MIT、Rapier 的 Apache-2.0 声明分别保留。 [原文](../LICENSES/POLYFORM-SHIELD-SABLE.md) |
| Aeronautics / Simulated / Offroad | [943753a38209](https://github.com/LyXith/create-aeronautics-fly/tree/943753a38209dde0f24eaeacc89240772d9e5581) | 代码 MIT；三个工程指定 assets 目录为 All Rights Reserved，跨 namespace 的内容同样在范围内。 [原文](../LICENSES/AERONAUTICS-CODE-AND-ASSETS.md) |
| Create Connected Adapted | [eca2561fc423](https://github.com/AlexSensey/create-connected-adapted/tree/eca2561fc423f9d024eed290b671025073d79c4c) | AGPL-3.0 及附加条款；要求保留来源链接，修改版使用可区分的名称和图标。 [原文](../LICENSES/AGPL-3.0-CONNECTED-WITH-ADDITIONAL-TERMS.txt) |
| Create Diesel Generators Adapted | [f5cff1bc288a](https://github.com/AlexSensey/create-diesel-generators-adapted/tree/f5cff1bc288a373bdb304c8bb312ebb3246c9556) | 代码 MIT；engine_normal.ogg 单独适用 Pixabay Content License。 [原文](../LICENSES/MIT-DIESEL.txt) |
| Create Liquid Fuel | [7728b17e541c](https://github.com/vibeported-mc/CreateLiquidFuel/tree/7728b17e541c046012860987191e5026da3c8358) | 导入源码为 MIT，保留 Forsteri 版权；26.2 移植分支为适配参照。 [原文](../LICENSES/MIT-LIQUID-FUEL.txt) |
| Create Ore Excavation | [724e18dd2c79](https://github.com/tom5454/Create-Ore-Excavation/tree/724e18dd2c792440976c0ec469d56d9f2c5e8411) | MIT；Jere655 的 1.21.11 工作分支作为额外移植参照。 [原文](../LICENSES/MIT-ORE-EXCAVATION.txt) |
| Transmission & Linkage | [322c4182d059](https://github.com/userenxv/aeronautics-utility-objects/tree/322c4182d0593cde79fd7a5217dbb84283d9c65f) | LICENSE 为 CC-BY-NC-4.0，另附 PolyForm Shield；两份均保留，不自行择一替代，作者 enxv233。 [原文](../LICENSES/CC-BY-NC-4.0-TRANSMISSION-LINKAGE.txt) |
| Create Electro Energetics | [4828c3487a9f](https://github.com/george8188625/Create-Electro-Energetics/tree/4828c3487a9fc0703717b415eb07d005c7a48653) | MIT；保留 George VI 版权，融合 NeoForge 功能源与固定 Fabric 26.2 移植参照。 [原文](../LICENSES/SOURCE-create-electro-energetics-adapted-263-LICENSE) |
| Create Storage | [fc8ab27bde33](https://github.com/ausmez/create-storage-neo-forge/tree/fc8ab27bde33458b7edfc24cf9cd053caf5fa9a7) | 源码随附 GNU GPL v3；发布站曾标 GPL-3.0-or-later，保留这一区别，不代替原文件指定授权版本范围。 [原文](../LICENSES/SOURCE-create-storage-adapted-263-LICENSE) |
| Create Additional Logistics | [b96c27a72c51](https://github.com/KhloeLeclair/CreateAdditionalLogistics/tree/b96c27a72c51dd2e41b75d8945863774a68a0f6a) | MIT；保留 Khloe Leclair 与引用的 Create 版权。 [原文](../LICENSES/SOURCE-create-additional-logistics-adapted-263-LICENSE-txt) |
| Create FluidLogistics | [28e0af76fe8f](https://github.com/yision1/CreateFluidLogistics/tree/28e0af76fe8f4300ba5cc7f28cc25691ff1ac5e3) | 自有代码 BSD-3-Clause；CDP 的 LGPL-3.0-or-later 与 CreateFluidStuffs 的 MIT 部分分别保留。 [原文](../LICENSES/SOURCE-create-fluid-logistics-adapted-263-LICENSE-txt) |
| Create Dragon’s Plus | [56f7a4a9f2fc](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus/tree/56f7a4a9f2fc04fa643fd993f217094bd30f8275) | LGPL-3.0-or-later；结合固定 NeoForge 功能基线及 Fly 移植参照，保留引用的 MIT 声明。 [原文](../LICENSES/SOURCE-create-dragons-plus-adapted-263-LICENSE-txt) |
| Create Enchantment Industry | [7cb2e9f59d83](https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry/tree/7cb2e9f59d83c800b3dde4d0a9123cbcc083c9a5) | LGPL-3.0-or-later；结合固定 NeoForge 功能基线及 Fly 移植参照，保留引用素材声明。 [原文](../LICENSES/SOURCE-create-enchantment-industry-adapted-263-LICENSE-txt) |

Flywheel、Ponder、Catnip 的代码许可保留在 [Flywheel](../modules/create-adapted-263/Flywheel/LICENSE.md) 和 [Ponder](../modules/create-adapted-263/Ponder/LICENSE) 工程内。Create 内部 `compat/neoforge263` 类源于 NeoForge 26.2.0.75，保留 Forge Development LLC 与贡献者版权及 LGPL-2.1-only 声明；其命名空间迁移与 API 调整见[兼容代码声明](../LICENSES/CREATE-NeoForge-compat-NOTICE.txt)。这些内部类不应被描述为官方 NeoForge API。

Registrate 的精确源码版本为 `MC1.21-1.3.0+67`，适用 MPL-2.0；原源码位于 [vendor](../vendor/registrate-MC1.21-1.3.0+67/)，原始归档 hash 与构建变换由第三方许可清单记录。源码文件、版权头和完整许可须随相应分发保留。

额外移植参照也有固定来源：

- Ore Excavation：[Jere655 的 `d95850584d1c`](https://github.com/Jere655/Create-Ore-Excavation/tree/d95850584d1c6beb055ca1b50e510f0e1d500832)。
- Electro Energetics：[Fabric 26.2 参照 `1033dcb653a3`](https://github.com/cat4blep/Creaty-fly-Electro-Energetics/tree/1033dcb653a3dafcbd436f1da5bd109fbd8e6ac6)。
- Dragon’s Plus：[Fly 参照 `27fd0e647e2e`](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus-Fly/tree/27fd0e647e2e1f644dce92aeba4027fe2dfb6414)。
- Enchantment Industry：[Fly 参照 `2dcc94608cf7`](https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry-Fly/tree/2dcc94608cf7787ce6bc045fb191eba3f82ef676)。
- FluidLogistics 引用的 CreateFluidStuffs：[固定许可 `63f7f35689b5`](https://github.com/MoePus/create-fluidstuffs/blob/63f7f35689b5d73e0828a978db1b8ab2da25abe6/LICENSE)。保留原文未填写的版权人字段，不补造权利人。

Diesel 的固定公开源码属性为 0.607，用户指定发行文件为 0.608；[来源清单](../manifests/sources.json)保留这一区别，相关配置枚举兼容由本工程实现，不将两者宣称为完全对应的源码和二进制。Transmission & Linkage 的两份许可，以及 Storage 的 GPL 版本声明差异，也继续按原记录保留；本说明不提供统一商业授权。

## 代码与素材分发边界

Aeronautics 上游[固定提交的许可证](https://github.com/LyXith/create-aeronautics-fly/blob/943753a38209dde0f24eaeacc89240772d9e5581/LICENSE.md)将以下三个目录中的所有文件列为 All Rights Reserved：

```text
aeronautics/src/main/resources/assets/
simulated/src/main/resources/assets/
offroad/src/main/resources/assets/
```

该范围按源目录划定，包含 `assets/minecraft/`、`assets/musicnotification/`、`assets/amendments/` 等跨 namespace 内容。更换路径、重新打包、逆向恢复代码或把同目录其他代码采用 MIT 发布，都不会给这些素材增加分发授权。

Create 官方美术独立于代码许可管理；本工程使用[外置素材模式](../modules/create-adapted-263/EXTERNAL_ASSETS.md)，通过资源锁获取固定官方 JAR，不把官方素材 JAR 作为源码依赖镜像。Diesel 的 `engine_normal.ogg` 单独适用 [Pixabay 素材声明](../LICENSES/DIESEL-THIRD-PARTY-NOTICES.md)，也不按 MIT 音效再授权。安装过程的来源和 hash 校验不改变原许可。

## 历史分发问题与当前公开计划

2026-11-04 的只读审计发现，原过滤规则只按三个主要 namespace 剥离素材，遗漏了上述保护目录中的 8 个跨 namespace 文件，其中 3 个 PNG。旧 Git 分支、标签、PR 合并引用及历史源码 ZIP 中仍有这些文件；旧安装器的 Aeronautics payload 也含这 3 个 PNG。历史源码和依赖目录另含一个旧 Create JAR，内部专用 Create 许可证将美术列为保留权利。仅在新提交删除文件不能清除历史对象。

本次公开采用独立仓库 `Flowfangfei/create-263-modpack-public`，仅导入清洁的最新源码快照并发布 `v0.3.0-alpha.3` 附件。公共副本使用独立初始提交，标注真实快照来源和整理方式，不导入原 Git 历史、旧素材或旧发布附件。原仓库 `Flowfangfei/create-263-modpack` 保持私有，完整历史和旧附件保留，不重写历史。

8 个跨 namespace 素材文件和两个旧依赖 JAR 已取消 Git 跟踪，忽略规则同步修正，本地文件字节保留。具体仓库创建、提交、附件上传和校验状态见[发布记录](RELEASE.md)；本节记录已确认的公开方案，不代替实际交付结果。

GNU GPL/LGPL/AGPL、MPL、MIT、BSD、PolyForm 与 CC-BY-NC 的条款分别适用。对外发布应保留原声明、修改版的来源与对应源码，并核对具体组件要求；不得把本工程的整合或恢复工作作为统一重新授权的依据。
