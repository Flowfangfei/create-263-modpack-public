package com.george_vi.electroenergetics.simulation.infrastructure;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.events.network.CEENetwork;
import com.george_vi.electroenergetics.events.recipe.FluidCompactingRecipe;
import com.george_vi.electroenergetics.events.FinishElectricSimulationEvent;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.TrainPantographEntry;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import com.george_vi.electroenergetics.mixin_interfaces.ICEETrainExtension;
import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.george_vi.electroenergetics.mixin_interfaces.ISchematicInfrastructureList;
import com.george_vi.electroenergetics.simulation.infrastructure.detached_nodes.DetachedNodeType;
import com.george_vi.electroenergetics.simulation.util.LegacySavedData;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlock;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import io.netty.buffer.Unpooled;

public final class CEEPlatformGameTests {
    @GameTest
    public void legacyRegistryAliasesDecodeAndWriteCanonicalIds(GameTestHelper helper) {
        JsonPrimitive legacy = new JsonPrimitive("electroenergetics:electric_motor");
        var block = BuiltInRegistries.BLOCK.byNameCodec().parse(JsonOps.INSTANCE, legacy).getOrThrow();
        var item = BuiltInRegistries.ITEM.byNameCodec().parse(JsonOps.INSTANCE, legacy).getOrThrow();
        helper.assertTrue(block == CEEBlocks.ELECTRIC_MOTORS[net.minecraft.world.item.DyeColor.RED.ordinal()].get()
            && item == block.asItem(), "Legacy electric motor IDs did not resolve to the red motor");
        helper.assertTrue(BuiltInRegistries.BLOCK.byNameCodec().encodeStart(JsonOps.INSTANCE, block).getOrThrow()
            .getAsString().equals("electroenergetics:red_electric_motor"), "A legacy block ID was written back to a save");
        helper.assertTrue(CEERegistries.PANEL_ATTACHMENT_TYPE.getValue(CreateElectroEnergetics.rl("miniature_ammeter"))
            == CEERegistries.PANEL_ATTACHMENT_TYPE.getValue(CreateElectroEnergetics.rl("ammeter")),
            "Legacy panel attachment aliases were not registered");
        helper.succeed();
    }

    @GameTest
    public void detachedNodesPreserveAllocatorAndUuidThroughCodec(GameTestHelper helper) {
        InfrastructureSavedData data = InfrastructureSavedData.load(helper.getLevel(), new CompoundTag());
        Vec3 position = helper.absoluteVec(new Vec3(2, 2, 2));
        InWorldNodeData first = data.createDetachedNode(DetachedNodeType.FIXED, position);
        InWorldNodeData freed = data.createDetachedNode(DetachedNodeType.FIXED, position.add(1, 0, 0));
        InWorldNodeData third = data.createDetachedNode(DetachedNodeType.FIXED, position.add(2, 0, 0));
        UUID entityId = UUID.fromString("fe3c8701-0df0-4770-a29c-15cdd37890ec");
        first.detachedNodeEntityId = entityId;
        first.label = "Detached feeder";
        data.removeNode(freed.node);
        var codec = InfrastructureSavedData.codec(helper.getLevel());
        InfrastructureSavedData restored = codec.parse(NbtOps.INSTANCE, codec.encodeStart(NbtOps.INSTANCE, data).getOrThrow()).getOrThrow();
        helper.assertTrue(entityId.equals(restored.getNodeData(first.node).detachedNodeEntityId), "Detached entity UUID changed");
        helper.assertTrue("Detached feeder".equals(restored.getNodeData(first.node).label), "Detached node label changed");
        helper.assertTrue(restored.createDetachedNode(DetachedNodeType.FIXED, position).node.id() == freed.node.id(),
            "The freed detached-node ID was not reused");
        helper.assertTrue(restored.createDetachedNode(DetachedNodeType.FIXED, position).node.id() > third.node.id(),
            "The detached-node allocator reused a live ID");
        helper.succeed();
    }

