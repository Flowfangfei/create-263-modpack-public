package dev.simulated_team.simulated.compat.create;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
public abstract class AbstractSimiScreen extends net.createmod.catnip.api.client.gui.AbstractSimiScreen {
    protected int guiLeft,guiTop,windowWidth,windowHeight,windowXOffset,windowYOffset;
    protected AbstractSimiScreen(){super(Component.empty());}
    protected AbstractSimiScreen(Component title){super(title);}
    protected void setWindowSize(int width,int height){windowWidth=width;windowHeight=height;}
    protected void setWindowOffset(int x,int y){windowXOffset=x;windowYOffset=y;}
    @Override protected void init(){super.init();guiLeft=(width-windowWidth)/2+windowXOffset;guiTop=(height-windowHeight)/2+windowYOffset;}
    protected <W extends net.minecraft.client.gui.components.events.GuiEventListener & net.minecraft.client.gui.components.Renderable & net.minecraft.client.gui.narration.NarratableEntry> void addRenderableWidgets(W widget){addRenderableWidget(widget);}
    protected void renderWindowBackground(GuiGraphicsExtractor graphics,int x,int y,float partialTick){}
    protected abstract void renderWindow(GuiGraphicsExtractor graphics,int x,int y,float partialTick);
    protected void renderWindowForeground(GuiGraphicsExtractor graphics,int x,int y,float partialTick){}
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int x,int y,float partialTick){
        renderWindowBackground(graphics,x,y,partialTick);renderWindow(graphics,x,y,partialTick);
        super.extractRenderState(graphics,x,y,partialTick);renderWindowForeground(graphics,x,y,partialTick);
    }
}
