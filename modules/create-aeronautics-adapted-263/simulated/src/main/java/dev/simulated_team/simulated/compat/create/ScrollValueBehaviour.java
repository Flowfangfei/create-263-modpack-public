package dev.simulated_team.simulated.compat.create;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Target Create combines server value storage and client interaction in one behaviour. */
public class ScrollValueBehaviour extends com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour {
    private final ValueBoxTransform simulatedSlot;
    public ScrollValueBehaviour(Component label,SmartBlockEntity be,ValueBoxTransform slot){super(label,be,slot);simulatedSlot=slot;}
    @Override public ScrollValueBehaviour between(int min,int max){super.between(min,max);return this;}
    @Override public ScrollValueBehaviour withCallback(Consumer<Integer> callback){super.withCallback(callback);return this;}
    @Override public ScrollValueBehaviour withFormatter(Function<Integer,String> formatter){super.withFormatter(formatter);return this;}
    @Override public ScrollValueBehaviour onlyActiveWhen(Supplier<Boolean> active){super.onlyActiveWhen(active);return this;}
    @Override public ValueBoxTransform getSlotPositioning(){return simulatedSlot;}
    public net.minecraft.world.level.Level getLevel(){return getWorld();}
    public int getMax(){return max;}
}
