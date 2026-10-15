package com.awerial.packeditor.gui;

import com.awerial.packeditor.CombatTracker;
import com.awerial.packeditor.Effects;
import com.awerial.packeditor.Group;
import com.awerial.packeditor.PackEditorClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

final class EffectsPanel {
   private static final int ROW = 20;
   private static final int[] PRESETS = new int[]{16726843, 16747039, 16767291, 8257339, 3066944, 59903, 3909631, 3099647, 9059327, 14236671, 16739285, 16777215, 9079434, 2763306};
   private final TextRenderer tr;
   private final List<Row> rows = new ArrayList();
   private final List<Hit> hits = new ArrayList();
   private int x;
   private int y;
   private int w;
   private int bottom;
   private String message;
   private long messageUntil;
   private Swatch editing;
   private int pkX;
   private int pkY;
   private float hue;
   private float sat;
   private float val;
   private Slider draggingSlider;
   private Hit draggingHit;
   private int pickerDrag;
   private boolean capturingKey;
   private static final int PW = 132;
   private static final int PH = 92;

   EffectsPanel(TextRenderer tr) {
      this.tr = tr;
   }

   void layout(Group group, int x, int y, int w, int bottom) {
      this.bottom = bottom;
      this.x = x;
      this.y = y;
      this.w = w;
      this.rows.clear();
      this.editing = null;
      switch (group) {
         case EFFECTS_COMBAT:
            this.rows.add((new Row("Totem pop counter", "Shows ×3 next to the name of anyone who popped totems (resets when they die). Key: set a key that resets the counts (Esc clears it)")).toggle(() -> Effects.popCounter, (v) -> {
               Effects.popCounter = v;
               if (!v) {
                  CombatTracker.resetPops();
               }

            }).swatch("Pop counter", () -> Effects.popColor, (c) -> Effects.popColor = c, () -> !Effects.popColorByCount).button("Reset", () -> {
               CombatTracker.resetPops();
               this.flash("Pop counts reset");
            }).keyButton().dropdown(() -> Effects.popCounter));
            this.rows.add((new Row("↳ Colour by pops", "Lime for 1-2 pops, dark green 3-4, yellow 5-6, orange 7-8, red 9+. Off: your own colour")).toggle(() -> Effects.popColorByCount, (v) -> Effects.popColorByCount = v).when(() -> Effects.popCounter));
            this.rows.add((new Row("Damage tint", "The colour players and mobs flash when they're hit")).toggle(() -> Effects.damageTint, (v) -> Effects.damageTint = v).swatch("Damage tint", () -> Effects.tintColor, (c) -> Effects.tintColor = c).dropdown(() -> Effects.damageTint));
            this.rows.add((new Row("↳ Gradient", "Two colours at once: your first colour at the head fading into this one at the feet (not with Iris shaderpacks)")).toggle(() -> Effects.tintGradient, (v) -> Effects.tintGradient = v).swatch("Gradient colour", () -> Effects.tintColor2, (c) -> Effects.tintColor2 = c).when(() -> Effects.damageTint));
            this.rows.add((new Row("↳ Opacity", "How strong the tint is: 0% invisible, 100% solid colour (vanilla red is about 30%)")).slider((double)0.0F, (double)100.0F, (double)30.0F, (v) -> Math.round(v) + "%", () -> (double)Effects.tintOpacity, (v) -> Effects.tintOpacity = (int)Math.round(v)).when(() -> Effects.damageTint));
            this.rows.add((new Row("↳ Show on", "Tint the player, their armour, or both")).choices(new String[]{"Player", "Armour", "Both"}, () -> Effects.tintTarget.ordinal(), (i) -> Effects.tintTarget = Effects.TintTarget.values()[i]).when(() -> Effects.damageTint));
            this.rows.add((new Row("Hit particles", "Crit, sweep and sharpness particles in your colour")).toggle(() -> Effects.hitParticles, (v) -> Effects.hitParticles = v).swatch("Hit particles", () -> Effects.hitColor, (c) -> Effects.hitColor = c));
            this.rows.add((new Row("Crystal glow outline", "Placed end crystals glow through walls in your colour (only you see it)")).toggle(() -> Effects.crystalOutline, (v) -> Effects.crystalOutline = v).swatch("Crystal outline", () -> Effects.outlineColor, (c) -> Effects.outlineColor = c));
            this.rows.add((new Row("Kill effect", "Plays on your screen when a player you hit dies")).choices(new String[]{"Off", "Lightning", "Firework", "Both"}, () -> Effects.killEffect.ordinal(), (i) -> Effects.killEffect = Effects.KillEffect.values()[i]).swatch("Firework", () -> Effects.killColor, (c) -> Effects.killColor = c, () -> Effects.killEffect == Effects.KillEffect.FIREWORK || Effects.killEffect == Effects.KillEffect.BOTH));
            break;
         case EFFECTS_WEAPONS:
            this.rows.add((new Row("Mace smash ring", "A shockwave ring where a mace smash lands")).toggle(() -> Effects.maceRing, (v) -> Effects.maceRing = v).swatch("Mace ring", () -> Effects.maceColor, (c) -> Effects.maceColor = c));
            this.rows.add((new Row("Heavy smash boom", "A bigger, deeper sound when a mace smash comes from high up")).toggle(() -> Effects.maceBoom, (v) -> Effects.maceBoom = v));
            this.rows.add((new Row("Lunge ring", "Spears with Lunge: a see-through ring under your crosshair fills white until your next jab can lunge (resets the moment you swap items, like the game)")).toggle(() -> Effects.spearCharge, (v) -> Effects.spearCharge = v));
            break;
         case EFFECTS_TOTEMS:
            this.rows.add((new Row("Totem particles", "The burst when a totem pops, in your colour")).toggle(() -> Effects.totemParticles, (v) -> Effects.totemParticles = v).swatch("Totem particles", () -> Effects.totemColor, (c) -> Effects.totemColor = c));
            this.rows.add((new Row("Pop animation", "The totem that flies up your screen: normal, twice as fast, or none")).choices(new String[]{"Normal", "Short", "None"}, () -> Effects.popAnimation.ordinal(), (i) -> Effects.popAnimation = Effects.PopAnimation.values()[i]));
            this.rows.add((new Row("Armour durability warning", "Armour under 10% flashes next to your hotbar")).toggle(() -> Effects.durabilityWarning, (v) -> Effects.durabilityWarning = v));
            break;
         case EFFECTS_HANDS:
            Row main = new Row("Main hand", (String)null);
            main.heading = true;
            this.rows.add(main);
            this.rows.add((new Row("  Left / right", (String)null)).slider((double)-0.5F, (double)0.5F, () -> (double)Effects.mainX, (v) -> Effects.mainX = (float)v));
            this.rows.add((new Row("  Up / down", (String)null)).slider((double)-0.5F, (double)0.5F, () -> (double)Effects.mainY, (v) -> Effects.mainY = (float)v));
            this.rows.add((new Row("  Near / far", (String)null)).slider((double)-0.5F, (double)0.5F, () -> (double)Effects.mainZ, (v) -> Effects.mainZ = (float)v));
            this.rows.add((new Row("  Size", (String)null)).slider(0.3, (double)1.5F, () -> (double)Effects.mainSize, (v) -> Effects.mainSize = (float)v));
            Row off = (new Row("Off hand", (String)null)).button("Reset both", () -> {
               Effects.offZ = 0.0F;
               Effects.offY = 0.0F;
               Effects.offX = 0.0F;
               Effects.mainZ = 0.0F;
               Effects.mainY = 0.0F;
               Effects.mainX = 0.0F;
               Effects.offSize = 1.0F;
               Effects.mainSize = 1.0F;
               this.flash("Hands back to normal");
            });
            off.heading = true;
            this.rows.add(off);
            this.rows.add((new Row("  Left / right", (String)null)).slider((double)-0.5F, (double)0.5F, () -> (double)Effects.offX, (v) -> Effects.offX = (float)v));
            this.rows.add((new Row("  Up / down", (String)null)).slider((double)-0.5F, (double)0.5F, () -> (double)Effects.offY, (v) -> Effects.offY = (float)v));
            this.rows.add((new Row("  Near / far", (String)null)).slider((double)-0.5F, (double)0.5F, () -> (double)Effects.offZ, (v) -> Effects.offZ = (float)v));
            this.rows.add((new Row("  Size", (String)null)).slider(0.3, (double)1.5F, () -> (double)Effects.offSize, (v) -> Effects.offSize = (float)v));
      }

   }

