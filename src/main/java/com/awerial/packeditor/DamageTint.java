package com.awerial.packeditor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public final class DamageTint {
   private static final int VANILLA = -1291911168;
   public static NativeImageBackedTexture texture;
   private static String shown = "";
   public static final String SHADER_MARK = "AWERIAL_GRADIENT";
   private static boolean shaderReady;
   public static int currentGradient = -1;
   private static long shaderChecked;

   private DamageTint() {
   }

   public static float strength() {
      return (float)Effects.tintOpacity / 100.0F;
   }

   public static void refresh() {
      if (texture != null) {
         boolean on = Effects.damageTint;
         String sig = on ? Effects.tintColor + "/" + Effects.tintColor2 + "/" + Effects.tintGradient + "/" + Effects.tintOpacity : "vanilla";
         if (!sig.equals(shown)) {
            NativeImage img = texture.getImage();
            if (img != null) {
               int alpha = Math.round((1.0F - strength()) * 255.0F) << 24;

               for(int y = 0; y < 8; ++y) {
                  int c = !on ? -1291911168 : alpha | (Effects.tintGradient ? Effects.mix(Effects.tintColor, Effects.tintColor2, (float)y / 7.0F) : Effects.tintColor);

                  for(int x = 0; x < 16; ++x) {
                     img.setColorArgb(x, y, c);
                  }
               }

               texture.upload();
               shown = sig;
            }
         }
      }
   }

   public static boolean gradientActive() {
      if (Effects.damageTint && Effects.tintGradient) {
         long now = Util.getMeasuringTimeMs();
         if (now - shaderChecked > 3000L) {
            shaderChecked = now;
            shaderReady = shaderInUse() && !irisShadersOn();
         }

         return shaderReady;
      } else {
         return false;
      }
   }

   public static int gradientOverlay(int whiteU, double feetY, float height) {
      int feet = (int)Math.round((Math.max((double)-64.0F, Math.min(63.9, feetY)) + (double)64.0F) * (double)16.0F) & 2047;
      int h = Math.max(1, Math.min(15, Math.round(height * 4.0F)));
      int u = feet << 4 | whiteU & 15;
      int v = 2816 | h << 4 | 3;
      return u | v << 16;
   }

   private static boolean shaderInUse() {
      try {
         Optional<Resource> res = MinecraftClient.getInstance().getResourceManager().getResource(Identifier.ofVanilla("shaders/core/entity.vsh"));
         if (res.isEmpty()) {
            return false;
         } else {
            InputStream in = ((Resource)res.get()).getInputStream();

            boolean var2;
            try {
               var2 = (new String(in.readAllBytes(), StandardCharsets.UTF_8)).contains("AWERIAL_GRADIENT");
            } catch (Throwable var5) {
               if (in != null) {
                  try {
                     in.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }
               }

               throw var5;
            }

            if (in != null) {
               in.close();
            }

            return var2;
         }
      } catch (Exception var6) {
         return false;
      }
   }

   private static boolean irisShadersOn() {
      try {
         Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
         Object instance = api.getMethod("getInstance").invoke((Object)null);
         return (Boolean)api.getMethod("isShaderPackInUse").invoke(instance);
      } catch (Throwable var2) {
         return false;
      }
   }

   public static String patchShader(String vanilla) {
      String line = "overlayColor = texelFetch(Sampler1, UV1, 0);";
      if (vanilla != null && vanilla.contains(line)) {
         String patch = "// AWERIAL_GRADIENT: Awerial's Pack Editor two-colour damage tint (vanilla otherwise)\n    ivec2 apeOverlay = UV1;\n    if ((apeOverlay.y >> 8) == 11) {\n        float apeFeet = float(apeOverlay.x >> 4) / 16.0 - 64.0;\n        float apeHeight = max(0.25, float((apeOverlay.y >> 4) & 15) / 4.0);\n        float apeT = clamp((Position.y - apeFeet) / apeHeight, 0.0, 1.0);\n        apeOverlay = ivec2(apeOverlay.x & 15, int(round((1.0 - apeT) * 7.0)));\n    }\n    overlayColor = texelFetch(Sampler1, apeOverlay, 0);";
         return vanilla.replace(line, patch);
      } else {
         return null;
      }
   }
}
