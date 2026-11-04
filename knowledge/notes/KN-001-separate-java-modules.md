---
id: create263-kn-001
kind: verified-method
status: verified-for-current-baseline
---
# 分离的模组 JAR 应使用独立 Java 包

FML 12 基于 Java 模块系统加载模组。主模组与可选桥接放在不同 JAR 时，共享的 Java 包会触发 split-package 错误；编译成功不能排除此问题。

本工程将 Connected 桥接移动至独立包，更新其反射入口、渲染/模型回调和 Mixin 配置，并仅将确有跨模块使用需求的拆除方法作为公共接口。打包工具现在检查直接模组之间的重复包。

证据：[联合验证](../../docs/VALIDATION.md)、[打包检查实现](../../scripts/package.py)。
