# Fluid Logistics 26.3 移植记录

> 本页保留源码融合、接口迁移和端侧排障阶段的技术记录。“当前”“等待”“待测”按相应段落当时的输入理解；后续运行结果见[联合验证](../VALIDATION.md)和[深入玩法](../GAMEPLAY-VALIDATION-26.3.md)，各项未覆盖范围仍分别保留。

## Native helpers review（2026-10-26）

对 `foundation/capability/**`、`SmartMultiFluidTank` 和 `FluidLogisticsNbt` 进行了只读事务审查。核对实际 NeoForge 26.3.0.22-beta 源码中 `ItemAccess.exchange` 与 `SnapshotJournal` 的嵌套事务规则：exchange 在子事务内抽出原物品并插入结果，插入不足时回滚，调用方未提交时也恢复；Fluid item helper 只修改私有物品副本，成功 exchange 后才提交其嵌套事务。

`SmartMultiFluidTank` 使用物理 owner 的唯一 journal，快照复制全部 `FluidStack` 与组件；多方块及侧面 wrapper 委托该 owner。Infinite tank 和 Snow Golem Freezer 的 `SingleTankResourceHandler` 每个实体各创建一个实例；Smart Hopper 和 Fluid Transporter 各缓存一个 native tank，过滤 wrapper 不创建第二份 journal。NBT helper 使用 registry-aware ItemStack/FluidStack codec 与 ValueIO，保留组件，没有另造精简流体标签。

本次只读审查未发现新增的可确认丢物缺陷。根端此前修复了同种流体占两个包槽时旧 drain 抽到首槽的问题：当前 helper 明确按所请求 index 调 `drainOneBucket`。`FiveAddonsProbe` 已加入第二槽抽取/回滚/组件、共享容量/嵌套回滚、真实铜桶 ItemAccess，以及满单槽、两只铜桶、`forHandlerIndexStrict` 交换失败时 accepted=0、总数和完整 ItemResource 均不变的实际回归项。结果必须由新的项目 runtime 实测确认，静态审查与 javac 通过不计作玩法通过。

通用活动 Mixin 的字节码审计保存在模块 `build/port-mixin-audit/`：25 个 Mixin、55 个方法选择器、真实目标 JAR 与源码 SHA。修复项包括 `IRON_CHAIN`、`ItemStack.is(Object)`、wrench lambda `$0` 及 `chainDestroyed` 的真实 `IRON_NUGGET` 字段注入点。客户端目标由客户端负责人单独复核，最终仍需真实 Mixin 应用和画面验收。


## 旧 BEWLR 入口替换为原生物品模型（2026-10-26）

`runtime/three-addons-20261026-145210-279201/create-server.log` 显示 `FluidLogistics.<clinit> -> AllBlocks.<clinit>:289` 加载无限流体罐物品时解析了旧 `CustomRenderedItemModelRenderer` 基类，该继承链仍需要实际 MC26.3 中不存在的 `BlockEntityWithoutLevelRenderer`。同类旧入口也残留在含水铜 casing 物品中。此前虽已有原生 `FluidContainerItemModel`，其 package/infinite 分支调用的静态 helper 本身仍继承旧 renderer，因此仅回避专服构造不能修复客户端。

现将物品渲染统一到已经注册的真实 `ItemModel` / `SpecialModelRenderer` 路径：

- `InfiniteFluidTankItem` 与 `WaterContainingCopperCasingItem` 移除已由物品模型 JSON 和 CODEC 取代的旧 `initializeClient` / `SimpleCustomRenderer` 入口；common 物品类不再引用原生客户端类型或旧 renderer。
- `FluidPackageItemRenderer` 和 `InfiniteFluidTankItemRenderer` 改为普通 final 静态几何 helper，移除旧基类及重复 frame renderer。原包裹液面、气体上浮、容量高度、无限罐 fluid 数据和第一人称 buffer 实现全部保留，由原生模型的 frame 层和 special fluid 层调用。
- `CompressedTankItemRenderer` 同样改为静态 helper，原生模型的压缩罐分支复用其显示场景判断及 fluid icon 绘制，保留 GUI/GROUND/FIXED/HEAD 行为与 FIXED 的 0.375 尺寸，其余场景继续显示真实 frame。
- 移除 `FluidProxyClient` 的废弃 BEWLR import。9 个 `fluidlogistics:fluid_container` 物品 JSON、原模型路径及 `RegisterItemModelsEvent` 中的 CODEC 注册保持不变；未停用物品或流体渲染。

