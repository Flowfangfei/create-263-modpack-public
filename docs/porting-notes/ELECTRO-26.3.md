# Electro Energetics 26.3 迁移记录

当前18模组整体运行与交付范围见[最终验证记录](../VALIDATION.md)。下文保留各次独立编译和静态审计的具体边界。
状态：先前正式 Gradle build/check/test 已通过；随后 17 模块新建专服在客户端类加载处失败。本轮隔离和 Mixin 修正已通过独立 javac，等待统一重建及重新运行。未访问或启动原 HMCL 存档。

## 可核验来源

| 来源 | 源提交 | ZIP SHA-256 |
| --- | --- | --- |
| 最高 Minecraft 版本源码，MC 26.2 Fabric/CreateFly，1.2.0 | `1033dcb653a3dafcbd436f1da5bd109fbd8e6ac6` | `0fb63fad90591438b9450dbaba9bec22ec86f75c169d97ab980deabef36290c8` |
| 官方 NeoForge dev，MC 1.21.1，1.2.0 | `4828c3487a9fc0703717b415eb07d005c7a48653` | `6fd00fc816f77990ed87628969f7bba8859b64e92de64d84f4f911a8f718a88d` |

模块：`modules/create-electro-energetics-adapted-263/`，保留 MIT 许可。最高 MC26.2 原始代码保存在 `src/main/java/`，官方 NeoForge 完整参考树在 `upstream-neoforge-reference/`。活动构建源集为 `src/neoforge/java/`；不会同时编译 Fabric 入口。逐文件上游候选哈希及适配后哈希见模块内 `source-fusion-manifest.json`。清单的 `initial_fusion_selection` 仅记录首次选择，后续文件融合使用两个参考来源。

## 实质迁移

- 使用共享 `gradle/create263-addon.gradle`，目标 Java25、MC26.3、NeoForge26.3.0.22-beta。入口、注册、事件、网络、配置以 NeoForge 真正 API 回接，核心算法与新版方块/物品实现融合最高 MC26.2 来源。
- 使用本包实际 Create、Catnip、Ponder、Flywheel 源码/JAR 适配包拆分、`Identifier`、NBT 可选值、注册表访问、坐标/交互接口；SavedData 使用实际 codec 保存路径保留历史 NBT 布局。
- 转换器使用 NeoForge 26.3 `EnergyHandler`、事务和快照日志。外部插入/抽取直接操作同一真实电力缓冲，回滚恢复缓冲，提交同步数据；设备主动推拉也使用真实事务。
- 保留 Sable 子空间节点投影、装配搬迁与设备数据迁移；保留 Aeronautics/Simulated 面板附件、风扇推力接口，以及受电弓、第三轨、列车电气化和电车声音逻辑。依赖本包构建的 Sable/Aeronautics/Simulated 与实际 sable-companion API。
- `CEEPartialModels.Bufferer` 将实际 Flywheel `PartialModel` 的 `BlockStateModelPart` 包为原生 `SingleVariant`，通过 Catnip `SuperBufferFactory` 生成真实几何；没有替代方块/空模型。
- `CEEPartialModels.GeometryCollector` 记录真实 RenderType 与完整顶点属性，再用原生 `SubmitNodeCollector.submitCustomGeometry` 提交。20 个方块实体渲染器显式覆盖 native 提取/提交入口（清单见 `native-renderer-inventory.json`），覆盖仪表、面板、线架、受电弓、电机、刷架、灯与动态附件。世界电线使用 `ExtractLevelRenderStateEvent` 记录，再由 `SubmitCustomGeometryEvent` 提交；Ponder 和蓝图预览也接入实际提交路径。
- 手持伸缩杆/钳表使用最高版本原生 `HandToolModel` 与 `RegisterItemModelsEvent`；保留第一人称目标跟随与伸缩动画、第三人称手臂姿态和持物显示。旗帜附件通过实际 `BannerFlagModel`/`BannerRenderer.submitPatterns` 提交。
- 配置重载实际读取已加载 ModConfig 文件、校正值并发出 Reloading 事件。电表放置数据改到真实 `BlockItem.placeBlock` 入口，保留累计电量和所有者。
- 运行时静态审查将蓝图注入改到真实 `SchematicRenderer.submit`，结构填充改为当前 `List<Block>` 参数；鼠标转向改包装真实 `LocalPlayer.turn` 调用。旧 `BlockStateBase.onRemove` 注入不再启用，设备移除由活动 `LevelChunk.setBlockState` 包装真实处理。
- 资源沿用最高版本资源与已生成数据；NeoForge 元数据保留官方功能声明。

