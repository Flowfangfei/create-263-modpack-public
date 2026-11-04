package com.simibubi.create.foundation.sound;

import com.simibubi.create.content.kinetics.fan.AirCurrent;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.event.ClientTickEvent.Pre;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;

@EventBusSubscriber(Dist.CLIENT)
public final class ClientSoundEvents {
   @SubscribeEvent
   public static void preTick(Pre event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && !mc.isPaused()) {
         AirCurrent.Client.tickClientPlayerSounds();
      }
   }

   @SubscribeEvent
   public static void postTick(Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && !mc.isPaused()) {
         SoundScapes.tick();
      }
   }

   @SubscribeEvent
   public static void unload(Unload event) {
      if (event.getLevel().isClientSide()) {
         reset();
      }
   }

   @SubscribeEvent
   public static void registerReloadListener(AddClientReloadListenersEvent event) {
      event.addListener(Identifier.fromNamespaceAndPath("create", "sound_lifecycle"), (ResourceManagerReloadListener)resources -> reset());
   }

   private static void reset() {
      AirCurrent.Client.resetClientPlayerSounds();
      SoundScapes.invalidateAll();
   }
}
