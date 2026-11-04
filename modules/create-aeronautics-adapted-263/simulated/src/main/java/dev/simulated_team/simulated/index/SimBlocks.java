package dev.simulated_team.simulated.index;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.block.DyedBlockList;
import com.simibubi.create.foundation.block.ItemUseOverrides;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.tterrag.registrate.Registrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import io.github.fabricators_of_create.porting_lib.models.generators.ConfiguredModel;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.config.server.blocks.SimStress;
import dev.simulated_team.simulated.content.blocks.altitude_sensor.AltitudeSensorBlock;
import dev.simulated_team.simulated.content.blocks.altitude_sensor.AltitudeSensorMovementBehaviour;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import dev.simulated_team.simulated.content.blocks.analog_transmission.AnalogTransmissionBlock;
import dev.simulated_team.simulated.content.blocks.auger_shaft.AugerCogBlock;
import dev.simulated_team.simulated.content.blocks.auger_shaft.AugerShaftBlock;
import dev.simulated_team.simulated.content.blocks.directional_gearshift.DirectionalGearshiftBlock;
import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorBlock;
import dev.simulated_team.simulated.content.blocks.docking_connector.PairedDockingConnectorBlock;
import dev.simulated_team.simulated.content.blocks.gimbal_sensor.GimbalSensorBlock;
import dev.simulated_team.simulated.content.blocks.handle.HandleBlock;
import dev.simulated_team.simulated.content.blocks.lasers.laser_pointer.LaserPointerBlock;
import dev.simulated_team.simulated.content.blocks.lasers.laser_sensor.LaserSensorBlock;
import dev.simulated_team.simulated.content.blocks.lasers.optical_sensor.OpticalSensorBlock;
import dev.simulated_team.simulated.content.blocks.merging_glue.MergingGlueBlock;
import dev.simulated_team.simulated.content.blocks.nameplate.NameplateBlock;
import dev.simulated_team.simulated.content.blocks.nameplate.NameplateBlockTarget;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlock;
import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerBlock;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlock;
import dev.simulated_team.simulated.content.blocks.redstone.directional_receiver.DirectionalLinkedReceiverBlock;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlock;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterItem;
import dev.simulated_team.simulated.content.blocks.redstone.modulating_receiver.ModulatingLinkedReceiverBlock;
import dev.simulated_team.simulated.content.blocks.redstone.redstone_accumulator.RedstoneAccumulatorBlock;
import dev.simulated_team.simulated.content.blocks.redstone.redstone_inductor.RedstoneInductorBlock;
import dev.simulated_team.simulated.content.blocks.redstone_magnet.RedstoneMagnetBlock;
import dev.simulated_team.simulated.content.blocks.rope.rope_connector.RopeConnectorBlock;
import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchBlock;
import dev.simulated_team.simulated.content.blocks.spring.SpringBlock;
import dev.simulated_team.simulated.content.blocks.steering_wheel.SteeringWheelBlock;
import dev.simulated_team.simulated.content.blocks.steering_wheel.SteeringWheelBlockEntity;

import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlock;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.link_block.SwivelBearingPlateBlock;
import dev.simulated_team.simulated.content.blocks.symmetric_sail.SymmetricSailBlock;
import dev.simulated_team.simulated.content.blocks.throttle_lever.ThrottleLeverBlock;
import dev.simulated_team.simulated.content.blocks.torsion_spring.TorsionSpringBlock;
import dev.simulated_team.simulated.content.blocks.velocity_sensor.VelocitySensorBlock;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import dev.simulated_team.simulated.registrate.SimDisplayRegistrations;
import dev.simulated_team.simulated.registrate.simulated_tab.CreativeTabItemTransforms;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.*;

@SuppressWarnings("removal")
public class SimBlocks {
    private static final SimulatedRegistrate REGISTRATE = Simulated.getRegistrate();

    public static final BlockEntry<PhysicsAssemblerBlock> PHYSICS_ASSEMBLER = REGISTRATE.block("physics_assembler", PhysicsAssemblerBlock::new)
                    .initialProperties(SharedProperties::wooden)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .tag(BlockTags.MINEABLE_WITH_AXE)

