package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEETags;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void electroenergetics$hideHeld(PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState state, float partialTicks, float xRot,
            InteractionHand hand, float attack, ItemStack item, float inverseArmHeight,
            PoseStack matrices, SubmitNodeCollector queue, int light, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.getMainHandItem().is(CEEItems.LINEMANS_STICK.get())
                && !item.is(CEEItems.LINEMANS_STICK.get()) && item.is(CEETags.HIDE_ON_LINEMANS_STICK))
            ci.cancel();
    }
}
