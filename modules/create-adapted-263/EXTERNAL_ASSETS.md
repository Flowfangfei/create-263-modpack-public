# 外置 Create 官方素材

本模块的 `buildRecoveredRelease` 输出不包含原版 Create 美术资源。客户端需要固定版本 `create-1.21.1-6.0.10.jar`，存放于实例的 `config/create-adapted/external-assets/`，作为资源包读取，不能放入 `mods/`。

开发环境执行根目录 `python scripts/bootstrap.py` 取得本地构建/测试所需资源；最终用户运行发布安装器即可将同一份经哈希校验的原版资源放入正确位置。来源、哈希和许可见根目录 [资源锁](../../manifests/resources-lock.json) 和 [许可说明](../../THIRD_PARTY_NOTICES.md)。本工程不镜像该原版 JAR。
