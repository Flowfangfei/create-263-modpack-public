package com.george_vi.electroenergetics.events.datagen;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.triggers.ItemUsedOnLocationTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// Pretty much *borrowed* from create
public class CEEAdvancement {

    static final Identifier BACKGROUND = CreateElectroEnergetics.rl("textures/gui/advancements.png");
    static final String LANG = "advancement." + CreateElectroEnergetics.ID + ".";
    static final String SECRET_SUFFIX = "\n§7(Hidden Advancement)";

    private final Map<String, Function<HolderLookup.Provider, Criterion<?>>> criteria = new LinkedHashMap<>();
    SimpleCEETrigger builtinTrigger;
    private CEEAdvancement parent;
    private final Builder createBuilder = new Builder();


    AdvancementHolder datagenResult;

    private String id;
    private String title;
    private String description;

    public CEEAdvancement(String id, UnaryOperator<Builder> b) {
        this.id = id;

        b.apply(createBuilder);

        if (!createBuilder.externalTrigger) {
            builtinTrigger = CEETriggers.get(id + "_builtin");
            criteria.put("0", provider -> builtinTrigger.createCriterion(builtinTrigger.instance()));
        }

        if (createBuilder.type == TaskType.SECRET)
            description += SECRET_SUFFIX;
    }

    private String titleKey() {
        return LANG + id;
    }

    private String descriptionKey() {
        return titleKey() + ".desc";
    }

    String getId() { return id; }

    public boolean isAlreadyAwardedTo(Player player) {
        if (!(player instanceof ServerPlayer sp))
            return true;
        AdvancementHolder advancement = sp.level().getServer()
                .getAdvancements()
                .get(CreateElectroEnergetics.rl(id));
        if (advancement == null)
            return true;
        return sp.getAdvancements()
                .getOrStartProgress(advancement)
                .isDone();
    }

    public void awardTo(Player player) {
        if (!(player instanceof ServerPlayer sp))
            return;
        if (builtinTrigger == null)
            throw new UnsupportedOperationException(
                    "Advancement " + id + " uses external Triggers, it cannot be awarded directly");
        builtinTrigger.trigger(sp);
    }

    void save(Consumer<AdvancementHolder> t, HolderLookup.Provider registries) {
        Advancement.Builder mcBuilder = Advancement.Builder.advancement();
        if (parent != null)
            mcBuilder.parent(parent.datagenResult);
        criteria.forEach((key, factory) -> mcBuilder.addCriterion(key, factory.apply(registries)));
        mcBuilder.display(createBuilder.icon.apply(registries), Component.translatable(titleKey()),
                Component.translatable(descriptionKey()).withStyle(s -> s.withColor(0xDBA213)),
                id.equals("root") ? BACKGROUND : null, createBuilder.type.advancementType, createBuilder.type.toast,
                createBuilder.type.announce, createBuilder.type.hide);

        datagenResult = mcBuilder.save(t, CreateElectroEnergetics.rl(id)
                .toString());
    }

    void provideLang(BiConsumer<String, String> consumer) {
        consumer.accept(titleKey(), title);
        consumer.accept(descriptionKey(), description);
    }

    enum TaskType {

        SILENT(AdvancementType.TASK, false, false, false),
        NORMAL(AdvancementType.TASK, true, false, false),
        NOISY(AdvancementType.TASK, true, true, false),
        EXPERT(AdvancementType.GOAL, true, true, false),
        SECRET(AdvancementType.GOAL, true, true, true),

        ;

        private final AdvancementType advancementType;
        private final boolean toast;
        private final boolean announce;
        private final boolean hide;

        TaskType(AdvancementType advancementType, boolean toast, boolean announce, boolean hide) {
            this.advancementType = advancementType;
            this.toast = toast;
            this.announce = announce;
            this.hide = hide;
        }
    }

    public class Builder {

        private TaskType type = TaskType.NORMAL;
        private boolean externalTrigger;
        private int keyIndex;
        private Function<HolderLookup.Provider, ItemStackTemplate> icon;

        Builder special(TaskType type) {
            this.type = type;
            return this;
        }

        Builder after(CEEAdvancement other) {
            CEEAdvancement.this.parent = other;
            return this;
        }

        Builder icon(ItemLike item) {
            this.icon = provider -> new ItemStackTemplate(itemHolder(provider, item), 1, DataComponentPatch.EMPTY);
            return this;
        }

        Builder icon(ItemStack stack) {
            icon = provider -> new ItemStackTemplate(itemHolder(provider, stack.getItem()), stack.getCount(), stack.getComponentsPatch());
            return this;
        }

        Builder icon(Function<HolderLookup.Provider, ItemStack> func) {
            this.icon = provider -> {
                ItemStack stack = func.apply(provider);
                return new ItemStackTemplate(itemHolder(provider, stack.getItem()), stack.getCount(), stack.getComponentsPatch());
            };
            return this;
        }

        Builder title(String title) {
            CEEAdvancement.this.title = title;
            return this;
        }

        Builder description(String description) {
            CEEAdvancement.this.description = description;
            return this;
        }

        Builder whenBlockPlaced(Block block) {
            return externalTrigger(ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block));
        }

        Builder whenIconCollected() {
            return externalTrigger(provider -> InventoryChangeTrigger.TriggerInstance.hasItems(itemPredicate(icon.apply(provider).item())));
        }

        Builder whenItemCollected(ItemLike itemProvider) {
            return externalTrigger(provider -> InventoryChangeTrigger.TriggerInstance.hasItems(itemPredicate(itemHolder(provider, itemProvider))));
        }

        Builder whenItemCollected(TagKey<Item> tag) {
            return externalTrigger(provider -> InventoryChangeTrigger.TriggerInstance
                    .hasItems(ItemPredicate.Builder.item().of(provider.lookupOrThrow(Registries.ITEM), tag).build()));
        }

        Builder awardedForFree() {
            return externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[] {}));
        }

        Builder externalTrigger(Criterion<?> trigger) {
            return externalTrigger(provider -> trigger);
        }

        Builder externalTrigger(Function<HolderLookup.Provider, Criterion<?>> trigger) {
            criteria.put(String.valueOf(keyIndex), trigger);
            externalTrigger = true;
            keyIndex++;
            return this;
        }

    }

    private static Holder<Item> itemHolder(HolderLookup.Provider provider, ItemLike item) {
        return provider.lookupOrThrow(Registries.ITEM).getOrThrow(ResourceKey.create(Registries.ITEM,
            BuiltInRegistries.ITEM.getKey(item.asItem())));
    }

    private static ItemPredicate itemPredicate(Holder<Item> item) {
        return new ItemPredicate(Optional.of(HolderSet.direct(item)), MinMaxBounds.Ints.ANY, DataComponentMatchers.ANY);
    }


}
