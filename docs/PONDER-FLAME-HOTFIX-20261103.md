# 热气球思索预览崩溃修复

完成：2026-11-03T01:05:30+08:00。现有Create263 Pack实例已安装；源码和本地证据已保留，尚未创建新提交或推送。未修改存档。

## 故障与原因

热气球方块思索演示曾触发客户端退出。崩溃为`Render Frame`，首个异常是`Render pass color attachment count must match pipeline color target state count.`，链路经过`PreparedRenderType.draw`和`PictureInPictureRenderer.prepare`。

航空学的`AeroRenderTypes.burnerFlamePipeline`使用空`RenderPipeline.builder()`，没有声明颜色输出。26.3会将它构建为零颜色目标，思索画中画却提供一个RGBA8颜色附件，设置管线时因此抛异常。热气球场景`aeronautics:envelope`播放至给燃烧器通电后，实际提交火焰，触发错误。报告中的高度及矿脉数据包均已启用；日志不支持将本次崩溃归因于存档损坏。

## 修改范围

仅修改[航空学渲染类型](../modules/create-aeronautics-adapted-263/aeronautics/src/main/java/dev/eriksonn/aeronautics/index/client/AeroRenderTypes.java)：导入`ColorTargetState`，在共享火焰管线factory补上`withColorTargetState(ColorTargetState.DEFAULT)`。

默认状态为一个RGBA8_UNORM颜色输出、无混合、RGBA全写入，与原火焰shader的location0输出及alpha裁切逻辑一致。普通火焰与Iris合成火焰共用此factory，两者都补齐声明。其余shader、深度状态、燃烧器逻辑、方块ID、数据格式和原版渲染校验保留。

新运行JAR与实际安装的旧航空学JAR逐ZIP条目内容比较，仅`dev/eriksonn/aeronautics/index/client/AeroRenderTypes.class`改变，无新增或删除条目。其他17个模组与原实例逐文件SHA-256相同。

## 实际验证

构建使用Java25、NeoForge26.3.0.22-beta及离线依赖；共享Gradle独占锁下执行`:aeronautics:jar`与探针`jar`，均成功。

| 对照 | 环境与结果 |
| --- | --- |
| 修复前 | 当前实例18个JAR原字节副本、项目新测试世界；两条火焰管线均为0颜色目标；热气球思索在已完成90tick采样后复现同一异常，进程异常退出，无超时 |
| 修复后 | 同一探针、同一18模组组合仅替换航空学JAR；两条火焰管线均为1颜色目标；热气球、热风燃烧器、蒸汽喷口3个思索场景共77个时间轴采样通过，实际GUI绘制、保存返回标题和退出码0通过 |
| 图像复核 | 人工查看热气球和燃烧器截图，火焰及橙/蓝两种外观可见 |
| 实例部署 | 旧JAR及实例标记在mods外备份；安装JAR哈希与受测候选一致，其他17JAR保持不变，不安装探针，不启动实例世界 |

探针每次推进约30个时间轴tick后等待10个客户端tick，让GUI实际渲染；这不是逐动画帧穷举。Iris未安装到该测试环境，因此只检查共用factory与两条实际管线的目标数量，不宣称着色器包实测通过。

新探针为[Java客户端探针](../tests/runtime-probe/src/main/java/dev/create263/probe/PipClientProbe.java)，仅在`create263.clientPipRegression`显式启用并核对测试目录标记后运行。运行脚本为[专项运行器](../scripts/test_pip_client.py)。所有世界和日志均在项目runtime中。

A/B正式证据：`runtime/pip-baseline-20261103-005908-852078/result.json`、`runtime/pip-candidate-20261103-010100-859519/result.json`。实际JAR、探针哈希及部署范围见[机器清单](../manifests/hotfixes/2026-11-03-ponder-flame.json)。两个正式运行除航空学JAR外，含探针在内的其他输入JAR哈希完全相同。

## 其他观察

部分上游思索模板加载时仍提示缺少`create:fluid_max_capacity`组件；本次A/B中均可观察到，修复版能完成保存退出。此项未纳入火焰崩溃修复，不将本次结论扩大为全部思索模板内容均正确。
