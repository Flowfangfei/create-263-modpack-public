package com.yision.fluidlogistics.mixin.client;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.yision.fluidlogistics.api.packager.PackageResourceCrafting;
import com.yision.fluidlogistics.api.packager.PackageResourceCraftingData;
import com.yision.fluidlogistics.api.packager.PackageResourceDisplay;
import com.yision.fluidlogistics.api.packager.PackageResources;
import com.yision.fluidlogistics.api.packager.client.StockKeeperAmountRenderer;
import com.yision.fluidlogistics.content.logistics.packageResource.client.PackageResourceClientRegistry;
import com.yision.fluidlogistics.content.processing.snowGolemFreezer.SnowGolemFreezerRenderer;
import com.yision.fluidlogistics.content.processing.snowGolemFreezer.SnowGolemFreezerBlockEntity;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.data.Couple;
import net.createmod.catnip.api.data.Pair;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.ModelBuffer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin {

	@Shadow(remap = false)
	public List<BigItemStack> itemsToOrder;

	@Shadow(remap = false)
	public List<CraftableBigItemStack> recipesToOrder;

	@Shadow(remap = false)
	public List<List<BigItemStack>> displayedItems;

	@Shadow(remap = false)
	StockTickerBlockEntity blockEntity;

	@Shadow(remap = false)
	private boolean canRequestCraftingPackage;

	@Shadow(remap = false)
	private BigItemStack selectedFor(ItemStack stack) { throw new AssertionError("Mixin shadow"); }

	@Shadow(remap = false)
	private Pair<Integer, List<List<BigItemStack>>> maxCraftable(CraftableBigItemStack cbis,
		InventorySummary summary,
		java.util.function.Function<ItemStack, Integer> countModifier, int newTypeLimit) { throw new AssertionError("Mixin shadow"); }

    @Shadow private List<BigItemStack> flattenedItems;
    @Shadow private int hoveredStockIndex(double x, double y) { throw new AssertionError("Mixin shadow"); }
    @Shadow private int hoveredOrderIndex(double x, double y) { throw new AssertionError("Mixin shadow"); }
    @Shadow private int hoveredRecipeIndex(double x, double y) { throw new AssertionError("Mixin shadow"); }
    @Shadow private int maxFirstRow() { throw new AssertionError("Mixin shadow"); }

    @Unique
    private Couple<Integer> getHoveredSlot(int mouseX, int mouseY) {
        int index = hoveredOrderIndex(mouseX, mouseY);
        if (index >= 0 && index < itemsToOrder.size()) return Couple.create(-1, index);
        index = hoveredRecipeIndex(mouseX, mouseY);
        if (index >= 0 && index < recipesToOrder.size()) return Couple.create(-2, index);
        index = hoveredStockIndex(mouseX, mouseY);
        if (index >= 0 && index < flattenedItems.size()) {
            BigItemStack entry = flattenedItems.get(index);
            for (int group = 0; group < displayedItems.size(); group++) {
                int slot = displayedItems.get(group).indexOf(entry);
                if (slot >= 0) return Couple.create(group, slot);
            }
        }
        return Couple.create(-1, -1);
    }

    @Unique
    private BigItemStack fluidlogistics$entryAt(int x, int y) {
        var slot = getHoveredSlot(x, y);
        if (fluidlogistics$isNoHoveredSlot(slot)) return null;
        if (slot.getFirst() == -1) return itemsToOrder.get(slot.getSecond());
        if (slot.getFirst() == -2) return recipesToOrder.get(slot.getSecond());
        return displayedItems.get(slot.getFirst()).get(slot.getSecond());
    }

    @WrapOperation(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void fluidlogistics$resourceCount(GuiGraphicsExtractor graphics, Font font, ItemStack stack,
        int x, int y, String text, Operation<Void> original) {
        BigItemStack entry = fluidlogistics$entryAt(x + 8, y + 8);
        var renderer = PackageResourceClientRegistry.stockKeeperRendererFor(stack);
        if (renderer != null && entry != null && entry.count > 0) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y);
            try { renderer.render(graphics, entry.count); }
            finally { graphics.pose().popMatrix(); }
            return;
        }
        String amount = entry == null ? text : PackageResources.formatAmount(entry.stack, entry.count,
            PackageResourceDisplay.Format.COMPACT).orElse(text);
        original.call(graphics, font, stack, x, y, amount);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void fluidlogistics$warehouseCooler(GuiGraphicsExtractor graphics, float partialTicks,
        int mouseX, int mouseY, CallbackInfo ci) {
        Level level = blockEntity.getLevel();
        if (level == null) return;
        for (var direction : net.createmod.catnip.api.data.Iterate.horizontalDirections) {
            var keeper = level.getBlockEntity(blockEntity.getBlockPos().relative(direction));
            if (keeper instanceof SnowGolemFreezerBlockEntity cooler) {
                var screen = (StockKeeperRequestScreen) (Object) this;
                net.createmod.catnip.api.client.gui.element.GuiGameElement.of(cooler)
                    .at(screen.getLeftPos() - 32, screen.getTopPos() + 70).scale(32)
                    .rotate(-22.5, 45, 0).submit(graphics);
                break;
            }
        }
    }

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
	private void fluidlogistics$handleDirectResourceClick(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick,
		CallbackInfoReturnable<Boolean> cir) {
        double mouseX = event.x(), mouseY = event.y();
        int button = event.button();
		boolean lmb = button == 0;
		boolean rmb = button == 1;
		if (!lmb && !rmb) {
			return;
		}

		Couple<Integer> hoveredSlot = getHoveredSlot((int) mouseX, (int) mouseY);
		if (fluidlogistics$isNoHoveredSlot(hoveredSlot) || hoveredSlot.getFirst() == -2) {
			return;
		}

		boolean orderClicked = hoveredSlot.getFirst() == -1;
		BigItemStack entry = orderClicked ? itemsToOrder.get(hoveredSlot.getSecond())
			: displayedItems.get(hoveredSlot.getFirst()).get(hoveredSlot.getSecond());
		if (!fluidlogistics$changeDirectResourceOrder(
			entry, orderClicked, !(rmb || orderClicked), entry.count)) {
			return;
		}

		cir.setReturnValue(true);
	}

	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true, remap = false)
	private void fluidlogistics$handleDirectResourceScroll(double mouseX, double mouseY, double scrollX, double scrollY,
		CallbackInfoReturnable<Boolean> cir) {
		Couple<Integer> hoveredSlot = getHoveredSlot((int) mouseX, (int) mouseY);
		if (fluidlogistics$isNoHoveredSlot(hoveredSlot)) {
			return;
		}
		if (hoveredSlot.getFirst() >= 0 && !com.yision.fluidlogistics.client.gui.GuiKeys.shift() && maxFirstRow() != 0) {
			return;
		}
		if (hoveredSlot.getFirst() == -2) {
			return;
		}

		boolean orderClicked = hoveredSlot.getFirst() == -1;
		BigItemStack entry = orderClicked ? itemsToOrder.get(hoveredSlot.getSecond())
			: displayedItems.get(hoveredSlot.getFirst()).get(hoveredSlot.getSecond());
		int steps = Mth.ceil(Math.abs(scrollY));
		if (steps <= 0) {
			if (PackageResources.findType(entry.stack).isPresent()) {
				cir.setReturnValue(true);
			}
			return;
		}

		boolean forward = scrollY > 0;
		int maxAvailable = blockEntity.getLastClientsideStockSnapshotAsSummary().getCountOf(entry.stack);
		if (!fluidlogistics$changeDirectResourceOrder(
			entry, orderClicked, forward, maxAvailable, steps)) {
			return;
		}
		cir.setReturnValue(true);
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
	private void fluidlogistics$handleCustomResourceRecipeClick(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick,
		CallbackInfoReturnable<Boolean> cir) {
        double mouseX = event.x(), mouseY = event.y();
        int button = event.button();
		if (button != 0 && button != 1) {
			return;
		}

		Couple<Integer> hoveredSlot = getHoveredSlot((int) mouseX, (int) mouseY);
		if (hoveredSlot.getFirst() != -2) {
			return;
		}

		CraftableBigItemStack cbis = recipesToOrder.get(hoveredSlot.getSecond());
		if (!fluidlogistics$isCustomResourceCraftable(cbis)) {
			return;
		}

		int delta = fluidlogistics$getResourceRecipeStepAmount(cbis.stack);
		if (button == 1) {
			delta = -delta;
		}

		fluidlogistics$handleCustomResourceCraftableRequest(cbis, delta);
		cir.setReturnValue(true);
	}

	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true, remap = false)
	private void fluidlogistics$handleCustomResourceRecipeScroll(double mouseX, double mouseY, double scrollX,
		double scrollY,
		CallbackInfoReturnable<Boolean> cir) {
		Couple<Integer> hoveredSlot = getHoveredSlot((int) mouseX, (int) mouseY);
		if (hoveredSlot.getFirst() != -2) {
			return;
		}

		CraftableBigItemStack cbis = recipesToOrder.get(hoveredSlot.getSecond());
		if (!fluidlogistics$isCustomResourceCraftable(cbis)) {
			return;
		}

		int steps = Mth.ceil(Math.abs(scrollY));
		if (steps <= 0) {
			cir.setReturnValue(true);
			return;
		}

		int delta = fluidlogistics$getResourceRecipeStepAmount(cbis.stack) * steps;
		if (scrollY < 0) {
			delta = -delta;
		}

		fluidlogistics$handleCustomResourceCraftableRequest(cbis, delta);
		cir.setReturnValue(true);
	}

	@Inject(method = "requestCraftable", at = @At("HEAD"), cancellable = true, remap = false)
	private void fluidlogistics$requestCustomResourceRecipe(CraftableBigItemStack cbis, int requestedDifference,
		CallbackInfo ci) {
		if (!fluidlogistics$hasCustomRecipeData(cbis)) {
			return;
		}

		fluidlogistics$handleCustomResourceCraftableRequest(cbis, requestedDifference);
		ci.cancel();
	}

	@Inject(method = "updateCraftableAmounts", at = @At("HEAD"), cancellable = true, remap = false)
	private void fluidlogistics$handleCustomRecipeAmountUpdates(CallbackInfo ci) {
		for (CraftableBigItemStack cbis : recipesToOrder) {
			if (PackageResourceCrafting.has(cbis)) {
				fluidlogistics$updateCraftableAmountsWithCustomEntries();
				ci.cancel();
				return;
			}
		}
	}

    @WrapOperation(method = "renderForeground", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setComponentTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V"))
    private void fluidlogistics$resourceTooltip(GuiGraphicsExtractor graphics, Font font,
        List<Component> nativeLines, int mouseX, int mouseY, Operation<Void> original) {
        BigItemStack entry = fluidlogistics$entryAt(mouseX, mouseY);
        if (entry == null) { original.call(graphics, font, nativeLines, mouseX, mouseY); return; }
        List<Component> lines = new ArrayList<>(nativeLines);
        PackageResources.nameOf(entry.stack).ifPresent(name -> {
            if (!lines.isEmpty()) lines.set(0, name);
        });
        var amount = PackageResources.formatAmount(entry.stack, entry.count, PackageResourceDisplay.Format.PRECISE);
        original.call(graphics, font, amount.map(value -> fluidlogistics$withResourceAmount(lines, value)).orElse(lines), mouseX, mouseY);
    }

	@Unique
	private void fluidlogistics$updateCraftableAmountsWithCustomEntries() {
		InventorySummary usedItems = new InventorySummary();
		InventorySummary availableItems = new InventorySummary();

		for (BigItemStack ordered : itemsToOrder) {
			availableItems.add(ordered.stack, ordered.count);
		}

		boolean hasCustomEntries = false;

		for (CraftableBigItemStack cbis : recipesToOrder) {
			PackageResourceCraftingData data = PackageResourceCrafting.get(cbis).orElse(null);
			if (data != null) {
				hasCustomEntries = true;

				int outputCount = data.outputCount();
				if (outputCount <= 0) {
					cbis.count = 0;
					continue;
				}

				int maxSets = fluidlogistics$getCustomCraftableSets(
					availableItems, usedItems, data.requirements());
				cbis.count = Math.min(cbis.count, maxSets * outputCount);

				int committedSets = cbis.count / outputCount;
				for (BigItemStack requirement : data.requirements()) {
					usedItems.add(requirement.stack, requirement.count * committedSets);
				}
				continue;
			}

			Pair<Integer, List<List<BigItemStack>>> craftingResult = maxCraftable(cbis, availableItems,
				stack -> -usedItems.getCountOf(stack), -1);
			int maxCraftable = craftingResult.getFirst();
			List<List<BigItemStack>> validEntriesByIngredient = craftingResult.getSecond();
			int outputCount = cbis.getOutputCount(blockEntity.getLevel());

			cbis.count = Math.min(cbis.count, maxCraftable);

			for (List<BigItemStack> list : validEntriesByIngredient) {
				int remaining = cbis.count / outputCount;
				for (BigItemStack entry : list) {
					if (remaining <= 0) {
						break;
					}
					usedItems.add(entry.stack, Math.min(remaining, entry.count));
					remaining -= entry.count;
				}
			}
		}

		canRequestCraftingPackage = false;
		if (hasCustomEntries) {
			return;
		}

		for (BigItemStack ordered : itemsToOrder) {
			if (usedItems.getCountOf(ordered.stack) != ordered.count) {
				return;
			}
		}
		canRequestCraftingPackage = true;
	}

	@Unique
	private static int fluidlogistics$getCustomCraftableSets(InventorySummary availableItems,
		List<BigItemStack> existingOrders,
		List<BigItemStack> requirements) {
		int craftableSets = Integer.MAX_VALUE;
		for (BigItemStack requirement : requirements) {
			int orderedCount = fluidlogistics$getMatchingCount(existingOrders, requirement.stack);
			int available = availableItems.getCountOf(requirement.stack) - orderedCount;
			craftableSets = Math.min(craftableSets, available / requirement.count);
		}
		return craftableSets == Integer.MAX_VALUE ? 0 : Math.max(0, craftableSets);
	}

	@Unique
	private static int fluidlogistics$getCustomCraftableSets(InventorySummary availableItems,
		InventorySummary usedItems,
		List<BigItemStack> requirements) {
		int craftableSets = Integer.MAX_VALUE;
		for (BigItemStack requirement : requirements) {
			int available = availableItems.getCountOf(requirement.stack) - usedItems.getCountOf(requirement.stack);
			craftableSets = Math.min(craftableSets, available / requirement.count);
		}
		return craftableSets == Integer.MAX_VALUE ? 0 : Math.max(0, craftableSets);
	}

	@Unique
	private static boolean fluidlogistics$canFitCustomRecipe(List<BigItemStack> existingOrders,
		List<BigItemStack> requirements) {
		int totalTypes = existingOrders.size();
		List<ItemStack> newTypes = new java.util.ArrayList<>();

		for (BigItemStack requirement : requirements) {
			if (fluidlogistics$hasMatchingStack(existingOrders, requirement.stack)
				|| fluidlogistics$hasMatchingStack(newTypes, requirement.stack)) {
				continue;
			}
			newTypes.add(requirement.stack);
			totalTypes++;
			if (totalTypes > 9) {
				return false;
			}
		}

		return true;
	}

	@Unique
	private static int fluidlogistics$getMatchingCount(List<BigItemStack> stacks, ItemStack target) {
		int total = 0;
		for (BigItemStack entry : stacks) {
			if (ItemStack.isSameItemSameComponents(entry.stack, target)) {
				total += entry.count;
			}
		}
		return total;
	}

	@Unique
	private static boolean fluidlogistics$hasMatchingStack(List<?> stacks, ItemStack target) {
		for (Object entry : stacks) {
			ItemStack stack = entry instanceof BigItemStack bigItemStack ? bigItemStack.stack : (ItemStack) entry;
			if (ItemStack.isSameItemSameComponents(stack, target)) {
				return true;
			}
		}
		return false;
	}

	@Unique
	private boolean fluidlogistics$hasCustomRecipeData(CraftableBigItemStack cbis) {
		return PackageResourceCrafting.has(cbis);
	}

	@Unique
	private boolean fluidlogistics$isCustomResourceCraftable(CraftableBigItemStack cbis) {
		return fluidlogistics$hasCustomRecipeData(cbis)
			&& PackageResources.findType(cbis.stack).isPresent();
	}

	@Unique
	private static boolean fluidlogistics$isNoHoveredSlot(Couple<Integer> hoveredSlot) {
		return hoveredSlot.getFirst() == -1 && hoveredSlot.getSecond() == -1;
	}

	@Unique
	private static List<Component> fluidlogistics$withResourceAmount(List<Component> nativeLines, String amount) {
		if (nativeLines.isEmpty()) {
			return nativeLines;
		}
		List<Component> lines = new ArrayList<>(nativeLines);
		lines.add(1, Component.literal(amount).withStyle(ChatFormatting.GRAY));
		return lines;
	}

	@Unique
	private boolean fluidlogistics$changeDirectResourceOrder(BigItemStack entry, boolean orderClicked, boolean forward,
		int maxAvailable) {
		return fluidlogistics$changeDirectResourceOrder(entry, orderClicked, forward, maxAvailable, 1);
	}

	@Unique
	private boolean fluidlogistics$changeDirectResourceOrder(BigItemStack entry, boolean orderClicked, boolean forward,
		int maxAvailable, int steps) {
		BigItemStack existingOrder = orderClicked ? entry : selectedFor(entry.stack);
		var adjusted = PackageResources.adjustAmount(entry.stack, new PackageResourceDisplay.Adjustment(
			existingOrder == null ? 0 : existingOrder.count,
			forward,
			com.yision.fluidlogistics.client.gui.GuiKeys.shift(),
			com.yision.fluidlogistics.client.gui.GuiKeys.control(),
			0,
			Math.max(0, maxAvailable),
			steps,
			orderClicked
				? PackageResourceDisplay.Interaction.STOCK_KEEPER_ORDER
				: PackageResourceDisplay.Interaction.STOCK_KEEPER_INVENTORY));
		if (adjusted.isEmpty()) {
			return false;
		}
		int newAmount = adjusted.getAsInt();
		if (existingOrder == null) {
			if (!forward || itemsToOrder.size() >= 9 || newAmount <= 0) {
				return true;
			}
			existingOrder = new BigItemStack(entry.stack.copyWithCount(1), 0);
			itemsToOrder.add(existingOrder);
		}
		if (newAmount <= 0) {
			itemsToOrder.remove(existingOrder);
			return true;
		}

		existingOrder.count = newAmount;
		return true;
	}

	@Unique
	private int fluidlogistics$getResourceRecipeStepAmount(ItemStack stack) {
		return PackageResources.adjustAmount(stack, new PackageResourceDisplay.Adjustment(
			0,
			true,
			com.yision.fluidlogistics.client.gui.GuiKeys.shift(),
			com.yision.fluidlogistics.client.gui.GuiKeys.control(),
			0,
			BigItemStack.INF,
			1,
			PackageResourceDisplay.Interaction.STOCK_KEEPER_INVENTORY))
			.orElse(1);
	}

	@Unique
	private void fluidlogistics$handleCustomResourceCraftableRequest(CraftableBigItemStack cbis,
		int requestedDifference) {
		PackageResourceCraftingData data = PackageResourceCrafting.get(cbis).orElse(null);
		if (data == null) {
			return;
		}
		int outputCount = data.outputCount();
		if (outputCount <= 0) {
			return;
		}

		boolean takeOrdersAway = requestedDifference < 0;
		if (takeOrdersAway) {
			requestedDifference = Math.max(-cbis.count, requestedDifference);
		}
		if (requestedDifference == 0) {
			return;
		}

		int requestedSets = Mth.ceil(Math.abs(requestedDifference) / (float) outputCount);
		int applicableSets;

		if (takeOrdersAway) {
			applicableSets = Math.min(requestedSets, cbis.count / outputCount);
		} else {
			InventorySummary availableItems = blockEntity.getLastClientsideStockSnapshotAsSummary();
			if (availableItems == null) {
				return;
			}

			if (!fluidlogistics$canFitCustomRecipe(itemsToOrder, data.requirements())) {
				return;
			}

			applicableSets = fluidlogistics$getCustomCraftableSets(
				availableItems, itemsToOrder, data.requirements());
			applicableSets = Math.min(requestedSets, applicableSets);
		}

		if (applicableSets <= 0) {
			return;
		}

		int amountDelta = applicableSets * outputCount;
		cbis.count += takeOrdersAway ? -amountDelta : amountDelta;

		for (BigItemStack requirement : data.requirements()) {
			int delta = requirement.count * applicableSets;
			BigItemStack existingOrder = selectedFor(requirement.stack);

			if (takeOrdersAway) {
				if (existingOrder == null) {
					continue;
				}
				existingOrder.count -= delta;
				if (existingOrder.count <= 0) {
					itemsToOrder.remove(existingOrder);
				}
				continue;
			}

			if (existingOrder == null) {
				existingOrder = new BigItemStack(requirement.stack.copyWithCount(1), 0);
				itemsToOrder.add(existingOrder);
			}
			existingOrder.count += delta;
		}

		if (cbis.count <= 0) {
			recipesToOrder.remove(cbis);
		}

		fluidlogistics$updateCraftableAmountsWithCustomEntries();
	}
}
