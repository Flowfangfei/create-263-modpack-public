---
id: create263-kn-002
kind: verified-method
status: loading-verified-gameplay-partial
---
# 另一模组重写方法时，按实际方法体设计兼容注入

Sable 重写了 PlayerList 的广播方法，用子空间距离替代三个坐标差。铁道针对原版 getX/getY/getZ 的注入因此失效。这是联合加载冲突，单模块编译不能发现。

本工程通过模组存在性条件选择专用桥接，在 Sable 方法体中的 position 调用处包装返回值，保留原距离算法并引入车掌相机位置。注入优先级必须满足重写方法的约束；不能通过简单降低 require 数量来宣称功能完好。

证据：[联合验证及未测范围](../../docs/VALIDATION.md)、[兼容实现](../../modules/steam-n-rails-263/fabric/src/main/java/com/railwayteam/railways/fabric_mixin/conductor_possession/PlayerListSableMixin.java)。
