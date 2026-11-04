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

package plus.dragons.createdragonsplus.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.api.registry.CDPRegistryEntry;
import plus.dragons.createdragonsplus.common.advancements.criterion.StatTrigger;

public final class CDPCriterions {
    private static StatTrigger stat;
    private static boolean registered;
    public static final CDPRegistryEntry<StatTrigger> STAT = new CDPRegistryEntry<>(CreateDragonsPlus.id("stat"), () -> stat);

    private CDPCriterions() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        stat = Registry.register(BuiltInRegistries.TRIGGER_TYPES, STAT.getId(), new StatTrigger());
    }

    public static void onStatChanged(ServerPlayer player, Stat<?> value, int amount) {
        if (stat != null) {
            stat.trigger(player, value, amount);
        }
    }
}
