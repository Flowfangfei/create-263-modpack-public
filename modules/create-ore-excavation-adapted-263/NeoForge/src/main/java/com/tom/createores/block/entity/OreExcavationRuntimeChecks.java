package com.tom.createores.block.entity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.tom.createores.*;
import com.tom.createores.block.IOBlock;
import com.tom.createores.components.OreVeinAtlasDataComponent;
import com.tom.createores.components.OreVeinAtlasDataComponent.OreVeinData;
import com.tom.createores.menu.OreVeinAtlasMenu;
import com.tom.createores.util.*;
import com.tom.createores.util.FluidTankHandler.FluidAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Opt-in, deterministic integration checks; only a marked disposable pack world may be changed. */
public final class OreExcavationRuntimeChecks {
    private static final BlockPos DRILL = new BlockPos(72, -58, 8);
    private static final BlockPos EXTRACTOR = new BlockPos(88, -58, 8);
    private static final Identifier IRON = Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "ore_vein_type/iron");
    private static final Identifier WATER = Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "ore_vein_type/water");
    private static LiveNetworkRun liveNetwork;
    private OreExcavationRuntimeChecks() {}

    public static void onServerStarted(ServerStartedEvent event) {
        if (!Boolean.getBoolean("createoreexcavation.runRuntimeChecks")) return;
        try { run(event.getServer().overworld()); }
        catch (Exception error) { CreateOreExcavation.LOGGER.error("COE263_RUNTIME_CHECKS_FAILED", error); }
    }

    /** May also be invoked by the pack's combined probe on the server thread. Does not stop the server. */
    public static void run(ServerLevel level) {
        Path root = Path.of(System.getProperty("create263.probeRoot", "")).toAbsolutePath().normalize();
        if (!Files.isRegularFile(root.resolve(".create263-disposable-runtime")))
            throw new IllegalStateException("Ore Excavation checks require the disposable-runtime marker");
        Path gameDirectory = net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().toAbsolutePath().normalize();
        Path worldDirectory = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toAbsolutePath().normalize();
        if (!gameDirectory.equals(root) || !worldDirectory.equals(root.resolve("world")))
            throw new IllegalStateException("Ore Excavation probeRoot must be the active game directory and contain the active disposable world");
        boolean reload = "reload".equals(System.getProperty("createoreexcavation.runtimeCheckMode", "initial"));
        JsonObject result = new JsonObject();
        result.addProperty("mode", reload ? "reload" : "initial");
        result.addProperty("fixture_drill", "72 -58 8");
        result.addProperty("fixture_extractor", "88 -58 8");
        result.addProperty("rotation_source", "controlled Kinetic interface; live motor network requires separate playtest");
        boolean writeNow = true;
        try {
            if (reload) {
                verifyReload(level, result);
                beginLiveNetwork(level, root, result);
                writeNow = false;
                return;
            }
            initial(level, result);
            result.addProperty("status", "passed");
            CreateOreExcavation.LOGGER.info(reload ? "COE263_RELOAD_CHECKS_OK {}" : "COE263_RUNTIME_CHECKS_OK {}", result);
        } catch (Exception error) {
            result.addProperty("status", "failed");
            result.addProperty("error", error.toString());
            throw new IllegalStateException("Ore Excavation runtime check failed", error);
        } finally {
            if (writeNow) writeResult(root, reload, result);
        }
    }

    private static void initial(ServerLevel level, JsonObject result) {
        int veins = level.recipeAccess().recipeMap().byType(CreateOreExcavation.VEIN_RECIPES.getRecipeType()).size();
        int drills = level.recipeAccess().recipeMap().byType(CreateOreExcavation.DRILLING_RECIPES.getRecipeType()).size();
        int fluids = level.recipeAccess().recipeMap().byType(CreateOreExcavation.EXTRACTING_RECIPES.getRecipeType()).size();
        require(veins >= 15 && drills >= 14 && fluids >= 1, "All shipped custom recipes must decode");
        require(new ItemStack(Registration.NORMAL_DRILL_ITEM.get()).is(CreateOreExcavation.DRILL_TAG), "Drill item tag");
        result.addProperty("vein_recipes", veins); result.addProperty("drilling_recipes", drills); result.addProperty("extracting_recipes", fluids);
        checkTransactions(level); result.addProperty("native_transactions_and_nbt", true);

        DrillBlockEntity drill = (DrillBlockEntity) placeMachine(level, DRILL, new ItemStack(Registration.DRILL_BLOCK_ITEM.get()));
        ExtractorBlockEntity extractor = (ExtractorBlockEntity) placeMachine(level, EXTRACTOR, new ItemStack(Registration.EXTRACTOR_BLOCK_ITEM.get()));
        configureVein(level, DRILL, IRON); configureVein(level, EXTRACTOR, WATER);
        drill.drillStack = new ItemStack(Registration.NORMAL_DRILL_ITEM.get());
        extractor.drillStack = new ItemStack(Registration.NORMAL_DRILL_ITEM.get());
        ControlledKinetic drillPower = new ControlledKinetic(), fluidPower = new ControlledKinetic();
        drill.addKinetic(drillPower); extractor.addKinetic(fluidPower);
        ResourceHandler<ItemResource> drillOutput = drill.getCaps(Capabilities.Item.BLOCK, IOBlockType.ITEM_OUT);
        FluidTankHandler fluidOutput = (FluidTankHandler) extractor.getCaps(Capabilities.Fluid.BLOCK, IOBlockType.FLUID_OUT);
        int drillTicks = 0, fluidTicks = 0;
        while (drillOutput.size() == 0 && drillTicks++ < 700) drill.tick();
        while (fluidOutput.getFluidAmount() == 0 && fluidTicks++ < 100) extractor.tick();
        drill.addKinetic(null); extractor.addKinetic(null);
        require(drillOutput.size() > 0 && drillOutput.getResource(0).matches(new ItemStack(Items.RAW_IRON)), "Iron drill completes a real production cycle");
        require(fluidOutput.getFluid().is(Fluids.WATER) && fluidOutput.getFluidAmount() == 500, "Water extractor produces 500 mB");
        require(drillPower.stress == 256 && fluidPower.stress == 256, "Recipe stress is reported to kinetic input");
        require(drill.data.save().extracted() == 1 && extractor.data.save().extracted() == 1, "Production debits each ore vein once");
        require(drill.completedOneCycle && extractor.completedOneCycle, "Atlas discovery production gate");
        result.addProperty("drill_production_ticks", drillTicks); result.addProperty("extractor_production_ticks", fluidTicks);
        result.addProperty("multiblock_item_placement_and_production", true);

        BlockPos drillIo = findIo(level, DRILL, IOBlockType.ITEM_OUT);
        BlockPos fluidIo = findIo(level, EXTRACTOR, IOBlockType.FLUID_OUT);
        require(level.getCapability(Capabilities.Item.BLOCK, drillIo, Direction.UP) == drillOutput, "Native item capability routes through multiblock IO");
        require(level.getCapability(Capabilities.Fluid.BLOCK, fluidIo, Direction.UP) == fluidOutput, "Native fluid capability routes through multiblock IO");
        try (var tx = Transaction.openRoot()) { require(fluidOutput.extract(0, FluidResource.of(Fluids.WATER), 250, tx) == 250, "Output automation extraction"); }
        require(fluidOutput.getFluidAmount() == 500, "Production fluid output rolls back");
        result.addProperty("native_multiblock_capabilities", true);

        var drillTag = drill.saveWithoutMetadata(level.registryAccess());
        var fluidTag = extractor.saveWithoutMetadata(level.registryAccess());
        DrillBlockEntity drillCopy = new DrillBlockEntity(Registration.DRILL_TILE.get(), DRILL, drill.getBlockState());
        ExtractorBlockEntity extractorCopy = new ExtractorBlockEntity(Registration.EXTRACTOR_TILE.get(), EXTRACTOR, extractor.getBlockState());
        drillCopy.setLevel(level); extractorCopy.setLevel(level);
        drillCopy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), drillTag));
        extractorCopy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), fluidTag));
        ResourceHandler<ItemResource> copyOutput = drillCopy.getCaps(Capabilities.Item.BLOCK, IOBlockType.ITEM_OUT);
        FluidTankHandler copyFluid = (FluidTankHandler) extractorCopy.getCaps(Capabilities.Fluid.BLOCK, IOBlockType.FLUID_OUT);
        require(copyOutput.size() == drillOutput.size() && !drillCopy.getDrill().isEmpty() && drillCopy.completedOneCycle, "Drill BE native serialization");
        require(copyFluid.getFluidAmount() == 500 && !extractorCopy.getDrill().isEmpty(), "Extractor BE native serialization");
        result.addProperty("block_entity_value_io_roundtrip", true);
        checkAtlas(level); result.addProperty("atlas_component_and_menu", true);
        checkSampleDrill(level); result.addProperty("sample_drill_backtank_cycle", true);
        level.getChunkAt(DRILL).markUnsaved(); level.getChunkAt(EXTRACTOR).markUnsaved();
    }

    private static void verifyReload(ServerLevel level, JsonObject result) {
        require(level.getBlockEntity(DRILL) instanceof DrillBlockEntity, "Saved drill exists");
        require(level.getBlockEntity(EXTRACTOR) instanceof ExtractorBlockEntity, "Saved extractor exists");
        DrillBlockEntity drill = (DrillBlockEntity) level.getBlockEntity(DRILL);
        ExtractorBlockEntity extractor = (ExtractorBlockEntity) level.getBlockEntity(EXTRACTOR);
        ResourceHandler<ItemResource> items = drill.getCaps(Capabilities.Item.BLOCK, IOBlockType.ITEM_OUT);
        FluidTankHandler fluids = (FluidTankHandler) extractor.getCaps(Capabilities.Fluid.BLOCK, IOBlockType.FLUID_OUT);
        require(items.size() > 0 && items.getResource(0).matches(new ItemStack(Items.RAW_IRON)), "Drill inventory survived restart");
        require(fluids.getFluidAmount() == 500, "Extractor output survived restart");
        require(drill.completedOneCycle && extractor.completedOneCycle && !drill.getDrill().isEmpty(), "Machine state survived restart");
        OreData iron = OreDataAttachment.getData(level.getChunkAt(DRILL)), water = OreDataAttachment.getData(level.getChunkAt(EXTRACTOR));
        require(IRON.equals(iron.getRecipeId()) && WATER.equals(water.getRecipeId()) && iron.save().extracted() == 1 && water.save().extracted() == 1, "Chunk ore attachment survived restart");
        result.addProperty("inventory_fluid_machine_and_chunk_attachment_restart", true);
    }

    public static void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        LiveNetworkRun pending = liveNetwork;
        if (pending == null || event.getServer() != pending.level.getServer() || ++pending.ticks < 220) return;
        liveNetwork = null;
        try {
            ServerLevel level = pending.level;
            DrillBlockEntity drill = (DrillBlockEntity) level.getBlockEntity(DRILL);
            ExtractorBlockEntity extractor = (ExtractorBlockEntity) level.getBlockEntity(EXTRACTOR);
            KineticInputBlockEntity drillInput = (KineticInputBlockEntity) level.getBlockEntity(DRILL.offset(-1, -1, 0));
            KineticInputBlockEntity extractorInput = (KineticInputBlockEntity) level.getBlockEntity(EXTRACTOR.offset(-1, -1, 0));
            require(drillInput != null && extractorInput != null, "Real kinetic input entities remain present");
            require(Math.abs(drillInput.getSpeed()) == 128 && Math.abs(extractorInput.getSpeed()) == 128, "Creative motor rotation reaches both native input shafts");
            require(drillInput.getRotationSpeed() == 128 && extractorInput.getRotationSpeed() == 128, "Real networks satisfy usable rotation and stress state");
            require(drillInput.calculateStressApplied() == 256 && extractorInput.calculateStressApplied() == 256, "Both real Create networks apply recipe stress 256");
            ResourceHandler<ItemResource> items = drill.getCaps(Capabilities.Item.BLOCK, IOBlockType.ITEM_OUT);
            FluidTankHandler fluid = (FluidTankHandler) extractor.getCaps(Capabilities.Fluid.BLOCK, IOBlockType.FLUID_OUT);
            long producedItems = itemCount(items);
            require(producedItems > pending.itemCount, "Normal world ticks produce additional iron through the real Create network");
            require(fluid.getFluidAmount() > pending.fluidAmount, "Normal world ticks produce additional water through the real Create network");
            require(OreDataAttachment.getData(level.getChunkAt(DRILL)).save().extracted() > 1 && OreDataAttachment.getData(level.getChunkAt(EXTRACTOR)).save().extracted() > 1, "Real network production debits persisted vein attachments");
            pending.result.addProperty("real_create_network_ticks", pending.ticks);
            pending.result.addProperty("real_create_input_rpm", 128);
            pending.result.addProperty("real_create_recipe_stress", 256);
            pending.result.addProperty("iron_items_after_real_network", producedItems);
            pending.result.addProperty("water_mb_after_real_network", fluid.getFluidAmount());
            pending.result.addProperty("real_create_network_and_additional_production", true);
            pending.result.addProperty("status", "passed");
            level.getChunkAt(DRILL).markUnsaved(); level.getChunkAt(EXTRACTOR).markUnsaved();
            CreateOreExcavation.LOGGER.info("COE263_RELOAD_CHECKS_OK {}", pending.result);
        } catch (Exception error) {
            pending.result.addProperty("status", "failed"); pending.result.addProperty("error", error.toString());
            CreateOreExcavation.LOGGER.error("COE263_RELOAD_CHECKS_FAILED", error);
        } finally {
            if (pending.forcedDrill) pending.level.setChunkForced(DRILL.getX() >> 4, DRILL.getZ() >> 4, false);
            if (pending.forcedExtractor) pending.level.setChunkForced(EXTRACTOR.getX() >> 4, EXTRACTOR.getZ() >> 4, false);
            writeResult(pending.root, true, pending.result);
        }
    }

    private static void beginLiveNetwork(ServerLevel level, Path root, JsonObject result) {
        require(liveNetwork == null, "Only one Ore Excavation live-network probe may run");
        DrillBlockEntity drill = (DrillBlockEntity) level.getBlockEntity(DRILL);
        ExtractorBlockEntity extractor = (ExtractorBlockEntity) level.getBlockEntity(EXTRACTOR);
        ResourceHandler<ItemResource> items = drill.getCaps(Capabilities.Item.BLOCK, IOBlockType.ITEM_OUT);
        FluidTankHandler fluid = (FluidTankHandler) extractor.getCaps(Capabilities.Fluid.BLOCK, IOBlockType.FLUID_OUT);
        boolean forcedDrill = level.setChunkForced(DRILL.getX() >> 4, DRILL.getZ() >> 4, true);
        boolean forcedExtractor = level.setChunkForced(EXTRACTOR.getX() >> 4, EXTRACTOR.getZ() >> 4, true);
        placeMotor(level, DRILL.offset(-2, -1, 0));
        placeMotor(level, EXTRACTOR.offset(-2, -1, 0));
        result.addProperty("rotation_source", "two native Create creative motors and ordinary server world ticks");
        liveNetwork = new LiveNetworkRun(level, root, result, itemCount(items), fluid.getFluidAmount(), forcedDrill, forcedExtractor);
        CreateOreExcavation.LOGGER.info("COE263_REAL_NETWORK_CHECKS_STARTED: awaiting 220 normal world ticks");
    }

    private static void placeMotor(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos, com.simibubi.create.AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(com.simibubi.create.content.kinetics.motor.CreativeMotorBlock.FACING, Direction.EAST));
        require(level.getBlockEntity(pos) instanceof com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity, "Native Create creative motor registration");
        var motor = (com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity) level.getBlockEntity(pos);
        motor.generatedSpeed.setValue(128);
    }

    private static long itemCount(ResourceHandler<ItemResource> items) {
        long count = 0;
        for (int index = 0; index < items.size(); index++) count += items.getAmountAsLong(index);
        return count;
    }

    private static void writeResult(Path root, boolean reload, JsonObject result) {
        try { Files.writeString(root.resolve(reload ? "ore-excavation-reload-checks.json" : "ore-excavation-runtime-checks.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result)); }
        catch (Exception error) { CreateOreExcavation.LOGGER.error("Cannot write Ore Excavation runtime result", error); }
    }

    private static final class LiveNetworkRun {
        final ServerLevel level; final Path root; final JsonObject result;
        final long itemCount; final int fluidAmount; final boolean forcedDrill, forcedExtractor;
        int ticks;
        LiveNetworkRun(ServerLevel level, Path root, JsonObject result, long itemCount, int fluidAmount, boolean forcedDrill, boolean forcedExtractor) {
            this.level = level; this.root = root; this.result = result; this.itemCount = itemCount; this.fluidAmount = fluidAmount; this.forcedDrill = forcedDrill; this.forcedExtractor = forcedExtractor;
        }
    }

    private static void checkTransactions(ServerLevel level) {
        QueueInventory queue = new QueueInventory(() -> {}); queue.add(new ItemStack(Items.RAW_IRON, 4));
        ItemResource iron = ItemResource.of(Items.RAW_IRON);
        try (var tx = Transaction.openRoot()) { require(queue.extract(0, iron, 2, tx) == 2, "Queue extracts in transaction"); }
        require(queue.getAmountAsLong(0) == 4, "Queue rollback");
        try (var tx = Transaction.openRoot()) { require(queue.insert(0, iron, 2, tx) == 0, "Queue rejects external insertion"); require(queue.extract(0, iron, 1, tx) == 1, "Queue commit extraction"); tx.commit(); }
        require(queue.getAmountAsLong(0) == 3, "Queue committed count");
        QueueInventory queueCopy = new QueueInventory(() -> {}); queueCopy.load(queue.toTag(level.registryAccess()), level.registryAccess());
        require(queueCopy.getAmountAsLong(0) == 3, "Queue NBT roundtrip");
        FluidResource water = FluidResource.of(Fluids.WATER);
        FluidTankHandler input = new FluidTankHandler(16000, true, () -> {}), output = new FluidTankHandler(16000, false, () -> {});
        try (var tx = Transaction.openRoot()) { require(input.insert(0, water, 1000, tx) == 1000, "Fluid input transaction"); }
        require(input.getFluidAmount() == 0, "Fluid input rollback");
        try (var tx = Transaction.openRoot()) { require(input.insert(0, water, 1000, tx) == 1000, "Fluid input commit"); tx.commit(); }
        try (var tx = Transaction.openRoot()) { require(input.extract(0, water, 500, tx) == 0 && output.insert(0, water, 1000, tx) == 0, "Directional fluid policies"); }
        require(input.drainInternal(200, FluidAction.EXECUTE).getAmount() == 200, "Drilling fluid internal consumption");
        require(output.fillInternal(new FluidStack(Fluids.WATER, 500), FluidAction.SIMULATE) == 500 && output.getFluidAmount() == 0, "Internal output simulation");
        require(output.fillInternal(new FluidStack(Fluids.WATER, 500), FluidAction.EXECUTE) == 500, "Internal output production");
        try (var tx = Transaction.openRoot()) { require(output.extract(0, water, 200, tx) == 200, "Fluid output automation"); tx.commit(); }
        require(output.getFluidAmount() == 300, "Fluid output committed count");
        TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess()); output.serialize(saved);
        FluidTankHandler copy = new FluidTankHandler(16000, false, () -> {}); copy.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
        require(copy.getFluidAmount() == 300, "Native fluid Value IO roundtrip");
    }

    private static BlockEntity placeMachine(ServerLevel level, BlockPos controller, ItemStack item) {
        BlockPos anchor = controller.below(), ground = controller.below(2);
        for (BlockPos pos : BlockPos.betweenClosed(ground.offset(-2, 0, -2), ground.offset(2, 0, 2))) level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        for (BlockPos pos : BlockPos.betweenClosed(anchor.offset(-2, 0, -2), anchor.offset(2, 3, 2))) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        var player = FakePlayerFactory.getMinecraft(level); player.setPos(controller.getX() + .5, controller.getY() + 1, controller.getZ() + 3); player.setYRot(180);
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        var hit = new BlockHitResult(new Vec3(ground.getX() + .5, ground.getY() + 1, ground.getZ() + .5), Direction.UP, ground, false);
        item.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        BlockEntity be = level.getBlockEntity(controller);
        require(be instanceof ExcavatingBlockEntity<?>, "Machine item creates controller and block entity at " + controller);
        return be;
    }

    private static BlockPos findIo(ServerLevel level, BlockPos controller, IOBlockType type) {
        for (BlockPos pos : BlockPos.betweenClosed(controller.offset(-1, -1, -1), controller.offset(1, 0, 1))) {
            var state = level.getBlockState(pos);
            if (state.is(Registration.IO_BLOCK.get()) && state.getValue(IOBlock.TYPE) == type) return pos.immutable();
        }
        throw new IllegalStateException("Missing multiblock port " + type);
    }

    private static void configureVein(ServerLevel level, BlockPos pos, Identifier id) {
        OreData data = OreDataAttachment.getData(level.getChunkAt(pos)); data.setRecipe(id); data.setLoaded(true); data.setRandomMul(.5f); data.setExtractedAmount(0); level.getChunkAt(pos).markUnsaved();
    }

    private static void checkAtlas(ServerLevel level) {
        ItemStack atlas = new ItemStack(Registration.VEIN_ATLAS_ITEM.get());
        var data = new OreVeinAtlasDataComponent(List.of(IRON), List.of(Pair.of(new DimChunkPos(level, DRILL), new OreVeinData(IRON, .5f, false))), List.of(), Optional.of(IRON));
        atlas.set(CreateOreExcavation.ORE_VEIN_ATLAS_DATA_COMPONENT, data);
        var ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        ItemStack copied = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, atlas).getOrThrow()).getOrThrow();
        require(data.equals(copied.get(CreateOreExcavation.ORE_VEIN_ATLAS_DATA_COMPONENT)), "Atlas item component persists");
        var player = FakePlayerFactory.getMinecraft(level); player.setItemInHand(InteractionHand.MAIN_HAND, copied);
        OreVeinAtlasMenu menu = new OreVeinAtlasMenu(Registration.VEIN_ATLAS_MENU.get(), 1, player.getInventory());
        require(menu.stillValid(player) && menu.getFiniteBase() == Config.finiteAmountBase, "Atlas menu registration and config synchronization");
    }

    private static void checkSampleDrill(ServerLevel level) {
        BlockPos pos = new BlockPos(104, -59, 8);
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos, Registration.SAMPLE_DRILL_BLOCK.get().defaultBlockState());
        level.setBlockAndUpdate(pos.above(), com.simibubi.create.AllBlocks.COPPER_BACKTANK.getDefaultState());
        require(level.getBlockEntity(pos) instanceof SampleDrillBlockEntity, "Sample drill registration");
        require(level.getBlockEntity(pos.above()) instanceof com.simibubi.create.content.equipment.armor.BacktankBlockEntity, "Create backtank registration");
        var tank = (com.simibubi.create.content.equipment.armor.BacktankBlockEntity) level.getBlockEntity(pos.above()); tank.setAirLevel(1000);
        var drill = (SampleDrillBlockEntity) level.getBlockEntity(pos); drill.setDrilling(true); drill.lazyTick();
        for (int tick = 0; tick < 200; tick++) drill.tick();
        require(drill.saveWithoutMetadata(level.registryAccess()).getIntOr("progress", 0) >= SampleDrillBlockEntity.DRILL_TIME && tank.getAirLevel() < 1000, "Sample drilling consumes Create compressed air and finishes");
    }

    private static void require(boolean passed, String description) { if (!passed) throw new IllegalStateException(description); }
    private static final class ControlledKinetic implements MultiblockCapHandler.Kinetic {
        private float stress;
        public float getRotationSpeed() { return 128; }
        public void setStress(float stress) { this.stress = stress; }
    }
}