最终独立真实 Java25 javac：`2026-10-26T15:38:14+08:00`，460 个活动源文件，exit 0，输出 100 warnings（javac 默认警告上限），缺失 classpath 为空。日志 `modules/create-fluid-logistics-adapted-263/build/port-javac/20261026-153814-292368/javac.log`，机器结果 `build/last-port-javac.json`。

额外扫描本轮 670 个全新编译 class：`BlockEntityWithoutLevelRenderer`、`CustomRenderedItemModelRenderer`、`CustomRenderedItemModel`、`PartialItemModelRenderer`、`SimpleCustomRenderer` 引用均为零。实际 MC26.3 patched JAR 存在原生 `ItemModel` 和 `SpecialModelRenderer`，不存在 BEWLR。独立证据为本地审计目录的 `fluid-native-item-render-audit.json`，同时记录 9 个 JSON 的 SHA-256。

本轮未运行 Gradle、游戏或世界操作。根端仍须定向重建，检查旧 `WaterContainingCopperCasingItem$Renderer.class` 未残留于最终 JAR，然后验证专服加载、客户端模型烘焙、九种物品的 frame/流体、气体、视角和压缩罐 icon，以及包裹实体/传送带/飞轮显示。


## 注册链的公共与客户端隔离（2026-10-26）

在旧渲染基类清理后，`runtime/three-addons-20261026-154219-458952/create-server.log` 的下一处真实错误为 `AllItems.<clinit>:49 -> FluidSchematicItem` 加载时缺失 `net.minecraft.client.gui.screens.Screen`。`FluidSchematicItem.displayBlueprintScreen` 的方法体直接构造 `FluidSchematicEditScreen`，即使调用处已有 `level.isClientSide()`、方法已有 `@OnlyIn(CLIENT)`，公共物品类验证仍会解析该客户端继承链。

新增 `client/FluidLogisticsClientAccess` 承接真实客户端实现，公共入口只使用公共参数和返回值：

- 蓝图界面继续从 shift + 主手、具有蓝图文件组件的交互入口打开；保留 `use` / `useOn` 的结果与原有客户端分支。
- `ResourceFactoryPanelBehaviour` 的界面实现移入 helper，保留普通面板委托 `FactoryPanelClient`，资源仪表按 `LocalPlayer` 创建专用界面。
- `SnowGolemFreezerMovementBehaviour` 的镜头实体查询移入 helper，保留原有朝向、车厢导体与头部追踪算法。
- 粒子 provider 注册移入 helper；公共 `MechanicalFluidGunStreamParticleData` 保留真实 `ParticleOptions`、`ICustomParticleData`、完整流体 CODEC / STREAM_CODEC，`getFactory` 仅委托惰性 `ClientFactory`，客户端粒子子类的构造保留在独立嵌套类。冷冻器雪花及流体枪粒子的实际 provider 均未停用。
- 复核后续流体注册发现 `PowderSnowFluidType.createModel` 同样在公共类中构造原生 `FluidModel.Unbaked` 和客户端 `FluidTintSource`。改为公共类型仅提供两个纹理 Identifier，在原有客户端 `RegisterFluidModelsEvent` 中调用 helper 构造同样的模型；纹理、无 overlay、两种染色返回 -1 及 source/flowing 注册均保留。

顶层 `FluidLogistics`、`AllItems`、`AllBlocks`、`AllMenuTypes` 的新编译字节码没有直接原生客户端类指令或原生客户端方法描述符。菜单保持 Registrate 标准双层 Supplier，实际 Screen constructor factory 仍延迟到客户端注册；tooltip factory 保持原功能，没有删注册或以空 renderer 换取专服加载。`ICustomParticleData.getFactory` 的 `ParticleProvider` 返回描述符仍是实际 Create API 必需的客户端方法，继续标记 `@OnlyIn(CLIENT)`，方法体不再构造客户端子类。

