package com.george_vi.electroenergetics.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/** Coordinates stay in the level's coordinate system when no moving-world provider is installed. */
public final class SpatialIntegration {
    public interface Pose {
        Vec3 transformPosition(Vec3 position);

        Vec3 transformPositionInverse(Vec3 position);

        Vec3 transformDirection(Vec3 direction);

        Vec3 transformDirectionInverse(Vec3 direction);
    }

    public interface SubLevel {
        Pose logicalPose();

        Pose lastPose();
    }

    public interface Provider {
        @Nullable
        SubLevel containing(Level level, Vec3 position);

        default boolean isLoaded(Level level, BlockPos position) {
            return level.hasChunkAt(position);
        }

        default Vec3 gravity(Level level, Vec3 position) {
            return new Vec3(0, -1, 0);
        }
    }

    private static Provider provider = (level, position) -> null;

    private SpatialIntegration() {}

    public static void register(Provider implementation) {
        provider = Objects.requireNonNull(implementation, "implementation");
    }

    public static @Nullable SubLevel containing(Level level, Vec3 position) {
        return provider.containing(level, position);
    }

    public static @Nullable SubLevel containing(Level level, BlockPos position) {
        return containing(level, Vec3.atCenterOf(position));
    }

    public static @Nullable SubLevel getSubLevel(Level level, BlockPos position) {
        return containing(level, position);
    }

    public static boolean isDynamic(Level level, BlockPos position) {
        return containing(level, position) != null;
    }

    public static boolean isSubLevel(Level level, BlockPos position) {
        return isDynamic(level, position);
    }

    public static boolean isSubLevelLoaded(Level level, BlockPos position) {
        return provider.isLoaded(level, position);
    }

    public static Vec3 projectOutOfSubLevel(Level level, Position position) {
        Vec3 point = new Vec3(position.x(), position.y(), position.z());
        SubLevel subLevel = containing(level, point);
        return subLevel == null ? point : subLevel.logicalPose().transformPosition(point);
    }

    public static Vec3 projectIntoSubLevel(Level level, BlockPos origin, Vec3 position) {
        SubLevel subLevel = containing(level, origin);
        return subLevel == null
                ? position
                : subLevel.logicalPose().transformPositionInverse(position);
    }

    public static double distanceSquaredWithSubLevels(Level level, Vec3 first, Vec3 second) {
        return projectOutOfSubLevel(level, first)
                .distanceToSqr(projectOutOfSubLevel(level, second));
    }

    public static Vec3 velocityAtPoint(Level level, Vec3 position) {
        SubLevel subLevel = containing(level, position);
        return subLevel == null
                ? Vec3.ZERO
                : subLevel.logicalPose()
                        .transformPosition(position)
                        .subtract(subLevel.lastPose().transformPosition(position))
                        .scale(20);
    }

    public static Vec3 getGravity(Level level, Vec3 position) {
        return provider.gravity(level, position);
    }
}