   private void flash(String text) {
      this.message = text;
      this.messageUntil = System.currentTimeMillis() + 2500L;
   }

   List<Text> render(DrawContext ctx, int mx, int my, double time) {
      this.hits.clear();
      List<Text> tip = null;
      List<Row> shown = this.rows.stream().filter((rx) -> rx.visible.getAsBoolean()).toList();
      int rowH = shown.isEmpty() ? 20 : Math.max(15, Math.min(20, (this.bottom - this.y - 11) / shown.size()));

      for(int i = 0; i < shown.size(); ++i) {
         Row r = (Row)shown.get(i);
         int ry = this.y + i * rowH;
         boolean hover = this.editing == null && mx >= this.x && mx < this.x + this.w && my >= ry && my < ry + rowH - 2;
         if (!r.heading) {
            PackEditorScreen.round(ctx, this.x, ry, this.w, rowH - 2, hover ? 1090519039 : 637534207);
         }

         int label = r.heading ? -1 : (hover ? -1 : -1645585);
         Text lt = r.heading ? Text.literal(r.label).formatted(Formatting.BOLD) : Text.literal(r.label);
         if (r.expanded != null) {
            String var10000 = r.label;
            lt = Text.literal(var10000 + (r.expanded.getAsBoolean() ? " ▾" : " ▸"));
         }

         ctx.drawText(this.tr, lt, this.x + 6, ry + (rowH - 2 - 8) / 2, label, r.heading);
         if (hover && r.tip != null && mx < this.x + 6 + this.tr.getWidth(lt) + 4) {
            List<Text> lines = new ArrayList();
            lines.add(Text.literal(r.label.replace("↳", "").trim()).formatted(Formatting.BOLD));

            for(StringVisitable part : this.tr.getTextHandler().wrapLines(r.tip, Math.min(220, this.w), Style.EMPTY)) {
               lines.add(Text.literal(part.getString()).formatted(Formatting.GRAY));
            }

            tip = lines;
         }

         int cx = this.x + this.w - 6;
         int cy = ry + (rowH - 2) / 2;
         if (r.on != null) {
            cx -= 30;
            this.drawSwitch(ctx, cx, cy - 6, r.on.getAsBoolean(), mx, my, time);
            this.hits.add(new Hit(cx, cy - 6, 30, 12, () -> r.setOn.accept(!r.on.getAsBoolean()), r, (Slider)null, (Swatch)null));
            cx -= 6;
         }

         for(int s = r.swatches.size() - 1; s >= 0; --s) {
            Swatch sw = (Swatch)r.swatches.get(s);
            if (sw.shown().getAsBoolean()) {
               cx -= 14;
               boolean sel = sw == this.editing;
               boolean hov = mx >= cx && mx < cx + 14 && my >= cy - 6 && my < cy + 6;
               PackEditorScreen.round(ctx, cx, cy - 6, 14, 12, -16777216 | sw.get().getAsInt());
               PackEditorScreen.outline(ctx, cx, cy - 6, 14, 12, sel ? PackEditorScreen.accent(time) : (hov ? -1 : -1593835521));
               int pickerX = cx;
               this.hits.add(new Hit(cx, cy - 6, 14, 12, () -> this.openPicker(sw, pickerX, cy + 8), r, (Slider)null, sw));
               cx -= 5;
            }
         }

         if (r.choices != null) {
            int sel = r.choice.getAsInt();

            for(int c = r.choices.length - 1; c >= 0; --c) {
               int bw = this.tr.getWidth(r.choices[c]) + 8;
               cx -= bw;
               boolean on = c == sel;
               boolean hov = mx >= cx && mx < cx + bw && my >= cy - 7 && my < cy + 7;
               PackEditorScreen.round(ctx, cx, cy - 7, bw, 14, on ? PackEditorScreen.withAlpha(PackEditorScreen.accent(time), 160) : (hov ? 1358954495 : 822083583));
               if (on) {
                  PackEditorScreen.outline(ctx, cx, cy - 7, bw, 14, PackEditorScreen.accent(time));
               }

               ctx.drawText(this.tr, r.choices[c], cx + 4, cy - 3, on ? -1 : -4935484, on);
               int choice = c;
               this.hits.add(new Hit(cx, cy - 7, bw, 14, () -> r.setChoice.accept(choice), r, (Slider)null, (Swatch)null));
               cx -= 2;
            }
         }

         if (r.slider != null) {
            int sw = Math.min(150, this.w / 2);
            cx -= sw + 30;
            this.drawSlider(ctx, r.slider, cx, cy, sw, time);
            this.hits.add(new Hit(cx - 3, cy - 6, sw + 6, 12, (Runnable)null, r, r.slider, (Swatch)null));
         }

         if (r.keyButton) {
            KeyBinding key = PackEditorClient.resetPopsKey();
            String keyLabel = this.capturingKey ? "Press a key…" : "Key: " + (key != null && !key.isUnbound() ? key.getBoundKeyLocalizedText().getString() : "None");
            int bw = this.tr.getWidth(keyLabel) + 10;
            cx -= bw + 4;
            boolean hov = mx >= cx && mx < cx + bw && my >= cy - 7 && my < cy + 7;
            PackEditorScreen.round(ctx, cx, cy - 7, bw, 14, this.capturingKey ? PackEditorScreen.withAlpha(PackEditorScreen.accent(time), 160) : (hov ? 1627389951 : 905969663));
            PackEditorScreen.outline(ctx, cx, cy - 7, bw, 14, !this.capturingKey && !hov ? 1358954495 : PackEditorScreen.accent(time));
            ctx.drawText(this.tr, keyLabel, cx + 5, cy - 3, -1, false);
            this.hits.add(new Hit(cx, cy - 7, bw, 14, () -> this.capturingKey = !this.capturingKey, r, (Slider)null, (Swatch)null));
         }

         if (r.button != null) {
            int bw = this.tr.getWidth(r.button) + 10;
            cx -= bw + 4;
            boolean hov = mx >= cx && mx < cx + bw && my >= cy - 7 && my < cy + 7;
            PackEditorScreen.round(ctx, cx, cy - 7, bw, 14, hov ? 1627389951 : 905969663);
            PackEditorScreen.outline(ctx, cx, cy - 7, bw, 14, hov ? PackEditorScreen.accent(time) : 1358954495);
            ctx.drawText(this.tr, r.button, cx + 5, cy - 3, -1, false);
            this.hits.add(new Hit(cx, cy - 7, bw, 14, r.press, r, (Slider)null, (Swatch)null));
         }
      }

      int footY = this.y + shown.size() * rowH + 4;
      if (footY + 9 > this.bottom) {
         if (this.message != null && System.currentTimeMillis() < this.messageUntil) {
            ctx.drawText(this.tr, Text.literal("✔ " + this.message).formatted(Formatting.GREEN), this.x + this.w - 6 - this.tr.getWidth("✔ " + this.message), this.y - 11, -1, true);
         }
      } else if (this.message != null && System.currentTimeMillis() < this.messageUntil) {
         ctx.drawText(this.tr, Text.literal("✔ " + this.message).formatted(Formatting.GREEN), this.x + 2, footY, -1, false);
      } else {
         ctx.drawText(this.tr, Text.literal("Changes apply instantly · hover a name for details").formatted(Formatting.DARK_GRAY), this.x + 2, footY, -1, false);
      }

      if (this.editing != null) {
         this.renderPicker(ctx, mx, my, time);
      }

      return tip;
   }

