package dev.simulated_team.simulated.neoforge;

import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorBattery;
import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorBlockEntity;
import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorDuoInventory;
import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorSoloInventory;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.multiloader.energy.SingleBattery;
import dev.simulated_team.simulated.multiloader.energy.SingleBatteryWrapper;
import dev.simulated_team.simulated.multiloader.inventory.ContainerSlot;
import dev.simulated_team.simulated.multiloader.inventory.MultiSlotContainer;
import dev.simulated_team.simulated.multiloader.inventory.neoforge.ContainerWrapper;
import dev.simulated_team.simulated.multiloader.tanks.CFluidType;
import dev.simulated_team.simulated.multiloader.tanks.SingleTank;
import dev.simulated_team.simulated.multiloader.tanks.neoforge.SingleTankWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Explicit smoke-server checks: registers no events and never starts a server.
 * Call once on the server thread in the dedicated disposable smoke world.
 * Binary NBT round trips are in memory. Three initially empty positions at y=90
 * are used for real docking block entities and removed in finally.
 */
public final class TransferRuntimeChecks {
    private TransferRuntimeChecks() {}

    public static List<String> run(ServerLevel level) {
        require(level != null, "A real ServerLevel is required");
        require(level.getServer().isSameThread(), "Run checks on the server thread");
        List<String> passed = new ArrayList<>();
        checkCase(passed, "tank/rejected-fluid-full-zero-blank-preconditions", TransferRuntimeChecks::tankRejection);
        checkCase(passed, "tank/source-and-flowing-are-distinct-resources", TransferRuntimeChecks::fluidIdentity);
        checkCase(passed, "tank/components-binary-nbt-reload-self-extraction", TransferRuntimeChecks::fluidRoundTrip);
        checkCase(passed, "tank/nested-transactions-shared-backend-and-callback", TransferRuntimeChecks::tankTransactions);
        checkCase(passed, "battery/ordinary-commit-capacity-throughput-callback-nbt", TransferRuntimeChecks::ordinaryBattery);
        checkCase(passed, "battery/docking-two-endpoints-abort-and-retarget", TransferRuntimeChecks::dockingBattery);
        checkCase(passed, "item/component-identity-and-dynamic-max-stack", TransferRuntimeChecks::itemIdentity);
        checkCase(passed, "item/nested-transactions-shared-backend-silent-abort", TransferRuntimeChecks::itemTransactions);
        checkCase(passed, "item/multislot-binary-nbt-count-filters-populated", () -> inventoryRoundTrip(level));
        dockingWorldChecks(level, passed);
        return List.copyOf(passed);
    }

    private static void tankRejection() {
        FluidResource water = FluidResource.of(Fluids.WATER);
        FluidResource lava = FluidResource.of(Fluids.LAVA);
        SingleTank tank = new SingleTank(1000);
        SingleTankWrapper handler = new SingleTankWrapper(tank);
        seed(tank, water, 500);
        try (Transaction tx = Transaction.openRoot()) {
            equal(0, handler.insert(0, lava, 100, tx), "different fluid rejection");
            equal(0, handler.insert(0, water, 0, tx), "zero request");
            tx.commit();
        }
        tank(tank, water, 500, "rejected insertion must not transmute contents");
        seed(tank, water, 1000);
        try (Transaction tx = Transaction.openRoot()) {
            equal(0, handler.insert(0, lava, 100, tx), "full tank rejects different fluid");
            equal(0, handler.insert(0, water, 100, tx), "full tank rejects same fluid");
            equal(0, handler.extract(0, lava, 100, tx), "different fluid extraction");
            illegalArgument(() -> handler.insert(0, water, -1, tx), "negative native tank insert");
            illegalArgument(() -> handler.extract(0, water, -1, tx), "negative native tank extract");
            illegalArgument(() -> handler.insert(0, FluidResource.EMPTY, 1, tx), "empty native tank insert");
            illegalArgument(() -> handler.extract(0, FluidResource.EMPTY, 1, tx), "empty native tank extract");
            tx.commit();
        }
        tank(tank, water, 1000, "full/invalid operations preserve contents");
        SingleTank empty = new SingleTank(1000);
        require(new CFluidType(Fluids.EMPTY, DataComponentMap.EMPTY).isBlank(), "empty map must still be blank");
        require(SingleTankWrapper.toCType(FluidStack.EMPTY).equals(CFluidType.BLANK), "blank representations must agree");
        equal(0, empty.insert(CFluidType.BLANK, 100, false), "blank backing-tank insertion");
        equal(0, empty.insert(SingleTankWrapper.toCType(water.toStack(1)), -1, false), "negative common insertion is inert");
        SingleTankWrapper emptyHandler = new SingleTankWrapper(empty);
        equal(0, emptyHandler.fill(FluidStack.EMPTY,
            com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler.FluidAction.EXECUTE), "legacy empty fill");
        require(emptyHandler.getResource(0).isEmpty() && empty.type.isBlank(), "empty fill preserves blank type");
        equal(0, empty.amount, "blank amount");
        try (Transaction tx = Transaction.openRoot()) {
            equal(100, emptyHandler.insert(0, water, 100, tx), "first real insert after empty fill");
            tx.commit();
        }
        tank(empty, water, 100, "first insert into blank tank");
    }

