# 五个附属模组及 Dragons Plus 的26.3迁移

更新：2026-10-27T00:23:52+08:00

候选版本为 **0.3.0-alpha.1**，共18个直接运行JAR、15个源码模块目录。本轮在已有十二模组组合上迁移Electro Energetics、Storage、FluidLogistics、Additional Logistics、Enchantment Industry及其Dragons Plus前置，统一使用Minecraft26.3、Java25、NeoForge26.3.0.22-beta和本仓库的Create/Sable/Simulated接口。

## 已有结果

新世界与独立JVM重启的四组服务端探针均通过，新增六模块两个阶段各37/37。最终客户端489个注册物品完成三展示上下文解析与十页实际GUI绘制，10个世界fixture、Ponder两场景、真实设置菜单及保存回标题/退出码0通过。Windows安装器实际运行后，恢复素材的18个JAR全部非目录条目与最终运行包一致，平台、官方素材哈希和无世界条件同时通过。

| 模块 | 功能源码基线 | 已验证场景 | 继续专项测试的范围 |
| --- | --- | --- | --- |
| Electro Energetics | NeoForge1.2.0功能树＋Fabric26.2接口参考 | 5项JUnit；原生能量事务、真实线网/Sable投影、连接保持；124注册物品模型 | 真实电机负载、移动子空间导线、电车及完整自定义渲染器画面 |
| Additional Logistics | 官方1.4.5 | 包裹编辑器库存/回滚、地址与规则处理、可选货币映射守卫；157物品模型 | 加速器时序、竖直传送带、列车界面及未安装外设 |
| FluidLogistics | 官方master，版本字段1.3.1 | 多槽/共享容量事务、铜桶ItemAccess、包裹事务、多流体罐磁盘重读、真实马达/泵/管道6000mB输送；88物品模型 | 更多过滤组合、各机器GUI及复杂生产线 |
| Dragons Plus | NeoForge1.11.9＋官方Fly26.1.2参考 | 19项JUnit；真实风处理配方、流体舱及原生事务；23物品模型 | 实际世界风场、改色及更多联动交互 |
| Enchantment Industry | NeoForge2.5.4b＋官方Fly26.1.2参考 | 六类移除/替换回调、经验转换、附魔行为/打印命名；三种机器15组玩家/注册机械臂供料；17物品模型 | 完整附魔/锻造产物与经验扣除、传送带打印耗液 |
| Storage | 官方1.3.8 | 私有库存/组件/事务、四类真实掉落/放置、原版81格跨JVM重读；80物品模型、真实背包点击、压缩配方网络往返 | 装备/升级完整流程、挂载拆装、过滤/控制器网络及复杂自动化 |

这些是明确执行过的场景。物品模型的上下文解析不等于全部手持/地面动画已人工验收；GUI颜色修复也不等于所有界面均已逐一操作。复杂玩法、多人及长期负载继续作为后续专项任务，未安装的JEI、Curios、KubeJS、ComputerCraft等不计为已验证。

## 源码与迁移方式

最高MC版本的Electro、Dragons Plus和附魔工业源码使用Fabric/Create Fly的 `com.zurrtum` 接口，本包使用NeoForge与 `com.simibubi`。迁移保留固定高版本参考，并以完整NeoForge功能树为基线，融合MC接口变化；各模块保留独立来源、版权和许可。[固定来源](../manifests/sources.json)、[检索记录](FIVE-ADDONS-SOURCE-DISCOVERY.md)记录具体提交及归档哈希。

主要修改涉及原生物品/流体/能量事务、组件绑定时序、ValueIO持久化、网络消息两端注册、方块生命周期、配方/进度/战利品条件、客户端提取/提交、物品与方块图集边界及文字ARGB。活动Mixin与实际运行目标核对，旧入口消失时保留功能迁到新入口。可选联动资源与实际默认源集分开，不用空处理器或空模型替代核心功能。

详细技术记录见[电力](porting-notes/ELECTRO-26.3.md)、[仓储与附加物流](porting-notes/STORAGE-LOGISTICS-26.3.md)、[附魔工业与Dragons Plus](porting-notes/ENCHANTMENT-DRAGONS-26.3.md)、[流体物流](porting-notes/FLUID-26.3.md)。局部静态审计的时间和边界保留，最终整体状态以[验证记录](VALIDATION.md)为准。

## Storage私有库存

`fxntstorage:storage_contents` 每格保存数量1的完整物品模板及独立实际数量，用于本模组箱子、背包、挂载库存与专用消息。自动化通过NeoForge标准事务接口接入，同一库存的不同视图共用所有者和快照，坏输入完整验证后才统一写入。

全局ItemStack/ItemStackTemplate、原版CONTAINER、原版箱子、玩家槽及光标保留原生规则。模组菜单只为私有槽位使用独立消息，原生点击hash校验继续生效。旧的全局数量候选已废止，不能用其历史测试替代当前方案。

实际客户端块交互打开背包后，原生点击完成1024→私有槽960＋光标64→私有槽1024，客户端和服务器均确认组件与数量守恒；铁三阶、煤二阶及剑负缓存通过实际请求/服务端构造/回复/客户端重建，未直接填入cache。标题和数量已截图复核。旧版Storage存档自动迁移未验证，原存档不作为测试输入。

## 复现与证据

按仓库[README](../README.md)准备依赖和测试模板后执行：

```powershell
python -X utf8 -B scripts/build.py --only five-addons
python -X utf8 -B scripts/package.py
python -X utf8 -B scripts/gradle_serial.py runtime-probe build
python -X utf8 -B scripts/test_three_addons.py server
python -X utf8 -B scripts/test_three_addons.py client
```

`server`只创建项目专属新世界并独立重启，`client`只使用其校验副本。运行路径、实际报告、JAR哈希与安装器验证见[机器可读摘要](../manifests/validation-summary.json)。原HMCL存档未被读取、启动或修改；日志、世界、截图、个人配置及AI记录留在本地忽略目录。

0.3源码归档与私有远程交付状态见[项目进展](../PROGRESS.md)，旧十二模组结果保留于[0.2验证副本](VALIDATION-v0.2.0-alpha.1.md)。
