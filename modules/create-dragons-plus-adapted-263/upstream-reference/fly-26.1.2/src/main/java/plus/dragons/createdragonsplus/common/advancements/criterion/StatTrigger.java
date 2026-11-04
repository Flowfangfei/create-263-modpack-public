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

package plus.dragons.createdragonsplus.common.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import plus.dragons.createdragonsplus.common.registry.CDPCriterions;
import plus.dragons.createdragonsplus.util.CDPCodecs;

/** Criterion trigger evaluated after a server player's statistic value changes. */
public final class StatTrigger implements CriterionTrigger<StatTrigger.Instance> {
    private final Map<PlayerAdvancements, Map<Stat<?>, Set<Listener<Instance>>>> listeners = new IdentityHashMap<>();

    public void trigger(ServerPlayer player, Stat<?> stat, int value) {
        Map<Stat<?>, Set<Listener<Instance>>> byStat = listeners.get(player.getAdvancements());
        if (byStat == null) {
            return;
        }
        Set<Listener<Instance>> active = byStat.get(stat);
        if (active == null) {
            return;
        }
        for (Listener<Instance> listener : Set.copyOf(active)) {
            if (listener.trigger().bounds().matches(value)) {
                listener.run(player.getAdvancements());
            }
        }
    }

    @Override
    public void addPlayerListener(PlayerAdvancements advancements, Listener<Instance> listener) {
        listeners.computeIfAbsent(advancements, ignored -> new HashMap<>())
                .computeIfAbsent(listener.trigger().stat(), ignored -> new LinkedHashSet<>())
                .add(listener);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements advancements, Listener<Instance> listener) {
        Map<Stat<?>, Set<Listener<Instance>>> byStat = listeners.get(advancements);
        if (byStat == null) {
            return;
        }
        Set<Listener<Instance>> active = byStat.get(listener.trigger().stat());
        if (active != null) {
            active.remove(listener);
            if (active.isEmpty()) {
                byStat.remove(listener.trigger().stat());
            }
        }
        if (byStat.isEmpty()) {
            listeners.remove(advancements);
        }
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements advancements) {
        listeners.remove(advancements);
    }

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public record Instance(Stat<?> stat, MinMaxBounds.Ints bounds) implements CriterionTriggerInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                CDPCodecs.STAT.forGetter(Instance::stat),
                MinMaxBounds.Ints.CODEC.fieldOf("bounds").forGetter(Instance::bounds))
                .apply(instance, Instance::new));

        public static Criterion<Instance> of(Stat<?> stat, MinMaxBounds.Ints bounds) {
            return CDPCriterions.STAT.get().createCriterion(new Instance(stat, bounds));
        }

        public static Criterion<Instance> of(Identifier stat, MinMaxBounds.Ints bounds) {
            return of(Stats.CUSTOM.get(stat), bounds);
        }

        @Override
        public void validate(ValidationContextSource context) {}
    }
}
