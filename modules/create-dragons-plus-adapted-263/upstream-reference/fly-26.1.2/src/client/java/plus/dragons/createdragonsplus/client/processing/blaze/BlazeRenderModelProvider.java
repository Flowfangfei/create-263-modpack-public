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

import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

/** Selects addon-owned Blaze head accessories for a rendering context. */
public interface BlazeRenderModelProvider {
    default @Nullable PartialModel goggles(BlazeRenderContext context) {
        return null;
    }

    default @Nullable PartialModel hat(BlazeRenderContext context) {
        return null;
    }

    static BlazeRenderModelProvider goggles(Function<BlazeRenderContext, @Nullable PartialModel> selector) {
        Objects.requireNonNull(selector, "selector");
        return new BlazeRenderModelProvider() {
            @Override
            public @Nullable PartialModel goggles(BlazeRenderContext context) {
                return selector.apply(context);
            }
        };
    }

    static BlazeRenderModelProvider hat(Function<BlazeRenderContext, @Nullable PartialModel> selector) {
        Objects.requireNonNull(selector, "selector");
        return new BlazeRenderModelProvider() {
            @Override
            public @Nullable PartialModel hat(BlazeRenderContext context) {
                return selector.apply(context);
            }
        };
    }
}
