package dev.simulated_team.simulated.neoforge.mixin.self_mixins;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
@Mixin(dev.simulated_team.simulated.content.entities.honey_glue.HoneyGlueEntity.class)
public abstract class HoneyGlueEntityMixin implements net.neoforged.neoforge.entity.IEntityWithComplexSpawn {
 @Shadow public abstract void addAdditionalSaveData(ValueOutput output);
 @Shadow public abstract void readAdditionalSaveData(ValueInput input);
 public void writeSpawnData(RegistryFriendlyByteBuf buffer){var output=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,buffer.registryAccess());addAdditionalSaveData(output);buffer.writeNbt(output.buildResult());}
 public void readSpawnData(RegistryFriendlyByteBuf buffer){var tag=java.util.Objects.requireNonNull(buffer.readNbt(),"Missing entity spawn NBT");readAdditionalSaveData(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,buffer.registryAccess(),tag));}
}
