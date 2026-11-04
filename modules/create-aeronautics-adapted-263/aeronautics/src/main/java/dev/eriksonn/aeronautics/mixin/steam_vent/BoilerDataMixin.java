package dev.eriksonn.aeronautics.mixin.steam_vent;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.simibubi.create.content.fluids.tank.BoilerData;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.eriksonn.aeronautics.mixinterface.BoilerDataExtension;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BoilerData.class, remap = false)
public abstract class BoilerDataMixin implements BoilerDataExtension {
    @Shadow
    public abstract boolean isPassive(int boilerSize);

	@Shadow
	@Final
	private static float passiveEngineEfficiency;

	@Unique
	private int aeronautics$attachedVents = 0;

	@Inject(method = "evaluate", at = @At("HEAD"))
	private void aeronautics$countVents1(final FluidTankBlockEntity controller, final CallbackInfoReturnable<Boolean> cir, @Share("prevVents") final LocalIntRef prevVents) {
		prevVents.set(this.aeronautics$attachedVents);
		this.aeronautics$attachedVents = 0;
	}

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method="evaluate",at=@At(value="INVOKE",target="Lcom/tterrag/registrate/util/entry/BlockEntry;has(Lnet/minecraft/world/level/block/state/BlockState;)Z",ordinal=0))
    private boolean aeronautics$countVents2(com.tterrag.registrate.util.entry.BlockEntry<?> entry,BlockState attachedState,com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original){
        if(AeroBlocks.STEAM_VENT.has(attachedState))aeronautics$attachedVents++;
        return original.call(entry,attachedState);
    }

	@ModifyReturnValue(method = "evaluate", at = @At("RETURN"))
	private boolean aeronautics$countVents3(final boolean original, @Share("prevVents") final LocalIntRef prevVents) {
		return original || this.aeronautics$attachedVents != prevVents.get();
	}

	@ModifyReturnValue(method = "isActive", at = @At("RETURN"))
	private boolean aeronautics$activeWithVents(final boolean original) {
		return original || this.aeronautics$attachedVents > 0;
	}

	@ModifyExpressionValue(method = {"getEngineEfficiency", "updateOcclusion"}, at = @At(value = "FIELD", target = "Lcom/simibubi/create/content/fluids/tank/BoilerData;attachedEngines:I"))
	private int aeronautics$ventEfficiency(final int original) {
		return original + this.aeronautics$attachedVents;
	}

	@Override
	public int aeronautics$getAttachedVents() {
		return this.aeronautics$attachedVents;
	}

	@Override
	public float aeronautics$getIdealEfficiency(final int boilerSize) {
		return this.isPassive(boilerSize) ? passiveEngineEfficiency : 1;
	}

	@Inject(method = "clear", at = @At("TAIL"))
	private void aeronautics$clearVents(final CallbackInfo ci) {
		this.aeronautics$attachedVents = 0;
	}

	@Inject(method = "write", at = @At("RETURN"))
    private void aeronautics$writeVentData(final CallbackInfoReturnable<net.minecraft.nbt.CompoundTag> cir) {
        cir.getReturnValue().putInt("SimVents",this.aeronautics$attachedVents);
    }

	@Inject(method = "read", at = @At("TAIL"))
	private void aeronautics$readVentData(final net.minecraft.nbt.CompoundTag input, final int boilerSize, final CallbackInfo ci) {
		this.aeronautics$attachedVents = input.getIntOr("SimVents", 0);
	}
}
