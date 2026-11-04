# alpha.2热修复实例部署

部署时间：2026-11-02T18:12:06+08:00。

## 当前实例

HMCL实例名为 `Create263 Pack`，Minecraft26.3 / NeoForge26.3.0.22-beta / Java25。安装前18个直接JAR与最新发布alpha.2逐个整文件哈希一致，随后仅替换以下两份已通过新建/重开验证的JAR。

| 文件 | 安装后的SHA-256 |
| --- | --- |
| railways-adapted.jar | fe096f6e4eb05f4287c890f229dde00635fdc838039dea52f120ef27bb54f1aa |
| storage-adapted.jar | ad68e51ff9eec458d1c27e3eb562f8bc8a6a3285fb9bd3fed3fc58494d874efc |

其余16个JAR逐个与安装前哈希一致，直接模组总数仍为18，没有安装测试探针。安装前检查目标游戏进程和文件占用；两个旧JAR均在HMCL根目录的 `instance-backups/Create263-Pack/` 下备份，备份哈希与原文件匹配。实例根目录的 `.create263-hotfix.json` 记录当前修复标记；具体机器路径仅保留在本地忽略的部署记录。

## 启动与恢复

桌面入口为 `Create263（修复版HMCL）`，指向现有HMCL。打开后选择 `Create263 Pack` 启动。实例沿用原有加载器与配置；安装过程没有读取或启动任何原世界。

需要恢复时先关闭游戏，将上述两个JAR移出mods目录，并将备份的两个同名旧文件放回；其余模组及世界文件保持原样。

## 源码与验证

源码交付分支：`codex/railways-storage-hotfix-263`，以已发布alpha.2后的源码记录为基线。保留旧标签、发布附件与原有审查分支。

- [修复及双阶段验证](VENT-RENDER-HOTFIX-20261029.md)
- [受测候选清单](../manifests/hotfixes/2026-11-02-vent-storage.json)
- [安装前18JAR实例核验](../manifests/instance-audits/2026-11-02-alpha2.json)
- [安装后部署清单](../manifests/deployments/2026-11-02-alpha2-hotfix.json)

运行世界、日志、桌面快捷方式、备份、机器配置和AI协作记录不参与远程Git。安装不改变原有存档位置，也不将原世界用于验证。
