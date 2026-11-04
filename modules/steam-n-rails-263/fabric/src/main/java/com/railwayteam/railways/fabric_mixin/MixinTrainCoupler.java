package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.coupling.coupler.TrackCoupler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.railwayteam.railways.mixin_interfaces.IOccupiedCouplers;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.simibubi.create.foundation.codec.CreateCodecs;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(value = Train.class, remap = false)
public abstract class MixinTrainCoupler implements IOccupiedCouplers {
    @Shadow
    public TrackGraph graph;

    @Shadow
    public boolean updateSignalBlocks;

    @Shadow
    public List<Carriage> carriages;

    @Unique
    private final Set<UUID> railways$occupiedCouplers = new HashSet<>();

    @Override
    public Set<UUID> railways$getOccupiedCouplers() {
        return railways$occupiedCouplers;
    }

    @Inject(
        method = "earlyTick",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/trains/entity/Train;addToSignalGroups(Ljava/util/Collection;)V",
            ordinal = 2,
            shift = At.Shift.AFTER
        )
    )
    private void railways$tickOccupiedCouplers(Level level, CallbackInfo ci) {
        if (graph == null || railways$occupiedCouplers.isEmpty())
            return;

        for (UUID uuid : railways$occupiedCouplers) {
            TrackCoupler coupler = graph.getPoint(CREdgePointTypes.COUPLER, uuid);
            if (coupler != null)
                coupler.keepAlive((Train) (Object) this);
        }
    }

    @Inject(method = "frontSignalListener", at = @At("RETURN"), cancellable = true)
    private void railways$frontCouplerListener(CallbackInfoReturnable<TravellingPoint.IEdgePointListener> cir) {
        TravellingPoint.IEdgePointListener originalListener = cir.getReturnValue();
        cir.setReturnValue((distance, couple) -> {
            if (couple.getFirst() instanceof TrackCoupler coupler) {
                railways$occupiedCouplers.add(coupler.getId());
                return false;
            }
            return originalListener.test(distance, couple);
        });
    }

    @Inject(method = "backSignalListener", at = @At("RETURN"), cancellable = true)
    private void railways$backCouplerListener(CallbackInfoReturnable<TravellingPoint.IEdgePointListener> cir) {
        TravellingPoint.IEdgePointListener originalListener = cir.getReturnValue();
        cir.setReturnValue((distance, couple) -> {
            if (couple.getFirst() instanceof TrackCoupler coupler) {
                railways$occupiedCouplers.remove(coupler.getId());
                return false;
            }
            return originalListener.test(distance, couple);
        });
    }

    @Inject(method = "collectInitiallyOccupiedSignalBlocks", at = @At("HEAD"))
    private void railways$clearOccupiedCouplers(CallbackInfo ci) {
        railways$occupiedCouplers.clear();
    }

    /**
     * Re-scan the initially occupied train path for couplers after Create has rebuilt its signal
     * occupancy.  Older ports injected into a numbered compiler-generated lambda here; those
     * names are not stable across Create releases (the same lambda is {@code $1} in 26.2).
     * Keeping the hook on the declared method makes it resilient to lambda renumbering.
     */
    @Inject(method = "collectInitiallyOccupiedSignalBlocks", at = @At("TAIL"))
    private void railways$reAddOccupiedCouplers(CallbackInfo ci) {
        TravellingPoint trailingPoint = carriages.getLast().getTrailingPoint();
        if (trailingPoint.edge == null)
            return;

        TravellingPoint couplerScout = new TravellingPoint(
            trailingPoint.node1,
            trailingPoint.node2,
            trailingPoint.edge,
            trailingPoint.position,
            false
        );
        ((Train) (Object) this).forEachTravellingPointBackwards((point, distance) ->
            couplerScout.travel(
                graph,
                distance,
                couplerScout.follow(point),
                (distanceToPoint, edgePoint) -> {
                    if (edgePoint.getFirst() instanceof TrackCoupler coupler)
                        railways$occupiedCouplers.add(coupler.getId());
                    return false;
                },
                couplerScout.ignoreTurns()
            )
        );
    }


    @Inject(method="write(Lcom/simibubi/create/content/trains/graph/DimensionPalette;Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/resources/RegistryOps;)Lnet/minecraft/nbt/CompoundTag;",at=@At("RETURN"))
    private void railways$persistCouplers(DimensionPalette dimensions,net.minecraft.core.HolderLookup.Provider registries,
      net.minecraft.resources.RegistryOps<net.minecraft.nbt.Tag> ops,CallbackInfoReturnable<net.minecraft.nbt.CompoundTag> ci) {
        var entries=new net.minecraft.nbt.ListTag();
        railways$occupiedCouplers.stream().sorted().forEach(id->entries.add(net.minecraft.nbt.StringTag.valueOf(id.toString())));
        ci.getReturnValue().put("OccupiedCouplers",entries);
    }
    @Inject(method="read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/resources/RegistryOps;Ljava/util/Map;Lcom/simibubi/create/content/trains/graph/DimensionPalette;)Lcom/simibubi/create/content/trains/entity/Train;",at=@At("RETURN"))
    private static void railways$readCouplers(net.minecraft.nbt.CompoundTag tag,net.minecraft.core.HolderLookup.Provider registries,
      net.minecraft.resources.RegistryOps<net.minecraft.nbt.Tag> ops,Map<UUID,TrackGraph> graphs,DimensionPalette dimensions,
      CallbackInfoReturnable<Train> ci) {
        var occupied=((IOccupiedCouplers)ci.getReturnValue()).railways$getOccupiedCouplers();
        for(var value:tag.getListOrEmpty("OccupiedCouplers"))value.asString().ifPresent(id->occupied.add(UUID.fromString(id)));
    }
}
