# Transmission & Linkage 的 26.3 适配

## 来源与构建边界

- 上游：[userenxv/aeronautics-utility-objects](https://github.com/userenxv/aeronautics-utility-objects)，`main` 快照 `322c4182d0593cde79fd7a5217dbb84283d9c65f`，上游版本 `0.2.8`，Minecraft 1.21.1 / NeoForge。
- 本模块版本：`0.2.8-26.3-adapted.1`，mod id 继续使用 `aeronautics_utility_objects`。保留原方块、物品、方块实体和网络包标识。
- 目标：Minecraft `26.3`、Java `25`、NeoForge `26.3.0.22-beta`、Gradle `9.5.0`、ModDev `2.0.148`。
- 实际编译依赖为本包构建的 Create `1.31`、Ponder/Catnip `1.0.100046-adapted-fork-early+mc26.3`、Flywheel `1.0.6`、Sable `3.0.0-adapted.1`、Simulated `1.3.0-adapted.1`。未使用上游 Sable 2.x 二进制。
- `LICENSE` 是上游声明的 CC-BY-NC-4.0，版权作者 `enxv233`。上游快照另带 PolyForm Shield `LICENSE.md`；两份文件原样保留并进入 JAR 的 `META-INF/`，本次适配没有重新解释或替换其适用范围。

构建由仓库串行入口执行，依赖根目录的共享 Gradle 配置：

```powershell
$env:CREATE263_GRADLE_OFFLINE='true'
python -X utf8 -B ../../scripts/gradle_serial.py transmission-linkage-adapted-263 build
```

输出为 `build/libs/create_aeronautics_transmission_linkage-0.2.8-26.3-adapted.1.jar` 和同名 `-sources.jar`。

## 接口与功能迁移

| 范围 | 适配内容 |
| --- | --- |
| 注册与数据 | 使用 26.3 方块实体类型构造器，为所有方块和物品显式赋予注册键；保留原注册名称。添加 `assets/.../items` 物品定义，配方原料迁移到当前字符串/标签编码。 |
| 万向节与液压杆 | 保留连接、双向引用、伸缩/限长/恢复力、创造/巨型液压、应力效果与连接移除逻辑；四个 BER 使用本包 Simulated 的公开兼容渲染层和原生 `SubmitNodeCollector`。 |
| 客户端预览 | `ExtractLevelRenderStateEvent` 计算预览和命中状态，将变换与几何绘制快照存入 `LevelRenderState`；`SubmitCustomGeometryEvent` 提交该快照。命中轮廓使用同一阶段划分。 |
| 设置界面 | 使用 `GuiGraphicsExtractor`、当前输入事件与 `ClientPacketDistributor`，保留滑块、滚轮、铰链角度和服务端参数更新。 |
| 铰链 | 保留普通旋转约束与黄铜限角通用约束、独立铰链子空间和归属检查。新子空间创建后立即更新边界，以便 Sable 在当前 tick 正确处理加载票据。 |
| 动能转换 | 保留 Simulated 旋转轴承扩展、物理角速度到 Create 动能输出、阻尼、输出应力和 ExtraKinetics 状态；原方块实体存储名称继续保留。 |
| 隐藏轴承连接盘 | 将已移除的 `BlockRenderDispatcher` 注入点迁移至当前 `ModelBlockRenderer.tesselateBlock`。注入只隐藏属于阻尼转换轴承的连接盘。 |
| Ponder | 保留全部场景、动画连杆与设置叠层，迁移到 `api.client` / `impl.client` 命名空间以及当前场景几何提交签名。 |
| 持久化 | 保留 `LinkedPos`、`LinkedSubLevel` 与铰链/设置标签。位置继续写上游 `{X,Y,Z}` 结构，同时接受当前 `BlockPos.CODEC` 编码；UUID 通过当前 codec 读写。约束 handle 仍只存在于运行期。 |
| 装配移动 | 保留 `beforeMove` → 本地移除保护 → `afterMove` 双端引用重映射事务，以及按服务器 game tick 过期的玩家选择缓存。 |

当前没有排除万向节、液压杆、铰链、动能转换或 Ponder 的核心实现。

上游两个 `assets/simulated/lang/*.json` 各仅含 `simulated.extra_kinetics.information` 一个翻译键（英文 `Information`、中文 `信息`）。适配将这两个键原样合并到本模组 `assets/aeronautics_utility_objects/lang/`，保留翻译键名和上游来源，删除跨命名空间语言文件，避免覆盖前置资源或被安装器作为航空学素材剔除。

## 已完成的验证

2026-10-25 在固定 Java25 / NeoForge26.3 平台执行 `build`，构建成功。最终打包与两项上游校验证据日志为仓库根目录的 `artifacts/build/transmission-linkage-adapted-263-20261025-131531.log`。运行 JAR 的 SHA-256 为 `04CA4FA2305766D656392811C9E22D8FBA5C30F79C9731F6CAEF694EF8978552`，sourcesJar 为 `772EECFAE03E677CE904E82F8F16D1C7E374F629A0C49FAEC7CE803D95148917`。

- `compileJava`、资源处理、JAR 与 sourcesJar 生成成功。
- 复用并通过上游 `verifyHydraulicLengthControl`：逼近方向、恢复力方向、长度硬限制、冲量上限、阻尼输出、液压设置、巨型液压视觉阶段与防积分饱和。
- 复用并通过上游 `verifyCompatibilityContract`：方块实体 id、持久链接键、装配移动事务、铰链归属隔离、双端冲量验证与输出状态键；Sable 最低依赖断言更新为 3.x。
- 这些检查验证数学与源码兼容契约。原生物理和存盘证据见下面的实际联合运行记录；客户端画面另有下面的实际渲染与截图验收记录。

## 联合运行探针

默认关闭。只有显式设置 `-DtransmissionLinkage.runtimeProbe=true` 才会运行，并同时要求：

1. `-Dcreate263.probeRoot=<server绝对路径>`；
2. 该目录存在 `.create263-disposable-runtime`；
3. `FMLPaths.GAMEDIR` 等于该目录；
4. 当前服务器的世界目录等于该目录下的 `world`。

探针在同一真实 `ServerLevel` 放置夹具，不触碰其他启动器实例，不自动停服。初轮在约 340 个服务器 tick 后写 `transmission-linkage-probe.json`，日志标记 `[TransmissionLinkageProbe]`。任一步失败会保存失败原因。

初轮检查包括：

- 真实 `assembleBlocks` 装配液压头与配重，连接地面端；
- 液压长度/恢复力方向、位置与子空间 UUID 的 NBT 往返；
- 万向节实际连接及持久化；
- 等待 Sable 自动 actor tick 创建原生约束；
- 普通旋转铰链与黄铜限角铰链的实际约束创建；
- 再次真实装配移动后，检查双端引用重映射和旧 handle 失效；
- 检查移动后的约束重建，解绑后双端清空且 handle 失效；
- 留下一对重新连接的液压头供存盘重载检查。

首次测试存盘停服后，使用同一世界并添加 `-DtransmissionLinkage.probeReload=true`。约 180 ticks 后验证磁盘重载的链接、子空间 UUID 和自动约束重建，仍不自动停服。初轮内存 NBT 往返与本次磁盘重载是两项不同证据。

| 可视夹具 | 世界坐标 |
| --- | --- |
| 地面液压头 / 移动液压头世界锚点 | `(128,-60,8)` / `(132,-60,8)` |
| 黄铜万向节 | `(144,-60,8)`、`(148,-60,8)` |
| 动能转换轴承 / 调节器 | `(152,-60,8)` / `(156,-60,8)` |
| 地面黄铜铰链 / 移动普通铰链世界锚点 | `(160,-60,8)` / `(164,-60,8)` |

JSON 同时提供移动端实际 plot 坐标与子空间 UUID，客户端应在世界锚点附近观察画面。

## 实际联合服务器结果

2026-10-25，Create Ore Excavation、Liquid Fuel 与本模块在同一新建测试世界联合运行。运行目录为仓库根目录下 `runtime/three-addons-20261025-125828-018989/`，未使用原 HMCL 实例或存档。

| 运行 | 结果 | 可核对证据 |
| --- | --- | --- |
| 首轮真实专用服务器 | 340 ticks，`passed=true`；液压与万向节 NBT 往返、恢复力方向、自动原生约束创建、普通/黄铜铰链约束、二次装配引用重映射、旧约束移除、解绑与重建均通过。 | `create-server.json` 的 `reports.transmission-linkage`；`create-server.log` 2026-10-25 12:59:22 `[TransmissionLinkageProbe] PASS ticks=340 reloadRequired=true`。 |
| 同一世界存盘后重启 | 180 ticks，`diskReload=true`、`constraintCreated=true`、`passed=true`，实际磁盘中的链接和子空间引用恢复，约束自动重建。 | `reload-server.json` 的 `reports.transmission-linkage`；`reload-server.log` 2026-10-25 13:00:02 `[TransmissionLinkageProbe] PASS ticks=180 reloadRequired=false`。 |
| 数据包加载与 `/reload` | 两轮日志没有本模块配方、注册数据或资源标识解析错误；首轮执行了真实 `/reload`，随后探针通过。 | 上述两轮完整服务器日志。服务器日志不能证明客户端模型、材质与 Ponder 画面完整。 |

根测试脚本汇总结果后可将逐模组临时 JSON 合入 `create-server.json` / `reload-server.json`，应以该汇总文件与原日志共同核对。运行时目录为本地测试产物，不作为源码发布内容。

## 实际客户端与 GUI 修复记录

客户端使用上述专用服务器世界的独立副本，未打开原 HMCL 存档。`runtime/three-addons-20261025-125828-018989/client-130751-227314/joint-client-probe.json` 记录全部 12 个模组加载、静态展示块 BER 类型匹配、世界加载和保存返回标题成功。探针通过实际 `mc.gui` 打开界面并等待真实提取/渲染帧，不把场景编译成功当作 UI 渲染成功。

| 项目 | 实际证据与结果 |
| --- | --- |
| 世界与 BER | 探针相机访问液体、矿脉和连杆夹具区域；联合测试已逐图查看模型/贴图/连杆画面，并核对万向节、液压调节器和动能转换轴承的 BER 注册。 |
| 液压属性 Ponder | `aeronautics_utility_objects:hydraulic_rod_properties` 真实 PonderUI 渲染 149 帧，存在可见 `HydraulicRodElement`；截图 `client-130751-227314/screenshots/2026-10-25_13.09.04.png`。 |
| 液压设置 Ponder | `aeronautics_utility_objects:hydraulic_rod_settings` 真实 PonderUI 渲染 122 帧，可见连杆和 `HydraulicSettingsOverlayElement`；截图 `2026-10-25_13.09.09.png` 显示设置叠层完整位于窗口右上范围内，标签和数值可读。该图仍显示黑色英文说明气泡右侧被裁切，已另行调整本场景锚点，见下文。 |
| 实际液压设置菜单 | 通过服务端真实菜单打开 `HydraulicConnectionHeadScreen`，原生界面渲染 89 帧；截图 `2026-10-25_13.09.15.png` 中标题、Expected Length、Redstone Range、数值和确认按钮均已目视确认可见。 |

第一轮真实设置截图曾出现滑条与按钮可见、全部文字消失。原因已确认为 `GuiGraphicsExtractor` 接收 ARGB，而上游渲染器传入六位 RGB，alpha 为零。已在 `HydraulicConnectionHeadSettingsRenderer`、`UniversalJointScreen` 和 `DampingStressBearingScreen` 补齐不透明 alpha；颜色插值函数也明确保留 `0xFF000000`。实际液压菜单截图已验证修复。其他两个设置屏的颜色调用已源码审查，但尚未逐屏打开验收。

Ponder 自有设置叠层先前继承场景导航变换，导致面板部分越出窗口。`HydraulicSettingsOverlayElement` 现在从单位二维矩阵绘制，并按 GUI 宽高缩放到可用区域；上述 1066×600、GUI scale 2 的截图已确认面板完整。仍被裁切的说明气泡由场景 `placeNearTarget()` 的锚点位置引起；本次最终构建只把 `hydraulicRodSettings` 的专用 `settingTextTarget` 从 `(-2, 2.6, 0)` 调到 `(+4, 2.6, 0)`，保留文字、时序与箭头，没有修改共享 Ponder 实现。此最后锚点调整等待联合测试负责人用新客户端截图最终确认，不能沿用前一截图声称它已通过。

上述 GUI 颜色、叠层布局和场景锚点修改均限于客户端类。服务端物理验证使用已通过的相同服务端实现；最终包需由根流程按相同 Flywheel 变换后逐条目核对，确认新增差异仅为这些客户端类。

## 尚未覆盖的范围

世界截图覆盖固定相机观察点，未系统验收玩家交互的连接预览、命中描边以及每个设置屏、每个 Ponder 时刻或所有 GUI 尺寸。探针未测量巨型液压负载曲线、多个运动子空间间的复杂闭环、长期稳定性、多人同时操作、跨维度/卸载边界和全部 Ponder 画面。动能转换方块的放置/加载不能替代实际机械转动和应力网络验收；应另外施加物理转动，观察后端输出转速、应力容量、阻尼变化与解体恢复。

最终锚点调整已由主任务复验：`runtime/three-addons-20261025-125828-018989/client-131720-538526` 完成十二模组、两场Ponder、真实设置菜单与保存退出，退出码0。13.18.46/13.18.52截图中说明气泡在1066×600/GUI2窗口内，标题、标签、数值可读；不再存在这两处已发现的裁切/透明文字问题。
