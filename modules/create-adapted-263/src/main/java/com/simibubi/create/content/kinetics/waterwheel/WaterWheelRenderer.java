package com.simibubi.create.content.kinetics.waterwheel;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.model.CreateStandaloneModels;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.SuperByteBufferCache;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;

public class WaterWheelRenderer<T extends WaterWheelBlockEntity> extends KineticBlockEntityRenderer<T> {
	public static final SuperByteBufferCache.Compartment<ModelKey> WATER_WHEEL = new SuperByteBufferCache.Compartment<>();

	protected final boolean large;

	public WaterWheelRenderer(Context context, boolean large) {
		super(context);
		this.large = large;
	}

	public static <T extends WaterWheelBlockEntity> WaterWheelRenderer<T> standard(Context context) {
		return new WaterWheelRenderer<>(context, false);
	}

	public static <T extends WaterWheelBlockEntity> WaterWheelRenderer<T> large(Context context) {
		return new WaterWheelRenderer<>(context, true);
	}

	protected SuperByteBuffer getRotatedModel(T be, BlockState state) {
		return null;
	}

	@Override
	protected List<BlockStateModelPart> getRotatingModelParts(T be, BlockState renderedState) {
		return List.of(WaterWheelModels.get(Variant.of(large, renderedState), be.material));
	}

	@Override
	protected void transformRotatingModel(T be, PoseStack ms, float partialTicks) {
		Axis axis = getRotationAxisOf(be);
		ms.translate(.5, .5, .5);
		ms.rotate(rotation(axis, getAngleForBe(be, be.getBlockPos(), axis, partialTicks)));
		orientToAxis(axis, ms);
		if (large && axis == Axis.Z)
			ms.rotate(com.mojang.math.Axis.YP.rotationDegrees(180));
		ms.translate(-.5, -.5, -.5);
	}

	private static void orientToAxis(Axis axis, PoseStack ms) {
		switch (axis) {
			case X -> {
				ms.rotate(com.mojang.math.Axis.YP.rotationDegrees(90));
				ms.rotate(com.mojang.math.Axis.XP.rotationDegrees(90));
			}
			case Z -> ms.rotate(com.mojang.math.Axis.XP.rotationDegrees(90));
			case Y -> {
			}
		}
	}

	public enum Variant {
		SMALL(AllPartialModels.WATER_WHEEL),
		LARGE(AllPartialModels.LARGE_WATER_WHEEL),
		LARGE_EXTENSION(AllPartialModels.LARGE_WATER_WHEEL_EXTENSION);

		private final PartialModel partial;

		Variant(PartialModel partial) {
			this.partial = partial;
		}

		public net.neoforged.neoforge.client.model.standalone.StandaloneModelKey<BlockStateModelPart> key() {
			return switch (this) {
				case SMALL -> CreateStandaloneModels.WATER_WHEEL;
				case LARGE -> CreateStandaloneModels.LARGE_WATER_WHEEL;
				case LARGE_EXTENSION -> CreateStandaloneModels.LARGE_WATER_WHEEL_EXTENSION;
			};
		}

		public static Variant of(boolean large, BlockState blockState) {
			if (large)
				return blockState.getValue(LargeWaterWheelBlock.EXTENSION) ? LARGE_EXTENSION : LARGE;
			return SMALL;
		}
	}

	public record ModelKey(boolean large, BlockState state, BlockState material) {}
}
