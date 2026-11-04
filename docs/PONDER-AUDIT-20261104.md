# 思索模块检查与兼容修复记录

日期：2026-11-04（UTC+08:00）

状态：**全部已启用思索通过回归，八个修复候选已安装到当前实例。**

本轮从 Storage 思索界面的库存读取崩溃开始，随后检查当前整合包全部思索注册，补充模板内容、时间轴和真实界面绘制检查。本文记录已确认的问题、源码修改及已有证据；完整统一回归与实例安装结果见文末记录。

## Storage 原故障

原版候选在加载 `fxntstorage:reserve_storage_box` 思索时，可复现以下错误：

```text
No key Entries in MapLike[{Items:[],Size:36}]
No key StorageFormat in MapLike[{Items:[],Size:36}]
```

这些库存来自随模组提供的旧思索模板。模板仍保存旧 `Items/Size` 结构，当前 Storage 私有库存读取器要求 `StorageFormat/Entries`。本轮将仓库内 18 个 Storage 模板中的 81 份库存转换为当前格式，按现有容器布局映射简易仓储箱的槽位和过滤信息，并逐项保留物品及数量。

`storageinterface/intro.nbt` 中 `[4,2,2]` 的简易仓储箱在迁移前后均为 64 个钻石；客户端另检查了钻石和过滤项。候选 Storage JAR 仅有这 18 个 NBT 资源条目变化，全部 Java class 字节保持不变。

专项客户端已取得的结果为：26 个注册组件完成编译、16 个不同场景完成实际界面绘制、184 个时间点采样，正常保存、返回标题界面并退出，退出码为 0。这组结果只对应 Storage 专项候选。

本地证据：

- `.local/storage-ponder-template-migration-applied.json`：18 模板、81 库存、迁移前后物品清单及资源 hash。
- `.local/last-storage-ponder-candidate.json`：JAR 条目比较及 class 字节不变检查。
- `runtime/storage-ponder-baseline-20261103-023112-476728/result.json`：原故障复现。该报告中的外层 `passed` 表示复现测试达到目的，内层探针明确记录了预期异常。
- `runtime/storage-ponder-candidate-20261104-151006-419540/result.json`：Storage 专项修复回归。

## 扩大检查后完成的修改

| 模块 | 已确认的问题与处理 |
| --- | --- |
| Railways | `semaphore.nbt`、`switch.nbt` 中 6 条轨道曲线使用旧 NBT 形状。将其整理为当前 `BezierConnection` 和向量读取器要求的结构，保留曲线几何、材料、标志及双向连接。变更限于这两个思索模板。 |
| Create Enchantment Industry | 经验灯笼恢复液体 NBT 时可能尚未绑定世界，更新光照的回调访问空 `level`。回调在未绑定时返回，绑定后的 `initialize()` 根据已恢复液体补算光照。测试夹具另补入已知经验流体，用于检查满、半满和清空；原灯笼模板初始为空。 |
| Fluid Logistics | 流体运输器与智能漏斗的思索脚本仍使用旧灌注、清空字段。脚本改写实际主 tank 的 `Fluid` 和 `Level`；清空时写入原生空流体表示。变更位于演示场景代码。 |
| 官方 Create 外置素材 | 增加专用读取适配器，将旧流体字段、行为 tank 容器和已知嵌套流体包装转换为当前 codec 结构，并迁移 Copycat、普通水轮和大型水轮的 `Material` 字段。旧 `create:milk` 按固定 26.3 目标规范为 `minecraft:milk`。适配仅作用于外置资源包的 `assets/create/ponder/**/*.nbt` 读取结果。 |
| Create 齿轮箱 | 虚拟场景中的传动来源可能没有可确定的方向。渲染器取得空方向时保留基础旋转角度，避免把空值传入方向相关计算。 |
| Create 机械臂 | 场景切换动作时，上一动作可保留 MOVE 阶段而 `previousIndex=-1`。索引访问增加上下界检查；没有上一目标时使用现有无目标角度。 |
| Offroad | 钻头收到演示停转请求后，以最终姿态向量长度而非角速度判断完成，停在非零角度时指令不会结束。修复只让演示指令在实际转速归零后完成，保留最终姿态；钻头 BE、挖掘、物理和保存格式不变。 |
| Electro Energetics | 交流发电机思索播放至转子音效时，`ClientExecution` 仍访问已删除的 `FMLEnvironment.dist`，导致真实客户端崩溃。改用 `getDist()`，候选仅该类变化；原有电路线程生命周期类字节不变。 |
| Simulated 绳轮贴图 | NeoForge 客户端入口漏掉 Fabric 原有的 `SimSpriteShifts.init()`，导致第一次绳轮渲染时才创建纹理偏移对象，错过图集拼接并取得空 sprite。改为物理客户端构造时提前登记；原图与滚动贴图已独立核验存在、路径正确并与上游字节一致，既有 Catnip 图集事件继续处理后续重载。 |
| Simulated 思索读取 | 在 `PonderSceneRegistry.loadSchematic(InputStream)` 的 NBT 读取结果上处理已知兼容数据：移除当前注册表不支持的 `create:fluid_max_capacity` 组件；将已知空 AIR 包裹占位规范为 `{}`；把已知旧 `OpenEnd.Fluid` 移至 `Tank`，并按模板相对坐标和方向构造当前 `Location`。遇到未知或冲突字段时保留原数据并记录，处理不进入世界文件或通用 stack codec。 |

