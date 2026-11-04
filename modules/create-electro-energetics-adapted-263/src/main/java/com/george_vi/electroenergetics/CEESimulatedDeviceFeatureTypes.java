package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.devices.device.DeviceFeatureType;
import com.george_vi.electroenergetics.foundation.device.ElectricalDevice;
import com.george_vi.electroenergetics.foundation.device.SpawnPreventingDevice;
import com.george_vi.electroenergetics.foundation.device.TickingElectricalDevice;
import com.george_vi.electroenergetics.registry.DeferredRegister;
import com.george_vi.electroenergetics.registry.RegistryEntry;

public class CEESimulatedDeviceFeatureTypes {

    private static final DeferredRegister<DeviceFeatureType> DEVICE_FEATURE_TYPES =
            DeferredRegister.create(
                    CEERegistries.SIMULATED_DEVICE_FEATURE_TYPE, CreateElectroEnergetics.ID);

    public static final RegistryEntry<DeviceFeatureType, DeviceFeatureType> ELECTRICAL =
            DEVICE_FEATURE_TYPES.register(
                    "electrical", () -> new DeviceFeatureType(ElectricalDevice.class));

    public static final RegistryEntry<DeviceFeatureType, DeviceFeatureType> TICKING_ELECTRICAL =
            DEVICE_FEATURE_TYPES.register(
                    "ticking_electrical",
                    () -> new DeviceFeatureType(TickingElectricalDevice.class));

    public static final RegistryEntry<DeviceFeatureType, DeviceFeatureType> SPAWN_PREVENTING =
            DEVICE_FEATURE_TYPES.register(
                    "spawn_preventing", () -> new DeviceFeatureType(SpawnPreventingDevice.class));

    public static void register() {
        DEVICE_FEATURE_TYPES.register();
        DeviceFeatureType.freeze();
    }
}
