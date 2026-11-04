package com.tom.createores.compat;

import java.util.function.UnaryOperator;

/**
 * Registrate's old Create-only helper generated a conventional mineable tag.
 * The explicit block tags are retained by Registration, so this transform is
 * deliberately identity until the dedicated Create Fly datagen API is ported.
 */
public final class CreateFlyTagGen {
    private CreateFlyTagGen() {
    }

    public static <T> UnaryOperator<T> pickaxeOnly() {
        return value -> value;
    }
}
