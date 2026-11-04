package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.events.CEEClientEvents;
import com.george_vi.electroenergetics.foundation.CEEHoldInteractionHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @WrapOperation(
            method = "turnPlayer",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void electroenergetics$turn(
            LocalPlayer player, double dx, double dy, Operation<Void> original) {
        if (CEEHoldInteractionHandler.isInteracting())
            CEEHoldInteractionHandler.onPlayerMove(dx, dy);
        else original.call(player, dx, dy);
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void electroenergetics$scroll(
            long window, double horizontal, double vertical, CallbackInfo ci) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (window == minecraft.getWindow().handle()
                && minecraft.gui.screen() == null
                && minecraft.gui.overlay() == null
                && minecraft.player != null
                && CEEClientEvents.mouseScrolled(vertical)) ci.cancel();
    }
}
