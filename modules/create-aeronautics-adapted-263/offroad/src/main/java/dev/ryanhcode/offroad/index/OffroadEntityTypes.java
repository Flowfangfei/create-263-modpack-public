package dev.ryanhcode.offroad.index;

import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;
import com.simibubi.create.content.contraptions.render.ContraptionVisual;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import com.tterrag.registrate.util.entry.EntityEntry;
import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.content.entities.BoreheadContraptionEntity;
import net.minecraft.world.entity.MobCategory;
import dev.simulated_team.simulated.index.SimEntityTypes.EntityLoaderData;

import static dev.simulated_team.simulated.index.SimEntityTypes.applyLoaderSpecificTransform;

public class OffroadEntityTypes {

    private static final SimulatedRegistrate REGISTRATE = Offroad.getRegistrate();

    public static final EntityEntry<BoreheadContraptionEntity> BOREHEAD_CONTRAPTION_ENTITY =
            REGISTRATE.entity("borehead_contraption_entity", BoreheadContraptionEntity::new, MobCategory.MISC)
                    .onRegister(type -> {
                if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
                    dev.simulated_team.simulated.compat.create.SimClientRegistration.entity(type, () -> ContraptionEntityRenderer::new);
                }
            })
                    .transform(builder -> applyLoaderSpecificTransform(builder,
                            new EntityLoaderData(20, 40, 1, 1, 0, false, true, false)))
                    .register();

    public static void init() {

    }

}
