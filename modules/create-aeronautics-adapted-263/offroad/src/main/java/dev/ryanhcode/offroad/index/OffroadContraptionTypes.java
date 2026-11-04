package dev.ryanhcode.offroad.index;

import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.contraptions.Contraption;
import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.content.contraptions.borehead_contraption.BoreheadBearingContraption;
import net.minecraft.core.Registry;

import java.util.function.Supplier;

public class OffroadContraptionTypes {

    public static final ContraptionType BOREHEAD_CONTRAPTION_TYPE = register("borehead_contraption", BoreheadBearingContraption::new);

    private static ContraptionType register(String name, Supplier<? extends Contraption> factory) {
        var type=new ContraptionType(factory);
        foundry.veil.platform.registry.RegistrationProvider.get(com.simibubi.create.api.registry.CreateBuiltInRegistries.CONTRAPTION_TYPE,Offroad.MOD_ID).register(name,()->type);
        return type;
    }

    public static void init() {}

}
