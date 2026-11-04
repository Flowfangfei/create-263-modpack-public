package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.zurrtum.create.AllItems;
import com.george_vi.electroenergetics.compat.SpatialIntegration;
import net.minecraft.core.Position;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AltitudeSensorPanelAttachment extends PanelAttachment {
    public boolean radial = false;
    public double visualHeight = 0;
    public double worldHeight = 0;
    public double prevVisualHeight = 0;

    public AltitudeSensorPanelAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public InteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(AllItems.WRENCH)) {
            radial ^= true;
            sendData();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        super.tickClient(be);
        Vec3 center = SpatialIntegration.projectOutOfSubLevel(level, (Position) net.minecraft.world.phys.Vec3.atCenterOf(pos));

        worldHeight = center.y;

        if (this.visualHeight == 0.0f)
            this.visualHeight = worldHeight;

        float step = 0.15f;
        prevVisualHeight = visualHeight;
        visualHeight = visualHeight * (1.0f - step) + worldHeight * step;
    }

    @Override
    public void preTick(BridgeCollector bridges) {

    }

    @Override
    public void postTick(SimulationResults results) {

    }
}
