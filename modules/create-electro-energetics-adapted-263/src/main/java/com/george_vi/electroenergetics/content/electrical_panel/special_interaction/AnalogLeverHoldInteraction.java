package com.george_vi.electroenergetics.content.electrical_panel.special_interaction;

import com.george_vi.electroenergetics.client.ElectricPropertiesOverlay;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.foundation.HoldInteractionBehavior;
import com.zurrtum.create.AllSoundEvents;
import com.george_vi.electroenergetics.events.network.CEENetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class AnalogLeverHoldInteraction implements HoldInteractionBehavior {
    public final BlockPos pos;
    public final int panelSlot;
    public int redstoneSignal;
    public float analogSignal;

    public AnalogLeverHoldInteraction(BlockPos pos, int panelSlot, int redstoneSignal) {
        this.pos = pos;
        this.panelSlot = panelSlot;
        this.redstoneSignal = redstoneSignal;
        this.analogSignal = redstoneSignal;
    }

    @Override
    public void release() {
        ElectricPropertiesOverlay.INSTANCE.removeAnalogLever();
        CEENetwork.NETWORK.sendToServer(new SetPanelAttachmentOptionsPacket(pos, panelSlot, SetPanelAttachmentOptionsPacket.HOLD_STATUS, false));
    }

    public boolean ticked = false;

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null)
            return;

        if (!ticked) {
            CEENetwork.NETWORK.sendToServer(new SetPanelAttachmentOptionsPacket(pos, panelSlot, SetPanelAttachmentOptionsPacket.HOLD_STATUS, true));
            ticked = true;
        }

        int prevRedstoneSignal = redstoneSignal;
        redstoneSignal = Math.round(analogSignal);
        redstoneSignal = Mth.clamp(redstoneSignal, 0, 15);
        if (prevRedstoneSignal != redstoneSignal) {
            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    AllSoundEvents.SCROLL_VALUE.getMainEvent(), SoundSource.PLAYERS,
                    0.25f, 1f, false);
            CEENetwork.NETWORK.sendToServer(new AnalogPanelAttachmentChangeStatePacket(pos, panelSlot, (byte) redstoneSignal));
        }

        ElectricPropertiesOverlay.INSTANCE.setAnalogLever(redstoneSignal);
    }

    @Override
    public void onMouseMove(double y, double x) {
        analogSignal = Mth.clamp(analogSignal + (float) y * -0.05f, 0, 15);
    }

    @Override
    public boolean isStillActive() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return false;
        double range = mc.player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;

        if (mc.player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) > range * range)
            return false;

        if (mc.level != null && mc.level.getBlockEntity(pos) instanceof ElectricalPanelBlockEntity be) {
            return be.getAttachments()[panelSlot] instanceof AnalogLeverPanelAttachment;
        }
        return false;
    }
}
