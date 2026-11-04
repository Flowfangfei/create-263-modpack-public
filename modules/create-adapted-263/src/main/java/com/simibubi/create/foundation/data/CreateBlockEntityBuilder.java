package com.simibubi.create.foundation.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.infrastructure.assets.ExternalCreateAssets;
import com.simibubi.create.Create;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;



public class CreateBlockEntityBuilder<T extends BlockEntity, P> extends BlockEntityBuilder<T, P> {
	private static final Collection<CreateBlockEntityBuilder<?, ?>> VISUALIZERS =
		Collections.synchronizedList(new ArrayList<>());


	@Nullable
	private NonNullSupplier<SimpleBlockEntityVisualizer.Factory<T>> visualFactory;
	private Predicate<@NotNull T> renderNormally;

	private Collection<NonNullSupplier<? extends Collection<NonNullSupplier<? extends Block>>>> deferredValidBlocks =
		new ArrayList<>();

	public static <T extends BlockEntity, P> BlockEntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent,
																			 String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
		return new CreateBlockEntityBuilder<>(owner, parent, name, callback, factory);
	}

	protected CreateBlockEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
									   BlockEntityFactory<T> factory) {
		super(owner, parent, name, callback, factory);
	}

	public CreateBlockEntityBuilder<T, P> validBlocksDeferred(
		NonNullSupplier<? extends Collection<NonNullSupplier<? extends Block>>> blocks) {
		deferredValidBlocks.add(blocks);
		return this;
	}

	@Override
	protected BlockEntityType<T> createEntry() {
		deferredValidBlocks.stream()
			.map(Supplier::get)
			.flatMap(Collection::stream)
			.forEach(this::validBlock);
		return super.createEntry();
	}

	public CreateBlockEntityBuilder<T, P> displaySource(RegistryEntry<DisplaySource, ? extends DisplaySource> source) {
		this.onRegisterAfter(
			CreateRegistries.DISPLAY_SOURCE,
			type -> DisplaySource.BY_BLOCK_ENTITY.add(type, source.get())
		);
		return this;
	}

	public CreateBlockEntityBuilder<T, P> displayTarget(RegistryEntry<DisplayTarget, ? extends DisplayTarget> target) {
		this.onRegisterAfter(
			CreateRegistries.DISPLAY_TARGET,
			type -> DisplayTarget.BY_BLOCK_ENTITY.register(type, target.get())
		);
		return this;
	}

	public CreateBlockEntityBuilder<T, P> visual(
		NonNullSupplier<SimpleBlockEntityVisualizer.Factory<T>> visualFactory) {
		return visual(visualFactory, true);
	}

	public CreateBlockEntityBuilder<T, P> visual(
		NonNullSupplier<SimpleBlockEntityVisualizer.Factory<T>> visualFactory,
		boolean renderNormally) {
		return visual(visualFactory, be -> renderNormally);
	}

	public CreateBlockEntityBuilder<T, P> visual(
		NonNullSupplier<SimpleBlockEntityVisualizer.Factory<T>> visualFactory,
		Predicate<@NotNull T> renderNormally) {
		if (this.visualFactory == null) {
			VISUALIZERS.add(this);
			System.err.println("[Create] queued Flywheel block visualizer");
		}

		this.visualFactory = visualFactory;
		this.renderNormally = renderNormally;

		return this;
	}

	public static void registerAllVisualizers() {
		synchronized (VISUALIZERS) {
			Create.LOGGER.info("Registering {} queued block entity visualizers", VISUALIZERS.size());
			VISUALIZERS.forEach(CreateBlockEntityBuilder::registerVisualizer);
		}
	}

	private void registerVisualizer() {
		var visualFactory = this.visualFactory;
		if (visualFactory != null) {
			System.err.println("[Create] Registering Flywheel visualizer: " + getEntry());
			Predicate<@NotNull T> renderNormally = this.renderNormally;
			SimpleBlockEntityVisualizer.<T>builder(getEntry())
				.factory(visualFactory.get())
				.skipVanillaRender(be -> ExternalCreateAssets.shouldUseFlywheelVisuals()
					&& !renderNormally.test(be))
				.apply();
		}
	}
}
