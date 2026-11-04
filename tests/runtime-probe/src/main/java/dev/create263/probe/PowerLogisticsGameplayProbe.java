package dev.create263.probe;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Real server-tick checks. Explicit disposable-world opt in; never part of a released pack. */
public final class PowerLogisticsGameplayProbe {
    private static final String E = "com.george_vi.electroenergetics.";
    private static final String TOKEN = "create263-power-logistics";
    private static final BlockPos BATTERY = new BlockPos(562,100,8);
    private static final BlockPos MOTOR = new BlockPos(568,100,8);
    private static final BlockPos DRILL = MOTOR.east(), TARGET = DRILL.east();
    private static final BlockPos HOPPER = TARGET.below(), OUTPUT = HOPPER.below();
    private static final int[] RPMS = {0,32,256};
    private static final List<Map<String,Object>> checks = new ArrayList<>();
    private static final List<Map<String,Object>> powerSamples = new ArrayList<>();
    private static final List<Map<String,Object>> timingSamples = new ArrayList<>();
    private static final List<Map<String,Object>> simulationSamples = new ArrayList<>();
    private static final Map<String,Object> result = new LinkedHashMap<>();
    private static final String BOOT = UUID.randomUUID().toString();
    private static Path root, workspace;
    private static String fixtureId;
    private static boolean installed, finished, reloaded, powerDone, logisticsDone, started;
    private static int ticks, setupTick, powerPhase, powerPhaseTick, timingStart;
    private static int dirtBefore, expectedDirt, stable;
    private static long negativeWire, positiveWire;
    private static Object infrastructure, lastSimulationResults;
    private static volatile Class<?> observedSimulationTickerClass;
    private static int simulationWaitStart=-1, simulationFirstReadyTick=-1, lastSimulationChangeTick=-1, distinctSimulationResults;
    private static final int[] animationDone = {-1,-1,-1}, cooldownDone = {-1,-1,-1};
    private static final int[] priorAnimation = new int[3], priorCooldown = new int[3], priorLazy = new int[3];
    private static final boolean[] lazyCompared = new boolean[3];
    private PowerLogisticsGameplayProbe() {}