源码入口：

- [Storage 模板迁移工具](../scripts/migrate_storage_ponder_templates.py)与[模板目录](../modules/create-storage-adapted-263/src/main/resources/assets/fxntstorage/ponder/)。
- [Railways 模板迁移工具](../modules/steam-n-rails-263/tools/migrate-ponder-track-templates.py)。
- [外置 Create 资源读取器](../modules/create-adapted-263/src/main/java/com/simibubi/create/infrastructure/assets/ExternalCreateAssets.java)及[专用模板适配器](../modules/create-adapted-263/src/main/java/com/simibubi/create/infrastructure/assets/ExternalCreatePonderTemplates.java)。
- [Simulated 专用模板适配器](../modules/create-aeronautics-adapted-263/simulated/src/main/java/dev/simulated_team/simulated/ponder/LegacyPonderTemplateData.java)及[读取 Mixin](../modules/create-aeronautics-adapted-263/simulated/src/main/java/dev/simulated_team/simulated/mixin/new_ponder/PonderSceneRegistryMixin.java)。

### 流体与材质的核对

对官方 Create 6.0.10 的全部 178 个模板，已用 Java 25 编译实际生产 helper，并使用精确 Minecraft 26.3 NBT 实现逐一运行。共有 34 个模板发生转换，涉及 152 个流体字段、18 个 Material 字段；13,700 项断言通过。检查覆盖流体身份及原量、二次读取幂等、结构元数据、无关 BE 字段和材质属性，未知旧流体案例为 0。milk 仅采用已确认的 `create:milk → minecraft:milk` 等价规则。

部分直接核验的初始内容如下：

| 模板及位置 | 应保留内容 |
| --- | --- |
| `create:depot`，spout `[2,3,2]` | 水 1000 mB |
| `create:smart_pipe`，basin `[4,1,1]` | 两个输入 tank 分别为巧克力 1000 mB、牛奶 1000 mB |
| `create:train_observer`，`[9,3,1]` 与 `[9,3,4]` | 熔岩 8000 mB、水 8000 mB |
| `create:high_logistics/package_postbox`，Copycat `[1,2,4]` | `create:railway_casing` 材质 |

另对 18 个实际运行模块 JAR 和官方 Create 素材 JAR 做了只读统计，共解析 361 个模板。已弃用容量组件仅出现在 `aeronautics:hot_air/steam_vent` 的 3 份水 stack 中，原 `amount` 分别为 `70632`、`1`、`1`。组件自己的容量值 `81000` 与流体量是两个字段；删除该组件时保留上述 amount 原值及其他组件，不做单位换算。