                    .item()
                    .transform(customItemModel())

                    .register();

    public static final BlockEntry<SwivelBearingBlock> SWIVEL_BEARING =
            REGISTRATE.block("swivel_bearing", SwivelBearingBlock::new)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .tag(AllTags.AllBlockTags.NON_MOVABLE.tag)
                    .initialProperties(SharedProperties::netheriteMetal)
                    .properties((properties -> properties.destroyTime(5f)))

                    .transform(SimStress.setImpact(4.0))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .item().transform(customItemModel("swivel_bearing", "item"))

                    .register();

    public static final BlockEntry<SwivelBearingPlateBlock> SWIVEL_BEARING_LINK_BLOCK =
            REGISTRATE.block("swivel_bearing_link_block", SwivelBearingPlateBlock::new)
                    .tag(AllTags.AllBlockTags.NON_MOVABLE.tag)

                    .initialProperties(SharedProperties::netheriteMetal)
                    .properties(properties -> properties
                            .destroyTime(5f))
                    .loot((p, b) -> p.dropOther(b, SWIVEL_BEARING.get()))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .register();

    public static final BlockEntry<MergingGlueBlock> MERGING_GLUE =
            REGISTRATE.block("merging_glue", MergingGlueBlock::new)

                    .properties(p -> p.noOcclusion().instabreak().noLootTable().mapColor(MapColor.GRASS).friction(0.8F).sound(SoundType.SLIME_BLOCK))

                    .register();

    public static final BlockEntry<RopeWinchBlock> ROPE_WINCH =
            REGISTRATE.block("rope_winch", RopeWinchBlock::new)
                    .tag(AllTags.AllBlockTags.NON_MOVABLE.tag)
                    .initialProperties(SharedProperties::stone)

                    .properties(Block.Properties::noOcclusion)
                    .transform(SimStress.setImpact(4.0))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .tag(BlockTags.MINEABLE_WITH_AXE)

                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<RopeConnectorBlock> ROPE_CONNECTOR =
            REGISTRATE.block("rope_connector", RopeConnectorBlock::new)
                    .tag(AllTags.AllBlockTags.NON_MOVABLE.tag)
                    .initialProperties(SharedProperties::stone)

