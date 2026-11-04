package com.yision.fluidlogistics.content.processing.snowGolemFreezer;

import java.util.List;
import java.util.Map;

import com.yision.fluidlogistics.infrastructure.config.Config;
import com.yision.fluidlogistics.registry.AllBlocks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public class SnowGolemFreezerBlockItem extends BlockItem {

	private final boolean capturedSnowGolem;

	public static SnowGolemFreezerBlockItem empty(Properties properties) {
		return new SnowGolemFreezerBlockItem(AllBlocks.SNOW_GOLEM_FREEZER.get(), properties, false);
	}

	public static SnowGolemFreezerBlockItem withSnowGolem(Block block, Properties properties) {
		return new SnowGolemFreezerBlockItem(block, properties, true);
	}

	private SnowGolemFreezerBlockItem(Block block, Properties properties, boolean capturedSnowGolem) {
		super(block, capturedSnowGolem ? properties : properties.overrideDescription("item.fluidlogistics.empty_snow_golem_freezer"));
		this.capturedSnowGolem = capturedSnowGolem;
	}

	@Override
	public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {
		if (capturedSnowGolem)
			super.registerBlocks(blockToItemMap, item);
	}


	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (capturedSnowGolem || !Config.isSnowGolemFreezerEnabled()
			|| !(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof SpawnerBlockEntity blockEntity))
			return super.useOn(context);

		CompoundTag spawnerData;
        try (var reporter=new net.minecraft.util.ProblemReporter.ScopedCollector(org.slf4j.LoggerFactory.getLogger("fluidlogistics/spawner"))) {
            var output=net.minecraft.world.level.storage.TagValueOutput.createWithContext(reporter,context.getLevel().registryAccess());
            blockEntity.getSpawner().save(output);
            spawnerData=output.buildResult();
        }
		List<SpawnData> possibleSpawns = SpawnData.LIST_CODEC
			.parse(NbtOps.INSTANCE, spawnerData.getListOrEmpty("SpawnPotentials"))
			.getOrThrow().unwrap().stream().map(Weighted::value).toList();
		if (possibleSpawns.isEmpty())
			possibleSpawns = SpawnData.CODEC.parse(NbtOps.INSTANCE, spawnerData.getCompoundOrEmpty("SpawnData"))
				.result().stream().toList();
		for (SpawnData spawn : possibleSpawns) {
			if (!spawn.entityToSpawn().read("id",net.minecraft.resources.Identifier.CODEC).map(id->id.equals(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(net.minecraft.world.entity.EntityTypes.SNOW_GOLEM))).orElse(false))
				continue;
			Player player = context.getPlayer();
			if (!context.getLevel().isClientSide() && player != null) {
				giveFreezerItemTo(player, context.getItemInHand(), context.getHand());
				context.getLevel().playSound(null, context.getClickedPos(), SoundEvents.SNOW_GOLEM_AMBIENT,
					SoundSource.NEUTRAL, .5f, 1);
			}
			return InteractionResult.SUCCESS;
		}
		return super.useOn(context);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack heldItem, Player player, LivingEntity entity,
		InteractionHand hand) {
		if (capturedSnowGolem || !Config.isSnowGolemFreezerEnabled()
			|| !(entity instanceof SnowGolem) || !entity.isAlive())
			return InteractionResult.PASS;
		if (player.level().isClientSide())
			return InteractionResult.FAIL;

		giveFreezerItemTo(player, heldItem, hand);
		player.level().playSound(null, entity.blockPosition(), SoundEvents.SNOW_GOLEM_AMBIENT,
			SoundSource.NEUTRAL, .5f, 1);
		entity.discard();
		return InteractionResult.FAIL;
	}

	private void giveFreezerItemTo(Player player, ItemStack heldItem, InteractionHand hand) {
		ItemStack filled = AllBlocks.SNOW_GOLEM_FREEZER.asStack();
		if (!player.isCreative())
			heldItem.shrink(1);
		if (heldItem.isEmpty())
			player.setItemInHand(hand, filled);
		else
			player.getInventory().placeItemBackInInventory(filled,net.minecraft.util.Prediction.SERVER_ONLY);
	}

	public boolean hasCapturedSnowGolem() {
		return capturedSnowGolem;
	}
}
