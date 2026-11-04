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

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.world.item.ItemStack;

/** Common contraption behaviour for addon-owned Blaze machines. */
public class BlazeMovementBehaviour extends MovementBehaviour {
    @Override
    public ItemStack canBeDisabledVia(MovementContext context) {
        return null;
    }

    @Override
    public void tick(MovementContext context) {
        if (context.world.isClientSide()) {
            BlazeClientBridge.INSTANCE.tickMovement(this, context);
        }
    }

    public void invalidate(MovementContext context) {
        context.data.remove("Conductor");
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return attachRender != null;
    }
}
