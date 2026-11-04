package com.yision.fluidlogistics.content.fluids.copperBucket.client;

import java.util.*;
import java.util.function.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.geometry.*;
import net.minecraft.client.resources.model.sprite.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.BuiltInRegistries;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import com.yision.fluidlogistics.FluidLogistics;
import com.yision.fluidlogistics.registry.AllDataComponents;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/** Native immutable item layers retain generated copper sprites and each fluid's real shape. */
public final class CopperBucketModel implements ItemModel {
    public static final Identifier ID = FluidLogistics.asResource("copper_bucket");
    private final Part fallback;
    private final Map<Item, Part> buckets;
    private final Matrix4fc transform;
    private CopperBucketModel(Part fallback, Map<Item, Part> buckets, Matrix4fc transform) { this.fallback=fallback; this.buckets=buckets; this.transform=transform; }
    @Override public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext context, ClientLevel level, ItemOwner owner, int seed) {
        var content = stack.getOrDefault(AllDataComponents.COPPER_BUCKET_CONTENT, SimpleFluidContent.EMPTY);
        Part part = content.isEmpty() ? fallback : buckets.getOrDefault(content.getFluid().getBucket(), fallback);
        state.appendModelIdentityElement(this); state.appendModelIdentityElement(part); state.appendModelIdentityElement(content);
        var layer=state.newLayer(); part.properties.applyToLayer(layer, context); layer.setLocalTransform(transform); layer.setExtents(part.extents);
        layer.setQuads(ItemQuads.split(part.quads));
        if (stack.hasFoil()) layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
        if (!content.isEmpty()) {
            int tint = com.yision.fluidlogistics.client.FluidVisuals.tint(content.copy());
            for (var quad : part.quads) { int index=quad.materialInfo().tintIndex(); if (index>=0) { while(layer.tintLayers().size()<=index) layer.tintLayers().add(-1); layer.tintLayers().set(index,tint); } }
        }
    }
    private record Part(List<BakedQuad> quads, ModelRenderProperties properties, Supplier<Vector3fc[]> extents) {}
    public record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC=MapCodec.unit(new Unbaked());
        @Override public MapCodec<Unbaked> type() { return CODEC; }
        @Override public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(FluidLogistics.asResource("item/copper_bucket"));
            BuiltInRegistries.FLUID.forEach(fluid -> { var bucket=fluid.getBucket(); if(bucket!=Items.AIR) resolver.markDependency(BuiltInRegistries.ITEM.getKey(bucket).withPrefix("item/")); });
        }
        @Override public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            var baker=context.blockModelBaker(); var fallback=bakePart(baker,FluidLogistics.asResource("item/copper_bucket"),null);
            Map<Item,Part> buckets=new IdentityHashMap<>();
            BuiltInRegistries.FLUID.forEach(fluid -> {
                Item bucket=fluid.getBucket(); if(bucket==Items.AIR || buckets.containsKey(bucket)) return;
                var id=BuiltInRegistries.ITEM.getKey(bucket);
                var layers=CopperBucketSpriteSource.layerSprites(Minecraft.getInstance().getResourceManager(),id);
                if(layers.isEmpty()) return;
                buckets.put(bucket,bakePart(baker,id.withPrefix("item/"),id));
            });
            return new CopperBucketModel(fallback,Map.copyOf(buckets),transformation);
        }
        private static Part bakePart(ModelBaker baker,Identifier id,Identifier bucket) {
            var model=baker.getModel(id); var slots=model.getTopTextureSlots();
            var quads=model.bakeTopGeometry(slots,baker,BlockModelRotation.IDENTITY).getAll();
            if(bucket!=null) quads=quads.stream().map(quad -> retexture(baker,model,bucket,quad)).toList();
            var frozen=List.copyOf(quads);
            return new Part(frozen,ModelRenderProperties.fromResolvedModel(baker,model,slots),Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(frozen)));
        }
        private static BakedQuad retexture(ModelBaker baker,ResolvedModel debug,Identifier bucket,BakedQuad quad) {
            var info=quad.materialInfo(); int sourceLayer=Math.max(0,info.tintIndex());
            var replacement=baker.materials().get(new Material(CopperBucketSpriteSource.generatedSprite(bucket,sourceLayer)),debug).sprite();
            if(replacement.contents().name().equals(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation())) return quad;
            var old=info.sprite();
            var material=new BakedQuad.MaterialInfo(replacement,info.layer(),info.itemRenderType(),info.itemGlintRenderType(),info.itemGlintSpecialRenderType(),info.tintIndex(),info.shadeDirectionOverride(),info.lightEmission(),info.ambientOcclusion());
            return new BakedQuad(quad.position0(),quad.position1(),quad.position2(),quad.position3(),uv(quad.packedUV0(),old,replacement),uv(quad.packedUV1(),old,replacement),uv(quad.packedUV2(),old,replacement),uv(quad.packedUV3(),old,replacement),quad.direction(),material,quad.bakedNormals(),quad.bakedColors());
        }
        private static long uv(long value,TextureAtlasSprite old,TextureAtlasSprite replacement) {
            float u=(UVPair.unpackU(value)-old.getU0())/(old.getU1()-old.getU0()); float v=(UVPair.unpackV(value)-old.getV0())/(old.getV1()-old.getV0());
            return UVPair.pack(replacement.getU(u),replacement.getV(v));
        }
    }
}
