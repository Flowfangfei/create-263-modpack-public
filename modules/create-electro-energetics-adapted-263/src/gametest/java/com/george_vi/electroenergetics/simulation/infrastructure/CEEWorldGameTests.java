package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEFluids;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CEESimulatedDevices;
import com.george_vi.electroenergetics.CEEWireAttachments;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlockEntity;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.content.bulb.BulbDevice;
import com.george_vi.electroenergetics.content.converter.ConverterBlock;
import com.george_vi.electroenergetics.content.converter.ConverterDevice;
import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorBlockEntity;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlock;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelDevice;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelSlot;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.BaseEnergyMeterAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.GaugePanelAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.IndicatorBulbPanelAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.MCBPanelAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachmentType;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.TransformerAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.special_interaction.AnalogLeverPanelAttachment;
import com.george_vi.electroenergetics.content.electronic_components.resistor.ResistorBlockEntity;
import com.george_vi.electroenergetics.content.fuse.FuseBlockEntity;
import com.george_vi.electroenergetics.content.fuse.FuseDevice;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterBlockEntity;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import com.george_vi.electroenergetics.content.rotor.AlternatorBrushesBlock;
import com.george_vi.electroenergetics.content.rotor.AlternatorBrushesDevice;
import com.george_vi.electroenergetics.content.rotor.AlternatorRotorBlock;
import com.george_vi.electroenergetics.content.rotor.AlternatorRotorBlockEntity;
import com.george_vi.electroenergetics.content.rotor.StatorBlock;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerBlockEntity;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerDevice;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlock;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlockEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/** Exercises real dedicated-server registration, ticking, solving and serialization. */
public final class CEEWorldGameTests {
    @GameTest
    public void allBlockEntitiesBootstrapAndRoundTrip(GameTestHelper helper) {
        int checked = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(CreateElectroEnergetics.ID)
                    || !(block instanceof EntityBlock entityBlock)) continue;
            BlockState state = block.defaultBlockState();
            BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
            BlockEntity original = entityBlock.newBlockEntity(pos, state);
            helper.assertTrue(original != null, "Missing block entity for " + BuiltInRegistries.BLOCK.getKey(block));
            helper.assertTrue(original.isValidBlockState(state), "Rejected registered block state " + state);
            if (original instanceof AccumulatorBlockEntity accumulator) {
                accumulator.cell1Charge = 24.123456789012345;
                accumulator.cell2Charge = 56.98765432109876;
            }
            CompoundTag saved = original.saveWithFullMetadata(helper.getLevel().registryAccess());
            BlockEntity restored = BlockEntity.loadStatic(pos, state, saved, helper.getLevel().registryAccess());
            helper.assertTrue(restored != null && restored.getType() == original.getType(),
                    "Block entity type changed after load for " + state);
            helper.assertTrue(saved.equals(restored.saveWithFullMetadata(helper.getLevel().registryAccess())),
                    "Default block entity state changed after serialization for " + state);
            checked++;
        }
        helper.assertTrue(checked >= 60, "Too few CEE block entities registered: " + checked);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void converterEnergyTransactionsRollbackAndRespectCapacity(GameTestHelper helper) {
        BlockPos converter = new BlockPos(2, 2, 2);
        helper.setBlock(converter, CEEBlocks.CONVERTER.get().defaultBlockState()
                .setValue(ConverterBlock.SOURCE, true).setValue(ConverterBlock.FACING, Direction.NORTH));
        helper.runAfterDelay(5, () -> {
            BlockPos pos = helper.absolutePos(converter);
            ConverterDevice device = DevicesSavedData.load(helper.getLevel()).getDevice(pos, ConverterDevice.class);
            helper.assertTrue(device != null && device.isSource, "Converter device was not registered as a source");
            EnergyStorage storage = EnergyStorage.SIDED.find(helper.getLevel(), pos, Direction.SOUTH);
            helper.assertTrue(storage != null && storage.supportsInsertion(), "Source converter energy lookup was not registered");
            helper.assertTrue(EnergyStorage.SIDED.find(helper.getLevel(), pos, Direction.NORTH) == null,
                    "Converter exposed its energy storage through the electrical terminal face");
            device.storedEnergy = 0;
            long amount = Math.min(100, storage.getCapacity());
            try (Transaction transaction = Transaction.openOuter()) {
                helper.assertTrue(storage.insert(amount, transaction) == amount, "Converter rejected energy insertion");
            }
            helper.assertTrue(device.storedEnergy == 0, "An aborted energy transaction changed stored power");
            try (Transaction transaction = Transaction.openOuter()) {
                storage.insert(amount, transaction);
                transaction.commit();
            }
            helper.assertTrue(storage.getAmount() == amount, "Committed converter energy was not retained");
            double maximum = CEEConfigs.server().converterMaxPowerKw.get() * 1000;
            device.storedEnergy = maximum - CEEConfigs.server().wattFeTConversionRate.get() / 4d;
            try (Transaction transaction = Transaction.openOuter()) {
                helper.assertTrue(storage.insert(1, transaction) == 0, "Fractional energy storage overflowed its capacity");
                transaction.commit();
            }
            helper.assertTrue(device.storedEnergy <= maximum, "Converter created energy beyond its capacity");
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 180)
    public void sourcePowersMotorAndOpenCircuitStopsIt(GameTestHelper helper) {
        BlockPos source = new BlockPos(1, 2, 1);
        BlockPos motor = new BlockPos(5, 2, 1);
        helper.setBlock(source, CEEBlocks.CREATIVE_BATTERY.get());
        helper.setBlock(motor, CEEBlocks.ELECTRIC_MOTORS[DyeColor.RED.ordinal()].get());
        helper.startSequence()
                .thenExecuteAfter(10, () -> connectPair(helper, source, motor))
                .thenExecuteAfter(50, () -> {
                    ElectricMotorBlockEntity be = helper.getBlockEntity(motor, ElectricMotorBlockEntity.class);
                    helper.assertTrue(be.averageVoltage.get() > 200, "The motor did not receive the source voltage");
                    helper.assertTrue(Math.abs(be.getGeneratedSpeed()) > 0, "The energized motor did not generate rotation");
                    InfrastructureSavedData data = InfrastructureSavedData.load(helper.getLevel());
                    data.removeConnection(new InWorldNodeConnection(
                            new InWorldNode(1, helper.absolutePos(source)), new InWorldNode(1, helper.absolutePos(motor))));
                })
                .thenExecuteAfter(50, () -> {
                    ElectricMotorBlockEntity be = helper.getBlockEntity(motor, ElectricMotorBlockEntity.class);
                    helper.assertTrue(be.averageVoltage.get() < 0.01, "Voltage remained after opening the circuit");
                    helper.assertTrue(be.getGeneratedSpeed() == 0, "The motor continued generating rotation with an open circuit");
                }).thenSucceed();
    }

    @GameTest(maxTicks = 200)
    public void drivenAlternatorPowersWiredMotor(GameTestHelper helper) {
        BlockPos drive = new BlockPos(1, 2, 4);
        BlockPos rotor = new BlockPos(2, 2, 4);
        BlockPos brushes = new BlockPos(3, 2, 4);
        BlockPos motor = new BlockPos(6, 2, 4);
        helper.setBlock(drive, AllBlocks.CREATIVE_MOTOR.defaultBlockState().setValue(CreativeMotorBlock.FACING, Direction.EAST));
        helper.setBlock(rotor, CEEBlocks.ALTERNATOR_ROTOR.get().defaultBlockState().setValue(AlternatorRotorBlock.AXIS, Direction.Axis.X));
        helper.setBlock(brushes, CEEBlocks.ALTERNATOR_BRUSHES.get().defaultBlockState().setValue(AlternatorBrushesBlock.FACING, Direction.EAST));
        helper.setBlock(rotor.above(), CEEBlocks.STATOR.get().defaultBlockState().setValue(StatorBlock.FACING, Direction.DOWN).setValue(StatorBlock.ROLL, true));
        helper.setBlock(motor, CEEBlocks.ELECTRIC_MOTORS[DyeColor.BLUE.ordinal()].get());
        helper.startSequence()
                .thenExecuteAfter(5, () -> helper.getBlockEntity(drive, CreativeMotorBlockEntity.class).generatedSpeed.setValue(128))
                .thenExecuteAfter(25, () -> connectPair(helper, brushes, motor))
                .thenExecuteAfter(70, () -> {
                    AlternatorRotorBlockEntity rotorEntity = helper.getBlockEntity(rotor, AlternatorRotorBlockEntity.class);
                    AlternatorBrushesDevice generator = DevicesSavedData.load(helper.getLevel())
                            .getDevice(helper.absolutePos(brushes), AlternatorBrushesDevice.class);
                    ElectricMotorBlockEntity motorEntity = helper.getBlockEntity(motor, ElectricMotorBlockEntity.class);
                    helper.assertTrue(rotorEntity.magnets == 3 && Math.abs(rotorEntity.getSpeed()) > 0,
                            "The driven rotor did not detect its stator or receive rotation");
                    helper.assertTrue(generator != null && generator.voltage > 80 && generator.storedEnergy > 0,
                            "The alternator did not generate electrical energy");
                    helper.assertTrue(motorEntity.averageVoltage.get() > 80 && Math.abs(motorEntity.getGeneratedSpeed()) > 0,
                            "Alternator energy did not reach the wired motor");
                }).thenSucceed();
    }

    @GameTest(maxTicks = 180)
    public void transformerRatioChangesVoltageUnderLoad(GameTestHelper helper) {
        BlockPos source = new BlockPos(1, 2, 1);
        BlockPos transformer = new BlockPos(3, 2, 1);
        BlockPos motor = new BlockPos(6, 2, 1);
        helper.setBlock(source, CEEBlocks.CREATIVE_BATTERY.get());
        helper.setBlock(transformer, CEEBlocks.TRANSFORMER.get());
        helper.setBlock(motor, CEEBlocks.ELECTRIC_MOTORS[DyeColor.GREEN.ordinal()].get());
        helper.startSequence()
                .thenExecuteAfter(10, () -> {
                    TransformerBlockEntity be = helper.getBlockEntity(transformer, TransformerBlockEntity.class);
                    be.ratio.setValue(29); // The user-facing 2 / 1 setting.
                    helper.assertTrue("2 / 1".equals(be.ratio.formatValue()), "Transformer ratio control changed its scale");
                    connect(helper, source, 0, transformer, 0);
                    connect(helper, source, 1, transformer, 1);
                    connect(helper, transformer, 2, motor, 0);
                    connect(helper, transformer, 3, motor, 1);
                })
                .thenExecuteAfter(50, () -> {
                    TransformerDevice device = DevicesSavedData.load(helper.getLevel())
                            .getDevice(helper.absolutePos(transformer), TransformerDevice.class);
                    ElectricMotorBlockEntity load = helper.getBlockEntity(motor, ElectricMotorBlockEntity.class);
                    helper.assertTrue(device != null && device.ratio == 2, "Ratio control did not update the simulated transformer");
                    helper.assertTrue(load.averageVoltage.get() > 130 && load.averageVoltage.get() < 170,
                            "The 2:1 transformer did not supply half the primary voltage: " + load.averageVoltage.get());
                    helper.assertTrue(Math.abs(load.getGeneratedSpeed()) > 0, "The transformer did not supply usable motor power");
                    helper.getBlockEntity(transformer, TransformerBlockEntity.class).ratio.setValue(42);
                })
                .thenExecuteAfter(50, () -> {
                    ElectricMotorBlockEntity load = helper.getBlockEntity(motor, ElectricMotorBlockEntity.class);
                    helper.assertTrue(load.averageVoltage.get() > 280 && load.averageVoltage.get() < 310,
                            "Changing the transformer to unity did not restore the supply voltage: " + load.averageVoltage.get());
                }).thenSucceed();
    }

    @GameTest(maxTicks = 200)
    public void bulbLightsTheWorldAndOvervoltageBreaksItsFilament(GameTestHelper helper) {
        BlockPos source = new BlockPos(1, 2, 1);
        BlockPos bulb = new BlockPos(5, 2, 1);
        for (BlockPos shell : BlockPos.betweenClosed(bulb.offset(-1, -1, -1), bulb.offset(1, 1, 1))) {
            if (!shell.equals(bulb)) helper.setBlock(shell, net.minecraft.world.level.block.Blocks.STONE);
        }
        helper.setBlock(source, CEEBlocks.CREATIVE_BATTERY.get());
        helper.setBlock(bulb, CEEBlocks.BULB.get());
        helper.startSequence()
                .thenExecuteAfter(10, () -> connectPair(helper, source, bulb))
                .thenExecuteAfter(20, () -> {
                    BulbDevice device = DevicesSavedData.load(helper.getLevel()).getDevice(helper.absolutePos(bulb), BulbDevice.class);
                    BlockState state = helper.getBlockState(bulb);
                    helper.assertTrue(device != null && device.temp > 0 && !device.destroyed, "Rated voltage did not heat an intact filament");
                    helper.assertTrue(state.getValue(BulbBlock.LIGHT) == 15 && state.getLightEmission() == 15,
                            "The powered bulb's state did not emit light");
                    waitForLight(helper, bulb);
                    helper.assertTrue(helper.getLevel().getBrightness(LightLayer.BLOCK, helper.absolutePos(bulb)) == 15,
                            "The powered bulb did not illuminate the isolated test chamber");
                })
                .thenExecute(() -> helper.getBlockEntity(source, CreativeBatteryBlockEntity.class).voltage.setValue(1_000_000))
                .thenExecuteAfter(30, () -> {
                    helper.assertBlockPresent(CEEBlocks.BROKEN_BULB.get(), bulb);
                    BulbDevice device = DevicesSavedData.load(helper.getLevel()).getDevice(helper.absolutePos(bulb), BulbDevice.class);
                    helper.assertTrue(device != null && device.destroyed, "Overvoltage did not persist the broken filament state");
                    helper.assertTrue(helper.getBlockState(bulb).getLightEmission() == 0,
                            "The broken bulb continued emitting block light");
                    waitForLight(helper, bulb);
                    helper.assertTrue(helper.getLevel().getBrightness(LightLayer.BLOCK, helper.absolutePos(bulb)) == 0,
                            "The isolated test chamber retained light from the broken filament");
                }).thenSucceed();
    }

    @GameTest(maxTicks = 100)
    public void fuseHeatsThenInterruptsAnOverloadedCircuit(GameTestHelper helper) {
        BlockPos source = new BlockPos(1, 2, 1);
        BlockPos fuse = new BlockPos(3, 2, 1);
        BlockPos resistor = new BlockPos(5, 2, 1);
        BlockPos motor = new BlockPos(5, 2, 4);
        helper.setBlock(source, CEEBlocks.CREATIVE_BATTERY.get());
        helper.setBlock(fuse, CEEBlocks.FUSE.get());
        helper.setBlock(resistor, CEEBlocks.CREATIVE_RESISTOR.get());
        helper.setBlock(motor, CEEBlocks.ELECTRIC_MOTORS[DyeColor.YELLOW.ordinal()].get());
        helper.startSequence()
                .thenExecuteAfter(10, () -> {
                    helper.getBlockEntity(resistor, ResistorBlockEntity.class).setResistance(10);
                    configureFuse(helper, fuse, 100);
                    connect(helper, source, 0, fuse, 0);
                    connect(helper, fuse, 1, resistor, 0);
                    connect(helper, resistor, 1, source, 1);
                    connect(helper, fuse, 1, motor, 0);
                    connect(helper, motor, 1, source, 1);
                })
                .thenExecuteAfter(20, () -> {
                    FuseDevice device = DevicesSavedData.load(helper.getLevel()).getDevice(helper.absolutePos(fuse), FuseDevice.class);
                    helper.assertTrue(device != null && !device.isBroken && device.temp > 0,
                            "An adequately rated fuse did not conduct and heat up");
                    helper.assertTrue(helper.getBlockEntity(motor, ElectricMotorBlockEntity.class).averageVoltage.get() > 280,
                            "The closed fuse did not supply the downstream load");
                    configureFuse(helper, fuse, 5);
                })
                .thenExecuteAfter(30, () -> {
                    helper.assertBlockPresent(CEEBlocks.BROKEN_FUSE.get(), fuse);
                    FuseDevice device = DevicesSavedData.load(helper.getLevel()).getDevice(helper.absolutePos(fuse), FuseDevice.class);
                    helper.assertTrue(device != null && device.isBroken && device.setAmperage == 5,
                            "Fuse failure lost its configured current rating");
                    helper.assertTrue(helper.getBlockEntity(fuse, FuseBlockEntity.class).setAmperage == 5,
                            "The broken fuse block entity lost its current rating");
                    ElectricMotorBlockEntity load = helper.getBlockEntity(motor, ElectricMotorBlockEntity.class);
                    helper.assertTrue(load.averageVoltage.get() < 0.01 && load.getGeneratedSpeed() == 0,
                            "The blown fuse did not interrupt the downstream motor");
                }).thenSucceed();
    }

    @GameTest(maxTicks = 40)
    public void panelAttachmentsPreserveConfigurationInBlockAndDeviceSaves(GameTestHelper helper) {
        BlockPos panel = new BlockPos(2, 2, 2);
        BlockPos copyPos = helper.absolutePos(new BlockPos(6, 2, 2));
        helper.setBlock(panel, CEEBlocks.ELECTRICAL_PANEL.get());
        helper.runAfterDelay(5, () -> {
            ElectricalPanelBlockEntity original = helper.getBlockEntity(panel, ElectricalPanelBlockEntity.class);
            int checked = 0;
            for (PanelAttachmentType type : CEERegistries.PANEL_ATTACHMENT_TYPE) {
                ElectricalPanelSlot slot = type.mode.possibleSlots().keySet().stream().min(java.util.Comparator.naturalOrder()).orElseThrow();
                PanelAttachment attachment = type.createNew(helper.absolutePos(panel), type.mode.getNodesFor(helper.absolutePos(panel), slot),
                        helper.getLevel(), slot, original.getBlockState().getValue(ElectricalPanelBlock.FACING), helper.getLevel().registryAccess());
                attachment.label = "Feeder " + CEERegistries.PANEL_ATTACHMENT_TYPE.getKey(type);
                seedAttachment(attachment);
                PanelAttachment[] attachments = new PanelAttachment[ElectricalPanelSlot.values().length];
                attachments[slot.ordinal()] = attachment;
                original.setAttachments(attachments);
                CompoundTag saved = original.saveWithFullMetadata(helper.getLevel().registryAccess());
                ElectricalPanelBlockEntity copy = (ElectricalPanelBlockEntity) BlockEntity.loadStatic(copyPos, original.getBlockState(), saved,
                        helper.getLevel().registryAccess());
                helper.assertTrue(copy != null, "Panel block entity did not load");
                copy.setLevel(helper.getLevel());
                helper.assertTrue(original.saveWithoutMetadata(helper.getLevel().registryAccess())
                                .equals(copy.saveWithoutMetadata(helper.getLevel().registryAccess())),
                        "Panel block save changed attachment configuration: " + CEERegistries.PANEL_ATTACHMENT_TYPE.getKey(type));
                PanelAttachment restored = copy.getAttachments()[slot.ordinal()];
                helper.assertTrue(restored != null && restored.type == type && attachment.label.equals(restored.label),
                        "Panel attachment type, slot or label was lost");
                ElectricalPanelDevice device = DevicesSavedData.load(helper.getLevel()).getDevice(helper.absolutePos(panel), ElectricalPanelDevice.class);
                CompoundTag deviceData = new CompoundTag();
                device.write(deviceData);
                ElectricalPanelDevice deviceCopy = new ElectricalPanelDevice(helper.getLevel(), helper.absolutePos(panel),
                        DevicesSavedData.load(helper.getLevel()), CEESimulatedDevices.ELECTRICAL_PANEL.get());
                deviceCopy.read(deviceData);
                CompoundTag after = new CompoundTag();
                deviceCopy.write(after);
                helper.assertTrue(deviceData.equals(after), "Panel device save changed attachment state: " + CEERegistries.PANEL_ATTACHMENT_TYPE.getKey(type));
                checked++;
            }
            original.setAttachments(new PanelAttachment[ElectricalPanelSlot.values().length]);
            helper.assertTrue(checked >= 16, "Too few panel attachment types registered: " + checked);
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 80)
    public void oilsFlowWithoutRenewingAndBucketsWorkWithDispensersAndFabricTransfer(GameTestHelper helper) {
        Fluid[] oils = {CEEFluids.TRANSFORMER_OIL.get().getSource(), CEEFluids.PLANT_OIL.get().getSource()};
        for (BlockPos floor : BlockPos.betweenClosed(new BlockPos(0, 1, 0), new BlockPos(7, 1, 7))) helper.setBlock(floor, Blocks.STONE);
        for (int x = 0; x < 8; x++) {
            helper.setBlock(new BlockPos(x, 2, 0), Blocks.STONE);
            helper.setBlock(new BlockPos(x, 2, 7), Blocks.STONE);
        }
        for (int z = 1; z < 7; z++) {
            helper.setBlock(new BlockPos(0, 2, z), Blocks.STONE);
            helper.setBlock(new BlockPos(7, 2, z), Blocks.STONE);
        }
        for (int i = 0; i < oils.length; i++) {
            int z = 1 + i * 4;
            BlockPos dispenser = new BlockPos(1, 2, z);
            helper.setBlock(dispenser, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST));
            helper.getBlockEntity(dispenser, DispenserBlockEntity.class).setItem(0, new ItemStack(oils[i].getBucket()));
            helper.setBlock(new BlockPos(0, 2, z), Blocks.REDSTONE_BLOCK);
            helper.setBlock(new BlockPos(4, 2, z), oils[i].defaultFluidState().createLegacyBlock());
            helper.setBlock(new BlockPos(6, 2, z), oils[i].defaultFluidState().createLegacyBlock());
        }
        helper.startSequence()
                .thenExecuteAfter(15, () -> {
                    for (int i = 0; i < oils.length; i++) {
                        int z = 1 + i * 4;
                        FluidState placed = helper.getBlockState(new BlockPos(2, 2, z)).getFluidState();
                        helper.assertTrue(placed.isSource() && placed.getType().isSame(oils[i]), "The dispenser did not place an oil source");
                        helper.assertTrue(helper.getBlockEntity(new BlockPos(1, 2, z), DispenserBlockEntity.class).getItem(0).is(Items.BUCKET),
                                "The oil dispenser did not retain an empty bucket");
                        FluidState middle = helper.getBlockState(new BlockPos(5, 2, z)).getFluidState();
                        helper.assertTrue(!middle.isEmpty() && middle.getType().isSame(oils[i]) && !middle.isSource(),
                                "Oil failed to flow between two sources or incorrectly became renewable");
                        helper.setBlock(new BlockPos(0, 2, z), Blocks.AIR);
                    }
                })
                .thenExecuteAfter(3, () -> {
                    for (int i = 0; i < oils.length; i++) helper.setBlock(new BlockPos(0, 2, 1 + i * 4), Blocks.REDSTONE_BLOCK);
                })
                .thenExecuteAfter(10, () -> {
                    for (int i = 0; i < oils.length; i++) {
                        int z = 1 + i * 4;
                        helper.assertTrue(helper.getBlockEntity(new BlockPos(1, 2, z), DispenserBlockEntity.class).getItem(0).is(oils[i].getBucket()),
                                "The dispenser did not pick up the matching oil bucket");
                        helper.assertTrue(!helper.getBlockState(new BlockPos(2, 2, z)).getFluidState().isSource(),
                                "Picking up the oil bucket duplicated the source");
                        assertFluidBucketTransactions(helper, oils[i]);
                    }
                }).thenSucceed();
    }

    @GameTest(maxTicks = 40)
    public void computerCraftPeripheralsExposeLiveMachineValuesWhenInstalled(GameTestHelper helper) {
        if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("computercraft")) {
            helper.succeed();
            return;
        }
        ComputerCraftAssertions.run(helper);
    }

    @GameTest(maxTicks = 40)
    public void infrastructurePreservesWireMetadataAndAdjacency(GameTestHelper helper) {
        BlockPos source = new BlockPos(1, 2, 1);
        BlockPos sink = new BlockPos(5, 2, 1);
        helper.setBlock(source, CEEBlocks.CREATIVE_BATTERY.get());
        helper.setBlock(sink, CEEBlocks.CREATIVE_RESISTOR.get());
        helper.runAfterDelay(5, () -> {
            InfrastructureSavedData data = InfrastructureSavedData.load(helper.getLevel());
            ensureNodes(helper, source);
            ensureNodes(helper, sink);
            InWorldNode first = new InWorldNode(0, helper.absolutePos(source));
            InWorldNode second = new InWorldNode(0, helper.absolutePos(sink));
            data.getNodeData(first).label = "Feeder A";
            WireAttachment damper = new WireAttachment(CEEWireAttachments.DAMPER.get());
            damper.data.putString("MaintenanceLabel", "North span");
            WireData wire = new WireData(CEEWireTypes.STANDARD.get(), 83.25f,
                    List.of(Pair.of(0.375f, damper)), 6.5);
            data.connect(first, second, wire);
            InfrastructureSavedData restored = InfrastructureSavedData.load(helper.getLevel(),
                    data.save(new CompoundTag(), helper.getLevel().registryAccess()));
            WireData savedWire = restored.getConnectionData(new InWorldNodeConnection(first, second));
            helper.assertTrue(savedWire != null, "Wire disappeared during infrastructure load");
            helper.assertTrue(restored.isConnected(first, second) && restored.isConnected(second, first),
                    "Wire adjacency was not restored in both directions");
            helper.assertTrue(savedWire.wireType() == wire.wireType() && savedWire.temperature() == 83.25f && savedWire.length == 6.5,
                    "Wire type, temperature or length changed during load");
            helper.assertTrue("Feeder A".equals(restored.getNodeData(first).label), "Node label changed during load");
            helper.assertTrue(savedWire.attachments().size() == 1 && savedWire.attachments().getFirst().getFirst() == 0.375f
                    && "North span".equals(savedWire.attachments().getFirst().getSecond().data.getStringOr("MaintenanceLabel", "")),
                    "Wire attachment or position changed during load");
            helper.succeed();
        });
    }

    private static void connectPair(GameTestHelper helper, BlockPos source, BlockPos sink) {
        ensureNodes(helper, source);
        ensureNodes(helper, sink);
        InfrastructureSavedData data = InfrastructureSavedData.load(helper.getLevel());
        for (int terminal = 0; terminal < 2; terminal++) {
            data.connect(new InWorldNode(terminal, helper.absolutePos(source)),
                    new InWorldNode(terminal, helper.absolutePos(sink)), CEEWireTypes.STANDARD.get());
        }
    }

    private static void connect(GameTestHelper helper, BlockPos first, int firstTerminal, BlockPos second, int secondTerminal) {
        ensureNodes(helper, first);
        ensureNodes(helper, second);
        InfrastructureSavedData.load(helper.getLevel()).connect(new InWorldNode(firstTerminal, helper.absolutePos(first)),
                new InWorldNode(secondTerminal, helper.absolutePos(second)), CEEWireTypes.STANDARD.get());
    }

    private static void configureFuse(GameTestHelper helper, BlockPos fuse, int amperage) {
        ItemStack stack = CEEBlocks.FUSE.asStack();
        stack.set(CEEDataComponents.FUSE_AMPERAGE, amperage);
        CEEBlocks.FUSE.get().setPlacedBy(helper.getLevel(), helper.absolutePos(fuse), helper.getBlockState(fuse), null, stack);
    }

    private static void waitForLight(GameTestHelper helper, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        CompletableFuture<?> pending = helper.getLevel().getChunkSource().getLightEngine()
                .waitForPendingTasks(pos.getX() >> 4, pos.getZ() >> 4).orTimeout(5, TimeUnit.SECONDS);
        // GameTest ticks are unthrottled. Pump normal server/chunk work until the real light worker reaches its barrier.
        helper.getLevel().getServer().managedBlock(pending::isDone);
        pending.join();
    }

    private static void assertFluidBucketTransactions(GameTestHelper helper, Fluid oil) {
        ItemStack[] container = {new ItemStack(oil.getBucket())};
        SingleStackStorage slot = new SingleStackStorage() {
            @Override protected ItemStack getStack() { return container[0]; }
            @Override protected void setStack(ItemStack stack) { container[0] = stack; }
        };
        ContainerItemContext context = ContainerItemContext.ofSingleSlot(slot);
        Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
        helper.assertTrue(storage != null, "The oil bucket did not expose Fabric fluid storage");
        try (Transaction transaction = Transaction.openOuter()) {
            helper.assertTrue(storage.extract(FluidVariant.of(oil), FluidConstants.BUCKET, transaction) == FluidConstants.BUCKET,
                    "Fabric fluid transfer could not empty the oil bucket");
        }
        helper.assertTrue(container[0].is(oil.getBucket()), "Aborting fluid transfer consumed the oil bucket");
        try (Transaction transaction = Transaction.openOuter()) {
            storage.extract(FluidVariant.of(oil), FluidConstants.BUCKET, transaction);
            transaction.commit();
        }
        helper.assertTrue(container[0].is(Items.BUCKET), "Committed fluid extraction did not return an empty bucket");
        Storage<FluidVariant> empty = context.find(FluidStorage.ITEM);
        helper.assertTrue(empty != null, "The empty bucket did not expose Fabric fluid storage");
        try (Transaction transaction = Transaction.openOuter()) {
            helper.assertTrue(empty.insert(FluidVariant.of(oil), FluidConstants.BUCKET, transaction) == FluidConstants.BUCKET,
                    "Fabric fluid transfer could not refill the oil bucket");
            transaction.commit();
        }
        helper.assertTrue(container[0].is(oil.getBucket()), "Refilling the bucket changed the oil type");
    }

    /** Kept behind the mod-presence check so absent optional classes are never verified. */
    private static final class ComputerCraftAssertions {
        static void run(GameTestHelper helper) {
            BlockPos accumulator = new BlockPos(1, 2, 2);
            BlockPos voltmeter = new BlockPos(3, 2, 2);
            BlockPos ammeter = new BlockPos(5, 2, 2);
            BlockPos meter = new BlockPos(7, 2, 2);
            helper.setBlock(accumulator, CEEBlocks.ACCUMULATOR.get());
            helper.setBlock(voltmeter, CEEBlocks.VOLTMETER.get());
            helper.setBlock(ammeter, CEEBlocks.AMMETER.get());
            helper.setBlock(meter, CEEBlocks.ENERGY_METER.get());
            helper.runAfterDelay(5, () -> {
                var lookup = dan200.computercraft.api.peripheral.PeripheralLookup.get();
                var accumulatorPeripheral = lookup.find(helper.getLevel(), helper.absolutePos(accumulator), Direction.NORTH);
                var voltmeterPeripheral = lookup.find(helper.getLevel(), helper.absolutePos(voltmeter), Direction.NORTH);
                var ammeterPeripheral = lookup.find(helper.getLevel(), helper.absolutePos(ammeter), Direction.NORTH);
                var meterPeripheral = lookup.find(helper.getLevel(), helper.absolutePos(meter), Direction.NORTH);
                helper.assertTrue(accumulatorPeripheral instanceof com.george_vi.electroenergetics.compat.computercraft.peripherals.AccumulatorPeripheral
                                && "ElectroEnergetics_Accumulator".equals(accumulatorPeripheral.getType()), "Accumulator peripheral lookup failed");
                helper.assertTrue(voltmeterPeripheral instanceof com.george_vi.electroenergetics.compat.computercraft.peripherals.ElectricGaugePeripheral
                                && "ElectroEnergetics_ElectricGauge".equals(voltmeterPeripheral.getType()), "Voltmeter peripheral lookup failed");
                helper.assertTrue(ammeterPeripheral instanceof com.george_vi.electroenergetics.compat.computercraft.peripherals.ElectricGaugePeripheral
                                && "ElectroEnergetics_ElectricGauge".equals(ammeterPeripheral.getType()), "Ammeter peripheral lookup failed");
                helper.assertTrue(meterPeripheral instanceof com.george_vi.electroenergetics.compat.computercraft.peripherals.EnergyMeterPeripheral
                                && "ElectroEnergetics_EnergyMeter".equals(meterPeripheral.getType()), "Energy meter peripheral lookup failed");
                AccumulatorBlockEntity cells = helper.getBlockEntity(accumulator, AccumulatorBlockEntity.class);
                cells.isDoubleCell = true;
                cells.cell1Charge = 12.25;
                cells.cell2Charge = 37.75;
                var cellApi = (com.george_vi.electroenergetics.compat.computercraft.peripherals.AccumulatorPeripheral) accumulatorPeripheral;
                helper.assertTrue(cellApi.hasTwoCells() && cellApi.getCellWattHours(0) == 12.25 && cellApi.getCellWattHours(1) == 37.75
                                && cellApi.getWattHours() == 50 && cellApi.getCapacity() == 2 * cellApi.getCellCapacity(),
                        "Accumulator peripheral did not expose live cell values");
                ElectricGaugeBlockEntity voltage = helper.getBlockEntity(voltmeter, ElectricGaugeBlockEntity.class);
                voltage.voltage = 230.125;
                voltage.scaling.setValue(4);
                helper.assertTrue(((com.george_vi.electroenergetics.compat.computercraft.peripherals.ElectricGaugePeripheral) voltmeterPeripheral).getValue() == 2301.25,
                        "Voltmeter peripheral did not apply the gauge scale");
                ElectricGaugeBlockEntity current = helper.getBlockEntity(ammeter, ElectricGaugeBlockEntity.class);
                current.voltage = 0.125;
                helper.assertTrue(((com.george_vi.electroenergetics.compat.computercraft.peripherals.ElectricGaugePeripheral) ammeterPeripheral).getValue() == 12.5,
                        "Ammeter peripheral did not convert the shunt voltage to current");
                EnergyMeterBlockEntity energy = helper.getBlockEntity(meter, EnergyMeterBlockEntity.class);
                energy.totalEnergy = 923456.7890123456;
                energy.disconnected = true;
                energy.scale.setValue(2);
                var energyApi = (com.george_vi.electroenergetics.compat.computercraft.peripherals.EnergyMeterPeripheral) meterPeripheral;
                helper.assertTrue(!energyApi.isConnected() && energyApi.getTotalEnergy() == energy.totalEnergy * 0.1,
                        "Energy meter peripheral did not expose the scaled energy or disconnection state");
                helper.assertTrue(lookup.find(helper.getLevel(), helper.absolutePos(accumulator), Direction.SOUTH) == accumulatorPeripheral,
                        "Peripheral lookup did not reuse the attached computer behaviour");
                helper.succeed();
            });
        }
    }

    private static void seedAttachment(PanelAttachment attachment) {
        if (attachment instanceof TransformerAttachment transformer) {
            transformer.primaryTurns = 37;
            transformer.secondaryTurns = 123;
            transformer.inverted = true;
            transformer.temp = 2345.5f;
            transformer.rmsPrimaryVoltages.add(229.25);
            transformer.rmsSecondaryVoltages.add(68.75);
        }
        if (attachment instanceof BaseEnergyMeterAttachment meter) {
            meter.owner = UUID.fromString("8eb9d7b8-3429-4b58-ac03-8719ef122411");
            meter.totalEnergy = 923456.7890123456;
            meter.activePower = 1267.8125;
            meter.disconnected = true;
            meter.inverted = true;
        }
        if (attachment instanceof MCBPanelAttachment breaker) {
            breaker.setAmperage = 37;
            breaker.isClosed = true;
            breaker.temp = 456.25f;
            breaker.behaviour.close();
        }
        if (attachment instanceof AnalogLeverPanelAttachment lever) {
            lever.color = DyeColor.CYAN;
            lever.style = AnalogLeverPanelAttachment.Style.BALL;
            lever.returnToZero = true;
            lever.redstoneSignal = 9;
            lever.linkFrequencies[0] = new ItemStack(Items.DIAMOND);
            lever.linkFrequencies[0].set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Control channel"));
            lever.linkFrequencies[1] = new ItemStack(Items.REDSTONE);
        }
        if (attachment instanceof IndicatorBulbPanelAttachment bulb) {
            bulb.color = DyeColor.PURPLE;
            bulb.light = 0.625f;
        }
        if (attachment instanceof GaugePanelAttachment gauge) {
            gauge.value = 72.125;
            gauge.rmsVoltages.add(72.125);
        }
    }

    private static void ensureNodes(GameTestHelper helper, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        BlockState state = helper.getLevel().getBlockState(pos);
        ((ElectricalDeviceBlock<?>) state.getBlock()).ensureNodesExist(helper.getLevel(), pos, state);
    }
}
