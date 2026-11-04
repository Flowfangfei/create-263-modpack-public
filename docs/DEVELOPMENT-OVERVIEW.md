# Create263 的开发路线与实现

[English](DEVELOPMENT-OVERVIEW.en.md)

本文面向继续维护这些移植模块的开发者，说明 Create263 从公开源码恢复底座、迁移铁路与物理链，到形成十八个直接运行 JAR 的技术路线。内容按依赖关系和实现问题组织；安装方法见 [README](../README.md)，一般迁移方法见[模组升级与测试](MOD-UPGRADE-AND-TESTING.md)。

当前组合固定为 Minecraft Java **26.3**、NeoForge **26.3.0.22-beta**、Java **25**。源码来自多个独立上游，保留各自身份、mod ID 和许可。精确提交、参考发行包及授权范围统一见[来源与许可](SOURCE-AND-LICENSES.md)和[来源清单](../manifests/sources.json)。

## 1. 从可维护的 Create 源码建立底座

### 公开基线与发布包各自提供什么

Create 底座以公开的 **26.2 / Adapted 0.996** 源码提交 `30ec1245a5af80228699453c8e59f3285a6be074` 为起点。目标参照为 **26.3 / 1.31** 发布包，同时比较 **26.2 / 1.31** 包以缩小 Minecraft 版本相关差异。公开源码负责提供可维护的工程、命名和未改变实现，发行二进制提供需要恢复的接口与行为参照。[输入身份与 SHA-256](SOURCE-AND-LICENSES.md)

恢复过程没有先构造一套完整的 26.2 / 1.31 中间源码。它在公开底座上合并目标所需差异，并继续修复整合过程中发现的问题。这份实现属于本工程维护的恢复分支；原作者未将它作为官方 26.3 原始源码提供，也未声称它与参照包逐字节一致。[源码身份说明](SOURCE-AND-LICENSES.md)

### 方法级比较如何缩小修改范围

恢复时把 JAR 内容分为 Java 类、内嵌依赖、资源和元数据，分别处理。类比较进一步区分以下层次：

| 比较层次 | 恢复时关注的内容 |
| --- | --- |
| 原始 class 字节 | 找出可以直接复用的类与需要进一步检查的类 |
| 类和成员声明 | 父类、接口、字段与方法描述符、可见性、泛型及常量 |
| 方法指令 | 常量池引用按实际值展开；检查字面量、调用、分支和异常处理范围 |
| 包名与符号差异 | 区分兼容包迁移与实际 API 契约改变 |
| 依赖和资源 | 核对内嵌 JAR 的真实内容，再处理资源路径、数据 schema 与加载入口 |

分支和异常表用指令位置比较，减少常量池排列、字节偏移和编译器调试信息带来的噪声；`invokedynamic` 还需展开引导方法和参数。反编译结果用于理解和合并局部逻辑，原源码仍是维护主体。指令表示相同只覆盖该比较范围，外部依赖实现、运行时验证与生命周期另行核验。[恢复方法说明](SOURCE-AND-LICENSES.md)

公开树保存恢复后的源码、构建变换和固定来源清单；当年的完整反编译缓存与逐方法比较输出不随源码分发。下文链接当前实现和后续验证记录，避免把历史单文件探测当作当前整包验收。

### 一个真实行为修改：物品替换的事务边界

[ResourceHandlerItemAdapter](../modules/create-adapted-263/src/main/java/com/simibubi/create/api/contraption/storage/item/simple/ResourceHandlerItemAdapter.java)保留旧 `IItemHandler` 入口，内部调用 NeoForge 原生资源事务。恢复的两个行为直接影响数量安全：

- `extractItem` 将请求限制在资源的实际最大堆叠数量以内；模拟提取关闭未提交事务。
- `setStackInSlot` 在同一事务中完整取出旧内容，再完整插入新内容；任一步只完成一部分时返回，由事务恢复原状态。

该文件在恢复过程中曾单独编译并与目标方法结构核对。随后才推进完整构建、专用服务器、客户端和保存重开；这些阶段的证据分别成立。现在的整体编译与运行范围见[初始联合验证](VALIDATION-v0.1.0-alpha.1.md)，不能用单文件比较替代它。

## 2. 对齐 NeoForge、依赖 ABI 与资源入口

### 内部兼容代码连接旧调用和新事务

26.3 的资源接口变化涉及大量旧物品、流体调用。Create 在自己的 `compat/neoforge263` 下保留兼容接口，连接到原生 `ResourceHandler`、`ItemResource`、`FluidResource` 和 `Transaction`。