## 覆盖与限制

编译覆盖电路求解、导线/节点同步、储能、转换器、电机/风扇/加热、面板附件、交流设备、列车电气化，以及 Sable/Aeronautics/Simulated 的实际代码。代码编译通过尚不能证明这些功能在联合实例正常运行。

当前未启用 JEI、ComputerCraft、Struts、Supplementaries Bunting 外围联动；它们原始源码完整保留，活动入口和注册不再引用缺失的可选模组。配方离线生成器暂不参与构建，已生成资源和运行时进度触发器保留。未使用可选依赖伪桩。

原生渲染桥已编译，尚需测试完整顶点布局、透明/发光层顺序、资源重载、蓝图/Ponder、第一/第三人称工具及人物姿态。方块渲染提取状态当前保存真实 BE 引用和 partial tick，提交阶段仍读取 BE 动画字段；不宣称已完成纯状态快照隔离。

## 验证记录

- 快照 SHA-256：已核对，完整源码与许可保留。
- 独立真实 javac，读取正式 Gradle 导出的 classpath/source：2026-10-25 23:31（Asia/Shanghai），504 个源文件，0 errors / 31 warnings，classpath 缺失项为空。
- 日志：`modules/create-electro-energetics-adapted-263/build/port-javac/20261025-233115-520338/javac.log`。
- 正式 Gradle build/check/test：已由唯一 Gradle writer 执行成功。日志 `artifacts/build/create-electro-energetics-adapted-263-20261025-234659.log`，`BUILD SUCCESSFUL in 31s`。原有 NbtViewTest（2 项）和 RMSHolderTest（3 项）均通过，0 failures / errors / skipped；JUnit Jupiter 5.13.4 / Platform 1.13.4，测试使用真实 Minecraft/Create classpath。
- 产物：`modules/create-electro-energetics-adapted-263/build/libs/electroenergetics-1.2.0-26.3-adapted.1.jar`，另生成 sources.jar。
- 新测试实例的服务端加载、客户端加载和核心/视觉/跨模组功能：待测。


## 2026-10-26 common/client 隔离与活动 Mixin 复核

原失败日志为 `runtime/three-addons-20261026-003217-309839/create-server.log`。专服加载 `CEEBlocks` 时验证 `EnergyMeterBlock.displayScreen`，其方法体直接引用 `EnergyMeterScreen` 与客户端 `Screen`，导致 `NoClassDefFoundError`。仅将调用放入客户端执行分支不足以隔离同一 common class 中需要验证的客户端继承关系。

本轮保留全部注册与活动 Mixin，作如下修改：

- 普通/三极电表与面板电表、变压器附件的屏幕实现移入 `client.CEEClientScreens`。common 回调仅传 common 类型；仍由原有客户端执行分支打开相同屏幕。
- 客户端事件移入 `events.ClientModEvents`、`events.ClientGameEvents`，均用 `EventBusSubscriber(value = Dist.CLIENT)` 限制。common 事件订阅类不再暴露客户端事件参数或初始化世界电线几何状态；服务端节点改名保留，客户端线路交互转交客户端类。
- 12 个客户端数据包将 `LocalPlayer` 强制转换改用接口实际提供的 common `Player`，保留原有导线/节点、电压、受电弓、列车音效及粒子处理。
- 电动机、电风扇、蜂鸣器、转子、调压器、高压开关、变压器和变压器铁芯的声音实现与实例字段移入每实体惰性 `ClientAudio` holder。外层 `tickAudio` 通过真实物理客户端分支调用，保留原音量、音调、位置、平滑、存活与停止逻辑。
- 缩放滚动设置的本地玩家权限查询移入客户端实现。

