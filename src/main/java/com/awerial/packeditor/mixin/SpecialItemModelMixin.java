package com.awerial.packeditor.mixin;

import com.awerial.packeditor.ShieldDisabled;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.item.model.SpecialItemModel;
import net.minecraft.client.render.item.model.special.ShieldModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({SpecialItemModel.class})
public class SpecialItemModelMixin {
   @WrapOperation(
      method = {"update"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/item/model/special/SpecialModelRenderer;getData(Lnet/minecraft/item/ItemStack;)Ljava/lang/Object;"
)}
   )
   private Object awerialspackeditor$markDisabledShield(SpecialModelRenderer<?> renderer, ItemStack stack, Operation<Object> original, @Local(argsOnly = true) HeldItemContext holder) {
      Object data = original.call(new Object[]{renderer, stack});
      if (renderer instanceof ShieldModelRenderer && data instanceof ComponentMap) {
         ComponentMap map = (ComponentMap)data;
         if (ShieldDisabled.appliesTo(holder, stack)) {
            return ShieldDisabled.mark(map);
         }
      }

      return data;
   }
}
