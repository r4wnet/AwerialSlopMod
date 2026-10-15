package com.awerial.packeditor.gui;

import com.awerial.packeditor.AnchorParts;
import com.awerial.packeditor.Category;
import com.awerial.packeditor.Drawings;
import com.awerial.packeditor.ItemTextures;
import com.awerial.packeditor.PackEditorClient;
import com.awerial.packeditor.WornArmour;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public class DrawScreen extends Screen {
   private static final int[] PALETTE = new int[]{-1, -3750202, -7631989, -11908534, -15066598, -16777216, -50373, -30177, -9925, -8519877, -13710272, -16717313, -12867585, -13677569, -7717889, -2540545, -37931, -7644629, -43691, -11141291, -11141121, -171, -5635926, -22016};
   private final PackEditorScreen parent;
   private final Category category;
   private final ItemTextures.Entry item;
   private final String editing;
   private int w;
   private int h;
   private int[] px;
   private final Category.Target worn;
   private int[] iconPx;
   private int[] wornPx;
   private int[] baseWorn;
   private int iw;
   private int ih;
   private int ww;
   private int wh;
   private boolean onWorn;
   private boolean wornTouched;
   private final boolean crystal;
   private int[] cubePx;
   private int[] glassPx;
   private int[] entity;
   private int ew;
   private int eh;
   private int scale;
   private boolean onGlass;
   private int[][] partPx;
   private int[] partW;
   private int[] partH;
   private int part;
   private static final int MAX_FRAMES = 4;
   private final List<int[]> frames;
   private int frame;
   private int frameTime;
   private boolean animatable;
   private final Deque<int[]> undo;
   private final List<Btn> buttons;
   private Tool tool;
   private int color;
   private float hue;
   private float sat;
   private float val;
   private boolean mirror;
   private boolean grid;
   private String name;
   private TextFieldWidget nameField;
   private Text status;
   private int drag;
   private int cell;
   private int cx;
   private int cy;
   private int panelX;
   private int svX;
   private int svY;
   private int svW;
   private int svH;
   private int hueY;
   private int palY;
   private int prevY;

   public static DrawScreen forCrystal(PackEditorScreen parent, NativeImage entityTexture, String editing) {
      return new DrawScreen(parent, entityTexture, editing);
   }

   private DrawScreen(PackEditorScreen parent, NativeImage ent, String editing) {
      super(Text.literal("Draw your own"));
      this.frames = new ArrayList();
      this.frameTime = 4;
      this.undo = new ArrayDeque();
      this.buttons = new ArrayList();
      this.tool = DrawScreen.Tool.PENCIL;
      this.color = -1;
      this.hue = 0.0F;
      this.sat = 0.0F;
      this.val = 1.0F;
      this.grid = true;
      this.status = Text.empty();
      this.parent = parent;
      this.category = Category.END_CRYSTAL;
      this.item = null;
      this.worn = null;
      this.editing = editing;
      this.name = editing != null ? editing : "";
      this.crystal = true;
      this.ew = ent.getWidth();
      this.eh = ent.getHeight();
      this.scale = Math.max(1, this.ew / 64);
      this.entity = new int[this.ew * this.eh];

      for(int y = 0; y < this.eh; ++y) {
         for(int x = 0; x < this.ew; ++x) {
            this.entity[y * this.ew + x] = ent.getColorArgb(x, y);
         }
      }

      ent.close();
      this.w = this.h = 8 * this.scale;
      this.glassPx = this.face(0, 0);
      this.cubePx = this.face(32 * this.scale, 0);
      this.px = this.cubePx;
   }

   private int[] face(int u, int v) {
      int d = 8 * this.scale;
      int[] f = new int[d * d];

      for(int y = 0; y < d; ++y) {
         for(int x = 0; x < d; ++x) {
            f[y * d + x] = this.entity[(v + d + y) * this.ew + u + d + x];
         }
      }

      return f;
   }

   private void wrap(int[] out, int u, int v, int[] f) {
      int d = 8 * this.scale;
      int[][] at = new int[][]{{u + d, v}, {u + 2 * d, v}, {u, v + d}, {u + d, v + d}, {u + 2 * d, v + d}, {u + 3 * d, v + d}};

      for(int[] p : at) {
         for(int y = 0; y < d; ++y) {
            for(int x = 0; x < d; ++x) {
               out[(p[1] + y) * this.ew + p[0] + x] = f[y * d + x];
            }
         }
      }

   }

   private void switchPart(boolean glass) {
      if (glass != this.onGlass) {
         this.onGlass = glass;
         this.px = glass ? this.glassPx : this.cubePx;
         this.undo.clear();
      }
   }

   public static DrawScreen forAnchor(PackEditorScreen parent, NativeImage[] starts, String editing) {
      DrawScreen s = new DrawScreen(parent, Category.RESPAWN_ANCHOR, starts[0], editing);
      s.animatable = false;
      int n = AnchorParts.PARTS.size();
      s.partPx = new int[n][];
      s.partW = new int[n];
      s.partH = new int[n];
      s.partPx[0] = s.iconPx;
      s.partW[0] = s.iw;
      s.partH[0] = s.ih;

      for(int i = 1; i < n; ++i) {
         NativeImage img = starts[i];
         s.partW[i] = img != null ? img.getWidth() : 16;
         s.partH[i] = img != null ? img.getHeight() : 16;
         s.partPx[i] = img != null ? WornArmour.pixels(img) : new int[256];
         if (img != null) {
            img.close();
         }
      }

      return s;
   }

   private void showAnchorPart(int i) {
      if (i != this.part) {
         this.part = i;
         this.px = this.partPx[i];
         this.w = this.partW[i];
         this.h = this.partH[i];
         this.undo.clear();
      }
   }

   private void showWorn(boolean show) {
      if (show != this.onWorn) {
         this.onWorn = show;
         if (show) {
            if (!this.wornTouched) {
               this.wornPx = WornArmour.matchColours(this.baseWorn, this.iconPx);
            }

            this.px = this.wornPx;
            this.w = this.ww;
            this.h = this.wh;
         } else {
            this.px = this.iconPx;
            this.w = this.iw;
            this.h = this.ih;
         }

         this.undo.clear();
      }
   }

   private void matchWorn() {
      this.pushUndo();
      System.arraycopy(WornArmour.matchColours(this.baseWorn, this.iconPx), 0, this.wornPx, 0, this.wornPx.length);
   }

   private void showFrame(int i) {
      this.frame = Math.floorMod(i, this.frames.size());
      this.px = (int[])this.frames.get(this.frame);
      this.undo.clear();
   }

   private void addFrame() {
      if (this.frames.size() >= 4) {
         this.status = Text.literal("4 frames max").formatted(Formatting.GOLD);
      } else {
         this.frames.add(this.frame + 1, (int[])this.px.clone());
         this.showFrame(this.frame + 1);
      }
   }

   private void removeFrame() {
      if (this.frames.size() > 1) {
         this.frames.remove(this.frame);
         this.showFrame(Math.min(this.frame, this.frames.size() - 1));
      }
   }

   public DrawScreen(PackEditorScreen parent, Category category, NativeImage start, String editing) {
      this(parent, category, (ItemTextures.Entry)null, start, editing, category.wornTarget());
   }

   public static DrawScreen forItem(PackEditorScreen parent, ItemTextures.Entry item, NativeImage start, boolean custom) {
      return new DrawScreen(parent, (Category)null, item, start, custom ? item.id() : null, WornArmour.wornTarget(item.id()));
   }

   private DrawScreen(PackEditorScreen parent, Category category, ItemTextures.Entry item, NativeImage start, String editing, Category.Target wornTarget) {
      super(Text.literal("Draw your own"));
      this.frames = new ArrayList();
      this.frameTime = 4;
      this.undo = new ArrayDeque();
      this.buttons = new ArrayList();
      this.tool = DrawScreen.Tool.PENCIL;
      this.color = -1;
      this.hue = 0.0F;
      this.sat = 0.0F;
      this.val = 1.0F;
      this.grid = true;
      this.status = Text.empty();
      this.parent = parent;
      this.category = category;
      this.item = item;
      this.editing = editing;
      this.name = editing != null && item == null ? editing : "";
      this.crystal = false;
      if (start != null) {
         this.w = start.getWidth();
         this.h = start.getHeight();
         this.px = WornArmour.pixels(start);
         start.close();
      } else {
         this.w = this.h = 16;
         this.px = new int[this.w * this.h];
      }

      int savedTime = editing == null ? 0 : (item != null ? ItemTextures.frameTime(item.id()) : Drawings.frameTime(category, editing));
      if (savedTime > 0 && this.h > this.w && this.h % this.w == 0) {
         int n = Math.min(4, this.h / this.w);

         for(int f = 0; f < n; ++f) {
            this.frames.add(Arrays.copyOfRange(this.px, f * this.w * this.w, (f + 1) * this.w * this.w));
         }

         this.px = (int[])this.frames.get(0);
         this.h = this.w;
         this.frameTime = savedTime;
      } else {
         this.frames.add(this.px);
      }

      this.iconPx = this.px;
      this.iw = this.w;
      this.ih = this.h;
      Category.Target found = null;
      if (wornTarget != null) {
         try {
            NativeImage base = WornArmour.vanillaPart(wornTarget);

            try {
               if (base != null) {
                  found = wornTarget;
                  this.ww = base.getWidth();
                  this.wh = base.getHeight();
                  this.baseWorn = WornArmour.pixels(base);
                  NativeImage saved = editing == null ? null : (item != null ? ItemTextures.loadWorn(item.id()) : Drawings.loadWorn(category, editing));
                  if (saved != null) {
                     if (saved.getWidth() == this.ww && saved.getHeight() == this.wh) {
                        this.wornPx = WornArmour.pixels(saved);
                        this.wornTouched = true;
                     }

                     saved.close();
                  }
               }
            } catch (Throwable var13) {
               if (base != null) {
                  try {
                     base.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }
               }

               throw var13;
            }

            if (base != null) {
               base.close();
            }
         } catch (Exception e) {
            PackEditorClient.LOGGER.warn("Couldn't load worn armour to draw on", e);
         }
      }

      this.worn = found;
      this.animatable = found == null;
   }

   protected void init() {
      this.buttons.clear();
      int top = 34;
      int bottom = this.height - 10;
      this.panelX = this.width - 206;
      int areaW = this.panelX - 24;
      int areaH = bottom - top - 6;
      this.cell = Math.max(2, Math.min(areaW / this.w, areaH / this.h));
      this.cx = 12 + (areaW - this.cell * this.w) / 2;
      this.cy = top + (areaH - this.cell * this.h) / 2;
      int pw = 194;
      int bw = (pw - 9) / 4;
      int y = top;
      if (this.crystal) {
         int hw = (pw - 3) / 2;
         this.buttons.add(new Btn(this.panelX, top, hw, 16, "Cube (inside)", () -> this.switchPart(false), !this.onGlass));
         this.buttons.add(new Btn(this.panelX + hw + 3, top, hw, 16, "Glass (frame)", () -> this.switchPart(true), this.onGlass));
         y = top + 20;
      }

      if (this.partPx != null) {
         List<String[]> parts = AnchorParts.PARTS;
         int qw = (pw - 3 * (parts.size() - 1)) / parts.size();

         for(int i = 0; i < parts.size(); ++i) {
            int index = i;
            this.buttons.add(new Btn(this.panelX + i * (qw + 3), y, qw, 16, parts.get(i)[2], () -> this.showAnchorPart(index), this.part == i));
         }

         y += 20;
      }

      if (this.worn != null) {
         int hw = (pw - 3) / 2;
         this.buttons.add(new Btn(this.panelX, y, hw, 16, "Icon (item)", () -> this.showWorn(false), !this.onWorn));
         this.buttons.add(new Btn(this.panelX + hw + 3, y, hw, 16, "Worn (on body)", () -> this.showWorn(true), this.onWorn));
         y += 20;
         if (this.onWorn) {
            this.buttons.add(new Btn(this.panelX, y, pw, 16, "Colour it like my icon", this::matchWorn, false));
            y += 20;
         }
      }

      for(int i = 0; i < DrawScreen.Tool.values().length; ++i) {
         Tool t = DrawScreen.Tool.values()[i];
         this.buttons.add(new Btn(this.panelX + i * (bw + 3), y, bw, 16, t.label, () -> this.tool = t, this.tool == t));
      }

      y += 20;
      this.buttons.add(new Btn(this.panelX, y, bw, 16, "Mirror", () -> this.mirror = !this.mirror, this.mirror));
      this.buttons.add(new Btn(this.panelX + bw + 3, y, bw, 16, "Grid", () -> this.grid = !this.grid, this.grid));
      this.buttons.add(new Btn(this.panelX + 2 * (bw + 3), y, bw, 16, "Undo", this::undo, false));
      this.buttons.add(new Btn(this.panelX + 3 * (bw + 3), y, bw, 16, "Clear", () -> {
         this.pushUndo();
         Arrays.fill(this.px, 0);
      }, false));
      y += 20;
      if (this.animatable) {
         int nav = 16;
         int lw = pw - 2 * nav - 40 - 46 - 12;
         this.buttons.add(new Btn(this.panelX, y, nav, 16, "‹", () -> this.showFrame(this.frame - 1), false));
         int var10003 = this.panelX + nav + 3;
         int var10007 = this.frame + 1;
         this.buttons.add(new Btn(var10003, y, lw, 16, "Frame " + var10007 + "/" + this.frames.size(), () -> this.showFrame(this.frame + 1), this.frames.size() > 1));
         this.buttons.add(new Btn(this.panelX + nav + lw + 6, y, nav, 16, "›", () -> this.showFrame(this.frame + 1), false));
         this.buttons.add(new Btn(this.panelX + 2 * nav + lw + 9, y, 40, 16, "+ Add", this::addFrame, false));
         this.buttons.add(new Btn(this.panelX + 2 * nav + lw + 52, y, 46, 16, "Remove", this::removeFrame, false));
         y += 20;
         if (this.frames.size() > 1) {
            String[] speeds = new String[]{"Slow", "Normal", "Fast"};
            int[] times = new int[]{8, 4, 2};
            int sw3 = (pw - 6) / 3;

            for(int i = 0; i < 3; ++i) {
               int t = times[i];
               this.buttons.add(new Btn(this.panelX + i * (sw3 + 3), y, sw3, 16, speeds[i], () -> this.frameTime = t, this.frameTime == t));
            }

            y += 20;
         }
      }

      y += 2;
      this.svX = this.panelX;
      this.svW = pw;
      this.svY = y + 14;
      this.svH = Math.max(16, Math.min(44, this.height - 150 - this.svY));
      this.hueY = this.svY + this.svH + 4;
      this.palY = this.hueY + 10;
      this.prevY = this.palY + 30 + 4;
      if (this.item == null) {
         this.nameField = new TextFieldWidget(this.textRenderer, this.panelX + 1, bottom - 40, pw - 2, 14, Text.literal("Name"));
         this.nameField.setMaxLength(24);
         this.nameField.setText(this.name);
         this.nameField.setChangedListener((s) -> this.name = s);
         this.nameField.setPlaceholder(Text.literal("Name your drawing...").formatted(Formatting.DARK_GRAY));
         this.addSelectableChild(this.nameField);
      }

      int sw = this.editing != null ? (pw - 6) / 3 : (pw - 3) / 2;
      this.buttons.add(new Btn(this.panelX, bottom - 20, sw, 18, "✔ Save", this::save, true));
      this.buttons.add(new Btn(this.panelX + sw + 3, bottom - 20, sw, 18, "Cancel", this::close, false));
      if (this.editing != null) {
         this.buttons.add(new Btn(this.panelX + 2 * (sw + 3), bottom - 20, sw, 18, this.item != null ? "Reset" : "Delete", this::delete, false));
      }

   }

   private void pushUndo() {
      if (this.onWorn) {
         this.wornTouched = true;
      }

      this.undo.push((int[])this.px.clone());

      while(this.undo.size() > 60) {
         this.undo.removeLast();
      }

   }

   private void undo() {
      if (!this.undo.isEmpty()) {
         System.arraycopy(this.undo.pop(), 0, this.px, 0, this.px.length);
      }

   }

   private void set(int x, int y, int c) {
      if (x >= 0 && y >= 0 && x < this.w && y < this.h) {
         this.px[y * this.w + x] = c;
         if (this.mirror) {
            this.px[y * this.w + (this.w - 1 - x)] = c;
         }

      }
   }

   private void fill(int x, int y, int c) {
      int target = this.px[y * this.w + x];
      if (target != c) {
         Deque<int[]> stack = new ArrayDeque();
         stack.push(new int[]{x, y});

         while(!stack.isEmpty()) {
            int[] p = (int[])stack.pop();
            int qx = p[0];
            int qy = p[1];
            if (qx >= 0 && qy >= 0 && qx < this.w && qy < this.h && this.px[qy * this.w + qx] == target) {
               this.px[qy * this.w + qx] = c;
               stack.push(new int[]{qx + 1, qy});
               stack.push(new int[]{qx - 1, qy});
               stack.push(new int[]{qx, qy + 1});
               stack.push(new int[]{qx, qy - 1});
            }
         }

      }
   }

   private int[] cellAt(double mx, double my) {
      int x = (int)Math.floor((mx - (double)this.cx) / (double)this.cell);
      int y = (int)Math.floor((my - (double)this.cy) / (double)this.cell);
      return x >= 0 && y >= 0 && x < this.w && y < this.h ? new int[]{x, y} : null;
   }

   private void paint(double mx, double my, boolean erase) {
      int[] c = this.cellAt(mx, my);
      if (c != null) {
         if (!erase && this.tool != DrawScreen.Tool.ERASER) {
            if (this.tool == DrawScreen.Tool.PENCIL) {
               this.set(c[0], c[1], this.color);
            }
         } else {
            this.set(c[0], c[1], 0);
         }

      }
   }

   private void setColor(int argb) {
      this.color = argb | -16777216;
      float r = (float)(this.color >> 16 & 255) / 255.0F;
      float g = (float)(this.color >> 8 & 255) / 255.0F;
      float b = (float)(this.color & 255) / 255.0F;
      float max = Math.max(r, Math.max(g, b));
      float min = Math.min(r, Math.min(g, b));
      float d = max - min;
      if (d > 0.0F) {
         float hh;
         if (max == r) {
            hh = (g - b) / d % 6.0F / 6.0F;
         } else if (max == g) {
            hh = ((b - r) / d + 2.0F) / 6.0F;
         } else {
            hh = ((r - g) / d + 4.0F) / 6.0F;
         }

         this.hue = hh < 0.0F ? hh + 1.0F : hh;
      }

      this.sat = max == 0.0F ? 0.0F : d / max;
      this.val = max;
   }

   private void save() {
      String n = this.item != null ? this.item.id() : Drawings.cleanName(this.name);
      if (n.isEmpty()) {
         this.status = Text.literal("Give your drawing a name first").formatted(Formatting.RED);
      } else {
         int ow = this.crystal ? this.ew : this.iw;
         int oh = this.crystal ? this.eh : this.ih;
         int[] out = this.iconPx;
         boolean animated = this.animatable && this.frames.size() > 1;
         if (animated) {
            oh = this.ih * this.frames.size();
            out = new int[ow * oh];

            for(int f = 0; f < this.frames.size(); ++f) {
               System.arraycopy(this.frames.get(f), 0, out, f * ow * this.ih, ow * this.ih);
            }
         }

         int savedFrameTime = animated ? this.frameTime : 0;
         if (this.crystal) {
            out = (int[])this.entity.clone();
            this.wrap(out, 0, 0, this.glassPx);
            this.wrap(out, 32 * this.scale, 0, this.cubePx);
         }

         try {
            NativeImage img = WornArmour.toImage(out, ow, oh);

            try {
               NativeImage wornImg = this.worn != null && this.wornTouched ? WornArmour.toImage(this.wornPx, this.ww, this.wh) : null;

               try {
                  if (this.item != null) {
                     ItemTextures.save(n, img, wornImg);
                     ItemTextures.saveFrameTime(n, savedFrameTime);
                  } else {
                     Drawings.save(this.category, n, img);
                     Drawings.saveFrameTime(this.category, n, savedFrameTime);
                     if (this.worn != null) {
                        Drawings.saveWorn(this.category, n, wornImg);
                     }

                     if (this.partPx != null) {
                        List<String[]> parts = AnchorParts.PARTS;

                        for(int i = 1; i < parts.size(); ++i) {
                           NativeImage p = WornArmour.toImage(this.partPx[i], this.partW[i], this.partH[i]);

                           try {
                              Drawings.savePart(this.category, n, ((String[])parts.get(i))[1], p);
                           } catch (Throwable var17) {
                              if (p != null) {
                                 try {
                                    p.close();
                                 } catch (Throwable var16) {
                                    var17.addSuppressed(var16);
                                 }
                              }

                              throw var17;
                           }

                           if (p != null) {
                              p.close();
                           }
                        }
                     }
                  }
               } catch (Throwable var18) {
                  if (wornImg != null) {
                     try {
                        wornImg.close();
                     } catch (Throwable var15) {
                        var18.addSuppressed(var15);
                     }
                  }

                  throw var18;
               }

               if (wornImg != null) {
                  wornImg.close();
               }
            } catch (Throwable var19) {
               if (img != null) {
                  try {
                     img.close();
                  } catch (Throwable var14) {
                     var19.addSuppressed(var14);
                  }
               }

               throw var19;
            }

            if (img != null) {
               img.close();
            }
         } catch (Exception e) {
            PackEditorClient.LOGGER.error("Couldn't save drawing", e);
            this.status = Text.literal("Couldn't save: " + e.getMessage()).formatted(Formatting.RED);
            return;
         }

         if (this.item != null) {
            this.parent.onItemDrawn(this.item, true);
         } else {
            this.parent.onDrawingSaved(this.category, n);
         }

         this.client.setScreen(this.parent);
      }
   }

   private void delete() {
      try {
         if (this.item != null) {
            ItemTextures.delete(this.item.id());
         } else {
            Drawings.delete(this.category, this.editing);
         }
      } catch (Exception e) {
         this.status = Text.literal("Couldn't delete: " + e.getMessage()).formatted(Formatting.RED);
         return;
      }

      if (this.item != null) {
         this.parent.onItemDrawn(this.item, false);
      } else {
         this.parent.onDrawingDeleted(this.category, this.editing);
      }

      this.client.setScreen(this.parent);
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      double mx = click.x();
      double my = click.y();
      if (click.button() == 0) {
         for(Btn b : this.buttons) {
            if (b.contains(mx, my)) {
               b.action().run();
               if (this.client.currentScreen == this) {
                  this.clearAndInit();
               }

               return true;
            }
         }

         if (mx >= (double)this.svX && mx < (double)(this.svX + this.svW) && my >= (double)this.svY && my < (double)(this.svY + this.svH)) {
            this.drag = 3;
            this.pickColor(mx, my);
            return true;
         }

         if (mx >= (double)this.svX && mx < (double)(this.svX + this.svW) && my >= (double)(this.hueY - 2) && my < (double)(this.hueY + 8)) {
            this.drag = 4;
            this.pickColor(mx, my);
            return true;
         }

         for(int i = 0; i < PALETTE.length; ++i) {
            int sx = this.svX + i % 12 * 16;
            int sy = this.palY + i / 12 * 15;
            if (mx >= (double)sx && mx < (double)(sx + 14) && my >= (double)sy && my < (double)(sy + 13)) {
               this.setColor(PALETTE[i]);
               if (this.tool == DrawScreen.Tool.ERASER || this.tool == DrawScreen.Tool.PICK) {
                  this.tool = DrawScreen.Tool.PENCIL;
               }

               this.clearAndInit();
               return true;
            }
         }
      }

      int[] c = this.cellAt(mx, my);
      if (c != null && (click.button() == 0 || click.button() == 1)) {
         if (click.button() == 0 && this.tool == DrawScreen.Tool.PICK) {
            if (this.px[c[1] * this.w + c[0]] >>> 24 != 0) {
               this.setColor(this.px[c[1] * this.w + c[0]]);
            }

            this.tool = DrawScreen.Tool.PENCIL;
            this.clearAndInit();
            return true;
         } else {
            this.pushUndo();
            if (click.button() == 0 && this.tool == DrawScreen.Tool.FILL) {
               this.fill(c[0], c[1], this.color);
               return true;
            } else {
               this.drag = click.button() == 1 ? 2 : 1;
               this.paint(mx, my, this.drag == 2);
               return true;
            }
         }
      } else {
         return super.mouseClicked(click, doubled);
      }
   }

   public boolean mouseDragged(Click click, double dx, double dy) {
      if (this.drag != 1 && this.drag != 2) {
         if (this.drag != 3 && this.drag != 4) {
            return super.mouseDragged(click, dx, dy);
         } else {
            this.pickColor(click.x(), click.y());
            return true;
         }
      } else {
         this.paint(click.x(), click.y(), this.drag == 2);
         return true;
      }
   }

   public boolean mouseReleased(Click click) {
      this.drag = 0;
      return super.mouseReleased(click);
   }

   private void pickColor(double mx, double my) {
      if (this.drag == 3) {
         this.sat = (float)Math.max((double)0.0F, Math.min((double)1.0F, (mx - (double)this.svX) / (double)(this.svW - 1)));
         this.val = (float)Math.max((double)0.0F, Math.min((double)1.0F, (double)1.0F - (my - (double)this.svY) / (double)(this.svH - 1)));
      } else {
         this.hue = (float)Math.max((double)0.0F, Math.min((double)1.0F, (mx - (double)this.svX) / (double)(this.svW - 1)));
      }

      this.color = -16777216 | hsv(this.hue, this.sat, this.val);
      if (this.tool == DrawScreen.Tool.ERASER || this.tool == DrawScreen.Tool.PICK) {
         this.tool = DrawScreen.Tool.PENCIL;
      }

   }

   public boolean keyPressed(KeyInput input) {
      if (input.hasCtrl() && input.key() == 90) {
         this.undo();
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
      super.render(ctx, mouseX, mouseY, delta);
      double time = (double)Util.getMeasuringTimeMs() / (double)1000.0F;
      PackEditorScreen.round(ctx, 6, 6, this.width - 12, this.height - 12, 1880100888);
      PackEditorScreen.outline(ctx, 6, 6, this.width - 12, this.height - 12, 1090519039);
      String what = this.crystal ? "End Crystal · " + (this.onGlass ? "Glass" : "Cube") : (this.item != null ? (new ItemStack(this.item.item())).getName().getString() : (this.category.material() != null ? (this.category.material().equals("diamond") ? "Diamond " : "Netherite ") + this.category.displayName : this.category.displayName));
      if (this.worn != null) {
         what = what + (this.onWorn ? " · Worn" : " · Icon");
      }

      if (this.partPx != null) {
         what = what + " · " + ((String[])AnchorParts.PARTS.get(this.part))[2];
      }

      Text title = Text.literal("✎ Draw your own ").formatted(Formatting.BOLD).setStyle(Style.EMPTY.withBold(true).withColor(PackEditorScreen.accent(time * 0.15) & 16777215)).append(Text.literal("— " + what + "  (" + this.w + "x" + this.h + ")").setStyle(Style.EMPTY.withBold(false).withColor(13222364)));
      ctx.drawText(this.textRenderer, title, 14, 14, -1, true);
      Text right = (Text)(!this.status.getString().isEmpty() ? this.status : (this.onWorn ? Text.literal("the armour unfolded: each block is one side of it").formatted(Formatting.DARK_GRAY) : Text.literal("left: draw • right: erase • Ctrl+Z: undo").formatted(Formatting.DARK_GRAY)));
      if (14 + this.textRenderer.getWidth(title) + 16 + this.textRenderer.getWidth(right) <= this.width - 14) {
         ctx.drawText(this.textRenderer, right, this.width - 14 - this.textRenderer.getWidth(right), 14, -1, false);
      }

      for(int y = 0; y < this.h; ++y) {
         for(int x = 0; x < this.w; ++x) {
            int x0 = this.cx + x * this.cell;
            int y0 = this.cy + y * this.cell;
            int bg = (x + y & 1) == 0 ? -14015178 : -14541268;
            ctx.fill(x0, y0, x0 + this.cell, y0 + this.cell, bg);
            int c = this.px[y * this.w + x];
            if (c >>> 24 != 0) {
               ctx.fill(x0, y0, x0 + this.cell, y0 + this.cell, c);
            }
         }
      }

      if (this.grid && this.cell >= 4) {
         for(int x = 0; x <= this.w; ++x) {
            ctx.fill(this.cx + x * this.cell, this.cy, this.cx + x * this.cell + 1, this.cy + this.h * this.cell, 822083583);
         }

         for(int y = 0; y <= this.h; ++y) {
            ctx.fill(this.cx, this.cy + y * this.cell, this.cx + this.w * this.cell, this.cy + y * this.cell + 1, 822083583);
         }
      }

      if (this.mirror) {
         ctx.fill(this.cx + this.w * this.cell / 2, this.cy - 3, this.cx + this.w * this.cell / 2 + 1, this.cy + this.h * this.cell + 3, -1593873451);
      }

      int[] hover = this.cellAt((double)mouseX, (double)mouseY);
      if (hover != null) {
         PackEditorScreen.outline(ctx, this.cx + hover[0] * this.cell, this.cy + hover[1] * this.cell, this.cell + 1, this.cell + 1, -1);
      }

      PackEditorScreen.outline(ctx, this.cx - 1, this.cy - 1, this.w * this.cell + 2, this.h * this.cell + 2, PackEditorScreen.accent(time * 0.1));

      for(Btn b : this.buttons) {
         boolean hov = b.contains((double)mouseX, (double)mouseY);
         boolean save = b.label().startsWith("✔");
         if (save) {
            ctx.fillGradient(b.x(), b.y(), b.x() + b.w(), b.y() + b.h(), PackEditorScreen.accent(time * 0.15), PackEditorScreen.accent(time * 0.15 + 0.33));
         } else {
            PackEditorScreen.round(ctx, b.x(), b.y(), b.w(), b.h(), b.selected() ? -12964779 : (hov ? -14542033 : -15264993));
         }

         PackEditorScreen.outline(ctx, b.x(), b.y(), b.w(), b.h(), b.selected() && !save ? PackEditorScreen.accent(time * 0.2) : (hov ? -1 : -13884103));
         ctx.drawCenteredTextWithShadow(this.textRenderer, b.label(), b.x() + b.w() / 2, b.y() + (b.h() - 8) / 2, -1);
      }

      ctx.fill(this.svX, this.svY - 13, this.svX + 11, this.svY - 2, this.color);
      PackEditorScreen.outline(ctx, this.svX, this.svY - 13, 11, 11, -1);
      ctx.drawText(this.textRenderer, String.format("#%06X", this.color & 16777215), this.svX + 15, this.svY - 11, -3554852, false);

      for(int i = 0; i < this.svW; ++i) {
         ctx.fillGradient(this.svX + i, this.svY, this.svX + i + 1, this.svY + this.svH, -16777216 | hsv(this.hue, (float)i / (float)(this.svW - 1), 1.0F), -16777216);
         ctx.fill(this.svX + i, this.hueY, this.svX + i + 1, this.hueY + 6, -16777216 | hsv((float)i / (float)(this.svW - 1), 1.0F, 1.0F));
      }

      int kx = this.svX + Math.round(this.sat * (float)(this.svW - 1));
      int ky = this.svY + Math.round((1.0F - this.val) * (float)(this.svH - 1));
      PackEditorScreen.outline(ctx, kx - 2, ky - 2, 5, 5, -1);
      int hx = this.svX + Math.round(this.hue * (float)(this.svW - 1));
      PackEditorScreen.round(ctx, hx - 2, this.hueY - 2, 5, 10, -1);

      for(int i = 0; i < PALETTE.length; ++i) {
         int sx = this.svX + i % 12 * 16;
         int sy = this.palY + i / 12 * 15;
         PackEditorScreen.round(ctx, sx, sy, 14, 13, PALETTE[i]);
         PackEditorScreen.outline(ctx, sx, sy, 14, 13, PALETTE[i] == this.color ? -1 : 1358954495);
      }

      int space = this.height - 52 - this.prevY;
      if (this.crystal && space >= 20) {
         ctx.drawText(this.textRenderer, "Preview", this.svX, this.prevY, -7502429, false);
         int x = this.svX + 44;

         for(int k : new int[]{1, 2}) {
            int size = this.w * 2 * k;
            if (size > space - 2 || x + size > this.svX + this.svW) {
               break;
            }

            int off = this.w * k / 2;

            for(int y = 0; y < this.h; ++y) {
               for(int xx = 0; xx < this.w; ++xx) {
                  int c = this.cubePx[y * this.w + xx];
                  if (c >>> 24 != 0) {
                     ctx.fill(x + off + xx * k, this.prevY + off + y * k, x + off + xx * k + k, this.prevY + off + y * k + k, c);
                  }
               }
            }

            for(int y = 0; y < this.h; ++y) {
               for(int xx = 0; xx < this.w; ++xx) {
                  int c = this.glassPx[y * this.w + xx];
                  if (c >>> 24 != 0) {
                     ctx.fill(x + xx * 2 * k, this.prevY + y * 2 * k, x + xx * 2 * k + 2 * k, this.prevY + y * 2 * k + 2 * k, c);
                  }
               }
            }

            x += size + 8;
         }
      } else if (space >= 20) {
         ctx.drawText(this.textRenderer, "Preview", this.svX, this.prevY, -7502429, false);
         int x = this.svX + 44;
         int[] shown = this.frames.size() > 1 && this.animatable ? (int[])this.frames.get((int)(Util.getMeasuringTimeMs() / ((long)this.frameTime * 50L) % (long)this.frames.size())) : this.px;

         for(int s : new int[]{1, 2, 4}) {
            if (this.h * s > space - 2 || x + this.w * s > this.svX + this.svW) {
               break;
            }

            for(int y = 0; y < this.h; ++y) {
               for(int xx = 0; xx < this.w; ++xx) {
                  int c = shown[y * this.w + xx];
                  if (c >>> 24 != 0) {
                     ctx.fill(x + xx * s, this.prevY + y * s, x + xx * s + s, this.prevY + y * s + s, c);
                  }
               }
            }

            x += this.w * s + 8;
         }
      }

      if (this.item != null) {
         ctx.drawText(this.textRenderer, this.worn != null ? "Changes the item and the worn armour" : "Changes this item everywhere", this.panelX, this.height - 46, -7502429, false);
      } else {
         PackEditorScreen.round(ctx, this.panelX - 1, this.nameField.getY() - 2, 196, 18, -16053999);
         PackEditorScreen.outline(ctx, this.panelX - 1, this.nameField.getY() - 2, 196, 18, this.nameField.isFocused() ? PackEditorScreen.accent(time * 0.2) : -13884103);
         this.nameField.render(ctx, mouseX, mouseY, delta);
      }
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

   private static enum Tool {
      PENCIL("Pencil"),
      ERASER("Eraser"),
      FILL("Fill"),
      PICK("Pick");

      final String label;

      private Tool(String label) {
         this.label = label;
      }

   }

   private static record Btn(int x, int y, int w, int h, String label, Runnable action, boolean selected) {
      boolean contains(double mx, double my) {
         return mx >= (double)this.x && mx < (double)(this.x + this.w) && my >= (double)this.y && my < (double)(this.y + this.h);
      }
   }
}
