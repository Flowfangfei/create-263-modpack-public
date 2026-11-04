package dev.engine_room.flywheel.lib.model.baked;

import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector3f;
import org.joml.Vector3fc;

class MeshEmitter {
   private static final int INITIAL_CAPACITY = 1;
   private final ByteBufferBuilderStack byteBufferBuilderStack;
   private final RenderType renderType;
   private Material @UnknownNullability [] materials = new Material[1];
   private BufferBuilder @UnknownNullability [] bufferBuilders = new BufferBuilder[1];
   private List<Vector3f> @UnknownNullability [] vertexNormals = new List[1];
   private int numBufferBuildersPopulated = 0;
   @UnknownNullability BlockMaterialFunction blockMaterialFunction;
   private int currentIndex = 0;

   MeshEmitter(ByteBufferBuilderStack byteBufferBuilderStack, RenderType renderType) {
      this.byteBufferBuilderStack = byteBufferBuilderStack;
      this.renderType = renderType;
   }

   public void prepare(BlockMaterialFunction blockMaterialFunction) {
      this.blockMaterialFunction = blockMaterialFunction;
   }

   public void prepareForBlock() {
      this.currentIndex = 0;
   }

   public void end(Builder<Model.ConfiguredMesh> out) {
      for (int index = 0; index < this.numBufferBuildersPopulated; index++) {
         MeshData renderedBuffer = this.bufferBuilders[index].build();
         if (renderedBuffer != null) {
            Material material = this.materials[index];
            Mesh mesh = MeshHelper.blockVerticesToMesh(renderedBuffer, "source=ModelBuilder,material=" + material, this.vertexNormals[index]);
            out.add(new Model.ConfiguredMesh(material, mesh));
            renderedBuffer.close();
         }
      }

      Arrays.fill(this.bufferBuilders, 0, this.numBufferBuildersPopulated, null);
      Arrays.fill(this.materials, 0, this.numBufferBuildersPopulated, null);
      Arrays.fill(this.vertexNormals, 0, this.numBufferBuildersPopulated, null);
      this.currentIndex = 0;
      this.numBufferBuildersPopulated = 0;
      this.blockMaterialFunction = null;
   }

   public BufferBuilder getBuffer(Material material) {
      while (this.currentIndex < this.numBufferBuildersPopulated) {
         if (material.equals(this.materials[this.currentIndex])) {
            return this.bufferBuilders[this.currentIndex];
         }

         this.currentIndex++;
      }

      if (this.currentIndex >= this.materials.length) {
         this.resize(this.materials.length * 2);
      }

      ByteBufferBuilder byteBufferBuilder = this.byteBufferBuilderStack.nextOrCreate();
      BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, this.renderType.primitiveTopology(), this.renderType.format());
      this.materials[this.currentIndex] = material;
      this.bufferBuilders[this.currentIndex] = bufferBuilder;
      this.numBufferBuildersPopulated++;
      return bufferBuilder;
   }

   protected void recordVertexNormal(BufferBuilder bufferBuilder, Vector3fc normal) {
      for (int index = 0; index < this.numBufferBuildersPopulated; index++) {
         if (this.bufferBuilders[index] == bufferBuilder) {
            List<Vector3f> normals = this.vertexNormals[index];
            if (normals == null) {
               this.vertexNormals[index] = normals = new ArrayList<>();
            }

            normals.add(new Vector3f(normal));
            return;
         }
      }
   }

   private void resize(int capacity) {
      BufferBuilder[] newBufferBuilders = new BufferBuilder[capacity];
      Material[] newMaterials = new Material[capacity];
      List<Vector3f>[] newVertexNormals = new List[capacity];
      System.arraycopy(this.bufferBuilders, 0, newBufferBuilders, 0, this.numBufferBuildersPopulated);
      System.arraycopy(this.materials, 0, newMaterials, 0, this.numBufferBuildersPopulated);
      System.arraycopy(this.vertexNormals, 0, newVertexNormals, 0, this.numBufferBuildersPopulated);
      this.bufferBuilders = newBufferBuilders;
      this.materials = newMaterials;
      this.vertexNormals = newVertexNormals;
   }
}