    public static synchronized void install() {
        if (installed || !Boolean.getBoolean("create263.powerLogisticsProbe")) return;
        try {
            String configured=System.getProperty("create263.probeRoot");
            require(configured!=null&&!configured.isBlank(),"Missing create263.probeRoot");
            root=Path.of(configured).toRealPath();
            require(root.equals(FMLPaths.GAMEDIR.get().toRealPath()),"Game directory differs from probeRoot");
            require(Files.isRegularFile(root.resolve(".create263-disposable-runtime"),LinkOption.NOFOLLOW_LINKS),"Missing disposable-runtime marker");
            for(Path p=root.getParent();p!=null;p=p.getParent())
                if(Files.isRegularFile(p.resolve("AGENTS.md"))&&Files.isRegularFile(p.resolve("PROJECT.json"))&&Files.isDirectory(p.resolve("tests/runtime-probe"))){workspace=p.toRealPath();break;}
            require(workspace!=null,"Project-owned source workspace not found");
            Path runtime=workspace.resolve("runtime").toRealPath();
            require(runtime.startsWith(workspace)&&root.startsWith(runtime)&&!root.equals(runtime),"Root must be inside the owned workspace/runtime");
            require(root.resolve(".create263-disposable-runtime").toRealPath().startsWith(root),"Marker escapes root");
            reloaded=Boolean.getBoolean("create263.gameplayReload");
            result.put("probe","power-logistics-gameplay-v1");result.put("boot_id",BOOT);result.put("world_reloaded",reloaded);
            result.put("remaining",List.of("Electro moving Sable sublevels, AC/rotor generation, overloaded networks and trains", "Additional vertical belts, train UI, peripheral integrations and long-duration concurrency"));
            installed=true;NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class,PowerLogisticsGameplayProbe::tick);
            Runtime.getRuntime().addShutdownHook(new Thread(PowerLogisticsGameplayProbe::shutdown,"Create263-PowerLogistics-Shutdown"));
        } catch(Exception e) {throw new IllegalStateException("Power/logistics probe guard refused installation",e);}
    }
    private static void require(boolean b,String m){if(!b)throw new IllegalStateException(m);}
    private static void eq(long e,long a,String m){require(e==a,m+": expected="+e+", actual="+a);}
    private static Object field(Object o,String name)throws Exception {
        for(Class<?> c=o instanceof Class<?> cl?cl:o.getClass();c!=null;c=c.getSuperclass())try{Field f=c.getDeclaredField(name);f.setAccessible(true);return f.get(o instanceof Class<?>?null:o);}catch(NoSuchFieldException ignored){}
        throw new NoSuchFieldException(name);
    }
    private static Class<?> boxed(Class<?> c){if(!c.isPrimitive())return c;return Map.of(int.class,Integer.class,long.class,Long.class,float.class,Float.class,double.class,Double.class,boolean.class,Boolean.class).get(c);}
    private static Object call(Object o,String n,Object...args)throws Exception {
        for(Class<?> c=o instanceof Class<?> cl?cl:o.getClass();c!=null;c=c.getSuperclass())for(Method m:c.getDeclaredMethods())if(m.getName().equals(n)&&m.getParameterCount()==args.length){
            Class<?>[] pt=m.getParameterTypes();boolean ok=true;for(int i=0;i<pt.length;i++)if(args[i]!=null&&!boxed(pt[i]).isInstance(args[i]))ok=false;
            if(ok){m.setAccessible(true);return m.invoke(o instanceof Class<?>?null:o,args);}
        }
        throw new NoSuchMethodException(o+"."+n+Arrays.toString(args));
    }
    private static Object node(int id,BlockPos p)throws Exception{return Class.forName(E+"foundation.nodes.InWorldNode").getConstructor(int.class,BlockPos.class).newInstance(id,p);}
    private static Object wireType()throws Exception{return call(field(Class.forName(E+"CEEWireTypes"),"COPPER"),"get");}
    private static BlockState state(String id){var b=BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));require(b!=null&&b!=Blocks.AIR,"Missing block "+id);return b.defaultBlockState();}
    private static BlockEntity be(ServerLevel l,BlockPos p){var b=l.getBlockEntity(p);require(b!=null,"Missing block entity "+p);return b;}
    private static BlockEntity place(ServerLevel l,BlockPos p,BlockState s){require(l.getBlockState(p).isAir(),"Fixture position occupied: "+p+" "+l.getBlockState(p));l.setBlock(p,s,3);return be(l,p);}
    private static void pass(String name,Object measured){checks.add(Map.of("name",name,"passed",true,"measured",measured));}
    private static BlockPos accelerator(int i){return new BlockPos(577,100,16+i*16);}
    private static BlockPos packager(int i){return accelerator(i).east();}
    private static BlockPos source(int i){return packager(i).south();}
    private static KineticBlockEntity kinetic(ServerLevel l,BlockPos p){return (KineticBlockEntity)be(l,p);}
    private static PackagerBlockEntity pack(ServerLevel l,int i){return (PackagerBlockEntity)be(l,packager(i));}
    private static int lazy(PackagerBlockEntity p)throws Exception {
        // The injected accessor belongs to SmartBlockEntity. Enumerating Packager methods
        // also resolves unrelated optional ComputerCraft signatures when CC is absent.
        return ((Number)com.simibubi.create.foundation.blockEntity.SmartBlockEntity.class
                .getDeclaredMethod("getLazyTickCounter").invoke(p)).intValue();
    }
    private static ItemStack namedInput(){ItemStack s=new ItemStack(Items.DIRT,7);s.set(DataComponents.CUSTOM_NAME,Component.literal(TOKEN+":"+fixtureId));return s;}
    private static int packageContents(ItemStack box){
        require(PackageItem.isPackage(box)&&box.getCount()==1,"Expected one real package");var h=PackageItem.getContents(box);int count=0;
        for(int s=0;s<h.getSlots();s++){ItemStack item=h.getStackInSlot(s);if(item.isEmpty())continue;require(item.is(Items.DIRT)&&Component.literal(TOKEN+":"+fixtureId).equals(item.get(DataComponents.CUSTOM_NAME)),"Package input/components changed");count+=item.getCount();}
        eq(7,count,"Package contents conservation");return count;
    }
    private static ItemStack witness(){ItemStack s=new ItemStack(Items.PAPER);s.set(DataComponents.CUSTOM_NAME,Component.literal(TOKEN+":"+fixtureId));return s;}
    private static int dirt(ServerLevel l){int n=0;for(BlockPos p:List.of(HOPPER,OUTPUT)){var c=(Container)be(l,p);for(int i=0;i<c.getContainerSize();i++){ItemStack s=c.getItem(i);if(p.equals(OUTPUT)&&i==26){require(ItemStack.isSameItemSameComponents(witness(),s)&&s.getCount()==1,"Saved chest witness changed");continue;}require(s.isEmpty()||s.is(Items.DIRT),"Unexpected drill output");n+=s.getCount();}}return n;}
    private static void shutdown() {
        boolean owned=false;
        Map<String,Object> report=new LinkedHashMap<>();
        report.put("probe","power-logistics-shutdown-v1");report.put("boot_id",BOOT);report.put("world_reloaded",reloaded);report.put("passed",false);
        try {
            require(Boolean.getBoolean("create263.powerLogisticsProbe"),"Probe flag removed before shutdown");
            require(root!=null&&workspace!=null&&root.equals(root.toRealPath()),"Shutdown runtime moved or missing");
            require(root.equals(FMLPaths.GAMEDIR.get().toRealPath()),"Shutdown game directory differs from probeRoot");
            Path runtime=workspace.resolve("runtime").toRealPath();
            require(runtime.startsWith(workspace)&&root.startsWith(runtime)&&!root.equals(runtime),"Shutdown runtime escapes owned workspace");
            Path marker=root.resolve(".create263-disposable-runtime");
            require(Files.isRegularFile(marker,LinkOption.NOFOLLOW_LINKS)&&marker.toRealPath().startsWith(root),"Shutdown disposable marker missing or escaped");
            require(root.resolve("world").toRealPath().startsWith(root),"Shutdown world escapes marked root");
            owned=true;
            Class<?> tickerClass=observedSimulationTickerClass;
            require(tickerClass!=null,"Production SimulationTicker was never observed; no shutdown evidence");
            java.util.concurrent.ExecutorService executor;
            // Match the monitor used by the production synchronized start/stop methods.
            // Read only the class reached by normal production setup; do not load/start/stop it here.
            synchronized(tickerClass){executor=(java.util.concurrent.ExecutorService)field(tickerClass,"electricalExecutorService");}
            long alive=Thread.getAllStackTraces().keySet().stream().filter(t->t.isAlive()&&t.getName().equals("CEE-Electrical-Simulator")).count();
            report.put("simulation_class_observed",true);report.put("executor_cleared",executor==null);
            report.put("executor_shutdown",executor!=null&&executor.isShutdown());report.put("executor_terminated",executor!=null&&executor.isTerminated());
            report.put("live_simulator_threads",alive);report.put("passed",executor==null&&alive==0);
            report.put("recorded_at",Instant.now().toString());
            Files.writeString(root.resolve("power-logistics-shutdown.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report)+"\n");
        } catch(Throwable failure) {
            failure.printStackTrace();report.put("fatal_error",failure.toString());report.put("recorded_at",Instant.now().toString());
            if(owned)try{Files.writeString(root.resolve("power-logistics-shutdown.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report)+"\n");}catch(Exception nested){nested.printStackTrace();}
        }
    }
    private static void guard(ServerTickEvent.Post e)throws Exception {
        require(Boolean.getBoolean("create263.powerLogisticsProbe"),"Probe flag removed");
        require(root.equals(FMLPaths.GAMEDIR.get().toRealPath()),"Runtime moved");
        require(Files.isRegularFile(root.resolve(".create263-disposable-runtime"),LinkOption.NOFOLLOW_LINKS),"Disposable marker removed");
        Path actual=e.getServer().getWorldPath(LevelResource.ROOT).toRealPath();
        require(actual.equals(root.resolve("world").toRealPath())&&actual.startsWith(root),"World escapes marked root/world");
    }
    private static void setup(ServerLevel l)throws Exception {
        for(int cx=35;cx<=36;cx++)for(int cz=0;cz<=3;cz++){l.setChunkForced(cx,cz,true);l.getChunk(cx,cz);}
        Path fixture=root.resolve("power-logistics-gameplay-fixture.json");
        require(reloaded==Files.isRegularFile(fixture),"Reload flag and existing fixture disagree");
        infrastructure=call(Class.forName(E+"simulation.infrastructure.InfrastructureSavedData"),"load",l);
        observedSimulationTickerClass=field(infrastructure,"ticker").getClass();
        if(reloaded){
            JsonObject f=JsonParser.parseString(Files.readString(fixture)).getAsJsonObject();
            require(f.get("world").getAsString().equals(root.resolve("world").toRealPath().toString()),"Fixture belongs to another world");
            require(!BOOT.equals(f.get("create_boot_id").getAsString()),"Restart must use an independent JVM");
            fixtureId=f.get("fixture_id").getAsString();negativeWire=f.get("negative_wire").getAsLong();positiveWire=f.get("positive_wire").getAsLong();
            eq(f.get("dirt_completed").getAsInt(),dirt(l),"Reloaded drill output");dirtBefore=dirt(l);
            require(l.getBlockState(MOTOR).is(state("electroenergetics:red_electric_motor").getBlock()),"Saved motor missing");
            require(l.getBlockState(BATTERY).is(state("electroenergetics:creative_battery").getBlock()),"Saved battery missing");
            require(AllBlocks.MECHANICAL_DRILL.has(l.getBlockState(DRILL)),"Saved drill missing");
            require(ItemStack.isSameItemSameComponents(witness(),((ChestBlockEntity)be(l,OUTPUT)).getItem(26)),"Saved output identity missing");
            require(call(infrastructure,"getConnectionData",negativeWire)!=null&&call(infrastructure,"getConnectionData",positiveWire)!=null,"Saved closed circuit wires missing");
            for(int i=0;i<3;i++){
                require(l.getBlockState(accelerator(i)).is(state("createadditionallogistics:package_accelerator").getBlock()),"Saved accelerator missing");
                require(AllBlocks.PACKAGER.has(l.getBlockState(packager(i))),"Saved packager missing");
                packageContents(pack(l,i).heldBox);
                eq(RPMS[i],((CreativeMotorBlockEntity)be(l,accelerator(i).west())).generatedSpeed.getValue(),"Saved rotation setting");
                require(((Container)be(l,source(i))).isEmpty(),"Saved source not empty");
            }
            pass("disk/independent-jvm-world-blocks-wires-package-components-and-speed-settings",Map.of("create_boot_id",f.get("create_boot_id").getAsString(),"reload_boot_id",BOOT,"dirt",dirtBefore,"saved_packages",3));
        } else {
            fixtureId=UUID.randomUUID().toString();
            var chest=(ChestBlockEntity)place(l,OUTPUT,Blocks.CHEST.defaultBlockState());chest.setItem(26,witness());chest.setChanged();
            place(l,HOPPER,Blocks.HOPPER.defaultBlockState());
            place(l,BATTERY,state("electroenergetics:creative_battery").setValue(BlockStateProperties.FACING,Direction.EAST));
            place(l,MOTOR,state("electroenergetics:red_electric_motor").setValue(BlockStateProperties.FACING,Direction.EAST));
            place(l,DRILL,AllBlocks.MECHANICAL_DRILL.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST));
            l.setBlock(TARGET,Blocks.DIRT.defaultBlockState(),3);
            for(int i=0;i<3;i++){
                place(l,source(i),Blocks.CHEST.defaultBlockState());
                place(l,packager(i),AllBlocks.PACKAGER.getDefaultState().setValue(BlockStateProperties.FACING,Direction.NORTH));
                place(l,accelArchive(i),Blocks.CHEST.defaultBlockState());
                place(l,accelerator(i),state("createadditionallogistics:package_accelerator").setValue(BlockStateProperties.FACING,Direction.EAST));
                var motor=(CreativeMotorBlockEntity)place(l,accelerator(i).west(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST));
                motor.generatedSpeed.setValue(RPMS[i]);motor.updateGeneratedRotation();motor.setChanged();
            }
            pass("fixture/actual-electrical-and-kinetic-blocks-placed",Map.of("electro_load","mechanical_drill","accelerator_rpms",RPMS));
        }
        setupTick=ticks;powerPhase=0;powerPhaseTick=ticks;result.put("fixture_id",fixtureId);write("running");
    }
    private static BlockPos accelArchive(int i){return packager(i).north();}
    private static double voltage(ServerLevel l)throws Exception{return ((Number)call(field(be(l,MOTOR),"averageVoltage"),"get")).doubleValue();}
    private static double current()throws Exception{Object r=field(field(infrastructure,"ticker"),"lastResults");require(r!=null,"No real simulation results");return Math.abs(((Number)call(r,"getCurrentThrough",MOTOR,0,1)).doubleValue());}
    private static boolean simulationReady(ServerLevel l)throws Exception {
        Object ticker=field(infrastructure,"ticker"), observed=field(ticker,"lastResults");
        var executor=(java.util.concurrent.ExecutorService)field(ticker,"electricalExecutorService");
        var future=(java.util.concurrent.Future<?>)field(ticker,"future");
        if(observed!=null&&observed!=lastSimulationResults){lastSimulationResults=observed;lastSimulationChangeTick=ticks;distinctSimulationResults++;}
        boolean executorRunning=executor!=null&&!executor.isShutdown()&&!executor.isTerminated();
        int resultAge=lastSimulationChangeTick<0?-1:ticks-lastSimulationChangeTick;
        boolean ready=executorRunning&&observed!=null&&distinctSimulationResults>=2&&resultAge<=40;
        if(!ready&&simulationWaitStart<0)simulationWaitStart=ticks;
        Map<String,Object> state=new LinkedHashMap<>();
        state.put("tick",ticks);state.put("ready",ready);state.put("executor_present",executor!=null);state.put("executor_running",executorRunning);
        state.put("future_present",future!=null);state.put("future_done",future!=null&&future.isDone());state.put("future_cancelled",future!=null&&future.isCancelled());
        state.put("results_present",observed!=null);state.put("distinct_results_observed",distinctSimulationResults);state.put("last_result_age_ticks",resultAge);
        state.put("battery_nodes",((List<?>)call(infrastructure,"getNodesAt",BATTERY)).size());state.put("motor_nodes",((List<?>)call(infrastructure,"getNodesAt",MOTOR)).size());
        state.put("world_frozen",l.tickRateManager().isFrozen());state.put("waited_ticks",simulationWaitStart<0?0:ticks-simulationWaitStart);
        result.put("simulation_readiness",state);
        if(ticks%20==0||ticks==simulationWaitStart||(ready&&simulationFirstReadyTick<0))simulationSamples.add(state);
        if(ready){if(simulationFirstReadyTick<0){simulationFirstReadyTick=ticks;result.put("simulation_first_ready_tick",ticks);}simulationWaitStart=-1;return true;}
        require(ticks-simulationWaitStart<200,"Natural electrical simulator not ready within 200 ticks: "+state);
        return false;
    }
    private static float load(ServerLevel l)throws Exception{return ((Number)field(be(l,MOTOR),"load")).floatValue();}
    private static boolean powered(ServerLevel l)throws Exception {
        double v=voltage(l);float ms=kinetic(l,MOTOR).getSpeed(),ds=kinetic(l,DRILL).getSpeed();
        return Double.isFinite(v)&&v>80&&Math.abs(ms)>0&&Math.abs(ms-ds)<0.01&&load(l)>0&&current()>0.001&&!kinetic(l,MOTOR).isOverStressed();
    }
    private static void powerSample(ServerLevel l,String phase)throws Exception {
        powerSamples.add(Map.of("tick",ticks,"phase",phase,"voltage_rms",voltage(l),"motor_current_amps",current(),"motor_rpm",kinetic(l,MOTOR).getSpeed(),"drill_rpm",kinetic(l,DRILL).getSpeed(),"load",load(l),"dirt_collected",dirt(l),"target_air",l.getBlockState(TARGET).isAir()));
    }
    private static void power(ServerLevel l)throws Exception {
        if(powerDone)return;
        require(ticks-powerPhaseTick<600,"Electro phase timed out: "+powerPhase+"; samples="+powerSamples);
        if(powerPhase==0&&ticks-setupTick>=30){
            if(!reloaded){
                // Nodes and simulated devices must have appeared through their normal scheduled block ticks.
                require(((List<?>)call(infrastructure,"getNodesAt",BATTERY)).size()==2,"Battery automatic nodes missing");
                require(((List<?>)call(infrastructure,"getNodesAt",MOTOR)).size()==2,"Motor automatic nodes missing");
                negativeWire=((Number)call(infrastructure,"connect",node(0,BATTERY),node(0,MOTOR),wireType())).longValue();
                positiveWire=((Number)call(infrastructure,"connect",node(1,BATTERY),node(1,MOTOR),wireType())).longValue();
            } else {if(!simulationReady(l)||!powered(l))return; pass("disk/saved-circuit-resumes-real-mechanical-load-without-replacement",Map.of("voltage_rms",voltage(l),"drill_rpm",kinetic(l,DRILL).getSpeed(),"load",load(l)));require(l.getBlockState(TARGET).isAir(),"Saved target should remain broken");l.setBlock(TARGET,Blocks.DIRT.defaultBlockState(),3);}
            expectedDirt=dirtBefore+1;powerPhase=1;powerPhaseTick=ticks;
        }
        if(powerPhase>0&&!simulationReady(l))return;
        if(powerPhase==1){
            stable=powered(l)?stable+1:0;
            if(ticks%20==0)powerSample(l,"energized");
            if(stable>=8&&l.getBlockState(TARGET).isAir()&&dirt(l)==expectedDirt){
                double capacity=((Number)call(be(l,MOTOR),"calculateAddedStressCapacity")).doubleValue();require(capacity>0&&Double.isFinite(capacity),"No generated stress capacity");
                pass(reloaded?"electro/reloaded-network-drives-new-real-drill-work":"electro/closed-circuit-voltage-to-loaded-motor-and-real-drill-output",Map.of("voltage_rms",voltage(l),"motor_rpm",kinetic(l,MOTOR).getSpeed(),"load",load(l),"stress_capacity",capacity,"motor_current_amps",current(),"dirt",dirt(l)));
                call(infrastructure,"removeConnectionNoDrops",call(infrastructure,"resolveConnection",positiveWire));
                powerPhase=2;powerPhaseTick=ticks;stable=0;
            }
        } else if(powerPhase==2){
            boolean stopped=voltage(l)<1&&Math.abs(kinetic(l,MOTOR).getSpeed())<0.01&&Math.abs(kinetic(l,DRILL).getSpeed())<0.01;
            stable=stopped?stable+1:0;
            if(stable>=8){l.setBlock(TARGET,Blocks.DIRT.defaultBlockState(),3);powerPhase=3;powerPhaseTick=ticks;}
        } else if(powerPhase==3&&ticks-powerPhaseTick>=100){
            powerSample(l,"open-circuit");
            require(voltage(l)<1,"Open circuit retained voltage");require(current()<0.001,"Open circuit retained electrical load current");require(Math.abs(kinetic(l,MOTOR).getSpeed())<0.01&&Math.abs(kinetic(l,DRILL).getSpeed())<0.01,"Disconnected motor/drill still rotates");
            require(l.getBlockState(TARGET).is(Blocks.DIRT),"Disconnected drill performed work");eq(expectedDirt,dirt(l),"Open-circuit output changed");
            pass("electro/open-circuit-stops-shaft-and-prevents-block-breaking",Map.of("observed_ticks",100,"voltage_rms",voltage(l),"dirt",dirt(l)));
            positiveWire=((Number)call(infrastructure,"connect",node(1,BATTERY),node(1,MOTOR),wireType())).longValue();
            powerPhase=4;powerPhaseTick=ticks;expectedDirt++;stable=0;
        } else if(powerPhase==4){
            stable=powered(l)?stable+1:0;
            if(ticks%20==0)powerSample(l,"reconnected");
            if(stable>=8&&l.getBlockState(TARGET).isAir()&&dirt(l)==expectedDirt){
                powerSample(l,"complete");pass("electro/reconnected-circuit-resumes-real-work-with-conserved-output",Map.of("dirt",dirt(l),"negative_wire",negativeWire,"positive_wire",positiveWire));powerDone=true;
            }
        }
    }
    private static void startLogistics(ServerLevel l)throws Exception {
        for(int i=0;i<3;i++){
            require(Math.abs(Math.abs(kinetic(l,accelerator(i)).getSpeed())-RPMS[i])<0.01,"Accelerator not driven by actual creative motor at "+RPMS[i]);
            var p=pack(l,i);require(p.targetInventory.getInventory()!=null,"Real packager inventory detection missing");
            if(reloaded){
                ItemStack saved=p.heldBox.copy();var h=l.getCapability(Capabilities.Item.BLOCK,packager(i),null);require(h!=null,"Packager native capability missing");
                try(Transaction tx=Transaction.openRoot()){eq(1,h.extract(0,ItemResource.of(saved),1,tx),"Collect saved package through native automation");tx.commit();}
                require(p.heldBox.isEmpty(),"Persisted package was not extracted");((Container)be(l,accelArchive(i))).setItem(0,saved);be(l,accelArchive(i)).setChanged();
            }
            ((Container)be(l,source(i))).setItem(0,namedInput());be(l,source(i)).setChanged();
            p.activate();eq(20,p.animationTicks,"Actual packager activation cycle");eq(40,p.buttonCooldown,"Actual activation cooldown");packageContents(p.heldBox);
            require(((Container)be(l,source(i))).isEmpty(),"Packager did not extract source items");
            priorAnimation[i]=p.animationTicks;priorCooldown[i]=p.buttonCooldown;priorLazy[i]=lazy(p);
        }
        timingStart=ticks;started=true;
        pass(reloaded?"additional/reloaded-apparatus-starts-second-real-packaging-cycle":"additional/native-packager-activation-extracts-named-input-and-creates-packages",Map.of("input_per_machine",7,"rpms",RPMS));
    }
    private static void logistics(ServerLevel l)throws Exception {
        if(logisticsDone)return;
        if(!started){if(ticks-setupTick>=50)startLogistics(l);return;}
        int elapsed=ticks-timingStart;require(elapsed<100,"Package acceleration timed out");
        for(int i=0;i<3;i++){
            var p=pack(l,i);int a=p.animationTicks,c=p.buttonCooldown,lc=lazy(p);int extra=i==0?0:i==1?1:8;
            require(Math.abs(Math.abs(kinetic(l,accelerator(i)).getSpeed())-RPMS[i])<0.01,"Accelerator shaft speed changed");
            require(a>=0&&c>=0,"Accelerator underflowed counters");
            if(elapsed==1){
                eq(priorAnimation[i]-1-extra,a,"Natural tick animation at "+RPMS[i]);
                eq(priorCooldown[i]-1-2*extra,c,"Natural tick cooldown at "+RPMS[i]);
                
            }
            // Both BEs tick naturally; iteration order can differ after a disk reload.
            // SmartBlockEntity resets only when its pre-decrement counter is <= 0.
            // A packager-first 256-rpm cycle therefore legitimately alternates 2/-7.
            int lazyRate=((Number)field(p,"lazyTickRate")).intValue();
            int packagerFirst=priorLazy[i]<=0?lazyRate-extra:priorLazy[i]-1-extra;
            int acceleratorFirst=priorLazy[i]-extra<=0?lazyRate:priorLazy[i]-extra-1;
            require(lc==packagerFirst||lc==acceleratorFirst,"Natural lazy recurrence at "+RPMS[i]+": prior="+priorLazy[i]+", actual="+lc+", expected="+packagerFirst+" or "+acceleratorFirst);
            if(priorLazy[i]>0&&lc==priorLazy[i]-1-extra)lazyCompared[i]=true;
            timingSamples.add(Map.of("elapsed_ticks",elapsed,"rpm",RPMS[i],"animation",a,"cooldown",c,"lazy_counter",lc));
            if(a==0&&animationDone[i]<0)animationDone[i]=elapsed;
            if(c==0&&cooldownDone[i]<0)cooldownDone[i]=elapsed;
            packageContents(p.heldBox);priorAnimation[i]=a;priorCooldown[i]=c;priorLazy[i]=lc;
        }
        if(elapsed>=40){
            for(int i=0;i<3;i++){require(lazyCompared[i],"Lazy countdown never observed at "+RPMS[i]);require(((Container)be(l,source(i))).isEmpty(),"Source inventory gained unexpected items");if(reloaded)packageContents(((Container)be(l,accelArchive(i))).getItem(0));}
            eq(20,animationDone[0],"Unpowered animation baseline");eq(40,cooldownDone[0],"Unpowered cooldown baseline");
            require(animationDone[1]>0&&animationDone[1]<animationDone[0]&&animationDone[2]>0&&animationDone[2]<animationDone[1],"Animation completion did not improve monotonically");
            require(cooldownDone[1]>0&&cooldownDone[1]<cooldownDone[0]&&cooldownDone[2]>0&&cooldownDone[2]<cooldownDone[1],"Cooldown completion did not improve monotonically");
            pass("additional/real-tick-speed-dependent-animation-cooldown-and-lazy-countdown",Map.of("rpm",RPMS,"animation_completion_ticks",animationDone,"cooldown_completion_ticks",cooldownDone));
            pass("additional/package-count-and-components-conserved-after-accelerated-cycles",Map.of("packages",3,"items_per_package",7,"source_items_remaining",0,"reloaded_cycle",reloaded));logisticsDone=true;
        }
    }
    private static void complete(ServerLevel l)throws Exception {
        for(BlockPos p:List.of(BATTERY,MOTOR,DRILL,HOPPER,OUTPUT))be(l,p).setChanged();
        for(int i=0;i<3;i++)for(BlockPos p:List.of(accelerator(i).west(),accelerator(i),packager(i),source(i),accelArchive(i)))be(l,p).setChanged();
        require(call(infrastructure,"getConnectionData",negativeWire)!=null&&call(infrastructure,"getConnectionData",positiveWire)!=null,"Final circuit not closed");
        if(!reloaded){Map<String,Object> f=new LinkedHashMap<>();f.put("fixture_id",fixtureId);f.put("create_boot_id",BOOT);f.put("world",root.resolve("world").toRealPath().toString());f.put("negative_wire",negativeWire);f.put("positive_wire",positiveWire);f.put("dirt_completed",dirt(l));f.put("game_time",l.getGameTime());f.put("saved_packages",3);Files.writeString(root.resolve("power-logistics-gameplay-fixture.json"),new GsonBuilder().setPrettyPrinting().create().toJson(f)+"\n");}
        result.put("final_dirt",dirt(l));result.put("minimum_check_count",reloaded?8:7);require(checks.size()>=(reloaded?8:7),"Incomplete check set");finished=true;write(reloaded?"reload-complete":"create-complete");
    }
    private static void write(String phase)throws Exception {
        result.put("phase",phase);result.put("status",finished&&!result.containsKey("fatal_error")?"passed":result.containsKey("fatal_error")?"failed":"running");result.put("passed",finished&&!result.containsKey("fatal_error"));
        result.put("ticks",ticks);result.put("checks",checks);result.put("power_samples",powerSamples);result.put("timing_samples",timingSamples);result.put("simulation_samples",simulationSamples);result.put("recorded_at",Instant.now().toString());
        Files.writeString(root.resolve("power-logistics-probe-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
    }
    private static void tick(ServerTickEvent.Post e){
        if(finished)return;
        try {
            ticks++;if(ticks==1||ticks%80==0)guard(e);if(ticks<40)return;
            ServerLevel l=e.getServer().overworld();
            if(setupTick==0)setup(l);
            power(l);logistics(l);if(powerDone&&logisticsDone)complete(l);
            else if(ticks%20==0)write("running");require(ticks<2400,"Total probe timeout");
        }catch(Throwable failure){finished=true;Throwable cause=failure instanceof java.lang.reflect.InvocationTargetException i?i.getTargetException():failure;cause.printStackTrace();result.put("fatal_error",cause.toString());try{write("failed");}catch(Exception nested){nested.printStackTrace();}}
    }
}