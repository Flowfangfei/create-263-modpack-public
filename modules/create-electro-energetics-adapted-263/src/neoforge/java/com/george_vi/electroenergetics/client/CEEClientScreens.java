package com.george_vi.electroenergetics.client;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterBlockEntity;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterScreen;
import com.george_vi.electroenergetics.content.energy_meter.PanelEnergyMeterScreen;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.BaseEnergyMeterAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.TransformerAttachment;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerScreen;
import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
/** Screen implementation reached only from a physical-client execution branch. */
public final class CEEClientScreens {
    private CEEClientScreens() {}
    public static boolean canPlayerSet(java.util.function.Predicate<java.util.UUID> predicate) {
        var player = net.minecraft.client.Minecraft.getInstance().player;
        return player != null && predicate.test(player.getUUID());
    }
    public static void openEnergyMeter(EnergyMeterBlockEntity meter, Player player) {
        if (player instanceof LocalPlayer) ScreenOpener.open(new EnergyMeterScreen(meter));
    }
    public static void openPanelEnergyMeter(BaseEnergyMeterAttachment meter, Player player) {
        if (player instanceof LocalPlayer) ScreenOpener.open(new PanelEnergyMeterScreen(meter));
    }
    public static void openTransformer(TransformerAttachment transformer, Player player) {
        if (player instanceof LocalPlayer) ScreenOpener.open(new TransformerScreen(transformer));
    }
}
