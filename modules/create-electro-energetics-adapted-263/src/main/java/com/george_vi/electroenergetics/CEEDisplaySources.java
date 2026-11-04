package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.energy_meter.ActivePowerDisplaySource;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterDisplaySource;
import com.george_vi.electroenergetics.content.gauge.AmperageDisplaySource;
import com.george_vi.electroenergetics.content.gauge.VoltageDisplaySource;
import com.george_vi.electroenergetics.registry.RegistryEntry;
import com.zurrtum.create.api.behaviour.display.DisplaySource;

import java.util.function.Supplier;

public class CEEDisplaySources {
    public static final RegistryEntry<DisplaySource, EnergyMeterDisplaySource> ENERGY_METER =
            simple("energy_meter", EnergyMeterDisplaySource::new);
    public static final RegistryEntry<DisplaySource, ActivePowerDisplaySource> WATTMETER =
            simple("wattmeter", ActivePowerDisplaySource::new);
    public static final RegistryEntry<DisplaySource, VoltageDisplaySource> VOLTAGE =
            simple("voltage", VoltageDisplaySource::new);
    public static final RegistryEntry<DisplaySource, AmperageDisplaySource> AMPERAGE =
            simple("amperage", AmperageDisplaySource::new);

    private static <T extends DisplaySource> RegistryEntry<DisplaySource, T> simple(
            String name, Supplier<T> supplier) {
        T source =
                net.minecraft.core.Registry.register(
                        com.zurrtum.create.api.registry.CreateRegistries.DISPLAY_SOURCE,
                        CreateElectroEnergetics.rl(name),
                        supplier.get());
        return new RegistryEntry<>(CreateElectroEnergetics.rl(name), () -> source);
    }

    public static void register() {}
}
