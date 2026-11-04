package dev.create263.probe;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.api.client.registration.StoryBoardEntry;
import net.createmod.ponder.api.client.scene.PonderScene;
import net.createmod.ponder.impl.client.gui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** All registrations are compiled; distinct scene/program/template combinations are rendered in bounded batches. */
final class AllPonderClientProbe {
    private static final Path ROOT=Path.of(System.getProperty("create263.clientProbeRoot")).toAbsolutePath().normalize();
    private static final boolean SCAN=System.getProperty("create263.allPonderMode","scan").equals("scan");
    private static final Map<String,Object> result=new LinkedHashMap<>();
    private static final List<Map<String,Object>> checks=new ArrayList<>(),failures=new ArrayList<>(),jobs=new ArrayList<>(),rendered=new ArrayList<>();
    private static final Map<Identifier,List<StoryBoardEntry>> registered=new TreeMap<>(Comparator.comparing(Identifier::toString));
    private static final Set<String> unique=new HashSet<>();
    private static final IdentityHashMap<Object,Integer> programs=new IdentityHashMap<>();
    // Source-verified, deliberately perpetual decorative animations; finite tails still must finish.
    private static final Set<String> CONTINUOUS_INSTRUCTIONS=Set.of(
        "com.jesz.createdieselgenerators.ponder.PumpjackScene$PumpjackAnimateInstruction",
        "dev.simulated_team.simulated.ponder.instructions.GimbalSensorVisualRotationInstruction");
    private static List<Identifier> components;
    private static ProbeUI ui;
    private static int startup,cursor,waitTicks,framesBefore,afterDisconnect,point,tailTicks,tailSamples;
    private static List<Integer> timeline;
    private static boolean initialized,finished,disconnecting,saveRequested,failed;
    private static volatile boolean saved;
    private static boolean reloadRequested;
    private static java.util.concurrent.CompletableFuture<Void> resourceReload;
    static void install(){
        require(ROOT.equals(FMLPaths.GAMEDIR.get().toAbsolutePath().normalize()),"Unowned game directory");
        require(Files.isRegularFile(ROOT.resolve(".create263-disposable-runtime")),"Missing test marker");
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class,event->tick());
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private static void write()throws Exception{Files.writeString(ROOT.resolve("all-ponder-client-audit.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");}
    private static void finish(Minecraft mc)throws Exception{
        if(!saveRequested){saveRequested=true;mc.gui.setScreen(null);var server=mc.getSingleplayerServer();server.execute(()->{server.saveEverything(false,true,true);saved=true;});}
        if(saved){result.put("world_saved",true);write();disconnecting=true;mc.disconnectWithSavingScreen();mc.gui.setScreen(new TitleScreen());}
    }
    private static void init()throws Exception{
        initialized=true;result.put("mode",SCAN?"scan":"render");result.put("status","running");
        for(var entry:PonderIndex.getSceneAccess().getRegisteredEntries())registered.computeIfAbsent(entry.getKey(),k->new ArrayList<>()).add(entry.getValue());
        components=new ArrayList<>(registered.keySet());
        result.put("registered_components",components.size());result.put("registered_storyboards",registered.values().stream().mapToInt(List::size).sum());
        var owners=new TreeMap<String,Integer>();for(var group:registered.values())for(var entry:group)owners.merge(entry.getNamespace(),1,Integer::sum);result.put("registration_namespaces",owners);
        var mods=new TreeMap<String,String>();for(var info:net.neoforged.fml.ModList.get().getMods())mods.put(info.getModId(),info.getDisplayName());result.put("loaded_mods",mods);
        require(!components.isEmpty(),"Ponder registry empty");
        if(!SCAN){
            var input=JsonParser.parseString(Files.readString(ROOT.resolve("ponder-render-jobs.json"))).getAsJsonArray();
            for(var element:input)jobs.add(new Gson().fromJson(element,new com.google.gson.reflect.TypeToken<LinkedHashMap<String,Object>>(){}.getType()));
            require(!jobs.isEmpty(),"Render batch empty");result.put("requested_jobs",jobs.size());
        }
        result.put("content_checks",PonderContentChecks.report());result.put("checks",checks);result.put("failures",failures);result.put("render_jobs",jobs);result.put("rendered",rendered);write();
    }
    private static void scan(Minecraft mc)throws Exception{
        if(cursor>=components.size()){result.put("compiled_components",checks.size());result.put("distinct_scene_template_pairs",jobs.size());result.put("distinct_scene_program_template_jobs",jobs.size());failed=!failures.isEmpty();finish(mc);return;}
        Identifier id=components.get(cursor++);result.put("active_component",id.toString());write();System.out.println("CREATE263_ALL_PONDER_COMPONENT "+id);
        try{
            var entries=registered.get(id);
            for(var entry:entries){
                Identifier template=entry.getSchematicIdentifier();
                require(mc.getResourceManager().getResource(Identifier.fromNamespaceAndPath(template.getNamespace(),"ponder/"+template.getPath()+".nbt")).isPresent(),"Missing template "+template);
            }
            // Match PonderUI's normal overload, including mod RETURN mixins.
            var scenes=PonderIndex.getSceneAccess().compile(id);require(scenes.size()==entries.size(),"Storyboard/scene count differs: "+id);
            var row=new LinkedHashMap<String,Object>();row.put("component",id.toString());row.put("scenes",scenes.stream().map(s->s.getSceneId().toString()).toList());row.put("templates",entries.stream().map(e->e.getSchematicIdentifier().toString()).toList());row.put("namespaces",entries.stream().map(StoryBoardEntry::getNamespace).toList());checks.add(row);
            for(int index=0;index<scenes.size();index++){
                var scene=scenes.get(index);var entry=entries.get(index);int program=programs.computeIfAbsent(entry.getBoard(),board->programs.size());String key=scene.getSceneId()+"|"+entry.getSchematicIdentifier()+"|"+program;
                PonderContentChecks.initial(scene,entry.getSchematicIdentifier().toString());
                PonderContentChecks.scriptedFluids(scene,id.toString());
                require(scene.getWorld()!=null,"Scene world missing "+key);require(scene.getTotalTime()>0,"Empty timeline "+key);
                if(unique.add(key)){
                    var job=new LinkedHashMap<String,Object>();job.put("component",id.toString());job.put("scene_index",index);job.put("scene_id",scene.getSceneId().toString());job.put("template",entry.getSchematicIdentifier().toString());job.put("namespace",entry.getNamespace());job.put("total_ticks",scene.getTotalTime());job.put("program",program);jobs.add(job);
                }
            }
        }catch(Throwable error){var row=new LinkedHashMap<String,Object>();row.put("component",id.toString());row.put("error",error.toString());failures.add(row);error.printStackTrace();}
        result.put("checked_components",cursor);write();
        if(cursor%25==0)System.out.println("CREATE263_ALL_PONDER_SCAN "+cursor+"/"+components.size()+" failures="+failures.size());
    }
    private static int integer(Object value){return ((Number)value).intValue();}
    private static void render(Minecraft mc)throws Exception{
        if(cursor>=jobs.size()){result.put("completed_jobs",rendered.size());finish(mc);return;}
        var job=jobs.get(cursor);
        if(ui==null){
            result.put("active_job",job);write();
            var available=PonderIndex.getSceneAccess().compile(Identifier.parse((String)job.get("component")));
            int index=integer(job.get("scene_index"));require(index<available.size(),"Scene index changed");var scene=available.get(index);
            require(scene.getSceneId().toString().equals(job.get("scene_id")),"Scene registration order changed");
            PonderContentChecks.initial(scene,(String)job.get("template"));
            ui=new ProbeUI(List.of(scene));mc.gui.setScreen(ui);waitTicks=0;framesBefore=0;point=0;tailTicks=0;tailSamples=0;
            TreeSet<Integer> points=new TreeSet<>(List.of(0,10,30,60,120));
            for(int time=240;time<scene.getTotalTime();time+=240)points.add(time);
            points.removeIf(time->time>=scene.getTotalTime());points.add(scene.getTotalTime()-1);timeline=new ArrayList<>(points);
            System.out.println("CREATE263_ALL_PONDER_OPEN "+(cursor+1)+"/"+jobs.size()+" "+job.get("scene_id"));return;
        }
        require(mc.gui.screen()==ui,"Ponder screen closed");if(++waitTicks<5)return;
        require(ui.frames>framesBefore,"No GUI frames after seek");var scene=ui.getActiveScene();
        if(++point<timeline.size()){
            int next=timeline.get(point);if(next>scene.getCurrentTime())scene.seekToTime(next);
            framesBefore=ui.frames;waitTicks=0;result.put("active_timeline",scene.getCurrentTime());write();return;
        }
        // Ponder may mark a scene finished before decorative tail instructions run.
        // Continue the real schedule, including its post-finish tail, and render between chunks.
        if(hasFiniteInstructions(scene)||(tailSamples==0&&!remaining(scene).isEmpty())){
            require(tailTicks<20000,"Ponder instruction tail exceeded20000 ticks: "+job.get("scene_id")+" remaining="+remaining(scene).stream().map(i->i.getClass().getName()).toList());
            for(int i=0;i<120&&(hasFiniteInstructions(scene)||tailSamples==0&&!remaining(scene).isEmpty());i++){scene.tick();tailTicks++;}
            tailSamples++;framesBefore=ui.frames;waitTicks=0;result.put("active_tail_ticks",tailTicks);write();return;
        }
        var done=new LinkedHashMap<String,Object>(job);done.put("rendered_gui_frames",ui.frames);done.put("timeline_samples",timeline.size()+tailSamples);done.put("tail_advanced_ticks",tailTicks);done.put("instruction_schedule_empty",remaining(scene).isEmpty());done.put("finite_instruction_schedule_complete",!hasFiniteInstructions(scene));done.put("continuous_instruction_classes",remaining(scene).stream().map(i->i.getClass().getName()).distinct().toList());done.put("final_time",scene.getCurrentTime());rendered.add(done);
        if(Boolean.getBoolean("create263.ponderScreenshots")||cursor==jobs.size()-1)Screenshot.grab(mc,false);
        result.put("completed_jobs",rendered.size());write();mc.gui.setScreen(null);ui=null;cursor++;
    }
    private static boolean hasFiniteInstructions(PonderScene scene)throws Exception{
        for(Object instruction:remaining(scene)){
            String name=instruction.getClass().getName();
            if(CONTINUOUS_INSTRUCTIONS.contains(name))continue;
            if(name.equals("dev.ryanhcode.offroad.content.ponder.instructions.ChangeBoreheadAndContraptionSpeedInstruction")){
                var field=instruction.getClass().getField("startSlowing");
                if(!field.getBoolean(instruction))continue; // A drill left running is a continuous background animation.
            }
            if(name.equals("net.createmod.ponder.impl.client.instruction.EmitParticlesInstruction")){
                var field=instruction.getClass().getSuperclass().getDeclaredField("totalTicks");field.setAccessible(true);
                if(field.getInt(instruction)==Integer.MAX_VALUE)continue; // Source-declared persistent resistor smoke.
            }
            if(name.equals("dev.eriksonn.aeronautics.content.ponder.instructions.PropellerRotateInstruction")){
                var field=instruction.getClass().getDeclaredField("slowdownController");field.setAccessible(true);
                if(field.get(instruction)==null)continue; // Running propellers deliberately persist; stopping propellers must finish.
            }
            return true;
        }
        return false;
    }
    private static List<?> remaining(PonderScene scene)throws Exception{
        var field=PonderScene.class.getDeclaredField("activeSchedule");field.setAccessible(true);return (List<?>)field.get(scene);
    }
    private static void tick(){
        if(finished)return;var mc=Minecraft.getInstance();
        try{
            if(mc.getWindow().isIconified()){org.lwjgl.sdl.SDLVideo.SDL_RestoreWindow(mc.getWindow().handle());return;}
            if(disconnecting){
                if(!(mc.gui.screen() instanceof TitleScreen)||mc.level!=null||mc.getSingleplayerServer()!=null||mc.gui.overlay()!=null){afterDisconnect=0;return;}
                if(++afterDisconnect==30){result.put("saved_and_returned_to_title",true);result.put("status",failed?"failed":"completed");result.put("passed",!failed);write();}
                if(afterDisconnect>=50){finished=true;mc.stop();}return;
            }
            if(mc.level==null||mc.player==null||mc.gui.overlay()!=null)return;
            require(mc.getSingleplayerServer()!=null,"Requires integrated test server");
            require(mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT).toRealPath().equals(ROOT.resolve("saves/three-addon-test-copy").toRealPath()),"Unowned world");
            if(failed){finish(mc);return;}if(++startup<35)return;
            if(!initialized&&Boolean.getBoolean("create263.ponderReloadResources")){
                if(!reloadRequested){reloadRequested=true;result.put("resource_reload_requested",true);resourceReload=mc.reloadResourcePacks();return;}
                if(resourceReload!=null){
                    if(!resourceReload.isDone())return;resourceReload.join();resourceReload=null;result.put("resource_reload_completed",true);
                    var field=Class.forName("dev.simulated_team.simulated.index.SimSpriteShifts").getDeclaredField("ROPE_WINCH_COIL");field.setAccessible(true);var entry=field.get(null);
                    require(entry.getClass().getMethod("getOriginal").invoke(entry)!=null&&entry.getClass().getMethod("getTarget").invoke(entry)!=null,"Rope sprites missing after actual resource reload");
                    result.put("rope_sprites_loaded_after_reload",true);
                }
            }
            if(!initialized){init();return;}
            if(SCAN)scan(mc);else render(mc);
        }catch(Throwable error){error.printStackTrace();result.put("error",error.toString());result.put("passed",false);failed=true;try{write();}catch(Exception ignored){}}
    }
    private static final class ProbeUI extends PonderUI {
        private int frames;ProbeUI(List<PonderScene> scenes){super(scenes);}
        @Override public void extractScaledRenderState(GuiGraphicsExtractor graphics,int x,int y,float partialTicks){super.extractScaledRenderState(graphics,x,y,partialTicks);frames++;}
    }
}
