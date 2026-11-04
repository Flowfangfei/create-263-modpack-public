package dev.create263.probe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/** Natural compaction requests and real replies, after the guarded Storage menu interaction. */
@SuppressWarnings("unchecked")
final class StorageClientCompactionProbe {
    private static final String HELPER = "net.fxnt.fxntstorage.simple_storage.CompactingRecipeHelper";
    private static final BlockPos BACKPACK = new BlockPos(176, -60, 8);
    private static final Item[] FILTERS = {Items.IRON_INGOT, Items.COAL, Items.DIAMOND_SWORD};
    private static final Map<String, Object> result = new LinkedHashMap<>();
    private static final Map<Item, Object> clientChains = new LinkedHashMap<>();
    private static Map<Level, Map<Item, Optional<Object>>> serverCache;
    private static Map<Item, Optional<Object>> clientCache;
    private static Map<Item, Long> pending;
    private static Method buildChain;
    private static CompletableFuture<ServerSnapshot> preparation;
    private static CompletableFuture<Map<Item, Optional<Object>>> confirmation;
    private static ServerSnapshot serverBefore;
    private static InventorySnapshot clientBefore;
    private static BlockEntity clientBackpack;
    private static BlockState clientBackpackState;
    private static MinecraftServer owner;
    private static java.util.UUID playerId;
    private static int phase, ticks, totalTicks;
    private static boolean finished;
    private StorageClientCompactionProbe() {}

    static boolean finished() { return finished; }
    static Map<String, Object> report() { return Map.copyOf(result); }

    static void tick(Minecraft mc) {
        if (finished) return;
        try {
            guard(mc);
            totalTicks++;
            require(++ticks <= 240, "Compaction protocol timed out in phase " + phase);
            advance(mc);
        } catch (RuntimeException | Error failure) {
            result.put("passed", false);
            result.put("failed_phase", phase);
            result.put("error", failure.toString());
            finished = true;
            throw failure;
        }
    }

