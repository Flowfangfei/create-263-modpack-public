package net.fxnt.fxntstorage.backpack;

import net.createmod.catnip.api.client.lang.FontHelper;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpack.client.menu.BackpackMenu;
import net.fxnt.fxntstorage.backpack.client.tooltip.BackpackTooltip;
import net.fxnt.fxntstorage.backpack.util.BackpackHelper;
import net.fxnt.fxntstorage.client.StorageTooltipText;
import net.fxnt.fxntstorage.init.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class BackpackItem extends BlockItem {
    private final Block block;

    public BackpackItem(Block block, Properties properties) {
        super(block, properties.useItemDescriptionPrefix().stacksTo(1).fireResistant()
                .component(ModDataComponents.BACKPACK_STACK_MULTIPLIER, ((BackpackBlock) block).getStackMultiplier()));
        this.block = block;
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        if (armorType != EquipmentSlot.CHEST) return false;
        if (stack.getItem() instanceof BackpackItem
                && entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof BackpackItem)
            return true; // Allow backpack swap
        if (FXNTStorage.CURIOS_LOADED) {
            return !BackpackHelper.isWearingBackpack((Player) entity);
        }
        return super.canEquip(stack, armorType, entity);
    }

    @Override
    public @NotNull net.minecraft.world.InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BackpackHelper.openBackpackFromInventory(serverPlayer, BackpackMenu.BackpackType.ITEM);
            return net.minecraft.world.InteractionResult.SUCCESS_SERVER;
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.CHEST;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> output, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pContext, display, output, pIsAdvanced);
        var client = net.minecraft.client.Minecraft.getInstance();
        boolean shift = client.hasShiftDown();
        boolean ctrl = client.hasControlDown();

        if (shift || !ctrl) {
            output.accept(Component.translatable("tooltip.fxntstorage.holdForDescription", shift ? "§fShift" : "§7Shift").withStyle(ChatFormatting.DARK_GRAY));
        }
        if (!shift || ctrl) {
            output.accept(Component.translatable("tooltip.fxntstorage.holdForContents", ctrl ? "§fCtrl" : "§7Ctrl").withStyle(ChatFormatting.DARK_GRAY));
        }
        if (!shift || ctrl) return;

        String translationKey = "tooltip.fxntstorage.backpack";
        int placeholder = ((BackpackBlock) block).stackMultiplier;
        output.accept(Component.empty());
        StorageTooltipText.format(Component.translatable(translationKey + ".summary"), FontHelper.Palette.STANDARD_CREATE).forEach(output);
        output.accept(Component.empty());
        for (int i = 1; i < 10; i++) {
            if (!net.minecraft.locale.Language.getInstance().has(translationKey + ".condition" + i)) break;
            StorageTooltipText.format(Component.translatable(translationKey + ".condition" + i), FontHelper.Palette.ALL_GRAY).forEach(output);
            StorageTooltipText.format(Component.translatable(translationKey + ".behaviour" + i, placeholder),
                    FontHelper.Palette.STANDARD_CREATE.primary(), FontHelper.Palette.STANDARD_CREATE.highlight(), 1).forEach(output);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        if (net.minecraft.client.Minecraft.getInstance().hasControlDown() && !net.minecraft.client.Minecraft.getInstance().hasShiftDown()) {
            return Optional.of(new BackpackTooltip(pStack));
        }
        return Optional.empty();
    }
}
