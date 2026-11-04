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

import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.catnip.config.DoubleRawValue;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.api.registry.CDPRegistryEntry;

/** Config-backed stress values for downstream Create addons. */
public class StressConfig extends ConfigBase {
    private static final Map<String, StressConfig> ACTIVE_PROVIDERS = new ConcurrentHashMap<>();

    protected final String modid;
    protected final Map<Identifier, Double> defaultImpacts = new LinkedHashMap<>();
    protected final Map<Identifier, Double> defaultCapacities = new LinkedHashMap<>();
    protected final Map<Identifier, DoubleRawValue> impacts = new LinkedHashMap<>();
    protected final Map<Identifier, DoubleRawValue> capacities = new LinkedHashMap<>();

    private boolean frozen;
    private boolean providerRegistered;

    public StressConfig(String modid) {
        this.modid = modid;
    }

    protected int getVersion() {
        return 1;
    }

    @Override
    public synchronized void registerAll(Builder builder) {
        frozen = true;
        impacts.clear();
        capacities.clear();

        builder.comment(Comments.SU, Comments.IMPACT).push("impact");
        defaultImpacts.forEach((id, value) -> impacts.put(id, builder.define(id.getPath(), value)));
        builder.pop();

        builder.comment(Comments.SU, Comments.CAPACITY).push("capacity");
        defaultCapacities.forEach((id, value) -> capacities.put(id, builder.define(id.getPath(), value)));
        builder.pop();

        if (!providerRegistered) {
            StressConfig active = ACTIVE_PROVIDERS.putIfAbsent(modid, this);
            if (active != null && active != this) {
                throw new IllegalStateException(
                        "StressConfig for " + modid + " must reuse the same instance when configs reload");
            }
            BlockStressValues.IMPACTS.registerProvider(this::getImpact);
            BlockStressValues.CAPACITIES.registerProvider(this::getCapacity);
            providerRegistered = true;
        }
    }

    @Override
    public String getName() {
        return "stressValues.v" + getVersion();
    }

    public synchronized StressConfig setNoImpact(Identifier id) {
        return setImpact(id, 0);
    }

    public synchronized StressConfig setImpact(Identifier id, double value) {
        putDefault(defaultImpacts, id, value, "impact");
        return this;
    }

    public synchronized StressConfig setCapacity(Identifier id, double value) {
        putDefault(defaultCapacities, id, value, "capacity");
        return this;
    }

    public <B extends Block> B setNoImpact(B block) {
        return setImpact(block, 0);
    }

    public <B extends Block> B setImpact(B block, double value) {
        setImpact(getRegisteredId(block), value);
        return block;
    }

    public <B extends Block> B setCapacity(B block, double value) {
        setCapacity(getRegisteredId(block), value);
        return block;
    }

    public <B extends Block> CDPRegistryEntry<B> setNoImpact(CDPRegistryEntry<B> entry) {
        return setImpact(entry, 0);
    }

    public <B extends Block> CDPRegistryEntry<B> setImpact(CDPRegistryEntry<B> entry, double value) {
        setImpact(entry.getId(), value);
        return entry;
    }

    public <B extends Block> CDPRegistryEntry<B> setCapacity(CDPRegistryEntry<B> entry, double value) {
        setCapacity(entry.getId(), value);
        return entry;
    }

    public synchronized @Nullable DoubleSupplier getImpact(Block block) {
        DoubleRawValue value = impacts.get(BuiltInRegistries.BLOCK.getKey(block));
        return value == null ? null : value::get;
    }

    public synchronized @Nullable DoubleSupplier getCapacity(Block block) {
        DoubleRawValue value = capacities.get(BuiltInRegistries.BLOCK.getKey(block));
        return value == null ? null : value::get;
    }

    private void putDefault(Map<Identifier, Double> defaults, Identifier id, double value, String kind) {
        if (frozen) {
            throw new IllegalStateException("Cannot register stress " + kind + " after " + getName() + " was built");
        }
        if (!id.getNamespace().equals(modid)) {
            throw new IllegalArgumentException("StressConfig for " + modid + " does not accept " + id);
        }
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException("Stress " + kind + " must be a finite non-negative value: " + value);
        }
        if (defaults.putIfAbsent(id, value) != null) {
            throw new IllegalArgumentException("Duplicate stress " + kind + " for " + id);
        }
    }

    private static Identifier getRegisteredId(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        if (BuiltInRegistries.BLOCK.getValue(id) != block) {
            throw new IllegalStateException("Block must be registered before assigning stress values");
        }
        return id;
    }

    private static final class Comments {
        private static final String SU = "[in Stress Units]";
        private static final String IMPACT = "Configure the individual stress impact of mechanical blocks. This cost scales with speed.";
        private static final String CAPACITY = "Configure how much stress a source can accommodate.";

        private Comments() {}
    }
}
