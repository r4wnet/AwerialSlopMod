package com.awerial.packeditor.gui;

import net.minecraft.client.texture.NativeImage;

final class PreviewImages {
   private PreviewImages() {
   }

   static NativeImage shieldFront(NativeImage tex) {
      int s = tex.getWidth() / 64;
      if (s < 1) {
         return tex;
      } else {
         int w = 12 * s;
         int h = 22 * s;
         NativeImage out = new NativeImage(w, h, true);

         for(int y = 0; y < h; ++y) {
            for(int x = 0; x < w; ++x) {
               out.setColorArgb(x, y, tex.getColorArgb(s + x, s + y));
            }
         }

         tex.close();
         return out;
      }
   }

   static NativeImage firstFrame(NativeImage img) {
      int w = img.getWidth();
      int h = img.getHeight();
      if (h > w && h % w == 0) {
         NativeImage out = new NativeImage(w, w, true);

         for(int y = 0; y < w; ++y) {
            for(int x = 0; x < w; ++x) {
               out.setColorArgb(x, y, img.getColorArgb(x, y));
            }
         }

         img.close();
         return out;
      } else {
         return img;
      }
   }

   static NativeImage crystalIcon(NativeImage tex) {
      int s = tex.getWidth() / 64;
      if (s >= 1 && tex.getHeight() >= 16 * s) {
         int face = 8 * s;
         int size = face * 2;
         NativeImage out = new NativeImage(size, size, true);
         out.fillRect(0, 0, size, size, 0);
         int off = face / 2;

         for(int y = 0; y < face; ++y) {
            for(int x = 0; x < face; ++x) {
               blend(out, off + x, off + y, tex.getColorArgb(5 * face + x, face + y));
            }
         }

         for(int y = 0; y < size; ++y) {
            for(int x = 0; x < size; ++x) {
               blend(out, x, y, tex.getColorArgb(face + x / 2, face + y / 2));
            }
         }

         tex.close();
         return out;
      } else {
         return tex;
      }
   }

   private static void blend(NativeImage img, int x, int y, int src) {
      int sa = src >>> 24;
      if (sa != 0) {
         if (sa == 255) {
            img.setColorArgb(x, y, src);
         } else {
            int dst = img.getColorArgb(x, y);
            int da = dst >>> 24;
            float a = (float)sa / 255.0F;
            int outA = Math.min(255, sa + da * (255 - sa) / 255);
            int r = mix(src >> 16 & 255, dst >> 16 & 255, a, da);
            int g = mix(src >> 8 & 255, dst >> 8 & 255, a, da);
            int b = mix(src & 255, dst & 255, a, da);
            img.setColorArgb(x, y, outA << 24 | r << 16 | g << 8 | b);
         }
      }
   }

   private static int mix(int s, int d, float a, int da) {
      return da == 0 ? s : Math.round((float)s * a + (float)d * (1.0F - a));
   }
}
