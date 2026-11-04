package com.railwayteam.railways.internal.compat.client.model;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

/** Post-bake model wrapper using NeoForge's real level-aware model hooks. */
public abstract class WrapperBlockStateModel implements BlockStateModel {
    protected final BlockStateModel model;
    protected WrapperBlockStateModel(BlockState state,BlockStateModel model) { this.model=model; }
    public void addPartsWithInfo(BlockAndTintGetter world,BlockPos pos,BlockState state,RandomSource random,List<BlockStateModelPart> parts) { model.collectParts(world,pos,state,random,parts); }
    public static void addPartsWithInfo(BlockStateModel model,BlockAndTintGetter world,BlockPos pos,BlockState state,RandomSource random,List<BlockStateModelPart> parts) { model.collectParts(world,pos,state,random,parts); }
    @Override public void collectParts(BlockAndTintGetter world,BlockPos pos,BlockState state,RandomSource random,List<BlockStateModelPart> parts) { addPartsWithInfo(world,pos,state,random,parts); }
    @Override public void collectParts(RandomSource random,List<BlockStateModelPart> parts) { model.collectParts(random,parts); }
    public boolean needUpdateTerrainParticle() { return false; }
    public Material.Baked particleMaterialWithInfo(BlockAndTintGetter world,BlockPos pos,BlockState state) { return model.particleMaterial(world,pos,state); }
    @Override public Material.Baked particleMaterial(BlockAndTintGetter world,BlockPos pos,BlockState state) { return particleMaterialWithInfo(world,pos,state); }
    @Override public Material.Baked particleMaterial() { return model.particleMaterial(); }
    @Override public int materialFlags() { return model.materialFlags(); }
}
