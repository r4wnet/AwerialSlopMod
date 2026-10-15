package com.awerial.packeditor.mixin;

import com.awerial.packeditor.DamageTint;
import com.awerial.packeditor.Effects;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({EquipmentRenderer.class})
public class EquipmentRendererMixin {
   private static final String RENDER = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V";
   @Unique
   private RenderLayer awerialspackeditor$hurtLayer;

   @Unique
   private static boolean awerialspackeditor$tints(Object state) {
      boolean var10000;
      if (Effects.damageTint && Effects.tintTarget != Effects.TintTarget.PLAYER && state instanceof LivingEntityRenderState living) {
         if (living.hurt) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   @WrapOperation(
      method = {"render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/RenderLayers;armorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
)}
   )
   private RenderLayer awerialspackeditor$hurtArmourLayer(Identifier texture, Operation<RenderLayer> original, @Local(argsOnly = true) Object state) {
      if (!awerialspackeditor$tints(state)) {
         this.awerialspackeditor$hurtLayer = null;
         return (RenderLayer)original.call(new Object[]{texture});
      } else {
         this.awerialspackeditor$hurtLayer = RenderLayers.entityCutoutNoCullZOffset(texture);
         return this.awerialspackeditor$hurtLayer;
      }
   }

   @WrapOperation(
      method = {"render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/command/RenderCommandQueue;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IIILnet/minecraft/client/texture/Sprite;ILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V"
)}
   )
   private void awerialspackeditor$hurtArmourOverlay(RenderCommandQueue queue, Model<?> model, Object state, MatrixStack matrices, RenderLayer layer, int light, int overlay, int color, Sprite sprite, int outline, ModelCommandRenderer.CrumblingOverlayCommand crumbling, Operation<Void> original) {
      if (layer != null && layer == this.awerialspackeditor$hurtLayer) {
         int g = DamageTint.currentGradient;
         overlay = g >= 0 ? g : OverlayTexture.packUv(OverlayTexture.getU(0.0F), OverlayTexture.getV(true));
      }

      original.call(new Object[]{queue, model, state, matrices, layer, light, overlay, color, sprite, outline, crumbling});
   }
}
