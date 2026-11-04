package dev.simulated_team.simulated.mixin.accessor;
import net.createmod.catnip.api.client.outliner.Outline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(Outline.OutlineParams.class)
public interface OutlineParamsAccessor {
    @Accessor("lightmap") int simulated$lightmap();
    @Accessor("disableLineNormals") boolean simulated$disableLineNormals();
}
