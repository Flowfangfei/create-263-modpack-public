/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.content.custom_bogeys.renderer.unified.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.railwayteam.railways.content.custom_bogeys.renderer.unified.BogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.unified.BogeyDisplayHolder;
import com.simibubi.create.content.trains.bogey.BogeyRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.client.render.SpriteShiftEntry;
import net.createmod.catnip.api.client.render.SuperBufferFactory;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.SuperByteBufferCache;
import net.createmod.catnip.api.data.Couple;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@ApiStatus.Internal
public class UnifiedBogeyRenderer implements BogeyRenderer, BogeyDisplayHolder {
    private static final SuperByteBufferCache.Compartment<PartialModel> PARTIALS = new SuperByteBufferCache.Compartment<>();
    static {
        SuperByteBufferCache.getInstance().registerCompartment(PARTIALS);
    }
    private final Couple<Renderer> renderers;
    private final @Nullable BogeyRenderer customRenderer;

    public UnifiedBogeyRenderer(BogeyDisplay.Factory factory) {
        renderers = Couple.createWithContext(inContraption -> Renderer.create(factory, inContraption));
        customRenderer = factory.createCustomRenderer();
    }

    @Override
    public void runWithDisplay(Consumer<BogeyDisplay> consumer) {
        consumer.accept(renderers.getFirst().display);
        consumer.accept(renderers.getSecond().display);
        if (customRenderer instanceof BogeyDisplayHolder holder) holder.runWithDisplay(consumer);
    }

    @Override
    public void render(CompoundTag data, float angle, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light, int overlay, boolean inContraption) {
        var commands = prepare(data, angle, inContraption);
        VertexConsumer consumer = buffers.getBuffer(RenderTypes.cutoutMovingBlock());
        for (DrawCommand command : commands) draw(command, poseStack.last(), consumer, light, overlay);
        if (customRenderer != null)
            customRenderer.render(data, angle, partialTick, poseStack, buffers, light, overlay, inContraption);
    }

    @Override
    public void submit(CompoundTag data, float angle, float partialTick, PoseStack poseStack,
                       SubmitNodeCollector collector, int light, int overlay, boolean inContraption) {
        // Copy per-element matrices and UV offsets before the collector defers rendering.
        // Create's BogeyStyle already applies the shared -1.5 vertical model offset.
        List<DrawCommand> commands = prepare(data, angle, inContraption);
        collector.submitCustomGeometry(poseStack, RenderTypes.cutoutMovingBlock(), (pose, consumer) -> {
            for (DrawCommand command : commands) draw(command, pose, consumer, light, overlay);
        });
        if (customRenderer != null)
            customRenderer.submit(data, angle, partialTick, poseStack, collector, light, overlay, inContraption);
    }

    private List<DrawCommand> prepare(@Nullable CompoundTag data, float angle, boolean inContraption) {
        Renderer renderer = renderers.get(inContraption);
        renderer.reset();
        renderer.display.update(data == null ? new CompoundTag() : data, angle);
        List<DrawCommand> commands = new ArrayList<>(renderer.allElements.size());
        for (RenderedElement.Single element : renderer.singleElements)
            commands.add(new DrawCommand(element.model(), new Matrix4f(element.element().pose), null, 0));
        for (RenderedElement.Multiple elements : renderer.multipleElements)
            for (RenderedElement element : elements.elements())
                commands.add(new DrawCommand(elements.model(), new Matrix4f(element.pose), null, 0));
        for (RenderedElement.Scrolling element : renderer.scrollingElements) {
            float spriteSize = element.entry.getTarget().getV1() - element.entry.getTarget().getV0();
            float scroll = (element.shiftV - Mth.floor(element.shiftV)) * spriteSize * 0.5f;
            commands.add(new DrawCommand(element.model, new Matrix4f(element.element.pose), element.entry, scroll));
        }
        return List.copyOf(commands);
    }

    private static void draw(DrawCommand command, PoseStack.Pose root, VertexConsumer consumer,
                             int light, int overlay) {
        // Zero scale deliberately hides handcar parts in first/third person.
        if (command.transform.determinant() == 0) return;
        PoseStack pose = new PoseStack();
        pose.last().pose().set(root.pose());
        pose.last().normal().set(root.normal());
        pose.mulPose(command.transform);
        SuperByteBuffer buffer = SuperByteBufferCache.getInstance().get(PARTIALS, command.model,
            () -> SuperBufferFactory.getInstance().createForBlock(new SingleVariant(command.model.get()),
                Blocks.AIR.defaultBlockState()));
        buffer.reset();
        buffer.light(light).overlay(overlay);
        if (command.shift != null) buffer.shiftUVScrolling(command.shift, command.scrollV);
        var complete = new com.railwayteam.railways.internal.compat.client.render.CompleteVertexConsumer(consumer, -1, overlay);
        try { buffer.renderInto(pose, complete); complete.finish(); } finally { buffer.reset(); }
    }

    private record DrawCommand(PartialModel model, Matrix4f transform, @Nullable SpriteShiftEntry shift,
                               float scrollV) {}

    private record Renderer(
        BogeyDisplay display,
        List<RenderedElement.Single> singleElements,
        List<RenderedElement.Multiple> multipleElements,
        List<RenderedElement.Scrolling> scrollingElements,
        List<RenderedElement> allElements
    ) {
        private void reset() {
            allElements.forEach(element -> element.pose.identity());
            scrollingElements.forEach(element -> element.shiftV = 0);
        }

        private static Renderer create(BogeyDisplay.Factory factory, boolean inContraption) {
            ArrayList<RenderedElement.Single> singleElements = new ArrayList<>();
            ArrayList<RenderedElement.Multiple> multipleElements = new ArrayList<>();
            ArrayList<RenderedElement.Scrolling> scrollingElements = new ArrayList<>();

            var prov = new RenderedElementProvider(singleElements, multipleElements, scrollingElements);
            BogeyDisplay display = factory.create(prov, inContraption);
            prov.freeze();

            List<RenderedElement> allElements = new ArrayList<>();
            singleElements.forEach(s -> allElements.add(s.element()));
            multipleElements.forEach(m -> Collections.addAll(allElements, m.elements()));
            scrollingElements.forEach(s -> allElements.add(s.element));

            return new Renderer(display, singleElements, multipleElements, scrollingElements, allElements);
        }
    }
}