    private static void fluidIdentity() {
        FluidResource water = FluidResource.of(Fluids.WATER);
        FluidResource flowing = FluidResource.of(Fluids.FLOWING_WATER);
        require(!water.equals(flowing), "fixture: native source/flowing resources differ");
        SingleTank tank = new SingleTank(1000);
        SingleTankWrapper handler = new SingleTankWrapper(tank);
        seed(tank, water, 500);
        try (Transaction tx = Transaction.openRoot()) {
            equal(0, handler.insert(0, flowing, 100, tx), "flowing water must not merge into source water");
            equal(0, handler.extract(0, flowing, 100, tx), "flowing water must not extract source water");
            tx.commit();
        }
        tank(tank, water, 500, "source fluid identity remains exact");
    }

    private static void fluidRoundTrip() {
        FluidStack original = namedFluid("component-A");
        FluidResource expected = FluidResource.of(original);
        CFluidType canonical = SingleTankWrapper.toCType(original);
        CFluidType fromPlainMap = new CFluidType(Fluids.WATER,
            DataComponentMap.builder().addAll(original.getComponents()).build());
        require(canonical.equals(fromPlainMap) && canonical.hashCode() == fromPlainMap.hashCode(),
            "equivalent patched/plain component maps");
        original.set(DataComponents.CUSTOM_NAME, Component.literal("mutated-caller"));
        require(canonical.equals(fromPlainMap), "fluid type must not retain mutable caller map");
        require(canonical.equals(CFluidType.read(nbt(canonical.write()))), "fluid type component NBT round trip");
        SingleTank source = new SingleTank(1000);
        SingleTankWrapper sourceHandler = new SingleTankWrapper(source);
        try (Transaction tx = Transaction.openRoot()) {
            equal(500, sourceHandler.insert(0, expected, 500, tx), "component fluid initial insert");
            tx.commit();
        }
        SingleTank restored = new SingleTank(1000);
        restored.read(nbt(source.write()));
        SingleTankWrapper restoredHandler = new SingleTankWrapper(restored);
        tank(restored, expected, 500, "reloaded fluid components");
        try (Transaction tx = Transaction.openRoot()) {
            equal(0, restoredHandler.insert(0, FluidResource.of(namedFluid("component-B")), 1, tx), "different fluid components");
            equal(100, restoredHandler.extract(0, restoredHandler.getResource(0), 100, tx), "reload self-extraction");
            tx.commit();
        }
        tank(restored, expected, 400, "component-preserving extraction after reload");
        SingleTank plain = new SingleTank(1000);
        seed(plain, FluidResource.of(Fluids.WATER), 250);
        SingleTank plainReloaded = new SingleTank(1000);
        plainReloaded.read(nbt(plain.write()));
        SingleTankWrapper plainHandler = new SingleTankWrapper(plainReloaded);
        try (Transaction tx = Transaction.openRoot()) {
            equal(250, plainHandler.extract(0, plainHandler.getResource(0), 250, tx), "ordinary water after reload");
            tx.commit();
        }
        tank(plainReloaded, FluidResource.EMPTY, 0, "last extraction returns to blank");
    }

