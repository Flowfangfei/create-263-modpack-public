package net.fxnt.fxntstorage.simple_storage.client;

import net.fxnt.fxntstorage.config.ConfigManager;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBox;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.List;

/** Client tooltip extraction is isolated from the common block entity verifier. */
public final class SimpleStorageBoxTooltip {
    private SimpleStorageBoxTooltip() {}
    public static boolean add(SimpleStorageBoxEntity entity, List<Component> tooltip) {
        // When a Compacting Upgrade is installed, show the currently displayed item instead of the underlying filter item
        ItemStack displayItem = entity.getDisplayedItem();
        if (displayItem.isEmpty() || ConfigManager.ClientConfig.SIMPLE_STORAGE_GOGGLE_INFO.get() == ConfigManager.ClientConfig.SimpleStorageBoxGoggleOverlay.OFF)
            return false;

        Minecraft mc = Minecraft.getInstance();
        if (!(mc.hitResult instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK)
            return false;
        if (blockHit.getDirection() != entity.getBlockState().getValue(SimpleStorageBox.FACING)) return false;

        boolean hasPotion = displayItem.has(DataComponents.POTION_CONTENTS);
        var ench = displayItem.get(DataComponents.ENCHANTMENTS);
        boolean hasEnchantments = (ench != null && !ench.isEmpty()) || displayItem.has(DataComponents.STORED_ENCHANTMENTS);
        boolean hasTrim = displayItem.has(DataComponents.TRIM);

        if ((!hasPotion && !hasEnchantments && !hasTrim) && ConfigManager.ClientConfig.SIMPLE_STORAGE_GOGGLE_INFO.get() == ConfigManager.ClientConfig.SimpleStorageBoxGoggleOverlay.ONLY_TAGGED)
            return false;

        List<Component> vanillaTooltip = displayItem.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.NORMAL);

        for (Component component : vanillaTooltip) {
            tooltip.add(Component.literal("    ").append(component.copy()));
        }

        return true;
    }
}
