/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.world.item.DyeColor;
import plus.dragons.createdragonsplus.fluid.FluidUnits;

/** Generates the loader-neutral core resources shipped by Create: Dragons Plus. */
public final class CDPCoreDataProvider implements DataProvider {
    private static final String MOD_ID = "create_dragons_plus";
    private static final List<String> COLORS = List.of(
            "white",
            "orange",
            "magenta",
            "light_blue",
            "yellow",
            "lime",
            "pink",
            "gray",
            "light_gray",
            "cyan",
            "purple",
            "blue",
            "brown",
            "green",
            "red",
            "black");

    private final Path output;
    private final Map<String, JsonElement> files = new TreeMap<>();

    public CDPCoreDataProvider(FabricPackOutput output) {
        this.output = output.getOutputFolder();
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        files.clear();
        generateAssets();
        generateRecipes();
        generateTags();
        generateDataMaps();
        generateLootTables();
        generateAdvancements();

        List<CompletableFuture<?>> writes = new ArrayList<>(files.size());
        files.forEach((path, json) -> writes.add(DataProvider.saveStable(cache, json, output.resolve(path))));
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Create: Dragons Plus core resources";
    }

    private void generateAssets() {
        generateBlockStatesAndModels();
        generateItemDefinitions();
        generateLanguage();
    }

    private void generateBlockStatesAndModels() {
        JsonObject hatchVariants = new JsonObject();
        for (Map.Entry<String, Integer> facing : List.of(
                Map.entry("north", 0), Map.entry("east", 90), Map.entry("south", 180), Map.entry("west", 270))) {
            for (boolean waterlogged : List.of(false, true)) {
                JsonObject model = object("model", MOD_ID + ":block/fluid_hatch");
                if (facing.getValue() != 0) {
                    model.addProperty("y", facing.getValue());
                }
                hatchVariants.add("facing=" + facing.getKey() + ",waterlogged=" + waterlogged, model);
            }
        }
        put("assets/" + MOD_ID + "/blockstates/fluid_hatch.json", object("variants", hatchVariants));
        put("assets/" + MOD_ID + "/models/item/fluid_hatch.json", object("parent", MOD_ID + ":block/fluid_hatch"));

        JsonObject cauldronVariants = new JsonObject();
        cauldronVariants.add("level=1", object("model", MOD_ID + ":block/dragon_breath_cauldron_level1"));
        cauldronVariants.add("level=2", object("model", MOD_ID + ":block/dragon_breath_cauldron_level2"));
        cauldronVariants.add("level=3", object("model", MOD_ID + ":block/dragon_breath_cauldron_level3"));
        cauldronVariants.add("level=4", object("model", MOD_ID + ":block/dragon_breath_cauldron_full"));
        put("assets/" + MOD_ID + "/blockstates/dragon_breath_cauldron.json", object("variants", cauldronVariants));

        for (String color : COLORS) {
            generateFluidBlockModel(color + "_dye", MOD_ID + ":fluid/dye_still");
            put("assets/" + MOD_ID + "/models/item/" + color + "_dye_bucket.json", object("parent", MOD_ID + ":item/dye_bucket"));
        }
        generateFluidBlockModel("dragon_breath", MOD_ID + ":fluid/dragon_breath_still");
        put(
                "assets/" + MOD_ID + "/models/item/dragon_breath_bucket.json",
                model("minecraft:item/generated", "layer0", MOD_ID + ":item/dragon_breath_bucket"));
        put(
                "assets/" + MOD_ID + "/models/item/blaze_upgrade_smithing_template.json",
                model("minecraft:item/generated", "layer0", MOD_ID + ":item/blaze_upgrade_smithing_template"));
        put(
                "assets/" + MOD_ID + "/models/item/rare_blaze_package.json",
                model("create:item/package/custom_12x10", "2", MOD_ID + ":block/package/rare_blaze"));
        put(
                "assets/" + MOD_ID + "/models/item/rare_marble_gate_package.json",
                model("create:item/package/custom_12x10", "2", MOD_ID + ":block/package/rare_marble_gate"));
    }

