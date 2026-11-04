package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.client.ClientNodeData;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.foundation.BoundingBoxUtils;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.zurrtum.create.catnip.data.Pair;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import java.util.*;

@Environment(EnvType.CLIENT)
public final class SchematicClientData {
    private SchematicClientData() {}
    public static void capture(Level level, BlockPos pos, Vec3i size,
        List<Pair<InWorldNodeConnection, WireData>> wireConnections,
        List<CatenaryConnection> catenaryConnections, Map<InWorldNode, String> nodeLabels) {
            Set<InWorldNodeConnection> visitedConnections = new HashSet<>();

            for (CatenaryConnection connection : WireRenderer.CATENARY) {
                BlockPos pos1 = connection.pos1();
                BlockPos pos2 = connection.pos2();
                if (BoundingBoxUtils.isIn(pos1, pos, size) && BoundingBoxUtils.isIn(pos2, pos, size))
                    catenaryConnections.add(new CatenaryConnection(pos1.subtract(pos), pos2.subtract(pos)));
            }

            for (Map.Entry<InWorldNode, ClientNodeData> e : WireRenderer.getNodeData().entrySet()) {
                InWorldNode node = e.getKey();
                ClientNodeData nodeData = e.getValue();
                if (nodeData.label == null)
                    continue;

                BlockPos nodePos = node.sourcePos();
                if (!BoundingBoxUtils.isIn(nodePos, pos, size))
                    continue;

                nodeLabels.put(new InWorldNode(node.id(), node.sourcePos().subtract(pos)), nodeData.label);
            }

            for (Pair<InWorldNodeConnection, WireData> wireDataPair : WireRenderer.getAllConnections()) {
                InWorldNodeConnection connection = wireDataPair.getFirst();
                WireData wireData = wireDataPair.getSecond();
                if (visitedConnections.contains(connection))
                    continue;
                visitedConnections.add(connection);
                InWorldNode node1 = connection.node1();
                InWorldNode node2 = connection.node2();

                if (BoundingBoxUtils.isIn(node1.sourcePos(), pos, size) &&
                        BoundingBoxUtils.isIn(node2.sourcePos(), pos, size)) {

                    wireConnections.add(Pair.of(new InWorldNodeConnection(new InWorldNode(node1.id(), node1.sourcePos().subtract(pos)),
                            new InWorldNode(node2.id(), node2.sourcePos().subtract(pos))), wireData));
                }
            }
    }
}
