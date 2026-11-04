package com.simibubi.create.content.kinetics.turntable;

import com.simibubi.create.AllBlocks;

import net.createmod.catnip.api.math.VecHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class TurntableHandler {
	private static float lastPartialTick = Float.NaN;

	public static void gameRenderFrame(DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null)
			return;
		BlockPos pos = mc.player.getOnPos();

		if (mc.gameMode == null)
			return;
		if (!AllBlocks.TURNTABLE.has(mc.level.getBlockState(pos)))
			return;
		if (!mc.player.onGround())
			return;
		if (mc.isPaused())
			return;

		BlockEntity blockEntity = mc.level.getBlockEntity(pos);
		if (!(blockEntity instanceof TurntableBlockEntity turnTable))
			return;

		float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
		float partialDelta = Float.isNaN(lastPartialTick) ? deltaTracker.getRealtimeDeltaTicks()
			: partialTick - lastPartialTick;
		lastPartialTick = partialTick;
		if (partialDelta < 0)
			partialDelta++;
		if (partialDelta > 1)
			partialDelta = deltaTracker.getRealtimeDeltaTicks();

		float tickSpeed = mc.level.tickRateManager().tickrate() / 20;
		float speed = turnTable.getSpeed() * (2/3f) * tickSpeed * partialDelta;

		if (speed == 0)
			return;

		Vec3 origin = VecHelper.getCenterOf(pos);
		Vec3 offset = mc.player.position().subtract(origin);

		if (offset.length() > 1 / 4f)
			speed *= (float)Mth.clamp((1 / 2f - offset.length()) * 2, 0, 1);

		float yRotOffset = speed * deltaTracker.getGameTimeDeltaPartialTick(false);
		mc.player.setYRot(mc.player.getYRot() - yRotOffset);
		mc.player.yBodyRot -= yRotOffset;
	}

}
