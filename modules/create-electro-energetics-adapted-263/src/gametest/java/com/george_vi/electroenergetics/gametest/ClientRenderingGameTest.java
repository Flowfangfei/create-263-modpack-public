package com.george_vi.electroenergetics.gametest;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelSlot;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.*;
import com.george_vi.electroenergetics.content.electrical_panel.link.*;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterBlockEntity;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterScreen;
import com.george_vi.electroenergetics.content.energy_meter.PanelEnergyMeterScreen;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerScreen;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.google.gson.JsonObject;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import com.zurrtum.create.client.ponder.foundation.ui.PonderUI;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.function.Supplier;

/** Real GPU, integrated-server and screen coverage; screenshots are artifacts for manual visual review. */
public final class ClientRenderingGameTest implements FabricClientGameTest {
    private static final BlockPos SOURCE = new BlockPos(0, 64, 0);
    private static final BlockPos MOTOR = new BlockPos(2, 64, 0);
    private static final BlockPos METER = new BlockPos(4, 64, 0);
    private static final BlockPos WIRE_START = new BlockPos(0, 66, 11);
    private static final BlockPos WIRE_END = new BlockPos(10, 66, 11);
    private static final BlockPos CATENARY_START = new BlockPos(0, 69, 3);
    private static final BlockPos CATENARY_END = new BlockPos(10, 69, 3);
    private static final BlockPos TRANSFORMER_PANEL = new BlockPos(2, 64, 8);
    private static final BlockPos ENERGY_PANEL = new BlockPos(4, 64, 8);
    private static final BlockPos LINK_PANEL = new BlockPos(6, 64, 8);

    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(
                mc -> {
                    mc.options.guiScale().set(2);
                    mc.getWindow().setWindowed(1280, 720);
                    mc.resizeGui();
                });
        context.waitTick();
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            context.runOnClient(ClientRenderingGameTest::verifyBakedResources);
            world.getServer().runCommand("gamemode creative @a");
            world.getServer().runCommand("time set noon");
            world.getServer().runOnServer(server -> buildShowcase(server.overworld()));
            world.getServer().runCommand("tp @a 5.5 69 19.5 180 25");
            context.waitTicks(30);
            world.getConnection().waitForChunksRender();
            world.getConnection().waitForClientboundPackets();
            context.waitFor(
                    mc ->
                            WireRenderer.WIRE_CONNECTIONS.size() >= 3
                                    && !WireRenderer.CATENARY.isEmpty(),
                    400);
            context.waitFor(
                    mc ->
                            mc.level.getBlockEntity(TRANSFORMER_PANEL)
                                            instanceof ElectricalPanelBlockEntity panel
                                    && java.util.Arrays.stream(panel.getAttachments())
                                            .anyMatch(TransformerAttachment.class::isInstance));

