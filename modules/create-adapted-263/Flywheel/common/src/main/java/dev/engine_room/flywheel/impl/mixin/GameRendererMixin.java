package dev.engine_room.flywheel.impl.mixin;

import dev.engine_room.flywheel.impl.event.LevelRenderMatrices;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
   @ModifyArg(
      method = "renderLevel()V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;",
         ordinal = 0
      ),
      index = 0
   )
   private Matrix4f flywheel$captureLevelProjection(Matrix4f projection) {
      LevelRenderMatrices.captureProjection(projection);
      return projection;
   }
}
