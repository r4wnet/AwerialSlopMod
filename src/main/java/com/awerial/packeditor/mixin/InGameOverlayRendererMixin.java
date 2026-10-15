package com.awerial.packeditor.mixin;

import com.awerial.packeditor.Effects;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InGameOverlayRenderer.class})
public class InGameOverlayRendererMixin {
   @Shadow
   private int floatingItemTimer;
   @Shadow
   private ItemStack floatingItem;

   @Inject(
      method = {"setFloatingItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void awerialspackeditor$noPop(ItemStack stack, Random random, CallbackInfo ci) {
      if (Effects.popAnimation == Effects.PopAnimation.NONE && stack.isOf(Items.TOTEM_OF_UNDYING)) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"tickFloatingItemTimer"},
      at = {@At("TAIL")}
   )
   private void awerialspackeditor$shortPop(CallbackInfo ci) {
      if (Effects.popAnimation == Effects.PopAnimation.SHORT && this.floatingItemTimer > 0) {
         if (this.floatingItem != null && this.floatingItem.isOf(Items.TOTEM_OF_UNDYING)) {
            if (--this.floatingItemTimer == 0) {
               this.floatingItem = null;
            }

         }
      }
   }
}
