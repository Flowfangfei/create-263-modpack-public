/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.schedule;

import com.mojang.serialization.Codec;
import com.simibubi.create.Create;
import net.createmod.catnip.api.data.Couple;
import net.createmod.catnip.api.data.WorldAttached;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DiscoveredPath;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.simibubi.create.content.trains.schedule.destination.ScheduleInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Emits a short wireless redstone pulse at the leading end of the train. */
public class RedstoneLinkInstruction extends ScheduleInstruction {
    private static final Codec<Couple<Frequency>> FREQUENCY_CODEC = Couple.codec(net.minecraft.world.item.ItemStack.OPTIONAL_CODEC.xmap(Frequency::of, Frequency::getStack));
    private static final WorldAttached<List<CustomRedstoneActor>> CUSTOM_ACTORS =
        new WorldAttached<>($ -> new ArrayList<>());

    public Couple<Frequency> freq;

    public RedstoneLinkInstruction(Identifier id) {
        super();
        freq = Couple.create(() -> Frequency.EMPTY);
        data.putInt("Power", 15);
    }

    public static void tick(Level level) {
        List<CustomRedstoneActor> actors = CUSTOM_ACTORS.get(level);
        for (Iterator<CustomRedstoneActor> iterator = actors.iterator(); iterator.hasNext(); ) {
            CustomRedstoneActor actor = iterator.next();
            actor.decrement();
            if (actor.isAlive()) {
                continue;
            }
            Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(level, actor);
            iterator.remove();
        }
    }

    @Override public Identifier getId() { return com.railwayteam.railways.registry.CRSchedule.REDSTONE_LINK; }

    public int power() { return intData("Power"); }
    @Override public int slotsTargeted() { return 2; }
    @Override public net.createmod.catnip.api.data.Pair<net.minecraft.world.item.ItemStack,net.minecraft.network.chat.Component> getSummary() { return new com.railwayteam.railways.content.schedule.client.RedstoneLinkInstructionRender().getSummary(this); }
    @Override public java.util.List<net.minecraft.network.chat.Component> getTitleAs(String type) { return new com.railwayteam.railways.content.schedule.client.RedstoneLinkInstructionRender().getTitleAs(this,type); }
    @Override public java.util.List<net.minecraft.network.chat.Component> getSecondLineTooltip(int slot) { return new com.railwayteam.railways.content.schedule.client.RedstoneLinkInstructionRender().getSecondLineTooltip(slot); }
    @Override public net.minecraft.world.item.ItemStack getSecondLineIcon() { return com.simibubi.create.AllBlocks.REDSTONE_LINK.asStack(); }
    @Override public void setItem(int slot,net.minecraft.world.item.ItemStack stack) { freq.set(slot==0,Frequency.of(stack)); }
    @Override public net.minecraft.world.item.ItemStack getItem(int slot) { return freq.get(slot==0).getStack(); }
    @Override public void initConfigurationWidgets(com.simibubi.create.foundation.gui.ModularGuiLineBuilder builder) { new com.railwayteam.railways.content.schedule.client.RedstoneLinkInstructionRender().initConfigurationWidgets(this,builder); }

    @Override
    public boolean supportsConditions() {
        return false;
    }

    @Override
    protected void writeAdditional(net.minecraft.core.HolderLookup.Provider registries, net.minecraft.nbt.CompoundTag tag) {
        var view = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, registries);
        view.store("Frequency", FREQUENCY_CODEC, freq);
        tag.merge(view.buildResult());
    }

    @Override
    protected void readAdditional(net.minecraft.core.HolderLookup.Provider registries, net.minecraft.nbt.CompoundTag tag) {
        var view = net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, registries, tag);
        view.read("Frequency", FREQUENCY_CODEC).ifPresent(value -> freq = value);
    }

    @Override
    public @Nullable DiscoveredPath start(ScheduleRuntime runtime, Level level) {
        Train train = runtime.train;
        if (train.graph == null || train.carriages.isEmpty()) {
            runtime.startCooldown();
            return null;
        }

        CustomRedstoneActor actor = new CustomRedstoneActor(this, train.carriages.getFirst());
        Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(level, actor);
        CUSTOM_ACTORS.get(level).add(actor);

        runtime.state = ScheduleRuntime.State.PRE_TRANSIT;
        runtime.currentEntry++;
        return null;
    }

    private static final class CustomRedstoneActor implements IRedstoneLinkable {
        private final RedstoneLinkInstruction instruction;
        private final Carriage carriage;
        private int ticks = 8;

        private CustomRedstoneActor(RedstoneLinkInstruction instruction, Carriage carriage) {
            this.instruction = instruction;
            this.carriage = carriage;
        }

        private void decrement() {
            ticks--;
        }

        @Override
        public int getTransmittedStrength() {
            return isAlive() ? instruction.intData("Power") : 0;
        }

        @Override
        public void setReceivedStrength(int power) {
        }

        @Override
        public boolean isListening() {
            return false;
        }

        @Override
        public boolean isAlive() {
            return ticks > 0;
        }

        @Override
        public Couple<Frequency> getNetworkKey() {
            return instruction.freq;
        }

        @Override
        public BlockPos getLocation() {
            return BlockPos.containing(carriage.getLeadingPoint().getPosition(carriage.train.graph));
        }
    }
}