最终独立 Java25 javac：`20261026-154944-816311`，461 个源文件，exit 0，100 warnings（javac 默认上限），缺失 classpath `[]`。日志为 `modules/create-fluid-logistics-adapted-263/build/port-javac/20261026-154944-816311/javac.log`。对本次 672 个新编译 class 复核，五种旧渲染符号仍全部零引用；上述四个公共实现类没有 `net/minecraft/client/` 类常量、指令或方法描述符。字节码和 class SHA-256 证据在本地审计产物的 `fluid-common-client-boundary-audit-154944.json`。

本轮只进行源码修复、真实 classpath javac 与静态字节码检查，Java 已冻结供统一重建。仍须真实专服通过完整 FML 加载，以及客户端验证蓝图编辑、普通/资源仪表界面、冷冻器追踪与雪花、流体枪流体粒子及粉雪流体模型；静态通过不等于这些运行行为已经验收。


## 包裹资源注册与默认组件生命周期（2026-10-26）

`runtime/three-addons-20261026-163525-711899/create-server.log` 已通过 RegisterEvent，随后在 CommonSetup 队列的 `PackageResources.bootstrap -> PackageResourceRegistry.bootstrap:103 -> new ItemStack -> Holder.Reference.components` 抛出 `Components not bound yet`。该阶段物品 holder 已注册，但仍不能读取其默认组件；将工作放入 `FMLCommonSetupEvent.enqueueWork` 并不提供这个保证。

本轮只修改 `PackageResourceRegistry`，保留资源类型和注册入口，将元数据冻结与需要组件的操作分开：

- `bootstrap` 继续校验 type ID、display、carrier/selector 的非空和非 AIR 身份、重复映射，并冻结三种身份映射。selector 的 resourceKey Supplier 作为尚未求值的工厂保存；该阶段不创建 `ItemStack`、不检查默认堆叠组件、不执行 key 规范化。
- 首次实际 stack API 使用时，仍逐 carrier 以真实默认 stack 校验 `maxStackSize == 1`，并执行 selector supplier、验证其类型及 carrier、规范化 key。全部成功才发布不可变的 key 缓存；失败保留异常，不发布半完成缓存，并清理进行中标记。volatile 完成标记和同步初始化避免并发重复发布，并拒绝递归初始化。
- `get(id)` 保持纯类型元数据查询，供紧邻的 `FactoryGauges.bootstrap` 查询资源类型；不间接触发 stack 校验。`findType`、`resolveRequestKey` 的 null/EMPTY 查询也不触发默认组件访问。

复核相邻 CommonSetup：FactoryGauges 只验证 Item 身份、namespace、gauge type 及资源类型元数据；packet 注册创建 payload type/codec 元数据，mounted-storage 注册使用 storage type/codec 工厂，CDP freezer 和 unpacking 注册按 Block 绑定 handler。这些注册实现未发现相邻的立即 stack 创建。本次 672 个编译 class 的 `<clinit>` 中，直接 `ItemStack` 构造、`asStack` 或 `getDefaultInstance` 调用为零；`PackageResourceRegistry.bootstrap` 的字节码也已没有直接 ItemStack、默认 stack 或 normalizeKey 调用。

最终独立 Java25 javac：`20261026-164103-686115`，461 个活动源文件，exit 0，100 warnings（默认警告上限），缺失 classpath `[]`。日志 `modules/create-fluid-logistics-adapted-263/build/port-javac/20261026-164103-686115/javac.log`；静态指令及 SHA-256 证据为本地审计产物的 `fluid-component-lifecycle-audit-164103.json`。

Java 已冻结供统一重建。本轮未运行 Gradle、游戏或世界操作；仍须实测 18 模块启动通过 CommonSetup，以及绑定完成后的首次 PackageResources 查询/包裹操作与 selector key 创建、规范化和原有校验。


## 严格物品槽交换探针的实际 API 复核（2026-10-26）

18 模组实际进入世界后，`runtime/three-addons-20261026-171505-534816/server/five-addons-probe-result.json` 的 `fluid/ItemAccess-exchange-failure-full-strict-slot-preserves-items` 得到 accepted=100，原探针预期 0。本轮只读核对实际启动参数指向的 NeoForge universal JAR 与 MC server patched JAR，发现测试夹具不构成预期的失败交换：铜桶注册 `stacksTo(1)`，探针却将已含 777mB 的资源 `toStack(2)` 直接交给 SimpleContainer 构造器。

