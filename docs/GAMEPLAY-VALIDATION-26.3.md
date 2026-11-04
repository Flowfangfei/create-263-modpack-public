# 26.3 服务端玩法深入验证

验证日期：2026-10-28（Asia/Shanghai）。平台：Minecraft Java 26.3、NeoForge 26.3.0.22-beta、Java 25。运行组合含 18 个生产 JAR，测试时另加独立探针。

本轮在 `v0.3.0-alpha.1` 基线上发现 Electro Energetics（下称 EE）的电力模拟生命周期缺陷，并对最小修复候选完成 **29 项新建世界检查、14 项独立 JVM 重启检查，以及两次正常停服检查**。本文记录这些实际执行的服务端场景。已有四组回归及客户端验证的结果由[主验证记录](VALIDATION.md)分别说明。

## alpha.1 基线与最小修复

alpha.1 已有的启动、注册、能力、供料和客户端检查继续保留其原有证据范围。本轮新增的真实电路负载检查暴露出一个尚未被旧检查覆盖的问题：生产 `MinecraftServerMixin` 保留了逐 tick 调用，却遗漏 `runServer()V` 开始和结束时的模拟器生命周期接入。服务器能够启动，电力模拟仍可能没有执行器及有效计算结果。因此，alpha.1 的既有通过记录不能覆盖本报告中的持续供电、真实钻头产出和停服线程回收场景。

修复位于 [NeoForge 的 MinecraftServerMixin](../modules/create-electro-energetics-adapted-263/src/neoforge/java/com/george_vi/electroenergetics/mixins/MinecraftServerMixin.java)：在 `runServer()V` 的 `HEAD` 调用 `SimulationTicker.runServer()`，在 `TAIL` 调用 `SimulationTicker.stopServer()`；逐 tick 的模拟逻辑保持原有实现。[SimulationTicker](../modules/create-electro-energetics-adapted-263/src/neoforge/java/com/george_vi/electroenergetics/simulation/simulator/SimulationTicker.java) 负责创建单线程执行器，并在正常结束时中断、等待和清空执行器。

候选目录为 `runtime/gameplay-assembly-20261028-134044-463472/runtime/`。候选组装时保留 alpha.1 发布文件，比较了全部 18 个生产 JAR：其他 17 个整文件相同；EE 两份 JAR 均有 2204 个非目录条目，其中唯一变化为 `com/george_vi/electroenergetics/mixins/MinecraftServerMixin.class`。该目录是验证用运行包，完整发行物仍须经过正式打包流程。

## 实际执行与通过数量

最终完整运行位于 `runtime/three-addons-20261028-134427-877462/`。`create` 在新建的专用世界中执行；保存退出后，`reload` 用另一个 JVM 读取同一世界。两个阶段均完成服务器就绪检查、原版 `time query gametime`、保存和正常退出，退出码均为 0，均未超时。

| 探针范围 | create 实际检查数 | reload 实际检查数 | 本轮覆盖 |
| --- | ---: | ---: | --- |
| Storage | 12 | 4 | 生存物品放置、原生事务、过滤、升级、真实漏斗、网络断开与重连、磁盘恢复 |
| Processing | 10 | 2 | 无资源负对照、附魔/合成/打印、真实风扇染色、物品与 XP、磁盘恢复 |
| Power / Logistics | 7 | 8 | 电路模拟、实际机械负载与钻头产出、断电/重连、三档加速、磁盘恢复后新一轮工作 |
| 合计 | **29** | **14** | **43 个 checks，全部通过** |

两次 shutdown 独立计数，未加进上述 43 项。测试入口在每次启动前删除旧报告，核对 JAR 指纹，要求所有组到达对应的 `create-complete` 或 `reload-complete`，并将正常退出和 shutdown 报告纳入整轮通过条件。

前两次失败运行保留在本地，未计入通过数量：

