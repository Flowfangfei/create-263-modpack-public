# 十八模组联合验证：0.3.0-alpha.2

平台为Minecraft26.3 / NeoForge26.3.0.22-beta / Java25。本版包含18个直接运行JAR、15个源码模块；所有测试使用项目runtime新世界，客户端只使用正常停服后的逐文件哈希校验副本，原HMCL实例与原存档未访问。

## 本次发现与修复

alpha.1的Electro NeoForge运行源漏接电路模拟线程的服务器生命周期，导致导线能够存在和保存、实际电路却不计算。恢复固定上游的 `runServer()V` HEAD启动与TAIL关闭钩子，保留原模拟算法和注入失败检查。新旧JAR逐条目核验：仅Electro的 `MinecraftServerMixin.class` 改变，其余17个JAR和该JAR其余2203个文件相同。源码修复见[Mixin](../modules/create-electro-energetics-adapted-263/src/neoforge/java/com/george_vi/electroenergetics/mixins/MinecraftServerMixin.java)。

## 已实际完成

| 范围 | 结果与边界 |
| --- | --- |
| 深入玩法 | 新建世界29项、独立JVM重启14项，共43项通过；两次进程exit0，原版time指令通过，正常停服后executor字段为空且电路模拟活线程为0。 |
| 仓储 | 控制器/过滤接口的原生事务、容量升级实际安装与安全拆装、原版漏斗输送、断网阻断和重连恢复、完整组件/升级/过滤设置落盘。创建12项、重启4项。 |
| 加工 | 真实tick完成附魔、锋利I+I→II、三颗钻石命名、两本附魔书复制、17白羊毛经真实风场变红；有不足XP/停转负对照及精确耗液断言，保存后继续tick无重复产出或扣费。创建10项、重启2项。 |
| 电力与物流 | 真实电路约299.87V/2.08A驱动电机及钻头32rpm，实际破坏方块并收集；断电100tick无工作，重连恢复，磁盘重启后继续工作。0/32/256rpm三套加速器实测自然tick，包裹各7个原组件物品守恒。创建7项、重启8项。 |
| 原有服务端回归 | 四组探针在新建世界和独立重启均通过，新增六模块每阶段37/37；真实数据包reload、原版time、保存及exit0通过。液体燃料泵管供液、钻井真实马达生产和连杆Sable装配/约束恢复再次通过。 |
| 客户端 | 同一18JAR进入校验副本，489注册物品三展示上下文解析、十页实际GUI绘制、10类世界fixture，两场Ponder、液压设置、保存回标题和exit0通过。 |
| 原生仓储菜单与网络 | 真实方块交互与客户端点击协议确认1024→960＋光标64→1024，组件及数量守恒；压缩配方实际请求/服务端构造/客户端回复重建及剑负缓存通过。 |
| 截图 | 人工核对1066×600截图，背包标题、Inventory和1024/960/64数量可见。不是以模型解析代替所有动画人工验收。 |
| Windows安装器 | 原生PowerShell5.1实际执行exit0；生成包18JAR所有非目录条目与同一受测候选完全相同，官方Create素材SHA、固定平台、精确模组集合及无世界条件通过。 |
| 构建 | Electro正式build及5项现有JUnit本轮通过；其余17个运行JAR未改，沿用固定输入。Dragons Plus19项单测保留alpha.1证据，不描述为本轮重跑。 |

43项深入检查的输入、生产入口、测量值及未覆盖项见[深入玩法报告](GAMEPLAY-VALIDATION-26.3.md)。[机器摘要](../manifests/validation-summary.json)和[运行JAR哈希](../manifests/runtime-binaries.json)绑定同一受测候选。安装器实际执行与18JAR核验已通过，发布附件摘要另见[发布页](https://github.com/Flowfangfei/create-263-modpack/releases/tag/v0.3.0-alpha.2)。

## 保存隔离与剩余范围

Storage大数量库存使用模组专用格式；全局ItemStack/模板codec、原版CONTAINER、玩家槽和光标保持原生。原版81格箱子的跨JVM检查在本轮基线回归再次通过。旧版Storage库存自动迁移尚未验证。

深入探针使用生产配置setter、接线API、Packager.activate与测试输入；自然电路/动力/加工tick和输出确已运行。Storage过滤UI鼠标操作、背包装备与主动升级、挂载构件、移动Sable子世界电线、交流/转子及过载网络、其他附魔/风处理模式、竖直带/列车/完整物流网络、多玩家和长期负载仍待专项验收；未安装的JEI/Curios/KubeJS/ComputerCraft等联动不记为通过。

停服检查覆盖正常stop；executor字段清空后没有保留旧对象直接检查isTerminated，不涵盖强制杀进程。日志仍有Windows性能计数器、非本机kqueue/epoll诊断和离线Realms等提示，不能表述为日志零错误。两次初始失败保留在本地，与最终通过计数区分。

旧alpha.1的[固定源码验证记录](https://github.com/Flowfangfei/create-263-modpack/blob/203897ef96ebe398787376ba1372fcd915286748/docs/VALIDATION.md)及更早版本保留；其电力连线存活检查不代替本次真实带负载验收。运行世界、日志、截图、机器配置与协作记录均不进入Git或发布附件。上游格式与许可原文保持，历史空白提示见[格式审计](../manifests/import-format-audit.json)。