   private void drawSwitch(DrawContext ctx, int sx, int sy, boolean on, int mx, int my, double time) {
      boolean hover = mx >= sx && mx < sx + 30 && my >= sy && my < sy + 12;
      if (on) {
         ctx.fillGradient(sx + 1, sy, sx + 29, sy + 12, PackEditorScreen.accent(time), PackEditorScreen.accent(time + 0.3));
      } else {
         PackEditorScreen.round(ctx, sx, sy, 30, 12, -14015434);
      }

      PackEditorScreen.outline(ctx, sx, sy, 30, 12, hover ? -1 : (on ? -2130706433 : 1358954495));
      PackEditorScreen.round(ctx, on ? sx + 19 : sx + 2, sy + 2, 9, 8, -1);
      ctx.drawText(this.tr, on ? "ON" : "OFF", on ? sx + 4 : sx + 12, sy + 2, on ? -1 : -4935484, false);
   }

   private void drawSlider(DrawContext ctx, Slider s, int sx, int cy, int sw, double time) {
      double v = s.get().getAsDouble();
      float t = (float)((v - s.min()) / (s.max() - s.min()));
      PackEditorScreen.round(ctx, sx, cy - 1, sw, 3, 1358954495);
      double zero = s.snap();
      int z = sx + Math.round((float)((zero - s.min()) / (s.max() - s.min())) * (float)sw);
      int k = sx + Math.round(t * (float)sw);
      ctx.fill(Math.min(z, k), cy - 1, Math.max(z, k), cy + 2, PackEditorScreen.accent(time));
      ctx.fill(z, cy - 3, z + 1, cy + 4, -2130706433);
      PackEditorScreen.round(ctx, k - 2, cy - 5, 5, 11, -1);
      String val = (String)s.format().apply(v);
      ctx.drawText(this.tr, val, sx + sw + 6, cy - 4, -4935484, false);
   }

