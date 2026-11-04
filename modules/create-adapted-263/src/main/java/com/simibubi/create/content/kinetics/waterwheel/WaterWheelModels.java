package com.simibubi.create.content.kinetics.waterwheel;

import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class WaterWheelModels {
	private static final RendererReloadCache<WaterWheelModels.Key, BlockStateModelPart> CACHE = new RendererReloadCache<>(WaterWheelModels::create);

	private WaterWheelModels() {
	}

	public static BlockStateModelPart get(WaterWheelRenderer.Variant variant, BlockState material) {
		return CACHE.get(new WaterWheelModels.Key(variant, material));
	}

	private static BlockStateModelPart create(WaterWheelModels.Key key) {
		ModelManager models = Minecraft.getInstance().getModelManager();
		BlockStateModelPart original = (BlockStateModelPart)models.getStandaloneModel(key.variant.key());
		TextureAtlasSprite planks = models.getBlockStateModelSet().get(key.material).particleMaterial().sprite();
		TextureAtlasSprite bark = planks;
		TextureAtlasSprite end = planks;
		Identifier id = BuiltInRegistries.BLOCK.getKey(key.material.getBlock());
		String wood = id.getPath().replaceFirst("_planks$", "");

		for (String suffix : List.of("_log", "_stem", "_block")) {
			Block log = (Block)BuiltInRegistries.BLOCK.getOptional(Identifier.fromNamespaceAndPath(id.getNamespace(), wood + suffix)).orElse(null);
			if (log != null) {
				List<BlockStateModelPart> parts = new ArrayList<>();
				models.getBlockStateModelSet().get(log.defaultBlockState()).collectParts(RandomSource.create(42L), parts);

				for (BlockStateModelPart part : parts) {
					if (!part.getQuads(Direction.NORTH).isEmpty()) {
						bark = ((BakedQuad)part.getQuads(Direction.NORTH).getFirst()).materialInfo().sprite();
					}

					if (!part.getQuads(Direction.UP).isEmpty()) {
						end = ((BakedQuad)part.getQuads(Direction.UP).getFirst()).materialInfo().sprite();
					}
				}
				break;
			}
		}

		return new WaterWheelModels.RetexturedPart(original, planks, bark, end);
	}

	private record Key(WaterWheelRenderer.Variant variant, BlockState material) {
	}

	private static final class RetexturedPart implements BlockStateModelPart {
		private final BlockStateModelPart original;
		private final TextureAtlasSprite planks;
		private final Map<Direction, List<BakedQuad>> quads = new HashMap<>();

		RetexturedPart(BlockStateModelPart original, TextureAtlasSprite planks, TextureAtlasSprite bark, TextureAtlasSprite end) {
			this.original = original;
			this.planks = planks;
			List<Direction> sides = new ArrayList<>(List.of(Direction.values()));
			sides.add(null);

			for (Direction side : sides) {
				List<BakedQuad> result = new ArrayList<>();

				for (BakedQuad quad : original.getQuads(side)) {
					TextureAtlasSprite source = quad.materialInfo().sprite();
					String name = source.contents().name().toString();

					TextureAtlasSprite target = switch (name) {
						case "minecraft:block/oak_planks" -> planks;
						case "minecraft:block/oak_log" -> bark;
						case "minecraft:block/oak_log_top" -> end;
						default -> null;
					};
					if (target != null && target != source) {
						MaterialInfo info = quad.materialInfo();
						MaterialInfo replacement = new MaterialInfo(
							target,
							info.layer(),
							info.itemRenderType(),
							info.itemGlintRenderType(),
							info.itemGlintSpecialRenderType(),
							info.tintIndex(),
							info.shadeDirectionOverride(),
							info.lightEmission(),
							info.ambientOcclusion()
						);
						result.add(
							new BakedQuad(
								quad.position0(),
								quad.position1(),
								quad.position2(),
								quad.position3(),
								uv(quad.packedUV0(), source, target),
								uv(quad.packedUV1(), source, target),
								uv(quad.packedUV2(), source, target),
								uv(quad.packedUV3(), source, target),
								quad.direction(),
								replacement
							)
						);
					} else {
						result.add(quad);
					}
				}

				this.quads.put(side, List.copyOf(result));
			}
		}

		private static long uv(long packed, TextureAtlasSprite source, TextureAtlasSprite target) {
			float u = Float.intBitsToFloat((int)(packed >>> 32));
			float v = Float.intBitsToFloat((int)packed);
			float localU = (u - source.getU0()) / (source.getU1() - source.getU0());
			float localV = (v - source.getV0()) / (source.getV1() - source.getV0());
			return Integer.toUnsignedLong(Float.floatToIntBits(target.getU(localU))) << 32 | Integer.toUnsignedLong(Float.floatToIntBits(target.getV(localV)));
		}

		public List<BakedQuad> getQuads(Direction side) {
			return this.quads.get(side);
		}

		public boolean useAmbientOcclusion() {
			return this.original.useAmbientOcclusion();
		}

		public Baked particleMaterial() {
			return new Baked(this.planks, this.original.particleMaterial().forceTranslucent());
		}

		public int materialFlags() {
			return this.original.materialFlags();
		}
	}
}
