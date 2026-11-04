package dev.eriksonn.aeronautics.neoforge.content.fluids;
import com.tterrag.registrate.builders.FluidBuilder;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector3f;
import java.util.function.Supplier;
/** Common fluid physics and appearance data; client extensions are registered on the client bus. */
public abstract class AeroFluidType extends FluidType {
    private Vector3f fogColor;private Supplier<Float> fogDistance;
    private final Identifier stillTexture,flowingTexture;
    public AeroFluidType(Properties properties,Identifier still,Identifier flowing){super(properties);stillTexture=still;flowingTexture=flowing;}
    public static FluidBuilder.FluidTypeFactory create(int color,Supplier<Float> distance,Factory factory){return (properties,still,flowing)->{
        var type=factory.create(properties,still,flowing);type.fogColor=new net.createmod.catnip.api.theme.Color(color,false).asVectorF();type.fogDistance=distance;return type;
    };}
    public interface Factory{AeroFluidType create(Properties properties,Identifier still,Identifier flowing);}
    public Identifier getStillTexture(){return stillTexture;}
    public Identifier getFlowingTexture(){return flowingTexture;}
    public Vector3f getCustomFogColor(){return fogColor;}
    public float getFogDistanceModifier(){return fogDistance.get();}
}
