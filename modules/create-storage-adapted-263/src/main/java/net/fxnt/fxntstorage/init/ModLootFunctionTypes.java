package net.fxnt.fxntstorage.init;

import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.datagen.helper.ModCopyBlockEntityData;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModLootFunctionTypes {

    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, FXNTStorage.MOD_ID);

    public static final Supplier<MapCodec<ModCopyBlockEntityData>> COPY_BLOCK_ENTITY_DATA =
            LOOT_FUNCTIONS.register("copy_block_entity_data",
                    () -> ModCopyBlockEntityData.CODEC);

    public static void register(IEventBus bus) {
        LOOT_FUNCTIONS.register(bus);
    }
}
