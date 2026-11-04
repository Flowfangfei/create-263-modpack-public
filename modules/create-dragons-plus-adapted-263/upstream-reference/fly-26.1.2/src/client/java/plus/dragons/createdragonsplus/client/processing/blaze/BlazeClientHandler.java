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

package plus.dragons.createdragonsplus.client.processing.blaze;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockEntity;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockRenderer;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeClientBridge;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeMovementBehaviour;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeMovementRenderBehaviour;

/** Client implementation of the server-safe Blaze animation bridge. */
public final class BlazeClientHandler extends BlazeClientBridge {
    private final Set<BlazeMovementBehaviour> warnedMissingRender = Collections.newSetFromMap(new IdentityHashMap<>());

    public static void register() {
        BlazeClientBridge.install(new BlazeClientHandler());
    }

    @Override
    public boolean shouldTickAnimation(BlazeBlockEntity blockEntity) {
        return !VisualizationManager.supportsVisualization(blockEntity.getLevel());
    }

    @Override
    public void tickAnimation(BlazeBlockEntity blockEntity) {
        BlazeBlockRenderer.tickAnimation(blockEntity);
    }

    @Override
    public void tickMovement(BlazeMovementBehaviour behaviour, MovementContext context) {
        if (behaviour.attachRender instanceof BlazeMovementRenderBehaviour render) {
            render.tick(context);
        } else if (warnedMissingRender.add(behaviour)) {
            CreateDragonsPlus.LOGGER.warn(
                    "BlazeMovementBehaviour has no client render attachment; using fallback rendering");
        }
    }
}
