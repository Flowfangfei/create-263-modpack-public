package com.simibubi.create.compat.rei.animation;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public class AnimatedBlazeBurner extends AnimatedKinetics {
	private HeatLevel heatLevel;

	public AnimatedBlazeBurner withHeat(HeatLevel heatLevel) {
		this.heatLevel = heatLevel;
		return this;
	}

	@Override
	protected void drawAnimation(GuiGraphicsExtractor graphics, int x, int y) {
		boolean superheated = heatLevel == HeatLevel.SEETHING;
		float bob = (Mth.sin(getAnimationTime() / 16f) + .5f) / 16f;
		blockElement(AllBlocks.BLAZE_BURNER.getDefaultState())
			.at(x, y).atLocal(0, 1.65f, 0).scale(23).submit(graphics);
		blockElement(superheated ? AllPartialModels.BLAZE_SUPER : AllPartialModels.BLAZE_ACTIVE)
			.at(x, y).atLocal(0, 1.8f, 0).rotateBlock(0, 180, 0).scale(23).submit(graphics);
		blockElement(superheated ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2 : AllPartialModels.BLAZE_BURNER_RODS_2)
			.at(x, y).atLocal(0, 1.7f + bob, 0).rotateBlock(0, 180, 0).scale(23).submit(graphics);
	}

	@Override protected float getGlobalXRotation() { return -15.5f; }
	@Override protected float getGlobalYRotation() { return 22.5f; }
}
