package dev.engine_room.flywheel.backend.engine.uniform;

import dev.engine_room.flywheel.api.backend.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.CardinalLighting.Type;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class LevelUniforms extends UniformWriter {
   private static final int SIZE = 116;
   static final UniformBuffer BUFFER = new UniformBuffer(4, 116);
   public static final Vector3f LIGHT0_DIRECTION = new Vector3f();
   public static final Vector3f LIGHT1_DIRECTION = new Vector3f();

   private LevelUniforms() {
   }

   public static void update(RenderContext context) {
      long ptr = BUFFER.ptr();
      ClientLevel level = context.level();
      float partialTick = context.partialTick();
      LevelRenderState levelState = Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState;
      SkyRenderState sky = levelState.skyRenderState;
      Vector3fc skyColor = sky.skyColor;
      int cloudColor = levelState.cloudColor;
      ptr = writeVec4(ptr, skyColor == null ? 0.0F : skyColor.x(), skyColor == null ? 0.0F : skyColor.y(), skyColor == null ? 0.0F : skyColor.z(), 1.0F);
      ptr = writeVec4(ptr, ARGB.redFloat(cloudColor), ARGB.greenFloat(cloudColor), ARGB.blueFloat(cloudColor), 1.0F);
      if (level.dimensionType().cardinalLightType() == Type.NETHER) {
         LIGHT0_DIRECTION.set(0.2F, 1.0F, -0.7F).normalize();
         LIGHT1_DIRECTION.set(-0.2F, -1.0F, 0.7F).normalize();
      } else {
         LIGHT0_DIRECTION.set(0.2F, 1.0F, -0.7F).normalize();
         LIGHT1_DIRECTION.set(-0.2F, 1.0F, 0.7F).normalize();
      }

      ptr = writeVec3(ptr, LIGHT0_DIRECTION);
      ptr = writeVec3(ptr, LIGHT1_DIRECTION);
      long dayTime = levelState.gameTime;
      long levelDay = dayTime / 24000L;
      float timeOfDay = (float)(dayTime - levelDay * 24000L) / 24000.0F;
      ptr = writeInt(ptr, (int)(levelDay % 2147483647L));
      ptr = writeFloat(ptr, timeOfDay);
      ptr = writeInt(ptr, level.dimensionType().hasSkyLight() ? 1 : 0);
      ptr = writeFloat(ptr, sky.sunAngle);
      ptr = writeFloat(ptr, sky.starBrightness);
      ptr = writeInt(ptr, sky.moonPhase.index());
      ptr = writeInt(ptr, sky.rainBrightness < 1.0F ? 1 : 0);
      ptr = writeFloat(ptr, 1.0F - sky.rainBrightness);
      ptr = writeInt(ptr, 0);
      ptr = writeFloat(ptr, 0.0F);
      ptr = writeFloat(ptr, 1.0F - sky.rainBrightness);
      ptr = writeInt(ptr, level.dimensionType().ambientLight() > 0.0F ? 1 : 0);
      ResourceKey<Level> dimension = level.dimension();
      int dimensionId;
      if (Level.OVERWORLD.equals(dimension)) {
         dimensionId = 0;
      } else if (Level.NETHER.equals(dimension)) {
         dimensionId = 1;
      } else if (Level.END.equals(dimension)) {
         dimensionId = 2;
      } else {
         dimensionId = -1;
      }

      ptr = writeInt(ptr, dimensionId);
      BUFFER.markDirty();
   }
}
