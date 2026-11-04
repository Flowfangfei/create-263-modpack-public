package com.george_vi.electroenergetics.content.clamp_meter;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.LightCoordsUtil;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickClientHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.catnip.math.VecHelper;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class ClampMeterRenderer {
    protected static final PartialModel METER = CEEPartialModels.CLAMP_METER;


    public static void renderFirstPerson(ClientLevel level, PoseStack pose, RenderCollector buffer, Camera camera) {

        Minecraft mc = Minecraft.getInstance();
        if (camera.isDetached() || mc.player == null)
            return;

        LocalPlayer player = mc.player;

        ItemStack mainStackItem = player.getMainHandItem();
        if (mainStackItem.getItem() != CEEItems.CLAMP_METER.get() ||
                player.getUseItem() != mainStackItem ||
                LinemansStickClientHandler.ticksSinceChange <= 0)
            return;

        HumanoidArm mainArm = player.getMainArm();

        Vec3 playerPos = player.getPosition(AnimationTickHolder.getPartialTicks());

        Vec3 offset = new Vec3(mainArm == HumanoidArm.RIGHT ? -0.3 : 0.3, 0.6, 0);
        offset = VecHelper.rotate(offset, -player.getViewYRot(AnimationTickHolder.getPartialTicks()), Direction.Axis.Y);
        Vec3 base = playerPos.add(offset);

        pose.pushPose();
        pose.translate(
                -camera.position().x() + base.x(),
                -camera.position().y() + base.y(),
                -camera.position().z() + base.z());

        Vec3 hitPos = LinemansStickClientHandler.linemansStickTarget.getHitPos(AnimationTickHolder.getPartialTicks());

        Vec3 diff = hitPos.subtract(base);

        int light = LightCoordsUtil.getLightCoords(level, player.blockPosition());

        double poleLength = diff.length();
        double hypot = Math.hypot(diff.x(), diff.z());
        ModelBuffers.partial(METER, Blocks.ANDESITE.defaultBlockState())
                .rotateY((float) Mth.atan2(diff.x(), diff.z()))
                .rotateX(-(float) Mth.atan2(diff.y(), hypot) + Mth.HALF_PI)
                .light(light)
                .translate(-0.5, poleLength - 0.75f, -0.5)
                .renderInto(pose, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

        pose.popPose();
    }
}
