package com.yision.fluidlogistics.foundation.capability;

import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler;
import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandlerItem;
import com.simibubi.create.compat.neoforge263.items.IItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

/** Real NeoForge capability lookups presented to the retained Create transfer algorithms. */
public final class LegacyCapabilityAccess {
    private LegacyCapabilityAccess() {}
    public static @Nullable IFluidHandler fluid(@Nullable ResourceHandler<FluidResource> value) {
        return value == null ? null : value instanceof IFluidHandler legacy ? legacy : new FluidView(value);
    }
    public static @Nullable IItemHandler item(@Nullable ResourceHandler<ItemResource> value) {
        return value == null ? null : IItemHandler.of(value);
    }
    public static @Nullable IFluidHandler fluid(Level level, BlockPos pos, @Nullable Direction side) {
        return fluid(level.getCapability(Capabilities.Fluid.BLOCK,pos,side));
    }
    public static @Nullable IFluidHandler fluid(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity entity, @Nullable Direction side) {
        return fluid(level.getCapability(Capabilities.Fluid.BLOCK,pos,state,entity,side));
    }
    public static @Nullable IItemHandler item(Level level, BlockPos pos, @Nullable Direction side) {
        return item(level.getCapability(Capabilities.Item.BLOCK,pos,side));
    }
    public static @Nullable IItemHandler item(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity entity, @Nullable Direction side) {
        return item(level.getCapability(Capabilities.Item.BLOCK,pos,state,entity,side));
    }
    public static @Nullable IFluidHandlerItem fluid(ItemStack stack) {
        return stack.isEmpty() ? null : com.simibubi.create.compat.neoforge263.fluids.FluidUtil.getFluidHandler(stack).orElse(null);
    }
    @SuppressWarnings("unchecked")
    public static @Nullable ResourceHandler<FluidResource> resource(@Nullable IFluidHandler handler) {
        if(handler == null)return null;
        if(handler instanceof ResourceHandler<?> modern)return (ResourceHandler<FluidResource>)modern;
        throw new IllegalArgumentException("Legacy handler has no transactional view: "+handler.getClass().getName());
    }
    private static final class FluidView implements IFluidHandler,ResourceHandler<FluidResource> {
        private final ResourceHandler<FluidResource> modern;
        private final IFluidHandler legacy;
        FluidView(ResourceHandler<FluidResource> modern){this.modern=modern;this.legacy=IFluidHandler.of(modern);}
        public int getTanks(){return legacy.getTanks();}
        public net.neoforged.neoforge.fluids.FluidStack getFluidInTank(int i){return legacy.getFluidInTank(i);}
        public int getTankCapacity(int i){return legacy.getTankCapacity(i);}
        public boolean isFluidValid(int i,net.neoforged.neoforge.fluids.FluidStack f){return legacy.isFluidValid(i,f);}
        public int fill(net.neoforged.neoforge.fluids.FluidStack f,FluidAction a){return legacy.fill(f,a);}
        public net.neoforged.neoforge.fluids.FluidStack drain(net.neoforged.neoforge.fluids.FluidStack f,FluidAction a){return legacy.drain(f,a);}
        public net.neoforged.neoforge.fluids.FluidStack drain(int a,FluidAction action){return legacy.drain(a,action);}
        public int size(){return modern.size();}
        public FluidResource getResource(int i){return modern.getResource(i);}
        public long getAmountAsLong(int i){return modern.getAmountAsLong(i);}
        public long getCapacityAsLong(int i,FluidResource f){return modern.getCapacityAsLong(i,f);}
        public boolean isValid(int i,FluidResource f){return modern.isValid(i,f);}
        public int insert(int i,FluidResource f,int amount,net.neoforged.neoforge.transfer.transaction.TransactionContext tx){return modern.insert(i,f,amount,tx);}
        public int extract(int i,FluidResource f,int amount,net.neoforged.neoforge.transfer.transaction.TransactionContext tx){return modern.extract(i,f,amount,tx);}
    }
}
