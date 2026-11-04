package com.tom.createores;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.tom.createores.block.DrillBlock;
import com.tom.createores.block.ExtractorBlock;
import com.tom.createores.block.IOBlock;
import com.tom.createores.block.KineticInputBlock;
import com.tom.createores.block.MultiblockBlock;
import com.tom.createores.block.MultiblockPart;
import com.tom.createores.block.SampleDrillBlock;
import com.tom.createores.block.entity.DrillBlockEntity;
import com.tom.createores.block.entity.ExtractorBlockEntity;
import com.tom.createores.block.entity.IOBlockEntity;
import com.tom.createores.block.entity.KineticInputBlockEntity;
import com.tom.createores.block.entity.MultiblockBlockEntity;
import com.tom.createores.block.entity.SampleDrillBlockEntity;
import com.tom.createores.item.MultiBlockItem;
import com.tom.createores.item.OreVeinAtlasItem;
import com.tom.createores.item.OreVeinFinderItem;
import com.tom.createores.menu.OreVeinAtlasMenu;

/** Runtime registration using NeoForge's 1.21.11-safe deferred registers. */
public final class Registration {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateOreExcavation.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateOreExcavation.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateOreExcavation.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CreateOreExcavation.MODID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateOreExcavation.MODID);

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK);
    }
    private static BlockBehaviour.Properties metal(Identifier id) {
        return metal().setId(ResourceKey.create(Registries.BLOCK, id));
    }
    private static Item.Properties item(Identifier id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
    }

    public static final DeferredHolder<net.minecraft.world.level.block.Block, DrillBlock> DRILL_BLOCK = BLOCKS.register("drilling_machine", id -> new DrillBlock(MultiblockPart.props().apply(metal(id))));
    public static final DeferredHolder<net.minecraft.world.level.block.Block, KineticInputBlock> KINETIC_INPUT = BLOCKS.register("kinetic_input", id -> new KineticInputBlock(MultiblockPart.propsGhost().apply(metal(id))));
    public static final DeferredHolder<net.minecraft.world.level.block.Block, MultiblockBlock> GHOST_BLOCK = BLOCKS.register("multiblock", id -> new MultiblockBlock(MultiblockPart.propsGhost().apply(metal(id))));
    public static final DeferredHolder<net.minecraft.world.level.block.Block, IOBlock> IO_BLOCK = BLOCKS.register("io_block", id -> new IOBlock(MultiblockPart.propsGhost().apply(metal(id))));
    public static final DeferredHolder<net.minecraft.world.level.block.Block, ExtractorBlock> EXTRACTOR_BLOCK = BLOCKS.register("extractor", id -> new ExtractorBlock(MultiblockPart.props().apply(metal(id))));
    public static final DeferredHolder<net.minecraft.world.level.block.Block, SampleDrillBlock> SAMPLE_DRILL_BLOCK = BLOCKS.register("sample_drill", id -> new SampleDrillBlock(MultiblockPart.props().apply(metal(id))));

    public static final DeferredHolder<Item, Item> DRILL_BLOCK_ITEM = ITEMS.register("drilling_machine", id -> new MultiBlockItem(DRILL_BLOCK.get(), item(id).stacksTo(1)));
    public static final DeferredHolder<Item, Item> EXTRACTOR_BLOCK_ITEM = ITEMS.register("extractor", id -> new MultiBlockItem(EXTRACTOR_BLOCK.get(), item(id).stacksTo(1)));
    public static final DeferredHolder<Item, Item> SAMPLE_DRILL_BLOCK_ITEM = ITEMS.register("sample_drill", id -> new BlockItem(SAMPLE_DRILL_BLOCK.get(), item(id).stacksTo(1)));
    public static final DeferredHolder<Item, Item> NORMAL_DRILL_ITEM = ITEMS.register("drill", id -> new Item(item(id).stacksTo(1)));
    public static final DeferredHolder<Item, Item> DIAMOND_DRILL_ITEM = ITEMS.register("diamond_drill", id -> new Item(item(id).stacksTo(1)));
    public static final DeferredHolder<Item, Item> NETHERITE_DRILL_ITEM = ITEMS.register("netherite_drill", id -> new Item(item(id).stacksTo(1)));
    public static final DeferredHolder<Item, Item> RAW_DIAMOND = ITEMS.register("raw_diamond", id -> new Item(item(id)));
    public static final DeferredHolder<Item, Item> RAW_EMERALD = ITEMS.register("raw_emerald", id -> new Item(item(id)));
    public static final DeferredHolder<Item, Item> RAW_REDSTONE = ITEMS.register("raw_redstone", id -> new Item(item(id)));
    public static final DeferredHolder<Item, OreVeinFinderItem> VEIN_FINDER_ITEM = ITEMS.register("vein_finder", id -> new OreVeinFinderItem(item(id).stacksTo(1)));
    public static final DeferredHolder<Item, OreVeinAtlasItem> VEIN_ATLAS_ITEM = ITEMS.register("vein_atlas", id -> new OreVeinAtlasItem(item(id).stacksTo(1)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DrillBlockEntity>> DRILL_TILE = BLOCK_ENTITIES.register("drill", () -> new BlockEntityType<>(Registration::createDrill, DRILL_BLOCK.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KineticInputBlockEntity>> KINETIC_INPUT_TILE = BLOCK_ENTITIES.register("kinetic_input", () -> new BlockEntityType<>(Registration::createKineticInput, KINETIC_INPUT.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultiblockBlockEntity>> GHOST_TILE = BLOCK_ENTITIES.register("multiblock", () -> new BlockEntityType<>(Registration::createGhost, GHOST_BLOCK.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IOBlockEntity>> IO_TILE = BLOCK_ENTITIES.register("io", () -> new BlockEntityType<>(Registration::createIo, IO_BLOCK.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtractorBlockEntity>> EXTRACTOR_TILE = BLOCK_ENTITIES.register("extractor", () -> new BlockEntityType<>(Registration::createExtractor, EXTRACTOR_BLOCK.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SampleDrillBlockEntity>> SAMPLE_DRILL_TILE = BLOCK_ENTITIES.register("sample_drill", () -> new BlockEntityType<>(Registration::createSampleDrill, SAMPLE_DRILL_BLOCK.get()));
    public static final DeferredHolder<MenuType<?>, MenuType<OreVeinAtlasMenu>> VEIN_ATLAS_MENU = MENUS.register("vein_atlas", () -> new MenuType<>(Registration::createAtlasMenu, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("create_ore_excavation", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.createoreexcavation"))
            .icon(() -> new ItemStack(DIAMOND_DRILL_ITEM.get()))
            .displayItems((parameters, output) -> {
                output.accept(DRILL_BLOCK_ITEM.get()); output.accept(EXTRACTOR_BLOCK_ITEM.get()); output.accept(SAMPLE_DRILL_BLOCK_ITEM.get());
                output.accept(NORMAL_DRILL_ITEM.get()); output.accept(DIAMOND_DRILL_ITEM.get()); output.accept(NETHERITE_DRILL_ITEM.get());
                output.accept(RAW_DIAMOND.get()); output.accept(RAW_EMERALD.get()); output.accept(RAW_REDSTONE.get());
                output.accept(VEIN_FINDER_ITEM.get()); output.accept(VEIN_ATLAS_ITEM.get());
            }).build());

    private Registration() {}

    private static OreVeinAtlasMenu createAtlasMenu(int id, net.minecraft.world.entity.player.Inventory inventory) {
        return new OreVeinAtlasMenu(VEIN_ATLAS_MENU.get(), id, inventory);
    }
    private static DrillBlockEntity createDrill(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) { return new DrillBlockEntity(DRILL_TILE.get(), pos, state); }
    private static KineticInputBlockEntity createKineticInput(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) { return new KineticInputBlockEntity(KINETIC_INPUT_TILE.get(), pos, state); }
    private static MultiblockBlockEntity createGhost(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) { return new MultiblockBlockEntity(GHOST_TILE.get(), pos, state); }
    private static IOBlockEntity createIo(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) { return new IOBlockEntity(IO_TILE.get(), pos, state); }
    private static ExtractorBlockEntity createExtractor(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) { return new ExtractorBlockEntity(EXTRACTOR_TILE.get(), pos, state); }
    private static SampleDrillBlockEntity createSampleDrill(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) { return new SampleDrillBlockEntity(SAMPLE_DRILL_TILE.get(), pos, state); }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus); ITEMS.register(bus); BLOCK_ENTITIES.register(bus); MENUS.register(bus); TABS.register(bus);
    }

    /** All shipped translations are generated resources; kept for source compatibility. */
    public static void add(String key, String value) {}
    public static void postRegister() {}
}
