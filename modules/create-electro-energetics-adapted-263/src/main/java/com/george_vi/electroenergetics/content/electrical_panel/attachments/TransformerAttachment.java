package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerElectricalProperties;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerScreen;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.RMSHolder;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.device.ElectricalDevice;
import com.george_vi.electroenergetics.foundation.nodes.DirectionalNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.george_vi.electroenergetics.events.network.CEENetwork;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

public class TransformerAttachment extends PanelAttachment {
    public int primaryTurns;
    public int secondaryTurns;
    public boolean inverted;
    public boolean blown;
    public RMSHolder rmsPrimaryVoltages;
    public RMSHolder rmsSecondaryVoltages;
    public double primaryVoltage;
    public double secondaryVoltage;
    public float temp;

    public TransformerAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {

    }

    @Override
    public void preTick(BridgeCollector bridges) {
        // 0 -R- PD --- 2
        // 1 -R- SD --- 3

        if (blown)
            return;

        InWorldNode primaryDivNode = new InWorldNode(nodes[0].id() + 1_000_000, nodes[0].sourcePos());
        InWorldNode secondaryDivNode = new InWorldNode(nodes[0].id() + 1_000_001, nodes[0].sourcePos());

        double ratio = (double) primaryTurns / -secondaryTurns;
        if (inverted)
            ratio = 1 / ratio;
        TransformerElectricalProperties ep = new TransformerElectricalProperties(ratio,
                new DirectionalNodeConnection(primaryDivNode, nodes[2]),
                new DirectionalNodeConnection(secondaryDivNode, nodes[3]));
        bridges.addInternalNode(primaryDivNode);
        bridges.addInternalNode(secondaryDivNode);
        bridges.bridge(nodes[0], primaryDivNode, ElectricalProperties.resistor(0.1));
        bridges.bridge(primaryDivNode, nodes[2], ep);

        bridges.bridge(nodes[1], secondaryDivNode, ElectricalProperties.resistor(0.1));
        bridges.bridge(secondaryDivNode, nodes[3], ep.getOtherProperties());
    }

    @Override
    public float getNodeSize(Level level, BlockPos pos, BlockState state, int id) {
        return 1/16f;
    }

    public double prevPrimaryVoltage = 0;
    public double prevSecondaryVoltage = 0;
    public double prevPower = 0;
    public double power = 0;

    @Override
    public void postTick(SimulationResults results) {
        if (!level.isLoaded(pos))
            return;

        if (blown) {
            power = 0;
            rmsPrimaryVoltages.add(0);
            rmsSecondaryVoltages.add(0);
        } else {
            InWorldNode primaryDivNode = new InWorldNode(nodes[0].id() + 1_000_000, nodes[0].sourcePos());
            InWorldNode secondaryDivNode = new InWorldNode(nodes[0].id() + 1_000_001, nodes[0].sourcePos());

            double vdp = inverted ? results.getVoltageAt(nodes[1], nodes[3]) : results.getVoltageAt(nodes[0], nodes[2]);
            double sdp = inverted ? results.getVoltageAt(nodes[0], nodes[2]) : results.getVoltageAt(nodes[1], nodes[3]);
            double ip = (inverted ? results.getVoltageAt(nodes[1], secondaryDivNode) : results.getVoltageAt(nodes[0], primaryDivNode)) / 0.1;
            power = ip * vdp;

            rmsPrimaryVoltages.add(vdp);
            rmsSecondaryVoltages.add(sdp);
        }
        primaryVoltage = rmsPrimaryVoltages.get();
        secondaryVoltage = rmsSecondaryVoltages.get();

        if (Math.abs(prevPrimaryVoltage - primaryVoltage) > 1 ||
                Math.abs(prevSecondaryVoltage - secondaryVoltage) > 1 ||
                Math.abs(prevPower - power) > 1) {
            if (level.getBlockEntity(pos) instanceof ElectricalPanelBlockEntity be)
                be.sendData();
            prevPrimaryVoltage = primaryVoltage;
            prevSecondaryVoltage = secondaryVoltage;
            prevPower = power;
        }

        double load = Math.abs(power) / CEEConfigs.server().powerValues.miniatureTransformerMaxPower.get();

        temp = ElectricalDevice.updateTemp(temp, (float) load * 1000);

        if (this.temp > 30_000) {
            if (level.isLoaded(pos)) {
                Vec3 center = getCenter();
                CEENetwork.NETWORK.sendToClientsAround((ServerLevel) level, center, 40, new SendSparkPacket(center, SendSparkPacket.SparkSize.SMALL));
                ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 0, 0, 0,0, 0);
            }
            temp = 0;
            blown = true;
        } else if (this.temp > 29_000) {
            Vec3 center = getCenter();
            if (level.getRandom().nextFloat() > 0.8f)
                ((ServerLevel)level).sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 5, 0.1, 0.1, 0.1, 0);

        }
    }

    @Override
    public InteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(AllItems.WRENCH))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        com.george_vi.electroenergetics.foundation.ClientExecution.run(() -> () -> this.displayScreen(player));
        return InteractionResult.SUCCESS;
    }

    @Environment(EnvType.CLIENT)
    protected void displayScreen(Player player) {
        if (player instanceof LocalPlayer)
            ScreenOpener.open(new TransformerScreen(this));
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        primaryTurns = Mth.clamp(tag.getIntOr("PrimaryTurns", 0), 1, 240);
        secondaryTurns = Mth.clamp(tag.getIntOr("SecondaryTurns", 0), 1, 240);
        primaryVoltage = tag.getDoubleOr("PrimaryVoltage", 0d);
        secondaryVoltage = tag.getDoubleOr("SecondaryVoltage", 0d);
        power = tag.getDoubleOr("Power", 0d);
        inverted = tag.getBooleanOr("Inverted", false);
        blown = tag.getBooleanOr("Blown", false);
        temp = tag.getFloatOr("Temp1", 0f);
        if (clientPacket)
            return;

        this.rmsPrimaryVoltages = new RMSHolder(2);
        this.rmsSecondaryVoltages = new RMSHolder(2);
        this.rmsPrimaryVoltages.read(tag, "PrimaryVoltages");
        this.rmsSecondaryVoltages.read(tag, "SecondaryVoltages");
    }

    @Override
    public List<ItemStack> getDrops() {
        if (blown)
            return new ArrayList<>();
        return super.getDrops();
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        tag.putDouble("PrimaryTurns", primaryTurns);
        tag.putDouble("SecondaryTurns", secondaryTurns);
        tag.putDouble("PrimaryVoltage", primaryVoltage);
        tag.putDouble("SecondaryVoltage", secondaryVoltage);
        tag.putDouble("Power", power);
        tag.putFloat("Temp1", temp);
        if (inverted)
            tag.putBoolean("Inverted", true);
        if (blown)
            tag.putBoolean("Blown", true);
        if (clientPacket)
            return;

        this.rmsPrimaryVoltages.write(tag, "PrimaryVoltages");
        this.rmsSecondaryVoltages.write(tag, "SecondaryVoltages");
    }

    @Override
    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        return (id == 0 || id == 2) ^ inverted ?
                CEELang.nodeLabel("primary"):  CEELang.nodeLabel("secondary");
    }
}
