package com.george_vi.electroenergetics.foundation.lang;
import java.text.NumberFormat;
import java.util.Locale;
public final class LangNumberFormat {
    private static final ThreadLocal<NumberFormat> FORMAT = ThreadLocal.withInitial(() -> {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
        format.setMaximumFractionDigits(2);
        return format;
    });
    private LangNumberFormat() {}
    public static String format(double value) { return FORMAT.get().format(value == 0 ? 0 : value).replace('\u00a0', ' '); }
}
