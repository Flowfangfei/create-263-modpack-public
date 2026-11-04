package com.george_vi.electroenergetics.foundation.scroll_value;

import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ResistanceScrollValueBehaviour extends CEEValueBehaviour {
    public ResistanceScrollValueBehaviour(Component label, SmartBlockEntity be) {
        super(label, be);
        this.between(0, 1_000_000_000);
        this.value = 1_000_000;
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
        int value = Math.max(1, valueSetting.value());
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        setValue((int) (indexToResistance(value, valueSetting.row()) * 1000d));
    }

    @Override
    public ValueSettings getValueSettings() {

        if (value >= 9_600 && value <= 1_000_000) {
            if (value <= 100_000)
                return new ValueSettings(1, value / 1_000);
            else
                return new ValueSettings(1, (value / 10_000) + 90);
        } else if (value > 1_000_000 && value <= 1_000_000_000) {
            if (value <= 100_000_000)
                return new ValueSettings(2, value / 1_000_000);
            else
                return new ValueSettings(2, (value / 10_000_000) + 90);
        }
        return new ValueSettings(0, (value - 100) / 50);
    }

    @Override
    public String formatValue() {
        return CEELang.formatResistance(value / 1000d).string();
    }

    public double getResistance() {
        return Math.max(0.01, value / 1000d);
    }

    private static double indexToResistance(int i, int row) {
        if (row == 0) {
            return (i / 20d) + 0.1;
        } else if (row == 1) {

            if (i < 100)
                return Math.max(1, i);
            else
                return (i - 90) * 10;
        }

        if (i < 100)
            return Math.max(1, i) * 1000;
        else
            return (i - 90) * 10000;
    }
}