HeldBox/InsertedBox 共发现 54 个字段，其中官方 Create 的 36 个 AIR 占位分布于 9 个模板：24 个为 `Count:0b`，12 个为 `Count:1b`；其余 18 个字段已是 `{}`。当前包裹读取器要求原生空栈表示，本轮只规范已知 AIR 占位，保留非 AIR 内容和未知附加数据。

本地证据位于 `.local/native-ponder-helper-check/report.json`、`smart-pipe-converted.json` 和 `ponder-capacity-package-audit.json`。离线 Java helper 检查没有初始化游戏注册、运行世界或渲染器，实际游戏中的注册表解析和界面表现由下文客户端回归核验。

Auger 模板中的旧 slot14 木板未作为已确认故障处理。`AugerShaftScenes` 明确通过 `createItemOnBeltLike` 生成示例木板；现有证据不足以把这个旧槽位认定为可见内容丢失。

## 检查口径与旧结果处理

前期扫描发现 750 个注册组件、1718 条 storyboard。早期用“场景 ID＋模板 ID”去重得到 353 项绘制任务，这会合并使用相同 ID 和模板、但回调不同的场景程序。最终扫描增加 storyboard 回调对象身份作为去重依据；最终任务数由新扫描报告给出。

旧批次还出现过内部异常被 Ponder 捕获并写入日志、客户端继续运行的情况。仅凭探针 `passed` 或进程退出码 0，曾会将这些批次记录为通过。旧批次整体已排除出最终验收：原批次在 start=200 时仅完成 25/40 项后失败，其前序结果也不作为最终全包通过证据。

当前运行器把 Ponder 内部异常、反序列化失败、BE 数据读取失败及未知注册键纳入失败判定，要求 `internal_errors=[]`。最终规则检查所有 ERROR，只排除五条精确匹配的 Windows 计数器、离线账户/Realms 和开发者披风联网提示。每批还核对全部 JAR hash 与最终扫描一致、任务顺序及实际完成清单一致、真实 GUI 产生绘制帧、保存后返回标题界面并正常退出。

源码检查确认，渲染探针对每项任务编译该组件的全部 storyboard。因此，同一模板的问题可能在同一组件的多个绘制任务中重复记录；错误次数与出错模板数分别统计。

### 严格扫描补充发现

新增日志门槛在 750 个组件均可加载后，仍检测到了旧模板里的机械臂 `HeldItem`、黄铜隧道 `StackToDistribute` 空 AIR、附属模板储罐包装及多方块坐标读错。本轮没有通过排除这些日志来放宽验收，而是在 Simulated 的专用思索适配中补齐处理：

- 空物品仅匹配两种已确认 BE 的两个字段，共 18 处；非 AIR、电池物品、现代格式和带未知附加键的记录保留。
- 根 `TankContent` 仅处理 Create 储罐及柴油蒸馏罐；分段 `Tanks[].TankContent` 仅处理 DragonsPlus 脆弱储罐与三种附魔工业加工方块。数字槽形式的多流体罐数据保持原样。Fragile 使用 Create 的 SmartFluidTankBehaviour，其旧 TankContent 明确迁移为 Fluid；CEI 保留自身 TankContent 字段。旧药水使用当前原生组件编码，核对效果和瓶型。
- `LastKnownPos`、`Controller` 仅处理 Connected 物品筒仓和 Fluid Logistics 多流体罐，原坐标值不变。Create 自身储罐、皮带、仓库仍使用各自已支持的格式，未泛化转换。
- Connected 筒仓会在思索场景备份时再次写出旧坐标。新增客户端 `PonderLevelBackupMixin` 只规范 `PonderLevel.createBackup()` 的内存副本，生产 BE 的保存和读取方法均保持原样。

