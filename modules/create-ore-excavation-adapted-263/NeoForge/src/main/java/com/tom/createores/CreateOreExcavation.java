package com.tom.createores;

import java.util.function.Supplier;

import org.slf4j.Logger;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;

import com.tom.createores.client.CCClientInit;
import com.tom.createores.client.ClientRegistration;
import com.tom.createores.components.OreVeinAtlasDataComponent;
import com.tom.createores.jm.JMEventListener;
import com.tom.createores.network.NetworkHandler;
import com.tom.createores.recipe.DrillingRecipe;
import com.tom.createores.recipe.ExtractorRecipe;
import com.tom.createores.recipe.VeinRecipe;

@Mod(CreateOreExcavation.MODID)
public class CreateOreExcavation {
	public static final String MODID = "createoreexcavation";
	public static final Logger LOGGER = LogUtils.getLogger();

	/** Bridges persisted recipe locations to the keyed recipe map introduced in 1.21.11. */
	public static ResourceKey<Recipe<?>> recipeKey(Identifier id) {
		return ResourceKey.create(Registries.RECIPE, id);
	}

	public static boolean journeyMap, kubeJSCreate;

	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
	private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);
	private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

	public static final RecipeTypeGroup<DrillingRecipe> DRILLING_RECIPES = recipe("drilling", () -> new DrillingRecipe.Serializer().serializer());
	public static final RecipeTypeGroup<ExtractorRecipe> EXTRACTING_RECIPES = recipe("extracting", () -> new ExtractorRecipe.Serializer().serializer());
	public static final RecipeTypeGroup<VeinRecipe> VEIN_RECIPES = recipe("vein", () -> new VeinRecipe.Serializer().serializer());

	public static final TagKey<Item> DRILL_TAG = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MODID, "drills"));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<OreVeinAtlasDataComponent>> ORE_VEIN_ATLAS_DATA_COMPONENT = DATA_COMPONENTS.register("ore_vein_altas_data", () -> DataComponentType.<OreVeinAtlasDataComponent>builder().persistent(OreVeinAtlasDataComponent.CODEC).build());
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ORE_VEIN_FINDER_FILTERED_COMPONENT = DATA_COMPONENTS.register("ore_vein_finder_filtered", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());

	public static final Supplier<AttachmentType<OreDataAttachment>> ORE_DATA = ATTACHMENT_TYPES.register(
			"ore_vein", () -> AttachmentType.serializable(OreDataAttachment::new).build());

	public CreateOreExcavation(ModContainer mc, IEventBus bus) {
		bus.addListener(this::setup);
		bus.addListener(this::doClientStuff);
		bus.addListener(this::enqueueIMC);
		bus.addListener(this::processIMC);
		bus.addListener(this::registerCapabilities);

		mc.registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
		mc.registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
		bus.register(ForgeConfig.class);
		bus.register(NetworkHandler.class);

		journeyMap = false; // Optional integration needs its own 26.3 API port.
		kubeJSCreate = false; // Optional integration needs its own 26.3 API port.

		if (CreateOreExcavation.isModLoaded("computercraft") && FMLEnvironment.getDist() == Dist.CLIENT) {
			CCClientInit.init(bus);
		}

		// Register ourselves for server and other game events we are interested in
		NeoForge.EVENT_BUS.register(this);

		Registration.register(bus);
        NeoForge.EVENT_BUS.addListener(com.tom.createores.block.entity.OreExcavationRuntimeChecks::onServerStarted);
        NeoForge.EVENT_BUS.addListener(com.tom.createores.block.entity.OreExcavationRuntimeChecks::onServerTick);

		RECIPE_SERIALIZER.register(bus);
		TYPE_REGISTER.register(bus);
		ATTACHMENT_TYPES.register(bus);
		DATA_COMPONENTS.register(bus);
	}

	private static <T extends Recipe<?>> RecipeTypeGroup<T> recipe(String name, Supplier<RecipeSerializer<T>> serializer) {
		RecipeTypeGroup<T> rg = new RecipeTypeGroup<>(Identifier.fromNamespaceAndPath(MODID, name));
		rg.recipeType = TYPE_REGISTER.register(name, () -> RecipeType.simple(Identifier.fromNamespaceAndPath(MODID, name)));
		rg.serializer = RECIPE_SERIALIZER.register(name, serializer);
		return rg;
	}

	public static class RecipeTypeGroup<T extends Recipe<?>> {
		private DeferredHolder<RecipeSerializer<?>, RecipeSerializer<T>> serializer;
		private DeferredHolder<RecipeType<?>, RecipeType<T>> recipeType;
		private Identifier id;

		public RecipeTypeGroup(Identifier id) {
			this.id = id;
		}

		public RecipeType<T> getRecipeType() {
			return recipeType.get();
		}

		public RecipeSerializer<T> getSerializer() {
			return serializer.get();
		}

		public Identifier getId() {
			return id;
		}
	}

	private void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("Create Ore Excavation starting");
		Registration.postRegister();
		//BlockStressValues.IMPACTS.register(Registration.KINETIC_INPUT.get(), AllConfigs.server().kinetics.stressValues.getImpact(null));
		COECommand.init();
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		event.enqueueWork(ClientRegistration::register);
		if (journeyMap)
			JMEventListener.register();
	}

	private void enqueueIMC(final InterModEnqueueEvent event) {
	}

	private void processIMC(final InterModProcessEvent event) {
	}

	@SubscribeEvent
    public void syncVeinRecipes(OnDatapackSyncEvent event) {
        event.sendRecipes(VEIN_RECIPES.getRecipeType(), DRILLING_RECIPES.getRecipeType(), EXTRACTING_RECIPES.getRecipeType());
    }

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent evt) {
		COECommand.register(evt);
	}

	@SubscribeEvent
	public void reloadEvent(TagsUpdatedEvent evt) {
		OreVeinGenerator.invalidate();
	}

	public static boolean isModLoaded(String id) {
		return ModList.get().isLoaded(id);
	}

	private void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.Item.BLOCK, Registration.IO_TILE.get(), (be, side) -> be.getCapability(Capabilities.Item.BLOCK, side));
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, Registration.IO_TILE.get(), (be, side) -> be.getCapability(Capabilities.Fluid.BLOCK, side));
		event.registerBlockEntity(Capabilities.Energy.BLOCK, Registration.IO_TILE.get(), (be, side) -> be.getCapability(Capabilities.Energy.BLOCK, side));
	}
}
