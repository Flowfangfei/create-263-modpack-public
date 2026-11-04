package com.tterrag.registrate.fabric;

import net.fabricmc.api.EnvType;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.FMLEnvironment;
import java.util.function.Supplier;

public class EnvExecutor {
	public static void runWhenOn(EnvType env, Supplier<Runnable> toRun) {
		if (FabricLoader.getInstance().getEnvironmentType() == env) {
			toRun.get().run();
		}
	}
}
