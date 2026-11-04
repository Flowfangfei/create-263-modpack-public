package dev.simulated_team.simulated.neoforge.mixin.multiloader;
import dev.simulated_team.simulated.multiloader.CommonRedstoneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(BlockBehaviour.class)
public abstract class RedstoneWireConnectionMixin {
    @Inject(method="shouldRedstoneWireConnectTo",at=@At("HEAD"),cancellable=true)
    private void simulated$wireConnection(BlockState state,BlockGetter level,BlockPos pos,Direction direction,CallbackInfoReturnable<Boolean> cir){
        if(state.getBlock() instanceof CommonRedstoneBlock block)cir.setReturnValue(block.commonConnectRedstone(state,level,pos,direction));
    }
}
