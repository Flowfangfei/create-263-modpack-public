package dev.create263.probe;

import java.util.*;
import net.createmod.ponder.api.client.scene.PonderScene;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.fluids.FluidStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.createmod.catnip.api.math.BlockFace;

/** Assertions on owned Ponder levels, including content that can disappear without throwing. */
final class PonderContentChecks {
    private static final List<Map<String,Object>> checks=new ArrayList<>();
    private static final Set<String> scripted=new HashSet<>();
    private static boolean templateHelperChecked;
    static List<Map<String,Object>> report(){return checks;}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private static Object call(Object receiver,String name,Class<?>[] types,Object...args)throws Exception{
        var method=receiver.getClass().getMethod(name,types);method.setAccessible(true);return method.invoke(receiver,args);
    }
    private static Object call(Object receiver,String name)throws Exception{return call(receiver,name,new Class<?>[0]);}
    private static FluidStack fluid(PonderScene scene,BlockPos pos){
        var be=scene.getWorld().getBlockEntity(pos);require(be!=null,"Missing fluid fixture at "+pos);
        var behaviour=BlockEntityBehaviour.get(be,SmartFluidTankBehaviour.TYPE);require(behaviour!=null,"Missing fluid behaviour at "+pos);
        return behaviour.getPrimaryHandler().getFluid();
    }
    private static void record(String check,Object detail){checks.add(Map.of("check",check,"detail",detail));}
    static void initial(PonderScene scene,String template)throws Exception{
        if(!templateHelperChecked){legacyTemplateHelperChecks(scene);templateHelperChecked=true;}
        checkPackagerFields(scene,template);
        expectedTemplateFluids(scene,template);
        if(template.equals("aeronautics:hot_air/steam_vent")){
            var pos=new BlockPos(6,2,5);var be=scene.getWorld().getBlockEntity(pos);require(be!=null,"Missing steam vent pump fixture");
            require(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()).toString().equals("create:mechanical_pump"),"Steam vent fixture is not the mechanical pump");
            var saved=be.saveWithFullMetadata(scene.getWorld().registryAccess());
            var east=compound(saved,"east","steam vent pump");var west=compound(saved,"west","steam vent pump");
            requireFluid(compound(compound(east,"Flow","east"),"Fluid","east.Flow"),1,"east.Flow.Fluid");
            requireFluid(compound(compound(west,"Flow","west"),"Fluid","west.Flow"),1,"west.Flow.Fluid");
            var openEnd=compound(east,"OpenEnd","east");requireFluid(compound(openEnd,"Tank","east.OpenEnd"),70632,"east.OpenEnd.Tank");
            var location=BlockFace.CODEC.parse(NbtOps.INSTANCE,openEnd.get("Location")).result().orElseThrow(()->new IllegalStateException("Steam vent OpenEnd location is unreadable"));
            require(location.getPos().equals(pos)&&location.getFace()==Direction.EAST,"Steam vent OpenEnd location changed: "+location.getPos()+"/"+location.getFace());
            require(!openEnd.contains("Fluid")&&!openEnd.contains("Direction"),"Steam vent OpenEnd still uses legacy keys after read/save");
            record("steam-vent-pump-preserved",Map.of("position",pos.toShortString(),"direction",location.getFace().getName(),"tank_amount",70632,"east_flow_amount",1,"west_flow_amount",1,"capacity_component_absent",true));
        }
        if(template.equals("create:depot")){
            FluidStack value=fluid(scene,new BlockPos(2,3,2));require(value.getAmount()==1000&&value.getFluid()==net.minecraft.world.level.material.Fluids.WATER,"Depot spout lost its1000mB water");record("depot-initial-fluid",value.toString());
        }
        if(template.equals("create:smart_pipe")){
            var be=scene.getWorld().getBlockEntity(new BlockPos(4,1,1));require(be!=null,"Missing basin fixture");var input=BlockEntityBehaviour.get(be,SmartFluidTankBehaviour.INPUT);require(input!=null,"Missing basin input tanks");
            var tanks=input.getTanks();require(tanks.length>=2&&tanks[0].getTank().getFluidAmount()==1000&&tanks[1].getTank().getFluidAmount()==1000,"Basin lost chocolate/milk1000mB each: "+java.util.Arrays.toString(java.util.Arrays.stream(tanks).map(t->t.getTank().getFluid().toString()).toArray()));require(BuiltInRegistries.FLUID.getKey(tanks[0].getTank().getFluid().getFluid()).toString().equals("create:chocolate")&&BuiltInRegistries.FLUID.getKey(tanks[1].getTank().getFluid().getFluid()).toString().equals("minecraft:milk"),"Basin restored wrong chocolate/milk fluid identities");record("basin-initial-fluids",List.of(tanks[0].getTank().getFluid().toString(),tanks[1].getTank().getFluid().toString()));
        }
        if(template.equals("create:high_logistics/package_postbox")){
            var be=scene.getWorld().getBlockEntity(new BlockPos(1,2,4));require(be instanceof CopycatBlockEntity,"Missing Copycat fixture");var material=((CopycatBlockEntity)be).getMaterial();require(BuiltInRegistries.BLOCK.getKey(material.getBlock()).toString().equals("create:railway_casing"),"Copycat template material lost");record("copycat-material",material.toString());
        }
        if(template.equals("fxntstorage:storageinterface/intro")){
            var be=scene.getWorld().getBlockEntity(new BlockPos(4,2,2));require(be!=null,"Missing Storage diamond fixture");Object handler=call(be,"getItemHandler");ItemStack stored=(ItemStack)call(handler,"getStackInSlot",new Class<?>[]{int.class},0);ItemStack filter=(ItemStack)call(be,"getFilterItem");
            require(stored.getCount()==64&&stored.is(net.minecraft.world.item.Items.DIAMOND)&&filter.getCount()==1&&filter.is(net.minecraft.world.item.Items.DIAMOND),"Ponder template lost64 diamonds/filter");record("storage-diamond-and-filter",64);
        }
        if(template.contains("experience_lantern")){
            var world=scene.getWorld();BlockPos pos=new BlockPos(2,2,0);var be=world.getBlockEntity(pos);require(be!=null,"Lantern failed during restore");
            Object handler=call(call(be,"getTank"),"getPrimaryHandler");FluidStack original=((FluidStack)call(handler,"getFluid")).copy();int capacity=((Number)call(handler,"getCapacity")).intValue();require(capacity>0,"Invalid lantern capacity");
            var sample=original.isEmpty()?BuiltInRegistries.FLUID.getValue(net.minecraft.resources.Identifier.parse("create_enchantment_industry:experience")):original.getFluid();require(sample!=null&&sample!=net.minecraft.world.level.material.Fluids.EMPTY,"Experience fluid registry entry missing");
            ((SmartBlockEntity)be).initialize();var property=(IntegerProperty)world.getBlockState(pos).getProperties().stream().filter(v->v.getName().equals("light")).findFirst().orElseThrow();
            for(int amount:new int[]{capacity,capacity/2,0}){
                FluidStack value=amount==0?FluidStack.EMPTY:new FluidStack(sample,amount);call(handler,"setFluid",new Class<?>[]{FluidStack.class},value);
                require(world.getBlockState(pos).getValue(property)==(int)((float)amount/capacity*15),"Lantern brightness mismatch at "+amount);
                require(((FluidStack)call(handler,"getFluid")).getAmount()==amount,"Lantern callback lost fluid");
            }
            call(handler,"setFluid",new Class<?>[]{FluidStack.class},original);record("lantern-restore-full-half-empty",capacity);
        }
    }
    private static com.google.gson.JsonArray expectedFluids;
    private static void expectedTemplateFluids(PonderScene scene,String template)throws Exception{
        if(expectedFluids==null){
            try(var stream=PonderContentChecks.class.getResourceAsStream("/ponder-template-fluid-expectations.json")){
                require(stream!=null,"Missing independent original-template fluid fixtures");
                expectedFluids=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("fixtures");
            }
        }
        int checked=0;var ops=scene.getWorld().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        for(var element:expectedFluids){
            var expected=element.getAsJsonObject();if(!expected.get("template").getAsString().equals(template))continue;
            var xyz=expected.getAsJsonArray("position");var pos=new BlockPos(xyz.get(0).getAsInt(),xyz.get(1).getAsInt(),xyz.get(2).getAsInt());
            var be=scene.getWorld().getBlockEntity(pos);require(be!=null,"Missing independently inventoried fluid fixture "+template+"/"+pos);
            require(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()).toString().equals(expected.get("be_id").getAsString()),"Fluid fixture BE changed at "+template+"/"+pos);
            Tag value=be.saveWithFullMetadata(scene.getWorld().registryAccess());
            for(var key:expected.getAsJsonArray(expected.has("saved_field_path")?"saved_field_path":"field_path")){
                if(key.getAsJsonPrimitive().isNumber())value=((ListTag)value).get(key.getAsInt());else value=((CompoundTag)value).get(key.getAsString());
                require(value!=null,"Missing fluid path "+template+"/"+pos+"/"+expected.get("field_path"));
            }
            var decoded=FluidStack.OPTIONAL_CODEC.parse(ops,value).result().orElseThrow(()->new IllegalStateException("Unreadable restored fixture "+template+"/"+pos));
            require(decoded.getAmount()==expected.get("amount").getAsInt()&&BuiltInRegistries.FLUID.getKey(decoded.getFluid()).toString().equals(expected.get("fluid").getAsString()),"Restored fluid identity/quantity differs from original "+template+"/"+pos+": "+value);
            if(expected.has("expected_components")){
                require(value instanceof CompoundTag&&((CompoundTag)value).contains("components"),"Lost potion components at "+template+"/"+pos);
                var actual=net.minecraft.nbt.NbtOps.INSTANCE.convertTo(com.mojang.serialization.JsonOps.INSTANCE,((CompoundTag)value).get("components"));
                require(actual.equals(expected.get("expected_components")),"Potion component payload changed at "+template+"/"+pos+": "+actual);
            }
            if(expected.has("potion")){
                var contents=decoded.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
                require(contents!=null&&contents.potion().isPresent()&&contents.potion().get().unwrapKey().orElseThrow().identifier().toString().equals(expected.get("potion").getAsString()),"Potion contents changed at "+template+"/"+pos);
            }
            if(expected.has("bottle"))require(decoded.getOrDefault(com.simibubi.create.AllDataComponents.POTION_FLUID_BOTTLE_TYPE,com.simibubi.create.content.fluids.potion.PotionFluid.BottleType.REGULAR).name().equals(expected.get("bottle").getAsString()),"Potion bottle changed at "+template+"/"+pos);
            checked++;
        }
        if(checked>0)record("independent-template-fluid-fixtures/"+template,checked);
    }
    private static CompoundTag compound(CompoundTag parent,String key,String path){
        require(parent.get(key) instanceof CompoundTag,"Missing compound "+path+"."+key);return (CompoundTag)parent.get(key);
    }
    private static void requireFluid(CompoundTag value,int amount,String path){
        require(value.getStringOr("id","").equals("minecraft:water")&&value.getIntOr("amount",-1)==amount,"Fluid fixture changed at "+path+": "+value);
        require(!value.getCompoundOrEmpty("components").contains("create:fluid_max_capacity"),"Obsolete capacity survived at "+path);
    }
    private static void checkPackagerFields(PonderScene scene,String template){
        var ops=scene.getWorld().registryAccess().createSerializationContext(NbtOps.INSTANCE);int count=0,empty=0;
        for(var be:scene.getWorld().getBlockEntities())if(be instanceof PackagerBlockEntity packager){
            var saved=be.saveWithFullMetadata(scene.getWorld().registryAccess());
            for(var field:Map.of("HeldBox",packager.heldBox,"InsertedBox",packager.previouslyUnwrapped).entrySet()){
                Tag value=saved.get(field.getKey());require(value!=null,"Missing saved packager field "+field.getKey());
                var decoded=ItemStack.OPTIONAL_CODEC.parse(ops,value).result().orElseThrow(()->new IllegalStateException("Unreadable saved packager field "+field.getKey()+" at "+be.getBlockPos()));
                require(ItemStack.matches(decoded,field.getValue()),"Packager read/save changed "+field.getKey()+" at "+be.getBlockPos());
                if(field.getValue().isEmpty()){require(value instanceof CompoundTag tag&&tag.isEmpty(),"Empty packager field is not canonical {}: "+value);empty++;}count++;
            }
        }
        if(template.equals("create:high_logistics/table_cloth"))require(empty>=2,"Table cloth template did not restore empty HeldBox/InsertedBox");
        if(count>0)record("packager-fields-readable/"+template,Map.of("fields",count,"empty_canonical_fields",empty));
    }
    private static CompoundTag adaptFixture(CompoundTag input)throws Exception{
        return (CompoundTag)Class.forName("dev.simulated_team.simulated.ponder.LegacyPonderTemplateData").getMethod("adapt",CompoundTag.class).invoke(null,input);
    }
    private static CompoundTag templateFixture(CompoundTag blockEntity){
        var block=new CompoundTag();block.put("pos",BlockPos.CODEC.encodeStart(NbtOps.INSTANCE,new BlockPos(6,2,5)).result().orElseThrow());block.put("nbt",blockEntity);block.putInt("state",0);
        var blocks=new ListTag();blocks.add(block);var root=new CompoundTag();root.put("blocks",blocks);root.putString("unrelated","preserve this structure metadata");return root;
    }
    private static void unchangedFixture(String label,CompoundTag input)throws Exception{
        var before=input.copy();require(adaptFixture(input)==input&&input.equals(before),"Ponder helper changed "+label);
    }
    private static CompoundTag pipeFixture(CompoundTag connection){
        var be=new CompoundTag();be.putString("id","create:mechanical_pump");be.put("east",connection);return be;
    }
    private static void legacyTemplateHelperChecks(PonderScene scene)throws Exception{
        var ops=scene.getWorld().registryAccess().createSerializationContext(NbtOps.INSTANCE);var pos=new BlockPos(6,2,5);
        var currentFluid=(CompoundTag)FluidStack.OPTIONAL_CODEC.encodeStart(ops,new FluidStack(net.minecraft.world.level.material.Fluids.WATER,23)).result().orElseThrow();
        var location=BlockFace.CODEC.encodeStart(NbtOps.INSTANCE,new BlockFace(pos,Direction.EAST)).result().orElseThrow();
        var openEnd=new CompoundTag();openEnd.put("Tank",currentFluid.copy());openEnd.put("Location",location.copy());openEnd.putBoolean("Pulling",true);
        var flow=new CompoundTag();flow.put("Fluid",currentFluid.copy());var connection=new CompoundTag();connection.put("OpenEnd",openEnd);connection.put("Flow",flow);
        unchangedFixture("valid current pipe format",templateFixture(pipeFixture(connection)));
        var packageBe=new CompoundTag();packageBe.putString("id","create:packager");packageBe.put("HeldBox",ItemStack.OPTIONAL_CODEC.encodeStart(ops,new ItemStack(net.minecraft.world.item.Items.DIAMOND,2)).result().orElseThrow());packageBe.put("InsertedBox",new CompoundTag());
        unchangedFixture("valid current nonempty item and empty package",templateFixture(packageBe));

        var oldFluid=new CompoundTag();oldFluid.putString("id","minecraft:water");oldFluid.putInt("amount",70632);
        var components=new CompoundTag();components.putInt("create:fluid_max_capacity",81000);var unknown=new CompoundTag();unknown.putByte("typed_marker",(byte)7);unknown.putIntArray("nested_values",new int[]{4,2,9});components.put("create263:unknown_fixture_component",unknown.copy());oldFluid.put("components",components);
        require(!BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(Identifier.parse("create:fluid_max_capacity")),"Legacy capacity unexpectedly has a current registered type");
        unchangedFixture("bare non-template fluid stack",oldFluid.copy());
        var oldFlow=new CompoundTag();oldFlow.put("Fluid",oldFluid.copy());var oldConnection=new CompoundTag();oldConnection.put("Flow",oldFlow);
        unchangedFixture("bare non-template block entity",pipeFixture(oldConnection.copy()));
        var fixture=templateFixture(pipeFixture(oldConnection));adaptFixture(fixture);
        var adapted=compound(oldFlow,"Fluid","unknown component fixture");
        require(adapted.get("id").equals(oldFluid.get("id"))&&adapted.get("amount").equals(oldFluid.get("amount")),"Capacity removal changed fluid id/amount/type");
        require(!adapted.getCompoundOrEmpty("components").contains("create:fluid_max_capacity")&&unknown.equals(adapted.getCompoundOrEmpty("components").get("create263:unknown_fixture_component")),"Capacity removal discarded unknown component data");
        unchangedFixture("already adapted template",fixture);
        // The helper is reached only by the Ponder resource entry; ordinary codecs still reject these old payloads.
        require(FluidStack.OPTIONAL_CODEC.parse(ops,oldFluid).result().isEmpty(),"A general fluid codec unexpectedly accepted unsupported legacy components");
        var legacyAir=new CompoundTag();legacyAir.putString("id","minecraft:air");legacyAir.putByte("Count",(byte)0);
        require(ItemStack.OPTIONAL_CODEC.parse(ops,legacyAir).result().isEmpty(),"A general item codec unexpectedly accepted a legacy AIR placeholder");
        for(int oldCount:new int[]{0,1}){
            var air=legacyAir.copy();air.putByte("Count",(byte)oldCount);var packages=new CompoundTag();packages.putString("id","create:repackager");packages.put("HeldBox",air.copy());packages.put("InsertedBox",air.copy());adaptFixture(templateFixture(packages));
            require(compound(packages,"HeldBox","air fixture").isEmpty()&&compound(packages,"InsertedBox","air fixture").isEmpty(),"Legacy AIR Count "+oldCount+" did not become optional empty stacks");
        }
        // These three intentionally rejected synthetic inputs each emit one expected compatibility WARN.
        var fluidConflict=new CompoundTag();fluidConflict.put("Fluid",oldFluid.copy());fluidConflict.put("Tank",currentFluid.copy());var conflictConnection=new CompoundTag();conflictConnection.put("OpenEnd",fluidConflict);
        unchangedFixture("conflicting old/new OpenEnd fluid fields",templateFixture(pipeFixture(conflictConnection)));
        var directionConflict=new CompoundTag();directionConflict.put("Tank",currentFluid.copy());directionConflict.put("Location",location.copy());directionConflict.putString("Direction","west");var directionConnection=new CompoundTag();directionConnection.put("OpenEnd",directionConflict);
        unchangedFixture("existing Location plus conflicting legacy Direction",templateFixture(pipeFixture(directionConnection)));
        var countConflict=legacyAir.copy();countConflict.putInt("count",1);var conflictPackage=new CompoundTag();conflictPackage.putString("id","create:packager");conflictPackage.put("HeldBox",countConflict);
        unchangedFixture("conflicting old/new package counts",templateFixture(conflictPackage));
        record("legacy-template-helper-boundaries",Map.of("current_format_unchanged",true,"nonempty_items_unchanged",true,"unknown_component_and_numeric_type_preserved",true,"non_template_inputs_unchanged",true,"general_codecs_unchanged",true,"conflicts_unchanged",true,"idempotent",true,"expected_negative_fixture_warnings",3));
    }
    static void scriptedFluids(PonderScene scene,String component)throws Exception{
        boolean hopper=component.equals("fluidlogistics:smart_hopper"),transporter=component.equals("fluidlogistics:fluid_transporter");
        if((!hopper&&!transporter)||!scripted.add(component))return;
        var positions=hopper?List.of(new BlockPos(4,1,3),new BlockPos(3,1,3)):List.of(new BlockPos(3,1,3));var traces=new LinkedHashMap<String,List<String>>();for(var pos:positions)traces.put(pos.toShortString(),new ArrayList<>());
        for(int step=0;step<=scene.getTotalTime()&&!scene.isFinished();step++){
            scene.tick();for(var pos:positions){FluidStack value=fluid(scene,pos);String state=value.isEmpty()?"empty":BuiltInRegistries.FLUID.getKey(value.getFluid())+"/"+value.getAmount();var list=traces.get(pos.toShortString());if(list.isEmpty()||!list.getLast().equals(state))list.add(state);}
        }
        for(var values:traces.values()){
            require(values.contains(hopper?"minecraft:water/500":"minecraft:water/1000"),"Ponder fluid fill not observed: "+component+" "+traces);
            if(transporter)require(values.contains("minecraft:lava/1000"),"Ponder lava fill not observed");
            require(values.getLast().equals("empty"),"Ponder fluid clear not observed: "+traces);
        }
        record("scripted-fluid-fill-clear/"+component,traces);
    }
}
