package com.yision.fluidlogistics.client;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelClient;
import com.yision.fluidlogistics.api.factorygauge.client.FactoryGaugeClient;
import com.yision.fluidlogistics.content.equipment.mechanicalFluidGun.particle.MechanicalFluidGunStreamParticleData;
import com.yision.fluidlogistics.content.logistics.factoryGauge.ResourceFactoryPanelBehaviour;
import com.yision.fluidlogistics.content.fluids.powderSnow.PowderSnowFluidType;
import com.yision.fluidlogistics.content.processing.snowGolemFreezer.SnowGolemFreezerSnowflakeParticle;
import com.yision.fluidlogistics.content.schematics.client.FluidSchematicEditScreen;
import com.yision.fluidlogistics.registry.AllFluidLogisticsParticleTypes;
import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.jetbrains.annotations.Nullable;

/** Client implementations reached through the original client-only call paths. */
public final class FluidLogisticsClientAccess {
    private FluidLogisticsClientAccess() {}

    public static void openBlueprintScreen() {
        ScreenOpener.open(new FluidSchematicEditScreen());
    }

    public static void displayGaugeScreen(ResourceFactoryPanelBehaviour behaviour, Player player) {
        if (!behaviour.isResourceGauge()) {
            FactoryPanelClient.openScreen(behaviour);
            return;
        }
        if (player instanceof LocalPlayer)
            FactoryGaugeClient.createScreen(behaviour).ifPresent(ScreenOpener::open);
    }

    @Nullable
    public static Entity cameraEntity() {
        return Minecraft.getInstance().getCameraEntity();
    }

    public static FluidModel.Unbaked powderSnowModel(PowderSnowFluidType type) {
        return new FluidModel.Unbaked(new Material(type.getStillTexture()), new Material(type.getFlowingTexture()),
            null, new FluidTintSource() {
                @Override
                public int color(FluidState state) { return -1; }

                @Override
                public int colorAsStack(FluidStack fluid) { return -1; }
            });
    }

    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        new MechanicalFluidGunStreamParticleData().register(AllFluidLogisticsParticleTypes.MECHANICAL_FLUID_GUN_STREAM.get(), event);
        event.registerSpriteSet(AllFluidLogisticsParticleTypes.SNOW_GOLEM_FREEZER_SNOWFLAKE.get(), SnowGolemFreezerSnowflakeParticle.Provider::new);
    }
}
