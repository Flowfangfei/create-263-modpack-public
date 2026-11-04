package dev.eriksonn.aeronautics.mixin.levitite;
@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.level.block.FireBlock.class)
public interface FireBlockInvoker {
 @org.spongepowered.asm.mixin.gen.Invoker("setFlammable") void aeronautics$setFlammable(net.minecraft.world.level.block.Block block,int encouragement,int flammability);
}
