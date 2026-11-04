package com.yision.fluidlogistics.client;
public final class FluidVisuals {
    private FluidVisuals() {}
    public static net.minecraft.client.renderer.block.FluidModel model(net.neoforged.neoforge.fluids.FluidStack fluid) { return net.minecraft.client.Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.getFluid().defaultFluidState()); }
    public static net.minecraft.client.renderer.texture.TextureAtlasSprite still(net.neoforged.neoforge.fluids.FluidStack fluid) { return model(fluid).stillMaterial().sprite(); }
    public static int tint(net.neoforged.neoforge.fluids.FluidStack fluid) { return model(fluid).fluidTintSource().colorAsStack(fluid); }
}
