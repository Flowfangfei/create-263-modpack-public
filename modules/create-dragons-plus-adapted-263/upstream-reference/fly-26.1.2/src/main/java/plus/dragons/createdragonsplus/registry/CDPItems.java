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

import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.box.PackageStyles.PackageStyle;
import java.util.List;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.api.registry.CDPRegistryEntry;
import plus.dragons.createdragonsplus.fluid.VaporizingBucketItem;

public final class CDPItems {
    private static Item fluidHatch;
    private static PackageItem rareBlazePackage;
    private static PackageItem rareMarbleGatePackage;
    private static SmithingTemplateItem blazeUpgradeSmithingTemplate;
    public static final CDPRegistryEntry<Item> FLUID_HATCH = new CDPRegistryEntry<>(CreateDragonsPlus.id("fluid_hatch"), () -> fluidHatch);
    public static final CDPRegistryEntry<PackageItem> RARE_BLAZE_PACKAGE = new CDPRegistryEntry<>(
            CreateDragonsPlus.id("rare_blaze_package"), () -> rareBlazePackage);
    public static final CDPRegistryEntry<PackageItem> RARE_MARBLE_GATE_PACKAGE = new CDPRegistryEntry<>(
            CreateDragonsPlus.id("rare_marble_gate_package"), () -> rareMarbleGatePackage);
    public static final CDPRegistryEntry<SmithingTemplateItem> BLAZE_UPGRADE_SMITHING_TEMPLATE = new CDPRegistryEntry<>(
            CreateDragonsPlus.id("blaze_upgrade_smithing_template"), () -> blazeUpgradeSmithingTemplate);
    private static boolean registered;

    private CDPItems() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        fluidHatch = registerBlockItem(CDPBlocks.FLUID_HATCH.get());

        for (var dyeFluid : CDPFluids.DYES_BY_VARIANT.values()) {
            registerBucket(dyeFluid.getId().withSuffix("_bucket"), dyeFluid.getSource(), Rarity.COMMON, false);
        }
        registerBucket(
                CDPFluids.DRAGON_BREATH.getId().withSuffix("_bucket"),
                CDPFluids.DRAGON_BREATH.getSource(),
                Rarity.UNCOMMON,
                true);

        rareBlazePackage = register(
                RARE_BLAZE_PACKAGE.getId(),
                PackageItem.styled(new PackageStyle(
                        CreateDragonsPlus.ID, "rare_blaze", 12, 10, 21.0F, true)),
                new Item.Properties()
                        .stacksTo(1)
                        .fireResistant()
                        .overrideDescription("item.create.rare_package"));
        rareMarbleGatePackage = register(
                RARE_MARBLE_GATE_PACKAGE.getId(),
                PackageItem.styled(new PackageStyle(
                        CreateDragonsPlus.ID, "rare_marble_gate", 12, 10, 21.0F, true)),
                new Item.Properties().stacksTo(1).overrideDescription("item.create.rare_package"));
        blazeUpgradeSmithingTemplate = register(
                BLAZE_UPGRADE_SMITHING_TEMPLATE.getId(),
                properties -> new SmithingTemplateItem(
                        Component.translatable("item.create_dragons_plus.smithing_template.blaze_upgrade.applies_to")
                                .withStyle(ChatFormatting.BLUE),
                        Component.translatable("item.create_dragons_plus.smithing_template.blaze_upgrade.ingredients")
                                .withStyle(ChatFormatting.BLUE),
                        Component.translatable(
                                "item.create_dragons_plus.smithing_template.blaze_upgrade.base_slot_description"),
                        Component.translatable(
                                "item.create_dragons_plus.smithing_template.blaze_upgrade.additions_slot_description"),
                        List.of(),
                        List.of(),
                        properties),
                new Item.Properties());
    }

    private static BucketItem registerBucket(Identifier id, Fluid fluid, Rarity rarity, boolean dragonBreath) {
        BucketItem bucket = register(
                id,
                properties -> new VaporizingBucketItem(fluid, dragonBreath, properties),
                new Item.Properties().craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1).rarity(rarity));
        Identifier fluidId = Identifier.fromNamespaceAndPath(
                id.getNamespace(), id.getPath().substring(0, id.getPath().length() - "_bucket".length()));
        CDPFluids.internal(fluidId).bucket = bucket;
        return bucket;
    }

    private static BlockItem registerBlockItem(Block block) {
        return register(
                block.builtInRegistryHolder().key().identifier(),
                properties -> new BlockItem(block, properties.useBlockDescriptionPrefix()),
                new Item.Properties());
    }

    private static <T extends Item> T register(
            Identifier id, Function<Item.Properties, T> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        T item = factory.apply(properties.setId(key));
        if (item instanceof BlockItem blockItem) {
            blockItem.registerBlocks(Item.BY_BLOCK, item);
        }
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }
}
