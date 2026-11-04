package dev.engine_room.flywheel.impl.mixin;

import dev.engine_room.flywheel.impl.FlwImplXplat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
abstract class LevelExtractorReloadMixin {
   @Inject(method = "allChanged", at = @At("RETURN"))
   private void flywheel$reload(CallbackInfo ci) {
      ClientLevel level = Minecraft.getInstance().level;
      if (level != null) {
         FlwImplXplat.INSTANCE.dispatchReloadLevelRendererEvent(level);
      }
   }
}
