---
id: create263-pack-KNOW-003
type: method
status: verified
created_at: 2026-10-25T12:55:00+08:00
provenance: TASK-002
---

# 26.3移植中的加载时序与事务边界

这三项经验来自本工程实际启动失败与后续功能复验，不推定适用于所有Minecraft版本。

- 26.3早期注册表加载可能发生在物品、流体组件绑定之前。此时自定义配方应解码为 `ItemStackTemplate` / `FluidStackTemplate`，在实际使用时 `create()`，避免启动阶段构造Stack失败。见矿脉模块 `VeinRecipe`、`ExtractorRecipe` 与 [适配记录](../../modules/create-ore-excavation-adapted-263/docs/PORTING-26.3.md)。
- Mixin的 `TAIL` 仅匹配最后一处返回，不能覆盖带有提前返回的整个tick。液体燃烧室需要 `RETURN` 加实现内部的服务端/创造模式条件，才能持续消耗燃料。见 `MixinBlazeBurnerTileEntity` 及 `IntegrationProbe` 的七tick断言。
- 读取容器里的流体不等于允许抽取。容器抽取与目标插入必须加入同一个原生事务，容量不足或不可抽取时整体回滚。可更换物品类型的容器不能使用 `ItemAccess.forStack`；临时 `ItemStacksResourceHandler` 支持熔岩桶变空桶，也保留可重复使用罐的组件。见 `BurnerStomachHandler.tryInsert`。
- Create机械臂模拟会传入工作副本，通过数量变化与余物计算接受量。模拟应回滚世界和原库存，同时遵守这一副本协议。测试须同时检查模拟余物、原罐内容和目标储液量。

功能证据见 [联合验证记录](../../docs/VALIDATION.md)。完整日志和测试世界保留在本机忽略目录，游玩包中不包含测试探针。