[物品桥](../modules/create-adapted-263/src/main/java/com/simibubi/create/compat/neoforge263/items/ItemResourceHandlerAdapter.java)保留“插入返回剩余物品”的约定；[流体桥](../modules/create-adapted-263/src/main/java/com/simibubi/create/compat/neoforge263/fluids/capability/FluidResourceHandlerAdapter.java)将旧的模拟/执行选择映射为是否提交事务。过滤、容量、组件身份和实际接收量仍属于各自接口的契约。

这些类含源自旧 NeoForge 的兼容代码，保留相应版权与 LGPL 声明，属于 Create 内部 API。后续附属可以使用本包实际提供的桥，也可以直接实现原生资源接口；代码来源及命名空间说明见[兼容代码声明](../LICENSES/CREATE-NeoForge-compat-NOTICE.txt)。

### Registrate 的版本名不足以确定接口

Create 的构建需要处理 Registrate 的旧 `ResourceLocation` 与目标 `Identifier` 描述符。当前 [prepareRegistrateCompileJar](../modules/create-adapted-263/build.gradle)从依赖输入创建专用副本，执行注册项修补和类型映射，检查关键方法签名，并比较原输入变换前后的哈希。编译类路径只替换为这个声明了任务依赖的输出。

这个任务解决编译接口准备。构建文件还有独立的发行包变换，审计时应分别读取各任务。源码与构建处理来源见 [Registrate 目录](../vendor/registrate-MC1.21-1.3.0+67/)及[第三方许可清单](../manifests/third-party-licenses.json)。

燃料注册还涉及真实运行 ABI：旧 `burnTime` 路径引用目标加载器已移除的数据映射字段。当前 [AllItems](../modules/create-adapted-263/src/main/java/com/simibubi/create/AllItems.java)与 [AllBlocks](../modules/create-adapted-263/src/main/java/com/simibubi/create/AllBlocks.java)在物品属性中设置 `DataComponents.COOKING_FUEL`，保存原来的燃烧时间。接口可编译和类实际初始化，是这类依赖迁移的两个检查点。

### 外部素材也有可执行的 API

Create 的官方美术通过外置资源包加载。[ExternalCreateAssets](../modules/create-adapted-263/src/main/java/com/simibubi/create/infrastructure/assets/ExternalCreateAssets.java)实现目标版本的 `openMetadata/openResources`，处理旧物品模型与 copycat blockstate 兼容资源，并依据实际 Flywheel 后端选择可视化路径。

运行用官方素材与构建源码分别固定来源和哈希。安装器在本地恢复素材，具体路径与分发范围见 [EXTERNAL_ASSETS](../modules/create-adapted-263/EXTERNAL_ASSETS.md)和[资源锁](../manifests/resources-lock.json)。

## 3. 开发依赖与运行命名空间

### 附属编译针对本包的实际 Create

同为“Create 26.3”的 fork 可以提供不同的包名、注册器、库存和渲染接口。本包使用 `com.simibubi.create`；铁路及航空学上游包含 `com.zurrtum.create` 的 Create Fly 接口，迁移需要恢复具体功能调用。

[依赖暂存脚本](../modules/steam-n-rails-263/tools/stage-create-adapted-deps.py)从本仓库 Create 构建结果获取 Create、Ponder、Catnip、Flywheel、Registrate 和访问变换配置，记录文件哈希。单独的编译兼容 JAR 只补充构建所需旧类型，附属发行 JAR 禁止夹带这些外部类。后来的附属使用[共享平台配置](../gradle/create263-addon.gradle)接入同一组开发接口。

### Flywheel 在打包阶段统一隔离

源码保留 `dev.engine_room.flywheel` 开发引用。发行变换使用 [IsolateFlywheel.java](../modules/create-adapted-263/scripts/IsolateFlywheel.java)，将运行 Java 包、mod ID、资源 namespace、Mixin/refmap 名和 JarJar 信息按对应结构改为隔离名称。附属引用经过相同变换。

| 需要变换 | 需要保留 |
| --- | --- |
| Java 包 `dev/engine_room/flywheel` | Create 自身 `content/kinetics/flywheel` |
| 运行 mod ID 与资源 namespace `create_flywheel` | Create 方块 ID `create:flywheel` |
| Flywheel 的 Mixin、refmap、内嵌 JAR 路径 | shader 内部子目录 `flywheel/` |
| 确认属于该库的路径与字符串引用 | 无关字符串、内容模型 ID 和已有隔离前缀 |

