package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.device.ElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.zurrtum.create.AllItems;
import com.george_vi.electroenergetics.events.network.CEENetwork;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MCBPanelAttachment extends PanelAttachment {
    public SwitchingBehaviour behaviour;
    public boolean isClosed;
    public float temp;
    public int setAmperage = 0;

    public MCBPanelAttachment(PanelAttachmentType type) {
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
        double voltage = Math.abs(results.getVoltageAt(nodes[0], nodes[1]));
        double current = voltage / behaviour.resistance();
        Vec3 sparkPos = getCenter();

        temp = ElectricalDevice.updateTemp(temp, (float) current);

        if (current < 1 || !isClosed)
            this.temp = 0;

        if (isClosed && (temp > ElectricalDevice.finalTempAt((float) setAmperage) || current > 5 * setAmperage)) {
            isClosed = false;
            CEESoundEvents.playOnServer(level, pos, CEESoundEvents.MCB_TRIP.get(), 1f, 1f);

            if (level.isLoaded(pos)) {
                sendData();
                CEENetwork.NETWORK.sendToClientsAround((ServerLevel) level, sparkPos, 40, new SendSparkPacket(sparkPos, SendSparkPacket.SparkSize.MEDIUM));
            }
        }

        if (current > 30 && !isClosed) {
            CEENetwork.NETWORK.sendToClientsAround((ServerLevel) level, sparkPos, 40, new SendSparkPacket(sparkPos, SendSparkPacket.SparkSize.SMALL));
        }

        this.behaviour.isClosed = isClosed;
        this.behaviour.postTick(voltage, sparkPos, level);
    }

    @Override
    public InteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof WireSpoolItem ||
                stack.getItem() instanceof EmptySpoolItem ||
                stack.is(AllItems.WRENCH))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) {
            CEESoundEvents.playOnServer(level, pos, isClosed ? CEESoundEvents.MCB_TRIP.get() : CEESoundEvents.MCB_CLOSE.get(), 1f, 1f);

            isClosed ^= true;
            sendData();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onInserted(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        setAmperage = stack.getOrDefault(CEEDataComponents.FUSE_AMPERAGE, 100);
        super.onInserted(stack, player, hand, hitResult);
    }

    @Override
    public List<ItemStack> getDrops() {
        ItemStack stack = defaultDroppedStack();
        if (setAmperage != 100)
            stack.set(CEEDataComponents.FUSE_AMPERAGE, setAmperage);
        return List.of(stack);
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        if (!clientPacket)
            behaviour = new SwitchingBehaviour(tag.getCompoundOrEmpty("Behaviour"));
        isClosed = tag.getBooleanOr("IsClosed", false);
        temp = tag.getFloatOr("Temp", 0f);
        setAmperage = tag.getIntOr("SetAmperage", 0);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        if (!clientPacket)
            tag.put("Behaviour", behaviour.write());
        if (isClosed)
            tag.putBoolean("IsClosed", true);
        tag.putFloat("Temp", temp);
        tag.putInt("SetAmperage", setAmperage);
    }
}
