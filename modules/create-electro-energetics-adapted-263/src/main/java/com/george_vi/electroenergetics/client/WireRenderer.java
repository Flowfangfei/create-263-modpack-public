package com.george_vi.electroenergetics.client;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.clamp_meter.ClampMeterRenderer;
import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickRenderer;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlock;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.WirePoints;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.mixins.LevelRendererAccessor;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.george_vi.electroenergetics.simulation.infrastructure.detached_nodes.DetachedNodeHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import it.unimi.dsi.fastutil.ints.IntSet;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.catnip.data.Pair;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.LightCoordsUtil;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;
import java.util.*;

public class WireRenderer {
    public static List<Pair<InWorldNodeConnection, WireData>> WIRE_CONNECTIONS = new ArrayList<>();

    public static Map<InWorldNode, ClientNodeData> NODE_DATA = new HashMap<>();
    protected static Map<InWorldNodeConnection, WireEffect> WIRE_EFFECTS = new HashMap<>();
    protected static Map<CatenaryConnection, WireEffect> CATENARY_EFFECTS = new HashMap<>();
    public static List<CatenaryConnection> CATENARY = new ArrayList<>();
    public static RenderCollector extract(Camera camera) {
        RenderCollector buffer=new RenderCollector();
        buffer.cardinalLighting=com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer.getCardinalLighting(Minecraft.getInstance().level);
        collect(buffer,new PoseStack(),camera);
        CEEHighlights.extract(camera,buffer);
        return buffer;
    }
    private static void collect(RenderCollector buffer,PoseStack pose,Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        float partialTick = AnimationTickHolder.getPartialTicks();
        ClientLevel level = mc.level;
        if (level == null)
            return;
        



        LinemansStickRenderer.renderFirstPerson(level, pose, buffer, camera);
        ClampMeterRenderer.renderFirstPerson(level, pose, buffer, camera);
        DetachedNodeRenderer.render(level, pose, buffer, camera);

        Map<InWorldNode, List<Vec3>> outerInsulatorJumpers = new HashMap<>();

        pose.pushPose();
        pose.translate(-camera.position().x(), -camera.position().y(), -camera.position().z());

        float baseCatenaryWidth = 0.66f;
        boolean renderImmediately = !VisualizationManager.supportsVisualization(level);
        if (CEEConfigs.client().disableFlywheelWireRendering.get())
            renderImmediately = true;
        Vec3 cameraPosition = mc.gameRenderer.mainCamera().position();
        if (renderImmediately)
            for (CatenaryConnection connection : CATENARY) {
                BlockPos pos1 = connection.pos1();
                BlockPos pos2 = connection.pos2();

                if (!InWorldNode.isPosFullyLoadable(level, pos1) ||
                        !InWorldNode.isPosFullyLoadable(level, pos2))
                    continue;
                
                BlockState startingState = level.getBlockState(pos1);
                BlockState endingState = level.getBlockState(pos2);
                AABB bb = AABB.encapsulatingFullBlocks(pos1, pos2);
                if (!camera.getCullFrustum().isVisible(bb))
                    continue;

                Vec3 start = Vec3.atCenterOf(pos1).add(0, -0.5, 0);
                Vec3 end = Vec3.atCenterOf(pos2).add(0, -0.5, 0);

                List<Vec3> lowerWirePoints = QuadraticWireHelper.cablePoints(start, end, 0, 10);

                for (int i = 0; i < lowerWirePoints.size(); i++) {
                    Vec3 point = lowerWirePoints.get(i);

                    if (point.distanceTo(cameraPosition) > CEEConfigs.client().wireRenderDistance.get())
                        continue;

                    Vec3 nextPoint = i == lowerWirePoints.size() - 1 ? end : lowerWirePoints.get(i + 1);

                    // the reason for catenaries to have variable width, is that when the player is far away, normal-width catenaries look out of place, and they give off a vibe of something solid or smth??

                    float catenaryThickness = (float) Mth.clamp(baseCatenaryWidth / (cameraPosition.distanceTo(point) / 30), 0.4f, baseCatenaryWidth);

                    ModelBuffers.partial(CEEPartialModels.WIRE_SEGMENT, Blocks.ANDESITE.defaultBlockState())
                            .translate(point)
                            .rotateY((float) Math.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                            .rotateX(-(float) Math.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                            .scale(catenaryThickness, catenaryThickness, (float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                            .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LightCoordsUtil.getLightCoords(level, BlockPos.containing(point.add(nextPoint).scale(0.5))) :
                                    maxLightLevel(LightCoordsUtil.getLightCoords(level, BlockPos.containing(point)),
                                            LightCoordsUtil.getLightCoords(level, BlockPos.containing(nextPoint))))
                            .renderInto(pose, buffer.getBuffer(RenderTypes.solidMovingBlock()));
                }
                boolean isStartingLow = CEEBlocks.CATENARY_HOLDER.has(startingState) && startingState.getValue(CatenaryHolderBlock.STYLE).isLow();
                boolean isEndingLow = CEEBlocks.CATENARY_HOLDER.has(endingState) && endingState.getValue(CatenaryHolderBlock.STYLE).isLow();
                if (isStartingLow || isEndingLow)
                    continue;

                Vec3 topStart = start.add(0, 1.5, 0);
                Vec3 topEnd = end.add(0, 1.5, 0);

                float distance = (float) topStart.distanceTo(topEnd);


                List<Vec3> upperWirePoints = QuadraticWireHelper.cablePoints(topStart, topEnd, 350f * (0.05f / distance), 4);

                for (int i = 0; i < upperWirePoints.size(); i++) {
                    Vec3 point = upperWirePoints.get(i);

                    if (point.distanceTo(cameraPosition) > CEEConfigs.client().wireRenderDistance.get())
                        continue;

                    Vec3 nextPoint = i == upperWirePoints.size() - 1 ? topEnd : upperWirePoints.get(i + 1);

                    // the reason for catenaries to have variable width, is that when the player is far away, normal-width catenaries look out of place, and they give off a vibe of something solid or smth??

                    float catenaryThickness = (float) Mth.clamp(baseCatenaryWidth / (cameraPosition.distanceTo(point) / 30), 0.4f, baseCatenaryWidth);

                    ModelBuffers.partial(CEEPartialModels.WIRE_SEGMENT, Blocks.ANDESITE.defaultBlockState())
                            .translate(point)
                            .rotateY((float) Math.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                            .rotateX(-(float) Math.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                            .scale(catenaryThickness, catenaryThickness, (float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                            .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LightCoordsUtil.getLightCoords(level, BlockPos.containing(point.add(nextPoint).scale(0.5))) :
                                    maxLightLevel(LightCoordsUtil.getLightCoords(level, BlockPos.containing(point)),
                                            LightCoordsUtil.getLightCoords(level, BlockPos.containing(nextPoint))))
                            .renderInto(pose, buffer.getBuffer(RenderTypes.solidMovingBlock()));
                    if (i == 0)
                        continue;
                    if (i == upperWirePoints.size() - 1)
                        if (point.distanceToSqr(topEnd) < 1.3)
                            continue;
                    Vec3 closest;
                    Vec3 ab = end.subtract(start);
                    Vec3 ap = point.subtract(start);
                    double denom = ab.lengthSqr();
                    if (denom == 0)
                        closest = start;
                    else {
                        double t = ap.dot(ab) / denom;
                        t = Mth.clamp(t, 0, 1);
                        closest = start.add(ab.scale(t));
                    }

                    ModelBuffers.partial(CEEPartialModels.WIRE_SEGMENT, Blocks.ANDESITE.defaultBlockState())
                            .translate(point)
                            .rotateY((float) Math.atan2(closest.x() - point.x(), closest.z() - point.z()))
                            .rotateX(-(float) Math.atan2(closest.y - point.y, Math.hypot(closest.x - point.x, closest.z - point.z)))
                            .scale(catenaryThickness * 0.66f, catenaryThickness * 0.66f, (float) (point.distanceTo(closest) * 2) + 0.02f)
                            .light(BlockPos.containing(point).equals(BlockPos.containing(closest)) ? LightCoordsUtil.getLightCoords(level, BlockPos.containing(point.add(closest).scale(0.5))) :
                                    maxLightLevel(LightCoordsUtil.getLightCoords(level, BlockPos.containing(point)),
                                            LightCoordsUtil.getLightCoords(level, BlockPos.containing(closest))))
                            .rotateZDegrees(45)
                            .renderInto(pose, buffer.getBuffer(RenderTypes.solidMovingBlock()));
                }
            }

        for (Pair<InWorldNodeConnection, WireData> wire : getAllConnections()) {
            InWorldNodeConnection connection = wire.getFirst();
            WireData wireData = wire.getSecond();

            InWorldNode node1 = connection.node1();
            InWorldNode node2 = connection.node2();
            BlockState state1 = level.getBlockState(node1.sourcePos());
            BlockState state2 = level.getBlockState(node2.sourcePos());

            Vec3 pos1 = node1.getPosition(level, partialTick);
            Vec3 pos2 = node2.getPosition(level, partialTick);

            if (pos1 == null) {
                if (DetachedNodeHelper.isDetached(node1))
                    continue;
                pos1 = Vec3.atCenterOf(node1.sourcePos());
            } if (pos2 == null) {
                if (DetachedNodeHelper.isDetached(node2))
                    continue;
                pos2 = Vec3.atCenterOf(node2.sourcePos());
            }

            boolean isBlock1Outer = state1.getBlock() instanceof ElectricalDeviceBlock<?> db &&
                    db.isOuterInsulator(level, node1.sourcePos(), state1, node1.id());
            boolean isBlock2Outer = state2.getBlock() instanceof ElectricalDeviceBlock<?> db &&
                    db.isOuterInsulator(level, node2.sourcePos(), state2, node2.id());
            double distance = pos1.distanceTo(pos2);
            IntSet birdPositions = WireEffects.birds.get(connection);
            if (birdPositions != null) {
                WirePoints wirePoints = QuadraticWireHelper.wirePoints(pos1, pos2, wire.getSecond().getSag(distance));
                for (int birdPos : birdPositions) {
                    if (birdPos >= wirePoints.size())
                        continue;

                    renderBird(wirePoints.get(birdPos), pos1, pos2, pose, buffer);
                }
            }
            for (Pair<Float, WireAttachment> attachment : wireData.attachments()) {
                WireAttachment attachmentType = attachment.getSecond();
                float positionOnWire = attachment.getFirst();

                Vec3 offset = QuadraticWireHelper.posAt(pos1, pos2, positionOnWire, wireData.getSag(distance));
                float elevation = QuadraticWireHelper.pointElevationInDegrees(pos1, pos2, positionOnWire, wireData.getSag(distance));

                if (offset.distanceTo(cameraPosition) > CEEConfigs.client().wireRenderDistance.get())
                    continue;

                pose.pushPose();
                PoseTransformStack msr = TransformStack.of(pose);
                msr.translate(offset);
                double angleY = Math.toDegrees(Math.atan2(pos2.x - pos1.x, pos2.z - pos1.z)) + 90;
                msr.rotateYDegrees((float) angleY);
                msr.rotateXDegrees(180);
                int light = LightCoordsUtil.getLightCoords(level, BlockPos.containing(offset));
                com.george_vi.electroenergetics.client.render.WireAttachmentRenderers.render(pose, buffer, attachmentType, offset, light, elevation);
                pose.popPose();
            }

            if (isBlock1Outer || isBlock2Outer || renderImmediately) {
                if (!connection.isFullyLoaded(level))
                    continue;
                List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, wireData.getSag(distance));
                List<Vec3> renderedPoints;
                if (wireData.wireType().shouldScaleLast())
                    renderedPoints = CEEConfigs.client().wireLOD.get() ?
                            QuadraticWireHelper.cablePoints(pos1, pos2, wireData.getSag(distance), cameraPosition) :
                            QuadraticWireHelper.cablePoints(pos1, pos2, wireData.getSag(distance));
                else
                    renderedPoints = QuadraticWireHelper.cablePointsRaw(pos1, pos2, wireData.getSag(distance));

                if (renderImmediately)
                    renderWire(renderedPoints, pos1, pos2, pose, buffer, camera, wireData.wireType(), level);

                if (points.size() >= 10) {
                    if (isBlock1Outer) {
                        Vec3 nextPoint = points.get(5);
                        if (nextPoint.distanceTo(cameraPosition) > CEEConfigs.client().wireRenderDistance.get())
                            continue;
                        ModelBuffers.partial(CEEPartialModels.INSULATOR, Blocks.ANDESITE.defaultBlockState())
                                .translate(pos1)
                                .rotateY((float) Math.atan2(nextPoint.x() - pos1.x(), nextPoint.z() - pos1.z()))
                                .rotateX(-(float) Math.atan2(nextPoint.y - pos1.y, 
                                        Math.hypot(nextPoint.x - pos1.x, nextPoint.z - pos1.z)))
                                .light(LightCoordsUtil.getLightCoords(level,
                                        BlockPos.containing(pos1.add(nextPoint).scale(0.5))))
                                .renderInto(pose, buffer.getBuffer(RenderTypes.solidMovingBlock()));

                        outerInsulatorJumpers.computeIfAbsent(node1, (k) -> new ArrayList<>())
                                .add(nextPoint);
                    }

                    if (isBlock2Outer) {
                        Vec3 nextPoint = points.get(points.size() - 6);
                        if (nextPoint.distanceTo(cameraPosition) > CEEConfigs.client().wireRenderDistance.get())
                            continue;
                        ModelBuffers.partial(CEEPartialModels.INSULATOR, Blocks.ANDESITE.defaultBlockState())
                                .translate(pos2)
                                .rotateY((float) Math.atan2(nextPoint.x() - pos2.x(), nextPoint.z() - pos2.z()))
                                .rotateX(-(float) Math.atan2(nextPoint.y - pos2.y,
                                        Math.hypot(nextPoint.x - pos2.x, nextPoint.z - pos2.z)))
                                .light(LightCoordsUtil.getLightCoords(level,
                                        BlockPos.containing(pos2.add(nextPoint).scale(0.5))))
                                .renderInto(pose, buffer.getBuffer(RenderTypes.solidMovingBlock()));

                        outerInsulatorJumpers.computeIfAbsent(node2, (k) -> new ArrayList<>())
                                .add(nextPoint);
                    }
                }
            }
        }

        for (Map.Entry<InWorldNode, List<Vec3>> e : outerInsulatorJumpers.entrySet()) {
            List<Vec3> positionsToConnect = e.getValue();

            for (int i = 0; i < positionsToConnect.size() - 1; i++) {
                Vec3 pos1 = positionsToConnect.get(i);
                Vec3 pos2 = positionsToConnect.get(i + 1);

                List<Vec3> points = QuadraticWireHelper.cablePoints(pos1, pos2, 3);

                renderWire(points, pos1, pos2, pose, buffer, camera, CEEWireTypes.IRON.get(), level);
            }
        }

        pose.popPose();


    }

    public static void renderWire(List<Vec3> points, Vec3 pos1, Vec3 pos2, PoseStack pose, RenderCollector buffer,
                                  Camera camera, WireType wireType, BlockAndTintGetter level) {
        Minecraft mc = Minecraft.getInstance();
        double miny = pos1.y;
        for (Vec3 point : points)
            miny = Math.min(miny, point.y());

        if (!camera.getCullFrustum().isVisible(new AABB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z).setMinY(miny)))
            return;

        RenderCollector.Target vb = buffer.getBuffer(wireType.renderType());

        boolean renderEnds = wireType.shouldScaleLast();

        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);
            if (point.distanceTo(mc.gameRenderer.mainCamera().position()) > CEEConfigs.client().wireRenderDistance.get())
                continue;

            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);

            PartialModel endPointModel = (wireType.getEndPointModel() == null ? null : PartialModel.of(wireType.getEndPointModel()));
            double hypot = Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z);

            if (endPointModel != null && i == 0) {
                ModelBuffers.partial(endPointModel, Blocks.ANDESITE.defaultBlockState())
                        .translate(point)
                        .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                        .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, hypot))
                        .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LightCoordsUtil.getLightCoords(level, BlockPos.containing(point.add(nextPoint).scale(0.5))) :
                                maxLightLevel(LightCoordsUtil.getLightCoords(level, BlockPos.containing(point)),
                                        LightCoordsUtil.getLightCoords(level, BlockPos.containing(nextPoint))))
                        .renderInto(pose, vb);
            } else if (endPointModel != null && i == points.size() - 1) {
                point = nextPoint;
                nextPoint = points.get(i);
                ModelBuffers.partial(endPointModel, Blocks.ANDESITE.defaultBlockState())
                        .translate(point)
                        .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                        .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, hypot))
                        .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LightCoordsUtil.getLightCoords(level, BlockPos.containing(point.add(nextPoint).scale(0.5))) :
                                maxLightLevel(LightCoordsUtil.getLightCoords(level, BlockPos.containing(point)),
                                        LightCoordsUtil.getLightCoords(level, BlockPos.containing(nextPoint))))
                        .renderInto(pose, vb);
            }
            if (!renderEnds && (i == 0 || i  == points.size() - 1))
                continue;

            ModelBuffers.partial(PartialModel.of(wireType.getModel()), Blocks.ANDESITE.defaultBlockState())
                    .translate(point)
                    .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                    .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, hypot))
                    .scaleZ((float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                    .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LightCoordsUtil.getLightCoords(level, BlockPos.containing(point.add(nextPoint).scale(0.5))) :
                            maxLightLevel(LightCoordsUtil.getLightCoords(level, BlockPos.containing(point)),
                                    LightCoordsUtil.getLightCoords(level, BlockPos.containing(nextPoint))))
                    .renderInto(pose, vb);
        }
    }

    public static void forceRenderWire(List<Vec3> points, Vec3 pos1, Vec3 pos2, PoseStack pose, RenderCollector buffer, WireType wireType, BlockAndTintGetter level) {

        double miny = pos1.y;
        for (Vec3 point : points)
            miny = Math.min(miny, point.y());

        boolean renderEnds = wireType.shouldScaleLast();
        RenderCollector.Target vb = buffer.getBuffer(wireType.renderType());

        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);

            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);

            PartialModel endPointModel = (wireType.getEndPointModel() == null ? null : PartialModel.of(wireType.getEndPointModel()));
            double hypot = Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z);
            if (endPointModel != null && i == 0) {
                ModelBuffers.partial(endPointModel, Blocks.ANDESITE.defaultBlockState())
                        .translate(point)
                        .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                        .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, hypot))
                        .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LightCoordsUtil.getLightCoords(level, BlockPos.containing(point.add(nextPoint).scale(0.5))) :
                                maxLightLevel(LightCoordsUtil.getLightCoords(level, BlockPos.containing(point)),
                                        LightCoordsUtil.getLightCoords(level, BlockPos.containing(nextPoint))))
                        .renderInto(pose, vb);
            } else if (endPointModel != null && i == points.size() - 1) {
                point = nextPoint;
                nextPoint = points.get(i);
                ModelBuffers.partial(endPointModel, Blocks.ANDESITE.defaultBlockState())
                        .translate(point)
                        .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                        .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, hypot))
                        .light(BlockPos.containing(point).equals(BlockPos.containing(nextPoint)) ? LightCoordsUtil.getLightCoords(level, BlockPos.containing(point.add(nextPoint).scale(0.5))) :
                                maxLightLevel(LightCoordsUtil.getLightCoords(level, BlockPos.containing(point)),
                                        LightCoordsUtil.getLightCoords(level, BlockPos.containing(nextPoint))))
                        .renderInto(pose, vb);
            }
            if (!renderEnds && (i == 0 || i  == points.size() - 1))
                continue;

            BlockPos pointBlockPos = BlockPos.containing(point);
            BlockPos nextPointBlockPos = BlockPos.containing(nextPoint);
            ModelBuffers.partial(PartialModel.of(wireType.getModel()), Blocks.ANDESITE.defaultBlockState())
                    .translate(point)
                    .rotateY((float) Mth.atan2(nextPoint.x() - point.x(), nextPoint.z() - point.z()))
                    .rotateX(-(float) Mth.atan2(nextPoint.y - point.y, Math.hypot(nextPoint.x - point.x, nextPoint.z - point.z)))
                    .scaleZ((float) (point.distanceTo(nextPoint) * 2) + 0.02f)
                    .light(pointBlockPos.equals(nextPointBlockPos) ?
                            LightCoordsUtil.getLightCoords(level,
                                    BlockPos.containing(point.add(nextPoint).scale(0.5))) :
                            maxLightLevel(LightCoordsUtil.getLightCoords(level, pointBlockPos),
                                    LightCoordsUtil.getLightCoords(level, nextPointBlockPos)))
                    .renderInto(pose, vb);
        }
    }
    public static void renderBird(Vec3 pos, Vec3 wirePos1, Vec3 wirePos2, PoseStack pose, RenderCollector buffer) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        ModelBuffers.partial(CEEPartialModels.BIRB, Blocks.ANDESITE.defaultBlockState())
                .translate(pos)
                .rotateY((float) Mth.atan2(wirePos2.x() - wirePos1.x(), wirePos2.z() - wirePos1.z()))
                .light(LightCoordsUtil.getLightCoords(mc.level, BlockPos.containing(pos)))
                .renderInto(pose, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
    }

    public static void removeConnections(InWorldNodeConnection toRemove) {
        WIRE_CONNECTIONS.removeIf(w -> w.getFirst().equals(toRemove));
        WireEffects.birds.remove(toRemove);
        WireEffect we = WIRE_EFFECTS.remove(toRemove);
        if (we != null)
            VisualizationHelper.queueRemove(we);
    }

    public static List<Pair<InWorldNodeConnection, WireData>> getAllConnections() {
       return List.copyOf(WIRE_CONNECTIONS);
    }

    public static WireData getConnectionData(InWorldNodeConnection connection) {
        for (Pair<InWorldNodeConnection, WireData> wire : getAllConnections()) {
            if (wire.getFirst().equals(connection))
                return wire.getSecond();
        }
        return null;
    }

    public static void addConnection(InWorldNodeConnection newConnection, WireData data) {
        for (int i = 0; i < WIRE_CONNECTIONS.size(); i++) {
            if (WIRE_CONNECTIONS.get(i).getFirst().equals(newConnection)) {
                WIRE_CONNECTIONS.remove(i);
                break;
            }
        }
        WIRE_CONNECTIONS.add(Pair.of(newConnection, data));
        if (CEEConfigs.client().disableFlywheelWireRendering.get())
            return;

        WireEffect we = new WireEffect(Minecraft.getInstance().level, newConnection, data.wireType(), data);
        WireEffect owe = WIRE_EFFECTS.put(newConnection, we);
        if (owe != null)
            VisualizationHelper.queueRemove(owe);
        VisualizationHelper.queueAdd(we);
        WIRE_EFFECTS.put(newConnection, we);
    }

    public static void clearAllWireConnections() {
        WIRE_CONNECTIONS.clear();
        WireEffects.birds.clear();

        for (WireEffect we : WIRE_EFFECTS.values())
            VisualizationHelper.queueRemove(we);
        WIRE_EFFECTS.clear();
    }

    public static void addCatenary(CatenaryConnection connection) {
        if (!CATENARY.contains(connection.swap())) {
            if (!CATENARY.contains(connection)) {
                CATENARY.add(connection);
                if (CEEConfigs.client().disableFlywheelWireRendering.get())
                    return;
                WireEffect we = new WireEffect(Minecraft.getInstance().level, connection, CEEWireTypes.STANDARD.get(),
                        new WireData(CEEWireTypes.STANDARD.get(), 0, Collections.emptyList(), 0));
                VisualizationHelper.queueAdd(we);
                CATENARY_EFFECTS.put(connection, we);
            }
        }
    }

    public static void removeCatenary(CatenaryConnection connection) {
        CATENARY.removeIf(c -> c.equals(connection.swap()));
        CATENARY.removeIf(c -> c.equals(connection));
        WireEffect we = CATENARY_EFFECTS.get(connection);
        WireEffect weSwapped = CATENARY_EFFECTS.get(connection.swap());
        if (we != null)
            VisualizationHelper.queueRemove(we);
        if (weSwapped != null)
            VisualizationHelper.queueRemove(weSwapped);
    }

    public static void removeVoltageData(InWorldNode node) {
        NodeVoltageHolder.NODE_VOLTAGES.remove(node);
    }

    public static void clearAllCatenaryConnections() {
        for (WireEffect we : CATENARY_EFFECTS.values())
            VisualizationHelper.queueRemove(we);

        CATENARY_EFFECTS.clear();
        CATENARY.clear();
    }

    public static void recreateVisuals() {
        for (WireEffect we : CATENARY_EFFECTS.values())
            VisualizationHelper.queueRemove(we);
        for (WireEffect we : WIRE_EFFECTS.values())
            VisualizationHelper.queueRemove(we);
        CATENARY_EFFECTS.clear();
        WIRE_EFFECTS.clear();
        if (CEEConfigs.client().disableFlywheelWireRendering.get())
            return;

        for (CatenaryConnection connection : CATENARY) {
            WireEffect we = new WireEffect(Minecraft.getInstance().level, connection, CEEWireTypes.STANDARD.get(),
                    new WireData(CEEWireTypes.STANDARD.get(), 0, Collections.emptyList(), 0));
            VisualizationHelper.queueAdd(we);
            CATENARY_EFFECTS.put(connection, we);
        }

        for (Pair<InWorldNodeConnection, WireData> connection : WIRE_CONNECTIONS) {
            WireEffect we = new WireEffect(Minecraft.getInstance().level, connection.getFirst(), connection.getSecond().wireType(),
                    connection.getSecond());
            VisualizationHelper.queueAdd(we);
            WIRE_EFFECTS.put(connection.getFirst(), we);
        }
    }

    public static @Nullable String getNodeLabel(InWorldNode node) {
        ClientNodeData nodeData = getNodeData(node);
        if (nodeData != null)
            return nodeData.label;
        return null;
    }

    public static void setNodeData(InWorldNode node, @Nullable String label, @Nullable Vec3 detachedPosition) {
        ClientNodeData nodeData = NODE_DATA.computeIfAbsent(node, n -> new ClientNodeData(node));
        nodeData.label = label;
        nodeData.targetPosition = detachedPosition;
    }

    public static Map<InWorldNode, ClientNodeData> getNodeData() {
        return NODE_DATA;
    }

    public static ClientNodeData getNodeData(InWorldNode node) {
        return NODE_DATA.get(node);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static ClientNodeData removeNodeData(InWorldNode node) {
        return NODE_DATA.remove(node);
    }

    public static void clearAllNodeData() {
        NODE_DATA.clear();
    }

    public static int maxLightLevel(int lightColo1, int lightColor2) {
        return LightCoordsUtil.pack(Math.max(LightCoordsUtil.block(lightColo1), LightCoordsUtil.block(lightColor2)), Math.max(LightCoordsUtil.sky(lightColo1), LightCoordsUtil.sky(lightColor2)));
    }
}
