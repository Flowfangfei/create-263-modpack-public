package com.railwayteam.railways.internal.compat.client;

import net.createmod.catnip.api.client.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public abstract class WindowScreen extends AbstractSimiScreen {
    protected int guiLeft,guiTop,windowWidth,windowHeight;
    protected WindowScreen(Component title) { super(title); }
    protected void setWindowSize(int width,int height) { windowWidth=width;windowHeight=height;guiLeft=(this.width-width)/2;guiTop=(this.height-height)/2; }
    protected void renderWindowBackground(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float tick) { }
    protected void renderWindow(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float tick) { }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float tick) {
        renderWindowBackground(graphics,mouseX,mouseY,tick);renderWindow(graphics,mouseX,mouseY,tick);
        super.extractRenderState(graphics,mouseX,mouseY,tick);
    }
}
