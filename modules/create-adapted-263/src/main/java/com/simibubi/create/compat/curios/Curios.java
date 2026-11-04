package com.simibubi.create.compat.curios;

import java.util.Collections;
import java.util.Optional;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.equipment.goggles.GogglesItem;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class Curios {

	private static Optional<ICuriosItemHandler> resolveCurios(LivingEntity entity) {
		return Optional.ofNullable(entity.getCapability(CuriosCapability.INVENTORY));
	}

	public static void init(IEventBus modEventBus) {
		modEventBus.addListener(Curios::onClientSetup);

		GogglesItem.addIsWearingPredicate(player -> resolveCurios(player)
			.map(handler -> handler.findFirstCurio(AllItems.GOGGLES::isIn).isPresent())
			.orElse(false));

		BacktankUtil.addBacktankSupplier(entity -> resolveCurios(entity)
			.map(handler -> handler.findCurios(AllTags.AllItemTags.PRESSURIZED_AIR_SOURCES::matches)
				.stream().map(SlotResult::stack).toList())
			.orElse(Collections.emptyList()));

		CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> modEventBus.addListener(CuriosRenderers::onLayerRegister));
	}

	private static void onClientSetup(final FMLClientSetupEvent event) {
		CuriosRenderers.register();
	}
}