因此运行包通过结构化重定位生成，不能对所有 `flywheel` 文本做全局替换。[打包器](../scripts/package.py)检查未隔离引用、跨模组 Java 包冲突、外部类误打包、加载器元数据和空依赖范围，并记录输入与输出哈希。客户端着色器和绘制仍由运行验证覆盖。

## 4. Steam ’n’ Rails：接回另一套 Create 实现

铁路以已有 NeoForge 26.3 社区源码为基线，继续适配本包 Create。工作包括注册、轨道材料、行为对象、流体、库存、持久化、模型和提交路径；本模组的桥接代码位于 [internal/compat](../modules/steam-n-rails-263/common/src/main/java/com/railwayteam/railways/internal/compat/)。

### 跨注册表工厂的求值时机

继承的铁路注册工厂会即时访问多个注册表。直接放在 ITEM 事件时，Create 的部分方块实体 supplier 尚未绑定。当前 [RailwaysNeoForge](../modules/steam-n-rails-263/neoforge/src/main/java/com/railwayteam/railways/neoforge/RailwaysNeoForge.java)依据 `GameData.getRegistrationOrder()`，在最后一个仍可写的 `RegisterEvent` 执行这些工厂；[初始化协调器](../modules/steam-n-rails-263/neoforge/src/main/java/com/railwayteam/railways/neoforge/RailwaysNeoForgeInit.java)处理事件总线绑定和排队回调。

这一选择依赖当前加载器的注册顺序和冻结规则。将来把即时工厂迁为完整 DeferredRegister 时，应重新划分注册阶段；改变加载器版本也需复查这里。

### 数据和渲染分别迁移

[资源处理任务](../modules/steam-n-rails-263/build.gradle.kts)处理 Create 配方字段、流体输入输出、颜料组件、条件加载、进度和标签，并保留对生成结果的检查。旧 Create Fly 的液滴单位与目标 mB 约定需在边界换算；颜料壶的[原生容器实现](../modules/steam-n-rails-263/fabric/src/main/java/com/railwayteam/railways/content/palettes/painting/fabric/PaintPitcherFluidStorage.java)只允许完整色阶转移。

渲染桥的 [CompleteVertexConsumer](../modules/steam-n-rails-263/common/src/main/java/com/railwayteam/railways/internal/compat/client/render/CompleteVertexConsumer.java)为旧 Catnip 四边形流补齐法线、overlay、额外 UV 和线宽。连接贴图还需要保留正确 atlas 源，构建时的资源生成和最终 JAR 都属于检查范围。

铁路的[世界 GameTest](../modules/steam-n-rails-263/neoforge/src/main/java/com/railwayteam/railways/neoforge/RailwaysGameTests.java)涵盖真实轨道路网、燃料罐以及批量方块/实体加载。后续整包记录保留铁路正常资源构建和联合加载结果。[初始联合验证](VALIDATION-v0.1.0-alpha.1.md)仍将整套列车、调度、车钩和多人车掌交互列为专项范围。

## 5. Sable → Simulated → Offroad / Aeronautics

物理链的上游目标为 Fabric 26.3。本包将 Sable 作为独立 NeoForge 前置迁移，Simulated 提供物理机械及共用平台层，Offroad 与 Aeronautics 使用它们。运行代码通过 NeoForge 原生入口、注册、事件、网络和事务接入；保留的 Fabric 源码或参考依赖按源集规则排除。[Sable 构建](../modules/sable-adapted-263/build.gradle)、[航空学共用构建](../modules/create-aeronautics-adapted-263/gradle/neoforge-module.gradle)

### Sable 自有区块格式与原生事件

Sable plot 保存的是子空间自己的区块格式。[SablePlotPlatformImpl](../modules/sable-adapted-263/src/neoforge/java/dev/ryanhcode/sable/neoforge/platform/SablePlotPlatformImpl.java)先恢复辅助光照和附件，在区块及方块实体恢复完成后，用 `SerializableChunkData.copyOf` 创建真实区块快照，交给类型化的 `ChunkDataEvent.Load`。直接把 plot NBT 当作原版 region NBT 解析，会缺少原版格式所需字段。

[SableRuntimeProbe](../modules/sable-adapted-263/src/neoforge/java/dev/ryanhcode/sable/neoforge/SableRuntimeProbe.java)提供重力、地形碰撞、质量、有限速度以及保存后结构身份和方块实体恢复断言。探针源码展示验收条件；已经执行过的联合推进与重载结果见[初始联合验证](VALIDATION-v0.1.0-alpha.1.md)。

### 原生事务要覆盖业务状态

