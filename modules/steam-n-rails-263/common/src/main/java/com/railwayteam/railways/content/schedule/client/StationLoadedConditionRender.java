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

import com.railwayteam.railways.content.schedule.StationLoadedCondition;
import net.createmod.catnip.api.data.Pair;
import com.simibubi.create.content.trains.schedule.IScheduleInput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Client-side summary renderer for the station-loaded wait condition. */
public class StationLoadedConditionRender {
    public Pair<ItemStack, Component> getSummary(StationLoadedCondition input) {
        return Pair.of(ItemStack.EMPTY, Component.translatable("railways.schedule.condition.loaded"));
    }
}
