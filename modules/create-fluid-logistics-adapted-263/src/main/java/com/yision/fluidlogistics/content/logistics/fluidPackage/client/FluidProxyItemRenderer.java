package com.yision.fluidlogistics.content.logistics.fluidPackage.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import org.joml.*;
import java.util.function.Consumer;
import com.yision.fluidlogistics.FluidLogistics;
import com.yision.fluidlogistics.content.logistics.fluidPackage.FluidProxyRegistry;
import net.neoforged.neoforge.fluids.FluidStack;
public final class FluidProxyItemRenderer implements ItemModel, SpecialModelRenderer<FluidStack> {
    public static final Identifier ID=FluidLogistics.asResource("fluid_proxy");
    private final Matrix4fc transform;
    private FluidProxyItemRenderer(Matrix4fc transform) {this.transform=transform;}
    public void update(ItemStackRenderState state,ItemStack stack,ItemModelResolver resolver,ItemDisplayContext context,ClientLevel level,ItemOwner owner,int seed) {
        var fluid=extractArgument(stack); state.appendModelIdentityElement(this); state.appendModelIdentityElement(fluid);
        var layer=state.newLayer();layer.setLocalTransform(transform);layer.setUsesBlockLight(false);
        layer.setExtents(() -> new Vector3fc[]{new Vector3f(-.5f,-.5f,-.05f),new Vector3f(.5f,.5f,.05f)});layer.setupSpecialModel(this,fluid);
    }
    public void submit(FluidStack fluid,PoseStack pose,SubmitNodeCollector collector,int light,int overlay,boolean glint,int color) {
        if(fluid.isEmpty())return;
        net.createmod.catnip.api.client.render.FluidRenderHelper.submitFluidBox(collector,fluid,-.5f,-.5f,-1/32f,.5f,.5f,0,pose,light,true,false);
    }
    public void getExtents(Consumer<Vector3fc> output) {output.accept(new Vector3f(-.5f,-.5f,-.05f));output.accept(new Vector3f(.5f,.5f,.05f));}
    public FluidStack extractArgument(ItemStack stack) {return FluidProxyRegistry.getFluid(stack).copy();}
    public record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC=MapCodec.unit(new Unbaked());
        public MapCodec<Unbaked> type(){return CODEC;}
        public void resolveDependencies(Resolver resolver){}
        public ItemModel bake(ItemModel.BakingContext context,Matrix4fc transform){return new FluidProxyItemRenderer(transform);}
    }
}
