package com.simibubi.create.foundation.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.foundation.item.CustomBlockEntityTagItem;
import com.simibubi.create.foundation.mixin.accessor.UseOnContextAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
   @WrapOperation(
      method = "place",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/item/BlockItem;updateCustomBlockEntityTag(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Z"
      )
   )
   private boolean create$customPlacementData(Level level, Player player, BlockPos pos, ItemStack stack, Operation<Boolean> original) {
      return this instanceof CustomBlockEntityTagItem item
         ? item.updateCustomBlockEntityTag(pos, level, player, stack, level.getBlockState(pos))
         : (Boolean)original.call(new Object[]{level, player, pos, stack});
   }

   @Inject(method = "place", at = @At("HEAD"), cancellable = true)
   private void create$fixDeployerPlacement(BlockPlaceContext pContext, CallbackInfoReturnable<InteractionResult> cir) {
      BlockState state = pContext.getLevel().getBlockState(((UseOnContextAccessor)pContext).create$getHitResult().getBlockPos());
      if (!state.canBeReplaced() && pContext.getPlayer() instanceof DeployerFakePlayer) {
         cir.setReturnValue(InteractionResult.PASS);
      }
   }
}
