package com.yision.fluidlogistics.content.logistics.fluidPackage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yision.fluidlogistics.foundation.fluid.FluidDisplayHelper;
import com.yision.fluidlogistics.foundation.render.FluidSlotRenderer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Dynamic icon layer used by the native fluid_container model. */
public final class CompressedTankItemRenderer {
    private CompressedTankItemRenderer() {}

    public static void renderDynamic(ItemStack stack, ItemDisplayContext context, PoseStack pose,
            MultiBufferSource buffer, int light) {
        var fluid = FluidDisplayHelper.getDisplayFluid(stack);
        if (!fluid.isEmpty() && shouldRenderFluidIcon(context)) {
            float halfSize = context == ItemDisplayContext.FIXED ? 0.375f : 0.5f;
            FluidSlotRenderer.renderFluidItemIcon(fluid, pose, buffer, light, halfSize);
        }
    }

    public static boolean shouldRenderFluidIcon(ItemDisplayContext context) {
        return context == ItemDisplayContext.GUI || context == ItemDisplayContext.GROUND
            || context == ItemDisplayContext.FIXED || context == ItemDisplayContext.HEAD;
    }
}
