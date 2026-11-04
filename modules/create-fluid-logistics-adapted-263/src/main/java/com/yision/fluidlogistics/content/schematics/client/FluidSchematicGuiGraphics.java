package com.yision.fluidlogistics.content.schematics.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.yision.fluidlogistics.client.FluidLogisticsGuiTextures;
import com.yision.fluidlogistics.mixin.client.GuiGraphicsExtractorAccessor;
/** Shares the real GUI submission queue and only substitutes the fluid schematic's presentation. */
public class FluidSchematicGuiGraphics extends GuiGraphicsExtractor {
    public FluidSchematicGuiGraphics(GuiGraphicsExtractor source) {
        super(Minecraft.getInstance(), ((GuiGraphicsExtractorAccessor)source).fluidlogistics$guiRenderState(), ((GuiGraphicsExtractorAccessor)source).fluidlogistics$mouseX(), ((GuiGraphicsExtractorAccessor)source).fluidlogistics$mouseY());
        pose().set(source.pose());
    }
    private static Identifier texture(Identifier id) {return id.equals(AllGuiTextures.HUD_BACKGROUND.location) ? FluidLogisticsGuiTextures.SCHEMATIC_OVERLAY.location : id;}
    @Override public void blit(RenderPipeline pipeline,Identifier id,int x,int y,float u,float v,int width,int height,int textureWidth,int textureHeight) {
        var slot=AllGuiTextures.SCHEMATIC_SLOT;
        if(id.equals(slot.location) && u==slot.getStartX() && v==slot.getStartY() && width==slot.getWidth() && height==slot.getHeight()) {FluidLogisticsGuiTextures.FLUID_SCHEMATIC_SLOT_OVERLAY.render(this,x,y);return;}
        super.blit(pipeline,texture(id),x,y,u,v,width,height,textureWidth,textureHeight);
    }
    @Override public void blit(RenderPipeline pipeline,Identifier id,int x,int y,float u,float v,int width,int height,int textureWidth,int textureHeight,int color) {super.blit(pipeline,texture(id),x,y,u,v,width,height,textureWidth,textureHeight,color);}
    @Override public void text(Font font,String text,int x,int y,int color,boolean shadow) {int tint=FluidSchematicColors.replaceCreateBlueText(color);super.text(font,text,x,y,tint,shadow||tint!=color);}
    @Override public void text(Font font,FormattedCharSequence text,int x,int y,int color,boolean shadow) {int tint=FluidSchematicColors.replaceCreateBlueText(color);super.text(font,text,x,y,tint,shadow||tint!=color);}
    @Override public void text(Font font,Component text,int x,int y,int color,boolean shadow) {int tint=FluidSchematicColors.replaceCreateBlueText(color);super.text(font,text,x,y,tint,shadow||tint!=color);}
}