    @GameTest
    public void currentAndLegacyCatenaryFormatsLoad(GameTestHelper helper) {
        BlockPos first = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos second = helper.absolutePos(new BlockPos(4, 2, 1));
        BlockPos third = helper.absolutePos(new BlockPos(1, 3, 4));
        CompoundTag legacy = new CompoundTag();
        legacy.store("From", BlockPos.CODEC, first);
        ListTag oldConnections = new ListTag();
        oldConnections.add(new IntArrayTag(new int[]{second.getX(), second.getY(), second.getZ()}));
        legacy.put("Connections", oldConnections);
        CompoundTag current = new CompoundTag();
        current.store("From", BlockPos.CODEC, third);
        CompoundTag connection = new CompoundTag();
        connection.store("Pos", BlockPos.CODEC, second);
        connection.putFloat("Temperature", 125.75f);
        connection.putBoolean("IsLow", true);
        connection.putDouble("Length", 12.5);
        ListTag currentConnections = new ListTag();
        currentConnections.add(connection);
        current.put("Connections", currentConnections);
        ListTag catenary = new ListTag();
        catenary.add(legacy);
        catenary.add(current);
        CompoundTag fixture = new CompoundTag();
        fixture.put("Catenary", catenary);
        InfrastructureSavedData restored = InfrastructureSavedData.load(helper.getLevel(), fixture);
        helper.assertTrue(restored.CATENARY_DATA.containsKey(new CatenaryConnection(first, second)), "Legacy catenary disappeared");
        CatenaryConnectionData modern = restored.CATENARY_DATA.get(new CatenaryConnection(third, second));
        helper.assertTrue(modern != null && modern.isLow && modern.temperature == 125.75f && modern.length == 12.5,
            "Modern catenary metadata changed during load");
        var codec = InfrastructureSavedData.codec(helper.getLevel());
        InfrastructureSavedData roundTrip = codec.parse(NbtOps.INSTANCE,
            codec.encodeStart(NbtOps.INSTANCE, restored).getOrThrow()).getOrThrow();
        helper.assertTrue(roundTrip.CATENARY_DATA.size() == 2, "Catenary links changed during codec round trip");
        helper.succeed();
    }

    @GameTest
    public void simulatedDeviceCodecPreservesTypesAndDoublePrecision(GameTestHelper helper) {
        var codec = DevicesSavedData.codec(helper.getLevel());
        DevicesSavedData fixture = codec.parse(NbtOps.INSTANCE, new CompoundTag()).getOrThrow();
        BlockPos connector = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos doubleConnector = connector.east();
        BlockPos converter = connector.east(2);
        fixture.addDevice(CEESimulatedDevices.CONNECTOR.get(), connector, new CompoundTag()).isHVSwitchTarget = true;
        fixture.addDevice(CEESimulatedDevices.DOUBLE_CONNECTOR.get(), doubleConnector, new CompoundTag());
        var source = fixture.addDevice(CEESimulatedDevices.CONVERTER.get(), converter, new CompoundTag());
        source.isSource = true;
        source.voltage = 749.1234567890123;
        source.resistance = 0.1234567890123;
        source.storedEnergy = 73941.1234567890123;
        DevicesSavedData restored = codec.parse(NbtOps.INSTANCE, codec.encodeStart(NbtOps.INSTANCE, fixture).getOrThrow()).getOrThrow();
        helper.assertTrue(restored.getDevices().size() == 3 && restored.getDevice(connector).type == CEESimulatedDevices.CONNECTOR.get()
            && restored.getDevice(doubleConnector).type == CEESimulatedDevices.DOUBLE_CONNECTOR.get(),
            "The simulated device registry type changed after serialization");
        var target = restored.getDevice(converter, com.george_vi.electroenergetics.content.converter.ConverterDevice.class);
        helper.assertTrue(target != null && target.isSource && target.voltage == source.voltage
            && target.resistance == source.resistance && target.storedEnergy == source.storedEnergy,
            "A simulated converter lost its persisted state or precision");
        helper.succeed();
    }

