package dev.simulated_team.simulated.compat.create;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.world.item.ItemStack;
import java.util.function.Predicate;

/** Delegate filtering, slot rendering and packet updates to native Create. */
public class FilteringBehaviour extends com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour {
    private final ValueBoxTransform simulatedSlot;
    public FilteringBehaviour(SmartBlockEntity be,ValueBoxTransform slot){super(be,slot);simulatedSlot=slot;}
    @Override public FilteringBehaviour withPredicate(Predicate<ItemStack> predicate){super.withPredicate(predicate);return this;}
    public ValueBoxTransform getSlotPositioning(){return simulatedSlot;}
}
