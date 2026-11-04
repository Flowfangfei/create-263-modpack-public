package com.yision.fluidlogistics.client.gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
public abstract class FluidSimiScreen extends net.createmod.catnip.api.client.gui.AbstractSimiScreen {
    protected int guiLeft, guiTop, windowWidth, windowHeight, windowOffsetX, windowOffsetY;
    protected FluidSimiScreen(Component title) { super(title); }
    protected FluidSimiScreen() { this(Component.empty()); }
    protected void setWindowSize(int width, int height) { windowWidth = width; windowHeight = height; }
    protected void setWindowOffset(int x, int y) { windowOffsetX = x; windowOffsetY = y; }
    protected void init() { super.init(); guiLeft = (width - windowWidth) / 2 + windowOffsetX; guiTop = (height - windowHeight) / 2 + windowOffsetY; }
    protected abstract void renderWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks);
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        renderWindow(graphics, mouseX, mouseY, partialTicks); super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }
}
