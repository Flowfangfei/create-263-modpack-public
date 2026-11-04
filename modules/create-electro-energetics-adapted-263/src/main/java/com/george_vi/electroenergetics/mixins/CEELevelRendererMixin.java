package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.client.render.WorldGeometry;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class CEELevelRendererMixin {
    @Inject(method = "submitFeatures", at = @At("HEAD"))
    private void electroenergetics$submit(
            LevelRenderState state, SubmitNodeCollector queue, boolean outline, CallbackInfo ci) {
        ((WorldGeometry) state).electroenergetics$getGeometry().submit(new PoseStack(), queue);
        if (outline)
            ((WorldGeometry) state)
                    .electroenergetics$getGeometry()
                    .submitOutlines(new PoseStack(), queue);
    }

    @Inject(method = "submitBlockOutline", at = @At("HEAD"), cancellable = true)
    private void electroenergetics$outline(
            PoseStack pose, SubmitNodeCollector queue, LevelRenderState state, CallbackInfo ci) {
        if (((WorldGeometry) state).electroenergetics$getGeometry().hideBlockOutline) ci.cancel();
    }
}