| 运行目录 | 实际结果 | 原因 |
| --- | --- | --- |
| `runtime/three-addons-20261028-133441-316368/` | 整轮失败；Power / Logistics 只完成 1 项 | 旧生命周期下未获得真实电力模拟结果，暴露生产缺陷 |
| `runtime/three-addons-20261028-134146-688213/` | 整轮失败；Power / Logistics 只完成 1 项 | 探针枚举 Packager 方法时解析了未安装 ComputerCraft 的可选签名；随后改为从 SmartBlockEntity 获取指定 accessor |

最终完整运行使用修正后的探针与 EE 候选。早期两轮中其他组的成功结果没有重复合并进最终计数。

## 电力模拟、断电和恢复

[PowerLogisticsGameplayProbe](../tests/runtime-probe/src/main/java/dev/create263/probe/PowerLogisticsGameplayProbe.java) 在世界中布置创造电池、红色电动机、Create 机械钻头、漏斗和输出箱。设备节点通过生产调度自然出现，探针通过生产接线 API 建立双线闭合电路；随后由服务器自然 tick 推进模拟和机械工作。

| 测量 | create | reload |
| --- | ---: | ---: |
| 模拟器首次就绪的探针 tick | 71 | 71 |
| 结束时观察到的不同结果对象数 | 158 | 157 |
| 带负载电压 RMS | 299.8662326149537 V | 299.8662326149537 V |
| 电机电流 | 2.0824043931594005 A | 2.0824043931594005 A |
| 电机与钻头转速 | 32 RPM | 32 RPM |
| 生产接口返回的应力容量 | 93.66641235351562 | 93.66641235351562 |
| 第一段工作完成后的累计泥土 | 1 | 3 |
| 断电观察期间累计泥土 | 1，保持不变 | 3，保持不变 |
| 恢复供电后的累计泥土 | 2 | 4 |

就绪检查同时要求执行器仍运行、存在真实结果、至少观察到两份不同结果，且最近结果年龄不超过 40 tick；最终结果年龄均为 0 tick。

断电负对照先移除正线，等电机和钻头连续 8 tick 停止后，再放置一块新泥土。观察 100 个自然 tick 后，电压、电流和两个转轴速度均为 0，目标泥土仍在，输出库存保持不变。重新接线后，钻头实际破坏目标，漏斗/箱内累计泥土增加 1。重启阶段用磁盘恢复的电路再次完成这一过程。

这里的“断电”指测试电路开路。两次服务器都经过正常保存和退出，未进行进程强杀或系统掉电实验。

## 加速器的三档实际时序

三套真实创造马达、Package Accelerator 和 Packager 分别运行于 0、32、256 RPM。每台从来源箱消费 7 个带有测试身份组件的泥土，调用生产 `Packager.activate()` 开始一次打包，然后让两类方块实体自然 tick。探针逐 tick 读取动画、冷却和 lazy counter，未直接调用机器 tick 或加速处理回调。

| 阶段 | RPM | 动画完成 tick | 冷却完成 tick | 采样数 |
| --- | ---: | ---: | ---: | ---: |
| create | 0 | 20 | 40 | 40 |
| create | 32 | 11 | 14 | 40 |
| create | 256 | 4 | 3 | 40 |
| reload | 0 | 20 | 40 | 40 |
| reload | 32 | 10 | 14 | 40 |
| reload | 256 | 4 | 3 | 40 |

每阶段共 120 行样本，三个转速均完整覆盖 elapsed tick 1–40。首个自然 tick 的动画、冷却递减符合对应转速；lazy counter 检查同时接受方块实体两种合法执行顺序。重启后的 32 RPM 动画完成时间相差 1 tick，仍满足实测的单调加速关系。

每阶段三台机器各保留一个含 7 个原物品及原组件的包，来源箱均为空，40 tick 观察期间包内容不变。重启阶段先用原生物品能力取出已保存的三个包并归档，再为三台机器各补充 7 个输入，完成第二轮打包；旧包也再次检查内容。

