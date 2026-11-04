package dev.engine_room.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import dev.engine_room.flywheel.api.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Vector3f;

@Internal
public class NeoforgeMeshEmitter extends MeshEmitter {
   private final RenderType renderType;

   NeoforgeMeshEmitter(ByteBufferBuilderStack byteBufferBuilderStack, RenderType renderType) {
      super(byteBufferBuilderStack, renderType);
      this.renderType = renderType;
   }

   public void put(Pose pose, BakedQuad quad, QuadInstance instance, BlockAndTintGetter level, BlockPos pos, BlockState state) {
      MaterialInfo info = quad.materialInfo();
      Material material = this.blockMaterialFunction.apply(this.renderType, info.shadeDirectionOverride() == null, info.ambientOcclusion());
      if (material != null) {
         int color = -1;
         if (info.isTinted()) {
            BlockTintSource tintSource = Minecraft.getInstance().getBlockColors().getTintSource(state, info.tintIndex());
            if (tintSource != null) {
               color = tintSource.colorInWorld(state, level, pos);
            }
         }

         instance.setColor(color);
         BufferBuilder buffer = this.getBuffer(material);
         buffer.putBakedQuad(pose, quad, instance);

         for (int vertex = 0; vertex < 4; vertex++) {
            int packed = quad.bakedNormals().normal(vertex);
            Vector3f normal = BakedNormals.isUnspecified(packed) ? new Vector3f(quad.direction().getUnitVec3f()) : BakedNormals.unpack(packed, new Vector3f());
            this.recordVertexNormal(buffer, pose.transformNormal(normal, normal));
         }
      }
   }
}
