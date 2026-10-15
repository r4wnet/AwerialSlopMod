package com.awerial.packeditor.mixin;

import com.awerial.packeditor.DamageTint;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({OverlayTexture.class})
public class OverlayTextureMixin {
   @Shadow
   @Final
   private NativeImageBackedTexture texture;

   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void awerialspackeditor$remember(CallbackInfo ci) {
      DamageTint.texture = this.texture;
      DamageTint.refresh();
   }
}