## 加工、XP 与物品守恒

[ProcessingGameplayProbe](../tests/runtime-probe/src/main/java/dev/create263/probe/ProcessingGameplayProbe.java) 先观察 60 个自然 tick：零 XP 的附魔器及合成台保留原输入且不产出；打印机分别持有低于成本的 9/10 mB、39/40 mB 经验流体，物品和流体不变；未供动力的风扇保留 17 个白羊毛。补充资源、将风扇马达设为 256 RPM 后，继续由原生机器和 depot 流程完成加工。

| 场景 | 实际输入与输出 | 资源及组件检查 |
| --- | --- | --- |
| 烈焰附魔器 | 1 把钻石剑 → 1 把带 3 项附魔的钻石剑 | 4000→3679 mB，消费 321 mB；保留自定义名称和 7 点损伤 |
| 烈焰合成台 | 两把锋利 I 钻石剑 → 1 把锋利 II 钻石剑 | 两个输入槽清空、无副产物；4000→3984 mB，消费 16 mB；保留基底名称 |
| 命名打印 | 3 颗钻石 → 3 颗命名钻石 | 消费 30 mB，完成后流体为 0 |
| 附魔书复制 | 2 本空白书 → 2 本锋利 II 附魔书 | 每本 40 mB，共 80 mB；模板保持原样，完成后流体为 0 |
| Dragons Plus 风扇染色 | 17 个白羊毛 → 17 个红羊毛 | 真实气流使用 `create_dragons_plus:coloring_red`；染料与物品有实体隔离，气流最大距离 20 格 |

附魔器和合成台在探针 tick 101 首次进入活动处理，在 tick 302 首次观察到完成，满足至少 200 个自然 tick 的处理时间。红羊毛首次出现于 tick 403。加工完成后再观察 80 tick，完整物品、组件、XP、模板和处理计时快照不变；独立 JVM 恢复后再观察 120 tick，同样没有重复产出或二次扣费。

[StorageGameplayProbe](../tests/runtime-probe/src/main/java/dev/create263/probe/StorageGameplayProbe.java) 通过生存玩家物品放置入口消耗 8 个方块物品，等待控制器自然发现网络。原生事务覆盖提交、插入/抽取回滚及空箱过滤器回滚；普通铁与带组件铁分别核对。升级通过正面物品交互安装，容量从 2048 增到 4096；生产菜单检查超出基础容量时拒绝拆卸升级，并在回到基础容量后允许拆卸及重装。

真实漏斗输入 24 铁和 7 金，在 220 tick 后传入 24 铁、保留 7 金，观测范围内物品总数保持 121。仅拆掉连接 trim 后，已取得的接口视图自然变为 0 槽；追加的 6 铁在断网期间留在漏斗，物品总数保持 127。重放 trim 并等待后，同一缓存接口恢复，6 铁传入。重启前普通铁为 97、组件铁为 23；重启后这些数量、升级箱中的 2048 铁和过滤设置均恢复，随后真实漏斗又传入 3 铁，7 金仍被拒绝。

## 独立 JVM、落盘和正常停服

两个阶段的进程 ID 和 JVM 启动时间均不同，测试身份保持连续。Storage 在完成时调用生产 `saveEverything(false, true, true)`，测试入口随后执行 `save-all flush` 和 `stop`。

- Storage 重启阶段先检查原方块、三个物理箱、库存、升级及过滤设置，再等待网络自然发现并进行新的漏斗传输。
- Processing 读取创建阶段的完整原生快照，与磁盘恢复状态逐项比较；重启分支没有重建加工装置。
- Power / Logistics 核对原电池、电机、钻头、双线、输出箱身份物品、三个包及马达转速设置；保存电路在不替换机器的情况下恢复带负载工作。

