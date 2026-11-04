package dev.simulated_team.simulated.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.ArrayList;
import java.util.function.BiConsumer;

/** PIP bridge for the current port's reusable and animated partial-model GUI previews. */
public final class GuiGameElement {
    public static GuiPartialRenderBuilder of(PartialModel partial){return new GuiPartialRenderBuilder(partial);}
    public static GuiBlockStateRenderBuilder of(BlockState state){return new GuiBlockStateRenderBuilder(state);}
    public static void registerRenderers(RegisterPictureInPictureRenderersEvent event){event.register(PreviewState.class,PreviewRenderer::new);}
    public abstract static class Builder<T extends Builder<T>> {
        protected float x,y,scale=1,rx,ry,rz;protected int padding;protected BiConsumer<PoseStack,Float> transform;
        protected T self(){return (T)this;}
        public T at(float x,float y){this.x=x;this.y=y;return self();}
        public T at(float x,float y,float z){return at(x,y);}
        public T scale(double scale){this.scale=(float)scale;return self();}
        public T padding(int padding){this.padding=padding;return self();}
        public T rotate(double x,double y,double z){rx=(float)x;ry=(float)y;rz=(float)z;return self();}
        public T transform(BiConsumer<PoseStack,Float> transform){this.transform=transform;return self();}
        protected abstract List<BlockStateModelPart> parts();
        public void clear(){markDirty();}
        public abstract void markDirty();
        public void render(GuiGraphicsExtractor graphics){
            PoseStack model=new PoseStack();
            if(transform!=null)transform.accept(model,Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
            else {model.translate(.5,.5,0);model.rotate(Axis.XP.rotationDegrees(rx));model.rotate(Axis.YP.rotationDegrees(ry));model.rotate(Axis.ZP.rotationDegrees(rz));model.scale(1,-1,1);model.translate(-.5,-.5,-.5);}
            int size=(int)Math.ceil(16*scale)+padding;
            var pose=new Matrix3x2f(graphics.pose()).translate(x,y);
            var scissor=graphics.peekScissorStack();
            var bounds=new ScreenRectangle(0,0,size,size).transformMaxBounds(pose);
            if(scissor!=null)bounds=bounds.intersection(scissor);
            graphics.submitPictureInPictureRenderState(new PreviewState(parts(),new Matrix4f(model.last().pose()),pose,0,0,size,size,scale,scissor,bounds));
        }
    }
    public static final class GuiPartialRenderBuilder extends Builder<GuiPartialRenderBuilder>{
        private PartialModel partial;private List<BlockStateModelPart> cached;
        private GuiPartialRenderBuilder(PartialModel partial){this.partial=partial;}
        public GuiPartialRenderBuilder partial(PartialModel value){if(value!=partial){partial=value;markDirty();}return this;}
        public void markDirty(){cached=null;}
        protected List<BlockStateModelPart> parts(){if(cached==null)cached=List.of(partial.get());return cached;}
    }
    public static final class GuiBlockStateRenderBuilder extends Builder<GuiBlockStateRenderBuilder>{
        private final BlockState state;private List<BlockStateModelPart> cached;
        private GuiBlockStateRenderBuilder(BlockState state){this.state=state;}
        public void markDirty(){cached=null;}
        protected List<BlockStateModelPart> parts(){if(cached==null){var list=new ArrayList<BlockStateModelPart>();Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state).collectParts(RandomSource.create(42),list);cached=List.copyOf(list);}return cached;}
    }
    public record PreviewState(List<BlockStateModelPart> parts,Matrix4f model,Matrix3x2f pose,
        int x0,int y0,int x1,int y1,float scale,@Nullable ScreenRectangle scissorArea,@Nullable ScreenRectangle bounds) implements PictureInPictureRenderState {}
    public static final class PreviewRenderer extends PictureInPictureRenderer<PreviewState>{
        public Class<PreviewState> getRenderStateClass(){return PreviewState.class;}
        protected String getTextureLabel(){return "simulated:partial_preview";}
        protected void renderToTexture(PreviewState state,PoseStack pose,SubmitNodeCollector collector){
            pose.scale(1,1,-1);pose.mulPose(state.model());
            for(var part:state.parts())collector.submitBlockModel(pose,(part.materialFlags()&1)!=0?Sheets.translucentBlockItemSheet():Sheets.cutoutBlockItemSheet(),List.of(part),new int[0],15728880,OverlayTexture.NO_OVERLAY,0);
        }
    }
}
