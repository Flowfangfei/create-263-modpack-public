package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.registry.EntityEntry;
import com.george_vi.electroenergetics.simulation.infrastructure.detached_nodes.DetachedNodeEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class CEEEntityTypes {
    public static final EntityEntry<DetachedNodeEntity> DETACHED_NODE = create();

    private static EntityEntry<DetachedNodeEntity> create() {
        var id = CreateElectroEnergetics.rl("detached_node");
        EntityType<DetachedNodeEntity> type =
                EntityType.Builder.of(DetachedNodeEntity::new, MobCategory.MISC)
                        .sized(0.25f, 0.25f)
                        .clientTrackingRange(2)
                        .updateInterval(3)
                        .fireImmune()
                        .noSummon()
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, id));
        Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type);
        return new EntityEntry<>(id, () -> type);
    }

    public static void register() {}
}
