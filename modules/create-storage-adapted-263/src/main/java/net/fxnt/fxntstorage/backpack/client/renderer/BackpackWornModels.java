package net.fxnt.fxntstorage.backpack.client.renderer;

import net.fxnt.fxntstorage.backpack.BackpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BackpackWornModels {
    private static final Map<Item,StandaloneModelKey<BlockStateModelPart>> KEYS=new ConcurrentHashMap<>();
    private BackpackWornModels() {}
    public static Identifier locationFor(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).withPath(path->"block/"+path+"_worn");
    }
    public static void register(ModelEvent.RegisterStandalone event) {
        for(Item item:BuiltInRegistries.ITEM) if(item instanceof BackpackItem) {
            var id=locationFor(item);
            var key=KEYS.computeIfAbsent(item,ignored->new StandaloneModelKey<>(()->"fxntstorage:worn/"+id));
            event.register(key,SimpleUnbakedStandaloneModel.simpleModelWrapper(id));
        }
    }
    public static BlockStateModelPart get(ItemStack backpack) {
        var key=KEYS.get(backpack.getItem());
        return key==null?null:Minecraft.getInstance().getModelManager().getStandaloneModel(key);
    }
}
