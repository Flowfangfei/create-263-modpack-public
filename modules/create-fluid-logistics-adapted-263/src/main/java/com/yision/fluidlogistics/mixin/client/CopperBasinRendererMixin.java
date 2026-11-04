package com.yision.fluidlogistics.mixin.client;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRenderer;
import com.yision.fluidlogistics.content.processing.copperBasin.*;
@Mixin(BasinRenderer.class)
public class CopperBasinRendererMixin {
    @Inject(method="renderFluids",at=@At("HEAD"),cancellable=true)
    private static void fluidlogistics$capacity(BasinBlockEntity basin,float partialTicks,com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.SubmitNodeCollector collector,int light,CallbackInfoReturnable<Float> cir) {
        if(basin instanceof CopperBasinBlockEntity) cir.setReturnValue(CopperBasinRenderer.submitCopperFluids(basin,partialTicks,pose,collector,light));
    }
}
