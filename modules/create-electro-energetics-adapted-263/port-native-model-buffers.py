from pathlib import Path
r=Path(__file__).parent;s=r/'src/neoforge/java';p=s/'com/george_vi/electroenergetics/CEEPartialModels.java';t=p.read_text(encoding='utf-8');i=t.rfind('}')
t=t[:i]+'''
    /** Adapts the upstream partial-model rendering to Catnip's real 26.3 mesh builder. */
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
        @Override public ModelBuffer useLevelLight(net.minecraft.client.renderer.block.BlockAndTintGetter level) { delegate.useLevelLight(level); return this; }
        @Override public ModelBuffer useLevelLight(net.minecraft.client.renderer.block.BlockAndTintGetter level, org.joml.Matrix4f matrix) { delegate.useLevelLight(level,matrix); return this; }
        @Override public ModelBuffer mulPose(org.joml.Matrix4fc pose) { getTransforms().last().pose().mul(pose); return this; }
        @Override public ModelBuffer mulNormal(org.joml.Matrix3fc normal) { getTransforms().last().normal().mul(normal); return this; }
        @Override public ModelBuffer translate(float x,float y,float z) { getTransforms().translate(x,y,z); return this; }
        @Override public ModelBuffer rotate(org.joml.Quaternionfc rotation) { getTransforms().rotate(rotation); return this; }
        @Override public ModelBuffer scale(float x,float y,float z) { getTransforms().scale(x,y,z); return this; }
        @Override public void delete() { delegate.delete(); }
    }
'''+t[i:];p.write_text(t,encoding='utf-8')
for q in s.rglob('*.java'):
    if q==p:continue
    t=q.read_text(encoding='utf-8');old=t
    t=t.replace('import net.createmod.catnip.api.client.render.CachedBuffers;','import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;').replace('CachedBuffers.', 'Bufferer.')
    t=t.replace('import net.createmod.catnip.api.client.render.SuperByteBuffer;','import com.george_vi.electroenergetics.CEEPartialModels.ModelBuffer;')
    import re
    t=re.sub(r'\bSuperByteBuffer\b','ModelBuffer',t)
    if t!=old:q.write_text(t,encoding='utf-8')