    private static void advance(Minecraft mc) {
        if (phase == 0) {
            owner = mc.getSingleplayerServer();
            playerId = mc.player.getUUID();
            require(mc.player.containerMenu == mc.player.inventoryMenu, "Close the Storage menu before the compaction protocol test");
            require(mc.player.containerMenu.getCarried().isEmpty(), "Compaction test started with a carried item");
            Class<?> helper = type(HELPER);
            serverCache = (Map<Level, Map<Item, Optional<Object>>>) staticField(helper, "SERVER_CACHE");
            clientCache = (Map<Item, Optional<Object>>) staticField(helper, "CLIENT_CACHE");
            pending = (Map<Item, Long>) staticField(helper, "CLIENT_REQUESTS");
            try { buildChain = helper.getMethod("buildChain", Level.class, Item.class); }
            catch (ReflectiveOperationException error) { throw new IllegalStateException("Missing natural compaction entry", error); }
            clientBefore = inventory(mc.player);
            clientBackpack = mc.level.getBlockEntity(BACKPACK);
            require(clientBackpack != null, "Existing client backpack fixture vanished");
            clientBackpackState = mc.level.getBlockState(BACKPACK);
            preparation = new CompletableFuture<>();
            owner.execute(() -> {
                try {
                    ServerPlayer player = ownedPlayer();
                    Map<Item, Optional<Object>> cache = serverCache.get(owner.overworld());
                    if (cache != null) for (Item item : FILTERS) cache.remove(item);
                    if (cache != null) for (Item item : FILTERS) require(!cache.containsKey(item), "Server test filter was not evicted: " + id(item));
                    ServerSnapshot snapshot = serverSnapshot(player);
                    require(!snapshot.amounts().isEmpty() && snapshot.amounts().getFirst() == 1024L, "Server backpack fixture does not hold the conserved 1024 items");
                    preparation.complete(snapshot);
                } catch (Throwable failure) { preparation.completeExceptionally(failure); }
            });
            phase = 1; ticks = 0; return;
        }
        if (phase == 1) {
            if (!preparation.isDone()) return;
            serverBefore = preparation.join();
            // Evict only these test keys. Global rebuild would disturb other fixture caches.
            for (Item item : FILTERS) { clientCache.remove(item); pending.remove(item); }
            for (Item item : FILTERS) {
                require(!clientCache.containsKey(item), "Client filter is already cached: " + id(item));
                require(naturalBuild(mc.level, item) == null, "Uncached natural request returned a chain synchronously: " + id(item));
                require(pending.containsKey(item), "Natural entry did not register a pending request: " + id(item));
            }
            result.put("fresh_natural_requests", List.of("minecraft:iron_ingot", "minecraft:coal", "minecraft:diamond_sword"));
            phase = 2; ticks = 0; return;
        }
        if (phase == 2) {
            for (Item item : FILTERS) if (!clientCache.containsKey(item) || pending.containsKey(item)) return;
            Optional<Object> iron = clientCache.get(Items.IRON_INGOT);
            Optional<Object> coal = clientCache.get(Items.COAL);
            Optional<Object> sword = clientCache.get(Items.DIAMOND_SWORD);
            require(iron != null && iron.isPresent(), "Client did not receive a positive iron chain");
            require(coal != null && coal.isPresent(), "Client did not receive a positive coal chain");
            require(sword != null && sword.isEmpty(), "Client did not receive an explicit negative sword cache entry");
            clientChains.put(Items.IRON_INGOT, iron.orElseThrow());
            clientChains.put(Items.COAL, coal.orElseThrow());
            validateIron(iron.orElseThrow());
            validateCoal(coal.orElseThrow());
            confirmation = new CompletableFuture<>();
            owner.execute(() -> {
                try {
                    ServerPlayer player = ownedPlayer();
                    require(serverBefore.equals(serverSnapshot(player)), "Compaction requests mutated server player inventory, backpack resources/counts, carried item, or fixture identity");
                    Map<Item, Optional<Object>> cache = serverCache.get(owner.overworld());
                    require(cache != null, "Server did not build any compaction cache for the actual overworld");
                    Map<Item, Optional<Object>> replies = new LinkedHashMap<>();
                    for (Item item : FILTERS) {
                        require(cache.containsKey(item), "Server did not build the requested filter: " + id(item));
                        replies.put(item, cache.get(item));
                    }
                    confirmation.complete(Map.copyOf(replies));
                } catch (Throwable failure) { confirmation.completeExceptionally(failure); }
            });
            phase = 3; ticks = 0; return;
        }
        if (phase == 3) {
            if (!confirmation.isDone()) return;
            Map<Item, Optional<Object>> serverReplies = confirmation.join();
            List<Map<String, Object>> chains = new ArrayList<>();
            for (Item item : List.of(Items.IRON_INGOT, Items.COAL)) {
                Object client = clientChains.get(item);
                Object server = serverReplies.get(item).orElseThrow();
                require(client != server, "Client reused the server object instead of reconstructing the wire reply: " + id(item));
                require(client.equals(server), "Server/client chain fields or variant sets differ: " + id(item));
                require(naturalBuild(mc.level, item) == client, "Natural entry did not reuse the received client chain: " + id(item));
                Map<String, Object> entry = describe(client);
                entry.put("filter", id(item));
                entry.put("server_fields_equal", true);
                entry.put("wire_reconstructed_distinct_object", true);
                chains.add(entry);
            }
            require(serverReplies.get(Items.DIAMOND_SWORD).isEmpty(), "Server did not confirm the negative sword result");
            require(naturalBuild(mc.level, Items.DIAMOND_SWORD) == null, "Negative result changed on cached lookup");
            for (Item item : FILTERS) require(!pending.containsKey(item), "Cached lookup resubmitted a network request: " + id(item));
            require(inventoriesEqual(clientBefore, inventory(mc.player)), "Compaction request/reply mutated client inventory components, counts, or cursor");
            require(mc.level.getBlockEntity(BACKPACK) == clientBackpack && mc.level.getBlockState(BACKPACK).equals(clientBackpackState), "Compaction changed client backpack fixture identity/state");
            result.put("chains", chains);
            result.put("negative_filter", "minecraft:diamond_sword");
            result.put("negative_reply_cached_without_retry", true);
            result.put("server_build_and_client_receive", true);
            result.put("server_player_and_backpack_quantity_components_identity_unchanged", true);
            result.put("client_inventory_components_and_fixture_identity_unchanged", true);
            result.put("elapsed_client_ticks", totalTicks);
            result.put("uncovered", List.of("Compaction box insertion/extraction and tier-scroll packets", "Optional-mod variant recipes and recipe/datapack reload invalidation", "Remote dedicated-server transport"));
            result.put("passed", true);
            finished = true;
        }
    }

