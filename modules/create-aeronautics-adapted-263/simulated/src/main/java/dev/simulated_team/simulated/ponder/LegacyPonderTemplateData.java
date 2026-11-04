package dev.simulated_team.simulated.ponder;

import com.simibubi.create.content.fluids.potion.PotionFluid;
import dev.simulated_team.simulated.Simulated;
import net.createmod.catnip.api.math.BlockFace;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Set;

/** Adapts in-memory Ponder resource templates, never saved worlds or general stack codecs. */
public final class LegacyPonderTemplateData {
    private static final String LEGACY_CAPACITY = "create:fluid_max_capacity";
    private static final Identifier LEGACY_CAPACITY_ID = Identifier.parse(LEGACY_CAPACITY);
    private static final Set<String> PIPE_ENTITIES = Set.of(
            "create:mechanical_pump", "create:fluid_pipe", "create:glass_fluid_pipe");
    private static final Set<String> PACKAGE_ENTITIES = Set.of(
            "create:packager", "create:repackager",
            "fluidlogistics:fluid_packager", "fluidlogistics:fluid_repackager");
    private static final Set<String> EMPTY_PACKAGE_KEYS = Set.of("id", "count", "Count");
    private static final Set<String> DIRECT_TANK_ENTITIES = Set.of(
            "create:fluid_tank", "create:creative_fluid_tank",
            "createdieselgenerators:distillation_tank_block_entity");
    private static final Set<String> LEGACY_FLUID_KEYS = Set.of("FluidName", "Amount");
    private static final Set<String> LEGACY_POTION_KEYS = Set.of("FluidName", "Amount", "Tag");
    private static final Set<String> SEGMENTED_TANK_ENTITIES = Set.of(
            "create_enchantment_industry:classic_blaze_enchanter",
            "create_enchantment_industry:blaze_enchanter", "create_enchantment_industry:blaze_forger");
    private static final String[] DIRECTIONS = {"down", "up", "north", "south", "west", "east"};

    private LegacyPonderTemplateData() {
    }

    /** Called only for the NbtIo.read result inside PonderSceneRegistry.loadSchematic(InputStream). */
    public static CompoundTag adapt(final CompoundTag template) {
        if (!(template.get("blocks") instanceof ListTag blocks))
            return template;

        final boolean capacityUnsupported = !BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(LEGACY_CAPACITY_ID);
        for (int i = 0; i < blocks.size(); i++) {
            if (!(blocks.get(i) instanceof CompoundTag block)
                    || !(block.get("nbt") instanceof CompoundTag blockEntity))
                continue;

            final String id = blockEntity.getStringOr("id", "");
            final String path = "blocks[" + i + "].nbt(" + id + ")";
            LegacyPonderTemplatePositions.adapt(blockEntity);
            LegacyPonderTemplateClientDefaults.adapt(blockEntity);
            LegacyPonderTemplateItems.adapt(blockEntity);
            if (DIRECT_TANK_ENTITIES.contains(id))
                adaptTankContent(blockEntity, path, capacityUnsupported);
            if (id.equals("create_dragons_plus:fragile_fluid_tank")
                    && blockEntity.get("Tanks") instanceof ListTag tanks) {
                for (int tank = 0; tank < tanks.size(); tank++)
                    if (tanks.get(tank) instanceof CompoundTag segment)
                        adaptFragileTankContent(segment, path + ".Tanks[" + tank + "]", capacityUnsupported);
            }
            if (SEGMENTED_TANK_ENTITIES.contains(id) && blockEntity.get("Tanks") instanceof ListTag tanks) {
                for (int tank = 0; tank < tanks.size(); tank++)
                    if (tanks.get(tank) instanceof CompoundTag segment)
                        adaptTankContent(segment, path + ".Tanks[" + tank + "]", capacityUnsupported);
            }
            if (PIPE_ENTITIES.contains(id)) {
                final Tag position = block.get("pos");
                final BlockPos blockPos = position == null ? null
                        : BlockPos.CODEC.parse(NbtOps.INSTANCE, position).result().orElse(null);
                adaptPipe(blockEntity, blockPos, path, capacityUnsupported);
            }
            if (PACKAGE_ENTITIES.contains(id)) {
                adaptEmptyPackage(blockEntity, "HeldBox", path);
                adaptEmptyPackage(blockEntity, "InsertedBox", path);
            }
        }
        return template;
    }

    private static void adaptFragileTankContent(final CompoundTag segment, final String path,
                                                final boolean capacityUnsupported) {
        final Tag value = segment.get("TankContent");
        if (value == null)
            return;
        // The active Sable integration uses Create's segment Fluid field, unlike the CEI reader.
        if (segment.contains("Fluid") || !(value instanceof CompoundTag old)) {
            kept(path, "conflicting Fluid/TankContent fields or unknown fragile tank content");
            return;
        }
        CompoundTag candidate = old;
        if (old.contains("Fluid")) {
            if (!old.keySet().equals(Set.of("Fluid")) || !(old.get("Fluid") instanceof CompoundTag nested)) {
                kept(path + ".TankContent", "conflicting or unknown legacy Fluid wrapper");
                return;
            }
            candidate = nested;
        }
        final CompoundTag fluid = convertLegacyTankFluid(candidate, path + ".TankContent");
        if (fluid == null)
            return;
        adaptCapacity(fluid, path + ".Fluid", capacityUnsupported);
        segment.put("Fluid", fluid);
        segment.remove("TankContent"); // Keep Level and every unrelated segment field.
    }

