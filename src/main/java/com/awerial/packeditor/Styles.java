package com.awerial.packeditor;

import java.util.List;
import java.util.function.IntUnaryOperator;

public final class Styles {
   public static final List<Style> ALL = List.of(new Style("crimson", "Crimson", colorize(0.0F, 0.75F, 1.05F)), new Style("frost", "Frost", colorize(195.0F, 0.55F, 1.25F)), new Style("toxic", "Toxic", colorize(105.0F, 0.85F, 1.1F)), new Style("royal", "Royal", colorize(275.0F, 0.7F, 1.1F)), new Style("gilded", "Gilded", colorize(45.0F, 0.8F, 1.2F)), new Style("sakura", "Sakura", colorize(330.0F, 0.5F, 1.3F)), new Style("ocean", "Ocean", colorize(220.0F, 0.8F, 1.0F)), new Style("midnight", "Midnight", colorize(240.0F, 0.35F, 0.6F)), new Style("neon", "Neon", Styles::neon), new Style("pastel", "Pastel", Styles::pastel), new Style("mono", "Monochrome", Styles::mono), new Style("inverted", "Inverted", (c) -> c & -16777216 | ~c & 16777215));

   public static int purpleToCyan(int c) {
      float[] hsv = toHsv(c);
      float deg = hsv[0] * 360.0F;
      return !(hsv[1] < 0.12F) && !(deg < 240.0F) && !(deg > 350.0F) ? fromHsv(c, 0.5138889F, hsv[1], hsv[2]) : c;
   }

   private Styles() {
   }

   public static Style byId(String id) {
      for(Style s : ALL) {
         if (s.id.equals(id)) {
            return s;
         }
      }

      return null;
   }

   private static IntUnaryOperator colorize(float hue, float sat, float bright) {
      return (c) -> {
         float[] hsv = toHsv(c);
         float s = Math.min(1.0F, hsv[1] * 0.4F + sat * 0.6F);
         float v = Math.min(1.0F, hsv[2] * bright);
         return fromHsv(c, hue / 360.0F, s, v);
      };
   }

   private static int neon(int c) {
      float[] hsv = toHsv(c);
      return fromHsv(c, hsv[0], Math.min(1.0F, hsv[1] * 1.8F + 0.3F), Math.min(1.0F, hsv[2] * 1.35F + 0.1F));
   }

   private static int pastel(int c) {
      float[] hsv = toHsv(c);
      return fromHsv(c, hsv[0], hsv[1] * 0.4F, Math.min(1.0F, hsv[2] * 0.6F + 0.4F));
   }

   private static int mono(int c) {
      int r = c >> 16 & 255;
      int g = c >> 8 & 255;
      int b = c & 255;
      int l = (int)((double)r * 0.299 + (double)g * 0.587 + (double)b * 0.114);
      return c & -16777216 | l << 16 | l << 8 | l;
   }

   private static float[] toHsv(int c) {
      float r = (float)(c >> 16 & 255) / 255.0F;
      float g = (float)(c >> 8 & 255) / 255.0F;
      float b = (float)(c & 255) / 255.0F;
      float max = Math.max(r, Math.max(g, b));
      float min = Math.min(r, Math.min(g, b));
      float d = max - min;
      float h;
      if (d == 0.0F) {
         h = 0.0F;
      } else if (max == r) {
         h = (g - b) / d % 6.0F;
      } else if (max == g) {
         h = (b - r) / d + 2.0F;
      } else {
         h = (r - g) / d + 4.0F;
      }

      h /= 6.0F;
      if (h < 0.0F) {
         ++h;
      }

      return new float[]{h, max == 0.0F ? 0.0F : d / max, max};
   }

   private static int fromHsv(int original, float h, float s, float v) {
      float hh = h % 1.0F * 6.0F;
      int i = (int)Math.floor((double)hh);
      float f = hh - (float)i;
      float p = v * (1.0F - s);
      float q = v * (1.0F - s * f);
      float t = v * (1.0F - s * (1.0F - f));
      float r;
      float g;
      float b;
      switch (i) {
         case 0:
            r = v;
            g = t;
            b = p;
            break;
         case 1:
            r = q;
            g = v;
            b = p;
            break;
         case 2:
            r = p;
            g = v;
            b = t;
            break;
         case 3:
            r = p;
            g = q;
            b = v;
            break;
         case 4:
            r = t;
            g = p;
            b = v;
            break;
         default:
            r = v;
            g = p;
            b = q;
      }

      return original & -16777216 | Math.round(r * 255.0F) << 16 | Math.round(g * 255.0F) << 8 | Math.round(b * 255.0F);
   }

   public static record Style(String id, String name, IntUnaryOperator op) {
   }
}
