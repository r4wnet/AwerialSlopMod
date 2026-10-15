package com.awerial.packeditor.mixin;

import com.awerial.packeditor.DamageTint;
import com.awerial.packeditor.Effects;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntityRenderer.class})
public class LivingEntityRendererMixin {
   @ModifyReturnValue(
      method = {"getOverlay"},
      at = {@At("RETURN")}
   )
   private static int awerialspackeditor$armourOnly(int original, LivingEntityRenderState state, float whiteOverlayProgress) {
      return Effects.damageTint && Effects.tintTarget == Effects.TintTarget.ARMOUR && state.hurt ? OverlayTexture.packUv(OverlayTexture.getU(whiteOverlayProgress), OverlayTexture.getV(false)) : original;
   }

   @Inject(
      method = {"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V"},
      at = {@At("HEAD")}
   )
   private void awerialspackeditor$gradientStart(LivingEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState camera, CallbackInfo ci) {
      DamageTint.currentGradient = -1;
      if (state.hurt && DamageTint.gradientActive()) {
         float feetY = matrices.peek().getPositionMatrix().m31();
         DamageTint.currentGradient = DamageTint.gradientOverlay(0, (double)feetY, Math.max(0.5F, state.height));
      }

   }

   @ModifyExpressionValue(
      method = {"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getOverlay(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)I"
)}
   )
   private int awerialspackeditor$gradientBody(int overlay) {
      int g = DamageTint.currentGradient;
      return g >= 0 && Effects.tintTarget != Effects.TintTarget.ARMOUR ? g & -16 | overlay & 15 : overlay;
   }

   @Inject(
      method = {"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V"},
      at = {@At("RETURN")}
   )
   private void awerialspackeditor$gradientEnd(LivingEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState camera, CallbackInfo ci) {
      DamageTint.currentGradient = -1;
   }
}
