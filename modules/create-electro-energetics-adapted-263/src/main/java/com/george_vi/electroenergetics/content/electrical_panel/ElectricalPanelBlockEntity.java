package com.george_vi.electroenergetics.content.electrical_panel;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachmentType;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockEntityItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ElectricalPanelBlockEntity extends SmartBlockEntity implements SpecialBlockEntityItemRequirement {

    // This exists purely to mirror the device's data, or for rendering on the client side.
    // The device actually owns those.
    public PanelAttachment[] beAttachments = new PanelAttachment[ElectricalPanelSlot.values().length];

    public ElectricalPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        com.george_vi.electroenergetics.foundation.ClientExecution.run(() -> this::tickAudio);
    }

    @Environment(EnvType.CLIENT)
    protected void tickAudio() {
        for (PanelAttachment attachment : beAttachments)
            if (attachment != null)
                attachment.tickClient(this);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {

    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);
        HolderLookup.Provider registries = input.lookup();

        PanelAttachment[] attachments = getAttachments();
        readAttachments(tag, attachments, worldPosition, level, registries, clientPacket, getBlockState().getValue(ElectricalPanelBlock.FACING));
        if (Arrays.stream(attachments).allMatch(Objects::isNull)) {
            Arrays.fill(attachments, null);
            readLegacyAttachments(tag, attachments, worldPosition, level, registries, getBlockState().getValue(ElectricalPanelBlock.FACING));
        }

    }

    static void readLegacyAttachments(CompoundTag tag, PanelAttachment[] toFill, BlockPos worldPosition, Level level,
                                      HolderLookup.Provider registries, Direction facing) {

        ElectricalPanelLayoutType layoutType = ElectricalPanelLayoutType.byIdOrNone(tag.getStringOr("Layout", ""));

        for (int i = 0; i < layoutType.slots.length; i++) {
            CompoundTag attachmentTag = tag.getCompoundOrEmpty("Attachment" + i);
            if (attachmentTag.isEmpty())
                continue;

            String id = attachmentTag.getStringOr("ID", "");

            Identifier attachmentId = Identifier.tryParse(id);

            if (attachmentId == null)
                continue;

            PanelAttachmentType type = CEERegistries.PANEL_ATTACHMENT_TYPE.getValue(com.george_vi.electroenergetics.events.RegistryRemaps.canonical(attachmentId));

            if (type == null)
                continue;

            if (toFill[layoutType.slots[i].ordinal()] == null) {
                toFill[layoutType.slots[i].ordinal()] = type.createNew(worldPosition, type.mode.getNodesFor(worldPosition, layoutType.slots[i]),
                        level, layoutType.slots[i], facing, registries);
                toFill[layoutType.slots[i].ordinal()].read(attachmentTag.getCompoundOrEmpty("Data"), false, registries);
                toFill[layoutType.slots[i].ordinal()].label = attachmentTag.contains("Label") ? attachmentTag.getStringOr("Label", "") : null;
            }
        }
    }

    static void readAttachments(CompoundTag tag, PanelAttachment[] toFill, BlockPos worldPosition, Level level,
                                HolderLookup.Provider registries, boolean clientPacket, Direction facing) {

        for (int i = 0; i < toFill.length; i++) {
            CompoundTag attachmentTag = tag.getCompoundOrEmpty("AttachmentSlot" + i);
            if (attachmentTag.isEmpty()) {
                toFill[i] = null;
                continue;
            }

            String id = attachmentTag.getStringOr("ID", "");

            Identifier attachmentId = Identifier.tryParse(id);
            if (attachmentId == null) {
                toFill[i] = null;
                continue;
            }

            PanelAttachmentType type = CEERegistries.PANEL_ATTACHMENT_TYPE.getValue(com.george_vi.electroenergetics.events.RegistryRemaps.canonical(attachmentId));
            if (type == null) {
                toFill[i] = null;
                continue;
            }

            ElectricalPanelSlot slot = ElectricalPanelSlot.values()[i];

            if (toFill[i] == null) {
                toFill[i] = type.createNew(worldPosition, type.mode.getNodesFor(worldPosition, slot),
                        level, slot, facing, registries);
            }

            toFill[i].read(attachmentTag.getCompoundOrEmpty("Data"), clientPacket, registries);
            toFill[i].label = attachmentTag.contains("Label") ? attachmentTag.getStringOr("Label", "") : null;
        }
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();
        HolderLookup.Provider registries = level != null ? level.registryAccess() : net.minecraft.core.RegistryAccess.EMPTY;

        PanelAttachment[] attachments = getAttachments();
        for (int i = 0; i < attachments.length; i++) {
            if (attachments[i] == null)
                continue;

            Identifier id = CEERegistries.PANEL_ATTACHMENT_TYPE.getKey(attachments[i].type);
            if (id == null)
                continue;

            CompoundTag attachmentTag = new CompoundTag();
            tag.put("AttachmentSlot" + i, attachmentTag);

            attachmentTag.putString("ID", id.toString());

            CompoundTag dataTag = new CompoundTag();
            attachmentTag.put("Data", dataTag);
            attachments[i].write(dataTag, clientPacket, registries);
            if (attachments[i].label != null)
                attachmentTag.putString("Label", attachments[i].label);
        }

        NbtView.write(output, tag);
    }

    public void attachmentUpdate() {
        sendData();
        if (level instanceof ServerLevel sl)
            ((ElectricalDeviceBlock<?>)getBlockState().getBlock()).ensureNodesExist(sl, getBlockPos(), getBlockState());
    }

    public PanelAttachment[] getAttachments() {
        ElectricalPanelDevice device = deviceOrNull();
        if (device != null)
            return device.attachments;
        return beAttachments;
    }

    public void setAttachments(PanelAttachment[] attachments) {
        ElectricalPanelDevice device = deviceOrNull();
        if (device != null)
            device.attachments = attachments;
        this.beAttachments = attachments;
    }

    private ElectricalPanelDevice deviceOrNull() {
        if (level instanceof ServerLevel sl) {
            DevicesSavedData dsd = DevicesSavedData.load(sl);

            return dsd.getDevice(worldPosition, ElectricalPanelDevice.class);
        }

        return null;
    }

    /**
     * @return -1 if no attachment
     */
    @Nullable
    public ElectricalPanelSlot getHoveringAttachmentIndex(Vec3 localClickPos) {
        localClickPos = VecHelper.rotateCentered(localClickPos, getBlockState().getValue(ElectricalPanelBlock.FACING).toYRot() + 180, Direction.Axis.Y);

        PanelAttachment[] attachments = this.getAttachments();

        for (PanelAttachment attachment : attachments) {
            if (attachment == null)
                continue;

            if (attachment.slot.shape.minX < localClickPos.x && attachment.slot.shape.maxX > localClickPos.x &&
                    attachment.slot.shape.minY < localClickPos.y && attachment.slot.shape.maxY > localClickPos.y)
                return attachment.slot;
        }

        return null;
    }

    public Map<Integer, Vec3> getNodePositions() {
        PanelAttachment[] attachments = getAttachments();
        Map<Integer, Vec3> out = new HashMap<>();
        Direction facing = getBlockState().getValue(ElectricalPanelBlock.FACING);
        for (PanelAttachment attachment : attachments) {
            if (attachment == null)
                continue;
            for (InWorldNode node : attachment.nodes) {
                int id = node.id();
                out.put(id, ElectricalPanelNodeState.configurator.getNodePos(facing, id));
            }
        }

        return out;
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state) {
        ItemRequirement requiredItems = super.getRequiredItems(state);
        for (PanelAttachment attachment : getAttachments()) {
            if (attachment == null)
                continue;

            requiredItems = requiredItems.union(new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, attachment.getDrops()));
        }
        return requiredItems;
    }
}
