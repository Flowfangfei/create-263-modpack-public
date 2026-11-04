package com.george_vi.electroenergetics;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public class CEEDamageTypes {
    public static final ResourceKey<DamageType> ELECTROCUTION = key("electrocution"),
            HV_ELECTROCUTION = key("hv_electrocution");

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, CreateElectroEnergetics.rl(name));
    }

    public static void bootstrap(BootstrapContext<DamageType> ctx) {
        ctx.register(
                ELECTROCUTION,
                new DamageType("electroenergetics.electrocution", DamageScaling.NEVER, 0.1f));
        ctx.register(
                HV_ELECTROCUTION,
                new DamageType("electroenergetics.hv_electrocution", DamageScaling.NEVER, 0.1f));
    }
}
