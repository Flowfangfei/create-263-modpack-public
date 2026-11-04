package dev.simulated_team.simulated.multiloader.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class SingleBattery {
    public final int maxEnergy;
    protected int throughput;
    protected int energy = 0;
    private final Runnable changed;

    public SingleBattery(final int maxEnergy, final int throughput) {
        this(maxEnergy, throughput, () -> {});
    }

    public SingleBattery(final int maxEnergy, final int throughput, final Runnable changed) {
        this.changed = changed;
        this.maxEnergy = maxEnergy;
        this.throughput = throughput;
    }

    public int getEnergy() {
        return this.energy;
    }

    public void setEnergy(final int v) {
        this.energy = v;
    }

    public int getThroughput() {
        return this.throughput;
    }

    public void setThroughput(final int v) {
        this.throughput = v;
    }

    public SingleBattery insertionTarget() { return this; }

    public void onContentsChanged() { changed.run(); }

    public int receiveEnergy(final int toReceive, final boolean simulate) {
        SingleBattery target = insertionTarget();
        if (target == null || toReceive <= 0) return 0;
        final int diff = Math.max(0, Math.min(Math.min(toReceive, target.maxEnergy - target.energy), target.throughput));
        if (!simulate && diff > 0) {
            target.energy += diff;
            target.onContentsChanged();
        }
        return diff;
    }

    public int extractEnergy(final int toExtract, final boolean simulate) {
        final int diff = Math.max(0, Math.min(Math.min(toExtract, this.energy), this.throughput));
        if (!simulate && diff > 0) {
            this.energy -= diff;
            this.onContentsChanged();
        }
        return diff;
    }

    public boolean canExtract() {
        return true;
    }

    public boolean canReceive() {
        return true;
    }

    public void read(final CompoundTag tag) {
        this.energy = Mth.clamp(tag.getIntOr("Energy", 0), 0, this.maxEnergy);
    }

    public CompoundTag write() {
        final CompoundTag tag = new CompoundTag();
        tag.putInt("Energy", this.energy);
        return tag;
    }
}
