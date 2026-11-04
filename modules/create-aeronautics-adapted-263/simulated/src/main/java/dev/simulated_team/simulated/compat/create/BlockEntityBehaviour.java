package dev.simulated_team.simulated.compat.create;

/** Native Create owns behaviour ticking and NBT persistence on NeoForge. */
public abstract class BlockEntityBehaviour extends com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour {
    protected BlockEntityBehaviour(com.simibubi.create.foundation.blockEntity.SmartBlockEntity be){super(be);}
    public net.minecraft.world.level.Level getLevel(){return blockEntity.getLevel();}
}
