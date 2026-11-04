package com.simibubi.create.compat.jei.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.api.client.gui.ILightingSettings;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.TypedInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = "create", value = Dist.CLIENT)
public final class GuiGameElement {
   private static final ThreadLocal<GuiGameElement.Batch> BATCH = new ThreadLocal<>();

   private GuiGameElement() {
   }

   @SubscribeEvent
   public static void registerRenderers(RegisterPictureInPictureRenderersEvent event) {
      event.register(GuiGameElement.ModelState.class, GuiGameElement.ModelRenderer::new);
   }

   public static void beginModelBatch(float x, float y, float z) {
      if (BATCH.get() != null) {
         throw new IllegalStateException("Nested JEI model batch");
      }

      BATCH.set(new GuiGameElement.Batch(x, y, z));
   }

   public static void endModelBatch(GuiGraphicsExtractor graphics) {
      GuiGameElement.Batch batch = BATCH.get();
      BATCH.remove();
      if (batch != null && batch.pose != null && !batch.entries.isEmpty()) {
         ScreenRectangle scissor = graphics.peekScissorStack();
         ScreenRectangle bounds = new ScreenRectangle(-80, -64, 160, 144).transformMaxBounds(batch.pose);
         if (scissor != null) {
            bounds = scissor.intersection(bounds);
         }

         graphics.submitPictureInPictureRenderState(
            new GuiGameElement.ModelState(List.copyOf(batch.entries), batch.pose, -80, -64, 80, 80, batch.scale, batch.rx, batch.ry, batch.rz, scissor, bounds)
         );
      }
   }

   public static GuiGameElement.GuiRenderBuilder of(BlockState state) {
      return new GuiGameElement.GuiRenderBuilder(state, null, null);
   }

   public static GuiGameElement.GuiRenderBuilder of(BlockStateModelPart part) {
      return new GuiGameElement.GuiRenderBuilder(null, part, null);
   }

   public static GuiGameElement.GuiRenderBuilder of(ItemStack stack) {
      return new GuiGameElement.GuiRenderBuilder(null, null, stack);
   }

   public static void submitFluidBox(
      TypedInstance<Fluid> fluid,
      float x,
      float y,
      float z,
      float scale,
      float postX,
      float postY,
      float postZ,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ
   ) {
      GuiGameElement.Batch batch = BATCH.get();
      if (batch == null) {
         throw new IllegalStateException("Fluid outside JEI model batch");
      }

      batch.entries.add(new GuiGameElement.Entry(null, null, fluid, x, y, z, 0.0F, 0.0F, 0.0F, scale, postX, postY, postZ, minX, minY, minZ, maxX, maxY, maxZ));
   }

   private static class Batch {
      final List<GuiGameElement.Entry> entries = new ArrayList<>();
      final float rx;
      final float ry;
      final float rz;
      Matrix3x2f pose;
      float scale;

      Batch(float rx, float ry, float rz) {
         this.rx = rx;
         this.ry = ry;
         this.rz = rz;
      }
   }

   public record Entry(
      @Nullable BlockState state,
      @Nullable BlockStateModelPart part,
      @Nullable TypedInstance<Fluid> fluid,
      float x,
      float y,
      float z,
      float rx,
      float ry,
      float rz,
      float scale,
      float postX,
      float postY,
      float postZ,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ
   ) {
   }

   public static final class GuiRenderBuilder {
      private final BlockState state;
      private final BlockStateModelPart part;
      private final ItemStack stack;
      private float x;
      private float y;
      private float localX;
      private float localY;
      private float localZ;
      private float rx;
      private float ry;
      private float rz;
      private float scale = 1.0F;

      private GuiRenderBuilder(BlockState state, BlockStateModelPart part, ItemStack stack) {
         this.state = state;
         this.part = part;
         this.stack = stack;
      }

      public GuiGameElement.GuiRenderBuilder at(float x, float y) {
         this.x = x;
         this.y = y;
         return this;
      }

      public GuiGameElement.GuiRenderBuilder at(float x, float y, float z) {
         return this.at(x, y);
      }

      public GuiGameElement.GuiRenderBuilder atLocal(float x, float y, float z) {
         this.localX = x;
         this.localY = y;
         this.localZ = z;
         return this;
      }

      public GuiGameElement.GuiRenderBuilder rotateBlock(double x, double y, double z) {
         this.rx = (float)x;
         this.ry = (float)y;
         this.rz = (float)z;
         return this;
      }

