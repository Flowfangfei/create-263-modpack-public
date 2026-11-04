package net.fxnt.fxntstorage.ponder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.createmod.catnip.api.client.gui.element.ScreenElement;
import net.createmod.catnip.api.math.Pointing;
import net.createmod.ponder.api.Ponder;
import net.createmod.ponder.api.client.PonderPalette;
import net.createmod.ponder.api.client.element.InputElementBuilder;
import net.createmod.ponder.api.client.scene.SceneBuilder;
import net.createmod.ponder.impl.client.gui.element.PonderGuiTextures;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.api.client.scene.PonderScene;
import net.createmod.ponder.impl.client.element.InputWindowElement;
import net.createmod.ponder.impl.client.instruction.ShowInputInstruction;
import net.createmod.ponder.impl.client.gui.PonderUI;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class CsInputWindowElement extends InputWindowElement {
    private final Vec3 sceneSpace;
    private final Pointing direction;
    @Nullable
    Identifier key;
    @Nullable
    ScreenElement icon;
    ItemStack item = ItemStack.EMPTY;
    int count = 0;

    public CsInputWindowElement(Vec3 sceneSpace, Pointing direction) {
        super(sceneSpace, direction);
        this.sceneSpace = sceneSpace;
        this.direction = direction;
    }

    public static Builder showControls(SceneBuilder scene, Vec3 sceneSpace, Pointing direction, int duration) {
        CsInputWindowElement element = new CsInputWindowElement(sceneSpace, direction);
        scene.addInstruction(new ShowInputInstruction(element, duration));
        return element.builder();
    }

    @Override
    public Builder builder() {
        return new Builder();
    }

    public class Builder implements InputElementBuilder {
        @Override
        public Builder withItem(ItemStack stack) {
            item = stack;
            return this;
        }

        public Builder withCount(int count) {
            CsInputWindowElement.this.count = count;
            return this;
        }

        @Override
        public Builder leftClick() {
            icon = net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures.ICON_LMB;
            return this;
        }

        @Override
        public Builder scroll() {
            icon = net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures.ICON_SCROLL;
            return this;
        }

        @Override
        public Builder rightClick() {
            icon = net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures.ICON_RMB;
            return this;
        }

        @Override
        public Builder showing(ScreenElement icon) {
            CsInputWindowElement.this.icon = icon;
            return this;
        }

        @Override
        public Builder whileSneaking() {
            key = Identifier.fromNamespaceAndPath("ponder", "sneak_and");
            return this;
        }

        @Override
        public Builder whileCTRL() {
            key = Identifier.fromNamespaceAndPath("ponder", "ctrl_and");
            return this;
        }
    }

    @Override
    public void render(PonderScene scene, PonderUI screen, GuiGraphicsExtractor graphics, float partialTicks, float fade) {
        Font font = screen.getFontRenderer();
        int width = 0;
        int height = 0;

        float xFade = direction == Pointing.RIGHT ? -1 : direction == Pointing.LEFT ? 1 : 0;
        float yFade = direction == Pointing.DOWN ? -1 : direction == Pointing.UP ? 1 : 0;
        xFade *= 10 * (1 - fade);
        yFade *= 10 * (1 - fade);

        boolean hasItem = !item.isEmpty();
        boolean hasText = key != null;
        boolean hasIcon = icon != null;
        int keyWidth = 0;
        String text = hasText ? PonderIndex.getLangAccess().getShared(key) : "";

        if (fade < 1 / 16f)
            return;
        Vec2 sceneToScreen = scene.getTransform()
                .sceneToScreen(sceneSpace, partialTicks);

        if (hasIcon) {
            width += 24;
            height = 24;
        }

        if (hasText) {
            keyWidth = font.width(text);
            width += keyWidth;
        }

        if (hasItem) {
            width += 24;
            height = 24;
        }

        org.joml.Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(sceneToScreen.x + xFade, sceneToScreen.y + yFade);

        PonderUI.renderSpeechBox(graphics, 0, 0, width, height, false, direction, true);

        graphics.nextStratum();

        if (hasText)
            graphics.text(font, text, 2, (int) ((height - font.lineHeight) / 2f + 2),
                    PonderPalette.WHITE.getColorObject().scaleAlpha(fade).getRGB(), false);

        if (hasIcon) {
            poseStack.pushMatrix();
            poseStack.translate(keyWidth, 0);
            poseStack.scale(1.5f, 1.5f);
            icon.render(graphics, 0, 0);
            poseStack.popMatrix();
        }

        if (hasItem) {
            int itemX = keyWidth + (hasIcon ? 24 : 0);
            GuiGameElement.of(item)
                    .<GuiGameElement.GuiRenderBuilder>at(itemX, 0)
                    .scale(1.5)
                    .render(graphics,0,0);
            graphics.nextStratum();

            if (count > 0) {
                poseStack.pushMatrix();
                // match the 1.5x scale of the rendered item so the count sits in the slot corner
                graphics.nextStratum();
                poseStack.translate(itemX, 0);
                poseStack.scale(1.5f, 1.5f);
                String countText = String.valueOf(count);
                graphics.text(font, countText, 19 - 2 - font.width(countText), 6 + 3, 0xFFFFFFFF, true);
                poseStack.popMatrix();
            }
        }

        poseStack.popMatrix();
    }
}
