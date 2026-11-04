package dev.simulated_team.simulated.compat.create;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Defers client provider construction until NeoForge's renderer registration event. */
public final class SimClientRegistration {
    private static final List<java.util.function.Consumer<EntityRenderersEvent.RegisterRenderers>> RENDERERS=new ArrayList<>();
    public static <T extends BlockEntity,S extends BlockEntityRenderState> void renderer(
        BlockEntityType<? extends T> type,Supplier<BlockEntityRendererProvider<T,S>> provider){
        RENDERERS.add(event->event.registerBlockEntityRenderer(type,provider.get()));
    }
    public static <T extends net.minecraft.world.entity.Entity> void entity(
        net.minecraft.world.entity.EntityType<? extends T> type,Supplier<net.minecraft.client.renderer.entity.EntityRendererProvider<T>> provider){
        RENDERERS.add(event->event.registerEntityRenderer(type,provider.get()));
    }
    public static void register(EntityRenderersEvent.RegisterRenderers event){RENDERERS.forEach(action->action.accept(event));}
}
