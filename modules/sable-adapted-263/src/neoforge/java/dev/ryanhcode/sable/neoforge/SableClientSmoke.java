package dev.ryanhcode.sable.neoforge;

import dev.ryanhcode.sable.Sable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;

/** Explicit opt-in hooks for the isolated CLI runner; normal clients never auto-exit. */
final class SableClientSmoke {
    private static int ticks;
    private static boolean captured;
    private static boolean disconnecting;
    private static boolean sceneRequested;
    private SableClientSmoke() {}
    static void tick() {
        if (!Boolean.getBoolean("sable.clientSmokeTest")) return;
        var expected=java.nio.file.Path.of(System.getProperty("create263.clientProbeRoot", "run/disabled-client-probe")).toAbsolutePath().normalize();
        if(!net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().toAbsolutePath().normalize().equals(expected))throw new IllegalStateException("Client smoke guard rejected non-test directory");
        Minecraft mc = Minecraft.getInstance();
        if (System.getProperty("sable.clientSmokeWorld") != null) {
            if (disconnecting) {
                if (mc.gui.screen() instanceof TitleScreen && mc.gui.overlay() == null && ++ticks >= 40) {
                    Sable.LOGGER.info("Sable client world smoke saved and returned to title screen");
                    mc.stop();
                }
                return;
            }
            if (mc.level == null || mc.player == null || mc.gui.overlay() != null) return;
            if (++ticks == 1) {
                Sable.LOGGER.info("Sable client world smoke loaded at {} in {}", mc.player.blockPosition(), mc.level.dimension().identifier());
            }
            if (ticks == 180) Screenshot.grab(mc, false);
            if (ticks >= 240) {
                disconnecting = true;
                ticks = 0;
                mc.disconnectWithSavingScreen();
                mc.gui.setScreen(new TitleScreen());
            }
            return;
        }
        if (!(mc.gui.screen() instanceof TitleScreen) || mc.gui.overlay() != null) { ticks=0; return; }
        if (++ticks == 80 && !captured) {
            captured = true;
            Sable.LOGGER.info("Sable client smoke reached title screen after resource reload");
            Screenshot.grab(mc, false);
        }
        if (ticks >= 140) mc.stop();
    }
}
