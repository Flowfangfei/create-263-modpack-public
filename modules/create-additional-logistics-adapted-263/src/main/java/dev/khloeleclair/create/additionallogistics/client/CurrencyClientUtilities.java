package dev.khloeleclair.create.additionallogistics.client;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.tableCloth.BlueprintOverlayShopContext;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem;
import dev.khloeleclair.create.additionallogistics.common.utilities.CurrencyUtilities;
import dev.khloeleclair.create.additionallogistics.mixin.client.IBlueprintOverlayRendererAccessor;
import net.createmod.catnip.api.data.Pair;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
public final class CurrencyClientUtilities {
    private CurrencyClientUtilities() {}
    public static void renderShoppingList(@Nullable ShoppingListItem.ShoppingList list) {
        if (list == null || IBlueprintOverlayRendererAccessor.CAL$getActive())
            return;

        var lists = list.bakeEntries(Minecraft.getInstance().level, null);
        var costs = CurrencyUtilities.splitCost(Minecraft.getInstance().player, lists.getSecond().getStacks());

        var currency_cost = costs.getFirst();
        var other_cost = costs.getSecond();
        var purchased = lists.getFirst();

        if (currency_cost == null || other_cost == null || purchased == null)
            return;

        IBlueprintOverlayRendererAccessor.callPrepareCustomOverlay();
        IBlueprintOverlayRendererAccessor.CAL$setNoOutput(false);
        IBlueprintOverlayRendererAccessor.CAL$setShopContext(new BlueprintOverlayShopContext(true, 1, 0));

        var player = Minecraft.getInstance().player;

        var ingredients = IBlueprintOverlayRendererAccessor.CAL$getIngredients();
        var results = IBlueprintOverlayRendererAccessor.CAL$getResults();

        for(var entry : currency_cost.entrySet()) {
            var currency = entry.getKey();
            var value = entry.getValue();
            var result = CurrencyUtilities.extractValueFromPlayer(player, currency, value, true);

            boolean can_afford = !result.inventoryFull() && result.remaining() <= 0;

            for(var item : currency.getStacksWithValue(value))
                ingredients.add(Pair.of(item, can_afford));
        }

        for(var entry : other_cost) {
            boolean can_afford = IBlueprintOverlayRendererAccessor.callCanAfford(player, entry);
            ingredients.add(Pair.of(entry.stack.copyWithCount(entry.count), can_afford));
        }

        for(BigItemStack entry : purchased.getStacksByCount())
            results.add(entry.stack.copyWithCount(entry.count));

    }
}
