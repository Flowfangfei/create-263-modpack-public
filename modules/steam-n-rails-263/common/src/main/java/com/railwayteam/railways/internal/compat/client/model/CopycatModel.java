package com.railwayteam.railways.internal.compat.client.model;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.copycat.CopycatBlock;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

/** Copycat material geometry is resolved from Create's actual block entity at the render position. */
public abstract class CopycatModel extends WrapperBlockStateModel {
    protected CopycatModel(BlockState state,BlockStateModel model) { super(state,model); }
    @Override public void addPartsWithInfo(BlockAndTintGetter world,BlockPos pos,BlockState state,RandomSource random,List<BlockStateModelPart> parts) {
        if (!(state.getBlock() instanceof CopycatBlock block)) { model.collectParts(world,pos,state,random,parts);return; }
        BlockState material=world.getBlockEntity(pos) instanceof CopycatBlockEntity copycat ? copycat.getMaterial() : AllBlocks.COPYCAT_BASE.getDefaultState();
        addPartsWithInfo(world,pos,state,block,material,random,parts);
    }
    protected abstract void addPartsWithInfo(BlockAndTintGetter world,BlockPos pos,BlockState state,CopycatBlock block,BlockState material,RandomSource random,List<BlockStateModelPart> parts);
    protected static BlockStateModel getModelOf(BlockState material) { return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(material); }
    protected void addModelParts(BlockAndTintGetter world,BlockPos pos,BlockState material,RandomSource random,BlockStateModel model,List<BlockStateModelPart> parts) { model.collectParts(world,pos,material,random,parts); }
    protected List<BlockStateModelPart> getMaterialParts(BlockAndTintGetter world,BlockPos pos,BlockState material,RandomSource random,BlockStateModel model) { var parts=new ArrayList<BlockStateModelPart>();addModelParts(world,pos,material,random,model,parts);return parts; }
    @Override public Material.Baked particleMaterialWithInfo(BlockAndTintGetter world,BlockPos pos,BlockState state) { return world.getBlockEntity(pos) instanceof CopycatBlockEntity copycat ? getModelOf(copycat.getMaterial()).particleMaterial(world,pos,copycat.getMaterial()) : model.particleMaterial(world,pos,state); }
    protected DirectionData gatherDirectionData(CopycatBlock block,BlockState state) { var data=new DirectionData();for(var face:Direction.values())if(block.shouldFaceAlwaysRender(state,face))data.noCull(face);return data; }
    protected static class DirectionData {
        private int data;
        public void cull(Direction face) { data &= ~(1<<face.get3DDataValue()); }
        public void noCull(Direction face) { data |= 1<<face.get3DDataValue(); }
        public boolean isCull(Direction face) { return (data & (1<<face.get3DDataValue()))==0; }
        public boolean isUncull(Direction face) { return !isCull(face); }
    }
}
