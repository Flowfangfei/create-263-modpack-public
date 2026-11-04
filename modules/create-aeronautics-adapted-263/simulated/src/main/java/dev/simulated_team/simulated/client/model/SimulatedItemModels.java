package dev.simulated_team.simulated.client.model;

import com.mojang.serialization.MapCodec;
import dev.simulated_team.simulated.content.items.plunger_launcher.PlungerLauncherItemModel;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffItemModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.resources.Identifier;

public final class SimulatedItemModels {
    private SimulatedItemModels() {
    }

    public static void register(net.neoforged.neoforge.client.event.RegisterItemModelsEvent event) {
        event.register(PhysicsStaffItemModel.ID, PhysicsStaffItemModel.Unbaked.CODEC);
        event.register(PlungerLauncherItemModel.ID, PlungerLauncherItemModel.Unbaked.CODEC);
    }

}