    private static void validateIron(Object chain) {
        require(value(chain, "t0") == Items.IRON_NUGGET && value(chain, "t1") == Items.IRON_INGOT && value(chain, "t2") == Items.IRON_BLOCK, "Iron chain did not resolve canonical vanilla tiers");
        require(integer(chain, "tiers") == 3 && integer(chain, "t0ToT1") == 9 && integer(chain, "t1ToT2") == 9, "Iron ratios were not 9:9");
        require(integer(chain, "highestTierT0PerUnit") == 81, "Iron block did not represent 81 nuggets");
        require(number(invoke(chain, "toT0Units", new Class<?>[]{Item.class, int.class}, Items.IRON_INGOT, 10)) == 90, "Ten iron ingots lost T0 units");
        Object highest = invoke(chain, "toHighestTier", new Class<?>[]{int.class}, 1000);
        int remainder = number(invoke(chain, "remainderAfterHighestTier", new Class<?>[]{int.class}, 1000));
        require(value(highest, "item") == Items.IRON_BLOCK && integer(highest, "count") == 12 && remainder == 28, "Iron decomposition of 1000 nuggets is wrong");
        require(integer(highest, "count") * 81 + (remainder / 9) * 9 + remainder % 9 == 1000, "Iron compaction arithmetic did not conserve T0 units");
    }
    private static void validateCoal(Object chain) {
        require(value(chain, "t0") == Items.COAL && value(chain, "t1") == Items.COAL_BLOCK && value(chain, "t2") == null, "Coal reply did not preserve the two-tier null upper tier");
        require(integer(chain, "tiers") == 2 && integer(chain, "t0ToT1") == 9 && integer(chain, "t1ToT2") == 1, "Coal ratios were not 9:1");
        Object highest = invoke(chain, "toHighestTier", new Class<?>[]{int.class}, 100);
        int remainder = number(invoke(chain, "remainderAfterHighestTier", new Class<?>[]{int.class}, 100));
        require(value(highest, "item") == Items.COAL_BLOCK && integer(highest, "count") == 11 && remainder == 1, "Coal compaction arithmetic lost T0 units");
    }
    private static Map<String, Object> describe(Object chain) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("tiers", value(chain, "tiers"));
        fields.put("t0", id((Item) value(chain, "t0")));
        fields.put("t1", id((Item) value(chain, "t1")));
        fields.put("t2", value(chain, "t2") == null ? "none" : id((Item) value(chain, "t2")));
        fields.put("t0_to_t1", value(chain, "t0ToT1"));
        fields.put("t1_to_t2", value(chain, "t1ToT2"));
        fields.put("lower_variants", itemIds(value(chain, "t0Variants")));
        fields.put("upper_variants", itemIds(value(chain, "t1Variants")));
        return fields;
    }
    private static List<String> itemIds(Object values) { return ((java.util.Set<Item>) values).stream().map(StorageClientCompactionProbe::id).sorted().toList(); }
    private static void guard(Minecraft mc) {
        require(Boolean.getBoolean("create263.clientFiveAddons"), "Missing explicit owned client probe flag");
        require(mc != null && mc.isSameThread() && mc.player != null && mc.level != null && mc.getConnection() != null, "Compaction probe requires the active client thread and world connection");
        require(mc.getSingleplayerServer() != null && mc.level.dimension().equals(Level.OVERWORLD), "Compaction probe requires the owned integrated overworld");
        if (owner != null) require(mc.getSingleplayerServer() == owner && mc.player.getUUID().equals(playerId), "Owned server/player identity changed during compaction protocol");
    }
    private static ServerPlayer ownedPlayer() {
        require(owner.isSameThread(), "Compaction server verification ran off the owned server thread");
        ServerPlayer player = owner.getPlayerList().getPlayer(playerId);
        require(player != null && player.level() == owner.overworld(), "Owned server player/overworld unavailable");
        require(player.containerMenu == player.inventoryMenu, "Server menu was reopened during compaction protocol");
        return player;
    }
    private record InventorySnapshot(List<ItemStack> slots, ItemStack carried) {}
    private static InventorySnapshot inventory(Player player) {
        List<ItemStack> slots = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) slots.add(player.getInventory().getItem(slot).copy());
        return new InventorySnapshot(List.copyOf(slots), player.containerMenu.getCarried().copy());
    }
    private static boolean inventoriesEqual(InventorySnapshot first, InventorySnapshot second) {
        return ItemStack.listMatches(first.slots(), second.slots()) && ItemStack.matches(first.carried(), second.carried());
    }
    private record ServerSnapshot(InventorySnapshot inventory, BlockEntity backpack, BlockState state, List<ItemResource> resources, List<Long> amounts) {
        @Override public boolean equals(Object other) {
            return other instanceof ServerSnapshot that && backpack == that.backpack && state.equals(that.state)
                    && inventoriesEqual(inventory, that.inventory) && resources.equals(that.resources) && amounts.equals(that.amounts);
        }
        @Override public int hashCode() { return System.identityHashCode(backpack); }
    }
    private static ServerSnapshot serverSnapshot(ServerPlayer player) {
        ServerLevel level = owner.overworld();
        BlockEntity entity = level.getBlockEntity(BACKPACK);
        require(entity != null, "Existing server backpack fixture vanished");
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, BACKPACK, null);
        require(handler != null, "Server backpack native item capability disappeared");
        List<ItemResource> resources = new ArrayList<>();
        List<Long> amounts = new ArrayList<>();
        for (int slot = 0; slot < handler.size(); slot++) { resources.add(handler.getResource(slot)); amounts.add(handler.getAmountAsLong(slot)); }
        return new ServerSnapshot(inventory(player), entity, level.getBlockState(BACKPACK), List.copyOf(resources), List.copyOf(amounts));
    }
    private static Object naturalBuild(Level level, Item item) {
        try { return buildChain.invoke(null, level, item); }
        catch (InvocationTargetException failure) { throw propagate("Natural compaction request failed", failure.getCause()); }
        catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
    }
    private static Object staticField(Class<?> type, String name) {
        try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(null); }
        catch (ReflectiveOperationException failure) { throw new IllegalStateException("Missing exact compaction cache " + name, failure); }
    }
    private static Object value(Object object, String name) { return invoke(object, name, new Class<?>[0]); }
    private static int integer(Object object, String name) { return number(value(object, name)); }
    private static int number(Object value) { return ((Number) value).intValue(); }
    private static Object invoke(Object object, String name, Class<?>[] signature, Object... arguments) {
        try { return object.getClass().getMethod(name, signature).invoke(object, arguments); }
        catch (InvocationTargetException failure) { throw propagate(name, failure.getCause()); }
        catch (ReflectiveOperationException failure) { throw new IllegalStateException("Missing exact chain method " + name, failure); }
    }
    private static RuntimeException propagate(String context, Throwable cause) {
        if (cause instanceof Error fatal) throw fatal;
        return cause instanceof RuntimeException runtime ? runtime : new IllegalStateException(context, cause);
    }
    private static Class<?> type(String name) {
        try { return Class.forName(name); } catch (ClassNotFoundException failure) { throw new IllegalStateException(name, failure); }
    }
    private static String id(Item item) { return BuiltInRegistries.ITEM.getKey(item).toString(); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
