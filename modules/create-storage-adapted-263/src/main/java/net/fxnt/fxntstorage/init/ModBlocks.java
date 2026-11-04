package net.fxnt.fxntstorage.init;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpack.BackpackBlock;
import net.fxnt.fxntstorage.backpack.BackpackItem;
import net.fxnt.fxntstorage.backpack.mounted.BackpackMovementBehaviour;
import net.fxnt.fxntstorage.compat.vanillabackport.VanillaBackportCompat;
import net.fxnt.fxntstorage.container.StorageBox;
import net.fxnt.fxntstorage.container.StorageBoxItem;
import net.fxnt.fxntstorage.container.mounted.StorageBoxMovementBehaviour;
import net.fxnt.fxntstorage.controller.StorageController;
import net.fxnt.fxntstorage.controller.StorageInterface;
import net.fxnt.fxntstorage.controller.StorageInterfaceFiltered;
import net.fxnt.fxntstorage.datagen.helper.*;
import net.fxnt.fxntstorage.passer.PasserBlock;
import net.fxnt.fxntstorage.registry.SpriteShifts;
import net.fxnt.fxntstorage.reserve_storage.ReserveStorageBox;
import net.fxnt.fxntstorage.reserve_storage.ReserveStorageBoxItem;
import net.fxnt.fxntstorage.reserve_storage.mounted.ReserveStorageBoxMovementBehaviour;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBox;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxCTBehaviour;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxItem;
import net.fxnt.fxntstorage.simple_storage.mounted.SimpleStorageBoxMovementBehaviour;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import static com.simibubi.create.api.contraption.storage.item.MountedItemStorageType.mountedItemStorage;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;

public class ModBlocks {
    private static final CreateRegistrate REGISTRATE = FXNTStorage.REGISTRATE;

    static {
        REGISTRATE.setCreativeTab(ModTabs.CREATIVE_MODE_TAB);
    }

    public static final BlockEntry<ReserveStorageBox> RESERVE_STORAGE_BOX = REGISTRATE
            .block("reserve_storage_box", ReserveStorageBox::new)
            .lang("Reserve Barrel")
            .initialProperties(() -> Blocks.BARREL)
            
            
            
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .tag(ModTags.Blocks.STORAGE_BOX)
            .transform(mountedItemStorage(ModMountedStorageTypes.RESERVE_STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new ReserveStorageBoxMovementBehaviour()))
            .item(ReserveStorageBoxItem::new)
            .build()
            .register();

    // STORAGE BOXES //
    public static final BlockEntry<StorageBox> CARDBOARD_STORAGE_BOX = REGISTRATE
            .block("cardboard_storage_box", properties -> new StorageBox(properties, Util.CARDBOARD_STORAGE_BOX_SIZE))
            .initialProperties(AllBlocks.CARDBOARD_BLOCK::get)
            .transform(mountedItemStorage(ModMountedStorageTypes.STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new StorageBoxMovementBehaviour()))
            
            
            
            .tag(ModTags.Blocks.STORAGE_BOX)
            .item(StorageBoxItem::new)
            
            .tag(ModTags.Items.STORAGE_BOX_ITEM)
            .build()
            .register();

    public static final BlockEntry<StorageBox> STORAGE_BOX = REGISTRATE
            .block("storage_box", properties -> new StorageBox(properties, Util.IRON_STORAGE_BOX_SIZE))
            .lang("Industrial Iron Storage Box")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .transform(mountedItemStorage(ModMountedStorageTypes.STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new StorageBoxMovementBehaviour()))
            
            
            
            .tag(ModTags.Blocks.STORAGE_BOX)
            .item(StorageBoxItem::new)
            
            .tag(ModTags.Items.STORAGE_BOX_ITEM)
            .build()
            .register();

    public static final BlockEntry<StorageBox> WEATHERED_STORAGE_BOX = REGISTRATE
            .block("weathered_storage_box", properties -> new StorageBox(properties, Util.IRON_STORAGE_BOX_SIZE))
            .lang("Weathered Iron Storage Box")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .transform(mountedItemStorage(ModMountedStorageTypes.STORAGE_BOX_MOUNTED))
            
            
            
