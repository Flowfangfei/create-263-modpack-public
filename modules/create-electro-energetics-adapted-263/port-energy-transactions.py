from pathlib import Path
r=Path(__file__).parent;s=r/'src/neoforge/java/com/george_vi/electroenergetics/content/converter'
p=s/'ConverterDevice.java';t=p.read_text(encoding='utf-8').replace('import net.neoforged.neoforge.energy.IEnergyStorage;','import net.neoforged.neoforge.transfer.energy.EnergyHandler;\nimport net.neoforged.neoforge.transfer.transaction.Transaction;')
t=t.replace('IEnergyStorage energyStorage','EnergyHandler energyStorage').replace('Capabilities.EnergyStorage.BLOCK','Capabilities.Energy.BLOCK')
t=t.replace('energyStorage.extractEnergy((int) ((getMaxEnergy() - this.storedEnergy) / CEEConfigs.server().wattFeTConversionRate.get()), false)','extractCommitted(energyStorage, (int) ((getMaxEnergy() - this.storedEnergy) / CEEConfigs.server().wattFeTConversionRate.get()))')
t=t.replace('energyStorage.receiveEnergy((int) (this.storedEnergy / CEEConfigs.server().wattFeTConversionRate.get()), false)','insertCommitted(energyStorage, (int) (this.storedEnergy / CEEConfigs.server().wattFeTConversionRate.get()))')
pos=t.rfind('}');t=t[:pos]+'''    private static int extractCommitted(EnergyHandler handler, int amount) {
        if (amount <= 0) return 0;
        try (Transaction transaction = Transaction.openRoot()) {
            int transferred = handler.extract(amount, transaction);
            transaction.commit();
            return transferred;
        }
    }

    private static int insertCommitted(EnergyHandler handler, int amount) {
        if (amount <= 0) return 0;
        try (Transaction transaction = Transaction.openRoot()) {
            int transferred = handler.insert(amount, transaction);
            transaction.commit();
            return transferred;
        }
    }
'''+t[pos:];p.write_text(t,encoding='utf-8')
p=s/'ConverterBlockEntity.java';t=p.read_text(encoding='utf-8').replace('import net.neoforged.neoforge.energy.EnergyStorage;','import net.neoforged.neoforge.transfer.energy.EnergyHandler;\nimport net.neoforged.neoforge.transfer.transaction.TransactionContext;\nimport net.neoforged.neoforge.transfer.transaction.SnapshotJournal;\nimport net.neoforged.neoforge.transfer.TransferPreconditions;').replace('Capabilities.EnergyStorage.BLOCK','Capabilities.Energy.BLOCK')
a=t.index('    public static class ConverterEnergyStorage extends EnergyStorage');t=t[:a]+'''    /** Shares the simulation buffer with the transactional NeoForge FE capability. */
    public static class ConverterEnergyStorage extends SnapshotJournal<Double> implements EnergyHandler {
        final ConverterBlockEntity be;

        public ConverterEnergyStorage(ConverterBlockEntity be) {
            this.be = be;
        }

        private ConverterDevice device() {
            if (!(be.getLevel() instanceof ServerLevel level)) return null;
            if (be.converterDevice == null || !be.converterDevice.isValid())
                be.converterDevice = DevicesSavedData.load(level).getDevice(be.getBlockPos(), ConverterDevice.class);
            return be.converterDevice;
        }

        private double conversionRate() {
            return CEEConfigs.server().wattFeTConversionRate.get();
        }

        @Override
        public long getAmountAsLong() {
            ConverterDevice device = device();
            double energy = device != null ? device.storedEnergy : be.storedEnergy;
            return Math.max(0L, (long) (energy / conversionRate()));
        }

        @Override
        public long getCapacityAsLong() {
            return Math.max(0L, (long) (CEEConfigs.server().converterMaxPowerKw.get() * 1000 / conversionRate()));
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            ConverterDevice device = device();
            if (device == null || device.isSource) return 0;
            int transferred = (int) Math.min(amount, Math.min(Integer.MAX_VALUE, getAmountAsLong()));
            if (transferred > 0) {
                updateSnapshots(transaction);
                device.storedEnergy -= transferred * conversionRate();
            }
            return transferred;
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            ConverterDevice device = device();
            if (device == null || !device.isSource) return 0;
            double room = Math.max(0, CEEConfigs.server().converterMaxPowerKw.get() * 1000 - device.storedEnergy);
            int transferred = (int) Math.min(amount, Math.min(Integer.MAX_VALUE, room / conversionRate()));
            if (transferred > 0) {
                updateSnapshots(transaction);
                device.storedEnergy += transferred * conversionRate();
            }
            return transferred;
        }

        @Override
        protected Double createSnapshot() {
            return be.converterDevice.storedEnergy;
        }

        @Override
        protected void revertToSnapshot(Double snapshot) {
            be.converterDevice.storedEnergy = snapshot;
        }

        @Override
        protected void onRootCommit(Double originalState) {
            be.storedEnergy = be.converterDevice.storedEnergy;
            be.setChanged();
            be.sendData();
        }
    }
}
''';p.write_text(t,encoding='utf-8')
