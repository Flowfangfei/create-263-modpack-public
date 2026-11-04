package dev.engine_room.flywheel.backend.engine;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.opengl.GlSampler;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import com.mojang.renderpearl.backend.opengl.GlTextureView;
import dev.engine_room.flywheel.backend.Samplers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL33;

public class TextureBinder {
   public static void bind(Identifier resourceLocation) {
      GlStateManager._bindTexture(byName(resourceLocation));
   }

   public static void bindLightAndOverlay() {
      GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
      GlSampler nearestSampler = (GlSampler)RenderSystem.getSamplerCache()
         .getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, false);
      GlSampler linearSampler = (GlSampler)RenderSystem.getSamplerCache()
         .getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
      Samplers.OVERLAY.makeActive();
      GlStateManager._bindTexture(((GlTextureView)gameRenderer.overlayTexture().getTextureView()).glId());
      GL33.glBindSampler(Samplers.OVERLAY.number, nearestSampler.getId());
      Samplers.LIGHT.makeActive();
      GlStateManager._bindTexture(((GlTextureView)gameRenderer.levelLightmap()).glId());
      GL33.glBindSampler(Samplers.LIGHT.number, linearSampler.getId());
   }

   public static void resetLightAndOverlay() {
      Samplers.LIGHT.makeActive();
      GlStateManager._bindTexture(0);
      GL33.glBindSampler(Samplers.LIGHT.number, 0);
      Samplers.OVERLAY.makeActive();
      GlStateManager._bindTexture(0);
      GL33.glBindSampler(Samplers.OVERLAY.number, 0);
   }

   public static int byName(Identifier texture) {
      GpuTexture gpuTexture = Minecraft.getInstance().getTextureManager().getTexture(texture).getTexture();
      return ((GlTexture)gpuTexture).glId();
   }
}
