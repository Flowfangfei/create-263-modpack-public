package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.registry.FluidEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.*;

/** Two finite, non-renewable oils, retaining the original source/flowing/bucket IDs. */
public final class CEEFluids {
    private static final OilPair TRANSFORMER = new OilPair("transformer_oil", MapColor.COLOR_GREEN);
    private static final OilPair PLANT = new OilPair("plant_oil", MapColor.COLOR_YELLOW);
    public static final FluidEntry<OilFluid> TRANSFORMER_OIL = TRANSFORMER.entry;
    public static final FluidEntry<OilFluid> PLANT_OIL = PLANT.entry;

    public static void register() {
        TRANSFORMER.registerFluids();
        PLANT.registerFluids();
    }

    public static void registerBlocks() {
        TRANSFORMER.registerBlock();
        PLANT.registerBlock();
    }

    public static void registerItems() {
        TRANSFORMER.registerBucket();
        PLANT.registerBucket();
    }

    private static final class OilPair {
        final String name;
        final MapColor color;
        final OilFluid still = new OilFluid(this, true);
        final OilFluid flowing = new OilFluid(this, false);
        final FluidEntry<OilFluid> entry;
        LiquidBlock block;
        BucketItem bucket;
        boolean fluidsRegistered;

        OilPair(String name, MapColor color) {
            this.name = name;
            this.color = color;
            entry =
                    new FluidEntry<>(
                            CreateElectroEnergetics.rl("flowing_" + name),
                            () -> flowing,
                            () -> still);
        }

        void registerFluids() {
            if (fluidsRegistered) return;
            Registry.register(BuiltInRegistries.FLUID, CreateElectroEnergetics.rl(name), still);
            Registry.register(
                    BuiltInRegistries.FLUID,
                    CreateElectroEnergetics.rl("flowing_" + name),
                    flowing);
            fluidsRegistered = true;
        }

        void registerBlock() {
            var id = CreateElectroEnergetics.rl(name);
            block =
                    new LiquidBlock(
                            still,
                            BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                                    .mapColor(color)
                                    .setId(ResourceKey.create(Registries.BLOCK, id)));
            Registry.register(BuiltInRegistries.BLOCK, id, block);
        }

        void registerBucket() {
            var id = CreateElectroEnergetics.rl(name + "_bucket");
            bucket =
                    new BucketItem(
                            still,
                            new Item.Properties()
                                    .craftRemainder(Items.BUCKET)
                                    .stacksTo(1)
                                    .setId(ResourceKey.create(Registries.ITEM, id)));
            Registry.register(BuiltInRegistries.ITEM, id, bucket);
            net.minecraft.world.level.block.DispenserBlock.registerBehavior(
                    bucket,
                    net.minecraft.world.level.block.DispenserBlock.DISPENSER_REGISTRY.get(
                            Items.WATER_BUCKET));
        }
    }

    public static final class OilFluid extends WaterFluid {
        private final OilPair pair;
        private final boolean source;

        OilFluid(OilPair pair, boolean source) {
            this.pair = pair;
            this.source = source;
        }

        @Override
        public Fluid getFlowing() {
            return pair.flowing;
        }

        @Override
        public Fluid getSource() {
            return pair.still;
        }

        @Override
        public Item getBucket() {
            return pair.bucket == null ? Items.BUCKET : pair.bucket;
        }

        @Override
        public boolean isSame(Fluid other) {
            return other == pair.still || other == pair.flowing;
        }

        @Override
        public int getAmount(FluidState state) {
            return source ? 8 : state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return source;
        }

        @Override
        protected void createFluidStateDefinition(
                StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public BlockState createLegacyBlock(FluidState state) {
            return pair.block
                    .defaultBlockState()
                    .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
        }

        @Override
        public int getDropOff(LevelReader level) {
            return 2;
        }

        @Override
        public int getSlopeFindDistance(LevelReader level) {
            return 3;
        }

        @Override
        public int getTickDelay(LevelReader level) {
            return 10;
        }

        @Override
        protected float getExplosionResistance() {
            return 100;
        }

        @Override
        protected boolean canConvertToSource(ServerLevel level) {
            return false;
        }

        @Override
        public boolean canBeReplacedWith(
                FluidState state,
                BlockGetter level,
                BlockPos pos,
                Fluid other,
                net.minecraft.core.Direction direction) {
            return false;
        }
    }
}