    private static void tankTransactions() {
        CountingTank tank = new CountingTank();
        SingleTankWrapper first = new SingleTankWrapper(tank), second = new SingleTankWrapper(tank);
        FluidResource water = FluidResource.of(Fluids.WATER);
        try (Transaction root = Transaction.openRoot()) {
            equal(300, first.insert(0, water, 300, root), "outer insert");
            try (Transaction child = Transaction.open(root)) {
                equal(80, second.extract(0, water, 80, child), "child extract");
            }
            tank(tank, water, 300, "child abort restores root state");
            try (Transaction child = Transaction.open(root)) {
                equal(120, second.insert(0, water, 120, child), "committed child insert");
                child.commit();
            }
            tank(tank, water, 420, "child commit is visible inside root");
            equal(0, tank.notifications, "no tank callback before root closes");
        }
        tank(tank, FluidResource.EMPTY, 0, "parent abort undoes committed child");
        equal(0, tank.notifications, "aborted tank transaction has no callback");
        try (Transaction root = Transaction.openRoot()) {
            equal(200, first.insert(0, water, 200, root), "committing outer insert");
            try (Transaction child = Transaction.open(root)) {
                equal(100, second.insert(0, water, 100, child), "second wrapper child insert");
                child.commit();
            }
            equal(0, tank.notifications, "child commit defers notification");
            root.commit();
        }
        tank(tank, water, 300, "root commit retains both wrapper changes");
        equal(1, tank.notifications, "shared backend sends one root-commit callback");
        try (Transaction tx = Transaction.openRoot()) {
            equal(300, second.extract(0, water, 1000, tx), "temporary full extraction");
            require(tank.type.isBlank(), "temporary drained state is blank");
        }
        tank(tank, water, 300, "abort restores type as well as amount");
        equal(1, tank.notifications, "extraction simulation is silent");
    }

    private static void ordinaryBattery() {
        int[] notifications = {0};
        SingleBattery battery = new SingleBattery(300, 125, () -> notifications[0]++);
        battery.setEnergy(100);
        SingleBatteryWrapper first = new SingleBatteryWrapper(battery), second = new SingleBatteryWrapper(battery);
        try (Transaction root = Transaction.openRoot()) {
            equal(125, first.insert(200, root), "battery throughput");
            try (Transaction child = Transaction.open(root)) {
                equal(50, second.extract(50, child), "nested battery extraction");
                child.commit();
            }
            equal(0, notifications[0], "battery callbacks wait for root close");
            root.commit();
        }
        equal(175, first.getAmountAsLong(), "ordinary battery committed value");
        equal(300, first.getCapacityAsLong(), "ordinary battery capacity");
        equal(1, notifications[0], "shared battery commit callback");
        try (Transaction root = Transaction.openRoot()) {
            equal(125, second.insert(1000, root), "capacity limits battery insert");
            try (Transaction child = Transaction.open(root)) {
                equal(75, first.extract(75, child), "aborted nested battery extraction");
            }
            equal(300, battery.getEnergy(), "child battery abort restores outer state");
            illegalArgument(() -> first.insert(-1, root), "negative energy insert");
            illegalArgument(() -> second.extract(-1, root), "negative energy extract");
            equal(0, first.insert(0, root), "zero energy insert");
        }
        equal(175, battery.getEnergy(), "ordinary battery root abort");
        equal(1, notifications[0], "aborted battery operations are silent");
        SingleBattery restored = new SingleBattery(300, 125);
        restored.read(nbt(battery.write()));
        equal(175, restored.getEnergy(), "battery NBT reload");
    }

