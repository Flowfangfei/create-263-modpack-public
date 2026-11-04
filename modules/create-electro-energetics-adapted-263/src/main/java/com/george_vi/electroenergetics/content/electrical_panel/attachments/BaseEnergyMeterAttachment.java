package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.energy_meter.PanelEnergyMeterScreen;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.george_vi.electroenergetics.foundation.OwnerNames;

import java.util.List;
import java.util.UUID;

public abstract class BaseEnergyMeterAttachment extends PanelAttachment {
    public double activePower = 0;
    public double totalEnergy = 0;
    public LerpedFloat smoothTotalEnergy = LerpedFloat.linear();
    public UUID owner;
    public int ticks = 0;
    public boolean disconnected;
    public boolean inverted = false;

    public BaseEnergyMeterAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        smoothTotalEnergy.tickChaser();
    }

    @Override
    public InteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(AllItems.WRENCH) || stack.getItem() instanceof WireSpoolItem || CEEItems.EMPTY_SPOOL.isIn(stack))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        com.george_vi.electroenergetics.foundation.ClientExecution.run(() -> () -> this.displayScreen(player));
        return InteractionResult.SUCCESS;
    }

    @Environment(EnvType.CLIENT)
    protected void displayScreen(Player player) {
        if (player instanceof LocalPlayer)
            ScreenOpener.open(new PanelEnergyMeterScreen(this));
    }

    @Override
    public void onInserted(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        UUID owner = stack.get(CEEDataComponents.OWNER);
        Double energy = stack.get(CEEDataComponents.ENERGY);
        if (energy != null && owner != null) {
            this.owner = owner;
            this.totalEnergy = energy;
        } else {
            this.owner = player.getUUID();
        }

        inverted = hitResult.getLocation().y - pos.getY() > 0.5;
    }

    @Override
    public List<ItemStack> getDrops() {
        ItemStack stack = defaultDroppedStack();
        stack.set(CEEDataComponents.ENERGY, totalEnergy);
        if (owner != null) {
            stack.set(CEEDataComponents.OWNER, owner);
            String username = OwnerNames.lastKnown(level, owner);
            stack.set(CEEDataComponents.OWNER_NAME, username == null ? "Unknown Player" : username);
        }
        return List.of(stack);
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        super.read(tag, clientPacket, registries);
        totalEnergy = tag.getDoubleOr("TotalEnergy", 0d);
        activePower = tag.getDoubleOr("ActivePower", 0d);
        disconnected = tag.getBooleanOr("Disconnected", false);
        inverted = tag.getBooleanOr("Inverted", false);
        if (tag.contains("Owner"))
            owner = tag.read("Owner", net.minecraft.core.UUIDUtil.CODEC).orElse(null);

        if (clientPacket)
            smoothTotalEnergy.chase(totalEnergy, 0.5, LerpedFloat.Chaser.EXP);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        super.write(tag, clientPacket, registries);
        tag.putDouble("TotalEnergy", totalEnergy);
        tag.putDouble("ActivePower", activePower);
        tag.putBoolean("Disconnected", disconnected);
        tag.putBoolean("Inverted", inverted);
        if (owner != null)
            tag.store("Owner", net.minecraft.core.UUIDUtil.CODEC, owner);
    }
}
