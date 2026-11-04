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

import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer.BlazeBurnerRenderData;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.api.registry.CDPRegistryEntry;

/** Client registry for addon-owned Blaze head models. */
public final class BlazeRenderModels {
    private static final Map<Block, BlazeRenderModelProvider> PROVIDERS = new IdentityHashMap<>();

    private BlazeRenderModels() {}

    public static synchronized void register(Block block, BlazeRenderModelProvider provider) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(provider, "provider");
        if (PROVIDERS.putIfAbsent(block, provider) != null) {
            throw new IllegalArgumentException("A Blaze render model provider is already registered for " + block);
        }
    }

    public static void register(
            CDPRegistryEntry<? extends Block> block, BlazeRenderModelProvider provider) {
        register(block.get(), provider);
    }

    public static synchronized @Nullable PartialModel goggles(BlazeRenderContext context) {
        BlazeRenderModelProvider provider = PROVIDERS.get(context.state().getBlock());
        return provider == null ? null : provider.goggles(context);
    }

    public static synchronized @Nullable PartialModel hat(BlazeRenderContext context) {
        BlazeRenderModelProvider provider = PROVIDERS.get(context.state().getBlock());
        return provider == null ? null : provider.hat(context);
    }

    public static void apply(BlazeBurnerRenderData data, BlazeRenderContext context) {
        PartialModel goggles = goggles(context);
        if (goggles != null) {
            data.goggles = CachedBuffers.partial(goggles, context.state())
                    .light(LightCoordsUtil.FULL_BRIGHT)
                    .extractRenderState();
        }

        PartialModel hat = hat(context);
        if (hat != null) {
            data.hat = CachedBuffers.partial(hat, context.state())
                    .light(LightCoordsUtil.FULL_BRIGHT)
                    .extractRenderState();
            data.hatScale = false;
            data.hatAngle = data.horizontalAngle == null
                    ? new org.joml.Quaternionf().setAngleAxis(Mth.PI, 0, 1, 0)
                    : new org.joml.Quaternionf(data.horizontalAngle).rotateY(Mth.PI);
        }
    }
}
