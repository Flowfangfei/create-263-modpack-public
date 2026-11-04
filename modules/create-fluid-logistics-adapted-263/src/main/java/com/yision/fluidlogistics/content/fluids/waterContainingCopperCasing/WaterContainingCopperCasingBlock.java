package com.yision.fluidlogistics.content.fluids.waterContainingCopperCasing;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.yision.fluidlogistics.foundation.render.FluidItemRenderHelper;
import com.yision.fluidlogistics.registry.AllBlockEntities;

import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.neoforge.fluids.FluidStack;

public class WaterContainingCopperCasingBlock extends Block
	implements IWrenchable, IBE<WaterContainingCopperCasingBlock.Entity> {

	public WaterContainingCopperCasingBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return InteractionResult.FAIL;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
		Player player, InteractionHand hand, BlockHitResult hitResult) {
		Entity be = getBlockEntity(level, pos);
		if (be != null && FluidHelper.tryEmptyItemIntoBE(level, player, hand, stack, be))
			return InteractionResult.SUCCESS;

		if (!stack.is(Items.BUCKET)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}

		if (!level.isClientSide()) {
			ItemStack filledBucket = new ItemStack(Items.WATER_BUCKET);
			player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filledBucket));
			player.awardStat(Stats.ITEM_USED.get(Items.BUCKET));
			level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
		}

		return InteractionResult.SUCCESS;
	}
    // Destruction and invalidation are handled by the native BlockEntity removal lifecycle.


	@Override
	public Class<Entity> getBlockEntityClass() {
		return Entity.class;
	}

	@Override
	public BlockEntityType<? extends Entity> getBlockEntityType() {
		return AllBlockEntities.WATER_CONTAINING_COPPER_CASING.get();
	}

	public static class Entity extends SmartBlockEntity {

		public Entity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
			super(type, pos, state);
		}

		@Override
		public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		}
	}

	public static class Renderer extends SafeBlockEntityRenderer<Entity> {

		private static final float FLUID_MIN = 1 / 16f;
		private static final float FLUID_MAX = 15 / 16f;

		public Renderer(BlockEntityRendererProvider.Context context) {
		}

		@Override
		protected void renderSafe(Entity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
			int overlay) {
			renderFluid(ms, buffer, light);
		}

		public static void renderFluid(PoseStack ms, MultiBufferSource buffer, int light) {
			renderFluid(ms, buffer, light, null);
		}

		public static void renderFluid(PoseStack ms, MultiBufferSource buffer, int light,
			ItemDisplayContext displayContext) {
			FluidRenderHelper.renderFluidBox(new FluidStack(Fluids.WATER, 1000), FLUID_MIN, FLUID_MIN, FLUID_MIN,
				FLUID_MAX, FLUID_MAX, FLUID_MAX, FluidItemRenderHelper.getFluidBuilder(buffer, displayContext), ms,
				light, true, true);
		}
	
    // Explicit native entry point: the parent SafeBlockEntityRenderer.submit is empty.
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() {
        return new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<Entity>();
    }
    @Override public void extractRenderState(Entity be,
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, float partialTicks,
        net.minecraft.world.phys.Vec3 cameraPosition,
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be, state, overlay);
        if (state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> raw) {
            @SuppressWarnings("unchecked") var target = (com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<Entity>) raw;
            target.blockEntity = be;
            target.partialTicks = partialTicks;
        }
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
        com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
        net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> captured)
            || !(captured.blockEntity instanceof Entity be) || isInvalid(be)) return;
        var geometry = new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.GeometryCollector(collector);
        renderSafe(be, captured.partialTicks, pose, geometry, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }

    }
}
