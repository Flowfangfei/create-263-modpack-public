# Create Storage / Additional Logistics 26.3 迁移记录

> 本页保留源码融合、接口迁移和端侧排障阶段的技术记录。“当前”“等待”“待测”按相应段落当时的输入理解；后续运行结果见[联合验证](../VALIDATION.md)和[深入玩法](../GAMEPLAY-VALIDATION-26.3.md)，各项未覆盖范围仍分别保留。

## 固定来源

| 模块 | 上游快照 | 版本 | ZIP SHA-256 | 许可 |
|---|---|---|---|---|
| Create Storage | ausmez/create-storage-neo-forge @ fc8ab27bde33458b7edfc24cf9cd053caf5fa9a7 | 1.3.8 | 9a79b85202e61f3aa701ac46a2222b2c906d0505cade7be6d3eefb409d5c5771 | GNU GPL 3.0 |
| Additional Logistics | KhloeLeclair/CreateAdditionalLogistics @ b96c27a72c51dd2e41b75d8945863774a68a0f6a | 1.4.5 | 89292cc6a4aaa394560e4099e27908d203e24a604edf63963ba285c6cd9d3f52 | MIT |

完整源码及许可导入到各自模块，保留上游构建原件于 `tooling/upstream-build/`。导入没有 `.git`、缓存或构建输出。

## 目标与依赖

Java 25 / Minecraft 26.3 / NeoForge 26.3.0.22-beta。构建复用根 `gradle/create263-addon.gradle`，使用整合包已构建的 Create Adapted、Catnip、Ponder、Flywheel 与 Registrate API；Storage 使用整合包 Sable 3.0.0 适配产物。Additional Logistics 的 ComputerCraft 编译 API 使用 26.3 / 1.120.2。

Storage 的 Curios、EMI、REI、Jade、Thirst、EveryCompat、ConstructionStick 代码和上游来源保留。历史第三方 API 当前仅作 `compileOnly` 迁移参考，不装入运行包，不构成 26.3 联动可用性声明。核心背包、存储箱、存储网络、转运与附加物流功能仍需通过编译及新建运行目录测试。

## 已完成的适配

Additional Logistics 保留现金收银台、包裹编辑、柔性轴、包裹加速器和网络监视器的核心实现。物品能力使用真实 NeoForge ResourceHandler/Transaction 与 Create legacy handler 桥；同一物理库存的多个视图共享快照。运行时方块回调、CT、属性和注册行为保留；旧离线 datagen 另设非默认源集。ComputerCraft 完整实现保留于非默认可选源集，当前整包未安装 CC，也未启用此联动。

Storage 已迁移原生能力、背包菜单输入、配方访问、压缩链服务端同步、方块生命周期、升级消费及事件入口。客户端使用原生 state/submit 方块实体渲染、AvatarRenderState 人物层、ItemModel 和真实独立模型；保留背包飞轮、移动仓储、矿脉渲染及各升级界面。Workshop 界面直接使用原生 GuiGameElement 和 CreateStandaloneModels，保留原光照与动画，并解除对未安装 JEI 的渲染硬引用。客户端源码编译通过尚不代表视觉或生命周期测试通过。

Storage 的 JEI、REI、EMI、Jade、EveryCompat 完整源码保留在非默认 `optionalIntegrations` 源集中，不向联合包加入这些新运行依赖。Curios、Thirst、ConstructionStick 的已有守卫代码及源码仍保留；当前验证不声明未安装第三方联动可用。

## 资源覆盖与数据格式

Additional Logistics 的 867 个、Storage 的 371 个现成生成资源已先与固定上游 ZIP 逐字节比对，并核对全部静态注册方块的 blockstate 覆盖。原始覆盖证据保存在各模块 `tooling/generated-resource-coverage.json`，该证据描述格式迁移前的基线。

