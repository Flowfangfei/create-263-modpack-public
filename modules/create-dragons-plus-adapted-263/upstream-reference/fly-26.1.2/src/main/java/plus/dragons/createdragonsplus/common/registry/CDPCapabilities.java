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

package plus.dragons.createdragonsplus.common.registry;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.common.behaviours.BehaviourProvider;

/** Fabric replacements for the public block capabilities exposed by earlier releases. */
public final class CDPCapabilities {
    public static final BlockApiLookup<BehaviourProvider, @Nullable Void> BEHAVIOUR_PROVIDER = BlockApiLookup.get(CreateDragonsPlus.id("behaviour_provider"), BehaviourProvider.class, Void.class);

    private CDPCapabilities() {}
}
