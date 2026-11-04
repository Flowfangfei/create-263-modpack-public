package dev.eriksonn.aeronautics.neoforge;

import com.google.gson.GsonBuilder;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlockEntity;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerBlockEntity;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.neoforge.TransferRuntimeChecks;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3d;

/** Explicit test-only assembly and propulsion probe. Never runs in a user's normal instance. */
public final class AeroRuntimeProbe {
    private static final Path ROOT=Path.of(System.getProperty("create263.probeRoot", "run/disabled-server-probe")).toAbsolutePath().normalize();
    private static final String NAME="aeronautics-adapted-propulsion-probe";
    private static final BlockPos MOTOR=new BlockPos(8,80,8), ASSEMBLER=MOTOR.south();
    private static int ticks;
    private static boolean restored;
    private static SuperGlueEntity probeGlue;
    private static ServerSubLevel body;
    private static BasePropellerBlockEntity propeller;
    private static double initialY;
    private static final Map<String,Object> result=new LinkedHashMap<>();
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
    private static void checkCannonTransaction(net.minecraft.server.level.ServerLevel level){
        BlockPos pos=new BlockPos(120,90,120);
        require(level.getBlockState(pos).isAir(),"Cannon probe position occupied");
        try{
            level.setBlock(pos,AeroBlocks.MOUNTED_POTATO_CANNON.getDefaultState(),2);
            var cannon=(dev.eriksonn.aeronautics.content.blocks.mounted_potato_cannon.MountedPotatoCannonBlockEntity)level.getBlockEntity(pos);
            require(cannon!=null,"Cannon block entity missing");
            cannon.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.POTATO,1));
            var state=cannon.saveWithFullMetadata(level.registryAccess());
            state.putFloat("ChargeTimer",7.5f);state.putInt("ItemTimer",11);state.putInt("ItemRotationID",321);
            cannon.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess(),state));
            var handler=new dev.simulated_team.simulated.multiloader.inventory.neoforge.ContainerWrapper<>(cannon.getInventory());
            var potato=net.neoforged.neoforge.transfer.item.ItemResource.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.POTATO));
            try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()){
                require(handler.extract(0,potato,1,tx)==1,"Cannon simulated extraction failed");
            }
            var after=cannon.saveWithFullMetadata(level.registryAccess());
            require(after.getFloatOr("ChargeTimer",0)==7.5f&&after.getIntOr("ItemTimer",0)==11&&after.getIntOr("ItemRotationID",0)==321,"Aborted extraction reset cannon charge or visual state");
            require(cannon.getInventory().getItem(0).getCount()==1,"Aborted extraction lost cannon ammo");
            try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()){
                require(handler.extract(0,potato,1,tx)==1,"Cannon committed extraction failed");tx.commit();
            }
            require(cannon.saveWithFullMetadata(level.registryAccess()).getFloatOr("ChargeTimer",-1)==0,"Committed ammo type change did not reset cannon");
        }finally{level.removeBlock(pos,false);}
    }

    public static void tick(ServerTickEvent.Post event){
        if(!Boolean.getBoolean("aeronautics.adapted.runtimeChecks"))return;
        require(FMLPaths.GAMEDIR.get().toAbsolutePath().normalize().equals(ROOT),"Probe rejected a non-test game directory");
        try{
            ++ticks;
            var level=event.getServer().overworld();
            var container=SubLevelContainer.getContainer(level);
            if(ticks==40){
                // No players exist in this dedicated probe: keep its construction area entity-ticking.
                for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)level.setChunkForced(x,z,true);
                result.put("transfer_checks",TransferRuntimeChecks.run(level));
                checkCannonTransaction(level);result.put("cannon_simulation_preserves_charge",true);
                AeroBuoyancyChecks.start(level);
                restored=Files.exists(ROOT.resolve("aero-probe-expected.txt"));
                body=container.getAllSubLevels().stream().filter(s->NAME.equals(s.getName())).findFirst().orElse(null);
                if(restored){
                    require(body!=null,"Force-loaded aircraft was not restored");
                    require(Files.readString(ROOT.resolve("aero-probe-expected.txt")).strip().equals(body.getUniqueId().toString()),"Aircraft UUID changed after reload");
                }else{
                    require(body==null,"Untracked previous aircraft");
                    level.getChunkAt(MOTOR);
                    level.setBlock(MOTOR,AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.UP),3);
                    level.setBlock(MOTOR.above(),AeroBlocks.WOODEN_PROPELLER.getDefaultState().setValue(BlockStateProperties.FACING,Direction.UP).setValue(dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlock.REVERSED,true),3);
                    level.setBlock(ASSEMBLER,SimBlocks.PHYSICS_ASSEMBLER.getDefaultState().setValue(BlockStateProperties.ATTACH_FACE,AttachFace.WALL).setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH),3);
                    // A mirrored assembler balances mass around the single thrust axis.
                    level.setBlock(MOTOR.north(),SimBlocks.PHYSICS_ASSEMBLER.getDefaultState().setValue(BlockStateProperties.ATTACH_FACE,AttachFace.WALL).setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH),3);
                    probeGlue=new SuperGlueEntity(level,new AABB(MOTOR.getX(),MOTOR.getY(),MOTOR.getZ()-1,MOTOR.getX()+1,MOTOR.getY()+2,MOTOR.getZ()+2));
                    require(level.addFreshEntity(probeGlue),"Could not add real Create glue");
                }
                result.put("phase",restored?"reload":"create");
            }
            if(ticks>=40){
                var buoyancy=AeroBuoyancyChecks.tick(level,ticks-40);
                if(buoyancy!=null)result.put("buoyancy_checks",buoyancy);
            }
            if(ticks==60 && !restored){
                var motor=(CreativeMotorBlockEntity)level.getBlockEntity(MOTOR);
                require(motor!=null,"Native Create creative motor missing");
                motor.generatedSpeed.setValue(256);motor.updateGeneratedRotation();
                var assembler=(PhysicsAssemblerBlockEntity)level.getBlockEntity(ASSEMBLER);
                require(assembler!=null,"Physics assembler missing");
                result.put("before_assembly_motor",level.getBlockState(MOTOR).toString());
                result.put("before_assembly_propeller",level.getBlockState(MOTOR.above()).toString());
                result.put("glue_bounds",probeGlue.getBoundingBox().toString());
                result.put("glue_alive",probeGlue.isAlive());
                result.put("glue_removal_reason",String.valueOf(probeGlue.getRemovalReason()));
                require(probeGlue.isAlive(),"Test glue was removed before assembly: "+probeGlue.getRemovalReason());
                result.put("glue_contains_motor",probeGlue.contains(MOTOR));
                result.put("glue_contains_propeller",probeGlue.contains(MOTOR.above()));
                var search=new dev.simulated_team.simulated.util.assembly.SimAssemblyContraption(null,false);
                search.searchMovedStructure(level,MOTOR);
                result.put("assembly_selection",search.getBlocks().stream().map(Object::toString).toList());
                result.put("assembly_glues",search.getGlues().size());
                assembler.assembleOrDisassemble();
                require(assembler.getLastAssemblyException()==null,"Assembly failed: "+assembler.getLastAssemblyException());
                body=container.getAllSubLevels().stream().filter(s->s.getName()==null||!NAME.equals(s.getName())).findFirst().orElse(null);
                require(body!=null,"Assembler did not create a real Sable body");
                body.setName(NAME);
                container.addForceLoadTicket(body,SubLevelLoadingTicketType.COMMAND_FORCED,Unit.INSTANCE);
                result.put("after_assembly_motor",level.getBlockState(MOTOR).toString());
                result.put("after_assembly_propeller",level.getBlockState(MOTOR.above()).toString());
            }
            if(ticks==90){
                require(body!=null,"Aircraft missing");
                if(!restored)require(level.getBlockState(MOTOR).isAir()&&level.getBlockState(MOTOR.above()).isAir(),"Assembled blocks remained in the original world: motor="+level.getBlockState(MOTOR)+", propeller="+level.getBlockState(MOTOR.above()));
                for(var actor:body.getPlot().getBlockEntityActors())if(actor instanceof BasePropellerBlockEntity p){propeller=p;break;}
                require(propeller!=null,"Aircraft plot lost its Aeronautics propeller block entity");
                initialY=body.logicalPose().position().y();
                result.put("uuid",body.getUniqueId().toString());result.put("initial_y",initialY);
                float motorSpeed=((CreativeMotorBlockEntity)propeller.getLevel().getBlockEntity(propeller.getBlockPos().below())).getGeneratedSpeed();
                result.put("motor_speed",motorSpeed);
                require(Math.abs(motorSpeed-256f)<0.001f,"Saved motor setting was not restored");
                require(propeller.getSpeed()>0,"Native kinetic network did not power the propeller");
            }
            if(ticks==240){
                require(result.containsKey("buoyancy_checks"),"Buoyancy check did not complete");
                var handle=RigidBodyHandle.of(body);
                require(handle!=null&&handle.isValid(),"Aircraft has no native physical body");
                var velocity=handle.getLinearVelocity(new Vector3d());
                double finalY=body.logicalPose().position().y(),mass=body.getMassTracker().getMass();
                require(Double.isFinite(finalY)&&velocity.isFinite()&&mass>0,"Invalid physical state");
                require(propeller.isActive()&&propeller.getScaledThrust()>0,"Propeller produced no thrust");
                result.put("global_local_up",body.logicalPose().orientation().transform(new Vector3d(0,1,0)).toString());
                result.put("scaled_upward_thrust",propeller.getScaledThrust());
                result.put("final_y",finalY);result.put("mass",mass);result.put("velocity",velocity.toString());
                if(!restored){
                    require(finalY>initialY+1,"Powered aircraft did not climb: "+initialY+" -> "+finalY+", thrust="+propeller.getThrust()+", mass="+mass);
                    result.put("motion_check","climb-from-construction-altitude");
                }else{
                    // Air density falls with altitude: the saved craft can already be at its hover ceiling.
                    require(finalY>initialY-40&&velocity.y()>-5,"Restored powered craft lost its lift: "+initialY+" -> "+finalY+", velocity="+velocity);
                    require(body.logicalPose().orientation().transform(new Vector3d(0,1,0)).y()>0.99,"Balanced saved aircraft lost its orientation");
                    result.put("motion_check","restored-lift-and-finite-stable-physics");
                }
                result.put("final_y",finalY);result.put("mass",mass);result.put("velocity",velocity.toString());
                result.put("propeller_rpm",propeller.getSpeed());result.put("thrust",propeller.getThrust());result.put("scaled_upward_thrust",propeller.getScaledThrust());result.put("passed",true);
                Files.writeString(ROOT.resolve("aero-probe-expected.txt"),body.getUniqueId().toString());
                Files.writeString(ROOT.resolve("aero-probe-last-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));
                Aeronautics.LOGGER.info("Aeronautics assembly and propulsion checks passed: {}",result);
            }
        }catch(Exception failure){
            result.put("passed",false);result.put("error",failure.toString());
            try{Files.writeString(ROOT.resolve("aero-probe-last-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception io){failure.addSuppressed(io);}
            throw new IllegalStateException("Aeronautics runtime check failed",failure);
        }
    }
}
