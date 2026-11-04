# Enchantment Industry / Dragon’s Plus 26.3 迁移记录

当前18模组整体运行与交付范围见[最终验证记录](../VALIDATION.md)。下文保留各次独立编译和静态审计的具体边界。
更新时间：2026-10-26T17:34:11+08:00。目标平台：Java 25、Minecraft 26.3、NeoForge 26.3.0.22-beta；使用整合包已经实际构建的 Create、Catnip、Ponder、Flywheel、Sable 与 Simulated API。

## 源码基线及融合方法

| 模组 | 最高 MC 源码快照 | 较新功能源码快照 | 本次处理 |
| --- | --- | --- | --- |
| Dragon’s Plus | Fly `27fd0e647e2e`，MC 26.1.2 Fabric，1.11.8 | NeoForge `56f7a4a9f2fc`，MC 1.21.1，1.11.9 | 保留完整 1.11.9 功能树及公开包路径；参照最高 MC 源码迁移原生资源、序列化、渲染及配方语义 |
| Enchantment Industry | Fly `2dcc94608cf7`，MC 26.1.2 Fabric，2.5.2-c | NeoForge `7cb2e9f59d83`，MC 1.21.1，2.5.4b | 保留完整 2.5.4b 功能树，融合 Fly 原生机器物品模型与现代资源定义，迁移附魔、打印、经验、磨石等运行路径 |

Fly 快照依赖 `com.zurrtum.create.*` 的 Create-Fly。当前整合包使用 `com.simibubi.create.*` 的 NeoForge Create，因此无法通过更换加载器入口直接链接。活动运行源以新版 NeoForge 功能树为结构基础，逐项移植高 MC 实现并按 26.3 真实签名调整。此项包含游戏 API、事务传输、渲染和资源格式的实际迁移。

两个模块的 `upstream-reference/fly-26.1.2/` 与 `upstream-reference/neoforge-*` 保留完整原始树，便于审计和继续比较。现有版权头、`LICENSE.txt`、Create/Ponder 第三方许可证及 `LICENSING.txt` 保留；原始快照不是虚构的历史提交。

源 ZIP SHA-256：

| 文件 | SHA-256 |
| --- | --- |
| `enchantment-industry-fly-2dcc94608cf7.zip` | `da9bbdf1f938b3ffed262fb2e5db1d777a2fa7361e09d3d26e541d14fcac3bae` |
| `dragons-plus-fly-27fd0e647e2e.zip` | `ffdf5f954708d78fa638063263c9f30eb5461db243046c1b1f8a2f4afdc0e63b` |
| `enchantment-industry-neoforge-7cb2e9f59d83.zip` | `a1c2bb2d9e088401ef065fbab8c363b000d1ae33b9e505d7f13ba912a618a05c` |
| `dragons-plus-neoforge-56f7a4a9f2fc.zip` | `79f13a9f1af44d8bec06e46ce0c487900e96cc0a7588483d43889355adb5da55` |

## 保留和迁移的功能

- CDP 1.11.9 的 Ponder 分组、合并、轮换和重载行为保留；两个上游行为测试类迁至 26.3 真实 API，继续执行。
- CDP 动态氧化/涂蜡抛光等运行时配方继续生成；使用原生 `RecipeMap` 替换、注册表序列化与分批服务端配方快照，使客户端机器预览可以查询实际同步配方。
- CDP 的流体、药水、龙息、染色、经验相关运行接口保留；流体模型改为 NeoForge 原生 `RegisterFluidModelsEvent`。染色实体和炼药锅交互采用当前实体类型及 dispatcher。
- CDP `FluidTankBehaviour.getResourceCapability()` 提供现代 `ResourceHandler<FluidResource>`，保留多槽、流体类别约束、事务快照回滚与提交通知；原 `getCapability()` 继续供既有公开调用方使用。
- CEI 2.5.4b 的经验流体容器燃料与容器返还、附魔/超附魔、诅咒处理、锻造模式、磨石、打印各种模板及经验灯笼保持在活动源集中。
- CEI 的打印机、磨石、附魔工作物品、经典附魔书和烈焰人外观迁至原生提交式方块实体渲染；磨石保留 Create 本身的动力渲染状态。
- CEI `BlazeDeviceItemModel` 直接移植最高 MC Fly 的原生物品模型实现，并适配 26.3 `ItemQuads`、姿态栈及 BookModel 签名。它完整替代三个旧自定义物品渲染器；旧类保留源文件，不再进入运行源集。
- 两模组融合最高 MC Fly 的 `assets/*/items/*.json` 现代物品定义，同时保留较新 NeoForge 模型和纹理。
- 两模组 Sable 源集均启用；CDP 易碎储罐、易碎物品及 Simulated 行为保留；CEI 子世界坐标、闪电充能、装配期间经验掉落约束保留。

