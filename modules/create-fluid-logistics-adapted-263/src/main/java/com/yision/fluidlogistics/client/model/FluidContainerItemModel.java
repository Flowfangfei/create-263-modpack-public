package com.yision.fluidlogistics.client.model;
import java.util.*;
import java.util.function.*;
import com.google.common.base.Suppliers;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.resources.model.geometry.*;
import net.minecraft.client.resources.model.sprite.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import org.joml.*;
import com.yision.fluidlogistics.FluidLogistics;
import com.yision.fluidlogistics.foundation.render.*;
import com.yision.fluidlogistics.content.logistics.fluidPackage.client.FluidPackageItemRenderer;
import com.yision.fluidlogistics.content.logistics.fluidPackage.client.CompressedTankItemRenderer;
/** Native item layers preserve both the frame and data-dependent fluid contents. */
public final class FluidContainerItemModel implements ItemModel,SpecialModelRenderer<FluidContainerItemModel.Data> {
    public static final Identifier ID=FluidLogistics.asResource("fluid_container");
    private final ItemQuads quads;private final ModelRenderProperties properties;private final Supplier<Vector3fc[]> extents;private final Matrix4fc transform;private final String kind;
    private FluidContainerItemModel(List<BakedQuad> quads,ModelRenderProperties properties,Matrix4fc transform,String kind){this.quads=ItemQuads.split(quads);this.properties=properties;this.transform=transform;this.kind=kind;extents=Suppliers.memoize(()->CuboidItemModelWrapper.computeExtents(quads));}
    public record Data(ItemStack stack,ItemDisplayContext context){}
    public void update(ItemStackRenderState state,ItemStack stack,ItemModelResolver resolver,ItemDisplayContext context,ClientLevel level,ItemOwner owner,int seed){
        state.appendModelIdentityElement(this);state.appendModelIdentityElement(stack.getComponentsPatch());
        boolean icon=kind.equals("compressed") && CompressedTankItemRenderer.shouldRenderFluidIcon(context) && !com.yision.fluidlogistics.foundation.fluid.FluidDisplayHelper.getDisplayFluid(stack).isEmpty();
        if(!icon){var frame=state.newLayer();properties.applyToLayer(frame,context);frame.setLocalTransform(transform);frame.setExtents(extents);frame.setQuads(quads);if(stack.hasFoil())frame.setFoilType(ItemStackRenderState.FoilType.STANDARD);}
        var fluid=state.newLayer();properties.applyToLayer(fluid,context);fluid.setLocalTransform(transform);fluid.setExtents(extents);fluid.setupSpecialModel(this,new Data(stack.copy(),context));
    }
    public void submit(Data data,PoseStack pose,SubmitNodeCollector collector,int light,int overlay,boolean glint,int color){
        var geometry=new FluidLogisticsRenderCompat.GeometryCollector(collector);
        switch(kind){
            case "package" -> FluidPackageItemRenderer.renderFluidContents(data.stack,pose,geometry,light);
            case "casing" -> {pose.pushPose();pose.translate(-.5f,-.5f,-.5f);com.yision.fluidlogistics.content.fluids.waterContainingCopperCasing.WaterContainingCopperCasingBlock.Renderer.renderFluid(pose,geometry,light,data.context);pose.popPose();}
            case "infinite" -> com.yision.fluidlogistics.content.fluids.infiniteFluidTank.client.InfiniteFluidTankItemRenderer.renderDynamic(data.stack,data.context,pose,geometry,light);
            case "compressed" -> CompressedTankItemRenderer.renderDynamic(data.stack,data.context,pose,geometry,light);
            default -> throw new IllegalArgumentException("Unknown fluid container renderer: "+kind);
        }
        geometry.submit();
    }
    public void getExtents(Consumer<Vector3fc> out){for(var point:extents.get())out.accept(point);}
    public Data extractArgument(ItemStack stack){return new Data(stack.copy(),ItemDisplayContext.NONE);}
    public record Unbaked(Identifier model,String kind) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC=RecordCodecBuilder.mapCodec(instance->instance.group(Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model),Codec.STRING.fieldOf("kind").forGetter(Unbaked::kind)).apply(instance,Unbaked::new));
        public MapCodec<Unbaked> type(){return CODEC;}
        public void resolveDependencies(Resolver resolver){resolver.markDependency(model);}
        public ItemModel bake(ItemModel.BakingContext context,Matrix4fc transform){var baker=context.blockModelBaker();var source=baker.getModel(model);var slots=source.getTopTextureSlots();return new FluidContainerItemModel(source.bakeTopGeometry(slots,baker,BlockModelRotation.IDENTITY).getAll(),ModelRenderProperties.fromResolvedModel(baker,source,slots),transform,kind);}
    }
}
