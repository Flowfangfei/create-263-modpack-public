package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.custom_tracks.casing.CasingChecker;
import com.railwayteam.railways.content.custom_tracks.casing.CasingCollisionUtils;
import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import net.createmod.catnip.api.level.wrapper.SchematicLevel;
import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.content.trains.track.TrackShape;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@Mixin(value = TrackBlockEntity.class, remap = false)
public abstract class MixinTrackBlockEntity extends SmartBlockEntity implements IHasTrackCasing {
    @Shadow
    Map<BlockPos, BezierConnection> connections;

    @Unique
    protected Block railways$trackCasing;
    @Unique
    protected boolean railways$isAlternateModel;

    protected MixinTrackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable Block railways$getTrackCasing() {
        return railways$trackCasing;
    }

    public void railways$setTrackCasing(@Nullable Block trackCasing) {
        if (trackCasing != null && !CasingChecker.isValid(trackCasing))
            return;
        this.railways$trackCasing = trackCasing;
        notifyUpdate();
        if (this.level == null)
            return;

        if (this.railways$trackCasing == null) {
            CasingCollisionUtils.manageTracks((TrackBlockEntity) (Object) this, true);
            if (!this.level.isClientSide()) {
                if (!this.connections.isEmpty() || getBlockState().getOptionalValue(TrackBlock.SHAPE)
                    .orElse(TrackShape.NONE)
                    .isPortal())
                    return;
                BlockState blockState = this.level.getBlockState(worldPosition);
                if (blockState.hasProperty(TrackBlock.HAS_BE))
                    level.setBlockAndUpdate(worldPosition, blockState.setValue(TrackBlock.HAS_BE, false));
            }
        } else if (!railways$isAlternateModel) {
            CasingCollisionUtils.manageTracks((TrackBlockEntity) (Object) this, false);
        }
    }

    public boolean railways$isAlternate() {
        return railways$isAlternateModel;
    }

    public void railways$setAlternate(boolean alternate) {
        if (getBlockState().getValue(TrackBlock.SHAPE).getModel().equals("ascending"))
            alternate = false;
        this.railways$isAlternateModel = alternate;
        if (railways$trackCasing != null)
            CasingCollisionUtils.manageTracks((TrackBlockEntity) (Object) this, alternate);
        notifyUpdate();
    }

    @Inject(method = "write(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V", at = @At("RETURN"))
    private void writeCasing(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        railways$storeCasing(tag);
    }

    @Inject(method = "writeSafe", at = @At("RETURN"))
    private void railways$writeCasingSafe(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        railways$storeCasing(tag);
    }

    @Unique
    private void railways$storeCasing(CompoundTag tag) {
        Block casing = railways$getTrackCasing();
        if (casing != null)
            tag.putString("TrackCasing", BuiltInRegistries.BLOCK.getKey(casing).toString());
        tag.putBoolean("AlternateModel", this.railways$isAlternate());
    }

    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V", at = @At("RETURN"))
    private void readCasing(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        railways$setAlternate(tag.getBooleanOr("AlternateModel", false));

        if (tag.getString("TrackCasing").isPresent()) {
            Identifier casingName = Identifier.parse(tag.getStringOr("TrackCasing", ""));
            if (BuiltInRegistries.BLOCK.containsKey(casingName)) {
                BuiltInRegistries.BLOCK.get(casingName)
                    .ifPresent(holder -> railways$setTrackCasing(holder.value()));
                return;
            }
        }
        railways$setTrackCasing(null);
    }

    @Inject(
        method = "removeConnection",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
        ),
        cancellable = true
    )
    private void railways$preventCasedTileRemoval(BlockPos target, CallbackInfo ci) {
        if (railways$getTrackCasing() != null) {
            notifyUpdate();
            ci.cancel();
        }
    }

    @Inject(
        method = "removeInboundConnections",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/foundation/blockEntity/RemoveBlockEntityPacket;<init>(Lnet/minecraft/core/BlockPos;)V"
        ),
        cancellable = true
    )
    private void railways$preventCasedTileRemovalInbound(boolean dropItems, CallbackInfo ci) {
        if (railways$getTrackCasing() != null) {
            notifyUpdate();
            ci.cancel();
        }
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void railways$removeCasingCollisions( CallbackInfo ci) {
        if (level == null || level.isClientSide() || level instanceof SchematicLevel)
            return;
        CasingCollisionUtils.manageTracks((TrackBlockEntity) (Object) this, true);
    }

    @Inject(method = "lazyTick", at = @At("HEAD"))
    private void manageCasingCollisions(CallbackInfo ci) {
        if (railways$trackCasing == null || railways$isAlternateModel || level instanceof SchematicLevel)
            return;
        CasingCollisionUtils.manageTracks((TrackBlockEntity) (Object) this, false);
    }

}
