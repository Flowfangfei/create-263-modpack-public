package com.railwayteam.railways.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.fabric.CRBlocksImpl;
import com.railwayteam.railways.content.fuel.tank.FuelTankBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/** Representative render scene, explicitly enabled only in the isolated renderer-scene world. */
final class ClientSmokeScene {
    private ClientSmokeScene() {}
    static void create(Minecraft mc) {
        var server=mc.getSingleplayerServer();
        if(server==null)throw new IllegalStateException("Render scene requires an integrated server");
        server.execute(() -> {
            var level=server.overworld();
            for(BlockPos pos:BlockPos.betweenClosed(-8,99,-5,8,99,10))level.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(-5,100,0),CRBlocksImpl.FUEL_TANK.getDefaultState());
            if(level.getBlockEntity(new BlockPos(-5,100,0)) instanceof FuelTankBlockEntity tank)
                tank.getTankInventory().fill(new FluidStack(Fluids.LAVA,1000),com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            level.setBlockAndUpdate(new BlockPos(0,100,0),CRBlocks.SINGLEAXLE_BOGEY.getDefaultState());
            level.setBlockAndUpdate(new BlockPos(4,100,0),CRBlocks.MONO_BOGEY.getDefaultState());
            level.setBlockAndUpdate(new BlockPos(-5,100,-3),CRBlocks.TRACK_COUPLER.getDefaultState());
            level.setBlockAndUpdate(new BlockPos(-1,100,-3),CRBlocks.SEMAPHORE.getDefaultState());
            var door=BuiltInRegistries.BLOCK.getValue(Railways.asResource("sliding_locometal_door")).defaultBlockState();
            level.setBlockAndUpdate(new BlockPos(3,100,-3),door.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));
            level.setBlockAndUpdate(new BlockPos(3,101,-3),door.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
            var flywheel=BuiltInRegistries.BLOCK.getValue(Railways.asResource("locometal_flywheel"));
            if(flywheel==Blocks.AIR)throw new IllegalStateException("Scene flywheel block missing");
            level.setBlockAndUpdate(new BlockPos(6,100,-3),flywheel.defaultBlockState());
            var source=server.createCommandSourceStack();
            server.getCommands().performPrefixedCommand(source,"gamemode creative @a");
            server.getCommands().performPrefixedCommand(source,"tp @a 0.5 102 8.5 180 22");
            Railways.LOGGER.info("Railways client render scene created: fuel tank, bogeys, coupler, semaphore, palette door, flywheel");
        });
    }
}
