package com.tom.createores.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.mojang.datafixers.util.Pair;

import com.tom.createores.Config;
import com.tom.createores.CreateOreExcavation;
import com.tom.createores.OreData;
import com.tom.createores.OreDataAttachment;
import com.tom.createores.OreVeinGenerator;
import com.tom.createores.Registration;
import com.tom.createores.components.OreVeinAtlasDataComponent;
import com.tom.createores.network.NetworkHandler;
import com.tom.createores.network.OreVeinInfoPacket;
import com.tom.createores.recipe.VeinRecipe;
import com.tom.createores.util.ComponentJoiner;
import com.tom.createores.util.RandomSpreadGenerator;

public class OreVeinFinderItem extends Item {

	public OreVeinFinderItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		if(!level.isClientSide())
			detect(level, player.blockPosition(), player);

		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		if(!ctx.getLevel().isClientSide())
			detect(ctx.getLevel(), ctx.getClickedPos(), ctx.getPlayer());

		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack pStack, TooltipContext p_339594_, TooltipDisplay display,
			java.util.function.Consumer<Component> tooltip, TooltipFlag p_41424_) {
		if (pStack.get(CreateOreExcavation.ORE_VEIN_FINDER_FILTERED_COMPONENT) == Boolean.TRUE) {
			tooltip.accept(Component.translatable("tooltip.coe.vein_finder.filtered"));
		}
	}

	@Override
	public void inventoryTick(ItemStack pStack, ServerLevel pLevel, Entity pEntity, net.minecraft.world.entity.EquipmentSlot slot) {
		if (!pLevel.isClientSide() && pEntity instanceof Player player && pLevel.getGameTime() % 20 == 10) {
			boolean hasAtlas = false;
			for (int i = 0;i < player.getInventory().getContainerSize(); i++) {
				ItemStack is = player.getInventory().getItem(i);
				if (is.getItem() == Registration.VEIN_ATLAS_ITEM.get()) {
					hasAtlas = true;
					break;
				}
			}
			pStack.set(CreateOreExcavation.ORE_VEIN_FINDER_FILTERED_COMPONENT, hasAtlas);
		}
	}

	@Override
	public boolean isFoil(ItemStack pStack) {
		return (pStack.get(CreateOreExcavation.ORE_VEIN_FINDER_FILTERED_COMPONENT) == Boolean.TRUE) || super.isFoil(pStack);
	}

	private void detect(Level level, BlockPos pos, Player player) {
		ItemStack atlas = ItemStack.EMPTY;
		for (int i = 0;i < player.getInventory().getContainerSize(); i++) {
			ItemStack is = player.getInventory().getItem(i);
			if (is.getItem() == Registration.VEIN_ATLAS_ITEM.get()) {
				atlas = is;
				break;
			}
		}
		var atlasData = atlas.get(CreateOreExcavation.ORE_VEIN_ATLAS_DATA_COMPONENT);
		Predicate<RecipeHolder<VeinRecipe>> filter = atlasData != null ? makeFilter(atlasData) : a -> true;

		ChunkPos center = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
		OreData found = null;
		List<OreData> nearby = new ArrayList<>();
		int near = Config.veinFinderNear;
		for(int x = -near;x <= near;x++) {
			for(int z = -near;z <= near;z++) {
				OreData d = OreDataAttachment.getData(level.getChunk(center.x() + x, center.z() + z));
				if(x == 0 && z == 0)found = d;
				else nearby.add(d);
			}
		}
		com.tom.createores.util.PlayerMessages.show(player, Component.translatable("chat.coe.veinFinder.info"), false);
		com.tom.createores.util.PlayerMessages.show(player, Component.translatable("chat.coe.veinFinder.pos", center.x(), center.z()), false);
		if (!(level instanceof ServerLevel serverLevel))
			return;
		RecipeManager m = serverLevel.getServer().getRecipeManager();
		Component f;
		Component nothing = Component.translatable("chat.coe.veinFinder.nothing");
		Component comma = Component.literal(", ");
		if(found != null && found.getRecipe(m) != null)f = found.getRecipe(m).value().getName();
		else f = nothing;
		com.tom.createores.util.PlayerMessages.show(player, Component.translatable("chat.coe.veinFinder.found", f), false);

		CompoundTag infoTag = new CompoundTag();
		Identifier id = found.getRecipeId();
		if (id != null)infoTag.putString("found", id.toString());
		infoTag.putInt("x", pos.getX());
		infoTag.putInt("z", pos.getZ());

		Identifier rl = nearby.stream().map(d -> d.getRecipe(m)).filter(r -> r != null).map(r -> r.id().identifier()).findFirst().orElse(null);
		if (rl != null) {
			infoTag.putString("nearby", rl.toString());
		}

		f = nearby.stream().map(d -> d.getRecipe(m)).filter(r -> r != null).map(r -> r.value().getName()).collect(ComponentJoiner.joining(nothing, comma));
		com.tom.createores.util.PlayerMessages.show(player, Component.translatable("chat.coe.veinFinder.nearby", f), false);

		Pair<BlockPos, RecipeHolder<VeinRecipe>> nearest = OreVeinGenerator.getPicker((ServerLevel) level).locate(pos, (ServerLevel) level, 16, filter);
		if(nearest != null) {
			BlockPos at = nearest.getFirst();
			int i = Math.round(RandomSpreadGenerator.distance2d(at, pos) / Config.veinFinderFar) * Config.veinFinderFar;
			com.tom.createores.util.PlayerMessages.show(player, Component.translatable("chat.coe.veinFinder.far", Component.translatable("chat.coe.veinFinder.distance", nearest.getSecond().value().getName(), i)), false);
			infoTag.putString("far", nearest.getSecond().id().toString());
			infoTag.putInt("dist", i);
		} else {
			com.tom.createores.util.PlayerMessages.show(player, Component.translatable("chat.coe.veinFinder.far", nothing), false);
		}
		NetworkHandler.sendTo((ServerPlayer) player, new OreVeinInfoPacket(infoTag));
		player.getCooldowns().addCooldown(player.getItemInHand(InteractionHand.MAIN_HAND), Config.veinFinderCd);
	}

	private Predicate<RecipeHolder<VeinRecipe>> makeFilter(OreVeinAtlasDataComponent atlasTag) {
		if (atlasTag.target().isPresent()) {
			var t = atlasTag.target().get();
		return v -> t.equals(v.id().identifier());
		}
		Set<Identifier> exclude = new HashSet<>(atlasTag.exclude());
		if (exclude.isEmpty())return a -> true;
		return v -> {
			return !exclude.contains(v.id().identifier());
		};
	}
}