    private static void dockingBattery() {
        DockingConnectorBattery a = new DockingConnectorBattery(1000, 1000);
        DockingConnectorBattery b = new DockingConnectorBattery(1000, 1000);
        DockingConnectorBattery c = new DockingConnectorBattery(1000, 1000);
        a.setEnergy(100); b.setEnergy(200); c.setEnergy(300);
        a.connect(b); b.connect(a);
        SingleBatteryWrapper first = new SingleBatteryWrapper(a), second = new SingleBatteryWrapper(b);
        try (Transaction tx = Transaction.openRoot()) {
            equal(50, first.insert(50, tx), "A inserts into actual B");
            equal(10, second.extract(10, tx), "B extracts local energy");
            equal(20, first.extract(20, tx), "A extracts local energy");
            equal(15, second.insert(15, tx), "B inserts into actual A without recursive routing");
            equal(95, a.getEnergy(), "routed A contents inside transaction");
            equal(240, b.getEnergy(), "routed B contents inside transaction");
        }
        equal(100, a.getEnergy(), "A restored after two-endpoint abort");
        equal(200, b.getEnergy(), "B restored after two-endpoint abort");
        try (Transaction tx = Transaction.openRoot()) {
            equal(50, first.insert(50, tx), "insert before link switch");
            a.connect(c);
            equal(40, first.insert(40, tx), "insert after link switch");
            a.disconnect();
        }
        equal(100, a.getEnergy(), "retarget abort leaves A unchanged");
        equal(200, b.getEnergy(), "retarget abort restores old actual recipient");
        equal(300, c.getEnergy(), "retarget abort restores new recipient after disconnect");
        a.connect(b);
        try (Transaction tx = Transaction.openRoot()) {
            equal(25, first.insert(25, tx), "routed battery commit");
            tx.commit();
        }
        equal(100, a.getEnergy(), "routed commit must not change sender buffer");
        equal(225, b.getEnergy(), "routed commit changes actual recipient");
        a.disconnect(); b.disconnect();
    }

    private static void itemIdentity() {
        DockingConnectorSoloInventory inventory = solo();
        ContainerWrapper<DockingConnectorSoloInventory> handler = new ContainerWrapper<>(inventory);
        ItemResource first = ItemResource.of(namedItem(Items.STONE, 1, "item-A"));
        ItemResource second = ItemResource.of(namedItem(Items.STONE, 1, "item-B"));
        try (Transaction tx = Transaction.openRoot()) {
            equal(3, handler.insert(0, first, 3, tx), "initial named item insert");
            tx.commit();
        }
        try (Transaction tx = Transaction.openRoot()) {
            equal(0, handler.insert(0, second, 1, tx), "different item components cannot merge");
            equal(0, handler.extract(0, second, 1, tx), "different item components cannot extract");
            illegalArgument(() -> handler.insert(0, first, -1, tx), "negative item insert");
            illegalArgument(() -> handler.extract(0, first, -1, tx), "negative item extract");
            illegalArgument(() -> handler.insert(0, ItemResource.EMPTY, 1, tx), "empty item insert");
            illegalArgument(() -> handler.extract(0, ItemResource.EMPTY, 1, tx), "empty item extract");
            equal(0, handler.insert(0, first, 0, tx), "zero item insert");
            tx.commit();
        }
        item(inventory.getItem(0), first, 3, "named identity survives rejected operations");
        DockingConnectorSoloInventory limited = solo();
        ContainerWrapper<DockingConnectorSoloInventory> limitedHandler = new ContainerWrapper<>(limited);
        ItemStack smallStack = new ItemStack(Items.STONE);
        smallStack.set(DataComponents.MAX_STACK_SIZE, 1);
        ItemResource small = ItemResource.of(smallStack);
        equal(1, limitedHandler.getCapacityAsLong(0, small), "component-dependent capacity");
        try (Transaction tx = Transaction.openRoot()) {
            equal(1, limitedHandler.insert(0, small, 2, tx), "component-dependent actual limit");
            equal(0, limitedHandler.insert(0, small, 1, tx), "limited stack remains full");
            tx.commit();
        }
        item(limited.getItem(0), small, 1, "max-size component preserved");
        equal(1, limited.getItem(0).getMaxStackSize(), "actual max stack size");
    }

