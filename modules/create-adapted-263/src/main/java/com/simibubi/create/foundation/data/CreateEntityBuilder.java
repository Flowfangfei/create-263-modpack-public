package com.simibubi.create.foundation.data;

import com.simibubi.create.infrastructure.assets.ExternalCreateAssets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.Create;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import dev.engine_room.flywheel.lib.visualization.SimpleEntityVisualizer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;


@ParametersAreNonnullByDefault
public class CreateEntityBuilder<T extends Entity, P> extends EntityBuilder<T, P> {
	private static final Collection<CreateEntityBuilder<?, ?>> VISUALIZERS =
		Collections.synchronizedList(new ArrayList<>());


	@Nullable
	private NonNullSupplier<SimpleEntityVisualizer.Factory<T>> visualFactory;
	private Predicate<@NotNull T> renderNormally;

	public static <T extends Entity, P> EntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
		return (new CreateEntityBuilder<>(owner, parent, name, callback, factory, classification)).defaultLang();
	}

	public CreateEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
		super(owner, parent, name, callback, factory, classification);
	}

	public CreateEntityBuilder<T, P> visual(NonNullSupplier<SimpleEntityVisualizer.Factory<T>> visualFactory) {
		return visual(visualFactory, true);
	}

	public CreateEntityBuilder<T, P> visual(NonNullSupplier<SimpleEntityVisualizer.Factory<T>> visualFactory, boolean renderNormally) {
		return visual(visualFactory, entity -> renderNormally);
	}

	public CreateEntityBuilder<T, P> visual(NonNullSupplier<SimpleEntityVisualizer.Factory<T>> visualFactory, Predicate<@NotNull T> renderNormally) {
		if (this.visualFactory == null) {
			VISUALIZERS.add(this);
			System.err.println("[Create] queued Flywheel entity visualizer");
		}

		this.visualFactory = visualFactory;
		this.renderNormally = renderNormally;

		return this;
	}

	public static void registerAllVisualizers() {
		synchronized (VISUALIZERS) {
			Create.LOGGER.info("Registering {} queued entity visualizers", VISUALIZERS.size());
			VISUALIZERS.forEach(CreateEntityBuilder::registerVisualizer);
		}
	}

	private void registerVisualizer() {
		var visualFactory = this.visualFactory;
		if (visualFactory != null) {
			System.err.println("[Create] Registering Flywheel visualizer: " + getEntry());
			Predicate<@NotNull T> renderNormally = this.renderNormally;
			SimpleEntityVisualizer.<T>builder(getEntry())
				.factory(visualFactory.get())
				.skipVanillaRender(entity -> ExternalCreateAssets.shouldUseFlywheelVisuals()
					&& !renderNormally.test(entity))
				.apply();
		}
	}
}