    private static void adaptTankContent(final CompoundTag blockEntity, final String path,
                                         final boolean capacityUnsupported) {
        final Tag value = blockEntity.get("TankContent");
        if (value == null)
            return;
        if (!(value instanceof CompoundTag old)) {
            kept(path + ".TankContent", "unknown tank content representation");
            return;
        }
        CompoundTag candidate = old;
        if (old.contains("Fluid")) {
            if (!old.keySet().equals(Set.of("Fluid")) || !(old.get("Fluid") instanceof CompoundTag nested)) {
                kept(path + ".TankContent", "conflicting or unknown legacy Fluid wrapper");
                return;
            }
            candidate = nested;
        }
        final CompoundTag fluid = convertLegacyTankFluid(candidate, path + ".TankContent");
        if (fluid == null)
            return;
        if (fluid != old)
            blockEntity.put("TankContent", fluid);
        adaptCapacity(fluid, path + ".TankContent", capacityUnsupported);
    }

    private static CompoundTag convertLegacyTankFluid(final CompoundTag old, final String path) {
        if (old.isEmpty() || (!old.contains("FluidName") && !old.contains("Amount")
                && !old.contains("Fluid") && isKnownFluidStack(old)))
            return old;
        final boolean potion = old.keySet().equals(LEGACY_POTION_KEYS);
        if ((!old.keySet().equals(LEGACY_FLUID_KEYS) && !potion)
                || !(old.get("FluidName") instanceof StringTag)
                || !(old.get("Amount") instanceof IntTag amount)) {
            kept(path, "unknown legacy tank fluid fields or numeric types");
            return null;
        }
        final String name = old.getStringOr("FluidName", "");
        if (!potion && name.equals("minecraft:empty") && amount.intValue() == 0)
            return new CompoundTag();
        final Identifier id = Identifier.tryParse(name);
        if (id == null || name.equals("minecraft:empty") || amount.intValue() <= 0
                || !BuiltInRegistries.FLUID.containsKey(id)) {
            kept(path, "unknown legacy fluid identity or amount");
            return null;
        }
        if (potion)
            return convertLegacyPotion(old, path);
        final CompoundTag fluid = new CompoundTag();
        fluid.put("id", old.get("FluidName"));
        fluid.put("amount", amount); // Keep the exact recorded quantity and IntTag type.
        return fluid;
    }

    private static CompoundTag convertLegacyPotion(final CompoundTag old, final String path) {
        if (!old.getStringOr("FluidName", "").equals("create:potion")
                || !(old.get("Tag") instanceof CompoundTag data)
                || !data.keySet().equals(Set.of("Bottle", "Potion"))
                || !(data.get("Bottle") instanceof StringTag) || !(data.get("Potion") instanceof StringTag)
                || Minecraft.getInstance().level == null) {
            kept(path, "unknown legacy custom fluid data");
            return null;
        }
        final PotionFluid.BottleType bottle = switch (data.getStringOr("Bottle", "")) {
            case "REGULAR" -> PotionFluid.BottleType.REGULAR;
            case "SPLASH" -> PotionFluid.BottleType.SPLASH;
            case "LINGERING" -> PotionFluid.BottleType.LINGERING;
            default -> null;
        };
        final var ops = Minecraft.getInstance().level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        final PotionContents contents = PotionContents.CODEC.parse(ops, data.get("Potion")).result().orElse(null);
        if (bottle == null || contents == null) {
            kept(path, "unknown legacy potion identity or bottle type");
            return null;
        }
        // Use the real Create factory and registered component codecs for the old potion semantics.
        final Tag encoded = FluidStack.OPTIONAL_CODEC.encodeStart(ops,
                PotionFluid.of(old.getIntOr("Amount", 0), contents, bottle)).result().orElse(null);
        if (encoded instanceof CompoundTag fluid && old.get("FluidName").equals(fluid.get("id"))
                && old.get("Amount").equals(fluid.get("amount")))
            return fluid;
        kept(path, "legacy potion codec did not preserve fluid identity and amount");
        return null;
    }

