package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.client.*;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelClientTicker;
import com.george_vi.electroenergetics.content.fuse.FuseBlockItem;
import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickClientHandler;
import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickItem;
import com.george_vi.electroenergetics.content.railway_electrification.gauges.ClientTrainGaugeData;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSounds;
import com.george_vi.electroenergetics.content.wire.interaction.*;
import com.george_vi.electroenergetics.content.wire_spool.ChangeLengthWireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire_spool.WireApplyingBehaviour;
import com.george_vi.electroenergetics.events.network.CEENetwork;
import com.george_vi.electroenergetics.foundation.CEEHoldInteractionHandler;
import com.george_vi.electroenergetics.foundation.CEELang;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public final class CEEClientEvents {
    private CEEClientEvents() {}

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) return;
            WireApplyingBehaviour.tick();
            WireInteractionHandler.tick();
            OutlinesOnWireRenderer.OutlinerExt.tick();
            WireEffects.tick();
            CEEHoldInteractionHandler.tick();
            ElectricTrainSounds.tick();
            ClientTrainGaugeData.tick();
            LinemansStickClientHandler.tick();
            LinemansStickItem.tickPlayerRange(client.player);
            FuseBlockItem.tickClient();
            ElectricalPanelClientTicker.tick();
            ElectricPropertiesOverlay.INSTANCE.ticks++;
            WireInteractionHandler.preventUseOnBlockPacket = false;
            for (ClientNodeData data : WireRenderer.NODE_DATA.values()) data.tick();
        });
        UseItemCallback.EVENT.register(CEEClientEvents::interact);
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> interact(player, level, hand));
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR,
            CreateElectroEnergetics.rl("electric_properties_overlay"), ElectricPropertiesOverlay.INSTANCE::extractRenderState);
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (stack.is(CEETags.ELECTRICAL_PANEL_ATTACHMENT) && CEEConfigs.client().displayPanelTooltip.get())
                lines.add(CEELang.translateDirect("hint.placeable_in_electrical_panel"));
        });
    }

    public static boolean mouseScrolled(double delta) {
        return FuseBlockItem.mouseScrolled(delta) || ChangeLengthWireInteractionBehaviour.mouseScrolled(delta);
    }

    private static InteractionResult interact(Player player, Level level, InteractionHand hand) {
        if (!level.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (CEEHoldInteractionHandler.isInteracting()) {
            WireInteractionHandler.preventUseOnBlockPacket = true;
            return InteractionResult.FAIL;
        }
        if (WireApplyingBehaviour.targetingDetachedNode != null) {
            CEENetwork.NETWORK.sendToServer(new InteractDetachedNodePacket(WireApplyingBehaviour.targetingDetachedNode));
            WireInteractionHandler.preventUseOnBlockPacket = true;
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        WireInteractionBehaviour behaviour = CEERegistries.WIRE_INTERACTION_BEHAVIOUR.stream()
            .filter(candidate -> candidate.isActiveFor(stack, player)).findFirst().orElse(null);
        if (behaviour != null && behaviour.tryUseOnWire(level, player, hand)) {
            WireInteractionHandler.preventUseOnBlockPacket = true;
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
