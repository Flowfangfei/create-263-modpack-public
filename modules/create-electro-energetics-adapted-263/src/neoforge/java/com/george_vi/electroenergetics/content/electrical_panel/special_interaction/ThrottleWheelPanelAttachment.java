package com.george_vi.electroenergetics.content.electrical_panel.special_interaction;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachmentType;
import com.george_vi.electroenergetics.content.electrical_panel.link.ElectricalPanelLink;
import com.george_vi.electroenergetics.content.electrical_panel.link.ElectricalPanelLinkable;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.foundation.CEEHoldInteractionHandler;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class ThrottleWheelPanelAttachment extends PanelAttachment implements ElectricalPanelLink, IAnalogPanelAttachment, IHoldStatusPanelAttachment {
    public int prevRedstoneSignal = -1;
    public int redstoneSignal;

    public DyeColor color;
    public boolean returnToZero = false;
    public boolean holding = false;

    // Rendering
    public float leverAngle = 0;
    public float prevLeverAngle = 0;

    public ThrottleWheelPanelAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void render(ElectricalPanelBlockEntity be, float partialTicks, PoseStack ms,
                       MultiBufferSource buffer, int light, int overlay) {
        transformPose(ms, be);

        Bufferer.partial(CEEPartialModels.PANEL_ATTACHMENT_STEERING_WHEEL, be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock()));

        float angle = Mth.lerp(partialTicks, prevLeverAngle, leverAngle);
        Bufferer.partial(CEEPartialModels.PANEL_ATTACHMENT_STEERING_WHEEL_WHEEL, be.getBlockState())
                .rotateZCenteredDegrees(angle)
                .light(light)
                .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock()));

        if (color == null)
            Bufferer.partial(CEEPartialModels.PANEL_ATTACHMENT_STEERING_WHEEL_WHEEL_HANDLE, be.getBlockState())
                    .rotateZCenteredDegrees(angle)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock()));
        else
            Bufferer.partial(CEEPartialModels.PANEL_ATTACHMENT_STEERING_WHEEL_WHEEL_HANDLE_DYEABLE, be.getBlockState())
                    .rotateZCenteredDegrees(angle)
                    .color(color.getFireworkColor())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock()));

        ms.translate(4/16f, -2/16f, 0);
        renderLinkAntenna(be, ms, buffer, light);
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        prevLeverAngle = leverAngle;
        float target = Math.clamp(redstoneSignal / 15f, 0f, 1f) * 180 - 90;

        leverAngle = Mth.lerp(0.5f, leverAngle, target);

        if (prevRedstoneSignal != redstoneSignal) {
            prevRedstoneSignal = redstoneSignal;
            updateLinkState(0, Mth.clamp(redstoneSignal, 0, 15));
        }
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (returnToZero && !holding && redstoneSignal != 0) {
            if (redstoneSignal > 0)
                redstoneSignal = Math.max(redstoneSignal - 5, 0);
            else
                redstoneSignal = Math.min(redstoneSignal + 5, 0);
        }

        if (prevRedstoneSignal != redstoneSignal) {
            prevRedstoneSignal = redstoneSignal;
            updateLinkState(0, Mth.clamp(redstoneSignal, 0, 15));
            sendData();
        }
    }

    @Override
    public void postTick(SimulationResults results) {
    }

    @Override
    public InteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof WireSpoolItem ||
                stack.getItem() instanceof EmptySpoolItem ||
                AllItems.WRENCH.isIn(stack))
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (AllBlocks.REDSTONE_LINK.isIn(stack)) {
            if (!level.isClientSide() && player instanceof ServerPlayer && player.mayBuild())
                player.openMenu(this, this::writeForConfiguration);
            return InteractionResult.SUCCESS;
        }

        if (stack.getItem() instanceof DyeItem di) {
            color = stack.getOrDefault(net.minecraft.core.component.DataComponents.DYE, net.minecraft.world.item.DyeColor.WHITE);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS);
            sendData();
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide()) {
            if (!CEEHoldInteractionHandler.isInteracting())
                CEEHoldInteractionHandler.startInteraction(new SteeringWheelHoldInteraction(pos, slot.ordinal(),
                        redstoneSignal, false));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void initialize() {
        updateLinkState(-1);
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public BlockPos getLocation() {
        return pos;
    }

    @Override
    public void onRemoved(Player player) {
        super.onRemoved(player);
        if (!level.isClientSide())
            removeLinkState(-1);
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        color = DyeColor.byName(tag.getStringOr("Color", ""), null);
        redstoneSignal = tag.getByteOr("RedstoneSignal", (byte) 0);
        returnToZero = tag.getBooleanOr("ReturnToZero", false);
        readLinkFrequencies(tag, registries);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        if (returnToZero)
            tag.putBoolean("ReturnToZero", true);
        if (redstoneSignal != 0)
            tag.putByte("RedstoneSignal", (byte) redstoneSignal);
        if (color != null)
            tag.putString("Color", color.getSerializedName());
        writeLinkFrequencies(tag, registries);
    }

    ItemStack[] linkFrequencies = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};

    @Override
    public ItemStack[] getLinkFrequencies() {
        return linkFrequencies;
    }

    ElectricalPanelLinkable[] linkables = new ElectricalPanelLinkable[] {new ElectricalPanelLinkable(this)};

    @Override
    public ElectricalPanelLinkable[] getLinkables() {
        return linkables;
    }

    @Override
    public int getAnalogState() {
        return redstoneSignal;
    }

    @Override
    public void setAnalogState(int state) {
        redstoneSignal = state;
    }

    @Override
    public boolean hasReturnToZeroOption() {
        return true;
    }

    @Override
    public void setReturnToZero(boolean returnToZero) {
        this.returnToZero = returnToZero;
    }

    @Override
    public boolean getReturnToZero() {
        return returnToZero;
    }

    @Override
    public void setHoldStatus(boolean holding) {
        this.holding = holding;
    }
}
