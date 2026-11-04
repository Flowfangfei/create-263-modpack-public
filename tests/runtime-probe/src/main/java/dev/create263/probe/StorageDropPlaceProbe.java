package dev.create263.probe;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Real loot and BlockItem placement checks. Caller must first guard its disposable project world. */
public final class StorageDropPlaceProbe {
    private static final List<String> IDS = List.of(
            "fxntstorage:hardened_backpack", "fxntstorage:hardened_storage_box",
            "fxntstorage:oak_simple_storage_box", "fxntstorage:reserve_storage_box");
    private StorageDropPlaceProbe() {}

    public static Map<String, Object> run(ServerLevel level) throws Exception {
        require(Boolean.getBoolean("create263.fiveAddonsProbe"), "Missing explicit runtime probe flag");
        require(level == level.getServer().overworld(), "Drop/place probe requires the guarded overworld");
        FakePlayer player = FakePlayerFactory.get(level, new GameProfile(
                UUID.fromString("7ea3b569-55d1-48bd-a344-1eb5883fce02"), "StorageDropProbe"));
        player.setGameMode(GameType.SURVIVAL);
        List<Map<String, Object>> fixtures = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        try {
            for (int index = 0; index < IDS.size(); index++) {
                String id = IDS.get(index);
                BlockPos source = new BlockPos(256 + index * 12, 8, 20);
                BlockPos destination = source.offset(4, 0, 0);
                List<BlockPos> placed = new ArrayList<>();
                List<BlockPos> supports = new ArrayList<>();
                try {
                    prepare(level, source, supports);
                    prepare(level, destination, supports);
                    Map<String, Object> result = fixture(level, player, id, source, destination, placed);
                    result.put("passed", true);
                    fixtures.add(result);
                } catch (Exception failure) {
                    Throwable cause = unwrap(failure);
                    fixtures.add(Map.of("block", id, "passed", false, "error", cause.toString()));
                    failures.add(id + ": " + cause);
                } finally {
                    // Every removed block was created by this invocation in its reserved blank fixture cells.
                    for (BlockPos pos : placed) level.removeBlock(pos, false);
                    for (BlockPos pos : supports) level.removeBlock(pos, false);
                }
            }
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("schema", 1);
        result.put("fixtures", fixtures);
        result.put("passed", failures.isEmpty());
        result.put("remaining", List.of("Player mining/drop-spawn events and save/restart are outside this loot-and-placement check."));
        if (!failures.isEmpty()) throw new IllegalStateException("Storage real drop/place failed: " + fixtures);
        return result;
    }

    private static Map<String, Object> fixture(ServerLevel level, FakePlayer player, String id,
            BlockPos source, BlockPos destination, List<BlockPos> placed) throws Exception {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
        require(item instanceof BlockItem, "Missing Storage BlockItem: " + id);
        BlockItem blockItem = (BlockItem) item;
        boolean backpack = id.equals("fxntstorage:hardened_backpack");
        ItemStack shell = new ItemStack(item);
        shell.set(DataComponents.CUSTOM_NAME, Component.literal("drop-place shell " + id));
        if (backpack) {
            shell.set(DataComponents.CUSTOM_DATA, data("shell", id));
            shell.set(DataComponents.RARITY, Rarity.RARE);
            shell.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            require(shell.has(DataComponents.ITEM_NAME), "Backpack fixture needs its default ITEM_NAME");
            shell.remove(DataComponents.ITEM_NAME);
            require(shell.getComponentsPatch().split().removed().contains(DataComponents.ITEM_NAME),
                    "Backpack fixture lacks an explicit removal patch");
        }
        ItemStack expectedShell = shell.copy();
        place(level, player, shell, source, placed);
        BlockEntity sourceEntity = entity(level, source);
        ResourceHandler<ItemResource> sourceCapability = capability(level, source);
        ItemStack prototype = new ItemStack(Items.DIAMOND);
        prototype.set(DataComponents.CUSTOM_NAME, Component.literal("drop-place contents " + id));
        prototype.set(DataComponents.CUSTOM_DATA, data("contents", id));
        prototype.set(DataComponents.RARITY, Rarity.EPIC);
        prototype.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        prototype.remove(DataComponents.ITEM_NAME);
        ItemResource resource = ItemResource.of(prototype);
        long capacity = 0;
        for (int slot = 0; slot < sourceCapability.size(); slot++)
            capacity = Math.addExact(capacity, sourceCapability.getCapacityAsLong(slot, resource));
        int amount = (int) Math.min(257L, capacity);
        require(amount > 99, "Fixture capacity must permit a quantity above 99: " + capacity);
        try (Transaction transaction = Transaction.openRoot()) {
            equal(amount, sourceCapability.insert(resource, amount, transaction), "Native insertion");
            transaction.commit();
        }
        checkCapability(sourceCapability, prototype, amount, "Source native capability");
        List<ItemStack> expected = stacks(sourceEntity);
        checkInventory(expected, prototype, amount, "Source private inventory");
        ItemStack drop = drop(level, source, player, blockItem);
        shell(expectedShell, drop, backpack, "First real loot");
        inventory(expected, contents(drop), "First loot private contents");
        noShadowInventory(drop);
        var ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        Tag encoded = ItemStack.CODEC.encodeStart(ops, drop).getOrThrow();
        ItemStack decoded = ItemStack.CODEC.parse(ops, encoded).getOrThrow();
        same(drop, decoded, "Native outer ItemStack.CODEC");
        shell(expectedShell, decoded, backpack, "Native codec shell");
        inventory(expected, contents(decoded), "Native codec private contents");
        level.removeBlock(source, false);
        require(level.getBlockEntity(source) == null, "Source BE survived removal");
        place(level, player, decoded, destination, placed);
        BlockEntity destinationEntity = entity(level, destination);
        require(destinationEntity.getType() == sourceEntity.getType(), "Placed BE type changed");
        inventory(expected, stacks(destinationEntity), "Actual BlockItem.place private inventory");
        checkCapability(capability(level, destination), prototype, amount, "Placed native capability");
        ItemStack secondDrop = drop(level, destination, player, blockItem);
        shell(expectedShell, secondDrop, backpack, "Placed block real loot");
        inventory(expected, contents(secondDrop), "Placed block second loot contents");
        noShadowInventory(secondDrop);
        ItemStack secondDecoded = ItemStack.CODEC.parse(ops,
                ItemStack.CODEC.encodeStart(ops, secondDrop).getOrThrow()).getOrThrow();
        same(secondDrop, secondDecoded, "Placed block second native codec");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("block", id);
        result.put("source", source.toShortString());
        result.put("destination", destination.toShortString());
        result.put("entity_type", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(sourceEntity.getType()).toString());
        result.put("native_capacity", capacity);
        result.put("quantity", amount);
        result.put("maximum_private_slot_quantity", expected.stream().mapToInt(ItemStack::getCount).max().orElse(0));
        result.put("native_capability_insert", true);
        result.put("actual_loot_table", true);
        result.put("native_itemstack_codec", true);
        result.put("actual_blockitem_place", true);
        result.put("second_actual_loot_table", true);
        result.put("inventory_types_counts_names_full_components_and_removals", true);
        result.put("shell_custom_name", true);
        if (backpack) result.put("backpack_shell_custom_components_and_explicit_removal", true);
        return result;
    }

    private static void prepare(ServerLevel level, BlockPos pos, List<BlockPos> supports) {
        level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
        level.getChunkAt(pos);
        require(level.getBlockState(pos).isAir(), "Reserved fixture cell is occupied: " + pos + " state=" + level.getBlockState(pos));
        BlockPos below = pos.below();
        if (level.getBlockState(below).isAir()) {
            require(level.setBlock(below, Blocks.STONE.defaultBlockState(), 3), "Could not create fixture support");
            supports.add(below);
        }
    }

    private static void place(ServerLevel level, FakePlayer player, ItemStack stack, BlockPos target,
            List<BlockPos> placed) {
        require(stack.getItem() instanceof BlockItem, "Cannot place non-block shell");
        require(level.getBlockState(target).isAir(), "Placement target became occupied: " + target);
        BlockItem item = (BlockItem) stack.getItem();
        player.setPos(target.getX() + 0.5, target.getY(), target.getZ() + 3.5);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos support = target.below();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(support).add(0, 0.5, 0),
                Direction.UP, support, false);
        BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit);
        require(context.getClickedPos().equals(target), "BlockPlaceContext resolved the wrong target");
        InteractionResult result;
        try {
            result = item.place(context);
        } finally {
            if (level.getBlockState(target).is(item.getBlock())) placed.add(target);
        }
        require(result.consumesAction(), "Actual BlockItem.place failed: " + result);
        require(level.getBlockState(target).is(item.getBlock()), "Actual placement produced the wrong block");
        require(stack.isEmpty(), "Survival BlockItem.place did not consume its single shell");
    }

    private static ItemStack drop(ServerLevel level, BlockPos pos, FakePlayer player, BlockItem item) {
        List<ItemStack> drops = Block.getDrops(level.getBlockState(pos), level, pos,
                entity(level, pos), player, new ItemStack(Items.DIAMOND_PICKAXE));
        require(drops.size() == 1, "Expected exactly one real loot shell at " + pos + ": " + drops);
        ItemStack drop = drops.getFirst();
        require(drop.is(item) && drop.getCount() == 1, "Real loot shell item/count changed: " + drop);
        return drop;
    }

    private static BlockEntity entity(ServerLevel level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        require(entity != null, "Missing Storage BE at " + pos);
        return entity;
    }

    private static ResourceHandler<ItemResource> capability(ServerLevel level, BlockPos pos) {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, null);
        require(handler != null, "Missing registered native block item capability at " + pos);
        return handler;
    }

    private static void checkCapability(ResourceHandler<ItemResource> handler, ItemStack prototype,
            long amount, String label) {
        long total = 0;
        for (int slot = 0; slot < handler.size(); slot++) {
            ItemResource value = handler.getResource(slot);
            long count = handler.getAmountAsLong(slot);
            if (value.isEmpty()) require(count == 0, label + " empty resource has a count");
            else {
                require(value.matches(prototype), label + " resource/components changed at " + slot);
                require(value.getComponentsPatch().equals(prototype.getComponentsPatch()),
                        label + " component removal patch changed at " + slot);
                require(count > 0, label + " nonempty resource lacks quantity");
                total = Math.addExact(total, count);
            }
        }
        equal(amount, total, label + " total");
    }

    @SuppressWarnings("unchecked")
    private static List<ItemStack> stacks(BlockEntity entity) throws Exception {
        Object handler = invoke(entity, "getItemHandler");
        int size = ((Number) invoke(handler, "getSlots")).intValue();
        List<ItemStack> result = new ArrayList<>();
        for (int slot = 0; slot < size; slot++)
            result.add(((ItemStack) invoke(handler, "getStackInSlot", slot)).copy());
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<ItemStack> contents(ItemStack stack) throws Exception {
        DataComponentType<Object> type = (DataComponentType<Object>) Class.forName(
                "net.fxnt.fxntstorage.init.ModDataComponents").getField("STORAGE_CONTENTS").get(null);
        Object contents = stack.get(type);
        require(contents != null, "Real loot lacks private Storage contents");
        int size = ((Number) invoke(contents, "getSlots")).intValue();
        List<ItemStack> result = new ArrayList<>();
        for (int slot = 0; slot < size; slot++) result.add((ItemStack) invoke(contents, "getStackInSlot", slot));
        return result;
    }

    private static void checkInventory(List<ItemStack> inventory, ItemStack prototype, long amount, String label) {
        long total = 0;
        for (ItemStack value : inventory) if (!value.isEmpty()) {
            same(prototype.copyWithCount(value.getCount()), value, label);
            total = Math.addExact(total, value.getCount());
        }
        equal(amount, total, label + " total");
    }

    private static void inventory(List<ItemStack> expected, List<ItemStack> actual, String label) {
        equal(expected.size(), actual.size(), label + " slots");
        for (int slot = 0; slot < expected.size(); slot++) same(expected.get(slot), actual.get(slot), label + " slot " + slot);
    }

    private static void shell(ItemStack expected, ItemStack actual, boolean backpack, String label) {
        require(actual.is(expected.getItem()) && actual.getCount() == 1, label + " item/count changed");
        require(java.util.Objects.equals(expected.get(DataComponents.CUSTOM_NAME), actual.get(DataComponents.CUSTOM_NAME)),
                label + " custom shell name changed");
        if (backpack) {
            require(java.util.Objects.equals(expected.get(DataComponents.CUSTOM_DATA), actual.get(DataComponents.CUSTOM_DATA)), label + " shell custom data changed");
            require(java.util.Objects.equals(expected.get(DataComponents.RARITY), actual.get(DataComponents.RARITY)), label + " shell rarity changed");
            require(java.util.Objects.equals(expected.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE), actual.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)), label + " shell glint changed");
            require(!actual.has(DataComponents.ITEM_NAME)
                    && actual.getComponentsPatch().split().removed().contains(DataComponents.ITEM_NAME),
                    label + " explicit ITEM_NAME removal was lost");
        }
    }

    private static void noShadowInventory(ItemStack stack) {
        require(!stack.has(DataComponents.CONTAINER), "Storage shell contains a vanilla CONTAINER shadow");
        var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        require(data == null || !data.copyTagWithoutId().contains("Items"), "Shell BE data duplicates private inventory");
    }

    private static CustomData data(String role, String id) {
        CompoundTag data = new CompoundTag();
        data.putString("probe", "storage-real-drop-place-v1");
        data.putString("role", role);
        data.putString("fixture", id);
        data.putInt("marker", 263);
        return CustomData.of(data);
    }

    private static void same(ItemStack expected, ItemStack actual, String label) {
        require(ItemStack.matches(expected, actual), label + " item/count/full components changed: " + expected + " -> " + actual);
        require(expected.getComponentsPatch().equals(actual.getComponentsPatch()), label + " full component patch changed");
        require(expected.getHoverName().equals(actual.getHoverName()), label + " display name changed");
    }

    private static Object invoke(Object receiver, String name, Object... args) throws Exception {
        for (Method method : receiver.getClass().getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != args.length) continue;
            method.setAccessible(true);
            return method.invoke(receiver, args);
        }
        throw new NoSuchMethodException(receiver.getClass().getName() + "." + name);
    }

    private static Throwable unwrap(Throwable failure) {
        while (failure instanceof InvocationTargetException && failure.getCause() != null) failure = failure.getCause();
        return failure;
    }
    private static void equal(long expected, long actual, String label) { require(expected == actual, label + ": " + expected + " != " + actual); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