真实 `ItemAccess.forHandlerIndexStrict` 返回 `HandlerItemAccess(RangedResourceHandler.ofSingleIndex(...), 0)`；该版本没有名为 `StrictResourceSlotAccess` 的内部类。`ItemAccess.exchange` 自带子事务，先抽旧资源、再插新资源，只在两者数量相等时提交。原生 `ItemStackResourceHandler.createSnapshot` 会先 `setStack(original.copy())`；VanillaContainerWrapper 的 setter 调用 `SimpleContainer.setItem(index, stack, true)`，后者仍会执行 `stack.limitSize(getMaxStackSize(stack))`，boolean 只抑制 setChanged。因此该非法 count=2 栈在快照阶段先被夹紧成 1，随后 extract(1) 清空槽，新组件资源能够插入，静态调用链与 observed accepted=100 一致。这不能作为一次真实失败交换的证明。

当前 Fluid adapter 先修改独立的单物品副本，再在调用者的子事务内 exchange；只有成功交换才返回已转移流体量，失败则不提交。没有为迎合原夹具修改生产 Fluid 源码，也没有增加影响专用大库存数量语义的通用最大堆叠限制。根端已接受改用合法单铜桶和过滤 ResourceHandler：过滤器只接受完整原 ItemResource，明确拒绝修改流体组件后的资源，以触发真正的插入失败；仍断言 insert/extract 返回 0、item count=1、完整 ItemResource 相等、777mB 和流体组件守恒。探针由根端修改和运行，本子任务未改 probe。

实际 JAR/class SHA-256、原生交换与快照链、测试条件和尚待实测的范围见本地审计产物的 `fluid-strict-exchange-fixture-analysis-172147.json`。本轮 Fluid Java 无改动，无新增 Fluid 编译或游戏执行。


## NeoForge 26.3 GLM 自动发现与优先级（2026-10-26）

`runtime/three-addons-20261026-172344-433388/create-server.log:314` 显示旧 `data/neoforge/loot_modifiers/global_loot_modifiers.json` 被作为个体 modifier 解码，因为内容只有 `entries` / `replace` 而缺少 `type`。实际 NeoForge 26.3.0.22-beta 的 `LootModifierManager` 已直接扫描 `loot_modifiers` 目录，使用 `IGlobalLootModifier.DIRECT_CODEC` 解码所有个体，再按各 modifier 的 `priority()` 降序排列，没有单独读取旧索引的代码；旧 IGlobalLootModifier Javadoc 仍提到旧索引，运行源码与实际 class 是本次判据。

旧索引从活动资源目录移出，其完整 before 和 SHA-256 已归档。唯一条目 `fluidlogistics:factory_gauge_drops` 仍通过原 `data/fluidlogistics/loot_modifiers/factory_gauge_drops.json` 自动发现，原 serializer 注册也保留。个体 JSON 增加 `priority: 1000`，自定义 CODEC 改为读取 optional priority 并以框架 `DEFAULT_PRIORITY` 为默认；priority 方法返回解码值，原 public 默认构造和 INSTANCE 仍保留。掉落 `apply` 方法正文逐字对照完全一致，仍按每个活动面板的真实 gauge type 还原物品并保留爆炸处理。

运行时可从原生公共 API 确认真实加载，而不只检查日志：

```java
LootModifierManager manager = level.getServer().getServerResources().managers()
    .getListener(NeoForgeReloadListeners.LOOT_MODIFIERS_KEY);
IGlobalLootModifier modifier = manager.getModifier(
    Identifier.parse("fluidlogistics:factory_gauge_drops"));
```

应确认 modifier 非 null、`manager.getId(modifier)` 等于该 ID、`getSortedModifiers()` 仅含该实例一次、`priority() == 1000`。这一访问链与真实 `CommonHooks.modifyLoot` 一致。最小掉落夹具可使用正常初始化的 `create:factory_gauge`：全部面板 inactive 后，仅启用一个 `ResourceFactoryPanelBehaviour` 并设 gaugeTypeId 为 `fluidlogistics:fluid`，调用已核对的 `Block.getDrops(state, level, pos, be)` 四参公共 API，应得到一个 `fluidlogistics:fluid_factory_gauge` 而没有普通 gauge；无需爆炸或真实挖方块。

