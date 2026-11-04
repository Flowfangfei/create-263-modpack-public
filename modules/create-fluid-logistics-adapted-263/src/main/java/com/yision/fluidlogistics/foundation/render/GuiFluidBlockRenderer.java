package com.yision.fluidlogistics.foundation.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public final class GuiFluidBlockRenderer {
	private static final float BLOCK_GUI_SCALE = 0.625f;

	private GuiFluidBlockRenderer() {
		throw new AssertionError("This class should not be instantiated");
	}

	public static void render(GuiGraphicsExtractor graphics, FluidStack fluid, int x, int y, float scale) {
		if (fluid.isEmpty() || fluid.getFluid() == Fluids.EMPTY) {
			return;
		}

        net.createmod.catnip.api.client.gui.element.GuiGameElement.of(fluid.getFluid())
            .at(x + 8 * scale, y + 8 * scale, 0).rotate(30, 225, 0)
            .scale(16 * scale * BLOCK_GUI_SCALE).submit(graphics);
    }
}
