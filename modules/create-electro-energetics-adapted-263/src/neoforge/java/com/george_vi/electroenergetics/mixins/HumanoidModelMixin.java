package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickRenderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("RETURN"))
    private void electroenergetics$pose(HumanoidRenderState state, CallbackInfo ci) {
        LinemansStickRenderer.setupAnimAfter(state, (HumanoidModel<?>) (Object) this);
    }
}
