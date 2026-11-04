package dev.simulated_team.simulated.content.blocks.steering_wheel;

import java.util.ArrayList;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelRenderer;
import com.simibubi.create.foundation.model.BakedModelHelper;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.simulated_team.simulated.index.SimPartialModels;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import dev.simulated_team.simulated.compat.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperBufferFactory;
import dev.simulated_team.simulated.compat.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.SuperByteBufferCache;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class SteeringWheelRenderer extends KineticBlockEntityRenderer<SteeringWheelBlockEntity> {
    public static final SuperByteBufferCache.Compartment<ModelKey> STEERING_WHEEL = new SuperByteBufferCache.Compartment<>();


    public SteeringWheelRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final SteeringWheelBlockEntity be, final float partialTicks, final PoseStack ms, final SubmitNodeCollector buffer,
                              final int light, final int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel()) && !isRenderingInSubLevel()) {
            return;
        }

        final boolean floor = be.getBlockState().getValue(SteeringWheelBlock.ON_FLOOR);
        final Direction facing = be.getBlockState().getValue(SteeringWheelBlock.FACING);

        if (be.shouldRenderShaft()) {
            final BlockState state = this.getRenderedBlockState(be);
            final RenderType type = RenderTypes.solidMovingBlock();
            renderRotatingBuffer(be, CachedBuffers.partialFacing(
                    AllPartialModels.SHAFT_HALF,
                    be.getBlockState(),
                    floor ? Direction.DOWN : Direction.UP
            ), ms, buffer, light);
        }

        final SuperByteBuffer model = this.getWheelModel(be);

        model.rotateCentered(facing.getRotation());
        if (floor) {
            model.translate(0, 6.5 / 16f, -5 / 16f);
        } else {
            model.translate(0, 6.5 / 16f, 5 / 16f);
        }
        model.rotateCentered(be.getRenderAngle(partialTicks), Direction.UP);

        model.light(light);
        model.color(Color.WHITE);
        model.submit(ms, buffer);
    }

    private SuperByteBuffer getWheelModel(final SteeringWheelBlockEntity be) {
        final ModelKey key = new ModelKey(be.material);
        return CachedBuffers.model(STEERING_WHEEL,key,()->generateModel(SimPartialModels.STEERING_WHEEL.get(),be.material),Blocks.AIR.defaultBlockState());
    }

    /** Retextures current immutable quads, preserving their normals, colors and material properties. */
    public static BlockStateModelPart generateModel(final BlockStateModelPart template,final BlockState planks){
        if(plankStateToWoodName(planks)==null)return template;
        var original=getSpriteOnSide(Blocks.OAK_PLANKS.defaultBlockState(),Direction.UP);
        var replacement=getSpriteOnSide(planks,Direction.UP);
        if(original==null||replacement==null)return template;
        return new BlockStateModelPart(){
            private final java.util.Map<Direction,List<BakedQuad>> quads=new java.util.HashMap<>();
            public List<BakedQuad> getQuads(Direction side){return quads.computeIfAbsent(side,d->template.getQuads(d).stream().map(q->swapSprite(q,original,replacement)).toList());}
            public boolean useAmbientOcclusion(){return template.useAmbientOcclusion();}
            public net.minecraft.client.resources.model.sprite.Material.Baked particleMaterial(){var m=template.particleMaterial();return m.sprite()==original?new net.minecraft.client.resources.model.sprite.Material.Baked(replacement,m.forceTranslucent()):m;}
            public int materialFlags(){return template.materialFlags();}
        };
    }
    private static BakedQuad swapSprite(BakedQuad quad,TextureAtlasSprite original,TextureAtlasSprite replacement){
        var m=quad.materialInfo();if(m.sprite()!=original)return quad;
        var material=new BakedQuad.MaterialInfo(replacement,m.layer(),m.itemRenderType(),m.itemGlintRenderType(),m.itemGlintSpecialRenderType(),m.tintIndex(),m.shadeDirectionOverride(),m.lightEmission(),m.ambientOcclusion());
        return new BakedQuad(quad.position0(),quad.position1(),quad.position2(),quad.position3(),remapUv(quad.packedUV0(),original,replacement),remapUv(quad.packedUV1(),original,replacement),remapUv(quad.packedUV2(),original,replacement),remapUv(quad.packedUV3(),original,replacement),quad.direction(),material,quad.bakedNormals(),quad.bakedColors());
    }
    private static long remapUv(long uv,TextureAtlasSprite original,TextureAtlasSprite replacement){
        float u=(net.minecraft.client.model.geom.builders.UVPair.unpackU(uv)-original.getU0())/(original.getU1()-original.getU0());
        float v=(net.minecraft.client.model.geom.builders.UVPair.unpackV(uv)-original.getV0())/(original.getV1()-original.getV0());
        return net.minecraft.client.model.geom.builders.UVPair.pack(replacement.getU(u),replacement.getV(v));
    }

    public record ModelKey(BlockState material) {
    }

    // todo tell create to make these public :p
    @Nullable
    private static String plankStateToWoodName(final BlockState planksBlockState) {
        final Block planksBlock = planksBlockState.getBlock();
        final Identifier id = RegisteredObjectsHelper.getKeyOrThrow(planksBlock);
        final String path = id.getPath();

        if (path.endsWith("_planks")) // Covers most wood types
            return (path.startsWith("archwood") ? "blue_" : "") + path.substring(0, path.length() - 7);

        if (path.contains("wood/planks/")) // TerraFirmaCraft
            return path.substring(12);

        return null;
    }

    private static TextureAtlasSprite getSpriteOnSide(final BlockState state, final Direction side) {
        final BlockStateModel model = Minecraft.getInstance()
                .getModelManager()
                .getBlockStateModelSet()
                .get(state);
        if (model == null)
            return null;
        final RandomSource random = RandomSource.create();
        random.setSeed(42L);
        final List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(random, parts);
        for (final BlockStateModelPart part : parts) {
            final List<BakedQuad> quads = part.getQuads(side);
            if (!quads.isEmpty()) {
                return quads.getFirst().materialInfo().sprite();
            }
        }
        random.setSeed(42L);
        for (final BlockStateModelPart part : parts) {
            for (final BakedQuad quad : part.getQuads(null)) {
                if (quad.direction() == side) {
                    return quad.materialInfo().sprite();
                }
            }
        }
        return model.particleMaterial().sprite();
    }
}
