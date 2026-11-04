package com.george_vi.electroenergetics.foundation.scroll_value;

import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class InductanceScrollValueBehaviour extends CEEValueBehaviour {
    public InductanceScrollValueBehaviour(Component label, SmartBlockEntity be) {
        super(label, be);
        this.between(0, 1_000_000_000);
        this.value = 300_000;
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
        int value = Math.max(1, valueSetting.value());
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        setValue((int) (indexToInductance(value, valueSetting.row()) * 1e+9d));
    }

    @Override
    public ValueSettings getValueSettings() {

        if (value <= 1_000_000) {
            if (value <= 100_000)
                return new ValueSettings(0, value / 1_000);
            else
                return new ValueSettings(0, (value / 10_000) + 90);
        }

        if (value <= 100_000_000)
            return new ValueSettings(1, value / 1_000_000);
        else
            return new ValueSettings(1, (value / 10_000_000) + 90);

    }

    @Override
    public String formatValue() {
        return CEELang.formatInductance(value / 1e+9d).string();
    }

    public double getInductance() {
        return value / 1e+9d;
    }

    private static double indexToInductance(int i, int row) {
        if (row == 0) {

            if (i < 100)
                return i * 1e-9d;
            else
                return (i - 90) * 1e-8d;
        }

        if (row == 1) {

            if (i < 100)
                return i == 0 ? 1e-6d : i * 1e-6d;
            else
                return (i - 90) * 1e-5d;
        }

        if (i < 100)
            return i == 0 ? 1e-3d : i * 1e-3d;
        else
            return (i - 90) * 1e-2d;
    }
}
