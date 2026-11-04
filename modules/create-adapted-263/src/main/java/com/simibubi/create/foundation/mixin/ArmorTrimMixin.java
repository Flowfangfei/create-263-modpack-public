package com.simibubi.create.foundation.mixin;

import com.simibubi.create.Create;
import java.util.Collection;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.client.resources.palette.PalettedTextureManager;
import net.minecraft.client.resources.palette.PalettedTextureManager.Handle;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer$TrimTextureKey")
public abstract class ArmorTrimMixin {
   @Shadow
   @Final
   private ArmorTrim trim;
   @Shadow
   @Final
   private LayerType layerType;
   @Shadow
   @Final
   private EquipmentClientInfo equipmentInfo;

   @Inject(method = "getOrPrepareTexture", at = @At("HEAD"), cancellable = true)
   private void create$cardboardTrim(PalettedTextureManager textures, CallbackInfoReturnable<Handle> cir) {
      boolean cardboard = this.equipmentInfo
         .layers()
         .values()
         .stream()
         .flatMap(Collection::stream)
         .anyMatch(layer -> layer.textureId().equals(Create.asResource("cardboard")));
      if (cardboard) {
         String leggings = this.layerType.trimAssetPrefix().contains("leggings") ? "_leggings" : "";
         Identifier texture = Create.asResource("trims/models/armor/card_" + ((TrimPattern)this.trim.pattern().value()).assetId().getPath() + leggings);
         cir.setReturnValue(textures.getOrPrepare(texture, ((TrimMaterial)this.trim.material().value()).paletteId()));
      }
   }
}
