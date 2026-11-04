---
id: create263-knowledge-storage-boundary
kind: verified-method
validation_scope: Minecraft 26.3 / NeoForge 26.3.0.22-beta
---

# 模组大容量库存与原版格式分开保存

大容量库存需要同时区分持久化格式、模组菜单消息、原版玩家槽/鼠标，以及自动化事务接口。只给ItemStack放宽全局数量范围，会同时改变不属于该模组的数据边界。

本工程使用 `fxntstorage:storage_contents`：每格保存数量1的完整物品模板与独立实际数量。模板保留组件及显式移除，外层仍是合法原版物品。模组专属菜单发送私有库存格式，玩家槽、光标和原生点击校验保持原有限制。

对外通过原生ResourceHandler/ItemAccess事务读写。相同库存的GUI、自动化与挂载视图必须共享实际所有者和快照；解析时先验证完整输入，再统一写入，避免坏输入造成部分覆盖。

实际验证包含数量1至84,934,656往返、九类坏输入原子拒绝、嵌套回滚、四类真实掉落/放置、原版箱子81格跨JVM重读，以及真实客户端1024→960+光标64→1024。该结论不等于自动兼容任意旧版Storage格式，也不覆盖所有多人/长期操作。

来源：[架构决策](../../DECISIONS.md)、[验证范围](../../docs/VALIDATION.md)、[私有格式实现](../../modules/create-storage-adapted-263/src/main/java/net/fxnt/fxntstorage/inventory/StorageContents.java)、[专属菜单边界](../../modules/create-storage-adapted-263/src/main/java/net/fxnt/fxntstorage/inventory/StorageMenu.java)。原始设计讨论及用户观察保留于本地私有协作记录，未随源码发布。
