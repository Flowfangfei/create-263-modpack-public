package com.tom.createores.client;

import net.createmod.ponder.api.client.PonderIndex;

public class ClientRegistration {

	public static void register() {
		PonderIndex.addPlugin(new COEPonderPlugin());
	}
}