    private void generateFluidBlockModel(String name, String particle) {
        put(
                "assets/" + MOD_ID + "/blockstates/" + name + ".json",
                object("variants", object("", object("model", MOD_ID + ":block/" + name))));
        put("assets/" + MOD_ID + "/models/block/" + name + ".json", object("textures", object("particle", particle)));
    }

    private void generateItemDefinitions() {
        List<String> items = new ArrayList<>();
        items.add("fluid_hatch");
        COLORS.forEach(color -> items.add(color + "_dye_bucket"));
        items.add("dragon_breath_bucket");
        items.add("rare_blaze_package");
        items.add("rare_marble_gate_package");
        items.add("blaze_upgrade_smithing_template");
        for (String item : items) {
            JsonObject model = object("type", "minecraft:model", "model", MOD_ID + ":item/" + item);
            if (item.endsWith("_dye_bucket")) {
                String color = item.substring(0, item.length() - "_dye_bucket".length());
                int tint = DyeColor.valueOf(color.toUpperCase(Locale.ROOT)).getTextureDiffuseColor() | 0xFF000000;
                model.add("tints", array(object("type", "minecraft:constant", "value", tint)));
            }
            put(
                    "assets/" + MOD_ID + "/items/" + item + ".json",
                    object("model", model));
        }
    }

    private void generateLanguage() {
        Map<String, String> lang = new TreeMap<>();
        lang.put("block." + MOD_ID + ".fluid_hatch", "Fluid Hatch");
        lang.put("block." + MOD_ID + ".dragon_breath", "Dragon's Breath");
        lang.put("block." + MOD_ID + ".dragon_breath_cauldron", "Dragon's Breath Cauldron");
        lang.put("fluid." + MOD_ID + ".dragon_breath", "Dragon's Breath");
        lang.put("item." + MOD_ID + ".dragon_breath_bucket", "Dragon's Breath Bucket");
        lang.put("item." + MOD_ID + ".rare_blaze_package", "Rare Blaze Package");
        lang.put("item." + MOD_ID + ".rare_marble_gate_package", "Rare Marble Gate Package");
        lang.put("item." + MOD_ID + ".blaze_upgrade_smithing_template", "Smithing Template");
        lang.put("itemGroup." + MOD_ID + ".base", "Create: Dragons Plus");
        lang.put("upgrade." + MOD_ID + ".blaze_upgrade", "Blaze Upgrade");
        lang.put("item." + MOD_ID + ".smithing_template.blaze_upgrade.applies_to", "Blaze Burner");
        lang.put("item." + MOD_ID + ".smithing_template.blaze_upgrade.ingredients", "Working blocks for Blaze");
        lang.put("item." + MOD_ID + ".smithing_template.blaze_upgrade.base_slot_description", "Add Blaze Burner");
        lang.put(
                "item." + MOD_ID + ".smithing_template.blaze_upgrade.additions_slot_description",
                "Add working blocks for Blaze");

        for (String color : COLORS) {
            String title = title(color) + " Dye";
            lang.put("block." + MOD_ID + "." + color + "_dye", title);
            lang.put("fluid." + MOD_ID + "." + color + "_dye", title);
            lang.put("item." + MOD_ID + "." + color + "_dye_bucket", title + " Bucket");
        }

        lang.put("block." + MOD_ID + ".fluid_hatch.tooltip.summary", "_Easily makes_ your _tank_ interactable with _fluid containers_.");
        lang.put("block." + MOD_ID + ".fluid_hatch.tooltip.condition1", "When R-Clicked");
        lang.put(
                "block." + MOD_ID + ".fluid_hatch.tooltip.behaviour1",
                "_Deposits_ _contained fluid_ from your _held item_ into the _tank_ it is placed on. If the held item cannot be emptied, it _fills_ the item from the _tank_ instead.");
        lang.put("block." + MOD_ID + ".fluid_hatch.tooltip.condition2", "When Sneak R-Clicked");
        lang.put(
                "block." + MOD_ID + ".fluid_hatch.tooltip.behaviour2",
                "_Fills_ your _held fluid container_ from the _tank_ first. If it cannot be filled, contained fluid is _deposited_ into the _tank_ instead.");

        lang.put("recipe." + MOD_ID + ".automated_coloring", "Automated Coloring");
        lang.put("recipe." + MOD_ID + ".fan_coloring", "Bulk Coloring");
        lang.put("recipe." + MOD_ID + ".fan_coloring.fan", "Fan behind Liquid Dye");
        lang.put("recipe." + MOD_ID + ".fan_freezing", "Bulk Freezing");
        lang.put("recipe." + MOD_ID + ".fan_freezing.fan", "Fan behind Powder Snow");
        lang.put("recipe." + MOD_ID + ".fan_sanding", "Bulk Sanding");
        lang.put("recipe." + MOD_ID + ".fan_sanding.fan", "Fan behind Quicksand");
        lang.put("recipe." + MOD_ID + ".fan_ending", "Bulk Ending");
        lang.put("recipe." + MOD_ID + ".fan_ending.fan", "Fan behind Dragon Head");

        lang.put("create.item_attributes." + MOD_ID + ".endable", "can be Ended");
        lang.put("create.item_attributes." + MOD_ID + ".endable.inverted", "cannot be Ended");
        lang.put("create.item_attributes." + MOD_ID + ".freezable", "can be Frozen");
        lang.put("create.item_attributes." + MOD_ID + ".freezable.inverted", "cannot be Frozen");
        lang.put("create.item_attributes." + MOD_ID + ".sandable", "can be Sanded");
        lang.put("create.item_attributes." + MOD_ID + ".sandable.inverted", "cannot be Sanded");
        lang.put("create.item_attributes." + MOD_ID + ".stainable", "can be Stained");
        lang.put("create.item_attributes." + MOD_ID + ".stainable.inverted", "cannot be Stained");

        addPonderLanguage(lang);

        JsonObject json = new JsonObject();
        lang.forEach(json::addProperty);
        put("assets/" + MOD_ID + "/lang/en_us.json", json);
        generateForeignLanguages(lang);
    }

