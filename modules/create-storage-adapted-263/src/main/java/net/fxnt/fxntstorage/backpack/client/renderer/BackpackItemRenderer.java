package net.fxnt.fxntstorage.backpack.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import java.util.function.Consumer;
import net.fxnt.fxntstorage.backpack.upgrade.workshop.WorkshopFlywheelRenderer;

public final class BackpackItemRenderer implements SpecialModelRenderer<Item> {
    @Override public Item extractArgument(ItemStack stack) { return stack.getItem(); }
    @Override public void submit(Item backpack,PoseStack pose,SubmitNodeCollector collector,int light,int overlay,boolean foil,int outline) {
        if(backpack==null) return;
        var geometry=new net.fxnt.fxntstorage.client.StorageRenderBuffers.GeometryCollector(collector);
        WorkshopFlywheelRenderer.render(backpack,false,pose,geometry.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock()),light,0);
        geometry.submit();
    }
    @Override public void getExtents(Consumer<Vector3fc> output) { output.accept(new org.joml.Vector3f(0,0,0));output.accept(new org.joml.Vector3f(1,1,1)); }
}
