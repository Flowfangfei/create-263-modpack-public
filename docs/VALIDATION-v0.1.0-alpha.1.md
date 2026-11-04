# 联合验证记录

验证日期：2026-10-25T00:24:37+08:00。平台：Minecraft 26.3、NeoForge 26.3.0.22-beta、Java 25。所有验证使用本工程中新建的平坦测试世界；客户端只使用该世界的校验副本。

| 检查 | 结果与实际范围 |
| --- | --- |
| 源码编译 | Create、Sable、Simulated、Offroad、Aeronautics、Railways、Connected、Connected Simulated Bridge、Diesel 均已编译通过。 |
| 静态打包检查 | 九个模组具有 NeoForge 元数据，无旧 Fabric 入口、无未隔离的 Flywheel 引用；附属模组不夹带 Minecraft/Create/Registrate 等外部类。额外检查跨模组重复 Java 包及空依赖范围。 |
| Railways 正常构建 | 完整资源转换、配方/标签/进度检查和发布命名空间检查通过。 |
| Diesel 服务端引用检查 | 上游 `auditDedicatedServerClasses` 通过。 |
| 整包服务端 | 新世界启动与保存退出通过，退出码 0；同一世界第二次加载、保存退出也通过。 |
| 原版命令 | `time query gametime` 成功。 |
| 新增模组注册 | 实际放置 Connected parallel_gearbox 与 Diesel diesel_engine，并用 `execute if block` 断言其存在。此检查不代表已完成应力或燃料功能验收。 |
| 航空学装配/推进 | 实际选择四个方块、Create 胶水装配子空间、256 RPM 电机驱动螺旋桨、爬升检查通过。 |
| 保存与重载 | 相同飞行器身份恢复；推进、有限且稳定的物理状态检查通过。 |
| 浮力 | 实际燃烧器/气球注册、气体供给、升力输出及关闭后的目标归零检查通过；未将此结果扩展为完整飞艇玩法验收。 |
| 传输 | 12 项液体、物品、电力事务、回滚、对接及状态重载检查通过；炮弹模拟提取保持装填状态检查通过。 |
| 客户端 | 实际进入测试世界副本、观察飞行器渲染、保存返回标题并退出；退出码 0。两张本地截图已留存，不随仓库或发布上传。 |
| Windows 安装器 | Windows PowerShell 实际下载并校验资源，生成 HMCL ZIP；其中九个 JAR 的全部非目录条目与已测试 JAR 内容逐项完全一致。 |
| 世界与素材隔离 | 生成包不包含 saves/world；官方 Create 素材在 config 下且哈希匹配，保留权利素材不进入 Git 或发布附件。 |

## 联合加载中修复的问题

- Connected 可选桥接存在空的 Simulated 依赖范围，改为本包实际版本；加载器会拒绝空范围。
- 桥接与主模组共享 Java 包，触发 FML/JPMS 的 split-package 错误。桥接移动到 `com.hlysine.create_connected_simulated`，反射入口、模型和渲染注册调用一并更新；拆除辅助方法提供明确公共接口。
- 桥接的物品返还、姿态旋转、材质字段和拆除回调迁移至实际 26.3 接口；保留材质的附魔光效与着色数据。
- Sable 重写 `PlayerList.broadcast`，铁道原 getX/getY/getZ 注入失效。存在 Sable 时改为包装重写方法中的 position 调用，保留 Sable 子空间距离计算，并使用车掌相机位置。此兼容层已通过实际加载；多人相机声音感受仍需专项测试。
- Windows PowerShell 的目录复制和合法文件名双点处理已在安装器实测中修正，下载具有校验、超时、重试及可选代理。

## 限制与待测

这是 alpha 集成验证。Connected 应力网络和传动比、Diesel 实际燃料循环/采油/发电、整套列车玩法、桥接油门无线控制和多人车掌视角声音，仍需进一步玩法回归。没有安装的可选第三方模组联动不视为已验证。旧版数据生成器和开发工具的迁移范围以各模块构建排除项为准。

客户端日志含系统性能计数器、离线账户属性、Realms 与开发者披风获取失败的诊断；本次客户端成功进入、保存和退出，没有把这些诊断隐藏为“零错误日志”。

源码来源与哈希见 [sources.json](../manifests/sources.json)，发布 JAR 及打包输入哈希见 [runtime-binaries.json](../manifests/runtime-binaries.json)。压缩器不同会改变 JAR 整体哈希，安装器对恢复资源进行逐条目内容核验；不能据压缩容器字节差异推断代码差异。完整日志、截图和世界仅保留在本机忽略目录。

最后一次 Create 重建仅更新了 JAR 中的 `EXTERNAL_ASSETS.md`。与已测版本逐条目比较，所有 class 及其他资源完全一致；这是文档变化，未因此重复游戏测试。前后哈希和范围记录于 [validation-summary.json](../manifests/validation-summary.json)。
