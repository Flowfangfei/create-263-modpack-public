package dev.khloeleclair.create.additionallogistics.common.registries;

import com.simibubi.create.*;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.contraptions.actors.seat.SeatInteractionBehaviour;
import com.simibubi.create.content.contraptions.actors.seat.SeatMovementBehaviour;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.logistics.packager.PackagerGenerator;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.content.trains.track.TrackTargetingBlockItem;
import com.simibubi.create.foundation.block.DyedBlockList;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.khloeleclair.create.additionallogistics.CreateAdditionalLogistics;
import dev.khloeleclair.create.additionallogistics.client.content.kinetics.lazy.EncasedLazyCogCTBehavior;
import dev.khloeleclair.create.additionallogistics.common.content.contraptions.actors.seats.ShortSeatBlock;
import dev.khloeleclair.create.additionallogistics.common.content.contraptions.actors.seats.TallSeatBlock;
import dev.khloeleclair.create.additionallogistics.common.content.kinetics.lazy.cog.EncasedLazyCogWheelBlock;
import dev.khloeleclair.create.additionallogistics.common.content.kinetics.lazy.cog.LazyCogWheelBlock;
import dev.khloeleclair.create.additionallogistics.common.content.kinetics.lazy.cog.LazyCogwheelBlockItem;
import dev.khloeleclair.create.additionallogistics.common.content.kinetics.lazy.flexible.EncasedFlexibleShaftBlock;
import dev.khloeleclair.create.additionallogistics.common.content.kinetics.lazy.flexible.FlexibleShaftBlock;
import dev.khloeleclair.create.additionallogistics.common.content.kinetics.lazy.shaft.EncasedLazyShaftBlock;
import dev.khloeleclair.create.additionallogistics.common.content.kinetics.lazy.shaft.LazyShaftBlock;
import dev.khloeleclair.create.additionallogistics.common.content.logistics.cashRegister.CashRegisterBlock;
import dev.khloeleclair.create.additionallogistics.common.content.logistics.packageAccelerator.PackageAcceleratorBlock;
import dev.khloeleclair.create.additionallogistics.common.content.logistics.packageEditor.PackageEditorBlock;
import dev.khloeleclair.create.additionallogistics.common.content.trains.networkMonitor.NetworkMonitor;
import dev.khloeleclair.create.additionallogistics.common.content.trains.networkMonitor.NetworkMonitorBlock;
import dev.khloeleclair.create.additionallogistics.common.runtime.CALBlockTransforms;
import net.createmod.catnip.api.data.Couple;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import static com.simibubi.create.api.behaviour.display.DisplaySource.displaySource;
import static com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour.interactionBehaviour;
import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import static com.simibubi.create.foundation.data.TagGen.*;

public class CALBlocks {

    private static final CreateRegistrate REGISTRATE = CreateAdditionalLogistics.REGISTRATE.get();

