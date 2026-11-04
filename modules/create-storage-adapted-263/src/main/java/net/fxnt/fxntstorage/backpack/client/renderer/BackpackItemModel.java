package net.fxnt.fxntstorage.backpack.client.renderer;

import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.ItemOwner;
import net.fxnt.fxntstorage.backpack.upgrade.UpgradeType;
import net.fxnt.fxntstorage.config.ConfigManager;
import net.fxnt.fxntstorage.init.ModDataComponents;
import net.fxnt.fxntstorage.item.upgrades.UpgradeItem;
import org.joml.*;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.function.Supplier;

/** Native ItemModel wrapper; transform forwarding follows the existing CDG 26.3 implementation. */
public final class BackpackItemModel implements ItemModel {
    private final ItemModel base;
    private static final BackpackItemRenderer FLYWHEELS=new BackpackItemRenderer();
    public BackpackItemModel(ItemModel base) { this.base=base; }
    @Override public void update(ItemStackRenderState state,ItemStack stack,ItemModelResolver resolver,ItemDisplayContext context,ClientLevel level,ItemOwner owner,int seed) {
        ItemTransform[] transform=new ItemTransform[1];
        base.update(new TransformedState(state,new Matrix4f(),transform,false),stack,resolver,context,level,owner,seed);
        var upgrades=stack.get(ModDataComponents.BACKPACK_UPGRADES);
        String workshop=((UpgradeItem)UpgradeType.WORKSHOP.getActiveItem()).getUpgradeName();
        if(upgrades==null || !upgrades.contains(workshop) || UpgradeType.WORKSHOP.isDisabled() || !ConfigManager.ClientConfig.WORKSHOP_FLYWHEEL_VISUALS.get()) return;
        var layer=state.newLayer();
        layer.setUsesBlockLight(state.usesBlockLight());
        layer.setItemTransform(transform[0]==null?ItemTransform.NO_TRANSFORM:transform[0]);
        layer.setExtents(()->new Vector3fc[]{new Vector3f(0,0,0),new Vector3f(1,1,1)});
        layer.setupSpecialModel(FLYWHEELS,stack.getItem());
        if(stack.hasFoil()) layer.setFoilType(FoilType.STANDARD);
    }
    private static final class TransformedState extends ItemStackRenderState {
        private final ItemStackRenderState delegate;
        private final Matrix4fc transform;
        private final ItemTransform[] sharedTransform;
        private final boolean reuseTransform;

        private TransformedState(ItemStackRenderState delegate, Matrix4fc transform,
                                 ItemTransform[] sharedTransform, boolean reuseTransform) {
            this.delegate = delegate;
            this.transform = transform;
            this.sharedTransform = sharedTransform;
            this.reuseTransform = reuseTransform;
        }

        @Override public LayerRenderState newLayer() {
            return new TransformedLayer(delegate, delegate.newLayer(), transform, sharedTransform, reuseTransform);
        }
        @Override public void setAnimated() { delegate.setAnimated(); }
        @Override public void appendModelIdentityElement(Object value) { delegate.appendModelIdentityElement(value); }
        @Override public void setOversizedInGui(boolean value) { delegate.setOversizedInGui(value); }
    }

    private static final class TransformedLayer extends ItemStackRenderState.LayerRenderState {
        private final ItemStackRenderState.LayerRenderState delegate;
        private final Matrix4fc transform;
        private final ItemTransform[] sharedTransform;
        private final boolean reuseTransform;

        private TransformedLayer(ItemStackRenderState owner, ItemStackRenderState.LayerRenderState delegate,
                                 Matrix4fc transform, ItemTransform[] sharedTransform, boolean reuseTransform) {
            owner.super();
            this.delegate = delegate;
            this.transform = transform;
            this.sharedTransform = sharedTransform;
            this.reuseTransform = reuseTransform;
        }

        @Override public void setQuads(net.minecraft.client.resources.model.geometry.ItemQuads quads) { delegate.setQuads(quads); }
        @Override public void setUsesBlockLight(boolean value) { delegate.setUsesBlockLight(value); }
        @Override public void setExtents(Supplier<Vector3fc[]> value) { delegate.setExtents(value); }
        @Override public void setParticleMaterial(Material.Baked value) { delegate.setParticleMaterial(value); }
        @Override public void setItemTransform(ItemTransform value) {
            if (sharedTransform != null && reuseTransform && sharedTransform[0] != null)
                delegate.setItemTransform(sharedTransform[0]);
            else {
                if (sharedTransform != null)
                    sharedTransform[0] = value;
                delegate.setItemTransform(value);
            }
        }
        @Override public void setLocalTransform(Matrix4fc value) {
            delegate.setLocalTransform(new Matrix4f(transform).mul(value));
        }
        @Override public <T> void setupSpecialModel(SpecialModelRenderer<T> renderer, T value) {
            delegate.setupSpecialModel(renderer, value);
        }
        @Override public void setFoilType(FoilType value) { delegate.setFoilType(value); }
        @Override public IntList tintLayers() { return delegate.tintLayers(); }
    }
}
