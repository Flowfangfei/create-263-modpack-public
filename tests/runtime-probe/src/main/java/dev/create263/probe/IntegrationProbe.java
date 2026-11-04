package dev.create263.probe;

import com.forsteri.createliquidfuel.core.BurnerTank;
import com.forsteri.createliquidfuel.core.IHasStomach;
import com.forsteri.createliquidfuel.core.LiquidFuel;
import com.forsteri.createliquidfuel.core.LiquidFuels;
import com.forsteri.createliquidfuel.mixin.BlazeBurnerAccessor;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Only loaded in an explicitly marked disposable server; never shipped in the playable pack. */
@Mod("create263_runtime_probe")
public final class IntegrationProbe {
    private static final List<String> passed = new ArrayList<>();
    private static final Map<String,Object> result = new LinkedHashMap<>();
    private static final BlockPos FIXTURE = new BlockPos(24,-60,8), PERSIST = new BlockPos(16,-60,8);
    private static final BlockPos SOURCE = new BlockPos(5,-60,8), PUMP = new BlockPos(6,-60,8), OUTPUT = new BlockPos(8,-60,8);
    private static Path root;
    private static int ticks;
    private static boolean restored, waiting, finished;

    public IntegrationProbe(IEventBus bus) {
        FiveAddonsProbe.install();
        StorageGameplayProbe.install();
        ProcessingGameplayProbe.install();
        PowerLogisticsGameplayProbe.install();
        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT
                && Boolean.getBoolean("create263.clientSmoke")) ClientProbe.install();
        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT
                && Boolean.getBoolean("create263.clientVentRegression")) VentClientProbe.install();
        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT
                && Boolean.getBoolean("create263.clientPipRegression")) PipClientProbe.install();
        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT
                && Boolean.getBoolean("create263.clientStoragePonderRegression")) StoragePonderClientProbe.install();
        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT
                && Boolean.getBoolean("create263.clientAllPonderAudit")) AllPonderClientProbe.install();
        if (!Boolean.getBoolean("create263.runtimeProbe")) return;
        root = Path.of(System.getProperty("create263.probeRoot")).toAbsolutePath().normalize();
        require(root.equals(FMLPaths.GAMEDIR.get().toAbsolutePath().normalize()),"Probe game directory mismatch");
        require(Files.isRegularFile(root.resolve(".create263-disposable-runtime")),"Missing disposable-runtime marker");
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class,IntegrationProbe::tick);
    }

    private static void require(boolean value,String message) { if (!value) throw new IllegalStateException(message); }
    private static void equal(long expected,long actual,String message) { require(expected==actual,message+": expected "+expected+", got "+actual); }
    private static BurnerTank tank(BlazeBurnerBlockEntity burner) { return ((IHasStomach)burner).getCapability(); }
    private static void check(String name,Runnable action) { action.run(); passed.add(name); }
    private static BlazeBurnerBlockEntity place(ServerLevel level,BlockPos pos) {
        level.removeBlock(pos,false);
        level.setBlock(pos,AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL,BlazeBurnerBlock.HeatLevel.SMOULDERING),3);
        var burner=(BlazeBurnerBlockEntity)level.getBlockEntity(pos);
        require(burner!=null&&tank(burner)!=null,"Missing mixed-in burner/tank");
        return burner;
    }
    private static FluidResource lava() { return FluidResource.of(Fluids.LAVA); }
    private static void fill(ResourceHandler<FluidResource> handler,FluidResource fluid,int amount) {
        try (Transaction tx=Transaction.openRoot()) { equal(amount,handler.insert(0,fluid,amount,tx),"committed fill");tx.commit(); }
    }
    private static FluidResource namedLava() {
        FluidStack stack=new FluidStack(Fluids.LAVA,321);
        stack.set(DataComponents.CUSTOM_NAME,Component.literal("create263-persistence-fixture"));
        return FluidResource.of(stack);
    }

    private static void liquidChecks(ServerLevel level) {
        check("liquid/data-map-positive-values-and-legacy-priority",()->{
            require(LiquidFuels.get(Fluids.LAVA)!=null,"Default lava data map missing");
            require(LiquidFuels.get(Fluids.WATER).superHeat(),"Test superheated water data map missing");
            require(LiquidFuels.get(Fluids.FLOWING_LAVA)==null,"Unsupported fixture fluid unexpectedly has fuel");
            require(LiquidFuel.CODEC.parse(JsonOps.INSTANCE,com.google.gson.JsonParser.parseString("{\"burnTime\":0}")).error().isPresent(),"Zero burn time accepted");
            require(LiquidFuel.CODEC.parse(JsonOps.INSTANCE,com.google.gson.JsonParser.parseString("{\"amountConsumedPerTick\":-1}")).error().isPresent(),"Negative consumption accepted");
            require(restored ? LiquidFuels.get(Fluids.FLOWING_WATER)==null : LiquidFuels.get(Fluids.FLOWING_WATER)!=null,"Legacy fuel reload state incorrect");
        });
        check("liquid/native-transactions-commit-abort-and-nested-abort",()->{
            AtomicInteger changed=new AtomicInteger();
            BurnerTank t=new BurnerTank(changed::incrementAndGet);
            try(Transaction tx=Transaction.openRoot()){equal(500,t.insert(0,lava(),500,tx),"simulated fill");}
            equal(0,t.getAmountAsInt(0),"aborted fill");equal(0,changed.get(),"abort callback");
            try(Transaction outer=Transaction.openRoot()){
                try(Transaction nested=Transaction.open(outer)){equal(500,t.insert(0,lava(),500,nested),"nested fill");nested.commit();}
            }
            equal(0,t.getAmountAsInt(0),"outer abort");equal(0,changed.get(),"nested abort callback");
            fill(t,lava(),500);equal(1,changed.get(),"root commit callback");
            try(Transaction tx=Transaction.openRoot()){equal(0,t.insert(0,FluidResource.of(Fluids.WATER),10,tx),"mixed fuel rejection");equal(0,t.insert(0,namedLava(),10,tx),"component mismatch rejection");tx.commit();}
            equal(500,t.getAmountAsInt(0),"rejected mutation");
            try(Transaction tx=Transaction.openRoot()){equal(100,t.extract(0,lava(),100,tx),"simulated extraction");}
            equal(500,t.getAmountAsInt(0),"aborted extraction");equal(1,changed.get(),"extraction abort callback");
            try(Transaction tx=Transaction.openRoot()){equal(500,t.insert(0,lava(),900,tx),"capacity clamp");tx.commit();}
            equal(1000,t.getAmountAsInt(0),"tank capacity");
            FluidStack copy=t.getFluid();copy.shrink(900);equal(1000,t.getAmountAsInt(0),"getter must return a copy");
        });
        check("liquid/all-sided-native-block-capability",()->{
            var b=place(level,FIXTURE);
            for(Direction side:Direction.values()) require(level.getCapability(Capabilities.Fluid.BLOCK,FIXTURE,side)==tank(b),"Capability missing on "+side);
            try(Transaction tx=Transaction.openRoot()){equal(0,tank(b).insert(0,FluidResource.of(Fluids.FLOWING_LAVA),100,tx),"invalid fuel rejection");tx.commit();}
        });
        check("liquid/bucket-simulation-consumption-remainder-and-overflow",()->{
            var b=place(level,FIXTURE);var stack=new ItemStack(Items.LAVA_BUCKET);
            var simulated=BlazeBurnerBlock.tryInsert(b.getBlockState(),level,FIXTURE,stack,true,false,true);
            require(simulated.success(),"Bucket simulation failed");equal(0,tank(b).getAmountAsInt(0),"simulation stored fluid");equal(1,stack.getCount(),"simulation consumed item");
            var real=BlazeBurnerBlock.tryInsert(b.getBlockState(),level,FIXTURE,stack,false,false,false);
            require(real.success()&&real.remainder().is(Items.BUCKET),"Bucket remainder incorrect");equal(0,stack.getCount(),"Bucket was not consumed");equal(1000,tank(b).getAmountAsInt(0),"Bucket fill amount");
            tank(b).set(0,lava(),250);var extra=new ItemStack(Items.LAVA_BUCKET);
            require(!BlazeBurnerBlock.tryInsert(b.getBlockState(),level,FIXTURE,extra,false,true,false).success(),"Partial-fit bucket accepted");
            equal(250,tank(b).getAmountAsInt(0),"Partial-fit overflow");equal(1,extra.getCount(),"Rejected bucket consumed");
        });
        check("liquid/diesel-canister-container-components-and-remainder",()->{
            var b=place(level,FIXTURE);
            var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("createdieselgenerators","canister"));
            require(item!=null&&item!=Items.AIR,"Diesel canister missing");
            var container=new ItemStack(item);container.set(DataComponents.CUSTOM_NAME,Component.literal("keep-this-container"));
            var handler=net.neoforged.neoforge.transfer.access.ItemAccess.forStack(container).getCapability(Capabilities.Fluid.ITEM);
            require(handler!=null,"Diesel canister capability missing");fill(handler,lava(),500);
            var workingCopy=container.copy();
            var simulation=BlazeBurnerBlock.tryInsert(b.getBlockState(),level,FIXTURE,workingCopy,false,false,true);
            require(simulation.success()&&simulation.remainder().is(item),"Canister simulation failed");
            equal(0,workingCopy.getCount(),"Create mechanical-arm simulation protocol");
            equal(500,handler.getAmountAsInt(0),"Simulation drained original canister");equal(1,container.getCount(),"Simulation consumed original canister");equal(0,tank(b).getAmountAsInt(0),"Simulation filled burner");
            var simulatedEmpty=net.neoforged.neoforge.transfer.access.ItemAccess.forStack(simulation.remainder()).getCapability(Capabilities.Fluid.ITEM);
            equal(0,simulatedEmpty.getAmountAsInt(0),"Simulated remainder not drained");
            require(Component.literal("keep-this-container").equals(simulation.remainder().get(DataComponents.CUSTOM_NAME)),"Simulated remainder components lost");
            var real=BlazeBurnerBlock.tryInsert(b.getBlockState(),level,FIXTURE,container,false,false,false);
            require(real.success()&&real.remainder().is(item),"Reusable canister was destroyed");equal(0,container.getCount(),"Original single canister not exchanged");equal(500,tank(b).getAmountAsInt(0),"Canister transfer amount");
            require(Component.literal("keep-this-container").equals(real.remainder().get(DataComponents.CUSTOM_NAME)),"Canister custom components lost");
            var emptied=net.neoforged.neoforge.transfer.access.ItemAccess.forStack(real.remainder()).getCapability(Capabilities.Fluid.ITEM);
            equal(0,emptied.getAmountAsInt(0),"Returned canister not drained");
            var rejected=new ItemStack(item);var rejectedHandler=net.neoforged.neoforge.transfer.access.ItemAccess.forStack(rejected).getCapability(Capabilities.Fluid.ITEM);fill(rejectedHandler,lava(),750);
            require(!BlazeBurnerBlock.tryInsert(b.getBlockState(),level,FIXTURE,rejected,false,true,false).success(),"Over-capacity canister accepted");
            equal(750,rejectedHandler.getAmountAsInt(0),"Rejected canister drained");equal(1,rejected.getCount(),"Rejected canister consumed");equal(500,tank(b).getAmountAsInt(0),"Failed container transfer did not roll back burner");
        });
        check("liquid/ordinary-burner-heating-and-consumption",()->{
            var b=place(level,FIXTURE);fill(tank(b),lava(),1000);
            for(int i=0;i<7;i++)b.tick();
            equal(993,tank(b).getAmountAsInt(0),"Lava cost");
            require(b.getActiveFuel()==BlazeBurnerBlockEntity.FuelType.NORMAL,"Liquid did not set NORMAL fuel");
            require(b.getHeatLevelFromBlock()==BlazeBurnerBlock.HeatLevel.KINDLED,"Normal liquid did not reach KINDLED");
            equal(134,b.getRemainingBurnTime(),"Ordinary burn time accounting");
        });
        check("liquid/superheating-and-no-ordinary-downgrade",()->{
            var b=place(level,FIXTURE);fill(tank(b),FluidResource.of(Fluids.WATER),100);b.tick();
            require(b.getActiveFuel()==BlazeBurnerBlockEntity.FuelType.SPECIAL&&b.getHeatLevelFromBlock()==BlazeBurnerBlock.HeatLevel.SEETHING,"Superheated fuel did not reach SEETHING");
            equal(90,tank(b).getAmountAsInt(0),"Superheated cost");
            tank(b).set(0,lava(),100);b.tick();equal(100,tank(b).getAmountAsInt(0),"Ordinary liquid consumed while SPECIAL active");
            require(b.getActiveFuel()==BlazeBurnerBlockEntity.FuelType.SPECIAL,"Ordinary fuel downgraded superheat");
        });
        check("liquid/creative-capacity-and-insufficient-residue",()->{
            var b=place(level,FIXTURE);fill(tank(b),lava(),100);b.isCreative=true;b.tick();equal(100,tank(b).getAmountAsInt(0),"Creative burner consumed liquid");
            b.isCreative=false;((BlazeBurnerAccessor)b).createliquidfuel$setActiveFuel(BlazeBurnerBlockEntity.FuelType.NORMAL);
            ((BlazeBurnerAccessor)b).createliquidfuel$setRemainingBurnTime(BlazeBurnerBlockEntity.MAX_HEAT_CAPACITY);b.tick();
            equal(100,tank(b).getAmountAsInt(0),"Full heat reservoir consumed liquid");
            b=place(level,FIXTURE);tank(b).set(0,FluidResource.of(Fluids.WATER),4);b.tick();equal(4,tank(b).getAmountAsInt(0),"Insufficient residue lost");
        });
        check("liquid/solid-fuel-fallback",()->{
            var b=place(level,FIXTURE);var coal=new ItemStack(Items.COAL);
            require(BlazeBurnerBlock.tryInsert(b.getBlockState(),level,FIXTURE,coal,false,false,false).success(),"Solid fuel stopped working");
            require(b.getActiveFuel()==BlazeBurnerBlockEntity.FuelType.NORMAL&&b.getRemainingBurnTime()>0,"Solid fuel state missing");
            equal(0,tank(b).getAmountAsInt(0),"Solid fuel polluted tank");
        });
        check("liquid/NBT-fluid-components-heat-and-empty-read",()->{
            var b=place(level,FIXTURE);tank(b).set(0,namedLava(),321);
            ((BlazeBurnerAccessor)b).createliquidfuel$setActiveFuel(BlazeBurnerBlockEntity.FuelType.SPECIAL);
            ((BlazeBurnerAccessor)b).createliquidfuel$setRemainingBurnTime(4321);
            var tag=b.saveWithFullMetadata(level.registryAccess());
            var restoredBurner=place(level,FIXTURE);
            restoredBurner.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tag));
            equal(321,tank(restoredBurner).getAmountAsInt(0),"NBT fluid amount");require(tank(restoredBurner).getResource(0).equals(namedLava()),"NBT fluid components");
            equal(4321,restoredBurner.getRemainingBurnTime(),"NBT burn time");require(restoredBurner.getActiveFuel()==BlazeBurnerBlockEntity.FuelType.SPECIAL,"NBT fuel type");
            tank(restoredBurner).read(level.registryAccess(),new net.minecraft.nbt.CompoundTag());equal(0,tank(restoredBurner).getAmountAsInt(0),"Missing stomach did not clear contents");
        });
        level.removeBlock(FIXTURE,false);
    }

    private static void persistence(ServerLevel level) throws Exception {
        if(restored){
            var b=(BlazeBurnerBlockEntity)level.getBlockEntity(PERSIST);
            require(b!=null&&b.isCreative(),"Persisted burner missing");
            equal(321,tank(b).getAmountAsInt(0),"Actual world reload amount");
            require(tank(b).getResource(0).equals(namedLava()),"Actual world reload components");
            passed.add("liquid/actual-world-save-and-reload");
        }else{
            var b=place(level,PERSIST);b.isCreative=true;tank(b).set(0,namedLava(),321);b.setChanged();
            Files.writeString(root.resolve("liquid-probe-expected.json"),"{\"amount\":321,\"creative\":true}\n");
        }
    }

    private static void startPipeline(ServerLevel level) {
        for(var p:List.of(SOURCE,PUMP,PUMP.east(),OUTPUT,PUMP.above(),PUMP.above().west()))level.removeBlock(p,false);
        level.setBlock(SOURCE,AllBlocks.FLUID_TANK.getDefaultState(),3);
        var source=level.getCapability(Capabilities.Fluid.BLOCK,SOURCE,Direction.EAST);
        require(source!=null,"Source fluid-tank capability missing");fill(source,lava(),6000);
        place(level,OUTPUT);
        level.setBlock(PUMP.east(),AllBlocks.FLUID_PIPE.getDefaultState().setValue(BlockStateProperties.EAST,true).setValue(BlockStateProperties.WEST,true),3);
        level.setBlock(PUMP,AllBlocks.MECHANICAL_PUMP.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST),3);
        level.setBlock(PUMP.above(),AllBlocks.COGWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS,Direction.Axis.X),3);
        level.setBlock(PUMP.above().west(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST),3);
        var motor=(CreativeMotorBlockEntity)level.getBlockEntity(PUMP.above().west());motor.generatedSpeed.setValue(-64);motor.updateGeneratedRotation();
    }
    private static void checkPipeline(ServerLevel level) {
        var source=level.getCapability(Capabilities.Fluid.BLOCK,SOURCE,Direction.EAST);
        var b=(BlazeBurnerBlockEntity)level.getBlockEntity(OUTPUT);
        var pump=(PumpBlockEntity)level.getBlockEntity(PUMP);
        result.put("pipe_source_remaining_mb",source.getAmountAsInt(0));result.put("pipe_burner_remaining_mb",tank(b).getAmountAsInt(0));result.put("pump_speed",pump.getSpeed());
        result.put("pipe_burner_heat",b.getHeatLevelFromBlock().name());
        require(pump.getSpeed()!=0,"Mechanical pump was not driven");
        require(source.getAmountAsInt(0)<6000&&tank(b).getAmountAsInt(0)>0,"Real pump/pipe did not feed burner");
        require(b.getHeatLevelFromBlock()==BlazeBurnerBlock.HeatLevel.KINDLED,"Pipe-fed burner did not heat");
        passed.add("liquid/creative-motor-cogwheel-pump-pipe-burner");
    }
    private static void showcase(ServerLevel level) {
        String[] ids={"universal_joint","brass_universal_joint","hydraulic_regulator","damping_stress_bearing"};
        for(int i=0;i<ids.length;i++){
            var id=net.minecraft.resources.Identifier.fromNamespaceAndPath("aeronautics_utility_objects",ids[i]);
            var block=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(id);
            require(block!=null&&block!=net.minecraft.world.level.block.Blocks.AIR,"Missing showcase block: "+id);
            level.setBlock(new BlockPos(i*2,-60,0),block.defaultBlockState(),3);
        }
    }
    private static void write() throws Exception {
        result.put("passed_checks",List.copyOf(passed));
        Files.writeString(root.resolve("three-addons-probe-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
    }
    private static void finish() throws Exception { result.put("passed",true);result.put("phase",restored?"reload-complete":"create-complete");finished=true;write(); }
    private static void tick(ServerTickEvent.Post event) {
        if(finished)return;
        try{
            ticks++;
            require(event.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toRealPath().equals(root.resolve("world").toRealPath()),"Probe world directory mismatch");
            var level=event.getServer().overworld();
            if(ticks==40){
                for(int x=0;x<=2;x++)level.setChunkForced(x,0,true);
                restored=Files.isRegularFile(root.resolve("liquid-probe-expected.json"));result.put("world_reloaded",restored);
                persistence(level);liquidChecks(level);startPipeline(level);showcase(level);
            }
            if(ticks==220){
                checkPipeline(level);
                if(restored){finish();return;}
                waiting=true;result.put("phase","ready-for-data-reload");write();
            }
            if(waiting&&LiquidFuels.get(Fluids.WATER).burnTime()==64&&LiquidFuels.get(Fluids.FLOWING_WATER)==null){
                passed.add("liquid/data-map-and-legacy-removal-after-reload");finish();
            }
        }catch(Throwable error){
            error.printStackTrace();result.put("passed",false);result.put("error",error.toString());finished=true;
            try{write();}catch(Exception secondary){secondary.printStackTrace();}
        }
    }
}