## 源集和依赖边界

活动源为 `src/main/java` 与 `src/integration/sable/java`。Sable/Simulated 从本整合包源码构建 JAR 编译；CEI 依赖本模块 CDP 实际 JAR。Conditional Mixin 0.6.4 作为 jar-in-jar 依赖打包。

当前联合运行包未安装 JEI。两模块的完整 JEI 集成源保留但不编译；CEI 的纯 JEI accessor 同样不启用。装配配方本身继续运行，JEI 专用子分类仅在显式调用时尝试加载对应可选类。要启用 JEI，仍需针对当前 Create/JEI API 完成该可选源集迁移。

上游静态 datagen 辅助类使用已移除的旧 `RecipeProvider`、配方构建和 ExistingFileHelper API，已保留源文件并明确排除；完整生成资源继续入包。**运行时动态配方提供器仍在活动源集并已原生实现。** 精确排除列表见各模块 `build.gradle`，未删除核心机器、配方或可选集成原始源码。

公开依赖调用方可继续使用原 NeoForge 包路径。新增共享入口是 `CDPRecipeAccess.of(level)` 与 `FluidTankBehaviour.getResourceCapability()`；CEI 机器新增 `getNativeFluidHandler(side)`，磨石排液器新增 `getNativeItemHandler(side)`。旧兼容 item/fluid 接口使用当前 Create 的 `com.simibubi.create.compat.neoforge263.*`。

## 验证证据与边界

- CDP：215 个活动源文件的真实 Java 25 javac 已通过；正式 Gradle `build/check/test` 已通过，根端日志 `artifacts/build/create-dragons-plus-adapted-263-20261025-232542.log`。
- CEI：160 个活动源文件的真实 Java 25 javac 已通过（两条旧兼容接口弃用警告）；日志 `modules/create-enchantment-industry-adapted-263/build/port-javac/20261025-232926-871507/javac.log`。
- CDP 实际产物：`create-dragons-plus-26.3-1.11.9-26.3-adapted.1.jar`。CEI 实际产物：`create-enchantment-industry-26.3-2.5.4b-26.3-adapted.1.jar`。两者后续资源与 Mixin 修正还需根端统一重构。
- 活动 Mixin 配置已用 JSON 解析并升至 JAVA25；对字节码已确认变化的五处调用描述符与 Sable 中心坐标静态调用完成修正。javac 通过不代表 Mixin 应用、客户端视觉、配方重载或五模组联合功能已经通过。
- 后续联合测试使用整合包内新建测试运行目录及新世界。没有访问、启动或迁移原 HMCL 存档。

最终静态收尾 javac：CDP `20261025-233326-818799`（215 源），CEI `20261025-233331-624227`（160 源），均零编译错误。经验容器破坏掉落继续使用原生 `server.schedule(TickTask)` 排队，保留上游快照捕获后的延迟处理语义。
## 2026-10-26 Mixin 启动前复核

对活动 `main`、Sable 和客户端 Mixin 配置逐项读取真实 class，检查目标方法、注入指令、callback 参数、静态属性、Shadow、accessor/invoker、wrapper、redirect 及局部变量范围。类来源和各 class 的 SHA-256 保存在模块 `build/runtime-mixin-audit.json` 中。

| 模块 | 配置中的 Mixin 类 | 可解析 handler/accessor/invoker | Shadow | 本轮静态结果 |
| --- | ---: | ---: | ---: | --- |
| CDP | 28 | 39 | 18 | 目标方法、指令描述符、完整 callback 参数及适用局部变量均匹配 |
| CEI | 15 | 19 | 5 | 当前适用的 14 个 Mixin 目标均匹配；另一个依赖未启用的 Apothic 集成 |

CDP `ConcretePowderBlockMixin.updateShape` 使用 26.3 完整参数顺序：`BlockState, LevelReader, ScheduledTickAccess, BlockPos, Direction, BlockPos, BlockState, RandomSource`。`onLand` 和 `getStateForPlacement` 的 callback 与实际目标签名同时复核。风扇、末端流体管、药水混合、运动结构、Ponder 界面及 Sable 行为的注入点、成员和局部变量也纳入检查。

CEI Sable 的经验灯笼包装器改用 `Vec3.atCenterOf(Vec3i)` 的实际调用描述符，首参保持 `Vec3i` 并直接传给原操作；移除了 26.3 已不存在的 `BlockPos(Vec3i)` 构造。`pullExp` 中用于吸引力距离和方向的两处中心坐标调用均被覆盖，继续进行子世界坐标投影。可选的 `CaptureEntityBehaviourMixin` 使用相同签名；其 Apothic 条件目标当前不编译、不进入联合运行包，源文件保留，启用该集成后仍需完成其目标类的运行验证。

