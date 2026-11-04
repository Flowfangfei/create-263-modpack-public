package net.fxnt.fxntstorage.simple_storage;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.client.gui.AbstractSimiScreen;
import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.theme.Color;
import net.fxnt.fxntstorage.network.packet.CompactingTierScrollPacket;
import net.fxnt.fxntstorage.util.KeybindHandler;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.network.PacketDistributor;

public class CompactingWheelScreen extends AbstractSimiScreen {
    private static final int INNER_RADIUS = 50;
    private static final int OUTER_RADIUS = 110;

    private final BlockPos blockPos;
    // For mounted storage: entityId >= 0, blockPos is local contraption pos
    private final int mountedEntityId;
    private final CompactingChain chain;
    private final int tiers;
    private final int[] t0CountPerSlot;

    private int selectedSegment;
    private int ticksOpen;

    public CompactingWheelScreen(BlockPos blockPos, CompactingChain chain, int t0Stored, int currentTier) {
        this(blockPos, -1, chain, t0Stored, currentTier);
    }

    public CompactingWheelScreen(int entityId, BlockPos localPos, CompactingChain chain, int t0Stored, int currentTier) {
        this(localPos, entityId, chain, t0Stored, currentTier);
    }

    private CompactingWheelScreen(BlockPos blockPos, int mountedEntityId, CompactingChain chain, int t0Stored, int currentTier) {
        super(net.minecraft.network.chat.Component.translatable("gui.fxntstorage.compacting_wheel"));
        this.blockPos = blockPos;
        this.mountedEntityId = mountedEntityId;
        this.chain = chain;
        this.tiers = chain.tiers();
        this.selectedSegment = currentTier;

        this.t0CountPerSlot = new int[tiers];
        for (int i = 0; i < tiers; i++) {
            t0CountPerSlot[i] = countForSlot(i, t0Stored);
        }
    }

    @Override
    public void tick() {
        ticksOpen++;
        if (ticksOpen % 10 == 0)
            refreshCounts();
        super.tick();
    }

