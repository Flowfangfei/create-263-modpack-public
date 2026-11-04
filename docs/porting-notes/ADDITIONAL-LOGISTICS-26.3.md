# Additional Logistics 26.3 端侧与 Mixin 复核

当前18模组整体运行与交付范围见[最终验证记录](../VALIDATION.md)。下文保留各次独立编译和静态审计的具体边界。
日期：2026-10-26（Asia/Shanghai）。目标 Java25 / Minecraft26.3 / NeoForge26.3.0.22-beta；使用本包实际 `com.simibubi.create` JAR。固定上游版本、源码快照、许可和已有资源迁移证据见 [Storage / Additional 原始迁移记录](STORAGE-LOGISTICS-26.3.md)。活动源码为 `modules/create-additional-logistics-adapted-263/src/main/java/`。

## 专服失败及修复

原失败日志 `runtime/three-addons-20261026-003217-309839/create-server.log` 记录构造器调用 `CurrencyUtilities.init()` 时出现 `NoClassDefFoundError: net/minecraft/client/player/LocalPlayer`。common 工具类中同时存在 `renderShoppingList`，其方法体将 `Minecraft.player` 传给 common Player 参数，类验证因此需要客户端继承关系。

购物清单叠加实现已完整移入 `client.CurrencyClientUtilities`，客户端 TableCloth Mixin 改调该类。货币注册、值换算、付款/找零、购物列表和后端初始化仍留在 common 工具类，注册顺序不变。

客户端数据包的柔性轴和销售账本屏幕实现、菜单/缓存读取客户端 Level，以及收银台 tooltip 的本地 Player 和 TooltipFlag 查询，均通过 `client.CALClientAccess` 隔离。common 调用边界只使用 common Minecraft 类型；原有屏幕、菜单和 tooltip 内容保留。

## 实际字节码发现的 Mixin 修正

| 活动 Mixin | 原问题 | 修正 |
| --- | --- | --- |
| `MixinGlob` | 裸方法名匹配两个 `toRegexPattern` 重载，callback 只接收一个字符串 | 精确指定 `(String)String` 描述符 |
| `client.MixinFactoryPanelScreen` | `sendIt` 已不存在 | 注入真实 `sendConfiguration(FactoryPanelPosition, boolean)` 返回点，并匹配参数 |
| `client.MixinStockKeeperRequestScreen` | `clipboardItem` Shadow 不存在 | 使用真实 `clipboardItems` 字段 |
| `client.MixinTrackTargetingClient` | `@ModifyVariable(name = "type")` 不匹配真实局部变量 | 使用 LVT 中 slot 6 的 `overlayType` |
| `MixinFactoryPanelBehaviour` | `resetTimer` INVOKE 目标仅写方法名 | 指定真实 owner 与 `()V` 描述符 |

全部 22 个配置活动 Mixin 的 58 项 callback、Shadow、Accessor、Invoker、Overwrite 与方法选择描述符检查无未匹配项。6 个 INVOKE 目标均存在于实际 javap 字节码。另核对 FactoryPanel `tickStorageMonitor` 的 `inStorage` / `demand` 局部变量，以及 `tryRestock` 赋值后的整数次序：availableOnNetwork=0、inStorage=1、promised=2、maxStackSize=3、demand=4、amountToOrder=5；与现有 `@Local` ordinal 一致。未移除注册、未删除或屏蔽活动 Mixin。

证据在模块 `build/mixin-bytecode-audit-20261026/`：`compiled-signatures.json`、`invocation-targets.json`、`factory-locals.javap.txt`、`track-locals.javap.txt` 及各目标类 javap 输出。该目录为本地生成诊断，不代替真实 transformer 应用测试。

## 验证与剩余边界

最终独立编译 `2026-10-26T14:10:22+08:00`：`scripts/javac_probe.py create-additional-logistics-adapted-263`，真实 Java25/Gradle 导出 classpath，150 个活动源文件、0 errors、58 warnings、无缺失 classpath。日志 `modules/create-additional-logistics-adapted-263/build/port-javac/20261026-141022-722062/javac.log`，机器结果 `build/last-port-javac.json`。

本子任务只执行独立 javac 与静态字节码检查；未运行 Gradle、游戏或世界操作。根端仍须重建并验证联合专服/客户端加载，以及收银台付款、购物清单叠加/tooltip、销售账本下单、FactoryPanel 追加库存/承诺限额提交、网络监视器轨道叠加、柔性轴配置等实际路径。Storage 源码、持久化、全局物品校验及共享构建不在本轮修改范围。


