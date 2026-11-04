package com.simibubi.create.compat.rei;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.compat.rei.animation.AnimatedKinetics;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public final class AnimatedFan extends AnimatedKinetics {
   private final String type;

   public AnimatedFan(String type) {
      this.type = type;
   }

   @Override
   protected float getGlobalXRotation() {
      return -12.5F;
   }

   @Override
   protected float getGlobalYRotation() {
      return 22.5F;
   }

   @Override
   protected void drawAnimation(GuiGraphicsExtractor graphics, int x, int y) {
      this.blockElement(AllBlocks.ENCASED_FAN.getDefaultState()).at(x, y).rotateBlock(0.0, 180.0, 0.0).scale(24.0).submit(graphics);
      this.blockElement(AllPartialModels.ENCASED_FAN_INNER).at(x, y).rotateBlock(180.0, 0.0, getCurrentAngle() * 16.0F).scale(24.0).submit(graphics);
      if (!this.type.equals("create:haunting") && !this.type.equals("create:fan_smoking")) {
         GuiGameElement.submitFluidBox(
            new FluidStack(this.type.equals("create:splashing") ? Fluids.WATER : Fluids.LAVA, 1000),
            0.0F,
            0.0F,
            2.0F,
            1.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            1.0F,
            1.0F,
            1.0F
         );
      } else {
         this.blockElement((this.type.equals("create:haunting") ? Blocks.SOUL_FIRE : Blocks.FIRE).defaultBlockState())
            .at(x, y)
            .atLocal(0.0F, 0.0F, 2.0F)
            .scale(24.0)
            .submit(graphics);
      }
   }
}
