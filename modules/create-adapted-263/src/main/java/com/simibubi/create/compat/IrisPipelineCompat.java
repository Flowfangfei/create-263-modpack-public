package com.simibubi.create.compat;

import java.lang.reflect.Method;
import net.createmod.catnip.api.client.render.CatnipRenderPipelines;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(Dist.CLIENT)
public final class IrisPipelineCompat {
   @SubscribeEvent
   public static void setup(FMLClientSetupEvent event) {
      if (ModList.get().isLoaded("iris")) {
         event.enqueueWork(IrisPipelineCompat.Mappings::register);
      }
   }

   private static final class Mappings {
      static void register() {
         IrisApi api = IrisApi.getInstance();
         assign(api, RenderPipelines.DEBUG_QUADS, IrisProgram.BASIC);

         try {
            Object ghost = CatnipRenderPipelines.class.getField("GHOST_BLOCK").get(null);
            assign(api, ghost, IrisProgram.BLOCK_TRANSLUCENT);
         } catch (NoSuchFieldException missingOptionalPipeline) {
         } catch (IllegalAccessException failure) {
            throw new IllegalStateException("Cannot register Catnip's public ghost pipeline", failure);
         }
      }

      private static void assign(IrisApi api, Object pipeline, IrisProgram program) {
         for (Method method : IrisApi.class.getMethods()) {
            if (method.getName().equals("assignPipeline")
               && method.getParameterCount() == 2
               && method.getParameterTypes()[0].isInstance(pipeline)
               && method.getParameterTypes()[1].isInstance(program)) {
               try {
                  method.invoke(api, pipeline, program);
                  return;
               } catch (ReflectiveOperationException failure) {
                  throw new IllegalStateException("Cannot register Iris pipeline", failure);
               }
            }
         }

         throw new IllegalStateException("Installed Iris has no compatible assignPipeline API");
      }
   }
}
