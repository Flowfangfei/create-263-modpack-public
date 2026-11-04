package dev.simulated_team.simulated.compat.render;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.client.render.SuperBufferFactory;
import net.createmod.catnip.api.client.render.SuperByteBufferCache;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

/** Cache geometry through target Catnip; each caller receives its own draw-state wrapper. */
public final class CachedBuffers {
    private record Key(PartialModel partial,BlockState reference){}
    private static final SuperByteBufferCache.Compartment<Key> PARTIALS=new SuperByteBufferCache.Compartment<>();
    public static void init(){SuperByteBufferCache.getInstance().registerCompartment(PARTIALS);}
    public static SuperByteBuffer block(BlockState state){return new SuperByteBuffer(()->net.createmod.catnip.api.client.render.CachedBuffers.block(state));}
    public static SuperByteBuffer block(SuperByteBufferCache.Compartment<BlockState> compartment,BlockState state){return new SuperByteBuffer(()->net.createmod.catnip.api.client.render.CachedBuffers.block(compartment,state));}
    public static <T> SuperByteBuffer model(SuperByteBufferCache.Compartment<T> compartment,T key,java.util.function.Supplier<BlockStateModelPart> supplier,BlockState state){
        return new SuperByteBuffer(()->SuperByteBufferCache.getInstance().get(compartment,key,()->{
            var part=supplier.get();
            BlockStateModel model=new BlockStateModel(){
                public void collectParts(RandomSource random,List<BlockStateModelPart> parts){parts.add(part);}
                public Material.Baked particleMaterial(){return part.particleMaterial();}
                public int materialFlags(){return part.materialFlags();}
            };
            return SuperBufferFactory.getInstance().createForBlock(model,state);
        }));
    }
    public static SuperByteBuffer partial(PartialModel partial,BlockState state){
        return new SuperByteBuffer(()->SuperByteBufferCache.getInstance().get(PARTIALS,new Key(partial,state),()->{
            var part=partial.get();
            BlockStateModel model=new BlockStateModel(){
                public void collectParts(RandomSource random,List<BlockStateModelPart> parts){parts.add(part);}
                public Material.Baked particleMaterial(){return part.particleMaterial();}
                public int materialFlags(){return part.materialFlags();}
            };
            return SuperBufferFactory.getInstance().createForBlock(model,state);
        }));
    }
    public static SuperByteBuffer partialDirectional(PartialModel partial,BlockState state,Direction direction,java.util.function.Function<Direction,com.mojang.blaze3d.vertex.PoseStack.Pose> transform){return partial(partial,state).transform(transform.apply(direction).pose());}
    public static SuperByteBuffer partialFacing(PartialModel partial,BlockState state,Direction direction){return partial(partial,state).transform(net.createmod.catnip.api.client.render.CachedBuffers.rotateToFace(direction).get());}
    public static SuperByteBuffer partialFacingVertical(PartialModel partial,BlockState state,Direction direction){return partial(partial,state).transform(net.createmod.catnip.api.client.render.CachedBuffers.rotateToFaceVertical(direction).get());}
}
