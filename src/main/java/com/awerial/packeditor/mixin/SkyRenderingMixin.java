package com.awerial.packeditor.mixin;

import com.awerial.packeditor.SkyTint;
import java.util.function.IntUnaryOperator;
import net.minecraft.client.render.SkyRendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({SkyRendering.class})
public class SkyRenderingMixin {
   @ModifyVariable(
      method = {"renderTopSky"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private int awerialspackeditor$tintSky(int color) {
      IntUnaryOperator op = SkyTint.activeOp();
      return op == null ? color : color & -16777216 | op.applyAsInt(color | -16777216) & 16777215;
   }
}
