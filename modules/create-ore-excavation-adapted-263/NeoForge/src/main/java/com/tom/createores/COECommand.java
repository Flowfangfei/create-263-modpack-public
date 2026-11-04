package com.tom.createores;

import java.util.stream.Stream;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import com.google.common.base.Stopwatch;

import com.tom.createores.recipe.VeinRecipe;
import com.tom.createores.util.RandomSpreadGenerator;

public class COECommand {
	private static final DynamicCommandExceptionType ERROR_VEIN_NOT_FOUND = new DynamicCommandExceptionType((p_214514_) -> {
		return Component.translatable("command.coe.locate.failed", p_214514_);
	});

	public static void init() {}

	public static void register(RegisterCommandsEvent evt) {
		LiteralArgumentBuilder<CommandSourceStack> l = Commands.literal("coe");
		l.then(Commands.literal("setvein").requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)).
				then(Commands.argument("pos", BlockPosArgument.blockPos()).
						then(Commands.argument("recipe", IdentifierArgument.id()).suggests(ALL_RECIPES).
								executes(c -> {
									BlockPos p = BlockPosArgument.getLoadedBlockPos(c, "pos");
									RecipeHolder<?> rl = getRecipe(c);
									if(rl != null && rl.value() instanceof VeinRecipe) {
										setVein(c.getSource(), p, rl.id().identifier(), 0.8F);
										c.getSource().sendSuccess(() -> Component.translatable("command.coe.setvein.success", rl.id().identifier().toString()), true);
										return 1;
									}
									return 0;
								}).
								then(Commands.argument("multiplier", FloatArgumentType.floatArg(0, 1000)).
										executes(c -> {
											float mul = FloatArgumentType.getFloat(c, "multiplier");
											BlockPos p = BlockPosArgument.getLoadedBlockPos(c, "pos");
											RecipeHolder<?> rl = getRecipe(c);
											if(rl != null && rl.value() instanceof VeinRecipe) {
											setVein(c.getSource(), p, rl.id().identifier(), mul);
											c.getSource().sendSuccess(() -> Component.translatable("command.coe.setvein.success", rl.id().identifier().toString()), true);
												return 1;
											}
											return 0;
										})
										)
								)
						)
				);
		l.then(Commands.literal("removevein").requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)).
				then(Commands.argument("pos", BlockPosArgument.blockPos()).
						executes(c -> {
							BlockPos p = BlockPosArgument.getLoadedBlockPos(c, "pos");
							setVein(c.getSource(), p, null, 0F);
							c.getSource().sendSuccess(() -> Component.translatable("command.coe.setvein.success", Component.translatable("chat.coe.veinFinder.nothing")), true);
							return 1;
						})
						)
				);
		l.then(Commands.literal("locate").requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)).
				then(Commands.argument("recipe", IdentifierArgument.id()).suggests(ALL_RECIPES).
						executes(c -> {
							RecipeHolder<?> rl = getRecipe(c);
							if(rl != null && rl.value() instanceof VeinRecipe) {
								BlockPos blockpos = BlockPos.containing(c.getSource().getPosition());
								Stopwatch stopwatch = Stopwatch.createStarted();
								BlockPos at = OreVeinGenerator.getPicker(c.getSource().getLevel()).locate(rl.id().identifier(), blockpos, c.getSource().getLevel(), 100);
								stopwatch.stop();
								if(at != null) {
									int i = Mth.floor(RandomSpreadGenerator.distance2d(at, blockpos));
									Component component = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", at.getX(), "~", at.getZ())).withStyle(tc -> {
									return tc.withColor(ChatFormatting.GREEN);
									});
									c.getSource().sendSuccess(() -> {
									return Component.translatable("command.coe.locate.success", rl.id().identifier().toString(), component, i);
									}, false);
								CreateOreExcavation.LOGGER.info("Locating element " + rl.id().identifier() + " took " + stopwatch.elapsed().toMillis() + " ms");
									return i;
								} else {
									throw ERROR_VEIN_NOT_FOUND.create(rl.id().identifier().toString());
								}
							} else {
								throw ERROR_VEIN_NOT_FOUND.create(IdentifierArgument.getId(c, "recipe").toString());
							}
						})
						)
				);
		evt.getDispatcher().register(l);
	}

	private static void setVein(CommandSourceStack css, BlockPos pos, Identifier rl, float mul) {
		ChunkPos p = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
		var chunk = css.getLevel().getChunk(p.x(), p.z());
		OreData data = OreDataAttachment.getData(chunk);
		data.setRecipe(rl);
		data.setLoaded(true);
		data.setRandomMul(mul);
		data.setExtractedAmount(0);
		chunk.markUnsaved();
	}

	private static RecipeHolder<?> getRecipe(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
		Identifier id = IdentifierArgument.getId(context, "recipe");
		return context.getSource().getLevel().recipeAccess().byKey(CreateOreExcavation.recipeKey(id)).orElse(null);
	}

	public static final SuggestionProvider<CommandSourceStack> ALL_RECIPES = SuggestionProviders.register(Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "all_recipes"), (ctx, builder) -> {
		// Command suggestions are also queried by generic client sources that do not
		// expose a recipe manager in 1.21.11. The command itself resolves and validates
		// the recipe on the server in getRecipe().
		Stream<Identifier> rl = Stream.empty();

		return SharedSuggestionProvider.suggestResource(rl, builder);
	});
}
