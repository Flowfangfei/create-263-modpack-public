package com.george_vi.electroenergetics.foundation.scroll_value;

import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import net.minecraft.world.level.storage.ValueInput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.UUID;
import java.util.function.Predicate;

public class ScalingScrollValueBehaviour extends CEEValueBehaviour {

    private static final double[] scales = new double[]{1/1000d, 1/100d, 1/10d, 1d, 10d, 100d, 1000d};
    private static final int[] displayScales = new int[]{1000, 100, 10, 1, 10, 100, 1000};
    private Predicate<UUID> playerPredicate;

    public ScalingScrollValueBehaviour(Component label, SmartBlockEntity be) {
        super(label, be);
        this.between(0, 7);
        this.value = 3;
        playerPredicate = u -> true;
    }

    public void setPlayerPredicate(Predicate<UUID> playerPredicate) {
        this.playerPredicate = playerPredicate;
    }

    @Override
    public boolean isActive() {
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide())
            if (!canLocalPlayerSet(playerPredicate))
                return false;
        return super.isActive();
    }

    @Environment(EnvType.CLIENT)
    private static boolean canLocalPlayerSet(Predicate<UUID> p) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return false;
        return p.test(player.getUUID());
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
        if (!playerPredicate.test(player.getUUID()))
            return;
        super.setValueSettings(player, valueSetting, ctrlDown);
    }

    @Override
    public String formatValue() {
        return CEELang.string(value > 2 ? "gauge.scaling_forward" : "gauge.scaling_backward") + displayScales[Mth.clamp(value, 0, scales.length - 1)];
    }

    // Overwrite so that when people update, it doesn't turn to 1/1000.
    @Override
    public void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag nbt = NbtView.read(input);

        value = nbt.contains("ScrollValue") ? nbt.getIntOr("ScrollValue", 0) : 3;

    }

    public double getScale() {
        return scales[Mth.clamp(value, 0, scales.length - 1)];
    }
}
