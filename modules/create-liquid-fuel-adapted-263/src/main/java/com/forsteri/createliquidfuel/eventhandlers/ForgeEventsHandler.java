package com.forsteri.createliquidfuel.eventhandlers;

import com.forsteri.createliquidfuel.core.LiquidBurnerFuelJsonLoader;

import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public class ForgeEventsHandler {
    public static void addReloadListeners(AddServerReloadListenersEvent event) {
        // 26.2 orders reload listeners by id, so each one names itself
        event.addListener(LiquidBurnerFuelJsonLoader.IDENTIFIER, LiquidBurnerFuelJsonLoader.INSTANCE);
    }
}