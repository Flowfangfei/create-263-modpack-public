package dev.simulated_team.simulated.mixin.new_ponder;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.ponder.new_ponder_tooltip.NewPonderTooltipManager;
import dev.simulated_team.simulated.service.SimConfigService;
import net.createmod.ponder.impl.client.tooltip.PonderTooltipHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(PonderTooltipHandler.class)
public class PonderTooltipHandlerMixin {
 @ModifyReturnValue(method="determineTooltip",at=@At("RETURN"))
 private static Component simulated$newSceneTag(Component original,ItemStack stack){
    if(original!=null && !stack.isEmpty() && SimConfigService.INSTANCE.client().itemConfig.showNewPonderTag.get() && !NewPonderTooltipManager.hasWatchedAllScenes(stack.getItem()))return original.copy().append(" ").append(SimLang.translate("tooltip.new_ponder").style(ChatFormatting.GOLD).component());
    return original;
 }
}
