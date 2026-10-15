package com.awerial.packeditor.mixin;

import com.awerial.packeditor.SkyTint;
import java.util.function.IntUnaryOperator;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({FogRenderer.class})
public class FogRendererMixin {
   @Inject(
      method = {"getFogColor"},
      at = {@At("RETURN")}
   )
   private void awerialspackeditor$tintFog(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir) {
      IntUnaryOperator op = SkyTint.activeOp();
      if (op != null && camera.getSubmersionType() == CameraSubmersionType.NONE) {
         Vector4f c = (Vector4f)cir.getReturnValue();
         int argb = -16777216 | to255(c.x) << 16 | to255(c.y) << 8 | to255(c.z);
         int t = op.applyAsInt(argb);
         c.set((float)(t >> 16 & 255) / 255.0F, (float)(t >> 8 & 255) / 255.0F, (float)(t & 255) / 255.0F, c.w);
      }
   }

   private static int to255(float f) {
      return Math.max(0, Math.min(255, Math.round(f * 255.0F)));
   }
}
