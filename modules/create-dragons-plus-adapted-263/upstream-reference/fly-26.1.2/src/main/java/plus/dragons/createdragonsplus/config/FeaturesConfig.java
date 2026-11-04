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

package plus.dragons.createdragonsplus.config;

import com.zurrtum.create.catnip.config.ConfigBase;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.CreateDragonsPlus;

public class FeaturesConfig extends ConfigBase {
    private static final Map<Identifier, ConfigFeature> FEATURES = new LinkedHashMap<>();
    protected final String modId;

    protected FeaturesConfig(String modId) {
        this.modId = modId;
    }

    public static Map<Identifier, ConfigFeature> getFeatures() {
        return Collections.unmodifiableMap(FEATURES);
    }

    public static boolean isFeatureEnabled(Identifier id) {
        ConfigFeature feature = FEATURES.get(id);
        return feature != null && feature.get();
    }

    protected ConfigFeature feature(boolean enabled, String name, String... comment) {
        return new ConfigFeature(name, enabled, comment);
    }

    public final class ConfigFeature extends ConfigBool {
        private final Identifier id;
        private final Set<String> metadataKeys = new LinkedHashSet<>();
        private @Nullable Boolean override;

        private ConfigFeature(String name, boolean defaultValue, String... comment) {
            super(name, defaultValue, comment);
            id = Identifier.fromNamespaceAndPath(modId, name);
            metadataKeys.add(id.toString());
            putFeature(id, this);
            resolveOverride();
        }

        public Identifier getId() {
            return id;
        }

        public ConfigFeature addAlias(String name) {
            Identifier alias = Identifier.fromNamespaceAndPath(modId, name);
            metadataKeys.add(alias.toString());
            putFeature(alias, this);
            resolveOverride();
            return this;
        }

        @Override
        public Boolean get() {
            return override == null ? super.get() : override;
        }

        private void resolveOverride() {
            Boolean selected = null;
            String selectedBy = null;
            for (var mod : FabricLoader.getInstance().getAllMods().stream()
                    .sorted((left, right) -> left.getMetadata()
                            .getId()
                            .compareTo(right.getMetadata().getId()))
                    .toList()) {
                for (String key : metadataKeys) {
                    CustomValue value = mod.getMetadata().getCustomValue(key);
                    if (value == null || value.getType() != CustomValue.CvType.BOOLEAN) {
                        continue;
                    }
                    boolean candidate = value.getAsBoolean();
                    if (selected != null && selected != candidate) {
                        CreateDragonsPlus.LOGGER.warn(
                                "Feature {} is overridden by both {}={} and {}={}; sorted mod-id order selects {}",
                                id,
                                selectedBy,
                                selected,
                                mod.getMetadata().getId(),
                                candidate,
                                mod.getMetadata().getId());
                    }
                    selected = candidate;
                    selectedBy = mod.getMetadata().getId();
                }
            }
            override = selected;
        }
    }

    private static void putFeature(Identifier id, ConfigFeature value) {
        ConfigFeature previous = FEATURES.putIfAbsent(id, value);
        if (previous != null && previous != value) {
            throw new IllegalStateException("Duplicate config feature: " + id);
        }
    }

    @Override
    public String getName() {
        return "features";
    }
}
