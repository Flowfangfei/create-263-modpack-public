---
id: TASK-003
status: complete
priority: high
created_at: 2026-10-25T22:49:23+08:00
updated_at: 2026-10-27T00:54:22+08:00
depends_on: [TASK-002]
---

# 五个附属及 Dragons Plus 的 26.3 联合迁移

将 Electro Energetics、Storage、FluidLogistics、Additional Logistics、Enchantment Industry 及其 Dragons Plus 前置迁入同一整合包仓库，融合最高 Minecraft 版本迁移参考与最新 NeoForge 功能源码，并统一验证。

## 目标与约束

当前候选为 `0.3.0-alpha.1`，共 18 个直接运行 JAR、15 个源码模块目录。平台为 Java 25、Minecraft 26.3、NeoForge 26.3.0.22-beta，使用同一 Create/Sable/Simulated API。

- 仅使用项目 `runtime/` 内新世界及其校验副本，原存档不读取、不修改、不启动。
- 保留固定上游来源、版权和独立许可；Fabric/Create Fly 与 NeoForge 接口分别核对。
- Gradle 执行保持串行，未安装的可选联动保留来源和范围说明。
- Storage 使用模组私有库存；全局 ItemStack / ItemStackTemplate 编解码器和原版 CONTAINER 格式保持原有实现，旧全局数量候选已废止。

## 已有结果

六个新增模块均已正式构建。已有的新世界创建与独立 JVM 重启两个阶段，四组服务端探针均通过，六模块探针两个阶段均为 37/37 项；两次进程正常退出，原版时间指令成功。Storage 的三只原版箱子共 81 格物品已跨独立 JVM 重读，数量、名称和组件检查通过；四类真实掉落/重新放置、私有库存往返和事务回归也已通过。

客户端排障修复涉及 Electro/Dragons Plus 客户端事件与着色、FluidLogistics Mixin、Electro/FluidLogistics 消息注册和 Storage 双向菜单同步，相关改动已进入正式构建与后续运行验证。新增附魔工业玩家与已注册机械臂供料检查已在两个阶段通过，覆盖三类机器的 15 组交互。最终客户端完整轮次已通过。

## 完成标准

- [x] 导入并记录固定来源，建立 `codex/five-addons-263` 迁移分支。
- [x] 六个新增运行模块正式编译并组装为 18 模块诊断包。
- [x] 新世界创建及独立 JVM 重启的四组服务端探针通过，六模块探针两个阶段均为 37/37 项。
- [x] 私有库存服务端回归、四类真实掉落/重新放置及原版 81 格箱子跨 JVM 重读通过。
- [x] 三类附魔工业机器的 15 组玩家/机械臂供料交互及重启回归通过。
- [x] 客户端实际进入世界，完成模型、菜单双向同步与玩家取放验收。
- [x] 明确复杂机器、物流、电网、附魔加工及挂载拆装的未验证场景与可选联动，保留后续专项任务。
- [x] 完成最终资源、Mixin、依赖和许可清单复核，并冻结对应运行产物与源码哈希。
- [x] 完成候选安装器、安装包和源码档案核验，记录版本与交付状态。

当前源码迁移、首轮alpha运行验收、安装器与源码归档核验及私有远程交付均已完成。复杂玩法继续作为TODO中的后续专项任务，未宣称全部上游功能已验证。

## 证据与范围

迁移分支最初基于提交 `3bcf1272e4715c1f4cc49a613e6367dc41518ba8`，后续构建与验证按本任务记录推进。

- [六模块迁移与当前验证边界](../docs/FIVE-ADDONS-26.3.md)
- [联合验证汇总](../docs/VALIDATION.md)
- [固定来源检索](../docs/FIVE-ADDONS-SOURCE-DISCOVERY.md)
- [固定提交记录](../manifests/source-discovery-five-addons-2026-10-25.json)
- [上一轮十二模组候选验证副本](../docs/VALIDATION-v0.2.0-alpha.1.md)

正式构建日志与新世界运行报告保存在项目忽略目录。复杂玩法、客户端实测和未安装第三方联动分别记录，历史全局数量候选的测试不作为当前私有库存方案的证据。

最终客户端已通过489物品/十页、实际背包点击及压缩配方消息往返、Ponder/设置及保存退出，数量/标题截图可见。交付归档与私有发布验收已完成，见[发布记录](../docs/RELEASE.md)；复杂玩法边界见主验证文档。
