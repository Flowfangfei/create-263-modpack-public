# 运行迁移与数据生成

当前迁移以26.3上游已经生成的 assets、配方、标签、战利品表和教程资源为基础。构建始终包含 src/main/resources 和 src/generated/resources；不会运行空的数据生成器或清理已生成资源。

旧版NeoForge参照中的数据生成接口和当前固定Create/Registrate开发依赖并不一致。运行注册代码中的 recipe、blockstate、model 回调暂从运行构建中分离，原始代码逐项保存在 deferred-datagen-callbacks.json，来源固定为上游943753a提交。功能数据仍由已保存的26.3资源提供。新增内容时应同时修改JSON；自动重新生成这些资源尚待迁移。

26.3区块渲染层由模型材质和纹理透明度决定，原addLayer不是运行注册所必需的。两种levitite已生成模型中的force_translucent声明保留并检查；其他cutout模型使用纹理透明度。

运行构建与玩法尚在迁移中。分离数据生成回调不构成玩法通过的证据，仍须核对每个注册内容的模型、物品定义、配方和行为。
