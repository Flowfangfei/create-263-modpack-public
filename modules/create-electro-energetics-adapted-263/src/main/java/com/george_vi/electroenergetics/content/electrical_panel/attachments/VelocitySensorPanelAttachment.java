package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.zurrtum.create.AllItems;
import com.george_vi.electroenergetics.compat.SpatialIntegration;
import com.george_vi.electroenergetics.compat.SpatialIntegration.SubLevel;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.time.LocalDate;
import java.time.Month;

public class VelocitySensorPanelAttachment extends PanelAttachment {
    public float dialTarget;
    public float dialState;
    public float prevDialState;
    public boolean wide;
    public SpeedUnit speedUnit = SpeedUnit.METERS_PER_S;

    public double value;

    public VelocitySensorPanelAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        SubLevel subLevel = SpatialIntegration.containing(level, pos);
        if (subLevel == null) {
            value = 0;
            return;
        }

        Vec3 prevPosition = subLevel.lastPose().transformPosition(net.minecraft.world.phys.Vec3.atCenterOf(pos));
        Vec3 position = subLevel.logicalPose().transformPosition(net.minecraft.world.phys.Vec3.atCenterOf(pos));

        double distance = position.distanceTo(prevPosition);
        // distance meters / tick
        value = distance * 20;
        // value meters / second

        dialTarget = (float) Mth.clamp(value / 60f, 0, 1);

        prevDialState = dialState;
        dialState += (dialTarget - dialState) * .125f;
        if (dialState > 1 && level.getRandom().nextFloat() < 1 / 2f)
            dialState -= (dialState - 1) * level.getRandom().nextFloat();
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        if (!level.isLoaded(pos))
            return;

        SubLevel subLevel = SpatialIntegration.containing(level, pos);
        if (subLevel == null) {
            value = 0;
            return;
        }

        Vec3 prevPosition = subLevel.lastPose().transformPosition(net.minecraft.world.phys.Vec3.atCenterOf(pos));
        Vec3 position = subLevel.logicalPose().transformPosition(net.minecraft.world.phys.Vec3.atCenterOf(pos));

        double distance = position.distanceTo(prevPosition);
        // distance meters / tick
        value = distance * 20;
        // value meters / second
    }

    @Override
    public void postTick(SimulationResults results) {
    }

    @Override
    public InteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (stack.is(AllItems.WRENCH)) {
            speedUnit = SpeedUnit.values()[(speedUnit.ordinal() + 1) % SpeedUnit.values().length];
            sendData();
            return InteractionResult.SUCCESS;
        }

        if (stack.getItem() == type.item.asItem() && slot.isHorizontal) {
            wide ^= true;
            sendData();
            return InteractionResult.SUCCESS;
        }

        return super.onInteract(stack, player, hand, hitResult);
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        value = tag.getDoubleOr("Value", 0d);
        wide = tag.getBooleanOr("Wide", false);
        speedUnit = SpeedUnit.values()[Mth.clamp(tag.getIntOr("SpeedUnit", 0), 0, SpeedUnit.values().length - 1)];
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        tag.putDouble("Value", value);
        if (wide)
            tag.putBoolean("Wide", true);
        if (speedUnit != SpeedUnit.METERS_PER_S)
            tag.putInt("SpeedUnit", speedUnit.ordinal());
    }

    public enum SpeedUnit {
        METERS_PER_S(1, " m/s"),
        KM_PER_H(3.6f, " km/h"),

        MILES_PER_H(2.237f, isAprilFools() ? " burgers per bald eagle" : " mph"), // who even uses this unit am I right
        ;

        public final float multiplier;
        public final String suffix;

        SpeedUnit(float multiplier, String suffix) {
            this.multiplier = multiplier;
            this.suffix = suffix;
        }

        private static boolean isAprilFools() {
            LocalDate localdate = LocalDate.now();
            int day = localdate.getDayOfMonth();
            Month month = localdate.getMonth();
            return month == Month.APRIL && day == 1;
        }
    }
}
