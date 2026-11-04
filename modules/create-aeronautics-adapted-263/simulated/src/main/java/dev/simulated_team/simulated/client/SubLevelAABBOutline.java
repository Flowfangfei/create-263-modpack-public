package dev.simulated_team.simulated.client;

import net.createmod.catnip.api.client.outliner.AABBOutline;
import net.minecraft.world.phys.AABB;

/** Parent renderer is projected by Sable's AABBOutlineMixin; avoid applying the pose twice. */
public class SubLevelAABBOutline extends AABBOutline {
    public SubLevelAABBOutline(AABB bounds){super(bounds);}
}
