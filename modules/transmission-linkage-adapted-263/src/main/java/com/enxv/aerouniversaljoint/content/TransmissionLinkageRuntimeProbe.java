package com.enxv.aerouniversaljoint.content;

import com.enxv.aerouniversaljoint.ModBlocks;
import com.enxv.aerouniversaljoint.content.hydraulic.HydraulicLengthControl;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Opt-in destructive fixture, restricted to an explicitly marked disposable runtime. */
public final class TransmissionLinkageRuntimeProbe {
    private static final Logger LOGGER = LoggerFactory.getLogger("TransmissionLinkageProbe");
    private static final BlockPos FIXED = new BlockPos(128, -60, 8);
    private static final BlockPos MOBILE = new BlockPos(132, -60, 8);
    private static int ticks;
    private static boolean done;
    private static HydraulicConnectionHeadBlockEntity fixed;
    private static HydraulicConnectionHeadBlockEntity mobile;
    private static PhysicsConstraintHandle previousConstraint;
    private static HydraulicConnectionHeadBlockEntity fixedHinge, mobileHinge;
    private static final JsonObject RESULT = new JsonObject();

    private TransmissionLinkageRuntimeProbe() {}

    public static void tick(ServerLevel level) {
        if (done || !Boolean.getBoolean("transmissionLinkage.runtimeProbe")) return;
        Path root = Path.of(System.getProperty("create263.probeRoot", "")).toAbsolutePath().normalize();
        if (!Files.isRegularFile(root.resolve(".create263-disposable-runtime"))) return;
        if (!net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().toAbsolutePath().normalize().equals(root)) return;
        if (!level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toAbsolutePath().normalize().equals(root.resolve("world"))) return;
        ticks++;
        try {
            if (ticks == 80) {
                RESULT.addProperty("startedAt", OffsetDateTime.now().toString());
                RESULT.addProperty("fixtureOrigin", FIXED.toShortString());
                if (Boolean.getBoolean("transmissionLinkage.probeReload")) {
                    fixed = (HydraulicConnectionHeadBlockEntity) level.getBlockEntity(FIXED);
                    require(fixed != null && fixed.hasLink(), "saved hydraulic endpoint did not reload");
                    mobile = fixed.getLoadedLinkedConnectionHead();
                    require(mobile != null && mobile.references(fixed), "saved mutual sublevel reference did not reload");
                    RESULT.addProperty("diskReload", true);
                } else {
                    prepare(level);
                }
            }
            if (ticks == 180) {
                require(fixed.references(mobile) && mobile.references(fixed), "mutual hydraulic references");
                previousConstraint = liveConstraint(mobile);
                require(previousConstraint != null && previousConstraint.isValid(), "automatic Sable actor constraint creation");
                RESULT.addProperty("constraintCreated", true);
                RESULT.addProperty("mobilePlotPosition", mobile.getBlockPos().toShortString());
                RESULT.addProperty("mobileSubLevelId", mobile.getContainingSubLevelId().toString());
                if (Boolean.getBoolean("transmissionLinkage.probeReload")) {
                    finish(root, true, false, null);
                    return;
                }
                PhysicsConstraintHandle rotary = readConstraint(mobileHinge, "hingeConstraintHandle");
                PhysicsConstraintHandle limited = readConstraint(fixedHinge, "hingeConstraintHandle");
                require(rotary != null && rotary.isValid(), "regular hinge creates a live rotary constraint");
                require(limited != null && limited.isValid(), "brass hinge creates a live limited generic constraint");
                RESULT.addProperty("hingeRotaryAndLimitedConstraints", true);
                ServerSubLevel moved = assemble(level, mobile.getBlockPos());
                mobile = hydraulicAt(level, moved.getPlot().getCenterBlock());
                require(fixed.references(mobile) && mobile.references(fixed), "actual assembly move remaps both endpoints");
                require(!previousConstraint.isValid(), "old constraint must be removed during assembly move");
                RESULT.addProperty("assemblyMoveReferences", true);
            }
            if (ticks == 280) {
                PhysicsConstraintHandle movedConstraint = liveConstraint(mobile);
                require(movedConstraint != null && movedConstraint.isValid(), "moved endpoint recreates its Sable constraint");
                mobile.detachLinkWithoutDrop();
                require(!fixed.hasLink() && !mobile.hasLink(), "detach clears both endpoint references");
                require(!movedConstraint.isValid(), "detach removes the actual native constraint");
                RESULT.addProperty("constraintRemoved", true);
                require(mobile.createMutualLink(fixed, true) == HydraulicConnectionHeadBlockEntity.LinkResult.SUCCESS, "relink for disk reload");
            }
            if (ticks == 340) finish(root, true, true, null);
        } catch (Throwable failure) {
            LOGGER.error("[TransmissionLinkageProbe] FAIL at tick {}", ticks, failure);
            finish(root, false, false, failure);
        }
    }

