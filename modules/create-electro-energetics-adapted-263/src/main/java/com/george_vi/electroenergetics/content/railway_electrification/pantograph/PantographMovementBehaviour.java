package com.george_vi.electroenergetics.content.railway_electrification.pantograph;

import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryConnection;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class PantographMovementBehaviour extends MovementBehaviour {

    @Override
    public void tick(MovementContext context) {
        float targetExtensionState = context.blockEntityData.getFloatOr("ExtensionState", 0f);
        float currentExtensionState = context.data.getFloatOr("CurrentExtensionState", 0f);
        float prevExtensionState = currentExtensionState;
        currentExtensionState = currentExtensionState < targetExtensionState ? Math.min(targetExtensionState, currentExtensionState + 0.05f) : Math.max(targetExtensionState, currentExtensionState - 0.05f);

        boolean isDouble = context.state.getValue(PantographBlock.DOUBLE);

        if (!(context.contraption.entity instanceof CarriageContraptionEntity e))
            return;

        boolean prevDisabled = context.data.getBooleanOr("Disabled", false);
        if (!context.data.contains("Forward")) {
            TrainPantographEntry pantographState = ((IPantographList) e.getCarriage()).getPantographState(context.localPos);
            if (pantographState != null)
                context.data.putBoolean("Forward", pantographState.facingForward);
        }
        if (context.disabled) {
            if (!prevDisabled)
                ((IPantographList) e.getCarriage()).changePantographState(context.localPos, false);
            targetExtensionState = 0f;
        } else {
            if (prevDisabled) {
                ((IPantographList) e.getCarriage()).changePantographState(context.localPos, true);
                targetExtensionState = 0.75f;
            }
        }

        if (context.world.isClientSide() && currentExtensionState == targetExtensionState && prevExtensionState != currentExtensionState)
            context.world.playLocalSound(context.position.x, context.position.y, context.position.z, SoundEvents.CHAIN_HIT, SoundSource.NEUTRAL, 0.1f, 1f, false);

        // Look for the connection point, and try to adjust the pantographs extension state to fit that point

        if (context.world.isClientSide() && !context.disabled) {
            double pantographX = context.state.getValue(PantographBlock.FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 0.5 : -0.5;
            if (!isDouble)
                pantographX /= 2;
            float halfPantoReach = 1.625f;
            Direction.Axis axis = context.state.getValue(FACING).getAxis();
            Vec3 pantographPos = new Vec3(axis == Direction.Axis.X ? pantographX : 0, halfPantoReach, axis == Direction.Axis.Z ? pantographX : 0);
            pantographPos = context.rotation.apply(pantographPos);
            pantographPos = pantographPos.add(context.position);

            Vec3 connectionPoint = null;
            if (CEEConfigs.client().debugPantographRange.get())
                context.world.addParticle(ParticleTypes.SCRAPE, pantographPos.x, pantographPos.y, pantographPos.z, 0, 0, 0);
            for (CatenaryConnection connection : WireRenderer.CATENARY) {
                Vec3 start = net.minecraft.world.phys.Vec3.atBottomCenterOf(connection.pos1());
                Vec3 end = net.minecraft.world.phys.Vec3.atBottomCenterOf(connection.pos2());
                Vec3 cp = checkCatenary(start, end, pantographPos, context, halfPantoReach);
                if (cp != null) {
                    if (connectionPoint == null || connectionPoint.y > cp.y)
                        connectionPoint = cp;
                }
            }

            for (Pair<InWorldNodeConnection, WireData> connection : WireRenderer.WIRE_CONNECTIONS) {
                if (connection.getSecond().wireType().getSag() != 0)
                    continue;
                Vec3 start = connection.getFirst().node1().getPosition(context.world);
                Vec3 end = connection.getFirst().node2().getPosition(context.world);
                if (start == null || end == null)
                    continue;
                float halfThickness = connection.getSecond().wireType().getThickness() * 0.5f;
                Vec3 cp = checkCatenary(start, end, pantographPos, context, halfPantoReach);
                if (cp != null) {
                    if (connectionPoint == null || connectionPoint.y > cp.y)
                        connectionPoint = cp.add(0, -halfThickness + 0.015f, 0);
                }
            }

            if (connectionPoint != null) {
                float lo = 0;
                float hi = 2.7f;
                for (int i = 0; i < 20; i++) {
                    float m1 = lo + (hi - lo) / 3;
                    float m2 = hi - (hi - lo) / 3;
                    if (Math.abs(getConnectorPos(m1, context).y - connectionPoint.y) < Math.abs(getConnectorPos(m2, context).y - connectionPoint.y))
                        hi = m2;
                    else
                        lo = m1;
                }
//                context.world.addParticle(ParticleTypes.SCULK_CHARGE_POP, connectionPoint.x, connectionPoint.y, connectionPoint.z, 0, 0, 0);
                float newExtensionState = (lo + hi) / 2;
                if (targetExtensionState == currentExtensionState && targetExtensionState != 0)
                    currentExtensionState = newExtensionState;

                targetExtensionState = newExtensionState;
            } else {
                targetExtensionState = 0f;
            }

            if (CEEConfigs.client().debugPantographRange.get() && (context.world.getGameTime() % 3) == 0) {
                Vec3 cp = getConnectorPos(currentExtensionState, context);
                context.world.addParticle(ParticleTypes.ELECTRIC_SPARK, cp.x, cp.y, cp.z, 0, 0, 0);

                Vec3 pp = new Vec3(axis == Direction.Axis.X ? pantographX : 0, halfPantoReach, axis == Direction.Axis.Z ? pantographX : 0);
                for (float y = -halfPantoReach; y <= halfPantoReach; y += 2 / 16f) {
                    float v = (y + halfPantoReach) * 0.1f + 0.125f;
                    v = v - (v % (1 / 16f));
                    for (float x = -v; x <= v; x += 2 / 16f) {
                        for (float z = -1.5f; z <= 1.5f; z += 2 / 16f) {
                            boolean edgeX = x == -v || x == v;
                            boolean edgeY = y == -halfPantoReach || y == halfPantoReach;
                            boolean edgeZ = z == -1.5f || z == 1.5f;
                            if ((edgeX && edgeY && !edgeZ) || (edgeX && !edgeY && edgeZ) || (!edgeX && edgeY && edgeZ)) {
                                Vec3 pos = context.rotation.apply(pp.add(axis == Direction.Axis.X ? x : z, y - 0.25f, axis == Direction.Axis.X ? z : x)).add(context.position);
                                context.world.addParticle(ParticleTypes.DOLPHIN, pos.x, pos.y, pos.z, 0, 0, 0);
                            }

                        }
                    }
                }
            }
        }

        context.data.putBoolean("Disabled", context.disabled);

        context.data.putFloat("CurrentExtensionState", currentExtensionState);
        context.blockEntityData.putFloat("ExtensionState", targetExtensionState);
        context.data.putFloat("PrevExtensionState", prevExtensionState);
    }

    Vec3 getConnectorPos(float extensionState, MovementContext context) {
        Direction.Axis axis = context.state.getValue(PantographBlock.FACING).getAxis();
        if (context.state.getValue(PantographBlock.DOUBLE)) {
            float lowerArmRadians = (-90 + extensionState * 27) * Mth.DEG_TO_RAD;
            double armHingePosY = Mth.cos(lowerArmRadians) * 1.5;
            double armHingePosX = Mth.sin(lowerArmRadians) * 1.5;

            float b = (float) (-0.4f - Math.abs(armHingePosX));
            float a = Mth.sqrt(-b * b + 2f * 2f);

            double pantographX = context.state.getValue(PantographBlock.FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 0.5 : -0.5;
            Vec3 connectorPlatePos = new Vec3(axis == Direction.Axis.X ? pantographX : 0, 0.1875 + a + armHingePosY, axis == Direction.Axis.Z ? pantographX : 0);
            connectorPlatePos = context.rotation.apply(connectorPlatePos);
            connectorPlatePos = connectorPlatePos.add(context.position);
            return connectorPlatePos;
        }
        float lowerArmRadians = (-75 + extensionState * 30) * Mth.DEG_TO_RAD;

        double armHingePosY = Mth.cos(lowerArmRadians) * 1.875;
        double armHingePosX = Mth.sin(lowerArmRadians) * 1.875;

        float upperArmRadians = (89 - extensionState * 50) * Mth.DEG_TO_RAD;
        double pantographX = context.state.getValue(PantographBlock.FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 0.25 : -0.25;
        Vec3 connectorPlatePos = new Vec3((axis == Direction.Axis.X ? pantographX : 0), armHingePosY, armHingePosX + (axis == Direction.Axis.Z ? pantographX : 0)).add(0, Mth.cos(upperArmRadians) * 1.9, Mth.sin(upperArmRadians) * 1.9);
        connectorPlatePos = context.rotation.apply(connectorPlatePos);
        connectorPlatePos = connectorPlatePos.add(context.position);
        return connectorPlatePos;
    }

    @Override
    public boolean mustTickWhileDisabled() {
        return true;
    }

    @Override
    public void onDisabledByControls(MovementContext context) {
        super.onDisabledByControls(context);
    }

    Vec3 checkCatenary(Vec3 start, Vec3 end, Vec3 pantographPos, MovementContext context, float halfPantoReach) {

        double t = 0;

        Vec3 ab = end.subtract(start);
        Vec3 ap = pantographPos.subtract(start);
        double denom = ab.lengthSqr();
        if (denom != 0)
            t = ap.dot(ab) / denom;
        if (t < -0.01 || t > 1.01)
            return null;

        Vec3 closest = VecHelper.lerp((float) t, start, end);
//        Vec3 closest = Minecraft.getInstance().player.position();
//        context.world.addParticle(ParticleTypes.SCULK_CHARGE_POP, closest.x, closest.y, closest.z, 0, 0, 0);
//        context.world.addParticle(ParticleTypes.SCULK_CHARGE_POP, pantographPos.x, pantographPos.y, pantographPos.z, 0, 0, 0);

        Vec3 distance = pantographPos.subtract(closest);
        distance = VecHelper.rotate(distance, -context.contraption.entity.getViewYRot(0), Direction.Axis.Y);
        float xTol = (float) ((distance.y + halfPantoReach) * 0.2f + 0.125f);
        if (Math.abs(distance.z) < 1.5 && Math.abs(distance.x) < xTol && Math.abs(distance.y) < halfPantoReach)
            return closest;

        return null;
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }
}
