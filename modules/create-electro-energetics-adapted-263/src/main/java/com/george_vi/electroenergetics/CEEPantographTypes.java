package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographType;
import com.george_vi.electroenergetics.registry.DeferredRegister;
import com.george_vi.electroenergetics.registry.RegistryEntry;

public class CEEPantographTypes {
    private static final DeferredRegister<PantographType> PANTOGRAPH_TYPE =
            DeferredRegister.create(CEERegistries.PANTOGRAPH_TYPE, CreateElectroEnergetics.ID);

    public static final RegistryEntry<PantographType, PantographType> STANDARD =
            PANTOGRAPH_TYPE.register("standard", () -> new PantographType(3.25f, 0.25f, 0.5f, 2f));
    public static final RegistryEntry<PantographType, PantographType> DOUBLE =
            PANTOGRAPH_TYPE.register("double", () -> new PantographType(3.25f, 0.5f, 0.5f, 2f));
    public static final RegistryEntry<PantographType, PantographType> RAIL_CONTACT_SHOE =
            PANTOGRAPH_TYPE.register(
                    "rail_contact_shoe", () -> new PantographType(1f, 0, -0.75f, 0.5f));

    public static void register() {
        PANTOGRAPH_TYPE.register();
    }
}