   private void openPicker(Swatch s, int ax, int ay) {
      if (this.editing == s) {
         this.editing = null;
      } else {
         this.editing = s;
         this.pkX = Math.max(this.x, Math.min(ax - 132 + 14, this.x + this.w - 132));
         this.pkY = ay + 92 > this.bottom ? ay - 14 - 92 - 2 : ay;
         int c = s.get().getAsInt();
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
   }

   private void renderPicker(DrawContext ctx, int mx, int my, double time) {
      ctx.getMatrices().pushMatrix();
      PackEditorScreen.round(ctx, this.pkX, this.pkY, 132, 92, -267251942);
      PackEditorScreen.outline(ctx, this.pkX, this.pkY, 132, 92, PackEditorScreen.accent(time));
      ctx.drawText(this.tr, Text.literal(this.editing.name()).formatted(Formatting.BOLD), this.pkX + 5, this.pkY + 4, -1, false);
      int c = this.editing.get().getAsInt();
      PackEditorScreen.round(ctx, this.pkX + 132 - 18, this.pkY + 3, 13, 10, -16777216 | c);
      int sx = this.pkX + 5;
      int sy = this.pkY + 16;
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
         if (PRESETS[i] == c) {
            PackEditorScreen.outline(ctx, px, ppy, 16, 9, -1);
         }
      }

      ctx.getMatrices().popMatrix();
   }

