package dev.create263.probe;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import java.lang.reflect.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.function.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Destructive gameplay checks, exclusively inside an explicitly marked project-owned disposable world. */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class FiveAddonsProbe {
    private static final String ELECTRO="com.george_vi.electroenergetics.", FLUID="com.yision.fluidlogistics.";
    private static final String CEI="plus.dragons.createenchantmentindustry.common.", CDP="plus.dragons.createdragonsplus.common.";
    private static final String ADD="dev.khloeleclair.create.additionallogistics.common.";
    private static final String[] MODS={"electroenergetics","fxntstorage","fluidlogistics","create_enchantment_industry","create_dragons_plus","createadditionallogistics"};
    private static final Map<String,Object> result=new LinkedHashMap<>();
    private static final List<Map<String,Object>> checks=new ArrayList<>();
    private static final List<String> remaining=new ArrayList<>();
    private static final BlockPos BACKPACK=new BlockPos(176,-60,8), TANK=new BlockPos(180,-60,8), CONVERTER=new BlockPos(184,-60,8);
    private static final BlockPos BATTERY=new BlockPos(188,-60,8), WIRE_END=new BlockPos(192,-60,8), EDITOR=new BlockPos(196,-60,8), PRINTER=new BlockPos(200,-60,8);
    private static final BlockPos PIPE_SOURCE=new BlockPos(208,-60,8), PUMP=PIPE_SOURCE.east(), PIPE_OUTPUT=PIPE_SOURCE.east(3);
    private static Path root,workspace;
    private static boolean installed,finished,reloaded;
    private static int ticks;
    private static String fixtureId;
    private static Object infrastructure;
    private static long wireId;
    private static final String TOKEN="create263-five-addons-component-fixture";
    private FiveAddonsProbe() {}

    public static synchronized void install() {
        if(!Boolean.getBoolean("create263.fiveAddonsProbe") || installed)return;
        try {
            String configured=System.getProperty("create263.probeRoot");
            require(configured!=null&&!configured.isBlank(),"Missing create263.probeRoot");
            root=Path.of(configured).toRealPath();
            require(root.equals(FMLPaths.GAMEDIR.get().toRealPath()),"FML game directory differs from probeRoot");
            require(Files.isRegularFile(root.resolve(".create263-disposable-runtime"),LinkOption.NOFOLLOW_LINKS),"Missing disposable-runtime marker");
            for(Path candidate=root.getParent();candidate!=null;candidate=candidate.getParent())
                if(Files.isRegularFile(candidate.resolve("AGENTS.md"))&&Files.isDirectory(candidate.resolve("modules"))&&Files.isDirectory(candidate.resolve("tests/runtime-probe"))){workspace=candidate.toRealPath();break;}
            require(workspace!=null,"Cannot identify source workspace");
            Path allowed=workspace.resolve("runtime").toRealPath();
            require(root.startsWith(allowed)&&!root.equals(allowed),"Probe directory must be a child of this workspace/runtime");
            require(root.resolve(".create263-disposable-runtime").toRealPath().startsWith(root),"Marker escapes probe root");
            installed=true;NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class,FiveAddonsProbe::tick);
        }catch(Exception e){throw new IllegalStateException("Five-addons probe guard refused installation",e);}
    }
    private static void require(boolean b,String message){if(!b)throw new IllegalStateException(message);}
    private static void eq(long expected,long actual,String what){require(expected==actual,what+": expected="+expected+", actual="+actual);}
    @FunctionalInterface private interface Checked {Object run() throws Exception;}
    private static void check(String name,Checked action){
        Map<String,Object> entry=new LinkedHashMap<>();entry.put("name",name);
        try{entry.put("measured",action.run());entry.put("passed",true);}catch(Throwable t){
            Throwable e=t instanceof InvocationTargetException i?i.getTargetException():t;
            entry.put("passed",false);entry.put("error",e.toString());e.printStackTrace();
        }checks.add(entry);
    }
    private static Class<?> type(String name)throws Exception{return Class.forName(name);}
    private static Class<?> boxed(Class<?> c){if(!c.isPrimitive())return c;return Map.of(int.class,Integer.class,long.class,Long.class,float.class,Float.class,double.class,Double.class,boolean.class,Boolean.class,byte.class,Byte.class,short.class,Short.class,char.class,Character.class).get(c);}
    private static boolean accepts(Class<?>[] types,Object[] args){if(types.length!=args.length)return false;for(int i=0;i<args.length;i++)if(args[i]!=null&&!boxed(types[i]).isInstance(args[i]))return false;return true;}
    private static Object call(Object receiver,String name,Object...args)throws Exception{
        Class<?> c=receiver instanceof Class<?> cl?cl:receiver.getClass();
        for(Class<?> k=c;k!=null;k=k.getSuperclass())for(Method m:k.getDeclaredMethods())
            if(m.getName().equals(name)&&accepts(m.getParameterTypes(),args)){m.setAccessible(true);return m.invoke(receiver instanceof Class<?>?null:receiver,args);}
        for(Method m:c.getMethods())if(m.getName().equals(name)&&accepts(m.getParameterTypes(),args))return m.invoke(receiver instanceof Class<?>?null:receiver,args);
        throw new NoSuchMethodException(c.getName()+"."+name+" "+Arrays.toString(args));
    }
    private static Object make(String name,Object...args)throws Exception{for(Constructor<?> c:type(name).getDeclaredConstructors())if(accepts(c.getParameterTypes(),args)){c.setAccessible(true);return c.newInstance(args);}throw new NoSuchMethodException(name+" constructor");}
    private static Object field(Object object,String name)throws Exception{Class<?> c=object instanceof Class<?> cl?cl:object.getClass();for(;c!=null;c=c.getSuperclass())try{Field f=c.getDeclaredField(name);f.setAccessible(true);return f.get(object instanceof Class<?>?null:object);}catch(NoSuchFieldException ignored){}throw new NoSuchFieldException(name);}
    private static Object constant(String c,String f)throws Exception{return field(type(c),f);}
    private static ItemStack item(String id){Item item=BuiltInRegistries.ITEM.getValue(Identifier.parse(id));require(item!=null&&item!=Items.AIR,"Missing item "+id);return new ItemStack(item);}
    private static BlockState state(String id){Block block=BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));require(block!=null&&block!=Blocks.AIR,"Missing block "+id);return block.defaultBlockState();}
    private static BlockEntity place(ServerLevel level,BlockPos pos,BlockState state){level.removeBlock(pos,false);level.setBlock(pos,state,3);BlockEntity be=level.getBlockEntity(pos);require(be!=null,"No block entity at "+pos+" for "+state);return be;}
    private static ResourceHandler<FluidResource> fluids(ServerLevel level,BlockPos pos){var h=level.getCapability(Capabilities.Fluid.BLOCK,pos,null);require(h!=null,"No native fluid capability at "+pos);return h;}
    private static ResourceHandler<ItemResource> items(ServerLevel level,BlockPos pos){var h=level.getCapability(Capabilities.Item.BLOCK,pos,null);require(h!=null,"No native item capability at "+pos);return h;}
    private static FluidResource namedWater(){FluidStack f=new FluidStack(Fluids.WATER,1);f.set(DataComponents.CUSTOM_NAME,Component.literal(TOKEN));return FluidResource.of(f);}
    private static ItemResource namedDiamond(){ItemStack s=new ItemStack(Items.DIAMOND);s.set(DataComponents.CUSTOM_NAME,Component.literal(TOKEN));return ItemResource.of(s);}
    private static long total(ResourceHandler<?> h){long n=0;for(int i=0;i<h.size();i++)n+=h.getAmountAsLong(i);return n;}
    private static void fill(ResourceHandler<FluidResource> h,int slot,FluidResource r,int n){try(Transaction tx=Transaction.openRoot()){eq(n,h.insert(slot,r,n,tx),"fluid insertion");tx.commit();}}
    private static void write()throws Exception{
        result.put("checks",checks);result.put("remaining",remaining);result.put("recorded_at",Instant.now().toString());
        boolean terminal=Set.of("create-complete","reload-complete").contains(result.get("phase"));
        Object fixtureList=result.get("client_fixtures");
        boolean fixturesReady=fixtureId!=null&&fixtureList instanceof List<?> list&&list.size()>=10;
        result.put("minimum_check_count",35);result.put("actual_check_count",checks.size());result.put("required_fixtures_ready",fixturesReady);
        boolean passed=!result.containsKey("fatal_error")&&terminal&&fixturesReady&&checks.size()>=35
                &&checks.stream().allMatch(c->Boolean.TRUE.equals(c.get("passed")));
        boolean knownFailure=result.containsKey("fatal_error")||checks.stream().anyMatch(c->Boolean.FALSE.equals(c.get("passed")));
        if(terminal||knownFailure){result.put("passed",passed);result.put("status",passed?"passed":"failed");}
        else{result.remove("passed");result.put("status","running");}
        result.put("all_requested_features_verified",passed&&remaining.isEmpty());
        Files.writeString(root.resolve("five-addons-probe-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
    }
    private static void guard(ServerTickEvent.Post event)throws Exception{
        require(Boolean.getBoolean("create263.fiveAddonsProbe"),"Probe flag was removed");
        require(FMLPaths.GAMEDIR.get().toRealPath().equals(root),"Game directory changed");
        require(Files.isRegularFile(root.resolve(".create263-disposable-runtime"),LinkOption.NOFOLLOW_LINKS),"Marker removed");
        Path world=event.getServer().getWorldPath(LevelResource.ROOT).toRealPath();
        require(world.equals(root.resolve("world").toRealPath())&&world.startsWith(root),"World is not the disposable root/world");
    }
    private static void recipes(ServerLevel level){
        check("native-cooking-fuel/create-and-aeronautics-original-values",()->{
            Map<String,Integer> create=Map.ofEntries(Map.entry("blaze_cake",6400),Map.entry("creative_blaze_cake",Integer.MAX_VALUE),Map.entry("cardboard",1000),Map.entry("bound_cardboard_block",4000),Map.entry("cardboard_block",4000),Map.entry("cardboard_boots",1000),Map.entry("cardboard_chestplate",1000),Map.entry("cardboard_helmet",1000),Map.entry("cardboard_leggings",1000),Map.entry("cardboard_sword",1000));
            for(var entry:create.entrySet())eq(entry.getValue(),com.simibubi.create.foundation.item.CookingFuelHelper.burnTime(level,BlockPos.ZERO,item("create:"+entry.getKey())),"Create fuel "+entry.getKey());
            int envelopes=0;for(var color:DyeColor.values())for(String suffix:List.of("_envelope","_envelope_encased_shaft")){
                String id="aeronautics:"+color.getSerializedName()+suffix;ItemStack stack=item(id);require(stack.has(DataComponents.COOKING_FUEL),"Envelope native cooking fuel missing: "+id);eq(100,com.simibubi.create.foundation.item.CookingFuelHelper.burnTime(level,BlockPos.ZERO,stack),"Envelope burn ticks "+id);envelopes++;
            }eq(32,envelopes,"All envelope fuel variants");return Map.of("create_items",create.size(),"envelope_items",envelopes,"envelope_burn_ticks",100);
        });
        for(String mod:MODS)check("load-and-server-recipes/"+mod,()->{
            require(ModList.get().isLoaded(mod),"Mod missing: "+mod);long count=0;
            for(var recipe:level.recipeAccess().getRecipes())if(recipe.id().identifier().getNamespace().equals(mod))count++;
            require(count>0,"No actual server recipes loaded for "+mod);return Map.of("recipes",count,"loaded",true);
        });
    }

    private static void fixture(ServerLevel level)throws Exception{
        Path marker=root.resolve("five-addons-fixture.json");reloaded=Files.isRegularFile(marker);
        if(reloaded){var expected=JsonParser.parseString(Files.readString(marker)).getAsJsonObject();fixtureId=expected.get("fixture_id").getAsString();require(expected.get("world").getAsString().equals(root.resolve("world").toRealPath().toString()),"Fixture belongs to a different world");}
        else{fixtureId=UUID.randomUUID().toString();
            BlockEntity backpack=place(level,BACKPACK,state("fxntstorage:hardened_backpack"));
            var h=items(level,BACKPACK);try(Transaction tx=Transaction.openRoot()){eq(1024,h.insert(0,namedDiamond(),1024,tx),"backpack world fixture");tx.commit();}backpack.setChanged();
            place(level,TANK,state("fluidlogistics:multi_fluid_tank"));fill(fluids(level,TANK),0,namedWater(),1234);level.getBlockEntity(TANK).setChanged();
            Files.writeString(marker,new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("fixture_id",fixtureId,"world",root.resolve("world").toRealPath().toString(),"backpack_amount",1024,"fluid_amount",1234))+"\n");
        }
        result.put("fixture_id",fixtureId);result.put("world_reloaded",reloaded);
    }
    private static void storage(ServerLevel level){
        check("storage/real-loot-native-item-codec-and-blockitem-placement",()->StorageDropPlaceProbe.run(level));
        check("storage/isolated-codecs-transactions-and-vanilla-chest-data",()->{
            Map<String,Object> isolation=StorageIsolationProbe.run(level,root,reloaded);
            if(isolation.get("remaining") instanceof List<?> pending)for(Object entry:pending)remaining.add(entry.toString());
            return isolation;
        });
        check("storage/actual-world-save-and-reload",()->{
            var h=items(level,BACKPACK);eq(1024,h.getAmountAsLong(0),"saved backpack large count");require(h.getResource(0).equals(namedDiamond()),"saved backpack components");
            if(!reloaded){remaining.add("Restart this exact disposable world to verify Storage and Fluid persisted fixtures");return Map.of("phase","fixture-created","amount",1024);}
            return Map.of("phase","world-reloaded","amount",h.getAmountAsLong(0),"components",true);
        });
        check("storage/physical-backpack-multiple-views-shared-journal",()->{
            var a=items(level,BACKPACK);var b=items(level,BACKPACK);long before=a.getAmountAsLong(0);
            eq(108,a.size(),"Block backpack exposes only physical item slots");eq(108,b.size(),"Second block capability item slots");
            try(Transaction outer=Transaction.openRoot()){
                eq(17,a.extract(0,namedDiamond(),17,outer),"view one extract");
                try(Transaction nested=Transaction.open(outer)){eq(31,b.insert(0,namedDiamond(),31,nested),"view two insert");nested.commit();}
            }eq(before,a.getAmountAsLong(0),"outer abort restored both views");
            try(Transaction tx=Transaction.openRoot()){eq(1,b.extract(0,namedDiamond(),1,tx),"commit extract");tx.commit();}
            eq(before-1,a.getAmountAsLong(0),"cross-view committed amount");
            try(Transaction tx=Transaction.openRoot()){eq(1,a.insert(0,namedDiamond(),1,tx),"restore fixture");tx.commit();}
            return Map.of("before",before,"after_abort",before,"committed_delta",-1);
        });
        check("storage/backpack-item-access-large-components-and-abort",()->{
            ItemStack stack=item("fxntstorage:hardened_backpack");DataComponentType<Integer> mult=(DataComponentType<Integer>)constant("net.fxnt.fxntstorage.init.ModDataComponents","BACKPACK_STACK_MULTIPLIER");stack.set(mult,Integer.MAX_VALUE);
            ItemAccess access=ItemAccess.forStack(stack);var h=(ResourceHandler<ItemResource>)call(type("net.fxnt.fxntstorage.util.StorageItemTransfer"),"backpack",access);
            try(Transaction tx=Transaction.openRoot()){eq(1024,h.insert(0,namedDiamond(),1024,tx),"item backpack insert");}eq(0,total(h),"item backpack aborted");
            try(Transaction tx=Transaction.openRoot()){eq(1024,h.insert(0,namedDiamond(),1024,tx),"item backpack commit");tx.commit();}
            eq(1024,h.getAmountAsLong(0),"item backpack count");require(h.getResource(0).equals(namedDiamond()),"item backpack components");
            var encoded=ItemStack.CODEC.encodeStart(level.registryAccess().createSerializationContext(JsonOps.INSTANCE),access.getResource().toStack(1)).getOrThrow();
            var restored=ItemStack.CODEC.parse(level.registryAccess().createSerializationContext(JsonOps.INSTANCE),encoded).getOrThrow();
            var reload=(ResourceHandler<ItemResource>)call(type("net.fxnt.fxntstorage.util.StorageItemTransfer"),"backpack",ItemAccess.forStack(restored));eq(1024,reload.getAmountAsLong(0),"item codec large count");
            return Map.of("amount",1024,"codec_roundtrip",true,"components",true);
        });
        check("storage/actual-box-capability-and-abort",()->{
            var p=BACKPACK.south(3);place(level,p,state("fxntstorage:storage_box"));var h=items(level,p);
            try(Transaction tx=Transaction.openRoot()){eq(64,h.insert(0,namedDiamond(),64,tx),"box native insert");}eq(0,h.getAmountAsLong(0),"box abort");
            try(Transaction tx=Transaction.openRoot()){eq(64,h.insert(0,namedDiamond(),64,tx),"box commit");tx.commit();}eq(64,h.getAmountAsLong(0),"box committed");return Map.of("amount",64,"slots",h.size());
        });
    }
    private static void fluid(ServerLevel level){
        check("fluid/registered-global-modifier-and-real-resource-gauge-loot",()->{
            var manager=level.getServer().getServerResources().managers().getListener(net.neoforged.neoforge.resource.NeoForgeReloadListeners.LOOT_MODIFIERS_KEY);
            var id=Identifier.parse("fluidlogistics:factory_gauge_drops");var modifier=manager.getModifier(id);
            require(modifier!=null&&id.equals(manager.getId(modifier)),"Factory gauge global modifier not actually loaded");
            eq(1000,modifier.priority(),"Loaded gauge modifier priority");
            BlockPos pos=new BlockPos(216,-60,8);
            try {
                var gauge=(com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity)place(level,pos,AllBlocks.FACTORY_GAUGE.getDefaultState());
                gauge.initialize();for(var panel:gauge.panels.values())panel.active=false;
                var panel=gauge.panels.values().iterator().next();call(panel,"setGaugeTypeId",Identifier.parse("fluidlogistics:fluid"));panel.active=true;
                var drops=Block.getDrops(gauge.getBlockState(),level,pos,gauge);
                int resource=0,ordinary=0;for(ItemStack drop:drops){if(drop.is(item("fluidlogistics:fluid_factory_gauge").getItem()))resource+=drop.getCount();if(AllBlocks.FACTORY_GAUGE.isIn(drop))ordinary+=drop.getCount();}
                eq(1,resource,"Resource gauge actual loot quantity");eq(0,ordinary,"Resource gauge duplicated ordinary drop");
                return Map.of("registered_modifier",id.toString(),"priority",1000,"resource_gauge_drops",resource,"ordinary_gauge_drops",ordinary);
            } finally {level.removeBlock(pos,false);}
        });
        check("fluid/shared-capacity-multislot-nested-components",()->{
            int[] changed={0};var h=(ResourceHandler<FluidResource>)make(FLUID+"content.fluids.multiFluidTank.SmartMultiFluidTank",2000,3,(Consumer<FluidStack[]>)a->changed[0]++);
            FluidResource w=namedWater(),lava=FluidResource.of(Fluids.LAVA);
            try(Transaction outer=Transaction.openRoot()){eq(700,h.insert(0,w,700,outer),"first slot");try(Transaction nested=Transaction.open(outer)){eq(600,h.insert(1,lava,600,nested),"second slot");nested.commit();}}eq(0,total(h),"nested outer abort");eq(0,changed[0],"abort callbacks");
            fill(h,0,w,1200);try(Transaction tx=Transaction.openRoot()){eq(800,h.insert(1,lava,1200,tx),"shared capacity clamp");tx.commit();}eq(2000,total(h),"shared capacity total");
            try(Transaction tx=Transaction.openRoot()){eq(0,h.insert(0,FluidResource.of(Fluids.WATER),1,tx),"component mismatch");}require(h.getResource(0).equals(w),"component identity");
            CompoundTag encoded=(CompoundTag)call(h,"save",level.registryAccess(),new CompoundTag());var restored=(ResourceHandler<FluidResource>)make(FLUID+"content.fluids.multiFluidTank.SmartMultiFluidTank",2000,3,(Consumer<FluidStack[]>)a->{});call(restored,"load",level.registryAccess(),encoded);eq(2000,total(restored),"multitank save/load");require(restored.getResource(0).equals(w),"multitank saved components");
            return Map.of("total",total(h),"slot0",h.getAmountAsLong(0),"slot1",h.getAmountAsLong(1),"callbacks",changed[0]);
        });
        check("fluid/real-copper-bucket-ItemAccess",()->{
            ItemAccess access=ItemAccess.forStack(item("fluidlogistics:copper_bucket"));var h=access.getCapability(Capabilities.Fluid.ITEM);require(h!=null,"copper bucket native item capability missing");
            try(Transaction tx=Transaction.openRoot()){eq(2300,h.insert(0,namedWater(),2300,tx),"bucket simulated insert");}eq(0,total(h),"bucket abort");
            fill(h,0,namedWater(),2300);require(h.getResource(0).equals(namedWater()),"bucket components");
            try(Transaction outer=Transaction.openRoot()){try(Transaction nested=Transaction.open(outer)){eq(500,h.extract(0,namedWater(),500,nested),"bucket nested extract");nested.commit();}}eq(2300,total(h),"bucket nested abort");return Map.of("amount",total(h),"capacity",h.getCapacityAsLong(0,namedWater()));
        });

        check("fluid/ItemAccess-exchange-failure-filtered-strict-slot-preserves-items",()->{
            ItemStack one=item("fluidlogistics:copper_bucket");ItemAccess preparation=ItemAccess.forStack(one);
            var preparationHandler=preparation.getCapability(Capabilities.Fluid.ITEM);require(preparationHandler!=null,"preparation bucket capability");fill(preparationHandler,0,namedWater(),777);
            ItemStack filled=preparation.getResource().toStack(1);
            ItemStack.validateStrict(filled).getOrThrow();
            var container=new net.minecraft.world.SimpleContainer(filled.copy());
            var delegate=net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(container);
            ItemResource allowed=ItemResource.of(filled);
            ResourceHandler<ItemResource> filtered=new ResourceHandler<>() {
                public int size(){return delegate.size();}
                public ItemResource getResource(int i){return delegate.getResource(i);}
                public long getAmountAsLong(int i){return delegate.getAmountAsLong(i);}
                public long getCapacityAsLong(int i,ItemResource r){return isValid(i,r)?delegate.getCapacityAsLong(i,r):0;}
                public boolean isValid(int i,ItemResource r){return r.equals(allowed)&&delegate.isValid(i,r);}
                public int insert(int i,ItemResource r,int n,net.neoforged.neoforge.transfer.transaction.TransactionContext tx){return isValid(i,r)?delegate.insert(i,r,n,tx):0;}
                public int extract(int i,ItemResource r,int n,net.neoforged.neoforge.transfer.transaction.TransactionContext tx){return delegate.extract(i,r,n,tx);}
            };
            ItemAccess strict=ItemAccess.forHandlerIndexStrict(filtered,0);ItemResource original=strict.getResource();int count=strict.getAmount();
            eq(1,count,"legal filtered strict-slot initial count");var h=strict.getCapability(Capabilities.Fluid.ITEM);require(h!=null,"strict bucket capability");
            try(Transaction tx=Transaction.openRoot()){
                eq(0,h.insert(0,namedWater(),100,tx),"failed exchange insert accepted amount");
                eq(0,h.extract(0,namedWater(),100,tx),"failed exchange extract accepted amount");tx.commit();
            }
            eq(1,strict.getAmount(),"failed exchange original count");require(strict.getResource().equals(original),"failed exchange changed complete original ItemResource");
            require(ItemStack.matches(filled,container.getItem(0))&&filled.getComponentsPatch().equals(container.getItem(0).getComponentsPatch()),"Failed exchange changed physical item or component removal patch");
            eq(777,h.getAmountAsLong(0),"failed exchange original fluid amount");require(h.getResource(0).equals(namedWater()),"failed exchange original fluid components");
            return Map.of("accepted_insert",0,"accepted_extract",0,"item_count",strict.getAmount(),"fluid_mB_per_item",h.getAmountAsLong(0),"full_item_resource_equal",true,"replacement_rejected_by_resource_filter",true);
        });
        check("fluid/filter-owner-journal-and-transfer",()->{
            ResourceHandler<FluidResource> source=(ResourceHandler<FluidResource>)make(FLUID+"content.fluids.multiFluidTank.SmartMultiFluidTank",2000,2,(Consumer<FluidStack[]>)a->{});fill(source,0,namedWater(),1000);
            var filter=(ResourceHandler<FluidResource>)make(FLUID+"foundation.capability.FilteredFluidResourceHandler",(Supplier<ResourceHandler<FluidResource>>)()->source,(Predicate<FluidResource>)r->r.equals(namedWater()),(Predicate<FluidResource>)r->r.equals(namedWater()));
            var bucket=ItemAccess.forStack(item("fluidlogistics:copper_bucket")).getCapability(Capabilities.Fluid.ITEM);require(bucket!=null,"bucket capability");
            try(Transaction tx=Transaction.openRoot()){eq(0,filter.insert(1,FluidResource.of(Fluids.LAVA),1,tx),"filter rejection");eq(500,filter.extract(0,namedWater(),500,tx),"filtered extract");eq(500,bucket.insert(0,namedWater(),500,tx),"transport insert");}eq(1000,total(source),"source rollback");eq(0,total(bucket),"destination rollback");
            try(Transaction tx=Transaction.openRoot()){eq(500,filter.extract(0,namedWater(),500,tx),"filtered commit extract");eq(500,bucket.insert(0,namedWater(),500,tx),"filtered commit insert");tx.commit();}eq(500,total(source),"remaining source");eq(500,total(bucket),"transported amount");return Map.of("source",total(source),"bucket",total(bucket),"rejected_lava",true);
        });
        check("fluid/package-same-fluid-two-slots-index1-abort-and-commit",()->{
            ItemStack pack=item("fluidlogistics:fluid_package");Object c0=make(FLUID+"datacomponent.FluidTankContent",namedWater().toStack(2000)),c1=make(FLUID+"datacomponent.FluidTankContent",namedWater().toStack(3000));
            pack.set((DataComponentType)constant(FLUID+"registry.AllDataComponents","PACKAGE_FLUID_CONTENT"),make(FLUID+"datacomponent.PackageFluidContents",List.of(c0,c1)));
            var access=ItemAccess.forStack(pack);var h=access.getCapability(Capabilities.Fluid.ITEM);require(h!=null&&h.size()==2,"package two-slot native capability missing");
            try(Transaction tx=Transaction.openRoot()){eq(1000,h.extract(1,namedWater(),1000,tx),"package slot1 abort extract");}eq(2000,h.getAmountAsLong(0),"slot0 after abort");eq(3000,h.getAmountAsLong(1),"slot1 after abort");
            try(Transaction tx=Transaction.openRoot()){eq(0,h.extract(1,namedWater(),999,tx),"package requires full bucket");eq(1000,h.extract(1,namedWater(),1000,tx),"package slot1 commit");tx.commit();}
            eq(2000,h.getAmountAsLong(0),"slot0 unchanged");eq(2000,h.getAmountAsLong(1),"slot1 exact subtraction");require(h.getResource(0).equals(namedWater())&&h.getResource(1).equals(namedWater()),"package components lost");return Map.of("slot0",2000,"slot1",2000,"components",true);
        });
        check("fluid/actual-world-multitank-save-reload",()->{var h=fluids(level,TANK);eq(1234,h.getAmountAsLong(0),"world multitank amount");require(h.getResource(0).equals(namedWater()),"world multitank components");return Map.of("phase",reloaded?"world-reloaded":"fixture-created","amount",1234);});
    }
    private static void electro(ServerLevel level){
        check("electro/actual-converter-native-energy-transaction",()->{
            BlockState bs=state("electroenergetics:converter");BooleanProperty prop=(BooleanProperty)bs.getBlock().getStateDefinition().getProperty("source");require(prop!=null,"converter source property");
            var be=place(level,CONVERTER,bs.setValue(prop,true));call(be,"initialize");
            EnergyHandler h=level.getCapability(Capabilities.Energy.BLOCK,CONVERTER,null);require(h!=null,"converter FE capability");
            long initial=h.getAmountAsLong();try(Transaction tx=Transaction.openRoot()){eq(100,h.insert(100,tx),"converter abort insert");}eq(initial,h.getAmountAsLong(),"converter rollback");
            try(Transaction outer=Transaction.openRoot()){try(Transaction nested=Transaction.open(outer)){eq(100,h.insert(100,nested),"converter nested insert");nested.commit();}}eq(initial,h.getAmountAsLong(),"converter outer rollback");
            try(Transaction tx=Transaction.openRoot()){eq(100,h.insert(100,tx),"converter committed insert");tx.commit();}eq(initial+100,h.getAmountAsLong(),"converter actual device buffer");return Map.of("amount",h.getAmountAsLong(),"capacity",h.getCapacityAsLong(),"device",field(be,"converterDevice").getClass().getName());
        });
        check("electro/real-wire-network-and-Sable-projection",()->{
            place(level,BATTERY,state("electroenergetics:creative_battery"));place(level,WIRE_END,state("electroenergetics:converter"));
            infrastructure=call(type(ELECTRO+"simulation.infrastructure.InfrastructureSavedData"),"load",level);
            call(infrastructure,"registerOrUpdateNodes",BATTERY,List.of(0,1));call(infrastructure,"registerOrUpdateNodes",WIRE_END,List.of(0,1));
            Object n0=make(ELECTRO+"foundation.nodes.InWorldNode",0,BATTERY),n1=make(ELECTRO+"foundation.nodes.InWorldNode",0,WIRE_END);
            Object wire=call(constant(ELECTRO+"CEEWireTypes","COPPER"),"get");wireId=((Number)call(infrastructure,"connect",n0,n1,wire)).longValue();
            require(call(infrastructure,"getConnectionData",wireId)!=null,"wire adjacency missing");
            Object projected=call(n0,"sableSourcePos",level);require(BATTERY.equals(projected),"Sable static-world projection changed node position");require(call(n0,"getPosition",level)!=null,"Sable global node position missing");
            return Map.of("wire_id",wireId,"connection",true,"Sable_static_world_projection",projected.toString());
        });
        remaining.add("Electro motor under real electrical load, moving Sable sublevel wire relocation, trains, and 20 custom renderer visual checks");
    }
    private static void cei(ServerLevel level){
        check("CEI/player-and-registered-arm-fuel-and-input-interactions",()->CEIFuelProbe.run(level));
        check("CEI/six-real-block-state-replacement-and-removal-callbacks",()->{
            List<String> ids=List.of("printer","experience_lantern","experience_hatch","blaze_enchanter","blaze_forger","classic_blaze_enchanter");List<Map<String,Object>> verified=new ArrayList<>();
            for(int i=0;i<ids.size();i++) {
                String id="create_enchantment_industry:"+ids.get(i);BlockPos pos=new BlockPos(320+i*8,8,40);List<BlockPos> supports=new ArrayList<>();
                level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,true);level.getChunkAt(pos);require(level.getBlockState(pos).isAir(),"Removal fixture occupied: "+pos);
                try {
                    for(Direction direction:Direction.values()){BlockPos support=pos.relative(direction);require(level.getBlockState(support).isAir(),"Removal fixture support occupied: "+support);level.setBlock(support,Blocks.STONE.defaultBlockState(),3);supports.add(support);}
                    BlockState initial=state(id);var first=(com.simibubi.create.foundation.blockEntity.SmartBlockEntity)place(level,pos,initial);first.initialize();
                    var property=initial.getProperties().stream().filter(value->!value.getName().equals("waterlogged")).findFirst().orElseThrow();BlockState changed=initial.cycle(property);
                    require(level.setBlock(pos,changed,3),"Same-block state update failed: "+id);require(level.getBlockEntity(pos)==first,"Same-block state update replaced BE: "+id);
                    require(level.setBlock(pos,Blocks.STONE.defaultBlockState(),3),"Solid replacement failed: "+id);require(level.getBlockEntity(pos)==null&&level.getBlockState(pos).is(Blocks.STONE),"Old BE survived solid replacement: "+id);
                    var second=(com.simibubi.create.foundation.blockEntity.SmartBlockEntity)place(level,pos,initial);second.initialize();require(second!=first,"Re-placement reused removed BE: "+id);
                    require(level.removeBlock(pos,false),"Air removal failed: "+id);require(level.getBlockEntity(pos)==null&&level.getBlockState(pos).isAir(),"Old BE survived air removal: "+id);
                    verified.add(Map.of("block",id,"same_block_state_preserved_entity",true,"changed_property",property.getName(),"solid_replacement",true,"air_removal",true));
                } finally {level.removeBlock(pos,false);for(BlockPos support:supports)level.removeBlock(support,false);}
            }return Map.of("verified_blocks",verified,"count",verified.size());
        });
        check("CEI/loaded-experience-fuel-map-and-empty-bucket-template",()->{
            var data=(net.neoforged.neoforge.registries.datamaps.DataMapType<Item,Object>)constant(CEI+"registry.CEIDataMaps","EXPERIENCE_FUEL");
            ItemStack bucket=item("create_enchantment_industry:experience_bucket");Object fuel=bucket.getItem().builtInRegistryHolder().getData(data);
            require(fuel!=null,"Experience bucket fuel data map was not loaded");eq(1000,((Number)call(fuel,"experience")).longValue(),"Experience fuel units");require(!(Boolean)call(fuel,"special"),"Normal experience bucket became special");
            var template=(ItemStackTemplate)((Optional<?>)call(fuel,"usingConvertTo")).orElseThrow();ItemStack returned=template.create();
            require(returned.is(Items.BUCKET)&&returned.getCount()==1,"Experience fuel empty bucket return template");
            Object cake=item("create_enchantment_industry:experience_cake").getItem().builtInRegistryHolder().getData(data);require(cake!=null&&(Boolean)call(cake,"special"),"Special experience fuel missing");eq(1000,((Number)call(cake,"experience")).longValue(),"Special fuel units");
            return Map.of("experience",1000,"special_cake",true,"returned_item","minecraft:bucket","returned_count",1);
        });
        check("CEI/experience-fluid-runtime-conversion",()->{
            Object fluid=call(constant(CEI+"registry.CEIFluids","EXPERIENCE"),"get");var stack=new FluidStack((net.minecraft.world.level.material.Fluid)fluid,321);
            eq(321,((Number)call(type(CEI+"fluids.experience.ExperienceHelper"),"getExperienceFromFluid",stack)).longValue(),"XP fluid units");
            eq(321,((Number)call(type(CEI+"fluids.experience.ExperienceHelper"),"getFluidFromExperience",stack,321)).longValue(),"XP inverse units");return Map.of("mB",321,"XP",321);
        });
        check("CEI/real-enchanting-behaviour-runtime-registry",()->{
            Object b=make(CEI+"processing.enchanter.behaviour.EnchantingBehaviour");ItemStack sword=new ItemStack(Items.DIAMOND_SWORD);RandomSource rng=RandomSource.create(263);
            call(b,"update",level,sword,30,false,false,rng);require((Boolean)call(b,"canProcess",level,sword,false),"No runtime enchantments for diamond sword");
            ItemStack output=(ItemStack)call(b,"getResult",level,sword,rng,false);require(!output.getOrDefault(DataComponents.ENCHANTMENTS,net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY).isEmpty(),"Enchanting produced no enchantments");
            return Map.of("enchantments",output.getEnchantments().size(),"experience_cost",call(b,"getExperienceCost"));
        });
        check("CEI/actual-printer-template-naming",()->{
            Object printer=place(level,PRINTER,state("create_enchantment_industry:printer"));call(printer,"initialize");Object tank=field(printer,"tank");
            ItemStack tag=new ItemStack(Items.NAME_TAG);tag.set(DataComponents.CUSTOM_NAME,Component.literal(TOKEN));
            Object data=call(type(CEI+"fluids.printer.behaviour.PrintingBehaviour"),"create",level,tank,tag);Object b=call(data,"getOrThrow");
            var ink=new FluidStack(Fluids.WATER,1000);ItemStack output=(ItemStack)call(b,"getResult",level,new ItemStack(Items.PAPER),ink);
            Component name=output.getOrDefault(DataComponents.CUSTOM_NAME,output.get(DataComponents.ITEM_NAME));require(name!=null&&name.getString().equals(TOKEN),"Printer naming template output");
            return Map.of("output_name",name.getString(),"required_items",call(b,"getRequiredItemCount",level,new ItemStack(Items.PAPER)),"required_fluid",call(b,"getRequiredFluidAmount",level,new ItemStack(Items.PAPER),ink));
        });
        remaining.add("CEI belt-driven printer fluid consumption, enchanter XP debit, and client GUI/visual interactions");
    }

    private static void cdp(ServerLevel level){
        check("CDP/actual-fluid-hatch-world-fixture",()->{var p=new BlockPos(204,-60,8);var be=place(level,p,state("create_dragons_plus:fluid_hatch"));return Map.of("block_entity",be.getClass().getName());});
        check("CDP/runtime-fan-recipe-actual-processing",()->{
            Map<String,Object> processing=new LinkedHashMap<>();
            for(String kind:List.of("FREEZING","SANDING","ENDING")){
                Object fan=call(constant(CDP+"registry.CDPFanProcessingTypes",kind),"get");String matched=null;
                for(Item candidate:BuiltInRegistries.ITEM){ItemStack input=new ItemStack(candidate);if(input.isEmpty()||!(Boolean)call(fan,"canProcess",input,level))continue;
                    Object output=call(fan,"process",input.copy(),level);require(output instanceof List<?> list&&!list.isEmpty(),"Fan matched but produced nothing");matched=BuiltInRegistries.ITEM.getKey(candidate)+" -> "+output;break;}
                require(matched!=null,"No real runtime recipe matched fan "+kind);processing.put(kind,matched);
            }return processing;
        });
        check("CDP/real-FluidTankBehaviour-native-transactions",()->{
            Class<?> factoryType=type(CDP+"fluids.tank.FluidTankBehaviour$TankFactory");
            Object factory=Proxy.newProxyInstance(factoryType.getClassLoader(),new Class<?>[]{factoryType},(proxy,method,args)->{
                if(method.getName().equals("create"))return make("com.simibubi.create.foundation.fluid.SmartFluidTank",2000,args[0]);
                if(method.getName().equals("toString"))return "FiveAddonsProbe real SmartFluidTank constructor";
                throw new UnsupportedOperationException(method.toString());
            });
            BlockEntity owner=level.getBlockEntity(PRINTER);require(owner!=null,"CDP tank test requires its actual printer owner; an earlier printer check failed");
            Object behaviour=make(CDP+"fluids.tank.FluidTankBehaviour",owner,factory);
            var h=(ResourceHandler<FluidResource>)call(behaviour,"getResourceCapability");
            try(Transaction outer=Transaction.openRoot()){try(Transaction nested=Transaction.open(outer)){eq(777,h.insert(0,namedWater(),777,nested),"CDP nested insert");nested.commit();}}eq(0,total(h),"CDP outer abort");
            fill(h,0,namedWater(),777);require(h.getResource(0).equals(namedWater()),"CDP fluid components");return Map.of("amount",total(h),"capacity",h.getCapacityAsLong(0,namedWater()));
        });
        remaining.add("CDP fan world airflow, fluid hatch player interactions and recoloring visuals");
    }
    private static void additional(ServerLevel level){
        check("additional/optional-currency-map-guard",()->{
            require(!ModList.get().isLoaded("numismatics"),"This fixture expects Numismatics absent");
            var type=(net.neoforged.neoforge.registries.datamaps.DataMapType<Item,Object>)constant(ADD+"registries.CALDataMaps","CURRENCY_DATA");
            var data=BuiltInRegistries.ITEM.getDataMap(type);require(data.isEmpty(),"Uninstalled optional currency produced entries");return Map.of("numismatics_loaded",false,"resolved_currency_entries",data.size());
        });
        check("additional/actual-package-editor-inventory-and-machine-rules",()->{
            Object editor=place(level,EDITOR,state("createadditionallogistics:package_editor"));
            // Call the inherited Create API directly; enumerating every parent method resolves unrelated optional CC descriptors.
            ((com.simibubi.create.foundation.blockEntity.SmartBlockEntity)editor).initialize();var h=items(level,EDITOR);
            ItemStack box=(ItemStack)call(type("com.simibubi.create.content.logistics.box.PackageItem"),"containing",List.of(new ItemStack(Items.DIAMOND)));
            var held=type("com.simibubi.create.content.logistics.packager.PackagerBlockEntity").getField("heldBox");held.set(editor,box.copy());
            ItemResource resource=ItemResource.of(box);try(Transaction tx=Transaction.openRoot()){eq(1,h.extract(0,resource,1,tx),"editor native package extraction");}eq(1,total(h),"editor package rollback");
            Object rule=call(type(ADD+"PatternReplacement"),"of","regex:warehouse-(.*)","dock-$1",false,false);call(editor,"setComputerReplacements",List.of(rule));
            String address=(String)call(editor,"applyRules","warehouse-east");require(address.equals("dock-east"),"Actual editor rule result "+address);
            call(type("com.simibubi.create.content.logistics.box.PackageItem"),"addAddress",box,"warehouse-east");call(editor,"readdressPackage",box);
            require(call(type("com.simibubi.create.content.logistics.box.PackageItem"),"getAddress",box).equals("dock-east"),"Package address was not mutated by machine");
            return Map.of("address",address,"native_inventory_slots",h.size(),"inventory_abort",true);
        });
        remaining.add("Additional accelerator cycle timing, vertical belts, train network GUI and computer peripherals");
    }
    private static void pipeline(ServerLevel level){
        check("fluid/setup-real-Create-pipe-interoperability",()->{
            for(BlockPos p:List.of(PIPE_SOURCE,PUMP,PUMP.east(),PIPE_OUTPUT,PUMP.above(),PUMP.above().west()))level.removeBlock(p,false);
            place(level,PIPE_SOURCE,AllBlocks.FLUID_TANK.getDefaultState());fill(fluids(level,PIPE_SOURCE),0,namedWater(),6000);
            place(level,PIPE_OUTPUT,state("fluidlogistics:multi_fluid_tank"));
            level.setBlock(PUMP.east(),AllBlocks.FLUID_PIPE.getDefaultState().setValue(BlockStateProperties.EAST,true).setValue(BlockStateProperties.WEST,true),3);
            place(level,PUMP,AllBlocks.MECHANICAL_PUMP.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST));
            place(level,PUMP.above(),AllBlocks.COGWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS,Direction.Axis.X));
            var motor=(CreativeMotorBlockEntity)place(level,PUMP.above().west(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST));motor.generatedSpeed.setValue(-64);motor.updateGeneratedRotation();
            return Map.of("initial_source_mB",6000,"target",PIPE_OUTPUT.toShortString());
        });
    }
    private static void clientFixtures(ServerLevel level){
        List<Map<String,Object>> fixtures=new ArrayList<>();
        Map<BlockPos,String> positions=new LinkedHashMap<>();positions.put(BACKPACK,"Storage persisted backpack / large inventory / GUI");positions.put(BACKPACK.south(3),"Storage box / GUI");positions.put(TANK,"Fluid persisted named-water multi tank");positions.put(CONVERTER,"Electro FE converter");positions.put(BATTERY,"Electro creative battery / real wire start");positions.put(WIRE_END,"Electro wire end converter");positions.put(EDITOR,"Additional package editor / address mutation");positions.put(PRINTER,"CEI printer / template behaviour");positions.put(new BlockPos(204,-60,8),"CDP fluid hatch / fluid filtering GUI");positions.put(PIPE_OUTPUT,"Fluid native Create pump pipeline output");
        for(var e:positions.entrySet()){
            BlockEntity be=level.getBlockEntity(e.getKey());if(be==null)continue;
            fixtures.add(Map.of("x",e.getKey().getX(),"y",e.getKey().getY(),"z",e.getKey().getZ(),"block_id",BuiltInRegistries.BLOCK.getKey(be.getBlockState().getBlock()).toString(),"block_entity_class",be.getClass().getName(),"role",e.getValue()));
        }result.put("client_fixtures",fixtures);
    }
    private static void finish(ServerLevel level)throws Exception{
        check("fluid/real-motor-cogwheel-pump-pipe-into-multitank",()->{long source=total(fluids(level,PIPE_SOURCE)),target=total(fluids(level,PIPE_OUTPUT));require(source<6000&&target>0,"Actual Create pipeline transferred no fluid");require(fluids(level,PIPE_OUTPUT).getResource(0).equals(namedWater()),"Pipeline fluid components lost");return Map.of("source_mB",source,"target_mB",target);});
        if(infrastructure!=null)check("electro/wire-still-connected-after-server-ticks",()->{require(call(infrastructure,"getConnectionData",wireId)!=null,"Wire disconnected during actual simulation");return Map.of("wire_id",wireId,"server_ticks",ticks);});
        clientFixtures(level);result.put("phase",reloaded?"reload-complete":"create-complete");result.put("server_ticks",ticks);finished=true;write();
    }
    private static void tick(ServerTickEvent.Post event){
        if(finished)return;
        try{
            guard(event);ticks++;ServerLevel level=event.getServer().overworld();
            if(ticks==40){for(int x=11;x<=13;x++)level.setChunkForced(x,0,true);recipes(level);check("fixture/prepare-world",()->{fixture(level);return Map.of("id",fixtureId,"reload",reloaded);});pipeline(level);}
            if(ticks==80){storage(level);fluid(level);electro(level);cei(level);cdp(level);additional(level);clientFixtures(level);result.put("phase","running-machines");write();}
            if(ticks==300)finish(level);
        }catch(Throwable e){e.printStackTrace();finished=true;result.put("fatal_error",e.toString());result.put("phase","failed");try{write();}catch(Exception secondary){secondary.printStackTrace();}}
    }
}
