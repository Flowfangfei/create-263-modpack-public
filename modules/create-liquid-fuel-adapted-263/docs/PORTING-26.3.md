# Liquid Fuel 的 26.3 适配

## 来源与平台

来源为 [vibeported-mc/CreateLiquidFuel](https://github.com/vibeported-mc/CreateLiquidFuel) 的 `26.2-vibeported` 分支，提交 `7728b17e541c046012860987191e5026da3c8358`。保留该源码快照的 MIT 许可与原作者版权声明；这项说明仅针对导入源码。目标为 Minecraft 26.3、NeoForge 26.3.0.22-beta、Java 25，使用根工程统一构建的 Create API。

## 功能与实现

| 功能 | 适配内容 |
| --- | --- |
| 烈焰人燃烧室储液 | 使用 NeoForge 原生 `FluidStacksResourceHandler`，容量1000 mB，所有方向开放 `Capabilities.Fluid.BLOCK`；不把 Create 旧式 SmartFluidTank 误作为新事务接口。 |
| 事务 | 提取与插入支持模拟、提交、嵌套事务和父事务回滚；提交后才通知保存。保留 FluidResource 的组件区分。 |
| 液体加热 | 显式写入 NORMAL / SPECIAL 燃料类型和燃烧时间，更新燃烧室方块热量。普通燃料不会降低仍在生效的超热状态，创造燃烧室不消耗液体。 |
| 手持容器 | 在隔离的单个物品副本上读取能力，仅接受完整容器可容纳的燃料；容量不足不吞桶、不溢出。在 `BlazeBurnerBlock.tryInsert` 中通过可更换物品的临时槽，把容器抽取与储液插入放进同一事务，返还真实余物（含组件），避免将可重复使用燃料罐按固体燃料吞掉。机械臂模拟按 Create 原有工作副本计数协议处理，世界和原物品不变。固体燃料保留原路径。 |
| 数据与重载 | 原生数据映射优先于旧 compat JSON，燃烧时间和消耗量必须为正整数；重载清除已删除的旧映射，防止旧规则残留。 |
| 保存 | 继续使用 `Stomach` 键，通过当前注册表 codec 保存流体及组件；不删除 Create 父级燃料与方块实体数据。 |
| Mixin | 在原有 addBehaviours/tick/read/write 及方块 tryInsert 上注入，不覆盖 Create 原实现；tick 使用 RETURN 覆盖全部提前返回；关键注入严格要求匹配。 |

## 构建与验证

在整合包根目录运行 `python -X utf8 -B scripts/build.py --only liquid-fuel`。JAR 为 `build/libs/createliquidfuel-3.0.0-26.3-adapted.1.jar`。

联合运行测试由根目录 `tests/runtime-probe` 和 `scripts/test_three_addons.py` 提供。测试 JAR 只进入标记的临时实例，不进入游玩整合包。检查原生事务、桶、热量、NBT、真实机械泵供液、数据包重载及实际存档重载。结果以根目录 [联合验证记录](../../../docs/VALIDATION.md) 为准；源码编译本身不能证明玩法已通过。

本模组不增加世界矿物生成，也没有旧区块补生成机制。没有安装的其他燃料扩展与第三方容器须分别验证。