现已将 Additional Logistics 的 94 个配方和 156 个掉落表、Storage 的 65 个配方和 44 个掉落表共 359 个 JSON 迁移到实际 26.3 格式。Ingredient 使用物品 ID 字符串或 `#tag`；掉落条件/函数使用 `type` 分派和 `condition` / `modifier` 容器字段。自定义 Ingredient、`neoforge:conditions`、所有数量、概率、权重和条件顺序保留。每个文件的完整原文、迁移前后 SHA-256 和逐结构逆变换相等结果保存在各模块 `tooling/resource-format-26.3/`；迁移脚本为 Storage 的 `tooling/normalize_26_3_resources.py`。后续仍需联合服务端实际加载全部资源。

## 私有库存与原版格式隔离

模组库存采用独立实现。新方案以 `StorageContents` / `StorageInventoryHandler` 保存模组物品格，注册 `fxntstorage:storage_contents`：数量为1的物品原型保存完整原生组件，实际数量单独编码。模组四类实体、背包、挂载库存、掉落物及专用消息使用该格式。

全局 ItemStack / ItemStackTemplate 数量限制及原版 CONTAINER 组件保持原有实现。旧 `tooling/persistence-proposal/review.patch` 中扩大全局计数的候选已废止，其 00:18 FML codec 测试属于历史候选，不能算作新方案证据；活动 Mixin 配置及主源集不包含该全局修改。

自动化使用真实 NeoForge ResourceHandler、ItemAccess 和 Transaction。物品背包通过物理 ItemAccess 的组件交换参与提交/放弃；放置背包的所有视图共享同一实体快照，对外仅开放108个物品槽，快照保留完整159个内部槽。升级、工具、合成、筛选格不作为对外物品槽。

加载前整份校验，构造完整替换列表后一次更换活库存；非法大小或条目不部分写入。背包容量取实际方块/物品等级，内部其他槽保留原生数量或专用单格限制；真实物品槽禁止嵌套背包。GUI缓存按物理物品组件刷新并及时写回，换背包时重载等级和设置，避免旧GUI覆盖自动化结果或串用更大容量。

## 只作用于仓储菜单的同步

原版初始/槽位数据包通过 ItemStackTemplate 校验物品数量，会将1024这类私有显示量判为非法。六种 Storage 菜单因此继承独立 `StorageMenu`，在 `setSynchronizer` 包装原生同步器：完整初始状态和私有槽增量通过模组专用消息发送，玩家槽、鼠标和dataSlots继续委托原生同步器。完整菜单允许159内部槽加36玩家槽，消息按实际菜单大小、containerId、15位stateId及全部条目预校验。

原生 HashedStack 的数量字段和 RemoteSlot 匹配本身支持独立整数，继续保留，以维持普通客户端点击的状态及hash校验；未通过Mixin放宽模板或绕过点击验证。

独立同步诊断已编译、真实目标字节码已核对；在独立Java进程执行时因FML尚未建立模组列表而停止，未进入运行断言，不能记为运行通过。实际游戏由统一客户端探针检查初始1024数量、原生取64/放回及服务端组件和数量守恒。

## 移植阶段的验证记录

| 范围 | 已有证据 | 待完成 |
|---|---|---|
| Additional Logistics | 正式Gradle build成功，公共/客户端入口修复、资源迁移及活动Mixin静态核对 | 联合核心机器、客户端交互 |
| Storage私有持久化、事务和槽位 | 正式Gradle build成功，活动14个Mixin与真实运行目标签名/相关指令一致 | 真实世界、掉落/重新放置、保存重启、挂载拆装 |
| Storage菜单同步 | 全290源javac及正式Gradle build成功；原生网络/模板/hash字节码核对 | 真实客户端协议和界面测试 |
| 新增回归探针 | 私有codec/非法输入/事务、原版81格箱子重启、四类真实掉落/放置、客户端取放探针正式构建成功 | 联合启动后实际执行，不以探针编译代替运行结果 |

所有测试仅在项目runtime新世界及其副本中进行；原HMCL存档不读取、不修改、不启动。上述阶段以旧十二模组为已验收基线，十八模组当时仍在联合启动排障。Gradle执行和最终验证由根任务串行负责，复杂玩法和未安装第三方联动不记作通过。