      public GuiGameElement.GuiRenderBuilder scale(double scale) {
         this.scale = (float)scale;
         return this;
      }

      public GuiGameElement.GuiRenderBuilder lighting(ILightingSettings settings) {
         return this;
      }

      public void submit(GuiGraphicsExtractor graphics) {
         if (this.stack != null) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(this.x, this.y).scale(this.scale);
            graphics.item(this.stack, 0, 0);
            graphics.pose().popMatrix();
         } else {
            boolean standalone = GuiGameElement.BATCH.get() == null;
            if (standalone) {
               GuiGameElement.beginModelBatch(0.0F, 0.0F, 0.0F);
            }

            GuiGameElement.Batch batch = GuiGameElement.BATCH.get();
            if (batch.pose == null) {
               batch.pose = new Matrix3x2f(graphics.pose()).translate(this.x, this.y);
               batch.scale = this.scale;
            }

            batch.entries
               .add(
                  new GuiGameElement.Entry(
                     this.state,
                     this.part,
                     null,
                     this.localX,
                     this.localY,
                     this.localZ,
                     this.rx,
                     this.ry,
                     this.rz,
                     1.0F,
                     0.0F,
                     0.0F,
                     0.0F,
                     0.0F,
                     0.0F,
                     0.0F,
                     0.0F,
                     0.0F,
                     0.0F
                  )
               );
            if (standalone) {
               GuiGameElement.endModelBatch(graphics);
            }
         }
      }
   }

   public static final class ModelRenderer extends PictureInPictureRenderer<GuiGameElement.ModelState> {
      public Class<GuiGameElement.ModelState> getRenderStateClass() {
         return GuiGameElement.ModelState.class;
      }

      protected String getTextureLabel() {
         return "create:jei_mechanism";
      }

      protected float getTranslateY(int height, int guiScale) {
         return 64.0F * guiScale;
      }

      protected void renderToTexture(GuiGameElement.ModelState state, PoseStack pose, SubmitNodeCollector collector) {
         pose.scale(1.0F, 1.0F, -1.0F);
         pose.rotate(Axis.XP.rotationDegrees(state.rx));
         pose.rotate(Axis.YP.rotationDegrees(state.ry));
         pose.rotate(Axis.ZP.rotationDegrees(state.rz));

         for (GuiGameElement.Entry entry : state.entries) {
            pose.pushPose();
            pose.translate(entry.x, entry.y, entry.z);
            pose.scale(1.0F, -1.0F, 1.0F);
            if (entry.fluid != null) {
               pose.scale(entry.scale, entry.scale, entry.scale);
               pose.translate(entry.postX, entry.postY, entry.postZ);
               FluidRenderHelper.submitFluidBox(
                  collector, entry.fluid, entry.minX, entry.minY, entry.minZ, entry.maxX, entry.maxY, entry.maxZ, pose, 15728880, false, true
               );
            } else {
               pose.translate(0.5, 0.5, 0.5);
               pose.rotate(Axis.ZP.rotationDegrees(entry.rz));
               pose.rotate(Axis.XP.rotationDegrees(entry.rx));
               pose.rotate(Axis.YP.rotationDegrees(entry.ry));
               pose.translate(-0.5, -0.5, -0.5);
               List<BlockStateModelPart> parts = new ArrayList<>();
               if (entry.part != null) {
                  parts.add(entry.part);
               }

               if (entry.state != null) {
                  Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(entry.state).collectParts(RandomSource.create(42L), parts);
               }

               for (BlockStateModelPart part : parts) {
                  RenderType type = (part.materialFlags() & 1) != 0 ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockItemSheet();
                  collector.submitBlockModel(pose, type, List.of(part), new int[0], 15728880, OverlayTexture.NO_OVERLAY, 0);
               }

               if (entry.state != null && !entry.state.getFluidState().isEmpty()) {
                  FluidRenderHelper.submitFluidBox(entry.state.getFluidState(), 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, collector, pose, 15728880, true, true);
               }
            }

            pose.popPose();
         }
      }
   }

   public record ModelState(
      List<GuiGameElement.Entry> entries,
      Matrix3x2f pose,
      int x0,
      int y0,
      int x1,
      int y1,
      float scale,
      float rx,
      float ry,
      float rz,
      @Nullable ScreenRectangle scissorArea,
      @Nullable ScreenRectangle bounds
   ) implements PictureInPictureRenderState {
   }
}
