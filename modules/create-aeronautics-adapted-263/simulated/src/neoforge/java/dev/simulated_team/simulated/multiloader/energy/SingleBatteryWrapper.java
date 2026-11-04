package dev.simulated_team.simulated.multiloader.energy;

import dev.simulated_team.simulated.multiloader.transfer.BackendTransactions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Native transactions snapshot the actual recipient, including a docking connector's peer. */
public final class SingleBatteryWrapper implements EnergyHandler {
    private final SingleBattery battery;
    public SingleBatteryWrapper(SingleBattery battery){this.battery=battery;}
    @Override public long getAmountAsLong(){return battery.getEnergy();}
    @Override public long getCapacityAsLong(){return battery.maxEnergy;}
    private static void snapshot(SingleBattery target,TransactionContext transaction){
        BackendTransactions.snapshot(target,transaction,target::getEnergy,target::setEnergy,target::onContentsChanged);
    }
    @Override public int insert(int amount,TransactionContext transaction){
        if(amount<0)throw new IllegalArgumentException("Negative amount");
        if(amount==0||!battery.canReceive())return 0;
        SingleBattery target=battery.insertionTarget(); if(target==null)return 0;
        int accepted=Math.max(0,Math.min(Math.min(amount,target.maxEnergy-target.getEnergy()),target.getThroughput()));
        if(accepted<=0)return 0;
        snapshot(target,transaction); target.setEnergy(target.getEnergy()+accepted); return accepted;
    }
    @Override public int extract(int amount,TransactionContext transaction){
        if(amount<0)throw new IllegalArgumentException("Negative amount");
        if(amount==0||!battery.canExtract())return 0;
        int accepted=battery.extractEnergy(amount,true);
        if(accepted<=0)return 0;
        snapshot(battery,transaction); battery.setEnergy(battery.getEnergy()-accepted); return accepted;
    }
}
