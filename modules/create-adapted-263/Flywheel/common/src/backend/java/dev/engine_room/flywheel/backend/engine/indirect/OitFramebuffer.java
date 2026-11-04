package dev.engine_room.flywheel.backend.engine.indirect;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import dev.engine_room.flywheel.backend.NoiseTextures;
import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.OitPrograms;
import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL46;

public class OitFramebuffer {
   public static final float[] CLEAR_TO_ZERO = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
   public static final int[] DEPTH_RANGE_DRAW_BUFFERS = new int[]{36064};
   public static final int[] RENDER_TRANSMITTANCE_DRAW_BUFFERS = new int[]{36065, 36066, 36067, 36068};
   public static final int[] ACCUMULATE_DRAW_BUFFERS = new int[]{36069};
   public static final int[] DEPTH_ONLY_DRAW_BUFFERS = new int[0];
   private final OitPrograms programs;
   private final int vao;
   private final int outputFbo;
   public int fbo = -1;
   public int depthBounds = -1;
   public int coefficients = -1;
   public int accumulate = -1;
   private int lastWidth = -1;
   private int lastHeight = -1;

   public OitFramebuffer(OitPrograms programs) {
      this.programs = programs;
      if (GlCompat.SUPPORTS_DSA) {
         this.vao = GL46.glCreateVertexArrays();
         this.outputFbo = GL46.glCreateFramebuffers();
      } else {
         this.vao = GL32.glGenVertexArrays();
         this.outputFbo = GL32.glGenFramebuffers();
      }
   }

   public void prepare() {
      RenderTarget renderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
      this.maybeResizeFBO(renderTarget.width, renderTarget.height);
      Samplers.COEFFICIENTS.makeActive();
      GL32.glBindTexture(3553, 0);
      GL32.glBindTexture(35866, this.coefficients);
      Samplers.DEPTH_RANGE.makeActive();
      GL32.glBindTexture(3553, this.depthBounds);
      Samplers.NOISE.makeActive();
      GL32.glBindTexture(3553, ((GlTexture)NoiseTextures.BLUE_NOISE.getTexture()).glId());
      GL32.glBindFramebuffer(36160, this.fbo);
      GL32.glFramebufferTexture(36160, 36096, ((GlTexture)renderTarget.getDepthTexture()).glId(), 0);
   }

   public void depthRange() {
      GL32.glDepthMask(false);
      GL32.glColorMask(true, true, true, true);
      GL32.glEnable(3042);
      GL32.glBlendFunc(1, 1);
      GL32.glBlendEquation(32776);
      float far = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16.0F;
      if (GlCompat.SUPPORTS_DSA) {
         GL46.glNamedFramebufferDrawBuffers(this.fbo, DEPTH_RANGE_DRAW_BUFFERS);
         GL46.glClearNamedFramebufferfv(this.fbo, 6144, 0, new float[]{-far, -far, 0.0F, 0.0F});
      } else {
         GL32.glDrawBuffers(DEPTH_RANGE_DRAW_BUFFERS);
         GL32.glClearColor(-far, -far, 0.0F, 0.0F);
         GL32.glClear(16384);
      }
   }

   public void renderTransmittance() {
      GL32.glDepthMask(false);
      GL32.glColorMask(true, true, true, true);
      GL32.glEnable(3042);
      GL32.glBlendFunc(1, 1);
      GL32.glBlendEquation(32774);
      if (GlCompat.SUPPORTS_DSA) {
         GL46.glNamedFramebufferDrawBuffers(this.fbo, RENDER_TRANSMITTANCE_DRAW_BUFFERS);
         GL46.glClearNamedFramebufferfv(this.fbo, 6144, 0, CLEAR_TO_ZERO);
         GL46.glClearNamedFramebufferfv(this.fbo, 6144, 1, CLEAR_TO_ZERO);
         GL46.glClearNamedFramebufferfv(this.fbo, 6144, 2, CLEAR_TO_ZERO);
         GL46.glClearNamedFramebufferfv(this.fbo, 6144, 3, CLEAR_TO_ZERO);
      } else {
         GL32.glDrawBuffers(RENDER_TRANSMITTANCE_DRAW_BUFFERS);
         GL32.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         GL32.glClear(16384);
      }
   }