            .tag(ModTags.Blocks.STORAGE_BOX)
            .onRegister(movementBehaviour(new StorageBoxMovementBehaviour()))
            .item(StorageBoxItem::new)
            
            .tag(ModTags.Items.STORAGE_BOX_ITEM)
            .build()
            .register();

    public static final BlockEntry<StorageBox> ANDESITE_STORAGE_BOX = REGISTRATE
            .block("andesite_storage_box", properties -> new StorageBox(properties, Util.ANDESITE_STORAGE_BOX_SIZE))
            .initialProperties(() -> Blocks.ANDESITE)
            .transform(mountedItemStorage(ModMountedStorageTypes.STORAGE_BOX_MOUNTED))
            
            
            
            .tag(ModTags.Blocks.STORAGE_BOX)
            .onRegister(movementBehaviour(new StorageBoxMovementBehaviour()))
            .item(StorageBoxItem::new)
            
            .tag(ModTags.Items.STORAGE_BOX_ITEM)
            .build()
            .register();

    public static final BlockEntry<StorageBox> COPPER_STORAGE_BOX = REGISTRATE
            .block("copper_storage_box", properties -> new StorageBox(properties, Util.COPPER_STORAGE_BOX_SIZE))
            .initialProperties(() -> Blocks.COPPER_BLOCK.weathering().unaffected())
            .transform(mountedItemStorage(ModMountedStorageTypes.STORAGE_BOX_MOUNTED))
            
            
            
            .tag(ModTags.Blocks.STORAGE_BOX)
            .onRegister(movementBehaviour(new StorageBoxMovementBehaviour()))
            .item(StorageBoxItem::new)
            
            .tag(ModTags.Items.STORAGE_BOX_ITEM)
            .build()
            .register();

    public static final BlockEntry<StorageBox> BRASS_STORAGE_BOX = REGISTRATE
            .block("brass_storage_box", properties -> new StorageBox(properties, Util.BRASS_STORAGE_BOX_SIZE))
            .initialProperties(AllBlocks.BRASS_BLOCK)
            .transform(mountedItemStorage(ModMountedStorageTypes.STORAGE_BOX_MOUNTED))
            
            
            
            .tag(ModTags.Blocks.STORAGE_BOX)
            .onRegister(movementBehaviour(new StorageBoxMovementBehaviour()))
            .item(StorageBoxItem::new)
            
            .tag(ModTags.Items.STORAGE_BOX_ITEM)
            .build()
            .register();

    public static final BlockEntry<StorageBox> HARDENED_STORAGE_BOX = REGISTRATE
            .block("hardened_storage_box", properties -> new StorageBox(properties, Util.HARDENED_STORAGE_BOX_SIZE))
            .initialProperties(() -> Blocks.NETHERITE_BLOCK)
            .transform(mountedItemStorage(ModMountedStorageTypes.STORAGE_BOX_MOUNTED))
            
            
            
            .tag(ModTags.Blocks.STORAGE_BOX)
            .onRegister(movementBehaviour(new StorageBoxMovementBehaviour()))
            .item(StorageBoxItem::new)
            
            .tag(ModTags.Items.STORAGE_BOX_ITEM)
            .build()
            .register();


    // BACKPACKS //
    public static final BlockEntry<BackpackBlock> BACKPACK = REGISTRATE
            .block("backpack", properties -> new BackpackBlock(properties, Util.IRON_BACKPACK_STACK_MULTIPLIER))
            .initialProperties(() -> Blocks.WOOL.white())
            .properties(BlockBehaviour.Properties::noCollision)
            
            
            
            .tag(ModTags.Blocks.BACKPACK)
            .transform(mountedItemStorage(ModMountedStorageTypes.BACKPACK_MOUNTED))
            .onRegister(movementBehaviour(new BackpackMovementBehaviour()))
            .item(BackpackItem::new)
            
            .tag(ModTags.Items.BACKPACK_ITEM)
            .build()
            .register();

    public static final BlockEntry<BackpackBlock> ANDESITE_BACKPACK = REGISTRATE
            .block("andesite_backpack", properties -> new BackpackBlock(properties, Util.ANDESITE_BACKPACK_STACK_MULTIPLIER))
            .initialProperties(() -> Blocks.WOOL.white())
            .properties(BlockBehaviour.Properties::noCollision)
            
            
            .tag(ModTags.Blocks.BACKPACK)
            .transform(mountedItemStorage(ModMountedStorageTypes.BACKPACK_MOUNTED))
            .onRegister(movementBehaviour(new BackpackMovementBehaviour()))
            .item(BackpackItem::new)
            
