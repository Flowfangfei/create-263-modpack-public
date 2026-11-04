package net.fxnt.fxntstorage.inventory;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import com.simibubi.create.compat.neoforge263.items.IItemHandler;
import com.simibubi.create.compat.neoforge263.items.ItemStackHandler;

/** Immutable mod-owned inventory. Vanilla codecs only ever see count-one prototypes. */
public final class StorageContents {
    public static final int MAX_SLOTS=4096, MAX_COUNT=1048576*81;
    public static final StorageContents EMPTY=new StorageContents(0,List.of());
    private final int size;
    private final List<Entry> entries;
    private record Entry(ItemStack item,int count) {
        private static final Codec<Entry> CODEC=RecordCodecBuilder.<Entry>create(i->i.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(Entry::item),
            Codec.intRange(0,MAX_COUNT).fieldOf("amount").forGetter(Entry::count)
        ).apply(i,Entry::new)).validate(e->{
            if(e.item.isEmpty()!= (e.count==0))return DataResult.error(()->"Empty prototype/count mismatch");
            if(!e.item.isEmpty()&&e.item.getCount()!=1)return DataResult.error(()->"Storage prototype must have count one");
            return e.item.isEmpty()?DataResult.success(e):ItemStack.validateStrict(e.item).map(valid->e);
        });
        private static Entry of(ItemStack stack){
            if(stack.isEmpty())return new Entry(ItemStack.EMPTY,0);
            if(stack.getCount()<1||stack.getCount()>MAX_COUNT)throw new IllegalArgumentException("Storage quantity out of range: "+stack.getCount());
            return new Entry(stack.copyWithCount(1),stack.getCount());
        }
        private ItemStack materialize(){return count==0?ItemStack.EMPTY:item.copyWithCount(count);}
        @Override public boolean equals(Object other){return other instanceof Entry e&&count==e.count&&ItemStack.matches(item,e.item);}
        @Override public int hashCode(){return Objects.hash(item.getItem(),item.getComponents(),count);}
    }
    public static final Codec<StorageContents> CODEC=Codec.lazyInitialized(()->RecordCodecBuilder.<StorageContents>create(i->i.group(
        Codec.intRange(1,1).fieldOf("StorageFormat").forGetter(c->1),
        Codec.intRange(0,MAX_SLOTS).fieldOf("Size").forGetter(StorageContents::getSlots),
        Entry.CODEC.listOf(0,MAX_SLOTS).fieldOf("Entries").forGetter(c->c.entries)
    ).apply(i,(version,size,entries)->new StorageContents(size,entries))).validate(c->c.size==c.entries.size()
        ?DataResult.success(c):DataResult.error(()->"Storage slot count does not match entries")));
    public static final MapCodec<StorageContents> MAP_CODEC=MapCodec.assumeMapUnsafe(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf,StorageContents> STREAM_CODEC=ByteBufCodecs.fromCodecWithRegistries(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf,ItemStack> STACK_STREAM_CODEC=STREAM_CODEC.map(
        value->{if(value.getSlots()!=1)throw new IllegalArgumentException("Expected one display slot");return value.getStackInSlot(0);},
        stack->fromItems(List.of(stack)));
    public static final Codec<ItemStackHandler> HANDLER_CODEC=CODEC.xmap(StorageContents::createHandler,StorageContents::fromHandler);
    private StorageContents(int size,List<Entry> entries){this.size=size;this.entries=List.copyOf(entries);}
    public static StorageContents fromItems(List<ItemStack> items){
        if(items.size()>MAX_SLOTS)throw new IllegalArgumentException("Too many Storage slots");
        return new StorageContents(items.size(),items.stream().map(Entry::of).toList());
    }
    public static StorageContents fromHandler(IItemHandler handler){
        List<ItemStack> items=new ArrayList<>();for(int i=0;i<handler.getSlots();i++)items.add(handler.getStackInSlot(i));return fromItems(items);
    }
    public int getSlots(){return size;}
    public ItemStack getStackInSlot(int slot){return slot<0||slot>=size?ItemStack.EMPTY:entries.get(slot).materialize();}
    public Stream<ItemStack> itemCopies(){return entries.stream().map(Entry::materialize);}
    public Stream<ItemStack> stream(){return itemCopies();}
    public StorageContents withSlot(int index,ItemStack stack,int minimumSize){
        int length=Math.max(size,minimumSize);if(index<0||index>=length||length>MAX_SLOTS)throw new IndexOutOfBoundsException(index);
        List<ItemStack> items=new ArrayList<>();for(int i=0;i<length;i++)items.add(i==index?stack:getStackInSlot(i));return fromItems(items);
    }
    private ItemStackHandler createHandler(){
        var handler=new StorageInventoryHandler(size);for(int i=0;i<size;i++)handler.setStackInSlot(i,getStackInSlot(i));return handler;
    }
    @Override public boolean equals(Object other){return other instanceof StorageContents c&&size==c.size&&entries.equals(c.entries);}
    @Override public int hashCode(){return Objects.hash(size,entries);}
}
