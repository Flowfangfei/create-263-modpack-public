package dev.create263.probe;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import java.lang.reflect.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Opt-in disposable worlds; no direct machine tick or processing callback invocation. */
public final class ProcessingGameplayProbe {
    private static final String TOKEN="create263-processing-gameplay";
    private static final BlockPos ENCHANTER=new BlockPos(480,100,0), FORGER=new BlockPos(484,100,0);
    private static final BlockPos NAME_DEPOT=new BlockPos(488,100,0), BOOK_DEPOT=new BlockPos(492,100,0);
    private static final BlockPos FAN=new BlockPos(481,101,12), COLOR_DEPOT=new BlockPos(484,100,12);
    private static final List<BlockPos> DEPOTS=List.of(NAME_DEPOT,BOOK_DEPOT,COLOR_DEPOT);
    private static final List<Map<String,Object>> checks=new ArrayList<>();
    private static final Map<String,Object> result=new LinkedHashMap<>();
    private static final Map<String,Integer> firstActive=new LinkedHashMap<>();
    private static Path root,workspace;
    private static JsonObject fixture,settled;
    private static boolean installed,finished,reloaded;
    private static int ticks,settleTick,enchanterCost,forgerCost,nameCost,bookCost;
    private static final long PROCESS_ID=ProcessHandle.current().pid();
    private static final long JVM_STARTED_AT=java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
    private static final int CREDIT=4000;
    private static Fluid experience;
    private static ItemStack inputSword,firstForgerInput,secondForgerInput,printBookTemplate;
    private ProcessingGameplayProbe() {}
    public static synchronized void install() {
        if(!Boolean.getBoolean("create263.processingGameplayProbe")||installed)return;
        try {
            String configured=System.getProperty("create263.probeRoot");
            require(configured!=null&&!configured.isBlank(),"Missing create263.probeRoot");
            root=Path.of(configured).toRealPath();
            require(root.equals(FMLPaths.GAMEDIR.get().toRealPath()),"FML game directory differs from probeRoot");
            require(Files.isRegularFile(root.resolve(".create263-disposable-runtime"),LinkOption.NOFOLLOW_LINKS),"Missing disposable-runtime marker");
            for(Path p=root.getParent();p!=null;p=p.getParent())
                if(Files.isRegularFile(p.resolve("AGENTS.md"))&&Files.isDirectory(p.resolve("modules"))&&Files.isDirectory(p.resolve("tests/runtime-probe"))){workspace=p.toRealPath();break;}
            require(workspace!=null,"Cannot identify source workspace");
            Path allowed=workspace.resolve("runtime").toRealPath();
            require(root.startsWith(allowed)&&!root.equals(allowed),"Probe directory must be a child of workspace/runtime");
            require(root.resolve(".create263-disposable-runtime").toRealPath().startsWith(root),"Marker escapes probe root");
            installed=true;NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class,ProcessingGameplayProbe::tick);
        }catch(Exception e){throw new IllegalStateException("Processing gameplay guard refused installation",e);}
    }
    private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
    private static void eq(long e,long a,String n){require(e==a,n+": expected="+e+", actual="+a);}
    private static void same(ItemStack e,ItemStack a,String n){require(ItemStack.matches(e,a),n+": expected="+e+", actual="+a);}
    @FunctionalInterface private interface Checked{Object run()throws Exception;}
    private static void check(String n,Checked action){
        Map<String,Object> entry=new LinkedHashMap<>();entry.put("name",n);
        try{entry.put("measured",action.run());entry.put("passed",true);}
        catch(Throwable t){Throwable e=t instanceof InvocationTargetException i?i.getTargetException():t;entry.put("passed",false);entry.put("error",e.toString());e.printStackTrace();}
        checks.add(entry);
    }
    private static Object field(Object o,String n)throws Exception{
        for(Class<?> c=o.getClass();c!=null;c=c.getSuperclass())try{Field f=c.getDeclaredField(n);f.setAccessible(true);return f.get(o);}catch(NoSuchFieldException ignored){}
        throw new NoSuchFieldException(n);
    }
    private static Class<?> box(Class<?> c){return !c.isPrimitive()?c:Map.of(int.class,Integer.class,long.class,Long.class,boolean.class,Boolean.class,float.class,Float.class,double.class,Double.class).get(c);}
    private static Object call(Object o,String n,Object...args)throws Exception{
        for(Class<?> c=o.getClass();c!=null;c=c.getSuperclass())for(Method m:c.getDeclaredMethods()){
            if(!m.getName().equals(n)||m.getParameterCount()!=args.length)continue;
            boolean accepts=true;for(int i=0;i<args.length;i++)if(args[i]!=null&&!box(m.getParameterTypes()[i]).isInstance(args[i]))accepts=false;
            if(accepts){m.setAccessible(true);return m.invoke(o,args);}
        }
        throw new NoSuchMethodException(o.getClass().getName()+"."+n);
    }
    private static int number(Object o){return ((Number)o).intValue();}
    private static BlockState state(String id){var b=BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));require(b!=null&&b!=Blocks.AIR,"Missing block "+id);return b.defaultBlockState();}
    private static Fluid fluid(String id){var f=BuiltInRegistries.FLUID.getValue(Identifier.parse(id));require(f!=null&&f!=Fluids.EMPTY,"Missing fluid "+id);return f;}
    private static BlockEntity be(ServerLevel l,BlockPos p){var b=l.getBlockEntity(p);require(b!=null,"Missing machine "+p);return b;}
    private static BlockEntity place(ServerLevel l,BlockPos p,BlockState s){require(l.getBlockState(p).isAir()&&l.getBlockEntity(p)==null,"Fixture cell occupied: "+p);require(l.setBlock(p,s,3),"Failed to place "+p);return be(l,p);}
    private static ResourceHandler<FluidResource> fluids(ServerLevel l,BlockPos p){var h=l.getCapability(Capabilities.Fluid.BLOCK,p,(Direction)null);require(h!=null,"No native fluid capability "+p);return h;}
    private static ResourceHandler<ItemResource> items(ServerLevel l,BlockPos p){var h=l.getCapability(Capabilities.Item.BLOCK,p,(Direction)null);require(h!=null,"No native item capability "+p);return h;}
    private static void fill(ServerLevel l,BlockPos p,int n){if(n==0)return;var h=fluids(l,p);try(Transaction tx=Transaction.openRoot()){eq(n,h.insert(0,FluidResource.of(experience),n,tx),"Native XP insertion "+p);tx.commit();}}
    private static void insert(ServerLevel l,BlockPos p,ItemStack s){var h=items(l,p);try(Transaction tx=Transaction.openRoot()){eq(s.getCount(),h.insert(0,ItemResource.of(s),s.getCount(),tx),"Native depot insertion "+p);tx.commit();}}
    private static int amount(ResourceHandler<?> h){int n=0;for(int i=0;i<h.size();i++)n+=h.getAmountAsInt(i);return n;}
    private static int xp(ServerLevel l,BlockPos p){return amount(fluids(l,p));}
    private static ItemStack held(ServerLevel l)throws Exception{return ((ItemStack)field(be(l,ENCHANTER),"heldItem")).copy();}
    private static ItemStack forged(ServerLevel l,int slot)throws Exception{return ((ItemStack)call(field(be(l,FORGER),"inventory"),"getStackInSlot",slot)).copy();}
    private static ItemStack namedSword(String suffix){var s=new ItemStack(Items.DIAMOND_SWORD);s.set(DataComponents.CUSTOM_NAME,Component.literal(TOKEN+"/"+suffix));return s;}
    private static ItemStack enchanted(ServerLevel l,String suffix,int sharpness){var s=namedSword(suffix);s.enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS),sharpness);return s;}
    private static int sharpness(ServerLevel l,ItemStack s){return EnchantmentHelper.getEnchantmentsForCrafting(s).getLevel(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS));}
    private static JsonElement encode(ServerLevel l,ItemStack s){return ItemStack.OPTIONAL_CODEC.encodeStart(l.registryAccess().createSerializationContext(JsonOps.INSTANCE),s).getOrThrow();}
    private static JsonArray stacks(ServerLevel l,ResourceHandler<ItemResource> h){JsonArray out=new JsonArray();for(int i=0;i<h.size();i++)out.add(encode(l,h.getResource(i).toStack(h.getAmountAsInt(i))));return out;}
    private static JsonObject snapshot(ServerLevel l)throws Exception{
        JsonObject s=new JsonObject();s.add("enchanter_item",encode(l,held(l)));s.addProperty("enchanter_xp",xp(l,ENCHANTER));
        JsonArray forge=new JsonArray();for(int slot=0;slot<4;slot++)forge.add(encode(l,forged(l,slot)));s.add("forger_items",forge);s.addProperty("forger_xp",xp(l,FORGER));
        for(int i=0;i<DEPOTS.size();i++)s.add("depot_"+i,stacks(l,items(l,DEPOTS.get(i))));
        s.addProperty("name_printer_xp",xp(l,NAME_DEPOT.above(2)));s.addProperty("book_printer_xp",xp(l,BOOK_DEPOT.above(2)));
        s.add("name_printer_template",encode(l,(ItemStack)call(field(be(l,NAME_DEPOT.above(2)),"printer"),"getFilter")));
        s.add("book_printer_template",encode(l,(ItemStack)call(field(be(l,BOOK_DEPOT.above(2)),"printer"),"getFilter")));
        s.addProperty("enchanter_processing_time",number(field(be(l,ENCHANTER),"processingTime")));
        s.addProperty("forger_processing_time",number(field(be(l,FORGER),"processingTime")));
        s.addProperty("name_printer_processing_ticks",number(field(be(l,NAME_DEPOT.above(2)),"processingTicks")));
        s.addProperty("book_printer_processing_ticks",number(field(be(l,BOOK_DEPOT.above(2)),"processingTicks")));
        s.addProperty("red_dye_source",l.getFluidState(FAN.east()).isSource());
        s.addProperty("dye_catalyst_block",BuiltInRegistries.BLOCK.getKey(l.getBlockState(FAN.east()).getBlock()).toString());
        s.addProperty("dye_barrier_block",BuiltInRegistries.BLOCK.getKey(l.getBlockState(FAN.east(2)).getBlock()).toString());
        return s;
    }
    private static boolean printedName(ItemStack s){Component n=s.getOrDefault(DataComponents.CUSTOM_NAME,s.get(DataComponents.ITEM_NAME));return s.is(Items.DIAMOND)&&n!=null&&n.getString().equals(TOKEN+"/printed");}
    private static int matching(ResourceHandler<ItemResource> h,java.util.function.Predicate<ItemStack> p){int n=0;for(int i=0;i<h.size();i++)if(p.test(h.getResource(i).toStack(1)))n+=h.getAmountAsInt(i);return n;}
    private static boolean copiedBook(ServerLevel l,ItemStack s){return s.is(Items.ENCHANTED_BOOK)&&sharpness(l,s)==2;}
    private static void printer(ServerLevel l,BlockPos d,ItemStack template,ItemStack input,int insufficient)throws Exception{
        place(l,d,AllBlocks.DEPOT.getDefaultState());BlockEntity p=place(l,d.above(2),state("create_enchantment_industry:printer"));
        require(Boolean.TRUE.equals(call(field(p,"printer"),"setFilter",template)),"Printer rejected template");insert(l,d,input);fill(l,d.above(2),insufficient);
    }
    private static void setup(ServerLevel l)throws Exception{
        require(!Files.exists(root.resolve("processing-gameplay-fixture.json")),"Create phase refuses existing processing fixture");experience=fluid("create_enchantment_industry:experience");
        BlockEntity enchanter=place(l,ENCHANTER,state("create_enchantment_industry:blaze_enchanter"));Object enchanting=field(enchanter,"enchanter");call(enchanting,"setValue",30);
        inputSword=namedSword("enchanter");inputSword.set(DataComponents.DAMAGE,7);
        require(((ItemStack)call(enchanter,"insertItem",inputSword.copy(),false)).isEmpty(),"Enchanter rejected sword");
        enchanterCost=number(call(enchanting,"getExperienceCost"));require(enchanterCost>0&&enchanterCost<=CREDIT,"Invalid enchanter cost "+enchanterCost);same(inputSword,held(l),"Enchanter original components");
        BlockEntity forger=place(l,FORGER,state("create_enchantment_industry:blaze_forger"));firstForgerInput=enchanted(l,"forger-base",1);secondForgerInput=enchanted(l,"forger-addition",1);
        require(((ItemStack)call(forger,"insertAutomationItem",firstForgerInput.copy(),false)).isEmpty(),"First automation input rejected");
        require(((ItemStack)call(forger,"insertAutomationItem",secondForgerInput.copy(),false)).isEmpty(),"Second automation input rejected");
        Object inventory=field(forger,"inventory"),preview=call(inventory,"getLastResult");require(Boolean.TRUE.equals(call(preview,"valid")),"Merge preview invalid: "+preview);
        eq(2,sharpness(l,(ItemStack)call(preview,"primaryOutput")),"Merge preview sharpness");forgerCost=number(call(inventory,"getExperienceCost"));require(forgerCost>0&&forgerCost<=CREDIT,"Invalid forger cost");
        var nameTag=new ItemStack(Items.NAME_TAG);nameTag.set(DataComponents.CUSTOM_NAME,Component.literal(TOKEN+"/printed"));printer(l,NAME_DEPOT,nameTag,new ItemStack(Items.DIAMOND,3),0);
        Object naming=call(field(be(l,NAME_DEPOT.above(2)),"printer"),"getPrintingBehaviour");
        nameCost=number(call(naming,"getRequiredFluidAmount",l,new ItemStack(Items.DIAMOND),new FluidStack(experience,CREDIT)));
        eq(10,nameCost,"Loaded native custom-name ingredient cost");fill(l,NAME_DEPOT.above(2),nameCost-1);
        printBookTemplate=new ItemStack(Items.ENCHANTED_BOOK);printBookTemplate.enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS),2);
        printer(l,BOOK_DEPOT,printBookTemplate,new ItemStack(Items.BOOK,2),0);
        Object printing=call(field(be(l,BOOK_DEPOT.above(2)),"printer"),"getPrintingBehaviour");bookCost=number(call(printing,"getRequiredFluidAmount",l,new ItemStack(Items.BOOK),new FluidStack(experience,4000)));
        require(bookCost>0&&bookCost*2<=4000,"Invalid book-copy cost "+bookCost);fill(l,BOOK_DEPOT.above(2),bookCost-1);setupFan(l);
        fixture=new JsonObject();fixture.addProperty("fixture_id",UUID.randomUUID().toString());fixture.addProperty("world",root.resolve("world").toRealPath().toString());
        fixture.addProperty("create_process_id",PROCESS_ID);fixture.addProperty("create_jvm_started_at_epoch_ms",JVM_STARTED_AT);
        fixture.addProperty("enchanter_cost_mb",enchanterCost);fixture.addProperty("forger_cost_mb",forgerCost);fixture.addProperty("name_cost_mb",nameCost);fixture.addProperty("book_cost_mb",bookCost);
        result.put("fixture_id",fixture.get("fixture_id").getAsString());result.put("phase","negative-controls-running");
        result.put("initial_inputs",Map.of("enchanter_swords",1,"forger_swords",2,"diamonds_for_naming",3,"books_for_copy",2,"white_wool_for_coloring",17));
    }
    private static void setupFan(ServerLevel l){
        place(l,COLOR_DEPOT,AllBlocks.DEPOT.getDefaultState());insert(l,COLOR_DEPOT,new ItemStack(Items.WOOL.white(),17));
        place(l,FAN,AllBlocks.ENCASED_FAN.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST));
        var motor=(CreativeMotorBlockEntity)place(l,FAN.west(),AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING,Direction.EAST));motor.generatedSpeed.setValue(0);motor.updateGeneratedRotation();
        BlockPos catalyst=FAN.east();for(BlockPos p:List.of(catalyst.below(),catalyst.north(),catalyst.south())){require(l.getBlockState(p).isAir(),"Dye basin cell occupied "+p);l.setBlock(p,Blocks.STONE.defaultBlockState(),3);}
        BlockPos barrier=catalyst.east();require(l.getBlockState(barrier).isAir(),"Dye barrier occupied");
        require(AllTags.AllBlockTags.FAN_TRANSPARENT.matches(Blocks.IRON_BARS.defaultBlockState()),"Loaded iron-bars tag must pass native air currents");
        require(l.setBlock(barrier,Blocks.IRON_BARS.defaultBlockState(),3),"Failed dye barrier placement");
        require(l.getBlockState(catalyst).isAir(),"Dye catalyst occupied");require(l.setBlock(catalyst,fluid("create_dragons_plus:red_dye").defaultFluidState().createLegacyBlock(),3),"Failed dye source placement");
    }
    private static void negativeControls(ServerLevel l)throws Exception{
        check("CEI/enchanter-no-XP-holds-unmodified-input",()->{
            same(inputSword,held(l),"No-XP enchanter input");eq(0,xp(l,ENCHANTER),"No-XP enchanter fluid");eq(-1,number(field(be(l,ENCHANTER),"processingTime")),"No-XP enchanter timer");return Map.of("waited_server_ticks",60,"input_count",1,"experience_mb",0);
        });
        check("CEI/forger-no-XP-retains-both-inputs-no-output",()->{
            same(firstForgerInput,forged(l,0),"No-XP forge first");same(secondForgerInput,forged(l,1),"No-XP forge second");require(forged(l,2).isEmpty()&&forged(l,3).isEmpty(),"Forger emitted with no XP");eq(0,xp(l,FORGER),"No-XP forge fluid");return Map.of("waited_server_ticks",60,"input_count",2,"output_count",0);
        });
        check("CEI/printer-below-cost-holds-items-and-preserves-fluid",()->{
            eq(3,amount(items(l,NAME_DEPOT)),"Naming input count");eq(0,matching(items(l,NAME_DEPOT),ProcessingGameplayProbe::printedName),"Premature named outputs");eq(nameCost-1,xp(l,NAME_DEPOT.above(2)),"Insufficient naming fluid retained");eq(-1,number(field(be(l,NAME_DEPOT.above(2)),"processingTicks")),"Naming timer did not start");
            eq(2,amount(items(l,BOOK_DEPOT)),"Book input count");eq(0,matching(items(l,BOOK_DEPOT),s->copiedBook(l,s)),"Premature copied books");eq(bookCost-1,xp(l,BOOK_DEPOT.above(2)),"Insufficient book-copy fluid retained");return Map.of("name_fluid_mb",nameCost-1,"name_cost_mb",nameCost,"book_fluid_mb",bookCost-1,"book_cost_mb",bookCost);
        });
        check("CDP/unpowered-fan-leaves-white-wool",()->{
            assertDyeIsolation(l);var fan=(EncasedFanBlockEntity)be(l,FAN);require(fan.getSpeed()==0,"Fan was powered in negative control");eq(17,matching(items(l,COLOR_DEPOT),s->s.is(Items.WOOL.white())),"Unpowered white wool");return Map.of("rpm",fan.getSpeed(),"white_wool",17);
        });
        enchanterCost=number(call(field(be(l,ENCHANTER),"enchanter"),"getExperienceCost"));require(enchanterCost>0&&enchanterCost<=CREDIT,"Post-initialization enchanter cost invalid");
        fill(l,ENCHANTER,CREDIT);fill(l,FORGER,CREDIT);fill(l,NAME_DEPOT.above(2),2*nameCost+1);fill(l,BOOK_DEPOT.above(2),bookCost+1);
        var motor=(CreativeMotorBlockEntity)be(l,FAN.west());motor.generatedSpeed.setValue(256);motor.updateGeneratedRotation();result.put("phase","native-processing-running");
    }
    private static void observe(ServerLevel l)throws Exception{
        if(number(field(be(l,ENCHANTER),"processingTime"))>0){firstActive.putIfAbsent("enchanter",ticks);eq(CREDIT,xp(l,ENCHANTER),"Enchanter debited before completion");}
        if(!held(l).getEnchantments().isEmpty())firstActive.putIfAbsent("enchanter_completed",ticks);
        if(number(field(be(l,FORGER),"processingTime"))>0){firstActive.putIfAbsent("forger",ticks);eq(CREDIT,xp(l,FORGER),"Forger debited before completion");}
        if(!forged(l,2).isEmpty())firstActive.putIfAbsent("forger_completed",ticks);
        if(number(field(be(l,NAME_DEPOT.above(2)),"processingTicks"))>=0)firstActive.putIfAbsent("name_printer",ticks);
        if(number(field(be(l,BOOK_DEPOT.above(2)),"processingTicks"))>=0)firstActive.putIfAbsent("book_printer",ticks);
        if(matching(items(l,COLOR_DEPOT),s->s.is(Items.WOOL.red()))>0)firstActive.putIfAbsent("first_red_wool",ticks);
    }
    private static void positiveChecks(ServerLevel l){
        check("CEI/enchanter-real-200-tick-completion-and-exact-XP-debit",()->{
            require(firstActive.containsKey("enchanter")&&firstActive.containsKey("enchanter_completed"),"Enchanter never completed native processing");require(firstActive.get("enchanter_completed")-firstActive.get("enchanter")>=200,"Enchanter completed before two hundred native ticks");ItemStack out=held(l);require(out.is(Items.DIAMOND_SWORD)&&!out.getEnchantments().isEmpty(),"No enchanted sword output");eq(1,out.getCount(),"Enchanted output count");require(inputSword.get(DataComponents.CUSTOM_NAME).equals(out.get(DataComponents.CUSTOM_NAME)),"Enchant lost custom name");eq(7,out.getDamageValue(),"Enchant lost damage");eq(CREDIT-enchanterCost,xp(l,ENCHANTER),"Exact one-operation XP debit");
            same(out,(ItemStack)call(be(l,ENCHANTER),"extractItem",false,true),"Output extraction simulation");same(out,held(l),"Simulation changed held output");return Map.of("active_at_server_tick",firstActive.get("enchanter"),"observed_at_server_tick",ticks,"cost_mb",enchanterCost,"remaining_mb",xp(l,ENCHANTER),"enchantments",out.getEnchantments().size(),"output_count",1);
        });
        check("CEI/forger-real-200-tick-sharpness-merge-input-debit-output",()->{
            require(firstActive.containsKey("forger")&&firstActive.containsKey("forger_completed"),"Forger never completed native processing");require(firstActive.get("forger_completed")-firstActive.get("forger")>=200,"Forger completed before two hundred native ticks");require(forged(l,0).isEmpty()&&forged(l,1).isEmpty(),"Both forge inputs not consumed");ItemStack out=forged(l,2);require(out.is(Items.DIAMOND_SWORD),"Forger output is not sword");eq(1,out.getCount(),"Forge output count");eq(2,sharpness(l,out),"Sharpness I+I must become II");require(firstForgerInput.get(DataComponents.CUSTOM_NAME).equals(out.get(DataComponents.CUSTOM_NAME)),"Forge lost base name");require(forged(l,3).isEmpty(),"Unexpected secondary forge output");eq(CREDIT-forgerCost,xp(l,FORGER),"Exact forge XP debit");
            same(out,(ItemStack)call(be(l,FORGER),"extractAutomationItem",0,1,true),"Forge automation simulated extraction");same(out,forged(l,2),"Forge simulation mutated output");return Map.of("input_swords",2,"output_swords",1,"sharpness",2,"cost_mb",forgerCost,"remaining_mb",xp(l,FORGER));
        });
        check("CEI/printer-real-depot-callbacks-three-items-thirty-mB",()->{
            require(firstActive.containsKey("name_printer"),"Name printer never entered native animation timer");var inventory=items(l,NAME_DEPOT);eq(3,amount(inventory),"Name-printing conservation");eq(3,matching(inventory,ProcessingGameplayProbe::printedName),"All three diamonds named");eq(0,xp(l,NAME_DEPOT.above(2)),"Three naming operations consume thirty mB");return Map.of("input_diamonds",3,"named_diamonds",3,"total_cost_mb",30,"native_depot_pipeline",true);
        });
        check("CEI/printer-real-enchanted-book-template-copies-and-fluid-debit",()->{
            require(firstActive.containsKey("book_printer"),"Book printer never entered native animation timer");var inventory=items(l,BOOK_DEPOT);eq(2,amount(inventory),"Book-copy conservation");eq(2,matching(inventory,s->copiedBook(l,s)),"Both books became Sharpness II books");eq(0,xp(l,BOOK_DEPOT.above(2)),"Two copy operations exact fluid debit");same(printBookTemplate,(ItemStack)call(field(be(l,BOOK_DEPOT.above(2)),"printer"),"getFilter"),"Copy template preserved");return Map.of("blank_books",2,"enchanted_books",2,"sharpness",2,"cost_per_book_mb",bookCost,"total_cost_mb",2*bookCost);
        });
        check("CDP/real-motor-fan-dye-aircurrent-processes-seventeen-wool",()->{
            assertDyeIsolation(l);var fan=(EncasedFanBlockEntity)be(l,FAN);require(fan.getSpeed()!=0&&fan.getAirFlowDirection()==Direction.EAST,"Native fan power/direction incorrect");require(fan.airCurrent.maxDistance>=3,"Air current did not reach depot");var type=fan.airCurrent.getTypeAt(2);require(type!=null,"No fan process type after catalyst");require(Identifier.parse("create_dragons_plus:coloring_red").equals(CreateBuiltInRegistries.FAN_PROCESSING_TYPE.getKey(type)),"Wrong fan process type: "+CreateBuiltInRegistries.FAN_PROCESSING_TYPE.getKey(type));
            eq(17,amount(items(l,COLOR_DEPOT)),"Coloring conservation");eq(17,matching(items(l,COLOR_DEPOT),s->s.is(Items.WOOL.red())),"All wool red via real fan");require(firstActive.containsKey("first_red_wool")&&firstActive.get("first_red_wool")>100,"No observed powered transition");return Map.of("fan_rpm",fan.getSpeed(),"aircurrent_distance",fan.airCurrent.maxDistance,"processing_type",CreateBuiltInRegistries.FAN_PROCESSING_TYPE.getKey(type).toString(),"white_wool_input",17,"red_wool_output",17,"fluid_isolated_from_items",true,"first_red_at_server_tick",firstActive.get("first_red_wool"));
        });
    }
    private static void assertDyeIsolation(ServerLevel l){
        require(l.getFluidState(FAN.east()).isSource()&&l.getFluidState(FAN.east()).getType()==fluid("create_dragons_plus:red_dye"),"Native dye catalyst source missing");
        require(l.getBlockState(FAN.east(2)).is(Blocks.IRON_BARS),"Dye barrier changed");
        require(l.getFluidState(COLOR_DEPOT).isEmpty()&&l.getFluidState(COLOR_DEPOT.above()).isEmpty(),"Dye reached depot/items and invalidated air-current isolation");
    }
    private static void forceChunks(ServerLevel l){for(int x=29;x<=32;x++)for(int z=0;z<=1;z++)l.setChunkForced(x,z,true);}
    private static void guard(ServerTickEvent.Post e)throws Exception{
        require(Boolean.getBoolean("create263.processingGameplayProbe"),"Probe flag removed");require(root.equals(FMLPaths.GAMEDIR.get().toRealPath()),"Game directory changed");require(Files.isRegularFile(root.resolve(".create263-disposable-runtime"),LinkOption.NOFOLLOW_LINKS),"Marker removed");Path world=e.getServer().getWorldPath(LevelResource.ROOT).toRealPath();require(world.equals(root.resolve("world").toRealPath())&&world.startsWith(root),"World is not disposable root/world");
    }
    private static void write()throws Exception{
        result.put("checks",checks);result.put("recorded_at",Instant.now().toString());result.put("process_id",PROCESS_ID);result.put("jvm_started_at_epoch_ms",JVM_STARTED_AT);result.put("server_ticks",ticks);result.put("world_reloaded",reloaded);result.put("first_active_ticks",firstActive);result.put("actual_check_count",checks.size());
        boolean failure=result.containsKey("fatal_error")||checks.stream().anyMatch(c->Boolean.FALSE.equals(c.get("passed")));result.put("status",finished?failure?"failed":"passed":failure?"failed":"running");result.put("passed",finished&&!failure&&checks.size()>=(reloaded?2:10));
        result.put("remaining",List.of("CEI super-XP/lightning and other forge modes","Physical belt transport and multi-machine XP piping","Other CDP fan types and living entity coloring","FluidLogistics package dispatch full network"));Files.writeString(root.resolve("processing-gameplay-probe-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
    }
    private static void tick(ServerTickEvent.Post event){
        if(finished)return;
        try{
            ticks++;guard(event);ServerLevel l=event.getServer().overworld();
            if(ticks==40){
                forceChunks(l);reloaded=Boolean.getBoolean("create263.gameplayReload");
                if(reloaded){
                    require(Files.isRegularFile(root.resolve("processing-gameplay-fixture.json")),"Reload phase has no fixture manifest");fixture=JsonParser.parseString(Files.readString(root.resolve("processing-gameplay-fixture.json"))).getAsJsonObject();require(fixture.get("world").getAsString().equals(root.resolve("world").toRealPath().toString()),"Fixture belongs to different world");result.put("fixture_id",fixture.get("fixture_id").getAsString());settled=fixture.getAsJsonObject("completed_snapshot");require(settled!=null,"Fixture has no completed native snapshot");require(fixture.get("create_process_id").getAsLong()!=PROCESS_ID||fixture.get("create_jvm_started_at_epoch_ms").getAsLong()!=JVM_STARTED_AT,"Reload must run in an independent JVM");
                    check("processing/independent-JVM-disk-reload-original-items-and-XP",()->{require(settled.equals(snapshot(l)),"Saved native state changed: expected="+settled+", actual="+snapshot(l));assertDyeIsolation(l);return Map.of("rebuilt_fixtures",false,"native_snapshot_match",true,"create_process_id",fixture.get("create_process_id").getAsLong(),"reload_process_id",PROCESS_ID,"independent_jvm",true);});result.put("phase","reload-observing-no-duplicate-output");
                }else setup(l);
                write();
            }
            if(reloaded&&ticks==160){check("processing/reloaded-world-no-second-output-or-resource-debit",()->{require(settled.equals(snapshot(l)),"120 further native ticks changed settled output/resources");return Map.of("native_ticks_after_reload",120,"same_snapshot",true);});result.put("phase","reload-complete");finished=true;write();return;}
            if(reloaded)return;
            if(ticks==100){negativeControls(l);write();}
            if(ticks>100&&ticks<=600)observe(l);
            if(ticks==600){positiveChecks(l);fixture.addProperty("enchanter_cost_mb",enchanterCost);settled=snapshot(l);settleTick=ticks;result.put("phase","observing-no-duplicate-output");write();}
            if(ticks==680){
                check("processing/eighty-more-native-ticks-no-duplicates-or-second-debit",()->{require(settled.equals(snapshot(l)),"Completed machines repeated output/debit during idle ticks");return Map.of("extra_native_ticks",ticks-settleTick,"same_snapshot",true);});fixture.add("completed_snapshot",settled);fixture.addProperty("completed_at",Instant.now().toString());Files.writeString(root.resolve("processing-gameplay-fixture.json"),new GsonBuilder().setPrettyPrinting().create().toJson(fixture)+"\n");
                for(BlockPos p:List.of(ENCHANTER,FORGER,NAME_DEPOT,NAME_DEPOT.above(2),BOOK_DEPOT,BOOK_DEPOT.above(2),FAN,FAN.west(),COLOR_DEPOT))be(l,p).setChanged();result.put("phase","create-complete");finished=true;write();
            }
        }catch(Throwable e){e.printStackTrace();result.put("fatal_error",e.toString());finished=true;try{write();}catch(Exception secondary){secondary.printStackTrace();}}
    }
}
