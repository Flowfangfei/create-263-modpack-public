/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.common.advancements.criterion;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

/** A unit criterion using Minecraft 26.3's advancement-owned trigger map. */
public class BuiltinTrigger implements CriterionTrigger<BuiltinTrigger>, CriterionTriggerInstance {
    private final Codec<BuiltinTrigger> codec = com.mojang.serialization.MapCodec.unitCodec(this);

    public void trigger(ServerPlayer player) {
        var advancements = player.getAdvancements();
        for (var key : List.copyOf(advancements.getTriggerMapForType(this).keySet()))
            advancements.award(key.advancement(), key.criterion());
    }

    @Override public Codec<BuiltinTrigger> codec() { return codec; }
    @Override public void validate(ValidationContextSource source) {}
}
