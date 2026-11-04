package com.george_vi.electroenergetics;

import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/** Minecraft 26.2 no longer creates render models from Registrate fluid sprites. */
@EventBusSubscriber(value = Dist.CLIENT, modid = CreateElectroEnergetics.ID)
public final class CEEFluidModels {
    private CEEFluidModels() {}

    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        register(event, CEEFluids.PLANT_OIL, "block/fluid/plant_oil_still", "block/fluid/plant_oil_flow");
        register(event, CEEFluids.TRANSFORMER_OIL, "block/fluid/transformer_oil_still", "block/fluid/transformer_oil_flow");
    }

    private static void register(RegisterFluidModelsEvent event,
                                 FluidEntry<? extends BaseFlowingFluid> entry,
                                 String still,
                                 String flowing) {
        BaseFlowingFluid source = entry.getSource();
        FluidModel.Unbaked model = new FluidModel.Unbaked(
                new Material(CreateElectroEnergetics.rl(still)),
                new Material(CreateElectroEnergetics.rl(flowing)),
                null,
                state -> 0xffffffff
        );
        event.register(model, source, source.getFlowing());
    }
}

