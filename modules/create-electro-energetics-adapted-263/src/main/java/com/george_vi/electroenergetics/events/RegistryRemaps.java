package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.CEERegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import java.util.Map;

/** Identifiers used by CEE saves before the current names were introduced. */
public final class RegistryRemaps {
    private static final Map<String, String> LEGACY = Map.of(
        "electric_motor", "red_electric_motor",
        "miniature_ammeter", "ammeter",
        "miniature_voltmeter", "voltmeter",
        "miniature_indicator_bulb", "indicator_bulb",
        "miniature_momentary_switch", "momentary_switch");

    private RegistryRemaps() {}

    public static void register() {
        Identifier oldMotor = CreateElectroEnergetics.rl("electric_motor");
        Identifier redMotor = CreateElectroEnergetics.rl("red_electric_motor");
        BuiltInRegistries.BLOCK.addAlias(oldMotor, redMotor);
        BuiltInRegistries.ITEM.addAlias(oldMotor, redMotor);
        LEGACY.forEach((oldName, newName) -> {
            if (!oldName.equals("electric_motor")) CEERegistries.PANEL_ATTACHMENT_TYPE.addAlias(
                CreateElectroEnergetics.rl(oldName), CreateElectroEnergetics.rl(newName));
        });
    }

    public static Identifier canonical(Identifier identifier) {
        if (identifier == null || !identifier.getNamespace().equals(CreateElectroEnergetics.ID)) return identifier;
        return CreateElectroEnergetics.rl(LEGACY.getOrDefault(identifier.getPath(), identifier.getPath()));
    }
}