            .tag(ModTags.Items.BACKPACK_ITEM)
            .build()
            .register();

    public static final BlockEntry<BackpackBlock> COPPER_BACKPACK = REGISTRATE
            .block("copper_backpack", properties -> new BackpackBlock(properties, Util.COPPER_BACKPACK_STACK_MULTIPLIER))
            .initialProperties(() -> Blocks.WOOL.white())
            .properties(BlockBehaviour.Properties::noCollision)
            
            
            .tag(ModTags.Blocks.BACKPACK)
            .transform(mountedItemStorage(ModMountedStorageTypes.BACKPACK_MOUNTED))
            .onRegister(movementBehaviour(new BackpackMovementBehaviour()))
            .item(BackpackItem::new)
            
            .tag(ModTags.Items.BACKPACK_ITEM)
            .build()
            .register();

    public static final BlockEntry<BackpackBlock> BRASS_BACKPACK = REGISTRATE
            .block("brass_backpack", properties -> new BackpackBlock(properties, Util.BRASS_BACKPACK_STACK_MULTIPLIER))
            .initialProperties(() -> Blocks.WOOL.white())
            .properties(BlockBehaviour.Properties::noCollision)
            
            
            .tag(ModTags.Blocks.BACKPACK)
            .transform(mountedItemStorage(ModMountedStorageTypes.BACKPACK_MOUNTED))
            .onRegister(movementBehaviour(new BackpackMovementBehaviour()))
            .item(BackpackItem::new)
            
            .tag(ModTags.Items.BACKPACK_ITEM)
            .build()
            .register();

    public static final BlockEntry<BackpackBlock> HARDENED_BACKPACK = REGISTRATE
            .block("hardened_backpack", properties -> new BackpackBlock(properties, Util.HARDENED_BACKPACK_STACK_MULTIPLIER))
            .initialProperties(() -> Blocks.WOOL.white())
            .properties(BlockBehaviour.Properties::noCollision)
            
            
            .tag(ModTags.Blocks.BACKPACK)
            .transform(mountedItemStorage(ModMountedStorageTypes.BACKPACK_MOUNTED))
            .onRegister(movementBehaviour(new BackpackMovementBehaviour()))
            .item(BackpackItem::new)
            
            .tag(ModTags.Items.BACKPACK_ITEM)
            .build()
            .register();


    // STORAGE CONTROLLER BLOCKS //
    public static final BlockEntry<StorageController> STORAGE_CONTROLLER = REGISTRATE
            .block("storage_controller", StorageController::new)
            .lang("Simple Storage Controller")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            
            
            .simpleItem()
            .register();

    public static final BlockEntry<StorageInterface> STORAGE_INTERFACE = REGISTRATE
            .block("storage_interface", StorageInterface::new)
            .lang("Simple Storage Interface")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            
            
            .simpleItem()
            .register();

    public static final BlockEntry<StorageInterfaceFiltered> STORAGE_INTERFACE_FILTERED = REGISTRATE
            .block("storage_interface_filtered", StorageInterfaceFiltered::new)
            .lang("Simple Storage Filtered Interface")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(BlockBehaviour.Properties::noOcclusion)
            
            
            .simpleItem()
            .register();


    // PASSER BLOCKS //
    public static final BlockEntry<PasserBlock> PASSER_BLOCK = REGISTRATE
            .block("passer_block", properties -> new PasserBlock(properties, false))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(properties -> properties.strength(1.5f))
            
            
            .simpleItem()
            .register();

    public static final BlockEntry<PasserBlock> SMART_PASSER_BLOCK = REGISTRATE
            .block("smart_passer_block", properties -> new PasserBlock(properties, true))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(properties -> properties.strength(1.5f))
            
            
            .simpleItem()
            .register();