            try {
                wireBackend(context, true);
                context.runOnClient(
                        mc ->
                                require(
                                        !WireRenderer.extract(mc.gameRenderer.mainCamera())
                                                .isEmpty(),
                                        "Fallback wire extraction produced no geometry for"
                                                + " synchronized wires and catenary"));
                screenshot(context, "01-world-fallback");
                wireBackend(context, false);
                screenshot(context, "02-world-flywheel");
                screens(context, world);
                ponder(context);
                tools(context, world);
                if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("jei"))
                    JeiChecks.recipes(context);
            } finally {
                context.runOnClient(
                        mc -> {
                            CEEConfigs.client().applyRemote(null);
                            WireRenderer.recreateVisuals();
                        });
                context.setScreen(() -> null);
            }
        }
    }

    private static void verifyBakedResources(Minecraft mc) {
        try {
            Class.forName(
                    "com.zurrtum.create.client.foundation.entity.behaviour.CarriageAudioBehaviour");
            Class.forName("com.zurrtum.create.client.content.schematics.client.SchematicRenderer");
        } catch (ClassNotFoundException missing) {
            throw new AssertionError("Create Fly carriage audio behaviour is unavailable", missing);
        }
        var models = mc.getModelManager().getBlockStateModelSet();
        int checkedBlocks = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!BuiltInRegistries.BLOCK
                    .getKey(block)
                    .getNamespace()
                    .equals(CreateElectroEnergetics.ID)) continue;
            for (BlockState state : block.getStateDefinition().getPossibleStates())
                require(
                        models.get(state) != models.missingModel(),
                        "Missing baked block model: " + state);
            checkedBlocks++;
        }
        require(checkedBlocks >= 60, "CEE block registry was not loaded");
        for (var field : CEEPartialModels.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            try {
                Object value = field.get(null);
                if (value instanceof PartialModel model)
                    verifyPartial(model, models.missingModel());
                if (value instanceof PartialModel[] partials)
                    for (PartialModel model : partials) verifyPartial(model, models.missingModel());
            } catch (IllegalAccessException exception) {
                throw new AssertionError(exception);
            }
        }
        int checkedItems = 0;
        for (var item : BuiltInRegistries.ITEM) {
            if (!BuiltInRegistries.ITEM
                    .getKey(item)
                    .getNamespace()
                    .equals(CreateElectroEnergetics.ID)) continue;
            ItemStackRenderState state = new ItemStackRenderState();
            mc.getItemModelResolver()
                    .updateForTopItem(
                            state, new ItemStack(item), ItemDisplayContext.GUI, null, null, 0);
            require(
                    !state.isEmpty(),
                    "Missing item render state: " + BuiltInRegistries.ITEM.getKey(item));
            checkedItems++;
        }
        require(checkedItems >= 100, "CEE item definitions were not loaded");
    }

    private static void verifyPartial(
            PartialModel model,
            net.minecraft.client.renderer.block.dispatch.BlockStateModel missing) {
        require(
                model != null && model.get() != null && model.get() != missing,
                "Missing partial model: " + (model == null ? "null" : model.modelLocation()));
    }

    private static void buildShowcase(ServerLevel level) {
        for (BlockPos pos : BlockPos.betweenClosed(-2, 63, -2, 13, 63, 16))
            level.setBlock(pos, Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(
                new BlockPos(5, 68, 19), Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_ALL);
        Block[] machines = {
            CEEBlocks.CREATIVE_BATTERY.get(),
            CEEBlocks.ELECTRIC_MOTORS[DyeColor.BLUE.ordinal()].get(),
            CEEBlocks.ENERGY_METER.get(),
            CEEBlocks.VOLTMETER.get(),
            CEEBlocks.AMMETER.get(),
            CEEBlocks.ELECTRIC_FAN.get(),
            CEEBlocks.HV_SWITCH.get(),
            CEEBlocks.PANTOGRAPH.get(),
            CEEBlocks.RAIL_CONTACT_SHOE.get(),
            CEEBlocks.RESISTOR.get(),
            CEEBlocks.FUSE_HOLDER.get(),
            CEEBlocks.INDICATOR_BULB.get(),
            CEEBlocks.POTENTIOMETER.get(),
            CEEBlocks.REDSTONE_POTENTIOMETER.get(),
            CEEBlocks.RESISTIVE_HEATER.get(),
            CEEBlocks.SYNCHROSCOPE.get(),
            CEEBlocks.FREQUENCY_METER.get(),
            CEEBlocks.VARIAC.get(),
            CEEBlocks.REDSTONE_VARIAC.get(),
            CEEBlocks.ALTERNATOR_ROTOR.get(),
            CEEBlocks.ALTERNATOR_BRUSHES.get(),
            CEEBlocks.BULB.get(),
            CEEBlocks.ACCUMULATOR.get(),
            CEEBlocks.TRANSFORMER_CORE.get()
        };
        for (int i = 0; i < machines.length; i++) {
            BlockPos pos = new BlockPos(i % 6 * 2, 64, i / 6 * 2);
            level.setBlock(pos, machines[i].defaultBlockState(), Block.UPDATE_ALL);
        }
        for (BlockPos pos : new BlockPos[] {WIRE_START, WIRE_END}) {
            level.setBlock(pos.below(), Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(pos, CEEBlocks.CONNECTOR.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        for (BlockPos pos : new BlockPos[] {CATENARY_START, CATENARY_END}) {
            level.setBlock(pos.above(), Blocks.IRON_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(
                    pos, CEEBlocks.CATENARY_HOLDER.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        InfrastructureSavedData data = InfrastructureSavedData.load(level);
        for (BlockPos pos :
                new BlockPos[] {SOURCE, MOTOR, WIRE_START, WIRE_END, CATENARY_START, CATENARY_END})
            ensureNodes(level, pos);
        for (int terminal = 0; terminal < 2; terminal++)
            data.connect(
                    new InWorldNode(terminal, SOURCE),
                    new InWorldNode(terminal, MOTOR),
                    CEEWireTypes.STANDARD.get());
        data.connect(
                new InWorldNode(0, WIRE_START),
                new InWorldNode(0, WIRE_END),
                WireData.ofLength(CEEWireTypes.STANDARD.get(), 12));
        data.connectCatenary(CATENARY_START, CATENARY_END);
        panel(level, TRANSFORMER_PANEL, CEEPanelAttachmentTypes.TRANSFORMER.get());
        panel(level, ENERGY_PANEL, CEEPanelAttachmentTypes.ENERGY_METER.get());
        panel(level, LINK_PANEL, CEEPanelAttachmentTypes.ANALOG_LEVER.get());
        level.setBlock(
                new BlockPos(12, 64, 0),
                CEEFluids.TRANSFORMER_OIL.getSource().defaultFluidState().createLegacyBlock(),
                Block.UPDATE_ALL);
        level.setBlock(
                new BlockPos(12, 64, 2),
                CEEFluids.PLANT_OIL.getSource().defaultFluidState().createLegacyBlock(),
                Block.UPDATE_ALL);
    }

    private static void panel(ServerLevel level, BlockPos pos, PanelAttachmentType type) {
        level.setBlock(pos, CEEBlocks.ELECTRICAL_PANEL.get().defaultBlockState(), Block.UPDATE_ALL);
        ElectricalPanelBlockEntity be = (ElectricalPanelBlockEntity) level.getBlockEntity(pos);
        ElectricalPanelSlot slot =
                type.mode.possibleSlots().keySet().stream()
                        .min(java.util.Comparator.comparingInt(ElectricalPanelSlot::ordinal))
                        .orElseThrow();
        PanelAttachment[] attachments = new PanelAttachment[ElectricalPanelSlot.values().length];
        attachments[slot.ordinal()] =
                type.createNew(
                        pos,
                        type.mode.getNodesFor(pos, slot),
                        level,
                        slot,
                        Direction.NORTH,
                        level.registryAccess());
        be.setAttachments(attachments);
        ensureNodes(level, pos);
        be.sendData();
    }

    private static void ensureNodes(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        ((ElectricalDeviceBlock<?>) state.getBlock()).ensureNodesExist(level, pos, state);
    }

    private static void wireBackend(ClientGameTestContext context, boolean fallback) {
        context.runOnClient(
                mc -> {
                    JsonObject overlay = new JsonObject();
                    overlay.addProperty("disableFlywheelWireRendering", fallback);
                    CEEConfigs.client().applyRemote(overlay);
                    WireRenderer.recreateVisuals();
                });
        context.waitTicks(40);
    }

    private static void screens(ClientGameTestContext context, TestSingleplayerContext world) {
        screen(
                context,
                () ->
                        new EnergyMeterScreen(
                                (EnergyMeterBlockEntity)
                                        Minecraft.getInstance().level.getBlockEntity(METER)),
                "03-energy-meter");
        screen(
                context,
                () -> new TransformerScreen((TransformerAttachment) attachment(TRANSFORMER_PANEL)),
                "04-transformer");
        screen(
                context,
                () ->
                        new PanelEnergyMeterScreen(
                                (BaseEnergyMeterAttachment) attachment(ENERGY_PANEL)),
                "05-panel-energy-meter");
        context.setScreen(() -> null);
        world.getServer()
                .runOnServer(
                        server -> {
                            var panel =
                                    (ElectricalPanelBlockEntity)
                                            server.overworld().getBlockEntity(LINK_PANEL);
                            var holder =
                                    (ElectricalPanelLink)
                                            java.util.Arrays.stream(panel.getAttachments())
                                                    .filter(java.util.Objects::nonNull)
                                                    .findFirst()
                                                    .orElseThrow();
                            holder.openHandledScreen(
                                    server.getPlayerList().getPlayers().getFirst());
                        });
        context.waitForScreen(ElectricalPanelLinkScreen.class);
        screenshot(context, "06-panel-link");
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("jei"))
            JeiChecks.ghost(context, world);
        context.runOnClient(mc -> mc.player.closeContainer());
        context.waitForScreen(null);
    }

    private static PanelAttachment attachment(BlockPos pos) {
        return java.util.Arrays.stream(
                        ((ElectricalPanelBlockEntity)
                                        Minecraft.getInstance().level.getBlockEntity(pos))
                                .getAttachments())
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow();
    }

    private static void ponder(ClientGameTestContext context) {
        context.runOnClient(
                mc -> {
                    PonderIndex.reload();
                    var entries =
                            PonderIndex.getSceneAccess().getRegisteredEntries().stream()
                                    .map(java.util.Map.Entry::getKey)
                                    .filter(
                                            id ->
                                                    id.getNamespace()
                                                            .equals(CreateElectroEnergetics.ID))
                                    .distinct()
                                    .toList();
                    require(entries.size() >= 20, "CEE Ponder storyboards were not registered");
                    for (var id : entries)
                        require(
                                !PonderIndex.getSceneAccess().compile(id).isEmpty(),
                                "Ponder scene did not compile: " + id);
                });
        context.setScreen(() -> PonderUI.of(CEEItems.WIRE_SPOOL.asStack()));
        context.waitForScreen(PonderUI.class);
        context.waitTicks(160);
        screenshot(context, "07-ponder-wires");
        context.setScreen(() -> null);
    }

    private static void screen(
            ClientGameTestContext context, Supplier<Screen> screen, String name) {
        context.setScreen(screen);
        context.waitTicks(5);
        screenshot(context, name);
    }

    private static void tools(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer()
                .runCommand(
                        "item replace entity @a weapon.mainhand with"
                                + " electroenergetics:linemans_stick");
        world.getServer()
                .runCommand(
                        "item replace entity @a weapon.offhand with electroenergetics:clamp_meter");
        context.waitTicks(20);
        world.getConnection().waitForClientboundPackets();
        context.getInput().lookAt(new BlockPos(5, 66, 11));
        context.waitTicks(20);
        screenshot(context, "08-linemans-stick-first-person");
        world.getServer()
                .runCommand(
                        "item replace entity @a weapon.mainhand with"
                                + " electroenergetics:clamp_meter");
        world.getServer()
                .runCommand(
                        "item replace entity @a weapon.offhand with"
                                + " electroenergetics:linemans_stick");
        world.getServer().runCommand("gamemode survival @a");
        context.waitTicks(10);
        world.getConnection().waitForClientboundPackets();
        screenshot(context, "09-clamp-meter-first-person");
        screen(
                context,
                () -> new InventoryScreen(Minecraft.getInstance().player),
                "10-tools-inventory");
        context.setScreen(() -> null);
    }

    private static void screenshot(ClientGameTestContext context, String name) {
        context.takeScreenshot(
                TestScreenshotOptions.of(name)
                        .withDestinationDir(Path.of("screenshots", "electroenergetics")));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /** This class is only resolved when JEI is present. */
    private static final class JeiChecks {
        private static void ghost(ClientGameTestContext context, TestSingleplayerContext world) {
            context.waitFor(mc -> mezz.jei.common.Internal.getOptionalJeiRuntime().isPresent());
            context.runOnClient(
                    mc -> {
                        var runtime = mezz.jei.common.Internal.getJeiRuntime();
                        var screen = (ElectricalPanelLinkScreen) mc.gui.screen();
                        var handlers = runtime.getScreenHelper().getGhostIngredientHandlers(screen);
                        require(!handlers.isEmpty(), "CEE JEI ghost handler was not registered");
                        var ingredient =
                                runtime.getIngredientManager()
                                        .createTypedIngredient(
                                                mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                                                CEEItems.WIRE_SPOOL.asStack())
                                        .orElseThrow();
                        var targets = handlers.getFirst().getTargetsTyped(screen, ingredient, true);
                        require(
                                targets.size() == 2,
                                "CEE panel did not expose both JEI frequency slots");
                        targets.getFirst().accept(CEEItems.WIRE_SPOOL.asStack());
                        require(
                                screen.getMenu()
                                        .ghostInventory
                                        .getItem(0)
                                        .is(CEEItems.WIRE_SPOOL.get()),
                                "JEI ghost ingredient was not applied on the client");
                    });
            context.waitTick();
            world.getConnection().waitForServerboundPackets();
            world.getServer()
                    .runOnServer(
                            server -> {
                                var menu =
                                        (ElectricalPanelLinkMenu)
                                                server.getPlayerList()
                                                        .getPlayers()
                                                        .getFirst()
                                                        .containerMenu;
                                require(
                                        menu.ghostInventory
                                                .getItem(0)
                                                .is(CEEItems.WIRE_SPOOL.get()),
                                        "JEI ghost ingredient was not synchronized to the actual"
                                                + " server menu");
                            });
            screenshot(context, "11-panel-jei-ghost");
        }

        private static void recipes(ClientGameTestContext context) {
            context.waitFor(mc -> mezz.jei.common.Internal.getOptionalJeiRuntime().isPresent());
            context.runOnClient(
                    mc -> {
                        var runtime = mezz.jei.common.Internal.getJeiRuntime();
                        var type =
                                com.george_vi.electroenergetics.compat.jei.FluidCompactingCategory
                                        .TYPE;
                        var manager = runtime.getRecipeManager();
                        require(
                                manager.getRecipeCategory(type) != null,
                                "CEE fluid-compacting JEI category is absent");
                        var recipes = manager.createRecipeLookup(type).get().toList();
                        require(
                                recipes.stream()
                                        .anyMatch(
                                                recipe ->
                                                        recipe.id()
                                                                .identifier()
                                                                .equals(
                                                                        CreateElectroEnergetics.rl(
                                                                                "compacting/plant_oil"))),
                                "CEE plant-oil recipe is absent from JEI's registered"
                                        + " fluid-compacting recipes");
                        runtime.getRecipesGui().showTypes(java.util.List.of(type));
                    });
            context.waitTicks(10);
            screenshot(context, "12-jei-fluid-compacting");
            context.setScreen(() -> null);
        }
    }
}