两份 shutdown 报告均观察到生产模拟器类，执行器字段已清空，名为 `CEE-Electrical-Simulator` 的存活线程数为 0。报告中的 `executor_shutdown=false` 和 `executor_terminated=false` 来自执行器字段已经为 `null` 的编码分支；本轮没有保留旧执行器引用去直接读取 `isTerminated()`。证据支持这两次正常停服后的清理结果，异常退出和强制终止仍未覆盖。

## 输入来源与玩家操作范围

本轮保留了生产代码的真实运算和自然 tick，同时通过专用探针准备部分输入和配置。其操作范围如下。

| 操作 | 本轮入口 | 证据范围 |
| --- | --- | --- |
| Storage 方块与升级安装 | 生存玩家物品放置、正面交互 | 物品消费和生产交互处理 |
| Storage 升级拆卸 | `BE.createMenu` → `AbstractContainerMenu.clicked(PICKUP)` | 服务端菜单规则 |
| Storage 过滤及范围设置 | 生产 behaviour setters | 已设置规则的事务、自动化和持久化行为 |
| 电力装置与接线 | 世界方块布置、生产接线 API | 自然模拟、电机负载、真实钻头工作及开路/重连 |
| 加工装置、库存与经验输入 | 生产插入/供液/配置入口及测试库存准备 | 原生加工、资源消费和输出组件 |
| 打包启动与转速 | 生产 `activate()`、创造马达设置 | 自然 tick 下的动画、冷却、lazy counter 和包内容 |

本报告未执行客户端鼠标点击、渲染、菜单网络往返或完整玩家布线流程。服务端菜单调用及 behaviour 设置的结果按上表解释；独立客户端验证见主验证记录。

## 文件关联与 SHA-256

最终运行的 19 个 JAR（18 个生产文件和 1 个探针）均与 `gameplay-inputs.json` 一致，两次运行前后指纹均保持不变。候选 18 个生产 JAR 全部匹配候选 manifest，且全部与最终测试目录中的对应文件相同。三个探针源文件均匹配输入记录和同次 sources JAR，已构建的探针 JAR 与实际加载的文件相同。

| 关键输入 | SHA-256 |
| --- | --- |
| alpha.1 的 EE 运行 JAR | `0ea990bba6e0e60f509e2cc7bf5938d657cd509915d57ebf2073c67a4d0e1495` |
| 已测候选的 EE 运行 JAR | `01db7cfe0e498bc1dcf5798e454367b97633373c99f95a937c48a2913dff744b` |
| 实际加载的 probe JAR | `f77f14ec37bdc6e4a29109761a5146136b7b419459b442ff0374c472cebc8813` |
| `PowerLogisticsGameplayProbe.java` | `9c0c79381bcbbacb3ad9a2e60d029d17f1cba6666a71601fe5344d3b2e0dae11` |
| `ProcessingGameplayProbe.java` | `5e267342c91adf4ab4b127d004cb0162cb29d3c18a3eb80eac89a3b3138a23ef` |
| `StorageGameplayProbe.java` | `0f0e1e88b62b3f38b0494fa767f506c7fe007951461730a755d59be4d94b3499` |

以下路径均相对于仓库根目录。完整 JSON、日志和测试世界保留于本地忽略目录，未随公共文档、源码或发行附件提供。

