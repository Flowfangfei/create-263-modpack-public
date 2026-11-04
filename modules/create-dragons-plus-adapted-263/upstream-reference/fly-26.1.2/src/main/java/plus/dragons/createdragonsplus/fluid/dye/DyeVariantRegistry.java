/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.fluid.dye;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.resources.Identifier;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.api.CreateDragonsPlusPlugin;

public final class DyeVariantRegistry {
    public static final String ENTRYPOINT = "create_dragons_plus";
    private static final AtomicBoolean FROZEN = new AtomicBoolean();
    private static volatile List<DyeVariant> all = List.of();
    private static volatile Map<Identifier, DyeVariant> byId = Map.of();
    private static volatile Map<Identifier, Integer> order = Map.of();

    private DyeVariantRegistry() {}

    public static void bootstrap() {
        if (isFrozen()) {
            return;
        }
        Builder builder = new Builder();
        builder.provider = CreateDragonsPlus.ID;
        DyeColors.registerVanilla(builder);
        FabricLoader.getInstance().getEntrypointContainers(ENTRYPOINT, CreateDragonsPlusPlugin.class).stream()
                .sorted((left, right) -> left.getProvider()
                        .getMetadata()
                        .getId()
                        .compareTo(right.getProvider().getMetadata().getId()))
                .forEach(container -> registerProvider(builder, container));
        freeze(builder.build());
    }

    private static void registerProvider(
            Builder builder, EntrypointContainer<CreateDragonsPlusPlugin> container) {
        builder.provider = container.getProvider().getMetadata().getId();
        container.getEntrypoint().registerDyeVariants(builder);
    }

    static void freeze(Collection<DyeVariant> variants) {
        if (!FROZEN.compareAndSet(false, true)) {
            throw new IllegalStateException("Dye variants have already been frozen");
        }
        all = List.copyOf(variants);
        LinkedHashMap<Identifier, DyeVariant> values = new LinkedHashMap<>();
        LinkedHashMap<Identifier, Integer> indexes = new LinkedHashMap<>();
        for (int index = 0; index < all.size(); index++) {
            DyeVariant variant = all.get(index);
            values.put(variant.id(), variant);
            indexes.put(variant.id(), index);
        }
        byId = Map.copyOf(values);
        order = Map.copyOf(indexes);
    }

    public static List<DyeVariant> all() {
        return all;
    }

    public static Optional<DyeVariant> get(Identifier id) {
        return Optional.ofNullable(byId.get(id));
    }

    public static int creativeModeTabIndex(Identifier id) {
        return order.getOrDefault(id, Integer.MAX_VALUE);
    }

    public static boolean isFrozen() {
        return FROZEN.get();
    }

    public static final class Builder {
        private final LinkedHashMap<Identifier, DyeVariant> variants = new LinkedHashMap<>();
        private final LinkedHashMap<Identifier, String> owners = new LinkedHashMap<>();
        private String provider = "unknown";

        public void add(DyeVariant variant) {
            String previous = owners.putIfAbsent(variant.id(), provider);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate dye variant " + variant.id() + " from " + provider
                        + "; already registered by " + previous);
            }
            variants.put(variant.id(), variant);
        }

        Collection<DyeVariant> build() {
            return variants.values();
        }
    }
}
