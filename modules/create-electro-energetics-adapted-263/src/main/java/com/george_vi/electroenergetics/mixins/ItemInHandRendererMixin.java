package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEETags;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void electroenergetics$hideHeld(
            LivingEntity entity,
            ItemStack item,
            ItemDisplayContext context,
            PoseStack matrices,
            SubmitNodeCollector queue,
            int light,
            CallbackInfo ci) {
        if (entity instanceof Player player
                && player.getMainHandItem().is(CEEItems.LINEMANS_STICK.get())
                && !item.is(CEEItems.LINEMANS_STICK.get())
                && item.is(CEETags.HIDE_ON_LINEMANS_STICK)) ci.cancel();
    }
}