独立 Java25 javac `20261026-173105-886131`：461 源文件，exit 0，100 warnings，classpath 无缺失。旧索引到新自动发现 ID 的双射、before/after、SHA-256、实际框架 class/source hash、正文一致性和待实测项见本地审计产物的 `fluid-additional-resource-metadata-migration-20261026.json`，原始 before 另存 `fluid-additional-resource-metadata-before-20261026.json`。Java/资源已冻结；未运行 Gradle、游戏或世界操作，真实 manager 和掉落断言由后续联合测试确认。


## 2026-10-26 客户端眼镜提示与仪表滚轮注入

真实日志 `runtime/three-addons-20261026-224159-491995/client-225939-868586.log:609` 在 `RegisterGuiLayersEvent` 初始化期间报告 `GoggleOverlayRendererMixin.fluidlogistics$wrapInstanceOfCheck` 的隐式局部捕获失败。实际运行包 Create 的 `renderOverlay(GuiGraphicsExtractor, DeltaTracker)` 在 offset101 读取 `Minecraft.hitResult`，offset104 存入编译临时 slot5，offset108/116 进行 BlockHitResult 检查与转换；这个临时值没有可供原 `@Local LocalRef<HitResult>` 捕获的局部记录。

`GoggleOverlayRendererMixin` 现在使用 `ModifyExpressionValue` 直接转换该字段读取。只有实现 `IHaveGoggleInformation` 的有效 EntityHitResult 才产生原有的合成 BlockHitResult，同时通过原有 Share 保存真实实体；后续 `IHaveGoggleInformation` 检查与 cast 继续转交实体。三处注入均约束到实际 renderOverlay 描述符，未放宽 defaultRequire，也未删除 Mixin。普通方块、储罐、资源仪表及移动结构原分支继续由 Create 处理。字节码另确认 offset195 的 getBlockEntity 结果即使为 null，也会直接进入 offset220 的接口检查；这段之间没有 return 或 null 分支，实体提示处理器可达。

相邻 `FactoryPanelScreenMixin.mouseScrolled` 的实际两个 Mth.clamp 调用数据来源不同：offset170 使用 slot13 的 BigItemStack input（作用域 139..178）；offset242 直接读取 outputConfig，没有该局部变量。原单一 WrapOperation 会在第二处再捕获不存在的局部。现按 ordinal0/1 拆成两个严格注入：输入使用明确命名 input，输出使用实际 outputConfig Shadow，两者共用原资源调整算法，保留资源步进、修饰键、批量上限和普通物品原始 clamp 回退。

已用实际客户端运行包对全部 22 个活动客户端 Mixin 进行调用选择器复核，共 29 项，无缺失。对隐式/命名局部的相关链条另外检查 LVT 范围：链条 connection/Map.Entry、交互主手物品、过滤行为、铜蛙港玩家及仪表滚轮；argsOnly 与实际方法描述符相符。FactoryGaugeHostModel 源码的 Flywheel 命名空间保持开发形式：实际 packaged Fluid class 的注解字符串已由打包器改为 create_flywheel，与同侧 Create 方法 owner 一致，未误改正常源码。

模块 `tooling/verify_native_client_mixins.py` 现支持 `--create-jar` 和 `--isolated-flywheel`，可按实际运行包及打包后的命名空间检查，排除 compile-compat，并明确采用 UTF-8。命令为 `python modules/create-fluid-logistics-adapted-263/tooling/verify_native_client_mixins.py --create-jar runtime/three-addons-20261026-224159-491995/client/mods/create-adapted-recovered.jar --isolated-flywheel`。证据和实际 JAR 哈希保存在模块 `build/client-goggle-api-audit-20261026/`，完整选择器报告与 javap 在 `build/port-mixin-audit/`。静态匹配不等于 Mixin transformer 已实际执行。

最终独立 Java25 javac `20261026-230558-314458`：461 活动源文件、exit0、100 warnings（默认上限）、classpath 缺失项为零，日志 `modules/create-fluid-logistics-adapted-263/build/port-javac/20261026-230558-314458/javac.log`。本轮未修改 build.gradle、许可、其他模块、probe 或启动脚本，未运行 Gradle、游戏或世界。Java 已冻结供统一重建；仍需真实客户端验收初始化、方块/实体/移动结构眼镜提示及资源仪表输入/输出滚轮。
