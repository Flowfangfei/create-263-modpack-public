package com.railwayteam.railways.registry;

import com.railwayteam.railways.content.custom_bogeys.renderer.narrow.NarrowDoubleScotchYokeBogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.narrow.NarrowScotchYokeBogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.narrow.NarrowSmallBogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.standard.HandcarBogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.standard.double_axle.*;
import com.railwayteam.railways.content.custom_bogeys.renderer.standard.large.*;
import com.railwayteam.railways.content.custom_bogeys.renderer.standard.medium.*;
import com.railwayteam.railways.content.custom_bogeys.renderer.standard.single_axle.*;
import com.railwayteam.railways.content.custom_bogeys.renderer.standard.triple_axle.*;
import com.railwayteam.railways.content.custom_bogeys.renderer.unified.BogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.wide.WideComicallyLargeScotchYokeBogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.wide.WideDefaultBogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.wide.WideScotchYokeBogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.special.invisible.InvisibleBogeyRenderer;
import com.railwayteam.railways.content.custom_bogeys.special.invisible.InvisibleBogeyVisual;
import com.railwayteam.railways.content.custom_bogeys.special.monobogey.MonoBogeyDisplay;
import com.simibubi.create.content.trains.bogey.BogeyStyle.SizeRenderer;

import java.util.function.Supplier;

public final class CRBogeyStyleRenders {
    private static final java.util.Map<String, java.util.List<Supplier<SizeRenderer>>> RENDERERS = new java.util.HashMap<>();
    private CRBogeyStyleRenders() {}

    public static synchronized void register() {
        if (!RENDERERS.isEmpty()) return;
        register("monobogey", factory(MonoBogeyDisplay::new));
        register("invisible", CRBogeyStyleRenders::invisible);
        register("invisible_monobogey", CRBogeyStyleRenders::invisible);

        register("singleaxle", simple(SingleaxleBogeyDisplay::new));
        register("leafspring", simple(LeafspringBogeyDisplay::new));
        register("coilspring", simple(CoilspringBogeyDisplay::new));

        register("freight", simple(FreightBogeyDisplay::new));
        register("archbar", simple(ArchbarBogeyDisplay::new));
        register("passenger", simple(PassengerBogeyDisplay::new));
        register("modern", simple(ModernBogeyDisplay::new));
        register("blomberg", simple(BlombergBogeyDisplay::new));
        register("y25", simple(Y25BogeyDisplay::new));

        register("heavyweight", simple(HeavyweightBogeyDisplay::new));
        register("radial", simple(RadialBogeyDisplay::new));
        register("handcar", simple(HandcarBogeyDisplay::new));

        register("wide_default",
            simple(WideDefaultBogeyDisplay::new), simple(WideScotchYokeBogeyDisplay::new));
        register("wide_comically_large",
            simple(WideComicallyLargeScotchYokeBogeyDisplay::new));
        register("narrow_default",
            simple(NarrowSmallBogeyDisplay::new), simple(NarrowScotchYokeBogeyDisplay::new));
        register("narrow_double_scotch",
            simple(NarrowDoubleScotchYokeBogeyDisplay::new));

        register("medium_standard", simple(MediumStandardDisplay::new));
        register("medium_single_wheel", simple(MediumSingleWheelDisplay::new));
        register("medium_2_0_2_trailing", simple(Medium202TrailingDisplay::new));
        register("medium_4_0_4_trailing", simple(Medium404TrailingDisplay::new));
        register("medium_triple_wheel", simple(MediumTripleWheelDisplay::new));
        register("medium_6_0_6_trailing", simple(Medium606TrailingDisplay::new));
        register("medium_6_0_6_tender", simple(Medium606TenderDisplay::new));
        register("medium_quadruple_wheel", simple(MediumQuadrupleWheelDisplay::new));
        register("medium_8_0_8_tender", simple(Medium808TenderDisplay::new));
        register("medium_quintuple_wheel", simple(MediumQuintupleWheelDisplay::new));
        register("medium_10_0_10_tender", simple(Medium10010TenderDisplay::new));

        register("large_create_style_0_4_0", simple(LargeCreateStyled040Display::new));
        register("large_create_style_0_6_0", simple(LargeCreateStyled060Display::new));
        register("large_create_style_0_8_0", simple(LargeCreateStyled080Display::new));
        register("large_create_style_0_10_0", simple(LargeCreateStyled0100Display::new));
        register("large_create_style_0_12_0", simple(LargeCreateStyled0120Display::new));
    }

    @SafeVarargs
    private static void register(String style, Supplier<SizeRenderer>... sizes) {
        RENDERERS.put(style, java.util.List.of(sizes));
    }

    public static SizeRenderer forStyle(net.minecraft.resources.Identifier id,
                                        com.simibubi.create.content.trains.bogey.BogeySizes.BogeySize size) {
        register();
        var factories = RENDERERS.get(id.getPath());
        if (factories == null) throw new IllegalArgumentException("Unknown Railways bogey style: " + id);
        int index = factories.size() > 1 && size == com.simibubi.create.content.trains.bogey.BogeySizes.LARGE ? 1 : 0;
        return factories.get(index).get();
    }

    private static Supplier<SizeRenderer> simple(BogeyDisplay.SimpleFactory factory) {
        return () -> BogeyDisplay.createSizeRenderer(factory);
    }

    private static Supplier<SizeRenderer> factory(BogeyDisplay.Factory factory) {
        return () -> BogeyDisplay.createSizeRenderer(factory);
    }

    private static SizeRenderer invisible() {
        return new SizeRenderer(new InvisibleBogeyRenderer(), InvisibleBogeyVisual::new);
    }
}
