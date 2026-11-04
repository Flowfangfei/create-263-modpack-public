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
import java.util.Map;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.StatAwardEvent;
import plus.dragons.createdragonsplus.common.registry.CDPCriterions;
import plus.dragons.createdragonsplus.util.CDPCodecs;

public class StatTrigger implements CriterionTrigger<StatTrigger.Instance> {
    public StatTrigger() { NeoForge.EVENT_BUS.register(this); }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void onStatAwardEvent(StatAwardEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var advancements = player.getAdvancements();
        for (var entry : Map.copyOf(advancements.getTriggerMapForType(this)).entrySet()) {
            var instance = entry.getValue();
            if (instance.stat().equals(event.getStat()) && instance.bounds().matches(event.getValue())) {
                var key = entry.getKey();
                advancements.award(key.advancement(), key.criterion());
            }
        }
    }

    @Override public Codec<Instance> codec() { return Instance.CODEC; }

    public record Instance(Stat<?> stat, MinMaxBounds.Ints bounds) implements CriterionTriggerInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                CDPCodecs.STAT.forGetter(Instance::stat),
                MinMaxBounds.Ints.CODEC.fieldOf("bounds").forGetter(Instance::bounds)).apply(instance, Instance::new));

        public static Criterion<Instance> of(Stat<?> stat, MinMaxBounds.Ints bounds) {
            return CDPCriterions.STAT.get().createCriterion(new Instance(stat, bounds));
        }

        public static Criterion<Instance> of(Identifier stat, MinMaxBounds.Ints bounds) {
            return CDPCriterions.STAT.get().createCriterion(new Instance(Stats.CUSTOM.get(stat), bounds));
        }

        @Override
        public void validate(ValidationContextSource validator) {}
    }
}
