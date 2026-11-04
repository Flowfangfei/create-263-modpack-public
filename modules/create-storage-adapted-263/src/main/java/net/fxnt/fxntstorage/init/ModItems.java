package net.fxnt.fxntstorage.init;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.MechanicalCraftingRecipeBuilder;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.item.upgrades.UpgradeItem;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import static net.fxnt.fxntstorage.FXNTStorage.modLoc;

public class ModItems {
    private static final CreateRegistrate REGISTRATE = FXNTStorage.REGISTRATE;

    static {
        REGISTRATE.setCreativeTab(ModTabs.CREATIVE_MODE_TAB);
    }

    // Simple storage box upgrade items
    public static final ItemEntry<UpgradeItem> STORAGE_BOX_COMPACTING_UPGRADE = REGISTRATE
            .item("storage_box_compacting_upgrade", properties -> new UpgradeItem(properties, "storage_box_compacting_upgrade"))
            .lang("Compacting Upgrade")
            .properties(properties -> properties.stacksTo(16))
            
            .tag(ModTags.Items.STORAGE_BOX_UPGRADE)
            .register();

    public static final ItemEntry<UpgradeItem> STORAGE_BOX_CAPACITY_UPGRADE = REGISTRATE
            .item("storage_box_capacity_upgrade", properties -> new UpgradeItem(properties, Util.STORAGE_BOX_CAPACITY_UPGRADE))
            .lang("Capacity Upgrade")
            .properties(properties -> properties.stacksTo(16))
            
            .tag(ModTags.Items.STORAGE_BOX_UPGRADE)
            .register();

    public static final ItemEntry<UpgradeItem> STORAGE_BOX_VOID_UPGRADE = REGISTRATE
            .item("storage_box_void_upgrade", properties -> new UpgradeItem(properties, Util.STORAGE_BOX_VOID_UPGRADE))
            .lang("Void Upgrade")
            .properties(properties -> properties.stacksTo(16))
            
            .tag(ModTags.Items.STORAGE_BOX_UPGRADE)
            .register();


