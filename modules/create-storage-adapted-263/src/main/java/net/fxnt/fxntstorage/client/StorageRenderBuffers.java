package net.fxnt.fxntstorage.client;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

/** Real PartialModel and vertex submission bridge, shared from the Electro 26.3 port. */
public final class StorageRenderBuffers {
    private StorageRenderBuffers() {}
    public static final class Bufferer {
        private static final java.util.Map<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart,
                java.util.Map<net.minecraft.world.level.block.state.BlockState,
                    net.createmod.catnip.api.client.render.SuperByteBuffer>> CACHE = new java.util.WeakHashMap<>();

        public static ModelBuffer partial(PartialModel partial, net.minecraft.world.level.block.state.BlockState state) {
            var part = java.util.Objects.requireNonNull(partial.get(), "Unbaked partial: " + partial.modelLocation());
            var states = CACHE.computeIfAbsent(part, ignored -> new java.util.HashMap<>());
            var buffer = states.computeIfAbsent(state, ignored ->
                net.createmod.catnip.api.client.render.SuperBufferFactory.getInstance()
                    .createForBlock(new net.minecraft.client.renderer.block.dispatch.SingleVariant(part), state));
            return new ModelBuffer(buffer.reset());
        }

        public static ModelBuffer partialFacing(PartialModel partial, net.minecraft.world.level.block.state.BlockState state) {
            var facing = state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)
                ? state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)
                : state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
            return partialFacing(partial, state, facing);
        }

        public static ModelBuffer partialFacing(PartialModel partial, net.minecraft.world.level.block.state.BlockState state,
                                                net.minecraft.core.Direction facing) {
            return partial(partial, state).transform(net.createmod.catnip.api.client.render.CachedBuffers.rotateToFace(facing).get());
        }

        public static ModelBuffer block(net.minecraft.world.level.block.state.BlockState state) {
            return new ModelBuffer(net.createmod.catnip.api.client.render.CachedBuffers.block(state).reset());
        }
    }

    public static final class ModelBuffer implements net.createmod.catnip.api.client.render.SuperByteBuffer,
            dev.engine_room.flywheel.lib.transform.Transform<ModelBuffer> {
        private final net.createmod.catnip.api.client.render.SuperByteBuffer delegate;
        ModelBuffer(net.createmod.catnip.api.client.render.SuperByteBuffer delegate) { this.delegate = delegate; }
        public static int maxLight(int first, int second) {
            return net.createmod.catnip.api.client.render.SuperByteBuffer.maxLight(first, second);
        }
        @Override public void renderInto(com.mojang.blaze3d.vertex.PoseStack pose, com.mojang.blaze3d.vertex.VertexConsumer consumer) { delegate.renderInto(pose, consumer); }
        @Override public boolean isEmpty() { return delegate.isEmpty(); }
        @Override public com.mojang.blaze3d.vertex.PoseStack getTransforms() { return delegate.getTransforms(); }
        @Override public ModelBuffer reset() { delegate.reset(); return this; }
        @Override public ModelBuffer color(int color) { delegate.color(color); return this; }
        @Override public ModelBuffer color(int red, int green, int blue, int alpha) { delegate.color(red,green,blue,alpha); return this; }
        @Override public ModelBuffer color(net.createmod.catnip.api.theme.Color color) { delegate.color(color); return this; }
        @Override public ModelBuffer disableDiffuse() { delegate.disableDiffuse(); return this; }
        @Override public ModelBuffer shiftUV(net.createmod.catnip.api.client.render.SpriteShiftEntry entry) { delegate.shiftUV(entry); return this; }
        @Override public ModelBuffer shiftUVScrolling(net.createmod.catnip.api.client.render.SpriteShiftEntry entry, float u, float v) { delegate.shiftUVScrolling(entry,u,v); return this; }
        @Override public ModelBuffer shiftUVtoSheet(net.createmod.catnip.api.client.render.SpriteShiftEntry entry, float u, float v, int size) { delegate.shiftUVtoSheet(entry,u,v,size); return this; }
        @Override public ModelBuffer overlay(int overlay) { delegate.overlay(overlay); return this; }
        @Override public ModelBuffer light(int light) { delegate.light(light); return this; }
        public ModelBuffer useLevelLight(net.minecraft.world.level.Level level) { return useLevelLight((net.minecraft.client.renderer.block.BlockAndTintGetter)level); }
        public ModelBuffer useLevelLight(net.minecraft.world.level.Level level, org.joml.Matrix4f matrix) { return useLevelLight((net.minecraft.client.renderer.block.BlockAndTintGetter)level,matrix); }
        @Override public ModelBuffer useLevelLight(net.minecraft.client.renderer.block.BlockAndTintGetter level) { delegate.useLevelLight(level); return this; }
        @Override public ModelBuffer useLevelLight(net.minecraft.client.renderer.block.BlockAndTintGetter level, org.joml.Matrix4f matrix) { delegate.useLevelLight(level,matrix); return this; }
        @Override public ModelBuffer mulPose(org.joml.Matrix4fc pose) { getTransforms().last().pose().mul(pose); return this; }
        @Override public ModelBuffer mulNormal(org.joml.Matrix3fc normal) { getTransforms().last().normal().mul(normal); return this; }
        @Override public ModelBuffer translate(float x,float y,float z) { getTransforms().translate(x,y,z); return this; }
        @Override public ModelBuffer rotate(org.joml.Quaternionfc rotation) { getTransforms().rotate(rotation); return this; }
        @Override public ModelBuffer scale(float x,float y,float z) { getTransforms().scale(x,y,z); return this; }
        @Override public void delete() { delegate.delete(); }
    }

    /** Records actual emitted geometry so legacy model animation enters the native 26.3 submission path. */
    public static final class GeometryCollector implements net.createmod.catnip.impl.client.render.MultiBufferSource {
        private final net.minecraft.client.renderer.SubmitNodeCollector collector;
        private final java.util.List<java.util.function.Consumer<net.minecraft.client.renderer.SubmitNodeCollector>> deferred=new java.util.ArrayList<>();
        public GeometryCollector() { this.collector=null; }
        private final java.util.Map<net.minecraft.client.renderer.rendertype.RenderType, RecordedVertices> buffers = new java.util.LinkedHashMap<>();
        public GeometryCollector(net.minecraft.client.renderer.SubmitNodeCollector collector) { this.collector=collector; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer getBuffer(net.minecraft.client.renderer.rendertype.RenderType type) {
            return buffers.computeIfAbsent(type, ignored -> new RecordedVertices());
        }
        public void submit() { submit(java.util.Objects.requireNonNull(collector)); }
        public void submit(net.minecraft.client.renderer.SubmitNodeCollector collector) {
            deferred.forEach(action -> action.accept(collector));
            buffers.forEach((type, vertices) -> {
                var frozen=java.util.List.copyOf(vertices.vertices);
                if (!frozen.isEmpty()) collector.submitCustomGeometry(new com.mojang.blaze3d.vertex.PoseStack(), type,
                    (pose, consumer) -> frozen.forEach(vertex -> vertex.emit(consumer)));
            });
        }
        public void defer(com.mojang.blaze3d.vertex.PoseStack pose, java.util.function.BiConsumer<com.mojang.blaze3d.vertex.PoseStack,net.minecraft.client.renderer.SubmitNodeCollector> action) {
            var snapshot=pose.last().copy();
            deferred.add(queue -> { var matrices=new com.mojang.blaze3d.vertex.PoseStack(); matrices.last().set(snapshot); action.accept(matrices,queue); });
        }
        public void text(com.mojang.blaze3d.vertex.PoseStack pose, String text, float x,float y,int color,int light) {
            text(pose,net.minecraft.network.chat.Component.literal(text),x,y,color,light);
        }
        public void text(com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.network.chat.Component text,float x,float y,int color,int light) {
            defer(pose,(matrices,queue)->queue.submitText(matrices,x,y,text.getVisualOrderText(),false,
                net.minecraft.client.gui.Font.DisplayMode.NORMAL,light,color,0,0));
        }
    }

    private static final class RecordedVertex {
        float x,y,z,u,v,uv3u,uv3v,nx,ny,nz,lineWidth=1;
        int red=255,green=255,blue=255,alpha=255,overlayU,overlayV,lightU,lightV;
        void emit(com.mojang.blaze3d.vertex.VertexConsumer target) {
            target.addVertex(x,y,z).setColor(red,green,blue,alpha).setUv(u,v)
                .setUv1(overlayU,overlayV).setUv2(lightU,lightV).setUv3(uv3u,uv3v)
                .setNormal(nx,ny,nz).setLineWidth(lineWidth);
        }
    }

    private static final class RecordedVertices implements com.mojang.blaze3d.vertex.VertexConsumer {
        final java.util.List<RecordedVertex> vertices=new java.util.ArrayList<>();
        RecordedVertex current;
        private RecordedVertex vertex() { return java.util.Objects.requireNonNull(current,"Vertex attributes precede position"); }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer addVertex(float x,float y,float z) {
            current=new RecordedVertex();current.x=x;current.y=y;current.z=z;vertices.add(current);return this;
        }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setColor(int r,int g,int b,int a) { var v=vertex();v.red=r;v.green=g;v.blue=b;v.alpha=a;return this; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setColor(int color) { return setColor((color>>>16)&255,(color>>>8)&255,color&255,(color>>>24)&255); }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setUv(float u,float v) { vertex().u=u;vertex().v=v;return this; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setUv1(int u,int v) { vertex().overlayU=u;vertex().overlayV=v;return this; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setUv2(int u,int v) { vertex().lightU=u;vertex().lightV=v;return this; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setUv3(float u,float v) { vertex().uv3u=u;vertex().uv3v=v;return this; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setNormal(float x,float y,float z) { vertex().nx=x;vertex().ny=y;vertex().nz=z;return this; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setLineWidth(float width) { vertex().lineWidth=width;return this; }
    }
}
