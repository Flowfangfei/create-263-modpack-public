package com.yision.fluidlogistics.client.gui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.createmod.catnip.api.lang.LangBuilder;
/** Preserves Create translation, status messages and exact colors through current native text APIs. */
public final class FluidClientLang extends com.simibubi.create.foundation.utility.CreateLang {
    private static Builder wrap(LangBuilder value) {return new Builder().add(value.component());}
    public static Builder builder(){return new Builder();}
    public static Builder translate(String key,Object... args){return wrap(com.simibubi.create.foundation.utility.CreateLang.translate(key,args));}
    public static Builder text(String text){return new Builder().text(text);}
    public static Builder number(double number){return wrap(com.simibubi.create.foundation.utility.CreateLang.number(number));}
    public static Builder itemName(net.minecraft.world.item.ItemStack item){return wrap(com.simibubi.create.foundation.utility.CreateLang.itemName(item));}
    public static Builder fluidName(net.neoforged.neoforge.fluids.FluidStack fluid){return wrap(com.simibubi.create.foundation.utility.CreateLang.fluidName(fluid));}
    public static final class Builder extends LangBuilder {
        Builder(){super("create");}
        public Builder color(int color){component().withStyle(style -> style.withColor(color));return this;}
        public Builder color(net.createmod.catnip.api.theme.Color color){return color(color.getRGB());}
        public void sendStatus(net.minecraft.world.entity.player.Player player){player.sendOverlayMessage(component());}
        public void addTo(java.util.List<Component> output){output.add(component());}
        @Override public Builder translate(String key,Object... args){super.translate(key,args);return this;}
        @Override public Builder text(String text){super.text(text);return this;}
        @Override public Builder text(ChatFormatting color,String text){super.text(color,text);return this;}
        @Override public Builder add(Component text){super.add(text);return this;}
        @Override public Builder add(LangBuilder text){super.add(text);return this;}
        @Override public Builder style(ChatFormatting color){super.style(color);return this;}
        @Override public Builder style(ChatFormatting... color){super.style(color);return this;}
        @Override public Builder space(){super.space();return this;}
    }
}
