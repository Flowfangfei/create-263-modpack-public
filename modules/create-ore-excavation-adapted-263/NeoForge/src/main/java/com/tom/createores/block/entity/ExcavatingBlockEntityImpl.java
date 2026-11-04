package com.tom.createores.block.entity;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import com.tom.createores.util.FluidTankHandler.FluidAction;

import com.tom.createores.recipe.ExcavatingRecipe;
import com.tom.createores.util.IOBlockType;
import com.tom.createores.util.TooltipUtil;
import com.tom.createores.util.FluidTankHandler;

public abstract class ExcavatingBlockEntityImpl<R extends ExcavatingRecipe> extends ExcavatingBlockEntity<R> {
	protected FluidTankHandler fluidTank;

	protected ExcavatingBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		fluidTank = new FluidTankHandler(16000, true, this::notifyUpdate);
	}

	@Override
	public <T> T getCaps(BlockCapability<T, Direction> cap, IOBlockType type) {
		if(type == IOBlockType.FLUID_IN && cap == Capabilities.Fluid.BLOCK) {
			return (T) fluidTank;
		}
		return null;
	}

	@Override
	protected void read(ValueInput input, boolean clientPacket) {
		super.read(input, clientPacket);
		input.child(getTankInName()).ifPresent(fluidTank::deserialize);
	}

	@Override
	public void write(ValueOutput output, boolean clientPacket) {
		super.write(output, clientPacket);
		fluidTank.serialize(output.child(getTankInName()));
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		TooltipUtil.forGoggles(tooltip, Component.translatable("info.coe.drill.fluidInfo"));
		if (!fluidTank.getFluid().isEmpty())
			TooltipUtil.forGoggles(tooltip, Component.literal(fluidTank.getFluidAmount() + " mB " + fluidTank.getFluid().getHoverName().getString()));
		return true;
	}

	@Override
	public void addToGoggleTooltip(List<Component> tooltip, R rec) {
		rec.getDrillingFluid().ifPresent(fluid -> {
			if (!fluid.test(fluidTank.getFluid()) || fluidTank.getFluidAmount() < fluid.getRequiredAmount()) {
				TooltipUtil.forGoggles(tooltip, Component.translatable("info.coe.drill.noFluid"));
			}
		});
	}

	@Override
	protected boolean validateRecipe(R recipe) {
		return super.validateRecipe(recipe) && (recipe.getDrillingFluid().isEmpty() || recipe.getDrillingFluid().get().test(fluidTank.getFluid()));
	}

	@Override
	protected boolean canExtract() {
		return current.value().getDrillingFluid().isEmpty() ||
				(current.value().getDrillingFluid().get().test(fluidTank.getFluid()) &&
						fluidTank.getFluidAmount() >= current.value().getDrillingFluid().get().getRequiredAmount());
	}

	@Override
	protected void onFinished() {
		current.value().getDrillingFluid().ifPresent(fluid -> {
			fluidTank.drainInternal(fluid.getRequiredAmount(), FluidAction.EXECUTE);
		});
	}

	protected String getTankInName() {
		return "tank";
	}
}