    private void generateForeignLanguages(Map<String, String> english) {
        String projectDirectory = System.getProperty("create_dragons_plus.project-dir");
        if (projectDirectory == null) {
            throw new IllegalStateException("Missing create_dragons_plus.project-dir datagen property");
        }
        Path sourceDirectory = Path.of(projectDirectory)
                .resolve("src/main/translations/assets/" + MOD_ID + "/lang");
        try (var paths = Files.list(sourceDirectory)) {
            paths.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .forEach(path -> generateForeignLanguage(english, path));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read translation sources from " + sourceDirectory, exception);
        }
    }

    private void generateForeignLanguage(Map<String, String> english, Path source) {
        JsonObject foreign;
        try (var reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) {
                throw new IllegalStateException("Translation source is not a JSON object: " + source);
            }
            foreign = parsed.getAsJsonObject();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read translation source " + source, exception);
        }

        JsonObject merged = new JsonObject();
        Map<String, String> untranslated = new TreeMap<>();
        english.forEach((key, fallback) -> {
            JsonElement translated = foreign.get(key);
            if (translated != null && translated.isJsonPrimitive() && translated.getAsJsonPrimitive().isString()) {
                merged.add(key, translated.deepCopy());
            } else {
                untranslated.put(key, fallback);
            }
        });
        if (!untranslated.isEmpty()) {
            merged.addProperty("_comment.unlocalized", "Remove this line after finishing localization.");
            untranslated.forEach(merged::addProperty);
        }

