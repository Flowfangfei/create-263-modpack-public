---
id: create263-pack-TASK-002
status: complete
priority: high
depends_on: [create263-pack-TASK-001]
provenance: user-request-2026-10-25
---

# 三个附属模组联合适配

## Outcome

在已发布的 Create263 Pack 框架下加入液体燃料、矿脉钻井及传动与连杆，保留独立来源和许可，使用同一版本的前置模组。

## Definition of done

- [x] 三个模块使用真实 26.3 API 编译，保留对应源码及可重复构建。
- [x] 源码、运行包与许可清单纳入统一打包流程。
- [x] 在仅含本工程新建世界的隔离目录联合启服、保存、重载与客户端加载。
- [x] 验证液体燃料输液/加热/事务/数据重载，矿脉钻井定位与提取，连杆与液压约束关键行为。
- [x] 写明实际测试、未覆盖功能与可选联动，不将编译通过等同玩法完整验收。

## Evidence

- [本地会话记录](../docs/ai-collaboration/sessions/2026-10-25-three-addons-263.md)
- [共享平台](../gradle/create263-addon.gradle)
- [串行构建入口](../scripts/gradle_serial.py)

- [最终联合验证](../docs/VALIDATION.md)与[机器可读摘要](../manifests/validation-summary.json)保存验收范围、实际结果和哈希。
