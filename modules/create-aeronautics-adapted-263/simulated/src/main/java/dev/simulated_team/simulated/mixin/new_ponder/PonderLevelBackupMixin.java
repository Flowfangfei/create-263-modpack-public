package dev.simulated_team.simulated.mixin.new_ponder;

import dev.simulated_team.simulated.ponder.LegacyPonderTemplatePositions;
import dev.simulated_team.simulated.ponder.LegacyPonderTemplateClientDefaults;
import net.createmod.ponder.api.client.level.PonderLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/** Adapts only Ponder's in-memory backups; ordinary level saves never use this class. */
@Mixin(PonderLevel.class)
public abstract class PonderLevelBackupMixin {
    @Shadow
    protected Map<BlockPos, CompoundTag> originalBlockEntities;

    @Inject(method = "createBackup()V", at = @At("RETURN"), require = 1)
    private void simulated$adaptLegacyBackupData(final CallbackInfo ci) {
        for (final CompoundTag blockEntity : originalBlockEntities.values()) {
            LegacyPonderTemplatePositions.adapt(blockEntity);
            LegacyPonderTemplateClientDefaults.adapt(blockEntity);
        }
    }
}
