package com.yision.fluidlogistics.mixin.client;

import java.util.List;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRenderer;
import com.yision.fluidlogistics.FluidLogistics;
import com.yision.fluidlogistics.content.logistics.fluidPackage.client.phantomChain.PhantomChainVisibility;
import com.yision.fluidlogistics.content.logistics.fluidPackage.phantomChain.PhantomChainConveyorAccess;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

@Mixin(ChainConveyorRenderer.class)
public class ChainConveyorRendererMixin {
    @Unique
    private static final Identifier fluidlogistics$PHANTOM_CHAIN_TEXTURE = FluidLogistics.asResource("textures/block/phantom_chain.png");
    @Unique
    private static final ThreadLocal<Identifier> fluidlogistics$chainTextureOverride = new ThreadLocal<>();

    @WrapOperation(method = "renderChains", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorRenderer;renderChainStrand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/Vec3;FFFFIIZ)V"))
    private void fluidlogistics$phantomChain(PoseStack pose, SubmitNodeCollector collector, BlockPos tilePos,
        Vec3 start, float yaw, float pitch, float animation, float length, int light1, int light2, boolean far,
        Operation<Void> original, @Local(argsOnly = true) ChainConveyorBlockEntity be,
        @Local(name = "connection") BlockPos connection) {
        if (!PhantomChainVisibility.shouldRenderConnection(be, connection)) return;
        boolean phantom = ((PhantomChainConveyorAccess) be).fluidlogistics$isPhantomConnection(connection);
        if (phantom) fluidlogistics$chainTextureOverride.set(fluidlogistics$PHANTOM_CHAIN_TEXTURE);
        try {
            original.call(pose, collector, tilePos, start, yaw, pitch, animation, length, light1, light2, far);
        } finally {
            if (phantom) fluidlogistics$chainTextureOverride.remove();
        }
    }

    @ModifyExpressionValue(method = "renderChainStrand", at = @At(value = "FIELD", target = "Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorRenderer;CHAIN_LOCATION:Lnet/minecraft/resources/Identifier;"))
    private static Identifier fluidlogistics$chainTexture(Identifier original) {
        Identifier override = fluidlogistics$chainTextureOverride.get();
        return override == null ? original : override;
    }

    // Native ItemStackRenderState includes FluidContainerItemModel, including under Flywheel.
    @WrapOperation(method = "renderPackages", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorRenderer;renderPackage(Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorPackage;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"), slice = @Slice(from = @At(value = "FIELD", target = "Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;travellingPackages:Ljava/util/Map;")))
    private void fluidlogistics$phantomPackage(ChainConveyorRenderer renderer, ChainConveyorBlockEntity be,
        ChainConveyorPackage box, float partialTicks, PoseStack pose, SubmitNodeCollector collector,
        Operation<Void> original, @Local Map.Entry<BlockPos, List<ChainConveyorPackage>> entry) {
        if (PhantomChainVisibility.shouldRenderConnection(be, entry.getKey()))
            original.call(renderer, be, box, partialTicks, pose, collector);
    }
}