    private static void adaptPipe(final CompoundTag blockEntity, final BlockPos blockPos, final String path,
                                  final boolean capacityUnsupported) {
        for (final String direction : DIRECTIONS) {
            if (!(blockEntity.get(direction) instanceof CompoundTag connection))
                continue;

            final String connectionPath = path + "." + direction;
            if (connection.get("Flow") instanceof CompoundTag flow)
                adaptCapacity(flow.get("Fluid"), connectionPath + ".Flow.Fluid", capacityUnsupported);

            if (!(connection.get("OpenEnd") instanceof CompoundTag openEnd))
                continue;
            final String openEndPath = connectionPath + ".OpenEnd";
            final Tag legacyFluid = openEnd.get("Fluid");
            if (legacyFluid != null) {
                // Never choose between old/new payloads or coerce an unknown stack representation.
                if (openEnd.contains("Tank") || !isKnownOptionalFluidStack(legacyFluid)) {
                    kept(openEndPath, "conflicting Tank/Fluid fields or unknown legacy fluid stack");
                    continue;
                }
                // Move the existing tag: id, amount, their NBT types and all other components survive.
                openEnd.put("Tank", legacyFluid);
                openEnd.remove("Fluid");
            }
            adaptOpenEndDirection(openEnd, blockPos, direction, openEndPath);
            adaptCapacity(openEnd.get("Tank"), openEndPath + ".Tank", capacityUnsupported);
        }
    }

    private static void adaptOpenEndDirection(final CompoundTag openEnd, final BlockPos blockPos,
                                             final String connectionDirection, final String path) {
        final Tag legacyDirection = openEnd.get("Direction");
        if (legacyDirection == null)
            return;
        if (openEnd.contains("Location")) {
            kept(path, "both Location and legacy Direction are present");
            return;
        }
        final Direction face = Direction.CODEC.parse(NbtOps.INSTANCE, legacyDirection).result().orElse(null);
        if (blockPos == null || face == null || !face.getName().equals(connectionDirection)) {
            kept(path, "missing exact structure position or unknown/conflicting legacy direction");
            return;
        }
        // Ponder places the template at ZERO; use Catnip's current position/direction wire format.
        // Remove the known legacy key only after the current codec has encoded it successfully.
        BlockFace.CODEC.encodeStart(NbtOps.INSTANCE, new BlockFace(blockPos, face))
                .resultOrPartial(error -> kept(path, "could not encode legacy direction: " + error))
                .ifPresent(location -> {
                    openEnd.put("Location", location);
                    openEnd.remove("Direction");
                });
    }

    private static void adaptCapacity(final Tag value, final String path, final boolean capacityUnsupported) {
        if (!capacityUnsupported || !(value instanceof CompoundTag fluid)
                || !(fluid.get("components") instanceof CompoundTag components)
                || !components.contains(LEGACY_CAPACITY))
            return;

        if (!isKnownFluidStack(fluid) || !isInteger(components.get(LEGACY_CAPACITY))) {
            kept(path, "unknown legacy capacity or fluid stack representation");
            return;
        }
        // This Fabric capacity metadata has no registered equivalent; it is not the fluid amount.
        components.remove(LEGACY_CAPACITY);
    }

    private static boolean isKnownFluidStack(final Tag value) {
        if (!(value instanceof CompoundTag fluid) || !isInteger(fluid.get("amount")))
            return false;
        final long amount = ((NumericTag) fluid.get("amount")).longValue();
        final Identifier id = Identifier.tryParse(fluid.getStringOr("id", ""));
        return amount > 0 && amount <= Integer.MAX_VALUE && id != null
                && !id.equals(Identifier.withDefaultNamespace("empty"))
                && BuiltInRegistries.FLUID.containsKey(id);
    }

    private static boolean isKnownOptionalFluidStack(final Tag value) {
        return value instanceof CompoundTag fluid && (fluid.isEmpty() || isKnownFluidStack(fluid));
    }

    private static void adaptEmptyPackage(final CompoundTag blockEntity, final String key, final String path) {
        if (!(blockEntity.get(key) instanceof CompoundTag item) || item.isEmpty())
            return;
        if (!item.getStringOr("id", "").equals("minecraft:air"))
            return;

        final Tag count = item.get("count");
        final Tag legacyCount = item.get("Count");
        if (!EMPTY_PACKAGE_KEYS.containsAll(item.keySet())
                || (count == null && legacyCount == null)
                || (count != null && !isEmptyPackageCount(count))
                || (legacyCount != null && !isEmptyPackageCount(legacyCount))
                || (count != null && legacyCount != null
                    && ((NumericTag) count).longValue() != ((NumericTag) legacyCount).longValue())) {
            kept(path + "." + key, "unknown or conflicting air placeholder data");
            return;
        }
        // Official fixtures use both Count:0b and Count:1b for empty air placeholders.
        blockEntity.put(key, new CompoundTag());
    }

    private static boolean isEmptyPackageCount(final Tag count) {
        return isInteger(count) && ((NumericTag) count).longValue() <= 1;
    }

    private static boolean isInteger(final Tag value) {
        return value instanceof NumericTag && value.getId() >= Tag.TAG_BYTE && value.getId() <= Tag.TAG_LONG;
    }

    private static void kept(final String path, final String reason) {
        Simulated.LOGGER.warn("Kept legacy Ponder template data at {}: {}", path, reason);
    }
}
