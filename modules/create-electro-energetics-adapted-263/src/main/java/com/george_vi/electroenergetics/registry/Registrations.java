package com.george_vi.electroenergetics.registry;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/** Runtime registration only. Models, recipes, loot and tags are shipped as resources. */
public final class Registrations {
    private final List<Runnable> blockItems = new ArrayList<>();

    public <T extends Block> BlockBuilder<T> block(
            String name, Function<BlockBehaviour.Properties, T> factory) {
        return new BlockBuilder<>(CreateElectroEnergetics.rl(name), factory);
    }

    public <T extends Item> ItemBuilder<T> item(String name, Function<Item.Properties, T> factory) {
        return new ItemBuilder<>(CreateElectroEnergetics.rl(name), factory);
    }

    public <T extends BlockEntity> BlockEntityBuilder<T> blockEntity(
            String name, BlockEntityFactory<T> factory) {
        return new BlockEntityBuilder<>(CreateElectroEnergetics.rl(name), factory);
    }

    public void registerBlockItems() {
        blockItems.forEach(Runnable::run);
        blockItems.clear();
    }

    public static <T extends Block> Consumer<T> movementBehaviour(MovementBehaviour behaviour) {
        return block -> MovementBehaviour.REGISTRY.register(block, behaviour);
    }

    @FunctionalInterface
    public interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
    }

    public final class BlockBuilder<T extends Block> {
        private final Identifier id;
        private final Function<BlockBehaviour.Properties, T> factory;
        private BlockBehaviour.Properties properties = BlockBehaviour.Properties.of();
        private final List<Consumer<T>> callbacks = new ArrayList<>();
        private BiFunction<T, Item.Properties, ? extends BlockItem> itemFactory;
        private Item.Properties itemProperties = new Item.Properties();

        private BlockBuilder(Identifier id, Function<BlockBehaviour.Properties, T> factory) {
            this.id = id;
            this.factory = factory;
        }

        public BlockBuilder<T> initialProperties(Supplier<BlockBehaviour.Properties> initial) {
            properties = initial.get();
            return this;
        }

        public BlockBuilder<T> properties(UnaryOperator<BlockBehaviour.Properties> change) {
            properties = change.apply(properties);
            return this;
        }

        public BlockBuilder<T> onRegister(Consumer<T> callback) {
            callbacks.add(callback);
            return this;
        }

        public BlockItemBuilder<T> item() {
            return item(BlockItem::new);
        }

        public BlockItemBuilder<T> item(
                BiFunction<T, Item.Properties, ? extends BlockItem> itemFactory) {
            this.itemFactory = itemFactory;
            return new BlockItemBuilder<>(this);
        }

        public BlockEntry<T> register() {
            ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
            T block =
                    Registry.register(
                            BuiltInRegistries.BLOCK, id, factory.apply(properties.setId(key)));
            BlockEntry<T> entry = new BlockEntry<>(id, () -> block);
            callbacks.forEach(callback -> callback.accept(block));
            if (itemFactory != null) {
                blockItems.add(
                        () -> {
                            Item.Properties props =
                                    itemProperties
                                            .setId(ResourceKey.create(Registries.ITEM, id))
                                            .useBlockDescriptionPrefix()
                                            .requiredFeatures(block.requiredFeatures());
                            BlockItem item = itemFactory.apply(block, props);
                            item.registerBlocks(Item.BY_BLOCK, item);
                            Registry.register(BuiltInRegistries.ITEM, id, item);
                        });
            }
            return entry;
        }
    }

    public final class BlockItemBuilder<T extends Block> {
        private final BlockBuilder<T> parent;

        private BlockItemBuilder(BlockBuilder<T> parent) {
            this.parent = parent;
        }

        public BlockItemBuilder<T> properties(UnaryOperator<Item.Properties> change) {
            parent.itemProperties = change.apply(parent.itemProperties);
            return this;
        }

        public BlockBuilder<T> build() {
            return parent;
        }

        public BlockEntry<T> register() {
            return parent.register();
        }
    }

    public final class ItemBuilder<T extends Item> {
        private final Identifier id;
        private final Function<Item.Properties, T> factory;
        private Item.Properties properties = new Item.Properties();

        private ItemBuilder(Identifier id, Function<Item.Properties, T> factory) {
            this.id = id;
            this.factory = factory;
        }

        public ItemBuilder<T> properties(UnaryOperator<Item.Properties> change) {
            properties = change.apply(properties);
            return this;
        }

        public ItemEntry<T> register() {
            T item =
                    Registry.register(
                            BuiltInRegistries.ITEM,
                            id,
                            factory.apply(
                                    properties.setId(ResourceKey.create(Registries.ITEM, id))));
            return new ItemEntry<>(id, () -> item);
        }
    }

    public final class BlockEntityBuilder<T extends BlockEntity> {
        private final Identifier id;
        private final BlockEntityFactory<T> factory;
        private final List<Supplier<? extends Block>> validBlocks = new ArrayList<>();
        private final List<Supplier<? extends DisplaySource>> displaySources = new ArrayList<>();

        private BlockEntityBuilder(Identifier id, BlockEntityFactory<T> factory) {
            this.id = id;
            this.factory = factory;
        }

        public BlockEntityBuilder<T> validBlock(Supplier<? extends Block> block) {
            validBlocks.add(block);
            return this;
        }

        @SafeVarargs
        public final BlockEntityBuilder<T> validBlocks(Supplier<? extends Block>... blocks) {
            validBlocks.addAll(Arrays.asList(blocks));
            return this;
        }

        public BlockEntityBuilder<T> displaySource(Supplier<? extends DisplaySource> source) {
            displaySources.add(source);
            return this;
        }

        @SuppressWarnings("unchecked")
        public BlockEntityEntry<T> register() {
            Set<Block> blocks = new LinkedHashSet<>();
            validBlocks.forEach(block -> blocks.add(block.get()));
            BlockEntityType<T>[] reference = (BlockEntityType<T>[]) new BlockEntityType<?>[1];
            BlockEntityType<T> type =
                    new BlockEntityType<>(
                            (pos, state) -> factory.create(reference[0], pos, state), blocks);
            reference[0] = type;
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type);
            displaySources.forEach(source -> DisplaySource.BY_BLOCK_ENTITY.add(type, source.get()));
            return new BlockEntityEntry<>(id, () -> type);
        }
    }
}
