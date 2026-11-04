/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.content.conductor.toolbox.MountedToolbox;
import com.railwayteam.railways.content.conductor.toolbox.MountedToolboxDisposeAllPacket;
import com.railwayteam.railways.content.conductor.toolbox.MountedToolboxEquipPacket;
import com.railwayteam.railways.registry.CRPackets;
import net.createmod.catnip.api.client.gui.AbstractSimiScreen;
import com.simibubi.create.content.equipment.toolbox.RadialToolboxMenu;
import com.simibubi.create.content.equipment.toolbox.RadialToolboxMenu.State;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RadialToolboxMenu.class, remap = false)
public abstract class MixinRadialToolboxMenu extends AbstractSimiScreen {
    protected MixinRadialToolboxMenu(net.minecraft.network.chat.Component title) { super(title); }
    @Shadow
    private ToolboxBlockEntity selectedBox;
    @Shadow
    private boolean scrollMode;
    @Shadow
    private int scrollSlot;
    @Shadow
    private int hoveredSlot;
    @Shadow
    private State state;

    @Inject(method = "lambda$removed$0", at = @At("HEAD"), cancellable = true)
    private static void railways$sendMountedToolboxDisposeAllPacketsToAll(ToolboxBlockEntity te, CallbackInfo ci) {
        if (te instanceof MountedToolbox mounted) {
            CRPackets.PACKETS.send(new MountedToolboxDisposeAllPacket(mounted.getParent()));
            ci.cancel();
        }
    }

    @Inject(
        method = "removed",
        remap = true,
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/equipment/toolbox/ToolboxDisposeAllPacket;<init>(Lnet/minecraft/core/BlockPos;)V",
            remap = false
        ),
        cancellable = true
    )
    private void railways$sendMountedToolboxDisposeAllPacket(CallbackInfo ci) {
        if (selectedBox instanceof MountedToolbox mounted) {
            CRPackets.PACKETS.send(new MountedToolboxDisposeAllPacket(mounted.getParent()));
            ci.cancel();
        }
    }

    @Inject(
        method = "removed",
        remap = true,
        at = {
            @At(
                value = "INVOKE",
                target = "Lcom/simibubi/create/content/equipment/toolbox/ToolboxEquipPacket;<init>(Lnet/minecraft/core/BlockPos;II)V",
                ordinal = 1,
                remap = false
            ),
            @At(
                value = "INVOKE",
                target = "Lcom/simibubi/create/content/equipment/toolbox/ToolboxEquipPacket;<init>(Lnet/minecraft/core/BlockPos;II)V",
                ordinal = 2,
                remap = false
            )
        },
        cancellable = true
    )
    private void railways$sendMountedToolboxEquipPacketOnRemove(CallbackInfo ci) {
        if (!(selectedBox instanceof MountedToolbox mounted))
            return;
        int selected = scrollMode ? scrollSlot : hoveredSlot;
        int hotbarSlot = minecraft.player.getInventory().getSelectedSlot();
        CRPackets.PACKETS.send(new MountedToolboxEquipPacket(mounted.getParent(), selected, hotbarSlot));
        ci.cancel();
    }

    // Target mouseClicked closes the screen; all equip packets are sent from removed().
}
