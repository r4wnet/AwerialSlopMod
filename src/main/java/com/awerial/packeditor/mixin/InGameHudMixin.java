package com.awerial.packeditor.mixin;

import com.awerial.packeditor.Category;
import com.awerial.packeditor.HudExtras;
import com.awerial.packeditor.PackEditorConfig;
import com.awerial.packeditor.Toggle;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InGameHud.class})
public class InGameHudMixin {
   @Unique
   private boolean awerialspackeditor$scaled;

   @ModifyArg(
      method = {"renderCrosshair"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V",
   ordinal = 0
),
      index = 0
   )
   private RenderPipeline awerialspackeditor$trueColourCrosshair(RenderPipeline pipeline) {
      boolean custom = !"vanilla".equals(PackEditorConfig.get(Category.CROSSHAIR));
      return custom && !PackEditorConfig.get(Toggle.INVERT_CROSSHAIR) ? RenderPipelines.GUI_TEXTURED : pipeline;
   }

   @Inject(
      method = {"renderCrosshair"},
      at = {@At("HEAD")}
   )
   private void awerialspackeditor$sizeStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.awerialspackeditor$scaled = false;
      int size = PackEditorConfig.crosshairSize();
      if (size > 0) {
         MinecraftClient mc = MinecraftClient.getInstance();
         double gui = (double)mc.getWindow().getScaleFactor();
         if (!(gui <= (double)0.0F) && (double)size != gui) {
            float s = (float)((double)size / gui);
            int x0 = (context.getScaledWindowWidth() - 15) / 2;
            int y0 = (context.getScaledWindowHeight() - 15) / 2;
            float tx = (float)Math.round((float)mc.getWindow().getFramebufferWidth() / 2.0F - 7.5F * (float)size) / (float)gui - s * (float)x0;
            float ty = (float)Math.round((float)mc.getWindow().getFramebufferHeight() / 2.0F - 7.5F * (float)size) / (float)gui - s * (float)y0;
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(tx, ty);
            context.getMatrices().scale(s, s);
            this.awerialspackeditor$scaled = true;
         }
      }
   }

   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   private void awerialspackeditor$hudExtras(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      HudExtras.render(context, tickCounter.getTickProgress(true));
   }

   @Inject(
      method = {"renderCrosshair"},
      at = {@At("RETURN")}
   )
   private void awerialspackeditor$sizeEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      if (this.awerialspackeditor$scaled) {
         context.getMatrices().popMatrix();
      }

      this.awerialspackeditor$scaled = false;
   }
}