四份活动 Mixin 配置继续保持 `required: true`、`defaultRequire: 1`；本轮修复未降低注入匹配要求，也未移除适用功能。

本轮 Java 25 独立编译：CDP 215 源、CEI 160 源均零错误，日志分别为 `modules/create-dragons-plus-adapted-263/build/port-javac/20261026-135644-457774/javac.log` 和 `modules/create-enchantment-industry-adapted-263/build/port-javac/20261026-140129-738177/javac.log`。编译仍有旧兼容 item/fluid 接口的弃用警告。CEI 编译使用重新导出的真实源集，排除了已由原生物品模型替代的三个旧 ItemRenderer 和未启用的 JEI accessor。

`runtime/three-addons-20261026-003217-309839/create-server.log` 的启动在其他模组初始化阶段失败。以上结果证明源码编译和所列字节码目标的静态一致性；两个模块的后续正式 JAR 重构、Mixin 实际应用及客户端/服务端联合功能仍由根端在新建测试运行目录中验证。
## 2026-10-26 服务端流体类加载修复

`runtime/three-addons-20261026-141918-630021/create-server.log:568` 记录了 `CDPFluids.dye` 在构造流体类型时提前链接客户端 `FluidTintSource`、进而缺少 `BlockTintSource` 的失败。模型、客户端扩展和雾渲染现已移入 `client/fluids/SolidFluidRendering`，由现有 `Dist.CLIENT` 的 `CDPClient` 在模型和扩展事件中注册。`SolidRenderFluidType` 直接继承原生 `FluidType`，公共加载链只保留纹理标识、颜色、雾参数和玩法属性；CEI 的 `ExperienceFluidType` 继续继承同一公共类型。原有静止/流动纹理、ARGB 模型颜色、方块颜色及雾颜色/距离计算保留。

CDP 216 个活动源的独立 Java 25 编译通过，日志为 `modules/create-dragons-plus-adapted-263/build/port-javac/20261026-142424-246302/javac-plus-client-helper.log`。此处以此前 schema 1 的真实导出依赖和 215 源列表补入唯一新增客户端源进行诊断编译，正式 Gradle 构建仍由根端执行。

独立 classloader 禁止读取任何 Minecraft、NeoForge 或 CDP 客户端类：失败运行包中的旧 CDP/CEI 四个公共流体类型均因客户端链接被拒；修复后的 `SolidRenderFluidType`、`DyeFluidType`、`DragonBreathFluidType` 以及既有 CEI `ExperienceFluidType` 的类定义、成员和构造器反射均通过，客户端类加载请求为零。前后日志及诊断程序位于 `modules/create-dragons-plus-adapted-263/build/client-linkage-probe/`。该检查覆盖公共类链接，实际服务端启动及客户端渲染仍需后续运行验证。
## 2026-10-26 静态标签 datagen 与运行注册分离

`runtime/three-addons-20261026-143042-814388/create-server.log:571` 的后续启动失败来自 `CDPRegistrate.registerFluidTags`：构造 `registry::generate` 方法引用时提前解析了旧 `RegistrateTagsProvider.IntrinsicImpl`，其父类型 `IntrinsicHolderTagsProvider` 已不在 26.3 运行环境中。

`registerTags`、`registerEnchantmentTags`、`registerBlockTags`、`registerItemTags`、`registerFluidTags` 和 `registerEntityTags` 现在仅排队原有标签数据对象。首次收到实际 `GatherDataEvent` 后，独立 `data/internal/CDPTagDataProviders` 才登记原有标签及语言生成回调。运行使用的 TagKey、已生成资源、动态运行配方和两模组功能注册保持原有路径。旧静态 datagen 提供器的 26.3 生成执行仍待单独迁移和验证。

CDP 217 源独立 javac 通过：`modules/create-dragons-plus-adapted-263/build/port-javac/20261026-143540-datagen-isolation/javac.log`。真实失败运行 JAR 与新 class 的字节码比较确认，六个运行注册入口均已移除 datagen 方法引用和 ProviderType 静态字段读取，仅 `onData` 调用提供器桥接；结果及前后反汇编见 `modules/create-dragons-plus-adapted-263/build/datagen-isolation-probe/`。后续服务端启动仍由根端重构后验证。

## 2026-10-26 磨石容量配置默认值

`runtime/three-addons-20261026-144319-969507/create-server.log` 中，CEI 构造因 `mechanicalGrindstoneFluidCapacity` 的默认值 1000 被自身校验器拒绝而失败。真实 Catnip `ConfigBase.i(default, min, ...)` 的第二个参数为最小值，上游两份快照中的 `i(1000, 5000, ...)` 因而定义了无效默认值。

