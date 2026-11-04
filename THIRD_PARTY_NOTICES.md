# 组件来源与许可

本仓库是多个独立模组的源码集合和构建工具。没有一份能替代全部组件许可的统一 MIT 授权。原作者的版权声明、源码头和组件许可证保留于各目录。修改的工程文件及对应源码随同本仓库提供。

| 组件 | 上游 | 适用许可及说明 |
| --- | --- | --- |
| Create Adapted | https://github.com/AlexSensey/create-adapted | Create 代码 MIT；官方美术另行授权。本包运行 JAR 采用 external-assets 形式，官方素材由本地安装过程获取。 |
| Flywheel、Ponder、Catnip | Create 源码内附属工程 | 相应代码许可保留在模块内，主要为 MIT；未把上游文档站点作为本包文档发布。 |
| Create NeoForge 兼容代码 | Create 模块内 `compat/neoforge263` | LGPL-2.1，随源码、NOTICE 和许可证提供。 |
| Registrate MC1.21-1.3.0+67 | https://github.com/IThundxr/Registrate | MPL-2.0；[精确源码来源和哈希](manifests/third-party-licenses.json)，对应源码在 `vendor/registrate-MC1.21-1.3.0+67`。 |
| Steam ’n’ Rails | https://github.com/zoom-packers/Create-Steam-n-Rails-Fly | LGPL-3.0-or-later；保留 Quilt DataFixer 的 Apache-2.0 声明。 |
| Aeronautics、Simulated、Offroad | https://github.com/LyXith/create-aeronautics-fly | 代码 MIT；`assets` 内容保留权利，不进入仓库或发布附件，从固定上游快照本地获取。 |
| Sable | https://github.com/LyXith/sable-port | PolyForm Shield 1.0.0，有竞争用途限制，不应称为 MIT 或统一自由软件授权。 |
| Sable 原生依赖与 companion | Sable 模块内附 | 保留 Rapier Apache-2.0 声明及 companion MIT 许可；原生归档的其他依赖声明以随附资料为准。 |
| Create Connected Adapted | https://github.com/AlexSensey/create-connected-adapted | AGPL-3.0 及附加条款。此修改版使用 Create263 Pack 后缀和新图标；不得误称原作者或官方版本。 |
| Create Diesel Generators Adapted | https://github.com/AlexSensey/create-diesel-generators-adapted | 代码 MIT；`engine_normal.ogg` 的 Pixabay Content License 声明保留。该音效由本地安装过程获取。 |
| Create Liquid Fuel | https://github.com/vibeported-mc/CreateLiquidFuel | 导入的26.2源码快照为 MIT，保留 Forsteri 原版权；对应源码经本包迁移至26.3。此结论不自动覆盖发布站其他二进制的许可。 |
| Create Ore Excavation | https://github.com/tom5454/Create-Ore-Excavation | MIT；结合 Jere655 的1.21.11工作分支作为移植参考，精确提交见来源清单及模块文档。 |
| Transmission & Linkage | https://github.com/userenxv/aeronautics-utility-objects | 上游 LICENSE 声明 CC-BY-NC-4.0，另附 LICENSE.md 为 PolyForm Shield；两份均原样保留，不推定其中一份可以删除或替代另一份。作者 enxv233，本包为26.3修改版；不提供统一商业使用授权。 |
| Create Electro Energetics | https://github.com/george8188625/Create-Electro-Energetics ; https://github.com/cat4blep/Creaty-fly-Electro-Energetics | MIT；保留 George VI 版权，融合 MC26.2 Fabric 迁移与官方 NeoForge 功能源码。 |
| Create Storage | https://github.com/ausmez/create-storage-neo-forge | 源码文件为 GNU GPL v3；发布站另标 GPL-3.0-or-later，保留该差异及原始许可，不自行缩减授权范围。 |
| Create Additional Logistics | https://github.com/KhloeLeclair/CreateAdditionalLogistics | MIT；保留 Khloe Leclair 及引用的 Create 版权。 |
| Create FluidLogistics | https://github.com/yision1/CreateFluidLogistics | 自有代码 BSD-3-Clause；另含 CDP 的 LGPL-3.0-or-later 与 CreateFluidStuffs 的 MIT 部分，各源码头及原始许可保留。 |
| Create Dragon’s Plus | https://github.com/DragonsPlusMinecraft/CreateDragonsPlus ; https://github.com/DragonsPlusMinecraft/CreateDragonsPlus-Fly | LGPL-3.0-or-later；保留 Create/Ponder MIT 引用与对应来源。 |
| Create Enchantment Industry | https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry ; https://github.com/DragonsPlusMinecraft/CreateEnchantmentIndustry-Fly | LGPL-3.0-or-later；保留源码头、完整许可与引用素材声明。 |

