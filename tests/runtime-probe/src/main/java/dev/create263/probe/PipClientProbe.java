package dev.create263.probe;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.api.client.scene.PonderScene;
import net.createmod.ponder.impl.client.gui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Real Ponder GUI regression in a marked, disposable world; never shipped in the pack. */
final class PipClientProbe {
    private static final Path ROOT=Path.of(System.getProperty("create263.clientProbeRoot")).toAbsolutePath().normalize();
    private static final Map<String,Object> result=new LinkedHashMap<>();
    private static final List<PonderScene> scenes=new ArrayList<>();
    private static final List<Map<String,Object>> samples=new ArrayList<>();
    private static ProbeUI ui;
    private static int ticks,sceneIndex,waitTicks,sampleTime,framesBefore,afterDisconnect;
    private static boolean started,finished,disconnecting,saveRequested,failed;
    private static volatile boolean saved;
    static void install(){
        require(ROOT.equals(FMLPaths.GAMEDIR.get().toAbsolutePath().normalize()),"Wrong client game directory");
        require(Files.isRegularFile(ROOT.resolve(".create263-disposable-runtime")),"Missing disposable runtime marker");
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class,event->tick());
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private static void write()throws Exception{Files.writeString(ROOT.resolve("pip-client-regression.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");}
    private static void open(Minecraft mc)throws Exception{
        PonderScene scene=scenes.get(sceneIndex);ui=new ProbeUI(List.of(scene));mc.gui.setScreen(ui);
        sampleTime=0;waitTicks=0;framesBefore=0;
        result.put("active_scene",scene.getSceneId().toString());result.put("active_scene_total_ticks",scene.getTotalTime());write();
        System.out.println("CREATE263_PIP_SCENE_OPEN "+scene.getSceneId());
    }
    private static void requestFinish(Minecraft mc)throws Exception{
        if(!saveRequested){saveRequested=true;mc.gui.setScreen(null);var server=mc.getSingleplayerServer();server.execute(()->{server.saveEverything(false,true,true);saved=true;});}
        if(saved){result.put("world_saved",true);write();disconnecting=true;mc.disconnectWithSavingScreen();mc.gui.setScreen(new TitleScreen());}
    }
    private static void tick(){
        if(finished)return;var mc=Minecraft.getInstance();
        try{
            if(mc.getWindow().isIconified()){org.lwjgl.sdl.SDLVideo.SDL_RestoreWindow(mc.getWindow().handle());return;}
            if(disconnecting){
                if(!(mc.gui.screen() instanceof TitleScreen)||mc.level!=null||mc.getSingleplayerServer()!=null||mc.gui.overlay()!=null){afterDisconnect=0;return;}
                if(++afterDisconnect==30){result.put("saved_and_returned_to_title",true);result.put("passed",!failed);write();}
                if(afterDisconnect>=50){finished=true;mc.stop();}return;
            }
            if(mc.level==null||mc.player==null||mc.gui.overlay()!=null)return;
            require(mc.getSingleplayerServer()!=null,"Requires integrated server");
            require(mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT).toRealPath().equals(ROOT.resolve("saves/three-addon-test-copy").toRealPath()),"Unowned world path");
            if(failed){requestFinish(mc);return;}
            if(++ticks<35)return;
            if(!started){
                started=true;result.put("status","running");
                var type=Class.forName("dev.eriksonn.aeronautics.index.client.AeroRenderTypes");
                var counts=new LinkedHashMap<String,Integer>();
                for(String method:List.of("burnerFlame","irisCompositeBurnerFlame")){
                    RenderType render=(RenderType)type.getMethod(method).invoke(null);
                    counts.put(method,render.pipeline().getColorTargetStates().size());
                }
                result.put("burner_color_target_counts",counts);System.out.println("CREATE263_PIP_PIPELINE_TARGETS "+counts);
                var registered=net.minecraft.core.registries.BuiltInRegistries.ITEM;
                var ids=registered.keySet().stream().filter(id->id.getNamespace().equals("aeronautics")&&id.getPath().contains("envelope")).sorted(java.util.Comparator.comparing(Identifier::toString)).toList();
                Identifier envelope=ids.stream().filter(id->id.getPath().equals("white_envelope")).findFirst().orElseThrow();
                for(Identifier component:List.of(envelope,Identifier.parse("aeronautics:adjustable_burner"),Identifier.parse("aeronautics:steam_vent"))){
                    require(PonderIndex.getSceneAccess().doScenesExistForId(component),"Missing Ponder component "+component);
                    for(var scene:PonderIndex.getSceneAccess().compile(component))
                        if(java.util.Set.of("envelope","hot_air_burner","steam_vent").contains(scene.getSceneId().getPath())&&scenes.stream().noneMatch(s->s.getSceneId().equals(scene.getSceneId())))scenes.add(scene);
                }
                require(scenes.size()==3,"Expected envelope, burner and steam-vent scenes; got "+scenes.stream().map(PonderScene::getSceneId).toList());
                result.put("scene_ids",scenes.stream().map(s->s.getSceneId().toString()).toList());open(mc);return;
            }
            if(sceneIndex>=scenes.size()){requestFinish(mc);return;}
            require(mc.gui.screen()==ui,"Ponder closed unexpectedly");
            if(++waitTicks<10)return;
            require(ui.frames>framesBefore,"No real GUI extraction since the last seek");
            PonderScene scene=ui.getActiveScene();
            var sample=new LinkedHashMap<String,Object>();sample.put("scene",scene.getSceneId().toString());sample.put("timeline_tick",scene.getCurrentTime());sample.put("extracted_gui_frames",ui.frames-framesBefore);samples.add(sample);
            result.put("samples",samples);result.put("active_timeline_tick",scene.getCurrentTime());write();
            int next=Math.min(scene.getTotalTime()-1,Math.max(sampleTime,scene.getCurrentTime())+30);
            if(next>scene.getCurrentTime()&&!scene.isFinished()){
                sampleTime=next;scene.seekToTime(next);framesBefore=ui.frames;waitTicks=0;return;
            }
            Screenshot.grab(mc,false);System.out.println("CREATE263_PIP_SCENE_RENDERED "+scene.getSceneId()+" frames="+ui.frames);
            sceneIndex++;
            if(sceneIndex<scenes.size()){open(mc);return;}
            result.put("completed_scenes",sceneIndex);result.put("sample_count",samples.size());result.put("render_checks_passed",true);write();requestFinish(mc);
        }catch(Throwable error){
            error.printStackTrace();result.put("passed",false);result.put("error",error.toString());failed=true;
            try{write();}catch(Exception secondary){secondary.printStackTrace();}
        }
    }
    private static final class ProbeUI extends PonderUI {
        private int frames;
        ProbeUI(List<PonderScene> scenes){super(scenes);}
        @Override public void extractScaledRenderState(GuiGraphicsExtractor graphics,int x,int y,float partialTicks){super.extractScaledRenderState(graphics,x,y,partialTicks);frames++;}
    }
}
