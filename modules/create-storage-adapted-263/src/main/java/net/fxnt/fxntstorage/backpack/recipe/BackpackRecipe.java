package net.fxnt.fxntstorage.backpack.recipe;

import com.mojang.serialization.MapCodec;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.fxnt.fxntstorage.init.ModDataComponents;
import net.fxnt.fxntstorage.init.ModTags;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BackpackRecipe extends ShapedRecipe {

    private static final Map<Item, Integer> BACKPACK_MULTIPLIERS = Map.of(
            ModBlocks.ANDESITE_BACKPACK.asItem(), Util.ANDESITE_BACKPACK_STACK_MULTIPLIER,
            ModBlocks.COPPER_BACKPACK.asItem(), Util.COPPER_BACKPACK_STACK_MULTIPLIER,
            ModBlocks.BRASS_BACKPACK.asItem(), Util.BRASS_BACKPACK_STACK_MULTIPLIER,
            ModBlocks.HARDENED_BACKPACK.asItem(), Util.HARDENED_BACKPACK_STACK_MULTIPLIER
    );

    private final ShapedRecipe wrapped;

    public BackpackRecipe(ShapedRecipe recipe) {
        super(new net.minecraft.world.item.crafting.Recipe.CommonInfo(recipe.showNotification()),
                new net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()),
                recipe.pattern, resultTemplate(recipe));
        this.wrapped = recipe;
    }

    public ShapedRecipe getWrapped() {
        return wrapped;
    }

    private static ItemStackTemplate resultTemplate(ShapedRecipe recipe) {
        // Recipe decoding precedes holder component binding. Reuse the unresolved template;
        // assembling or resolving the display here would create an ItemStack too early.
        if (recipe.display().getFirst().result() instanceof SlotDisplay.ItemStackSlotDisplay result) {
            return result.stack();
        }
        throw new IllegalArgumentException("Backpack shaped recipe requires an item-stack result template");
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = super.assemble(input);

        // Find the old backpack in the input
        Optional<ItemStack> oldBackpack = input.items().stream()
                .filter(stack -> !stack.isEmpty() && stack.is(ModTags.Items.BACKPACK_ITEM))
                .findFirst();

        if (oldBackpack.isEmpty()) return result;

        // Preserve mod inventory, upgrade settings and explicit component removals.
        result.applyComponents(oldBackpack.get().getComponentsPatch());
        result.remove(DataComponents.CONTAINER);
        // Copy container contents and custom name from old backpack
        if (oldBackpack.get().has(net.fxnt.fxntstorage.init.ModDataComponents.STORAGE_CONTENTS)) {
            result.copyFrom(oldBackpack.get(), net.fxnt.fxntstorage.init.ModDataComponents.STORAGE_CONTENTS);
        }
        if (oldBackpack.get().has(DataComponents.CUSTOM_NAME)) {
            result.copyFrom(oldBackpack.get(), DataComponents.CUSTOM_NAME);
        }

        // Set the new multiplier
        Integer newStackMultiplier = BACKPACK_MULTIPLIERS.get(result.getItem());
        if (newStackMultiplier != null) {
            result.set(ModDataComponents.BACKPACK_STACK_MULTIPLIER, newStackMultiplier);
        }

        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        // ShapedRecipe narrows this covariant return; the registered codec still constructs BackpackRecipe.
        return (RecipeSerializer) Serializer.INSTANCE;
    }

    public static final class Serializer {
        public static final MapCodec<BackpackRecipe> CODEC = ShapedRecipe.MAP_CODEC.xmap(
                BackpackRecipe::new, BackpackRecipe::getWrapped);
        public static final StreamCodec<RegistryFriendlyByteBuf, BackpackRecipe> STREAM_CODEC = ShapedRecipe.STREAM_CODEC.map(
                BackpackRecipe::new, BackpackRecipe::getWrapped);
        public static final RecipeSerializer<BackpackRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);
        private Serializer() {}
    }
}
