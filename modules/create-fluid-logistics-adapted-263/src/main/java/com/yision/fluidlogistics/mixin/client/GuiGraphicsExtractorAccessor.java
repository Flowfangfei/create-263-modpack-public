package com.yision.fluidlogistics.mixin.client;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(net.minecraft.client.gui.GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {
    @Accessor("guiRenderState") net.minecraft.client.renderer.state.gui.GuiRenderState fluidlogistics$guiRenderState();
    @Accessor("mouseX") int fluidlogistics$mouseX();
    @Accessor("mouseY") int fluidlogistics$mouseY();
}
