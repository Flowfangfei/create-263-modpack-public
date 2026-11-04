package dev.eriksonn.aeronautics.mixin.steam_vent;
import com.simibubi.create.content.fluids.tank.BoilerData;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.eriksonn.aeronautics.data.AeroLang;
import dev.eriksonn.aeronautics.mixinterface.BoilerDataExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Target Create owns boiler tooltips directly, including the shared engine/vent budget. */
@Mixin(value=BoilerData.class,remap=false)
public abstract class FluidTankTooltipBehaviourMixin {
 @Inject(method="addToGoggleTooltip",at=@At("HEAD"),cancellable=true)
 private void aeronautics$ventTooltip(List<Component> tooltip,boolean sneaking,int size,CallbackInfoReturnable<Boolean> cir){
    BoilerData data=(BoilerData)(Object)this;BoilerDataExtension extension=(BoilerDataExtension)this;
    int vents=extension.aeronautics$getAttachedVents();if(vents==0)return;
    data.calcMinMaxForSize(size);
    CreateLang.translate("boiler.status",data.getHeatLevelTextComponent().withStyle(ChatFormatting.GREEN)).forGoggles(tooltip);
    CreateLang.builder().add(data.getSizeComponent(true,false)).forGoggles(tooltip,1);
    CreateLang.builder().add(data.getWaterComponent(true,false)).forGoggles(tooltip,1);
    CreateLang.builder().add(data.getHeatComponent(true,false)).forGoggles(tooltip,1);
    int level=Math.max(Math.min(data.activeHeat,Math.min(data.getMaxHeatLevelForWaterSupply(),data.getMaxHeatLevelForBoilerSize(size))),1);
    float ideal=extension.aeronautics$getIdealEfficiency(size);
    double total=ideal*16*level*com.simibubi.create.api.stress.BlockStressValues.getCapacity(com.simibubi.create.AllBlocks.STEAM_ENGINE.get());
    CreateLang.translate("tooltip.capacityProvided").style(ChatFormatting.GRAY).forGoggles(tooltip);
    CreateLang.number(total).translate("generic.unit.stress").style(ChatFormatting.AQUA).forGoggles(tooltip,1);
    float fraction=data.getEngineEfficiency(size)/ideal;
    AeroLang.translate("tooltip.capacity_used").style(ChatFormatting.GRAY).forGoggles(tooltip);
    if(data.attachedEngines>0)CreateLang.number(total*fraction*data.attachedEngines/level).translate("generic.unit.stress").style(ChatFormatting.AQUA).space().add((data.attachedEngines==1?CreateLang.translate("boiler.via_one_engine"):CreateLang.translate("boiler.via_engines",data.attachedEngines)).style(ChatFormatting.DARK_GRAY)).forGoggles(tooltip,1);
    CreateLang.number(total*fraction*vents/level).translate("generic.unit.stress").style(ChatFormatting.AQUA).space().add((vents==1?AeroLang.translate("boiler.via_one_vent"):AeroLang.translate("boiler.via_vents",vents)).style(ChatFormatting.DARK_GRAY).component()).forGoggles(tooltip,1);
    cir.setReturnValue(true);
 }
}
