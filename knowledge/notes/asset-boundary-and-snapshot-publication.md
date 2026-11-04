# 按来源边界检查素材，再发布独立源码快照

- 类型：verified-method
- 日期：2026-11-04
- 证据：[来源与许可](../../docs/SOURCE-AND-LICENSES.md)、[发布记录](../../docs/RELEASE.md)、[快照来源](../../manifests/publication-source.json)

上游按源目录保留素材权利时，过滤必须覆盖该目录中的所有 namespace。仅匹配 assets/aeronautics 等主要名字会漏掉兼容目录中的 minecraft、amendments 和 musicnotification 资源。生成资源和嵌套依赖 JAR 也应核对。

根目录 .gitignore 不能保证子模块不重新纳入文件；更近的 !libs/*.jar 例外可覆盖它。取消跟踪后应核对 git ls-files、git check-ignore -v --no-index，以及实际归档成员。最终导出器应主动拒绝受限路径和旧依赖二进制。本项目规则见 [publication_policy.py](../../scripts/publication_policy.py) 和 [source_archive.py](../../scripts/source_archive.py)。

本轮取消跟踪8个跨namespace素材和2个旧参考JAR，全部本地原文件保留。安装器按资源锁排除1593目标，再从固定上游在用户本地还原；18JAR全部有效条目与受测组合相同。

清理当前树不会清理旧提交和旧附件。独立公开快照保留真实来源提交、树和逐文件hash，使用新的初始提交；旧私有库保留原历史。公开前比对53592个Git blob及文件模式，新增真实来源记录，未引入旧历史或保留权利素材。对原作者许可的保留与来源核验，不构成整包统一重新授权。
