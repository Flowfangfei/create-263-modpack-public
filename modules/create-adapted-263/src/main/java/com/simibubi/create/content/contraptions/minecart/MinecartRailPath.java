package com.simibubi.create.content.contraptions.minecart;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** The centreline between the two vanilla exits of a rail block. */
record MinecartRailPath(BlockPos pos, RailShape shape, Vec3i firstExit, Vec3i secondExit, Vec3 first, Vec3 second) {
	private static final double EDGE_MARGIN = 1e-5;
	private static final double COLLISION_TOLERANCE = 1e-7;
	private static final double LEGACY_RAIL_OFFSET = 1 / 16.0;

	@Nullable
	static MinecartRailPath at(Level level, AbstractMinecart cart, BlockPos pos) {
		if (!level.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(pos.getX()),
			SectionPos.blockToSectionCoord(pos.getZ())))
			return null;
		var state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof BaseRailBlock rail))
			return null;
		RailShape shape = rail.getRailDirection(state, level, pos, cart);
		var exits = AbstractMinecart.exits(shape);
		double offset = cart.getBehavior() instanceof NewMinecartBehavior
			? NewMinecartBehavior.ON_RAIL_Y_OFFSET : LEGACY_RAIL_OFFSET;
		Vec3 base = Vec3.atBottomCenterOf(pos).add(0, offset, 0);
		return new MinecartRailPath(pos, shape, exits.getFirst(), exits.getSecond(),
			endpoint(base, shape, exits.getFirst()), endpoint(base, shape, exits.getSecond()));
	}

	private static Vec3 endpoint(Vec3 base, RailShape shape, Vec3i exit) {
		return base.add(exit.getX() * .5, shape.isSlope() ? 1 + exit.getY() : 0, exit.getZ() * .5);
	}

	Vec3 axis() {
		return second.subtract(first).normalize();
	}

	private Vec3 exit(boolean forwards) {
		return forwards ? second : first;
	}

	@Nullable
	private MinecartRailPath next(Level level, AbstractMinecart cart, boolean forwards) {
		Vec3i direction = forwards ? secondExit : firstExit;
		int edgeHeight = pos.getY() + (shape.isSlope() ? 1 + direction.getY() : 0);
		BlockPos across = new BlockPos(pos.getX() + direction.getX(), edgeHeight, pos.getZ() + direction.getZ());
		Vec3 edge = exit(forwards);
		for (BlockPos candidate : new BlockPos[] { across, across.below() }) {
			MinecartRailPath rail = at(level, cart, candidate);
			if (rail != null && (samePoint(rail.first, edge) || samePoint(rail.second, edge)))
				return rail;
		}
		return null;
	}

	private static boolean samePoint(Vec3 first, Vec3 second) {
		return first.distanceToSqr(second) <= COLLISION_TOLERANCE * COLLISION_TOLERANCE;
	}

	@Nullable
	CurvedLink curvedLink(Level level, AbstractMinecart cart, AbstractMinecart other,
		MinecartRailPath otherRail, double maxDistance) {
		if (pos.equals(otherRail.pos))
			return null;
		double start = progress(cart.position());
		double end = otherRail.progress(other.position());
		Route forward = routeTo(level, cart, otherRail, start, end, true, maxDistance);
		Route backward = routeTo(level, cart, otherRail, start, end, false, maxDistance);
		Route nearest = forward == null || backward != null && backward.distance < forward.distance ? backward : forward;
		return nearest != null && nearest.curved
			? new CurvedLink(nearest.distance, nearest.startDirection, nearest.endDirection)
			: null;
	}

	@Nullable
	private Route routeTo(Level level, AbstractMinecart cart, MinecartRailPath destination,
		double start, double end, boolean forward, double maxDistance) {
		Vec3 startDirection = axis().scale(forward ? 1 : -1);
		double distance = (forward ? 1 - start : start) * first.distanceTo(second);
		MinecartRailPath rail = this;
		boolean curved = false;
		while (distance <= maxDistance) {
			Vec3 edge = rail.exit(forward);
			rail = rail.next(level, cart, forward);
			if (rail == null || rail.pos.equals(pos))
				return null;
			forward = samePoint(rail.first, edge);
			Vec3 direction = rail.axis().scale(forward ? 1 : -1);
			curved |= startDirection.horizontal().normalize().dot(direction.horizontal().normalize()) < .99;
			double length = rail.first.distanceTo(rail.second);
			if (rail.pos.equals(destination.pos)) {
				distance += (forward ? end : 1 - end) * length;
				return distance <= maxDistance ? new Route(distance, startDirection, direction.reverse(), curved) : null;
			}
			distance += length;
		}
		return null;
	}

	private double progress(Vec3 position) {
		Vec3 horizontal = second.subtract(first).horizontal();
		return Mth.clamp(position.subtract(first).dot(horizontal) / horizontal.lengthSqr(), 0, 1);
	}

	record CurvedLink(double distance, Vec3 firstToward, Vec3 secondToward) {}

	private record Route(double distance, Vec3 startDirection, Vec3 endDirection, boolean curved) {}

	boolean move(Level level, AbstractMinecart cart, Vec3 correction) {
		MinecartRailPath rail = this;
		double signedDistance = axis().dot(correction);
		double remaining = Math.abs(signedDistance);
		boolean forwards = signedDistance > 0;
		Vec3 heading = horizontalHeading(forwards);
		boolean moved = false;
		while (remaining > EDGE_MARGIN) {
			Vec3 segment = rail.second.subtract(rail.first);
			double length = segment.length();
			double progress = rail.progress(cart.position());
			double toExit = length * (forwards ? 1 - progress : progress);
			double distance = Math.min(remaining, toExit);
			double targetProgress = Mth.clamp(progress + (forwards ? distance : -distance) / length, EDGE_MARGIN, 1 - EDGE_MARGIN);
			Vec3 target = rail.first.lerp(rail.second, targetProgress);
			if (cart.position().distanceToSqr(target) > EDGE_MARGIN * EDGE_MARGIN) {
				Vec3 nextHeading = rail.horizontalHeading(forwards);
				Vec3 momentum = cart.getDeltaMovement();
				if (!samePoint(heading, nextHeading)) {
					// A coupling can draw a moving cart backwards around a corner. Carry its original travel sign.
					double along = momentum.dot(heading);
					if (Math.abs(along) > COLLISION_TOLERANCE)
						momentum = nextHeading.scale(Math.copySign(momentum.horizontalDistance(), along))
							.add(0, momentum.y, 0);
				}
				if (!rail.moveTo(level, cart, target, momentum))
					break;
				heading = nextHeading;
				moved = true;
			}
			remaining -= distance;
			if (remaining <= EDGE_MARGIN)
				break;
			Vec3 edge = rail.exit(forwards);
			rail = rail.next(level, cart, forwards);
			if (rail == null)
				break;
			forwards = samePoint(rail.first, edge);
		}
		return moved;
	}

	private Vec3 horizontalHeading(boolean forwards) {
		return second.subtract(first).horizontal().normalize().scale(forwards ? 1 : -1);
	}

	private boolean moveTo(Level level, AbstractMinecart cart, Vec3 target, Vec3 momentum) {
		Vec3 start = cart.position();
		Vec3 horizontal = target.subtract(start).horizontal();
		double lift = shape.isSlope() ? Math.max(0, Math.max(first.y, second.y) - start.y) : Math.max(0, target.y - start.y);
		Vec3 upward = new Vec3(0, lift, 0);
		AABB box = cart.getBoundingBox();
		// Legacy minecarts clear the uphill support at the upper rail height before settling to the rail surface.
		if (!canMove(level, cart, box, upward) || !canMove(level, cart, box.move(upward), horizontal))
			return false;

		Vec3 motion = cart.getDeltaMovement();
		cart.setPos(start.add(upward));
		cart.move(MoverType.SELF, horizontal);
		cart.setDeltaMovement(motion);
		if (cart.position().horizontal().distanceToSqr(target.horizontal()) > COLLISION_TOLERANCE * COLLISION_TOLERANCE) {
			// An entity interaction can change the result of the real movement after the collision check.
			cart.setPos(start);
			return false;
		}
		cart.setPos(target);
		cart.setDeltaMovement(momentum);
		if (cart.getBehavior() instanceof NewMinecartBehavior behavior) {
			behavior.lerpSteps.add(new NewMinecartBehavior.MinecartStep(target, momentum, cart.getYRot(),
				cart.getXRot(), (float) start.distanceTo(target)));
		}
		return true;
	}

	private static boolean canMove(Level level, AbstractMinecart cart, AABB box, Vec3 movement) {
		if (movement.lengthSqr() == 0 || cart.noPhysics)
			return true;
		var entities = level.getEntityCollisions(cart, box.expandTowards(movement));
		Vec3 clipped = Entity.collideBoundingBox(cart, movement, box, level, entities);
		return movement.distanceToSqr(clipped) <= COLLISION_TOLERANCE * COLLISION_TOLERANCE;
	}
}
