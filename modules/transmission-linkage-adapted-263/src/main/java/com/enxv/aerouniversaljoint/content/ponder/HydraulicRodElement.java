package com.enxv.aerouniversaljoint.content.ponder;

import com.enxv.aerouniversaljoint.content.HydraulicConnectionHeadBlockEntity;
import com.enxv.aerouniversaljoint.content.HydraulicConnectionHeadRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.createmod.catnip.api.theme.Color;
import net.createmod.ponder.api.client.element.ElementLink;
import net.createmod.ponder.api.client.element.WorldSectionElement;
import net.createmod.ponder.api.client.level.PonderLevel;
import net.createmod.ponder.api.client.scene.PonderScene;
import net.createmod.ponder.impl.client.element.AnimatedSceneElementBase;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class HydraulicRodElement extends AnimatedSceneElementBase {
    private final SectionAnchor startAnchor;
    private final SectionAnchor endAnchor;
    private final HydraulicConnectionHeadBlockEntity previewHead;
    private final boolean creative;

    private Vec3 lastStart;
    private Vec3 currentStart;
    private Vec3 lastEnd;
    private Vec3 currentEnd;

    public HydraulicRodElement(ElementLink<WorldSectionElement> startSection, ElementLink<WorldSectionElement> endSection,
                               Vec3 startAnchor, Vec3 endAnchor, BlockState renderState, boolean creative) {
        this(new SectionAnchor(startSection, startAnchor), new SectionAnchor(endSection, endAnchor), renderState, creative);
    }

    public HydraulicRodElement(SectionAnchor startAnchor, SectionAnchor endAnchor, BlockState renderState, boolean creative) {
        this.startAnchor = startAnchor;
        this.endAnchor = endAnchor;
        this.previewHead = new HydraulicConnectionHeadBlockEntity(net.minecraft.core.BlockPos.ZERO, renderState);
        this.creative = creative;
        this.lastStart = startAnchor.position();
        this.currentStart = startAnchor.position();
        this.lastEnd = endAnchor.position();
        this.currentEnd = endAnchor.position();
    }

    @Override
    public void reset(PonderScene scene) {
        super.reset(scene);
        this.lastStart = this.currentStart = this.startAnchor.resolve(scene);
        this.lastEnd = this.currentEnd = this.endAnchor.resolve(scene);
    }

    @Override
    public void tick(PonderScene scene) {
        this.lastStart = this.currentStart;
        this.lastEnd = this.currentEnd;
        this.currentStart = this.startAnchor.resolve(scene);
        this.currentEnd = this.endAnchor.resolve(scene);
    }

    @Override
    public void whileSkipping(PonderScene scene) {
        this.tick(scene);
    }

    @Override
    protected void renderLast(PonderLevel level, net.createmod.catnip.impl.client.render.MultiBufferSource immediate, SubmitNodeCollector buffer, Camera camera, CameraRenderState cameraState, PoseStack ps, float fade, float partialTicks) {
        Vec3 start = lerp(this.lastStart, this.currentStart, partialTicks);
        Vec3 end = lerp(this.lastEnd, this.currentEnd, partialTicks);
        if (start.distanceToSqr(end) < 1.0E-6D) {
            return;
        }

        SubmitNodeCollector consumer = buffer;
        Color color = Color.WHITE.copy().scaleAlpha(fade);
        HydraulicConnectionHeadRenderer.renderPreview(this.previewHead, start, end, ps, consumer,
                LightCoordsUtil.FULL_BRIGHT, color, partialTicks, this.creative);
    }

    private static Vec3 lerp(Vec3 start, Vec3 end, float partialTicks) {
        return new Vec3(
                Mth.lerp(partialTicks, start.x, end.x),
                Mth.lerp(partialTicks, start.y, end.y),
                Mth.lerp(partialTicks, start.z, end.z));
    }

}