Simulated 的[后端事务表](../modules/create-aeronautics-adapted-263/simulated/src/neoforge/java/dev/simulated_team/simulated/multiloader/transfer/BackendTransactions.java)让同一实际库存、罐或电池的不同包装器共享 SnapshotJournal。回滚恢复同一份后端状态，业务通知在根事务提交后执行。

这样既处理数量，也处理库存派生索引、连接端与业务回调。[传输检查](../modules/create-aeronautics-adapted-263/simulated/src/neoforge/java/dev/simulated_team/simulated/neoforge/TransferRuntimeChecks.java)覆盖组件、容量、共享后端、嵌套事务和对接；[航空探针](../modules/create-aeronautics-adapted-263/aeronautics/src/neoforge/java/dev/eriksonn/aeronautics/neoforge/AeroRuntimeProbe.java)另检查模拟取弹后炮台装填与充能状态保留。

### 移动 plot 的渲染所有权

Flywheel 的“跳过原生 BER”条件必须与实际拥有的 Visual 一致。迁移期间，移动 plot 的部分方块尚无完整实例化渲染，提前跳过 BER 会使螺旋桨等几何消失。

当前 [SmartBlockEntityRenderer](../modules/create-aeronautics-adapted-263/simulated/src/main/java/dev/simulated_team/simulated/compat/create/SmartBlockEntityRenderer.java)在提取阶段把 plot 身份存入 RenderState，提交时恢复对应作用域并在 `finally` 中还原。普通世界保留 Flywheel；有本包 Smart renderer 的移动 plot 使用实际 BER 几何。[客户端工厂注册](../modules/create-aeronautics-adapted-263/simulated/src/main/java/dev/simulated_team/simulated/SimulatedClient.java)连接这一分流。

[SableCreateRenderContext](../modules/create-aeronautics-adapted-263/simulated/src/main/java/dev/simulated_team/simulated/compat/create/SableCreateRenderContext.java)还对显式子空间提取限定作用域，避免状态跨对象或渲染调用泄漏。高级可视化、所有优化模组路径及全部载具模型仍需分别验证。

### 玩法数据与 datagen 分开维护

运行构建保留已生成的模型、配方、标签和其他功能资源。旧自动数据生成接口及回调在[运行与 datagen 说明](../modules/create-aeronautics-adapted-263/docs/porting/RUNTIME-AND-DATAGEN.md)中单独记录，新增内容需要同步维护 JSON。当前源集中未启用的旧生成器和参考 Mixin，不能当作已经运行的实现。

联合测试实际使用 Create 胶水、物理组装器、256 RPM 电机和螺旋桨，检查组装、爬升、同身份保存重开及推进恢复；另有燃烧器供气、升力输出与关闭后的目标归零检查。热气球完整载具运动、复杂地面驾驶及多人同步仍有独立边界。[已执行场景](VALIDATION-v0.1.0-alpha.1.md)

## 6. 从单模块到同一模组组合

基础模块分别工作后，组合仍暴露了不同层次的冲突。[九模组联合记录](VALIDATION-v0.1.0-alpha.1.md)保存以下修复和验证范围：

| 组合问题 | 实现 |
| --- | --- |
| Connected 主模组与 Simulated 桥共享包，触发 split-package | 桥接包迁至 `com.hlysine.create_connected_simulated`，同步更新反射与注册调用 |
| 可选桥接的依赖范围为空 | 使用实际 Simulated 版本范围，并由打包检查拒绝空声明 |
| Sable 重写广播方法，原车掌坐标注入失效 | [条件化包装 position 调用](../modules/steam-n-rails-263/fabric/src/main/java/com/railwayteam/railways/fabric_mixin/conductor_possession/PlayerListSableMixin.java)，保留子空间距离计算和车掌相机位置 |
| 新附属需要真实物理接口 | 编译使用同一 Create/Sable/Simulated 输出，运行保留独立 mod ID 和元数据 |

[构建入口](../scripts/build.py)按依赖顺序构建；[跨进程锁](../scripts/gradle_lock.py)限制 Gradle 写入者。编译输入、运行隔离包与剥离受限素材后的载荷分别记录哈希。[运行清单](../manifests/runtime-modules.json)描述十八个直接 JAR；Ponder、Catnip、Flywheel、Registrate 按 Create 的构建与内嵌依赖体系提供。

## 7. 扩展加工、物流、仓储与电力

后续模块沿用上述底座，迁移重点逐步转向资源事务、持久化、网络和业务生命周期。[三附属说明](THREE-ADDONS-26.3.md)与[六模块说明](FIVE-ADDONS-26.3.md)保留功能范围、来源和更详细的技术入口。

