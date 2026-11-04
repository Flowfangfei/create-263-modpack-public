package com.tom.createores.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import com.tom.createores.block.MultiblockPart;
import com.tom.createores.block.MultiblockPart.MultiblockGhostPart;
import com.tom.createores.block.entity.MultiblockCapHandler.Kinetic;

public class KineticInputBlockEntity extends KineticBlockEntity implements Kinetic {
	private float reqStress = 1;

	public KineticInputBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}

	@Override
	public float calculateStressApplied() {
		this.lastStressApplied = reqStress;
		return reqStress;
	}

	@Override
	public float calculateAddedStressCapacity() {
		return 0;
	}

	@Override
	public void initialize() {
		super.initialize();

		BlockState state = level.getBlockState(worldPosition);
		BlockPos pos = worldPosition;
		for (int i = 0;i<5 && state.getBlock() instanceof MultiblockGhostPart;i++) {
			Direction d = ((MultiblockGhostPart)state.getBlock()).getParentDir(state);
			pos = pos.relative(d, 1);
			state = level.getBlockState(pos);
		}
		if (state.getBlock() instanceof MultiblockPart) {
			MultiblockPart d = (MultiblockPart) state.getBlock();
			if (!(d instanceof MultiblockGhostPart)) {
				BlockEntity te = level.getBlockEntity(pos);
				if (te instanceof MultiblockCapHandler) {
					((MultiblockCapHandler)te).addKinetic(this);
				}
			}
		}
	}

	@Override
	public void setStress(float stress) {
		this.reqStress = stress;
		if(lastStressApplied != stress && hasNetwork()) {
			getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
			networkDirty = true;
			notifyUpdate();
		}
	}

	@Override
	protected void write(net.minecraft.nbt.CompoundTag output, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
		super.write(output, registries, clientPacket);
		output.putFloat("reqStress", reqStress);
	}

	@Override
	protected void read(net.minecraft.nbt.CompoundTag input, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
		super.read(input, registries, clientPacket);
		reqStress = input.getFloatOr("reqStress", 1f);
	}

	@Override
	public float getRotationSpeed() {
		if(overStressed || lastStressApplied != reqStress)return 0;
		return Math.abs(speed);
	}
}
