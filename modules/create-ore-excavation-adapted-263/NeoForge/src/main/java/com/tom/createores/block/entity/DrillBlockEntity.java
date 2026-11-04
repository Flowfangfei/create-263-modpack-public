package com.tom.createores.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;

import com.tom.createores.CreateOreExcavation;
import com.tom.createores.recipe.DrillingRecipe;
import com.tom.createores.util.IOBlockType;
import com.tom.createores.util.QueueInventory;

public class DrillBlockEntity extends ExcavatingBlockEntityImpl<DrillingRecipe> {
	private QueueInventory inventory;

	public DrillBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		inventory = new QueueInventory(this::notifyUpdate);
	}

	@Override
	public <T> T getCaps(BlockCapability<T, Direction> cap, IOBlockType type) {
		if(type == IOBlockType.ITEM_OUT && cap == Capabilities.Item.BLOCK) {
			return (T) inventory;
		}
		return super.getCaps(cap, type);
	}

	@Override
	protected void read(ValueInput input, boolean clientPacket) {
		super.read(input, clientPacket);
		if(!clientPacket) {
			input.list("inv", net.minecraft.world.item.ItemStack.CODEC).ifPresent(list -> inventory.loadStacks(list.stream().toList()));
		}
	}

	@Override
	public void write(ValueOutput output, boolean clientPacket) {
		super.write(output, clientPacket);
		if(!clientPacket) {
			var stacks = output.list("inv", net.minecraft.world.item.ItemStack.CODEC);
			inventory.stacks().forEach(stacks::add);
		}
	}

	@Override
	public void dropInv() {
		super.dropInv();
		inventory.stacks().forEach(this::dropItemStack);
	}

	@Override
	protected boolean canExtract() {
		return inventory.hasSpace() && super.canExtract();
	}

	@Override
	protected void onFinished() {
		current.value().getOutput().stream().map(o -> o.rollOutput(level.getRandom())).filter(i -> !i.isEmpty()).forEach(inventory::add);
		super.onFinished();
	}

	@Override
	protected RecipeType<DrillingRecipe> getRecipeType() {
		return CreateOreExcavation.DRILLING_RECIPES.getRecipeType();
	}
}
