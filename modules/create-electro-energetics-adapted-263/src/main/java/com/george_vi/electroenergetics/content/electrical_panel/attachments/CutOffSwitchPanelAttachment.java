package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.link.ElectricalPanelLink;
import com.george_vi.electroenergetics.content.electrical_panel.link.ElectricalPanelLinkable;
import com.george_vi.electroenergetics.content.wire_spool.EmptySpoolItem;
import com.george_vi.electroenergetics.content.wire_spool.WireSpoolItem;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.george_vi.electroenergetics.foundation.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class CutOffSwitchPanelAttachment extends PanelAttachment implements ElectricalPanelLink {
    public SwitchingBehaviour behaviour;
    public boolean isClosed;
    public Style style = Style.LEVER;
    public DyeColor color;

    public float leverAngle = 0;
    public float prevLeverAngle = 0;

    public CutOffSwitchPanelAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        prevLeverAngle = leverAngle;
        float target = switch (style) {
            case LEVER, DIAL -> isClosed ? 45 : -45;
            case BUTTON -> isClosed ? (miniature() ? 0.99f : 1.99f)/16f : 0f;
        };

        leverAngle = Mth.lerp(style == Style.DIAL ? 0.5f : 1f, leverAngle, target);
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        double r = behaviour.resistance();
        if (r < 1e+10d)
            bridges.bridge(nodes[0], nodes[1], ElectricalProperties.resistor(r));
    }

    @Override
    public void postTick(SimulationResults results) {
        double voltage = results.getVoltageAt(nodes[0], nodes[1]);
        this.behaviour.isClosed = isClosed;
        this.behaviour.postTick(voltage, getCenter(), level);
    }

    @Override
    public InteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof WireSpoolItem ||
                stack.getItem() instanceof EmptySpoolItem ||
                stack.is(AllItems.WRENCH))
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (stack.is(AllBlocks.REDSTONE_LINK.asItem())) {
            if (!level.isClientSide() && player instanceof ServerPlayer && player.mayBuild())
                openHandledScreen((net.minecraft.server.level.ServerPlayer) player);
            return InteractionResult.SUCCESS;
        }

        if (stack.getItem() == type.item.asItem()) {
            style = Style.values()[(style.ordinal() + 1) % Style.values().length];
            prevLeverAngle = 0;
            leverAngle = 0;
            sendData();
            return InteractionResult.SUCCESS;
        }

        if (stack.getItem() instanceof DyeItem di) {
            color = stack.getOrDefault(net.minecraft.core.component.DataComponents.DYE, net.minecraft.world.item.DyeColor.WHITE);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS);
            sendData();
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide()) {
            CEESoundEvents.playOnServer(level, pos, isClosed ? CEESoundEvents.CONTACT_OPEN.get() : CEESoundEvents.CONTACT_CLOSE.get(), 1f, 1f);
            isClosed ^= true;
            updateLinkState(-1, isClosed ? 15 : 0);
            sendData();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void initialize() {
        updateLinkState(-1);
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public BlockPos getLocation() {
        return pos;
    }

    @Override
    public void onRemoved(Player player) {
        super.onRemoved(player);
        if (!level.isClientSide())
            removeLinkState(-1);
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        if (!clientPacket)
            behaviour = new SwitchingBehaviour(tag.getCompoundOrEmpty("Behaviour"));
        color = DyeColor.byName(tag.getStringOr("Color", ""), null);
        isClosed = tag.getBooleanOr("IsClosed", false);
        style = Style.byId(tag.getStringOr("Style", ""));
        readLinkFrequencies(tag, registries);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        if (!clientPacket)
            tag.put("Behaviour", behaviour.write());
        if (isClosed)
            tag.putBoolean("IsClosed", true);
        if (style != Style.LEVER)
            tag.putString("Style", style.getSerializedName());
        if (color != null)
            tag.putString("Color", color.getSerializedName());
        writeLinkFrequencies(tag, registries);
    }

    public boolean miniature() {
        return slot.fullWidth == 14;
    }

    public ItemStack[] linkFrequencies = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};

    @Override
    public ItemStack[] getLinkFrequencies() {
        return linkFrequencies;
    }

    public ElectricalPanelLinkable[] linkables = new ElectricalPanelLinkable[] {new ElectricalPanelLinkable(this)};

    @Override
    public ElectricalPanelLinkable[] getLinkables() {
        return linkables;
    }

    public enum Style implements StringRepresentable {
        LEVER(CEEPartialModels.PANEL_ATTACHMENT_SMOL_CUT_OFF_SWITCH,
                CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH,
                CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_LEVER,
                CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_LEVER_DYEABLE),
        BUTTON(CEEPartialModels.PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH,
                CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH,
                CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON,
                CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON_DYEABLE),
        DIAL(CEEPartialModels.PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH,
                CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH,
                CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_DIAL,
                CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_DIAL_DYEABLE);

        public final PartialModel miniatureBody;
        public final PartialModel body;
        public final PartialModel dial;
        public final PartialModel dialDyeable;

        Style(PartialModel miniatureBody, PartialModel body, PartialModel dial, PartialModel dialDyeable) {
            this.miniatureBody = miniatureBody;
            this.body = body;
            this.dial = dial;
            this.dialDyeable = dialDyeable;
        }

        @Override
        public @NotNull String getSerializedName() {
            return Lang.asId(name());
        }

        public static Style byId(String id) {
            for (Style type : values())
                if (type.getSerializedName().equals(id))
                    return type;
            return LEVER;
        }
    }
}