    // SIMPLE STORAGE BOXES //
    public static final BlockEntry<SimpleStorageBox> SIMPLE_STORAGE_BOX_OAK = REGISTRATE
            .block("oak_simple_storage_box", SimpleStorageBox::new)
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .transform(mountedItemStorage(ModMountedStorageTypes.SIMPLE_STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new SimpleStorageBoxMovementBehaviour()))
            .onRegister(connectedTextures(() -> new SimpleStorageBoxCTBehaviour(SpriteShifts.OAK_CASING, ModBlocks.STORAGE_TRIM_OAK)))
            
            
            
            .item(SimpleStorageBoxItem::new)
            
            .build()
            .register();

    public static final BlockEntry<SimpleStorageBox> SIMPLE_STORAGE_BOX_SPRUCE = REGISTRATE
            .block("spruce_simple_storage_box", SimpleStorageBox::new)
            .initialProperties(() -> Blocks.SPRUCE_PLANKS)
            .transform(mountedItemStorage(ModMountedStorageTypes.SIMPLE_STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new SimpleStorageBoxMovementBehaviour()))
            .onRegister(connectedTextures(() -> new SimpleStorageBoxCTBehaviour(SpriteShifts.SPRUCE_CASING, ModBlocks.STORAGE_TRIM_SPRUCE)))
            
            
            
            .item(SimpleStorageBoxItem::new)
            
            .build()
            .register();

    public static final BlockEntry<SimpleStorageBox> SIMPLE_STORAGE_BOX_BIRCH = REGISTRATE
            .block("birch_simple_storage_box", SimpleStorageBox::new)
            .initialProperties(() -> Blocks.BIRCH_PLANKS)
            .transform(mountedItemStorage(ModMountedStorageTypes.SIMPLE_STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new SimpleStorageBoxMovementBehaviour()))
            .onRegister(connectedTextures(() -> new SimpleStorageBoxCTBehaviour(SpriteShifts.BIRCH_CASING, ModBlocks.STORAGE_TRIM_BIRCH)))
            
            
            
            .item(SimpleStorageBoxItem::new)
            
            .build()
            .register();

    public static final BlockEntry<SimpleStorageBox> SIMPLE_STORAGE_BOX_JUNGLE = REGISTRATE
            .block("jungle_simple_storage_box", SimpleStorageBox::new)
            .initialProperties(() -> Blocks.JUNGLE_PLANKS)
            .transform(mountedItemStorage(ModMountedStorageTypes.SIMPLE_STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new SimpleStorageBoxMovementBehaviour()))
            .onRegister(connectedTextures(() -> new SimpleStorageBoxCTBehaviour(SpriteShifts.JUNGLE_CASING, ModBlocks.STORAGE_TRIM_JUNGLE)))
            
            
            
            .item(SimpleStorageBoxItem::new)
            
            .build()
            .register();

    public static final BlockEntry<SimpleStorageBox> SIMPLE_STORAGE_BOX_ACACIA = REGISTRATE
            .block("acacia_simple_storage_box", SimpleStorageBox::new)
            .initialProperties(() -> Blocks.ACACIA_PLANKS)
            .transform(mountedItemStorage(ModMountedStorageTypes.SIMPLE_STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new SimpleStorageBoxMovementBehaviour()))
            .onRegister(connectedTextures(() -> new SimpleStorageBoxCTBehaviour(SpriteShifts.ACACIA_CASING, ModBlocks.STORAGE_TRIM_ACACIA)))
            
            
            
            .item(SimpleStorageBoxItem::new)
            
            .build()
            .register();

    public static final BlockEntry<SimpleStorageBox> SIMPLE_STORAGE_BOX_DARK_OAK = REGISTRATE
            .block("dark_oak_simple_storage_box", SimpleStorageBox::new)
            .initialProperties(() -> Blocks.DARK_OAK_PLANKS)
            .transform(mountedItemStorage(ModMountedStorageTypes.SIMPLE_STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new SimpleStorageBoxMovementBehaviour()))
            .onRegister(connectedTextures(() -> new SimpleStorageBoxCTBehaviour(SpriteShifts.DARK_OAK_CASING, ModBlocks.STORAGE_TRIM_DARK_OAK)))
            
            
            
            .item(SimpleStorageBoxItem::new)
            
            .build()
            .register();

