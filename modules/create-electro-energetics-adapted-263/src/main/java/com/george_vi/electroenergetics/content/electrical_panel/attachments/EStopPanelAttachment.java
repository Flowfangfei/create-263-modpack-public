package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.events.datagen.CEEAdvancements;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.zurrtum.create.AllItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public class EStopPanelAttachment extends PanelAttachment {
    public SwitchingBehaviour behaviour;
    public boolean isClosed;

    public EStopPanelAttachment(PanelAttachmentType type) {
        super(type);
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
        this.behaviour.isClosed = isClosed;
        this.behaviour.postTick(voltage, getCenter(), level);
    }

    @Override
    public InteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof WireSpoolItem ||
                stack.getItem() instanceof EmptySpoolItem ||
                stack.is(AllItems.WRENCH))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (isClosed ^ !player.isShiftKeyDown())
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) {
            CEESoundEvents.playOnServer(level, pos, isClosed ? CEESoundEvents.CONTACT_OPEN.get() : CEESoundEvents.CONTACT_CLOSE.get(), 1f, 1f);
            if (isClosed)
                CEEAdvancements.ESTOP.awardTo(player);
            isClosed ^= true;
            sendData();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        if (!clientPacket)
            behaviour = new SwitchingBehaviour(tag.getCompoundOrEmpty("Behaviour"));
        isClosed = tag.getBooleanOr("IsClosed", false);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        if (!clientPacket)
            tag.put("Behaviour", behaviour.write());
        if (isClosed)
            tag.putBoolean("IsClosed", true);
    }
}
