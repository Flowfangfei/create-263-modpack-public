package com.simibubi.create.infrastructure.assets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.Create;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

/** Adapts only known block-entity fields in the external official Ponder resource pack. */
final class ExternalCreatePonderTemplates {
    private static final Set<String> LEGACY_FLUID_KEYS = Set.of("FluidName", "Amount");
    private static final List<String> FACES = List.of("down", "up", "north", "south", "west", "east");

    private ExternalCreatePonderTemplates() {}

    static InputStream adapt(Identifier location, InputStream stream) throws IOException {
        byte[] original = stream.readAllBytes();
        CompoundTag template = NbtIo.readCompressed(new ByteArrayInputStream(original), NbtAccounter.uncompressedQuota());
        boolean changed = false;
        if (template.get("blocks") instanceof ListTag blocks) {
            for (Tag entry : blocks) {
                if (!(entry instanceof CompoundTag block) || !(block.get("nbt") instanceof CompoundTag nbt))
                    continue;
                String context = location + " at " + block.get("pos") + " (" + nbt.getStringOr("id", "") + ")";
                changed |= adaptBlockEntity(nbt, context);
            }
        }
        if (!changed)
            return new ByteArrayInputStream(original);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        NbtIo.writeCompressed(template, output);
        return new ByteArrayInputStream(output.toByteArray());
    }

    private static boolean adaptBlockEntity(CompoundTag nbt, String context) {
        return switch (nbt.getStringOr("id", "")) {
            case "create:fluid_tank", "create:creative_fluid_tank" -> adaptStackField(nbt, "TankContent", context);
            case "create:hose_pulley" -> adaptStackField(nbt, "Tank", context);
            case "create:spout", "create:item_drain" -> adaptSegments(nbt, "Tanks", context);
            case "create:basin" -> adaptSegments(nbt, "InputTanks", context) | adaptSegments(nbt, "OutputTanks", context);
            case "create:fluid_pipe", "create:glass_fluid_pipe", "create:mechanical_pump" -> adaptConnections(nbt, context);
            case "create:copycat", "create:water_wheel", "create:large_water_wheel" -> adaptMaterial(nbt, context);
            default -> false;
        };
    }

    private static boolean adaptSegments(CompoundTag nbt, String key, String context) {
        if (!nbt.contains(key))
            return false;
        if (!(nbt.get(key) instanceof ListTag segments))
            return unsupported(context, key);
        boolean changed = false;
        for (int i = 0; i < segments.size(); i++) {
            if (!(segments.get(i) instanceof CompoundTag segment)) {
                unsupported(context, key + "[" + i + "]");
                continue;
            }
            if (!segment.contains("TankContent"))
                continue;
            String path = key + "[" + i + "].TankContent";
            if (segment.contains("Fluid") || !(segment.get("TankContent") instanceof CompoundTag old)) {
                unsupported(context, path);
                continue;
            }
            CompoundTag fluid = adaptFluidStack(old, context, path);
            if (fluid == null)
                continue;
            segment.put("Fluid", fluid);
            segment.remove("TankContent");
            changed = true;
        }
        return changed;
    }

    private static boolean adaptStackField(CompoundTag container, String key, String context) {
        if (!container.contains(key))
            return false;
        if (!(container.get(key) instanceof CompoundTag old))
            return unsupported(context, key);
        CompoundTag fluid = adaptFluidStack(old, context, key);
        if (fluid == null || fluid.equals(old))
            return false;
        container.put(key, fluid);
        return true;
    }

    @Nullable
    private static CompoundTag adaptFluidStack(CompoundTag old, String context, String path) {
        if (old.isEmpty())
            return old;
        if (old.keySet().equals(Set.of("Fluid")) && old.get("Fluid") instanceof CompoundTag nested)
            return adaptFluidStack(nested, context, path + ".Fluid");
        if (old.keySet().equals(LEGACY_FLUID_KEYS)
                && old.get("FluidName") instanceof StringTag && old.get("Amount") instanceof IntTag) {
            String id = old.getStringOr("FluidName", "");
            int amount = old.getIntOr("Amount", -1);
            if (id.equals("minecraft:empty") && amount == 0)
                return new CompoundTag(); // FluidStack.OPTIONAL_CODEC represents empty stacks as {}.
            if (!id.equals("minecraft:empty") && Identifier.tryParse(id) != null && amount > 0) {
                CompoundTag fluid = new CompoundTag();
                // The fixed 26.3 target registers milk as minecraft:milk; official legacy templates use create:milk.
                fluid.putString("id", id.equals("create:milk") ? "minecraft:milk" : id);
                fluid.putInt("amount", amount);
                return fluid;
            }
        } else if (!old.contains("FluidName") && !old.contains("Amount") && !old.contains("Fluid")
                && old.get("id") instanceof StringTag && old.get("amount") instanceof IntTag
                && Identifier.tryParse(old.getStringOr("id", "")) != null
                && !old.getStringOr("id", "").equals("minecraft:empty") && old.getIntOr("amount", 0) > 0) {
            return old; // Preserve components and any other native stack data.
        }
        unsupported(context, path);
        return null;
    }

    private static boolean adaptConnections(CompoundTag nbt, String context) {
        boolean changed = false;
        for (String face : FACES) {
            if (!(nbt.get(face) instanceof CompoundTag connection))
                continue;
            // Old pipe serializers merged the fluid into the flow/open-end compound.
            if (connection.get("Flow") instanceof CompoundTag flow)
                changed |= nestLegacyFluid(flow, "Fluid", context, face + ".Flow");
            if (connection.get("OpenEnd") instanceof CompoundTag openEnd)
                changed |= nestLegacyFluid(openEnd, "Tank", context, face + ".OpenEnd");
        }
        return changed;
    }

    private static boolean nestLegacyFluid(CompoundTag container, String key, String context, String path) {
        if (!container.contains("FluidName") && !container.contains("Amount"))
            return false;
        // No legacy custom fluid data is present in the fixed official templates. Do not guess its conversion.
        if (container.contains(key) || container.contains("Tag") || container.contains("tag") || container.contains("components"))
            return unsupported(context, path);
        CompoundTag old = new CompoundTag();
        Tag id = container.get("FluidName");
        Tag amount = container.get("Amount");
        if (id == null || amount == null)
            return unsupported(context, path);
        old.put("FluidName", id);
        old.put("Amount", amount);
        CompoundTag fluid = adaptFluidStack(old, context, path);
        if (fluid == null)
            return false;
        container.put(key, fluid);
        container.remove("FluidName");
        container.remove("Amount");
        return true; // Keep pressure, direction, progress, location and all other container fields.
    }

    private static boolean adaptMaterial(CompoundTag nbt, String context) {
        if (!nbt.contains("Material"))
            return false;
        if (!(nbt.get("Material") instanceof CompoundTag material))
            return unsupported(context, "Material");
        if (!material.contains("Name") && !material.contains("Properties"))
            return false;
        if ((material.contains("Name") && (!(material.get("Name") instanceof StringTag) || material.contains("id")
                || Identifier.tryParse(material.getStringOr("Name", "")) == null))
                || (material.contains("Properties") && (!(material.get("Properties") instanceof CompoundTag) || material.contains("properties"))))
            return unsupported(context, "Material");
        if (material.contains("Name"))
            material.put("id", material.remove("Name"));
        if (material.contains("Properties"))
            material.put("properties", material.remove("Properties"));
        return true;
    }

    private static boolean unsupported(String context, String path) {
        Create.LOGGER.warn("Preserving unsupported external Create Ponder data: {} field {}", context, path);
        return false;
    }
}
