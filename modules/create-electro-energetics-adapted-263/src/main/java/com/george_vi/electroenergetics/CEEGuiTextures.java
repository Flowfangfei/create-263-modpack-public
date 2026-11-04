package com.george_vi.electroenergetics;

import net.minecraft.client.renderer.RenderPipelines;
import com.zurrtum.create.client.catnip.gui.TextureSheetSegment;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.gui.element.ScreenElement;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public enum CEEGuiTextures implements ScreenElement, TextureSheetSegment {
    ELECTRICAL_PANEL_LINK("electrical_panel_link", 72, 109),
    BIDIRECTIONAL_LINK("bidirectional_link", 110, 109),
    LINK_RETURN_TO_ORIGINAL("bidirectional_link", 0, 109, 16, 16),
    TRANSFORMER("transformer", 120, 120),
    TRANSFORMER_SWAP_SIDES("transformer", 0, 120, 14, 14),
    ENERGY_METER("energy_meter", 232, 120),
    ENERGY_METER_RESET("energy_meter",195, 121, 15, 16),
    ENERGY_METER_OPEN("energy_meter",211, 121, 15, 16),
    ENERGY_METER_CLOSE("energy_meter",227, 121, 15, 16);

    public final Identifier location;
    private final int width;
    private final int height;
    private final int startX;
    private final int startY;

    CEEGuiTextures(String name, int width, int height) {
        this(name, 0, 0, width, height);
    }

    CEEGuiTextures(String name, int startX, int startY, int width, int height) {
        this.location = CreateElectroEnergetics.rl("textures/gui/" + name + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
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

    @Override
    public void render(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, location, x, y, startX, startY, width, height, 256, 256);
    }
    public void render(GuiGraphicsExtractor graphics, int x, int y, Color c) {
        UIRenderHelper.drawColoredTexture(graphics, bind(), c, x, y, startX, startY, width, height);
    }

    @Override
    public @NotNull Identifier getLocation() {
        return location;
    }
}
