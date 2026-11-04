# 0.3.0-alpha.3 — 公开实验性发布 / Public experimental release

发布页：[v0.3.0-alpha.3](https://github.com/Flowfangfei/create-263-modpack-public/releases/tag/v0.3.0-alpha.3)。发布时间：`2026-11-04T14:15:51Z`。状态为 public prerelease，非 draft。

新公开仓库使用独立初始提交 `49a7bb8e6d909a2aaa3bb01acade4c8c0cb4fe34`，树为 `f7f34e67e60036435536e85709a592db2d8f2ed9`。真实导入来源见[快照记录](../manifests/publication-source.json)。原私有仓库的 [PR #4](https://github.com/Flowfangfei/create-263-modpack/pull/4) 已合并到 main（`cef7df2a663127d3311ab36e67f653cba00ddfbe`）；该仓库、旧分支、标签及旧附件继续保持私有，未导入本公开 Git 历史。

| 附件 / Asset | 字节 / Bytes | SHA-256 |
| --- | ---: | --- |
| [Create263-Pack-v0.3.0-alpha.3-installer.zip](https://github.com/Flowfangfei/create-263-modpack-public/releases/download/v0.3.0-alpha.3/Create263-Pack-v0.3.0-alpha.3-installer.zip) | 57813731 | `7bc3ed9c60e52b9fc7d56b61e494604519e7c3287e06f35266409b4cc73ac77f` |
| [Create263-Pack-v0.3.0-alpha.3-sources.zip](https://github.com/Flowfangfei/create-263-modpack-public/releases/download/v0.3.0-alpha.3/Create263-Pack-v0.3.0-alpha.3-sources.zip) | 73910426 | `8a16f89462a299cb195950310b7f936f66f634882914cdd0e80ca573394a4011` |
| [SHA256SUMS.txt](https://github.com/Flowfangfei/create-263-modpack-public/releases/download/v0.3.0-alpha.3/SHA256SUMS.txt) | 218 | `82ec436db87a71c5c16093c651f337dacbe63c29e8bdb324ffa5d88d21114b77` |

三个附件的 GitHub 服务端摘要、大小及 uploaded 状态与本地最终文件一致。SHA256SUMS.txt 已重新下载且 hash 一致；完整安装器在当前网络未收到数据，限时公开请求以连接超时结束（exit 28），因此不声称已完成完整附件回下载核验。读取代理配置的尝试被自动审批拒绝，未读取配置或更改网络设置。具体范围见[机器发布清单](../manifests/releases/v0.3.0-alpha.3.json)。

最终安装器 ZIP 在全新目录解压后，经原生 Windows PowerShell 5.1 实际执行通过。还原的 18 个 JAR 全部非目录条目与已完成客户端验证的组合一致；1593 个航空学锁定素材目标在负载中排除、还原后 hash 正确。官方 Create 素材和 Diesel 单独授权音效校验通过；固定上游输入来自已核验 SHA-256 的缓存。完整 HMCL 包仅在本地生成，不作为 GitHub 附件镜像。

源码 ZIP 从固定提交按 Git 规范字节导出，15 模块 53,363 文件逐项核对通过。公开树隐私审计覆盖 53,593 个跟踪路径，阻断和待审项均为 0；另确认全部 53,592 个导入 Git blob 与文件模式等同已审计来源树，仅新增真实快照记录。上游原有空白格式和署名保持，当前任务修改在私有合并前通过差异检查。

思索验证完成 389 场景、3458 采样点、38592 帧，资源重载后另验 12 场景；这仍是部分测试，快进采样不等于全程自然播放或完整玩法验收。后续采用同模组同实际逻辑的代表样本，并保留受影响组与少量跨模组检查。多人、长期负载、移动子空间和旧 Storage 自动迁移等仍有待测项。原存档未访问，本轮发布未重复游戏回归。

中英 README、原创 SVG、链接和版本已检查，GitHub 内容 API 返回的 README/英文版/架构图 Git blob 与本地一致。内置浏览器两次超时，GitHub 网页排版未完成预览；SVG 已离线渲染并目视核验。

The public release contains an independent audited source snapshot, a license-aware installer, and SHA-256 sums. Native Windows installation restored all 18 runtime JARs byte-for-byte at entry level. Component licenses and original credits remain applicable; see [English source and license notes](SOURCE-AND-LICENSES.en.md) and the [upgrade/testing guide](MOD-UPGRADE-AND-TESTING.en.md). Testing is partial. The original private repository, historical refs and old release files are preserved separately.

---

> 2026-11-04 校正：下列旧版本记录中“附件不含受限素材”的结论不完整。新审计发现旧源码 ZIP、Git 历史及安装器中有遗漏的跨命名空间航空学素材，旧依赖 JAR 也含受限美术。旧记录作为历史证据保留，相关旧仓库和附件继续保持私有；详见[来源与许可](SOURCE-AND-LICENSES.md)。新版本的分发核验单独记录。

# 0.3.0-alpha.2 发布记录

私有预发布：[v0.3.0-alpha.2](https://github.com/Flowfangfei/create-263-modpack/releases/tag/v0.3.0-alpha.2)；审查：[草稿PR #3](https://github.com/Flowfangfei/create-263-modpack/pull/3)，基于 `codex/five-addons-263`。

- 源码提交：`35cb63c63412f2bef62f6fd37afe77977235fbc6`；Git tree：`fe52ae5ae0cdfa5b4829864f38f2f471916f56b1`。
- 标签固定在上述受测源码提交；交付文档在后续提交补齐，不移动标签。
- GitHub发布时间：`2026-10-28T06:03:05Z`；远程核验：`2026-10-28T14:04:40+08:00`。
- 状态：私有仓库中的prerelease，非draft；PR仍为草稿，未合并，默认main与旧分支保留。

| 附件 | 字节数 | SHA-256 |
| --- | ---: | --- |
| [Create263-Pack-v0.3.0-alpha.2-installer.zip](https://github.com/Flowfangfei/create-263-modpack/releases/download/v0.3.0-alpha.2/Create263-Pack-v0.3.0-alpha.2-installer.zip) | 57720580 | `135e92e6decd2f1e0cca2eedce03f9fa3ddad5404b05c4d31f1da8685b06e967` |
| [Create263-Pack-v0.3.0-alpha.2-sources.zip](https://github.com/Flowfangfei/create-263-modpack/releases/download/v0.3.0-alpha.2/Create263-Pack-v0.3.0-alpha.2-sources.zip) | 101453024 | `4077af6f2edd922329a6178f702ac2e2fecc0e4ef4b44f650574cc80b5f870c4` |
| [SHA256SUMS.txt](https://github.com/Flowfangfei/create-263-modpack/releases/download/v0.3.0-alpha.2/SHA256SUMS.txt) | 218 | `dcba0178e37327715ffc004a185a0efbdac5d461a456cf918febef8de228fef1` |

三个附件的GitHub摘要、大小及上传状态与本地一致；安装器和SHA256SUMS已重新下载并校验。新分支、固定标签、提交tree与草稿PR头均已核对；alpha.1的标签和全部三个附件摘要/大小保持原样。机器记录见[本版发布清单](../manifests/releases/v0.3.0-alpha.2.json)。

本版恢复Electro电路模拟生命周期。受测18JAR仅一个Mixin类与alpha.1不同；深入43项、两次正常停服、原有四组双阶段回归和客户端489物品/实际菜单/保存退出均通过。原生Windows PowerShell5.1实际安装及最终ZIP全新解压安装均exit0，生成18个JAR所有非目录条目与受测候选一致。完整结果和范围见[主验证](VALIDATION.md)及[深入验证](GAMEPLAY-VALIDATION-26.3.md)。

源码ZIP从固定提交按Git规范字节导出，15模块53,365文件逐项验证通过。附件不包含世界、运行日志、截图、个人配置、协作记录或保留权利素材；受限资源从固定上游在本地恢复。完整HMCL导入包只在本地生成，未镜像到GitHub附件。原实例与原存档未修改。

alpha.2仍是实验性版本。多人长期负载、其他发电/加工模式、移动子空间与完整列车/物流网络、未安装可选联动及旧Storage自动迁移仍待专项验收。

---

# 0.3.0-alpha.1 发布记录

私有预发布：[v0.3.0-alpha.1](https://github.com/Flowfangfei/create-263-modpack/releases/tag/v0.3.0-alpha.1)；审查：[草稿PR #2](https://github.com/Flowfangfei/create-263-modpack/pull/2)，基于 `codex/three-addons-263`。

- 状态：私有仓库中的 prerelease，非 draft；默认分支、旧分支和旧发布保留，未合并PR。
- 源码提交：`203897ef96ebe398787376ba1372fcd915286748`；Git tree：`9b460a0f1aeb185628cf8d3f58c7a5e643745a15`。
- 标签 `v0.3.0-alpha.1` 固定在上述源码提交；交付记录在后续文档提交中补齐，不移动标签。
- GitHub发布时间：`2026-10-26T16:51:55Z`；远程核验：`2026-10-27T00:53:15+08:00`。
- 平台：Minecraft26.3 / NeoForge26.3.0.22-beta / Java25；18个直接运行JAR、15个源码模块。

| 附件 | 字节数 | SHA-256 |
| --- | ---: | --- |
| [Create263-Pack-v0.3.0-alpha.1-installer.zip](https://github.com/Flowfangfei/create-263-modpack/releases/download/v0.3.0-alpha.1/Create263-Pack-v0.3.0-alpha.1-installer.zip) | 57702850 | `da0b8d457c95604269678c54ce25de6cb0e2151d0c170f01938985ff303a2443` |
| [Create263-Pack-v0.3.0-alpha.1-sources.zip](https://github.com/Flowfangfei/create-263-modpack/releases/download/v0.3.0-alpha.1/Create263-Pack-v0.3.0-alpha.1-sources.zip) | 101394016 | `26ccb089a80047769ba2ea032b990606fee64c16602511175aa311c726706c37` |
| [SHA256SUMS.txt](https://github.com/Flowfangfei/create-263-modpack/releases/download/v0.3.0-alpha.1/SHA256SUMS.txt) | 218 | `2c3ad1ed8551d7e0fe6dbbf6e86aba27be170e9dab408574c0196f7f30ff4928` |

远程分支、标签对应提交和源码tree已核对；三个附件的GitHub摘要、大小与上传状态匹配。安装器与SHA256SUMS已实际重新下载并校验。机器可读记录见[发布清单](../manifests/releases/v0.3.0-alpha.1.json)。

源码ZIP按Git规范字节导出，53,365个模块文件逐项SHA-256核对通过。安装器从固定上游校验恢复资源，在本地生成HMCL导入包；原生Windows PowerShell5.1实际执行通过，生成18个JAR的所有非目录条目与最终运行包相同。GitHub附件和源码库不含保留权利素材、完整HMCL包、世界、运行日志或AI协作记录。

新建世界与独立JVM重启各37/37、四组探针通过；最终客户端489物品/十页展示、真实仓储点击、压缩配方请求/回复、Ponder、设置及保存退出通过。Storage原版81格箱子跨JVM保存重读通过，私有库存1024→960＋光标64→1024守恒。全部测试使用项目新世界与其校验副本，原HMCL存档未访问。

该版本为alpha；复杂生产线、挂载控制、完整附魔加工、多人/长期负载及未安装可选联动仍未完成专项验收，旧版Storage库存自动迁移未验证。详见[验证范围](VALIDATION.md)及[TODO](../TODO.md)。

---

# 0.1.0-alpha.1 发布记录

私有仓库：[Flowfangfei/create-263-modpack](https://github.com/Flowfangfei/create-263-modpack)。发布页：[v0.1.0-alpha.1](https://github.com/Flowfangfei/create-263-modpack/releases/tag/v0.1.0-alpha.1)。

- 发布状态：GitHub prerelease，非 draft。
- 对应源码提交：`9e2aecea626429db470d006ff7bb3433441cd7b4`。
- GitHub 发布时间：`2026-10-24T16:44:53Z`。
- 远程核验时间：`2026-10-25T00:50:28+08:00`。
- 平台：Minecraft 26.3、NeoForge 26.3.0.22-beta、Java 25。

| 附件 | 字节数 | SHA-256 |
| --- | ---: | --- |
| [Create263-Pack-v0.1.0-alpha.1-installer.zip](https://github.com/Flowfangfei/create-263-modpack/releases/download/v0.1.0-alpha.1/Create263-Pack-v0.1.0-alpha.1-installer.zip) | 45026470 | `91ebddf20926fb81088dd80975d0b47635e317aea522debc3aee748f47be1937` |
| [Create263-Pack-v0.1.0-alpha.1-source.zip](https://github.com/Flowfangfei/create-263-modpack/releases/download/v0.1.0-alpha.1/Create263-Pack-v0.1.0-alpha.1-source.zip) | 82225487 | `f9b30cce4b66fa5f5ae47f9a29726a1fe5865f67b02afe1e971f8d5e528bd775` |
| [SHA256SUMS.txt](https://github.com/Flowfangfei/create-263-modpack/releases/download/v0.1.0-alpha.1/SHA256SUMS.txt) | 219 | `5bc56631526335f0e91de42b04a069472d7f64db2d337dab79f9b8f1bbcdc192` |

远程分支提交及 Git tree SHA 与本地一致；六个源码目录和 Registrate 对应源码已核验。三个附件的 GitHub 摘要、大小和上传状态均匹配；安装器与 SHA256SUMS 已实际重新下载并校验。

源码档案按 Git 规范化文本内容导出，40,364 个模块文件与源码清单逐个相符，另有 68 个 Registrate 源码/许可文件。安装器附件不含保留权利素材；它从固定上游来源校验下载，在本机生成 HMCL 完整导入包。生成包的九个 JAR 与验证运行包逐条目一致。

运行检查与待测玩法见 [VALIDATION.md](VALIDATION.md)。原存档未作为测试输入，测试世界、日志、截图和机器配置保存在本机忽略目录；协作记录没有上传。
