package dev.create263.probe;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Opt-in gameplay fixtures in one guarded, project-owned disposable overworld chunk. */
public final class StorageGameplayProbe {
    private static final String FLAG = "create263.storageGameplayProbe";
    private static final String TOKEN = "create263-storage-gameplay-v1";
    private static final String REPORT = "storage-gameplay-probe-result.json";
    private static final String MANIFEST = "storage-gameplay-fixture.json";
    private static final BlockPos CONTROLLER = new BlockPos(402, 100, 84);
    private static final BlockPos BRIDGE = CONTROLLER.east();
    private static final BlockPos BOX_A = BRIDGE.east();
    private static final BlockPos BOX_B = BOX_A.east();
    private static final BlockPos FILTERED = BRIDGE.south();
    private static final BlockPos INTERFACE = BRIDGE.north();
    private static final BlockPos HOPPER = FILTERED.above();
    private static final BlockPos UPGRADE_BOX = new BlockPos(410, 100, 90);
    private static final List<BlockPos> BASE = List.of(CONTROLLER, BRIDGE, BOX_A, BOX_B, FILTERED, INTERFACE, UPGRADE_BOX);
    private static final List<Map<String, Object>> checks = new ArrayList<>();
    private static final Map<String, Object> result = new LinkedHashMap<>();
    private static Path root;
    private static boolean installed, finished, reload;
    private static int ticks, hopperStartedAt;
    private static long automationInputTotal;
    private static FakePlayer player;
    private static ResourceHandler<ItemResource> cachedInterface, cachedFiltered;
    private static AbstractContainerMenu upgradeMenu;

    private StorageGameplayProbe() {}

