package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.content.electrical_panel.*;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PanelAttachment {
    public InWorldNode[] nodes;
    public BlockPos pos;
    public Level level;
    public ElectricalPanelSlot slot;
    public Direction panelFacing;
    public final PanelAttachmentType type;
    public String label;

    public PanelAttachment(PanelAttachmentType type) {
        this.type = type;
    }

    /**
     * Called on the logical client only when the position is loaded. Ticked by the BE.
     */
    @Environment(EnvType.CLIENT)
    public void tickClient(ElectricalPanelBlockEntity be) {

    }

    /**
     * Called on the logical server only. Ticked by the device.
     */
    public abstract void preTick(BridgeCollector bridges);

    /**
     * Called on the logical server only. Ticked by the device.
     */
    public abstract void postTick(SimulationResults results);

    public void read(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {

    }

    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {

    }

    /**
     * Called both on the logical client and server.
     */
    public void onInserted(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {

    }

    /**
     * Called both on the logical client and server.
     * @return null if nothing changed (doesn't send BE data), otherwise the interaction result.
     */
    public InteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return null;
    }

    /**
     * Called on the logical server the first tick it's active.
     */
    public void initialize() {

    }

    /**
     * @return the center position of this attachment
     */
    public @NotNull Vec3 getCenter() {
        return VecHelper.rotateCentered(slot.center, -panelFacing.toYRot() + 180, Direction.Axis.Y).add(Vec3.atLowerCornerOf(pos));
    }

    public void sendData() {
        if (level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ElectricalPanelBlockEntity be)
                be.sendData();
    }

    public void onRemoved(@Nullable Player player) {

    }

    public List<ItemStack> getDrops() {
        return List.of(defaultDroppedStack());
    }

    public @NotNull ItemStack defaultDroppedStack() {
        ItemStack stack = new ItemStack(type.item.asItem());
        if (label != null)
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(label));
        return stack;
    }

    public MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
        return CEELang.nodeLabel("node");
    }

    @SuppressWarnings("unused")
    public float getNodeSize(Level level, BlockPos pos, BlockState state, int id) {
        return slot.nodeSize;
    }
}
