package net.fxnt.fxntstorage.client;

import net.createmod.catnip.api.client.lang.FontHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

/** Styles item descriptions that may also be read by the asynchronous recipe search. */
public final class StorageTooltipText {
    private StorageTooltipText() {}

    public static List<Component> format(Component text, FontHelper.Palette palette) {
        return format(text, palette.primary(), palette.highlight());
    }

    public static List<Component> format(Component text, Style primary, Style highlight) {
        return format(text, primary, highlight, 0);
    }

    public static List<Component> format(Component text, Style primary, Style highlight, int indent) {
        String value = text.getString();
        if (value.isEmpty()) return List.of();

        // Font measurement can upload glyphs. Leave pixel wrapping to the GUI's tooltip renderer.
        List<Component> paragraphs = new ArrayList<>();
        boolean highlighted = false;
        for (String paragraph : value.split("\\R", -1)) {
            MutableComponent formatted = Component.literal(" ".repeat(indent)).withStyle(primary);
            String[] parts = paragraph.split("_", -1);
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) highlighted = !highlighted;
                formatted.append(Component.literal(parts[i]).withStyle(highlighted ? highlight : primary));
            }
            paragraphs.add(formatted);
        }
        return paragraphs;
    }
}
