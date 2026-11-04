package com.george_vi.electroenergetics.events.datagen;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.function.Supplier;

public abstract class CEECriterionTriggerBase<T extends CEECriterionTriggerBase.Instance> extends SimpleCriterionTrigger<T> {
    private final Identifier id;
    public CEECriterionTriggerBase(String id) { this.id = CreateElectroEnergetics.rl(id); }
    public Identifier getId() { return id; }
    protected void trigger(ServerPlayer player, @Nullable List<Supplier<Object>> suppliers) {
        super.trigger(player, instance -> instance.test(suppliers));
    }
    public abstract static class Instance implements SimpleCriterionTrigger.SimpleInstance {
        protected abstract boolean test(@Nullable List<Supplier<Object>> suppliers);
    }
}
