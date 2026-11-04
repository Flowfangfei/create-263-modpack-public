package dev.create263.probe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Exercises the real client/server menu protocol in the guarded disposable client world. */
final class StorageClientMenuProbe {
    private static final BlockPos POS=new BlockPos(176,-60,8);
    private static final Map<String,Object> result=new LinkedHashMap<>();
    private static int phase,ticks,containerId;
    private static ItemStack prototype;
    private static CompletableFuture<Map<String,Object>> serverState;
    private static boolean finished;
    static boolean finished(){return finished;}
    static Map<String,Object> report(){return Map.copyOf(result);}
    private static void require(boolean test,String message){if(!test)throw new IllegalStateException(message);}
    private static void same(ItemStack stack,int count,String label){
        require(stack.getCount()==count,label+" quantity expected "+count+" actual "+stack.getCount());
        if(count>0)require(ItemStack.isSameItemSameComponents(prototype,stack),label+" components changed");
    }
    static void tick(Minecraft mc){
        if(finished)return;
        require(Boolean.getBoolean("create263.clientFiveAddons"),"Missing explicit client probe flag");
        require(mc.player!=null&&mc.level!=null&&mc.gameMode!=null,"Client world unavailable during menu test");
        if(++ticks>240)throw new IllegalStateException("Storage menu phase timed out: "+phase);
        if(phase==0){
            mc.gui.setScreen(null);
            var server=mc.getSingleplayerServer();require(server!=null,"Menu test requires owned integrated server");
            var id=mc.player.getUUID();
            server.execute(()->{
                var player=server.getPlayerList().getPlayer(id);
                if(player==null)throw new IllegalStateException("Owned test player missing");
                player.closeContainer();player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                player.teleportTo(server.overworld(),176.5,-60,11.5,Set.of(),180,0,true);
            });
            phase=1;ticks=0;return;
        }
        if(phase==1){
            if(ticks<40)return;
            require(mc.level.getBlockEntity(POS)!=null,"Backpack test fixture not loaded");
            mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(POS).add(0,0,0.5),Direction.SOUTH,POS,false));
            phase=2;ticks=0;return;
        }
        if(phase==2){
            if(ticks<30)return;
            var menu=mc.player.containerMenu;
            require(menu.getClass().getName().equals("net.fxnt.fxntstorage.backpack.client.menu.BackpackMenu"),"Native interaction did not open BackpackMenu: "+menu.getClass().getName());
            require(mc.gui.screen()!=null&&mc.gui.screen().getClass().getSimpleName().equals("BackpackScreen"),"Real BackpackScreen missing");
            require(menu.getCarried().isEmpty(),"Unexpected carried item before private menu test");
            prototype=menu.getSlot(0).getItem().copyWithCount(1);require(!prototype.isEmpty(),"Private menu slot vanished during initial sync");
            same(menu.getSlot(0).getItem(),1024,"Initial private menu slot");
            containerId=menu.containerId;result.put("initial_private_quantity",1024);Screenshot.grab(mc,false);
            mc.gameMode.handleContainerInput(containerId,0,0,ContainerInput.PICKUP,mc.player);
            phase=3;ticks=0;return;
        }
        if(phase==3){
            if(ticks<25)return;
            var menu=mc.player.containerMenu;require(menu.containerId==containerId,"Menu identity changed during take");
            same(menu.getCarried(),64,"Native cursor after take");same(menu.getSlot(0).getItem(),960,"Private slot after take");
            result.put("native_cursor_quantity",64);result.put("private_quantity_after_take",960);Screenshot.grab(mc,false);
            mc.gameMode.handleContainerInput(containerId,0,0,ContainerInput.PICKUP,mc.player);
            phase=4;ticks=0;return;
        }
        if(phase==4){
            if(ticks<25)return;
            var menu=mc.player.containerMenu;require(menu.containerId==containerId,"Menu identity changed during return");
            require(menu.getCarried().isEmpty(),"Native cursor retained items after return");same(menu.getSlot(0).getItem(),1024,"Private slot after return");
            var server=mc.getSingleplayerServer();var id=mc.player.getUUID();ItemStack expected=prototype.copy();
            serverState=new CompletableFuture<>();server.execute(()->{
                try{
                    var player=server.getPlayerList().getPlayer(id);require(player!=null,"Server test player missing");
                    require(player.containerMenu.containerId==containerId,"Server menu identity differs");
                    ItemStack stored=player.containerMenu.getSlot(0).getItem();
                    require(stored.getCount()==1024&&ItemStack.isSameItemSameComponents(expected,stored),"Server inventory did not conserve the actual clicked items/components");
                    require(player.containerMenu.getCarried().isEmpty(),"Server carried stack not empty");
                    serverState.complete(Map.of("private_quantity",stored.getCount(),"cursor_empty",true,"components",true));
                }catch(Throwable failure){serverState.completeExceptionally(failure);}
            });
            phase=5;ticks=0;return;
        }
        if(phase==5){
            if(!serverState.isDone())return;
            result.put("server_confirmed_after_native_clicks",serverState.join());
            result.put("actual_block_interaction",true);result.put("actual_client_screen",true);
            result.put("native_container_click_protocol",true);result.put("quantity_and_components_conserved",true);
            Screenshot.grab(mc,false);mc.player.closeContainer();phase=6;ticks=0;return;
        }
        if(phase==6){
            StorageClientCompactionProbe.tick(mc);
            if(!StorageClientCompactionProbe.finished())return;
            var compaction=StorageClientCompactionProbe.report();
            require(Boolean.TRUE.equals(compaction.get("passed")),"Compaction request/reply failed");
            result.put("actual_compaction_request_reply",compaction);
            result.put("passed",true);finished=true;
        }
    }
}
