package com.railwayteam.railways.content.fuel.tank;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Keeps the mounted tank's lerped fluid level ticking on the client. */
public class FuelTankMovementBehavior implements MovementBehaviour {
    @Override
    public boolean mustTickWhileDisabled() {
        return true;
    }

    @Override
    public void tick(MovementContext context) {
        if (!context.world.isClientSide()) {
            return;
        }
        BlockEntity be = context.contraption.getBlockEntityClientSide(context.localPos);
        if (be instanceof FuelTankBlockEntity tank && tank.getFluidLevel() != null) {
            tank.getFluidLevel().tickChaser();
        }
    }
}
