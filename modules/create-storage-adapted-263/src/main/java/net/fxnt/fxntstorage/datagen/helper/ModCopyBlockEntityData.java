package net.fxnt.fxntstorage.datagen.helper;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.logistics.filter.FilterItem;
import net.fxnt.fxntstorage.container.StorageBoxEntity;
import net.fxnt.fxntstorage.init.ModLootFunctionTypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.core.Holder;
import java.util.Optional;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModCopyBlockEntityData extends LootItemConditionalFunction {

    public static final MapCodec<ModCopyBlockEntityData> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(instance -> commonFields(instance).apply(instance, ModCopyBlockEntityData::new));

    ModCopyBlockEntityData(Optional<Holder<LootItemCondition>> conditions) {
        super(conditions);
    }

    @Override
    public MapCodec<ModCopyBlockEntityData> codec() {
        return CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.BLOCK_ENTITY);
    }

    @Override
    public ItemStack run(ItemStack stack, LootContext context) {
        BlockEntity blockEntity = context.getOptional(LootContextParams.BLOCK_ENTITY);
        if (blockEntity != null) {
            var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                    net.minecraft.util.ProblemReporter.DISCARDING, context.getLevel().registryAccess());
            blockEntity.saveCustomOnly(output);
            blockEntity.removeComponentsFromTag(output);
            // Inventory lives once in the mod-owned component when the block becomes an item.
            if (blockEntity instanceof net.fxnt.fxntstorage.backpack.BackpackEntity
                    || blockEntity instanceof StorageBoxEntity
                    || blockEntity instanceof net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxEntity
                    || blockEntity instanceof net.fxnt.fxntstorage.reserve_storage.ReserveStorageBoxEntity)
                output.discard("Items");
            net.minecraft.world.item.BlockItem.setBlockEntityData(stack, blockEntity.getType(), output);
            stack.applyComponents(blockEntity.collectComponents());
            if (blockEntity instanceof net.fxnt.fxntstorage.backpack.BackpackEntity backpack)
                stack=backpack.saveToItemStack(stack); // replays explicit component removals as well

            // Strip FilterItem from BLOCK_ENTITY_DATA to avoid duplication
            if (blockEntity instanceof StorageBoxEntity sbe) {
                ItemStack filterItem = sbe.getFilter().getFilter();
                if (filterItem.getItem() instanceof FilterItem) {
                    TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
                    if (data != null) {
                        CompoundTag tag = data.copyTagWithoutId();
                        tag.remove("Filter");
                        tag.remove("FilterAmount");
                        stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(data.type(), tag));
                    }
                }
            }
        }
        return stack;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new ModCopyBlockEntityData(getCondition());
        }
    }
}
