package com.awerial.packeditor;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.texture.NativeImage;

public final class AnchorParts {
   public static final List<String[]> PARTS = List.of(new String[]{"block/respawn_anchor_side4.png", null, "Side"}, new String[]{"block/respawn_anchor_top.png", "top", "Top"}, new String[]{"block/respawn_anchor_top_off.png", "top_off", "Top off"}, new String[]{"block/respawn_anchor_bottom.png", "bottom", "Bottom"});

   private AnchorParts() {
   }

   static NativeImage load(String drawing, String path) throws IOException {
      for(String[] p : PARTS) {
         if (p[0].equals(path)) {
            return p[1] == null ? Drawings.load(Category.RESPAWN_ANCHOR, drawing) : Drawings.loadPart(Category.RESPAWN_ANCHOR, drawing, p[1]);
         }
      }

      if (path.startsWith("block/respawn_anchor_side")) {
         NativeImage side = Drawings.load(Category.RESPAWN_ANCHOR, drawing);
         if (side == null) {
            return null;
         } else {
            NativeImage var8;
            try {
               var8 = chargeLevel(side, path);
            } finally {
               side.close();
            }

            return var8;
         }
      } else {
         return null;
      }
   }

   private static NativeImage chargeLevel(NativeImage drawnFull, String levelPath) throws IOException {
      byte[] fullB = Variant.readVanilla("block/respawn_anchor_side4.png");
      byte[] levelB = Variant.readVanilla(levelPath);
      int w = drawnFull.getWidth();
      int h = drawnFull.getHeight();
      NativeImage out = new NativeImage(w, h, true);

      for(int y = 0; y < h; ++y) {
         for(int x = 0; x < w; ++x) {
            out.setColorArgb(x, y, drawnFull.getColorArgb(x, y));
         }
      }

      if (fullB != null && levelB != null) {
         NativeImage full = NativeImage.read(fullB);

         try {
            NativeImage level = NativeImage.read(levelB);

            try {
               int vw = Math.min(full.getWidth(), level.getWidth());
               int vh = Math.min(full.getWidth(), level.getWidth());

               for(int y = 0; y < h; ++y) {
                  for(int x = 0; x < w; ++x) {
                     int vx = x * vw / w;
                     int vy = y * vh / h;
                     int lit = full.getColorArgb(vx, vy);
                     int unlit = level.getColorArgb(vx, vy);
                     if (lit != unlit) {
                        out.setColorArgb(x, y, dim(out.getColorArgb(x, y)));
                     }
                  }
               }
            } catch (Throwable var19) {
               if (level != null) {
                  try {
                     level.close();
                  } catch (Throwable var18) {
                     var19.addSuppressed(var18);
                  }
               }

               throw var19;
            }

            if (level != null) {
               level.close();
            }
         } catch (Throwable var20) {
            if (full != null) {
               try {
                  full.close();
               } catch (Throwable var17) {
                  var20.addSuppressed(var17);
               }
            }

            throw var20;
         }

         if (full != null) {
            full.close();
         }

         return out;
      } else {
         return out;
      }
   }

   private static int dim(int c) {
      int r = (c >> 16 & 255) * 3 / 10;
      int g = (c >> 8 & 255) * 3 / 10;
      int b = (c & 255) * 3 / 10;
      return c & -16777216 | r << 16 | g << 8 | b;
   }
}
