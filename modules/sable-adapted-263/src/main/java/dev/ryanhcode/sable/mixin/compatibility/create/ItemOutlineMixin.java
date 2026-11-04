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

@Mixin(targets="net.createmod.catnip.api.client.outliner.ItemOutline")
public class ItemOutlineMixin {
    @Shadow protected Vec3 pos;
    @WrapMethod(method="render")
    private void sable$plotOutline(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera,
            float partialTicks, Operation<Void> original) {
        ClientSubLevel sub=SubLevelOutlinePose.find(pos);
        if (sub==null) { original.call(ms,buffer,camera,partialTicks); return; }
        SubLevelOutlinePose.runProjected(ms,sub,camera,
            local -> original.call(ms,buffer,local,partialTicks));
    }
}
