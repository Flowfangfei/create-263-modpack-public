package com.george_vi.electroenergetics.foundation;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NbtViewTest {
    @Test
    void preservesNestedDeviceStateThroughValueViews() {
        CompoundTag original = new CompoundTag();
        original.putDouble("Cell1Charge", 124.1234567890123);
        original.putIntArray("Node", new int[]{2, -115, 80, 256});
        original.putLongArray("AverageVoltage", new long[]{Double.doubleToLongBits(-230.75), 0});
        CompoundTag wire = new CompoundTag();
        wire.putString("WireType", "electroenergetics:standard");
        wire.putFloat("Temperature", 125.5f);
        ListTag connections = new ListTag();
        connections.add(wire);
        original.put("Connections", connections);

        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        NbtView.write(output, original);
        CompoundTag restored = NbtView.read(TagValueInput.create(
                ProblemReporter.DISCARDING, RegistryAccess.EMPTY, output.buildResult()));

        assertEquals(original, restored);
    }

    @Test
    void addsDeviceFieldsWithoutDiscardingSuperclassFields() {
        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        output.putFloat("Speed", 32);
        CompoundTag deviceState = new CompoundTag();
        deviceState.putDouble("Power", 7360);
        NbtView.write(output, deviceState);

        assertEquals(32, output.buildResult().getFloatOr("Speed", 0));
        assertEquals(7360, output.buildResult().getDoubleOr("Power", 0));
    }
}
