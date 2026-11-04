from pathlib import Path
import re
r=Path(__file__).parent;src=r/'src/neoforge/java'
for p in src.rglob('*.java'):
    t=p.read_text(encoding='utf-8'); orig=t
    t=t.replace('net.minecraft.client.renderer.MultiBufferSource','net.createmod.catnip.impl.client.render.MultiBufferSource')
    t=t.replace('net.minecraft.client.renderer.LightTexture','net.minecraft.util.LightCoordsUtil').replace('LightTexture.','LightCoordsUtil.')
    t=t.replace('net.minecraft.world.entity.MobSpawnType','net.minecraft.world.entity.EntitySpawnReason').replace('MobSpawnType.','EntitySpawnReason.')
    t=re.sub(r'\bUseAnim\b','ItemUseAnimation',t)
    t=t.replace('import net.minecraft.world.level.block.state.properties.DirectionProperty;','import net.minecraft.world.level.block.state.properties.EnumProperty;')
    t=re.sub(r'\bDirectionProperty\b','EnumProperty<Direction>',t)
    t=t.replace('ItemInteractionResult','InteractionResult').replace('InteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION','InteractionResult.TRY_WITH_EMPTY_HAND').replace('InteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION','InteractionResult.PASS')
    t=t.replace('import net.minecraft.world.InteractionResultHolder;','import net.minecraft.world.InteractionResult;')
    t=t.replace('InteractionResultHolder<ItemStack>','InteractionResult')
    t=re.sub(r'InteractionResultHolder\.pass\([^;\n]+\)','InteractionResult.PASS',t)
    # The old holder factories carried the replacement stack; the new Success result carries it directly.
    t=re.sub(r'InteractionResultHolder\.success\(([^;\n]+)\)',r'InteractionResult.SUCCESS.heldItemTransformedTo(\1)',t)
    t=t.replace('import net.minecraft.client.gui.GuiGraphics;','import net.minecraft.client.gui.GuiGraphicsExtractor;').replace('GuiGraphics ', 'GuiGraphicsExtractor ')
    t=t.replace('import net.minecraft.client.gui.LayeredDraw;','import net.neoforged.neoforge.client.gui.GuiLayer;').replace('implements LayeredDraw.Layer','implements GuiLayer')
    t=re.sub(r'^import net.minecraft.MethodsReturnNonnullByDefault;\n','',t,flags=re.M).replace('@MethodsReturnNonnullByDefault\n','')
    # Avoid duplicate imports after merging ItemInteractionResult into InteractionResult.
    seen=set();lines=[]
    for line in t.splitlines():
        if line.startswith('import '):
            if line in seen:continue
            seen.add(line)
        lines.append(line)
    t='\n'.join(lines)+'\n'
    if t!=orig:p.write_text(t,encoding='utf-8')
p=src/'com/george_vi/electroenergetics/events/datagen/SimpleCEETrigger.java';t=p.read_text(encoding='utf-8')
t=t.replace('import net.minecraft.advancements.predicates.ContextAwarePredicate;','import net.minecraft.core.Holder;\nimport net.minecraft.world.level.storage.loot.predicates.LootItemCondition;').replace('EntityPredicate.ADVANCEMENT_CODEC','LootItemCondition.CODEC').replace('Optional<ContextAwarePredicate>','Optional<Holder<LootItemCondition>>')
p.write_text(t,encoding='utf-8')
