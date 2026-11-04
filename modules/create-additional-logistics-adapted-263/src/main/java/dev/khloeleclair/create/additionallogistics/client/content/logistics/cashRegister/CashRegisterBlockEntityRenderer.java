package dev.khloeleclair.create.additionallogistics.client.content.logistics.cashRegister;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.khloeleclair.create.additionallogistics.client.CALRenderModels;
import dev.khloeleclair.create.additionallogistics.common.content.logistics.cashRegister.CashRegisterBlockEntity;
import dev.khloeleclair.create.additionallogistics.common.content.logistics.cashRegister.CashRegisterBlock;
import dev.khloeleclair.create.additionallogistics.common.registries.CALPartialModels;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

public class CashRegisterBlockEntityRenderer extends SmartBlockEntityRenderer<CashRegisterBlockEntity> {
    private static class State extends SmartRenderState { CashRegisterBlockEntity register; }
    public CashRegisterBlockEntityRenderer(BlockEntityRendererProvider.Context context) { super(context); }
    @Override public BlockEntityRenderState createRenderState() { return new State(); }
    @Override public void extractRenderState(CashRegisterBlockEntity be, BlockEntityRenderState state, float partial, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        super.extractRenderState(be, state, partial, camera, overlay);
        ((State)state).register = be;
    }
    @Override public void submit(BlockEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, pose, collector, camera);
        CashRegisterBlockEntity be = ((State)state).register;
        if (be == null || isInvalid(be)) return;
        var dir = be.getBlockState().getValue(CashRegisterBlock.FACING);
        if (be.hadRecentSale()) CALRenderModels.submit(CALPartialModels.CASH_REGISTER_SALE_NOTICE, dir, false, pose, collector, RenderTypes.cutoutMovingBlock(), state.lightCoords);
        CALRenderModels.submit(CALPartialModels.CASH_REGISTER_GLASS, dir, false, pose, collector, RenderTypes.translucentMovingBlock(), state.lightCoords);
    }
}