`CEIFluidsConfig` 改为 `i(1000, 1000, ...)`，保留实际默认容量 1000 mB；磨石排液器仍将该整数直接传给 `SmartFluidTankBehaviour.single`。真实 `ModConfigSpec` 检查覆盖该配置类 21 个默认值：旧运行 JAR 有 1 个无效，新 class 为 0 个；999 仍拒绝，1000 和 5000 均接受。检查源及日志见 `modules/create-enchantment-industry-adapted-263/build/fluid-config-validation/`。独立 javac 日志为 `modules/create-enchantment-industry-adapted-263/build/port-javac/20261026-144918-707777/javac.log`。

## 2026-10-26 进度图标与组件绑定时机

`runtime/three-addons-20261026-155318-862248/create-server.log:305` 的注册阶段失败来自 `CEICommon.register → CEIAdvancements.<clinit> → CDPAdvancement.Builder.icon → new ItemStack`。26.3 的物品 holder 在该阶段尚未绑定组件，构造 ItemStack 抛出 `Components not bound yet`。

`CDPAdvancement.Builder` 现保存图标工厂，在 `save(consumer, registries)` 生成进度数据时才创建显示所需的 ItemStack。普通物品、Registrate 物品/方块条目、既有 ItemStack 和需要注册表查询的图标接口均保留；`whenIconCollected` 和 `whenItemCollected` 直接使用原生 ItemLike 条件，不再为取得物品类型构造临时栈。CEI 的默认包裹图标也改为延迟调用 `PackageStyles.getDefaultBox()`，避免在进入 Builder 前就创建栈。没有以空物品替代图标。

CEI 仍在 `TRIGGER_TYPES` 的 RegisterEvent 初始化进度定义并登记 builtin trigger，没有将触发器注册移到 registry 冻结之后。32 个进度 ID、父子结构、显示类型、图标选择及收集/统计/内置触发器定义保留。与真实失败运行 JAR 的字节码比较确认，全部进度公开字段一致；图标和收集条件入口均无 ItemStack 分配或 `asStack` 调用，分配仅位于保存阶段调用的图标 lambda；原生 `InventoryChangeTrigger.TriggerInstance.hasItems(ItemLike...)` 使用物品 holder，不读取组件。报告及新旧反汇编见 `modules/create-dragons-plus-adapted-263/build/advancement-icon-lifecycle-audit/`，检查没有使用 compile-compat 类型作为运行 API 证据。

本轮独立 Java 25 javac：CDP 217 源通过，日志 `modules/create-dragons-plus-adapted-263/build/port-javac/20261026-163139-105316/javac.log`；CEI 160 源通过，日志 `modules/create-enchantment-industry-adapted-263/build/port-javac/20261026-163138-958438/javac.log`。仍有旧兼容接口弃用警告。正式重构、实际注册和启动、datagen 输出等同性、游戏内进度授予及图标显示继续由根端验证；以上静态证据不代表这些运行行为已通过。

## 2026-10-26 动态配方结果与组件绑定时机

`runtime/three-addons-20261026-164309-933853/create-server.log:331` 显示后续启动已进入 AddPackFindersEvent；动态数据包生成抛光配方时，Create 的 `ProcessingRecipeBuilder.output(ItemLike)` 先创建临时 ItemStack，再转为 ProcessingOutput，因默认物品组件尚未绑定而失败。

`CDPRuntimeRecipeProvider` 的普通抛光、去氧化及去蜡三条生成路径现直接使用真实 Create 提供的 `ProcessingOutput(item, 1, 1.0F)`。此结果对象只保存物品、数量、组件 patch 和概率，主编码器也直接序列化这些字段；实际 ItemStack 仍在游戏内取用结果时创建。三类配方继续采用原来的输入、ID、配置开关、标签排除及原生 Create recipe/codec，动态数据包仍在 AddPackFindersEvent 注册，进度定义未改动。

独立 Java 25 javac 覆盖 CDP 217 源并通过：`modules/create-dragons-plus-adapted-263/build/port-javac/20261026-164620-968259/javac.log`。真实失败运行 JAR 与新 class 比较确认三处输出均由 ItemLike 重载转为 ProcessingOutput 重载；结果构造器、Builder 对应重载及主编码器均无 ItemStack 创建或组件读取。实际 Create class 来源、SHA-256、反汇编和复现脚本见 `modules/create-dragons-plus-adapted-263/build/runtime-recipe-component-audit/`，其中不使用 compile-compat 作为运行证据。正式启动、生成 JSON、配方重载、标签排除及三类抛光玩法仍待根端运行验证。

