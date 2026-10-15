package com.awerial.packeditor;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.texture.NativeImage;

public final class ImageColours {
   private ImageColours() {
   }

   public static boolean isImage(Path p) {
      String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
      return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".bmp") || n.endsWith(".gif");
   }

   public static int[] palette(Path image) throws IOException {
      Pixels img = read(image);
      int w = img.w;
      int h = img.h;
      Map<Integer, Integer> edge = new HashMap();
      int stepX = Math.max(1, w / 64);
      int stepY = Math.max(1, h / 64);
      boolean seeThrough = false;

      for(int x = 0; x < w; x += stepX) {
         seeThrough |= countEdge(edge, img.getRGB(x, 0)) | countEdge(edge, img.getRGB(x, h - 1));
      }

      for(int y = 0; y < h; y += stepY) {
         seeThrough |= countEdge(edge, img.getRGB(0, y)) | countEdge(edge, img.getRGB(w - 1, y));
      }

      int edgeSamples = edge.values().stream().mapToInt(Integer::intValue).sum();
      List<Integer> backgrounds = seeThrough ? List.of() : edge.entrySet().stream().filter((e) -> (Integer)e.getValue() * 12 >= edgeSamples).map(Map.Entry::getKey).toList();
      int x0 = seeThrough ? 0 : w * 15 / 100;
      int y0 = seeThrough ? 0 : h * 15 / 100;
      int x1 = w - x0;
      int y1 = h - y0;
      int sx = Math.max(1, (x1 - x0) / 96);
      int sy = Math.max(1, (y1 - y0) / 96);
      List<Integer> all = new ArrayList();
      List<Integer> kept = new ArrayList();

      for(int y = y0; y < y1; y += sy) {
         for(int x = x0; x < x1; x += sx) {
            int c = img.getRGB(x, y);
            if (c >>> 24 >= 128) {
               c |= -16777216;
               all.add(c);
               boolean bg = false;

               for(int b : backgrounds) {
                  bg |= distance(c, b) <= 60;
               }

               if (!bg) {
                  kept.add(c);
               }
            }
         }
      }

      List<Integer> use = kept.size() >= all.size() / 10 && kept.size() >= 16 ? kept : all;
      Map<Integer, Integer> freq = new HashMap();

      for(int c : use) {
         count(freq, c);
      }

      int total = use.size();
      List<Integer> common = use.stream().filter((cx) -> (Integer)freq.get(cx & 15790320 | -16777216) * 250 >= total).toList();
      if (common.size() >= use.size() / 2 && common.size() >= 16) {
         use = common;
      }

      if (use.size() < 2) {
         throw new IOException("Couldn't find enough colours in that picture");
      } else {
         int[] out = new int[use.size()];

         for(int i = 0; i < out.length; ++i) {
            out[i] = (Integer)use.get(i);
         }

         return out;
      }
   }

   private static Pixels read(Path image) throws IOException {
      try {
         BufferedImage b = ImageIO.read(image.toFile());
         if (b != null) {
            int w = b.getWidth();
            int h = b.getHeight();
            return new Pixels(w, h, b.getRGB(0, 0, w, h, (int[])null, 0, w));
         }
      } catch (IOException e) {
         throw e;
      } catch (Throwable var6) {
      }

      if (image.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png")) {
         NativeImage n = NativeImage.read(Files.readAllBytes(image));

         Pixels w;
         try {
            w = new Pixels(n.getWidth(), n.getHeight(), WornArmour.pixels(n));
         } catch (Throwable var7) {
            if (n != null) {
               try {
                  n.close();
               } catch (Throwable var4) {
                  var7.addSuppressed(var4);
               }
            }

            throw var7;
         }

         if (n != null) {
            n.close();
         }

         return w;
      } else {
         throw new IOException("That picture type can't be read here, save it as a .png");
      }
   }

   public static int mainColour(Path image) throws IOException {
      int[] px = palette(image);
      float[] weight = new float[24];
      float[][] sum = new float[24][3];

      for(int c : px) {
         int r = c >> 16 & 255;
         int g = c >> 8 & 255;
         int b = c & 255;
         int max = Math.max(r, Math.max(g, b));
         int min = Math.min(r, Math.min(g, b));
         float sat = max == 0 ? 0.0F : (float)(max - min) / (float)max;
         float val = (float)max / 255.0F;
         if (!(sat < 0.2F) && !(val < 0.15F)) {
            float hue = max == r ? (float)(g - b) / (float)(max - min) / 6.0F : (max == g ? ((float)(b - r) / (float)(max - min) + 2.0F) / 6.0F : ((float)(r - g) / (float)(max - min) + 4.0F) / 6.0F);
            if (hue < 0.0F) {
               ++hue;
            }

            int bin = Math.min(23, (int)(hue * 24.0F));
            float wgt = sat * val;
            weight[bin] += wgt;
            sum[bin][0] += (float)r * wgt;
            sum[bin][1] += (float)g * wgt;
            sum[bin][2] += (float)b * wgt;
         }
      }

      int best = -1;

      for(int i = 0; i < 24; ++i) {
         float around = weight[i] + 0.5F * (weight[(i + 23) % 24] + weight[(i + 1) % 24]);
         float bestAround = best < 0 ? 0.0F : weight[best] + 0.5F * (weight[(best + 23) % 24] + weight[(best + 1) % 24]);
         if (weight[i] > 0.0F && around > bestAround) {
            best = i;
         }
      }

      if (best >= 0) {
         float wsum = weight[best];
         return Math.round(sum[best][0] / wsum) << 16 | Math.round(sum[best][1] / wsum) << 8 | Math.round(sum[best][2] / wsum);
      } else {
         long r = 0L;
         long g = 0L;
         long b = 0L;

         for(int c : px) {
            r += (long)(c >> 16 & 255);
            g += (long)(c >> 8 & 255);
            b += (long)(c & 255);
         }

         return (int)(r / (long)px.length) << 16 | (int)(g / (long)px.length) << 8 | (int)(b / (long)px.length);
      }
   }

   private static boolean countEdge(Map<Integer, Integer> m, int argb) {
      if (argb >>> 24 < 128) {
         return true;
      } else {
         count(m, argb);
         return false;
      }
   }

   private static void count(Map<Integer, Integer> m, int rgb) {
      int q = rgb & 15790320 | -16777216;
      m.merge(q, 1, Integer::sum);
   }

   private static int distance(int a, int b) {
      int dr = (a >> 16 & 255) - (b >> 16 & 240);
      int dg = (a >> 8 & 255) - (b >> 8 & 240);
      int db = (a & 255) - (b & 240);
      return (int)Math.sqrt((double)(dr * dr + dg * dg + db * db));
   }

   private static record Pixels(int w, int h, int[] argb) {
      int getRGB(int x, int y) {
         return this.argb[y * this.w + x];
      }
   }
}
