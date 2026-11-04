package net.fxnt.fxntstorage.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;
import net.fxnt.fxntstorage.util.RendererHelper;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContraptionEntityRenderer.class)
public abstract class StorageContraptionRendererMixin {
    @Inject(method = "submit(Lcom/simibubi/create/content/contraptions/render/ContraptionEntityRenderer$ContraptionRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("TAIL"), remap = false)
    private void fxntstorage$submitMounted(ContraptionEntityRenderer.ContraptionRenderState<?> state,
            PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci,
            @com.llamalad7.mixinextras.sugar.Local com.simibubi.create.content.contraptions.AbstractContraptionEntity entity) {
        RendererHelper.submitMounted(entity, net.createmod.catnip.api.client.animation.AnimationTickHolder.getPartialTicks(), pose, collector);
    }
}
