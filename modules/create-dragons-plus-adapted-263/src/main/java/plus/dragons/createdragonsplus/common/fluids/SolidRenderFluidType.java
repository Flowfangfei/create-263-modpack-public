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

package plus.dragons.createdragonsplus.common.fluids;

import com.tterrag.registrate.builders.FluidBuilder.FluidTypeFactory;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector3f;
import plus.dragons.createdragonsplus.util.CodeReference;

/** Common fluid properties. Client models and extensions are registered by CDPClient. */
@CodeReference(targets = "com.simibubi.create.AllFluids.SolidRenderedPlaceableFluidType", source = "create", license = "mit")
public class SolidRenderFluidType extends FluidType {
    protected static final int NO_TINT = -1;
    protected static final int NO_ALPHA = 0x00FFFFFF;
    private final int tintColor;
    private final int blockTintColor;
    private final Identifier stillTexture;
    private final Identifier flowingTexture;
    private final Vector3f fogColor;
    private final Supplier<Float> fogDistanceModifier;

    protected SolidRenderFluidType(Properties properties, Identifier stillTexture, Identifier flowingTexture, int tintColor, Vector3f fogColor, Supplier<Float> fogDistanceModifier) {
        super(properties);
        this.tintColor = tintColor;
        this.blockTintColor = tintColor & NO_ALPHA;
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.fogColor = fogColor;
        this.fogDistanceModifier = fogDistanceModifier;
    }
    public static FluidTypeFactory create(int tintColor, Vector3f fogColor, Supplier<Float> fogDistanceModifier) {
        return (properties, stillTexture, flowingTexture) -> new SolidRenderFluidType(properties, stillTexture, flowingTexture, tintColor, fogColor, fogDistanceModifier);
    }
    public static FluidTypeFactory create(Vector3f fogColor, Supplier<Float> fogDistanceModifier) {
        return create(NO_TINT, fogColor, fogDistanceModifier);
    }
    public Identifier getStillTexture() { return stillTexture; }
    public Identifier getFlowingTexture() { return flowingTexture; }
    public int getModelTintColor() { return tintColor; }
    public int getTintColor(FluidStack stack) { return tintColor; }
    public int getBlockTintColor() { return blockTintColor; }
    public Vector3f getCustomFogColor() { return fogColor; }
    public float getFogDistanceModifier() { return fogDistanceModifier.get(); }
}
