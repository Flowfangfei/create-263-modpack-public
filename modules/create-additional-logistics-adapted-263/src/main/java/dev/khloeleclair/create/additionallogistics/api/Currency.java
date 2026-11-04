package dev.khloeleclair.create.additionallogistics.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Currency {

    @Nullable
    private static ICurrency.ICurrencyBackend BACKEND;

    @ApiStatus.Internal
    public static void setBackend(ICurrency.ICurrencyBackend backend) {
        BACKEND = backend;
    }

    public static void registerFormatter(Identifier id, ICurrency.IValueFormatter formatter) {
        Objects.requireNonNull(BACKEND);
        BACKEND.registerFormatter(id, formatter);
    }

    public static void registerCurrency(Identifier id, ICurrency currency) {
        Objects.requireNonNull(BACKEND);
        BACKEND.registerCurrency(id, currency);
    }

    public static ICurrencyBuilder builder(Identifier id) {
        Objects.requireNonNull(BACKEND);
        return BACKEND.newBuilder(id);
    }

    @Nullable
    static ICurrency get(Identifier id) {
        if (BACKEND == null)
            return null;
        return BACKEND.get(id);
    }

    @Nullable
    static ICurrency getForItem(Item item) {
        if (BACKEND == null)
            return null;
        return BACKEND.getForItem(item);
    }

}
