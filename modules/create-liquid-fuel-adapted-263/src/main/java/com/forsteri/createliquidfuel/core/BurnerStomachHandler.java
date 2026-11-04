package com.forsteri.createliquidfuel.core;

import com.forsteri.createliquidfuel.mixin.BlazeBurnerAccessor;
import com.forsteri.createliquidfuel.util.Triplet;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.HashMap;
import java.util.Map;

public final class BurnerStomachHandler {
    public static final Map<Fluid, Pair<Identifier, Triplet<Integer, Boolean, Integer>>> LIQUID_BURNER_FUEL_MAP = new HashMap<>();

    public static void tick(SmartBlockEntity entity) {
        if (!(entity instanceof BlazeBurnerBlockEntity burner) || !(entity instanceof IHasStomach owner)) return;
        if (entity.getLevel() == null || entity.getLevel().isClientSide() || burner.isCreative()) return;
        BurnerTank tank = owner.getCapability();
        if (tank == null || tank.getAmountAsInt(0) == 0) return;
        FluidResource resource = tank.getResource(0);
        LiquidFuel fuel = LiquidFuels.get(resource.getFluid());
        if (fuel == null || tank.getAmountAsInt(0) < fuel.amountConsumedPerTick()) return;

        FuelType nextFuel = fuel.superHeat() ? FuelType.SPECIAL : FuelType.NORMAL;
        if (nextFuel.ordinal() < burner.getActiveFuel().ordinal()) return;
        int oldTime = nextFuel == burner.getActiveFuel() ? burner.getRemainingBurnTime() : 0;
        long nextTime = (long) oldTime + fuel.burnTime();
        if (nextTime > BlazeBurnerBlockEntity.MAX_HEAT_CAPACITY) return;

        try (Transaction tx = Transaction.openRoot()) {
            if (tank.extract(0, resource, fuel.amountConsumedPerTick(), tx) != fuel.amountConsumedPerTick()) return;
            tx.commit();
        }
        BlazeBurnerAccessor access = (BlazeBurnerAccessor) burner;
        access.createliquidfuel$setActiveFuel(nextFuel);
        access.createliquidfuel$setRemainingBurnTime((int) nextTime);
        burner.updateBlockState();
        burner.setChanged();
    }

    /** Return null for non-liquid items so Create can process its regular fuel path. */
    public static com.simibubi.create.content.processing.burner.BlazeBurnerBlock.InsertResult tryInsert(
            net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level,
            net.minecraft.core.BlockPos pos, ItemStack stack, boolean doNotConsume, boolean simulate) {
        if (stack.isEmpty() || !state.hasBlockEntity()
                || !(level.getBlockEntity(pos) instanceof BlazeBurnerBlockEntity burner)
                || !(burner instanceof IHasStomach owner) || burner.isCreative() || burner.isCreativeFuel(stack)) return null;
        BurnerTank tank = owner.getCapability();
        if (tank == null) return null;
        // A real temporary slot supports both component changes and Item replacement (e.g. lava bucket -> bucket).
        var slot = new net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler(1);
        slot.set(0, net.neoforged.neoforge.transfer.item.ItemResource.of(stack.copyWithCount(1)), 1);
        ItemAccess access = ItemAccess.forHandlerIndexStrict(slot, 0);
        ResourceHandler<FluidResource> handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() != 1) return null;
        FluidStack fluid = FluidUtil.getStack(handler, 0);
        if (fluid.isEmpty() || !LiquidFuels.isFuel(fluid.getFluid())) return null;
        FluidResource resource = FluidResource.of(fluid);
        ItemStack remainder;
        boolean commit = !simulate && !level.isClientSide();
        try (Transaction tx = Transaction.openRoot()) {
            // Both participants must agree to the complete transfer. Failed extraction or capacity rolls both back.
            if (handler.extract(0, resource, fluid.getAmount(), tx) != fluid.getAmount()
                    || tank.insert(0, resource, fluid.getAmount(), tx) != fluid.getAmount())
                return com.simibubi.create.content.processing.burner.BlazeBurnerBlock.InsertResult.fail();
            remainder = access.getResource().toStack(access.getAmount());
            if (commit) tx.commit();
        }
        // Create mechanical arms pass a working copy and infer simulated acceptance from its decrease.
        if (!level.isClientSide() && !doNotConsume) stack.shrink(1);
        if (commit) {
            FluidUtil.triggerSoundAndGameEvent(resource, level, net.minecraft.world.phys.Vec3.atCenterOf(pos), null, false);
        }
        return com.simibubi.create.content.processing.burner.BlazeBurnerBlock.InsertResult.success(doNotConsume ? ItemStack.EMPTY : remainder);
    }
}
