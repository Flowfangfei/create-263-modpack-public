# 五个 Create 附属模组的源码检索与收集

检索日期：2026-10-25。结论对应本次固定提交与发布附件，后续上游可能继续更新。

五个目标都找到公开源码。已收集 12 份固定 Git 提交的源码 ZIP，以及 4 份发布平台提供的 `sources.jar`。本次未找到可核验的 26.3 移植源码；已证实的较高版本参考是 Electro Energetics 的 Fabric 26.2 移植，以及附魔工业的 Fabric 26.1.2 / Create Fly 分支。

本轮完成源码定位、下载和静态结构核验。没有执行这些源码的本地编译或游戏测试，也没有将它们加入现有运行包。源码 ZIP 是快照，不含 Git 历史。

## 官方源码与适配基线

| 模组 | 已核实仓库 | 主要构建目标 | 许可声明 |
| --- | --- | --- | --- |
| Create: Electro Energetics | [george8188625/Create-Electro-Energetics](https://github.com/george8188625/Create-Electro-Energetics) | 1.21.1 NeoForge；开发 1.2.0、稳定 1.1.4 | MIT |
| Create: Storage | [ausmez/create-storage-neo-forge](https://github.com/ausmez/create-storage-neo-forge) | 1.21.1 NeoForge 1.3.8；另存旧包 1.3.4 | 仓库 GPL v3；Modrinth 标注 GPL-3.0-or-later |
| Create: FluidLogistics | [yision1/CreateFluidLogistics](https://github.com/yision1/CreateFluidLogistics) | 1.21.1 NeoForge；当前开发配置仍写 1.3.1 | 自有部分 BSD-3-Clause，另含 LGPL 与 MIT 部分 |
| Create: Additional Logistics | [KhloeLeclair/CreateAdditionalLogistics](https://github.com/KhloeLeclair/CreateAdditionalLogistics) | 1.21.1 NeoForge 1.4.5 | MIT；保留 Create 来源声明 |
| Create: Enchantment Industry | [DragonsPlusMinecraft/CreateEnchantmentIndustry](https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry) | 1.21.1 NeoForge 2.5.4b；另存旧包 2.4.2 | LGPL-3.0-or-later |

身份核验使用各自 CurseForge 的 Source / Modrinth 的 View source 链接，以及旧包 JAR 中的 mod id、作者与 issue URL。FluidLogistics 的旧仓库地址 `CreateFluidLogistic` 会重定向至复数 `CreateFluidLogistics`。Storage 对应 `fxntstorage`；它源于 FoxyNoTail 的 Fabric 模组，ausmez 维护的是旧整合包采用的 NeoForge/Forge 分支。

所有下载的仓库快照均包含 Java 源码、Gradle 构建文件与 Wrapper。结构齐全不能替代实际依赖解析、编译和运行验证。

## 与旧整合包的对应关系

| 旧包中的版本 | 已收集的来源 | 核验边界 |
| --- | --- | --- |
| Electro Energetics 1.21.1-1.1.4 | 官方稳定分支 `73a40be208b2...` | 版本声明吻合，未验证重建 JAR 与旧二进制逐字节一致 |
| Storage 1.3.4 | 官方 release 提交 `b1f26dabf3ae...` | 提交说明及配置均为 1.3.4；另保留当前 1.3.8 |
| FluidLogistics 1.3.1 | Modrinth 发布版本 `YepGDZRz` 的 sources.jar | 应优先作为旧包发布源码；master 已包含发布后修改，版本号相同不等于源码相同 |
| Additional Logistics 1.4.5 | 官方 main `b96c27a72c51...` | 配置为 1.4.5，未验证二进制可复现一致 |
| Enchantment Industry 2.4.2 | Modrinth `qUpvzZ83` 的 sources.jar；官方提交 `56d1d50a282f...` | 发布源码与构建工程分别保留；另存当前 2.5.4b |

## 可参考的较高版本源码

### Electro Energetics：Fabric 26.2

[cat4blep/Creaty-fly-Electro-Energetics](https://github.com/cat4blep/Creaty-fly-Electro-Energetics)，分支 `fabric-26.2`，固定提交 `1033dcb653a3dafcbd436f1da5bd109fbd8e6ac6`。

[版本配置](https://github.com/cat4blep/Creaty-fly-Electro-Energetics/blob/1033dcb653a3dafcbd436f1da5bd109fbd8e6ac6/gradle.properties)声明 MC 26.2、Java 25、Fabric API 0.161.0+26.2、Create Fly 26.2-6.0.9-2。其 [GitHub Release](https://github.com/cat4blep/Creaty-fly-Electro-Energetics/releases/tag/v1.2.0%2Bfabric.26.2)提供源码 JAR，下载后已与发布资产的 SHA-256 对上。

[该提交的远程 CI](https://github.com/cat4blep/Creaty-fly-Electro-Energetics/actions/runs/38776745741)显示 build 与客户端 GameTest 步骤成功。这是上游远程证据，本轮没有在本机重复执行。

该移植基于官方开发提交 `4828c348...`。核心电力与电车逻辑可作为 API 迁移参考；[兼容说明](https://github.com/cat4blep/Creaty-fly-Electro-Energetics/blob/1033dcb653a3dafcbd436f1da5bd109fbd8e6ac6/FABRIC_PORT.md)列出 Sable、Aeronautics、Offroad、Simulated 等集成的限制，部分代码位于 `src/unsupported`。适配现有 NeoForge 26.3 包仍需要保留并迁移这些集成，不能直接把 Fabric 成品放入现有实例。

### Enchantment Industry：Fabric 26.1.2

[DragonsPlusMinecraft/CreateEnchantmentIndustry-Fly](https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry-Fly) 位于原作者组织，分支 `26.1.2/6.0.0-dev`，固定提交 `2dcc94608cf7787ce6bc045fb191eba3f82ef676`。

源码配置为 mod 2.5.2-c、MC 26.1.2、Java 25、Fabric Loader 0.19.3、Create Fly 26.1.2-6.0.9-4；已收集对应官方发布 sources.jar。README 简写 2.5.2，具体补丁版本以固定提交的 `gradle.properties` 与发布附件为准。

该分支包含核心经验、附魔、烈焰机器、Ponder 和 JEI；其说明列出的 Apotheosis / Apothic Enchanting / Sable 联动不在该 Fabric 构建中。迁移时应从 NeoForge 源码保留现有包需要的联动，从 Fly 分支参考较新 Minecraft API 的改法。

### 必须一并考虑的前置

当前 NeoForge 附魔工业依赖独立的 [Create: Dragons Plus](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus)。已收集其 1.21.1 分支 1.11.9，以及同组织 [CreateDragonsPlus-Fly](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus-Fly) 的 26.1.2 分支。前置需要单独适配，不能只改附魔工业的加载器声明。

## 其他分支与排除项

- Storage 官方还维护 `1.20.1-6.0.x`；Additional Logistics 还存在 `1.20.1`。这些是旧版本参考，未下载为本轮主要基线。
- [FoxyNoTail 原 Fabric 项目](https://github.com/foxynotail/create-storage-mod-fabric)及 [Storage Refabricated](https://github.com/SiliconCarbideCube/create-storage-mod-refabricated)可参考跨加载器实现；后者已核实为 1.20.1 Fabric，没有证实 26.x 适配。
- [create-storage-extended](https://github.com/Tetrachlorosilane/create-storage-extended)的 mod id 为 `createstorageextended`，依赖 Storage；它是独立扩展，不能当成 Storage 整体移植。
- [LeeWikie 的附魔工业分支](https://github.com/LeeWikie/CreateEnchantmentIndustry/tree/fabric-26.1.2-create-fly)也是 26.1.2 Fabric，配置依赖相邻目录的 Create Fly JAR；本轮优先保存原作者组织维护的 Fly 分支。
- [附魔工业 Fabric 分支](https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry-Fabric)是 1.20.1，另有已归档 Legacy 仓库；不能与 Fly 26.1.2 混同。
- [electroenergetics-TFCR](https://github.com/GranniXXie/electroenergetics-TFCR)仅有说明、许可与 JAR，没有可用源码工程。
- RosaryMala 的 Electro Energetics 分支虽名为 `1.20.1`，实际构建配置仍写 1.21.1；没有把分支名称当作移植完成证据。

## 许可记录

Electro Energetics 与 Additional Logistics 的实际许可文本为 MIT。Storage 的 LICENSE 是 GPL v3，而发布平台标为 GPL-3.0-or-later，后续分发应保留上游的具体声明。

[FluidLogistics 的许可说明](https://github.com/yision1/CreateFluidLogistics/blob/28e0af76fe8f4300ba5cc7f28cc25691ff1ac5e3/README.md)明确说明部分代码来自 CreateDragonsPlus（LGPL-3.0-or-later）与 CreateFluidStuffs（MIT），所以不能把全部文件统一改标为 BSD。附魔工业及 Dragons Plus 配置均声明 LGPL-3.0-or-later；Dragons Plus 另有 `LICENSING.txt`、Create / Ponder 来源许可。下载的原始归档保留这些文件，尚未对所有资源进行逐文件分发审计。

## 本地资料与核验

- [机器可读索引](../manifests/source-discovery-five-addons-2026-10-25.json)：固定提交、下载地址、构建版本、许可文件、Java 文件数、SHA-256 与验证范围。
- [本地下载目录](../artifacts/source-discovery-2026-10-25/)：12 个 ZIP 与 4 个 sources.jar。
- [下载校验清单](../artifacts/source-discovery-2026-10-25/SHA256SUMS.txt)。

16 个文件均重新计算 SHA-256 并检查 ZIP/JAR 的 CRC；4 个发布 sources.jar 还与平台提供的 SHA-1 或 SHA-256 核对成功。发布 sources.jar 主要服务于源码对照，可能不含整个 Gradle 工程；准备迁移时结合仓库快照使用。

下载目录受现有 `.gitignore` 保护。运行模块、安装器、世界、发布包与 GitHub 远端未因本轮检索而改变。

## 固定提交快照清单

| 本地标识 | 源码版本 / MC | 固定提交 | Java 文件数 |
| --- | --- | --- | --- |
| `electroenergetics-upstream-dev` | 1.21.1-1.2.0 / 1.21.1 | [4828c3487a9f](https://github.com/george8188625/Create-Electro-Energetics/tree/4828c3487a9fc0703717b415eb07d005c7a48653) | 513 |
| `electroenergetics-upstream-stable` | 1.21.1-1.1.4 / 1.21.1 | [73a40be208b2](https://github.com/george8188625/Create-Electro-Energetics/tree/73a40be208b2a120f7cf9549a2c1c216b82100cf) | 477 |
| `storage-neoforge` | 1.3.8 / 1.21.1 | [fc8ab27bde33](https://github.com/ausmez/create-storage-neo-forge/tree/fc8ab27bde33458b7edfc24cf9cd053caf5fa9a7) | 312 |
| `additional-logistics` | 1.4.5 / 1.21.1 | [b96c27a72c51](https://github.com/KhloeLeclair/CreateAdditionalLogistics/tree/b96c27a72c51dd2e41b75d8945863774a68a0f6a) | 173 |
| `dragons-plus-neoforge` | 1.11.9 / 1.21.1 | [56f7a4a9f2fc](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus/tree/56f7a4a9f2fc04fa643fd993f217094bd30f8275) | 294 |
| `dragons-plus-fly` | 1.11.8 / 26.1.2 | [27fd0e647e2e](https://github.com/DragonsPlusMinecraft/CreateDragonsPlus-Fly/tree/27fd0e647e2e1f644dce92aeba4027fe2dfb6414) | 129 |
| `fluidlogistics` | 1.3.1 / 1.21.1 | [28e0af76fe8f](https://github.com/yision1/CreateFluidLogistics/tree/28e0af76fe8f4300ba5cc7f28cc25691ff1ac5e3) | 473 |
| `enchantment-industry-neoforge` | 2.5.4b / 1.21.1 | [7cb2e9f59d83](https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry/tree/7cb2e9f59d83c800b3dde4d0a9123cbcc083c9a5) | 351 |
| `enchantment-industry-fly` | 2.5.2-c / 26.1.2 | [2dcc94608cf7](https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry-Fly/tree/2dcc94608cf7787ce6bc045fb191eba3f82ef676) | 169 |
| `electroenergetics-fly` | 1.2.0+fabric.26.2 / 26.2 | [1033dcb653a3](https://github.com/cat4blep/Creaty-fly-Electro-Energetics/tree/1033dcb653a3dafcbd436f1da5bd109fbd8e6ac6) | 574 |
| `storage-neoforge-oldpack` | 1.3.4 / 1.21.1 | [b1f26dabf3ae](https://github.com/ausmez/create-storage-neo-forge/tree/b1f26dabf3ae0ec45762475733d49d812bca8c96) | 297 |
| `enchantment-industry-oldpack` | 2.4.2 / 1.21.1 | [56d1d50a282f](https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry/tree/56d1d50a282f015a322769f98353d0d7b1d88b87) | 307 |

Java 文件数统计归档中的全部 `.java`，可能包含测试、生成源码或未启用源集，不能直接用来推算移植成本。

## 检索范围和结论边界

检索涵盖官方 CF / Modrinth / GitHub、公开 fork 的分支、精确名称与 mod id、26.2 / 26.3 关键词及其他代码托管站的公开索引。已检查的可访问 fork 数分别为 Electro Energetics 42、Storage 7、Additional Logistics 5、FluidLogistics 8、Enchantment Industry 82；后者是 API 当时可访问并完整分页的集合，不等同于页面显示的历史 fork 总数。

本次没有发现可核验的 26.3 分支或发布。这个结论不覆盖未公开、未索引、不可访问或尚未发布的移植。内置浏览器多次超时，页面证据采用网页读取，仓库内容采用 gh API，并由实际下载的源码文件补证。
