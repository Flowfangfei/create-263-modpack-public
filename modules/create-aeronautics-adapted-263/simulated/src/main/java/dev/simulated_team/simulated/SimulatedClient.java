package dev.simulated_team.simulated;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.kinetics.transmission.SplitShaftVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.simibubi.create.foundation.item.ItemDescription;
import dev.simulated_team.simulated.client.BlockPropertiesTooltip;
import dev.simulated_team.simulated.compat.create.LegacyItemTooltips;
import dev.simulated_team.simulated.compat.create.SableCreateBlockEntityRenderer;
import dev.simulated_team.simulated.compat.create.SableCreateKineticRenderer;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer;
import dev.simulated_team.simulated.content.blocks.analog_transmission.AnalogTransmissionVisual;
import dev.simulated_team.simulated.content.blocks.directional_gearshift.DirectionalGearshiftRenderer;
import dev.simulated_team.simulated.content.blocks.gimbal_sensor.GimbalSensorRenderer;
import dev.simulated_team.simulated.content.blocks.gimbal_sensor.GimbalSensorVisual;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableVisual;
import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerRenderer;
import dev.simulated_team.simulated.content.blocks.spring.SpringRenderer;
import dev.simulated_team.simulated.content.blocks.redstone.modulating_receiver.ModulatingLinkVisual;
import dev.simulated_team.simulated.content.blocks.redstone.redstone_inductor.RedstoneInductorRenderer;
import dev.simulated_team.simulated.content.blocks.redstone.redstone_inductor.RedstoneInductorVisual;
import dev.simulated_team.simulated.content.blocks.steering_wheel.SteeringWheelRenderer;
import dev.simulated_team.simulated.content.blocks.steering_wheel.SteeringWheelVisual;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingVisual;
import dev.simulated_team.simulated.content.blocks.throttle_lever.ThrottleLeverVisual;
import dev.simulated_team.simulated.content.blocks.torsion_spring.TorsionSpringVisual;
import dev.simulated_team.simulated.content.items.merging_glue.MergingGlueItemHandler;
import dev.simulated_team.simulated.content.items.plunger_launcher.PlungerLauncherItemRenderer;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffClientHandler;
import dev.simulated_team.simulated.client.model.SimulatedItemModels;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.index.SimRenderTypes;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.index.SimResourceManagers;
import dev.simulated_team.simulated.index.ponder.SimPonderPlugin;
import net.createmod.catnip.api.client.render.SuperByteBufferCache;
import net.createmod.ponder.api.client.PonderIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SimulatedClient {

    public static final PhysicsStaffClientHandler PHYSICS_STAFF_CLIENT_HANDLER = new PhysicsStaffClientHandler();
    public static PlungerLauncherItemRenderer.RenderHandler PLUNGER_LAUNCHER_RENDER_HANDLER = new PlungerLauncherItemRenderer.RenderHandler();
	public static final MergingGlueItemHandler MERGING_GLUE_ITEM_HANDLER = new MergingGlueItemHandler();

    public static void init() {
        // Register every custom RenderPipeline before Minecraft's first shader reload.
        SimRenderTypes.init();
        dev.simulated_team.simulated.compat.render.CachedBuffers.init();
        registerVisuals();
        registerSableImmediateRenderers();

        ItemDescription.useKey(SimBlocks.IRON_HANDLE.get(), "block.simulated.handle");
        ItemDescription.useKey(SimBlocks.COPPER_HANDLE.get(), "block.simulated.handle");
        SimBlocks.DYED_HANDLES.forEach(block -> ItemDescription.useKey(block, "block.simulated.handle"));
        SimBlocks.NAMEPLATES.forEach(block -> ItemDescription.useKey(block, "block.simulated.nameplate"));
        SimBlocks.PORTABLE_ENGINES.forEach(block ->
                ItemDescription.useKey(block, "block.simulated.red_portable_engine"));

        LegacyItemTooltips.register(Simulated.getRegistrate());

        SimPartialModels.init();
        BlockPropertiesTooltip.init();
        SimResourceManagers.init();

        PonderIndex.addPlugin(new SimPonderPlugin());

        SuperByteBufferCache.getInstance().registerCompartment(SteeringWheelRenderer.STEERING_WHEEL);
    }

    /**
     * The 1.21.10 port builds Simulated against Sable's published API artifact,
     * while the Sodium fallback hook is supplied by the sibling Sable project.
     * Register reflectively so the two jars remain independently buildable and
     * older Sable versions simply retain their previous rendering behavior.
     */
    private static void registerSableImmediateRenderers() {
        try {
            final Class<?> registry = Class.forName("dev.ryanhcode.sable.api.client.SubLevelBlockEntityRenderRegistry");
            final Class<?> rendererType = Class.forName("dev.ryanhcode.sable.api.client.SubLevelBlockEntityRenderRegistry$Renderer");
            final Method register = registry.getMethod("register", BlockEntityType.class, rendererType);

            registerSableImmediateRenderer(
                    register,
                    rendererType,
                    AllBlockEntityTypes.BRACKETED_KINETIC.get(),
                    SableCreateKineticRenderer::renderBracketedKinetic
            );
            registerSableImmediateRenderer(
                    register,
                    rendererType,
                    AllBlockEntityTypes.BACKTANK.get(),
                    SableCreateKineticRenderer::renderBacktank
            );
            registerCreateImmediateRenderers(
                    register,
                    rendererType,
                    AllBlockEntityTypes.GEARBOX.get(),
                    AllBlockEntityTypes.CLUTCH.get(),
                    AllBlockEntityTypes.GEARSHIFT.get(),
                    AllBlockEntityTypes.ENCASED_SHAFT.get(),
                    AllBlockEntityTypes.ADJUSTABLE_CHAIN_GEARSHIFT.get(),
                    AllBlockEntityTypes.CHAIN_CONVEYOR.get(),
                    AllBlockEntityTypes.BELT.get(),
                    AllBlockEntityTypes.MOTOR.get(),
                    AllBlockEntityTypes.WATER_WHEEL.get(),
                    AllBlockEntityTypes.LARGE_WATER_WHEEL.get(),
                    AllBlockEntityTypes.ENCASED_FAN.get(),
                    AllBlockEntityTypes.TURNTABLE.get(),
                    AllBlockEntityTypes.HAND_CRANK.get(),
                    AllBlockEntityTypes.CUCKOO_CLOCK.get(),
                    AllBlockEntityTypes.MILLSTONE.get(),
                    AllBlockEntityTypes.CRUSHING_WHEEL.get(),
                    AllBlockEntityTypes.MECHANICAL_PRESS.get(),
                    AllBlockEntityTypes.MECHANICAL_MIXER.get(),
                    AllBlockEntityTypes.HEATER.get(),
                    AllBlockEntityTypes.DEPOT.get(),
                    AllBlockEntityTypes.WEIGHTED_EJECTOR.get(),
                    AllBlockEntityTypes.SPEEDOMETER.get(),
                    AllBlockEntityTypes.STRESSOMETER.get(),
                    AllBlockEntityTypes.FLUID_PIPE.get(),
                    AllBlockEntityTypes.ENCASED_FLUID_PIPE.get(),
                    AllBlockEntityTypes.MECHANICAL_PUMP.get(),
                    AllBlockEntityTypes.SMART_FLUID_PIPE.get(),
                    AllBlockEntityTypes.FLUID_VALVE.get(),
                    AllBlockEntityTypes.VALVE_HANDLE.get(),
                    AllBlockEntityTypes.HOSE_PULLEY.get(),
                    AllBlockEntityTypes.ITEM_DRAIN.get(),
                    AllBlockEntityTypes.SPOUT.get(),
                    AllBlockEntityTypes.PORTABLE_FLUID_INTERFACE.get(),
                    AllBlockEntityTypes.STEAM_ENGINE.get(),
                    AllBlockEntityTypes.MECHANICAL_PISTON.get(),
                    AllBlockEntityTypes.GANTRY_PINION.get(),
                    AllBlockEntityTypes.GANTRY_SHAFT.get(),
                    AllBlockEntityTypes.WINDMILL_BEARING.get(),
                    AllBlockEntityTypes.MECHANICAL_BEARING.get(),
                    AllBlockEntityTypes.CLOCKWORK_BEARING.get(),
                    AllBlockEntityTypes.ROPE_PULLEY.get(),
                    AllBlockEntityTypes.ELEVATOR_PULLEY.get(),
                    AllBlockEntityTypes.STICKER.get(),
                    AllBlockEntityTypes.DRILL.get(),
                    AllBlockEntityTypes.SAW.get(),
                    AllBlockEntityTypes.DEPLOYER.get(),
                    AllBlockEntityTypes.PORTABLE_STORAGE_INTERFACE.get(),
                    AllBlockEntityTypes.HARVESTER.get(),
                    AllBlockEntityTypes.MECHANICAL_ROLLER.get(),
                    AllBlockEntityTypes.MECHANICAL_CRAFTER.get(),
                    AllBlockEntityTypes.SEQUENCED_GEARSHIFT.get(),
                    AllBlockEntityTypes.FLYWHEEL.get(),
                    AllBlockEntityTypes.MECHANICAL_ARM.get(),
                    AllBlockEntityTypes.TRACK_STATION.get(),
                    AllBlockEntityTypes.TRACK_SIGNAL.get(),
                    AllBlockEntityTypes.TRACK_OBSERVER.get(),
                    AllBlockEntityTypes.CONTRAPTION_CONTROLS.get(),
                    AllBlockEntityTypes.PACKAGER.get(),
                    AllBlockEntityTypes.REPACKAGER.get(),
                    AllBlockEntityTypes.PACKAGE_FROGPORT.get(),
                    AllBlockEntityTypes.PACKAGE_POSTBOX.get(),
                    AllBlockEntityTypes.PACKAGER_LINK.get(),
                    AllBlockEntityTypes.DISPLAY_LINK.get(),
                    AllBlockEntityTypes.FLAP_DISPLAY.get(),
                    AllBlockEntityTypes.NIXIE_TUBE.get(),
                    AllBlockEntityTypes.ANALOG_LEVER.get(),
                    AllBlockEntityTypes.REDSTONE_LINK.get(),
                    AllBlockEntityTypes.PECULIAR_BELL.get(),
                    AllBlockEntityTypes.HAUNTED_BELL.get(),
                    AllBlockEntityTypes.TOOLBOX.get(),
                    AllBlockEntityTypes.TRACK.get()
            );
            // Brewin' And Chewin' Fly renders both the coaster and its contents
            // through its block-entity renderer, so neither is part of Sable's
            // normal sublevel block-model pass.
            registerOptionalRenderStateRenderer(
                    register,
                    rendererType,
                    Identifier.fromNamespaceAndPath("brewinandchewin", "coaster")
            );
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.PHYSICS_ASSEMBLER.get(),
                    (be, partialTick, poseStack, collector, light, overlay) ->
                            PhysicsAssemblerRenderer.renderInSubLevel(
                                    (dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerBlockEntity) be,
                                    partialTick, poseStack, collector, light, overlay));
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.SPRING.get(),
                    (be, partialTick, poseStack, collector, light, overlay) ->
                            SpringRenderer.renderInSubLevel(
                                    (dev.simulated_team.simulated.content.blocks.spring.SpringBlockEntity) be,
                                    partialTick, poseStack, collector, light, overlay));
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.GIMBAL_SENSOR.get(),
                    (be, partialTick, poseStack, collector, light, overlay) ->
                            GimbalSensorRenderer.renderInSubLevel(
                                    (dev.simulated_team.simulated.content.blocks.gimbal_sensor.GimbalSensorBlockEntity) be,
                                    partialTick, poseStack, collector, light, overlay));

            // Explicit compatibility list: do not opt unrelated block entities
            // into the immediate renderer automatically.
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.NAVIGATION_TABLE.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.LASER_POINTER.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.OPTICAL_SENSOR.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.ALTITUDE_SENSOR.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.VELOCITY_SENSOR.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.NAMEPLATE.get());
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.PORTABLE_ENGINE.get(),
                    (be, partialTick, poseStack, collector, light, overlay) -> {
                        if (be.getBlockState().is(SimBlocks.RED_PORTABLE_ENGINE.get())) {
                            renderRegisteredLegacyRenderer(be, partialTick, poseStack, collector, light, overlay);
                        }
                    });
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.AUGER_SHAFT.get(),
                    (be, partialTick, poseStack, collector, light, overlay) -> {
                        if (be.getBlockState().is(SimBlocks.AUGER_COG.get())) {
                            renderRegisteredLegacyRenderer(be, partialTick, poseStack, collector, light, overlay);
                        }
                    });
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.TORSION_SPRING.get());
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.DIRECTIONAL_GEARSHIFT.get(),
                    (be, partialTick, poseStack, collector, light, overlay) ->
                            DirectionalGearshiftRenderer.renderInSubLevel(
                                    (dev.simulated_team.simulated.content.blocks.directional_gearshift.DirectionalGearshiftBlockEntity) be,
                                    partialTick, poseStack, collector, light, overlay));
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.ROPE_WINCH.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.ROPE_CONNECTOR.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.SWIVEL_BEARING.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.DOCKING_CONNECTOR.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.MERGING_GLUE.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.SIMPLE_BE.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.STEERING_WHEEL.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.THROTTLE_LEVER.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.LINKED_TYPEWRITER.get());
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.MODULATING_LINKED_RECEIVER.get(),
                    SableCreateBlockEntityRenderer::render);
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.REDSTONE_ACCUMULATOR.get());
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.REDSTONE_INDUCTOR.get(),
                    (be, partialTick, poseStack, collector, light, overlay) ->
                            RedstoneInductorRenderer.renderInSubLevel(
                                    (dev.simulated_team.simulated.content.blocks.redstone.redstone_inductor.RedstoneInductorBlockEntity) be,
                                    partialTick, poseStack, collector, light, overlay));
        } catch (final ReflectiveOperationException e) {
            Simulated.LOGGER.debug("Sable immediate block-entity renderer hook is unavailable", e);
        }
    }

    private static void registerOptionalRenderStateRenderer(
            final Method register,
            final Class<?> rendererType,
            final Identifier blockEntityTypeId
    ) throws ReflectiveOperationException {
        final BlockEntityType<?> blockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE
                .getOptional(blockEntityTypeId)
                .orElse(null);
        if (blockEntityType != null) {
            registerSableImmediateRenderer(
                    register,
                    rendererType,
                    blockEntityType,
                    SableCreateBlockEntityRenderer::render
            );
        }
    }

    private static void registerLegacySubLevelRenderer(final Method register, final Class<?> rendererType,
                                                       final BlockEntityType<?> blockEntityType)
            throws ReflectiveOperationException {
        registerSableImmediateRenderer(register, rendererType, blockEntityType,
                SimulatedClient::renderRegisteredLegacyRenderer);
    }

    private static void registerCreateImmediateRenderers(
            final Method register,
            final Class<?> rendererType,
            final BlockEntityType<?>... blockEntityTypes
    ) throws ReflectiveOperationException {
        for (final BlockEntityType<?> blockEntityType : blockEntityTypes) {
            registerSableImmediateRenderer(
                    register,
                    rendererType,
                    blockEntityType,
                    SableCreateBlockEntityRenderer::render
            );
        }
    }

    private static void registerSableImmediateRenderer(final Method register, final Class<?> rendererType,
                                                       final BlockEntityType<?> blockEntityType,
                                                       final SableImmediateRenderer renderer)
            throws ReflectiveOperationException {
        final Object proxy = Proxy.newProxyInstance(
                SimulatedClient.class.getClassLoader(),
                new Class<?>[]{rendererType},
                (ignoredProxy, method, args) -> {
                    if ("render".equals(method.getName()) && args != null && args.length == 6) {
                        renderer.render(
                                (BlockEntity) args[0],
                                (float) args[1],
                                (PoseStack) args[2],
                                (SubmitNodeCollector) args[3],
                                (int) args[4],
                                (int) args[5]
                        );
                    }
                    return null;
                }
        );
        register.invoke(null, blockEntityType, proxy);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void renderRegisteredLegacyRenderer(final BlockEntity blockEntity, final float partialTick,
                                                       final PoseStack poseStack, final SubmitNodeCollector collector,
                                                       final int light, final int overlay) {
        final Object registeredRenderer = Minecraft.getInstance()
                .getBlockEntityRenderDispatcher()
                .getRenderer(blockEntity);
        if (registeredRenderer instanceof final SmartBlockEntityRenderer<?> renderer) {
            ((SmartBlockEntityRenderer) renderer).renderExplicitlyInSubLevel(
                    blockEntity, partialTick, poseStack, collector, light, overlay);
        }
    }

    @FunctionalInterface
    private interface SableImmediateRenderer {
        void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack,
                    SubmitNodeCollector collector, int light, int overlay);
    }

    // Native Create installs values, filtering, goggles and kinetic audio through
    // the common block entity; Fabric's separate client behaviour registry is not used.

    private static void registerVisuals() {
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.SIMPLE_BE.get()).factory(AnalogTransmissionVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.TORSION_SPRING.get()).factory(TorsionSpringVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.GIMBAL_SENSOR.get()).factory(GimbalSensorVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.SWIVEL_BEARING.get()).factory(SwivelBearingVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.STEERING_WHEEL.get()).factory(SteeringWheelVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.THROTTLE_LEVER.get()).factory(ThrottleLeverVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.NAVIGATION_TABLE.get()).factory(NavTableVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.MODULATING_LINKED_RECEIVER.get()).factory(ModulatingLinkVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.REDSTONE_INDUCTOR.get()).factory(RedstoneInductorVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.DIRECTIONAL_GEARSHIFT.get()).factory(SplitShaftVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
    }
}