    private static void itemTransactions() {
        ProbeInventory inventory = new ProbeInventory(2);
        ItemResource stone = ItemResource.of(namedItem(Items.STONE, 1, "nested"));
        ItemResource dirt = ItemResource.of(new ItemStack(Items.DIRT));
        inventory.setItem(0, stone.toStack(4));
        inventory.notifications = 0;
        ContainerWrapper<ProbeInventory> first = new ContainerWrapper<>(inventory), second = new ContainerWrapper<>(inventory);
        try (Transaction root = Transaction.openRoot()) {
            equal(2, first.insert(0, stone, 2, root), "outer item insertion");
            try (Transaction child = Transaction.open(root)) {
                equal(3, second.extract(0, stone, 3, child), "nested item extraction");
            }
            item(inventory.getItem(0), stone, 6, "item child abort");
            try (Transaction child = Transaction.open(root)) {
                equal(5, second.insert(1, dirt, 5, child), "nested item insertion");
                child.commit();
            }
            equal(1, first.extract(0, stone, 1, root), "outer mutation after committed child");
            equal(10, inventory.storedItemCount, "transactional count index");
            equal(0, inventory.notifications, "no owner callback before commit");
        }
        item(inventory.getItem(0), stone, 4, "parent item abort restores components/count");
        require(inventory.getItem(1).isEmpty(), "parent abort removes committed-child insertion");
        equal(4, inventory.storedItemCount, "parent abort restores count index");
        require(indices(inventory.getPopulatedSlots()).equals(Set.of(0)), "parent abort restores populated index");
        require(inventory.getFilteredSlots(Items.DIRT).isEmpty(), "parent abort restores filter");
        equal(0, inventory.notifications, "item abort dispatches no owner callbacks");
        try (Transaction root = Transaction.openRoot()) {
            equal(2, first.insert(0, stone, 2, root), "committing first-wrapper insertion");
            equal(5, second.insert(1, dirt, 5, root), "committing second-wrapper insertion");
            try (Transaction child = Transaction.open(root)) {
                equal(1, second.extract(0, stone, 1, child), "committing child extraction");
                child.commit();
            }
            equal(0, inventory.notifications, "child commit still defers owner callback");
            root.commit();
        }
        item(inventory.getItem(0), stone, 5, "root commit first slot");
        item(inventory.getItem(1), dirt, 5, "root commit second slot");
        equal(10, inventory.storedItemCount, "committed count index");
        require(inventory.notifications > 0, "root commit notifies inventory owner");
    }

    private static void inventoryRoundTrip(ServerLevel level) {
        ProbeInventory source = new ProbeInventory(4);
        ItemStack first = namedItem(Items.STONE, 3, "persist-A");
        ItemStack second = new ItemStack(Items.DIRT, 5);
        ItemStack third = namedItem(Items.STONE, 2, "persist-B");
        source.setItem(0, first); source.setItem(1, second); source.setItem(2, third);
        CompoundTag saved = nbt(source.write(level.registryAccess()));
        saved.putInt("Stored Count", 999); // Rebuild derived indexes from actual data.
        ProbeInventory restored = new ProbeInventory(4);
        restored.setItem(0, new ItemStack(Items.DIAMOND, 64));
        restored.setItem(3, new ItemStack(Items.APPLE, 7));
        restored.read(level.registryAccess(), saved);
        checkRestoredInventory(restored, first, second, third);
        restored.read(level.registryAccess(), saved);
        checkRestoredInventory(restored, first, second, third);
        DockingConnectorSoloInventory single = solo(), singleRestored = solo();
        single.setItem(0, first.copy());
        singleRestored.read(level.registryAccess(), nbt(single.write(level.registryAccess())));
        stack(first, singleRestored.getItem(0), "single-slot binary NBT round trip");
    }

