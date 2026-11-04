/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.schedule.client;

import com.google.common.collect.ImmutableList;
import com.railwayteam.railways.content.schedule.RedstoneLinkInstruction;
import com.simibubi.create.AllItems;
import net.createmod.catnip.api.data.Pair;
import com.simibubi.create.content.trains.schedule.IScheduleInput;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Client-side editor and summary renderer for a wireless redstone pulse. */
public class RedstoneLinkInstructionRender {
    public int slotsTargeted() {
        return 2;
    }

    public Pair<ItemStack, Component> getSummary(RedstoneLinkInstruction input) {
        return Pair.of(icon(), formatted(input));
    }

    private MutableComponent formatted(RedstoneLinkInstruction input) {
        return Component.translatable(
            "railways.schedule.instruction.redstone_link.power",
            input.power()
        );
    }

    public List<Component> getSecondLineTooltip(int slot) {
        return ImmutableList.of(CreateLang.translateDirect(
            slot == 0 ? "logistics.firstFrequency" : "logistics.secondFrequency"
        ).withStyle(ChatFormatting.RED));
    }

    public List<Component> getTitleAs(RedstoneLinkInstruction input, String type) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule.condition.redstone_link.frequency_powered"),
            Component.literal(" #1 ").withStyle(ChatFormatting.GRAY)
                .append(input.freq.getFirst().getStack().getHoverName().copy().withStyle(ChatFormatting.DARK_AQUA)),
            Component.literal(" #2 ").withStyle(ChatFormatting.GRAY)
                .append(input.freq.getSecond().getStack().getHoverName().copy().withStyle(ChatFormatting.DARK_AQUA))
        );
    }

    public void setItem(RedstoneLinkInstruction input, int slot, ItemStack stack) {
        input.freq.set(slot == 0, Frequency.of(stack));
    }

    public ItemStack getItem(RedstoneLinkInstruction input, int slot) {
        return input.freq.get(slot == 0).getStack();
    }

    public ItemStack getSecondLineIcon() {
        return icon();
    }

    public void initConfigurationWidgets(RedstoneLinkInstruction input, ModularGuiLineBuilder builder) {
        builder.addScrollInput(20, 101, (scroll, label) -> scroll
            .withRange(1, 16)
            .withStepFunction(context -> context.shift ? 5 : 1)
            .titled(Component.translatable("railways.schedule.instruction.redstone_link.power_edit_box")), "Power");
    }

    private ItemStack icon() {
        return com.simibubi.create.AllBlocks.REDSTONE_LINK.asStack();
    }
}