   public void renderDepthFromTransmittance() {
      GL32.glDepthMask(true);
      GL32.glColorMask(false, false, false, false);
      GL32.glDisable(3042);
      GL32.glDepthFunc(519);
      if (GlCompat.SUPPORTS_DSA) {
         GL46.glNamedFramebufferDrawBuffers(this.fbo, DEPTH_ONLY_DRAW_BUFFERS);
      } else {
         GL32.glDrawBuffers(DEPTH_ONLY_DRAW_BUFFERS);
      }

      this.programs.getOitDepthProgram().bind();
      this.drawFullscreenQuad();
   }

   public void accumulate() {
      GL32.glDepthMask(false);
      GL32.glColorMask(true, true, true, true);
      GL32.glEnable(3042);
      GL32.glBlendFunc(1, 1);
      GL32.glBlendEquation(32774);
      if (GlCompat.SUPPORTS_DSA) {
         GL46.glNamedFramebufferDrawBuffers(this.fbo, ACCUMULATE_DRAW_BUFFERS);
         GL46.glClearNamedFramebufferfv(this.fbo, 6144, 0, CLEAR_TO_ZERO);
      } else {
         GL32.glDrawBuffers(ACCUMULATE_DRAW_BUFFERS);
         GL32.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         GL32.glClear(16384);
      }
   }

   public void composite() {
      RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
      this.bindOutput(target);
      GL32.glDepthMask(true);
      GL32.glColorMask(true, true, true, true);
      GL32.glEnable(3042);
      GL32.glBlendFuncSeparate(770, 771, 1, 771);
      GL32.glBlendEquation(32774);
      GL32.glDepthFunc(519);
      GlTextureUnit.T0.makeActive();
      GL32.glBindTexture(3553, this.accumulate);
      this.programs.getOitCompositeProgram().bind();
      this.drawFullscreenQuad();
      this.bindOutput(Minecraft.getInstance().gameRenderer.mainRenderTarget());
   }

   public void delete() {
      this.deleteTextures();
      GL32.glDeleteVertexArrays(this.vao);
      GL32.glDeleteFramebuffers(this.outputFbo);
   }

   private void drawFullscreenQuad() {
      GL32.glBindVertexArray(this.vao);
      GL32.glDrawArrays(4, 0, 3);
   }

   private void deleteTextures() {
      if (this.depthBounds != -1) {
         GL32.glDeleteTextures(this.depthBounds);
      }

      if (this.coefficients != -1) {
         GL32.glDeleteTextures(this.coefficients);
      }

      if (this.accumulate != -1) {
         GL32.glDeleteTextures(this.accumulate);
      }

      if (this.fbo != -1) {
         GL32.glDeleteFramebuffers(this.fbo);
      }

      Samplers.COEFFICIENTS.makeActive();
      GL32.glBindTexture(35866, 0);
      Samplers.DEPTH_RANGE.makeActive();
      GL32.glBindTexture(3553, 0);
   }

