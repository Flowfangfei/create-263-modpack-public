/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways;

import com.mojang.brigadier.CommandDispatcher;
import com.railwayteam.railways.base.reload.ClientResourceReloadCallback;
import com.railwayteam.railways.compat.tracks.GenericTrackCompat;
import com.railwayteam.railways.content.buffer.BufferModelUtils;
import com.railwayteam.railways.content.conductor.ConductorCapModel;
import com.railwayteam.railways.content.conductor.ConductorEntityModel;
import com.railwayteam.railways.content.conductor.ConductorRenderer;
import com.railwayteam.railways.content.conductor.vent.CopycatVentModel;
import com.railwayteam.railways.content.conductor.whistle.ConductorWhistleFlagRenderer;
import com.railwayteam.railways.content.coupling.coupler.TrackCouplerRenderer;
import com.railwayteam.railways.content.animated_flywheel.FlywheelMovementRender;
import com.railwayteam.railways.content.palettes.ct.PaletteConnectedTextures;
import com.railwayteam.railways.content.semaphore.SemaphoreRenderer;
import com.railwayteam.railways.content.switches.TrackSwitchRenderer;
import com.railwayteam.railways.internal.compat.client.model.AllModels;
import com.railwayteam.railways.internal.compat.client.render.AllBlockEntityRenders;
import com.simibubi.create.content.trains.bogey.BogeyBlockEntityRenderer;
import com.simibubi.create.content.trains.bogey.BogeyBlockEntityVisual;
import com.railwayteam.railways.content.custom_tracks.casing.CasingRenderUtils;
import com.railwayteam.railways.ponder.CRPonderPlugin;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CRBlockEntities;
import com.railwayteam.railways.registry.CRBogeyStyleRenders;
import net.createmod.ponder.api.client.PonderIndex;
import com.railwayteam.railways.registry.CRCommandsClient;
import com.railwayteam.railways.registry.CRDevCaps;
import com.railwayteam.railways.registry.CRDisplaySources;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.railwayteam.railways.registry.CREntities;
import com.railwayteam.railways.registry.CRFluids;
import com.railwayteam.railways.registry.CRKeys;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.registry.CRScheduleClient;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.registry.CRContainerTypes;
import com.railwayteam.railways.util.CustomTrackOverlayRendering;
import com.railwayteam.railways.util.DevCapeUtils;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.simibubi.create.content.trains.track.TrackMaterial;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RailwaysClient {
  private static final List<WeakReference<ClientResourceReloadCallback>> RELOAD_CALLBACKS = new ArrayList<>();

  public static void init() {
    registerModelLayer(ConductorEntityModel.LAYER_LOCATION, ConductorEntityModel::createBodyLayer);
    registerModelLayer(ConductorCapModel.LAYER_LOCATION, ConductorCapModel::createBodyLayer);
    registerEntityRenderers();

    registerBuiltinPack("legacy_semaphore", "Steam 'n' Rails Legacy Semaphores");
    registerBuiltinPack("green_signals", "Steam 'n' Rails Green Signals");
    registerBuiltinPack("legacy_palettes", "Steam 'n' Rails Legacy Palettes Textures");

    registerClientCommands(CRCommandsClient::register);

    CRPackets.PACKETS.registerS2CListener();

    PonderIndex.addPlugin(new CRPonderPlugin());

    CRKeys.register();
    CRBlockPartials.init();
    FlywheelMovementRender.register();
    CRBogeyStyleRenders.register();
    CRScheduleClient.register();
    registerDisplaySourceRenders();
    registerBogeyBlockEntityRenders();

    AllModels.register(com.railwayteam.railways.registry.CRBlocks.CONDUCTOR_VENT.get(), CopycatVentModel::new);
    PaletteConnectedTextures.register();

    CustomTrackOverlayRendering.register(CREdgePointTypes.COUPLER, CRBlockPartials.COUPLER_BOTH);
    CustomTrackOverlayRendering.register(CREdgePointTypes.SWITCH, CRBlockPartials.SWITCH_RIGHT_TURN);

    CRDevCaps.register();
    BufferModelUtils.register();

    CRFluids.initRendering();

    DevCapeUtils.INSTANCE.init();

    registerTrackModels();
  }

  private static void registerDisplaySourceRenders() {
    // SingleLineDisplaySource in Create Adapted implements its GUI directly.
    if (!(CRDisplaySources.TRACK_SWITCH.get() instanceof com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource)
      || !(CRDisplaySources.SIGNAL.get() instanceof com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource))
      throw new IllegalStateException("Railways single-line display sources have incompatible base types");
  }

  private static void registerBogeyBlockEntityRenders() {
    AllBlockEntityRenders.visual(CRBlockEntities.BOGEY.get(),
      BogeyBlockEntityRenderer::new, BogeyBlockEntityVisual::new);
    AllBlockEntityRenders.visual(CRBlockEntities.MONO_BOGEY.get(),
      BogeyBlockEntityRenderer::new, BogeyBlockEntityVisual::new);
    AllBlockEntityRenders.visual(CRBlockEntities.INVISIBLE_BOGEY.get(),
      BogeyBlockEntityRenderer::new, BogeyBlockEntityVisual::new);
    AllBlockEntityRenders.visual(CRBlockEntities.INVISIBLE_MONO_BOGEY.get(),
      BogeyBlockEntityRenderer::new, BogeyBlockEntityVisual::new);
    AllBlockEntityRenders.render(CRBlockEntities.SEMAPHORE.get(), SemaphoreRenderer::new);
    AllBlockEntityRenders.render(CRBlockEntities.CONDUCTOR_WHISTLE_FLAG.get(), ConductorWhistleFlagRenderer::new);
    AllBlockEntityRenders.render(CRBlockEntities.TRACK_COUPLER.get(), TrackCouplerRenderer::new);
    AllBlockEntityRenders.render(CRBlockEntities.ANDESITE_SWITCH.get(), TrackSwitchRenderer::new);
    AllBlockEntityRenders.render(CRBlockEntities.BRASS_SWITCH.get(), TrackSwitchRenderer::new);
  }

  /**
   * Create only assigns a render {@link TrackMaterial.TrackModelHolder} to its own track
   * materials, so addon tracks have a null model holder and render no curves or placement preview.
   * Register a holder for every Railways track material from its partial models. Standard, wide,
   * narrow, phantom, ender, and tieless tracks use {@code block/track/<material>/...}. Integration
   * tracks use {@code block/track/compat/<mod>/<material>/...} in the Railways namespace. Monorail
   * uses its custom middle/top/bottom partials, with {@code MixinSegmentAngles} adjusting the curve
   * transforms so Create Fly renders them as one monorail beam instead of two standard rails.
   */
  private static void registerTrackModels() {
    // The real TrackMaterialFactory supplies and registers the partials during content initialization.
    for (TrackMaterial material : TrackMaterial.ALL.values()) {
      if ((Railways.MOD_ID.equals(material.id.getNamespace()) || GenericTrackCompat.get(material) != null)
        && material.getModelHolder() == null)
        throw new IllegalStateException("Missing Railway track models for " + material.id);
    }
  }

  private static void registerEntityRenderers() {
    registerEntityRenderer(CREntities.CART_BLOCK.get(), minecartRenderer());
    registerEntityRenderer(CREntities.CART_JUKEBOX.get(), minecartRenderer());
    registerEntityRenderer(CREntities.CONDUCTOR.get(), ConductorRenderer::new);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static <T extends AbstractMinecart> EntityRendererProvider<T> minecartRenderer() {
    return ctx -> (EntityRenderer) new MinecartRenderer(ctx, ModelLayers.MINECART);
  }

  public static void registerReloadCallback(ClientResourceReloadCallback callback) {
    synchronized (RELOAD_CALLBACKS) {
      RELOAD_CALLBACKS.add(new WeakReference<>(callback));
    }
  }

  public static void invalidateRenderers() {
    CasingRenderUtils.clearModelCache();

    synchronized (RELOAD_CALLBACKS) {
      var iterator = RELOAD_CALLBACKS.iterator();
      while (iterator.hasNext()) {
        ClientResourceReloadCallback cb = iterator.next().get();
        if (cb == null) {
          iterator.remove();
        } else {
          cb.onResourceManagerReload();
        }
      }
    }
  }

  public static void registerClientCommands(Consumer<CommandDispatcher<SharedSuggestionProvider>> consumer) {
    com.railwayteam.railways.fabric.RailwaysClientImpl.registerClientCommands(consumer);
  }

  public static void registerModelLayer(ModelLayerLocation layer, Supplier<LayerDefinition> definition) {
    com.railwayteam.railways.fabric.RailwaysClientImpl.registerModelLayer(layer, definition);
  }

  public static <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
    com.railwayteam.railways.fabric.RailwaysClientImpl.registerEntityRenderer(type, provider);
  }

  public static void registerBuiltinPack(String id, String name) {
    com.railwayteam.railways.fabric.RailwaysClientImpl.registerBuiltinPack(id, name);
  }
}
