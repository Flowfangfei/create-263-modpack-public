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

package plus.dragons.createdragonsplus.common.processing.blaze;

import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import java.util.Objects;

/**
 * Server-safe bridge for Blaze animation code implemented in the client source set.
 *
 * <p>The default instance is deliberately inert so loading a {@link BlazeBlockEntity} on a dedicated
 * server never resolves a client class.
 */
public class BlazeClientBridge {
    public static volatile BlazeClientBridge INSTANCE = new BlazeClientBridge();

    public static void install(BlazeClientBridge bridge) {
        INSTANCE = Objects.requireNonNull(bridge, "bridge");
    }

    public boolean shouldTickAnimation(BlazeBlockEntity blockEntity) {
        return false;
    }

    public void tickAnimation(BlazeBlockEntity blockEntity) {}

    public void tickMovement(BlazeMovementBehaviour behaviour, MovementContext context) {}
}
