/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.schedule.RedstoneLinkInstruction;
import com.railwayteam.railways.content.schedule.StationLoadedCondition;
import com.railwayteam.railways.content.schedule.WaypointDestinationInstruction;
import com.simibubi.create.content.trains.schedule.Schedule;
import net.createmod.catnip.api.data.Pair;
import com.simibubi.create.content.trains.schedule.condition.ScheduleWaitCondition;
import com.simibubi.create.content.trains.schedule.destination.ScheduleInstruction;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Function;

/** Registers the Railways schedule entries in Create Fly's factory lists. */
public final class CRSchedule {
    public static final Identifier REDSTONE_LINK = Railways.asResource("redstone_link");
    public static final Identifier WAYPOINT_DESTINATION = Railways.asResource("waypoint_destination");
    public static final Identifier LOADED = Railways.asResource("loaded");

    private static boolean registered;

    private CRSchedule() {
    }

    public static synchronized void register() {
        if (!registered) {
            registerInstruction(REDSTONE_LINK, RedstoneLinkInstruction::new);
            registerInstruction(WAYPOINT_DESTINATION, WaypointDestinationInstruction::new);
            registerCondition(LOADED, StationLoadedCondition::new);
            registered = true;
        }

        verifyRegistration();
    }

    private static void registerInstruction(
        Identifier id,
        Function<Identifier, ? extends ScheduleInstruction> factory
    ) {
        removeDuplicate(Schedule.INSTRUCTION_TYPES, id);
        Schedule.INSTRUCTION_TYPES.add(Pair.of(id, () -> factory.apply(id)));
    }

    private static void registerCondition(
        Identifier id,
        Function<Identifier, ? extends ScheduleWaitCondition> factory
    ) {
        removeDuplicate(Schedule.CONDITION_TYPES, id);
        Schedule.CONDITION_TYPES.add(Pair.of(id, () -> factory.apply(id)));
    }

    private static <T> void removeDuplicate(List<Pair<Identifier, T>> entries, Identifier id) {
        entries.removeIf(entry -> entry.getFirst().equals(id));
    }

    private static ScheduleInstruction createInstruction(Identifier id) {
        return Schedule.INSTRUCTION_TYPES.stream().filter(entry -> entry.getFirst().equals(id))
            .findFirst().orElseThrow().getSecond().get();
    }
    private static ScheduleWaitCondition createCondition(Identifier id) {
        return Schedule.CONDITION_TYPES.stream().filter(entry -> entry.getFirst().equals(id))
            .findFirst().orElseThrow().getSecond().get();
    }
    private static void verifyRegistration() {
        long instructionCount = Schedule.INSTRUCTION_TYPES.stream()
            .filter(entry -> entry.getFirst().equals(REDSTONE_LINK)
                || entry.getFirst().equals(WAYPOINT_DESTINATION))
            .count();
        long conditionCount = Schedule.CONDITION_TYPES.stream()
            .filter(entry -> entry.getFirst().equals(LOADED))
            .count();

        if (instructionCount != 2
            || conditionCount != 1
            || !(createInstruction(REDSTONE_LINK) instanceof RedstoneLinkInstruction redstone)
            || redstone.power() != 15
            || !(createInstruction(WAYPOINT_DESTINATION)
                instanceof WaypointDestinationInstruction)
            || !(createCondition(LOADED) instanceof StationLoadedCondition)) {
            throw new IllegalStateException("Railways custom schedule registration is incomplete");
        }

        Railways.LOGGER.info("Railways custom schedule instruction/condition registration verified");
    }
}
