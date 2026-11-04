package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.foundation.DielectricMobEffect;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class CEEMobEffects {
    public static final Holder<MobEffect> DIELECTRIC =
            Registry.registerForHolder(
                    BuiltInRegistries.MOB_EFFECT,
                    CreateElectroEnergetics.rl("dielectric"),
                    new DielectricMobEffect(MobEffectCategory.BENEFICIAL, 0xF5D933));

    public static void register() {}
}
