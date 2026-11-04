package dev.simulated_team.simulated.content.blocks.docking_connector;

import dev.simulated_team.simulated.multiloader.energy.SingleBattery;

public class DockingConnectorBattery extends SingleBattery {
    private DockingConnectorBattery other = null;

    public DockingConnectorBattery(final int maxEnergy, final int throughput) {
        super(maxEnergy, throughput);
    }

    public DockingConnectorBattery(final int maxEnergy, final int throughput, final Runnable changed) {
        super(maxEnergy, throughput, changed);
    }

    public void connect(final DockingConnectorBattery other) {
        this.other = other;
    }

    public void disconnect() {
        this.other = null;
    }

    @Override
    public SingleBattery insertionTarget() { return this.other; }

    @Override
    public boolean canReceive() { return this.other != null; }
}
