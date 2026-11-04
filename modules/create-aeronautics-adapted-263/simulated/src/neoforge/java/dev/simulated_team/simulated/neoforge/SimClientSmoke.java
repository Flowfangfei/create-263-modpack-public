package dev.simulated_team.simulated.neoforge;

import dev.simulated_team.simulated.Simulated;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

/** Opt-in smoke probe for this project's isolated client, with an optional owned world copy. */
final class SimClientSmoke {
    private static final Path GAME_DIR = Path.of(System.getProperty("create263.clientProbeRoot", "run/disabled-client-probe")).toAbsolutePath().normalize();
    private static final String WORLD = "aeronautics-probe-copy";
    private static int ticks;
    private static boolean captured;
    private static boolean disconnecting;
    private static boolean checkedMods;
    private static volatile boolean observedAircraft;

    private SimClientSmoke() {}

    static void tick() {
        if (!Boolean.getBoolean("simulated.clientSmokeTest")) return;
        requirePath(FMLPaths.GAMEDIR.get(), GAME_DIR, "game directory");
        final String world = System.getProperty("simulated.clientSmokeWorld");
        if (world != null && !WORLD.equals(world)) {
            throw new IllegalStateException("Client smoke guard rejected unexpected world: " + world);
        }
        if (!checkedMods) {
            for (String mod : new String[] {"create", "sable", "simulated", "offroad", "aeronautics"}) {
                if (!ModList.get().isLoaded(mod)) throw new IllegalStateException("Client smoke missing mod: " + mod);
            }
            checkedMods = true;
        }

        final Minecraft mc = Minecraft.getInstance();
        if (world != null) {
            if (disconnecting) {
                if (!(mc.gui.screen() instanceof TitleScreen) || mc.gui.overlay() != null
                        || mc.level != null || mc.getSingleplayerServer() != null) {
                    ticks = 0;
                    return;
                }
                if (++ticks == 40) {
                    Simulated.LOGGER.info("Aeronautics client world smoke saved and returned to title screen");
                    Simulated.LOGGER.info("Aeronautics client smoke reached title screen after resource reload");
                    Screenshot.grab(mc, false);
                }
                if (ticks >= 100) mc.stop();
                return;
            }
            if (mc.level == null || mc.player == null || mc.gui.overlay() != null) return;
            final var server = mc.getSingleplayerServer();
            if (server == null) throw new IllegalStateException("Client smoke requires the isolated integrated server");
            requirePath(server.getWorldPath(LevelResource.ROOT), GAME_DIR.resolve("saves").resolve(WORLD), "world directory");
            if (++ticks == 1) {
                Simulated.LOGGER.info("Aeronautics client world smoke loaded at {} in {}", mc.player.blockPosition(), mc.level.dimension().identifier());
            }
            if (ticks >= 20 && ticks % 5 == 0) {
                final var playerId=mc.player.getUUID();
                server.execute(()->{
                    var level=server.overworld();
                    var body=dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level).getAllSubLevels().stream()
                        .filter(x->"aeronautics-adapted-propulsion-probe".equals(x.getName())).findFirst().orElse(null);
                    var viewer=server.getPlayerList().getPlayer(playerId);
                    if(body==null||viewer==null)return;
                    if(!observedAircraft)Simulated.LOGGER.info("Aeronautics client smoke following saved aircraft {}",body.getUniqueId());
                    observedAircraft=true;
                    viewer.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.CREATIVE);
                    viewer.getAbilities().flying=true;viewer.onUpdateAbilities();
                    var pos=body.logicalPose().position();
                    viewer.teleportTo(level,pos.x()+8,pos.y()+2,pos.z()+8,java.util.Set.of(),135f,18f,true);
                });
            }
            if (ticks == 100) {
                var clientBodies = dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(mc.level);
                for (var clientBody : clientBodies.getAllSubLevels()) {
                    for (var holder : clientBody.getPlot().getLoadedChunks()) {
                        Simulated.LOGGER.info("Client plot BE probe: body={}, chunk={}, entities={}", clientBody.getUniqueId(), holder.getChunk().getPos(), holder.getChunk().getBlockEntities().size());
                        for (var be : holder.getChunk().getBlockEntities().values()) {
                            var renderer = mc.getBlockEntityRenderDispatcher().getRenderer(be);
                            Simulated.LOGGER.info("Client plot renderer probe: be={}, pos={}, removed={}, hasLevel={}, renderer={}", be.getClass().getSimpleName(), be.getBlockPos(), be.isRemoved(), be.hasLevel(), renderer == null ? "missing" : renderer.getClass().getName());
                        }
                    }
                }
            }
            if (ticks == 180) {
                if(!observedAircraft)throw new IllegalStateException("Saved test aircraft was not restored in the owned world copy");
                Simulated.LOGGER.info("Aeronautics client world smoke capturing world screenshot");
                Screenshot.grab(mc, false);
            }
            if (ticks >= 240) {
                disconnecting = true;
                ticks = 0;
                mc.disconnectWithSavingScreen();
                mc.gui.setScreen(new TitleScreen());
            }
            return;
        }

        if (!(mc.gui.screen() instanceof TitleScreen) || mc.gui.overlay() != null) {
            ticks = 0;
            return;
        }
        if (++ticks == 80 && !captured) {
            captured = true;
            Simulated.LOGGER.info("Aeronautics client smoke reached title screen after resource reload");
            Screenshot.grab(mc, false);
        }
        if (ticks >= 140) mc.stop();
    }

    private static void requirePath(Path actual, Path expected, String kind) {
        try {
            if (!actual.toRealPath().equals(expected.toAbsolutePath().normalize())) {
                throw new IllegalStateException("Client smoke guard rejected non-test " + kind + ": " + actual);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Client smoke cannot validate " + kind, exception);
        }
    }
}