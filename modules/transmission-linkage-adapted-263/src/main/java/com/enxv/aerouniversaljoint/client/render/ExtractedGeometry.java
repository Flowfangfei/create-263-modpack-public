package com.enxv.aerouniversaljoint.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

/** Records only immutable custom-geometry submissions; a world lookup belongs to extraction. */
public final class ExtractedGeometry {
    private ExtractedGeometry() {}
    public static List<Draw> capture(Consumer<SubmitNodeCollector> extract) {
        List<Draw> draws = new ArrayList<>();
        SubmitNodeCollector collector = (SubmitNodeCollector) Proxy.newProxyInstance(SubmitNodeCollector.class.getClassLoader(),
                new Class<?>[]{SubmitNodeCollector.class}, (proxy, method, args) -> {
                    if (method.getName().equals("order")) return proxy;
                    if (method.getName().equals("submitCustomGeometry")) {
                        PoseStack source = (PoseStack) args[0];
                        PoseStack snapshot = new PoseStack(); snapshot.mulPose(source.last().pose());
                        draws.add(new Draw(snapshot, (RenderType) args[1], (SubmitNodeCollector.CustomGeometryRenderer) args[2]));
                        return null;
                    }
                    throw new UnsupportedOperationException("Link extraction only records custom geometry: " + method.getName());
                });
        extract.accept(collector);
        return List.copyOf(draws);
    }
    public record Draw(PoseStack pose, RenderType type, SubmitNodeCollector.CustomGeometryRenderer geometry) {
        public void submit(PoseStack parent, SubmitNodeCollector collector) {
            PoseStack combined = new PoseStack(); combined.mulPose(parent.last().pose()); combined.mulPose(pose.last().pose());
            collector.submitCustomGeometry(combined, type, geometry);
        }
    }
}
