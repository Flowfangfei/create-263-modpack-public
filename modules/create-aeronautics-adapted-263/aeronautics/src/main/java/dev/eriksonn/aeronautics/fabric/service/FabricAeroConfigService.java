package dev.eriksonn.aeronautics.fabric.service;

import com.simibubi.create.api.stress.BlockStressValues;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.config.AeroConfig;
import dev.eriksonn.aeronautics.config.client.AeroClient;
import dev.eriksonn.aeronautics.config.server.AeroServer;

public final class FabricAeroConfigService implements AeroConfig {
    private static AeroServer server;
    private static AeroClient client;

    public static void register() {
        server = Builder.create(AeroServer::new, Aeronautics.MOD_ID, "server");
        client = Builder.create(AeroClient::new, Aeronautics.MOD_ID, "client");
        BlockStressValues.IMPACTS.registerProvider(server.kinetics.stressValues::getImpact);
        BlockStressValues.CAPACITIES.registerProvider(server.kinetics.stressValues::getCapacity);
    }

    @Override
    public AeroServer getServerConfig() {
        return server;
    }

    @Override
    public AeroClient getClientConfig() {
        return client;
    }
}