真实 Create JAR 为 `Train.read/write` 与 `Carriage.read/write` 同时提供 `HolderLookup.Provider`、`RegistryOps` 包装入口以及共用内部实现。`TrainMixin` 原先只按方法名包装，会匹配三个不同描述符；现指定含 Provider 与 RegistryOps 的共用 core 描述符，并完整转发原参数。`CarriageMixin` 同样注入共用 core，保证通过实际 RegistryOps 入口读写时仍保留电气状态和受电弓。`CarriageContraption.capture` 的泛型返回类型也对齐实际 Apache Commons Pair。

复核范围是 JSON 配置的全部 20 个活动 Mixin。基于本轮编译 classfile 与实际 Gradle 导出 classpath，43 项 callback、Shadow、Accessor、WrapMethod/WrapOperation/Redirect 描述符或目标检查无未匹配项；5 个 INVOKE 目标均见于实际 `javap -p -s -c` 字节码。证据在模块 `build/mixin-bytecode-audit-20261026/compiled-signatures.json`、`invocation-targets.json` 及各 `.javap.txt`。这些静态检查未执行 Mixin transformer，不能代替客户端和专服实际加载。

最终独立编译：`2026-10-26T14:07:00+08:00`，`scripts/javac_probe.py create-electro-energetics-adapted-263`，真实 Java25、508 个活动源文件、0 errors、31 warnings、无缺失 classpath。日志 `modules/create-electro-energetics-adapted-263/build/port-javac/20261026-140700-977697/javac.log`，机器结果 `build/last-port-javac.json`。本子任务未运行 Gradle、游戏或世界操作；统一重建、联合专服/客户端加载及先前列出的功能和视觉场景仍需实际验收。


### 18 模块首轮后的节点 codec 类验证链修复

`runtime/three-addons-20261026-141918-630021/create-server.log` 进一步定位到 `CEEPackets -> SendWireConnectionsPacket -> DirectionalInWorldNodeConnection.STREAM_CODEC -> InWorldNode`。`InWorldNode` 中的客户端拾取方法仍直接读取 `Minecraft.player`，专服在验证共享节点类时因此尝试解析 `LocalPlayer`。

已将完整拾取算法和全部客户端节点渲染缓存读取移入 `client.CEEClientNodes`。common `InWorldNode` 仅通过 `Level`、`BlockHitResult`、`Vec3` 和节点自身类型转交，保留原 codec、最近节点/脱离节点比较、Sable 投影、插值、缓存缺失回退，以及缓存存在但位置为空时的原有返回行为。客户端拾取继续由原有线路交互入口调用。

最终 `scripts/javac_probe.py create-electro-energetics-adapted-263` 于 `2026-10-26T14:22:17+08:00` 验证 509 个源文件，0 errors / 31 warnings，缺失 classpath 为零。日志为 `modules/create-electro-energetics-adapted-263/build/port-javac/20261026-142217-371698/javac.log`。对应 `InWorldNode.class` 不含 `net/minecraft/client/`、`client/WireRenderer` 或 `client/ClientNodeData` 常量引用。仍需根端定向重建并实际启动验收，本子任务未运行 Gradle、游戏或世界。


### PartialModel 初始化与附件渲染隔离

`runtime/three-addons-20261026-143042-814388/create-server.log` 显示 common 入口 `CreateElectroEnergetics.<init>` 无条件调用 `CEEPartialModels.register()`，触发整个模型类静态初始化，继而在真实 Flywheel `PartialModel` 构造器中访问专服不存在的 `ModelDebugName`。

