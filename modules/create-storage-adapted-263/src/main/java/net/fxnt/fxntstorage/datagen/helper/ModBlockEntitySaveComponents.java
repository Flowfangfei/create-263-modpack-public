package net.fxnt.fxntstorage.datagen.helper;

import com.mojang.serialization.MapCodec;
import net.fxnt.fxntstorage.init.ModLootConditionTypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;


import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModBlockEntitySaveComponents implements LootItemCondition {

    public static final ModBlockEntitySaveComponents INSTANCE = new ModBlockEntitySaveComponents();
    public static final MapCodec<ModBlockEntitySaveComponents> CODEC = MapCodec.unit(INSTANCE);

    private ModBlockEntitySaveComponents() {
    }

    @Override
    public MapCodec<ModBlockEntitySaveComponents> codec() {
        return CODEC;
    }

    @Override
    public boolean test(LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getOptional(LootContextParams.BLOCK_ENTITY);

        if (blockEntity == null) {
            return false;
        }

        CompoundTag tag = blockEntity.saveWithoutMetadata(lootContext.getLevel().registryAccess());

        if (tag.contains("CustomName")) return true;

        if (tag.contains("ExtraComponents")) return true;

        // Check if a Create FilteringBehaviour filter is set
        CompoundTag filterTag = tag.getCompound("Filter").orElse(new CompoundTag());
        if (!filterTag.isEmpty()) return true;

        // Check if the block entity has items
        if (tag.contains("Items")) {
            CompoundTag itemsCompound = tag.getCompound("Items").orElse(new CompoundTag());
            if (itemsCompound.contains("StorageFormat")) {
                var inventory=net.fxnt.fxntstorage.inventory.StorageContents.CODEC.parse(
                    lootContext.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),itemsCompound).getOrThrow();
                return inventory.itemCopies().anyMatch(stack->!stack.isEmpty());
            }
            if (itemsCompound.contains("Items")) {
                ListTag itemList = itemsCompound.getListOrEmpty("Items");
                return !itemList.isEmpty();
            }
        }
        return false;
    }

    public static net.minecraft.core.Holder<LootItemCondition> builder() {
        return net.minecraft.core.Holder.direct(INSTANCE);
    }
}
