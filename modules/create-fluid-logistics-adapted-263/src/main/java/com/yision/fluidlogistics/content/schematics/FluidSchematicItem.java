package com.yision.fluidlogistics.content.schematics;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.schematics.SchematicItem;
import com.yision.fluidlogistics.registry.AllItems;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class FluidSchematicItem extends SchematicItem {

	public FluidSchematicItem(Properties properties) {
		super(properties);
	}

	@OnlyIn(Dist.CLIENT)
	protected void displayBlueprintScreen() {
		com.yision.fluidlogistics.client.FluidLogisticsClientAccess.openBlueprintScreen();
	}

    private boolean openBlueprint(Level level,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand) {
        if(!player.isShiftKeyDown() || hand!=net.minecraft.world.InteractionHand.MAIN_HAND || !player.getItemInHand(hand).has(AllDataComponents.SCHEMATIC_FILE)) return false;
        if(level.isClientSide()) displayBlueprintScreen();
        return true;
    }
    @Override public net.minecraft.world.InteractionResult use(Level level,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand) {
        return openBlueprint(level,player,hand)?net.minecraft.world.InteractionResult.SUCCESS:super.use(level,player,hand);
    }
    @Override public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        return context.getPlayer()!=null && openBlueprint(context.getLevel(),context.getPlayer(),context.getHand())
            ?net.minecraft.world.InteractionResult.SUCCESS:super.useOn(context);
    }

	public static ItemStack create(Level level, String schematic, String owner) {
		ItemStack blueprint = AllItems.FLUID_SCHEMATIC.asStack();
		blueprint.set(AllDataComponents.SCHEMATIC_DEPLOYED, false);
		blueprint.set(AllDataComponents.SCHEMATIC_OWNER, owner);
		blueprint.set(AllDataComponents.SCHEMATIC_FILE, schematic);
		blueprint.set(AllDataComponents.SCHEMATIC_ANCHOR, BlockPos.ZERO);
		blueprint.set(AllDataComponents.SCHEMATIC_ROTATION, Rotation.NONE);
		blueprint.set(AllDataComponents.SCHEMATIC_MIRROR, Mirror.NONE);
		writeSize(level, blueprint);
		return blueprint;
	}
}
