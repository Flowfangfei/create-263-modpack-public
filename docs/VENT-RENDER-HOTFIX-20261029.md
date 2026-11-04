# 机器人通道崩溃与重开兼容修复

开始：2026-10-29。完成与实例核验：2026-11-02。

安装前核验确认HMCL中的 `Create263 Pack` 与最新已发布的 `v0.3.0-alpha.2` 完全一致：18个直接JAR逐个整文件SHA-256匹配，无多装或漏装。2026-11-02按授权安装两个新修复，当前实例为alpha.2加本轮热修复，其余16个JAR保持一致。旧发布附件保留。

## 原因与代码修改

| 模组 | 原因 | 修复 | 保存格式 |
| --- | --- | --- | --- |
| Steam ’n’ Rails | “机器人通道” `railways:conductor_vent` 的 `supportsExternalFaceHiding` 和 `hidesNeighborFace` 残留直接抛AssertionError的平台占位 | 恢复NeoForge默认true/false；保留原版形状剔面，不在网格线程读取方块实体 | 不变 |
| Storage | 已有玩家设置在登录时进入迁移分支，访问26.3已移除的FMLEnvironment.dist字段 | 仅改为FMLEnvironment.getDist()；保持CLIENT判断、全部迁移算法和字段 | 不变 |

上传报告的首个业务栈是 `VentBlock.hidesNeighborFace`，红沙是当时正在渲染的相邻方块。26.3先调用邻居的隐藏面钩子，再判断当前块是否支持该功能；只改支持标志不足以修复。

不按贴皮材质推断整面遮挡，是因为通道模型还会显示空框或格栅。默认false保留这些透空区域应看到的相邻面；原版对实心邻块的形状剔面继续生效。没有改方块ID、实体类型、碰撞、传送、箱子和背包的物品保存代码。

## 最新发布与实例的核验链

1. 通过GitHub API核验最新非草稿发布为 [v0.3.0-alpha.2](https://github.com/Flowfangfei/create-263-modpack/releases/tag/v0.3.0-alpha.2)。本地发布安装器SHA-256与实时远端digest一致：`135e92e6decd2f1e0cca2eedce03f9fa3ddad5404b05c4d31f1da8685b06e967`。
2. 独立审查核验安装器18份payload清单、固定补充资产、官方Create外部资源；按资源锁重构的18份运行JAR条目均与HMCL输出吻合。
3. HMCL输出归档SHA-256为 `7f9e64b0ef921145ea1bc760c7fbb5af1cfbb6c96f7c8487ce6f6366edc43273`。主线程再次逐个计算当前实例18个JAR与该归档的整文件哈希，18/18一致。
4. Electro当前JAR为alpha.2的 `d6e37bd470d359344e3bae1fb198d214f3841f1623cf2f7b69f1e35f093ffe12`，实际生命周期类含runServer/stopServer修复。它与alpha.1的模组版本字符串相同，不能只看游戏内版本号。

完整机器可读结果见[实例核验](../manifests/instance-audits/2026-11-02-alpha2.json)。旧的 `Create-Adapted-26.3-Recovered` 是另一个只有单独Create的实例，不影响当前18模组实例的结论。

## 最终候选验证

基线为上述最新发布的18模组组合，只在项目创建的新世界副本替换两个目标JAR，另加仅供测试的探针。修复候选按原包相同规则隔离Flywheel命名空间；与基线逐条目比较，每个JAR都只有上述一个目标class变化，无新增或删除条目。

矩阵包含5种状态/材质、3种邻材质、6个方向，共90个通道；另放4个通道、复制板及复制台阶，共94个通道和96个测试方块。覆盖裸通道、石头/玻璃贴皮、格栅切换、水相邻、缺失方块实体和原版实心面剔除。

| 验证 | 最终结果 |
| --- | --- |
| 生产构建 | 铁道jar、sourcesJar、命名空间核验通过；Storage jar通过 |
| 两个最终JAR共同启用的新建副本 | 3314次面渲染调用检查；90个矩阵位置所在区段已编译，8个区段网格对象实际替换；正常保存、返回标题、exit0，无超时 |
| 独立JVM重开同一保存副本 | 初始化前核对90个方块ID、格栅状态、材质，共270项通过；再次3314次检查及8区段重建通过；正常保存与exit0，无超时 |
| 客户端截图 | 新建与重开截图保存在各自测试目录；已人工复核最终重开截图，世界与测试阵列实际可见 |
| 版本适用范围 | 已实测最新v0.3.0-alpha.2组合；未启动用户原世界，不代表铁路所有玩法或其他未来版本的完整验收 |

原错误已在旧JAR复现。早期诊断有退出超时、Storage字段错误，以及探针仅设置贴皮外观而未记录材料物品的无效样例，均保留且不计入最终通过。最终探针补齐Create原生要求的来源物品记录，重开阶段不重新摆放或修改被核对的数据。

证据见[热修复验证清单](../manifests/hotfixes/2026-11-02-vent-storage.json)。本地原始记录保存在 `runtime/three-addons-20261029-005603-489197/`。测试入口为 [test_vent_client.py](../scripts/test_vent_client.py)，探针从正式Gradle输出加载，不随补丁分发。

## 使用

增量补丁：`artifacts/hotfixes/Create263-alpha2-hotfix-20261102.zip`。

关闭游戏，解压补丁，将当前实例mods里的 `railways-adapted.jar` 和 `storage-adapted.jar` 移到mods外备份，再放入补丁mods目录中的两个同名JAR。其余16个JAR保留。该操作无需修改世界文件。

当前实例已安装这两个JAR，旧文件在mods目录之外校验备份，原存档未读取或启动。源码交付使用 `codex/railways-storage-hotfix-263` 分支，旧发布标签与附件保留。详见[实例部署记录](DEPLOYMENT-HOTFIX-20261102.md)，增量包仍提供SHA-256及生产源码差异。
