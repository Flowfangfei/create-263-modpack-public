package com.tom.createores.block.entity;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import com.tom.createores.util.FluidTankHandler.FluidAction;

import com.tom.createores.CreateOreExcavation;
import com.tom.createores.recipe.ExtractorRecipe;
import com.tom.createores.util.IOBlockType;
import com.tom.createores.util.TooltipUtil;
import com.tom.createores.util.FluidTankHandler;

public class ExtractorBlockEntity extends ExcavatingBlockEntityImpl<ExtractorRecipe> {
	private FluidTankHandler fluidTankOut;

	public ExtractorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		fluidTankOut = new FluidTankHandler(16000, false, this::notifyUpdate);
	}


	@Override
	public <T> T getCaps(BlockCapability<T, Direction> cap, IOBlockType type) {
		if(type == IOBlockType.FLUID_OUT && cap == Capabilities.Fluid.BLOCK) {
			return (T) fluidTankOut;
		}
		return super.getCaps(cap, type);
	}

	@Override
	protected boolean canExtract() {
		return super.canExtract() && fluidTankOut.fillInternal(current.value().getOutput(), FluidAction.SIMULATE) == current.value().getOutput().getAmount();
	}

	@Override
	protected void onFinished() {
		fluidTankOut.fillInternal(current.value().getOutput(), FluidAction.EXECUTE);
		super.onFinished();
	}

	@Override
	protected void read(ValueInput input, boolean clientPacket) {
		super.read(input, clientPacket);
		input.child("tank").ifPresent(fluidTankOut::deserialize);
	}

	@Override
	public void write(ValueOutput output, boolean clientPacket) {
		super.write(output, clientPacket);
		fluidTankOut.serialize(output.child("tank"));
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		TooltipUtil.forGoggles(tooltip, Component.translatable("info.coe.extractor.output"));
		if (!fluidTankOut.getFluid().isEmpty())
			TooltipUtil.forGoggles(tooltip, Component.literal(fluidTankOut.getFluidAmount() + " mB " + fluidTankOut.getFluid().getHoverName().getString()));
		return true;
	}

	@Override
	protected RecipeType<ExtractorRecipe> getRecipeType() {
		return CreateOreExcavation.EXTRACTING_RECIPES.getRecipeType();
	}

	@Override
	protected String getTankInName() {
		return "tankIn";
	}
}