## 2026-10-26 进度配方引用与实际资源 ID

`runtime/three-addons-20261026-170912-489243/create-server.log:316` 报告 8 个未绑定配方 ID。实际运行 JAR 内对应配方均位于 `recipe/crafting/`，而配方解锁进度的 criterion 和 reward 仍引用没有 `crafting/` 的短 ID，例如 `create_dragons_plus:fluid_hatch`。

现将 CDP 4 张、CEI 10 张 `advancement/recipes` 资源中的 28 处引用精确映射至同名的 `namespace:crafting/<id>`。覆盖当前报错的 8 项，以及受配置或可选模组条件保护的 6 个相同问题。所有 14 个目标配方在失败运行包中实际存在，类型均为原生有序/无序合成、产出仍为原物品 ID；每组 recipe 与 advancement 的 `neoforge:conditions` 顺序及内容完全一致。未改物品 ID、配方资源、进度 ID、其他收集 criterion、requirements 或加载条件，也未删减进度。

反向应用这 28 处映射可完整还原原始 advancement AST。扫描两模块全部 50 张进度，以真实运行 JAR 的 167 个配方为索引，当前 `recipe_unlocked` 及配方奖励引用未解析数为 0。完整映射、JAR/JSON SHA-256、前后快照和复现脚本见 `modules/create-dragons-plus-adapted-263/build/advancement-recipe-id-audit/`。本轮仅修改 JSON，无需 Java 编译；正式资源重构、RegistryDataLoader 加载及实际解锁奖励行为仍待根端验证。

源代码追溯表明，保留的旧静态合成 datagen 先按短 ID 构建进度，再由 `BaseRecipeBuilder.accept` 为配方加目录前缀。这些旧 datagen 类目前明确排除且没有运行；其原生 26.3 迁移与后续重新生成仍在既有 datagen 验证边界内。

## 2026-10-26 附魔能力组件的正值约束

`runtime/three-addons-20261026-171505-534816/create-server.log:535` 中，正常钻石剑、附魔等级 30 的 EnchantingBehaviour 探针在 `getAdjustedLevel` 抛出 `Enchantment value must be positive, but was 0`。这是实际端口缺陷：Java 会先求值 `getOrDefault(ENCHANTABLE, new Enchantable(0))` 的默认参数，再调用组件查询；即使物品已有该组件也会失败。真实 26.3 `Enchantable` 构造器及 codec 都要求正整数。

`CEIEnchantmentHelper` 改为 `stack.get(ENCHANTABLE)` 后按可空结果读取整数值，缺省整数仍为 0，与最高 MC Fly 参考源的读取方式一致。其后的 `1 + value / 4` 加成、15% 等级取整与 clamp 均保持；所有附魔选择、兼容、多级扩展和费用方法未改动。没有构造无效组件或把缺失值替换为正数。

CEI 160 源独立 Java 25 javac 通过，日志 `modules/create-enchantment-industry-adapted-263/build/port-javac/20261026-171834-774189/javac.log`。失败运行 JAR 与新 helper 的字节码比较确认非法构造已移除，其他 helper 方法的解析后指令引用一致；实际 Minecraft class、SHA-256、反汇编及复现脚本见 `modules/create-enchantment-industry-adapted-263/build/enchantable-component-audit/`。后续正式重构和实际附魔探针重跑由根端执行；机器经验扣除、特殊/诅咒及多级行为仍待相应完整运行验证。

## 2026-10-26 经验燃料返还容器的数据描述

`runtime/three-addons-20261026-172344-433388/create-server.log:315` 的经验燃料 data map 在默认组件绑定前使用 `ItemStack.CODEC` 解析返还桶，报 `Item minecraft:bucket does not have components yet`。`ExperienceFuel.usingConvertTo` 现采用最高 MC Fly 的 `Optional<ItemStackTemplate>` 与原生模板 codec；保留接受 ItemStack 的工厂重载，并新增直接接受模板的重载。排空结果为空时不构建非空模板。`BlazeExperienceBlock` 在实际消费燃料、返还容器时调用 `template.create()`，datagen 的桶也直接以模板描述。

经验燃料 JSON 未作修改，11 项经验数值、special 标记、返还桶及全部可选模组条件与失败运行 JAR 字节相同；经验桶仍为 1000、返还普通桶。原生模板构造仅保存 holder/count/patch，不查询 holder 默认组件或构造 ItemStack。CEI 160 源独立 javac 通过：`modules/create-enchantment-industry-adapted-263/build/port-javac/20261026-173013-074961/javac.log`。实际 class、SHA-256、原始数据及反汇编见 `modules/create-enchantment-industry-adapted-263/build/experience-fuel-template-audit/`。重构后的实际 data map 加载和 1000 经验加实际返还桶仍待根端运行验证。