## 本工程修改

原有 26.3 适配成果按工作区快照保留。本次整合将开发依赖改为仓库内真实构建产物；Connected 调整为 .22 基线，保留新旧配置枚举兼容并更换品牌图标；Diesel 增加 `SYNCED/SERVER`、`LOCAL/COMMON` 枚举兼容、实际 Sable API 依赖和受模组存在性控制的装配联动。Flywheel 隔离在打包阶段统一执行。

Registrate 通过 Create 的 `build.gradle` 中 ASM 变换适配资源标识和 RegistryEntry 接口。首选修改形式为对应源码与这些明确的构建变换，修改脚本和 MPL 许可证一并提供，精确下载的源码归档哈希见许可清单。

许可证全文位于 [LICENSES](LICENSES)。本仓库和发布源代码档案提供修改版的对应源码。后续若改变分发方式、公开服务或商业用途，应重新核验各组件条款与素材许可；私人使用许可不能推定为素材再授权。


## 源码恢复与来源角色

完整来源与许可范围另见[中文说明](docs/SOURCE-AND-LICENSES.md)和[English](docs/SOURCE-AND-LICENSES.en.md)。上表及原有许可证、版权头继续保留；本工程的作者署名不替代原作者、适配维护者与第三方引用代码的署名。

Create 26.3 恢复版以公开 MC26.2 / Adapted0.996 提交 `30ec1245a5af80228699453c8e59f3285a6be074` 为源码底座，比较固定的 26.2 / 1.31 与 26.3 / 1.31 发布包，通过方法级差分、反编译参照及实际 API 核对恢复所需实现。固定文件页和输入 SHA-256 见上述双语说明。该恢复工程包含本地重建与后续修复，不标为原作者提供的 26.3 原始源码，也不承诺与参照发布包逐字节相同。

差分分析或反编译不授予新的代码或素材许可证。原组件的 MIT、GPL/LGPL/AGPL、MPL、BSD、PolyForm、CC-BY-NC 以及单独素材条款分别保留；公开可读与不受限制的开源授权应分别说明。恢复或整合工作不产生整套代码和素材的统一重新授权。

## 素材边界与历史分发状态

上表关于排除素材的说明是本工程的分发政策。Aeronautics / Simulated / Offroad 的保留权利范围覆盖其指定 `src/main/resources/assets/` 完整目录，包括 `minecraft`、`musicnotification`、`amendments` 等其他 namespace；不能只按三个模组 namespace 判断素材来源。

2026-11-04 只读审计确认，旧过滤逻辑遗漏了这些目录中的 8 个跨 namespace 文件，其中 3 个 PNG；它们存在于旧 Git 引用及历史源码附件，3 个 PNG 还存在于旧安装器。旧源码和依赖目录另保留过一个包含 Create 保留权利美术的依赖 JAR。因此，历史实际内容尚不能用“素材已全部剥离”概括。

本次公开采用独立仓库 `Flowfangfei/create-263-modpack-public`，仅导入清洁的最新源码快照并发布 `v0.3.0-alpha.3` 附件。公共副本以独立初始提交标注真实快照来源，不导入原 Git 历史、旧素材或旧附件。原 `Flowfangfei/create-263-modpack` 保持私有，完整历史和旧附件保留，不重写历史。8 个跨 namespace 素材文件和两个旧依赖 JAR 已取消 Git 跟踪并修正忽略规则，本地文件字节保留。实际创建和交付状态见[发布记录](docs/RELEASE.md)。官方 Create 素材与 Diesel 单独授权音效继续通过固定来源及 hash 校验在本地获取，原声明和权利边界不变。
