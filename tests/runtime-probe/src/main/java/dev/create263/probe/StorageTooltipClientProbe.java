package dev.create263.probe;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletionException;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.sdl.SDLKeyboard;

/** Runs the async recipe-search and rendered inventory paths with real Storage items. */
final class StorageTooltipClientProbe {
    private static final Path ROOT = Path.of(System.getProperty("create263.clientProbeRoot")).toAbsolutePath().normalize();
    private static final String WORLD = "three-addon-test-copy";
    private static final String DESCRIPTION = "tooltip.fxntstorage.holdForDescription";
    private static final String CONTENTS = "tooltip.fxntstorage.holdForContents";
    private static final String SUMMARY = "tooltip.fxntstorage.backpack.summary";
    private static final Map<String, Object> result = new LinkedHashMap<>();
    private static final Map<String, Object> hoverResults = new LinkedHashMap<>();
    private static final boolean baseline = "baseline".equals(System.getProperty("create263.storageTooltipPhase"));
    private static int ticks, titleTicks, mode;
    private static boolean disconnecting, finished;
    private static ItemStack backpack;
    private static HoverScreen screen;
    private static Map<String, Object> gathered;

    static void install() {
        require(ROOT.equals(FMLPaths.GAMEDIR.get().toAbsolutePath().normalize()), "Client game directory mismatch");
        require(Files.isRegularFile(ROOT.resolve(".create263-disposable-runtime")), "Client test marker missing");
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> tick());
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, RenderTooltipEvent.GatherComponents.class,
                StorageTooltipClientProbe::gather);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, RenderTooltipEvent.Pre.class, StorageTooltipClientProbe::beforeRender);
    }

    private static void require(boolean test, String message) {
        if (!test) throw new IllegalStateException(message);
    }

    private static void write() throws Exception {
        Files.writeString(ROOT.resolve("storage-tooltip-client-regression.json"),
                new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n");
    }

    private static Identifier storageId(String name) {
        return Identifier.fromNamespaceAndPath("fxntstorage", name);
    }

    @SuppressWarnings("unchecked")
    private static ItemStack backpack() {
        ItemStack stack = BuiltInRegistries.ITEM.getOptional(storageId("backpack")).orElseThrow().getDefaultInstance();
        var upgrades = (DataComponentType<List<String>>) BuiltInRegistries.DATA_COMPONENT_TYPE
                .getOptional(storageId("backpack_upgrades")).orElseThrow();
        stack.set(upgrades, List.of("backpack_magnet_upgrade"));
        return stack;
    }

    private static void search(Minecraft mc) {
        var book = new ClientRecipeBook();
        var output = new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(backpack));
        var ingredient = new SlotDisplay.ItemSlotDisplay(Items.PAPER);
        var table = new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE);
        book.add(new RecipeDisplayEntry(new RecipeDisplayId(0),
                new ShapelessCraftingRecipeDisplay(List.of(ingredient), output, table), OptionalInt.empty(),
                RecipeBookCategories.CRAFTING_EQUIPMENT, Optional.empty()));
        book.rebuildCollections();
        require(book.getCollections().size() == 1, "Recipe fixture failed to build one real collection");
        SessionSearchTrees trees = new SessionSearchTrees();
        String query = Language.getInstance().getOrDefault(DESCRIPTION).formatted("shift");
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        var target = mc.gameRenderer.mainRenderTarget().getColorTextureView();
        try (var pass = encoder.createRenderPass(() -> "Storage recipe-search regression", target, Optional.empty())) {
            trees.updateRecipes(book, mc.level);
            var index = trees.recipes();
            require(index.search(query).contains(book.getCollections().getFirst()),
                    "The Chinese tooltip hint was not indexed by the recipe search: " + query);
            require(!baseline, "The older Storage JAR did not reproduce the reported crash in the guarded render pass");
            result.put("recipe_tooltip_search_hit", query);
            result.put("recipe_search_joined_with_render_pass_open", true);
        } catch (CompletionException failure) {
            if (!baseline || !isReportedFailure(failure)) throw failure;
            result.put("baseline_reported_render_pass_error", failure.getCause().toString());
        }
    }

    private static boolean isReportedFailure(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            String message = cause.getMessage();
            if (message != null && message.toLowerCase(java.util.Locale.ROOT).contains("render pass")) {
                boolean backpackPath = false;
                boolean searchPath = false;
                for (var frame : cause.getStackTrace()) {
                    backpackPath |= frame.getClassName().endsWith(".BackpackItem") && frame.getMethodName().equals("appendHoverText");
                    searchPath |= frame.getClassName().endsWith(".SessionSearchTrees");
                }
                if (backpackPath && searchPath) return true;
            }
        }
        return false;
    }

    private static boolean backpackEvent(ItemStack stack) {
        return screen != null && stack == backpack;
    }

    private static void gather(RenderTooltipEvent.GatherComponents event) {
        if (!backpackEvent(event.getItemStack())) return;
        var texts = event.getTooltipElements().stream().flatMap(element -> element.left().stream())
                .map(FormattedText::getString).toList();
        var components = event.getTooltipElements().stream().flatMap(element -> element.right().stream())
                .map(element -> element.getClass().getSimpleName()).toList();
        gathered = new LinkedHashMap<>();
        gathered.put("text", texts);
        gathered.put("data_components", components);
        gathered.put("max_width", event.getMaxWidth());
    }

    private static void beforeRender(RenderTooltipEvent.Pre event) {
        if (!backpackEvent(event.getItemStack()) || gathered == null) return;
        gathered.put("rendered_components", event.getComponents().stream()
                .map(element -> element.getClass().getSimpleName()).toList());
        gathered.put("rendered_widths", event.getComponents().stream().map(element -> element.getWidth(event.getFont())).toList());
    }

    private static void verifyHover(Minecraft mc) {
        require(gathered != null && gathered.containsKey("rendered_components"), "No rendered tooltip for mode " + mode);
        String text = gathered.get("text").toString();
        String components = gathered.get("rendered_components").toString();
        String description = Component.translatable(DESCRIPTION, mode == 1 ? "§fShift" : "§7Shift").getString();
        String contents = Component.translatable(CONTENTS, mode == 2 ? "§fCtrl" : "§7Ctrl").getString();
        String summary = Component.translatable(SUMMARY).getString().replace("_", "");
        require(text.contains(description) == (mode != 2), "Wrong Shift hint visibility: " + text);
        require(text.contains(contents) == (mode != 1), "Wrong Ctrl hint visibility: " + text);
        require(text.contains(summary) == (mode == 1), "Wrong summary visibility: " + text);
        require(components.contains("BackpackClientTooltip") == (mode == 2), "Wrong contents preview visibility: " + components);
        if (mode == 1) require((int) gathered.get("max_width") == 200, "Shift tooltip width limit missing");
        hoverResults.put(List.of("ordinary", "shift", "ctrl").get(mode), gathered);
        Screenshot.grab(mc, false);
    }

    private static void tick() {
        if (finished) return;
        var mc = Minecraft.getInstance();
        try {
            if (disconnecting) {
                if (!(mc.gui.screen() instanceof TitleScreen) || mc.level != null || mc.getSingleplayerServer() != null) return;
                if (++titleTicks == 20) {
                    result.put("saved_and_returned_to_title", true);
                    result.put("passed", true);
                    write();
                    finished = true;
                    mc.stop();
                }
                return;
            }
            if (mc.level == null || mc.player == null || mc.gui.overlay() != null) return;
            var server = mc.getSingleplayerServer();
            require(server != null && server.getWorldPath(LevelResource.ROOT).toRealPath()
                    .equals(ROOT.resolve("saves").resolve(WORLD).toRealPath()), "Client world path mismatch");
            if (++ticks == 20) {
                require("zh_cn".equals(mc.getLanguageManager().getSelected()), "Test requires zh_cn");
                require(Language.getInstance().has(SUMMARY), "Storage tooltip translations missing");
                result.put("language", "zh_cn");
                backpack = backpack();
                search(mc);
                if (baseline) {
                    disconnect(mc);
                    return;
                }
                mc.getConnection().searchTrees().recipes();
                result.put("live_session_recipe_index_joined", true);
                screen = new HoverScreen(mc);
                mc.gui.setScreen(screen);
            }
            if (ticks >= 45 && (ticks - 45) % 25 == 0) {
                verifyHover(mc);
                gathered = null;
                if (++mode == 3) {
                    result.put("inventory_hover", hoverResults);
                    screen.restore();
                    disconnect(mc);
                }
            }
        } catch (Throwable error) {
            error.printStackTrace();
            result.put("passed", false);
            result.put("error", error.toString());
            try {
                if (screen != null) screen.restore();
                write();
            } catch (Exception secondary) {
                error.addSuppressed(secondary);
            }
            finished = true;
            mc.stop();
        }
    }

    private static void disconnect(Minecraft mc) {
        disconnecting = true;
        mc.disconnectWithSavingScreen();
        mc.gui.setScreen(new TitleScreen());
    }

    private static final class HoverScreen extends InventoryScreen {
        private final Slot slot;
        private final ItemStack original;

        private HoverScreen(Minecraft mc) {
            super(mc.player);
            slot = mc.player.inventoryMenu.getSlot(36);
            original = slot.getItem();
            slot.set(backpack);
        }

        private void restore() {
            slot.set(original);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float partialTick) {
            // Scope the SDL key state to this inventory extraction, including its native hover path.
            var keys = SDLKeyboard.SDL_GetKeyboardState();
            int[] codes = {InputConstants.KEY_LSHIFT, InputConstants.KEY_RSHIFT, InputConstants.KEY_LCONTROL, InputConstants.KEY_RCONTROL};
            byte[] originalKeys = new byte[codes.length];
            for (int i = 0; i < codes.length; i++) originalKeys[i] = keys.get(codes[i]);
            try {
                for (int i = 0; i < codes.length; i++) keys.put(codes[i], (byte) 0);
                if (mode == 1) keys.put(InputConstants.KEY_LSHIFT, (byte) 1);
                if (mode == 2) keys.put(InputConstants.KEY_LCONTROL, (byte) 1);
                super.extractRenderState(graphics, getLeftPos() + slot.x + 8, getTopPos() + slot.y + 8, partialTick);
            } finally {
                for (int i = 0; i < codes.length; i++) keys.put(codes[i], originalKeys[i]);
            }
        }
    }
}