同日志的 `neoforge:furnace_fuels` 警告来自其他模块。实际运行包中仅 `create-adapted-recovered.jar` 与 `aeronautics-adapted.jar` 包含该旧资源，分别为 Create 的 10 项燃烧时长及 Aeronautics 的 `#aeronautics:envelope` 100 tick；CDP/CEI 没有该文件，本次未越界修改。真实 NeoForge 26.3 的 `registries.datamaps.builtin.NeoForgeDataMaps` 已无 FURNACE_FUELS，原生熔炉读取 `DataComponents.COOKING_FUEL`，codec 字段为 `burn_time` 与 `speed_multiplier`。可由本版 `ModifyDefaultComponentsEvent` 的三参 initializer 注册默认组件；其他模块的资源迁移及实际燃烧验证交由其所有者处理。

## 2026-10-26 方块移除回调与重启探针级联异常

`runtime/three-addons-20261026-174625-228679/reload-server.log:464` 的 CEI 打印机检查在重新放置前调用正常 `Level.removeBlock`；`PrinterBlock.affectNeighborsAfterRemoval` 向 Create 的 `IBE.onRemove` 传入空新状态，后者读取 `newBlockState.getBlock()` 时抛出异常。这属于正常破坏或替换也会触发的生产缺陷。CEI 的经验灯笼、经验舱口、烈焰附魔器、烈焰锻造器及经典烈焰附魔器有相同移植写法，本轮一并修复。

六处回调现传入 `level.getBlockState(pos)` 的实际替换状态，保留 `IBE.onRemove` 对同方块且仍有方块实体的早退判断。真实 26.3 `LevelChunk.setBlockState` 在邻居移除回调前执行 `BlockEntity.preRemoveSideEffects` 并移除旧方块实体；当前实际 Create 的 `SmartBlockEntity.preRemoveSideEffects` 已调用 `destroy()`。原有行为销毁、经验流体掉落和卸载清理继续沿用该生命周期，回调、清理调用及其他玩法方法均保留。

| 方块 ID，命名空间均为 `create_enchantment_industry` | 修改类 |
| --- | --- |
| `printer` | `PrinterBlock` |
| `experience_lantern` | `ExperienceLanternBlock` |
| `experience_hatch` | `ExperienceHatchBlock` |
| `blaze_enchanter` | `BlazeEnchanterBlock` |
| `blaze_forger` | `BlazeForgerBlock` |
| `classic_blaze_enchanter` | `ClassicBlazeEnchanterBlock` |

同日志后续的 CDP `journal.onRootCommit` 异常由前一失败级联而来：打印机旧实体已经移除，CEI 的放置流程尚未创建新实体就异常返回；CDP 探针随后把 `level.getBlockEntity(PRINTER)` 的空结果用作 `FluidTankBehaviour` 的所属实体。提交时访问该空实体触发异常。本轮没有修改 CDP 事务算法或加入掩盖空所属实体的容错。根探针应在构造行为前验证所属实体存在，并在重构 CEI 后重跑创建与重启流程。

CEI 160 源独立 Java 25 编译通过，日志为 `modules/create-enchantment-industry-adapted-263/build/port-javac/20261026-223823-172482/javac.log`，包含 70 条旧兼容接口的弃用警告。模块脚本 `tooling/audit-removal-state.py` 读取失败运行包的 CEI、Create 与实际服务端 Minecraft class，以 SHA-256 记录来源，保存完整异常链及前后反汇编；六个回调均读取实际替换状态，其余 49 个方法的解析后指令保持一致。报告为 `modules/create-enchantment-industry-adapted-263/build/removal-state-audit/20261026-224035-842916/report.json`，检查没有使用 compile-compat 类型作为运行 API 证据。

本轮没有启动 Gradle 或游戏，没有访问原 HMCL 存档。正式 JAR 重构、六种方块的同方块属性更新、正常破坏与替换、流体能力失效、经验掉落及新世界跨 JVM 重启检查仍由根端统一验证。六个生产 Java 文件已冻结。

## 2026-10-26 客户端物品着色与原生材质图层

`runtime/three-addons-20261026-224159-491995/client.log` 和同目录客户端崩溃报告记录了 CDP 在构造流体时通过 Registrate `ItemBuilder.color` 链接已删除的 `RegisterColorHandlersEvent$Item`。实际 NeoForge 26.3 提供 `RegisterColorHandlersEvent.ItemTintSources`，Minecraft 则通过物品模型 `tints` 数组中的 `minecraft:constant` provider 提供固定颜色。