| 证据文件 | SHA-256 |
| --- | --- |
| `runtime/three-addons-20261028-134427-877462/gameplay-inputs.json` | `56d5529befa95ee7ab459f58ef3b43c06cb35f46a35fb79c6a0a9650dd8cb182` |
| `runtime/three-addons-20261028-134427-877462/gameplay-create-server.json` | `0eccb3d3703e92ff63b398a2cd4a87d10e9b34bb32baa5a0e53e196f1e40e5b6` |
| `runtime/three-addons-20261028-134427-877462/gameplay-reload-server.json` | `8d09da5454228bc6df4815b85a14670a4d64c5cd625e04bf29a4bb95e7acb6f6` |
| `runtime/three-addons-20261028-134427-877462/gameplay-create-shutdown.json` | `f6139d990100f52603a5ec65270db7c4914cd278539d74c931d4f539f8c78f3d` |
| `runtime/three-addons-20261028-134427-877462/gameplay-reload-shutdown.json` | `ada983d38597eb98e05cd51f1401c6d20f8ac8a97310a65dac4bc18a3ae26ca7` |
| `runtime/three-addons-20261028-134427-877462/gameplay-summary.json` | `9a8f7df23646fed344e95444464e659249b1b11a5118d266dfdfeb2ba2bbc01c` |
| `runtime/gameplay-assembly-20261028-134044-463472/candidate-diff.json` | `8362144a0712d4b4fa23509a81f3bbb8398a1e24848461a5493ac75c773b2ebd` |
| `runtime/gameplay-assembly-20261028-134044-463472/runtime/package-manifest.json` | `0e36cc7a12929ed36998fff9f8089f6fb31f756a68aadec207cbf342c6d7efed` |

## 复现入口

先按[源码编译与联合测试说明](../README.md)准备固定平台、依赖、`.local/config.json` 和独立服务器启动模板。[test_gameplay.py](../scripts/test_gameplay.py) 会在项目 `runtime/` 下创建带专用标记的新世界，依次执行 create 和独立 JVM reload；仅 create 整轮通过时才进入 reload。探针通过开关启用，不进入游玩包。

本报告针对保留 alpha.1 默认运行包、单独验证 EE 修复候选的工作方式：

```powershell
python -X utf8 -B scripts/build.py --only electro
python -X utf8 -B scripts/gradle_serial.py runtime-probe jar
python -X utf8 -B scripts/stage_gameplay_candidate.py
```

[stage_gameplay_candidate.py](../scripts/stage_gameplay_candidate.py) 以当前 `artifacts/runtime/package-manifest.json` 为比较基线，复制全部 18 个运行 JAR，仅重新组装源构建产物已变化的模块，并输出 `runtime_package`。将本次输出的相对目录传给测试入口：

```powershell
$candidateRuntime = 'runtime/gameplay-assembly-<本次时间戳>/runtime'
python -X utf8 -B scripts/test_gameplay.py --runtime-package $candidateRuntime
```

本报告已测的参数为 `runtime/gameplay-assembly-20261028-134044-463472/runtime`。上述 helper 用于基线与新构建产物确有差异的候选验证；无变化时会停止并提示使用基线。

后续 alpha.2 默认包若已包含此修复，完成正常构建和 `package.py` 后可直接验证默认运行包，无需再次 stage 同一修复：

```powershell
python -X utf8 -B scripts/gradle_serial.py runtime-probe jar
python -X utf8 -B scripts/package.py
python -X utf8 -B scripts/test_gameplay.py
```

可用 `--group storage`、`--group processing` 或 `--group power-logistics` 选择范围；省略时执行全部三组。每次新运行的路径和指纹以该次 `gameplay-inputs.json` 为准。

## 剩余场景

本轮摘要保留 `all_requested_features_verified=false`。以下场景尚未由这 43 项检查覆盖：

- EE 的移动 Sable 子世界、AC/转子发电、过载电网、列车及长期电力负载；异常退出与强制终止后的线程和数据行为。
- Additional Logistics 的垂直传送带、列车界面、外设集成及长期并发；完整玩家打包操作链。
- Enchantment Industry 的超级经验、雷击与其他合成台模式，物理传送带输送及多机器 XP 管网；Dragons Plus 的其他风扇类型和生物染色。
- Storage 的背包穿戴、主动升级 tick、移动结构、多玩家竞争及长期负载；本报告未覆盖的客户端过滤界面操作。
- FluidLogistics 的完整包裹发运网络。

日志中存在 Windows 性能计数器/OSHI、未安装可选兼容类及非本机 kqueue/epoll 日志组件的诊断。本轮通过结论依据上述运行条件和实际检查，不以日志没有异常文本为前提。
