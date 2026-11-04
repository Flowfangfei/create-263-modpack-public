package dev.khloeleclair.create.additionallogistics.client.registries;

import dev.khloeleclair.create.additionallogistics.CreateAdditionalLogistics;
import net.createmod.catnip.api.client.gui.TextureSheetSegment;
import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.ScreenElement;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public enum CALGuiTextures implements ScreenElement, TextureSheetSegment {

    CASH_REGISTER_BG("cash_register", 220, 124),
    PROMISE_LIMIT_BG("promise_limit", 0, 0, 72, 28, 128, 32),
    ADDITIONAL_STOCK_BG("promise_limit", 72, 0, 47, 18, 128, 32)

    ;

    public final Identifier location;
    private final int width;
    private final int height;
    private final int startX;
    private final int startY;
    private final int textureWidth;
    private final int textureHeight;

    CALGuiTextures(String location, int width, int height) {
        this(location, 0, 0, width, height);
    }

    CALGuiTextures(String location, int startX, int startY, int width, int height) {
        this(CreateAdditionalLogistics.MODID, location, startX, startY, width, height, 256, 256);
    }

    CALGuiTextures(String location, int startX, int startY, int width, int height, int textureWidth, int textureHeight) {
        this(CreateAdditionalLogistics.MODID, location, startX, startY, width, height, textureWidth, textureHeight);
    }

    CALGuiTextures(String namespace, String location, int startX, int startY, int width, int height, int textureWidth, int textureHeight) {
        this.location = Identifier.fromNamespaceAndPath(namespace, "textures/gui/" + location + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public Identifier getId() {
        return location;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, location, x, y, startX, startY, width, height, textureWidth, textureHeight);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphicsExtractor graphics, int x, int y, Color c) {
        UIRenderHelper.drawColoredTexture(
                graphics,
                bind(),
                c,
                x, y,
                startX, startY,
                width, height,
                textureWidth, textureHeight
        );
    }

    @Override
    public int getStartX() {
        return startX;
    }

    @Override
    public int getStartY() {
        return startY;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }


}
