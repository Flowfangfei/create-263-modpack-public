package dev.engine_room.flywheel.backend.engine;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.backend.opengl.GlSampler;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import dev.engine_room.flywheel.api.material.DepthTest;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import dev.engine_room.flywheel.backend.Samplers;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL33;

public final class MaterialRenderState {
   public static final Comparator<Material> COMPARATOR = MaterialRenderState::compare;

   private MaterialRenderState() {
   }

   public static void setup(Material material) {
      setupTexture(material);
      setupBackfaceCulling(material.backfaceCulling());
      setupPolygonOffset(material.polygonOffset());
      setupDepthTest(material.depthTest());
      setupTransparency(material.transparency());
      setupWriteMask(material.writeMask());
   }

   public static void setupOit(Material material) {
      setupTexture(material);
      setupBackfaceCulling(material.backfaceCulling());
      setupPolygonOffset(material.polygonOffset());
      setupDepthTest(material.depthTest());
      WriteMask mask = material.writeMask();
      boolean writeColor = mask.color();
      GL11.glColorMask(writeColor, writeColor, writeColor, writeColor);
   }

   private static void setupTexture(Material material) {
      Samplers.DIFFUSE.makeActive();
      AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(material.texture());
      GlTexture gpuTexture = (GlTexture)texture.getTexture();
      FilterMode filter = material.blur() ? FilterMode.LINEAR : FilterMode.NEAREST;
      GlSampler sampler = (GlSampler)RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.REPEAT, filter, filter, material.mipmap());
      GlStateManager._bindTexture(gpuTexture.glId());
      GL33.glBindSampler(Samplers.DIFFUSE.number, sampler.getId());
   }

   private static void setupBackfaceCulling(boolean backfaceCulling) {
      if (backfaceCulling) {
         GL11.glEnable(2884);
      } else {
         GL11.glDisable(2884);
      }
   }

   private static void setupPolygonOffset(boolean polygonOffset) {
      if (polygonOffset) {
         GL11.glPolygonOffset(-1.0F, -10.0F);
         GL11.glEnable(32823);
      } else {
         GL11.glPolygonOffset(0.0F, 0.0F);
         GL11.glDisable(32823);
      }
   }

   private static void setupDepthTest(DepthTest depthTest) {
      switch (depthTest) {
         case OFF:
            GL11.glDisable(2929);
            break;
         case NEVER:
            GL11.glEnable(2929);
            GL11.glDepthFunc(512);
            break;
         case LESS:
            GL11.glEnable(2929);
            GL11.glDepthFunc(516);
            break;
         case EQUAL:
            GL11.glEnable(2929);
            GL11.glDepthFunc(514);
            break;
         case LEQUAL:
            GL11.glEnable(2929);
            GL11.glDepthFunc(518);
            break;
         case GREATER:
            GL11.glEnable(2929);
            GL11.glDepthFunc(513);
            break;
         case NOTEQUAL:
            GL11.glEnable(2929);
            GL11.glDepthFunc(517);
            break;
         case GEQUAL:
            GL11.glEnable(2929);
            GL11.glDepthFunc(515);
            break;
         case ALWAYS:
            GL11.glEnable(2929);
            GL11.glDepthFunc(519);
      }
   }

   private static void setupTransparency(Transparency transparency) {
      switch (transparency) {
         case OPAQUE:
            GL11.glDisable(3042);
            break;
         case ADDITIVE:
            GL11.glEnable(3042);
            GL11.glBlendFunc(1, 1);
            break;
         case LIGHTNING:
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 1);
            break;
         case GLINT:
            GL11.glEnable(3042);
            GL14.glBlendFuncSeparate(768, 1, 0, 1);
            break;
         case CRUMBLING:
            GL11.glEnable(3042);
            GL14.glBlendFuncSeparate(774, 768, 1, 0);
            break;
         case TRANSLUCENT:
         case ORDER_INDEPENDENT:
            GL11.glEnable(3042);
            GL14.glBlendFuncSeparate(770, 771, 1, 771);
      }
   }

   private static void setupWriteMask(WriteMask mask) {
      GL11.glDepthMask(mask.depth());
      boolean writeColor = mask.color();
      GL11.glColorMask(writeColor, writeColor, writeColor, writeColor);
   }

   public static void reset() {
      resetTexture();
      resetBackfaceCulling();
      resetPolygonOffset();
      resetDepthTest();
      resetTransparency();
      resetWriteMask();
   }

   private static void resetTexture() {
      Samplers.DIFFUSE.makeActive();
      GlStateManager._bindTexture(0);
      GL33.glBindSampler(Samplers.DIFFUSE.number, 0);
   }

   private static void resetBackfaceCulling() {
      GL11.glEnable(2884);
   }

   private static void resetPolygonOffset() {
      GL11.glPolygonOffset(0.0F, 0.0F);
      GL11.glDisable(32823);
   }

   private static void resetDepthTest() {
      GL11.glDisable(2929);
      GL11.glDepthFunc(518);
   }

   private static void resetTransparency() {
      GL11.glDisable(3042);
      GL14.glBlendFuncSeparate(770, 771, 1, 0);
   }

   private static void resetWriteMask() {
      GL11.glDepthMask(true);
      GL11.glColorMask(true, true, true, true);
   }

   public static boolean materialEquals(Material lhs, Material rhs) {
      return lhs == rhs
         ? true
         : lhs.blur() == rhs.blur()
            && lhs.mipmap() == rhs.mipmap()
            && lhs.backfaceCulling() == rhs.backfaceCulling()
            && lhs.polygonOffset() == rhs.polygonOffset()
            && lhs.depthTest() == rhs.depthTest()
            && lhs.transparency() == rhs.transparency()
            && lhs.writeMask() == rhs.writeMask()
            && lhs.light().source().equals(rhs.light().source())
            && lhs.texture().equals(rhs.texture())
            && lhs.cutout().source().equals(rhs.cutout().source())
            && lhs.shaders().fragmentSource().equals(rhs.shaders().fragmentSource())
            && lhs.shaders().vertexSource().equals(rhs.shaders().vertexSource());
   }

   public static boolean materialIsAllNonNull(@Nullable Material material) {
      return material != null
         && material.shaders() != null
         && material.shaders().fragmentSource() != null
         && material.shaders().vertexSource() != null
         && material.fog() != null
         && material.fog().source() != null
         && material.cutout() != null
         && material.cutout().source() != null
         && material.light() != null
         && material.light().source() != null
         && material.texture() != null
         && material.depthTest() != null
         && material.transparency() != null
         && material.writeMask() != null
         && material.cardinalLightingMode() != null;
   }

   public static int compare(Material lhs, Material rhs) {
      if (lhs == rhs) {
         return 0;
      }

      int cmp = lhs.transparency().compareTo(rhs.transparency());
      if (cmp != 0) {
         return cmp;
      }

      cmp = lhs.light().source().compareTo(rhs.light().source());
      if (cmp != 0) {
         return cmp;
      }

      cmp = lhs.cutout().source().compareTo(rhs.cutout().source());
      if (cmp != 0) {
         return cmp;
      }

      cmp = lhs.shaders().fragmentSource().compareTo(rhs.shaders().fragmentSource());
      if (cmp != 0) {
         return cmp;
      }

      cmp = lhs.shaders().vertexSource().compareTo(rhs.shaders().vertexSource());
      if (cmp != 0) {
         return cmp;
      }

      cmp = lhs.texture().compareTo(rhs.texture());
      if (cmp != 0) {
         return cmp;
      }

      cmp = Boolean.compare(lhs.blur(), rhs.blur());
      if (cmp != 0) {
         return cmp;
      }

      cmp = Boolean.compare(lhs.mipmap(), rhs.mipmap());
      if (cmp != 0) {
         return cmp;
      }

      cmp = Boolean.compare(lhs.backfaceCulling(), rhs.backfaceCulling());
      if (cmp != 0) {
         return cmp;
      }

      cmp = Boolean.compare(lhs.polygonOffset(), rhs.polygonOffset());
      if (cmp != 0) {
         return cmp;
      }

      cmp = lhs.depthTest().compareTo(rhs.depthTest());
      if (cmp != 0) {
         return cmp;
      }

      cmp = lhs.writeMask().compareTo(rhs.writeMask());
      return cmp != 0 ? cmp : 0;
   }
}
