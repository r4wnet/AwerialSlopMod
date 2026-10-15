package com.awerial.packeditor.mixin;

import com.awerial.packeditor.ShieldDisabled;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.item.model.special.ShieldModelRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.component.ComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({ShieldModelRenderer.class})
public class ShieldModelRendererMixin {
   @ModifyExpressionValue(
      method = {"render(Lnet/minecraft/component/ComponentMap;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IIZI)V"},
      at = {@At(
   value = "FIELD",
   target = "Lnet/minecraft/client/render/model/ModelBaker;SHIELD_BASE:Lnet/minecraft/client/util/SpriteIdentifier;"
)}
   )
   private SpriteIdentifier awerialspackeditor$disabledBase(SpriteIdentifier original, @Local(argsOnly = true) ComponentMap data) {
      return data != null && ShieldDisabled.isMarked(data) ? ShieldDisabled.SPRITE : original;
   }

   @ModifyExpressionValue(
      method = {"render(Lnet/minecraft/component/ComponentMap;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IIZI)V"},
      at = {@At(
   value = "FIELD",
   target = "Lnet/minecraft/client/render/model/ModelBaker;SHIELD_BASE_NO_PATTERN:Lnet/minecraft/client/util/SpriteIdentifier;"
)}
   )
   private SpriteIdentifier awerialspackeditor$disabledNoPattern(SpriteIdentifier original, @Local(argsOnly = true) ComponentMap data) {
      return data != null && ShieldDisabled.isMarked(data) ? ShieldDisabled.SPRITE_NOPATTERN : original;
   }
}
