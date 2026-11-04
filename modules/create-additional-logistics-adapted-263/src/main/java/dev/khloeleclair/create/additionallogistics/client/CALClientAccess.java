package dev.khloeleclair.create.additionallogistics.client;
import dev.khloeleclair.create.additionallogistics.client.content.kinetics.lazy.FlexibleShaftScreen;
import dev.khloeleclair.create.additionallogistics.client.content.logistics.cashRegister.SalesLedgerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
/** Common callers expose only common Minecraft types across this boundary. */
public final class CALClientAccess {
    private CALClientAccess() {}
    @Nullable
    public static Level level() { return Minecraft.getInstance().level; }
    public static net.minecraft.world.entity.player.Player player() { return Minecraft.getInstance().player; }
    public static net.minecraft.world.item.TooltipFlag tooltipFlag() {
        return net.neoforged.neoforge.client.ClientTooltipFlag.of(Minecraft.getInstance().options.advancedItemTooltips
            ? net.minecraft.world.item.TooltipFlag.ADVANCED : net.minecraft.world.item.TooltipFlag.NORMAL);
    }
    public static void openFlexibleShaft(BlockPos pos) {
        Minecraft.getInstance().setScreenAndShow(new FlexibleShaftScreen(pos));
    }
    public static void openSalesLedger(ItemStack stack, Map<UUID, String> names) {
        Minecraft.getInstance().setScreenAndShow(new SalesLedgerScreen(stack, names));
    }
}
