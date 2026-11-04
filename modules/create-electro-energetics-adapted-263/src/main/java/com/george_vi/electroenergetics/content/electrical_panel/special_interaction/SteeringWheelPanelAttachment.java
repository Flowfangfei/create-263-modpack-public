package com.george_vi.electroenergetics.content.electrical_panel.special_interaction;

import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachmentType;
import com.george_vi.electroenergetics.content.electrical_panel.link.BiDirectionalLinkMenu;
import com.george_vi.electroenergetics.content.electrical_panel.link.ElectricalPanelLinkable;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.foundation.CEEHoldInteractionHandler;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public class SteeringWheelPanelAttachment extends ThrottleWheelPanelAttachment {
    public SteeringWheelPanelAttachment(PanelAttachmentType type) {
        super(type);
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
            updateLinkState(0, Mth.clamp(-redstoneSignal, 0, 15));
            updateLinkState(1, Mth.clamp(redstoneSignal, 0, 15));
            sendData();
        }
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        prevLeverAngle = leverAngle;
        float target = Math.clamp(redstoneSignal / 15f, -1f, 1f) * 90;

        leverAngle = Mth.lerp(0.5f, leverAngle, target);

        if (prevRedstoneSignal != redstoneSignal) {
            prevRedstoneSignal = redstoneSignal;
            updateLinkState(0, Mth.clamp(-redstoneSignal, 0, 15));
            updateLinkState(1, Mth.clamp(redstoneSignal, 0, 15));
        }
    }

    @Override
    public InteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof WireSpoolItem ||
                stack.getItem() instanceof EmptySpoolItem ||
                stack.is(AllItems.WRENCH))
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (stack.is(AllBlocks.REDSTONE_LINK.asItem())) {
            if (!level.isClientSide() && player instanceof ServerPlayer && player.mayBuild())
                openHandledScreen((net.minecraft.server.level.ServerPlayer) player);
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
                        redstoneSignal, true));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public com.zurrtum.create.foundation.gui.menu.MenuBase<?> createMenu(int containerId, Inventory playerInventory, Player player, net.minecraft.network.RegistryFriendlyByteBuf extraData) {
        writeForConfiguration(extraData);
        return BiDirectionalLinkMenu.create(containerId, playerInventory, this);
    }

    public ItemStack[] linkFrequencies = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

    @Override
    public ItemStack[] getLinkFrequencies() {
        return linkFrequencies;
    }

    ElectricalPanelLinkable[] linkables = new ElectricalPanelLinkable[] {
            new ElectricalPanelLinkable(this),
            new ElectricalPanelLinkable(this)};

    @Override
    public ElectricalPanelLinkable[] getLinkables() {
        return linkables;
    }

    @Override
    public int frequencies() {
        return 2;
    }

    @Override
    public int getAnalogMin() {
        return -15;
    }
}
