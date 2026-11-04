package dev.engine_room.flywheel.impl.mixin;

import dev.engine_room.flywheel.api.event.EndClientResourceReloadEvent;
import dev.engine_room.flywheel.impl.FlwImpl;
import dev.engine_room.flywheel.impl.task.FlwTaskExecutor;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.neoforged.fml.ModLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
   @Shadow
   @Final
   private ReloadableResourceManager resourceManager;
   @Unique
   private boolean flywheel$initialReload = true;

   @Inject(
      method = "<init>",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;createReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/server/packs/resources/ReloadInstance;"
      )
   )
   private void flywheel$onBeginInitialResourceReload(CallbackInfo ci) {
      FlwImpl.freezeRegistries();
   }

   @Inject(method = "onResourceLoadFinished", at = @At("HEAD"))
   private void flywheel$onReloadFinished(@Coerce Object cookie, CallbackInfo ci) {
      ModLoader.postEvent(new EndClientResourceReloadEvent((Minecraft)(Object)this, this.resourceManager, this.flywheel$initialReload, Optional.empty()));
      this.flywheel$initialReload = false;
   }

   @Inject(method = "rollbackResourcePacks", at = @At("HEAD"))
   private void flywheel$onReloadFailed(Throwable error, @Coerce Object cookie, CallbackInfo ci) {
      ModLoader.postEvent(new EndClientResourceReloadEvent((Minecraft)(Object)this, this.resourceManager, this.flywheel$initialReload, Optional.of(error)));
   }

   @Inject(method = "close", at = @At("HEAD"))
   private void flywheel$shutdownTaskExecutor(CallbackInfo ci) {
      FlwTaskExecutor.shutdown();
   }
}
