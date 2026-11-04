from pathlib import Path
import re
r=Path(__file__).parent;src=r/'src/neoforge/java'
for p in src.rglob('*.java'):
    t=p.read_text(encoding='utf-8');old=t
    if 'RenderType.' in t:
        t=re.sub(r'\bRenderType\.(solid|cutout|translucent)\(\)',lambda m:'RenderTypes.'+{'solid':'solidMovingBlock','cutout':'cutoutMovingBlock','translucent':'translucentMovingBlock'}[m[1]]+'()',t)
        t=re.sub(r'\bRenderType\.(cutoutMipped)\(\)','RenderTypes.cutoutMovingBlock()',t)
        t=re.sub(r'\bRenderType\.(entity\w+|lines|lightning|leash|debug\w+)\(',r'RenderTypes.\1(',t)
        if 'RenderTypes.' in t and 'import net.minecraft.client.renderer.rendertype.RenderTypes;' not in t:
            t=t.replace('import net.minecraft.client.renderer.rendertype.RenderType;','import net.minecraft.client.renderer.rendertype.RenderType;\nimport net.minecraft.client.renderer.rendertype.RenderTypes;')
    if t!=old:p.write_text(t,encoding='utf-8')
p=src/'com/george_vi/electroenergetics/events/ModEvents.java';t=p.read_text(encoding='utf-8')
t=re.sub(r'    @SubscribeEvent\n    @OnlyIn\(Dist.CLIENT\)\n    public static void onModelRegistry\(ModelEvent.RegisterAdditional event\) \{.*?\n    }\n','',t,flags=re.S)
t=re.sub(r'^import net.minecraft.client.(renderer.ItemBlockRenderTypes|resources.model.ModelIdentifier);\n','',t,flags=re.M)
t=re.sub(r'^        ItemBlockRenderTypes.setRenderLayer\([^\n]+\);\n','',t,flags=re.M)
p.write_text(t,encoding='utf-8')
