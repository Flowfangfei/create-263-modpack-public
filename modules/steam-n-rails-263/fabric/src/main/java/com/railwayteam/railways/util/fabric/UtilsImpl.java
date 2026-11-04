package com.railwayteam.railways.util.fabric;

import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.HonkPacket;

import net.neoforged.fml.loading.FMLPaths;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.nio.file.Path;

public class UtilsImpl {
    public static Path configDir() {
        return FMLPaths.CONFIGDIR.get(); // NeoForge
    }

    public static boolean isDevEnv() {
        return !net.neoforged.fml.loading.FMLEnvironment.isProduction(); // NeoForge
    }

    public static void sendHonkPacket(Level level, Train train, boolean isHonk) {
        MinecraftServer server = level.getServer();
        if (server != null) {
            for (var player : server.getPlayerList().getPlayers()) net.createmod.catnip.platform.CatnipServices.NETWORK.sendToClient(player, new HonkPacket.Clientbound(train,isHonk));
        }
    }

    public static void postChunkEventClient(LevelChunk chunk, boolean load) {
    }

    public static Path modsDir() {
        return FMLPaths.MODSDIR.get(); // NeoForge
    }
}
