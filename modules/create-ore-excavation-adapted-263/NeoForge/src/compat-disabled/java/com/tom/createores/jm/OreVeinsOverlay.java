package com.tom.createores.jm;

import net.minecraft.nbt.CompoundTag;

/**
 * JourneyMap is intentionally optional in this port.  Its full overlay is
 * compiled when the JourneyMap integration is enabled; this no-op endpoint
 * keeps the common sync packet safe when it is absent.
 */
public final class OreVeinsOverlay {
	private OreVeinsOverlay() {
	}

	public static void addOreInfoToMap(CompoundTag tag) {
	}
}