                    .properties(p -> p.noOcclusion().forceSolidOn())
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_AXE, AllTags.AllBlockTags.BRITTLE.tag, SimTags.Blocks.SUPER_LIGHT)

                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<HandleBlock> IRON_HANDLE = createHandle(null, HandleBlock.Variant.IRON)

            .register();

    public static final BlockEntry<HandleBlock> COPPER_HANDLE = createHandle(null, HandleBlock.Variant.COPPER)

            .transform(CreativeTabItemTransforms.VisibilityType.SEARCH_ONLY.applyBlock())
            .register();

    public static final DyedBlockList<HandleBlock> DYED_HANDLES = new DyedBlockList<>(color ->
            createHandle(color, HandleBlock.Variant.DYED)

                .transform(CreativeTabItemTransforms.VisibilityType.SEARCH_ONLY.applyBlock())
                .register()
    );

    public static final BlockEntry<DirectionalGearshiftBlock> DIRECTIONAL_GEARSHIFT = REGISTRATE
            .block("directional_gearshift", DirectionalGearshiftBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion()
                    .isRedstoneConductor(SimBlocks::never)
                    .mapColor(MapColor.PODZOL))

            .transform(axeOrPickaxe())


            .item()
            .transform(customItemModel())
            .register();
    public static final BlockEntry<TorsionSpringBlock> TORSION_SPRING =
            REGISTRATE.block("torsion_spring", TorsionSpringBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(BlockBehaviour.Properties::noOcclusion)

                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .tag(BlockTags.MINEABLE_WITH_AXE)
                    .transform(SimStress.setImpact(16.0))
                    .transform(SimStress.setBlockCapacity(8.0))

                    .item().transform(customItemModel())
                    .register();
    public static final BlockEntry<AugerShaftBlock> AUGER_SHAFT =
            REGISTRATE.block("auger_shaft", AugerShaftBlock::new)
                    .initialProperties(SharedProperties::softMetal)

                    .transform(pickaxeOnly())
                    .transform(SimStress.setImpact(0.5))

                    .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
                    .properties(BlockBehaviour.Properties::noOcclusion)

                    .transform(SimDisplayRegistrations.displaySource(SimDisplaySources.AUGER_DISPLAY))
                    .item().transform(customItemModel())
                    .register();
    public static final BlockEntry<AugerCogBlock> AUGER_COG =
            REGISTRATE.block("auger_cog", AugerCogBlock::new)
                    .initialProperties(SharedProperties::softMetal)

                    .lang("Auger Cogwheel")
                    .transform(pickaxeOnly())
                    .transform(SimStress.setImpact(0.5))

                    .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
                    .properties(BlockBehaviour.Properties::noOcclusion)

                    .transform(SimDisplayRegistrations.displaySource(SimDisplaySources.AUGER_DISPLAY))
                    .item().transform(customItemModel())
                    .register();


    public static final DyedBlockList<PortableEngineBlock> PORTABLE_ENGINES = new DyedBlockList<>(color -> {
        final String colorName = color.getSerializedName();

        if (color == DyeColor.RED) {
            return createPortableEngine(color)

                    .item()

                    .build()
                    .register();
        } else {
            return createPortableEngine(color)
                    .transform(CreativeTabItemTransforms.VisibilityType.SEARCH_ONLY.applyBlock())
                    .item()

                    .build()
                    .register();
        }
    });

    public static final java.util.function.Supplier<PortableEngineBlock> RED_PORTABLE_ENGINE =
            () -> PORTABLE_ENGINES.get(DyeColor.RED).get();

    private static BlockBuilder<PortableEngineBlock, com.simibubi.create.foundation.data.CreateRegistrate> createPortableEngine(final DyeColor color) {
        final String colorName = color.getSerializedName();
        return REGISTRATE.block(colorName + "_portable_engine", p -> new PortableEngineBlock(p, color))
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.sound(SoundType.NETHERITE_BLOCK).lightLevel((state) -> PortableEngineBlock.isLitState(state) ? 6 : 0))
                .properties(BlockBehaviour.Properties::noOcclusion)

                .transform(SimStress.setBlockCapacity(64.0))
                .onRegister(block -> BlockStressValues.setGeneratorSpeed(32).accept(block))
                .transform(SimDisplayRegistrations.displaySource(SimDisplaySources.PORTABLE_ENGINE_DISPLAY))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    public static final BlockEntry<LaserPointerBlock> LASER_POINTER =
            REGISTRATE.block("laser_pointer", LaserPointerBlock::new)
                    .initialProperties(SharedProperties::softMetal)

                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)


                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .tag(BlockTags.MINEABLE_WITH_AXE)
                    .item().transform(customItemModel())
                    .register();

    public static final BlockEntry<LaserSensorBlock> LASER_SENSOR =
            REGISTRATE.block("laser_sensor", LaserSensorBlock::new)
                    .transform(SimDisplayRegistrations.displaySource(SimDisplaySources.LASER_SENSOR_DISPLAY))
                    .initialProperties(SharedProperties::wooden)
                    .properties(BlockBehaviour.Properties::noOcclusion)

                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .tag(BlockTags.MINEABLE_WITH_AXE)

                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<VelocitySensorBlock> VELOCITY_SENSOR =
            REGISTRATE.block("velocity_sensor", VelocitySensorBlock::new)
                    .initialProperties(SharedProperties::wooden)
                    .transform(axeOrPickaxe())
                    .properties(BlockBehaviour.Properties::noOcclusion)

                    .transform(SimDisplayRegistrations.displaySource(SimDisplaySources.VELO_DISPLAY))
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)

                    .item().transform(customItemModel())

                    .register();

    public static final BlockEntry<AltitudeSensorBlock> ALTITUDE_SENSOR =
            REGISTRATE.block("altitude_sensor", AltitudeSensorBlock::new)
                    .initialProperties(SharedProperties::wooden)
                    .transform(axeOrPickaxe())

                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)

                    .transform(SimDisplayRegistrations.displaySource(SimDisplaySources.ALTITUDE_SENSOR_DISPLAY))
                    .onRegister(block -> MovementBehaviour.REGISTRY.register(block, new AltitudeSensorMovementBehaviour()))

                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<GimbalSensorBlock> GIMBAL_SENSOR =
            REGISTRATE.block("gimbal_sensor", GimbalSensorBlock::new)

                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .tag(BlockTags.MINEABLE_WITH_AXE)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .transform(SimDisplayRegistrations.displaySource(SimDisplaySources.GIMBAL_DISPLAY))


                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<NavTableBlock> NAVIGATION_TABLE =
            REGISTRATE.block("navigation_table", NavTableBlock::new)
                    .initialProperties(SharedProperties::wooden)
                    .transform(axeOrPickaxe())

                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)

                    .transform(SimDisplayRegistrations.displaySource(SimDisplaySources.NAV_TABLE_DISPLAY))

                    .item().transform(customItemModel())
                    .register();

    public static final BlockEntry<OpticalSensorBlock> OPTICAL_SENSOR =
            REGISTRATE.block("optical_sensor", OpticalSensorBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .transform(SimDisplayRegistrations.displaySource(SimDisplaySources.OPTICAL_SENSOR_DISPLAY))
                    .properties(BlockBehaviour.Properties::noOcclusion)

                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .tag(BlockTags.MINEABLE_WITH_AXE)
                    .item().transform(customItemModel())

                    .register();

    public static final BlockEntry<DockingConnectorBlock> DOCKING_CONNECTOR =
            REGISTRATE.block("docking_connector", DockingConnectorBlock::new)
                    .tag(AllTags.AllBlockTags.NON_MOVABLE.tag)
                    .initialProperties(SharedProperties::stone)
                    .transform(pickaxeOnly())
                    .properties(p -> p
                            .sound(SoundType.NETHERITE_BLOCK)
                            .isRedstoneConductor(SimBlocks::never)
                            .forceSolidOn()
                    )

                    .transform(SimDisplayRegistrations.displaySource(SimDisplaySources.DOCKING_CONNECTOR_DISPLAY))
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(BlockBehaviour.Properties::dynamicShape)

                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<PairedDockingConnectorBlock> PAIRED_DOCKING_CONNECTOR =
            REGISTRATE.block("paired_docking_connector", PairedDockingConnectorBlock::new)
                    .tag(AllTags.AllBlockTags.NON_MOVABLE.tag)
                    .initialProperties(SharedProperties::stone)

                    .transform(pickaxeOnly())
                    .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
                    .properties(properties -> properties
                            .noOcclusion()
                            .noLootTable()
                            .pushReaction(PushReaction.IMMOVEABLE)
                            .forceSolidOff())
                    .register();

    public static final BlockEntry<AnalogTransmissionBlock> ANALOG_TRANSMISSION =
            REGISTRATE.block("analog_transmission", AnalogTransmissionBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties((p) -> p.noOcclusion()
                            .isRedstoneConductor(SimBlocks::never))

                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .tag(BlockTags.MINEABLE_WITH_AXE)
                    .item().transform(customItemModel())

                    .register();

    public static final BlockEntry<SteeringWheelBlock> STEERING_WHEEL =
            REGISTRATE.block("steering_wheel", SteeringWheelBlock::new)
                    .initialProperties(SharedProperties::wooden)
                    .properties(p -> p.mapColor(MapColor.PODZOL))

                    .transform(axeOrPickaxe())

                    .onRegister(ItemUseOverrides::addBlock)
                    .transform(SimStress.setBlockCapacity(16.0))
                    .onRegister(block -> BlockStressValues.setGeneratorSpeed(SteeringWheelBlockEntity.RPM).accept(block))
                    .tag(SimTags.Blocks.LIGHT)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)

                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<ThrottleLeverBlock> THROTTLE_LEVER =
            REGISTRATE.block("throttle_lever", ThrottleLeverBlock::new)
                    .initialProperties(() -> Blocks.LEVER)
                    .transform(axeOrPickaxe())
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)

                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<LinkedTypewriterBlock> LINKED_TYPEWRITER =
            REGISTRATE.block("linked_typewriter", LinkedTypewriterBlock::new)
                    .initialProperties(SharedProperties::softMetal)

                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .transform(SimDisplayRegistrations.displaySource(SimDisplaySources.LINKED_TYPEWRITER__DISPLAY))
                    .transform(axeOrPickaxe())
                    .item(LinkedTypewriterItem::new)
                    .transform(customItemModel())

                    .register();

    public static final BlockEntry<DirectionalLinkedReceiverBlock> DIRECTIONAL_LINKED_RECEIVER =
            REGISTRATE.block("directional_linked_receiver", DirectionalLinkedReceiverBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.noOcclusion().forceSolidOn())

                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag, AllTags.AllBlockTags.BRITTLE.tag, SimTags.Blocks.SUPER_LIGHT, SimTags.Blocks.QUARTER_VOLUME)
                    .transform(axeOrPickaxe())
                    .item().transform(customItemModel())

                    .register();

    public static final BlockEntry<ModulatingLinkedReceiverBlock> MODULATING_LINKED_RECEIVER =
            REGISTRATE.block("modulating_linked_receiver", ModulatingLinkedReceiverBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.noOcclusion().forceSolidOn())

                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag, AllTags.AllBlockTags.BRITTLE.tag, SimTags.Blocks.SUPER_LIGHT, SimTags.Blocks.QUARTER_VOLUME)
                    .transform(axeOrPickaxe())
                    .item().transform(customItemModel())

                    .register();

    public static final BlockEntry<RedstoneAccumulatorBlock> REDSTONE_ACCUMULATOR =
            REGISTRATE.block("redstone_accumulator", RedstoneAccumulatorBlock::new)
                    .initialProperties(() -> Blocks.REPEATER)

                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag, SimTags.Blocks.DIODE)

                    .item()
                    .transform(customItemModel())

                    .register();

    public static final BlockEntry<RedstoneInductorBlock> REDSTONE_INDUCTOR =
            REGISTRATE.block("redstone_inductor", RedstoneInductorBlock::new)
                    .initialProperties(() -> Blocks.REPEATER)

                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag, SimTags.Blocks.DIODE)

                    .item()
                    .transform(customItemModel())

                    .register();

