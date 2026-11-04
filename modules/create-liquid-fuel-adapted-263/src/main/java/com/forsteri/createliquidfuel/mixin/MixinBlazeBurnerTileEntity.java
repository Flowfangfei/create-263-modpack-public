package com.forsteri.createliquidfuel.mixin;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import com.forsteri.createliquidfuel.core.BurnerTank;
import com.forsteri.createliquidfuel.core.IHasStomach;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(value = BlazeBurnerBlockEntity.class, remap = false)
public abstract class MixinBlazeBurnerTileEntity extends SmartBlockEntity implements IHasStomach {
    @Unique private BurnerTank createliquidfuel$stomach;

    public MixinBlazeBurnerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    @Unique
    public BurnerTank getCapability() { return createliquidfuel$stomach; }

    @Inject(method = "addBehaviours", at = @At("TAIL"))
    private void createliquidfuel$addTank(List<BlockEntityBehaviour> behaviours, CallbackInfo ci) {
        createliquidfuel$stomach = new BurnerTank(this::setChanged);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void createliquidfuel$tick(CallbackInfo ci) { BurnerStomachHandler.tick(this); }

    @Inject(method = "read", at = @At("TAIL"))
    private void createliquidfuel$read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (createliquidfuel$stomach != null) createliquidfuel$stomach.read(registries, tag.getCompoundOrEmpty("Stomach"));
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void createliquidfuel$write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (createliquidfuel$stomach != null) tag.put("Stomach", createliquidfuel$stomach.write(registries));
    }

}