    private static void checkRestoredInventory(ProbeInventory restored, ItemStack first, ItemStack second, ItemStack third) {
        stack(first, restored.getItem(0), "first encoded stack");
        stack(second, restored.getItem(1), "second encoded stack");
        stack(third, restored.getItem(2), "third encoded stack");
        require(restored.getItem(3).isEmpty(), "empty slot clears stale contents");
        equal(10, restored.storedItemCount, "recomputed stored count");
        require(indices(restored.getPopulatedSlots()).equals(Set.of(0, 1, 2)), "recomputed populated slots");
        require(indices(restored.getFilteredSlots(Items.STONE)).equals(Set.of(0, 2)), "recomputed stone filter");
        require(indices(restored.getFilteredSlots(Items.DIRT)).equals(Set.of(1)), "recomputed dirt filter");
        require(indices(restored.getFilteredSlots(Items.AIR)).equals(Set.of(3)), "recomputed empty filter");
        require(restored.getFilteredSlots(Items.DIAMOND).isEmpty(), "stale diamond filter removed");
        require(restored.getFilteredSlots(Items.APPLE).isEmpty(), "stale apple filter removed");
    }

    private static void dockingWorldChecks(ServerLevel level, List<String> passed) {
        List<BlockPos> positions = List.of(new BlockPos(96, 90, 96), new BlockPos(100, 90, 96), new BlockPos(104, 90, 96));
        for (BlockPos pos : positions) {
            require(level.getBlockState(pos).isAir() && level.getBlockEntity(pos) == null,
                "Reserved position occupied; refusing to overwrite " + pos);
        }
        List<BlockPos> placed = new ArrayList<>();
        try {
            List<DockingConnectorBlockEntity> connectors = new ArrayList<>();
            for (BlockPos pos : positions) {
                placed.add(pos);
                require(level.setBlock(pos, SimBlocks.DOCKING_CONNECTOR.get().defaultBlockState(), 2), "Cannot place connector at " + pos);
                require(level.getBlockEntity(pos) instanceof DockingConnectorBlockEntity, "Real docking block entity absent at " + pos);
                connectors.add((DockingConnectorBlockEntity) level.getBlockEntity(pos));
            }
            DockingConnectorBlockEntity a = connectors.get(0), b = connectors.get(1), c = connectors.get(2);
            checkCase(passed, "tank/real-docking-both-wrappers-nested-abort", () -> dockingTanks(a, b));
            checkCase(passed, "tank/real-docking-retarget-disconnect-rollback", () -> retargetTanks(a, b, c));
            checkCase(passed, "item/real-duo-solo-backend-routing-nested-abort", () -> dockingItems(a, b));
        } finally {
            for (int i = placed.size() - 1; i >= 0; i--) {
                BlockPos pos = placed.get(i);
                if (level.getBlockEntity(pos) instanceof DockingConnectorBlockEntity connector) {
                    connector.tank.disconnect();
                    connector.battery.disconnect();
                    connector.inventory.clearContent();
                }
                if (level.getBlockState(pos).is(SimBlocks.DOCKING_CONNECTOR.get())) {
                    require(level.removeBlock(pos, false), "Cannot clean probe connector at " + pos);
                }
            }
        }
    }