   private void maybeResizeFBO(int width, int height) {
      if (this.lastWidth != width || this.lastHeight != height) {
         this.lastWidth = width;
         this.lastHeight = height;
         this.deleteTextures();
         if (GlCompat.SUPPORTS_DSA) {
            this.fbo = GL46.glCreateFramebuffers();
            this.depthBounds = GL46.glCreateTextures(3553);
            this.coefficients = GL46.glCreateTextures(35866);
            this.accumulate = GL46.glCreateTextures(3553);
            GL46.glTextureStorage2D(this.depthBounds, 1, 33328, width, height);
            GL46.glTextureStorage3D(this.coefficients, 1, 34842, width, height, 4);
            GL46.glTextureStorage2D(this.accumulate, 1, 34842, width, height);
            GL46.glNamedFramebufferTexture(this.fbo, 36064, this.depthBounds, 0);
            GL46.glNamedFramebufferTextureLayer(this.fbo, 36065, this.coefficients, 0, 0);
            GL46.glNamedFramebufferTextureLayer(this.fbo, 36066, this.coefficients, 0, 1);
            GL46.glNamedFramebufferTextureLayer(this.fbo, 36067, this.coefficients, 0, 2);
            GL46.glNamedFramebufferTextureLayer(this.fbo, 36068, this.coefficients, 0, 3);
            GL46.glNamedFramebufferTexture(this.fbo, 36069, this.accumulate, 0);
         } else {
            this.fbo = GL46.glGenFramebuffers();
            this.depthBounds = GL32.glGenTextures();
            this.coefficients = GL32.glGenTextures();
            this.accumulate = GL32.glGenTextures();
            GlTextureUnit.T0.makeActive();
            GL32.glBindTexture(3553, 0);
            GL32.glBindTexture(3553, this.depthBounds);
            GL32.glTexImage2D(3553, 0, 33328, width, height, 0, 6408, 5120, 0L);
            GL32.glTexParameteri(3553, 10241, 9728);
            GL32.glTexParameteri(3553, 10240, 9728);
            GL32.glTexParameteri(3553, 10242, 33071);
            GL32.glTexParameteri(3553, 10243, 33071);
            GL32.glBindTexture(35866, this.coefficients);
            GL32.glTexImage3D(35866, 0, 34842, width, height, 4, 0, 6408, 5120, 0L);
            GL32.glTexParameteri(35866, 10241, 9728);
            GL32.glTexParameteri(35866, 10240, 9728);
            GL32.glTexParameteri(35866, 10242, 33071);
            GL32.glTexParameteri(35866, 10243, 33071);
            GL32.glBindTexture(3553, this.accumulate);
            GL32.glTexImage2D(3553, 0, 34842, width, height, 0, 6408, 5120, 0L);
            GL32.glTexParameteri(3553, 10241, 9728);
            GL32.glTexParameteri(3553, 10240, 9728);
            GL32.glTexParameteri(3553, 10242, 33071);
            GL32.glTexParameteri(3553, 10243, 33071);
            GL32.glBindFramebuffer(36160, this.fbo);
            GL46.glFramebufferTexture(36160, 36064, this.depthBounds, 0);
            GL46.glFramebufferTextureLayer(36160, 36065, this.coefficients, 0, 0);
            GL46.glFramebufferTextureLayer(36160, 36066, this.coefficients, 0, 1);
            GL46.glFramebufferTextureLayer(36160, 36067, this.coefficients, 0, 2);
            GL46.glFramebufferTextureLayer(36160, 36068, this.coefficients, 0, 3);
            GL46.glFramebufferTexture(36160, 36069, this.accumulate, 0);
         }
      }
   }

   private void bindOutput(RenderTarget target) {
      int color = ((GlTexture)target.getColorTexture()).glId();
      int depth = target.getDepthTexture() == null ? 0 : ((GlTexture)target.getDepthTexture()).glId();
      if (GlCompat.SUPPORTS_DSA) {
         GL46.glNamedFramebufferTexture(this.outputFbo, 36064, color, 0);
         GL46.glNamedFramebufferTexture(this.outputFbo, 36096, depth, 0);
      } else {
         GL32.glBindFramebuffer(36160, this.outputFbo);
         GL32.glFramebufferTexture(36160, 36064, color, 0);
         GL32.glFramebufferTexture(36160, 36096, depth, 0);
      }

      GL32.glBindFramebuffer(36160, this.outputFbo);
      GL32.glViewport(0, 0, target.width, target.height);
   }
}
