package com.forsteri.createliquidfuel.mixin;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=BlazeBurnerBlock.class, remap=false)
public abstract class MixinBlazeBurnerBlock {
    @Inject(method="tryInsert",at=@At("HEAD"),cancellable=true)
    private static void createliquidfuel$insertContainer(BlockState state, Level level, BlockPos pos,
            ItemStack stack, boolean doNotConsume, boolean forceOverflow, boolean simulate,
            CallbackInfoReturnable<BlazeBurnerBlock.InsertResult> cir) {
        var result=BurnerStomachHandler.tryInsert(state,level,pos,stack,doNotConsume,simulate);
        if(result!=null)cir.setReturnValue(result);
    }
}
