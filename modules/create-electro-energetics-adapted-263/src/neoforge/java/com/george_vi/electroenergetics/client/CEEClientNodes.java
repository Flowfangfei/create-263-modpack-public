package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionHandler;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Rendering cache and local-player picking stay outside the network node codec's class. */
public final class CEEClientNodes {
    private CEEClientNodes() {}

    @Nullable
    public static Vec3 getPosition(InWorldNode node) {
        ClientNodeData data = WireRenderer.getNodeData(node);
        return data == null ? null : data.position;
    }

    @Nullable
    public static Vec3 getPositionOrFallback(InWorldNode node, @Nullable Vec3 fallback) {
        ClientNodeData data = WireRenderer.getNodeData(node);
        return data == null ? fallback : data.position;
    }

    @Nullable
    public static Vec3 getPosition(InWorldNode node, float partialTicks) {
        ClientNodeData data = WireRenderer.getNodeData(node);
        return data == null ? null : data.getPos(partialTicks);
    }

    public static InWorldNode getHitNode(BlockHitResult hitResult, Level level) {
        Minecraft mc = Minecraft.getInstance();
        assert mc.player != null;
        BlockPos hoveredPos = hitResult.getBlockPos();
        Vec3 hoveredLocation = hitResult.getLocation();

        double range = mc.player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;

        Vec3 from = WireInteractionHandler.getTraceOrigin(mc.player);
        Vec3 to = WireInteractionHandler.getTraceTarget(mc.player, range, from);

        // Detached Node Interactions:
        InWorldNode detachedHoveredNode = null;
        for (ClientNodeData nodeData : WireRenderer.NODE_DATA.values()) {
            Vec3 nodePos = nodeData.position;
            if (nodePos == null)
                continue;

            AABB bb = AABB.ofSize(nodePos, 4 / 16f, 4 / 16f, 4 / 16f);
            if (bb.clip(from, to).isEmpty())
                continue;

            detachedHoveredNode = nodeData.node;
        }

        //

        InWorldNode hoveredNode = InWorldNode.closestNode(level, hoveredLocation, 1.5f, hitResult.getBlockPos());
        if (hoveredNode == null)
            hoveredNode = InWorldNode.closestNode(level, hoveredPos, level.getBlockState(hoveredPos), 1.5f, hoveredLocation);

        if (hoveredNode == null && detachedHoveredNode == null)
            return null;
        if (hoveredNode == null)
            return detachedHoveredNode;
        if (detachedHoveredNode == null)
            return hoveredNode;

        Vec3 position = hoveredNode.getPosition(level);
        Vec3 detachedPosition = detachedHoveredNode.getPosition(level);
        if (position == null || detachedPosition == null)
            return null;

        Vec3 origin = WireInteractionHandler.getTraceOrigin(mc.player);

        double hoveredDistance = position.distanceTo(origin);
        double detachedDistance = detachedPosition.distanceTo(origin);

        return hoveredDistance < detachedDistance ? hoveredNode : detachedHoveredNode;
    }
}
