package dev.eriksonn.aeronautics.mixin.ponder;

import net.createmod.ponder.impl.client.instruction.TickingInstruction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TickingInstruction.class)
public interface TickingInstructionAccessor {
    @Accessor(remap = false)
    void setRemainingTicks(int time);
}
