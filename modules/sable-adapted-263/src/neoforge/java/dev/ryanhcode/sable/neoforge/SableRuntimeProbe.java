package dev.ryanhcode.sable.neoforge;

import com.google.gson.GsonBuilder;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3d;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/** Opt-in physical and persistence checks restricted to this project's isolated server. */
public final class SableRuntimeProbe {
    private static final String NAME="sable-adapted-physical-probe";
    private static int ticks;
    private static ServerSubLevel body;
    private static boolean restored;
    private static double initialY;
    private static final Map<String,Object> result=new LinkedHashMap<>();
    public static void tick(ServerTickEvent.Post event) {
        if(!Boolean.getBoolean("sable.adapted.runtimeChecks"))return;
        var root=FMLPaths.GAMEDIR.get().toAbsolutePath().normalize();
        if(!root.endsWith(java.nio.file.Path.of("sable-adapted-263","runtime","smoke-server")))
            throw new IllegalStateException("Runtime probe must use the isolated Sable smoke server");
        try {
            ++ticks;
            if(ticks==40){
                var level=event.getServer().overworld();
                ServerSubLevelContainer container=SubLevelContainer.getContainer(level);
                if(container==null)throw new IllegalStateException("Missing sublevel container");
                body=container.getAllSubLevels().stream().filter(x->NAME.equals(x.getName())).findFirst().orElse(null);
                restored=Files.exists(root.resolve("probe-expected.txt"));
                if(restored && body==null)throw new IllegalStateException("Saved force-loaded body was not restored");
                if(body==null){
                    level.getChunk(0,0);
                    Pose3d pose=new Pose3d();pose.position().set(8,80,8);
                    body=(ServerSubLevel)container.allocateNewSubLevel(pose);body.setName(NAME);
                    var plot=body.getPlot();plot.newEmptyChunk(plot.getCenterChunk());
                    for(int x=0;x<2;++x)for(int y=0;y<2;++y)for(int z=0;z<2;++z)
                        plot.getEmbeddedLevelAccessor().setBlock(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState(),3);
                    var shaft=BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("create","shaft"));
                    if(shaft==null||shaft==Blocks.AIR)throw new IllegalStateException("Create shaft not registered");
                    plot.getEmbeddedLevelAccessor().setBlock(new BlockPos(0,2,0),shaft.defaultBlockState(),3);
                    body.updateLastPose();
                    container.addForceLoadTicket(body,SubLevelLoadingTicketType.COMMAND_FORCED,Unit.INSTANCE);
                }
                var embedded=body.getPlot().getEmbeddedLevelAccessor();
                if(!embedded.getBlockState(BlockPos.ZERO).is(Blocks.STONE))throw new IllegalStateException("Stone plot data missing");
                var be=embedded.getBlockEntity(new BlockPos(0,2,0));
                if(be==null || !BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()).getNamespace().equals("create"))
                    throw new IllegalStateException("Create block entity missing from plot");
                initialY=body.logicalPose().position().y();
                result.put("phase",restored?"reload":"create");result.put("uuid",body.getUniqueId().toString());
                result.put("initial_y",initialY);result.put("create_block_entity",be.getType().toString());
                if(restored && !Files.readString(root.resolve("probe-expected.txt")).strip().equals(body.getUniqueId().toString()))
                    throw new IllegalStateException("Sublevel UUID changed across save/reload");
                Sable.LOGGER.info("Sable runtime probe allocated/restored {}",body.getUniqueId());
            }
            if(body!=null && ticks==240){
                var handle=RigidBodyHandle.of(body);
                if(handle==null || !handle.isValid())throw new IllegalStateException("Physics body missing");
                var pos=body.logicalPose().position();var velocity=handle.getLinearVelocity(new Vector3d());
                double mass=body.getMassTracker().getMass();
                if(!Double.isFinite(pos.y())||!velocity.isFinite()||!(mass>0))throw new IllegalStateException("Nonfinite physics or zero mass");
                if(!restored && !(pos.y()<initialY-10))throw new IllegalStateException("Gravity did not move the body");
                if(!(pos.y()>-63 && pos.y()<-55) || velocity.length()>1)
                    throw new IllegalStateException("Body did not settle on flat terrain: y="+pos.y()+", velocity="+velocity);
                result.put("final_y",pos.y());result.put("mass",mass);result.put("velocity",velocity.toString());result.put("passed",true);
                Files.writeString(root.resolve("probe-expected.txt"),body.getUniqueId().toString());
                Files.writeString(root.resolve("probe-last-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));
                Sable.LOGGER.info("Sable physical checks passed: {}",result);
            }
        } catch(Exception error) {
            result.put("passed",false);result.put("error",error.toString());
            try {Files.writeString(root.resolve("probe-last-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}
            catch(java.io.IOException io){error.addSuppressed(io);}
            throw new IllegalStateException("Sable physical runtime check failed",error);
        }
    }
}
