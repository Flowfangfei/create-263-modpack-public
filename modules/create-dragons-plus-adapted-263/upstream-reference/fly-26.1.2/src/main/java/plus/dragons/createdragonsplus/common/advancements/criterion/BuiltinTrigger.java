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
import com.mojang.serialization.MapCodec;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

/** A small trigger that can be awarded directly by library consumers. */
public class BuiltinTrigger implements CriterionTrigger<BuiltinTrigger>, CriterionTriggerInstance {
    private final Map<PlayerAdvancements, Set<Listener<BuiltinTrigger>>> listeners = new IdentityHashMap<>();
    private final Codec<BuiltinTrigger> codec = MapCodec.unitCodec(this);

    public void trigger(ServerPlayer player) {
        Set<Listener<BuiltinTrigger>> active = listeners.get(player.getAdvancements());
        if (active != null) {
            Set.copyOf(active).forEach(listener -> listener.run(player.getAdvancements()));
        }
    }

    @Override
    public final void addPlayerListener(PlayerAdvancements advancements, Listener<BuiltinTrigger> listener) {
        listeners.computeIfAbsent(advancements, ignored -> new LinkedHashSet<>()).add(listener);
    }

    @Override
    public final void removePlayerListener(PlayerAdvancements advancements, Listener<BuiltinTrigger> listener) {
        Set<Listener<BuiltinTrigger>> active = listeners.get(advancements);
        if (active != null) {
            active.remove(listener);
            if (active.isEmpty()) {
                listeners.remove(advancements);
            }
        }
    }

    @Override
    public final void removePlayerListeners(PlayerAdvancements advancements) {
        listeners.remove(advancements);
    }

    @Override
    public Codec<BuiltinTrigger> codec() {
        return codec;
    }

    @Override
    public void validate(ValidationContextSource context) {}
}