/*    public static final BlockEntry<AbsorberBlock> ABSORBER =
            REGISTRATE.block("absorber", AbsorberBlock::new)
                    .initialProperties(SharedProperties::copperMetal)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .blockstate((ctx, prov) -> {
                        prov.horizontalBlock(ctx.get(), blockState -> prov.models()
                                .getExistingFile(prov.modLoc("block/" + ctx.getName() + "/block" + (blockState.getValue(BlockStateProperties.POWERED) ? "_powered" : ""))));
                    })
//                    .recipe((c, p) -> p.shaped(RecipeCategory.MISC, c.get(), 1)
//                            .pattern("G")
//                            .pattern("S")
//                            .pattern("R")
//                            .define('G', Blocks.COPPER_GRATE)
//                            .define('S', Blocks.SPONGE)
//                            .define('R', Items.REDSTONE)
//                            .unlockedBy("has_ingredient", RegistrateRecipeProvider.has(SimTags.Items.REDSTONE_DUST))
//                            .save(p))

                    .item().transform(customItemModel())
                    .register();
 */
    public static final BlockEntry<RedstoneMagnetBlock> REDSTONE_MAGNET =
            REGISTRATE.block("redstone_magnet", RedstoneMagnetBlock::new)
                    .initialProperties(SharedProperties::stone)

                    .transform(pickaxeOnly())
                    .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
                    .properties(BlockBehaviour.Properties::noOcclusion)

                    .item()
                    .tag(SimTags.Items.ROTATE_WITH_NAV_ARROW)
                    .transform(customItemModel())

                    .register();

    public static final BlockEntry<SymmetricSailBlock> WHITE_SYMMETRIC_SAIL =
            REGISTRATE.block("white_symmetric_sail", (prov) -> SymmetricSailBlock.withCanvas(prov, DyeColor.WHITE))
                    .initialProperties(SharedProperties::wooden)

                    .properties(p -> p.sound(SoundType.SCAFFOLDING))
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .lang("Symmetric Sail")


                    .tag(BlockTags.MINEABLE_WITH_AXE, AllTags.AllBlockTags.WINDMILL_SAILS.tag, SimTags.Blocks.SYMMETRIC_SAILS)
                    .item()
                    .transform(customItemModel("symmetric_sail/item"))
                    .register();

    public static final DyedBlockList<SymmetricSailBlock> DYED_SYMMETRIC_SAILS = new DyedBlockList<>(colour -> {
        if (colour == DyeColor.WHITE) {
            return WHITE_SYMMETRIC_SAIL;
        } else {
            String colorName = colour.getSerializedName();
            return REGISTRATE.block(colorName + "_symmetric_sail", p -> SymmetricSailBlock.withCanvas(p, colour))
                    .initialProperties(SharedProperties::wooden)

                    .properties(p -> p.sound(SoundType.SCAFFOLDING))
                    .properties(BlockBehaviour.Properties::noOcclusion)

                    .tag(BlockTags.MINEABLE_WITH_AXE, AllTags.AllBlockTags.WINDMILL_SAILS.tag, SimTags.Blocks.SYMMETRIC_SAILS)
                    .loot((p, b) -> p.dropOther(b, WHITE_SYMMETRIC_SAIL.asItem()))
                    .register();
        }
    });

    public static final DyedBlockList<NameplateBlock> NAMEPLATES = new DyedBlockList<>(color -> {
        String colorName = color.getSerializedName();
        return REGISTRATE.block(colorName + "_nameplate", p -> new NameplateBlock(p, color))
                .initialProperties(SharedProperties::wooden)
                .properties(p -> p.forceSolidOn())
                .transform(axeOnly())
                .tag(SimTags.Blocks.NAMEPLATE_BLOCKS)

                .transform(SimDisplayRegistrations.displayTarget(NameplateBlockTarget.NAMEPLATE))
                .transform(x -> {
                    // Only a recipe for the white one
                    return x;
                })
                .item()
                .tag(SimTags.Items.NAMEPLATE_ITEMS)
                .transform(b -> b.build())
                .transform(CreativeTabItemTransforms.VisibilityType.SEARCH_ONLY.conditionalApplyBlock(() -> !colorName.equals(DyeColor.WHITE.getSerializedName())))
                .register();
    });

    public static final BlockEntry<SpringBlock> SPRING =
            REGISTRATE.block("spring", SpringBlock::new)
                    .transform(pickaxeOnly())
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.forceSolidOn())

                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag, AllTags.AllBlockTags.BRITTLE.tag, AllTags.AllBlockTags.NON_MOVABLE.tag, SimTags.Blocks.LIGHT)
                    .loot((tables, block) -> {
                        tables.add(block, tables.createSingleItemTable(SimItems.SPRING));
                    })
                    .register();

    private static BlockBuilder<HandleBlock, com.simibubi.create.foundation.data.CreateRegistrate> createHandle(@Nullable final DyeColor color, final HandleBlock.Variant variant) {
        final String prefix;
        if(variant == HandleBlock.Variant.DYED && color != null) {
            prefix = color.getSerializedName();
        } else {
            prefix = variant.getSerializedName();
        }

        final String name = prefix + "_handle";

        final BlockBuilder<HandleBlock, com.simibubi.create.foundation.data.CreateRegistrate> builder = REGISTRATE.block(name, p -> new HandleBlock(p, color, variant))
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.sound(SoundType.COPPER).noOcclusion().forceSolidOn())
                .tag(SimTags.Blocks.HANDLES)
                .onRegister(ItemUseOverrides::addBlock);


        final ItemBuilder<BlockItem, BlockBuilder<HandleBlock, com.simibubi.create.foundation.data.CreateRegistrate>> itemBuilder = builder.item();

        if(variant != HandleBlock.Variant.IRON) {
            itemBuilder.tag(SimTags.Items.HANDLE_VARIANTS);
        }

        itemBuilder.build();

        return builder;
    }

    private static Boolean never(final BlockState state, final BlockGetter blockGetter, final BlockPos pos) {
        return false;
    }

    public static void register() {
    }

}
