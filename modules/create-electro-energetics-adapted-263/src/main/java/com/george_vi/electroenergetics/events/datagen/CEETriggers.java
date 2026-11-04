package com.george_vi.electroenergetics.events.datagen;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registers trigger identities before registry freeze without constructing item-stack icons. */
public final class CEETriggers {
    private static final Map<String, SimpleCEETrigger> TRIGGERS = new LinkedHashMap<>();
    static {
        for (String name : new String[]{"connect_wires", "short_circuit", "blown_fuse", "estop",
            "fuse_bypass", "fuse_streak", "energy_meter_total"})
            TRIGGERS.put(name + "_builtin", new SimpleCEETrigger(name + "_builtin"));
    }

    private CEETriggers() {}

    public static SimpleCEETrigger get(String name) {
        SimpleCEETrigger trigger = TRIGGERS.get(name);
        if (trigger == null) throw new IllegalArgumentException("Unregistered CEE advancement trigger: " + name);
        return trigger;
    }

    public static void register() {
        TRIGGERS.forEach((name, trigger) -> Registry.register(BuiltInRegistries.TRIGGER_TYPES,
            CreateElectroEnergetics.rl(name), trigger));
    }
}
