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

package plus.dragons.createdragonsplus.registry;

import java.util.Map;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import plus.dragons.createdragonsplus.config.CDPConfig;

/** Restores the smithing-template chance in vanilla Nether structure loot tables. */
public final class CDPLoots {
    private static final Map<ResourceKey<LootTable>, Integer> BLAZE_UPGRADE_SMITHING_TEMPLATE = Map.of(
            BuiltInLootTables.BASTION_TREASURE,
            1,
            BuiltInLootTables.BASTION_OTHER,
            10,
            BuiltInLootTables.BASTION_BRIDGE,
            10,
            BuiltInLootTables.BASTION_HOGLIN_STABLE,
            10,
            BuiltInLootTables.NETHER_BRIDGE,
            10);
    private static boolean registered;

    private CDPLoots() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            Integer totalWeight = BLAZE_UPGRADE_SMITHING_TEMPLATE.get(key);
            if (totalWeight == null || !CDPConfig.features().blazeUpgradeSmithingTemplate.get()) {
                return;
            }
            LootPool.Builder pool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(CDPItems.BLAZE_UPGRADE_SMITHING_TEMPLATE.get()).setWeight(1));
            if (totalWeight > 1) {
                pool.add(EmptyLootItem.emptyItem().setWeight(totalWeight - 1));
            }
            table.withPool(pool);
        });
    }
}