CDP 16 张原版染料桶入口已有准确的原生 tint，现保留原文件；35 张可选染料桶入口补入对应变体的 opaque ARGB，颜色取自各自保留的 Dye Depot、Dyenamics 和 Arts and Crafts 变体定义。所有 51 张底层物品模型、通用桶模型及其纹理引用保留；35 张入口除新增 `tints` 外的 JSON 结构不变。覆盖层位于 layer0，桶体位于 layer1；实际 `BlockModelFeatureRenderer` 对超出 tint 数组的索引使用基础颜色，因此单个 tint provider 延续原有仅覆盖层着色的效果。

`CDPFluids.dye` 移除旧颜色注册，`SimpleItemColors.singleLayer` 改为返回原生 `List<ItemTintSource>`，由 `Constant` 表达同一固定颜色。CEI 没有旧物品颜色注册。同一构造链另有 CEI 四个旧 `.addLayer` lambda 引用已删除的 `net.minecraft.client.renderer.RenderType`，现沿用 26.3 的原生模型材质图层选择：`BakedQuad.MaterialInfo.of` 根据纹理透明度调用 `ChunkSectionLayer.byTransparency`。四类方块的模型、纹理和透明像素均保留。

独立 Java 25 编译通过：CDP 217 源，日志 `modules/create-dragons-plus-adapted-263/build/port-javac/20261026-225229-523601/javac.log`；CEI 客户端注册修复时 160 源，日志 `modules/create-enchantment-industry-adapted-263/build/port-javac/20261026-225451-623934/javac.log`。模块工具 `tooling/item-tint-26.3/audit.py` 和 `NativeTintProbe.java` 使用实际客户端 Minecraft/NeoForge JAR，对 51 个颜色逐一执行原生 `Constant.MAP_CODEC` 解析、精确 ARGB 比对和编码往返，全部通过；没有将 compile-compat 放入该验证 classpath。最终报告为 `modules/create-dragons-plus-adapted-263/build/item-tint-audit/20261026-230135-203118/report.json`，包含实际 class 来源、输入 SHA-256、资源保持检查及两模块直接客户端类型缺失为零的结果。

该离线检查没有烘焙整套模型或生成游戏画面。客户端启动、材质烘焙、实际颜色和透明效果仍需根端重构后在新测试运行目录中验证。扫描同时列出已隔离的旧静态 datagen 类型引用，静态 datagen 的迁移边界保持原记录。

## 2026-10-26 燃料结果记录与玩家及机械臂入口

客户端类型审计进一步发现，CEI 的 `BlazeExperienceBlock` 和三个烈焰设备机械臂交互类仍使用已删除的 `InteractionResultHolder`。这些方法处于活动路径：`useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult): InteractionResult` 与实际 26.3 方块方法同签名；三个机械臂类型由 `CEICommon` 注册，其 `insert(ArmBlockEntity, ItemStack, boolean): ItemStack` 与实际 Create 一致。玩家入口和机械臂插入均调用共享 `applyFuel`，当前没有可替代的另一活动入口。

现采用最高 MC Fly 参考源中的模块内 `FuelApplication(InteractionResult result, ItemStack remainder)` 记录，保留成功、拒绝、非燃料三个结果以及返还物品；三种机械臂继续使用相同燃料和库存分流。普通燃料经验数值、容量、`forceOverflow`、`notConsume`、返还模板及普通经验燃料的模拟路径保持。父类对非燃料返回 `PASS`，让三个子类继续尝试库存插入；此前映射为 `TRY_WITH_EMPTY_HAND` 会使子类提前返回。创意烈焰蛋糕分支也采用参考源的 `!simulate` 守卫，避免机械臂模拟插入时改变机器状态。

CEI 160 源独立 Java 25 编译通过：`modules/create-enchantment-industry-adapted-263/build/port-javac/20261026-230033-330512/javac.log`。`tooling/audit-fuel-result.py` 对照实际客户端 Minecraft、Create 和本轮编译 class 检查玩家及机械臂描述符、活动注册及共享燃料调用，报告为 `modules/create-enchantment-industry-adapted-263/build/fuel-result-audit/20261026-230226-430940/report.json`。后续全量类型扫描中的 `InteractionResultHolder` 引用已清零；这些检查尚未执行世界内燃料消费或库存操作。

联合回归应覆盖三机器经验桶的 1000 经验及返还空桶、非燃料玩家插入、机械臂模拟与实际创意燃料。三种普通经验槽默认均为 4000 mB、最小配置 1000 mB；经典附魔器默认关闭，测试需临时启用并恢复配置。经典附魔器还需设置合法附魔模板作为过滤器，例如含锋利 I 的普通附魔模板配无附魔钻石剑，再验证其 `canProcess`。本轮仅修改授权模块及证据文档，没有启动 Gradle 或游戏，没有触碰原 HMCL 存档；生产 Java/resources 已再次冻结。

