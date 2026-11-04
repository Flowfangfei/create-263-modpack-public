package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.client.render.WorldGeometry;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public class CEELevelExtractorMixin {
    @Shadow @Final private LevelRenderState levelRenderState;

    @Inject(method = "extract", at = @At("TAIL"))
    private void electroenergetics$extract(
            DeltaTracker tracker, Camera camera, float partialTick, CallbackInfo ci) {
        ((WorldGeometry) levelRenderState)
                .electroenergetics$setGeometry(WireRenderer.extract(camera));
    }
}
