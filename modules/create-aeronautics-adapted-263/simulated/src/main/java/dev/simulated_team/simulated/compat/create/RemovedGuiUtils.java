package dev.simulated_team.simulated.compat.create;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.FormattedText;
/** Uses the target tooltip queue; custom legacy border colors use the current game theme. */
public final class RemovedGuiUtils {
    public static final int DEFAULT_BACKGROUND_COLOR=0xF0100010,DEFAULT_BORDER_COLOR_START=0x505000FF,DEFAULT_BORDER_COLOR_END=0x5028007F;
    public static void drawHoveringText(GuiGraphicsExtractor graphics,List<? extends FormattedText> lines,int x,int y,int background,int top,int bottom,Font font){
        graphics.setComponentTooltipForNextFrame(font,lines,x,y,net.minecraft.world.item.ItemStack.EMPTY);
    }
    public static void drawHoveringText(GuiGraphicsExtractor graphics,List<? extends FormattedText> lines,int x,int y,int screenWidth,int screenHeight,int maxWidth,int background,int top,int bottom,Font font){
        drawHoveringText(graphics,lines,x,y,background,top,bottom,font);
    }
}