    /** Creates the fixture in the same real ServerLevel used by the other add-ons. */
    public static void prepare(ServerLevel level) {
        for (int x = 8; x <= 10; x++) level.setChunkForced(x, 0, true);
        BlockState state = ModBlocks.HYDRAULIC_CONNECTION_HEAD.get().defaultBlockState();
        level.setBlockAndUpdate(FIXED, state.setValue(HydraulicConnectionHeadBlock.FACING, Direction.EAST));
        level.setBlockAndUpdate(MOBILE, state.setValue(HydraulicConnectionHeadBlock.FACING, Direction.WEST));
        level.setBlockAndUpdate(MOBILE.below(), Blocks.IRON_BLOCK.defaultBlockState());
        fixed = hydraulicAt(level, FIXED);
        ServerSubLevel assembled = assemble(level, MOBILE);
        mobile = hydraulicAt(level, assembled.getPlot().getCenterBlock());
        require(mobile.createMutualLink(fixed, true) == HydraulicConnectionHeadBlockEntity.LinkResult.SUCCESS, "link ground to assembled endpoint");
        mobile.setExpectedLengthTenths(40);
        mobile.setReturnForce(3);
        mobile.setStretchResistance(2);
        CompoundTag saved = new CompoundTag();
        mobile.write(saved, level.registryAccess(), false);
        HydraulicConnectionHeadBlockEntity restored = new HydraulicConnectionHeadBlockEntity(mobile.getBlockPos(), mobile.getBlockState());
        restored.setLevel(level);
        restored.read(saved, level.registryAccess(), false);
        require(restored.getLinkedPos().equals(fixed.getBlockPos()), "hydraulic LinkedPos codec persistence");
        require(restored.getExpectedLengthTenths() == mobile.getExpectedLengthTenths(), "hydraulic length settings persistence");
        CompoundTag fixedSaved = new CompoundTag();
        fixed.write(fixedSaved, level.registryAccess(), false);
        HydraulicConnectionHeadBlockEntity restoredFixed = new HydraulicConnectionHeadBlockEntity(FIXED, fixed.getBlockState());
        restoredFixed.setLevel(level);
        restoredFixed.read(fixedSaved, level.registryAccess(), false);
        require(restoredFixed.getLinkedSubLevelId().equals(mobile.getContainingSubLevelId()), "hydraulic LinkedSubLevel UUID codec persistence");
        RESULT.addProperty("hydraulicNbtRoundTrip", true);
        require(HydraulicLengthControl.calculateReturnImpulse(3, 5, 4, .05) > 0
                && HydraulicLengthControl.calculateReturnImpulse(3, 3, 4, .05) < 0, "hydraulic restoring force direction");
        RESULT.addProperty("hydraulicControlSign", true);
        BlockPos jointA = new BlockPos(144, -60, 8), jointB = new BlockPos(148, -60, 8);
        level.setBlockAndUpdate(jointA, ModBlocks.BRASS_UNIVERSAL_JOINT.get().defaultBlockState());
        level.setBlockAndUpdate(jointB, ModBlocks.BRASS_UNIVERSAL_JOINT.get().defaultBlockState());
        UniversalJointBlockEntity a = (UniversalJointBlockEntity) level.getBlockEntity(jointA);
        UniversalJointBlockEntity b = (UniversalJointBlockEntity) level.getBlockEntity(jointB);
        require(a.createMutualLink(b, true) == UniversalJointBlockEntity.LinkResult.SUCCESS, "universal joint live link");
        CompoundTag jointSaved = new CompoundTag(); a.write(jointSaved, level.registryAccess(), false);
        UniversalJointBlockEntity clone = new UniversalJointBlockEntity(jointA, a.getBlockState());
        clone.setLevel(level); clone.read(jointSaved, level.registryAccess(), false);
        require(clone.getLinkedPos().equals(jointB), "universal joint persistent endpoint");
        RESULT.addProperty("universalJointLinkNbt", true);
        level.setBlockAndUpdate(new BlockPos(152, -60, 8), ModBlocks.DAMPING_STRESS_BEARING.get().defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(156, -60, 8), ModBlocks.HYDRAULIC_REGULATOR.get().defaultBlockState());
        RESULT.addProperty("kineticConverterAndRegulatorPlaced", true);
        BlockPos hingeFixed = new BlockPos(160, -60, 8), hingeMobile = new BlockPos(164, -60, 8);
        level.setBlockAndUpdate(hingeFixed, ModBlocks.BRASS_HYDRAULIC_HINGE_HEAD.get().defaultBlockState()
                .setValue(HydraulicConnectionHeadBlock.FACING, Direction.EAST).setValue(HydraulicHingeHeadBlock.HINGE_AXIS, Direction.Axis.Y));
        level.setBlockAndUpdate(hingeMobile, ModBlocks.HYDRAULIC_HINGE_HEAD.get().defaultBlockState()
                .setValue(HydraulicConnectionHeadBlock.FACING, Direction.WEST).setValue(HydraulicHingeHeadBlock.HINGE_AXIS, Direction.Axis.Y));
        level.setBlockAndUpdate(hingeMobile.below(), Blocks.IRON_BLOCK.defaultBlockState());
        fixedHinge = hydraulicAt(level, hingeFixed);
        ServerSubLevel hingeBody = assemble(level, hingeMobile);
        mobileHinge = hydraulicAt(level, hingeBody.getPlot().getCenterBlock());
        require(mobileHinge.createMutualLink(fixedHinge, true) == HydraulicConnectionHeadBlockEntity.LinkResult.SUCCESS, "hinge link creation");
        fixedHinge.setHingeAngleLimits(-30, 45);
        RESULT.addProperty("visibleFixedHydraulicHead", "128,-60,8");
        RESULT.addProperty("visibleMobileHydraulicWorldAnchor", "132,-60,8");
        RESULT.addProperty("visibleFixedBrassHinge", "160,-60,8");
        RESULT.addProperty("visibleMobileRegularHingeWorldAnchor", "164,-60,8");
        RESULT.addProperty("visibleUniversalJoints", "144,-60,8;148,-60,8");
        LOGGER.info("[TransmissionLinkageProbe] fixture prepared; waiting for native physics ticks");
    }

