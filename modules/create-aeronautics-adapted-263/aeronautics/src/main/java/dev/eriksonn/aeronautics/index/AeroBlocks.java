package dev.eriksonn.aeronautics.index;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.foundation.block.DyedBlockList;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.config.server.AeroStress;
import dev.eriksonn.aeronautics.content.blocks.hot_air.envelope.EnvelopeBlock;
import dev.eriksonn.aeronautics.content.blocks.hot_air.envelope.EnvelopeEncasedShaftBlock;
import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlock;
import dev.eriksonn.aeronautics.content.blocks.hot_air.steam_vent.SteamVentBlock;
import dev.eriksonn.aeronautics.content.blocks.mounted_potato_cannon.MountedPotatoCannonBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.gyroscopic_propeller_bearing.GyroscopicPropellerBearingBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing.PropellerBearingBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.andesite.AndesitePropellerBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.smart_propeller.SmartPropellerBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.wooden.WoodenPropellerBlock;
import dev.eriksonn.aeronautics.content.components.Levitating;
import dev.ryanhcode.sable.index.SableTags;
import dev.simulated_team.simulated.index.SimItems;
import dev.simulated_team.simulated.index.sounds.SimLazySoundType;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import dev.simulated_team.simulated.registrate.simulated_tab.CreativeTabItemTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import io.github.fabricators_of_create.porting_lib.models.generators.ConfiguredModel;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.*;
import io.github.fabricators_of_create.porting_lib.models.generators.ModelBuilder;
import dev.eriksonn.aeronautics.mixin.levitite.FireBlockInvoker;

public class AeroBlocks {
    private static final SimulatedRegistrate REGISTRATE = Aeronautics.getRegistrate();

    private static Boolean neverSpawn(final BlockState state, final BlockGetter blockGetter, final BlockPos pos, final EntityType<?> entity) {
        return false;
    }

    public static final BlockEntry<EnvelopeBlock> WHITE_ENVELOPE_BLOCK = REGISTRATE
            .block("white_envelope", p -> new EnvelopeBlock(p, DyeColor.WHITE))
            .lang("Hot Air Envelope")
            .initialProperties(SharedProperties::wooden)
            .properties(p -> p.isValidSpawn(AeroBlocks::neverSpawn))
            .properties(p -> p.sound(new SimLazySoundType(1.0f, 1.0f,
                    AeroSoundEvents.ENVELOPE_BREAK::event,
                    () -> SoundEvents.WOOL_STEP,
                    AeroSoundEvents.ENVELOPE_PLACE::event,
                    AeroSoundEvents.ENVELOPE_HIT::event,
                    () -> SoundEvents.WOOL_FALL)))
            .properties(p -> p.mapColor(DyeColor.WHITE))


