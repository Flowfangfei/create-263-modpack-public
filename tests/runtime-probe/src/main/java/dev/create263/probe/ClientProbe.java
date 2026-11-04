package dev.create263.probe;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.api.client.element.PonderElement;
import net.createmod.ponder.api.client.scene.PonderScene;
import net.createmod.ponder.impl.client.gui.PonderUI;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.apache.logging.log4j.LogManager;

/** Separate client-only class; the dedicated-server entry point never loads it. */
final class ClientProbe {
    private static final String WORLD="three-addon-test-copy";
    private static final Path ROOT=Path.of(System.getProperty("create263.clientProbeRoot")).toAbsolutePath().normalize();
    private static final Map<String,Object> result=new LinkedHashMap<>();
    private static final Map<String,String> renderers=new LinkedHashMap<>();
    private static int ticks,afterDisconnect,settingsFrames;
    private static List<PonderScene> hydraulicScenes;
    private static ProbePonderUI ponderScreen;
    private static volatile String asynchronousFailure;
    private static boolean disconnecting,finished;
    private static final org.apache.logging.log4j.Logger LOGGER=LogManager.getLogger("Create263ClientProbe");
    static void install(){
        require(ROOT.equals(FMLPaths.GAMEDIR.get().toAbsolutePath().normalize()),"Client game directory mismatch");
        require(Files.isRegularFile(ROOT.resolve(".create263-disposable-runtime")),"Client test marker missing");
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class,event->tick());
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Post.class,event->{
            if(event.getScreen().getClass().getName().equals("com.enxv.aerouniversaljoint.client.HydraulicConnectionHeadScreen"))settingsFrames++;
        });
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private static void write()throws Exception{Files.writeString(ROOT.resolve("joint-client-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");}
    private static void camera(Minecraft mc,double x,double y,double z,float yaw,float pitch){
        var server=mc.getSingleplayerServer();var id=mc.player.getUUID();
        server.execute(()->{var player=server.getPlayerList().getPlayer(id);if(player==null)return;
            player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.CREATIVE);
            player.getAbilities().flying=true;player.onUpdateAbilities();
            player.teleportTo(server.overworld(),x,y,z,Set.of(),yaw,pitch,true);
        });
    }
    private static void renderer(Minecraft mc,BlockPos pos){
        var be=mc.level.getBlockEntity(pos);require(be!=null,"Client block entity missing at "+pos);
        var renderer=mc.getBlockEntityRenderDispatcher().getRenderer(be);require(renderer!=null,"Client BER missing for "+be.getClass().getName());
        renderers.put(be.getClass().getSimpleName()+"@"+pos.toShortString(),renderer.getClass().getName());
    }
    private static PonderScene scene(String name){
        return hydraulicScenes.stream().filter(scene->scene.getSceneId().getPath().equals(name)).findFirst()
            .orElseThrow(()->new IllegalStateException("T&L Ponder scene missing: "+name));
    }
    private static void openPonder(Minecraft mc,String name){
        ponderScreen=new ProbePonderUI(List.of(scene(name)));mc.gui.setScreen(ponderScreen);
        LOGGER.info("CREATE263_TL_PONDER_OPEN {}",name);
    }
    private static void assertPonder(Minecraft mc,String name,boolean overlay){
        require(mc.gui.screen()==ponderScreen,"Ponder screen closed before rendering "+name);
        require(ponderScreen.frames>=5,"Ponder has not completed five real GUI extraction frames: "+name);
        var visible=new java.util.LinkedHashSet<String>();
        ponderScreen.getActiveScene().forEachVisible(PonderElement.class,element->visible.add(element.getClass().getSimpleName()));
        require(visible.contains("HydraulicRodElement"),"Ponder custom hydraulic rod not visible: "+visible);
        if(overlay)require(visible.contains("HydraulicSettingsOverlayElement"),"Ponder hydraulic settings overlay not visible: "+visible);
        result.put(name+"_render_frames",ponderScreen.frames);result.put(name+"_visible_elements",visible);
        result.put(name+"_rendered",true);
        Screenshot.grab(mc,false);LOGGER.info("CREATE263_CLIENT_SCREENSHOT_TL_PONDER_{}",name.toUpperCase(java.util.Locale.ROOT));
    }
    private static final class ProbePonderUI extends PonderUI {
        private int frames;
        private ProbePonderUI(List<PonderScene> scenes){super(scenes);}
        @Override public void extractScaledRenderState(GuiGraphicsExtractor graphics,int x,int y,float partialTicks){
            super.extractScaledRenderState(graphics,x,y,partialTicks);frames++;
        }
    }
    private static void openLiveHydraulicSettings(Minecraft mc){
        var server=mc.getSingleplayerServer();var playerId=mc.player.getUUID();
        server.execute(()->{try{
            var player=server.getPlayerList().getPlayer(playerId);
            var head=server.overworld().getBlockEntity(new BlockPos(128,-60,8));
            require(player!=null&&head!=null,"Live hydraulic settings endpoint missing");
            Class<?> headType=Class.forName("com.enxv.aerouniversaljoint.content.HydraulicConnectionHeadBlockEntity");
            Class<?> menuType=Class.forName("com.enxv.aerouniversaljoint.content.HydraulicConnectionHeadMenu");
            menuType.getMethod("open",net.minecraft.world.entity.player.Player.class,headType,boolean.class).invoke(null,player,head,true);
        }catch(Throwable failure){asynchronousFailure=failure.toString();}});
    }
    private static void tick(){
        if(finished)return;var mc=Minecraft.getInstance();
        try{
            require(asynchronousFailure==null,"Client probe server callback failed: "+asynchronousFailure);
            if(disconnecting){
                if(!(mc.gui.screen() instanceof TitleScreen)||mc.level!=null||mc.getSingleplayerServer()!=null||mc.gui.overlay()!=null){afterDisconnect=0;return;}
                if(++afterDisconnect==40){
                    result.put("saved_and_returned_to_title",true);result.put("passed",true);write();
                    LOGGER.info("CREATE263_JOINT_CLIENT_SAVE_OK");
                }
                if(afterDisconnect>=80){finished=true;mc.stop();}return;
            }
            if(mc.level==null||mc.player==null||mc.gui.overlay()!=null)return;
            var server=mc.getSingleplayerServer();require(server!=null,"Client test requires the owned integrated server");
            require(server.getWorldPath(LevelResource.ROOT).toRealPath().equals(ROOT.resolve("saves").resolve(WORLD).toRealPath()),"Client world path mismatch");
            if(++ticks==1){
                for(String mod:List.of("create","sable","railways","create_connected","create_connected_simulated","createdieselgenerators","simulated","offroad","aeronautics","createliquidfuel","createoreexcavation","aeronautics_utility_objects"))
                    require(ModList.get().isLoaded(mod),"Client missing runtime mod "+mod);
                result.put("all_twelve_mods_loaded",true);LOGGER.info("CREATE263_JOINT_CLIENT_WORLD_LOADED");
            }
            if(ticks==20)camera(mc,10,-54,17,166,24);
            if(ticks==100){
                for(int x:new int[]{0,2,4,6})renderer(mc,new BlockPos(x,-60,0));
                renderer(mc,new BlockPos(8,-60,8));
                Screenshot.grab(mc,false);LOGGER.info("CREATE263_CLIENT_SCREENSHOT_LIQUID_AND_LINKAGE");
            }
            if(ticks==130)camera(mc,80,-50,26,180,25);
            if(ticks==220){
                renderer(mc,new BlockPos(72,-58,8));renderer(mc,new BlockPos(88,-58,8));
                Screenshot.grab(mc,false);LOGGER.info("CREATE263_CLIENT_SCREENSHOT_ORE");
            }
            if(ticks==250)camera(mc,142,-51,28,180,23);
            if(ticks==340){
                Screenshot.grab(mc,false);LOGGER.info("CREATE263_CLIENT_SCREENSHOT_PHYSICS_LINKAGE");
                result.put("renderers",renderers);result.put("world_loaded",true);write();
            }
            if(ticks==380){
                Identifier rod=Identifier.fromNamespaceAndPath("aeronautics_utility_objects","hydraulic_rod");
                require(PonderIndex.getSceneAccess().doScenesExistForId(rod),"Hydraulic rod Ponder registration missing");
                hydraulicScenes=PonderIndex.getSceneAccess().compile(rod);
                require(hydraulicScenes.size()>=2,"Hydraulic rod Ponder must retain properties and settings scenes");
                result.put("hydraulic_rod_ponder_scene_ids",hydraulicScenes.stream().map(scene->scene.getSceneId().toString()).toList());
                openPonder(mc,"hydraulic_rod_properties");
            }
            if(ticks==400)ponderScreen.seekToTime(100);
            if(ticks==460)assertPonder(mc,"hydraulic_rod_properties",false);
            if(ticks==480)openPonder(mc,"hydraulic_rod_settings");
            if(ticks==500)ponderScreen.seekToTime(80);
            if(ticks==560)assertPonder(mc,"hydraulic_rod_settings",true);
            if(ticks==580){
                result.put("ponder_ui_rendered",true);mc.gui.setScreen(null);
                camera(mc,128,-58,12,180,15);LOGGER.info("CREATE263_TL_PONDER_RENDER_OK");
            }
            if(ticks==620)openLiveHydraulicSettings(mc);
            if(ticks==680){
                require(mc.gui.screen()!=null&&mc.gui.screen().getClass().getName().equals("com.enxv.aerouniversaljoint.client.HydraulicConnectionHeadScreen"),"Live hydraulic slider screen did not open");
                require(settingsFrames>=5,"Live hydraulic slider screen did not render five GUI frames");
                result.put("hydraulic_settings_screen_render_frames",settingsFrames);
                result.put("hydraulic_settings_screen_rendered",true);
                Screenshot.grab(mc,false);LOGGER.info("CREATE263_CLIENT_SCREENSHOT_TL_LIVE_HYDRAULIC_SETTINGS");write();
            }
            if(ticks==720){mc.player.closeContainer();mc.gui.setScreen(null);}
            if(ticks>=750)FiveAddonsClientProbe.tick(mc);
            if(ticks>=760&&FiveAddonsClientProbe.finished()){
                if(FiveAddonsClientProbe.enabled())result.put("five_addons_client_checks",FiveAddonsClientProbe.report());
                disconnecting=true;mc.disconnectWithSavingScreen();mc.gui.setScreen(new TitleScreen());
            }
        }catch(Throwable error){
            error.printStackTrace();result.put("passed",false);result.put("error",error.toString());
            try{write();}catch(Exception secondary){secondary.printStackTrace();}
            finished=true;mc.stop();
        }
    }
}
