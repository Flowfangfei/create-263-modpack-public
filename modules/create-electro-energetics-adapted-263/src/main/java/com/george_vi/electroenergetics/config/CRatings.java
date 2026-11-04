package com.george_vi.electroenergetics.config;


public class CRatings extends CEEConfigBase {
    public final ConfigFloat variacMaxPower = f(20_000, 1, "variacMaxPower", "[in Watts]");
    public final ConfigFloat potentiometerMaxPower = f(1_300, 1, "potentiometerMaxPower", "[in Watts]");
    public final ConfigFloat resistorMaxPower = f(1_300, 1, "resistorMaxPower", "[in Watts]");
    public final ConfigFloat transformerMaxPower = f(25_000, 1, "transformerMaxPower", "[in Watts]");
    public final ConfigFloat miniatureTransformerMaxPower = f(2_500, 1, "miniatureTransformerMaxPower", "[in Watts]");
    public final ConfigFloat electricFanMaxPower = f(750, 1, "electricFanMaxPower", "[in Watts]");

    @Override
    public String getName() {
        return "Ratings";
    }

}
