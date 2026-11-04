package com.enxv.aerouniversaljoint.mixin;

import com.enxv.aerouniversaljoint.access.SwivelBearingPlateParentAccess;
import dev.simulated_team.simulated.index.SimBlocks;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
public class BlockRenderDispatcherMixin {
    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    private void aeronautics$hideDampingBearingPlate(BlockQuadOutput output, float x, float y, float z,
            BlockAndTintGetter level, BlockPos pos, BlockState state, BlockStateModel model, long seed, CallbackInfo ci) {
        if (aeronautics$shouldHide(state, pos, level)) ci.cancel();
    }

    private boolean aeronautics$shouldHide(BlockState state, BlockPos pos, BlockAndTintGetter level) {
        if (!state.is(SimBlocks.SWIVEL_BEARING_LINK_BLOCK)) {
            return false;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SwivelBearingPlateParentAccess access)) {
            return false;
        }

        Level actualLevel = blockEntity.getLevel();
        if (actualLevel == null) {
            return false;
        }

        return access.aeronautics$isDampingStressBearingParent(actualLevel);
    }
}
