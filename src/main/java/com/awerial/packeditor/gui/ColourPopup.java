package com.awerial.packeditor.gui;

import java.util.function.IntConsumer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

final class ColourPopup {
   static final int W = 132;
   static final int H = 92;
   private static final int[] PRESETS = new int[]{16726843, 16747039, 16767291, 8257339, 3066944, 59903, 3909631, 3099647, 9059327, 14236671, 16739285, 16777215, 9079434, 2763306};
   private final String title;
   private final IntConsumer onChange;
   final int x;
   final int y;
   private int color;
   private float hue;
   private float sat;
   private float val;
   private int drag;

   ColourPopup(String title, int color, int x, int y, int minX, int maxX, int minY, int maxY, IntConsumer onChange) {
      this.title = title;
      this.onChange = onChange;
      this.x = Math.max(minX, Math.min(x, maxX - 132));
      this.y = Math.max(minY, Math.min(y, maxY - 92));
      this.setColor(color);
   }

   private void setColor(int c) {
      this.color = c & 16777215;
      float r = (float)(c >> 16 & 255) / 255.0F;
      float g = (float)(c >> 8 & 255) / 255.0F;
      float b = (float)(c & 255) / 255.0F;
      float max = Math.max(r, Math.max(g, b));
      float min = Math.min(r, Math.min(g, b));
      float d = max - min;
      if (d > 0.0F) {
         float h;
         if (max == r) {
            h = (g - b) / d % 6.0F / 6.0F;
         } else if (max == g) {
            h = ((b - r) / d + 2.0F) / 6.0F;
         } else {
            h = ((r - g) / d + 4.0F) / 6.0F;
         }

         this.hue = h < 0.0F ? h + 1.0F : h;
      }

      this.sat = max == 0.0F ? 0.0F : d / max;
      this.val = max;
   }

   boolean contains(double mx, double my) {
      return mx >= (double)this.x && mx < (double)(this.x + 132) && my >= (double)this.y && my < (double)(this.y + 92);
   }

   void render(DrawContext ctx, TextRenderer tr, double time) {
      PackEditorScreen.round(ctx, this.x, this.y, 132, 92, -267251942);
      PackEditorScreen.outline(ctx, this.x, this.y, 132, 92, PackEditorScreen.accent(time));
      ctx.drawText(tr, Text.literal(this.title).formatted(Formatting.BOLD), this.x + 5, this.y + 4, -1, false);
      PackEditorScreen.round(ctx, this.x + 132 - 18, this.y + 3, 13, 10, -16777216 | this.color);
      int sx = this.x + 5;
      int sy = this.y + 16;
      int sw = 122;
      int sh = 36;

      for(int i = 0; i < sw; ++i) {
         ctx.fillGradient(sx + i, sy, sx + i + 1, sy + sh, -16777216 | hsv(this.hue, (float)i / (float)(sw - 1), 1.0F), -16777216);
         ctx.fill(sx + i, sy + sh + 4, sx + i + 1, sy + sh + 9, -16777216 | hsv((float)i / (float)(sw - 1), 1.0F, 1.0F));
      }

      int kx = sx + Math.round(this.sat * (float)(sw - 1));
      int ky = sy + Math.round((1.0F - this.val) * (float)(sh - 1));
      PackEditorScreen.outline(ctx, kx - 2, ky - 2, 5, 5, -1);
      int hx = sx + Math.round(this.hue * (float)(sw - 1));
      ctx.fill(hx - 1, sy + sh + 3, hx + 2, sy + sh + 10, -1);
      int py = sy + sh + 14;

      for(int i = 0; i < PRESETS.length; ++i) {
         int px = sx + i % 7 * 18;
         int ppy = py + i / 7 * 11;
         PackEditorScreen.round(ctx, px, ppy, 16, 9, -16777216 | PRESETS[i]);
         if (PRESETS[i] == this.color) {
            PackEditorScreen.outline(ctx, px, ppy, 16, 9, -1);
         }
      }

   }

   boolean click(double mx, double my) {
      if (!this.contains(mx, my)) {
         return false;
      } else {
         int sx = this.x + 5;
         int sy = this.y + 16;
         int sw = 122;
         int sh = 36;
         if (my >= (double)sy && my < (double)(sy + sh)) {
            this.drag = 1;
         } else {
            if (!(my >= (double)(sy + sh + 2)) || !(my < (double)(sy + sh + 11))) {
               int py = sy + sh + 14;

               for(int i = 0; i < PRESETS.length; ++i) {
                  int px = sx + i % 7 * 18;
                  int ppy = py + i / 7 * 11;
                  if (mx >= (double)px && mx < (double)(px + 16) && my >= (double)ppy && my < (double)(ppy + 9)) {
                     this.setColor(PRESETS[i]);
                     this.onChange.accept(this.color);
                  }
               }

               return true;
            }

            this.drag = 2;
         }

         this.move(mx, my);
         return true;
      }
   }

   boolean dragging() {
      return this.drag != 0;
   }

   void move(double mx, double my) {
      int sx = this.x + 5;
      int sy = this.y + 16;
      int sw = 122;
      int sh = 36;
      if (this.drag == 1) {
         this.sat = (float)Math.max((double)0.0F, Math.min((double)1.0F, (mx - (double)sx) / (double)(sw - 1)));
         this.val = (float)Math.max((double)0.0F, Math.min((double)1.0F, (double)1.0F - (my - (double)sy) / (double)(sh - 1)));
      } else {
         if (this.drag != 2) {
            return;
         }

         this.hue = (float)Math.max((double)0.0F, Math.min((double)1.0F, (mx - (double)sx) / (double)(sw - 1)));
      }

      this.color = hsv(this.hue, this.sat, this.val);
      this.onChange.accept(this.color);
   }

   void release() {
      this.drag = 0;
   }

   private static int hsv(float h, float s, float v) {
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

      return Math.round(r * 255.0F) << 16 | Math.round(g * 255.0F) << 8 | Math.round(b * 255.0F);
   }
}