    public static final BlockEntry<SimpleStorageBox> SIMPLE_STORAGE_BOX_MANGROVE = REGISTRATE
            .block("mangrove_simple_storage_box", SimpleStorageBox::new)
            .initialProperties(() -> Blocks.MANGROVE_PLANKS)
            .transform(mountedItemStorage(ModMountedStorageTypes.SIMPLE_STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new SimpleStorageBoxMovementBehaviour()))
            .onRegister(connectedTextures(() -> new SimpleStorageBoxCTBehaviour(SpriteShifts.MANGROVE_CASING, ModBlocks.STORAGE_TRIM_MANGROVE)))
            
            
            
            .item(SimpleStorageBoxItem::new)
            
            .build()
            .register();

    public static final BlockEntry<SimpleStorageBox> SIMPLE_STORAGE_BOX_CHERRY = REGISTRATE
            .block("cherry_simple_storage_box", SimpleStorageBox::new)
            .initialProperties(() -> Blocks.CHERRY_PLANKS)
            .transform(mountedItemStorage(ModMountedStorageTypes.SIMPLE_STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new SimpleStorageBoxMovementBehaviour()))
            .onRegister(connectedTextures(() -> new SimpleStorageBoxCTBehaviour(SpriteShifts.CHERRY_CASING, ModBlocks.STORAGE_TRIM_CHERRY)))
            
            
            
            .item(SimpleStorageBoxItem::new)
            
            .build()
            .register();

    public static final BlockEntry<SimpleStorageBox> SIMPLE_STORAGE_BOX_BAMBOO = REGISTRATE
            .block("bamboo_simple_storage_box", SimpleStorageBox::new)
            .initialProperties(() -> Blocks.BAMBOO_PLANKS)
            .transform(mountedItemStorage(ModMountedStorageTypes.SIMPLE_STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new SimpleStorageBoxMovementBehaviour()))
            .onRegister(connectedTextures(() -> new SimpleStorageBoxCTBehaviour(SpriteShifts.BAMBOO_CASING, ModBlocks.STORAGE_TRIM_BAMBOO)))
            
            
            
            .item(SimpleStorageBoxItem::new)
            
            .build()
            .register();

    public static final BlockEntry<SimpleStorageBox> SIMPLE_STORAGE_BOX_CRIMSON = REGISTRATE
            .block("crimson_simple_storage_box", SimpleStorageBox::new)
            .initialProperties(() -> Blocks.CRIMSON_PLANKS)
            .transform(mountedItemStorage(ModMountedStorageTypes.SIMPLE_STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new SimpleStorageBoxMovementBehaviour()))
            .onRegister(connectedTextures(() -> new SimpleStorageBoxCTBehaviour(SpriteShifts.CRIMSON_CASING, ModBlocks.STORAGE_TRIM_CRIMSON)))
            
            
            
            .item(SimpleStorageBoxItem::new)
            
            .build()
            .register();

    public static final BlockEntry<SimpleStorageBox> SIMPLE_STORAGE_BOX_WARPED = REGISTRATE
            .block("warped_simple_storage_box", SimpleStorageBox::new)
            .initialProperties(() -> Blocks.WARPED_PLANKS)
            .transform(mountedItemStorage(ModMountedStorageTypes.SIMPLE_STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new SimpleStorageBoxMovementBehaviour()))
            .onRegister(connectedTextures(() -> new SimpleStorageBoxCTBehaviour(SpriteShifts.WARPED_CASING, ModBlocks.STORAGE_TRIM_WARPED)))
            
            
            
            .item(SimpleStorageBoxItem::new)
            
            .build()
            .register();

    public static final BlockEntry<SimpleStorageBox> SIMPLE_STORAGE_BOX_PALE_OAK = REGISTRATE
            .block("pale_oak_simple_storage_box", SimpleStorageBox::new)
            .initialProperties(VanillaBackportCompat.planksOrOak(VanillaBackportCompat.PALE_OAK_PLANKS))
            .transform(mountedItemStorage(ModMountedStorageTypes.SIMPLE_STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new SimpleStorageBoxMovementBehaviour()))
            .onRegister(connectedTextures(() -> new SimpleStorageBoxCTBehaviour(SpriteShifts.PALE_OAK_CASING, ModBlocks.STORAGE_TRIM_PALE_OAK)))
            
            
            