    @GameTest
    public void legacySavedDataMigratesAndReopensFromDisk(GameTestHelper helper) throws IOException {
        String name = "cee_test_migration_" + UUID.randomUUID().toString().replace("-", "");
        Path dataDirectory = DimensionType.getStorageFolder(helper.getLevel().dimension(),
            helper.getLevel().getServer().getWorldPath(LevelResource.ROOT)).resolve("data");
        Path originalFile = dataDirectory.resolve(name + ".dat");
        Path migratedFile = CreateElectroEnergetics.rl(name).withSuffix(".dat").resolveAgainst(dataDirectory);
        InfrastructureSavedData fixture = InfrastructureSavedData.load(helper.getLevel(), new CompoundTag());
        InWorldNodeData node = fixture.createDetachedNode(DetachedNodeType.FIXED, helper.absoluteVec(new Vec3(2, 2, 2)));
        node.label = "Legacy disk feeder";
        node.detachedNodeEntityId = UUID.fromString("a140b739-3c4c-4427-aec4-ff13828b2cbc");
        CompoundTag root = new CompoundTag();
        root.put("data", fixture.save(new CompoundTag(), helper.getLevel().registryAccess()));
        NbtUtils.addCurrentDataVersion(root);
        Files.createDirectories(dataDirectory);
        NbtIo.writeCompressed(root, originalFile);
        SavedDataType<InfrastructureSavedData> type = new SavedDataType<>(CreateElectroEnergetics.rl(name),
            () -> InfrastructureSavedData.load(helper.getLevel(), new CompoundTag()),
            InfrastructureSavedData.codec(helper.getLevel()), DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
        try {
            InfrastructureSavedData migrated = LegacySavedData.load(helper.getLevel(), type, name,
                tag -> InfrastructureSavedData.load(helper.getLevel(), tag));
            helper.assertTrue("Legacy disk feeder".equals(migrated.getNodeData(node.node).label), "The legacy save was not imported");
            helper.getLevel().getDataStorage().saveAndJoin();
            helper.assertTrue(Files.isRegularFile(originalFile) && Files.isRegularFile(migratedFile),
                "Legacy migration did not retain its original and write a namespaced save");
            try (SavedDataStorage reopened = new SavedDataStorage(dataDirectory,
                    helper.getLevel().getServer().getFixerUpper(), helper.getLevel().registryAccess())) {
                InfrastructureSavedData restored = reopened.get(type);
                helper.assertTrue(restored != null && "Legacy disk feeder".equals(restored.getNodeData(node.node).label)
                    && node.detachedNodeEntityId.equals(restored.getNodeData(node.node).detachedNodeEntityId),
                    "The migrated saved data did not reopen with its node label and UUID");
            }
        } finally {
            Files.deleteIfExists(originalFile);
            Files.deleteIfExists(migratedFile);
        }
        helper.succeed();
    }

    @GameTest
    public void blockRemovalWithoutNeighborUpdatesRemovesDevice(GameTestHelper helper) {
        BlockPos relative = new BlockPos(2, 2, 2);
        helper.setBlock(relative, CEEBlocks.CREATIVE_RESISTOR.get());
        BlockPos absolute = helper.absolutePos(relative);
        DevicesSavedData devices = DevicesSavedData.load(helper.getLevel());
        helper.assertTrue(devices.getDevice(absolute) != null, "Device was not registered when its block was placed");
        helper.getLevel().setBlock(absolute, Blocks.AIR.defaultBlockState(), 2);
        helper.assertTrue(devices.getDevice(absolute) == null, "Device survived a block replacement with update flag 2");
        helper.succeed();
    }

    @GameTest
    public void invalidWirePacketsDoNotResolveMissingNodes(GameTestHelper helper) {
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        InWorldNode first = new InWorldNode(73, helper.absolutePos(new BlockPos(1, 2, 1)));
        InWorldNode second = new InWorldNode(89, helper.absolutePos(new BlockPos(3, 2, 1)));
        helper.assertTrue(!CEENetwork.canReachNode(player, first), "A missing node was accepted");
        helper.assertTrue(!CEENetwork.canReachWire(player, new NodeConnectionPoint(first, second, 0.5f)), "A missing wire was accepted");
        helper.assertTrue(!CEENetwork.canReachWire(player, new NodeConnectionPoint(first, second, Float.NaN)), "A NaN wire point was accepted");
        helper.assertTrue(!CEENetwork.canReachWire(player, new NodeConnectionPoint(first, second, 1.01f)), "An out-of-range wire point was accepted");
        helper.succeed();
    }

    @GameTest
    public void configOverlayPreservesPrecisionAndCapturedSuppliers(GameTestHelper helper) {
        var server = CEEConfigs.server();
        var capturedResistance = server.resistanceValues.wireResistance;
        double original = capturedResistance.get();
        JsonObject values = server.snapshot();
        JsonObject resistances = values.getAsJsonObject(server.resistanceValues.getName());
        resistances.addProperty("wireResistance", 0.1234567890123);
        values.getAsJsonObject(server.simulationConfig.getName()).addProperty("microTicks", 999);
        try {
            server.applyRemote(values);
            helper.assertTrue(capturedResistance.get() == 0.1234567890123, "Server config overlay lost double precision");
            helper.assertTrue(server.simulationConfig.microTicks.get() == 64, "Server config overlay bypassed bounds");
            resistances.addProperty("wireResistance", Double.NaN);
            boolean rejected = false;
            try { server.applyRemote(values); } catch (IllegalArgumentException expected) { rejected = true; }
            helper.assertTrue(rejected && Double.isFinite(capturedResistance.get()), "Invalid overlay poisoned the active config");
        } finally {
            server.applyRemote(null);
        }
        helper.assertTrue(capturedResistance.get() == original, "Clearing an overlay changed the local value");
        CEEConfigs.reload();
        helper.assertTrue(CEEConfigs.server() == server && server.resistanceValues.wireResistance == capturedResistance,
            "Config reload invalidated an existing wire supplier");
        helper.succeed();
    }

    @GameTest(maxTicks = 180)
    public void mechanicalPressProducesPlantOil(GameTestHelper helper) {
        var registeredRecipe = helper.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE,
            CreateElectroEnergetics.rl("compacting/plant_oil")));
        helper.assertTrue(registeredRecipe.isPresent() && registeredRecipe.get().value() instanceof FluidCompactingRecipe,
            "The fluid compacting recipe was not loaded");
        BlockPos basin = new BlockPos(2, 2, 2);
        BlockPos press = basin.above(2);
        BlockPos motor = press.east();
        helper.setBlock(basin, AllBlocks.BASIN);
        helper.setBlock(press, AllBlocks.MECHANICAL_PRESS.defaultBlockState().setValue(HorizontalKineticBlock.HORIZONTAL_FACING, Direction.EAST));
        helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.defaultBlockState().setValue(CreativeMotorBlock.FACING, Direction.WEST));
        helper.startSequence().thenExecuteAfter(5, () -> {
            helper.getBlockEntity(motor, CreativeMotorBlockEntity.class).generatedSpeed.setValue(128);
            helper.getBlockEntity(basin, BasinBlockEntity.class).itemCapability.setItem(0, new ItemStack(Items.WHEAT_SEEDS));
        }).thenExecuteAfter(120, () -> {
            BasinBlockEntity blockEntity = helper.getBlockEntity(basin, BasinBlockEntity.class);
            int output = 0;
            for (int slot = 0; slot < blockEntity.fluidCapability.size(); slot++) {
                var fluid = blockEntity.fluidCapability.getStack(slot);
                if (fluid.getFluid() == CEEFluids.PLANT_OIL.getSource()) output += fluid.getAmount();
            }
            helper.assertTrue(output == 8_100, "The mechanical press did not produce 100 mB of plant oil: " + output);
            helper.assertTrue(blockEntity.itemCapability.getItem(0).isEmpty(), "Plant oil was produced without consuming its seed");
        }).thenSucceed();
    }

    @GameTest
    public void trainAndCarriagePersistElectricalDataThroughBothApis(GameTestHelper helper) {
        Carriage carriage = new Carriage(new CarriageBogey(AllBlocks.SMALL_BOGEY, false, new CompoundTag()), null, 3);
        IPantographList carriageData = (IPantographList) carriage;
        TrainPantographEntry pantograph = new TrainPantographEntry(new BlockPos(1, 4, 2), new BlockPos(-2, 4, 1),
            CEEPantographTypes.DOUBLE.get(), true, false);
        carriageData.electroEnergetics$setPantographList(List.of(pantograph));
        carriageData.electroEnergetics$setElectricMotor(true);
        Train train = new Train(UUID.randomUUID(), UUID.randomUUID(), null, List.of(carriage), List.of(), false, 7);
        ICEETrainExtension extension = (ICEETrainExtension) train;
        extension.setSoundType(CEEElectricTrainSoundTypes.IGBT.get());
        extension.getElectricTrainData().accumulators = 4;
        extension.getElectricTrainData().accumulatorCharge = 73.1234567890123;
        extension.getElectricTrainData().hasCreativeSource = true;
        extension.getElectricTrainData().lastVoltage = 749.25;

        DimensionPalette dimensions = new DimensionPalette();
        Tag encoded = Train.encode(train, NbtOps.INSTANCE, NbtOps.INSTANCE.empty(), dimensions).getOrThrow();
        Train restored = Train.decode(NbtOps.INSTANCE, encoded, Map.of(), dimensions);
        assertTrainData(helper, restored);
        RegistryFriendlyByteBuf network = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            Train.STREAM_CODEC.encode(network, train);
            helper.assertTrue(network.readableBytes() < 16_384, "Train metadata produced an unexpectedly large packet");
            assertTrainData(helper, Train.STREAM_CODEC.decode(network));
            helper.assertTrue(network.readableBytes() == 0, "Train stream codec did not consume its extension fields");
        } finally {
            network.release();
        }
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        train.write(output, dimensions);
        Train restoredFromView = Train.read(TagValueInput.create(ProblemReporter.DISCARDING,
            helper.getLevel().registryAccess(), output.buildResult()), Map.of(), dimensions);
        assertTrainData(helper, restoredFromView);
        helper.succeed();
    }

    private static void assertTrainData(GameTestHelper helper, Train train) {
        ICEETrainExtension extension = (ICEETrainExtension) train;
        var electrical = extension.getElectricTrainData();
        helper.assertTrue(extension.getSoundType() == CEEElectricTrainSoundTypes.IGBT.get(), "Train sound type changed");
        helper.assertTrue(electrical.accumulators == 4 && electrical.accumulatorCharge == 73.1234567890123
            && electrical.hasCreativeSource && electrical.lastVoltage == 749.25, "Train electrical state changed");
        IPantographList carriage = (IPantographList) train.carriages.getFirst();
        helper.assertTrue(carriage.electroEnergetics$hasElectricMotor(), "Carriage motor flag disappeared");
        helper.assertTrue(carriage.electroEnergetics$getPantographList().size() == 1 && electrical.pantographs.size() == 1,
            "Carriage pantograph was not restored into its train");
        TrainPantographEntry pantograph = carriage.electroEnergetics$getPantographList().getFirst();
        helper.assertTrue(pantograph.originalPos.equals(new BlockPos(1, 4, 2))
            && pantograph.rotatedPos.equals(new BlockPos(-2, 4, 1)) && pantograph.active && !pantograph.facingForward
            && pantograph.type == CEEPantographTypes.DOUBLE.get(), "Pantograph metadata changed");
    }

    @GameTest
    public void schematicCaptureLoadAndRotationPreserveInfrastructure(GameTestHelper helper) {
        BlockPos start = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos end = start.east(2);
        helper.getLevel().setBlock(start, CEEBlocks.CREATIVE_BATTERY.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(end, CEEBlocks.CREATIVE_RESISTOR.get().defaultBlockState(), 3);
        for (BlockPos pos : List.of(start, end)) {
            var state = helper.getLevel().getBlockState(pos);
            ((ElectricalDeviceBlock<?>) state.getBlock()).ensureNodesExist(helper.getLevel(), pos, state);
            helper.getLevel().setBlock(pos.above(), CEEBlocks.CATENARY_HOLDER.get().defaultBlockState(), 3);
        }
        InfrastructureSavedData data = InfrastructureSavedData.load(helper.getLevel());
        InWorldNode first = new InWorldNode(0, start);
        InWorldNode second = new InWorldNode(0, end);
        data.getNodeData(first).label = "Rotated feeder";
        WireAttachment damper = new WireAttachment(CEEWireAttachments.DAMPER.get());
        damper.data.putString("Fixture", "Schematic attachment");
        data.connect(first, second, new WireData(CEEWireTypes.STANDARD.get(), 62.5f, List.of(Pair.of(0.375f, damper)), 4.75));
        data.connectCatenary(start.above(), end.above());

        StructureTemplate original = new StructureTemplate();
        original.fillFromWorld(helper.getLevel(), start, new Vec3i(3, 2, 1), false, List.of(Blocks.AIR));
        CompoundTag encoded = original.save(new CompoundTag());
        CompoundTag infrastructure = encoded.getCompoundOrEmpty("electroenergetics_data");
        helper.assertTrue(infrastructure.getListOrEmpty("Connections").size() == 1, "Schematic captured the wire twice or lost it");
        helper.assertTrue(infrastructure.getListOrEmpty("Catenary").size() == 1, "Schematic lost its catenary");
        StructureTemplate restored = new StructureTemplate();
        restored.load(BuiltInRegistries.BLOCK, encoded);
        helper.assertTrue(restored.save(new CompoundTag()).getCompoundOrEmpty("electroenergetics_data").equals(infrastructure),
            "Schematic infrastructure changed while loading");

        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(Rotation.CLOCKWISE_90);
        BlockPos destination = helper.absolutePos(new BlockPos(6, 2, 2));
        BlockPos targetFirst = destination;
        BlockPos targetSecond = destination.south(2);
        SchematicLevel preview = new SchematicLevel(helper.getLevel());
        restored.placeInWorld(preview, destination, destination, settings, helper.getLevel().getRandom(), 2);
        ISchematicInfrastructureList previewData = (ISchematicInfrastructureList) preview;
        InWorldNodeConnection target = new InWorldNodeConnection(new InWorldNode(0, targetFirst), new InWorldNode(0, targetSecond));
        helper.assertTrue(previewData.getWireConnections().containsKey(target), "Schematic preview did not rotate its wire endpoints");
        helper.assertTrue("Rotated feeder".equals(previewData.getNodeLabels().get(new InWorldNode(0, targetFirst))),
            "Schematic preview did not rotate its node label");

        helper.assertTrue(restored.placeInWorld(helper.getLevel(), destination, destination, settings, helper.getLevel().getRandom(), 2),
            "The captured schematic could not be placed");
        WireData wire = data.getConnectionData(target);
        helper.assertTrue(wire != null && wire.temperature() == 62.5f && wire.length == 4.75
            && wire.attachments().size() == 1 && wire.attachments().getFirst().getFirst() == 0.375f,
            "Placed schematic lost wire metadata");
        helper.assertTrue("Rotated feeder".equals(data.getNodeData(new InWorldNode(0, targetFirst)).label),
            "Placed schematic did not transform its node label");
        helper.assertTrue(data.getAllCatenaryConnections().contains(new CatenaryConnection(targetFirst.above(), targetSecond.above())),
            "Placed schematic did not rotate its catenary endpoints");
        helper.succeed();
    }

    @GameTest(maxTicks = 100)
    public void asyncSimulationPublishesFreshResultsOnTheServerThread(GameTestHelper helper) {
        BlockPos source = new BlockPos(1, 2, 1);
        BlockPos resistor = new BlockPos(4, 2, 1);
        helper.setBlock(source, CEEBlocks.CREATIVE_BATTERY.get());
        helper.setBlock(resistor, CEEBlocks.CREATIVE_RESISTOR.get());
        InfrastructureSavedData data = InfrastructureSavedData.load(helper.getLevel());
        AtomicBoolean listening = new AtomicBoolean(true);
        AtomicInteger completions = new AtomicInteger();
        AtomicBoolean wrongThread = new AtomicBoolean();
        FinishElectricSimulationEvent.EVENT.register(event -> {
            if (listening.get() && event.sd == data) {
                completions.incrementAndGet();
                wrongThread.set(!event.level.getServer().isSameThread());
            }
        });
        helper.startSequence().thenExecuteAfter(5, () -> {
            for (BlockPos relative : List.of(source, resistor)) {
                BlockPos absolute = helper.absolutePos(relative);
                var state = helper.getLevel().getBlockState(absolute);
                ((ElectricalDeviceBlock<?>) state.getBlock()).ensureNodesExist(helper.getLevel(), absolute, state);
            }
            for (int terminal = 0; terminal < 2; terminal++) data.connect(
                new InWorldNode(terminal, helper.absolutePos(source)), new InWorldNode(terminal, helper.absolutePos(resistor)),
                CEEWireTypes.STANDARD.get());
        }).thenExecuteAfter(25, () -> {
            listening.set(false);
            helper.assertTrue(completions.get() >= 10 && !wrongThread.get(), "Electrical completion callbacks did not run each tick on the server thread");
            helper.assertTrue(data.ticker.lastResults != null && data.ticker.lastStats != null,
                "The asynchronous simulator did not publish its results");
            var completed = data.ticker.lastResults;
            helper.runAfterDelay(5, () -> {
                helper.assertTrue(data.ticker.lastResults != completed, "Electrical simulation reused an earlier tick result");
                helper.succeed();
            });
        });
    }
}
