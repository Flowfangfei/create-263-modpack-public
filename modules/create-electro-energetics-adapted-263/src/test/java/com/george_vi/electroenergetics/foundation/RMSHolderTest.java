package com.george_vi.electroenergetics.foundation;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RMSHolderTest {
    @Test
    void computesRollingRmsAndPreservesSignForDc() {
        RMSHolder rms = new RMSHolder(2);
        rms.add(-3);
        rms.add(-4);
        assertEquals(Math.sqrt(12.5), rms.get(), 1e-12);
        assertEquals(-Math.sqrt(12.5), rms.getSigned(), 1e-12);
        rms.add(0);
        assertEquals(Math.sqrt(8), rms.get(), 1e-12);
    }

    @Test
    void restoresWindowAndNextInsertionPosition() {
        RMSHolder original = new RMSHolder(3);
        original.add(10);
        original.add(20);
        original.add(30);
        original.add(40);
        CompoundTag save = new CompoundTag();
        original.write(save, "Voltage");
        RMSHolder restored = new RMSHolder(3);
        restored.read(save, "Voltage");
        original.add(50);
        restored.add(50);
        assertEquals(original.get(), restored.get(), 1e-12);
    }

    @Test
    void normalizesLegacyNegativeWindowPointer() {
        CompoundTag save = new CompoundTag();
        save.putInt("VoltagePointer", -1);
        RMSHolder restored = new RMSHolder(3);
        restored.read(save, "Voltage");
        restored.add(6);
        assertEquals(Math.sqrt(12), restored.get(), 1e-12);
    }
}
