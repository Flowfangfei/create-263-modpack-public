package dev.engine_room.flywheel.backend.gl;

import com.mojang.renderpearl.backend.opengl.GlStateManager;
import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.gl.buffer.GlBufferType;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

public class GlStateTracker {
   private static final int[] BUFFERS = new int[GlBufferType.values().length];
   private static int vao;
   private static int program;

   public static int getBuffer(GlBufferType type) {
      return BUFFERS[type.ordinal()];
   }

   public static int getVertexArray() {
      return vao;
   }

   public static int getProgram() {
      return program;
   }

   public static void _setBuffer(GlBufferType type, int id) {
      BUFFERS[type.ordinal()] = id;
   }

   public static void _setVertexArray(int id) {
      vao = id;
   }

   public static void _setProgram(int id) {
      program = id;
   }

   public static GlStateTracker.State getRestoreState() {
      vao = GL11.glGetInteger(34229);
      program = GL11.glGetInteger(35725);
      GlBufferType[] values = GlBufferType.values();

      for (int i = 0; i < values.length; i++) {
         BUFFERS[i] = GL11.glGetInteger(values[i].glBindingEnum);
      }

      int activeTexture = GL11.glGetInteger(34016);
      int textureUnitCount = Samplers.NOISE.number + 1;
      int[] textures2d = new int[textureUnitCount];
      int[] textures2dArray = new int[textureUnitCount];
      int[] samplers = new int[textureUnitCount];

      for (int i = 0; i < textureUnitCount; i++) {
         GlStateManager._activeTexture(33984 + i);
         textures2d[i] = GL11.glGetInteger(32873);
         textures2dArray[i] = GL11.glGetInteger(35869);
         samplers[i] = GL11.glGetInteger(35097);
      }

      GlStateManager._activeTexture(activeTexture);
      int[] viewport = new int[4];
      GL11.glGetIntegerv(2978, viewport);
      return new GlStateTracker.State(
         (int[])BUFFERS.clone(),
         vao,
         program,
         activeTexture,
         GL11.glGetInteger(36006),
         GL11.glGetInteger(36010),
         GL11.glIsEnabled(2929),
         GL11.glGetInteger(2932),
         GL11.glGetBoolean(2930),
         GL11.glIsEnabled(3042),
         GL11.glGetInteger(32969),
         GL11.glGetInteger(32968),
         GL11.glGetInteger(32971),
         GL11.glGetInteger(32970),
         GL11.glGetInteger(32777),
         GL11.glGetInteger(34877),
         GL11.glIsEnabled(2884),
         GL11.glIsEnabled(32823),
         GL11.glGetFloat(32824),
         GL11.glGetFloat(10752),
         textures2d,
         textures2dArray,
         samplers,
         viewport
      );
   }

   public static void bindVao(int vao) {
      if (vao != GlStateTracker.vao) {
         GL30.glBindVertexArray(vao);
         GlStateTracker.vao = vao;
      }
   }

   public static void bindBuffer(GlBufferType type, int buffer) {
      if (BUFFERS[type.ordinal()] != buffer || type == GlBufferType.ELEMENT_ARRAY_BUFFER) {
         GL15.glBindBuffer(type.glEnum, buffer);
         BUFFERS[type.ordinal()] = buffer;
      }
   }

   public record State(
      int[] buffers,
      int vao,
      int program,
      int activeTexture,
      int drawFramebuffer,
      int readFramebuffer,
      boolean depthTest,
      int depthFunc,
      boolean depthMask,
      boolean blend,
      int blendSrcRgb,
      int blendDstRgb,
      int blendSrcAlpha,
      int blendDstAlpha,
      int blendEquationRgb,
      int blendEquationAlpha,
      boolean cullFace,
      boolean polygonOffsetFill,
      float polygonOffsetFactor,
      float polygonOffsetUnits,
      int[] textures2d,
      int[] textures2dArray,
      int[] samplers,
      int[] viewport
   ) implements AutoCloseable {
      public void restore() {
         GL30.glBindFramebuffer(36009, this.drawFramebuffer);
         GL30.glBindFramebuffer(36008, this.readFramebuffer);
         if (this.vao != GlStateTracker.vao) {
            GL30.glBindVertexArray(this.vao);
            GlStateTracker.vao = this.vao;
         }

         GlBufferType[] values = GlBufferType.values();

         for (int i = 0; i < values.length; i++) {
            if (this.buffers[i] != GlStateTracker.BUFFERS[i] && values[i] != GlBufferType.ELEMENT_ARRAY_BUFFER) {
               GL15.glBindBuffer(values[i].glEnum, this.buffers[i]);
               GlStateTracker.BUFFERS[i] = this.buffers[i];
            }
         }

         if (this.program != GlStateTracker.program) {
            GL20.glUseProgram(this.program);
            GlStateTracker.program = this.program;
         }

         for (int i = 0; i < this.textures2d.length; i++) {
            GlStateManager._activeTexture(33984 + i);
            GlStateManager._bindTexture(this.textures2d[i]);
            GL11.glBindTexture(35866, this.textures2dArray[i]);
            GL33.glBindSampler(i, this.samplers[i]);
         }

         GlStateManager._activeTexture(this.activeTexture);
         GL11.glViewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);
         setEnabled(2929, this.depthTest);
         GL11.glDepthFunc(this.depthFunc);
         GL11.glDepthMask(this.depthMask);
         setEnabled(3042, this.blend);
         GL14.glBlendFuncSeparate(this.blendSrcRgb, this.blendDstRgb, this.blendSrcAlpha, this.blendDstAlpha);
         GL20.glBlendEquationSeparate(this.blendEquationRgb, this.blendEquationAlpha);
         setEnabled(2884, this.cullFace);
         GL11.glPolygonOffset(this.polygonOffsetFactor, this.polygonOffsetUnits);
         setEnabled(32823, this.polygonOffsetFill);
      }

      private static void setEnabled(int capability, boolean enabled) {
         if (enabled) {
            GL11.glEnable(capability);
         } else {
            GL11.glDisable(capability);
         }
      }

      @Override
      public void close() {
         this.restore();
      }
   }
}