            .tag(AeroTags.BlockTags.ENVELOPE)
            .tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE)
            .transform(flammable(30, 60))
            .item()
            .properties(p -> p.component(DataComponents.COOKING_FUEL,
                    new CookingFuel(new ResolvableInt.Constant(100), new ResolvableFloat.Constant(1.0F))))
            .tag(AeroTags.ItemTags.ENVELOPE)
            .tag(AeroTags.ItemTags.SHAFTLESS_ENVELOPE)
            .build()
            .register();

    public static final DyedBlockList<EnvelopeBlock> DYED_ENVELOPE_BLOCKS = new DyedBlockList<>(color -> {
        String colorName = color.getSerializedName();
        if (color == DyeColor.WHITE) {
            return WHITE_ENVELOPE_BLOCK;
        } else {
            return REGISTRATE.block(colorName + "_envelope", p -> new EnvelopeBlock(p, color))
                    .lang(RegistrateLangProvider.toEnglishName(color.getName()) + " Hot Air Envelope")
                    .initialProperties(SharedProperties::wooden)
                    .properties(p -> p.isValidSpawn(AeroBlocks::neverSpawn))
                    .properties(p -> p.sound(
                            new SimLazySoundType(1.0f, 1.0f,
                                    AeroSoundEvents.ENVELOPE_BREAK::event,
                                    () -> SoundEvents.WOOL_STEP,
                                    AeroSoundEvents.ENVELOPE_PLACE::event,
                                    AeroSoundEvents.ENVELOPE_HIT::event,
                                    () -> SoundEvents.WOOL_FALL)))
                    .properties(p -> p.mapColor(color))


                    .tag(AeroTags.BlockTags.ENVELOPE)
                    .tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE)
                    .transform(CreativeTabItemTransforms.VisibilityType.SEARCH_ONLY.applyBlock())
                    .transform(flammable(30, 60))
                    .item()
                    .properties(p -> p.component(DataComponents.COOKING_FUEL,
                            new CookingFuel(new ResolvableInt.Constant(100), new ResolvableFloat.Constant(1.0F))))
                    .tag(AeroTags.ItemTags.ENVELOPE)
                    .tag(AeroTags.ItemTags.SHAFTLESS_ENVELOPE)
                    .build()
                    .register();
        }
    });

    public static final DyedBlockList<EnvelopeEncasedShaftBlock> ENVELOPE_ENCASED_SHAFTS = new DyedBlockList<>(color -> {
        String colorName = color.getSerializedName();

        return REGISTRATE.block(colorName + "_envelope_encased_shaft", p -> EnvelopeEncasedShaftBlock.withCanvas(p, color))
                .initialProperties(SharedProperties::wooden)
                .properties(p -> p.sound(SoundType.SCAFFOLDING))
                .properties(BlockBehaviour.Properties::noOcclusion)
                .properties(p -> p.sound(
                        new SimLazySoundType(1.0f, 1.0f,
                                AeroSoundEvents.ENVELOPE_BREAK::event,
                                () -> SoundEvents.WOOL_STEP,
                                AeroSoundEvents.ENVELOPE_PLACE::event,
                                AeroSoundEvents.ENVELOPE_HIT::event,
                                () -> SoundEvents.WOOL_FALL)))
                .properties(p -> p.mapColor(color))

                .loot((p, b) -> p.add(b, p.createSingleItemTable(DYED_ENVELOPE_BLOCKS.get(color))
                        .withPool(p.applyExplosionCondition(AllBlocks.SHAFT, LootPool.lootPool()
                                .setRolls(Holder.direct(new ConstantValue(1)))
                                .add(LootItem.lootTableItem(AllBlocks.SHAFT))))))
                .tag(AeroTags.BlockTags.ENVELOPE)
                .transform(axeOnly())
                .onRegister(block -> EncasingRegistry.addVariant(AllBlocks.SHAFT.get(), block))
                .transform(CreativeTabItemTransforms.VisibilityType.INVISIBLE.applyBlock())
                .item()
                .properties(p -> p.component(DataComponents.COOKING_FUEL,
                        new CookingFuel(new ResolvableInt.Constant(100), new ResolvableFloat.Constant(1.0F))))
                .tag(AeroTags.ItemTags.ENVELOPE)
                .transform(b -> b.build())
                .register();
    });

    public static final BlockEntry<HotAirBurnerBlock> HOT_AIR_BURNER =
            REGISTRATE.block("adjustable_burner", HotAirBurnerBlock::new)
                    .lang("Hot Air Burner")
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(p -> p.lightLevel(HotAirBurnerBlock::getLightPower))

                    .transform(dev.simulated_team.simulated.registrate.SimDisplayRegistrations.displaySource(AeroDisplaySources.GAS_DISPLAY))
                    .transform(pickaxeOnly())
                    .item()
                    .transform(customItemModel())

                    .register();

    public static final BlockEntry<SteamVentBlock> STEAM_VENT =
            REGISTRATE.block("steam_vent", SteamVentBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
                    .properties(BlockBehaviour.Properties::noOcclusion)

                    .item()
                    .transform(customItemModel())
                    .transform(dev.simulated_team.simulated.registrate.SimDisplayRegistrations.displaySource(AeroDisplaySources.GAS_DISPLAY))
                    .tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE)

                    .register();

    public static final BlockEntry<PropellerBearingBlock> PROPELLER_BEARING =
            REGISTRATE.block("propeller_bearing", PropellerBearingBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.sound(SoundType.COPPER))
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .transform(AeroStress.setImpact(2.0))

                    .transform(axeOrPickaxe())
                    .item()
                    .transform(customItemModel())

                    .register();
    public static final BlockEntry<GyroscopicPropellerBearingBlock> GYROSCOPIC_PROPELLER_BEARING =
            REGISTRATE.block("gyroscopic_propeller_bearing", GyroscopicPropellerBearingBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.sound(SoundType.COPPER))
                    .transform(AeroStress.setImpact(2.0))
                    .properties(BlockBehaviour.Properties::noOcclusion)

                    .transform(axeOrPickaxe())
                    .item()
                    .transform(customItemModel())

                    .register();

    public static final BlockEntry<SmartPropellerBlock> SMART_PROPELLER =
            REGISTRATE.block("smart_propeller", SmartPropellerBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .transform(axeOrPickaxe())
                    .transform(AeroStress.setImpact(4.0))

                    .item()
                    .transform(customItemModel())

                    .register();

    public static final BlockEntry<AndesitePropellerBlock> ANDESITE_PROPELLER =
            REGISTRATE.block("andesite_propeller", AndesitePropellerBlock::new)
                    .initialProperties(SharedProperties::wooden)
                    .transform(axeOrPickaxe())
                    .properties(p -> p.sound(SoundType.WOOD))
                    .transform(AeroStress.setImpact(4.0))

                    .item()
                    .transform(customItemModel())

                    .register();

    public static final BlockEntry<WoodenPropellerBlock> WOODEN_PROPELLER =
            REGISTRATE.block("wooden_propeller", WoodenPropellerBlock::new)
                    .initialProperties(SharedProperties::wooden)
                    .transform(axeOrPickaxe())
                    .properties(p -> p.sound(SoundType.WOOD))
                    .transform(AeroStress.setImpact(4.0))


                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<MountedPotatoCannonBlock> MOUNTED_POTATO_CANNON =
            REGISTRATE.block("mounted_potato_cannon", MountedPotatoCannonBlock::new)
                    .initialProperties(SharedProperties::stone)

                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .transform(AeroStress.setImpact(2.0))
                    .transform(pickaxeOnly())
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<Block> LEVITITE = REGISTRATE.block("levitite", Block::new)
            .properties(p -> p.lightLevel($ -> 10))
            .properties(BlockBehaviour.Properties::noLootTable)
            .properties(p -> p.strength(7, 20))
            .properties(p -> p.sound(new SimLazySoundType(1.0f, 1.0f,
                    AeroSoundEvents.LEVITITE_BREAK::event,
                    () -> SoundEvents.AMETHYST_BLOCK_STEP,
                    AeroSoundEvents.LEVITITE_PLACE::event,
                    () -> SoundEvents.AMETHYST_BLOCK_HIT,
                    () -> SoundEvents.AMETHYST_BLOCK_FALL)))
            .tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, AeroTags.BlockTags.LEVITITE)
            .tag(SableTags.ALWAYS_CHUNK_RENDERING)
            // 26.3 的区块渲染层由模型纹理透明度推导，不再有运行时注册；
            // 这里声明半透明，RegistrateBlockstateProvider#simpleBlock 才会生成带
            // force_translucent 的 cube_all 模型（否则该模型根本不会被生成）。

            .item(BlockItem::new)
            .tag(AeroTags.ItemTags.LEVITITE)
            .properties(p -> p.component(AeroDataComponents.LEVITATING, Levitating.LEVITITE))
            .build()
            .register();


    public static final BlockEntry<Block> PEARLESCENT_LEVITITE =
            REGISTRATE.block("pearlescent_levitite", Block::new)
                    .properties(p -> p.lightLevel($ -> 10))
                    .properties(BlockBehaviour.Properties::noLootTable)
                    .properties(p -> p.strength(7, 20))
                    .properties(p -> p.sound(new SimLazySoundType(1.0f, 1.0f,
                            AeroSoundEvents.LEVITITE_BREAK::event,
                            () -> SoundEvents.AMETHYST_BLOCK_STEP,
                            AeroSoundEvents.LEVITITE_PLACE::event,
                            () -> SoundEvents.AMETHYST_BLOCK_HIT,
                            () -> SoundEvents.AMETHYST_BLOCK_FALL)))
                    .tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, AeroTags.BlockTags.LEVITITE)
                    .tag(SableTags.ALWAYS_CHUNK_RENDERING)
                    // 同 LEVITITE：声明半透明才会生成带 force_translucent 的模型

                    .item(BlockItem::new)
                    .tag(AeroTags.ItemTags.LEVITITE)
                    .properties(p -> p.component(AeroDataComponents.LEVITATING, Levitating.PEARLESCENT_LEVITITE))
                    .build()
                    .register();

    private static <B extends Block, R> NonNullUnaryOperator<BlockBuilder<B, R>> flammable(final int encouragement, final int flamability) {
        return builder -> builder.onRegisterAfter(Registries.BLOCK, block ->
                ((FireBlockInvoker) Blocks.FIRE).aeronautics$setFlammable(block, encouragement, flamability));
    }

    public static void init() {

    }

}
