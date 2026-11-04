package net.fxnt.fxntstorage.backpack.upgrade.workshop;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class WorkshopMachineRenderer {
    private static final int DEPLOYER_SCALE = 16;
    private static final int PRESS_SCALE = 16;

    private static final int SHADOW_W = 52;
    private static final int SHADOW_H = 11;
    private static final float DEPLOYER_SHADOW_SCALE = DEPLOYER_SCALE / 20f;
    private static final float PRESS_SHADOW_SCALE = PRESS_SCALE / 24f;
    private static final int DEPLOYER_SHADOW_DX = 0;
    private static final int DEPLOYER_SHADOW_DY = 20;
    private static final int PRESS_SHADOW_DX = 16;
    private static final int PRESS_SHADOW_DY = 40;

    private float animTime = 0f;
    private float lastRenderTime = Float.NaN;

    private float displayOffset = 0f;
    private int lastSync = Integer.MIN_VALUE;
    private float prevOffset = 0f;
    private float currOffset = 0f;
    private float baseRenderTime = Float.NaN;

    public void draw(GuiGraphicsExtractor graphics, int anchorX, int anchorY, boolean deployer, int syncValue) {
        boolean processing = syncValue > 0;
        advanceClock(processing);
        updateOffset(syncValue, processing);

        if (deployer) {
            drawDeployer(graphics, anchorX, anchorY);
        } else {
            drawPress(graphics, anchorX, anchorY);
        }
    }

    private void updateOffset(int syncValue, boolean processing) {
        if (!processing) {
            lastSync = syncValue;
            baseRenderTime = Float.NaN;
            prevOffset = currOffset = displayOffset = 0f;
            return;
        }

        float now = AnimationTickHolder.getRenderTime();
        if (syncValue != lastSync || Float.isNaN(baseRenderTime)) {
            lastSync = syncValue;
            prevOffset = currOffset;
            currOffset = (syncValue - 1) / 1000f;
            baseRenderTime = now;
        }

        float elapsed = Math.clamp(now - baseRenderTime, 0f, 1f);
        displayOffset = Mth.lerp(elapsed, prevOffset, currOffset);
    }

    private void advanceClock(boolean processing) {
        float now = AnimationTickHolder.getRenderTime();
        if (processing && !Float.isNaN(lastRenderTime)) {
            float delta = now - lastRenderTime;
            // Guard against large jumps (pauses, screen reopen) so the animation stays smooth
            if (delta > 0f && delta < 100f) {
                animTime += delta;
            }
        }
        lastRenderTime = now;
    }

    private float shaftAngle() {
        float degPerTick = WorkshopUpgrade.kineticSpeed() * 3f / 10f;
        return (animTime * degPerTick) % 360f;
    }

    private void drawDeployer(GuiGraphicsExtractor graphics, int anchorX, int anchorY) {
        drawShadow(graphics, anchorX + 13 + DEPLOYER_SHADOW_DX, anchorY + 30 + DEPLOYER_SHADOW_DY, DEPLOYER_SHADOW_SCALE);

        org.joml.Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(anchorX + 3, anchorY + 18);
        net.createmod.catnip.api.client.gui.element.GuiGameElement.beginModelBatch(-15.5f,22.5f,0);
        int scale = DEPLOYER_SCALE;

        defaultBlockElement(shaft(Direction.Axis.Z))
                .rotateBlock(0, 0, shaftAngle())
                .scale(scale)
                .render(graphics,0,0);

        defaultBlockElement(AllBlocks.DEPLOYER.getDefaultState()
                        .setValue(DeployerBlock.FACING, Direction.DOWN)
                        .setValue(DeployerBlock.AXIS_ALONG_FIRST_COORDINATE, false))
                .scale(scale)
                .render(graphics,0,0);

        pose.pushMatrix();
        defaultBlockElement(AllPartialModels.DEPLOYER_POLE)
                .atLocal(0,displayOffset * 17 / scale,0)
                .rotateBlock(90, 0, 0)
                .scale(scale)
                .render(graphics,0,0);
        defaultBlockElement(AllPartialModels.DEPLOYER_HAND_HOLDING)
                .atLocal(0,displayOffset * 17 / scale,0)
                .rotateBlock(90, 0, 0)
                .scale(scale)
                .render(graphics,0,0);
        pose.popMatrix();

        defaultBlockElement(AllBlocks.DEPOT.getDefaultState())
                .atLocal(0, 2, 0)
                .scale(scale)
                .render(graphics,0,0);

        net.createmod.catnip.api.client.gui.element.GuiGameElement.endModelBatch(graphics);
        pose.popMatrix();
    }

    private void drawPress(GuiGraphicsExtractor graphics, int anchorX, int anchorY) {
        drawShadow(graphics, anchorX + PRESS_SHADOW_DX, anchorY + PRESS_SHADOW_DY, PRESS_SHADOW_SCALE);

        org.joml.Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(anchorX + 5, anchorY + 24);
        net.createmod.catnip.api.client.gui.element.GuiGameElement.beginModelBatch(-15.5f,22.5f,0);
        int scale = PRESS_SCALE;

        defaultBlockElement(shaft(Direction.Axis.Z))
                .rotateBlock(0, 0, shaftAngle())
                .scale(scale)
                .render(graphics,0,0);

        defaultBlockElement(AllBlocks.MECHANICAL_PRESS.getDefaultState())
                .scale(scale)
                .render(graphics,0,0);

        // displayOffset is 0 (up) .. 1 (fully down) - the head model presses down by that amount
        defaultBlockElement(AllPartialModels.MECHANICAL_PRESS_HEAD)
                .atLocal(0, displayOffset, 0)
                .scale(scale)
                .render(graphics,0,0);

        net.createmod.catnip.api.client.gui.element.GuiGameElement.endModelBatch(graphics);
        pose.popMatrix();
    }

    private void drawShadow(GuiGraphicsExtractor graphics, float centreX, float centreY, float scale) {
        org.joml.Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(centreX, centreY);
        pose.scale(scale, scale);
        AllGuiTextures.JEI_SHADOW.render(graphics, -SHADOW_W / 2, -SHADOW_H / 2);
        pose.popMatrix();
    }

    private static final net.createmod.catnip.api.client.gui.ILightingSettings MACHINE_LIGHTING =
            com.simibubi.create.foundation.gui.CustomLightingSettings.builder()
                    .firstLightRotation(12.5f, -45.0f).secondLightRotation(-20.0f, -50.0f).build();

    private static net.createmod.catnip.api.client.gui.element.GuiGameElement.GuiRenderBuilder defaultBlockElement(BlockState state) {
        return net.createmod.catnip.api.client.gui.element.GuiGameElement.of(state).lighting(MACHINE_LIGHTING);
    }

    private static net.createmod.catnip.api.client.gui.element.GuiGameElement.GuiRenderBuilder defaultBlockElement(dev.engine_room.flywheel.lib.model.baked.PartialModel partial) {
        var key = partial == AllPartialModels.DEPLOYER_POLE
                ? com.simibubi.create.foundation.model.CreateStandaloneModels.DEPLOYER_POLE
                : partial == AllPartialModels.DEPLOYER_HAND_HOLDING
                ? com.simibubi.create.foundation.model.CreateStandaloneModels.DEPLOYER_HAND_HOLDING
                : partial == AllPartialModels.MECHANICAL_PRESS_HEAD
                ? com.simibubi.create.foundation.model.CreateStandaloneModels.MECHANICAL_PRESS_HEAD : null;
        if (key == null) throw new IllegalArgumentException("Unregistered workshop machine partial: " + partial);
        return net.createmod.catnip.api.client.gui.element.GuiGameElement.of(
                net.minecraft.client.Minecraft.getInstance().getModelManager().getStandaloneModel(key)).lighting(MACHINE_LIGHTING);
    }

    private static BlockState shaft(Direction.Axis axis) {
        return AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, axis);
    }
}
