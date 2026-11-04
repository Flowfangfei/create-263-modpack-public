package plus.dragons.createdragonsplus.common.recipe;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class CDPRecipeSynchronization {
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("26.3-1").playToClient(RecipeSnapshotPayload.TYPE, RecipeSnapshotPayload.CODEC,
            (payload, context) -> context.enqueueWork(() -> CDPRecipeAccess.applyClientSnapshot(payload.reset(), payload.recipes())));
    }
    @SubscribeEvent public static void synchronize(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) send(event.getPlayer());
        else event.getPlayerList().getPlayers().forEach(CDPRecipeSynchronization::send);
    }
    private static void send(ServerPlayer player) {
        var recipes = new ArrayList<RecipeHolder<?>>(player.level().recipeAccess().getRecipes());
        if (recipes.isEmpty()) { PacketDistributor.sendToPlayer(player, new RecipeSnapshotPayload(true, List.of())); return; }
        for (int i = 0; i < recipes.size(); i += 32)
            PacketDistributor.sendToPlayer(player, new RecipeSnapshotPayload(i == 0, List.copyOf(recipes.subList(i, Math.min(i + 32, recipes.size())))));
    }
}