独立原模板清单抽取了 12 个有液体的 fixture、10 份来源资源与 hash，当前配置启用其中 11 个；经典附魔器功能默认关闭，剩余 1 个只做静态迁移核对、未动态执行。见测试资源 `tests/runtime-probe/src/main/resources/ponder-template-fluid-expectations.json`。真实客户端必须在 BE 读后保存中保留精确 ID、数量、药水效果、瓶型及已有组件，避免仅验证“不报错”。

渲染测试也补上 `markAsFinished()` 之后未计入进度条的有限尾段。抽油机摆动与万向传感器有经源码确认的永久动画，按有界采样检查并单独列出；它们不要求终止，螺旋桨仅在未安排减速停止时作为持续动画采样，一旦存在停止 controller 仍须等停转完成；其余有限指令必须执行完毕。时间轴中间仍为抽样绘制，并不声称每个自然帧均已观察。

轨道切换器的两个 BE 在思索以客户端形式加载时缺少 `SwitchState`，会尝试解析空枚举字符串。专用适配器仅为缺失字段补当前 getter/client writer 的默认值 `normal`，并处理思索内存备份；已有字段及实际世界接口不变。两个原生空管端 `Fluid:{}` 同时迁移为 `Tank:{}`，保持空流体语义与原方向。

最终尾段判定经全部模块及 integration 的 67 个相关文件审查。仅五类效果允许有界持续采样：抽油机摆动、万向传感器视觉更新、未安排减速的螺旋桨、未安排停止的钻头旋转，以及 `totalTicks=Integer.MAX_VALUE` 的电阻烟雾。螺旋桨或钻头收到停止请求后仍必须完成；普通粒子、门缩放和液压设置指令均按有限过程验收。

## 复现工具

以下命令从项目根目录运行，使用项目配置的 Python、Java 25 和已构建候选。执行客户端验证会创建新的项目自有测试副本。Storage 和 Railways 迁移工具只接受固定模板目录，原始模板备份与迁移清单保存在本地忽略目录。

```powershell
# 从本地原始模板备份重算迁移计划；不写入生产模板。
python -X utf8 -B scripts/migrate_storage_ponder_templates.py --from-backup

# 复现 Storage 原故障，以及验证已准备的 Storage 候选。
python -X utf8 -B scripts/test_storage_ponder_client.py baseline
$candidate = Get-Content .local/last-all-ponder-candidate.json -Raw | ConvertFrom-Json
python -X utf8 -B scripts/test_storage_ponder_client.py candidate --storage $candidate.jars.'storage-adapted.jar'

# 以下为本轮全量复现命令；后续修复按AGENTS选代表样本，不默认整包重播。
# 用同一份候选清单执行最终扫描。
$scanArgs = @('-X','utf8','-B','scripts/test_all_ponder_client.py','scan','--storage',$candidate.jars.'storage-adapted.jar')
foreach ($entry in $candidate.jars.PSObject.Properties) {
    if ($entry.Name -ne 'storage-adapted.jar') {
        $scanArgs += @('--candidate-mod', $entry.Value)
    }
}
python @scanArgs

# 扫描通过且 internal_errors 为空后，全量分批绘制。
$scan = (Get-Content .local/last-all-ponder-scan.json -Raw | ConvertFrom-Json).result
python -X utf8 -B scripts/test_all_ponder_batches.py --scan $scan --candidates .local/last-all-ponder-candidate.json --batch-size 40
```

最终批次清单由 `.local/last-ponder-batches.json` 指向，逐场景结果写入对应运行目录的 `rendered-scenes.csv`。本地报告和 classpath 参数文件可能含机器路径，不随可提交文档或发行包分发。

