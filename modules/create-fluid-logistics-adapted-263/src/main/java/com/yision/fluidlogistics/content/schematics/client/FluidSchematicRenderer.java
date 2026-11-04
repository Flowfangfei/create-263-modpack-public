package com.yision.fluidlogistics.content.schematics.client;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.schematics.client.SchematicRenderer;
import com.yision.fluidlogistics.content.schematics.FluidSchematicPlan;
import com.yision.fluidlogistics.content.schematics.FluidSchematicPlan.Kind;

import net.createmod.catnip.api.level.wrapper.SchematicLevel;
import net.createmod.catnip.api.client.render.ShadedBlockSbbBuilder;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.ModelBuffer;
import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class FluidSchematicRenderer extends SchematicRenderer {

	private static final ThreadLocal<ShadedBlockSbbBuilder> FLUID_BUFFER_BUILDER = ThreadLocal
		.withInitial(ShadedBlockSbbBuilder::create);

	private final Map<RenderType, List<FluidSectionBuffer>> fluidBufferCache = new LinkedHashMap<>();
	private boolean fluidChanged = true;
    private final SchematicLevel schematic;

	public FluidSchematicRenderer(SchematicLevel world) {
		this(createFluidLevel(world),true);
    }
    private FluidSchematicRenderer(SchematicLevel world,boolean prepared) {
        super(world); this.schematic=world;
	}

	private static SchematicLevel createFluidLevel(SchematicLevel source) {
		SchematicLevel fluidLevel = new SchematicLevel(source.anchor, Minecraft.getInstance().level);
		fluidLevel.setBounds(source.getBounds());
		for (BlockPos localPos : source.getAllPositions()) {
			BlockPos pos = localPos.offset(source.anchor);
			BlockState state = source.getBlockState(pos);
			if (FluidSchematicPlan.classify(state) != Kind.AIR) {
				fluidLevel.setBlock(pos, state, Block.UPDATE_CLIENTS);
				fluidLevel.getBlockEntity(pos);
			}
		}
		return fluidLevel;
	}

	@Override
	public void update() {
		super.update();
		fluidChanged = true;
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null) {
			return;
		}
		if (fluidChanged) {
			redrawFluids();
			fluidChanged = false;
		}

		super.submit(poseStack, collector, camera);
        var buffers = new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.GeometryCollector(collector);
		fluidBufferCache.forEach((layer, sectionBuffers) -> {
			for (FluidSectionBuffer sectionBuffer : sectionBuffers) {
				BlockPos offset = sectionBuffer.offset();
				poseStack.pushPose();
				poseStack.translate(offset.getX(), offset.getY(), offset.getZ());
				sectionBuffer.buffer().renderInto(poseStack, buffers.getBuffer(layer));
				poseStack.popPose();
			}
		});
        buffers.submit();
	}

	private void redrawFluids() {
		fluidBufferCache.clear();
		Map<RenderType, Map<FluidSectionKey, List<BlockPos>>> positionsByLayer = new LinkedHashMap<>();

		for (BlockPos localPos : schematic.getAllPositions()) {
			BlockPos pos = localPos.offset(schematic.anchor);
			BlockState state = schematic.getBlockState(pos);
			if (FluidSchematicPlan.classify(state) == Kind.AIR) {
				continue;
			}
			FluidState fluidState = state.getFluidState();
			var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState);
            RenderType layer = model.layer() == net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT ? RenderTypes.translucentMovingBlock() : RenderTypes.cutoutMovingBlock();
			FluidSectionKey section = FluidSectionKey.of(pos);
			positionsByLayer.computeIfAbsent(layer, ignored -> new LinkedHashMap<>())
				.computeIfAbsent(section, ignored -> new ArrayList<>())
				.add(pos);
		}

		FluidRenderer dispatcher = new FluidRenderer(Minecraft.getInstance().getModelManager().getFluidStateModelSet());
		ShadedBlockSbbBuilder builder = FLUID_BUFFER_BUILDER.get();
		schematic.renderMode = true;
		try {
			for (RenderType layer : positionsByLayer.keySet()) {
				Map<FluidSectionKey, List<BlockPos>> sections = positionsByLayer.get(layer);
				if (sections == null) {
					continue;
				}

				List<FluidSectionBuffer> sectionBuffers = new ArrayList<>();
				for (Map.Entry<FluidSectionKey, List<BlockPos>> entry : sections.entrySet()) {
					builder.begin();
					VertexConsumer liquidBuffer = builder.unwrap(false);
					if (layer == RenderTypes.solidMovingBlock()) {
						liquidBuffer = new SolidFluidVertexConsumer(liquidBuffer);
					}
					for (BlockPos pos : entry.getValue()) {
						BlockState state = schematic.getBlockState(pos);
						VertexConsumer output=liquidBuffer;
                        dispatcher.tesselate(new RenderWorld(schematic), pos, ignored -> output, state, state.getFluidState());
					}
					SuperByteBuffer buffer = builder.end();
					if (!buffer.isEmpty()) {
						sectionBuffers.add(new FluidSectionBuffer(entry.getKey().offsetFrom(schematic.anchor), buffer));
					}
				}
				if (!sectionBuffers.isEmpty()) {
					fluidBufferCache.put(layer, sectionBuffers);
				}
			}
		} finally {
			schematic.renderMode = false;
		}
	}

	private record FluidSectionKey(int x, int y, int z) {

		private static FluidSectionKey of(BlockPos pos) {
			return new FluidSectionKey(
				SectionPos.blockToSectionCoord(pos.getX()),
				SectionPos.blockToSectionCoord(pos.getY()),
				SectionPos.blockToSectionCoord(pos.getZ()));
		}

		private BlockPos offsetFrom(BlockPos anchor) {
			return new BlockPos(
				SectionPos.sectionToBlockCoord(x) - anchor.getX(),
				SectionPos.sectionToBlockCoord(y) - anchor.getY(),
				SectionPos.sectionToBlockCoord(z) - anchor.getZ());
		}
	}

	private record FluidSectionBuffer(BlockPos offset, SuperByteBuffer buffer) {
	}

	private record SolidFluidVertexConsumer(VertexConsumer delegate) implements VertexConsumer {

		@Override
		public VertexConsumer addVertex(float x, float y, float z) {
			delegate.addVertex(x, y, z);
			return this;
		}

		@Override
		public VertexConsumer setColor(int red, int green, int blue, int alpha) {
			delegate.setColor(red, green, blue, alpha == 0 ? 255 : alpha);
			return this;
		}

		@Override
		public VertexConsumer setUv(float u, float v) {
			delegate.setUv(u, v);
			return this;
		}

		@Override
		public VertexConsumer setUv1(int u, int v) {
			delegate.setUv1(u, v);
			return this;
		}

		@Override
		public VertexConsumer setUv2(int u, int v) {
			delegate.setUv2(u, v);
			return this;
		}

		@Override
		public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
			delegate.setNormal(normalX, normalY, normalZ);
			return this;
		}
        @Override public VertexConsumer setColor(int color) {delegate.setColor(color);return this;}
        @Override public VertexConsumer setUv3(float u,float v) {delegate.setUv3(u,v);return this;}
        @Override public VertexConsumer setLineWidth(float width) {delegate.setLineWidth(width);return this;}

	}

    private record RenderWorld(SchematicLevel level) implements net.minecraft.client.renderer.block.BlockAndTintGetter {
        public net.minecraft.world.level.CardinalLighting cardinalLighting() {return Minecraft.getInstance().level.cardinalLighting();}
        public int getBlockTint(BlockPos pos,net.minecraft.world.level.ColorResolver resolver) {return Minecraft.getInstance().level.getBlockTint(pos,resolver);}
        public net.minecraft.world.level.lighting.LevelLightEngine getLightEngine() {return level.getLightEngine();}
        public net.minecraft.world.level.block.entity.BlockEntity getBlockEntity(BlockPos pos) {return level.getBlockEntity(pos);}
        public BlockState getBlockState(BlockPos pos) {return level.getBlockState(pos);}
        public FluidState getFluidState(BlockPos pos) {return level.getFluidState(pos);}
        public int getHeight() {return level.getHeight();}
        public int getMinY() {return level.getMinY();}
    }
}
