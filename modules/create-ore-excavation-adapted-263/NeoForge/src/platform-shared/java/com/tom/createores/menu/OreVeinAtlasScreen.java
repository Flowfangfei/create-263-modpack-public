package com.tom.createores.menu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import com.tom.createores.CreateOreExcavation;
import com.tom.createores.client.ClientVeinRecipes;
import com.tom.createores.components.OreVeinAtlasDataComponent.OreVeinData;
import com.tom.createores.network.NetworkHandler;
import com.tom.createores.network.OreVeinAtlasClickPacket;
import com.tom.createores.network.OreVeinAtlasClickPacket.Option;
import com.tom.createores.recipe.VeinRecipe;
import com.tom.createores.util.DimChunkPos;
import com.tom.createores.util.NumberFormatter;
import com.tom.createores.util.ThreeState;

public class OreVeinAtlasScreen extends AbstractContainerScreen<OreVeinAtlasMenu> {
	private static final Identifier GUI_TEXTURES = Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "textures/gui/atlas.png");
	private static final Identifier EXCLUDE = Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "atlas_exclude");
	private static final Identifier TARGET = Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "atlas_target");
	private static final Identifier SHOW = Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "atlas_show");
	private static final Identifier HIDE = Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "atlas_hidden");

	protected static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
			Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "atlas_button"),
			Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "atlas_button_disabled"),
			Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "atlas_button_hovered")
			);

	protected static final WidgetSprites ACTION_BUTTON_SPRITES = new WidgetSprites(
			Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "atlas_action_button"),
			Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "atlas_action_button_disabled"),
			Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "atlas_action_button_hovered")
			);

	private static final Button.OnPress NULL_PRESS = b -> {};

	private List<Vein> veinsList = new ArrayList<>();
	private List<RecipeHolder<VeinRecipe>> veinTypesList = new ArrayList<>();
	private List<Vein> veinsListSorted = new ArrayList<>();
	private List<RecipeHolder<VeinRecipe>> veinTypesListSorted = new ArrayList<>();
	private Set<Identifier> excluded = new HashSet<>();
	private String target;
	private VeinsListWidget discovered;
	private VeinTypesListWidget veinTypes;
	private Component veinTypesTitle = Component.translatable("info.coe.atlas.vein_types");
	private PageButton backButton;
	private Vein selected;
	private boolean showHidden;
	private ToggleHideButton toggleHideButton;

	public OreVeinAtlasScreen(OreVeinAtlasMenu p_97741_, Inventory p_97742_, Component p_97743_) {
		super(p_97741_, p_97742_, p_97743_, 282, 182);
	}

	protected void renderBg(GuiGraphicsExtractor gr, float partial, int mx, int my) {
		gr.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 512, 256);
	}

	@Override
	protected void init() {
		super.init();
		int i = this.leftPos;
		int j = this.topPos;
		discovered = addRenderableWidget(new VeinsListWidget(i + 20, j + 24, 110, 140));
		veinTypes = addRenderableWidget(new VeinTypesListWidget(i + 150, j + 24, 110, 140));
		backButton = addRenderableWidget(new PageButton(i + 20 + 4, j + 24 + 140 - 12, false, p_98287_ -> this.pageBack(), true));
		toggleHideButton = addRenderableWidget(new ToggleHideButton(i + 118, j + 9, b -> toggleHide()));
		toggleHideButton.setTooltip(Tooltip.create(Component.translatable("tooltip.coe.atlas.show_hide")));
		backButton.active = false;
		discovered.list = () -> veinsListSorted;
		veinTypes.list = () -> veinTypesListSorted;
		fillLists();
		updateVeinsList();
		updateVeinTypesList();
		discovered.updateContent();
		veinTypes.updateContent();
	}

	private void toggleHide() {
		if (selected != null) {
			toggleHideButton.hideMode = selected.hidden = !selected.hidden;
			send2(Option.TOGGLE_HIDE, veinsList.indexOf(selected));
			updateVeinsList();
		} else {
			toggleHideButton.hideMode = showHidden = !showHidden;
			updateVeinsList();
		}
	}

	private void pageBack() {
		backButton.active = false;
		discovered.setVisible(true);
		selected = null;
		toggleHideButton.hideMode = showHidden;
		toggleHideButton.setTooltip(Tooltip.create(Component.translatable("tooltip.coe.atlas.show_hide")));
		toggleHideButton.setX(leftPos + 118);
		toggleHideButton.setY(topPos + 9);
	}

	public void setSelected(Vein selected) {
		this.selected = selected;
		toggleHideButton.hideMode = selected.hidden;
		toggleHideButton.setTooltip(Tooltip.create(Component.translatable("tooltip.coe.atlas.hide_vein")));
		toggleHideButton.setX(leftPos + 18);
		toggleHideButton.setY(topPos + 24);
	}

	private void updateVeinTypesList() {
		veinTypesListSorted.clear();
		veinTypesListSorted.addAll(veinTypesList);
		veinTypes.updateContent();
	}

	private void updateVeinsList() {
		veinsListSorted.clear();
		veinsList.stream().filter(e -> e.hidden == showHidden).forEach(veinsListSorted::add);
		discovered.updateContent();
	}

	private void fillLists() {
		veinsList.clear();
		veinTypesList.clear();
		excluded.clear();
		ItemStack is = menu.getHeldItem();
		var tag = is.get(CreateOreExcavation.ORE_VEIN_ATLAS_DATA_COMPONENT);
		if (tag == null)return;
		target = tag.target().map(Identifier::toString).orElse(null);

		for (Identifier v : tag.discovered()) {
			ClientVeinRecipes.byId(v).ifPresent(vr -> {
				if (vr.value() instanceof VeinRecipe) {
					veinTypesList.add((RecipeHolder<VeinRecipe>) vr);
				}
			});
		}

		for (Pair<DimChunkPos, OreVeinData> e : tag.veins()) {
			var vid = e.getSecond().id();
			var pos = e.getFirst();
			float size = e.getSecond().size();
			boolean hide = e.getSecond().hide();
			ClientVeinRecipes.byId(vid).ifPresent(vr -> {
				if (vr.value() instanceof VeinRecipe) {
					veinsList.add(new Vein(pos, (RecipeHolder<VeinRecipe>) vr, size, hide));
				}
			});
		}

		for (Identifier v : tag.exclude()) {
			excluded.add(v);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor st, int mouseX, int mouseY, float partialTicks) {
		renderBg(st, partialTicks, mouseX, mouseY);
		super.extractRenderState(st, mouseX, mouseY, partialTicks);

		if (selected != null) {
			st.pose().pushMatrix();
			st.pose().translate(this.leftPos + 60, this.topPos + 40);
			float f = 2f;
			st.pose().scale(f, f);
			st.item(getSelectedRecipe().getIcon(), 0, 0);
			st.pose().popMatrix();

			var t = getSelectedRecipe().veinName;
			st.text(font, t, this.leftPos + 75 - font.width(t) / 2, this.topPos + 80, 0xff404040, false);

			long size = getVeinSize();
			if (size == 0)
				t = Component.translatable("info.coe.atlas.vein_size.infinite");
			else
				t = Component.literal("~" + NumberFormatter.formatNumber(size));
			t = Component.translatable("info.coe.atlas.vein_size", t);
			st.text(font, t, this.leftPos + 15, this.topPos + 100, 0xff404040, false);

			t = Component.translatable("info.coe.atlas.location");
			st.text(font, t, this.leftPos + 15, this.topPos + 110, 0xff404040, false);
			t = Component.translatable("info.coe.atlas.location2", Math.round((selected.pos.x() * 16 + 8) / 10f) * 10, Math.round((selected.pos.z() * 16 + 8) / 10f) * 10);
			st.text(font, t, this.leftPos + 25, this.topPos + 120, 0xff404040, false);
			t = Component.translatable("info.coe.atlas.dimension", selected.pos.dimension().identifier().toString());
			st.text(font, t, this.leftPos + 15, this.topPos + 130, 0xff404040, false);
		}
	}

	private VeinRecipe getSelectedRecipe() {
		return selected.recipe.value();
	}

	private long getVeinSize() {
		if (getSelectedRecipe().isFinite() != ThreeState.NEVER) {
			if (getSelectedRecipe().isFinite() == ThreeState.DEFAULT && menu.isDefaultInfinite())return 0L;
			double mul = (getSelectedRecipe().getMaxAmount() - getSelectedRecipe().getMinAmount()) * selected.size + getSelectedRecipe().getMinAmount();
			long am = Math.round(mul * menu.getFiniteBase());
			return am;
		}
		return 0L;
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor gr, int pMouseX, int pMouseY) {
		gr.text(font, title, 75 - font.width(title) / 2, 16, 0xff404040, false);
		gr.text(font, veinTypesTitle, 205 - font.width(veinTypesTitle) / 2, 16, 0xff404040, false);
	}

	private static class Vein {
		private final DimChunkPos pos;
		private final RecipeHolder<VeinRecipe> recipe;
		private final float size;
		private boolean hidden;

		public Vein(DimChunkPos pos, RecipeHolder<VeinRecipe> recipe, float size, boolean hidden) {
			this.pos = pos;
			this.recipe = recipe;
			this.size = size;
			this.hidden = hidden;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((pos == null) ? 0 : pos.hashCode());
			result = prime * result + ((recipe == null) ? 0 : recipe.id().hashCode());
			result = prime * result + Float.floatToIntBits(size);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Vein other = (Vein) obj;
			if (pos == null) {
				if (other.pos != null) return false;
			} else if (!pos.equals(other.pos)) return false;
			if (recipe == null) {
				if (other.recipe != null) return false;
			} else if (!recipe.id().equals(other.recipe.id())) return false;
			if (Float.floatToIntBits(size) != Float.floatToIntBits(other.size)) return false;
			return true;
		}
	}

	private static record VeinWidgetHolder(VeinInfoButton btn) {}

	private class VeinsListWidget extends PagedListWidget<VeinWidgetHolder, Vein> {

		public VeinsListWidget(int x, int y, int w, int h) {
			super(x, y, w, h, 20);
		}

		@Override
		protected <W extends AbstractWidget> W addWidgetToGUI(W w) {
			return addRenderableWidget(w);
		}

		@Override
		protected VeinWidgetHolder makeElement(int x, int y) {
			return new VeinWidgetHolder(addRenderableWidget(new VeinInfoButton(x, y, 110)));
		}

		@Override
		protected void updateElement(VeinWidgetHolder element, Vein data) {
			element.btn.visible = data != null;
			if (data != null) {
				element.btn.setVein(data);
			}
		}
	}

	private class VeinTypeButton extends Button {
		protected RecipeHolder<VeinRecipe> recipe;

		private VeinTypeButton(int pX, int pY, int pWidth, int pHeight) {
			super(pX, pY, pWidth, pHeight, Component.empty(), NULL_PRESS, DEFAULT_NARRATION);
		}

		protected VeinRecipe getRecipe() {
			return recipe.value();
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor pGuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
			Minecraft minecraft = Minecraft.getInstance();
			pGuiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, BUTTON_SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
			int i = isHovered ? 0xFCFCA0 : 0xC0C0C0;
			this.renderScrollingText(pGuiGraphicsExtractor, minecraft.font, 20, i | Mth.ceil(this.alpha * 255.0F) << 24);
			renderItem(pGuiGraphicsExtractor);
		}

		protected void renderItem(GuiGraphicsExtractor pGuiGraphicsExtractor) {
			pGuiGraphicsExtractor.item(getRecipe().getIcon(), getX() + 1, getY() + 1);
		}

		protected void renderScrollingText(GuiGraphicsExtractor pGuiGraphicsExtractor, Font pFont, int pWidth, int pColor) {
			int i = this.getX() + pWidth;
			int j = this.getX() + this.getWidth();
			int available = Math.max(1, j - i);
            int overflow = Math.max(0, pFont.width(this.getMessage()) - available);
            double phase = (System.currentTimeMillis() % 6000L) / 6000.0;
            int shift = (int) Math.round(
                    overflow * (1.0 - Math.cos(2.0 * Math.PI * phase)) / 2.0);

            pGuiGraphicsExtractor.enableScissor(i, this.getY(), j, this.getY() + this.getHeight());
            pGuiGraphicsExtractor.text(
                    pFont, this.getMessage(), i - shift,
                    this.getY() + (this.getHeight() - 9) / 2, pColor);
            pGuiGraphicsExtractor.disableScissor();
		}

		public Tooltip createTooltip() {
			return null;
		}

		public void updateTooltip() {
			setTooltip(createTooltip());
		}

		@Override
		public void onPress(InputWithModifiers input) {
		}
	}

	private class ToggleHideButton extends Button {
		private boolean hideMode;

		private ToggleHideButton(int pX, int pY, Button.OnPress pOnPress) {
			super(pX, pY, 16, 16, Component.empty(), pOnPress, DEFAULT_NARRATION);
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor pGuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
			pGuiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, BUTTON_SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
			pGuiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, hideMode ? HIDE : SHOW, this.getX(), this.getY(), 16, 16);
		}
	}

	private class VeinInfoButton extends VeinTypeButton {
		private Vein vein;

		private VeinInfoButton(int pX, int pY, int pWidth) {
			super(pX, pY, pWidth, 20);
		}

		@Override
		public void onPress(InputWithModifiers input) {
			discovered.setVisible(false);
			backButton.active = true;
			setSelected(vein);
		}

		public void setVein(Vein vein) {
			this.vein = vein;
			recipe = vein.recipe;
			setMessage(getRecipe().getName());
		}
	}

	private class VeinTargetButton extends VeinTypeButton {
		private Tooltip TT_TARGET;
		private Tooltip TT_SWITCH_TARGET;
		private Tooltip TT_SET_TARGET;

		private VeinTargetButton(int pX, int pY, int pWidth) {
			super(pX, pY, pWidth, 10);
			TT_TARGET = Tooltip.create(Component.translatable("tooltip.coe.atlas.target"));
			TT_SWITCH_TARGET = Tooltip.create(Component.translatable("tooltip.coe.atlas.switch_target"));
			TT_SET_TARGET = Tooltip.create(Component.translatable("tooltip.coe.atlas.set_target"));
		}

		@Override
		protected void renderItem(GuiGraphicsExtractor pGuiGraphicsExtractor) {
			pGuiGraphicsExtractor.pose().pushMatrix();
			pGuiGraphicsExtractor.pose().translate(getX() + 1, getY() + 1);
			float f = 0.5f;
			pGuiGraphicsExtractor.pose().scale(f, f);
			pGuiGraphicsExtractor.item(getRecipe().getIcon(), 0, 0);
			pGuiGraphicsExtractor.pose().popMatrix();

			if (target != null && !target.isEmpty() && target.equals(recipe.id().identifier().toString())) {
				pGuiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, TARGET, this.getX() + 10, this.getY() + 1, 8, 8);
			}
		}

		@Override
		public Tooltip createTooltip() {
			if(target != null && !target.isEmpty()) {
				if (target.equals(recipe.id().identifier().toString()))
					return TT_TARGET;
				else
					return TT_SWITCH_TARGET;
			} else {
				return TT_SET_TARGET;
			}
		}

		@Override
		public void onPress(InputWithModifiers input) {
			if(target != null && !target.isEmpty()) {
				if (target.equals(recipe.id().identifier().toString()))target(null);
				else target(recipe);
			} else {
				target(recipe);
			}
		}
	}

	private class VeinExcludeButton extends Button {
		private RecipeHolder<VeinRecipe> data;
		private Tooltip TT_EXCLUDE;
		private Tooltip TT_INCLUDE;

		protected VeinExcludeButton(int pX, int pY) {
			super(pX, pY, 10, 10, Component.empty(), NULL_PRESS, DEFAULT_NARRATION);
			TT_EXCLUDE = Tooltip.create(Component.translatable("tooltip.coe.atlas.exclude"));
			TT_INCLUDE = Tooltip.create(Component.translatable("tooltip.coe.atlas.include"));
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor pGuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
			pGuiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, ACTION_BUTTON_SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());

			if (excluded.contains(data.id().identifier())) {
				pGuiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, EXCLUDE, this.getX() + 1, this.getY() + 1, 8, 8);
			}
		}

		@Override
		public void onPress(InputWithModifiers input) {
			if(excluded.contains(data.id().identifier())) {
				exclude(data, false);
			} else {
				exclude(data, true);
			}
		}

		public Tooltip createTooltip() {
			if(excluded.contains(data.id().identifier())) {
				return TT_EXCLUDE;
			} else {
				return TT_INCLUDE;
			}
		}

		public void updateTooltip() {
			setTooltip(createTooltip());
		}
	}

	private static record VeinTypeWidgetHolder(VeinTargetButton btn, VeinExcludeButton trg) {}

	private class VeinTypesListWidget extends PagedListWidget<VeinTypeWidgetHolder, RecipeHolder<VeinRecipe>> {

		public VeinTypesListWidget(int x, int y, int w, int h) {
			super(x, y, w, h, 10);
		}

		@Override
		protected <W extends AbstractWidget> W addWidgetToGUI(W w) {
			return addRenderableWidget(w);
		}

		@Override
		protected VeinTypeWidgetHolder makeElement(int x, int y) {
			return new VeinTypeWidgetHolder(
					addRenderableWidget(new VeinTargetButton(x, y, 100)),
					addRenderableWidget(new VeinExcludeButton(x + 100, y))
					);
		}

		@Override
		protected void updateElement(VeinTypeWidgetHolder element, RecipeHolder<VeinRecipe> data) {
			element.btn.visible = data != null;
			element.trg.visible = data != null;
			if (data != null) {
				element.btn.recipe = data;
				element.btn.setMessage(data.value().getName());
				element.btn.updateTooltip();
				element.trg.data = data;
				element.trg.updateTooltip();
			}
		}
	}

	public void exclude(RecipeHolder<VeinRecipe> data, boolean ex) {
		if (ex) {
			excluded.add(data.id().identifier());
			target = null;
			send(Option.ADD_EXCLUDE, data.id().identifier());
		} else {
			excluded.remove(data.id().identifier());
			send(Option.REMOVE_EXCLUDE, data.id().identifier());
		}
		veinTypes.updateContent();
	}

	private void send(Option opt, Identifier id) {
		NetworkHandler.sendDataToServer(new OreVeinAtlasClickPacket(opt, id));
	}

	private void send2(Option opt, int id) {
		NetworkHandler.sendDataToServer(new OreVeinAtlasClickPacket(opt, id));
	}

	public void target(RecipeHolder<VeinRecipe> data) {
		if (data != null) {
			target = data.id().identifier().toString();
			send(Option.SET_TARGET, data.id().identifier());
		} else {
			target = null;
			send(Option.REMOVE_TARGET, null);
		}
		veinTypes.updateContent();
	}
}
