package com.george_vi.electroenergetics.foundation.scroll_value;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Common value state shared by the server interaction and client value-box adapters. */
public class CEEValueBehaviour extends ServerScrollValueBehaviour {
    public int value;
    public int min;
    public int max = 1;
    public final Component label;
    public Function<Integer, String> formatter = String::valueOf;
    public Supplier<Boolean> active = () -> true;
    public boolean needsWrench;

    public CEEValueBehaviour(Component label, SmartBlockEntity blockEntity) {
        super(blockEntity);
        this.label = label;
    }

    @Override
    public void setValue(int value) {
        int next = Mth.clamp(value, min, max);
        if (next == this.value) return;
        this.value = next;
        callback.accept(next);
        blockEntity.setChanged();
        blockEntity.sendData();
    }

    @Override
    public int getValue() { return value; }

    @Override
    public int getMax() { return max; }

    @Override
    public ValueSettings getValueSettings() { return new ValueSettings(0, value); }

    @Override
    public CEEValueBehaviour between(int min, int max) {
        this.min = min;
        this.max = max;
        return this;
    }

    @Override
    public CEEValueBehaviour withCallback(Consumer<Integer> callback) {
        this.callback = callback;
        return this;
    }

    public CEEValueBehaviour withFormatter(Function<Integer, String> formatter) {
        this.formatter = formatter;
        return this;
    }

    public CEEValueBehaviour onlyActiveWhen(Supplier<Boolean> active) {
        this.active = active;
        return this;
    }

    public CEEValueBehaviour requiresWrench() { needsWrench = true; return this; }
    public boolean isActive() { return active.get(); }
    public String formatValue() { return formatter.apply(value); }

    @Override
    public void read(ValueInput input, boolean clientPacket) {
        value = Mth.clamp(input.getIntOr("ScrollValue", value), min, max);
    }

    @Override
    public void write(ValueOutput output, boolean clientPacket) {
        output.putInt("ScrollValue", value);
    }
}