    public static synchronized void install() {
        if (!Boolean.getBoolean(FLAG) || installed) return;
        try {
            String configured = System.getProperty("create263.probeRoot");
            require(configured != null && !configured.isBlank(), "Missing create263.probeRoot");
            root = Path.of(configured).toRealPath();
            require(root.equals(FMLPaths.GAMEDIR.get().toRealPath()), "FML gameDir differs from probeRoot");
            Path marker = root.resolve(".create263-disposable-runtime");
            require(Files.isRegularFile(marker, LinkOption.NOFOLLOW_LINKS) && marker.toRealPath().startsWith(root), "Missing real owned runtime marker");
            Path workspace = null;
            for (Path p = root.getParent(); p != null; p = p.getParent()) {
                if (Files.isRegularFile(p.resolve("AGENTS.md")) && Files.isDirectory(p.resolve("modules"))
                        && Files.isDirectory(p.resolve("tests/runtime-probe"))) { workspace = p.toRealPath(); break; }
            }
            require(workspace != null, "Cannot identify source workspace");
            Path allowed = workspace.resolve("runtime").toRealPath();
            require(root.startsWith(allowed) && !root.equals(allowed), "Probe root must be a child of this workspace/runtime");
            reload = Boolean.getBoolean("create263.gameplayReload");
            result.put("schema", 1);
            result.put("fixture_token", TOKEN);
            result.put("phase", reload ? "reload" : "create");
            result.put("process_identity", processIdentity());
            result.put("fixture_chunk", List.of(25, 5));
            result.put("fixture_bounds", Map.of("minimum", List.of(402, 99, 83), "maximum", List.of(410, 101, 90)));
            installed = true;
            NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, StorageGameplayProbe::tick);
        } catch (Exception e) { throw new IllegalStateException("Storage gameplay guard refused installation", e); }
    }

    private static String processIdentity() { return ProcessHandle.current().pid() + "@" + ManagementFactory.getRuntimeMXBean().getStartTime(); }
    private static void require(boolean okay, String message) { if (!okay) throw new IllegalStateException(message); }
    private static void eq(long expected, long actual, String message) { require(expected == actual, message + ": expected=" + expected + ", actual=" + actual); }
    @FunctionalInterface private interface Checked { Object run() throws Exception; }
    private static void check(String name, Checked action) throws Exception {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("name", name); entry.put("server_tick", ticks);
        try { entry.put("measured", action.run()); entry.put("passed", true); }
        catch (Throwable t) {
            Throwable cause = t instanceof InvocationTargetException e ? e.getTargetException() : t;
            entry.put("passed", false); entry.put("error", cause.toString()); checks.add(entry);
            throw new IllegalStateException(name, cause);
        }
        checks.add(entry);
    }

    private static void guard(ServerTickEvent.Post event) throws Exception {
        require(Boolean.getBoolean(FLAG), "Probe flag removed");
        require(root.equals(FMLPaths.GAMEDIR.get().toRealPath()), "GameDir changed");
        Path marker = root.resolve(".create263-disposable-runtime");
        require(Files.isRegularFile(marker, LinkOption.NOFOLLOW_LINKS) && marker.toRealPath().startsWith(root), "Owned marker removed or escaped root");
        Path world = event.getServer().getWorldPath(LevelResource.ROOT).toRealPath();
        require(world.equals(root.resolve("world").toRealPath()) && world.startsWith(root), "World is not owned probeRoot/world");
    }

    private static void tick(ServerTickEvent.Post event) {
        if (finished || ++ticks < 40) return;
        try {
            guard(event);
            ServerLevel level = event.getServer().overworld();
            if (ticks == 40) {
                level.setChunkForced(25, 5, true);
                level.getChunkAt(CONTROLLER);
                player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("370b09ab-a3fc-4dbe-b175-fcf6bd116613"), "StorageGameplay"));
                player.setGameMode(GameType.SURVIVAL);
                if (reload) check("restart/manifest-and-unchanged-physical-fixtures", () -> loadPersisted(level));
                else check("fixture/actual-survival-block-item-placement", () -> setup(level));
            }
            if (reload) {
                if (ticks == 180) {
                    check("restart/natural-controller-discovery-and-persisted-settings", () -> restored(level));
                    check("restart/native-transaction-abort-and-hopper-input", () -> restartInput(level));
                }
                if (ticks == 280) {
                    check("restart/real-hopper-still-routes-and-filter-still-rejects", () -> restartHopper(level));
                    saveAndFinish(level);
                }
            } else {
                if (ticks == 180) {
                    check("controller/natural-network-discovery", () -> discovery(level));
                    check("filter/native-reject-abort-extract-and-empty-scope", () -> filteredTransactions(level));
                    check("upgrade/survival-front-face-install-and-capacity", () -> installUpgrade(level));
                    check("upgrade/production-menu-overload-removal-guard", () -> upgradeMenu(level));
                    check("automation/real-hopper-input", () -> startHopper(level));
                }
                if (ticks == 400) {
                    check("automation/hopper-ticks-conserve-items-and-filter-rejects", () -> hopperComplete(level, 91));
                    check("network/remove-only-connecting-trim", () -> disconnect(level));
                }
                if (ticks == 540) {
                    check("network/cached-native-views-observe-disconnection", () -> disconnected(level));
                }
                if (ticks == 580) {
                    check("network/real-hopper-blocked-at-disconnected-interface", () -> disconnectedHopper(level));
                    check("network/actual-trim-block-item-reconnect", () -> { place(level, BRIDGE, "fxntstorage:oak_storage_trim"); return BRIDGE.toShortString(); });
                }
                if (ticks == 680) {
                    check("network/cached-native-views-recover-after-reconnect", () -> reconnected(level));
                    saveAndFinish(level);
                }
            }
        } catch (Throwable failure) {
            failure.printStackTrace();
            result.put("fatal_error", failure.toString());
            try { finish(false); } catch (Exception writing) { writing.printStackTrace(); }
        }
    }

    private static Object setup(ServerLevel level) throws Exception {
        require(!Files.exists(safeFile(MANIFEST), LinkOption.NOFOLLOW_LINKS), "Create phase refuses an existing storage fixture manifest");
        for (BlockPos pos : BASE) {
            require(level.getBlockState(pos).isAir(), "Fixture cell already occupied: " + pos);
            require(level.getBlockState(pos.below()).isAir(), "Fixture support cell occupied: " + pos.below());
        }
        require(level.getBlockState(HOPPER).isAir(), "Hopper fixture occupied");
        for (BlockPos pos : BASE) require(level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3), "Could not place owned support");
        place(level, CONTROLLER, "fxntstorage:storage_controller");
        place(level, BRIDGE, "fxntstorage:oak_storage_trim");
        place(level, BOX_A, "fxntstorage:oak_simple_storage_box");
        place(level, BOX_B, "fxntstorage:oak_simple_storage_box");
        place(level, FILTERED, "fxntstorage:storage_interface_filtered");
        place(level, INTERFACE, "fxntstorage:storage_interface");
        place(level, UPGRADE_BOX, "fxntstorage:oak_simple_storage_box");
        place(level, HOPPER, "minecraft:hopper");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return Map.of("survival_block_items_consumed", 8, "network_boxes", 2, "physical_capacity_fixture", UPGRADE_BOX.toShortString());
    }

    private static void place(ServerLevel level, BlockPos pos, String id) {
        require(level.getBlockState(pos).isAir(), "Placement target occupied: " + pos);
        ItemStack stack = item(id);
        require(stack.getItem() instanceof BlockItem, "Missing BlockItem: " + id);
        BlockItem blockItem = (BlockItem) stack.getItem();
        player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + 3.5);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos support = pos.below();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(support).add(0, .5, 0), Direction.UP, support, false);
        BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit);
        require(context.getClickedPos().equals(pos), "Wrong placement target");
        InteractionResult interaction = blockItem.place(context);
        require(interaction.consumesAction() && level.getBlockState(pos).is(blockItem.getBlock()), "Actual placement failed for " + id);
        require(stack.isEmpty(), "Survival placement did not consume " + id);
    }

    private static Object discovery(ServerLevel level) throws Exception {
        eq(2, capability(level, CONTROLLER).size(), "Controller discovers two boxes through tagged trim");
        cachedInterface = capability(level, INTERFACE);
        cachedFiltered = capability(level, FILTERED);
        eq(2, cachedInterface.size(), "Interface bound naturally");
        eq(2, cachedFiltered.size(), "Unconfigured filtered interface bound naturally");
        return Map.of("controller_slots", 2, "interface_slots", cachedInterface.size(), "filtered_slots", cachedFiltered.size(), "natural_tick_wait", 140);
    }

    private static ItemResource iron() { return ItemResource.of(Items.IRON_INGOT); }
    private static ItemResource gold() { return ItemResource.of(Items.GOLD_INGOT); }
    private static ItemStack markedIronStack() {
        ItemStack stack = new ItemStack(Items.IRON_INGOT);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(TOKEN));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        stack.remove(DataComponents.ITEM_NAME);
        return stack;
    }
    private static ItemResource markedIron() { return ItemResource.of(markedIronStack()); }

    private static Object filteredTransactions(ServerLevel level) throws Exception {
        ResourceHandler<ItemResource> first = capability(level, BOX_A);
        insert(first, iron(), 65, true, 65);
        FilteringBehaviour filtering = filtering(level);
        require(filtering.setFilter(new ItemStack(Items.IRON_INGOT)), "Production FilteringBehaviour rejected iron filter");
        option(level, FILTERED).setValue(1);
        eq(1, cachedFiltered.size(), "Exclude-empty view only exposes matching occupied box");
        insert(cachedFiltered, gold(), 13, true, 0);
        insert(cachedFiltered, iron(), 11, false, 11);
        eq(65, amount(level, BOX_A, iron()), "Aborted network insertion rolled back physical box");
        eq(0, amount(level, BOX_B, markedIron()), "Aborted insertion did not claim empty box");
        extract(cachedFiltered, iron(), 7, false, 7);
        eq(65, amount(level, BOX_A, iron()), "Aborted filtered extraction rolled back physical box");
        extract(cachedFiltered, iron(), 7, true, 7);
        insert(cachedFiltered, iron(), 9, true, 9);
        eq(67, amount(level, BOX_A, iron()), "Filtered insert/extract conserve normal iron");
        insert(cachedFiltered, markedIron(), 23, true, 0);
        eq(0, total(capability(level, BOX_B)), "Exclude-empty forbids component-distinct iron claiming empty box");
        option(level, CONTROLLER).setValue(1);
        insert(cachedInterface, gold(), 5, true, 0);
        eq(0, total(capability(level, BOX_B)), "Controller deny-empty forbids plain interface claiming empty box");
        option(level, CONTROLLER).setValue(0);
        option(level, FILTERED).setValue(0);
        insert(cachedFiltered, markedIron(), 23, false, 23);
        eq(0, total(capability(level, BOX_B)), "Aborted include-empty insert restores count and claimed filter");
        require(((ItemStack) call(entity(level, BOX_B), "getFilterItem")).isEmpty(), "Aborted include-empty insertion left a ghost filter");
        insert(cachedFiltered, markedIron(), 23, true, 23);
        eq(23, amount(level, BOX_B, markedIron()), "Include-empty claims second box with full component patch");
        option(level, FILTERED).setValue(1);
        option(level, CONTROLLER).setValue(1);
        eq(2, cachedFiltered.size(), "Two matching occupied component variants remain exposed");
        insert(cachedFiltered, gold(), 13, true, 0);
        eq(90, total(capability(level, CONTROLLER)), "Network total after transactions");
        return Map.of("plain_iron", 67, "component_iron", 23, "gold_rejected", 13, "abort_preserves_count_and_empty_filter", true,
                "scope_via_production_behaviour_setters", true, "controller_fill_empty_persisted", "deny", "filtered_scope_persisted", "exclude");
    }

    private static Object installUpgrade(ServerLevel level) throws Exception {
        ResourceHandler<ItemResource> handler = capability(level, UPGRADE_BOX);
        insert(handler, iron(), 2048, true, 2048);
        insert(handler, iron(), 1, true, 0);
        ItemStack upgrade = item("fxntstorage:storage_box_capacity_upgrade");
        player.setItemInHand(InteractionHand.MAIN_HAND, upgrade);
        BlockState state = level.getBlockState(UPGRADE_BOX);
        Direction front = state.getValue(HorizontalDirectionalBlock.FACING);
        player.setPos(UPGRADE_BOX.getX() + .5 + front.getStepX() * 2, UPGRADE_BOX.getY(), UPGRADE_BOX.getZ() + .5 + front.getStepZ() * 2);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(UPGRADE_BOX).add(front.getStepX() * .5, 0, front.getStepZ() * .5), front, UPGRADE_BOX, false);
        InteractionResult action = player.gameMode.useItemOn(player, level, upgrade, InteractionHand.MAIN_HAND, hit);
        require(action.consumesAction(), "Production ServerPlayerGameMode.useItemOn did not consume upgrade interaction: " + action);
        require(player.getMainHandItem().isEmpty(), "Survival capacity upgrade was not consumed");
        eq(1, number(call(entity(level, UPGRADE_BOX), "getCapacityUpgrades")), "Real interaction installed exactly one upgrade");
        eq(4096, number(call(entity(level, UPGRADE_BOX), "getMaxItemCapacity")), "Installed capacity doubles baseline");
        insert(handler, iron(), 1000, true, 1000);
        eq(3048, amount(level, UPGRADE_BOX, iron()), "Items above baseline accepted after upgrade");
        return Map.of("interaction", action.toString(), "upgrade_consumed", true, "baseline", 2048, "upgraded_capacity", 4096, "stored_above_baseline", 3048);
    }

    private static Object upgradeMenu(ServerLevel level) throws Exception {
        upgradeMenu = (AbstractContainerMenu) call(entity(level, UPGRADE_BOX), "createMenu", 71, player.getInventory(), player);
        require(upgradeMenu != null && upgradeMenu.getCarried().isEmpty(), "Production upgrade menu missing or carried stack dirty");
        require(upgradeMenu.slots.get(2).getItem().is(item("fxntstorage:storage_box_capacity_upgrade").getItem()), "Capacity upgrade menu index changed");
        upgradeMenu.clicked(2, 0, ContainerInput.PICKUP, player);
        require(upgradeMenu.getCarried().isEmpty() && upgradeMenu.slots.get(2).hasItem(), "Overloaded box allowed unsafe capacity upgrade removal");
        eq(3048, amount(level, UPGRADE_BOX, iron()), "Denied removal changed contents");
        ResourceHandler<ItemResource> handler = capability(level, UPGRADE_BOX);
        try (Transaction transaction = Transaction.openRoot()) {
            int remaining = 1000;
            while (remaining > 0) {
                int moved = handler.extract(0, iron(), remaining, transaction);
                require(moved > 0 && moved <= remaining, "Native extraction stopped before downgrade threshold");
                remaining -= moved;
            }
            transaction.commit();
        }
        eq(2048, amount(level, UPGRADE_BOX, iron()), "Downgrade threshold reached without loss");
        upgradeMenu.clicked(2, 0, ContainerInput.PICKUP, player);
        require(upgradeMenu.getCarried().is(item("fxntstorage:storage_box_capacity_upgrade").getItem()) && !upgradeMenu.slots.get(2).hasItem(), "Safe production-menu removal failed");
        eq(2048, number(call(entity(level, UPGRADE_BOX), "getMaxItemCapacity")), "Removed upgrade restores baseline capacity");
        upgradeMenu.clicked(2, 0, ContainerInput.PICKUP, player);
        require(upgradeMenu.getCarried().isEmpty() && upgradeMenu.slots.get(2).hasItem(), "Production-menu reinstall failed");
        eq(4096, number(call(entity(level, UPGRADE_BOX), "getMaxItemCapacity")), "Reinstalled upgrade restores capacity");
        eq(2048, amount(level, UPGRADE_BOX, iron()), "Upgrade removal/reinstall preserved contents");
        upgradeMenu.removed(player);
        upgradeMenu = null;
        return Map.of("entry", "BE.createMenu -> AbstractContainerMenu.clicked(PICKUP)", "above_baseline_removal_rejected", true, "at_baseline_removal_and_reinstall", true,
                "physical_iron", 2048, "installed_upgrade", 1, "client_protocol_or_render_verified", false);
    }

    private static Object startHopper(ServerLevel level) {
        HopperBlockEntity hopper = hopper(level);
        for (int slot = 0; slot < hopper.getContainerSize(); slot++) require(hopper.getItem(slot).isEmpty(), "Hopper setup inventory dirty");
        hopper.setItem(0, new ItemStack(Items.IRON_INGOT, 24));
        hopper.setItem(1, new ItemStack(Items.GOLD_INGOT, 7));
        hopper.setChanged();
        recordAutomationInput(level);
        return Map.of("fixture_input_iron", 24, "fixture_input_gold", 7, "transport", "vanilla hopper block ticks -> registered native filtered interface capability");
    }

    private static Object hopperComplete(ServerLevel level, int expectedIron) {
        HopperBlockEntity hopper = hopper(level);
        eq(0, hopper.getItem(0).getCount(), "Real hopper did not transfer accepted iron");
        require(hopper.getItem(1).is(Items.GOLD_INGOT), "Rejected hopper resource changed");
        eq(7, hopper.getItem(1).getCount(), "Real hopper consumed filter-rejected gold");
        eq(expectedIron, amount(level, BOX_A, iron()), "Hopper routing altered total plain iron");
        eq(23, amount(level, BOX_B, markedIron()), "Hopper routing merged component-distinct iron");
        assertOnlyHopperRemainders(level, 0);
        eq(expectedIron, total(capability(level, BOX_A)), "Hopper put unexpected resources into box A");
        eq(23, total(capability(level, BOX_B)), "Hopper put unexpected resources into box B");
        eq(automationInputTotal, automationTotal(level), "Measured hopper-plus-boxes total changed from recorded fixture input");
        return Map.of("box_a_plain_iron", expectedIron, "box_b_component_iron", 23, "hopper_gold_remainder", 7,
                "total_before_hopper_ticks", automationInputTotal, "total_after_hopper_ticks", automationTotal(level), "natural_hopper_ticks", ticks - hopperStartedAt);
    }

    private static void assertOnlyHopperRemainders(ServerLevel level, int ironRemainder) {
        HopperBlockEntity hopper = hopper(level);
        if (ironRemainder == 0) require(hopper.getItem(0).isEmpty(), "Unexpected hopper iron remainder");
        else require(ItemResource.of(hopper.getItem(0)).equals(iron()) && hopper.getItem(0).getCount() == ironRemainder,
                "Disconnected hopper lost or altered its accepted input");
        require(ItemResource.of(hopper.getItem(1)).equals(gold()) && hopper.getItem(1).getCount() == 7,
                "Rejected gold components or count changed");
        for (int slot = 2; slot < hopper.getContainerSize(); slot++)
            require(hopper.getItem(slot).isEmpty(), "Unexpected items in hopper slot " + slot);
    }

    private static long automationTotal(ServerLevel level) {
        long count = total(capability(level, BOX_A)) + total(capability(level, BOX_B));
        HopperBlockEntity hopper = hopper(level);
        for (int slot = 0; slot < hopper.getContainerSize(); slot++) count += hopper.getItem(slot).getCount();
        return count;
    }

    private static void recordAutomationInput(ServerLevel level) {
        hopperStartedAt = ticks;
        automationInputTotal = automationTotal(level);
    }

    private static Object disconnect(ServerLevel level) {
        require(level.getBlockState(BRIDGE).is(BuiltInRegistries.BLOCK.getValue(Identifier.parse("fxntstorage:oak_storage_trim"))), "Owned connecting trim missing");
        require(level.removeBlock(BRIDGE, false), "Owned connecting trim could not be removed");
        return Map.of("removed_bridge", BRIDGE.toShortString(), "inventories_removed", false);
    }

    private static Object disconnected(ServerLevel level) {
        eq(0, capability(level, CONTROLLER).size(), "Disconnected controller retained remote boxes");
        eq(0, cachedInterface.size(), "Previously acquired native interface view retained stale network");
        eq(0, cachedFiltered.size(), "Previously acquired native filtered view retained stale network");
        insert(cachedFiltered, iron(), 6, true, 0);
        insert(cachedInterface, iron(), 6, true, 0);
        eq(91, amount(level, BOX_A, iron()), "Disconnected cached handler altered remote physical box");
        eq(23, amount(level, BOX_B, markedIron()), "Disconnected network altered component-distinct box");
        assertOnlyHopperRemainders(level, 0);
        hopper(level).setItem(0, new ItemStack(Items.IRON_INGOT, 6));
        hopper(level).setChanged();
        recordAutomationInput(level);
        return Map.of("cached_interface_slots", cachedInterface.size(), "cached_filtered_slots", cachedFiltered.size(),
                "remote_box_unchanged", 91, "new_hopper_iron_while_disconnected", 6, "natural_tick_wait", 140);
    }

    private static Object disconnectedHopper(ServerLevel level) {
        eq(0, cachedInterface.size(), "Plain interface reconnected without a physical bridge");
        eq(0, cachedFiltered.size(), "Filtered interface reconnected without a physical bridge");
        eq(91, amount(level, BOX_A, iron()), "Disconnected hopper wrote to remote physical box");
        eq(23, amount(level, BOX_B, markedIron()), "Disconnected hopper changed component-distinct box");
        assertOnlyHopperRemainders(level, 6);
        eq(automationInputTotal, automationTotal(level), "Disconnected hopper lost or duplicated input");
        int blockedTicks = ticks - hopperStartedAt;
        hopperStartedAt = ticks;
        return Map.of("hopper_retained_iron", 6, "hopper_retained_gold", 7, "natural_hopper_blocked_ticks", blockedTicks,
                "total_before_hopper_ticks", automationInputTotal, "total_after_hopper_ticks", automationTotal(level));
    }

    private static Object reconnected(ServerLevel level) throws Exception {
        eq(2, cachedInterface.size(), "Cached interface view did not recover after physical reconnection");
        eq(2, cachedFiltered.size(), "Cached filtered view did not recover after physical reconnection");
        Object transport = hopperComplete(level, 97);
        return Map.of("cached_interfaces_reused", true, "real_hopper_resumed_iron_after_reconnect", 6,
                "plain_iron", 97, "component_iron", 23, "natural_hopper_transport", transport);
    }

    private static Object loadPersisted(ServerLevel level) throws Exception {
        Path manifestPath = safeFile(MANIFEST);
        require(Files.isRegularFile(manifestPath, LinkOption.NOFOLLOW_LINKS), "Missing prior create-phase fixture manifest");
        JsonObject manifest = JsonParser.parseString(Files.readString(manifestPath)).getAsJsonObject();
        require(TOKEN.equals(manifest.get("fixture_token").getAsString()), "Wrong fixture identity");
        require(!processIdentity().equals(manifest.get("creation_process_identity").getAsString()), "Reload must use a distinct JVM");
        for (var cell : Map.of(CONTROLLER, "fxntstorage:storage_controller", BRIDGE, "fxntstorage:oak_storage_trim", BOX_A, "fxntstorage:oak_simple_storage_box",
                BOX_B, "fxntstorage:oak_simple_storage_box", FILTERED, "fxntstorage:storage_interface_filtered", INTERFACE, "fxntstorage:storage_interface",
                UPGRADE_BOX, "fxntstorage:oak_simple_storage_box", HOPPER, "minecraft:hopper").entrySet()) {
            require(level.getBlockState(cell.getKey()).is(BuiltInRegistries.BLOCK.getValue(Identifier.parse(cell.getValue()))), "Persisted fixture block missing: " + cell);
        }
        eq(97, amount(level, BOX_A, iron()), "Disk-reloaded normal iron");
        eq(23, amount(level, BOX_B, markedIron()), "Disk-reloaded iron component patch");
        eq(2048, amount(level, UPGRADE_BOX, iron()), "Disk-reloaded upgraded-box contents");
        eq(1, number(call(entity(level, UPGRADE_BOX), "getCapacityUpgrades")), "Disk-reloaded installed upgrade");
        eq(4096, number(call(entity(level, UPGRADE_BOX), "getMaxItemCapacity")), "Disk-reloaded upgraded capacity");
        assertOnlyHopperRemainders(level, 0);
        eq(7, hopper(level).getItem(1).getCount(), "Disk-reloaded filtered gold remainder");
        return Map.of("different_jvm", true, "creation_process", manifest.get("creation_process_identity").getAsString(), "physical_boxes", 3, "plain_iron", 97, "component_iron", 23, "upgraded_box_iron", 2048);
    }

    private static Object restored(ServerLevel level) {
        eq(2, capability(level, CONTROLLER).size(), "Restarted controller did not naturally rediscover network");
        cachedInterface = capability(level, INTERFACE); cachedFiltered = capability(level, FILTERED);
        eq(2, cachedInterface.size(), "Restarted plain interface did not bind controller");
        eq(2, cachedFiltered.size(), "Restarted filtered interface did not bind controller");
        require(ItemStack.isSameItemSameComponents(new ItemStack(Items.IRON_INGOT), filtering(level).getFilter()), "Persisted filtering behaviour changed");
        eq(1, option(level, FILTERED).getValue(), "Persisted exclude-empty scope");
        eq(1, option(level, CONTROLLER).getValue(), "Persisted deny-empty controller setting");
        insert(cachedFiltered, gold(), 5, true, 0);
        return Map.of("controller_slots", 2, "filtered_scope", "exclude_empty", "controller_fill_empty", "deny", "iron_filter", true, "gold_insert_rejected", true);
    }

    private static Object restartInput(ServerLevel level) {
        insert(cachedFiltered, iron(), 5, false, 5);
        eq(97, amount(level, BOX_A, iron()), "Restarted transaction abort changed saved inventory");
        HopperBlockEntity hopper = hopper(level);
        require(hopper.getItem(0).isEmpty(), "Persisted hopper iron slot not empty");
        hopper.setItem(0, new ItemStack(Items.IRON_INGOT, 3)); hopper.setChanged();
        recordAutomationInput(level);
        return Map.of("native_aborted_insert", 5, "new_hopper_iron", 3);
    }

    private static Object restartHopper(ServerLevel level) {
        hopperComplete(level, 100);
        extract(cachedFiltered, iron(), 3, true, 3);
        eq(97, amount(level, BOX_A, iron()), "Restart hopper evidence cleanup changed stable manifest quantity");
        return Map.of("real_hopper_after_distinct_jvm", true, "transferred_iron", 3, "gold_still_rejected", 7, "stable_saved_plain_iron", 97);
    }

    private static void saveAndFinish(ServerLevel level) throws Exception {
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        require(level.getServer().saveEverything(false, true, true), "Disposable server saveEverything returned false");
        if (!reload) {
            Map<String, Object> manifest = Map.of("fixture_token", TOKEN, "creation_process_identity", processIdentity(), "created_at", Instant.now().toString(),
                    "plain_iron", 97, "component_iron", 23, "upgraded_box_iron", 2048, "capacity_upgrades", 1, "hopper_gold", 7);
            Files.writeString(safeFile(MANIFEST), new GsonBuilder().setPrettyPrinting().create().toJson(manifest) + "\n");
        }
        result.put("saved_with_native_server_save", true);
        finish(true);
    }

    private static void finish(boolean passed) throws Exception {
        finished = true;
        result.put("passed", passed && checks.stream().allMatch(c -> Boolean.TRUE.equals(c.get("passed"))));
        result.put("status", Boolean.TRUE.equals(result.get("passed")) ? "passed" : "failed");
        result.put("phase", reload ? "reload-complete" : "create-complete");
        result.put("recorded_at", Instant.now().toString()); result.put("checks", checks);
        result.put("actual_check_count", checks.size());
        result.put("remaining", List.of("No real client mouse/network/render check was performed by this server probe.",
                "Filter/scope configuration uses production behaviour setters; the rendered Create filter UI is not exercised.",
                "Backpack wear/active-upgrade ticks, moving contraptions, multiplayer races, and long-duration load remain outside this probe.",
                reload ? "A separate-JVM restart and real hopper tick after restore were verified." : "This create phase requires a separate JVM with create263.gameplayReload=true to verify disk restore."));
        result.put("all_requested_features_verified", false);
        Files.writeString(safeFile(REPORT), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n");
    }

    private static Path safeFile(String name) throws Exception {
        Path path = root.resolve(name).normalize(); require(path.getParent().equals(root), "Output escaped probe root");
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && path.toRealPath().startsWith(root), "Output is a link or escapes root");
        return path;
    }
    private static ItemStack item(String id) { var item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id)); require(item != null && item != Items.AIR, "Missing item " + id); return new ItemStack(item); }
    private static BlockEntity entity(ServerLevel level, BlockPos pos) { BlockEntity entity = level.getBlockEntity(pos); require(entity != null, "Missing actual world BE at " + pos); return entity; }
    private static HopperBlockEntity hopper(ServerLevel level) { require(entity(level, HOPPER) instanceof HopperBlockEntity, "Missing actual hopper BE"); return (HopperBlockEntity) entity(level, HOPPER); }
    private static ResourceHandler<ItemResource> capability(ServerLevel level, BlockPos pos) { var handler = level.getCapability(Capabilities.Item.BLOCK, pos, null); require(handler != null, "Missing registered native item capability at " + pos); return handler; }
    private static FilteringBehaviour filtering(ServerLevel level) { var behaviour = ((SmartBlockEntity) entity(level, FILTERED)).getBehaviour(FilteringBehaviour.TYPE); require(behaviour != null, "Missing production filtering behaviour"); return behaviour; }
    private static ScrollValueBehaviour option(ServerLevel level, BlockPos pos) { var behaviour = ((SmartBlockEntity) entity(level, pos)).getBehaviour(ScrollValueBehaviour.TYPE); require(behaviour != null, "Missing production scroll option behaviour"); return behaviour; }
    private static long total(ResourceHandler<ItemResource> handler) { long amount = 0; for (int i = 0; i < handler.size(); i++) amount += handler.getAmountAsLong(i); return amount; }
    private static long amount(ServerLevel level, BlockPos pos, ItemResource resource) { var h = capability(level, pos); long amount = 0; for (int i = 0; i < h.size(); i++) if (resource.equals(h.getResource(i))) amount += h.getAmountAsLong(i); return amount; }
    private static void insert(ResourceHandler<ItemResource> handler, ItemResource resource, int count, boolean commit, int expected) { try (Transaction tx = Transaction.openRoot()) { eq(expected, handler.insert(resource, count, tx), "Native capability insertion"); if (commit) tx.commit(); } }
    private static void extract(ResourceHandler<ItemResource> handler, ItemResource resource, int count, boolean commit, int expected) { try (Transaction tx = Transaction.openRoot()) { eq(expected, handler.extract(resource, count, tx), "Native capability extraction"); if (commit) tx.commit(); } }
    private static int number(Object value) { return ((Number) value).intValue(); }
    /** Reflection only invokes public production inspection/menu methods; no private state is read or written. */
    private static Object call(Object target, String name, Object... args) throws Exception {
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != args.length) continue;
            Class<?>[] types = method.getParameterTypes(); boolean matches = true;
            for (int i = 0; i < args.length; i++) {
                Class<?> expected = types[i] == int.class ? Integer.class : types[i];
                if (args[i] != null && !expected.isInstance(args[i])) { matches = false; break; }
            }
            if (matches) return method.invoke(target, args);
        }
        throw new NoSuchMethodException(target.getClass().getName() + "." + name);
    }
}
