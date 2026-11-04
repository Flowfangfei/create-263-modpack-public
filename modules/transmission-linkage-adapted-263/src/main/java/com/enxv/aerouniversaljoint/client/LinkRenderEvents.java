package com.enxv.aerouniversaljoint.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

/** The extract phase snapshots link geometry; submission never reads the world. */
public final class LinkRenderEvents {
    private static final ContextKey<List<Draw>> GEOMETRY = new ContextKey<>(Identifier.fromNamespaceAndPath("aeronautics_utility_objects", "links"));
    private LinkRenderEvents() {}

    public static void extract(ExtractLevelRenderStateEvent event) {
        List<Draw> draws = new ArrayList<>();
        SubmitNodeCollector collector = (SubmitNodeCollector) Proxy.newProxyInstance(SubmitNodeCollector.class.getClassLoader(),
                new Class<?>[]{SubmitNodeCollector.class}, (proxy, method, args) -> {
                    if (method.getName().equals("order")) return proxy;
                    if (method.getName().equals("submitCustomGeometry")) {
                        PoseStack source = (PoseStack) args[0];
                        PoseStack snapshot = new PoseStack();
                        snapshot.mulPose(source.last().pose());
                        draws.add(new Draw(snapshot, (RenderType) args[1], (SubmitNodeCollector.CustomGeometryRenderer) args[2]));
                        return null;
                    }
                    throw new UnsupportedOperationException("Link extraction only records custom geometry: " + method.getName());
                });
        Context context = new Context(new PoseStack(), event.getCamera().position(), event.getDeltaTracker().getGameTimeDeltaPartialTick(true), collector);
        ConnectionPreviewRenderer.render(context);
        HydraulicRodTargeting.render(context);
        event.getRenderState().setRenderData(GEOMETRY, List.copyOf(draws));
    }

    public static void submit(SubmitCustomGeometryEvent event) {
        List<Draw> draws = event.getLevelRenderState().getRenderData(GEOMETRY);
        if (draws == null) return;
        for (Draw draw : draws) event.getSubmitNodeCollector().submitCustomGeometry(draw.pose(), draw.type(), draw.geometry());
    }

    public record Context(PoseStack getPoseStack, Vec3 cameraPosition, float partialTicks, SubmitNodeCollector getCollector) {}
    private record Draw(PoseStack pose, RenderType type, SubmitNodeCollector.CustomGeometryRenderer geometry) {}
}
