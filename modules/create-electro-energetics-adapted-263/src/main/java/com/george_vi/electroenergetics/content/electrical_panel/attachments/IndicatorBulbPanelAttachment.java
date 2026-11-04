package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public class IndicatorBulbPanelAttachment extends PanelAttachment {

    public DyeColor color = DyeColor.WHITE;
    public LerpedFloat smoothLight = LerpedFloat.linear();
    public float light = 0f;

    public IndicatorBulbPanelAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        smoothLight.tickChaser();
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.bridge(nodes[0], nodes[1], ElectricalProperties.resistor(1000));
    }

    @Override
    public void postTick(SimulationResults results) {
        float newLight = (float) Math.min(1, Math.abs(results.getVoltageAt(nodes[0], nodes[1]) / 70));

        if (Math.abs(light - newLight) > 0.02) {
            light = newLight;
            sendData();
        }
    }

    @Override
    public InteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof WireSpoolItem ||
                stack.getItem() instanceof EmptySpoolItem ||
                stack.is(AllItems.WRENCH))
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (stack.getItem() instanceof DyeItem di) {
            color = stack.getOrDefault(net.minecraft.core.component.DataComponents.DYE, net.minecraft.world.item.DyeColor.WHITE);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS);
            sendData();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        color = DyeColor.byName(tag.getStringOr("Color", ""), DyeColor.WHITE);
        light = tag.getFloatOr("Light", 0f);

        if (clientPacket)
            smoothLight.chase(light, 0.75, LerpedFloat.Chaser.EXP);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        if (light != 0)
            tag.putFloat("Light", light);
        if (color != null)
            tag.putString("Color", color.getSerializedName());
    }

    public boolean miniature() {
        return slot.fullWidth == 14;
    }
}