    private void refreshCounts() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        int t0Stored;
        if (mountedEntityId >= 0) {
            Entity entity = mc.level.getEntity(mountedEntityId);
            if (!(entity instanceof AbstractContraptionEntity ce)) return;
            StructureTemplate.StructureBlockInfo info = ce.getContraption().getBlocks().get(blockPos);
            if (info == null || info.nbt() == null) return;
            t0Stored = info.nbt().getInt("StoredAmount").orElse(0);
        } else {
            var be = mc.level.getBlockEntity(blockPos);
            if (!(be instanceof SimpleStorageBoxEntity ssb)) return;
            t0Stored = ssb.getStoredAmount();
        }
        for (int i = 0; i < tiers; i++) {
            t0CountPerSlot[i] = countForSlot(i, t0Stored);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        float fade = Math.min(1f, (ticksOpen + AnimationTickHolder.getPartialTicks()) / 10f);
        Color color = Color.BLACK.copy().setAlpha(0.5f * fade);
        guiGraphics.fillGradient(0, 0, this.width, this.height, color.getRGB(), color.getRGB());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        int cx = this.width / 2;
        int cy = this.height / 2;

        org.joml.Matrix3x2fStack ms = graphics.pose();
        ms.pushMatrix();
        ms.translate(cx, cy);

        int mouseOffsetX = mouseX - cx;
        int mouseOffsetY = mouseY - cy;

        if (Mth.length(mouseOffsetX, mouseOffsetY) > INNER_RADIUS - 5) {
            float sectorAngle = 360f / tiers;
            selectedSegment = (int) Math.floor(
                    ((-AngleHelper.deg(Mth.atan2(mouseOffsetX, mouseOffsetY)) + 180 + sectorAngle / 2) % 360)
                            / sectorAngle
            );
        }

        renderRadialSectors(graphics);

        UIRenderHelper.streak(graphics, 0, 0, 0, 32, 65, Color.BLACK.setAlpha(0.8f));
        UIRenderHelper.streak(graphics, 180, 0, 0, 32, 65, Color.BLACK.setAlpha(0.8f));

        graphics.centeredText(font, "Select item", 0, -9,
                UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
        graphics.centeredText(font, "for extraction", 0, 1,
                UIRenderHelper.COLOR_TEXT.getFirst().getRGB());

        ms.popMatrix();
    }

    private void renderRadialSectors(GuiGraphicsExtractor graphics) {
        org.joml.Matrix3x2fStack poseStack = graphics.pose();
        float sectorAngle = 360f / tiers;
        int sectorWidth = OUTER_RADIUS - INNER_RADIUS;

        poseStack.pushMatrix();

        for (int i = 0; i < tiers; i++) {
            Color innerColor = Color.WHITE.setAlpha(0.05f);
            Color outerColor = Color.WHITE.setAlpha(0.3f);

            poseStack.pushMatrix();

            if (i == selectedSegment) {
                innerColor.mixWith(new Color(0.8f, 0.8f, 0.2f, 0.2f), 0.5f);
                outerColor.mixWith(new Color(0.8f, 0.8f, 0.2f, 0.6f), 0.5f);
                UIRenderHelper.drawRadialSector(graphics, OUTER_RADIUS + 2, OUTER_RADIUS + 3,
                        -(sectorAngle / 2 + 90), sectorAngle, outerColor, outerColor);
            }

            UIRenderHelper.drawRadialSector(graphics, INNER_RADIUS, OUTER_RADIUS,
                    -(sectorAngle / 2 + 90), sectorAngle, innerColor, outerColor);
            Color innerEdge = innerColor.copy().setAlpha(0.5f);
            UIRenderHelper.drawRadialSector(graphics, INNER_RADIUS - 3, INNER_RADIUS - 2,
                    -(sectorAngle / 2 + 90), sectorAngle, innerEdge, innerEdge);

            // Translate to the mid-sector position, then rotate back to upright for icons/text
            poseStack.translate(0,-(sectorWidth / 2f + INNER_RADIUS));
            poseStack.rotate((float)Math.toRadians(-i*sectorAngle));

            graphics.nextStratum();

            ItemStack stack = itemForSlot(i);
            graphics.item(stack, -8, -8);

            graphics.nextStratum();

            String count = Util.formatNumber(t0CountPerSlot[i]);
            graphics.centeredText(font, count, 0, 10, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());

            graphics.centeredText(font, itemForSlot(i).getHoverName(), 0, -18,
                    UIRenderHelper.COLOR_TEXT.getFirst().getRGB());

            poseStack.popMatrix();

            // Divider line at the boundary between this sector and the next
            poseStack.pushMatrix();
            poseStack.rotate((float)Math.toRadians(sectorAngle/2));
            poseStack.translate(0, -(INNER_RADIUS + 20));
            UIRenderHelper.angledGradient(graphics, -90, 0, 0, 0.5f, sectorWidth - 10,
                    Color.WHITE.setAlpha(0.5f), Color.WHITE.setAlpha(0.15f));
            UIRenderHelper.angledGradient(graphics, 90, 0, 0, 0.5f, 25,
                    Color.WHITE.setAlpha(0.5f), Color.WHITE.setAlpha(0.15f));
            poseStack.popMatrix();

            poseStack.rotate((float)Math.toRadians(sectorAngle));
        }

        poseStack.popMatrix();
    }

    @Override
    public boolean keyReleased(net.minecraft.client.input.KeyEvent event) {
        InputConstants.Key key = InputConstants.getKey(event);
        if (KeybindHandler.COMPACTING_WHEEL_KEY.isActiveAndMatches(key)) {
            applyAndClose();
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        int button=event.button();
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            applyAndClose();
            return true;
        } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            onClose();
            return true;
        }
        return super.mouseClicked(event,doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int delta = (int) Math.round(Math.signum(scrollY));
        selectedSegment = Math.floorMod(selectedSegment - delta, tiers);
        return true;
    }

    private void applyAndClose() {
        Minecraft mc = Minecraft.getInstance();
        if (mountedEntityId >= 0) {
            // Mounted storage
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(mountedEntityId);
                if (entity instanceof AbstractContraptionEntity ce) {
                    StructureTemplate.StructureBlockInfo info = ce.getContraption().getBlocks().get(blockPos);
                    if (info != null && info.nbt() != null) {
                        info.nbt().putInt("CompactingSelectedTier", selectedSegment);
                    }
                }
            }
            net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(CompactingTierScrollPacket.forMounted(blockPos, selectedSegment, mountedEntityId));
        } else {
            // Static block entity
            if (mc.level != null) {
                var be = mc.level.getBlockEntity(blockPos);
                if (be instanceof SimpleStorageBoxEntity ssb) {
                    ssb.compactingSelectedTier = selectedSegment;
                }
            }
            net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(CompactingTierScrollPacket.forBlock(blockPos, selectedSegment));
        }
        onClose();
    }

    private ItemStack itemForSlot(int slotIdx) {
        if (tiers == 2) return slotIdx == 0 ? new ItemStack(chain.t1()) : new ItemStack(chain.t0());
        return switch (slotIdx) {
            case 0 -> new ItemStack(chain.t2());
            case 1 -> new ItemStack(chain.t1());
            default -> new ItemStack(chain.t0());
        };
    }

    private int countForSlot(int slotIdx, int t0Stored) {
        if (tiers == 2) return slotIdx == 0 ? chain.t1Count(t0Stored) : t0Stored;
        return switch (slotIdx) {
            case 0 -> chain.t2Count(t0Stored);
            case 1 -> chain.t1Count(t0Stored);
            default -> t0Stored;
        };
    }

}
