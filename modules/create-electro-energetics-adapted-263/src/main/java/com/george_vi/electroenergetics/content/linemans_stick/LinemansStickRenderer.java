package com.george_vi.electroenergetics.content.linemans_stick;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.LightCoordsUtil;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.content.clamp_meter.ClampMeterItem;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.catnip.math.VecHelper;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class LinemansStickRenderer {

    protected static final PartialModel HEAD = CEEPartialModels.LINEMANS_STICK_HEAD;
    protected static final PartialModel BASE = CEEPartialModels.LINEMANS_STICK_BASE;
    protected static final PartialModel TELESCOPE = CEEPartialModels.LINEMANS_STICK_TELESCOPE;


    public static void setupAnimAfter(HumanoidRenderState player, HumanoidModel<?> humanoidModel) {
        ItemStack stackInHand = player.getMainHandItemStack();
        if (stackInHand.getItem() != CEEItems.LINEMANS_STICK.get())
            return;
        HumanoidArm mainArm = player.mainArm;
        ModelPart armModel = mainArm == HumanoidArm.RIGHT ? humanoidModel.rightArm : humanoidModel.leftArm;

        armModel.resetPose();
        armModel.xRot = humanoidModel.head.xRot - 0.23f;
        armModel.yRot = humanoidModel.head.yRot - 0.08f;
    }
    public static void renderFirstPerson(ClientLevel level, PoseStack pose, RenderCollector buffer, Camera camera) {

        Minecraft mc = Minecraft.getInstance();
        if (camera.isDetached() || mc.player == null)
            return;


        LocalPlayer player = mc.player;

        ItemStack mainStackItem = player.getMainHandItem();
        if (mainStackItem.getItem() != CEEItems.LINEMANS_STICK.get())
            return;

        HumanoidArm mainArm = player.getMainArm();

        ItemStack offHandItem = player.getOffhandItem();

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
        ModelBuffers.partial(HEAD, Blocks.ANDESITE.defaultBlockState())
                .rotateY((float) Mth.atan2(diff.x(), diff.z()))
                .rotateX(-(float) Mth.atan2(diff.y(), hypot) + Mth.HALF_PI)
                .light(light)
                .translate(-0.5, poleLength - 1.3f, -0.5)
                .renderInto(pose, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

        ModelBuffers.partial(TELESCOPE, Blocks.ANDESITE.defaultBlockState())
                .rotateY((float) Mth.atan2(diff.x(), diff.z()))
                .rotateX(-(float) Mth.atan2(diff.y(), hypot) + Mth.HALF_PI)
                .light(light)
                .translate(-0.5, Mth.clamp((poleLength - 1.3f) / 2, poleLength - 3f, poleLength - 1.3f), -0.5)
                .renderInto(pose, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        ModelBuffers.partial(BASE, Blocks.ANDESITE.defaultBlockState())
                .rotateY((float) Mth.atan2(diff.x(), diff.z()))
                .rotateX(-(float) Mth.atan2(diff.y(), hypot) + Mth.HALF_PI)
                .light(light)
                .translate(-0.5, Mth.clamp((poleLength - 1.3f) / 4, poleLength - 4.6f, poleLength - 1.3f), -0.5)
                .renderInto(pose, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        if (offHandItem.is(CEETags.HIDE_ON_LINEMANS_STICK)) {
            ItemStackRenderState heldState=new ItemStackRenderState();
            mc.getItemModelResolver().updateForTopItem(heldState,offHandItem,ItemDisplayContext.NONE,level,player,0);
            pose.pushPose();

            PoseTransformStack msr = TransformStack.of(pose)
                    .translate(diff);

            if (WireInteractionHandler.targetedPoint == null ||
                    offHandItem.getItem() instanceof ClampMeterItem)
                msr.rotateY(Mth.PI + (float) Mth.atan2(diff.x(), diff.z()));
            else {
                Vec3 pos1 = WireInteractionHandler.targetedPoint.node1().getPosition(level);
                Vec3 pos2 = WireInteractionHandler.targetedPoint.node2().getPosition(level);

                msr.rotateY(Mth.HALF_PI + (float) Mth.atan2(pos1.x() - pos2.x(), pos1.z() - pos2.z()));
            }

            if (offHandItem.is(CEETags.WIRE_DAMPER_ITEM)) {

                msr.translate(0.5, 0.5, 0.5);
            } else if (offHandItem.getItem() instanceof BannerItem) {
                msr.translate(0, -1.5f, 0);
            } else if (offHandItem.getItem() instanceof ClampMeterItem) {
                msr
                        .rotateYDegrees(179) // 179 to prevent Z-fighting
                        .rotateX(-(float) Mth.atan2(diff.y(), hypot) + Mth.HALF_PI)
                        .translate(0, -4/16f, 0);

            }

            if(offHandItem.is(CEETags.WIRE_DAMPER_ITEM))
                ModelBuffers.partial(CEEPartialModels.WIRE_DAMPER_ATTACHMENT,Blocks.ANDESITE.defaultBlockState()).light(light).renderInto(pose,buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
            else buffer.defer(pose,(matrices,queue)->heldState.submit(matrices,queue,light,OverlayTexture.NO_OVERLAY,0));
            pose.popPose();
        }

        pose.popPose();
    }
}
