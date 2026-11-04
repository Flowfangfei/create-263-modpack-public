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

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer;
import com.zurrtum.create.client.content.processing.burner.ScrollInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visual.TickableVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.Translate;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleTickableVisual;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.client.processing.blaze.BlazeRenderContext;
import plus.dragons.createdragonsplus.client.processing.blaze.BlazeRenderModels;

/** Flywheel visual for addon-owned Blaze machines. */
public class BlazeBlockVisual<T extends BlazeBlockEntity> extends AbstractBlockEntityVisual<T>
        implements SimpleDynamicVisual, SimpleTickableVisual {
    private HeatLevel heatLevel = HeatLevel.SMOULDERING;
    private boolean active;
    private final TransformedInstance head;
    private @Nullable TransformedInstance smallRods;
    private @Nullable TransformedInstance largeRods;
    private @Nullable ScrollInstance flame;
    private @Nullable PartialModel gogglesModel;
    private @Nullable TransformedInstance goggles;
    private @Nullable PartialModel hatModel;
    private @Nullable TransformedInstance hat;

    public BlazeBlockVisual(VisualizationContext context, T blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        active = blockEntity.isActive();
        PartialModel blazeModel = BlazeBurnerRenderer.getBlazeModel(heatLevel, active);
        head = instancerProvider()
                .instancer(InstanceTypes.TRANSFORMED, Models.partial(blazeModel))
                .createInstance();
        head.light(LightCoordsUtil.FULL_BRIGHT);
        animate(partialTick);
    }

    @Override
    public void tick(TickableVisual.Context context) {
        blockEntity.tickAnimation();
    }

    @Override
    public void beginFrame(DynamicVisual.Context context) {
        if (isVisible(context.frustum()) && !doDistanceLimitThisFrame(context)) {
            animate(context.partialTick());
        }
    }

    protected void animate(float partialTick) {
        float animation = blockEntity.headAnimation.getValue(partialTick) * 0.175F;
        boolean active = animation > 0.125F;
        HeatLevel heatLevel = blockEntity.getHeatLevelForRender();
        boolean heatChanged = heatLevel != this.heatLevel;

        if (active != this.active || heatChanged) {
            PartialModel blazeModel = BlazeBurnerRenderer.getBlazeModel(heatLevel, active);
            instancerProvider()
                    .instancer(InstanceTypes.TRANSFORMED, Models.partial(blazeModel))
                    .stealInstance(head);
            updateRods(heatLevel);
            if (heatChanged && flame != null) {
                flame.delete();
                flame = null;
            }
            this.active = active;
            this.heatLevel = heatLevel;
        }

        if (active && flame == null) {
            setupFlameInstance();
        } else if (!active && flame != null) {
            flame.delete();
            flame = null;
        }

        BlazeRenderContext renderContext = new BlazeRenderContext(blockEntity.getBlockState(), heatLevel, blockEntity, null);
        updateGoggles(BlazeRenderModels.goggles(renderContext));
        updateHat(BlazeRenderModels.hat(renderContext));

        int seed = blockEntity.hashCode();
        float renderTime = AnimationTickHolder.getRenderTime(level);
        float seededRenderTime = renderTime + (seed % 13) * 16F;
        float offsetScale = heatLevel.isAtLeast(HeatLevel.FADING) ? 64 : 16;
        float offset = Mth.sin((seededRenderTime / 16F) % Mth.TWO_PI) / offsetScale;
        float headY = offset - animation * 0.75F;
        float horizontalAngle = AngleHelper.rad(blockEntity.headAngle.getValue(partialTick));

        head.setIdentityTransform()
                .translate(getVisualPosition())
                .translateY(headY)
                .translate(Translate.CENTER)
                .rotateY(horizontalAngle)
                .translateBack(Translate.CENTER)
                .setChanged();

        if (goggles != null && gogglesModel != null) {
            setupGogglesTransform(gogglesModel, goggles, headY, horizontalAngle);
        }
        if (hat != null && hatModel != null) {
            setupHatTransform(hatModel, hat, headY, horizontalAngle);
        }
        if (smallRods != null) {
            float smallOffset = Mth.sin((seededRenderTime / 16F + Mth.PI) % Mth.TWO_PI) / offsetScale;
            smallRods.setIdentityTransform()
                    .translate(getVisualPosition())
                    .translateY(smallOffset + animation + 0.125F)
                    .setChanged();
        }
        if (largeRods != null) {
            float largeOffset = Mth.sin((seededRenderTime / 16F + Mth.HALF_PI) % Mth.TWO_PI) / offsetScale;
            largeRods.setIdentityTransform()
                    .translate(getVisualPosition())
                    .translateY(largeOffset + animation - 3 / 16F)
                    .setChanged();
        }
    }

    private void updateRods(HeatLevel heatLevel) {
        if (!heatLevel.isAtLeast(HeatLevel.FADING)) {
            if (smallRods != null) {
                smallRods.delete();
                smallRods = null;
            }
            if (largeRods != null) {
                largeRods.delete();
                largeRods = null;
            }
            return;
        }
        PartialModel smallModel = heatLevel == HeatLevel.SEETHING
                ? AllPartialModels.BLAZE_BURNER_SUPER_RODS
                : AllPartialModels.BLAZE_BURNER_RODS;
        PartialModel largeModel = heatLevel == HeatLevel.SEETHING
                ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2
                : AllPartialModels.BLAZE_BURNER_RODS_2;
        if (smallRods == null) {
            smallRods = instancerProvider()
                    .instancer(InstanceTypes.TRANSFORMED, Models.partial(smallModel))
                    .createInstance();
            smallRods.light(LightCoordsUtil.FULL_BRIGHT);
        } else {
            instancerProvider()
                    .instancer(InstanceTypes.TRANSFORMED, Models.partial(smallModel))
                    .stealInstance(smallRods);
        }
        if (largeRods == null) {
            largeRods = instancerProvider()
                    .instancer(InstanceTypes.TRANSFORMED, Models.partial(largeModel))
                    .createInstance();
            largeRods.light(LightCoordsUtil.FULL_BRIGHT);
        } else {
            instancerProvider()
                    .instancer(InstanceTypes.TRANSFORMED, Models.partial(largeModel))
                    .stealInstance(largeRods);
        }
    }

    private void updateGoggles(@Nullable PartialModel model) {
        if (model == null) {
            if (goggles != null) {
                goggles.delete();
                goggles = null;
            }
            gogglesModel = null;
            return;
        }
        if (goggles == null) {
            goggles = instancerProvider()
                    .instancer(InstanceTypes.TRANSFORMED, Models.partial(model))
                    .createInstance();
            goggles.light(LightCoordsUtil.FULL_BRIGHT);
        } else if (gogglesModel != model) {
            instancerProvider()
                    .instancer(InstanceTypes.TRANSFORMED, Models.partial(model))
                    .stealInstance(goggles);
        }
        gogglesModel = model;
    }

    private void updateHat(@Nullable PartialModel model) {
        if (model == null) {
            if (hat != null) {
                hat.delete();
                hat = null;
            }
            hatModel = null;
            return;
        }
        if (hat == null) {
            hat = instancerProvider()
                    .instancer(InstanceTypes.TRANSFORMED, Models.partial(model))
                    .createInstance();
            hat.light(LightCoordsUtil.FULL_BRIGHT);
        } else if (hatModel != model) {
            instancerProvider()
                    .instancer(InstanceTypes.TRANSFORMED, Models.partial(model))
                    .stealInstance(hat);
        }
        hatModel = model;
    }

    protected void setupGogglesTransform(
            PartialModel model, TransformedInstance instance, float headY, float horizontalAngle) {
        instance.setIdentityTransform()
                .translate(getVisualPosition())
                .rotateCentered(horizontalAngle, Direction.UP)
                .translateY(headY + 0.5F)
                .setChanged();
    }

    protected void setupHatTransform(
            PartialModel model, TransformedInstance instance, float headY, float horizontalAngle) {
        instance.setIdentityTransform()
                .translate(getVisualPosition())
                .rotateCentered(horizontalAngle + Mth.PI, Direction.UP)
                .translate(0.5F, headY + 0.75F, 0.5F)
                .setChanged();
    }

    protected void setupFlameInstance() {
        flame = instancerProvider()
                .instancer(
                        AllInstanceTypes.SCROLLING,
                        Models.partial(AllPartialModels.BLAZE_BURNER_FLAME))
                .createInstance();
        flame.position(getVisualPosition()).light(LightCoordsUtil.FULL_BRIGHT);
        SpriteShiftEntry spriteShift = heatLevel == HeatLevel.SEETHING
                ? AllSpriteShifts.SUPER_BURNER_FLAME
                : AllSpriteShifts.BURNER_FLAME;
        float spriteWidth = spriteShift.getTarget().getU1() - spriteShift.getTarget().getU0();
        float spriteHeight = spriteShift.getTarget().getV1() - spriteShift.getTarget().getV0();
        float speed = 1 / 32F + 1 / 64F * heatLevel.ordinal();
        flame.speedU = speed / 2;
        flame.speedV = speed;
        flame.scaleU = spriteWidth / 2;
        flame.scaleV = spriteHeight / 2;
        flame.diffU = spriteShift.getTarget().getU0() - spriteShift.getOriginal().getU0();
        flame.diffV = spriteShift.getTarget().getV0() - spriteShift.getOriginal().getV0();
    }

    @Override
    public void updateLight(float partialTick) {}

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {}

    @Override
    protected void _delete() {
        head.delete();
        if (smallRods != null) {
            smallRods.delete();
        }
        if (largeRods != null) {
            largeRods.delete();
        }
        if (flame != null) {
            flame.delete();
        }
        if (goggles != null) {
            goggles.delete();
        }
        if (hat != null) {
            hat.delete();
        }
    }
}
