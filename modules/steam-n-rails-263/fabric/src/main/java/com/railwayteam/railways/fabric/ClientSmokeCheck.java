package com.railwayteam.railways.fabric;

import com.railwayteam.railways.Railways;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;

/** Explicit opt-in hooks for the isolated CLI runner; normal clients never auto-exit. */
final class ClientSmokeCheck {
    private static int ticks;
    private static boolean captured;
    private static boolean disconnecting;
    private static boolean sceneRequested;
    private ClientSmokeCheck() {}
    static void tick() {
        if (!Boolean.getBoolean("railways.clientSmokeTest")) return;
        Minecraft mc = Minecraft.getInstance();
        if (System.getProperty("railways.clientSmokeWorld") != null) {
            if (disconnecting) {
                if (mc.gui.screen() instanceof TitleScreen && mc.gui.overlay() == null && ++ticks >= 40) {
                    Railways.LOGGER.info("Railways client world smoke saved and returned to title screen");
                    mc.stop();
                }
                return;
            }
            if (mc.level == null || mc.player == null || mc.gui.overlay() != null) return;
            if (Boolean.getBoolean("railways.clientSmokeScene") && !sceneRequested) {
                sceneRequested = true;
                ClientSmokeScene.create(mc);
            }
            if (++ticks == 1) {
                checkConnectedSprites();
                Railways.LOGGER.info("Railways client world smoke loaded at {} in {}", mc.player.blockPosition(), mc.level.dimension().identifier());
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
            checkConnectedSprites();
            Railways.LOGGER.info("Railways client smoke reached title screen after resource reload");
            Screenshot.grab(mc, false);
        }
        if (ticks >= 140) mc.stop();
    }
    private static void checkConnectedSprites() {
        var shifts=java.util.List.of(com.railwayteam.railways.registry.CRSpriteShifts.FUEL_TANK,
            com.railwayteam.railways.registry.CRSpriteShifts.FUEL_TANK_TOP,
            com.railwayteam.railways.registry.CRSpriteShifts.FUEL_TANK_INNER);
        for(var shift:shifts) {
            if(shift.getTarget()==null || shift.getTarget().contents().name().equals(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation()))
                throw new IllegalStateException("Railways connected sprite missing: "+shift.getTargetIdentifier());
        }
        Railways.LOGGER.info("Railways connected fuel tank sprite checks passed");
    }
}