| 模块组 | 关键实现与效果 |
| --- | --- |
| Liquid Fuel | [容器与燃烧室](../modules/create-liquid-fuel-adapted-263/src/main/java/com/forsteri/createliquidfuel/core/BurnerStomachHandler.java)在同一事务抽取和插入，容量不足回滚，返还带组件的实际余物 |
| Ore Excavation | [端口记录](../modules/create-ore-excavation-adapted-263/docs/PORTING-26.3.md)覆盖原生注册、FIFO 事务、持久化、配方网络及真实钻井/泵输送 |
| Transmission & Linkage | [端口记录](../modules/transmission-linkage-adapted-263/docs/PORTING-26.3.md)说明 Sable 约束、装配前后端点映射；运行 handle 与持久化 UUID/坐标分离 |
| Electro、Dragons Plus、Enchantment Industry | 以完整 NeoForge 功能树融合固定高版本 Fly 源码中的 MC 迁移点；原生事务、加工回调和双端网络分别接回 |
| Additional Logistics、FluidLogistics | [物流记录](porting-notes/STORAGE-LOGISTICS-26.3.md)与[流体记录](porting-notes/FLUID-26.3.md)处理包裹、组件身份、共享容量、事务及机器路径 |

### Storage 的大数量只存在于自己的库存

[StorageContents](../modules/create-storage-adapted-263/src/main/java/net/fxnt/fxntstorage/inventory/StorageContents.java)以数量为一的完整物品模板保存身份，另用独立数量字段表达模组库存。所有输入先完成验证，再统一替换状态；[转移适配](../modules/create-storage-adapted-263/src/main/java/net/fxnt/fxntstorage/util/StorageItemTransfer.java)按实际物理所有者共享事务日志。

原版 ItemStack/ItemStackTemplate、箱子、玩家槽及光标继续使用原生规则。模组菜单通过[专用同步器](../modules/create-storage-adapted-263/src/main/java/net/fxnt/fxntstorage/inventory/StorageMenuSynchronizer.java)处理私有数量，同时保留原生点击状态校验。实际客户端完成 `1024 → 私有槽 960 + 光标 64 → 私有槽 1024`，并验证服务端和客户端数量、组件一致。[验证记录](VALIDATION.md)

旧版 Storage 自动存档迁移尚未验证。当前格式、事务与网络通过的结果适用于受测来源和组合，未知旧格式需独立识别和迁移。

## 8. 验证随功能链推进

工程使用静态检查、完整构建、专用服务器、客户端、真实业务夹具和独立 JVM 重启，分别核对不同问题。最终 JAR 和测试输入通过清单哈希对应；重压缩造成容器哈希变化时，还需比较条目内容。[验证总表](VALIDATION.md)、[机器可读摘要](../manifests/validation-summary.json)

| 阶段 | 已有记录回答的问题 | 仍需单独判断 |
| --- | --- | --- |
| 恢复与基础整合 | 源码、依赖、元数据能否构建和联合加载，航空装配/事务能否保存重开 | 全部铁路、载具、配置和可选联动 |
| 三附属 | 实际泵送、钻井、约束和事务回滚能否运行并重载 | 复杂生产线和大规模约束 |
| 六模块与深入玩法 | 库存保存/点击、加工产物、能量负载及物流时序 | 多人、长期负载、复杂移动子空间 |
| 后续客户端修复 | 实际 GUI、管线附件、图集重载和 Ponder 模板/动画 | 全部画面逐帧人工检查 |

[深入玩法验证](GAMEPLAY-VALIDATION-26.3.md)通过真实负载发现 Electro 的服务器启动/停止钩子未接入。修复在 [MinecraftServerMixin](../modules/create-electro-energetics-adapted-263/src/neoforge/java/com/george_vi/electroenergetics/mixins/MinecraftServerMixin.java)恢复模拟器生命周期，再以断电负对照、重连、独立重启和正常停服检查执行效果。线网存在与电机实际带载由不同断言覆盖。

后续 Ponder 修复补充了虚拟世界数据、渲染管线和资源生命周期的客户端验收。旧模板转换限定具体方块实体与字段；管线声明按实际颜色目标修复；绳索 sprite 在首次图集拼接前注册，并检查资源重载。对应代码、采样方法与历史覆盖见 [Ponder 审计](PONDER-AUDIT-20261104.md)，日常回归按相同实际逻辑取代表。

继续开发时，可从[通用升级方法](MOD-UPGRADE-AND-TESTING.md)选择所需检查，再沿本页链接找到当前源码和具体报告。复杂移动物理、完整列车运营、多人、长期运行、未安装的可选联动，以及旧世界自动迁移，均应保留各自的测试输入与结论。
