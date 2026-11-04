package foundry.veil.platform.registry;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import java.util.*;
import java.util.function.Supplier;
public interface RegistrationProvider<T> {
    Map<ResourceKey<?>,Registry<?>> CUSTOM=new HashMap<>();
    @SuppressWarnings("unchecked")
    static <T> RegistrationProvider<T> get(ResourceKey<? extends Registry<T>> key,String mod){
        return get(key,mod,true);
    }
    @SuppressWarnings("unchecked")
    static <T> RegistrationProvider<T> get(ResourceKey<? extends Registry<T>> key,String mod,boolean sync){
        Registry<T> registry=(Registry<T>)BuiltInRegistries.REGISTRY.getValue(key.identifier());
        if(registry==null){
            synchronized(CUSTOM){
                registry=(Registry<T>)CUSTOM.get(key);
                if(registry==null){
                    registry=new RegistryBuilder<T>(key).sync(sync).create();CUSTOM.put(key,registry);
                    Registry<T> created=registry;
                    NeoForgeRegistryBindings.onBus(mod,bus->bus.addListener(NewRegistryEvent.class,event->event.register(created)));
                }
            }
        }
        return new NativeProvider<>(registry,mod);
    }
    static <T> RegistrationProvider<T> get(Registry<T> registry,String mod){return new NativeProvider<>(registry,mod);}
    default <I extends T> RegistryObject<I> register(String name,Supplier<? extends I> supplier){return register(Identifier.fromNamespaceAndPath(getModId(),name),supplier);}
    <I extends T> RegistryObject<I> register(Identifier id,Supplier<? extends I> supplier);
    Collection<RegistryObject<T>> getEntries();
    Registry<T> asVanillaRegistry();
    String getModId();
    final class NativeProvider<T> implements RegistrationProvider<T> {
        private final Registry<T> registry;private final String mod;
        private final DeferredRegister<T> deferred;private final List<RegistryObject<T>> entries=new ArrayList<>();
        NativeProvider(Registry<T> registry,String mod){this.registry=registry;this.mod=mod;deferred=DeferredRegister.create(registry.key(),mod);NeoForgeRegistryBindings.onBus(mod,deferred::register);}
        @SuppressWarnings({"unchecked","rawtypes"})
        public <I extends T> RegistryObject<I> register(Identifier id,Supplier<? extends I> supplier){
            if(!id.getNamespace().equals(mod))throw new IllegalArgumentException("Wrong registration namespace: "+id);
            DeferredHolder<T,I> holder=deferred.register(id.getPath(),supplier);
            RegistryObject<I> object=new RegistryObject<>(){
                public ResourceKey<I> getResourceKey(){return (ResourceKey)holder.getKey();}
                public I get(){return holder.get();}
                public boolean isPresent(){return holder.isBound();}
                public Holder<I> asHolder(){return (Holder)holder;}
            };
            entries.add((RegistryObject)object);return object;
        }
        public Collection<RegistryObject<T>> getEntries(){return Collections.unmodifiableList(entries);}
        public Registry<T> asVanillaRegistry(){return registry;}
        public String getModId(){return mod;}
    }
}
