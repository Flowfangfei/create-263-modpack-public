package com.railwayteam.railways.internal.compat.create.content.trains.track;

import com.railwayteam.railways.compat.tracks.SoftIngredient;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackMaterial;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import java.util.function.Supplier;

/** Adapts the local entry Supplier types to Create's real material builder. */
public final class TrackMaterialFactory {
    private final Identifier id;
    private final com.simibubi.create.content.trains.track.TrackMaterialFactory delegate;
    private TrackMaterialFactory(Identifier id) {
        this.id = id;
        delegate = com.simibubi.create.content.trains.track.TrackMaterialFactory.make(id)
            .lang(id.getPath()).particle(id.withPrefix("block/track/")).defaultModels();
    }
    public static TrackMaterialFactory make(Identifier id) { return new TrackMaterialFactory(id); }
    public TrackMaterialFactory block(Supplier<? extends Supplier<? extends TrackBlock>> block) {
        delegate.block(() -> () -> block.get().get()); return this;
    }
    public TrackMaterialFactory customBlockFactory(TrackBlockFactory factory) {
        delegate.customBlockFactory(factory::create); return this;
    }
    public TrackMaterialFactory customModels(Supplier<Supplier<PartialModel>> tie, Supplier<Supplier<PartialModel>> left, Supplier<Supplier<PartialModel>> right) {
        delegate.customModels(tie, left, right); return this;
    }
    public TrackMaterialFactory lang(String value) { delegate.lang(value); return this; }
    public TrackMaterialFactory noRecipeGen() { delegate.noRecipeGen(); return this; }
    public TrackMaterialFactory particle(Identifier value) { delegate.particle(value); return this; }
    public TrackMaterialFactory rails(ItemLike value) { delegate.rails(value); return this; }
    public TrackMaterialFactory sleeper(ItemLike value) { delegate.sleeper(value); return this; }
    public TrackMaterialFactory sleeper(Object value) {
        if (value instanceof Ingredient ingredient) delegate.sleeper(ingredient);
        else if (value instanceof ItemLike item) delegate.sleeper(item);
        else if (value instanceof SoftIngredient soft) {
            // Optional absent-mod tracks have no crafting recipe until their material exists.
            var item = BuiltInRegistries.ITEM.getOptional(soft.item);
            if (item.isPresent()) delegate.sleeper(item.get()); else delegate.noRecipeGen();
        } else throw new IllegalArgumentException("Unsupported track sleeper: " + value);
        return this;
    }
    public TrackMaterialFactory standardModels() { delegate.standardModels(); return this; }
    public TrackMaterialFactory trackType(TrackMaterial.TrackType value) { delegate.trackType(value); return this; }
    public TrackMaterial build() { return delegate.build(); }
    public Identifier getId() { return id; }
    @FunctionalInterface public interface TrackBlockFactory extends TrackMaterial.TrackType.TrackBlockFactory { }
}
