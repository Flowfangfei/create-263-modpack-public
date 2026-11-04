# Create Ore Excavation 26.3 移植记录

本模块将 tom5454 的 Create Ore Excavation 1.6.8 移植到 Create263 Pack。Minecraft 26.3、Java 25、NeoForge 26.3.0.22-beta、Gradle 9.5.0、ModDev 2.0.148 为统一基线。

## 来源与构建

- 官方源码：[tom5454/Create-Ore-Excavation](https://github.com/tom5454/Create-Ore-Excavation)，`main`，提交 `724e18dd2c792440976c0ec469d56d9f2c5e8411`，原目标 NeoForge 1.21.1。
- 参考移植：[Jere655/Create-Ore-Excavation](https://github.com/Jere655/Create-Ore-Excavation/tree/port-neoforge-1.21.11-wip)，提交 `d95850584d1c6beb055ca1b50e510f0e1d500832`。沿用其 NeoForge Value IO、配方、DeferredRegister 和 Transfer API 工作，再适配真实 Create263；没有沿用其 CreateFly 包名替换流程。
- 两份来源均保留 MIT 许可；`LICENSE` 同时进入运行 JAR 与 sources JAR。机器可读来源见 [UPSTREAM-PROVENANCE.json](UPSTREAM-PROVENANCE.json)。`Fabric/` 是保留的原始平台源码，不进入当前构建。
- 当前构建入口是本模块根 `build.gradle`，共用 `../../gradle/create263-addon.gradle`。`NeoForge/build.gradle` 与该目录属性是上游参考构建，不是发布入口。源码直接编译，不从生成目录替换包名。
- 从整包根调用 `python -X utf8 -B scripts/gradle_serial.py create-ore-excavation-adapted-263 build`，遵守全工程单 Gradle writer。
- 输出 `build/libs/createoreexcavation-1.6.8-26.3-adapted.1.jar` 与对应 `-sources.jar`。哈希与构建日志见 [BUILD-EVIDENCE.json](BUILD-EVIDENCE.json)。

## 核心迁移

1. 方块、物品、方块实体、配方类型、菜单、数据组件与创造模式分页改用原生 DeferredRegister，保留上游注册 ID。补齐 26.3 物品定义，并保留模型、材质、语言、Ponder NBT、配方及标签。
2. 迁移 `Identifier`、记录类型 `ChunkPos`、原生消息方法和 26.3 配方序列化 record；修正带 JSON 字符串的矿脉名称组件；矿脉图标与抽液输出改为 ItemStackTemplate/FluidStackTemplate，避免注册表阶段提前访问尚未绑定的组件。进度的 recipe_unlocked 条件迁移为 recipes 列表，Create 加工配方保留当前框架的 ingredients 数组。
3. `ExcavationSmartBlockEntity` 在 Create263 的 CompoundTag hooks 与原生 ValueInput/ValueOutput 间桥接，先保留 Create 父类行为的持久化，再序列化钻头、生产进度、队列与液槽。独立的矿脉 chunk attachment 使用原生 Value IO。生产完成时明确标记 chunk 待保存。
4. FIFO 产物队列使用原生 `ResourceHandler<ItemResource>` 与事务快照；输入液槽只允许外部注入，输出液槽只允许外部抽取。内部钻液消耗与抽液生产直接调用存储层，模拟不会提交；修复参考分支输出液槽拒绝内部生产的问题。
5. 原生 IO 方块 capability 沿多方块父链路由到控制器。转速与配方压力继续通过 Create Kinetic 输入连接。
6. 钻头、轴与碎屑渲染改用 `BlockEntityRenderState`/`SubmitNodeCollector`；半轴渲染独立提交，避免受 Flywheel 的其他视觉对象开关影响。图册界面迁移至 `GuiGraphicsExtractor`，补齐菜单画面注册，并为原生 GUI 文字显式保留不透明 alpha。
7. 服务端同步矿脉、钻井和抽液配方；客户端缓存支持图册与护目镜显示，避免独立服务端连接时访问不存在的客户端 RecipeManager。

## 验证范围

`build` 已通过真实 Create263 六个编译 JAR 与 NeoForge 26.3 编译，产出运行包与源码包；包内 101 个 JSON 资源语法检查通过，11 个注册物品均有新物品定义，MIT 许可进入归档。没有独立 JUnit 项目；这一步仅证明可编译、可打包。整包联合服务端在 `runtime/three-addons-20261025-125059-362850` 通过基础功能探针：15 矿脉、14 钻井、1 抽液配方加载，钻井 162 手动 tick、抽液 17 手动 tick 完成生产，事务、capability、NBT、图册组件/菜单和背罐取样均通过。日志 `COE263_RUNTIME_CHECKS_OK` 与该轮 `server/ore-excavation-runtime-checks.json` 提供证据。首轮暴露的旧进度条件、加工配方字段和注册表阶段提前创建 stack 问题已修复。同一世界保存、停服与重启后，精确磁盘状态核验通过；两台真实 Create 创意马达以 128 RPM 驱动 220 个普通世界 tick，输入轴配方压力为 256，铁产物从 1 增至 2，水量从 500 mB 增至 16000 mB，`COE263_RELOAD_CHECKS_OK` 与 `server/ore-excavation-reload-checks.json` 均确认通过。首轮客户端成功进入世界并渲染两台完整机器，图像 `client/screenshots/2026-10-25_12.54.33.png` 与 `CREATE263_CLIENT_SCREENSHOT_ORE` 提供证据。未发现本模块命名空间的模型/材质/物品定义缺失。客户端还暴露出特殊机器配方被普通配方书尝试放置的警告，已补 `isSpecial=true`，等待最终日志复核。

可选开关 `-Dcreateoreexcavation.runRuntimeChecks=true` 在 `ServerStartedEvent` 运行检查；要求 `-Dcreate263.probeRoot=<server目录>` 且目录有 `.create263-disposable-runtime` 标记；还会核验该路径等于实际 `FMLPaths.GAMEDIR`，活动世界根目录等于该目录下的 `world/`。代码不会自动停服。也可由联合探针调用 `com.tom.createores.block.entity.OreExcavationRuntimeChecks.run(ServerLevel)`。

初次检查覆盖实际配方加载、钻头标签、原生物品/流体事务回滚与提交、液槽方向限制、队列与液槽 NBT、真实机器物品的多方块放置、钻井与抽液的生产 tick、配方压力、IO capability、机器 Value IO、矿脉消耗、图册数据组件与菜单、取样钻与 Create 背罐耗气。生产检查以受控 `Kinetic` 接口提供 128 RPM，循环结束立即解除临时供能，不能代替实际电机网络测试。手动推进循环上限为钻井 700 tick、抽液 100 tick、取样 200 tick。

- 夹具：钻井控制器 `(72,-58,8)`、抽液控制器 `(88,-58,8)`、取样钻 `(104,-59,8)`，与整包液体燃料夹具分离。
- 成功日志 `COE263_RUNTIME_CHECKS_OK`；输出 `<probeRoot>/ore-excavation-runtime-checks.json`。失败写 `status=failed` 并记录 `COE263_RUNTIME_CHECKS_FAILED`。
- 整包执行保存、退出、重启后，加 `-Dcreateoreexcavation.runtimeCheckMode=reload`。此模式先保留夹具并核验产物、500 mB 水、钻头、完成状态和两处 chunk 矿脉 attachment，再在 `(70,-59,8)`/`(86,-59,8)` 放置朝东的 128 RPM 原生 Create 创意马达，临时强制加载夹具 chunk，等待 220 个普通世界 tick，核验输入轴转速、256 配方压力与额外铁/水产量。最终结果在这个异步检查完成后写入；结束时只撤销本次新加的强制加载票据，保留完整机器和马达供客户端验证。成功日志 `COE263_RELOAD_CHECKS_OK`；输出 `ore-excavation-reload-checks.json`。
- 本模块绝不访问原 HMCL 存档，测试只用于带标记的新建世界。

## 未启用范围与剩余验证

JEI、KubeJS/Create KubeJS、CC:Tweaked、JourneyMap 尚未取得相应 26.3 API，当前构建明确排除这些联动源码。源码与资源仍保留，禁用门面只供基础模组启动；检测到这些外部模组也不会启用其旧 API。上游数据生成器仍保留但不进入生产编译，发布使用保留并迁移的生成资源。

当前不承诺旧 Minecraft 世界的跨版本兼容性。客户端已进入世界并渲染完整机器；物品图标、四向放置/破坏、护目镜与图册的完整手动交互仍可补测。真实 Create 电机、输入轴压力与生产已经通过服务端检查；外部漏斗/机械臂和管路的完整玩家搭建流程仍可补充检查。资源管线、玩法运行、重启持久化的最终状态应以联合测试输出更新本记录。
