package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.link.ElectricalPanelLink;
import com.george_vi.electroenergetics.content.electrical_panel.link.ElectricalPanelLinkable;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class MomentarySwitchPanelAttachment extends PanelAttachment implements ElectricalPanelLink {

    public SwitchingBehaviour behaviour;
    public int closedTicks;
    public DyeColor color;

    public float offsetState = 0;
    public float prevOffsetState = 0;

    public MomentarySwitchPanelAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        prevOffsetState = offsetState;
        offsetState = closedTicks > 0 ? (miniature() ? 0.99f : 1.99f)/16f : 0f;
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        double r = behaviour.resistance();
        if (r < 1e+10d)
            bridges.bridge(nodes[0], nodes[1], ElectricalProperties.resistor(r));
    }

    @Override
    public void postTick(SimulationResults results) {
        double voltage = results.getVoltageAt(nodes[0], nodes[1]);
        this.behaviour.isClosed = this.closedTicks > 0;
        this.behaviour.postTick(voltage, getCenter(), level);
        if (this.closedTicks == 0) {
            CEESoundEvents.playOnServer(level, pos, CEESoundEvents.CONTACT_OPEN.get(), 1, 1);
            sendData();
            updateLinkState(0, 0);
        }
        this.closedTicks = Math.max(-1, this.closedTicks - 1);
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

        boolean swing = closedTicks == -1;
        if (!level.isClientSide()) {
            if (closedTicks == -1)
                CEESoundEvents.playOnServer(level, pos, CEESoundEvents.CONTACT_CLOSE.get(), 1f, 1f);
            closedTicks = 4;
            updateLinkState(0, 15);
            sendData();
        }
        return swing ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
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
        if (!clientPacket)
            behaviour = new SwitchingBehaviour(tag.getCompoundOrEmpty("Behaviour"));
        color = DyeColor.byName(tag.getStringOr("Color", ""), null);
        closedTicks = tag.contains("ClosedTicks") ? tag.getIntOr("ClosedTicks", 0) : -1;
        readLinkFrequencies(tag, registries);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        if (!clientPacket)
            tag.put("Behaviour", behaviour.write());
        if (closedTicks != -1)
            tag.putInt("ClosedTicks", closedTicks);
        if (color != null)
            tag.putString("Color", color.getSerializedName());
        writeLinkFrequencies(tag, registries);
    }

    public boolean miniature() {
        return slot.fullWidth == 14;
    }

    public ItemStack[] linkFrequencies = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};

    @Override
    public ItemStack[] getLinkFrequencies() {
        return linkFrequencies;
    }

    public ElectricalPanelLinkable[] linkables = new ElectricalPanelLinkable[] {new ElectricalPanelLinkable(this)};

    @Override
    public ElectricalPanelLinkable[] getLinkables() {
        return linkables;
    }

}
