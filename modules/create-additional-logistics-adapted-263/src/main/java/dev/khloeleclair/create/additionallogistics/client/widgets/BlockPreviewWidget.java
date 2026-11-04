package dev.khloeleclair.create.additionallogistics.client.widgets;

import com.mojang.math.Axis;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/** Interactive six-face shaft configuration using the 26.3 GUI model submission pipeline.
 * Retains the upstream EnderIO/Patchouli ray transform and interaction behavior.
 * Original immediate-mode source and attribution remain in tooling/upstream-rendering/widgets.
 */
public class BlockPreviewWidget extends AbstractWidget {
    private static final Quaternionf ROT_180_Z = Axis.ZP.rotation((float)Math.PI);
    private static final Vec3 RAY_ORIGIN = new Vec3(1.5, 1.5, 1.5);
    private static final Vec3 RAY_START = new Vec3(1.5, 1.5, -1);
    private static final Vec3 RAY_END = new Vec3(1.5, 1.5, 3);
    private static final BlockPos RAY_POS = new BlockPos(1, 1, 1);
    private final Minecraft minecraft = Minecraft.getInstance();
    private final BlockPos position;
    private final Vector3f worldOrigin;
    private final List<BlockPos> neighbors = new ArrayList<>();
    private float scale = 80;
    private float pitch;
    private float yaw;
    private boolean didClick;
    private boolean neighborsVisible = true;
    private @Nullable Predicate<Direction> canSelectDirection;
    private @Nullable BiConsumer<Direction, Integer> clickedDirection;
    private @Nullable Direction selection;

    public BlockPreviewWidget(int x, int y, int width, int height, BlockPos position) {
        super(x, y, width, height, Component.empty());
        this.position = position;
        worldOrigin = new Vector3f(position.getX()+.5f, position.getY()+.5f, position.getZ()+.5f);
        for (Direction direction : Direction.values()) neighbors.add(position.relative(direction));
        if (minecraft.player != null) { pitch = minecraft.player.getXRot(); yaw = minecraft.player.getYRot(); }
    }
    public BlockPreviewWidget canSelectDirection(@Nullable Predicate<Direction> predicate) { canSelectDirection = predicate; return this; }
    public BlockPreviewWidget onClick(@Nullable BiConsumer<Direction, Integer> consumer) { clickedDirection = consumer; return this; }
    public void toggleNeighborVisibility() { neighborsVisible = !neighborsVisible; }
    @Override public void mouseMoved(double x, double y) { didClick = false; super.mouseMoved(x, y); }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { didClick = isMouseOver(event.x(), event.y()); return didClick; }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        boolean handled = active && visible && didClick && selection != null && clickedDirection != null;
        if (handled) clickedDirection.accept(selection, event.button());
        didClick = false;
        return handled;
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        didClick = false;
        if (!visible || !isMouseOver(event.x(), event.y())) return false;
        yaw += 4 * (float)(dx / minecraft.getWindow().getGuiScaledWidth()) * 180;
        pitch = Math.clamp(pitch + 2 * (float)(dy / minecraft.getWindow().getGuiScaledHeight()) * 180, -80, 80);
        return true;
    }
    @Override public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (!visible || !isMouseOver(x,y)) return false;
        scale = Math.clamp(scale-(float)scrollY, 10, 160);
        return true;
    }
    private static Vec3 transform(Vec3 vector, Matrix4f transform) {
        Vector4f value = new Vector4f((float)(vector.x-RAY_ORIGIN.x), (float)(vector.y-RAY_ORIGIN.y), (float)(vector.z-RAY_ORIGIN.z), 1);
        value.mul(transform);
        return new Vec3(value.x+RAY_ORIGIN.x, value.y+RAY_ORIGIN.y, value.z+RAY_ORIGIN.z);
    }
    @Override protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        if (!visible || minecraft.level == null) return;
        int centerX = getX()+width/2, centerY = getY()+height/2;
        Quaternionf rotPitch = Axis.XN.rotationDegrees(pitch), rotYaw = Axis.YP.rotationDegrees(yaw);
        Matrix4f rayTransform = new Matrix4f().rotation(ROT_180_Z).rotate(rotYaw).rotate(rotPitch);
        float dx = (mouseX-centerX)/scale, dy = (mouseY-centerY)/scale;
        BlockHitResult hit = Shapes.block().move(-.5,-.5,-.5).clip(transform(RAY_START.add(dx,dy,0),rayTransform), transform(RAY_END.add(dx,dy,0),rayTransform), RAY_POS);
        selection = hit == null || (canSelectDirection != null && !canSelectDirection.test(hit.getDirection())) ? null : hit.getDirection();
        graphics.enableScissor(getX(),getY(),getX()+width,getY()+height);
        if (neighborsVisible) for (BlockPos neighbor : neighbors) submitBlock(graphics,neighbor,centerX,centerY,.35f);
        submitBlock(graphics,position,centerX,centerY,1);
        if (selection != null) {
            List<BlockStateModelPart> parts = new ArrayList<>();
            minecraft.getModelManager().getBlockStateModelSet().get(minecraft.level.getBlockState(position)).collectParts(RandomSource.create(42),parts);
            Direction face = selection;
            for (BlockStateModelPart part : parts) {
                BlockStateModelPart highlight = new BlockStateModelPart() {
                    @Override public List<BakedQuad> getQuads(@Nullable Direction side) { return side == null ? part.getQuads(face) : List.of(); }
                    @Override public boolean useAmbientOcclusion() { return false; }
                    @Override public Material.Baked particleMaterial() { return part.particleMaterial(); }
                    @Override public int materialFlags() { return part.materialFlags(); }
                };
                GuiGameElement.of(highlight).at(centerX,centerY,101).atLocal(-.5f,-.5f,-.5f).rotate(-pitch,yaw,180).scale(scale*1.002).color(0xB0FFFF40).submit(graphics);
            }
            graphics.text(minecraft.font, Component.literal(face.getSerializedName()), getX()+4, getY()+height-14, 0xFFFFFF40);
        }
        graphics.disableScissor();
    }
    private void submitBlock(GuiGraphicsExtractor graphics, BlockPos pos, int x, int y, float alpha) {
        var state = minecraft.level.getBlockState(pos);
        GuiGameElement.of(state, minecraft.level.getBlockEntity(pos)).at(x,y,100)
            .atLocal(pos.getX()-worldOrigin.x,pos.getY()-worldOrigin.y,pos.getZ()-worldOrigin.z)
            .rotate(-pitch,yaw,180).scale(scale).withAlpha(alpha).submit(graphics);
    }
    @Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
}
