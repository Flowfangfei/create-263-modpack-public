package com.george_vi.electroenergetics.client.render;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

/** Native item-model layers for the telescoping stick and clamp meter. */
public final class HandToolModel implements ItemModel {
    public static final Identifier ID = CreateElectroEnergetics.rl("hand_tool");
    private final String kind;
    private final List<Part> parts;

    private HandToolModel(String kind, List<Part> parts) {
        this.kind = kind;
        this.parts = parts;
    }

    @Override
    public void update(
            ItemStackRenderState state,
            ItemStack stack,
            ItemModelResolver resolver,
            ItemDisplayContext context,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner,
            int seed) {
        state.appendModelIdentityElement(this);
        var player = Minecraft.getInstance().player;
        boolean mainFirst =
                player != null
                        && ((context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                                        && player.getMainArm() == HumanoidArm.LEFT)
                                || (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                                        && player.getMainArm() == HumanoidArm.RIGHT));
        if (mainFirst && (kind.equals("linemans_stick") || player.getUseItem() == stack)) return;
        float extension =
                context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                        ? 0.25f
                        : 0;
        for (int i = 0; i < parts.size(); i++) {
            Part part = parts.get(i);
            var layer =
                    ItemModelRenderHelper.submitQuads(state, part.properties, context, part.quads);
            layer.setExtents(part.extents);
            if (i > 0)
                layer.setLocalTransform(
                        new org.joml.Matrix4f().translation(0, extension * 1.7f * i, 0));
            if (stack.hasFoil()) layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
        }
    }

    private record Part(
            List<BakedQuad> quads,
            ModelRenderProperties properties,
            Supplier<Vector3fc[]> extents) {}

    public record Unbaked(String kind) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC =
                RecordCodecBuilder.mapCodec(
                        i ->
                                i.group(
                                                com.mojang.serialization.Codec.STRING
                                                        .fieldOf("kind")
                                                        .forGetter(Unbaked::kind))
                                        .apply(i, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        private List<Identifier> models() {
            return kind.equals("linemans_stick")
                    ? List.of(
                            CreateElectroEnergetics.rl("item/linemans_stick"),
                            CreateElectroEnergetics.rl("item/linemans_stick_telescope"),
                            CreateElectroEnergetics.rl("item/linemans_stick_head"))
                    : List.of(CreateElectroEnergetics.rl("item/clamp_meter"));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            models().forEach(resolver::markDependency);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            List<Part> parts = new ArrayList<>();
            var baker = context.blockModelBaker();
            ModelRenderProperties properties = null;
            for (Identifier id : models()) {
                ResolvedModel resolved = baker.getModel(id);
                TextureSlots textures = resolved.getTopTextureSlots();
                if (properties == null)
                    properties = ModelRenderProperties.fromResolvedModel(baker, resolved, textures);
                List<BakedQuad> quads =
                        resolved.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY)
                                .getAll();
                parts.add(
                        new Part(
                                quads,
                                properties,
                                Suppliers.memoize(
                                        () -> CuboidItemModelWrapper.computeExtents(quads))));
            }
            return new HandToolModel(kind, parts);
        }
    }
}
