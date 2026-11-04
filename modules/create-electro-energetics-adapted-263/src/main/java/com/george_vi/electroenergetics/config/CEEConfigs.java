package com.george_vi.electroenergetics.config;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.google.gson.JsonObject;
import com.zurrtum.create.catnip.config.Builder;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CEEConfigs {
    private static CServer server;
    private static CClient client;
    public static final AtomicBoolean CHANGED = new AtomicBoolean();

    private CEEConfigs() {}

    public static CServer server() {
        if (server == null) register();
        return server;
    }

    public static CClient client() {
        if (client == null) register();
        return client;
    }

    public static synchronized void register() {
        ensureConfigFiles();
        if (server == null) server = Builder.create(CServer::new, CreateElectroEnergetics.ID, "server");
        if (client == null) client = Builder.create(CClient::new, CreateElectroEnergetics.ID, "client");
    }

    public static synchronized void reload() {
        register();
        server.applyRemote(null);
        server.prepareReload();
        client.prepareReload();
        Builder.create(() -> server, CreateElectroEnergetics.ID, "server");
        Builder.create(() -> client, CreateElectroEnergetics.ID, "client");
        CHANGED.set(true);
    }

    private static void ensureConfigFiles() {
        java.nio.file.Path directory = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir()
            .resolve(CreateElectroEnergetics.ID);
        try {
            java.nio.file.Files.createDirectories(directory);
            for (String side : new String[]{"server", "client"}) {
                java.nio.file.Path path = directory.resolve(side + ".json");
                if (!java.nio.file.Files.exists(path)) java.nio.file.Files.writeString(path, "{}",
                    java.nio.file.StandardOpenOption.CREATE_NEW);
            }
        } catch (java.io.IOException failure) {
            throw new IllegalStateException("Cannot create CEE configuration files in " + directory, failure);
        }
    }

    public static void applyServerConfig(JsonObject values) {
        server().applyRemote(values);
    }

    public static void clearServerConfig() {
        server().applyRemote(null);
    }
}