已将这次全量模型初始化移入物理客户端 `CEEClient` 入口。共同注册的 `WireType` / `Builder` 改为保存真实 `Supplier<PartialModel>`，全部普通、染色电线和绝缘子模型、端点模型只在客户端渲染获取时解析；现有直接传 `PartialModel` 的 Builder 构造器和 `endPointItem` 重载仍保留。注册 ID、电气参数、颜色索引、实际模型对象与资源路径均保持原样。

共同注册的 Banner/Damper 附件及链条的原生渲染实现同时完整移入 `client.CEEWireAttachmentRenderers`，避免这些共同类型的方法体继续链接原生旗帜 Model 继承关系。common 附件保留原宽高、掉落物、材料需求和渲染回调，客户端回调转交原几何实现。

最终独立 javac 于 `2026-10-26T14:35:02+08:00` 验证 510 个活动源文件，0 errors / 31 warnings、无缺失 classpath；日志 `modules/create-electro-energetics-adapted-263/build/port-javac/20261026-143502-141976/javac.log`。编译后的 common 入口、BannerAttachmentType、DamperAttachmentType 均不含 `net/minecraft/client/` 或 `CEEPartialModels` 常量引用。真实客户端模型烘焙、附件/电线视觉和新专服加载仍由统一重建后实际验证。本轮未运行 Gradle、游戏或世界。


## 2026-10-26 客户端选择高亮迁移

首次联合客户端日志 `runtime/three-addons-20261026-224159-491995/client.log` 和 `client/crash-reports/crash-2026-10-26_22.46.17-client.txt` 报告已移除的 `RenderHighlightEvent$Block`。该事件只在编译兼容包中存在，实际 NeoForge 26.3.0.22-beta universal JAR 中不存在。

本轮将 `ClientGameEvents` 的活动订阅迁到真实 `ExtractBlockOutlineRenderStateEvent`。蓄电池仍按命中位置、朝向与 ROLL 属性选择对应半段；单节仍使用普通轮廓。电气面板仍仅在正面、内框范围和已存在附件槽命中时选中旋转后的附件 AABB，其他位置回退普通方块轮廓。两个辅助方法返回提取出的 `VoxelShape`，不再引用旧事件或即时缓冲源。

实际 MC `LevelExtractor.extractBlockOutline` 在事件前清空 `blockOutlineRenderState`，事件取消后直接返回。因此处理器写入真实 `BlockOutlineRenderState` 并取消默认提取，保留选中的自定义形状；随后实际 `LevelRenderer.submitBlockOutline` 使用 `SubmitNodeCollector.submitShapeOutline` 提交，沿用原生高对比模式、线宽、相机偏移及透明层顺序。没有停用订阅或提交空几何。线缆交互点继续只取消底下的普通方块框，实际线缆与节点轮廓由原有 `WireInteractionHandler` / `WireApplyingBehaviour` / `OutlinesOnWireRenderer` 路径保留。

API 证据取自该模块实际 `minecraft-patched-26.3.0.22-beta-sources.jar` 的 `LevelExtractor.java`、`LevelRenderer.java`，以及实际 NeoForge universal 和 Minecraft patched classfile；提取片段、`javap` 签名、输入 JAR 哈希记录于模块 `build/client-outline-api-audit-20261026/`。新增实现未使用编译兼容包提供的运行 API。活动 NeoForge 客户端 import 另经真实 universal 条目核对，本轮未发现其他缺失的客户端事件类型。

独立 `scripts/javac_probe.py create-electro-energetics-adapted-263` 于 `2026-10-26T22:54:12+08:00` 通过：510 个活动源文件、0 errors / 31 warnings、无缺失 classpath，日志 `modules/create-electro-energetics-adapted-263/build/port-javac/20261026-225412-702971/javac.log`。三个修改后的 classfile 均无 `RenderHighlightEvent` 常量引用。尚未由本子任务运行 Gradle、游戏或世界；根端需要重建并验证客户端加载、蓄电池多节/各朝向/ROLL、面板附件边缘/正反面、线缆/节点选择以及高对比与透明场景。
