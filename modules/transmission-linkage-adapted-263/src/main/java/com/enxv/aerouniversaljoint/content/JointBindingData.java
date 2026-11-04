package com.enxv.aerouniversaljoint.content;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public final class JointBindingData {
    private JointBindingData() {
    }

    public record Selection(Identifier dimensionId, BlockPos pos, @Nullable UUID subLevelId,
                            boolean creativeHydraulic, boolean giantHydraulic) {
        public Selection(Identifier dimensionId, BlockPos pos, @Nullable UUID subLevelId) {
            this(dimensionId, pos, subLevelId, false, false);
        }

        public Selection(Identifier dimensionId, BlockPos pos, @Nullable UUID subLevelId,
                         boolean creativeHydraulic) {
            this(dimensionId, pos, subLevelId, creativeHydraulic, false);
        }
    }
}

