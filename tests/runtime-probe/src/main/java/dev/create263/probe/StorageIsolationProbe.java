package dev.create263.probe;

import com.google.gson.*;
import com.mojang.serialization.*;
import io.netty.buffer.Unpooled;
import java.lang.reflect.*;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Opt-in checks confined to the independently guarded, project-owned disposable world. */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class StorageIsolationProbe {
    private static final String BASE="net.fxnt.fxntstorage.";
    private static final String CONTENTS=BASE+"inventory.StorageContents";
    private static final String SERIAL=BASE+"util.StorageSerialization";
    private static final String TOKEN="create263-storage-isolation-v1";
    private static final int MAX=84_934_656;
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private StorageIsolationProbe() {}

    @FunctionalInterface private interface Checked { Map<String,Object> run() throws Exception; }
    private static void check(List<Map<String,Object>> checks,String name,Checked action) {
        Map<String,Object> result=new LinkedHashMap<>();result.put("name",name);
        try {result.put("measured",action.run());result.put("passed",true);}
        catch(Throwable failure) {
            Throwable cause=unwrap(failure);result.put("passed",false);result.put("error",cause.toString());
            cause.printStackTrace();
        }
        checks.add(result);
    }
    public static Map<String,Object> run(ServerLevel level,Path requestedRoot,boolean reloaded) throws Exception {
        Path root=guard(level,requestedRoot);
        List<Map<String,Object>> checks=new ArrayList<>();List<String> remaining=new ArrayList<>();
        check(checks,"vanilla-codec-and-template-limits",()->vanilla(level));
        check(checks,"private-codec-network-components-and-nested-inventories",()->codecs(level));
        check(checks,"private-malformed-input-rejection-preserves-live-handler",()->invalidInputs(level));
        check(checks,"hardened-backpack-itemaccess-capacity-and-transactions",()->transactions());
        check(checks,"old-gui-save-preserves-native-committed-insert",()->guiNative(false));
        check(checks,"old-gui-save-does-not-revive-native-extracted-items",()->guiNative(true));
        check(checks,"cached-backpack-tier-and-invalid-slot-load-isolation",()->backpackBoundaries());
        check(checks,"ordinary-chests-native-save-and-reload",()->chests(level,root,reloaded));
        if(!reloaded)remaining.add("Restart this exact disposable world and rerun with reloaded=true to verify all 81 ordinary-chest slots from disk.");
        remaining.add("Storage GUI interactions, backpack equipment/upgrades, mounted contraptions, filters, controller networks, multiplayer and long-duration gameplay remain outside this isolation probe.");
        boolean passed=checks.stream().allMatch(result->Boolean.TRUE.equals(result.get("passed")));
        Map<String,Object> result=new LinkedHashMap<>();result.put("schema",1);result.put("recorded_at",Instant.now().toString());
        result.put("world",root.resolve("world").toRealPath().toString());result.put("reloaded",reloaded);
        result.put("checks",checks);result.put("passed",passed);result.put("remaining",remaining);
        result.put("all_requested_features_verified",false);
        guard(level,root);Path output=safeFile(root,"storage-isolation-result.json");
        Files.writeString(output,GSON.toJson(result)+"\n",StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
        if(!passed)throw new IllegalStateException("Storage isolation probe failed: "+GSON.toJson(checks.stream().filter(r->Boolean.FALSE.equals(r.get("passed"))).toList()));
        return result;
    }

    private static Path guard(ServerLevel level,Path requested) throws Exception {
        require(Boolean.getBoolean("create263.fiveAddonsProbe"),"Missing explicit create263.fiveAddonsProbe flag");
        require(requested!=null,"Missing probe root");Path root=requested.toRealPath();
        require(root.equals(FMLPaths.GAMEDIR.get().toRealPath()),"probe root differs from FML gameDir");
        Path marker=root.resolve(".create263-disposable-runtime");
        require(Files.isRegularFile(marker,LinkOption.NOFOLLOW_LINKS)&&marker.toRealPath().startsWith(root),"Missing real disposable-runtime marker");
        Path workspace=null;
        for(Path p=root.getParent();p!=null;p=p.getParent())
            if(Files.isRegularFile(p.resolve("AGENTS.md"))&&Files.isDirectory(p.resolve("modules"))&&Files.isDirectory(p.resolve("tests/runtime-probe"))){workspace=p.toRealPath();break;}
        require(workspace!=null,"Source workspace cannot be identified");Path runtime=workspace.resolve("runtime").toRealPath();
        require(root.startsWith(runtime)&&!root.equals(runtime),"Probe root is not a child of this source workspace/runtime");
        Path world=root.resolve("world").toRealPath();
        require(world.startsWith(root)&&level.getServer().getWorldPath(LevelResource.ROOT).toRealPath().equals(world),"World path is not disposable root/world");
        require(level==level.getServer().overworld(),"Probe requires the disposable overworld");return root;
    }
    private static Path safeFile(Path root,String name) throws Exception {
        Path path=root.resolve(name).normalize();require(path.getParent().equals(root),"Probe output escaped root");
        if(Files.exists(path,LinkOption.NOFOLLOW_LINKS))require(Files.isRegularFile(path,LinkOption.NOFOLLOW_LINKS)&&path.toRealPath().startsWith(root),"Probe file is a link or escapes root: "+name);
        return path;
    }
    private static Map<String,Object> vanilla(ServerLevel level) {
        var ops=level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        JsonObject input=new JsonObject();input.addProperty("id","minecraft:diamond");input.addProperty("count",99);
        eq(99,ItemStack.CODEC.parse(ops,input).getOrThrow().getCount(),"Native ItemStack.CODEC upper legal count");
        input.addProperty("count",100);String decode=error(ItemStack.CODEC.parse(ops,input),"Native ItemStack.CODEC accepted count 100");
        String encode=error(ItemStack.CODEC.encodeStart(ops,new ItemStack(Items.DIAMOND,100)),"Native ItemStack.CODEC encoded count 100");
        input.addProperty("count",0);error(ItemStack.CODEC.parse(ops,input),"Native ItemStack.CODEC accepted count zero");
        ItemStack legal=new ItemStackTemplate(Items.DIAMOND,64).create();
        eq(64,legal.getCount(),"Native template legal count");eq(64,legal.getMaxStackSize(),"DIAMOND max stack size");
        require(new ItemStackTemplate(Items.DIAMOND,65).create().isEmpty(),"Native ItemStackTemplate.create accepted DIAMOND 65");
        return Map.of("native_codec_count_99_accepted",true,"count_100_decode_error",decode,"count_100_encode_error",encode,"template_64_accepted",true,"template_65_empty",true,"diamond_max_stack",64);
    }
    private static ItemStack named(Item item,int count,String suffix) {
        ItemStack stack=new ItemStack(item,count);stack.set(DataComponents.CUSTOM_NAME,Component.literal(TOKEN+"-"+suffix));return stack;
    }
    private static ItemStack enchanted(ServerLevel level) {
        ItemStack stack=named(Items.DIAMOND_SWORD,1,"enchanted");
        stack.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING),3);
        return stack;
    }
    private static ItemStack nativeContainer(ServerLevel level,String suffix) {
        ItemStack stack=named(Items.SHULKER_BOX,1,"native-container-"+suffix);
        stack.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(ItemStack.EMPTY,new ItemStack(Items.DIAMOND,64),named(Items.PAPER,7,suffix),enchanted(level))));
        return stack;
    }
    private static ItemStack backpack() {return item("fxntstorage:hardened_backpack");}
    private static Object contents(List<ItemStack> stacks) throws Exception {return call(type(CONTENTS),"fromItems",stacks);}
    private static Codec<Object> codec() throws Exception {return (Codec<Object>)constant(CONTENTS,"CODEC");}
    private static DataComponentType<Object> storageType() throws Exception {return (DataComponentType<Object>)constant(BASE+"init.ModDataComponents","STORAGE_CONTENTS");}
    private static List<ItemStack> fixtures(ServerLevel level) throws Exception {
        ItemStack removed=named(Items.DIAMOND,17,"removed-item-name");removed.remove(DataComponents.ITEM_NAME);
        require(!removed.has(DataComponents.ITEM_NAME),"Fixture did not remove its default component");
        ItemStack nested=backpack();nested.set(storageType(),contents(List.of(ItemStack.EMPTY,named(Items.DIAMOND,4096,"nested-mod"),ItemStack.EMPTY)));
        require(!nested.has(DataComponents.CONTAINER),"Nested Storage fixture unexpectedly has vanilla CONTAINER");
        return List.of(ItemStack.EMPTY,named(Items.DIAMOND,1,"one"),named(Items.DIAMOND,1000,"large"),nativeContainer(level,"codec"),enchanted(level).copyWithCount(2048),removed,nested,ItemStack.EMPTY,new ItemStack(Items.DIAMOND,MAX),ItemStack.EMPTY);
    }
    private static void sameContents(List<ItemStack> expected,Object actual,String stage) throws Exception {
        eq(expected.size(),number(call(actual,"getSlots")),stage+" slot count");
        for(int slot=0;slot<expected.size();slot++)same(expected.get(slot),(ItemStack)call(actual,"getStackInSlot",slot),stage+" slot "+slot);
    }
    private static Object network(StreamCodec<RegistryFriendlyByteBuf,Object> codec,Object input,ServerLevel level) {
        RegistryFriendlyByteBuf buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),level.registryAccess());
        try {codec.encode(buffer,input);Object output=codec.decode(buffer);eq(0,buffer.readableBytes(),"Unread network payload bytes");return output;}
        finally {buffer.release();}
    }
    private static Map<String,Object> codecs(ServerLevel level) throws Exception {
        List<ItemStack> expected=fixtures(level);Object value=contents(expected);Codec<Object> codec=codec();
        var ops=level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        JsonElement json=codec.encodeStart(ops,value).getOrThrow();Object decoded=codec.parse(ops,json).getOrThrow();sameContents(expected,decoded,"registry JSON roundtrip");
        var nbtOps=level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        Tag tag=codec.encodeStart(nbtOps,value).getOrThrow();sameContents(expected,codec.parse(nbtOps,tag).getOrThrow(),"registry NBT roundtrip");
        var stream=(StreamCodec<RegistryFriendlyByteBuf,Object>)constant(CONTENTS,"STREAM_CODEC");sameContents(expected,network(stream,value,level),"private network roundtrip");
        ItemStack outer=backpack();outer.set(storageType(),value);require(!outer.has(DataComponents.CONTAINER),"Private inventory touched outer vanilla CONTAINER");
        JsonElement outerJson=ItemStack.CODEC.encodeStart(ops,outer).getOrThrow();ItemStack restored=ItemStack.CODEC.parse(ops,outerJson).getOrThrow();same(outer,restored,"Native outer backpack codec");
        ItemStack received=(ItemStack)network((StreamCodec)ItemStack.STREAM_CODEC,outer,level);same(outer,received,"Native outer backpack network");
        for(ItemStack tested:List.of(outer,restored,received)) {
            require(!tested.has(DataComponents.CONTAINER),"Outer mod backpack gained native CONTAINER");sameContents(expected,tested.get(storageType()),"Outer storage component");
            ItemStack inner=(ItemStack)call(tested.get(storageType()),"getStackInSlot",6);require(!inner.has(DataComponents.CONTAINER),"Nested mod backpack gained native CONTAINER");
            Object nested=inner.get(storageType());eq(4096,((ItemStack)call(nested,"getStackInSlot",1)).getCount(),"Nested mod quantity");
            ItemStack shulker=(ItemStack)call(tested.get(storageType()),"getStackInSlot",3);same(expected.get(3),shulker,"Nested native CONTAINER and components");
            ItemStack sword=(ItemStack)call(tested.get(storageType()),"getStackInSlot",4);require(!sword.getEnchantments().isEmpty(),"Enchantment removed in private roundtrip");
            require(!((ItemStack)call(tested.get(storageType()),"getStackInSlot",5)).has(DataComponents.ITEM_NAME),"Deleted component restored in private roundtrip");
        }
        Object handler=handler(expected);CompoundTag stored=(CompoundTag)call(type(SERIAL),"write",handler,level.registryAccess());
        Object target=handler(List.of(new ItemStack(Items.DIRT,3)));call(type(SERIAL),"read",target,level.registryAccess(),stored);sameHandler(expected,target,"StorageSerialization ItemStackHandler roundtrip");
        return Map.of("slots",expected.size(),"maximum_quantity",MAX,"empty_slots",List.of(0,7,9),"registry_json",true,"registry_nbt",true,"private_network",true,"native_outer_codec_and_network",true,"nested_native_container",true,"nested_mod_container_untouched",true,"components_and_removals",true);
    }
    private static Object handler(List<ItemStack> stacks) throws Exception {
        Object handler=make("com.simibubi.create.compat.neoforge263.items.ItemStackHandler",stacks.size());
        for(int i=0;i<stacks.size();i++)call(handler,"setStackInSlot",i,stacks.get(i).copy());return handler;
    }
    private static void sameHandler(List<ItemStack> expected,Object handler,String stage) throws Exception {
        eq(expected.size(),number(call(handler,"getSlots")),stage+" slots");for(int i=0;i<expected.size();i++)same(expected.get(i),(ItemStack)call(handler,"getStackInSlot",i),stage+" slot "+i);
    }
    private static Map<String,Object> invalidInputs(ServerLevel level) throws Exception {
        var ops=level.registryAccess().createSerializationContext(JsonOps.INSTANCE);Codec<Object> codec=codec();
        JsonObject valid=codec.encodeStart(ops,contents(List.of(new ItemStack(Items.DIAMOND,64),ItemStack.EMPTY))).getOrThrow().getAsJsonObject();
        List<JsonObject> invalid=new ArrayList<>();List<String> labels=new ArrayList<>();
        for(int quantity:new int[]{-1,0,MAX+1,Integer.MAX_VALUE}) {
            JsonObject copy=valid.deepCopy();entry(copy).addProperty("amount",quantity);invalid.add(copy);labels.add("amount="+quantity);
        }
        JsonObject prototype=valid.deepCopy();entry(prototype).getAsJsonObject("item").addProperty("count",2);invalid.add(prototype);labels.add("prototype-count=2");
        JsonObject count100=valid.deepCopy();entry(count100).getAsJsonObject("item").addProperty("count",100);invalid.add(count100);labels.add("prototype-count=100");
        JsonObject mismatch=valid.deepCopy();mismatch.addProperty("Size",3);invalid.add(mismatch);labels.add("size-entry-mismatch");
        JsonObject malformed=valid.deepCopy();JsonObject badComponents=new JsonObject();badComponents.addProperty("minecraft:max_stack_size",0);entry(malformed).getAsJsonObject("item").add("components",badComponents);invalid.add(malformed);labels.add("invalid-component-max-stack-zero");
        JsonObject contradiction=valid.deepCopy();JsonObject damageable=new JsonObject();damageable.addProperty("minecraft:max_damage",10);entry(contradiction).getAsJsonObject("item").add("components",damageable);invalid.add(contradiction);labels.add("strict-invalid-stackable-damageable-prototype");
        List<ItemStack> sentinel=List.of(named(Items.EMERALD,11,"preserved"),ItemStack.EMPTY,new ItemStack(Items.PAPER,9));
        Map<String,Object> errors=new LinkedHashMap<>();
        for(int index=0;index<invalid.size();index++) {
            String label=labels.get(index);JsonObject bad=invalid.get(index);String why=error(codec.parse(ops,bad),"Private codec accepted "+label);
            Object existing=handler(sentinel);CompoundTag nbt=(CompoundTag)JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE,bad);boolean rejected=false;
            try {call(type(SERIAL),"read",existing,level.registryAccess(),nbt);}catch(InvocationTargetException failure) {
                Throwable cause=unwrap(failure);if(!(cause instanceof IllegalArgumentException||cause instanceof IllegalStateException))throw failure;rejected=true;
            }
            require(rejected,"StorageSerialization.read accepted "+label);sameHandler(sentinel,existing,"Rejected "+label+" preserves existing handler");errors.put(label,why);
        }
        for(int count:new int[]{MAX+1,Integer.MAX_VALUE}) {
            boolean rejected=false;try {contents(List.of(new ItemStack(Items.DIAMOND,count)));}catch(InvocationTargetException expected){if(!(unwrap(expected) instanceof IllegalArgumentException))throw expected;rejected=true;}
            require(rejected,"fromItems accepted quantity "+count);
        }
        return Map.of("rejections",errors,"all_existing_handler_slots_unchanged",true,"fromItems_overflow_rejected",true);
    }
    private static JsonObject entry(JsonObject document) {return document.getAsJsonArray("Entries").get(0).getAsJsonObject();}

    private static ResourceHandler<ItemResource> transfer(ItemAccess access) throws Exception {return (ResourceHandler<ItemResource>)call(type(BASE+"util.StorageItemTransfer"),"backpack",access);}
    private static long total(ResourceHandler<ItemResource> handler) {long total=0;for(int i=0;i<handler.size();i++)total+=handler.getAmountAsLong(i);return total;}
    private static void accessSame(ItemAccess access,ItemResource expected,int amount,String stage) {eq(amount,access.getAmount(),stage+" owner amount");require(access.getResource().equals(expected),stage+" owner item/components changed");}
    private static Map<String,Object> transactions() throws Exception {
        ItemStack original=backpack();DataComponentType<Integer> multiplier=(DataComponentType<Integer>)constant(BASE+"init.ModDataComponents","BACKPACK_STACK_MULTIPLIER");original.set(multiplier,Integer.MAX_VALUE);
        ItemAccess access=ItemAccess.forStack(original);ResourceHandler<ItemResource> handler=transfer(access);ItemResource diamond=ItemResource.of(named(Items.DIAMOND,1,"transfer"));
        eq(108,handler.size(),"Only item slots are exposed");eq(2048,handler.getCapacityAsLong(0,diamond),"Hardened capacity ignores forged INT_MAX component");eq(2048,handler.getCapacityAsLong(107,diamond),"Last exposed item capacity");
        boolean outOfBounds=false;try {handler.getResource(108);}catch(IndexOutOfBoundsException expected){outOfBounds=true;}require(outOfBounds,"Tool/upgrade slot 108 was exposed");
        ItemResource before=access.getResource();try(Transaction tx=Transaction.openRoot()){eq(2048,handler.insert(0,diamond,Integer.MAX_VALUE,tx),"Capacity-bound simulated insert");}accessSame(access,before,1,"Root abort");eq(0,total(handler),"Root abort contents");
        try(Transaction tx=Transaction.openRoot()){eq(2048,handler.insert(0,diamond,Integer.MAX_VALUE,tx),"Capacity-bound committed insert");eq(0,handler.insert(0,diamond,1,tx),"Full-slot overflow rejected");tx.commit();}eq(2048,handler.getAmountAsLong(0),"Committed quantity");
        require(handler.getResource(0).equals(diamond),"Committed components changed");before=access.getResource();
        try(Transaction outer=Transaction.openRoot()){try(Transaction nested=Transaction.open(outer)){eq(37,handler.extract(0,diamond,37,nested),"Nested extract");nested.commit();}}accessSame(access,before,1,"Nested commit/root abort");eq(2048,handler.getAmountAsLong(0),"Nested rollback quantity");
        try(Transaction outer=Transaction.openRoot()){try(Transaction nested=Transaction.open(outer)){eq(11,handler.extract(0,diamond,11,nested),"Nested aborted extract");}eq(2048,handler.getAmountAsLong(0),"Nested abort restored quantity");outer.commit();}accessSame(access,before,1,"Nested abort/root commit");
        try(Transaction outer=Transaction.openRoot()){try(Transaction nested=Transaction.open(outer)){eq(48,handler.extract(0,diamond,48,nested),"Nested committed extract");nested.commit();}outer.commit();}eq(2000,handler.getAmountAsLong(0),"Nested/root commit quantity");
        before=access.getResource();ItemResource bag=ItemResource.of(backpack());require(!handler.isValid(1,bag),"BackpackItem accepted as inventory resource");
        try(Transaction tx=Transaction.openRoot()){eq(0,handler.insert(1,bag,1,tx),"Nested backpack rejected");tx.commit();}accessSame(access,before,1,"Backpack rejection");
        ItemStack filled=access.getResource().toStack(1);require(!filled.has(DataComponents.CONTAINER),"Transfer stored inventory in native CONTAINER");
        SimpleContainer physical=new SimpleContainer(filled.copy());ResourceHandler<ItemResource> delegate=VanillaContainerWrapper.of(physical);ItemResource allowed=ItemResource.of(filled);
        ResourceHandler<ItemResource> filtered=new ResourceHandler<>() {
            public int size(){return delegate.size();}public ItemResource getResource(int i){return delegate.getResource(i);}public long getAmountAsLong(int i){return delegate.getAmountAsLong(i);}
            public long getCapacityAsLong(int i,ItemResource r){return isValid(i,r)?delegate.getCapacityAsLong(i,r):0;}public boolean isValid(int i,ItemResource r){return r.equals(allowed)&&delegate.isValid(i,r);}
            public int insert(int i,ItemResource r,int n,TransactionContext tx){return isValid(i,r)?delegate.insert(i,r,n,tx):0;}public int extract(int i,ItemResource r,int n,TransactionContext tx){return delegate.extract(i,r,n,tx);}
        };
        ItemAccess strict=ItemAccess.forHandlerIndexStrict(filtered,0);ResourceHandler<ItemResource> rejected=transfer(strict);eq(108,rejected.size(),"Strict slot backpack view");
        ItemResource strictOriginal=strict.getResource();try(Transaction tx=Transaction.openRoot()) {
            eq(0,rejected.insert(0,diamond,1,tx),"Strict filtered exchange insert failure");eq(0,rejected.extract(0,diamond,1,tx),"Strict filtered exchange extract failure");tx.commit();
        }
        accessSame(strict,strictOriginal,1,"Strict exchange failure");same(filled,physical.getItem(0),"Actual physical strict slot preserved");eq(2000,rejected.getAmountAsLong(0),"Strict exchange preserved stored quantity");
        return Map.of("item_slots",108,"capacity",2048,"forged_multiplier",Integer.MAX_VALUE,"committed_quantity",2000,"root_and_nested_rollback",true,"nested_commit",true,"backpack_item_rejected",true,"strict_filtered_slot_exchange_failure_preserved",true);
    }

    private static ItemStack chestItem(ServerLevel level,int fixture,int slot) {
        return switch(fixture) {case 0->new ItemStack(Items.DIAMOND,64);case 1->named(Items.PAPER,64,"chest-slot-"+slot);default->nativeContainer(level,"chest-slot-"+slot);};
    }
    private static Map<String,Object> guiNative(boolean extraction) throws Exception {
        ItemStack physical=backpack();ItemStack diamonds=named(Items.DIAMOND,1,extraction?"gui-extract":"gui-insert");
        int initial=extraction?500:0,changed=extraction?200:333,paperCount=extraction?23:17;
        if(initial>0)physical.set(storageType(),contents(List.of(diamonds.copyWithCount(initial),ItemStack.EMPTY)));
        Object oldGui=make(BASE+"backpack.inventory.BackpackContainer",null,physical);
        ItemAccess access=ItemAccess.forStack(physical);ResourceHandler<ItemResource> nativeHandler=access.getCapability(Capabilities.Item.ITEM);
        require(nativeHandler!=null,"Registered native backpack item capability is absent");
        ItemResource resource=ItemResource.of(diamonds);
        try(Transaction tx=Transaction.openRoot()) {
            eq(changed,extraction?nativeHandler.extract(0,resource,changed,tx):nativeHandler.insert(0,resource,changed,tx),"Native change with old GUI snapshot alive");tx.commit();
        }
        int remaining=extraction?initial-changed:changed;
        Object committed=physical.get(storageType());require(committed!=null,"Native transaction did not write same physical ItemStack component");
        same(diamonds.copyWithCount(remaining),(ItemStack)call(committed,"getStackInSlot",0),"Physical stack immediately after native commit");
        ItemStack paper=named(Items.PAPER,paperCount,extraction?"gui-after-extract":"gui-after-insert");
        Object oldGuiHandler=call(oldGui,"getItemHandler");call(oldGuiHandler,"setStackInSlot",1,paper);call(oldGui,"saveItemsToStack");
        Object saved=physical.get(storageType());require(saved!=null,"Old GUI save removed private inventory");
        same(diamonds.copyWithCount(remaining),(ItemStack)call(saved,"getStackInSlot",0),extraction?"Old GUI revived extracted diamonds":"Old GUI overwrote native inserted diamonds");
        same(paper,(ItemStack)call(saved,"getStackInSlot",1),"Old GUI paper write missing");
        long savedTotal=0;for(int i=0;i<number(call(saved,"getSlots"));i++)savedTotal+=((ItemStack)call(saved,"getStackInSlot",i)).getCount();
        eq(remaining+paperCount,savedTotal,"Physical private inventory total after GUI save");
        eq(initial+paperCount+(extraction?0:changed),savedTotal+(extraction?changed:0),"Native/GUI/extracted-output quantity conservation");
        eq(savedTotal,total(nativeHandler),"Existing native view sees GUI write");require(!physical.has(DataComponents.CONTAINER),"GUI/native interleaving touched vanilla CONTAINER");
        return Map.of("same_physical_stack",true,"gui_constructed_before_native_transaction",true,"initial_diamonds",initial,"native_"+(extraction?"extracted":"inserted"),changed,"saved_diamonds",remaining,"saved_named_paper",paperCount,"saved_total",savedTotal,"total_conserved",true);
    }
    @SuppressWarnings("unchecked")
    private static Map<String,Object> backpackBoundaries() throws Exception {
        ItemStack high=backpack(),low=item("fxntstorage:backpack");
        DataComponentType<Object> sort=(DataComponentType<Object>)constant(BASE+"init.ModDataComponents","INVENTORY_SORT_ORDER");
        Object name=Enum.valueOf((Class)type(BASE+"util.SortOrder"),"NAME");
        Object count=Enum.valueOf((Class)type(BASE+"util.SortOrder"),"COUNT");
        high.set(sort,name);low.set(sort,count);
        Object gui=make(BASE+"backpack.inventory.BackpackContainer",null,high);
        eq(32,number(call(gui,"getStackMultiplier")),"Hardened GUI multiplier");
        require(call(gui,"getSortOrder")==name,"Initial GUI setting");
        call(gui,"setContext",low,null);
        eq(2,number(call(gui,"getStackMultiplier")),"Replacement basic GUI multiplier");
        require(call(gui,"getSortOrder")==count,"Old backpack settings leaked into replacement");
        Object handler=call(gui,"getItemHandler");
        ItemStack remainder=(ItemStack)call(handler,"insertItem",0,new ItemStack(Items.DIAMOND,500),false);
        eq(372,remainder.getCount(),"Basic backpack insertion remainder");
        eq(128,((ItemStack)call(handler,"getStackInSlot",0)).getCount(),"Basic backpack actual capacity");
        List<ItemStack> before=new ArrayList<>();for(int i=0;i<159;i++)before.add(((ItemStack)call(handler,"getStackInSlot",i)).copy());
        List<ItemStack> illegal=new ArrayList<>(java.util.Collections.nCopies(159,ItemStack.EMPTY));
        illegal.set(0,new ItemStack(Items.EMERALD,10));illegal.set(108,new ItemStack(Items.DIAMOND,65));
        Throwable failure=null;try{call(gui,"readInventory",contents(illegal));}catch(Throwable e){failure=unwrap(e);}
        require(failure instanceof IllegalArgumentException,"Oversized tool-slot document was accepted: "+failure);
        sameHandler(before,handler,"Invalid document partly changed cached backpack");
        failure=null;try{call(handler,"setStackInSlot",108,backpack());}catch(Throwable e){failure=unwrap(e);}
        require(failure instanceof IllegalArgumentException,"Direct nested-backpack setter was accepted: "+failure);
        sameHandler(before,handler,"Rejected nested backpack changed inventory");
        call(gui,"saveItemsToStack");
        ResourceHandler<ItemResource> nativeView=ItemAccess.forStack(low).getCapability(Capabilities.Item.ITEM);
        require(nativeView!=null,"Replacement backpack native capability missing");
        eq(128,total(nativeView),"GUI and native basic backpack inventory diverged");
        return Map.of("high_tier_multiplier",32,"low_tier_multiplier",2,"settings_reloaded",true,"basic_capacity",128,"tool_slot_overflow_rejected_atomically",true,"nested_backpack_rejected",true);
    }
    private static Map<String,Object> chests(ServerLevel level,Path root,boolean reloaded) throws Exception {
        guard(level,root);Path manifest=safeFile(root,"storage-isolation-fixture.json");var ops=level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        JsonObject data;
        if(reloaded) {
            require(Files.isRegularFile(manifest,LinkOption.NOFOLLOW_LINKS),"Reload requested without saved Storage isolation fixture");
            data=JsonParser.parseString(Files.readString(manifest)).getAsJsonObject();
            require(data.get("token").getAsString().equals(TOKEN),"Wrong isolation fixture token");
            require(data.get("world").getAsString().equals(root.resolve("world").toRealPath().toString()),"Chest fixture belongs to another world");
            require(data.get("jvm_start_epoch_millis").getAsLong()!=ManagementFactory.getRuntimeMXBean().getStartTime(),"Reload claimed inside the same JVM that created the chest fixture");
        } else {
            require(!Files.exists(manifest,LinkOption.NOFOLLOW_LINKS),"First-phase chest fixture already exists; refusing overwrite");
            data=new JsonObject();data.addProperty("token",TOKEN);data.addProperty("id",UUID.randomUUID().toString());data.addProperty("world",root.resolve("world").toRealPath().toString());data.addProperty("jvm_start_epoch_millis",ManagementFactory.getRuntimeMXBean().getStartTime());JsonArray boxes=new JsonArray();
            for(int fixture=0;fixture<3;fixture++) {
                guard(level,root);BlockPos pos=new BlockPos(224+fixture*8,-60,8);level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,true);level.getChunkAt(pos);
                level.removeBlock(pos,false);require(level.setBlock(pos,Blocks.CHEST.defaultBlockState(),3),"Could not place disposable native chest "+pos);
                require(level.getBlockEntity(pos) instanceof ChestBlockEntity,"Missing native chest block entity "+pos);ChestBlockEntity chest=(ChestBlockEntity)level.getBlockEntity(pos);eq(27,chest.getContainerSize(),"Ordinary chest slots");
                JsonObject box=new JsonObject();box.addProperty("x",pos.getX());box.addProperty("y",pos.getY());box.addProperty("z",pos.getZ());JsonArray slots=new JsonArray();
                for(int slot=0;slot<chest.getContainerSize();slot++){ItemStack expected=chestItem(level,fixture,slot);require(!expected.isEmpty()&&expected.getCount()==expected.getMaxStackSize(),"Chest fixture is not a legal full stack");chest.setItem(slot,expected);slots.add(ItemStack.CODEC.encodeStart(ops,expected).getOrThrow());}
                chest.setChanged();box.add("slots",slots);boxes.add(box);
            }
            data.add("chests",boxes);guard(level,root);require(level.getServer().saveEverything(false,true,true),"Disposable server saveEverything returned false");
            Files.writeString(manifest,GSON.toJson(data)+"\n",StandardOpenOption.CREATE_NEW);
        }
        JsonArray boxes=data.getAsJsonArray("chests");eq(3,boxes.size(),"Chest fixture count");int checked=0;
        for(JsonElement e:boxes) {
            JsonObject box=e.getAsJsonObject();BlockPos pos=new BlockPos(box.get("x").getAsInt(),box.get("y").getAsInt(),box.get("z").getAsInt());
            require(List.of(224,232,240).contains(pos.getX())&&pos.getY()==-60&&pos.getZ()==8,"Chest manifest coordinate escaped authorized fixture area");
            level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,true);level.getChunkAt(pos);require(level.getBlockState(pos).is(Blocks.CHEST),"Native chest missing after save/reload at "+pos);
            require(level.getBlockEntity(pos) instanceof ChestBlockEntity,"Native chest entity missing after save/reload at "+pos);ChestBlockEntity chest=(ChestBlockEntity)level.getBlockEntity(pos);JsonArray slots=box.getAsJsonArray("slots");eq(27,slots.size(),"Saved chest slot count");eq(27,chest.getContainerSize(),"Actual native chest slot count");
            for(int slot=0;slot<27;slot++){ItemStack expected=ItemStack.CODEC.parse(ops,slots.get(slot)).getOrThrow();same(expected,chest.getItem(slot),(reloaded?"Reloaded":"Created")+" native chest "+pos+" slot "+slot);checked++;}
        }
        return Map.of("phase",reloaded?"disk-reload-verified":"saved-awaiting-restart","chests",3,"checked_full_slots",checked,"positions_x",List.of(224,232,240),"y",-60,"z",8,"ordinary_stack_max",64,"fixture_id",data.get("id").getAsString());
    }
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
    private static void eq(long expected,long actual,String label){require(expected==actual,label+": expected="+expected+", actual="+actual);}
    private static void same(ItemStack expected,ItemStack actual,String label){require(ItemStack.matches(expected,actual),label+": expected="+expected+" patch="+expected.getComponentsPatch()+", actual="+actual+" patch="+actual.getComponentsPatch());}
    private static long number(Object value){return ((Number)value).longValue();}
    private static String error(DataResult<?> result,String acceptedMessage){require(result.error().isPresent()&&result.result().isEmpty(),acceptedMessage);return result.error().orElseThrow().message();}
    private static Throwable unwrap(Throwable t){while(t instanceof InvocationTargetException i&&i.getTargetException()!=null)t=i.getTargetException();return t;}
    private static ItemStack item(String id){Item item=BuiltInRegistries.ITEM.getValue(Identifier.parse(id));require(item!=null&&item!=Items.AIR,"Missing item "+id);return new ItemStack(item);}
    private static Class<?> type(String name)throws ClassNotFoundException{return Class.forName(name);}
    private static Class<?> boxed(Class<?> c){if(!c.isPrimitive())return c;if(c==int.class)return Integer.class;if(c==boolean.class)return Boolean.class;if(c==long.class)return Long.class;if(c==double.class)return Double.class;if(c==float.class)return Float.class;if(c==byte.class)return Byte.class;if(c==short.class)return Short.class;if(c==char.class)return Character.class;return c;}
    private static boolean accepts(Class<?>[] types,Object[] args){if(types.length!=args.length)return false;for(int i=0;i<types.length;i++)if(args[i]!=null&&!boxed(types[i]).isInstance(args[i]))return false;return true;}
    private static Object call(Object receiver,String name,Object...args)throws Exception {
        Class<?> c=receiver instanceof Class<?> cl?cl:receiver.getClass();
        for(Class<?> current=c;current!=null;current=current.getSuperclass())for(Method method:current.getDeclaredMethods())
            if(method.getName().equals(name)&&accepts(method.getParameterTypes(),args)){method.setAccessible(true);return method.invoke(receiver instanceof Class<?>?null:receiver,args);}
        for(Method method:c.getMethods())if(method.getName().equals(name)&&accepts(method.getParameterTypes(),args))return method.invoke(receiver instanceof Class<?>?null:receiver,args);
        throw new NoSuchMethodException(c.getName()+"."+name+" "+Arrays.toString(args));
    }
    private static Object make(String name,Object...args)throws Exception {for(Constructor<?> ctor:type(name).getDeclaredConstructors())if(accepts(ctor.getParameterTypes(),args)){ctor.setAccessible(true);return ctor.newInstance(args);}throw new NoSuchMethodException(name+" constructor "+Arrays.toString(args));}
    private static Object constant(String name,String field)throws Exception {Field member=type(name).getDeclaredField(field);member.setAccessible(true);return member.get(null);}
}
