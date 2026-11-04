package dev.khloeleclair.create.additionallogistics.common.content.logistics.cashRegister;

import dev.khloeleclair.create.additionallogistics.common.CALLang;
import dev.khloeleclair.create.additionallogistics.common.registries.CALDataComponents;
import dev.khloeleclair.create.additionallogistics.common.registries.CALPackets;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class SalesLedgerItem extends Item {

    public SalesLedgerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        var data = stack.get(CALDataComponents.SALES_HISTORY);
        if (data == null)
            return;

        tooltipComponents.accept(
                CALLang.translate(
                        "sales.entries",
                        CALLang.number(data.saleCount()).style(ChatFormatting.GOLD)
                ).style(ChatFormatting.GRAY).component()
        );

        var cmp = data.getTimeRange(ChatFormatting.AQUA);
        if (cmp != null)
            tooltipComponents.accept(cmp.withStyle(ChatFormatting.GRAY));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 1000;
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return getDamage(stack) != 0;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    @Override
    public int getDamage(ItemStack stack) {
        var history = stack.get(CALDataComponents.SALES_HISTORY);
        return history == null ? 0 : history.saleCount();
    }

    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null)
            return InteractionResult.PASS;
        return use(context.getLevel(), context.getPlayer(), context.getHand());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (!player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            if (!level.isClientSide() && player instanceof ServerPlayer sp)
                CALPackets.OpenSalesLedgerScreen.create(heldItem).ifPresent(p -> p.send(sp));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