            .item(SimpleStorageBoxItem::new)
            
            .build()
            .register();

    public static final BlockEntry<SimpleStorageBox> SIMPLE_STORAGE_BOX_POPLAR = REGISTRATE
            .block("poplar_simple_storage_box", SimpleStorageBox::new)
            .initialProperties(VanillaBackportCompat.planksOrOak(VanillaBackportCompat.POPLAR_PLANKS))
            .transform(mountedItemStorage(ModMountedStorageTypes.SIMPLE_STORAGE_BOX_MOUNTED))
            .onRegister(movementBehaviour(new SimpleStorageBoxMovementBehaviour()))
            .onRegister(connectedTextures(() -> new SimpleStorageBoxCTBehaviour(SpriteShifts.POPLAR_CASING, ModBlocks.STORAGE_TRIM_POPLAR)))
            
            
            
            .item(SimpleStorageBoxItem::new)
            
            .build()
            .register();


    // CASING BLOCKS //
    public static final BlockEntry<CasingBlock> STORAGE_TRIM_OAK = REGISTRATE
            .block("oak_storage_trim", CasingBlock::new)
            .properties(properties -> properties.mapColor(MapColor.PODZOL))
            .transform(ModBlockBuilderHelper.casing(() -> SpriteShifts.OAK_CASING, ModBlocks.SIMPLE_STORAGE_BOX_OAK))
            
            
            .removeTag(ProviderType.BLOCK_TAGS, AllTags.AllBlockTags.CASING.tag)
            .register();

    public static final BlockEntry<CasingBlock> STORAGE_TRIM_SPRUCE = REGISTRATE
            .block("spruce_storage_trim", CasingBlock::new)
            .properties(properties -> properties.mapColor(MapColor.PODZOL))
            .transform(ModBlockBuilderHelper.casing(() -> SpriteShifts.SPRUCE_CASING, ModBlocks.SIMPLE_STORAGE_BOX_SPRUCE))
            
            
            .removeTag(ProviderType.BLOCK_TAGS, AllTags.AllBlockTags.CASING.tag)
            .register();

    public static final BlockEntry<CasingBlock> STORAGE_TRIM_BIRCH = REGISTRATE
            .block("birch_storage_trim", CasingBlock::new)
            .properties(properties -> properties.mapColor(MapColor.PODZOL))
            .transform(ModBlockBuilderHelper.casing(() -> SpriteShifts.BIRCH_CASING, ModBlocks.SIMPLE_STORAGE_BOX_BIRCH))
            
            
            .removeTag(ProviderType.BLOCK_TAGS, AllTags.AllBlockTags.CASING.tag)
            .register();

    public static final BlockEntry<CasingBlock> STORAGE_TRIM_JUNGLE = REGISTRATE
            .block("jungle_storage_trim", CasingBlock::new)
            .properties(properties -> properties.mapColor(MapColor.PODZOL))
            .transform(ModBlockBuilderHelper.casing(() -> SpriteShifts.JUNGLE_CASING, ModBlocks.SIMPLE_STORAGE_BOX_JUNGLE))
            
            
            .removeTag(ProviderType.BLOCK_TAGS, AllTags.AllBlockTags.CASING.tag)
            .register();

    public static final BlockEntry<CasingBlock> STORAGE_TRIM_ACACIA = REGISTRATE
            .block("acacia_storage_trim", CasingBlock::new)
            .properties(properties -> properties.mapColor(MapColor.PODZOL))
            .transform(ModBlockBuilderHelper.casing(() -> SpriteShifts.ACACIA_CASING, ModBlocks.SIMPLE_STORAGE_BOX_ACACIA))
            
            
            .removeTag(ProviderType.BLOCK_TAGS, AllTags.AllBlockTags.CASING.tag)
            .register();

    public static final BlockEntry<CasingBlock> STORAGE_TRIM_DARK_OAK = REGISTRATE
            .block("dark_oak_storage_trim", CasingBlock::new)
            .properties(properties -> properties.mapColor(MapColor.PODZOL))
            .transform(ModBlockBuilderHelper.casing(() -> SpriteShifts.DARK_OAK_CASING, ModBlocks.SIMPLE_STORAGE_BOX_DARK_OAK))
            
            
            .removeTag(ProviderType.BLOCK_TAGS, AllTags.AllBlockTags.CASING.tag)
            .register();

