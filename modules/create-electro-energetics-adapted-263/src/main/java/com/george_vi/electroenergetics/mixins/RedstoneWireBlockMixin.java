package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.content.redstone_relay.RedstoneRelayBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneWireBlock.class)
public class RedstoneWireBlockMixin {
    @Inject(method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z",
        at = @At("HEAD"), cancellable = true)
    private static void electroenergetics$connectRelay(BlockState state, Direction direction,
                                                       CallbackInfoReturnable<Boolean> callback) {
        if (state.getBlock() instanceof RedstoneRelayBlock relay)
            callback.setReturnValue(relay.canConnectRedstone(state, null, null, direction));
    }
}
