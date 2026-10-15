package com.awerial.packeditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.texture.NativeImage;

public final class WornArmour {
   private static final int[] HELMET = new int[]{0, 0, 64, 16};
   private static final int[] CHESTPLATE = new int[]{16, 16, 40, 16};
   private static final int[] BOOTS = new int[]{0, 16, 16, 16};
   private static final int[] WHOLE = new int[]{0, 0, 64, 32};
   private static final Map<String, Optional<Category.Target>> CACHE = new HashMap();

   private WornArmour() {
   }

   public static Category.Target wornTarget(String itemId) {
      return (Category.Target)((Optional)CACHE.computeIfAbsent(itemId, (k) -> Optional.ofNullable(findWornTarget(k)))).orElse(null);
   }

   private static Category.Target findWornTarget(String itemId) {
      int us = itemId.lastIndexOf(95);
      if (us < 0) {
         return null;
      } else {
         String var10000;
         switch (itemId.substring(0, us)) {
            case "golden" -> var10000 = "gold";
            case "turtle" -> var10000 = "turtle_scute";
            default -> var10000 = itemId.substring(0, us);
         }

         String material = var10000;
         String humanoid = "entity/equipment/humanoid/" + material + ".png";
         Category.Target var10;
         switch (itemId.substring(us + 1)) {
            case "helmet" -> var10 = new Category.Target(humanoid, List.of(), HELMET);
            case "chestplate" -> var10 = new Category.Target(humanoid, List.of(), CHESTPLATE);
            case "boots" -> var10 = new Category.Target(humanoid, List.of(), BOOTS);
            case "leggings" -> var10 = new Category.Target("entity/equipment/humanoid_leggings/" + material + ".png", List.of(), WHOLE);
            default -> var10 = null;
         }

         Category.Target t = var10;

         try {
            return t != null && Variant.readVanilla(t.path()) != null ? t : null;
         } catch (IOException var7) {
            return null;
         }
      }
   }

   public static NativeImage vanillaPart(Category.Target worn) throws IOException {
      byte[] b = Variant.readVanillaAsset("textures/" + worn.path());
      if (b == null) {
         return null;
      } else {
         NativeImage full = NativeImage.read(b);

         NativeImage var15;
         try {
            int[] r = worn.region() != null ? worn.region() : WHOLE;
            int fw = full.getWidth();
            int fh = full.getHeight();
            int x0 = r[0] * fw / 64;
            int y0 = r[1] * fh / 32;
            int w = r[2] * fw / 64;
            int h = r[3] * fh / 32;
            NativeImage out = new NativeImage(w, h, true);

            for(int y = 0; y < h; ++y) {
               for(int x = 0; x < w; ++x) {
                  out.setColorArgb(x, y, full.getColorArgb(x0 + x, y0 + y));
               }
            }

            var15 = out;
         } catch (Throwable var14) {
            if (full != null) {
               try {
                  full.close();
               } catch (Throwable var13) {
                  var14.addSuppressed(var13);
               }
            }

            throw var14;
         }

         if (full != null) {
            full.close();
         }

         return var15;
      }
   }

   public static NativeImage wornImage(Category.Target worn, NativeImage part, NativeImage icon) throws IOException {
      try {
         if (part == null && icon != null) {
            part = autoPart(worn, icon);
         }
      } finally {
         if (icon != null) {
            icon.close();
         }

      }

      if (part == null) {
         return null;
      } else {
         int[] r = worn.region();
         if (r == null) {
            return part;
         } else {
            int fw = part.getWidth() * 64 / r[2];
            int fh = fw / 2;
            NativeImage out = new NativeImage(fw, fh, true);
            out.fillRect(0, 0, fw, fh, 0);
            int x0 = r[0] * fw / 64;
            int y0 = r[1] * fh / 32;

            for(int y = 0; y < part.getHeight() && y0 + y < fh; ++y) {
               for(int x = 0; x < part.getWidth() && x0 + x < fw; ++x) {
                  out.setColorArgb(x0 + x, y0 + y, part.getColorArgb(x, y));
               }
            }

            part.close();
            return out;
         }
      }
   }

   public static NativeImage autoPart(Category.Target worn, NativeImage icon) throws IOException {
      NativeImage base = vanillaPart(worn);
      if (base == null) {
         return null;
      } else {
         int[] px = matchColours(pixels(base), pixels(icon));
         NativeImage out = toImage(px, base.getWidth(), base.getHeight());
         base.close();
         return out;
      }
   }

   public static int[] matchColours(int[] base, int[] icon) {
      List<Integer> all = new ArrayList();

      for(int c : icon) {
         if (c >>> 24 > 64) {
            all.add(c);
         }
      }

      List<Integer> cols = new ArrayList(all.stream().filter((cx) -> luminance(cx) >= 30.0F).toList());
      if (cols.size() < 2) {
         cols = all;
      }

      if (cols.size() < 2) {
         return base.clone();
      } else {
         cols.sort(Comparator.comparingDouble(WornArmour::luminance));
         float min = Float.MAX_VALUE;
         float max = -1.0F;

         for(int c : base) {
            if (c >>> 24 != 0) {
               float l = luminance(c);
               min = Math.min(min, l);
               max = Math.max(max, l);
            }
         }

         int[] out = base.clone();

         for(int i = 0; i < out.length; ++i) {
            int c = base[i];
            if (c >>> 24 != 0) {
               float t = max > min ? (luminance(c) - min) / (max - min) : 0.5F;
               int pick = (Integer)cols.get(Math.round(t * (float)(cols.size() - 1)));
               out[i] = c & -16777216 | pick & 16777215;
            }
         }

         return out;
      }
   }

   public static int[] pixels(NativeImage img) {
      int w = img.getWidth();
      int h = img.getHeight();
      int[] px = new int[w * h];

      for(int y = 0; y < h; ++y) {
         for(int x = 0; x < w; ++x) {
            px[y * w + x] = img.getColorArgb(x, y);
         }
      }

      return px;
   }

   public static NativeImage toImage(int[] px, int w, int h) {
      NativeImage img = new NativeImage(w, h, true);

      for(int y = 0; y < h; ++y) {
         for(int x = 0; x < w; ++x) {
            img.setColorArgb(x, y, px[y * w + x]);
         }
      }

      return img;
   }

   private static float luminance(int c) {
      return (float)(c >> 16 & 255) * 0.299F + (float)(c >> 8 & 255) * 0.587F + (float)(c & 255) * 0.114F;
   }
}
