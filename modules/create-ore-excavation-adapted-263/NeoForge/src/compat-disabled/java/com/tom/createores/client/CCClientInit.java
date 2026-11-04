package com.tom.createores.client;

import net.neoforged.bus.api.IEventBus;

/**
 * Build-time fallback while no CC:Tweaked artifact targeting NeoForge 21.11 is
 * available.  The full implementation remains in src/main/java and is used
 * when cc_tweaked_enabled is set with a compatible dependency.
 */
public final class CCClientInit {
    private CCClientInit() {
    }

    public static void init(IEventBus bus) {
    }
}