        String locale = source.getFileName().toString().replaceFirst("\\.json$", "");
        put("assets/" + MOD_ID + "/lang/" + locale + ".json", merged);
    }

    private static void addPonderLanguage(Map<String, String> lang) {
        lang.put(MOD_ID + ".ponder.bulk_coloring.header", "Bulk Coloring");
        lang.put(MOD_ID + ".ponder.bulk_coloring.text_1", "Air Flows passing through Liquid Dye create a Coloring Setup");
        lang.put(
                MOD_ID + ".ponder.bulk_coloring.text_2",
                "Like Bulk Blasting and Bulk Smoking, Bulk Coloring is automatically compatible with certain crafting recipes using dyes");
        lang.put(MOD_ID + ".ponder.bulk_coloring.text_3", "Coloring Air Flow can also affect entities, changing their color.");
        lang.put(MOD_ID + ".ponder.bulk_coloring.text_4", "Equipped armor will also be colored.");
        lang.put(MOD_ID + ".ponder.bulk_ending.header", "Bulk Ending");
        lang.put(
                MOD_ID + ".ponder.bulk_ending.text_1",
                "Air Flows passing through Dragon Head or Dragon Breath create an Ending Setup");
        lang.put(MOD_ID + ".ponder.bulk_freezing.header", "Bulk Freezing");
        lang.put(MOD_ID + ".ponder.bulk_freezing.text_1", "Air Flows passing through Powder Snow create a Freezing Setup");
        lang.put(MOD_ID + ".ponder.bulk_sanding.header", "Bulk Sanding");
        lang.put(
                MOD_ID + ".ponder.bulk_sanding.text_1",
                "Air Flows passing through Bulk Sanding Catalysts (Example: Quicksand) create a Sanding Setup");
        lang.put(
                MOD_ID + ".ponder.bulk_sanding.text_2",
                "Like Bulk Blasting and Bulk Smoking, Bulk Sanding is automatically compatible with Sand Paper polishing recipes");
        lang.put(MOD_ID + ".ponder.dragon_breath_fluid.header", "Dragon's Breath Fluid");
        lang.put(MOD_ID + ".ponder.dragon_breath_fluid.text_1", "Dragon's Breath supports Cauldron behaviour");
        lang.put(
                MOD_ID + ".ponder.dragon_breath_fluid.text_2",
                "When flowing Dragon's Breath meets lava, it forms End Stone");
        lang.put(MOD_ID + ".ponder.dye_fluids.header", "Liquid Dye");
        lang.put(MOD_ID + ".ponder.dye_fluids.text_1", "Mixing dye with water creates Liquid Dye");
        lang.put(MOD_ID + ".ponder.dye_fluids.text_2", "Heated mixing can recover the dye item from the fluid");
        lang.put(MOD_ID + ".ponder.dye_fluids.text_3", "Items and living entities touching Liquid Dye are colored");
        lang.put(MOD_ID + ".ponder.dye_fluids.text_4", "When Liquid Dye meets lava, it forms matching concrete");
    }

    private void generateRecipes() {
        generateCraftingRecipes();
        generateDyeMixingRecipes();
        generateDragonBreathRecipes();

        fanRecipe("freezing", "packed_ice_from_ice", "minecraft:ice", "minecraft:packed_ice");
        fanRecipe("freezing", "blue_ice_from_packed_ice", "minecraft:packed_ice", "minecraft:blue_ice");
        fanRecipe("freezing", "breeze_rod_from_blaze_rod", "minecraft:blaze_rod", "minecraft:breeze_rod");
        fanRecipe("freezing", "slime_ball_from_magma_cream", "minecraft:magma_cream", "minecraft:slime_ball");

        fanRecipe("ending", "chorus_fruit_from_apple", "minecraft:apple", "minecraft:chorus_fruit");
        fanRecipe("ending", "end_stone_from_cobblestone", "minecraft:cobblestone", "minecraft:end_stone");
        fanRecipe("ending", "end_stone_bricks_from_stone_bricks", "minecraft:stone_bricks", "minecraft:end_stone_bricks");
        fanRecipe(
                "ending",
                "end_stone_brick_slab_from_stone_brick_slab",
                "minecraft:stone_brick_slab",
                "minecraft:end_stone_brick_slab");
        fanRecipe(
                "ending",
                "end_stone_brick_stairs_from_stone_brick_stairs",
                "minecraft:stone_brick_stairs",
                "minecraft:end_stone_brick_stairs");
        fanRecipe(
                "ending",
                "end_stone_brick_wall_from_stone_brick_wall",
                "minecraft:stone_brick_wall",
                "minecraft:end_stone_brick_wall");
        fanRecipe("ending", "phantom_membrane_from_leathers", "#c:leathers", "minecraft:phantom_membrane");
    }

    private void generateCraftingRecipes() {
        JsonObject hatch = withFeature("block/fluid_hatch");
        hatch.addProperty("type", "minecraft:crafting_shapeless");
        hatch.addProperty("category", "misc");
        hatch.add("ingredients", array("#c:ingots/copper", "create:item_drain"));
        hatch.add("result", object("count", 1, "id", MOD_ID + ":fluid_hatch"));
        put(data("recipe/crafting/fluid_hatch.json"), hatch);

        JsonObject template = withFeature("item/blaze_upgrade_smithing_template");
        template.addProperty("type", "minecraft:crafting_shaped");
        template.addProperty("category", "misc");
        template.add("pattern", array("btb", "bnb", "bbb"));
        template.add("key", object("b", "minecraft:blaze_rod", "n", "minecraft:netherrack", "t", MOD_ID + ":blaze_upgrade_smithing_template"));
        template.add("result", object("count", 2, "id", MOD_ID + ":blaze_upgrade_smithing_template"));
        put(data("recipe/crafting/blaze_upgrade_smithing_template.json"), template);
    }

    private void generateDyeMixingRecipes() {
        for (String color : COLORS) {
            String fluid = MOD_ID + ":" + color + "_dye";
            JsonObject fromItem = withFeature("fluid/dye");
            fromItem.addProperty("type", "create:mixing");
            fromItem.add("ingredients", array("#" + MOD_ID + ":dyes/minecraft/" + color));
            fromItem.add(
                    "fluid_ingredients",
                    array(fluidStack("minecraft:water", FluidUnits.QUARTER_BUCKET)));
            fromItem.add("fluid_results", array(object("amount", FluidUnits.QUARTER_BUCKET, "id", fluid)));
            put(data("recipe/mixing/" + color + "_dye_from_item.json"), fromItem);

            JsonObject fromFluid = withFeature("fluid/dye");
            fromFluid.addProperty("type", "create:mixing");
            fromFluid.addProperty("heat_requirement", "heated");
            fromFluid.add("fluid_ingredients", array(fluidStack(fluid, FluidUnits.QUARTER_BUCKET)));
            fromFluid.add("results", array(object("id", "minecraft:" + color + "_dye")));
            put(data("recipe/mixing/" + color + "_dye_from_fluid.json"), fromFluid);
        }
    }

    private void generateDragonBreathRecipes() {
        JsonObject emptying = withFeature("fluid/dragon_breath");
        emptying.addProperty("type", "create:emptying");
        emptying.addProperty("ingredient", "minecraft:dragon_breath");
        emptying.add("fluid_result", object("amount", FluidUnits.QUARTER_BUCKET, "id", MOD_ID + ":dragon_breath"));
        emptying.add("result", object("id", "minecraft:glass_bottle"));
        put(data("recipe/emptying/dragon_breath.json"), emptying);

        JsonObject filling = withFeature("fluid/dragon_breath");
        filling.addProperty("type", "create:filling");
        filling.addProperty("ingredient", "minecraft:glass_bottle");
        filling.add("fluid_ingredient", fluidStack(MOD_ID + ":dragon_breath", FluidUnits.QUARTER_BUCKET));
        filling.add("result", object("id", "minecraft:dragon_breath"));
        put(data("recipe/filling/dragon_breath.json"), filling);
    }

    private void fanRecipe(String type, String name, String ingredient, String result) {
        put(
                data("recipe/" + type + "/" + name + ".json"),
                object("type", MOD_ID + ":" + type, "ingredients", array(ingredient), "results", array(object("id", result))));
    }

    private void generateTags() {
        put(tag("create_dragons_plus", "block/passive_block_freezers"), tagValues(
                "minecraft:snow_block",
                "minecraft:powder_snow",
                "minecraft:frosted_ice",
                "minecraft:ice",
                "minecraft:packed_ice",
                "minecraft:blue_ice"));
        put(tag("create_dragons_plus", "block/not_applicable_for_polishing"), tagValues());
        put(tag("create_dragons_plus", "block/fan_processing_catalysts/ending"), tagValues());
        put(
                tag("create_dragons_plus", "block/fan_processing_catalysts/sanding"),
                tagValues(object("id", "#c:sands", "required", false)));
        put(
                tag("create_dragons_plus", "fluid/fan_processing_catalysts/ending"),
                tagValues(MOD_ID + ":dragon_breath", MOD_ID + ":flowing_dragon_breath"));
        JsonArray swimmable = new JsonArray();
        swimmable.add(MOD_ID + ":dragon_breath");
        swimmable.add(MOD_ID + ":flowing_dragon_breath");
        put(tag("create_dragons_plus", "item/not_applicable_for_coloring"), tagValues());
        put(
                tag("create", "block/fan_transparent"),
                tagValues(
                        "minecraft:dragon_head",
                        "minecraft:dragon_wall_head",
                        "minecraft:sand",
                        "#create_dragons_plus:fan_processing_catalysts/sanding"));
        put(tag("create", "item/packages"), tagValues(MOD_ID + ":rare_blaze_package", MOD_ID + ":rare_marble_gate_package"));
        put(tag("minecraft", "block/mineable/pickaxe"), tagValues(MOD_ID + ":fluid_hatch"));

        JsonArray aggregateFluids = new JsonArray();
        JsonArray aggregateBuckets = new JsonArray();
        for (String color : COLORS) {
            String source = MOD_ID + ":" + color + "_dye";
            String flowing = MOD_ID + ":flowing_" + color + "_dye";
            swimmable.add(source);
            swimmable.add(flowing);
            put(tag(MOD_ID, "item/dyes/minecraft/" + color), tagValues("minecraft:" + color + "_dye"));

            put(tag("c", "fluid/dyes/" + color), tagValues(source, flowing));
            aggregateFluids.add("#c:dyes/" + color);
            String bucketTag = "#c:buckets/dye/" + color;
            put(tag("c", "item/buckets/dye/" + color), tagValues(MOD_ID + ":" + color + "_dye_bucket"));
            aggregateBuckets.add(bucketTag);
        }
        put(tag("c", "fluid/dyes"), tagValues(aggregateFluids));
        put(tag(MOD_ID, "fluid/swimmable"), tagValues(swimmable));
        put(tag("c", "fluid/dragon_breath"), tagValues(MOD_ID + ":dragon_breath", MOD_ID + ":flowing_dragon_breath"));
        put(tag("c", "item/buckets/dye"), tagValues(aggregateBuckets));
        put(tag("c", "item/buckets/dragon_breath"), tagValues(MOD_ID + ":dragon_breath_bucket"));

        JsonArray allBuckets = new JsonArray();
        allBuckets.add("#c:buckets/dye");
        allBuckets.add("#c:buckets/dragon_breath");
        put(tag("c", "item/buckets"), tagValues(allBuckets));
    }

    private void generateDataMaps() {
        JsonObject fluidValues = new JsonObject();
        JsonObject blockValues = new JsonObject();
        for (String color : COLORS) {
            String dye = "minecraft:" + color;
            fluidValues.addProperty(MOD_ID + ":" + color + "_dye", dye);
            fluidValues.addProperty(MOD_ID + ":flowing_" + color + "_dye", dye);
            blockValues.addProperty("minecraft:" + color + "_concrete", dye);
        }
        put(data("data_maps/fluid/fan_processing_catalysts/coloring.json"), object("replace", false, "values", fluidValues));
        put(data("data_maps/block/fan_processing_catalysts/coloring.json"), object("replace", false, "values", blockValues));
    }

    private void generateLootTables() {
        JsonObject pool = object(
                "bonus_rolls",
                0.0,
                "conditions",
                array(object("condition", "minecraft:survives_explosion")),
                "entries",
                array(object("type", "minecraft:item", "name", MOD_ID + ":fluid_hatch")),
                "rolls",
                1.0);
        put(
                data("loot_table/blocks/fluid_hatch.json"),
                object("type", "minecraft:block", "pools", array(pool), "random_sequence", MOD_ID + ":blocks/fluid_hatch"));
        JsonObject cauldronPool = object(
                "bonus_rolls",
                0.0,
                "conditions",
                array(object("condition", "minecraft:survives_explosion")),
                "entries",
                array(object("type", "minecraft:item", "name", "minecraft:cauldron")),
                "rolls",
                1.0);
        put(
                data("loot_table/blocks/dragon_breath_cauldron.json"),
                object(
                        "type",
                        "minecraft:block",
                        "pools",
                        array(cauldronPool),
                        "random_sequence",
                        MOD_ID + ":blocks/dragon_breath_cauldron"));
    }

    private void generateAdvancements() {
        recipeAdvancement(
                "fluid_hatch",
                "block/fluid_hatch",
                "has_item_drain",
                "create:item_drain",
                MOD_ID + ":crafting/fluid_hatch");
        recipeAdvancement(
                "blaze_upgrade_smithing_template",
                "item/blaze_upgrade_smithing_template",
                "has_template",
                MOD_ID + ":blaze_upgrade_smithing_template",
                MOD_ID + ":crafting/blaze_upgrade_smithing_template");
    }

    private void recipeAdvancement(String name, String feature, String itemCriterion, String item, String recipe) {
        JsonObject criteria = new JsonObject();
        criteria.add(
                itemCriterion,
                object(
                        "conditions",
                        object("items", array(object("items", item))),
                        "trigger",
                        "minecraft:inventory_changed"));
        criteria.add(
                "has_the_recipe",
                object("conditions", object("recipe", recipe), "trigger", "minecraft:recipe_unlocked"));
        JsonObject advancement = withFeature(feature);
        advancement.add("criteria", criteria);
        advancement.add("requirements", array(array("has_the_recipe", itemCriterion)));
        advancement.add("rewards", object("recipes", array(recipe)));
        advancement.addProperty("sends_telemetry_event", true);
        put(data("advancement/recipes/" + name + ".json"), advancement);
    }

    private JsonObject withFeature(String feature) {
        JsonObject result = new JsonObject();
        result.add(
                "fabric:load_conditions",
                array(object("condition", MOD_ID + ":config_feature", "feature", MOD_ID + ":" + feature)));
        return result;
    }

    private static JsonObject fluidStack(String fluid, long amount) {
        return object("type", "fluid_stack", "amount", amount, "fluid", fluid);
    }

    private static JsonObject model(String parent, String textureKey, String texture) {
        return object("parent", parent, "textures", object(textureKey, texture));
    }

    private static JsonObject tagValues(Object... values) {
        return object("values", array(values));
    }

    private static JsonObject tagValues(JsonArray values) {
        return object("values", values);
    }

    private static JsonArray array(Object... values) {
        JsonArray array = new JsonArray();
        for (Object value : values) {
            add(array, value);
        }
        return array;
    }

    private static JsonObject object(Object... values) {
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("JSON object entries must be key/value pairs");
        }
        JsonObject object = new JsonObject();
        for (int i = 0; i < values.length; i += 2) {
            String key = (String) values[i];
            add(object, key, values[i + 1]);
        }
        return object;
    }

    private static void add(JsonArray array, Object value) {
        if (value instanceof JsonElement json) {
            array.add(json);
        } else if (value instanceof Number number) {
            array.add(number);
        } else if (value instanceof Boolean bool) {
            array.add(bool);
        } else {
            array.add(String.valueOf(value));
        }
    }

    private static void add(JsonObject object, String key, Object value) {
        if (value instanceof JsonElement json) {
            object.add(key, json);
        } else if (value instanceof Number number) {
            object.addProperty(key, number);
        } else if (value instanceof Boolean bool) {
            object.addProperty(key, bool);
        } else {
            object.addProperty(key, String.valueOf(value));
        }
    }

    private static String title(String value) {
        StringBuilder result = new StringBuilder();
        for (String word : value.split("_")) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        return result.toString();
    }

    private static String data(String path) {
        return "data/" + MOD_ID + "/" + path;
    }

    private static String tag(String namespace, String path) {
        return "data/" + namespace + "/tags/" + path + ".json";
    }

    private void put(String path, JsonElement json) {
        JsonElement previous = files.put(path, json);
        if (previous != null) {
            throw new IllegalStateException("Duplicate generated resource: " + path);
        }
    }
}
