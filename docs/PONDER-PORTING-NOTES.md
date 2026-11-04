# Ponder 模板、渲染与回归细则

[English](PONDER-PORTING-NOTES.en.md) · [通用升级方法](MOD-UPGRADE-AND-TESTING.md) · [实际审计](PONDER-AUDIT-20261104.md)

本页保留虚拟世界、精确字段兼容和客户端回归的具体方法。它属于客户端专项，机器事务、持久化、物理和真实生产链验收见通用指南与开发总览。执行命令前准备匹配产物、独立启动模板和测试夹具；源码包不附种子世界。

## 1. NBT、数据组件与 Ponder 虚拟世界

普通世界保存、网络包、资源模板和 Ponder 内存备份可能使用不同格式。先定位数据来源与读写链，再确定迁移入口。资源模板中遗留的旧 NBT，可以在模板加载边界转换；这类问题通常无需改变真实世界方块实体的保存格式或通用物品、流体 Codec。

一个可审查的适配器应限定注册 ID、字段、旧格式特征和允许的键集合，并保证再次执行结果不变。保留合法新格式、无关附加数据及互相冲突的两种表示，便于报告和进一步处理。以下是已确认的实例；字段规则不能扩展为全局 NBT 替换：

| 来源与症状 | 限定转换 | 必须保留的内容 |
| --- | --- | --- |
| 旧模板的大容量库存无法解码 | 对确认的私有库存 ID 与旧字段生成对应私有格式 | 槽位、实际数量、物品组件、过滤器 |
| 机械臂或黄铜隧道反序列化 AIR 报错 | 仅 `create:mechanical_arm/HeldItem` 与 `create:brass_tunnel/StackToDistribute` 中，恰有 `id`、`Count` 两键且为 `minecraft:air`、整数 0 或 1 的旧栈，改为 `{}` | 非 AIR、新格式、额外键、其他数量与冲突数据 |
| 旧流体栈字段与目标格式不同 | 按确认的容器和字段转换 ID、数量及组件 | 药水效果、瓶型、原数量；容量信息独立处理 |
| 控制器位置或材料字段结构改变 | 仅适配确认的方块实体 ID 和对应字段 | 位置语义、方向、材料身份及无关元数据 |

[旧物品占位适配器](../modules/create-aeronautics-adapted-263/simulated/src/main/java/dev/simulated_team/simulated/ponder/LegacyPonderTemplateItems.java) 使用上述 AIR 规则。若 Ponder 创建备份时又产生旧结构，还需在它自己的内存备份边界做同样受限的处理；[备份适配入口](../modules/create-aeronautics-adapted-263/simulated/src/main/java/dev/simulated_team/simulated/mixin/new_ponder/PonderLevelBackupMixin.java) 不改变真实世界的保存行为。

静态测试可以枚举模板、比较变更前后字段、验证未知格式保留及重复转换结果。动态测试还要确认虚拟世界恢复、脚本修改、镜头渲染和备份恢复。Ponder 可以手动设置动力速度而没有真实动力源；渲染侧在确认无来源时使用合理中性因子，保留有来源时的计算，可避免为展示场景修改全局动力网络。

## 2. 渲染迁移：管线、PiP 与纹理生命周期

沿数据提取、渲染提交、顶点写入、材质、图集和输出目标逐层核对。延迟提交的渲染路径应使用该帧的数据快照，不能假定提交时的可变对象在绘制时仍有相同状态。顶点格式、shader 输入、采样器、uniform 绑定、混合、深度、剔除和输出 attachment 都属于同一个契约。

世界画面、物品预览、Ponder 或其他画中画（PiP）目标可能具有不同 attachment 数量。只在主世界中测试，容易遗漏预览窗口的管线校验错误。26.3 的一个火焰管线实例需要使颜色输出与主世界和 PiP 实际提供的目标一致；不能把其他管线的多目标布局直接套入。[目标版本管线示例](../modules/create-aeronautics-adapted-263/aeronautics/src/main/java/dev/eriksonn/aeronautics/index/client/AeroRenderTypes.java)

纹理文件存在，还需要完成 sprite shift 注册和图集装载。若对象第一次在渲染中懒初始化，而图集拼接已结束，`SpriteShiftEntry.getOriginal()` 仍可能为空。本次绳索故障通过在物理客户端构造入口提前调用 `SimSpriteShifts.init()` 修复；已有 Catnip 图集事件继续在资源重载时更新 sprite。专用服务器不加载此客户端入口。[客户端初始化](../modules/create-aeronautics-adapted-263/simulated/src/neoforge/java/dev/simulated_team/simulated/neoforge/SimulatedNeoForgeClient.java)

