package com.railwayteam.railways.internal.compat.client.render;

import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class AllBlockEntityRenders {
    private static final List<Consumer<EntityRenderersEvent.RegisterRenderers>> REGISTRATIONS=new ArrayList<>();
    private AllBlockEntityRenders() { }
    public static <T extends BlockEntity, S extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> void render(BlockEntityType<? extends T> type,BlockEntityRendererProvider<T,S> factory) { REGISTRATIONS.add(event->event.registerBlockEntityRenderer(type,factory)); }
    public static <T extends BlockEntity, S extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> void visual(BlockEntityType<T> type,BlockEntityRendererProvider<T,S> renderer,SimpleBlockEntityVisualizer.Factory<T> factory) { render(type,renderer);SimpleBlockEntityVisualizer.builder(type).factory(factory).apply(); }
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) { REGISTRATIONS.forEach(registration->registration.accept(event)); }
}
