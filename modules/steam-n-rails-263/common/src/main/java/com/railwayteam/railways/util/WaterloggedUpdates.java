package com.railwayteam.railways.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

public final class WaterloggedUpdates {
    private WaterloggedUpdates() { }
    public static void schedule(LevelReader level,ScheduledTickAccess ticks,BlockState state,BlockPos pos) {
        if(state.getValue(BlockStateProperties.WATERLOGGED))ticks.scheduleTick(pos,Fluids.WATER,Fluids.WATER.getTickDelay(level));
    }
}