   private boolean pickerClick(double mx, double my) {
      if (this.editing == null) {
         return false;
      } else if (!(mx < (double)this.pkX) && !(mx >= (double)(this.pkX + 132)) && !(my < (double)this.pkY) && !(my >= (double)(this.pkY + 92))) {
         int sx = this.pkX + 5;
         int sy = this.pkY + 16;
         int sw = 122;
         int sh = 36;
         if (my >= (double)sy && my < (double)(sy + sh)) {
            this.pickerDrag = 1;
         } else {
            if (!(my >= (double)(sy + sh + 2)) || !(my < (double)(sy + sh + 11))) {
               int py = sy + sh + 14;

               for(int i = 0; i < PRESETS.length; ++i) {
                  int px = sx + i % 7 * 18;
                  int ppy = py + i / 7 * 11;
                  if (mx >= (double)px && mx < (double)(px + 16) && my >= (double)ppy && my < (double)(ppy + 9)) {
                     this.editing.set().accept(PRESETS[i]);
                     this.openPickerKeep();
                     Effects.save();
                  }
               }

               return true;
            }

            this.pickerDrag = 2;
         }

         this.pickerMove(mx, my);
         return true;
      } else {
         this.editing = null;
         return false;
      }
   }

   private void openPickerKeep() {
      Swatch s = this.editing;
      int px = this.pkX;
      int py = this.pkY;
      this.editing = null;
      this.openPicker(s, 0, 0);
      this.pkX = px;
      this.pkY = py;
   }

   private void pickerMove(double mx, double my) {
      int sx = this.pkX + 5;
      int sy = this.pkY + 16;
      int sw = 122;
      int sh = 36;
      if (this.pickerDrag == 1) {
         this.sat = (float)Math.max((double)0.0F, Math.min((double)1.0F, (mx - (double)sx) / (double)(sw - 1)));
         this.val = (float)Math.max((double)0.0F, Math.min((double)1.0F, (double)1.0F - (my - (double)sy) / (double)(sh - 1)));
      } else {
         if (this.pickerDrag != 2) {
            return;
         }

         this.hue = (float)Math.max((double)0.0F, Math.min((double)1.0F, (mx - (double)sx) / (double)(sw - 1)));
      }

      this.editing.set().accept(hsv(this.hue, this.sat, this.val));
   }

   boolean click(double mx, double my) {
      if (this.pickerClick(mx, my)) {
         return true;
      } else {
         boolean wasCapturing = this.capturingKey;

         for(Hit h : this.hits) {
            if (h.contains(mx, my)) {
               if (h.slider() != null) {
                  this.draggingSlider = h.slider();
                  this.draggingHit = h;
                  this.slide(mx);
                  return true;
               }

               if (h.action() != null) {
                  h.action().run();
                  Effects.save();
                  return true;
               }
            }
         }

         if (wasCapturing) {
            this.capturingKey = false;
         }

         return false;
      }
   }