## 2026-10-26 可选客户端资源的作用域

CEI 主资源中的 15 张 `items/*.json` 属于可选 Apothic Enchanting 或 Apotheosis 集成，却引用了各集成资源树中的模型和纹理。实际 26.3 `ClientItemInfoLoader` 遍历物品入口，`ClientItem.CODEC` 没有顶层条件字段；因此仅给物品入口添加条件无法隔离这些引用。现将四张 Apothic Enchanting、十一张 Apotheosis 物品入口原字节移回各自既有集成资源树，共用的 `infused_dragon_breath_bucket` 仍在主资源。主 blocks atlas 的四个可选 essence source 则使用原生 `SpriteSources.FILE_CODEC` 支持的逐 source `neoforge:conditions`，条件为加载 `apotheosis`。素材、模型、物品 ID 及原有集成加载条件保留。

迁移证据为 `modules/create-enchantment-industry-adapted-263/build/client-resource-scope-audit/20261026-233210-634791/`。检查确认 15 张入口字节与此前运行 JAR 相同，两模块活动注册物品入口完整，主资源与各集成树内自身模型、纹理引用闭合。根端后续 `runtime/three-addons-20261026-232255-494739/client-233310-732933.log` 已不再出现所列 CEI 可选模型或 essence 纹理缺失。

## 2026-10-26 稀有包裹的世界渲染材质与静态栈审计

同一客户端日志的 `SimpleModelWrapper` 拒绝 CDP 两种稀有包裹，原因是世界 partial 指向 `item/rare_blaze_package` 和 `item/rare_marble_gate_package`，其纹理来自物品图集。包装器会将含非方块图集 sprite 的结果替换为缺失模型。实际 26.3 `MaterialBaker.bake` 优先在物品图集查找同名 sprite，因此把相同 sprite ID 再加入方块图集仍会优先选中物品材质。

`CDPPartialModels` 的包裹世界入口现指向 `block/package/<原物品 ID>`。新增两张世界模型继承各自原物品模型，仅将纹理槽 `2` 改为独立 `block/package/<style>` sprite；blocks atlas 的两个原生 `single` source 将原 `item/package/<style>.png` 绑定到这些别名。原物品入口、物品模型、PNG 均与失败运行 JAR 逐字节相同，原几何与 rigging 入口保留。没有复制 PNG，也没有添加占位模型。

CDP 217 源独立 Java 25 编译通过：`modules/create-dragons-plus-adapted-263/build/port-javac/20261026-234115-411495/javac.log`。离线工具 `tooling/package-atlas-26.3/NativePackageAtlasProbe.java` 使用实际 Minecraft 客户端 JAR 的 `SingleFile.MAP_CODEC` 和 `TextureSlots`，验证两组原资源到新 sprite 的映射、codec 往返及模型纹理槽；全部通过，classpath 不含 compile-compat。字节保持、原生 codec 日志和输入路径记录在 `modules/create-dragons-plus-adapted-263/build/client-package-atlas-audit/20261026-234115-210508/`。完整图集拼接、partial 烘焙及世界内画面仍由根端客户端重试验证。

另对 CDP 300、CEI 216 个当前编译 class 执行 `javap` 静态审计，覆盖全部类初始化器和渲染器构造器，共 69、79 个入口；保守合并重载后，模块内调用图分别到达 119、116 个方法名。未发现非空 `ItemStack`、`FluidStack` 构造或 `asStack`、`getDefaultInstance` 调用；直接栈引用仅有三处将既有 `EMPTY` 放入结果对象。Ponder 场景中的栈在场景执行时创建，排除的 JEI 源不属于活动运行类。本检查不证明任意外部回调的执行时机；没有基于该扫描更改生产算法。证据为 `modules/create-dragons-plus-adapted-263/build/client-static-stack-audit/20261026-234250-627345/report.json` 及完整反汇编。CEI 在该日志中没有 `SimpleModelWrapper` 拒绝项。

本轮没有运行 Gradle 或游戏，没有访问原存档。CDP/CEI 生产 Java 和资源已冻结，后续集成与真实客户端验证由根端串行执行。

补充父模型验证：只读项目测试目录中的固定外置 `create-1.21.1-6.0.10.jar`，沿 `custom_12x10 → package_transforms → minecraft:block/block` 比较继承链。两种包裹各一个立方体、六个面的几何和 UV 不变，全部面与 particle 均解析到对应新方块 sprite。两个入口保留相同的原生末端父模型与 display 变换；证据为上述包裹审计目录的 `parent-geometry-audit.json`。
