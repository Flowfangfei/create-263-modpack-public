package dev.eriksonn.aeronautics.neoforge.events;
import dev.eriksonn.aeronautics.events.AeronauticsClientEvents;
import dev.eriksonn.aeronautics.neoforge.content.fluids.AeroFluidType;
import dev.eriksonn.aeronautics.neoforge.index.AeroFluidsNeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
public class AeroNeoForgeClientEvents {
    @SubscribeEvent public static void preClientTick(ClientTickEvent.Pre event){AeronauticsClientEvents.clientLevelTick(false);}
    @SubscribeEvent public static void postClientTick(ClientTickEvent.Post event){AeronauticsClientEvents.clientLevelTick(true);}
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerFluidModels(RegisterFluidModelsEvent event) {
            var fluid = AeroFluidsNeoForge.LEVITITE_BLEND;
            var type = (AeroFluidType) fluid.getType();
            var still = new Material(type.getStillTexture(), true);
            var flowing = new Material(type.getFlowingTexture(), true);
            FluidTintSource tint = state -> 0xffffffff;
            event.register(new FluidModel.Unbaked(still, flowing, flowing, tint),
                    fluid.getSource(), fluid.get());
        }

        @SubscribeEvent public static void registerClientExtensions(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event){
            var type=(AeroFluidType)AeroFluidsNeoForge.LEVITITE_BLEND.getType();
            event.registerFluidType(new net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions(){
                @Override public void modifyFogColor(net.minecraft.client.Camera camera,float partialTick,net.minecraft.client.multiplayer.ClientLevel level,int distance,float darken,org.joml.Vector4f color){var custom=type.getCustomFogColor();if(custom!=null)color.set(custom.x,custom.y,custom.z,color.w);}
                @Override public void modifyFogRender(net.minecraft.client.Camera camera,net.minecraft.client.renderer.fog.environment.FogEnvironment environment,float distance,float partialTick,net.minecraft.client.renderer.fog.FogData fog){
                    float modifier=type.getFogDistanceModifier();if(modifier!=1){fog.environmentalStart=-8;fog.environmentalEnd=96*modifier;}
                }
            },type);
        }
    }
}
