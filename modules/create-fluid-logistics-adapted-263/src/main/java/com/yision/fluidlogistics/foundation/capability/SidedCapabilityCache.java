package com.yision.fluidlogistics.foundation.capability;

import java.util.EnumMap;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

/** Typed native capability caches with an optional retained-API view. */
public final class SidedCapabilityCache<T> {
    private final EnumMap<Direction,Supplier<T>> caches=new EnumMap<>(Direction.class);
    private final Lookup<T> direct;
    private final CacheFactory<T> factory;
    public SidedCapabilityCache(BlockCapability<T,@Nullable Direction> capability) {this(capability,Function.identity());}
    public <R> SidedCapabilityCache(BlockCapability<R,@Nullable Direction> capability,Function<R,T> view) {
        direct=(level,pos,side)->{R value=level.getCapability(capability,pos,side);return value==null?null:view.apply(value);};
        factory=(level,pos,side)->{
            BlockCapabilityCache<R,@Nullable Direction> cache=BlockCapabilityCache.create(capability,level,pos,side);
            return ()->{R value=cache.getCapability();return value==null?null:view.apply(value);};
        };
    }
    public @Nullable T get(Level level,BlockPos targetPos,Direction side) {
        if(!(level instanceof ServerLevel server))return direct.get(level,targetPos,side.getOpposite());
        return caches.computeIfAbsent(side,key->factory.create(server,targetPos,key.getOpposite())).get();
    }
    public void clear(){caches.clear();}
    private interface Lookup<T>{T get(Level level,BlockPos pos,Direction side);}
    private interface CacheFactory<T>{Supplier<T> create(ServerLevel level,BlockPos pos,Direction side);}
}
