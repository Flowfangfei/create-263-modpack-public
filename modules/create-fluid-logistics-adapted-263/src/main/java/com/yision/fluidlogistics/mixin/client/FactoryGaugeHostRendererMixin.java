package com.yision.fluidlogistics.mixin.client;

import java.util.Map;
import java.util.function.Function;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.factoryBoard.*;
import com.yision.fluidlogistics.api.factorygauge.client.FactoryGaugeClient;
import com.yision.fluidlogistics.api.factorygauge.client.FactoryGaugeModelSet;
import com.yision.fluidlogistics.content.logistics.factoryGauge.ResourceFactoryPanelBehaviour;
import com.yision.fluidlogistics.content.logistics.factoryGauge.client.ResourceFactoryGaugeModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

@Mixin(FactoryPanelRenderer.class)
public abstract class FactoryGaugeHostRendererMixin {
    @Unique
    private static FactoryGaugeModelSet fluidlogistics$models(FactoryPanelBehaviour behaviour) {
        return behaviour instanceof ResourceFactoryPanelBehaviour resource && resource.isResourceGauge()
            ? resource.registeredType().flatMap(type -> FactoryGaugeClient.modelsFor(type.id())).orElse(null) : null;
    }

    @WrapOperation(method = "submitBulb", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelRenderer;model(Lnet/neoforged/neoforge/client/model/standalone/StandaloneModelKey;)Lnet/minecraft/client/renderer/block/dispatch/BlockStateModelPart;"))
    private BlockStateModelPart fluidlogistics$bulb(FactoryPanelRenderer renderer,
        StandaloneModelKey<BlockStateModelPart> key, Operation<BlockStateModelPart> original,
        @Local(argsOnly = true) FactoryPanelBehaviour behaviour) {
        var models = fluidlogistics$models(behaviour);
        return models == null ? original.call(renderer, key)
            : (behaviour.redstonePowered || behaviour.isMissingAddress() ? models.bulbRed() : models.bulbLight()).get();
    }

    @WrapOperation(method = "submitPanel", at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"))
    private Object fluidlogistics$panel(Map<?, ?> cache, Object key, Function<?, ?> mapping,
        Operation<Object> original, @Local(argsOnly = true) FactoryPanelBehaviour behaviour,
        @Local(argsOnly = true) FactoryPanelBlockEntity be) {
        var models = fluidlogistics$models(behaviour);
        if (models == null) return original.call(cache, key, mapping);
        return ResourceFactoryGaugeModel.panelFor(models,
            be.restocker ? FactoryPanelBlock.PanelType.PACKAGER : FactoryPanelBlock.PanelType.NETWORK,
            behaviour.count == 0 ? FactoryPanelBlock.PanelState.PASSIVE : FactoryPanelBlock.PanelState.ACTIVE).get();
    }

    @WrapOperation(method = "submitFilter", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/render/FlatGuiItemRenderer;submit(Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IFF)V"))
    private void fluidlogistics$worldFluid(net.minecraft.world.item.ItemStack stack,
        com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
        int light, float width, float height, Operation<Void> original,
        @Local(argsOnly = true) FactoryPanelBehaviour behaviour) {
        var geometry = new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.GeometryCollector(collector);
        if (com.yision.fluidlogistics.content.logistics.factoryGauge.client.FactoryGaugeWorldFluidRenderer
            .tryRender(behaviour, stack, pose, geometry, light)) {
            geometry.submit();
            return;
        }
        original.call(stack, pose, collector, light, width, height);
    }
}
