package com.tom.createores.client;
// Made with Blockbench 4.2.5
// Exported for Minecraft version 1.17 - 1.18 with Mojang mappings
// Paste this class into your mod and generate all required imports

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tom.createores.CreateOreExcavation;
import com.tom.createores.block.entity.IDrill;

public class DrillRenderer<T extends BlockEntity & IDrill> implements BlockEntityRenderer<T, DrillRenderer.DrillRenderState> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "drill"), "main");
	private static final Identifier SHAFT = Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "textures/entity/shaft.png");
	private final ModelPart head;
	private final ModelPart shaft;
	private final ModelPart rubble;

	public DrillRenderer(BlockEntityRendererProvider.Context dispatcher) {
		ModelPart root = dispatcher.bakeLayer(LAYER_LOCATION);
		this.head = root.getChild("head");
		this.shaft = root.getChild("shaft");
		this.rubble = root.getChild("rubble");
	}

	public static LayerDefinition createModel() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		head.addOrReplaceChild("Bit4_r1", CubeListBuilder.create().texOffs(20, 26).addBox(-1.0F, 6.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(12, 16).addBox(-1.5F, 4.5F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(12, 10).addBox(-2.0F, 2.5F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 24).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 16).addBox(-5.0F, -2.5F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 16).addBox(3.0F, -2.5F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 10).addBox(-2.0F, -2.5F, -5.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 10).addBox(-2.0F, -2.5F, 3.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-3.0F, -2.5F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.5F, 0.0F, 0.0F, 0.7854F, 0.0F));

		partdefinition.addOrReplaceChild("shaft", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition rubble  = partdefinition.addOrReplaceChild("rubble", CubeListBuilder.create(), PartPose.offset(1.0F, 24.0F, 0.0F));

		rubble.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 22).addBox(-2.2F, -2.8F, -1.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, -0.2486F, -0.1618F, 0.1217F));

		rubble.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 13).addBox(-8.0F, -3.0F, -1.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, -0.0598F, 0.0156F, -0.1786F));

		rubble.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 6).addBox(-9.0F, -3.0F, -7.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 0.3412F, 0.1265F, -0.0937F));

		rubble.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -3.0F, -7.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 0.3414F, -0.0741F, 0.2054F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public DrillRenderState createRenderState() {
		return new DrillRenderState();
	}

	@Override
	public void extractRenderState(T blockEntity, DrillRenderState state, float partialTick, net.minecraft.world.phys.Vec3 cameraPos,
			CrumblingOverlay breakingProgress) {
		BlockEntityRenderState.extractBase(blockEntity, state, breakingProgress);
		state.facing = blockEntity.getFacing();
		state.yOffset = blockEntity.getYOffset();
		state.drillOffset = blockEntity.getDrillOffset();
		state.rotation = Mth.lerp(partialTick, blockEntity.getPrevRotation(), blockEntity.getRotation());
		state.renderShaft = blockEntity.shouldRenderShaft();
		state.renderRubble = blockEntity.shouldRenderRubble() && blockEntity.getLevel() != null;
		ItemStack drill = blockEntity.getDrill();
		state.drillTexture = drill.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(drill.getItem());
		if (state.renderRubble) {
			BlockPos below = blockEntity.getBelow();
			BlockState belowState = blockEntity.getLevel().getBlockState(below);
			state.rubbleTexture = belowState.isAir() ? null : PlatformClient.getBlockTexture(belowState, blockEntity.getLevel(), below);
		}
	}

	@Override
	public void submit(DrillRenderState state, PoseStack pPoseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		pPoseStack.pushPose();
		float f = state.facing.toYRot();
		pPoseStack.translate(0.5D, 0.5D, 0.5D);
		pPoseStack.rotate(Axis.YP.rotationDegrees(-f));
		pPoseStack.translate(0, state.yOffset, 0);
		pPoseStack.scale(1, -1, -1);

		pPoseStack.pushPose();
		pPoseStack.translate(0, state.drillOffset, 0);
		pPoseStack.rotate(Axis.YP.rotationDegrees(state.rotation));

		if (state.renderShaft)
			collector.submitModelPart(shaft, pPoseStack, RenderTypes.entityCutout(SHAFT), state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, null);

		if (state.drillTexture != null) {
			Identifier tex = Identifier.fromNamespaceAndPath(state.drillTexture.getNamespace(), "textures/entity/drill/" + state.drillTexture.getPath() + ".png");
			collector.submitModelPart(head, pPoseStack, RenderTypes.entityCutout(tex), state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, null);
		}

		pPoseStack.popPose();

		if (state.rubbleTexture != null)
			collector.submitModelPart(rubble, pPoseStack, RenderTypes.cutoutMovingBlock(), state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, state.rubbleTexture);
		pPoseStack.popPose();
	}

	public static class DrillRenderState extends BlockEntityRenderState {
		private net.minecraft.core.Direction facing = net.minecraft.core.Direction.NORTH;
		private float yOffset;
		private float drillOffset;
		private float rotation;
		private boolean renderShaft;
		private boolean renderRubble;
		private Identifier drillTexture;
		private TextureAtlasSprite rubbleTexture;
	}
}
