package net.createmod.catnip.api.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.StagedVertexBuffer.Draw;
import net.minecraft.client.renderer.StagedVertexBuffer.ExecuteInfo;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class StagedSuperRenderTypeBuffer implements SuperRenderTypeBuffer, AutoCloseable {
   private final List<Map<RenderType, Batch>> phases = List.of(new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
   private final List<PreparedDraw> prepared = new ArrayList<>();
   private boolean uploaded;

   public void beginFrame() {
      this.prepared.clear();

      for (Map<RenderType, Batch> phase : this.phases) {
         Iterator<Batch> iterator = phase.values().iterator();

         while (iterator.hasNext()) {
            Batch batch = iterator.next();
            if (batch.draw == null) {
               batch.buffer.close();
               iterator.remove();
            } else {
               batch.buffer.endFrame();
               batch.draw = null;
            }
         }
      }

      this.uploaded = false;
   }

   private VertexConsumer buffer(int phase, RenderType type) {
      if (this.uploaded) {
         throw new IllegalStateException("Overlay vertices requested after upload");
      }

      Batch batch = this.phases.get(phase).computeIfAbsent(type, key -> new Batch(key));
      if (batch.draw == null) {
         batch.draw = batch.buffer
            .appendDraw(type.format(), type.primitiveTopology(), type.primitiveTopology() == PrimitiveTopology.QUADS ? VertexSorting.DISTANCE_TO_ORIGIN : null);
      }

      return batch.buffer.getVertexBuilder(batch.draw);
   }

   @Override
   public VertexConsumer getEarlyBuffer(RenderType type) {
      return this.buffer(0, type);
   }

   @Override
   public VertexConsumer getBuffer(RenderType type) {
      return this.buffer(1, type);
   }

   @Override
   public VertexConsumer getLateBuffer(RenderType type) {
      return this.buffer(2, type);
   }

   @Override
   public void draw() {
      if (!this.uploaded) {
         for (Map<RenderType, Batch> phase : this.phases) {
            for (Entry<RenderType, Batch> entry : phase.entrySet()) {
               Batch batch = entry.getValue();
               if (batch.draw != null) {
                  batch.buffer.upload();
                  ExecuteInfo info = batch.buffer.getExecuteInfo(batch.draw);
                  if (info != null) {
                     this.prepared.add(new PreparedDraw(entry.getKey().prepare(), info));
                  }
               }
            }
         }

         this.uploaded = true;
      }
   }

   @Override
   public void draw(RenderType type) {
      this.draw();
   }

   public void drawPrepared(RenderPass pass) {
      for (PreparedDraw draw : this.prepared) {
         draw.type.drawFromBuffer(draw.info, pass);
      }
   }

   public int preparedDrawCount() {
      return this.prepared.size();
   }

   @Override
   public void close() {
      this.prepared.clear();

      for (Map<RenderType, Batch> phase : this.phases) {
         for (Batch batch : phase.values()) {
            batch.buffer.close();
         }

         phase.clear();
      }

      this.uploaded = false;
   }

   private static final class Batch {
      final StagedVertexBuffer buffer;
      Draw draw;

      Batch(RenderType type) {
         this.buffer = new StagedVertexBuffer(() -> "Catnip overlay " + type, 4096);
      }
   }

   private record PreparedDraw(PreparedRenderType type, ExecuteInfo info) {
   }
}
