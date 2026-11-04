package dev.engine_room.flywheel.backend.engine;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import dev.engine_room.flywheel.api.backend.Engine;
import dev.engine_room.flywheel.api.backend.RenderContext;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visualization.VisualEmbedding;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.backend.FlwBackend;
import dev.engine_room.flywheel.backend.engine.embed.EmbeddedEnvironment;
import dev.engine_room.flywheel.backend.engine.embed.Environment;
import dev.engine_room.flywheel.backend.engine.embed.EnvironmentStorage;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.backend.gl.GlStateTracker;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL32;

public class EngineImpl implements Engine {
   private final DrawManager<? extends AbstractInstancer<?>> drawManager;
   private final int sqrMaxOriginDistance;
   private final EnvironmentStorage environmentStorage;
   private final LightStorage lightStorage;
   private int worldTargetFbo = -1;
   private BlockPos renderOrigin = BlockPos.ZERO;
   private boolean fallbackQueued;

   public EngineImpl(LevelAccessor level, DrawManager<? extends AbstractInstancer<?>> drawManager, int maxOriginDistance) {
      this.drawManager = drawManager;
      this.sqrMaxOriginDistance = maxOriginDistance * maxOriginDistance;
      this.environmentStorage = new EnvironmentStorage();
      this.lightStorage = new LightStorage(level);
   }

   @Override
   public VisualizationContext createVisualizationContext() {
      return new EngineImpl.VisualizationContextImpl();
   }

   @Override
   public Plan<RenderContext> createFramePlan() {
      return this.drawManager.createFramePlan().and(this.lightStorage.createFramePlan());
   }

   @Override
   public Vec3i renderOrigin() {
      return this.renderOrigin;
   }

   @Override
   public boolean updateRenderOrigin(Vec3 cameraPos) {
      double dx = this.renderOrigin.getX() - cameraPos.x;
      double dy = this.renderOrigin.getY() - cameraPos.y;
      double dz = this.renderOrigin.getZ() - cameraPos.z;
      double distanceSqr = dx * dx + dy * dy + dz * dz;
      if (distanceSqr <= this.sqrMaxOriginDistance) {
         return false;
      }

      this.renderOrigin = BlockPos.containing(cameraPos);
      this.drawManager.onRenderOriginChanged();
      return true;
   }

   @Override
   public void lightSections(LongSet sections) {
      this.lightStorage.sections(sections);
   }

   @Override
   public void onLightUpdate(SectionPos sectionPos, LightLayer layer) {
      this.lightStorage.onLightUpdate(sectionPos.asLong());
   }

   @Override
   public void render(RenderContext context) {
      try (GlStateTracker.State state = GlStateTracker.getRestoreState()) {
         this.bindWorldTarget();
         Uniforms.update(context);
         this.environmentStorage.flush();
         this.drawManager.render(this.lightStorage, this.environmentStorage);
      } catch (Exception e) {
         FlwBackend.LOGGER.error("Falling back", e);
         this.triggerFallback();
      }
   }

   @Override
   public void renderCrumbling(RenderContext context, List<Engine.CrumblingBlock> crumblingBlocks) {
      try (GlStateTracker.State state = GlStateTracker.getRestoreState()) {
         this.drawManager.renderCrumbling(crumblingBlocks);
      } catch (Exception e) {
         FlwBackend.LOGGER.error("Falling back", e);
         this.triggerFallback();
      }
   }

   @Override
   public void delete() {
      this.drawManager.delete();
      this.lightStorage.delete();
      this.environmentStorage.delete();
      if (this.worldTargetFbo != -1) {
         GL32.glDeleteFramebuffers(this.worldTargetFbo);
         this.worldTargetFbo = -1;
      }
   }

   private void bindWorldTarget() {
      RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
      if (this.worldTargetFbo == -1) {
         this.worldTargetFbo = GL32.glGenFramebuffers();
      }

      GL32.glBindFramebuffer(36160, this.worldTargetFbo);
      int color = ((GlTexture)target.getColorTexture()).glId();
      GL32.glFramebufferTexture(36160, 36064, color, 0);
      if (target.getDepthTexture() != null) {
         int depth = ((GlTexture)target.getDepthTexture()).glId();
         GL32.glFramebufferTexture(36160, 36096, depth, 0);
      } else {
         GL32.glFramebufferTexture(36160, 36096, 0, 0);
      }

      GL32.glDrawBuffer(36064);
      GL32.glViewport(0, 0, target.width, target.height);
   }

   private void triggerFallback() {
      if (!this.fallbackQueued) {
         this.fallbackQueued = true;
         Minecraft.getInstance().schedule(this.drawManager::triggerFallback);
      }
   }

   public <I extends Instance> Instancer<I> instancer(Environment environment, InstanceType<I> type, Model model, int bias) {
      return this.drawManager.getInstancer(environment, type, model, bias);
   }

   public EnvironmentStorage environmentStorage() {
      return this.environmentStorage;
   }

   public LightStorage lightStorage() {
      return this.lightStorage;
   }

   public DrawManager<? extends AbstractInstancer<?>> drawManager() {
      return this.drawManager;
   }

   private class VisualizationContextImpl implements VisualizationContext {
      private final InstancerProviderImpl instancerProvider = new InstancerProviderImpl(EngineImpl.this);

      public VisualizationContextImpl() {
      }

      @Override
      public InstancerProvider instancerProvider() {
         return this.instancerProvider;
      }

      @Override
      public Vec3i renderOrigin() {
         return EngineImpl.this.renderOrigin();
      }

      @Override
      public VisualEmbedding createEmbedding(Vec3i renderOrigin) {
         EmbeddedEnvironment out = new EmbeddedEnvironment(EngineImpl.this, renderOrigin);
         EngineImpl.this.environmentStorage.track(out);
         return out;
      }
   }
}
