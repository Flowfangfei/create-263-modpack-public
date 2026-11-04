package dev.eriksonn.aeronautics.neoforge;

import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.ServerBalloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.map.BalloonMap;
import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlockEntity;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.eriksonn.aeronautics.index.AeroLiftingGasTypes;
import dev.eriksonn.aeronautics.index.AeroTags;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLPaths;

/** Disposable smoke-world gas/lift-output check; no event registration or aircraft movement claim. */
public final class AeroBuoyancyChecks {
    private static final BlockPos BURNER = new BlockPos(40, 90, 16), POWER = BURNER.east();
    private static final Path ROOT = Path.of(System.getProperty("create263.probeRoot", "run/disabled-server-probe")).toAbsolutePath().normalize();
    private static final List<BlockPos> placed = new ArrayList<>();
    private static final Map<String, Object> result = new LinkedHashMap<>();
    private static final boolean[] wasForced = new boolean[2], ticketTouched = new boolean[2];
    private static ServerLevel owner;
    private static HotAirBurnerBlockEntity burner;
    private static ServerBalloon balloon;
    private static Map<String, Object> completed;
    private static boolean active;
    private static int offTick = -1;
    private static double filledBeforeOff;
    private AeroBuoyancyChecks() {}

    public static void start(ServerLevel level) {
        require(level.getServer().isSameThread(), "Buoyancy check must run on server thread");
        require(Boolean.getBoolean("aeronautics.adapted.runtimeChecks"), "Explicit runtimeChecks property required");
        require(FMLPaths.GAMEDIR.get().toAbsolutePath().normalize().equals(ROOT), "Non-smoke world rejected");
        require(!active, "Buoyancy check already running");
        owner = level; active = true; completed = null; offTick = -1;
        burner = null; balloon = null; placed.clear(); result.clear();
        try {
            for (int z = 0; z < 2; z++) {
                wasForced[z] = level.getForceLoadedChunks().contains(new ChunkPos(2, z).pack());
                ticketTouched[z] = true; level.setChunkForced(2, z, true);
            }
            for (int x = 38; x <= 42; x++) for (int y = 90; y <= 96; y++) for (int z = 14; z <= 18; z++) {
                BlockPos pos = new BlockPos(x, y, z);
                require(level.getBlockState(pos).isAir() && level.getBlockEntity(pos) == null, "Occupied probe region: " + pos);
            }
            require(Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.WHITE).defaultBlockState().is(AeroTags.BlockTags.AIRTIGHT), "White wool lacks real airtight tag");
            require(BalloonMap.MAP.get(level).getBalloon(BURNER.above(5)) == null, "Existing balloon overlaps test region");
            for (int x = 38; x <= 42; x++) for (int y = 92; y <= 96; y++) for (int z = 14; z <= 18; z++)
                if (y == 96 || x == 38 || x == 42 || z == 14 || z == 18)
                    put(level, new BlockPos(x, y, z), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.WHITE).defaultBlockState());
            put(level, BURNER, AeroBlocks.HOT_AIR_BURNER.getDefaultState());
            require(level.getBlockEntity(BURNER) instanceof HotAirBurnerBlockEntity, "Registered burner block entity absent");
            burner = (HotAirBurnerBlockEntity) level.getBlockEntity(BURNER);
            put(level, POWER, Blocks.REDSTONE_BLOCK.defaultBlockState());
            burner.updateSignal(); // Read actual neighboring redstone; never use the Ponder-only setter.
            result.put("burner_position", BURNER.toString());
            result.put("scope", "real_burner_balloon_registration_gas_and_lift_output");
        } catch (RuntimeException | Error failure) { abort(level, failure); throw failure; }
    }

    /** Call every server tick after start; null while pending, immutable results when complete. */
    public static Map<String, Object> tick(ServerLevel level, int ticksSinceStart) {
        if (!active) return completed;
        require(level == owner && level.getServer().isSameThread(), "Wrong level/thread for buoyancy check");
        try {
            if (offTick < 0 && ticksSinceStart >= 60) {
                burner.updateSignal();
                require(burner.canOutputGas() && burner.getSignalStrength() > 0 && burner.getGasOutput() > 0, "Real redstone did not supply gas");
                require(burner.getLiftingGasType() == AeroLiftingGasTypes.DEFAULT_GAS.get(), "Wrong registered lifting gas");
                require(burner.getBalloon() instanceof ServerBalloon, "Burner did not create/join real ServerBalloon");
                balloon = (ServerBalloon) burner.getBalloon();
                require(burner.getCastPosition() != null, "Natural burner raycast missed envelope");
                require(BalloonMap.MAP.get(level).getBalloon(burner.getCastPosition()) == balloon, "BalloonMap registration missing");
                require(balloon.getHeaters().contains(burner) && balloon.getCapacity() > 0, "Balloon lacks heater/capacity");
                filledBeforeOff = balloon.getTotalFilledVolume();
                double lift = balloon.getTotalLift();
                require(Double.isFinite(filledBeforeOff) && filledBeforeOff > 0, "Natural balloon tick produced no finite gas");
                require(Double.isFinite(lift) && lift > 0, "Registered gas produced no finite lift output");
                require(balloon.getLiftingGasHolders().stream().anyMatch(h -> h.type() == burner.getLiftingGasType() && h.data().amount > 0), "Registered gas holder is empty");
                result.put("capacity", balloon.getCapacity()); result.put("cast_position", burner.getCastPosition().toString());
                result.put("signal", burner.getSignalStrength()); result.put("gas_output", burner.getGasOutput());
                result.put("filled_volume", filledBeforeOff); result.put("lift_output", lift);
                result.put("gas_class", burner.getLiftingGasType().getClass().getSimpleName());
                require(level.setBlock(POWER, Blocks.AIR.defaultBlockState(), 3), "Could not remove own redstone source");
                burner.updateSignal(); offTick = ticksSinceStart;
            }
            if (offTick >= 0 && ticksSinceStart >= offTick + 40) {
                require(!burner.canOutputGas() && burner.getGasOutput() == 0, "Redstone removal did not stop supply");
                double cooled = balloon.getTotalFilledVolume();
                require(balloon.getTotalTargetVolume() == 0 && Double.isFinite(cooled) && cooled >= 0 && cooled < filledBeforeOff, "Natural gas decay after shutdown failed");
                result.put("filled_after_shutdown", cooled); result.put("shutdown_target", balloon.getTotalTargetVolume());
                cleanup(level); result.put("cleanup_completed", true); result.put("passed", true);
                completed = Map.copyOf(result); return completed;
            }
            return null;
        } catch (RuntimeException | Error failure) { abort(level, failure); throw failure; }
    }

    private static void put(ServerLevel level, BlockPos pos, BlockState state) {
        placed.add(pos);
        require(level.setBlock(pos, state, 3), "Could not place own probe block at " + pos);
    }

    private static void abort(ServerLevel level, Throwable failure) {
        try { cleanup(level); } catch (RuntimeException | Error cleanupFailure) { failure.addSuppressed(cleanupFailure); }
    }

    private static void cleanup(ServerLevel level) {
        try {
            // Remove only this test's registered balloon before opening its envelope.
            var map = BalloonMap.MAP.get(level);
            for (var it = map.getBalloons().iterator(); it.hasNext();) {
                var candidate = it.next();
                if (candidate == balloon || (burner != null && candidate.getHeaters().contains(burner))) {
                    it.remove(); candidate.onRemoved(); map.markDirty();
                }
            }
            if (burner != null) burner.setBalloon(null);
            for (int i = placed.size() - 1; i >= 0; i--) level.setBlock(placed.get(i), Blocks.AIR.defaultBlockState(), 3);
        } finally {
            for (int z = 0; z < 2; z++) if (ticketTouched[z]) {
                level.setChunkForced(2, z, wasForced[z]); ticketTouched[z] = false;
            }
            active = false; placed.clear();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("Buoyancy check: " + message);
    }
}

