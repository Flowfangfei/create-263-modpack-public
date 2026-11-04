package dev.simulated_team.simulated.content.blocks.docking_connector;

import dev.simulated_team.simulated.multiloader.tanks.CFluidType;
import dev.simulated_team.simulated.multiloader.tanks.SingleTank;
import net.minecraft.core.BlockPos;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class DockingConnectorTank extends SingleTank {

    private final DockingConnectorBlockEntity blockEntity;
    private BlockPos connectedPos;
    private DockingConnectorTank connectedTank;

    public DockingConnectorTank(final DockingConnectorBlockEntity blockEntity) {
        super(1000);
        this.blockEntity = blockEntity;
    }

    public void connect(final BlockPos pos, final DockingConnectorTank other) {
        this.connectedPos = pos;
        this.connectedTank = other;
    }

    public void disconnect() {
        this.connectedPos = null;
        this.connectedTank = null;
    }

    private boolean canInteract() {
        final Level level = this.blockEntity.getLevel();
        if (level == null) {
            return false;
        }

        if (this.connectedPos == null || !(level.getBlockEntity(this.connectedPos) instanceof final DockingConnectorBlockEntity other)) {
            return false;
        }

        return this.connectedTank == other.tank;
    }

    @Override
    public @Nullable SingleTank insertionTarget() {
        return this.canInteract() ? this.connectedTank : null;
    }

    @Override
    public void onContentsChanged() {
        this.blockEntity.setChanged();
    }
}
