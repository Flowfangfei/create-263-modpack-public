package com.yision.fluidlogistics.mixin.accessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
@Mixin(value=BasinBlockEntity.class,remap=false)
public interface BasinResourceAccessor {
 @Invoker("getItemResourceCapability") ResourceHandler<ItemResource> fluidlogistics$itemResources();
 @Invoker("getFluidResourceCapability") ResourceHandler<FluidResource> fluidlogistics$fluidResources();
}
