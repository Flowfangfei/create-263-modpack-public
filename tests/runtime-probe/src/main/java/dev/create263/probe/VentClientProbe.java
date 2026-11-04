package dev.create263.probe;

import com.google.gson.GsonBuilder;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Opt-in regression, only in a marked project-owned disposable client world. */
final class VentClientProbe {
    private static final Path ROOT = Path.of(System.getProperty("create263.clientProbeRoot")).toAbsolutePath().normalize();
    private static final Map<String,Object> result = new LinkedHashMap<>();
    private static final List<BlockPos> positions = new ArrayList<>();
    private static final Map<Long,Object> meshesBeforeUpdate = new LinkedHashMap<>();
    private static volatile boolean ready, saveComplete;
    private static volatile Throwable setupFailure;
    private static int ticks, afterDisconnect;
    private static boolean scheduled, disconnecting, finished, failed, saveRequested;

    static void install() {
        require(ROOT.equals(FMLPaths.GAMEDIR.get().toAbsolutePath().normalize()), "Client game directory mismatch");
        require(Files.isRegularFile(ROOT.resolve(".create263-disposable-runtime")), "Missing owned-runtime marker");
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> tick());
    }
    private static void require(boolean ok, String message) { if (!ok) throw new IllegalStateException(message); }
    private static BlockState vent() {
        var block = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("railways", "conductor_vent"));
        require(block != null && block != Blocks.AIR, "Conductor vent not registered");
        return block.defaultBlockState();
    }
    private static void write() throws Exception {
        Files.writeString(ROOT.resolve("vent-client-regression.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n");
    }
    private static void prepare(Minecraft mc) throws Exception {
        var server = mc.getSingleplayerServer();
        require(server != null, "Expected integrated server");
        require(server.getWorldPath(LevelResource.ROOT).toRealPath().equals(ROOT.resolve("saves/three-addon-test-copy").toRealPath()), "World outside owned test path");
        var playerId = mc.player.getUUID();
        server.execute(() -> {
            try {
                var level = server.overworld();
                var base = vent();
                var visible = (BooleanProperty)base.getProperties().stream().filter(p -> p.getName().equals("conductor_visible")).findFirst().orElseThrow();
                boolean reload = Boolean.getBoolean("create263.ventReload");
                int index = 0;
                for (int variant = 0; variant < 5; variant++) {
                    for (var neighbor : List.of(Blocks.RED_SAND, Blocks.STONE, Blocks.GLASS)) {
                        for (var dir : Direction.values()) {
                            var pos = new BlockPos((index % 15) * 4, -60, (index / 15) * 4);
                            var state = base.setValue(visible, variant == 3);
                            if (reload) {
                                var saved = level.getBlockState(pos);
                                require(saved.is(base.getBlock()), "Saved vent missing at " + pos);
                                require(saved.getValue(visible) == (variant != 3), "Saved grate state changed at " + pos);
                                require(level.getBlockEntity(pos) instanceof CopycatBlockEntity, "Saved vent entity missing");
                                var savedMaterial = ((CopycatBlockEntity)level.getBlockEntity(pos)).getMaterial();
                                var expectedMaterial = variant == 1 || variant == 3 ? Blocks.STONE.defaultBlockState()
                                    : variant == 2 ? Blocks.GLASS.defaultBlockState() : AllBlocks.COPYCAT_BASE.getDefaultState();
                                require(savedMaterial.equals(expectedMaterial), "Saved vent material changed at " + pos);
                            } else {
                                level.setBlock(pos, state, 3);
                                require(level.getBlockEntity(pos) instanceof CopycatBlockEntity, "Missing vent copycat entity");
                                var be = (CopycatBlockEntity)level.getBlockEntity(pos);
                                if (variant == 1 || variant == 3) be.setMaterial(Blocks.STONE.defaultBlockState());
                                if (variant == 2) be.setMaterial(Blocks.GLASS.defaultBlockState());
                                // Native copycat placement records the provided item too; read() validates it.
                                if (be.hasCustomMaterial())
                                    be.setConsumedItem(new net.minecraft.world.item.ItemStack(be.getMaterial().getBlock()));
                                be.notifyUpdate();
                                var other = pos.relative(dir);
                                level.setBlock(other, neighbor.defaultBlockState(), 3);
                                // Water adjacent, rather than inventing a WATERLOGGED property this block lacks.
                                if (variant == 4) level.setBlock(pos.above(2), Blocks.WATER.defaultBlockState(), 3);
                            }
                            positions.add(pos); index++;
                        }
                    }
                }
                // Partial copycats and adjacent vents must not cause cross-block property reads.
                if (!reload) {
                level.setBlock(new BlockPos(62,-60,0), base, 3);
                level.setBlock(new BlockPos(63,-60,0), AllBlocks.COPYCAT_PANEL.getDefaultState(), 3);
                level.setBlock(new BlockPos(62,-60,4), base, 3);
                level.setBlock(new BlockPos(63,-60,4), AllBlocks.COPYCAT_STEP.getDefaultState(), 3);
                level.setBlock(new BlockPos(62,-60,8), base, 3);
                level.setBlock(new BlockPos(63,-60,8), base, 3);
                }
                result.put("saved_fixture_checks_before_any_reinitialization", reload ? 270 : 0);
                var player = server.getPlayerList().getPlayer(playerId);
                require(player != null, "Test player missing");
                player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.CREATIVE);
                player.getAbilities().flying = true; player.onUpdateAbilities();
                player.teleportTo(level,30,-43,42,Set.of(),180,30,true);
                result.put("fixture_vent_count", positions.size() + 4);
                result.put("fixture_block_count", positions.size() + 6);
                ready = true;
            } catch (Throwable failure) { setupFailure = failure; }
        });
    }
    private static void checkFaces(Minecraft mc) {
        int calls = 1;
        var base = vent();
        // Reproduce the uploaded report's exact red-sand -> vent call before any other hook.
        require(Block.shouldRenderFace(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Blocks.RED_SAND.defaultBlockState(), base, Direction.EAST), "Original red-sand path hidden");
        for (var pos : positions) {
            var state = mc.level.getBlockState(pos);
            require(state.is(base.getBlock()), "Vent fixture not loaded at " + pos);
            require(state.supportsExternalFaceHiding(), "External face hiding opt-in lost");
            for (var dir : Direction.values()) {
                var otherPos = pos.relative(dir);
                var other = mc.level.getBlockState(otherPos);
                require(!state.hidesNeighborFace(mc.level, pos, other, dir), "Vent must not forcibly hide a neighbor");
                require(Block.shouldRenderFace(mc.level, otherPos, other, state, dir.getOpposite()), "Neighbor face wrongly hidden");
                // Includes the reverse path and native solid-block culling.
                Block.shouldRenderFace(mc.level, pos, state, other, dir);
                calls += 3;
            }
        }
        for (var dir : Direction.values()) {
            require(!base.hidesNeighborFace(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Blocks.RED_SAND.defaultBlockState(), dir), "Missing-BE fallback wrong");
            require(Block.shouldRenderFace(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Blocks.RED_SAND.defaultBlockState(), base, dir), "Original red-sand path did not render");
            require(!Block.shouldRenderFace(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, base, Blocks.STONE.defaultBlockState(), dir), "Vanilla solid-face culling lost");
            for (var other : List.of(AllBlocks.COPYCAT_PANEL.getDefaultState(), AllBlocks.COPYCAT_STEP.getDefaultState(), base))
                Block.shouldRenderFace(mc.level, BlockPos.ZERO, other, base, dir);
            calls += 6;
        }
        result.put("face_checks", calls);
        result.put("six_directions_and_missing_block_entity", true);
        result.put("vanilla_solid_face_culling_preserved", true);
    }
    private static void requestFinish(Minecraft mc) throws Exception {
        if (!saveRequested) {
            saveRequested = true;
            mc.getSingleplayerServer().execute(() -> {
                mc.getSingleplayerServer().saveEverything(false, true, true);
                saveComplete = true;
            });
        }
        if (saveComplete) {
            result.put("fixture_save_flush_completed", true); write();
            disconnecting = true; mc.disconnectWithSavingScreen(); mc.gui.setScreen(new TitleScreen());
        }
    }
    private static void tick() {
        if (finished) return;
        var mc = Minecraft.getInstance();
        try {
            // Restore only this disposable game's own window before framebuffer work.
            // The 26.3 surface API cannot render its saving screen while iconified.
            if (mc.getWindow().isIconified()) {
                org.lwjgl.sdl.SDLVideo.SDL_RestoreWindow(mc.getWindow().handle());
                return;
            }
            if (disconnecting) {
                if (!(mc.gui.screen() instanceof TitleScreen) || mc.level != null || mc.getSingleplayerServer() != null || mc.gui.overlay() != null) { afterDisconnect=0; return; }
                if (++afterDisconnect == 30) { result.put("saved_and_returned_to_title", true); result.put("passed", !failed); write(); }
                if (afterDisconnect >= 50) { finished = true; mc.stop(); }
                return;
            }
            if (mc.level == null || mc.player == null || mc.gui.overlay() != null) return;
            if (failed) { requestFinish(mc); return; }
            if (!scheduled) { scheduled = true; prepare(mc); }
            if (setupFailure != null) throw new IllegalStateException("Fixture setup failed", setupFailure);
            if (!ready) return;
            if (++ticks == 100) {
                checkFaces(mc);
                var fixtureSections = positions.stream().map(pos -> SectionPos.of(pos).asLong()).collect(java.util.stream.Collectors.toSet());
                for (var section : mc.levelRenderer.visibleSections())
                    if (fixtureSections.contains(section.getSectionNode())) meshesBeforeUpdate.put(section.getSectionNode(), section.getSectionMesh());
                mc.getSingleplayerServer().execute(() -> {
                    var level = mc.getSingleplayerServer().overworld();
                    for (var pos : positions) {
                        var state = level.getBlockState(pos);
                        var visible = (BooleanProperty)state.getProperties().stream().filter(p -> p.getName().equals("conductor_visible")).findFirst().orElseThrow();
                        level.setBlock(pos, state.cycle(visible), 3);
                    }
                });
            }
            if (ticks == 200) {
                checkFaces(mc);
                long compiled = positions.stream().filter(pos -> mc.levelRenderer.isSectionCompiledAndVisible(pos, 0)).count();
                require(compiled >= 45, "Fewer than half the fixture positions actually compiled/visible: " + compiled);
                int rebuilt = 0;
                for (var section : mc.levelRenderer.visibleSections()) {
                    var oldMesh = meshesBeforeUpdate.get(section.getSectionNode());
                    if (oldMesh != null && oldMesh != section.getSectionMesh()) rebuilt++;
                }
                require(rebuilt > 0, "No observed fixture section mesh was replaced after the state update");
                result.put("matrix_positions_in_compiled_sections", compiled);
                result.put("watched_fixture_sections_before_update", meshesBeforeUpdate.size());
                result.put("rebuilt_fixture_sections", rebuilt);
                result.put("two_pass_face_checks", 2 * ((Number)result.get("face_checks")).intValue());
                Screenshot.grab(mc, false); result.put("chunk_mesh_rebuild_and_render_ticks", 200);
                result.put("render_checks_passed", true); write();
            }
            if (ticks >= 240) requestFinish(mc);
        } catch (Throwable failure) {
            failure.printStackTrace(); result.put("passed", false); result.put("error", failure.toString());
            try { write(); } catch (Exception secondary) { secondary.printStackTrace(); }
            failed=true;
        }
    }
}
