package dev.create263.probe;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import net.minecraft.core.BlockPos;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.MissingItemModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/** Actual client model resolution and rendered galleries; only used by the owned test instance. */
final class FiveAddonsClientProbe {
    private static final Set<String> MODS=Set.of("electroenergetics","fxntstorage","fluidlogistics",
        "createadditionallogistics","create_dragons_plus","create_enchantment_industry");
    private static final Map<String,Object> result=new LinkedHashMap<>();
    private static final List<ItemStack> items=new ArrayList<>();
    private static final List<String> renderedPages=new ArrayList<>();
    private static Gallery gallery;
    private static int page,pageTicks,fixtureTicks;
    private static JsonArray fixtures;
    private static boolean fixturesRendered,galleryComplete;
    private static boolean started,finished;
    private static final int PAGE_SIZE=54;
    static boolean enabled(){return Boolean.getBoolean("create263.clientFiveAddons");}
    static boolean finished(){return !enabled()||finished;}
    static Map<String,Object> report(){return Map.copyOf(result);}
    private static void require(boolean test,String message){if(!test)throw new IllegalStateException(message);}
    private static void write()throws Exception{
        Path root=Path.of(System.getProperty("create263.clientProbeRoot")).toAbsolutePath().normalize();
        require(root.equals(net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().toAbsolutePath().normalize()),"Gallery game directory mismatch");
        require(Files.isRegularFile(root.resolve(".create263-disposable-runtime")),"Gallery marker missing");
        Files.writeString(root.resolve("five-addons-client-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
    }
    static void tick(Minecraft mc)throws Exception{
        if(!enabled()||finished)return;
        if(galleryComplete){
            StorageClientMenuProbe.tick(mc);
            if(StorageClientMenuProbe.finished()){
                result.put("storage_actual_menu_protocol",StorageClientMenuProbe.report());
                result.put("passed",true);write();finished=true;
            }
            return;
        }
        if(!started){
            started=true;
            var counts=new LinkedHashMap<String,Integer>();
            for(String id:MODS){require(ModList.get().isLoaded(id),"Missing client add-on "+id);counts.put(id,0);}
            var resolved=new LinkedHashMap<String,String>();
            for(var item:BuiltInRegistries.ITEM){
                Identifier id=BuiltInRegistries.ITEM.getKey(item);
                if(!MODS.contains(id.getNamespace()))continue;
                ItemStack stack=item.getDefaultInstance();
                Identifier modelId=stack.getOrDefault(DataComponents.ITEM_MODEL,id);
                var model=mc.getModelManager().getItemModel(modelId);
                require(model!=null&&!(model instanceof MissingItemModel),"Missing baked item model "+id+" -> "+modelId);
                for(var display:List.of(ItemDisplayContext.GUI,ItemDisplayContext.GROUND,ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)){
                    ItemStackRenderState state=new ItemStackRenderState();
                    mc.getItemModelResolver().updateForTopItem(state,stack,display,mc.level,mc.player,0);
                    require(!state.isEmpty(),"Empty item render state "+id+" in "+display);
                }
                items.add(stack);counts.compute(id.getNamespace(),(key,count)->count+1);
                resolved.put(id.toString(),model.getClass().getName());
            }
            for(var count:counts.entrySet())require(count.getValue()>0,"No registered items for "+count.getKey());
            result.put("registered_item_counts",counts);result.put("baked_models",resolved);
            result.put("resolved_display_contexts",List.of("GUI","GROUND","THIRD_PERSON_RIGHT_HAND"));
            result.put("model_resolution_passed",true);write();
            Path root=Path.of(System.getProperty("create263.clientProbeRoot")).toAbsolutePath().normalize();
            var serverReport=JsonParser.parseString(Files.readString(root.resolve("server-five-addons-probe-result.json"))).getAsJsonObject();
            require(serverReport.get("passed").getAsBoolean(),"Copied server fixtures did not pass gameplay checks");
            fixtures=serverReport.getAsJsonArray("client_fixtures");
            require(fixtures!=null&&fixtures.size()>=10,"Incomplete server fixture manifest");
            var server=mc.getSingleplayerServer();var playerId=mc.player.getUUID();
            server.execute(()->{
                var player=server.getPlayerList().getPlayer(playerId);
                if(player!=null)player.teleportTo(server.overworld(),197,-51,27,Set.of(),180,24,true);
            });
            return;
        }
        if(!fixturesRendered){
            if(++fixtureTicks<60)return;
            var rendererClasses=new LinkedHashMap<String,String>();
            for(var value:fixtures){
                var fixture=value.getAsJsonObject();
                BlockPos pos=new BlockPos(fixture.get("x").getAsInt(),fixture.get("y").getAsInt(),fixture.get("z").getAsInt());
                var be=mc.level.getBlockEntity(pos);
                require(be!=null,"Client fixture missing at "+pos);
                require(be.getClass().getName().equals(fixture.get("block_entity_class").getAsString()),"Client fixture class differs at "+pos);
                var renderer=mc.getBlockEntityRenderDispatcher().getRenderer(be);
                // These Electro entities intentionally use static block models and
                // their common world wire renderer; upstream registers no BER.
                if(renderer==null)require(Set.of("CreativeBatteryBlockEntity","ConverterBlockEntity").contains(be.getClass().getSimpleName()),"Missing fixture BER for "+be.getClass().getName());
                rendererClasses.put(pos.toShortString(),renderer==null?"static block model and shared world wire renderer":renderer.getClass().getName());
            }
            Screenshot.grab(mc,false);result.put("client_fixture_renderers",rendererClasses);
            result.put("fixture_world_frames_before_screenshot",fixtureTicks);
            fixturesRendered=true;gallery=new Gallery(page);mc.gui.setScreen(gallery);write();return;
        }
        require(mc.gui.screen()==gallery,"Client item gallery closed unexpectedly");
        if(++pageTicks<35)return;
        require(gallery.frames>=5,"Item gallery did not render five frames: "+page);
        Screenshot.grab(mc,false);
        renderedPages.add("page-"+(page+1)+"-frames-"+gallery.frames);
        if((page+1)*PAGE_SIZE<items.size()){
            page++;pageTicks=0;gallery=new Gallery(page);mc.gui.setScreen(gallery);return;
        }
        result.put("rendered_pages",List.copyOf(renderedPages));
        result.put("actual_gui_item_draws",items.size());
        result.put("status","model-gallery-complete-testing-storage-menu");
        galleryComplete=true;write();mc.gui.setScreen(null);
    }
    private static final class Gallery extends Screen {
        private final int page;
        private int frames;
        Gallery(int page){super(Component.literal("Create 26.3 addon model validation"));this.page=page;}
        @Override public boolean isPauseScreen(){return false;}
        @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partialTicks){
            graphics.fill(0,0,width,height,0xff20242c);
            graphics.text(font,title,12,10,0xffffffff,false);
            graphics.text(font,"Page "+(page+1)+" / "+((items.size()+PAGE_SIZE-1)/PAGE_SIZE),12,25,0xffccddee,false);
            int cellWidth=Math.max(28,(width-24)/9),cellHeight=Math.max(25,(height-50)/6);
            for(int cell=0;cell<PAGE_SIZE;cell++){
                int index=page*PAGE_SIZE+cell;if(index>=items.size())break;
                int x=12+(cell%9)*cellWidth,y=44+(cell/9)*cellHeight;
                graphics.item(items.get(index),x,y);graphics.itemDecorations(font,items.get(index),x,y);
                graphics.text(font,Integer.toString(index),x,y+17,0xffc0c0c0,false);
            }
            frames++;
        }
    }
}