早期诊断轮次的第一批已实际完成全部 100 项绘制、有限指令及保存退出，但首次日志门槛因 `Railways: Failed to fetch dev cape data` 判失败。堆栈与 `DevCapeUtils` 源码确认这是异步开发者披风 HTTP 连接失败。该消息以精确 logger＋文本加入外部联网提示排除项；原始失败报告保留，派生复核报告重新检查同一日志、全部 JAR hash、完整任务清单、有限指令完成和退出状态，再从第 101 项续跑。没有覆盖原始证据，也没有把 `Failed to read SwitchState` 等同模组真实错误排除。该轮之后又新增了生产修复，因此最终八候选统一回归重新从0开始，四批均直接通过，没有复用旧组合的验收结果。

绳索修复另执行了真实资源重载（调用 Minecraft 的 `reloadResourcePacks()`，与 F3＋T 使用同一资源重载入口）。重载完成后断言绳轮原图和滚动目标 sprite 均非空，随后绘制末尾 12 个场景，相关错误为 0，正常保存、返回标题并退出。这组专项报告为 `runtime/all-ponder-render-20261104-182631-953292/result.json`；完整冷启动389项仍以最终批次为准。

## 最终回归记录

以下为最终18个生产模组＋1个测试探针的统一回归结果。完成时间：2026-11-04T18:50:17+08:00。

| 验收项目 | 最终结果 | 本地证据 |
| --- | --- | --- |
| 最终候选清单及 18 个运行 JAR hash | 8个候选；18个生产JAR完整hash已记录 | [修复清单](../manifests/hotfixes/2026-11-04-all-ponder-audit.json) |
| 注册组件与 storyboard 总数 | 750组件、1718条目 | [完整注册表](validation/ponder-registrations-20261104.csv) |
| 按场景、模板和回调身份去重后的任务数 | 389项 | [逐场景结果](validation/ponder-rendered-scenes-20261104.csv) |
| 全注册编译及初始内容断言 | 全部通过；11组启用流体fixture回读通过 | 最终scan报告 |
| 严格日志检查，`internal_errors=[]` | 0个相关错误，五条精确外部提示排除项 | 各批完整日志 |
| 全部批次、全部任务的真实 GUI 绘制 | 4批，389/389项 | [逐场景结果](validation/ponder-rendered-scenes-20261104.csv) |
| 时间轴采样数与 GUI 绘制帧数 | 3458次时间轴采样；38592帧GUI绘制 | 最终batch清单 |
| 每批保存、返回标题界面及退出码 0 | 全部正常保存、回标题、exit0 | 各批result.json |
| 备份、部署及部署后 JAR 一致性 | 8替换＋10不变，18/18匹配受测组合，无probe | 本地部署报告及实例标记 |

本轮未访问或修改用户存档，客户端验证使用项目自有测试副本；未修改通用原版保存接口、全局 ItemStack/FluidStack codec 或原版库存数量约束。原官方 Create 素材 JAR 保持不变，SHA-256 为 `ef87fe5709f1ba1f5b8bb20a2925b5afb4669e178fd6d8bf10c167759eefe37a`。

验证范围覆盖注册场景编译、指定初始内容、脚本灌注与清空、若干时间点的真实 GUI 绘制及正常保存退出。时间轴采用快进和采样，没有逐个自然播放帧观察；本报告也不覆盖全部世界玩法、多人交互、长期负载或所有可选联动。结论适用于当前18模组与已启用功能。本轮源码未提交、推送或创建新发布。

## 后续采样规则

按用户后续指示，动态验证按模组与实际演示逻辑分组，每组只测一个代表。同逻辑的不同物品、材质不重复播放；只有与修复相关的数据格式、渲染路径或配置分支不同才保留额外样本。修改后回归受影响组，并加少量跨模组检查，不因无关JAR哈希改变而整包重播。完整注册清单保留用于低成本静态检查。本轮389项属于规则提出前已启动的统一回归，结束后不追加重复播放。

实例备份位于HMCL根目录下 `instance-backups/Create263-Pack/ponder-audit-20261104-184912`。八个旧JAR和实例标记已校验备份，其余十个JAR不变。没有启动或访问该实例的用户存档。