    // Backpack upgrade items
    public static final ItemEntry<UpgradeItem> BACKPACK_BLANK_UPGRADE = REGISTRATE
            .item("backpack_blank_upgrade", properties -> new UpgradeItem(properties, Util.BLANK_UPGRADE))
            .lang("Upgrade Template")
            .properties(properties -> properties.stacksTo(16))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_MAGNET_UPGRADE = REGISTRATE
            .item("backpack_magnet_upgrade", properties -> new UpgradeItem(properties, Util.MAGNET_UPGRADE))
            .lang("Magnet Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_MAGNET_UPGRADE_DEACTIVATED = REGISTRATE
            .item("backpack_magnet_upgrade_deactivated", properties -> new UpgradeItem(properties, Util.MAGNET_UPGRADE_DEACTIVATED))
            .lang("Magnet Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_PICKBLOCK_UPGRADE = REGISTRATE
            .item("backpack_pickblock_upgrade", properties -> new UpgradeItem(properties, Util.PICKBLOCK_UPGRADE))
            .lang("Pick Block Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_PICKBLOCK_UPGRADE_DEACTIVATED = REGISTRATE
            .item("backpack_pickblock_upgrade_deactivated", properties -> new UpgradeItem(properties, Util.PICKBLOCK_UPGRADE_DEACTIVATED))
            .lang("Pick Block Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_ITEMPICKUP_UPGRADE = REGISTRATE
            .item("backpack_itempickup_upgrade", properties -> new UpgradeItem(properties, Util.ITEMPICKUP_UPGRADE))
            .lang("Item Pickup Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_ITEMPICKUP_UPGRADE_DEACTIVATED = REGISTRATE
            .item("backpack_itempickup_upgrade_deactivated", properties -> new UpgradeItem(properties, Util.ITEMPICKUP_UPGRADE_DEACTIVATED))
            .lang("Item Pickup Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_FLIGHT_UPGRADE = REGISTRATE
            .item("backpack_flight_upgrade", properties -> new UpgradeItem(properties, Util.FLIGHT_UPGRADE))
            .lang("Jetpack Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_FLIGHT_UPGRADE_DEACTIVATED = REGISTRATE
            .item("backpack_flight_upgrade_deactivated", properties -> new UpgradeItem(properties, Util.FLIGHT_UPGRADE_DEACTIVATED))
            .lang("Jetpack Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_REFILL_UPGRADE = REGISTRATE
            .item("backpack_refill_upgrade", properties -> new UpgradeItem(properties, Util.REFILL_UPGRADE))
            .lang("Refill Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_REFILL_UPGRADE_DEACTIVATED = REGISTRATE
            .item("backpack_refill_upgrade_deactivated", properties -> new UpgradeItem(properties, Util.REFILL_UPGRADE_DEACTIVATED))
            .lang("Refill Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_FEEDER_UPGRADE = REGISTRATE
            .item("backpack_feeder_upgrade", properties -> new UpgradeItem(properties, Util.FEEDER_UPGRADE))
            .lang("Feeder Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_FEEDER_UPGRADE_DEACTIVATED = REGISTRATE
            .item("backpack_feeder_upgrade_deactivated", properties -> new UpgradeItem(properties, Util.FEEDER_UPGRADE_DEACTIVATED))
            .lang("Feeder Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_TOOLSWAP_UPGRADE = REGISTRATE
            .item("backpack_toolswap_upgrade", properties -> new UpgradeItem(properties, Util.TOOLSWAP_UPGRADE))
            .lang("Tool Swap Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_TOOLSWAP_UPGRADE_DEACTIVATED = REGISTRATE
            .item("backpack_toolswap_upgrade_deactivated", properties -> new UpgradeItem(properties, Util.TOOLSWAP_UPGRADE_DEACTIVATED))
            .lang("Tool Swap Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_FALLDAMAGE_UPGRADE = REGISTRATE
            .item("backpack_falldamage_upgrade", properties -> new UpgradeItem(properties, Util.FALLDAMAGE_UPGRADE))
            .lang("Fall Damage Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_FALLDAMAGE_UPGRADE_DEACTIVATED = REGISTRATE
            .item("backpack_falldamage_upgrade_deactivated", properties -> new UpgradeItem(properties, Util.FALLDAMAGE_UPGRADE_DEACTIVATED))
            .lang("Fall Damage Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_OREMINING_UPGRADE = REGISTRATE
            .item("backpack_oremining_upgrade", properties -> new UpgradeItem(properties, Util.OREMINING_UPGRADE))
            .lang("Ore Mining Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_OREMINING_UPGRADE_DEACTIVATED = REGISTRATE
            .item("backpack_oremining_upgrade_deactivated", properties -> new UpgradeItem(properties, Util.OREMINING_UPGRADE_DEACTIVATED))
            .lang("Ore Mining Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_TORCHDEPLOYER_UPGRADE = REGISTRATE
            .item("backpack_torchdeployer_upgrade", properties -> new UpgradeItem(properties, Util.TORCHDEPLOYER_UPGRADE))
            .lang("Torch Deployer Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_TORCHDEPLOYER_UPGRADE_DEACTIVATED = REGISTRATE
            .item("backpack_torchdeployer_upgrade_deactivated", properties -> new UpgradeItem(properties, Util.TORCHDEPLOYER_UPGRADE_DEACTIVATED))
            .lang("Torch Deployer Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_JUKEBOX_UPGRADE = REGISTRATE
            .item("backpack_jukebox_upgrade", properties -> new UpgradeItem(properties, Util.JUKEBOX_UPGRADE))
            .lang("Jukebox Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_JUKEBOX_UPGRADE_DEACTIVATED = REGISTRATE
            .item("backpack_jukebox_upgrade_deactivated", properties -> new UpgradeItem(properties, Util.JUKEBOX_UPGRADE_DEACTIVATED))
            .lang("Jukebox Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_HEALTH_UPGRADE = REGISTRATE
            .item(Util.HEALTH_UPGRADE, properties -> new UpgradeItem(properties, Util.HEALTH_UPGRADE))
            .lang("Mechanical Heart Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_HEALTH_UPGRADE_DEACTIVATED = REGISTRATE
            .item(Util.HEALTH_UPGRADE_DEACTIVATED, properties -> new UpgradeItem(properties, Util.HEALTH_UPGRADE_DEACTIVATED))
            .lang("Mechanical Heart Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_CRAFTING_UPGRADE = REGISTRATE
            .item(Util.CRAFTING_UPGRADE, properties -> new UpgradeItem(properties, Util.CRAFTING_UPGRADE))
            .lang("Crafting Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_CRAFTING_UPGRADE_DEACTIVATED = REGISTRATE
            .item(Util.CRAFTING_UPGRADE_DEACTIVATED, properties -> new UpgradeItem(properties, Util.CRAFTING_UPGRADE_DEACTIVATED))
            .lang("Crafting Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_WORKSHOP_UPGRADE = REGISTRATE
            .item(Util.WORKSHOP_UPGRADE, properties -> new UpgradeItem(properties, Util.WORKSHOP_UPGRADE))
            .lang("Portable Workshop Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_WORKSHOP_UPGRADE_DEACTIVATED = REGISTRATE
            .item(Util.WORKSHOP_UPGRADE_DEACTIVATED, properties -> new UpgradeItem(properties, Util.WORKSHOP_UPGRADE_DEACTIVATED))
            .lang("Portable Workshop Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_THIRST_UPGRADE = REGISTRATE
            .item(Util.THIRST_UPGRADE, properties -> new UpgradeItem(properties, Util.THIRST_UPGRADE))
            .lang("Thirst Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_THIRST_UPGRADE_DEACTIVATED = REGISTRATE
            .item(Util.THIRST_UPGRADE_DEACTIVATED, properties -> new UpgradeItem(properties, Util.THIRST_UPGRADE_DEACTIVATED))
            .lang("Thirst Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_VOID_UPGRADE = REGISTRATE
            .item(Util.VOID_UPGRADE, properties -> new UpgradeItem(properties, Util.VOID_UPGRADE))
            .lang("Void Upgrade")
            .properties(properties -> properties.stacksTo(1))
            
            .register();

    public static final ItemEntry<UpgradeItem> BACKPACK_VOID_UPGRADE_DEACTIVATED = REGISTRATE
            .item(Util.VOID_UPGRADE_DEACTIVATED, properties -> new UpgradeItem(properties, Util.VOID_UPGRADE_DEACTIVATED))
            .lang("Void Upgrade")
            .properties(properties -> properties.stacksTo(1))
            .tag(ModTags.Items.BACKPACK_UPGRADE_DEACTIVATED)
            .register();

    public static void register() {
    }
}
