package com.forsteri.createliquidfuel.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record LiquidFuel(int burnTime, boolean superHeat, int amountConsumedPerTick) {
    public LiquidFuel {
        if (burnTime <= 0 || amountConsumedPerTick <= 0) throw new IllegalArgumentException("Fuel values must be positive");
    }
    public static final Codec<LiquidFuel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("superHeat", false).forGetter(LiquidFuel::superHeat),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("burnTime").forGetter(fuel -> Optional.of(fuel.burnTime())),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("amountConsumedPerTick").forGetter(fuel -> Optional.of(fuel.amountConsumedPerTick()))
    ).apply(instance, (superHeat, burnTime, amountConsumedPerTick) -> new LiquidFuel(
            burnTime.orElse(superHeat ? 32 : 20),
            superHeat,
            amountConsumedPerTick.orElse(superHeat ? 10 : 1)
    )));
}