    static {
        REGISTRATE.defaultCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey());
    }

    // Lazy Shafts
    public static final BlockEntry<LazyShaftBlock> LAZY_SHAFT =
            REGISTRATE.block("lazy_shaft", LazyShaftBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.PODZOL))
                    .transform(axeOrPickaxe())
                    
                    .tag(CALTags.CALBlockTags.LAZY.tag)
                    .tag(CALTags.CALBlockTags.BASIC_SHAFTS.tag)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    
                    .item()
                    .tag(CALTags.CALItemTags.LAZY.tag)
                    .tag(CALTags.CALItemTags.BASIC_SHAFTS.tag)
                    .transform(ModelGen.customItemModel())
                    .register();

    // Encased Lazy Shafts
    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    public static <T extends Block & EncasedBlock, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> encasedLazyShaft() {
        return t -> t.transform(EncasingRegistry.addVariantTo(CALBlocks.LAZY_SHAFT))
                .transform(axeOrPickaxe())
                .tag(CALTags.CALBlockTags.LAZY.tag)
                .tag(CALTags.CALBlockTags.BASIC_SHAFTS.tag)
                .item()
                
                .build();
    }

    public static BlockEntry<EncasedLazyShaftBlock> ANDESITE_ENCASED_LAZY_SHAFT =
            REGISTRATE.block("andesite_encased_lazy_shaft", p -> new EncasedLazyShaftBlock(p, AllBlocks.ANDESITE_CASING::get))
                    .properties(p -> p.mapColor(MapColor.PODZOL))
                    .transform(CALBlockTransforms.encasedLazyShaft("andesite", Create.asResource("block/gearbox"), () -> AllSpriteShifts.ANDESITE_CASING))
                    .transform(encasedLazyShaft())
                    .register();

    public static BlockEntry<EncasedLazyShaftBlock> BRASS_ENCASED_LAZY_SHAFT =
            REGISTRATE.block("brass_encased_lazy_shaft", p -> new EncasedLazyShaftBlock(p, AllBlocks.BRASS_CASING::get))
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
                    .transform(CALBlockTransforms.encasedLazyShaft("brass", () -> AllSpriteShifts.BRASS_CASING))
                    .transform(encasedLazyShaft())
                    .register();

    public static BlockEntry<EncasedLazyShaftBlock> COPPER_ENCASED_LAZY_SHAFT =
            REGISTRATE.block("copper_encased_lazy_shaft", p -> new EncasedLazyShaftBlock(p, AllBlocks.COPPER_CASING::get))
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY))
                    .transform(CALBlockTransforms.encasedLazyShaft("copper", CreateAdditionalLogistics.asResource("block/copper_gearbox"), () -> AllSpriteShifts.COPPER_CASING))
                    .transform(encasedLazyShaft())
                    .register();

    /*public static BlockEntry<EncasedLazyShaftBlock> RAILWAY_ENCASED_LAZY_SHAFT =
            REGISTRATE.block("railway_encased_lazy_shaft", p -> new EncasedLazyShaftBlock(p, AllBlocks.RAILWAY_CASING::get))
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_CYAN))
                    .transform(CALBlockTransforms.layeredEncasedLazyShaft("railway", Create.asResource("block/brass_gearbox"), () -> AllSpriteShifts.RAILWAY_CASING_SIDE, () -> AllSpriteShifts.RAILWAY_CASING))
                    .transform(encasedLazyShaft())
                    .register();*/

    public static BlockEntry<EncasedLazyShaftBlock> INDUSTRIAL_IRON_ENCASED_LAZY_SHAFT =
            REGISTRATE.block("industrial_iron_encased_lazy_shaft", p -> new EncasedLazyShaftBlock(p, AllBlocks.INDUSTRIAL_IRON_BLOCK))
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(CALBlockTransforms.encasedLazyShaftNotConnected("industrial_iron", Create.asResource("block/industrial_iron_block"), CreateAdditionalLogistics.asResource("block/industrial_iron_gearbox")))
                    .transform(encasedLazyShaft())
                    .register();

    public static BlockEntry<EncasedLazyShaftBlock> WEATHERED_IRON_ENCASED_LAZY_SHAFT =
            REGISTRATE.block("weathered_iron_encased_lazy_shaft", p -> new EncasedLazyShaftBlock(p, AllBlocks.WEATHERED_IRON_BLOCK))
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(CALBlockTransforms.encasedLazyShaftNotConnected("weathered_iron", Create.asResource("block/weathered_iron_block"), CreateAdditionalLogistics.asResource("block/weathered_iron_gearbox")))
                    .transform(encasedLazyShaft())
                    .register();

    static {
        REGISTRATE.defaultCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey());
    }

    // Lazy CogWheel
    public static final BlockEntry<LazyCogWheelBlock> LAZY_COGWHEEL =
            REGISTRATE.block("lazy_cogwheel", LazyCogWheelBlock::small)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.sound(SoundType.WOOD).mapColor(MapColor.DIRT))
                    .transform(axeOrPickaxe())
                    
                    .tag(CALTags.CALBlockTags.LAZY.tag)
                    .tag(CALTags.CALBlockTags.LAZY_COGS.tag)
                    
                    .item(LazyCogwheelBlockItem::new)
                    .tag(CALTags.CALItemTags.LAZY.tag)
                    .tag(CALTags.CALItemTags.LAZY_COGS.tag)
                    
                    .build()
                    .register();

    public static final BlockEntry<LazyCogWheelBlock> LAZY_LARGE_COGWHEEL =
            REGISTRATE.block("lazy_large_cogwheel", LazyCogWheelBlock::large)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.sound(SoundType.WOOD).mapColor(MapColor.DIRT))
                    .transform(axeOrPickaxe())
                    
                    .tag(CALTags.CALBlockTags.LAZY.tag)
                    .tag(CALTags.CALBlockTags.LAZY_LARGE_COGS.tag)
                    
                    .item(LazyCogwheelBlockItem::new)
                    .tag(CALTags.CALItemTags.LAZY.tag)
                    .tag(CALTags.CALItemTags.LAZY_LARGE_COGS.tag)
                    .onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "block.createadditionallogistics.lazy_cogwheel"))
                    
                    .build()
                    .register();

    // Encased Lazy CogWheels
    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    public static <T extends Block & EncasedBlock, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> encasedLazyCog(boolean large) {
        return t -> t.transform(EncasingRegistry.addVariantTo(large ? CALBlocks.LAZY_LARGE_COGWHEEL : CALBlocks.LAZY_COGWHEEL))
                .transform(axeOrPickaxe())
                .tag(CALTags.CALBlockTags.LAZY.tag)
                .tag(large ? CALTags.CALBlockTags.LAZY_LARGE_COGS.tag : CALTags.CALBlockTags.LAZY_COGS.tag)
                .item()
                
                .build();
    }

    public static BlockEntry<EncasedLazyCogWheelBlock> ANDESITE_ENCASED_LAZY_COGWHEEL =
            REGISTRATE.block("andesite_encased_lazy_cogwheel", p -> EncasedLazyCogWheelBlock.small(p, AllBlocks.ANDESITE_CASING::get))
                    .properties(p -> p.mapColor(MapColor.PODZOL))
                    .transform(CALBlockTransforms.encasedLazyCogwheel("andesite", () -> AllSpriteShifts.ANDESITE_CASING))
                    .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedLazyCogCTBehavior(AllSpriteShifts.ANDESITE_CASING,
                            Couple.create(AllSpriteShifts.ANDESITE_ENCASED_COGWHEEL_SIDE,
                                    AllSpriteShifts.ANDESITE_ENCASED_COGWHEEL_OTHERSIDE))))
                    .transform(encasedLazyCog(false))
                    .register();

    public static BlockEntry<EncasedLazyCogWheelBlock> ANDESITE_ENCASED_LAZY_LARGE_COGWHEEL =
            REGISTRATE.block("andesite_encased_lazy_large_cogwheel", p -> EncasedLazyCogWheelBlock.large(p, AllBlocks.ANDESITE_CASING::get))
                    .properties(p -> p.mapColor(MapColor.PODZOL))
                    .transform(CALBlockTransforms.encasedLazyLargeCogwheel("andesite", () -> AllSpriteShifts.ANDESITE_CASING))
                    .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedLazyCogCTBehavior(AllSpriteShifts.ANDESITE_CASING,
                            Couple.create(AllSpriteShifts.ANDESITE_ENCASED_COGWHEEL_SIDE,
                                    AllSpriteShifts.ANDESITE_ENCASED_COGWHEEL_OTHERSIDE))))
                    .transform(encasedLazyCog(true))
                    .register();

    public static BlockEntry<EncasedLazyCogWheelBlock> BRASS_ENCASED_LAZY_COGWHEEL =
            REGISTRATE.block("brass_encased_lazy_cogwheel", p -> EncasedLazyCogWheelBlock.small(p, AllBlocks.BRASS_CASING::get))
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
                    .transform(CALBlockTransforms.encasedLazyCogwheel("brass", () -> AllSpriteShifts.BRASS_CASING))
                    .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedLazyCogCTBehavior(AllSpriteShifts.BRASS_CASING,
                            Couple.create(AllSpriteShifts.BRASS_ENCASED_COGWHEEL_SIDE,
                                    AllSpriteShifts.BRASS_ENCASED_COGWHEEL_OTHERSIDE))))
                    .transform(encasedLazyCog(false))
                    .register();

    public static BlockEntry<EncasedLazyCogWheelBlock> BRASS_ENCASED_LAZY_LARGE_COGWHEEL =
            REGISTRATE.block("brass_encased_lazy_large_cogwheel", p -> EncasedLazyCogWheelBlock.large(p, AllBlocks.BRASS_CASING::get))
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
                    .transform(CALBlockTransforms.encasedLazyLargeCogwheel("brass", () -> AllSpriteShifts.BRASS_CASING))
                    .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedLazyCogCTBehavior(AllSpriteShifts.BRASS_CASING,
                            Couple.create(AllSpriteShifts.BRASS_ENCASED_COGWHEEL_SIDE,
                                    AllSpriteShifts.BRASS_ENCASED_COGWHEEL_OTHERSIDE))))
                    .transform(encasedLazyCog(true))
                    .register();

    public static BlockEntry<EncasedLazyCogWheelBlock> COPPER_ENCASED_LAZY_COGWHEEL =
            REGISTRATE.block("copper_encased_lazy_cogwheel", p -> EncasedLazyCogWheelBlock.small(p, AllBlocks.COPPER_CASING::get))
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY))
                    .transform(CALBlockTransforms.encasedLazyCogwheel("copper", () -> AllSpriteShifts.COPPER_CASING))
                    .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedLazyCogCTBehavior(AllSpriteShifts.COPPER_CASING,
                            Couple.create(CALSpriteShifts.COPPER_ENCASED_COGWHEEL_SIDE,
                                    CALSpriteShifts.COPPER_ENCASED_COGWHEEL_OTHERSIDE))))
                    .transform(encasedLazyCog(false))
                    .register();

    public static BlockEntry<EncasedLazyCogWheelBlock> COPPER_ENCASED_LAZY_LARGE_COGWHEEL =
            REGISTRATE.block("copper_encased_lazy_large_cogwheel", p -> EncasedLazyCogWheelBlock.large(p, AllBlocks.COPPER_CASING::get))
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY))
                    .transform(CALBlockTransforms.encasedLazyLargeCogwheel("copper", () -> AllSpriteShifts.COPPER_CASING))
                    .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedLazyCogCTBehavior(AllSpriteShifts.COPPER_CASING,
                            Couple.create(CALSpriteShifts.COPPER_ENCASED_COGWHEEL_SIDE,
                                    CALSpriteShifts.COPPER_ENCASED_COGWHEEL_OTHERSIDE))))
                    .transform(encasedLazyCog(true))
                    .register();

    public static BlockEntry<EncasedLazyCogWheelBlock> INDUSTRIAL_IRON_ENCASED_LAZY_COGWHEEL =
            REGISTRATE.block("industrial_iron_encased_lazy_cogwheel", p -> EncasedLazyCogWheelBlock.small(p, AllBlocks.INDUSTRIAL_IRON_BLOCK))
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(CALBlockTransforms.encasedLazyCogwheel("industrial_iron", null))
                    .transform(encasedLazyCog(false))
                    .register();

    public static BlockEntry<EncasedLazyCogWheelBlock> INDUSTRIAL_IRON_ENCASED_LAZY_LARGE_COGWHEEL =
            REGISTRATE.block("industrial_iron_encased_lazy_large_cogwheel", p -> EncasedLazyCogWheelBlock.large(p, AllBlocks.INDUSTRIAL_IRON_BLOCK))
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(CALBlockTransforms.encasedLazyLargeCogwheel("industrial_iron", null))
                    .transform(encasedLazyCog(true))
                    .register();

    public static BlockEntry<EncasedLazyCogWheelBlock> WEATHERED_IRON_ENCASED_LAZY_COGWHEEL =
            REGISTRATE.block("weathered_iron_encased_lazy_cogwheel", p -> EncasedLazyCogWheelBlock.small(p, AllBlocks.WEATHERED_IRON_BLOCK))
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(CALBlockTransforms.encasedLazyCogwheel("weathered_iron", null))
                    .transform(encasedLazyCog(false))
                    .register();

    public static BlockEntry<EncasedLazyCogWheelBlock> WEATHERED_IRON_ENCASED_LAZY_LARGE_COGWHEEL =
            REGISTRATE.block("weathered_iron_encased_lazy_large_cogwheel", p -> EncasedLazyCogWheelBlock.large(p, AllBlocks.WEATHERED_IRON_BLOCK))
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(CALBlockTransforms.encasedLazyLargeCogwheel("weathered_iron", null))
                    .transform(encasedLazyCog(true))
                    .register();

    static {
        REGISTRATE.defaultCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey());
    }

    // Flexible Shafts

    public static final BlockEntry<FlexibleShaftBlock> FLEXIBLE_SHAFT =
            REGISTRATE.block("flexible_shaft", FlexibleShaftBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.PODZOL))
                    .transform(axeOrPickaxe())
                    .lang("Flexible Lazy Shaft")
                    
                    .tag(CALTags.CALBlockTags.LAZY.tag)
                    .tag(CALTags.CALBlockTags.FLEXIBLE_SHAFTS.tag)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    
                    .item()
                    .tag(CALTags.CALItemTags.LAZY.tag)
                    .tag(CALTags.CALItemTags.FLEXIBLE_SHAFTS.tag)
                    .transform(ModelGen.customItemModel())
                    .register();


    public static final DyedBlockList<FlexibleShaftBlock> DYED_FLEXIBLE_SHAFTS = new DyedBlockList<>(color -> {
        String colorName = color.getSerializedName();
        return REGISTRATE.block(colorName + "_flexible_shaft", p -> new FlexibleShaftBlock(p, color))
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.mapColor(color))
                .transform(axeOrPickaxe())
                .lang(RegistrateLangProvider.toEnglishName(colorName + "_flexible_lazy_shaft"))
                
                .tag(CALTags.CALBlockTags.LAZY.tag)
                .tag(CALTags.CALBlockTags.FLEXIBLE_SHAFTS.tag)
                .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                
                .onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "block.createadditionallogistics.flexible_shaft"))
                .item()
                .tag(CALTags.CALItemTags.LAZY.tag)
                .tag(CALTags.CALItemTags.FLEXIBLE_SHAFTS.tag)
                .tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey(), (c,p) -> p.accept(c.get(), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY))
                .build()
                .register();
    });

    // Encased Flexible Shafts

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    public static <T extends EncasedFlexibleShaftBlock, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> encasedFlexibleShaft() {
        return encasedFlexibleShaft(null);
    }

    public static <T extends EncasedFlexibleShaftBlock, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> encasedFlexibleShaft(@Nullable DyeColor color) {
        return t -> t.transform(EncasingRegistry.addVariantTo(color == null ? CALBlocks.FLEXIBLE_SHAFT : CALBlocks.DYED_FLEXIBLE_SHAFTS.get(color)))
                .transform(axeOrPickaxe())
                .tag(CALTags.CALBlockTags.LAZY.tag)
                .tag(CALTags.CALBlockTags.FLEXIBLE_SHAFTS.tag)
                .item()
                
                .build();
    }

    public static final BlockEntry<EncasedFlexibleShaftBlock> ANDESITE_ENCASED_FLEXIBLE_SHAFT =
            REGISTRATE.block("andesite_encased_flexible_shaft", p -> new EncasedFlexibleShaftBlock(p, AllBlocks.ANDESITE_CASING::get))
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.PODZOL))
                    .transform(CALBlockTransforms.encasedFlexibleShaft("andesite_casing", () -> AllSpriteShifts.ANDESITE_CASING))
                    .transform(encasedFlexibleShaft())
                    .register();

    public static final DyedBlockList<EncasedFlexibleShaftBlock> DYED_ANDESITE_ENCASED_FLEXIBLE_SHAFTS = new DyedBlockList<>(color -> {
        String colorName = color.getSerializedName();
        return REGISTRATE.block("andesite_encased_" + colorName + "_flexible_shaft", p -> new EncasedFlexibleShaftBlock(p, color, AllBlocks.ANDESITE_CASING::get))
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.mapColor(color))
                .transform(CALBlockTransforms.encasedFlexibleShaft("andesite_casing", color, () -> AllSpriteShifts.ANDESITE_CASING))
                .transform(encasedFlexibleShaft(color))
                .register();
    });

    public static final BlockEntry<EncasedFlexibleShaftBlock> BRASS_ENCASED_FLEXIBLE_SHAFT =
            REGISTRATE.block("brass_encased_flexible_shaft", p -> new EncasedFlexibleShaftBlock(p, AllBlocks.BRASS_CASING::get))
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
                    .transform(CALBlockTransforms.encasedFlexibleShaft("brass_casing", () -> AllSpriteShifts.BRASS_CASING))
                    .transform(encasedFlexibleShaft())
                    .register();

    public static final DyedBlockList<EncasedFlexibleShaftBlock> DYED_BRASS_ENCASED_FLEXIBLE_SHAFTS = new DyedBlockList<>(color -> {
        String colorName = color.getSerializedName();
        return REGISTRATE.block("brass_encased_" + colorName + "_flexible_shaft", p -> new EncasedFlexibleShaftBlock(p, color, AllBlocks.BRASS_CASING::get))
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.mapColor(color))
                .transform(CALBlockTransforms.encasedFlexibleShaft("brass_casing", color, () -> AllSpriteShifts.BRASS_CASING))
                .transform(encasedFlexibleShaft(color))
                .register();
    });

    public static final BlockEntry<EncasedFlexibleShaftBlock> COPPER_ENCASED_FLEXIBLE_SHAFT =
            REGISTRATE.block("copper_encased_flexible_shaft", p -> new EncasedFlexibleShaftBlock(p, AllBlocks.COPPER_CASING::get))
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY))
                    .transform(CALBlockTransforms.encasedFlexibleShaft("copper_casing", () -> AllSpriteShifts.COPPER_CASING))
                    .transform(encasedFlexibleShaft())
                    .register();

    public static final DyedBlockList<EncasedFlexibleShaftBlock> DYED_COPPER_ENCASED_FLEXIBLE_SHAFTS = new DyedBlockList<>(color -> {
        String colorName = color.getSerializedName();
        return REGISTRATE.block("copper_encased_" + colorName + "_flexible_shaft", p -> new EncasedFlexibleShaftBlock(p, color, AllBlocks.COPPER_CASING::get))
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.mapColor(color))
                .transform(CALBlockTransforms.encasedFlexibleShaft("copper_casing", color, () -> AllSpriteShifts.COPPER_CASING))
                .transform(encasedFlexibleShaft(color))
                .register();
    });

    public static final BlockEntry<EncasedFlexibleShaftBlock> INDUSTRIAL_IRON_ENCASED_FLEXIBLE_SHAFT =
            REGISTRATE.block("industrial_iron_encased_flexible_shaft", p -> new EncasedFlexibleShaftBlock(p, AllBlocks.INDUSTRIAL_IRON_BLOCK::get))
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(CALBlockTransforms.encasedFlexibleShaft("industrial_iron_block", null))
                    .transform(encasedFlexibleShaft())
                    .register();

    public static final DyedBlockList<EncasedFlexibleShaftBlock> DYED_INDUSTRIAL_IRON_ENCASED_FLEXIBLE_SHAFTS = new DyedBlockList<>(color -> {
        String colorName = color.getSerializedName();
        return REGISTRATE.block("industrial_iron_encased_" + colorName + "_flexible_shaft", p -> new EncasedFlexibleShaftBlock(p, color, AllBlocks.INDUSTRIAL_IRON_BLOCK::get))
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.mapColor(color))
                .transform(CALBlockTransforms.encasedFlexibleShaft("industrial_iron_block", color, null))
                .transform(encasedFlexibleShaft(color))
                .register();
    });

    public static final BlockEntry<EncasedFlexibleShaftBlock> WEATHERED_IRON_ENCASED_FLEXIBLE_SHAFT =
            REGISTRATE.block("weathered_iron_encased_flexible_shaft", p -> new EncasedFlexibleShaftBlock(p, AllBlocks.WEATHERED_IRON_BLOCK::get))
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(CALBlockTransforms.encasedFlexibleShaft("weathered_iron_block", null))
                    .transform(encasedFlexibleShaft())
                    .register();

    public static final DyedBlockList<EncasedFlexibleShaftBlock> DYED_WEATHERED_IRON_ENCASED_FLEXIBLE_SHAFTS = new DyedBlockList<>(color -> {
        String colorName = color.getSerializedName();
        return REGISTRATE.block("weathered_iron_encased_" + colorName + "_flexible_shaft", p -> new EncasedFlexibleShaftBlock(p, color, AllBlocks.WEATHERED_IRON_BLOCK::get))
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.mapColor(color))
                .transform(CALBlockTransforms.encasedFlexibleShaft("weathered_iron_block", color, null))
                .transform(encasedFlexibleShaft(color))
                .register();
    });

    // Network Monitor
    static {
        // Don't list the network monitor in a creative tab if CC: Tweaked isn't installed.
        REGISTRATE.defaultCreativeTab(Mods.COMPUTERCRAFT.isLoaded() ? AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey() : null);
    }

    public static final BlockEntry<NetworkMonitorBlock> NETWORK_MONITOR =
            REGISTRATE.block("network_monitor", NetworkMonitorBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.PODZOL)
                            .noOcclusion()
                            .sound(SoundType.NETHERITE_BLOCK))
                    .transform(pickaxeOnly())
                    
                    .lang("Train Network Monitor Peripheral")
                    
                    .item(TrackTargetingBlockItem.ofType(NetworkMonitor.NETWORK_MONITOR))
                    .build()
                    .register();

    static {
        REGISTRATE.defaultCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey());
    }

    // Vertical Belt
    /*public static final BlockEntry<VerticalBeltBlock> VERTICAL_BELT =
            REGISTRATE.block("vertical_belt", VerticalBeltBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .transform(axeOrPickaxe())
                    .register();*/

    // Cash Register
    public static final BlockEntry<CashRegisterBlock> CASH_REGISTER =
            REGISTRATE.block("cash_register", CashRegisterBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.sound(SoundType.METAL))
                    .transform(TagGen.axeOrPickaxe())
                    
                    .tag(AllTags.AllBlockTags.WRENCH_PICKUP.tag)
                    
                    .item(LogisticallyLinkedBlockItem::new)
                    
                    .build()
                    .register();

    // Package Accelerator
    public static final BlockEntry<PackageAcceleratorBlock> PACKAGE_ACCELERATOR =
            REGISTRATE.block("package_accelerator", PackageAcceleratorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(DyeColor.GRAY))
                    
                    .transform(axeOrPickaxe())
                    .tag(AllTags.AllBlockTags.WRENCH_PICKUP.tag)
                    
                    .item()
                    
                    .build()
                    .register();

    // Package Editor
    public static final BlockEntry<PackageEditorBlock> PACKAGE_EDITOR =
            REGISTRATE.block("package_editor", PackageEditorBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p
                            .noOcclusion()
                            .isRedstoneConductor((a,b,c) -> false)
                            .mapColor(MapColor.TERRACOTTA_CYAN)
                            .sound(SoundType.NETHERITE_BLOCK)
                    )
                    .transform(pickaxeOnly())
                    
                    .tag(AllTags.AllBlockTags.WRENCH_PICKUP.tag)
                    
                    .item()
                    
                    .build()
                    .register();

    static {
        REGISTRATE.defaultCreativeTab(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getKey());
    }

    // Short Seats
    public static final DyedBlockList<ShortSeatBlock> SHORT_SEATS = new DyedBlockList<>(color -> {
        String colorName = color.getSerializedName();
        SeatMovementBehaviour movementBehaviour = new SeatMovementBehaviour();
        SeatInteractionBehaviour interactionBehaviour = new SeatInteractionBehaviour();
        return REGISTRATE.block(colorName + "_short_seat", p -> new ShortSeatBlock(p, color))
                .initialProperties(SharedProperties::wooden)
                .properties(p -> p.mapColor(color))
                .transform(axeOnly())
                .onRegister(movementBehaviour(movementBehaviour))
                .onRegister(interactionBehaviour(interactionBehaviour))
                .transform(displaySource(AllDisplaySources.ENTITY_NAME))
                
                /**/
                
                .onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "block.create.seat"))
                .tag(CALTags.CALBlockTags.SHORT_SEATS.tag)
                .item()
                .tag(CALTags.CALItemTags.SHORT_SEATS.tag)
                .tab(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getKey(), (c,p) -> p.accept(c.get(), color == DyeColor.RED ? CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS : CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY))
                .build()
                .register();
    });

    // Tall Seats
    public static final DyedBlockList<TallSeatBlock> TALL_SEATS = new DyedBlockList<>(color -> {
        String colorName = color.getSerializedName();
        SeatMovementBehaviour movementBehaviour = new SeatMovementBehaviour();
        SeatInteractionBehaviour interactionBehaviour = new SeatInteractionBehaviour();
        return REGISTRATE.block(colorName + "_tall_seat", p -> new TallSeatBlock(p, color))
                .initialProperties(SharedProperties::wooden)
                .properties(p -> p.mapColor(color))
                .transform(axeOnly())
                .onRegister(movementBehaviour(movementBehaviour))
                .onRegister(interactionBehaviour(interactionBehaviour))
                .transform(displaySource(AllDisplaySources.ENTITY_NAME))
                
                /**/
                
                .onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "block.create.seat"))
                .tag(CALTags.CALBlockTags.TALL_SEATS.tag)
                .item()
                .tag(CALTags.CALItemTags.TALL_SEATS.tag)
                .tab(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getKey(), (c,p) -> p.accept(c.get(), color == DyeColor.RED ? CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS : CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY))
                .build()
                .register();
    });

    public static void register() { }

}
