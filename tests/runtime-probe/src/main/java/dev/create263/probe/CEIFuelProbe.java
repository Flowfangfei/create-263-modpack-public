package dev.create263.probe;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** Real player and registered mechanical-arm entry points, only in a guarded disposable overworld. */
public final class CEIFuelProbe {
    private static final String CEI = "plus.dragons.createenchantmentindustry.common.";
    private static final String[] ROLES = {"blaze_enchanter", "blaze_forger", "classic_blaze_enchanter"};
    private CEIFuelProbe() {}

    public static Map<String, Object> run(ServerLevel level) {
        guard(level);
        Object feature = field(call(type("plus.dragons.createenchantmentindustry.config.CEIConfig"), "features"), "classicBlazeEnchanter");
        require(field(feature, "override") == null, "Classic feature has a mod-property override; cannot temporarily enable it");
        Object rawFeature = field(feature, "value");
        require(rawFeature != null, "Classic feature is not registered");
        Object previousFeature = call(rawFeature, "get");
        List<BlockPos> planned = new ArrayList<>();
        for (int role = 0; role < ROLES.length; role++)
            for (int cell = 0; cell < 6; cell++) planned.add(new BlockPos(400 + role * 64 + cell * 8, 8, 40));
        for (BlockPos pos : planned)
            require(level.getBlockState(pos).isAir() && level.getBlockEntity(pos) == null, "Fuel fixture cell is occupied: " + pos);
        FakePlayer player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "CEIFuelProbe"));
        require(!player.hasInfiniteMaterials(), "Fuel player must consume items");
        List<ItemStack> savedInventory = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++)
            savedInventory.add(player.getInventory().getItem(slot).copy());
        ItemStack savedOffhand = player.getOffhandItem().copy();
        Vec3 savedPosition = player.position();
        List<BlockPos> created = new ArrayList<>();
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> operations = new ArrayList<>();
        Throwable primary = null;
        try {
            call(rawFeature, "set", true);
            require(Boolean.TRUE.equals(call(feature, "get")), "Classic feature did not enable");
            for (int index = 0; index < ROLES.length; index++) {
                String role = ROLES[index];
                int start = 400 + index * 64;
                BlockPos armPos = new BlockPos(start + 40, 8, 40);
                BlockEntity arm = place(level, armPos, "create:mechanical_arm", created);
                require(type("com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity").isInstance(arm), "Fixture is not a real arm");
                Object registered = call(field(type(CEI + "registry.CEIArmInterationPoints"), role.toUpperCase(java.util.Locale.ROOT)), "get");
                Map<String, Object> measured = new LinkedHashMap<>();
                measured.put("machine", role);
                measured.put("registered_arm_type", registered.getClass().getName());

                BlockPos playerFuelPos = new BlockPos(start, 8, 40);
                BlockEntity playerFuel = place(level, playerFuelPos, "create_enchantment_industry:" + role, created);
                require(number(call(call(playerFuel, "getNormalTank"), "getCapacity")) >= 1000, "Normal tank cannot hold one XP bucket");
                clearPlayer(player);
                ItemStack playerBucket = named("create_enchantment_industry:experience_bucket", role + "/player-fuel");
                ItemStack playerBucketBefore = playerBucket.copy();
                player.setItemInHand(InteractionHand.MAIN_HAND, playerBucket);
                InteractionResult playerFuelResult = use(level, playerFuelPos, player);
                require(playerFuelResult.consumesAction(), "Player fuel was not consumed: " + role);
                equal(1000, normal(playerFuel), "Player XP credit " + role);
                equal(0, special(playerFuel), "Player special tank changed " + role);
                plainBucket(player.getMainHandItem(), "Player empty bucket " + role);
                equal(1, inventoryCount(player, Items.BUCKET), "Player empty bucket returned once " + role);
                equal(0, inventoryCount(player, playerBucketBefore.getItem()), "Player fuel remained " + role);
                require(playerBucket.isEmpty(), "Player fuel input was not consumed exactly once");
                measured.put("player_experience_bucket", Map.of("normal_xp", normal(playerFuel), "returned_buckets", 1, "input_consumed", 1, "entry", "BlockState.useItemOn"));

                BlockPos armFuelPos = new BlockPos(start + 8, 8, 40);
                BlockEntity armFuel = place(level, armFuelPos, "create_enchantment_industry:" + role, created);
                Object fuelPoint = point(registered, level, armFuelPos);
                ItemStack armBucket = named("create_enchantment_industry:experience_bucket", role + "/arm-fuel");
                ItemStack armBucketBefore = armBucket.copy();
                MachineSnapshot emptyFuel = snapshot(level, armFuelPos, armFuel);
                ItemStack simulatedBucket = (ItemStack) call(fuelPoint, "insert", arm, armBucket, true);
                plainBucket(simulatedBucket, "Simulated arm bucket " + role);
                unchanged(level, armFuelPos, armFuel, emptyFuel, "Arm fuel simulation " + role);
                same(armBucketBefore, armBucket, "Arm simulation original input/components " + role);
                ItemStack actualBucket = (ItemStack) call(fuelPoint, "insert", arm, armBucket, false);
                plainBucket(actualBucket, "Actual arm bucket " + role);
                equal(1000, normal(armFuel), "Arm XP credit " + role);
                equal(0, special(armFuel), "Arm special tank changed " + role);
                same(armBucketBefore, armBucket, "Arm actual original input/components " + role);
                measured.put("arm_experience_bucket", Map.of("normal_xp", normal(armFuel), "returned_buckets", actualBucket.getCount(), "simulation_unchanged", true, "original_input_and_components_preserved", true));

                BlockPos playerSwordPos = new BlockPos(start + 16, 8, 40);
                BlockEntity playerSword = place(level, playerSwordPos, "create_enchantment_industry:" + role, created);
                prepareSwordInput(level, playerSword, role);
                clearPlayer(player);
                ItemStack sword = named("minecraft:diamond_sword", role + "/player-input");
                ItemStack swordBefore = sword.copy();
                Object nonFuel = call(type(CEI + "fluids.experience.BlazeExperienceBlock"), "applyFuel", level.getBlockState(playerSwordPos), level, playerSwordPos, sword.copy(), false, false, true);
                require(call(nonFuel, "result") == InteractionResult.PASS, "Sword did not take the non-fuel PASS path");
                player.setItemInHand(InteractionHand.MAIN_HAND, sword);
                require(use(level, playerSwordPos, player).consumesAction(), "Player sword did not reach the subclass inventory: " + role);
                require(player.getMainHandItem().isEmpty(), "Player retained inserted sword " + role);
                same(swordBefore, stored(playerSword, role), "Player inventory components " + role);
                same(swordBefore, sword, "Player insertion source/components " + role);
                equal(0, inventoryCount(player, Items.DIAMOND_SWORD), "Player duplicated sword " + role);
                measured.put("player_non_fuel_inventory", Map.of("item", "minecraft:diamond_sword", "pass_fallthrough", true, "stored_count", 1, "components_preserved", true));

                BlockPos armSwordPos = new BlockPos(start + 24, 8, 40);
                BlockEntity armSword = place(level, armSwordPos, "create_enchantment_industry:" + role, created);
                prepareSwordInput(level, armSword, role);
                Object swordPoint = point(registered, level, armSwordPos);
                ItemStack armInput = named("minecraft:diamond_sword", role + "/arm-input");
                ItemStack armInputBefore = armInput.copy();
                MachineSnapshot emptyInventory = snapshot(level, armSwordPos, armSword);
                require(((ItemStack) call(swordPoint, "insert", arm, armInput, true)).isEmpty(), "Arm sword simulation rejected input " + role);
                require(stored(armSword, role).isEmpty(), "Arm sword simulation mutated inventory " + role);
                unchanged(level, armSwordPos, armSword, emptyInventory, "Arm sword simulation " + role);
                same(armInputBefore, armInput, "Arm sword simulated source/components " + role);
                require(((ItemStack) call(swordPoint, "insert", arm, armInput, false)).isEmpty(), "Arm sword insertion rejected input " + role);
                same(armInputBefore, stored(armSword, role), "Arm inventory components " + role);
                same(armInputBefore, armInput, "Arm sword actual source/components " + role);
                measured.put("arm_non_fuel_inventory", Map.of("stored_count", 1, "simulation_unchanged", true, "original_input_and_components_preserved", true));

                BlockPos creativePos = new BlockPos(start + 32, 8, 40);
                BlockEntity creative = place(level, creativePos, "create_enchantment_industry:" + role, created);
                Object creativePoint = point(registered, level, creativePos);
                ItemStack cake = named("create:creative_blaze_cake", role + "/creative");
                ItemStack cakeBefore = cake.copy();
                MachineSnapshot beforeCreative = snapshot(level, creativePos, creative);
                require(!beforeCreative.creative(), "Creative fixture was already creative");
                require(((ItemStack) call(creativePoint, "insert", arm, cake, true)).isEmpty(), "Creative arm simulation rejected fuel " + role);
                unchanged(level, creativePos, creative, beforeCreative, "Creative arm simulation " + role);
                same(cakeBefore, cake, "Creative simulation source/components " + role);
                require(((ItemStack) call(creativePoint, "insert", arm, cake, false)).isEmpty(), "Creative arm actual rejected fuel " + role);
                require(Boolean.TRUE.equals(call(creative, "isCreative")), "Creative fuel did not make the machine creative " + role);
                require(!level.getBlockState(creativePos).equals(beforeCreative.state()), "Creative fuel did not change heat state " + role);
                equal(number(call(call(creative, "getNormalTank"), "getCapacity")), normal(creative), "Creative normal tank " + role);
                equal(0, special(creative), "First creative fuel unexpectedly filled special tank " + role);
                same(cakeBefore, cake, "Creative actual source/components " + role);
                measured.put("arm_creative_fuel", Map.of("simulation_preserved_creative_tanks_and_heat", true, "actual_creative", true, "normal_xp", normal(creative), "special_xp", special(creative), "source_components_preserved", true));
                operations.add(measured);
            }
            result.put("machines", operations);
            result.put("machine_count", operations.size());
            result.put("operation_count", operations.size() * 5);
            result.put("uncovered", List.of("Full enchanting/forging output and XP debit", "Belt transport and long-running mechanical-arm motion", "Overflow, full tanks, stacked fuel and special-fuel variants", "Player/client GUI and visual synchronization"));
        } catch (RuntimeException | Error error) {
            primary = error;
            throw error;
        } finally {
            RuntimeException cleanupError = null;
            try { call(rawFeature, "set", previousFeature); require(previousFeature.equals(call(rawFeature, "get")), "Classic raw feature was not restored"); }
            catch (RuntimeException error) { cleanupError = error; }
            try {
                player.getInventory().clearContent();
                for (int slot = 0; slot < savedInventory.size(); slot++) player.getInventory().setItem(slot, savedInventory.get(slot));
                player.setItemInHand(InteractionHand.OFF_HAND, savedOffhand);
                player.setPos(savedPosition);
            } catch (RuntimeException error) { if (cleanupError == null) cleanupError = error; else cleanupError.addSuppressed(error); }
            for (int cell = created.size() - 1; cell >= 0; cell--) {
                BlockPos pos = created.get(cell);
                try {
                    BlockEntity entity = level.getBlockEntity(pos);
                    if (entity instanceof Clearable clearable) clearable.clearContent();
                    call(type(CEI + "fluids.experience.ExperienceFluidDropContext"), "suppressDuring", (Runnable) () -> level.removeBlock(pos, false));
                    require(level.getBlockState(pos).isAir() && level.getBlockEntity(pos) == null, "Fixture cleanup failed at " + pos);
                } catch (RuntimeException error) { if (cleanupError == null) cleanupError = error; else cleanupError.addSuppressed(error); }
            }
            if (cleanupError != null) { if (primary != null) primary.addSuppressed(cleanupError); else throw cleanupError; }
        }
        result.put("classic_raw_feature_restored", true);
        result.put("created_cells_cleaned", created.size());
        return result;
    }

    private static void guard(ServerLevel level) {
        try {
            require(Boolean.getBoolean("create263.fiveAddonsProbe"), "CEI fuel probe is not explicitly enabled");
            require(level != null && level == level.getServer().overworld() && level.getServer().isSameThread(), "CEI fuel probe requires the actual server-thread overworld");
            String configured = System.getProperty("create263.probeRoot");
            require(configured != null && !configured.isBlank(), "Missing disposable probe root");
            Path root = Path.of(configured).toRealPath();
            require(root.equals(FMLPaths.GAMEDIR.get().toRealPath()), "Probe root does not match game directory");
            require(Files.isRegularFile(root.resolve(".create263-disposable-runtime"), LinkOption.NOFOLLOW_LINKS), "Missing disposable runtime marker");
            Path world = level.getServer().getWorldPath(LevelResource.ROOT).toRealPath();
            require(world.startsWith(root) && world.equals(root.resolve("world").toRealPath()), "Probe world does not match disposable root/world");
        } catch (java.io.IOException error) { throw new IllegalStateException("CEI fuel safety guard failed", error); }
    }
    private static BlockEntity place(ServerLevel level, BlockPos pos, String id, List<BlockPos> created) {
        require(level.getBlockState(pos).isAir() && level.getBlockEntity(pos) == null, "Occupied fixture cell " + pos);
        var block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
        require(block != null && block != Blocks.AIR, "Missing block " + id);
        created.add(pos);
        require(level.setBlock(pos, block.defaultBlockState(), 3), "Could not place " + id);
        BlockEntity entity = level.getBlockEntity(pos);
        require(entity != null, "Missing block entity for " + id);
        require(entity instanceof SmartBlockEntity, "Fixture is not a real SmartBlockEntity: " + id);
        ((SmartBlockEntity) entity).initialize();
        return entity;
    }
    private static Object point(Object registered, ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        require(Boolean.TRUE.equals(call(registered, "canCreatePoint", level, pos, state)), "Registered arm type rejected machine at " + pos);
        Object point = call(registered, "createPoint", level, pos, state);
        require(point != null, "Registered type did not create an arm point");
        return point;
    }
    private static void prepareSwordInput(ServerLevel level, BlockEntity entity, String role) {
        if (role.equals("blaze_enchanter")) {
            Object behaviour = field(entity, "enchanter");
            ((ScrollValueBehaviour) behaviour).setValue(30);
            ItemStack candidate = new ItemStack(Items.DIAMOND_SWORD);
            call(behaviour, "update", candidate);
            require(Boolean.TRUE.equals(call(behaviour, "canProcess", candidate)), "Enchanter sword fixture cannot process at level 30");
        }
        if (role.equals("classic_blaze_enchanter")) {
            ItemStack template = named("create_enchantment_industry:enchanting_template", role + "/filter");
            template.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS), 1);
            Object behaviour = field(entity, "enchanter");
            require(Boolean.TRUE.equals(call(behaviour, "setFilter", template)), "Classic rejected the sharpness template");
            require(Boolean.TRUE.equals(call(behaviour, "canProcess", new ItemStack(Items.DIAMOND_SWORD))), "Classic sword fixture cannot process");
        }
    }
    private static InteractionResult use(ServerLevel level, BlockPos pos, FakePlayer player) {
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 2.0);
        return level.getBlockState(pos).useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.SOUTH, pos, false));
    }
    private static ItemStack stored(BlockEntity entity, String role) {
        return role.equals("blaze_forger") ? (ItemStack) call(field(entity, "inventory"), "getStackInSlot", 0) : (ItemStack) field(entity, "heldItem");
    }
    private static ItemStack named(String id, String token) {
        var item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
        require(item != null && item != Items.AIR, "Missing item " + id);
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("CEIFuelProbe/" + token));
        return stack;
    }
    private static void clearPlayer(FakePlayer player) { player.getInventory().clearContent(); player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY); }
    private static int inventoryCount(FakePlayer player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }
    private static void plainBucket(ItemStack stack, String context) { same(new ItemStack(Items.BUCKET), stack, context); }
    private static void same(ItemStack expected, ItemStack actual, String context) { require(ItemStack.matches(expected, actual), context + ": expected=" + expected + ", actual=" + actual); }
    private static int normal(Object entity) { return number(call(entity, "getNormalExperience")); }
    private static int special(Object entity) { return number(call(entity, "getSpecialExperience")); }
    private static int number(Object value) { return ((Number) value).intValue(); }
    private record MachineSnapshot(boolean creative, int normal, int special, BlockState state) {}
    private static MachineSnapshot snapshot(ServerLevel level, BlockPos pos, Object entity) { return new MachineSnapshot((Boolean) call(entity, "isCreative"), normal(entity), special(entity), level.getBlockState(pos)); }
    private static void unchanged(ServerLevel level, BlockPos pos, Object entity, MachineSnapshot expected, String context) { require(expected.equals(snapshot(level, pos, entity)), context + " mutated creative flag, tanks, or heat state"); }
    private static void equal(int expected, int actual, String context) { require(expected == actual, context + ": expected=" + expected + ", actual=" + actual); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static Class<?> type(String name) { try { return Class.forName(name); } catch (ClassNotFoundException error) { throw new IllegalStateException(name, error); } }
    private static Object field(Object object, String name) {
        Class<?> current = object instanceof Class<?> clazz ? clazz : object.getClass();
        for (; current != null; current = current.getSuperclass()) {
            try { Field field = current.getDeclaredField(name); field.setAccessible(true); return field.get(object instanceof Class<?> ? null : object); }
            catch (NoSuchFieldException ignored) {} catch (ReflectiveOperationException error) { throw new IllegalStateException(name, error); }
        }
        throw new IllegalStateException("Missing field " + name);
    }
    private static Object call(Object object, String name, Object... arguments) {
        Class<?> current = object instanceof Class<?> clazz ? clazz : object.getClass();
        for (; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != arguments.length || method.isBridge()) continue;
                Class<?>[] parameters = method.getParameterTypes();
                boolean accepted = true;
                for (int index = 0; index < parameters.length; index++) {
                    Class<?> parameter = parameters[index];
                    if (parameter == boolean.class) parameter = Boolean.class;
                    else if (parameter == int.class) parameter = Integer.class;
                    if (arguments[index] != null && !parameter.isInstance(arguments[index])) { accepted = false; break; }
                }
                if (!accepted || object instanceof Class<?> && !Modifier.isStatic(method.getModifiers())) continue;
                try { method.setAccessible(true); return method.invoke(object instanceof Class<?> ? null : object, arguments); }
                catch (InvocationTargetException error) {
                    Throwable cause = error.getCause();
                    if (cause instanceof RuntimeException runtime) throw runtime;
                    if (cause instanceof Error fatal) throw fatal;
                    throw new IllegalStateException(name, cause);
                } catch (ReflectiveOperationException error) { throw new IllegalStateException(name, error); }
            }
        }
        throw new IllegalStateException("Missing method " + name + " on " + object);
    }
}
