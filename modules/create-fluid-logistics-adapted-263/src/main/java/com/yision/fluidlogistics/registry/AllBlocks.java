package com.yision.fluidlogistics.registry;

import static com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour.interactionBehaviour;
import static com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType.mountedFluidStorage;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static com.yision.fluidlogistics.FluidLogistics.REGISTRATE;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.interaction.ConductorBlockInteractionBehavior;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.fluids.PipeAttachmentModel;
import com.simibubi.create.content.processing.basin.BasinMovementBehaviour;
import com.yision.fluidlogistics.content.processing.snowGolemFreezer.SnowGolemFreezerBlockItem;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.yision.fluidlogistics.content.equipment.mechanicalFluidGun.MechanicalFluidGunBlock;
import com.yision.fluidlogistics.content.equipment.mechanicalFluidGun.MechanicalFluidGunItem;
import com.yision.fluidlogistics.content.fluids.faucet.FaucetBlock;
import com.yision.fluidlogistics.content.fluids.faucet.SmartFaucetBlock;
import com.yision.fluidlogistics.content.fluids.flowMeter.FlowMeterBlock;
import com.yision.fluidlogistics.content.fluids.fluidHatch.FluidHatchBlock;
import com.yision.fluidlogistics.content.fluids.fluidPort.FluidInventoryAccessPortBlock;
import com.yision.fluidlogistics.content.fluids.fluidPort.MultiFluidAccessPortBlock;
import com.yision.fluidlogistics.content.fluids.fluidPump.FluidPumpBlock;
import com.yision.fluidlogistics.content.fluids.horizontalMultiFluidTank.HorizontalMultiFluidTankBlock;
import com.yision.fluidlogistics.content.fluids.horizontalMultiFluidTank.HorizontalMultiFluidTankItem;
import com.yision.fluidlogistics.content.fluids.horizontalMultiFluidTank.HorizontalMultiFluidTankModel;
import com.yision.fluidlogistics.content.fluids.infiniteFluidTank.InfiniteFluidTankBlock;
import com.yision.fluidlogistics.content.fluids.infiniteFluidTank.InfiniteFluidTankItem;
import com.yision.fluidlogistics.content.fluids.multiFluidTank.MultiFluidTankBlock;
import com.yision.fluidlogistics.content.fluids.multiFluidTank.MultiFluidTankItem;
import com.yision.fluidlogistics.content.fluids.multiFluidTank.MultiFluidTankModel;
import com.yision.fluidlogistics.content.fluids.redstoneFluidValve.RedstoneFluidValveBlock;
import com.yision.fluidlogistics.content.fluids.waterContainingCopperCasing.WaterContainingCopperCasingBlock;
import com.yision.fluidlogistics.content.fluids.waterContainingCopperCasing.WaterContainingCopperCasingItem;
import com.yision.fluidlogistics.content.logistics.copperFrogport.CopperFrogportBlock;
import com.yision.fluidlogistics.content.logistics.copperFrogport.CopperFrogportItem;
import com.yision.fluidlogistics.content.logistics.fluidPackager.FluidPackagerBlock;
import com.yision.fluidlogistics.content.logistics.fluidPackager.repackager.FluidRepackagerBlock;
import com.yision.fluidlogistics.content.logistics.fluidTransporter.FluidTransporterBlock;
import com.yision.fluidlogistics.content.logistics.potatoServer.PotatoServerBlock;
import com.yision.fluidlogistics.content.logistics.smartHopper.SmartHopperBlock;
import com.yision.fluidlogistics.content.processing.copperBasin.CopperBasinBlock;
import com.yision.fluidlogistics.content.processing.snowGolemFreezer.SnowGolemFreezerBlock;
import com.yision.fluidlogistics.content.processing.snowGolemFreezer.SnowGolemFreezerMovementBehaviour;
import com.yision.fluidlogistics.content.schematics.cannon.CopperSchematicannonBlock;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.material.MapColor;


public class AllBlocks {

	public static final BlockEntry<PotatoServerBlock> POTATO_SERVER = REGISTRATE
		.block("potato_server", PotatoServerBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion())
		.transform(pickaxeOnly())
		.onRegisterAfter(CreateRegistries.DISPLAY_SOURCE, block -> {
			DisplaySource.BY_BLOCK.add(block, AllFluidLogisticsDisplaySources.POTATO_TPS.get());
			DisplaySource.BY_BLOCK.add(block, AllFluidLogisticsDisplaySources.POTATO_MSPT.get());
			DisplaySource.BY_BLOCK.add(block, AllFluidLogisticsDisplaySources.POTATO_MEMORY.get());
			DisplaySource.BY_BLOCK.add(block, AllFluidLogisticsDisplaySources.POTATO_CPU.get());
			DisplaySource.BY_BLOCK.add(block, AllFluidLogisticsDisplaySources.POTATO_UPLOAD.get());
			DisplaySource.BY_BLOCK.add(block, AllFluidLogisticsDisplaySources.POTATO_DOWNLOAD.get());
			DisplaySource.BY_BLOCK.add(block, AllFluidLogisticsDisplaySources.POTATO_TIME.get());
			DisplaySource.BY_BLOCK.add(block, AllFluidLogisticsDisplaySources.POTATO_ENTITIES.get());
			DisplaySource.BY_BLOCK.add(block, AllFluidLogisticsDisplaySources.POTATO_DROPPED_ITEMS.get());
			DisplaySource.BY_BLOCK.add(block, AllFluidLogisticsDisplaySources.POTATO_LOADED_CHUNKS.get());
			DisplaySource.BY_BLOCK.add(block, AllFluidLogisticsDisplaySources.POTATO_GC_COUNT.get());
			DisplaySource.BY_BLOCK.add(block, AllFluidLogisticsDisplaySources.POTATO_GC_TIME.get());
		})
		
		
		
