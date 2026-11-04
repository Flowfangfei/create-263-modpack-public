package com.railwayteam.railways.internal.compat.client.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class AllModels {
    private static final Map<Block,BiFunction<BlockState,BlockStateModel,BlockStateModel>> WRAPPERS=new IdentityHashMap<>();
    private AllModels() { }
    public static void register(Block block,BiFunction<BlockState,BlockStateModel,BlockStateModel> factory) {
        WRAPPERS.merge(block,factory,(previous,next)->(state,model)->next.apply(state,previous.apply(state,model)));
    }
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        event.getBakingResult().blockStateModels().replaceAll((state,model)-> {
            var factory=WRAPPERS.get(state.getBlock());return factory==null?model:factory.apply(state,model);
        });
    }
}
