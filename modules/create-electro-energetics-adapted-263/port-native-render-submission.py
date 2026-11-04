from pathlib import Path
import re
r=Path(__file__).parent;s=r/'src/neoforge/java';p=s/'com/george_vi/electroenergetics/CEEPartialModels.java';t=p.read_text(encoding='utf-8');i=t.rfind('}')
t=t[:i]+'''
    /** Records actual emitted geometry so legacy model animation enters the native 26.3 submission path. */
    public static final class GeometryCollector implements net.createmod.catnip.impl.client.render.MultiBufferSource {
        private final net.minecraft.client.renderer.SubmitNodeCollector collector;
        private final java.util.Map<net.minecraft.client.renderer.rendertype.RenderType, RecordedVertices> buffers = new java.util.LinkedHashMap<>();
        public GeometryCollector(net.minecraft.client.renderer.SubmitNodeCollector collector) { this.collector=collector; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer getBuffer(net.minecraft.client.renderer.rendertype.RenderType type) {
            return buffers.computeIfAbsent(type, ignored -> new RecordedVertices());
        }
        public void submit() {
            buffers.forEach((type, vertices) -> {
                var frozen=java.util.List.copyOf(vertices.vertices);
                if (!frozen.isEmpty()) collector.submitCustomGeometry(new com.mojang.blaze3d.vertex.PoseStack(), type,
                    (pose, consumer) -> frozen.forEach(vertex -> vertex.emit(consumer)));
            });
        }
        public void text(com.mojang.blaze3d.vertex.PoseStack pose, String text, float x,float y,int color,int light) {
            collector.submitText(pose,x,y,net.minecraft.network.chat.Component.literal(text).getVisualOrderText(),true,
                net.minecraft.client.gui.Font.DisplayMode.NORMAL,light,color,0,0);
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
'''+t[i:];p.write_text(t,encoding='utf-8')
# Preserve every custom renderer's legacy model geometry and animation through explicit native extraction and submission.
changed=[]
for q in s.rglob('*Renderer.java'):
    text=q.read_text(encoding='utf-8');match=re.search(r'extends (SafeBlockEntityRenderer|SmartBlockEntityRenderer|KineticBlockEntityRenderer)<(\w+)>',text)
    if not match or 'void renderSafe(' not in text:continue
    base,be=match.groups();at=text.rfind('}')
    smart='        submitBehaviours(be, nativeState.partialTicks, pose, collector, state.lightCoords);\n' if base=='SmartBlockEntityRenderer' else ''
    text=text[:at]+f'''
    private static final class CEENativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {{
        {be} blockEntity;
        float partialTicks;
    }}
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() {{ return new CEENativeState(); }}
    @Override public void extractRenderState({be} be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float partialTicks, net.minecraft.world.phys.Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {{
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        CEENativeState nativeState=(CEENativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=partialTicks;
    }}
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {{
        if (!(state instanceof CEENativeState nativeState)) return;
        {be} be=nativeState.blockEntity;
        if (be==null || isInvalid(be)) return;
{smart}        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(collector);
        renderSafe(be,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }}
'''+text[at:];q.write_text(text,encoding='utf-8');changed.append(q.name)
p=s/'com/george_vi/electroenergetics/content/electrical_panel/attachments/PanelAttachment.java';t=p.read_text(encoding='utf-8');t=re.sub(r'        mc.font.drawInBatch\(label,.*?LightCoordsUtil.FULL_BRIGHT\);', '''        if (buffer instanceof com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector geometry)
            geometry.text(ms,label,-fullWidth-width/2f,0,0xffffffff,LightCoordsUtil.FULL_BRIGHT);''',t,flags=re.S);p.write_text(t,encoding='utf-8')
print('Native rendering paths:',len(changed),', '.join(changed))
