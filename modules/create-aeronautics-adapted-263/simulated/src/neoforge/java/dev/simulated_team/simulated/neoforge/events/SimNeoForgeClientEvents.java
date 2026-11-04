package dev.simulated_team.simulated.neoforge.events;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterItemBindHandler;
import dev.simulated_team.simulated.events.SimulatedCommonClientEvents;
import dev.simulated_team.simulated.index.SimClickInteractions;
import dev.simulated_team.simulated.index.SimItems;
import dev.simulated_team.simulated.index.SimKeys;
import dev.simulated_team.simulated.neoforge.service.SimpleResourceManagerRegistryService;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import dev.simulated_team.simulated.util.hold_interaction.HoldInteractionManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.minecraft.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class SimNeoForgeClientEvents {

	@SubscribeEvent
	public static void keyInput(final InputEvent.InteractionKeyMappingTriggered event) {
		if (event.isUseItem()) {
			if (SimulatedCommonClientEvents.useItemMappingTriggered()) {
				event.setCanceled(true);
				event.setSwingHand(false);
			}
		}
	}

	@SubscribeEvent
	public static void useItemOnBlock(final UseItemOnBlockEvent event) {
		if (event.getLevel().isClientSide()) {
			if (event.getPlayer() != null && event.getUsePhase() == UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK) {
				if (SimulatedCommonClientEvents.useItemOnBlockEvent(event.getLevel(), event.getPlayer(), event.getItemStack(), event.getHand())) {
					event.cancelWithResult(InteractionResult.CONSUME);
				}
			}

			useItemOnBlockClient(event);
		}
	}

	@SubscribeEvent
	public static void onLivingEntityUseItem(final PlayerInteractEvent.RightClickItem event) {
		final LivingEntity entity = event.getEntity();
		if (entity instanceof final Player player && player.isLocalPlayer()) {
			SimulatedCommonClientEvents.useItemOnAirEvent(entity.level(), player, event.getItemStack(), event.getHand());
		}
	}


	private static void useItemOnBlockClient(final UseItemOnBlockEvent event) {
		if (event.getPlayer() != null && event.getPlayer().isLocalPlayer() && HoldInteractionManager.isActive()) {
			event.setCanceled(true);
		}
	}



	@SubscribeEvent
	public static void preClientTick(final ClientTickEvent.Pre event) {
		SimulatedCommonClientEvents.preClientTick(Minecraft.getInstance());
	}

	@SubscribeEvent
	public static void postClientTick(final ClientTickEvent.Post event) {
		SimulatedCommonClientEvents.postClientTick(Minecraft.getInstance());
	}

	@SubscribeEvent
	public static void postRenderGui(final RenderGuiEvent.Post event) {
		SimulatedCommonClientEvents.renderOverlays(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
	}

	@SubscribeEvent
	public static void keyInput(final InputEvent.Key event) {
		SimulatedCommonClientEvents.onAfterKeyPress(event.getKey(), event.getKeyEvent().keycode(), event.getAction(), event.getModifiers());
	}

	@SubscribeEvent
	public static void postMouseButtonInput(final InputEvent.MouseButton.Post event) {
		SimulatedCommonClientEvents.onAfterMouseInput(event.getButton(), event.getModifiers(), event.getAction());
	}

	@SubscribeEvent
	public static void playerInteractRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
		if (event.getEntity().isLocalPlayer()) {
			final InteractionResult res = SimulatedCommonClientEvents.onRightClickBlock(event.getEntity(), event.getHand(), event.getPos(), event.getHitVec());

			if (res != null) {
				event.setCancellationResult(res);
				event.setCanceled(true);
				return;
			}
		}

		if (event.getItemStack().is(SimItems.HONEY_GLUE)) {
			event.setUseBlock(TriState.FALSE);
			if (event.getLevel().isClientSide()) {
				SimClickInteractions.HONEY_GLUE_MANAGER.selectPos(event.getPos(), event.getEntity(), event.getItemStack());
			}
			event.setCancellationResult(InteractionResult.SUCCESS);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void itemTooltip(final ItemTooltipEvent event) {
		SimulatedCommonClientEvents.appendTooltip(event.getItemStack(), event.getFlags(), event.getEntity(), event.getToolTip());
	}

	public static class ModBusEvents {

		@SubscribeEvent
		public static void registerKeyMappings(final RegisterKeyMappingsEvent event) {
			SimKeys.registerTo(event::register);
		}

		@SubscribeEvent
		public static void registerGuiLayers(final RegisterGuiLayersEvent event) {
			// This upstream branch has no binding overlay; its handler is intentionally empty.
		}

		@SubscribeEvent
		public static void addReloadListener(final AddClientReloadListenersEvent event) {
            int index=0;
            for(final PreparableReloadListener listener: SimpleResourceManagerRegistryService.LISTENERS)
                event.addListener(Simulated.path("simple_resource_"+(index++)),listener);
		}
	}
}
