package dev.ryanhcode.sable.mixin.compatibility.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.mixinhelpers.compatibility.create.SubLevelOutlinePose;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.world.phys.AABB;
import org.joml.Vector4f;

@Mixin(targets="net.createmod.catnip.api.client.outliner.AABBOutline")
public class AABBOutlineMixin {
    @WrapMethod(method="renderBox")
    private void sable$plotOutline(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera,
            AABB box, Vector4f color, int light, boolean disableNormals, Operation<Void> original) {
        ClientSubLevel sub = SubLevelOutlinePose.find(box);
        if (sub == null) { original.call(ms,buffer,camera,box,color,light,disableNormals); return; }
        SubLevelOutlinePose.runProjected(ms,sub,camera,
            local -> original.call(ms,buffer,local,box,color,light,disableNormals));
    }
}
