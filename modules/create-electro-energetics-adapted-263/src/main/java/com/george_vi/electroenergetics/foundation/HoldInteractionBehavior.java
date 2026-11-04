package com.george_vi.electroenergetics.foundation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface HoldInteractionBehavior {
    void release();

    @Environment(EnvType.CLIENT)
    void tick();

    void onMouseMove(double y, double x);

    @Environment(EnvType.CLIENT)
    boolean isStillActive();
}
