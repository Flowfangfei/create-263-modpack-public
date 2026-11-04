package foundry.veil.platform.registry;
import java.util.*;
import java.util.function.Consumer;
import net.neoforged.bus.api.IEventBus;
public final class NeoForgeRegistryBindings {
    private static final Map<String,IEventBus> BUSES=new HashMap<>();
    private static final Map<String,List<Consumer<IEventBus>>> WAITING=new HashMap<>();
    public static synchronized void bind(String mod,IEventBus bus){
        IEventBus old=BUSES.putIfAbsent(mod,bus);if(old!=null&&old!=bus)throw new IllegalStateException("Different mod buses for "+mod);
        var tasks=WAITING.remove(mod);if(tasks!=null)tasks.forEach(task->task.accept(bus));
    }
    public static synchronized void onBus(String mod,Consumer<IEventBus> task){
        var bus=BUSES.get(mod);if(bus!=null)task.accept(bus);else WAITING.computeIfAbsent(mod,key->new ArrayList<>()).add(task);
    }
}
