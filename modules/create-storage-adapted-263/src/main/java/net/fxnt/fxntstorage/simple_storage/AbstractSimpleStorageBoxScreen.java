package net.fxnt.fxntstorage.simple_storage;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpack.client.menu.TruncatedTitle;
import net.fxnt.fxntstorage.init.ModItems;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public abstract class AbstractSimpleStorageBoxScreen<M extends AbstractContainerMenu & ISimpleStorageBoxMenu>
        extends AbstractContainerScreen<M> {

    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(
            FXNTStorage.MOD_ID, "textures/gui/container/simple_storage_box_screen.png");
    private static final int GUI_TEXTURE_WIDTH = 176;
    private static final int GUI_TEXTURE_HEIGHT = 178;
    private static final int FILTER_SLOT_SIZE = 34;

    private final TruncatedTitle titleRenderer = new TruncatedTitle();

    protected AbstractSimpleStorageBoxScreen(M menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, GUI_TEXTURE_WIDTH, GUI_TEXTURE_HEIGHT);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {

        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int slotX = leftPos + 30;
        int slotY = topPos + 20;
        renderFilterItem(graphics, slotX, slotY);

        ItemStack filterItem = menu.getDisplayedItem();
        if (!filterItem.isEmpty() && mouseX >= slotX && mouseX < slotX + FILTER_SLOT_SIZE && mouseY >= slotY && mouseY < slotY + FILTER_SLOT_SIZE) {
            graphics.setTooltipForNextFrame(font, filterItem, mouseX, mouseY);
        }
        titleRenderer.renderTooltipIfHovered(graphics, font, leftPos, topPos, mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);

        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, GUI_TEXTURE_WIDTH, GUI_TEXTURE_HEIGHT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        titleRenderer.draw(graphics, font, title, 7, 6, imageWidth - 7 - 4, 0xFF404040);
        graphics.text(font, playerInventoryTitle, 7, 93 - 11, 0xFF404040, false);

        String storedText = Component.translatable("container.fxntstorage.simple_storage_box.stored").append(": ").getString();
        String capacityText = Component.translatable("container.fxntstorage.simple_storage_box.capacity").append(": ").getString();
        String voidText = Component.translatable("container.fxntstorage.simple_storage_box.upgrade").append(": ").getString();

        graphics.text(font, storedText + menu.getDisplayedStoredAmount(), 68, 21, 0xFF404040, false);
        graphics.text(font, capacityText + menu.getDisplayedMaxCapacity(), 68, 33, 0xFF404040, false);
        graphics.text(font, voidText + getUpgradeLabel(), 68, 45, 0xFF404040, false);
    }

    private String getUpgradeLabel() {
        boolean hasVoid = menu.getSlot(0).getItem().is(ModItems.STORAGE_BOX_VOID_UPGRADE);
        boolean hasCompacting = menu.getSlot(1).getItem().is(ModItems.STORAGE_BOX_COMPACTING_UPGRADE);

        if (hasVoid && hasCompacting)
            return Component.translatable("container.fxntstorage.simple_storage_box.compacting_void").getString();
        if (hasVoid)
            return Component.translatable("container.fxntstorage.simple_storage_box.void").getString();
        if (hasCompacting)
            return Component.translatable("container.fxntstorage.simple_storage_box.compacting").getString();

        return Component.translatable("container.fxntstorage.simple_storage_box.none").getString();
    }

    private void renderFilterItem(GuiGraphicsExtractor graphics, int x, int y) {
        ItemStack itemStack = menu.getDisplayedItem();
        if (!itemStack.isEmpty()) {
            renderFilterItemStack(graphics, itemStack, x + FILTER_SLOT_SIZE / 2f, y + FILTER_SLOT_SIZE / 2f);
            renderFilterItemDecoration(graphics, font, itemStack, x + 2, y + 2, menu.getDisplayedStoredAmount());
        }
    }

    private void renderFilterItemStack(GuiGraphicsExtractor graphics, ItemStack stack, float x, float y) {
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x - 13, y - 13);
        pose.scale(26f / 16f, 26f / 16f);
        graphics.item(stack, 0, 0);
        pose.popMatrix();
    }

    private void renderFilterItemDecoration(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y, int amount) {
        org.joml.Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();

        if (!stack.isEmpty() && stack.isBarVisible()) {
            int i = stack.getBarWidth();
            int j = stack.getBarColor();
            int xOffset = x + 3;
            int yOffset = y + 28;
            int width = 26;
            int height = 2;
            graphics.fill(xOffset, yOffset, xOffset + width, yOffset + height, -16777216);
            graphics.fill(xOffset, yOffset, xOffset + (i * 2), yOffset + (height / 2), j | -16777216);
        }

        String string = Util.formatNumber(amount);
        graphics.nextStratum();
        graphics.text(font, string, x + 33 - font.width(string), y + 25, 0xFFFFFFFF, true);

        poseStack.popMatrix();
    }
}
