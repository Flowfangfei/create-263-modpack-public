/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package plus.dragons.createdragonsplus.data.internal;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider.IntrinsicImpl;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createdragonsplus.common.CDPRegistrate;
import plus.dragons.createdragonsplus.data.tag.IntrinsicTagRegistry;
import plus.dragons.createdragonsplus.data.tag.ItemTagRegistry;
import plus.dragons.createdragonsplus.data.tag.TagRegistry;

/** Provider classes are resolved only while handling an actual GatherDataEvent. */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class CDPTagDataProviders {
    private CDPTagDataProviders() {}
    public static void register(CDPRegistrate registrate, Map<String, List<Object>> deferred) {
        for (Object value : deferred.getOrDefault("generic", List.of())) {
            Object[] pair = (Object[]) value;
            registrate.addDataGenerator((ProviderType) pair[0], provider -> ((TagRegistry) pair[1]).generate((RegistrateTagsProvider) provider));
        }
        for (Object value : deferred.getOrDefault("enchantment", List.of())) {
            TagRegistry<Enchantment, RegistrateTagsProvider<Enchantment>> registry = (TagRegistry) value;
            registrate.addDataGenerator(ProviderType.ENCHANTMENT_TAGS, registry::generate);
        }
        for (Object value : deferred.getOrDefault("block", List.of())) {
            IntrinsicTagRegistry<Block, IntrinsicImpl<Block>> registry = (IntrinsicTagRegistry) value;
            registrate.addDataGenerator(ProviderType.BLOCK_TAGS, registry::generate);
            registrate.addDataGenerator(ProviderType.LANG, registry::generate);
        }
        for (Object value : deferred.getOrDefault("item", List.of())) {
            ItemTagRegistry registry = (ItemTagRegistry) value;
            registrate.addDataGenerator(ProviderType.ITEM_TAGS, registry::generate);
            registrate.addDataGenerator(ProviderType.LANG, registry::generate);
        }
        for (Object value : deferred.getOrDefault("fluid", List.of())) {
            IntrinsicTagRegistry<Fluid, IntrinsicImpl<Fluid>> registry = (IntrinsicTagRegistry) value;
            registrate.addDataGenerator(ProviderType.FLUID_TAGS, registry::generate);
            registrate.addDataGenerator(ProviderType.LANG, registry::generate);
        }
        for (Object value : deferred.getOrDefault("entity", List.of())) {
            IntrinsicTagRegistry<EntityType<?>, IntrinsicImpl<EntityType<?>>> registry = (IntrinsicTagRegistry) value;
            registrate.addDataGenerator(ProviderType.ENTITY_TAGS, registry::generate);
            registrate.addDataGenerator(ProviderType.LANG, registry::generate);
        }
    }
}