    private static ServerSubLevel assemble(ServerLevel level, BlockPos anchor) {
        List<BlockPos> positions = List.of(anchor, anchor.below());
        ServerSubLevel sub = SubLevelAssemblyHelper.assembleBlocks(level, anchor, positions, BoundingBox3i.from(positions));
        SubLevelContainer.getContainer(level).addForceLoadTicket(sub, SubLevelLoadingTicketType.COMMAND_FORCED, Unit.INSTANCE);
        return sub;
    }

    private static HydraulicConnectionHeadBlockEntity hydraulicAt(ServerLevel level, BlockPos pos) {
        var head = level.getBlockEntity(pos);
        require(head instanceof HydraulicConnectionHeadBlockEntity, "hydraulic endpoint exists at " + pos);
        return (HydraulicConnectionHeadBlockEntity) head;
    }

    private static PhysicsConstraintHandle liveConstraint(HydraulicConnectionHeadBlockEntity head) throws ReflectiveOperationException {
        return readConstraint(head, "constraintHandle");
    }

    private static PhysicsConstraintHandle readConstraint(HydraulicConnectionHeadBlockEntity head, String name) throws ReflectiveOperationException {
        var field = HydraulicConnectionHeadBlockEntity.class.getDeclaredField(name);
        field.setAccessible(true);
        return (PhysicsConstraintHandle) field.get(head);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static void finish(Path root, boolean passed, boolean reloadRequired, Throwable error) {
        done = true;
        RESULT.addProperty("passed", passed);
        RESULT.addProperty("completedTicks", ticks);
        RESULT.addProperty("completedAt", OffsetDateTime.now().toString());
        RESULT.addProperty("reloadRequired", reloadRequired);
        if (error != null) RESULT.addProperty("failure", error.toString());
        try {
            Files.writeString(root.resolve("transmission-linkage-probe.json"), new GsonBuilder().setPrettyPrinting().create().toJson(RESULT));
        } catch (Exception writeError) { LOGGER.error("[TransmissionLinkageProbe] Could not write result", writeError); }
        LOGGER.info("[TransmissionLinkageProbe] {} ticks={} reloadRequired={}", passed ? "PASS" : "FAIL", ticks, reloadRequired);
    }
}
