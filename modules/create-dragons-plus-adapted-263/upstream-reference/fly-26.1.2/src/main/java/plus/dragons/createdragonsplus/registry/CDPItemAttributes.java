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

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.zurrtum.create.content.logistics.item.filter.attribute.SingletonItemAttribute;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.api.registry.CDPRegistryEntry;
import plus.dragons.createdragonsplus.common.kinetics.fan.coloring.ColoringFanProcessingType;
import plus.dragons.createdragonsplus.config.CDPConfig;

/** Create item-filter attributes backed by the four CDP fan-processing families. */
public final class CDPItemAttributes {
    private static final Map<StackKey, Boolean> STAINABLE_CACHE = new ConcurrentHashMap<>();
    private static ItemAttributeType freezable;
    private static ItemAttributeType sandable;
    private static ItemAttributeType endable;
    private static ItemAttributeType stainable;
    private static boolean registered;

    public static final CDPRegistryEntry<ItemAttributeType> FREEZABLE = new CDPRegistryEntry<>(CreateDragonsPlus.id("freezable"), () -> freezable);
    public static final CDPRegistryEntry<ItemAttributeType> SANDABLE = new CDPRegistryEntry<>(CreateDragonsPlus.id("sandable"), () -> sandable);
    public static final CDPRegistryEntry<ItemAttributeType> ENDABLE = new CDPRegistryEntry<>(CreateDragonsPlus.id("endable"), () -> endable);
    public static final CDPRegistryEntry<ItemAttributeType> STAINABLE = new CDPRegistryEntry<>(CreateDragonsPlus.id("stainable"), () -> stainable);

    private CDPItemAttributes() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        freezable = register(FREEZABLE, fanProcessing("freezable", CDPFanProcessingTypes.FREEZING));
        sandable = register(SANDABLE, fanProcessing("sandable", CDPFanProcessingTypes.SANDING));
        endable = register(ENDABLE, fanProcessing("endable", CDPFanProcessingTypes.ENDING));
        stainable = register(STAINABLE, colorFanProcessing(CDPFanProcessingTypes.COLORING.values()));
    }

    private static ItemAttributeType fanProcessing(String name, FanProcessingType processingType) {
        return new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(
                type, processingType::canProcess, CreateDragonsPlus.ID + "." + name));
    }

    private static ItemAttributeType colorFanProcessing(Collection<ColoringFanProcessingType> processingTypes) {
        return new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(
                type,
                (stack, level) -> canProcessByColoring(stack, level, processingTypes),
                CreateDragonsPlus.ID + ".stainable"));
    }

    private static boolean canProcessByColoring(
            ItemStack stack, Level level, Collection<ColoringFanProcessingType> processingTypes) {
        if (!CDPConfig.recipes().enableBulkColoring.get()) {
            return false;
        }
        return STAINABLE_CACHE.computeIfAbsent(
                StackKey.of(stack),
                ignored -> processingTypes.stream().anyMatch(type -> type.canProcess(stack, level)));
    }

    private static ItemAttributeType register(
            CDPRegistryEntry<ItemAttributeType> entry, ItemAttributeType attribute) {
        return Registry.register(CreateRegistries.ITEM_ATTRIBUTE_TYPE, entry.getId(), attribute);
    }

    public static void recreateCache() {
        STAINABLE_CACHE.clear();
    }

    private record StackKey(Item item, DataComponentPatch components) {
        static StackKey of(ItemStack stack) {
            return new StackKey(stack.getItem(), stack.getComponentsPatch());
        }
    }
}