## 可选 CC 外设注册的反射隔离（2026-10-26）

联合 18 模组已实际进入世界。`runtime/three-addons-20261026-171505-534816/create-server.log` 中的包裹编辑器探针在 `Class.getDeclaredMethods` 解析 `PackageEditorBlockEntity` 时出现缺失 `dan200.computercraft.api.peripheral.IPeripheral`。真实运行 JAR 的该类虽已在 `registerCapabilities` 中检查 CC 是否加载，lambda 仍被编译为返回 `IPeripheral` 的合成方法；反射枚举方法无需执行分支就会解析它。`CashRegisterBlockEntity` 和 `NetworkMonitorBlockEntity` 各有同样的裸返回描述符。

三种 BE 现在继续注册原物品 capability，再调用 `CALComputerCraftProxy.registerPeripheralCapability`。proxy 的签名只使用原生事件、BE type 和返回 `AbstractEventfulComputerBehavior` 的 Function；确认 CC 已加载后才调用新增普通类 `CALComputerCraftCapabilities`。实际 `PeripheralCapability.get()`、返回外设的 lambda、原 BE type 与 `computerBehavior.getPeripheralCapability()` 全部在该可选类内，原外设及 fallback 行为保留。未添加 CC 运行依赖、删除外设注册或修改原 CC implementation 源码及既有构建排除策略。

最终独立真实 Java25 javac `20261026-172147-506450`：151 个源文件，exit 0，58 warnings，无缺失 classpath。扫描全部 215 个全新 class，只有隔离的 `CALComputerCraftCapabilities` 带 `dan200/` 类常量或字段/方法描述符；三种公共 BE 与 proxy 均为零。真实运行 JAR 的 before 方法、完整描述符、class SHA-256 及新编译 after 对照见本地审计产物的 `additional-cc-reflection-isolation-172147.json`。

Java 已冻结供统一重建。本轮未运行 Gradle、游戏或操作世界；需由下一次 CC 未安装的真实包裹编辑器探针验证反射、库存回滚与地址规则，CC 已安装的集成路径也尚未验收。


## 可选货币数据映射的逐值条件（2026-10-26）

`runtime/three-addons-20261026-172344-433388/create-server.log:345-350` 的六个 Numismatics 缺失物品警告来自 `data/createadditionallogistics/data_maps/item/currency.json`。原文件已有顶层 `neoforge:conditions`，但实际 26.3 的 `DataMapLoader.readData` 直接解码 DataMapFile；DataMapFile 只在每个 values 值上包装 ConditionalOps，根对象中的条件被忽略。`buildDataMap` 则在解析 registry key 之前跳过 empty 条件结果。

现将相同的 `mod_loaded(numismatics)` 条件移到六个币种各自的 value 上，保留全部键、`id: numismatics:coins` 和币值：spur=1、bevel=8、sprocket=16、cog=64、crown=512、sun=4096。去掉新增的逐值条件后，六项 payload 与原文件深比较完全一致。main 与 legacyDatagen 的两份 CALDataMapProvider 也改用真实 `Builder.add(..., condition)` 参数生成相同条件，避免重新生成失效顶层条件；同时移除对框架 abstract `super.gather` 的调用。默认排除的两份 provider 分别用真实导出 classpath 独立 javac 校验，均 exit 0、0 warnings。

真实数据映射类型为 `CALDataMaps.CURRENCY_DATA`，即 `DataMapType<Item, CurrencyData>`，ID=`createadditionallogistics:currency`，registry=`minecraft:item`。运行时通过 `BuiltInRegistries.ITEM.getDataMap(CALDataMaps.CURRENCY_DATA)` 读取。在当前没有 Numismatics、没有外包追加映射的集合中应为 size=0；至少应断言没有 namespace 为 numismatics 的键，再结合加载日志确认缺失 ID 不再被解析。Numismatics 安装时应保留上述全部六项，此分支本轮尚未实测。

最终活动模块独立 Java25 javac `20261026-173150-891090`：151 源文件，exit 0，58 warnings，classpath 无缺失。额外 provider 编译日志在本地审计产物 `additional-datamap-provider-javac-20261026/result.json`；before/after、hash、六币值语义与框架加载代码证据在 `fluid-additional-resource-metadata-migration-20261026.json`。源码与资源均已冻结供统一重建，本轮未运行 Gradle、游戏或世界操作。
