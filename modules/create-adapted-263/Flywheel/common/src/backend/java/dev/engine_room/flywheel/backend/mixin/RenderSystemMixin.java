package dev.engine_room.flywheel.backend.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.engine_room.flywheel.backend.gl.GlCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderSystem.class, remap = false)
abstract class RenderSystemMixin {
   @Inject(method = "initRenderer", at = @At("RETURN"))
   private static void flywheel$onInitRenderer(CallbackInfo ci) {
      GlCompat.init();
   }
}
