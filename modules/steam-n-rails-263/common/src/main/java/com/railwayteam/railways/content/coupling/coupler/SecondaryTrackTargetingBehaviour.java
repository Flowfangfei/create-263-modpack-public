package com.railwayteam.railways.content.coupling.coupler;

import com.railwayteam.railways.mixin.AccessorTrackTargetingBehavior;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.content.trains.track.BezierTrackPointLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SecondaryTrackTargetingBehaviour<T extends TrackEdgePoint> extends TrackTargetingBehaviour<T> {
	public static final BehaviourType<SecondaryTrackTargetingBehaviour<?>> TYPE = new BehaviourType<>();

	public SecondaryTrackTargetingBehaviour(SmartBlockEntity te, EdgePointType<T> edgePointType) {
		super(te, edgePointType);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@Override
	public void write(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        net.minecraft.world.level.storage.TagValueOutput output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, registries);
		AccessorTrackTargetingBehavior accessor = (AccessorTrackTargetingBehavior) this;
		output.store("SecondaryId", UUIDUtil.CODEC, accessor.getId());
		output.store("SecondaryTargetTrack", BlockPos.CODEC, accessor.getTargetTrack());
		output.putBoolean("SecondaryOrtho", accessor.isOrthogonal());
		output.putBoolean("SecondaryTargetDirection", accessor.getTargetDirection() == Direction.AxisDirection.POSITIVE);
		if (accessor.getRotatedDirection() != null)
			output.store("SecondaryRotatedAxis", Vec3.CODEC, accessor.getRotatedDirection());
		if (accessor.getPrevDirection() != null)
			output.store("SecondaryPrevAxis", Vec3.CODEC, accessor.getPrevDirection());
		if (accessor.getMigrationData() != null && !clientPacket)
			output.store("SecondaryMigrate", CompoundTag.CODEC, accessor.getMigrationData());
		if (accessor.getTargetBezier() != null) {
			ValueOutput bezier = output.child("SecondaryBezier");
			bezier.putInt("Segment", accessor.getTargetBezier().segment());
			bezier.store("Key", BlockPos.CODEC, accessor.getTargetBezier().curveTarget()
				.subtract(getPos()));
		}

        tag.merge(output.buildResult());
    }

	@Override
	public void read(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        ValueInput input = net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, registries, tag);
		AccessorTrackTargetingBehavior accessor = (AccessorTrackTargetingBehavior) this;
		accessor.setId(input.read("SecondaryId", UUIDUtil.CODEC).orElseGet(UUID::randomUUID));
		accessor.setTargetTrack(input.read("SecondaryTargetTrack", BlockPos.CODEC).orElse(BlockPos.ZERO));
		accessor.setTargetDirection(input.getBooleanOr("SecondaryTargetDirection", false)
			? Direction.AxisDirection.POSITIVE
			: Direction.AxisDirection.NEGATIVE);
		accessor.setOrthogonal(input.getBooleanOr("SecondaryOrtho", false));
		input.read("SecondaryPrevAxis", Vec3.CODEC).ifPresent(accessor::setPrevDirection);
		input.read("SecondaryRotatedAxis", Vec3.CODEC).ifPresent(accessor::setRotatedDirection);
		input.read("SecondaryMigrate", CompoundTag.CODEC).ifPresent(accessor::setMigrationData);
		if (clientPacket)
			accessor.setEdgePoint(null);
		input.child("SecondaryBezier").ifPresent(bezier -> {
			BlockPos key = bezier.read("Key", BlockPos.CODEC).orElse(BlockPos.ZERO);
			accessor.setTargetBezier(new BezierTrackPointLocation(key.offset(getPos()), bezier.getIntOr("Segment", 0)));
		});
	}
}