核验应同时覆盖首次加载和 F3+T 资源重载：等待重载完成，确认 original 与 target sprite 均存在，再实际显示对象。测试使用 `Minecraft.reloadResourcePacks()` 时，也要等待其异步完成。随后查看真实 GUI、相关错误日志和截图；单纯跳过空 sprite 的绘制会留下缺失的对象。检查同一注册入口的全部 sprite shift，以及资源路径、大小写、图集目录覆盖和材质缓存重建。

## 3. 动画完成条件与脚本尾段

区分预定持续播放的背景指令与应该结束的操作。旋转背景可以持续到场景关闭；收到减速或停止请求后，测试应等待实际停止。持续状态应由具体类、字段和参数判断，不能把所有超时或同一模组的指令都视为正常。

停止判定应对应被控制的量。钻头停转后可以保留任意最终角度，用绝对旋转姿态长度接近零判断完成，会把已停止的动画永久留在队列中。本次目标方块实体会在低速阈值处把角速度明确归零，因此指令在 `startSlowing && currentRotationRate == 0` 时结束，并保留最终姿态。其他实现若仅渐近趋零，应按其单位、阈值和稳定条件定义判定。[停转指令实现](../modules/create-aeronautics-adapted-263/offroad/src/main/java/dev/ryanhcode/offroad/content/ponder/instructions/ChangeBoreheadAndContraptionSpeedInstruction.java)

测试时间轴的可计数部分结束后，仍要执行真实活动队列中的有限指令。任意 seek 到末尾、看到 `markAsFinished` 或达到估算时长，都可能漏掉清空库存、停转、淡出和收尾脚本。对有限尾段设置有诊断信息的上限，超限时报告类名、关键状态及停止请求是否已发出。已确认的持续背景可按精确条件排除；带停止控制器或已收到停止请求的同类实例仍需完成。

动画恢复还要覆盖无目标、前一个索引为 `-1`、空列表和目标消失。仅客户端动画分支需要的边界判断应留在该分支，保持服务器行为和保存格式。

## 按实际逻辑选取代表

Ponder 的默认采样单位是**同一模组中相同实际逻辑的一组程序**。按 storyboard 实现和有意义的捕获参数分组；仅物品或材料别名不同的条目选择一个代表。回调对象身份、重复场景名和跨 JVM 的列表索引都不能单独充当语义分组依据。渲染方式、数据格式、配置分支或本次修改行为明显不同的条目保留独立样本。

每次变更验证受影响的代表组，再加少量跨模组冒烟样本。完整注册索引和模板扫描成本较低，可以保留全量静态检查。无关 JAR 的哈希变化用于记录新的输入，不应单独触发全包回放。测试框架本身改变、共享渲染或序列化基础设施改变、分组依据失效等情况，才根据影响重新扩大范围，并记录理由。

代表清单至少记录：模组、实现、关键参数、同组别名、所选场景、选择原因、输入哈希、执行状态和未覆盖分支。新扫描后重新核对选中程序，避免把旧索引解释为新列表中的同一个场景。脚本目前按索引和数量选取程序，语义分组仍需由审查者记录。

一次历史 Ponder 回归记录了 **389 个程序、3,458 个时间采样点、38,592 个 GUI 帧**，相关错误为零，并覆盖指定内容、有限脚本收尾、资源重载及正常保存退出。这些数字描述该批输入与采样范围；帧数也不表示逐帧像素或每个自然时间点都已检查。多人、长期负载、全部配置、可选模组组合和全部玩法仍需各自的验证。[历史审计与局限](PONDER-AUDIT-20261104.md)

Ponder 可能捕获异常、记录 ERROR 后继续运行。通过条件应同时检查进程退出、超时、探针断言、预期与实际任务、相关 ERROR 和输入哈希。日志解析器应支持属性顺序变化，遇到预期格式缺失时明确失败；确需排除的环境消息用精确 logger 与消息匹配，并记录理由。

## 扫描和代表回归入口

Ponder 选用新扫描中的代表索引：

```powershell
$storageJar = Read-Host 'Candidate Storage JAR under runtime/'
python -X utf8 -B scripts/test_all_ponder_client.py scan --storage $storageJar
$scan = (Get-Content -LiteralPath .local/last-all-ponder-scan.json -Raw | ConvertFrom-Json).result
$jobIndex = [int](Read-Host 'Representative index in this scan render_jobs')
python -X utf8 -B scripts/test_all_ponder_client.py render --storage $storageJar --scan $scan --start $jobIndex --limit 1 --reload-resources
```

此入口另需匹配种子世界、`package-manifest.json` 与夹具；额外候选用 `--candidate-mod` 同时传入扫描/渲染。不可把旧列表索引当作新扫描的同一程序。