		.item()
		
		.build()
		.register();

	public static final BlockEntry<SnowGolemFreezerBlock> SNOW_GOLEM_FREEZER = REGISTRATE
		.block("snow_golem_freezer", SnowGolemFreezerBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.ICE).lightLevel(state -> 0))
		.transform(pickaxeOnly())
		
		.tag(AllBlockTags.FAN_TRANSPARENT.tag)
		
		
		.onRegister(b -> MovementBehaviour.REGISTRY.register(b, new SnowGolemFreezerMovementBehaviour()))
		.onRegister(interactionBehaviour(new ConductorBlockInteractionBehavior.BlazeBurner()))
		.item(SnowGolemFreezerBlockItem::withSnowGolem)
		
		.build()
		.register();

	public static final BlockEntry<CopperFrogportBlock> COPPER_FROGPORT = REGISTRATE
		.block("copper_frogport", CopperFrogportBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.TERRACOTTA_BLUE)
			.sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		
		
		.item(CopperFrogportItem::new)
		
		.build()
		.register();

	public static final BlockEntry<FluidTransporterBlock> FLUID_TRANSPORTER = REGISTRATE
		.block("fluid_transporter", FluidTransporterBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.noOcclusion().isRedstoneConductor(($1, $2, $3) -> false))
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW).sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		
		
		.item()
		
		.build()
		.register();

	public static final BlockEntry<FluidPackagerBlock> FLUID_PACKAGER = REGISTRATE
		.block("fluid_packager", FluidPackagerBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.noOcclusion())
		.properties(p -> p.isRedstoneConductor(($1, $2, $3) -> false))
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_BLUE)
			.sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		
		
		.item()
		
		.build()
		.register();

	public static final BlockEntry<FluidRepackagerBlock> FLUID_REPACKAGER = REGISTRATE
		.block("fluid_repackager", FluidRepackagerBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.noOcclusion())
		.properties(p -> p.isRedstoneConductor(($1, $2, $3) -> false))
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_BLUE)
			.sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		
		
		.item()
		
		.build()
		.register();

	public static final BlockEntry<SmartFaucetBlock> SMART_FAUCET = REGISTRATE
		.block("smart_faucet", SmartFaucetBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion().isRedstoneConductor(($1, $2, $3) -> false))
		.properties(p -> p.mapColor(MapColor.COLOR_ORANGE).sound(SoundType.COPPER))
		.transform(pickaxeOnly())
		
		
		.item()
		
		.build()
		.register();

	public static final BlockEntry<FaucetBlock> FAUCET = REGISTRATE.block("faucet", FaucetBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion().isRedstoneConductor(($1, $2, $3) -> false))
		.properties(p -> p.mapColor(MapColor.COLOR_ORANGE).sound(SoundType.COPPER))
		.transform(pickaxeOnly())
		
		
		.item()
		
		.build()
		.register();

	public static final BlockEntry<MultiFluidTankBlock> MULTI_FLUID_TANK = REGISTRATE
		.block("multi_fluid_tank", MultiFluidTankBlock::regular)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion()
			.isRedstoneConductor((p1, p2, p3) -> true))
		.transform(pickaxeOnly())
		
		
		.onRegister(CreateRegistrate.blockModel(() -> MultiFluidTankModel::standard))
		.item(MultiFluidTankItem::new)
		
		.build()
		.transform(mountedFluidStorage(AllMountedStorageTypes.MULTI_FLUID_TANK))
		.register();

	public static final BlockEntry<HorizontalMultiFluidTankBlock> HORIZONTAL_MULTI_FLUID_TANK = REGISTRATE
		.block("horizontal_multi_fluid_tank", HorizontalMultiFluidTankBlock::regular)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion()
			.isRedstoneConductor((p1, p2, p3) -> true))
		.transform(pickaxeOnly())
		
		
		.onRegister(CreateRegistrate.blockModel(() -> HorizontalMultiFluidTankModel::standard))
		.item(HorizontalMultiFluidTankItem::new)
		
		.build()
		.transform(mountedFluidStorage(AllMountedStorageTypes.HORIZONTAL_MULTI_FLUID_TANK))
		.register();

	public static final BlockEntry<MultiFluidAccessPortBlock> MULTI_FLUID_ACCESS_PORT = REGISTRATE
		.block("multi_fluid_access_port", MultiFluidAccessPortBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.noOcclusion().isRedstoneConductor(($1, $2, $3) -> false))
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_BLUE).sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		
		
		.item()
		
		.build()
		.register();

	public static final BlockEntry<SmartHopperBlock> SMART_HOPPER = REGISTRATE
		.block("smart_hopper", SmartHopperBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.noOcclusion().isRedstoneConductor(($1, $2, $3) -> false))
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_CYAN).sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		
		
		.item()
		
		.build()
		.register();

	public static final BlockEntry<FlowMeterBlock> FLOW_METER = REGISTRATE
		.block("flow_meter", FlowMeterBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion().isRedstoneConductor(($1, $2, $3) -> false))
		.transform(pickaxeOnly())
		.transform(DisplaySource.displaySource(AllFluidLogisticsDisplaySources.FLOW_RATE))
		
		
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::withAO))
		.item()
		
		.build()
		.register();

	public static final BlockEntry<RedstoneFluidValveBlock> REDSTONE_FLUID_VALVE = REGISTRATE
		.block("redstone_fluid_valve", RedstoneFluidValveBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion().isRedstoneConductor(($1, $2, $3) -> false))
		.transform(pickaxeOnly())
		
		
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::withAO))
		.item()
		
		.build()
		.register();

	public static final BlockEntry<FluidPumpBlock> FLUID_PUMP = REGISTRATE.block("fluid_pump", FluidPumpBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion().isRedstoneConductor(($1, $2, $3) -> false))
		.properties(p -> p.mapColor(MapColor.STONE).sound(SoundType.COPPER))
		.transform(pickaxeOnly())
		
		
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::withAO))
		.item()
		
		.build()
		.register();

	public static final BlockEntry<InfiniteFluidTankBlock> INFINITE_FLUID_TANK = REGISTRATE
		.block("infinite_fluid_tank", InfiniteFluidTankBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.COLOR_PURPLE)
			.isRedstoneConductor(($1, $2, $3) -> true))
		.transform(pickaxeOnly())
		
		
		
		.item(InfiniteFluidTankItem::new)
		.properties(p -> p.rarity(Rarity.EPIC))
		
		.build()
		.register();

	public static final BlockEntry<WaterContainingCopperCasingBlock> WATER_CONTAINING_COPPER_CASING = REGISTRATE
		.block("water_containing_copper_casing", WaterContainingCopperCasingBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion().isRedstoneConductor(($1, $2, $3) -> false))
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY).sound(SoundType.COPPER))
		.transform(pickaxeOnly())
		
		
		.item(WaterContainingCopperCasingItem::new)
		
		.build()
		.register();

	public static final BlockEntry<CopperBasinBlock> COPPER_BASIN = REGISTRATE
		.block("copper_basin", CopperBasinBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(p -> p.noOcclusion()
			.mapColor(MapColor.COLOR_GRAY)
			.sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		
		
		.onRegister(b -> MovementBehaviour.REGISTRY.register(b, new BasinMovementBehaviour()))
		.item()
		
		.build()
		.register();

	public static final BlockEntry<MechanicalFluidGunBlock> MECHANICAL_FLUID_GUN = REGISTRATE
		.block("mechanical_fluid_gun", MechanicalFluidGunBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion().isRedstoneConductor(($1, $2, $3) -> false))
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY).sound(SoundType.COPPER))
		.transform(pickaxeOnly())
		
		
		.item(MechanicalFluidGunItem::new)
		
		.build()
		.register();

	public static final BlockEntry<FluidInventoryAccessPortBlock> FLUID_INVENTORY_ACCESS_PORT = REGISTRATE
		.block("fluid_inventory_access_port", FluidInventoryAccessPortBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.noOcclusion().isRedstoneConductor(($1, $2, $3) -> false))
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_ORANGE).sound(SoundType.NETHERITE_BLOCK))
		.transform(pickaxeOnly())
		
		
		.item()
		
		.build()
		.register();

	public static final BlockEntry<CopperSchematicannonBlock> COPPER_SCHEMATICANNON = REGISTRATE
		.block("copper_schematicannon", CopperSchematicannonBlock::new)
		.initialProperties(() -> Blocks.DISPENSER)
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY))
		.transform(pickaxeOnly())
		
		
		
		.item()
		
		.build()
		.register();

	public static final BlockEntry<FluidHatchBlock> FLUID_HATCH = REGISTRATE.block("fluid_hatch", FluidHatchBlock::new)
		.initialProperties(SharedProperties::copperMetal)
		.properties(p -> p.noOcclusion().isRedstoneConductor(($1, $2, $3) -> false))
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY))
		.transform(pickaxeOnly())
		
		
		.item()
		
		.build()
		.register();

	public static void register() {
	}
}
