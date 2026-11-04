package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.content.converter.*;
import com.george_vi.electroenergetics.content.creative_battery.*;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlock;
import com.george_vi.electroenergetics.content.electric_motor.*;
import com.george_vi.electroenergetics.content.electronic_components.capacitor.*;
import com.george_vi.electroenergetics.content.electronic_components.inductor.*;
import com.george_vi.electroenergetics.content.electronic_components.resistor.*;
import com.george_vi.electroenergetics.content.energy_meter.*;
import com.george_vi.electroenergetics.content.gauge.*;
import com.george_vi.electroenergetics.content.potentiometer.*;
import com.george_vi.electroenergetics.content.transmission_distribution.current_transformer.*;
import com.george_vi.electroenergetics.content.transmission_distribution.hv_capacitor.*;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.*;
import com.george_vi.electroenergetics.content.transmission_distribution.voltage_regulator.*;
import com.george_vi.electroenergetics.content.variac.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlock;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlock;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CEEBlockEntityBehaviours {
    public static void register() {
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.ENERGY_METER.get(),
                be -> new CEEClientValue(be, new EnergyMeterBlockEntityBox()));
        AllBlockEntityBehaviours.add(CEEBlockEntityTypes.ACCUMULATOR.get(), CEEGoggles::new);
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.VOLTMETER.get(),
                CEEGoggles::new,
                be -> new CEEClientValue(be, new ElectricGaugeBlockEntityBox()));
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.AMMETER.get(),
                CEEGoggles::new,
                be -> new CEEClientValue(be, new ElectricGaugeBlockEntityBox()));
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.CREATIVE_BATTERY.get(),
                be -> new CEEClientValue(be, new CreativeBatteryBlockEntityBox()));
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.TRANSFORMER.get(),
                CEEGoggles::new,
                be -> new CEEClientValue(be, new TransformerBlockEntityBox()));
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.TRANSFORMER_CORE.get(),
                CEEGoggles::new,
                be -> new CEEClientValue(be, new TransformerCoreBlockEntityBox()));
        AllBlockEntityBehaviours.add(CEEBlockEntityTypes.ELECTRIC_PUMP.get(), CEEGoggles::new);
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.VOLTAGE_REGULATOR.get(),
                CEEGoggles::new,
                be -> new CEEClientValue(be, new VoltageRegulatorBlockEntityBox()));
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.ELECTRIC_MOTOR.get(),
                CEEGoggles::new,
                be -> new CEEClientValue(be, new ElectricMotorBlockEntityBox()));
        AllBlockEntityBehaviours.add(CEEBlockEntityTypes.ALTERNATOR_BRUSHES.get(), CEEGoggles::new);
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.CONVERTER.get(),
                CEEGoggles::new,
                be -> new CEEClientValue(be, new ConverterBlockEntityBox()));
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.RESISTOR.get(),
                be -> new CEEClientValue(be, new ResistorBlockEntityBox()));
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.CAPACITOR.get(),
                be -> new CEEClientValue(be, new CapacitorBlockEntityBox()));
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.INDUCTOR.get(),
                be -> new CEEClientValue(be, new InductorBlockEntityBox()));
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.HV_CAPACITOR.get(),
                be -> new CEEClientValue(be, new HVCapacitorBlockEntityBox()));
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.POTENTIOMETER.get(),
                be -> new CEEClientValue(be, new PotentiometerBlockEntityBox()));
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.REDSTONE_POTENTIOMETER.get(),
                be -> new CEEClientValue(be, new RedstonePotentiometerBlockEntityBox()));
        AllBlockEntityBehaviours.add(CEEBlockEntityTypes.SYNCHROSCOPE.get(), CEEGoggles::new);
        AllBlockEntityBehaviours.add(CEEBlockEntityTypes.FREQUENCY_METER.get(), CEEGoggles::new);
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.CURRENT_TRANSFORMER.get(),
                be -> new CEEClientValue(be, new CurrentTransformerBlockEntityBox()));
        AllBlockEntityBehaviours.add(CEEBlockEntityTypes.ELECTRICAL_PANEL.get(), CEEGoggles::new);
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.VARIAC.get(),
                be -> new CEEClientValue(be, new VariacBlockEntityBox()));
        AllBlockEntityBehaviours.add(
                CEEBlockEntityTypes.REDSTONE_VARIAC.get(),
                be -> new CEEClientValue(be, new RedstoneVariacBlockEntityBox()));
    }

    private static class ConverterBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 5);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis() == state.getValue(ConverterBlock.FACING).getAxis()
                    && state.getValue(ConverterBlock.SOURCE);
        }
    }

    private static class CreativeBatteryBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 16);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return state.getValue(CreativeBatteryBlock.FACING).getAxis().isHorizontal()
                    ? (direction.getAxis().isHorizontal()
                            && direction.getAxis()
                                    != state.getValue(CreativeBatteryBlock.FACING).getAxis())
                    : direction.getAxis() == Direction.Axis.X;
        }
    }

    private static class ElectricMotorBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return Vec3.ZERO;
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            Direction facing = state.getValue(DeployerBlock.FACING);
            Vec3 vec = VecHelper.voxelSpace(8f, 8f, 15f);

            vec =
                    VecHelper.rotateCentered(
                            vec, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
            vec =
                    VecHelper.rotateCentered(
                            vec, AngleHelper.verticalAngle(getSide()), Direction.Axis.X);
            vec = vec.subtract(Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(2 / 16f));

            return vec;
        }

        @Override
        public void rotate(BlockState state, PoseStack ms) {
            super.rotate(state, ms);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            Direction facing = state.getValue(ElectricMotorBlock.FACING);
            return direction.getAxis() != facing.getAxis();
        }
    }

    private static class EnergyMeterBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 16);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            Direction facing = state.getValue(EnergyMeterBlock.FACING);
            return direction.getOpposite() == facing;
        }
    }

    private static class ElectricGaugeBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 14);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            Direction facing = state.getValue(ElectricGaugeBlock.FACING);
            boolean axisAlongFirst = state.getValue(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE);
            if (facing.getAxis().isVertical()) return direction == facing;
            if (facing.getAxis() == Direction.Axis.X)
                return axisAlongFirst
                        ? direction.getAxis() == Direction.Axis.Z
                        : direction.getAxis().isVertical();
            return axisAlongFirst
                    ? direction.getAxis().isVertical()
                    : direction.getAxis() == Direction.Axis.X;
        }
    }

    private static class PotentiometerBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 2, 14);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal()
                    && !state.getValue(PotentiometerBlock.HORIZONTAL_FACING).equals(direction);
        }
    }

    private static class RedstonePotentiometerBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 2, 14);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal()
                    && !state.getValue(PotentiometerBlock.HORIZONTAL_FACING).equals(direction);
        }
    }

    private static class RedstoneVariacBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 2, 14);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal()
                    && !state.getValue(VariacBlock.HORIZONTAL_FACING).equals(direction);
        }
    }

    private static class VariacBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 2, 14);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal()
                    && !state.getValue(VariacBlock.HORIZONTAL_FACING).equals(direction);
        }
    }

    private static class CurrentTransformerBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 14, 13);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return state.getValue(CurrentTransformerBlock.TOP)
                    && direction.getAxis().isHorizontal()
                    && direction.getAxis()
                            != state.getValue(CurrentTransformerBlock.FACING).getAxis();
        }
    }

    private static class HVCapacitorBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 12, 13);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            Vec3 location = super.getLocalOffset(state);
            Direction facing = state.getValue(HVCapacitorBlock.FACING);
            if (state.getValue(HVCapacitorBlock.ROLL)) {
                if (facing == Direction.DOWN) location = location.subtract(0, 8 / 16f, 0);
                else if (facing == Direction.SOUTH)
                    location = location.subtract(0, 4 / 16f, -4 / 16f);
                else if (facing == Direction.NORTH)
                    location = location.subtract(0, 4 / 16f, 4 / 16f);
                else if (facing == Direction.WEST)
                    location = location.subtract(4 / 16f, 4 / 16f, 0);
                else if (facing == Direction.EAST)
                    location = location.subtract(-4 / 16f, 4 / 16f, 0);
            } else if (facing.getAxis().isHorizontal()) {
                location =
                        VecHelper.rotateCentered(
                                location,
                                -facing.toYRot() + (getSide() == Direction.DOWN ? 0 : 180),
                                Direction.Axis.Y);
            }
            return location;
        }

        @Override
        public void rotate(BlockState state, PoseStack ms) {
            super.rotate(state, ms);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            if (state.getValue(HVCapacitorBlock.FACING).getAxis().isVertical())
                return state.getValue(HVCapacitorBlock.ROLL)
                        ? direction.getAxis() == Direction.Axis.X
                        : direction.getAxis() == Direction.Axis.Z;
            else if (!state.getValue(HVCapacitorBlock.ROLL))
                return direction.getAxis().isVertical();
            return direction.getAxis().isHorizontal()
                    && direction.getAxis() != state.getValue(HVCapacitorBlock.FACING).getAxis();
        }
    }

    private static class TransformerBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 7, 15);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal()
                    && direction.getAxis() != state.getValue(TransformerBlock.FACING).getAxis();
        }
    }

    private static class TransformerCoreBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 14);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction == state.getValue(TransformerBlock.FACING).getOpposite();
        }
    }

    private static class VoltageRegulatorBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 10, 13);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return state.getValue(VoltageRegulatorBlock.TOP)
                    && direction.getAxis().isHorizontal()
                    && direction.getAxis()
                            == state.getValue(VoltageRegulatorBlock.FACING).getAxis();
        }
    }

    private static class CapacitorBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 10);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return state.getValue(CreativeBatteryBlock.FACING).equals(direction);
        }
    }

    private static class InductorBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 15);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return state.getValue(CreativeBatteryBlock.FACING).equals(direction);
        }
    }

    private static class ResistorBlockEntityBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 4);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return super.getLocalOffset(state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return state.getValue(CreativeBatteryBlock.FACING).equals(direction);
        }
    }
}