    public static final BlockEntry<CasingBlock> STORAGE_TRIM_MANGROVE = REGISTRATE
            .block("mangrove_storage_trim", CasingBlock::new)
            .properties(properties -> properties.mapColor(MapColor.PODZOL))
            .transform(ModBlockBuilderHelper.casing(() -> SpriteShifts.MANGROVE_CASING, ModBlocks.SIMPLE_STORAGE_BOX_MANGROVE))
            
            
            .removeTag(ProviderType.BLOCK_TAGS, AllTags.AllBlockTags.CASING.tag)
            .register();

    public static final BlockEntry<CasingBlock> STORAGE_TRIM_CHERRY = REGISTRATE
            .block("cherry_storage_trim", CasingBlock::new)
            .properties(properties -> properties.mapColor(MapColor.PODZOL))
            .transform(ModBlockBuilderHelper.casing(() -> SpriteShifts.CHERRY_CASING, ModBlocks.SIMPLE_STORAGE_BOX_CHERRY))
            
            
            .removeTag(ProviderType.BLOCK_TAGS, AllTags.AllBlockTags.CASING.tag)
            .register();

    public static final BlockEntry<CasingBlock> STORAGE_TRIM_BAMBOO = REGISTRATE
            .block("bamboo_storage_trim", CasingBlock::new)
            .properties(properties -> properties.mapColor(MapColor.PODZOL))
            .transform(ModBlockBuilderHelper.casing(() -> SpriteShifts.BAMBOO_CASING, ModBlocks.SIMPLE_STORAGE_BOX_BAMBOO))
            
            
            .removeTag(ProviderType.BLOCK_TAGS, AllTags.AllBlockTags.CASING.tag)
            .register();

    public static final BlockEntry<CasingBlock> STORAGE_TRIM_CRIMSON = REGISTRATE
            .block("crimson_storage_trim", CasingBlock::new)
            .properties(properties -> properties.mapColor(MapColor.PODZOL))
            .transform(ModBlockBuilderHelper.casing(() -> SpriteShifts.CRIMSON_CASING, ModBlocks.SIMPLE_STORAGE_BOX_CRIMSON))
            
            
            .removeTag(ProviderType.BLOCK_TAGS, AllTags.AllBlockTags.CASING.tag)
            .setData(ProviderType.ITEM_TAGS, NonNullBiConsumer.noop())
            .register();

    public static final BlockEntry<CasingBlock> STORAGE_TRIM_WARPED = REGISTRATE
            .block("warped_storage_trim", CasingBlock::new)
            .properties(properties -> properties.mapColor(MapColor.PODZOL))
            .transform(ModBlockBuilderHelper.casing(() -> SpriteShifts.WARPED_CASING, ModBlocks.SIMPLE_STORAGE_BOX_WARPED))
            
            
            .removeTag(ProviderType.BLOCK_TAGS, AllTags.AllBlockTags.CASING.tag)
            .register();

    public static final BlockEntry<CasingBlock> STORAGE_TRIM_PALE_OAK = REGISTRATE
            .block("pale_oak_storage_trim", CasingBlock::new)
            .properties(properties -> properties.mapColor(MapColor.QUARTZ))
            .transform(ModBlockBuilderHelper.casing(() -> SpriteShifts.PALE_OAK_CASING, ModBlocks.SIMPLE_STORAGE_BOX_PALE_OAK))
            
            
            .removeTag(ProviderType.BLOCK_TAGS, AllTags.AllBlockTags.CASING.tag)
            .register();

    public static final BlockEntry<CasingBlock> STORAGE_TRIM_POPLAR = REGISTRATE
            .block("poplar_storage_trim", CasingBlock::new)
            .properties(properties -> properties.mapColor(MapColor.QUARTZ))
            .transform(ModBlockBuilderHelper.casing(() -> SpriteShifts.POPLAR_CASING, ModBlocks.SIMPLE_STORAGE_BOX_POPLAR))
            
            
            .removeTag(ProviderType.BLOCK_TAGS, AllTags.AllBlockTags.CASING.tag)
            .register();

    public static void register() {
    }
}
