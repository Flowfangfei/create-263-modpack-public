package com.george_vi.electroenergetics.foundation.scroll_value;

import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.george_vi.electroenergetics.foundation.lang.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class KineticUnlockableScrollValueBehaviour extends CEEValueBehaviour {

    public KineticUnlockableScrollValueBehaviour(Component label, SmartBlockEntity be) {
        super(label, be);
        withFormatter(v -> isUnlocked() ?
                CEELang.translateDirect("electric_motor.x_rpm", getUnlockedStressRPMMultiplier(v)).getString() :
                String.valueOf(Math.abs(v)));
    }

    public boolean isUnlocked() {
        return value >= 1_000_000;
    }

    @Override
    public CEEValueBehaviour between(int min, int max) {
        return super.between(min, 1_000_000 + max);
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
        int value;
        if (valueSetting.row() == 2)
            value = 1_000_000 + Math.max(1, valueSetting.value());
        else
            value = Math.max(1, valueSetting.value());
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        setValue(valueSetting.row() == 0 ? -value : value);
    }

    @Override
    public ValueSettings getValueSettings() {
        if (value >= 1_000_000)
            return new ValueSettings(2, Math.abs(value - 1_000_000));
        return new ValueSettings(value < 0 ? 0 : 1, Math.abs(value));
    }

    public MutableComponent formatSettings(ValueSettings settings) {
        if (settings.row() == 2)
            return CEELang.translateDirect("electric_motor.x_rpm", getUnlockedStressRPMMultiplier(settings.value()));
        return CreateLang.number(Math.max(1, Math.abs(settings.value())))
                .add(CreateLang.text(settings.row() == 0 ? "⟳" : "⟲")
                        .style(ChatFormatting.BOLD))
                .component();
    }

    public int getUnlockedStressRPMMultiplier(int value) {
        if (value < 1_000_000)
            return value;
        return value - 1_000_000;
    }

    public float getUnlockedStressRPMMultiplier() {
        return getUnlockedStressRPMMultiplier(value);
    }

    @Override
    public String getClipboardKey() {
        return "SpeedLockable";
    }
}