   boolean keyPressed(KeyInput input) {
      if (!this.capturingKey) {
         return false;
      } else {
         this.capturingKey = false;
         KeyBinding binding = PackEditorClient.resetPopsKey();
         if (binding == null) {
            return true;
         } else {
            binding.setBoundKey(input.isEscape() ? InputUtil.UNKNOWN_KEY : InputUtil.fromKeyCode(input));
            KeyBinding.updateKeysByCode();
            MinecraftClient.getInstance().options.write();
            this.flash(binding.isUnbound() ? "Reset key cleared" : "Reset key: " + binding.getBoundKeyLocalizedText().getString());
            return true;
         }
      }
   }

   boolean drag(double mx, double my) {
      if (this.pickerDrag != 0 && this.editing != null) {
         this.pickerMove(mx, my);
         return true;
      } else if (this.draggingSlider != null) {
         this.slide(mx);
         return true;
      } else {
         return false;
      }
   }

   void release() {
      if (this.pickerDrag != 0 || this.draggingSlider != null) {
         Effects.save();
      }

      this.pickerDrag = 0;
      this.draggingSlider = null;
   }

   private void slide(double mx) {
      Slider s = this.draggingSlider;
      int sx = this.draggingHit.x() + 3;
      int sw = this.draggingHit.w() - 6;
      double t = Math.max((double)0.0F, Math.min((double)1.0F, (mx - (double)sx) / (double)sw));
      double v = s.min() + t * (s.max() - s.min());
      double zero = s.snap();
      if (Math.abs(v - zero) < (s.max() - s.min()) * 0.03) {
         v = zero;
      }

      s.set().accept((double)Math.round(v * (double)100.0F) / (double)100.0F);
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

   private static record Swatch(String name, IntSupplier get, IntConsumer set, BooleanSupplier shown) {
   }

   private static record Slider(double min, double max, double snap, DoubleFunction<String> format, DoubleSupplier get, DoubleConsumer set) {
   }

   private static final class Row {
      final String label;
      final String tip;
      BooleanSupplier on;
      Consumer<Boolean> setOn;
      final List<Swatch> swatches = new ArrayList();
      String[] choices;
      IntSupplier choice;
      IntConsumer setChoice;
      Slider slider;
      String button;
      Runnable press;
      boolean heading;
      boolean keyButton;
      BooleanSupplier visible = () -> true;
      BooleanSupplier expanded;

      Row(String label, String tip) {
         this.label = label;
         this.tip = tip;
      }

      Row toggle(BooleanSupplier on, Consumer<Boolean> set) {
         this.on = on;
         this.setOn = set;
         return this;
      }

      Row swatch(String name, IntSupplier get, IntConsumer set) {
         return this.swatch(name, get, set, () -> true);
      }

      Row swatch(String name, IntSupplier get, IntConsumer set, BooleanSupplier shown) {
         this.swatches.add(new Swatch(name, get, set, shown));
         return this;
      }

      Row choices(String[] options, IntSupplier get, IntConsumer set) {
         this.choices = options;
         this.choice = get;
         this.setChoice = set;
         return this;
      }

      Row slider(double min, double max, DoubleSupplier get, DoubleConsumer set) {
         double snap = min < (double)0.0F ? (double)0.0F : (double)1.0F;
         return this.slider(min, max, snap, (v) -> String.format(Locale.ROOT, min < (double)0.0F ? "%+.2f" : "%.2fx", v), get, set);
      }

      Row slider(double min, double max, double snap, DoubleFunction<String> format, DoubleSupplier get, DoubleConsumer set) {
         this.slider = new Slider(min, max, snap, format, get, set);
         return this;
      }

      Row keyButton() {
         this.keyButton = true;
         return this;
      }

      Row when(BooleanSupplier shown) {
         this.visible = shown;
         return this;
      }

      Row dropdown(BooleanSupplier open) {
         this.expanded = open;
         return this;
      }

      Row button(String label, Runnable press) {
         this.button = label;
         this.press = press;
         return this;
      }
   }

   private static record Hit(int x, int y, int w, int h, Runnable action, Row row, Slider slider, Swatch swatch) {
      boolean contains(double mx, double my) {
         return mx >= (double)this.x && mx < (double)(this.x + this.w) && my >= (double)this.y && my < (double)(this.y + this.h);
      }
   }
}