    private static void dockingTanks(DockingConnectorBlockEntity a, DockingConnectorBlockEntity b) {
        FluidResource water = FluidResource.of(Fluids.WATER);
        seed(a.tank, water, 200); seed(b.tank, water, 300);
        a.tank.connect(b.getBlockPos(), b.tank); b.tank.connect(a.getBlockPos(), a.tank);
        require(a.tank.insertionTarget() == b.tank, "real A tank resolves B");
        SingleTankWrapper first = new SingleTankWrapper(a.tank), second = new SingleTankWrapper(b.tank);
        try (Transaction tx = Transaction.openRoot()) {
            equal(100, first.insert(0, water, 100, tx), "real dock routed insertion");
            equal(50, second.extract(0, water, 50, tx), "other wrapper accesses B backend");
            equal(50, first.extract(0, water, 50, tx), "same wrapper also extracts local A");
        }
        tank(a.tank, water, 200, "mixed-direction A rollback");
        tank(b.tank, water, 300, "shared-backend B rollback");
        try (Transaction root = Transaction.openRoot()) {
            equal(100, first.insert(0, water, 100, root), "outer routed insert");
            try (Transaction child = Transaction.open(root)) {
                equal(50, second.extract(0, water, 50, child), "nested peer extraction");
                equal(25, first.extract(0, water, 25, child), "nested local extraction");
                child.commit();
            }
        }
        tank(a.tank, water, 200, "parent abort restores committed child A");
        tank(b.tank, water, 300, "parent abort restores committed child B");
    }

    private static void retargetTanks(DockingConnectorBlockEntity a, DockingConnectorBlockEntity b, DockingConnectorBlockEntity c) {
        FluidResource water = FluidResource.of(Fluids.WATER);
        seed(a.tank, water, 200); seed(b.tank, water, 300); seed(c.tank, water, 400);
        a.tank.connect(b.getBlockPos(), b.tank);
        SingleTankWrapper handler = new SingleTankWrapper(a.tank);
        try (Transaction tx = Transaction.openRoot()) {
            equal(100, handler.insert(0, water, 100, tx), "old actual tank recipient");
            a.tank.connect(c.getBlockPos(), c.tank);
            equal(25, handler.insert(0, water, 25, tx), "new actual tank recipient");
            a.tank.disconnect();
        }
        tank(a.tank, water, 200, "retarget leaves local tank unchanged");
        tank(b.tank, water, 300, "retarget restores old tank recipient");
        tank(c.tank, water, 400, "disconnect still restores new tank recipient");
        a.tank.connect(b.getBlockPos(), b.tank);
        try (Transaction tx = Transaction.openRoot()) {
            equal(0, handler.insert(0, FluidResource.of(Fluids.LAVA), 100, tx), "routed mismatch rejects without transmutation");
            equal(20, handler.insert(0, water, 20, tx), "routed tank commit");
            tx.commit();
        }
        tank(a.tank, water, 200, "routed commit keeps local buffer");
        tank(b.tank, water, 320, "routed commit changes actual recipient");
    }

    private static void dockingItems(DockingConnectorBlockEntity a, DockingConnectorBlockEntity b) {
        a.inventory.dock(); b.inventory.dock();
        ItemResource stone = ItemResource.of(namedItem(Items.STONE, 1, "duo"));
        a.inventory.setItem(0, stone.toStack(4)); b.inventory.setItem(0, stone.toStack(6));
        ContainerWrapper<DockingConnectorDuoInventory> ab = new ContainerWrapper<>(new DockingConnectorDuoInventory(a, b));
        ContainerWrapper<DockingConnectorDuoInventory> ba = new ContainerWrapper<>(new DockingConnectorDuoInventory(b, a));
        ContainerWrapper<DockingConnectorSoloInventory> soloB = new ContainerWrapper<>(b.inventory);
        item(ab.getStackInSlot(0), stone, 6, "duo zero exposes remote input");
        item(ab.getStackInSlot(1), stone, 4, "duo one exposes local output");
        try (Transaction root = Transaction.openRoot()) {
            equal(3, ab.insert(0, stone, 3, root), "AB inserts into B");
            equal(2, ba.insert(0, stone, 2, root), "BA inserts into A");
            equal(1, soloB.extract(0, stone, 1, root), "solo shares B backend");
            equal(2, ab.extract(1, stone, 2, root), "AB extracts local A");
            equal(0, ab.insert(1, stone, 1, root), "output slot rejects insertion");
            equal(0, ab.extract(0, stone, 1, root), "input slot rejects extraction");
        }
        item(a.inventory.getItem(0), stone, 4, "duo/solo rollback restores A");
        item(b.inventory.getItem(0), stone, 6, "duo/solo rollback restores B");
        try (Transaction root = Transaction.openRoot()) {
            equal(3, ab.insert(0, stone, 3, root), "outer duo insert");
            try (Transaction child = Transaction.open(root)) {
                equal(2, ba.extract(1, stone, 2, child), "other duo reads actual B");
                equal(1, ab.extract(1, stone, 1, child), "nested local duo extract");
                child.commit();
            }
            equal(1, ab.insert(0, stone, 1, root), "outer mutation after child duo commit");
        }
        item(a.inventory.getItem(0), stone, 4, "parent abort restores A after committed duo child");
        item(b.inventory.getItem(0), stone, 6, "parent abort restores B after committed duo child");
    }

