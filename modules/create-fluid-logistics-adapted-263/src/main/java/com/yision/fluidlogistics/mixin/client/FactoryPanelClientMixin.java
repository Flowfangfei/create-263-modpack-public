package com.yision.fluidlogistics.mixin.client;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelClient;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.yision.fluidlogistics.content.logistics.factoryGauge.ResourceFactoryPanelBehaviour;
@Mixin(FactoryPanelClient.class)
public class FactoryPanelClientMixin {
    @Inject(method="openScreen",at=@At("HEAD"),cancellable=true)
    private static void fluidlogistics$resourceScreen(FactoryPanelBehaviour behaviour,CallbackInfo ci) {
        if(behaviour instanceof ResourceFactoryPanelBehaviour resource && resource.isResourceGauge()) {
            var player=net.minecraft.client.Minecraft.getInstance().player;
            if(player!=null) resource.displayScreen(player);
            ci.cancel();
        }
    }
}
