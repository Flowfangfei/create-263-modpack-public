package net.fxnt.fxntstorage.init;

import net.fxnt.fxntstorage.FXNTStorage;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.renderpearl.api.pipeline.*;

/** Native line pipelines retain depth-tested and through-wall ore preview passes. */
public final class ModRenderTypes {
    private ModRenderTypes() {}
    public static final RenderType ORE_LINES_SOLID=RenderType.create("fxntstorage:ore_lines_solid",
        RenderSetup.builder(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(FXNTStorage.modLoc("pipeline/ore_lines_solid"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).build()).createRenderSetup());
    public static final RenderType ORE_LINES_XRAY=RenderType.create("fxntstorage:ore_lines_xray",
        RenderSetup.builder(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(FXNTStorage.modLoc("pipeline/ore_lines_xray"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS,false)).build()).createRenderSetup());
}