    private static DockingConnectorSoloInventory solo() {
        DockingConnectorSoloInventory inventory = new DockingConnectorSoloInventory();
        inventory.dock();
        return inventory;
    }

    private static FluidStack namedFluid(String name) {
        FluidStack stack = new FluidStack(Fluids.WATER, 1);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        CompoundTag data = new CompoundTag(); data.putInt("transfer-check", 7);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    private static ItemStack namedItem(Item item, int count, String name) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        CompoundTag data = new CompoundTag(); data.putString("transfer-check", name);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    private static void seed(SingleTank tank, FluidResource resource, long amount) {
        require(amount >= 0 && amount <= tank.capacity, "invalid tank fixture amount");
        tank.amount = amount;
        tank.type = amount == 0 ? CFluidType.BLANK : SingleTankWrapper.toCType(resource.toStack(1));
    }

    private static void tank(SingleTank actual, FluidResource expected, long amount, String label) {
        equal(amount, actual.amount, label + " amount");
        if (amount == 0) {
            require(actual.type.isBlank() && new SingleTankWrapper(actual).getResource(0).isEmpty(), label + " blank identity");
        } else {
            require(new SingleTankWrapper(actual).getResource(0).equals(expected), label + " fluid/components");
        }
    }

    private static void item(ItemStack actual, ItemResource expected, int count, String label) {
        equal(count, actual.getCount(), label + " count");
        require(count == 0 ? actual.isEmpty() : expected.matches(actual), label + " item/components");
    }

    private static void stack(ItemStack expected, ItemStack actual, String label) {
        equal(expected.getCount(), actual.getCount(), label + " count");
        require(ItemStack.isSameItemSameComponents(expected, actual), label + " item/components");
    }

    private static Set<Integer> indices(Set<ContainerSlot> slots) {
        Set<Integer> result = new HashSet<>();
        for (ContainerSlot slot : slots) result.add(slot.getIndex());
        return result;
    }

    private static CompoundTag nbt(CompoundTag value) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) { NbtIo.write(value, output); }
            try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { return NbtIo.read(input); }
        } catch (IOException failure) {
            throw new IllegalStateException("In-memory binary NBT round trip failed", failure);
        }
    }

    private static void checkCase(List<String> passed, String name, Runnable check) {
        try {
            check.run();
            passed.add(name);
        } catch (RuntimeException | Error failure) {
            throw new IllegalStateException("Transfer runtime check failed: " + name, failure);
        }
    }

    private static void illegalArgument(Runnable action, String label) {
        try { action.run(); }
        catch (IllegalArgumentException expected) { return; }
        throw new IllegalStateException(label + ": expected IllegalArgumentException");
    }

    private static void equal(long expected, long actual, String label) {
        require(expected == actual, label + ": expected " + expected + ", actual " + actual);
    }

    private static void require(boolean condition, String detail) {
        if (!condition) throw new IllegalStateException(detail);
    }

    /** Concrete owner hooks for real MultiSlotContainer storage; no API mocking. */
    private static final class ProbeInventory extends MultiSlotContainer {
        private int notifications;
        private ProbeInventory(int size) { super(size); }
        @Override public void setChanged() { notifications++; }
    }

    private static final class CountingTank extends SingleTank {
        private int notifications;
        private CountingTank() { super(1000); }
        @Override public void onContentsChanged() { notifications++; }
    }
}

