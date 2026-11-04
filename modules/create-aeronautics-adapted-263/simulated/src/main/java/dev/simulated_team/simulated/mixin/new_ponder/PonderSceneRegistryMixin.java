package dev.simulated_team.simulated.mixin.new_ponder;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.simulated_team.simulated.ponder.LegacyPonderTemplateData;
import dev.simulated_team.simulated.ponder.new_ponder_tooltip.NewPonderTooltipManager;
import net.createmod.ponder.api.client.scene.PonderScene;
import net.createmod.ponder.impl.client.registration.PonderSceneRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PonderSceneRegistry.class)
public class PonderSceneRegistryMixin {
	// Only resource-backed Ponder schematics reach this method; world/codec reads do not.
	@ModifyExpressionValue(
		method = "loadSchematic(Ljava/io/InputStream;)Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtIo;read(Ljava/io/DataInput;Lnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;"),
		require = 1
	)
	private static CompoundTag simulated$adaptLegacyPonderTemplate(final CompoundTag template) {
		return LegacyPonderTemplateData.adapt(template);
	}

	@Inject(method = "compile(Lnet/minecraft/resources/Identifier;)Ljava/util/List;", at = @At("RETURN"))
	private void simulated$compile(final Identifier id, final CallbackInfoReturnable<List<PonderScene>> cir) {
		NewPonderTooltipManager.setSceneWatched(cir.getReturnValue().getFirst().getSceneId());
	}
}
