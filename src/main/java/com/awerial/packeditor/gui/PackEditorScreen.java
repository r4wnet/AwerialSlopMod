package com.awerial.packeditor.gui;

import com.awerial.packeditor.AnchorParts;
import com.awerial.packeditor.Announcements;
import com.awerial.packeditor.Category;
import com.awerial.packeditor.CrosshairPresets;
import com.awerial.packeditor.Drawings;
import com.awerial.packeditor.ExternalPack;
import com.awerial.packeditor.Group;
import com.awerial.packeditor.ImageColours;
import com.awerial.packeditor.ItemTextures;
import com.awerial.packeditor.LegacyGlint;
import com.awerial.packeditor.PackBuilder;
import com.awerial.packeditor.PackEditorClient;
import com.awerial.packeditor.PackEditorConfig;
import com.awerial.packeditor.Profiles;
import com.awerial.packeditor.SharedLooks;
import com.awerial.packeditor.SkyTint;
import com.awerial.packeditor.Stats;
import com.awerial.packeditor.Toggle;
import com.awerial.packeditor.UserPacks;
import com.awerial.packeditor.Variant;
import com.awerial.packeditor.VariantRegistry;
import com.awerial.packeditor.WornArmour;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.Util;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class PackEditorScreen extends Screen {
   private static final int CARD_W = 58;
   private static final int CARD_H = 72;
   private static final int GAP = 5;
   private static final int PREVIEW = 40;
   private static final int SIDEBAR_W = 116;
   private static final int PURPLE = -5018625;
   private static final int CYAN = -11541505;
   private static final int PINK = -37931;
   private static final int PANEL = 1880100888;
   private static final int CARD = 822083583;
   private static final int CARD_HOVER = 1358954495;
   private static final int LINE = 1358954495;
   private static final int DIM = -4935484;
   private static final int SOFT = -1645585;
   private static int textureCounter;
   private final Screen parent;
   private final Map<Category, String> pending = new EnumMap(Category.class);
   private final Map<Group, Category> lastPiece = new EnumMap(Group.class);
   private final List<Btn> buttons = new ArrayList();
   private Map<Category, List<Variant>> variants;
   private Group group;
   private Category current;
   private double scroll;
   private Text status;
   private int x0;
   private int y0;
   private int x1;
   private int y1;
   private int tabH;
   private int gridX;
   private int gridY;
   private int gridW;
   private int gridH;
   private int cols;
   private int headlineY;
   private int crosshairSize;
   private ColourPopup colourPopup;
   private Category popupCategory;
   private final Map<Category, Integer> customPreviews;
   private static final int CUSTOM_DEFAULT = 10320895;
   private int cobwebOpacity;
   private int cwX;
   private int cwW;
   private int barY;
   private static final String[] TOTEM_STOPS = new String[]{"Very Small", "Small", "Big"};
   private static final String[] TOTEM_STOP_IDS = new String[]{"size:very_small", "size:small", "vanilla"};
   private String confirmDeleteTexture;
   private Section section;
   private final Map<Section, Group> lastGroup;
   private TextFieldWidget nameField;
   private TextFieldWidget codeField;
   private String nameText;
   private String codeText;
   private String lastCode;
   private int profileScroll;
   private String confirmDelete;
   private final List<Object[]> configRows;
   private int cfgX;
   private int cfgW;
   private int cfgSaveY;
   private int cfgCodeY;
   private int cfgListY;
   private int cfgRows;
   private int packScroll;
   private String confirmRemove;
   private final List<Object[]> packRows;
   private int mpX;
   private int mpW;
   private int mpDropY;
   private int mpListY;
   private int mpRows;
   private int restoreW;
   private List<ExternalPack> uploadedPacks;
   private int dropY;
   private boolean dropHover;
   private static final int SLOT = 22;
   private TextFieldWidget searchField;
   private String searchText;
   private boolean onlyDrawn;
   private boolean itemsDirty;
   private List<ItemTextures.Entry> itemList;
   private int itX;
   private int itW;
   private int itGridY;
   private int itCols;
   private int itRows;
   private int itScroll;
   private boolean diamond;
   private final Map<Toggle, Boolean> toggles;
   private static final int[] PRESETS = new int[]{16726843, 16747039, 16767291, 8257339, 3066944, 59903, 3909631, 3099647, 9059327, 14236671, 16739285, 16777215, 9079434, 2763306};
   private int shieldColor;
   private float hue;
   private float sat;
   private float val;
   private int drag;
   private int pkX;
   private int pkY;
   private int pkW;
   private int pkH;
   private int svX;
   private int svY;
   private int svW;
   private int svH;
   private int hueY;
   private int presetY;
   private int defX;
   private int defY;
   private boolean waitingForLegacyGlint;
   private boolean editingDisabled;
   private int disabledShieldColor;
   private EffectsPanel effects;
   private List<Text> effectsTip;
   static final int ACCENT = -6456321;
   private static final Preview LOADING = new Preview((Identifier)null, 0, 0);
   private static final Map<String, Preview> PREVIEWS = new HashMap();
   private static final Map<String, Future<NativeImage>> MAKING = new HashMap();
   private static final ExecutorService PREVIEW_THREAD = Executors.newSingleThreadExecutor((r) -> {
      Thread t = new Thread(r, "APE previews");
      t.setDaemon(true);
      return t;
   });
   private static int previewGeneration = -1;

   private int customPreviewFor(Category c) {
      return (Integer)this.customPreviews.getOrDefault(c, 10320895);
   }

   private int customCardColour() {
      String now = (String)this.pending.getOrDefault(this.current, "");
      if (now.startsWith("recolour:") && (this.colourPopup == null || !this.colourPopup.dragging())) {
         Variant.RecolourVariant rv = Variant.RecolourVariant.of(now);
         if (rv != null) {
            return rv.color;
         }
      }

      return this.customPreviewFor(this.current);
   }

   private boolean hasBar() {
      return this.group == Group.TOTEMS || this.group == Group.CROSSHAIR || this.group == Group.ENDER_PEARLS || this.group == Group.COBWEB;
   }

   private boolean togglesInBar() {
      return this.group == Group.ENDER_PEARLS;
   }

   private void deleteTexture() {
      String id = (String)this.pending.getOrDefault(this.current, "vanilla");
      Variant v = VariantRegistry.find(this.currentList(), id);
      if (v != null && !v.isVanilla()) {
         String key = this.current.id + "|" + id;
         if (!key.equals(this.confirmDeleteTexture)) {
            this.confirmDeleteTexture = key;
            this.status = Text.literal("Click Sure? to delete '" + v.name + "'").formatted(Formatting.GOLD);
         } else {
            this.confirmDeleteTexture = null;

            try {
               if (v instanceof Variant.DrawnVariant) {
                  Variant.DrawnVariant d = (Variant.DrawnVariant)v;
                  Drawings.delete(this.current, d.drawingName);
               } else {
                  PackEditorConfig.delete(this.current, id);
               }
            } catch (IOException e) {
               this.status = Text.literal("Couldn't delete it: " + e.getMessage()).formatted(Formatting.RED);
               return;
            }

            this.pending.put(this.current, "vanilla");
            this.rescan();
            this.status = v instanceof Variant.DrawnVariant ? Text.literal("Deleted your drawing '" + v.name + "' — press Apply").formatted(Formatting.GRAY) : Text.literal("Deleted '" + v.name + "' (My Packs › Restore brings it back) — press Apply").formatted(Formatting.GRAY);
         }
      } else {
         this.status = Text.literal("Click the texture you want to delete first (Vanilla can't be deleted)").formatted(Formatting.GOLD);
      }
   }

   private void initBar() {
      this.gridH -= 28;
      this.barY = this.gridY + this.gridH + 8;
      if (this.group == Group.TOTEMS) {
         int tx = this.gridX + this.textRenderer.getWidth(Text.literal("Totem size").formatted(Formatting.BOLD)) + 14 + this.textRenderer.getWidth("Very Small") / 2;
         int tw = Math.min(180, this.x1 - 20 - this.textRenderer.getWidth("Big") / 2 - tx);

         for(int i = 0; i < TOTEM_STOPS.length; ++i) {
            String sizeId = TOTEM_STOP_IDS[i];
            int sx = tx + i * tw / 2;
            this.buttons.add(new Btn(PackEditorScreen.Kind.TOTEM_STOP, sx - tw / 4, this.barY, tw / 2, 22, TOTEM_STOPS[i], (Group)null, (Category)null, () -> this.pending.put(Category.TOTEM_SIZE, sizeId)));
         }
      } else if (this.group == Group.COBWEB) {
         this.cwX = this.gridX + this.textRenderer.getWidth(Text.literal("Opacity").formatted(Formatting.BOLD)) + 14;
         this.cwW = Math.max(60, Math.min(180, this.x1 - 50 - this.cwX));
      } else if (this.group == Group.CROSSHAIR) {
         int bx = this.gridX + this.textRenderer.getWidth(Text.literal("Crosshair size").formatted(Formatting.BOLD)) + 10;

         for(int n = 0; n <= 4; ++n) {
            String label = n == 0 ? "Auto" : "GUI " + n;
            int w = this.textRenderer.getWidth(label) + 12;
            int size = n;
            this.buttons.add(new Btn(PackEditorScreen.Kind.CROSSHAIR_SIZE, bx, this.barY + 3, w, 16, label, (Group)null, (Category)null, () -> this.crosshairSize = size));
            bx += w + 3;
         }
      }

   }

   private void renderBar(DrawContext ctx, int mx, int my, double time) {
      ctx.fill(this.gridX, this.barY - 5, this.x1 - 10, this.barY - 4, 1358954495);
      if (this.group == Group.TOTEMS) {
         ctx.drawText(this.textRenderer, Text.literal("Totem size").formatted(Formatting.BOLD), this.gridX, this.barY + 3, -1, true);
         ctx.drawText(this.textRenderer, "+ small pop", this.gridX, this.barY + 13, -37931, false);
         String sel = (String)this.pending.getOrDefault(Category.TOTEM_SIZE, "vanilla");
         List<Btn> stops = this.buttons.stream().filter((bx) -> bx.kind == PackEditorScreen.Kind.TOTEM_STOP).toList();
         if (stops.isEmpty()) {
            return;
         }

         int first = ((Btn)stops.get(0)).x + ((Btn)stops.get(0)).w / 2;
         int last = ((Btn)stops.get(stops.size() - 1)).x + ((Btn)stops.get(stops.size() - 1)).w / 2;
         round(ctx, first, this.barY + 4, last - first, 3, 1358954495);

         for(int i = 0; i < stops.size(); ++i) {
            Btn b = (Btn)stops.get(i);
            int cx = b.x + b.w / 2;
            boolean on = TOTEM_STOP_IDS[i].equals(sel);
            boolean hover = b.contains((double)mx, (double)my);
            if (on) {
               round(ctx, first, this.barY + 4, cx - first, 3, accent(time * 0.2));
            }

            ctx.fill(cx - 1, this.barY + 3, cx + 2, this.barY + 8, -1);
            if (on) {
               round(ctx, cx - 4, this.barY, 9, 11, -1);
               outline(ctx, cx - 4, this.barY, 9, 11, accent(time * 0.2));
            }

            int lw = this.textRenderer.getWidth(b.label);
            ctx.drawText(this.textRenderer, b.label, cx - lw / 2, this.barY + 13, on ? -1 : (hover ? -1645585 : -4935484), on);
         }
      } else if (this.group == Group.COBWEB) {
         ctx.drawText(this.textRenderer, Text.literal("Opacity").formatted(Formatting.BOLD), this.gridX, this.barY + 7, -1, true);
         int ty = this.barY + 10;
         int k = this.cwX + Math.round((float)this.cobwebOpacity / 100.0F * (float)this.cwW);
         round(ctx, this.cwX, ty - 1, this.cwW, 3, 1358954495);
         ctx.fill(this.cwX, ty - 1, k, ty + 2, accent(time * 0.2));
         boolean hov = mx >= this.cwX - 3 && mx <= this.cwX + this.cwW + 3 && my >= this.barY + 2 && my < this.barY + 18;
         round(ctx, k - 2, ty - 5, 5, 11, !hov && this.drag != 3 ? -1645585 : -1);
         int var10002 = this.cobwebOpacity;
         ctx.drawText(this.textRenderer, var10002 + "%", this.cwX + this.cwW + 8, this.barY + 7, this.cobwebOpacity < 100 ? -1645585 : -4935484, false);
         if (hov) {
            ctx.drawTooltip(this.textRenderer, Text.literal("How see-through cobwebs are (100% = normal) — press Apply"), mx, my);
         }
      } else if (this.group == Group.CROSSHAIR) {
         ctx.drawText(this.textRenderer, Text.literal("Crosshair size").formatted(Formatting.BOLD), this.gridX, this.barY + 7, -1, true);

         for(Btn b : this.buttons) {
            if (b.kind == PackEditorScreen.Kind.CROSSHAIR_SIZE) {
               boolean sel = b.label.equals(this.crosshairSize == 0 ? "Auto" : "GUI " + this.crosshairSize);
               boolean hover = b.contains((double)mx, (double)my);
               if (sel) {
                  ctx.fillGradient(b.x + 1, b.y, b.x + b.w - 1, b.y + b.h, withAlpha(accent(time * 0.15), 144), withAlpha(accent(time * 0.15 + 0.3), 112));
                  outline(ctx, b.x, b.y, b.w, b.h, accent(time * 0.15));
               } else {
                  round(ctx, b.x, b.y, b.w, b.h, hover ? 1358954495 : 822083583);
                  outline(ctx, b.x, b.y, b.w, b.h, hover ? withAlpha(-5018625, 170) : 1358954495);
               }

               ctx.drawText(this.textRenderer, b.label, b.x + 6, b.y + 4, sel ? -1 : -1645585, sel);
               if (hover) {
                  ctx.drawTooltip(this.textRenderer, Text.literal(b.label.equals("Auto") ? "Follows your GUI scale" : "As big as at GUI scale " + b.label.substring(4) + ", whatever your GUI scale is"), mx, my);
               }
            }
         }
      }

   }

   private void openSection(Section s) {
      this.section = s;
      Group g = (Group)this.lastGroup.getOrDefault(s, (Group)s.groups.get(0));
      this.group = g;
      if (!g.isPanel()) {
         this.current = this.matchMaterial((Category)this.lastPiece.getOrDefault(g, (Category)g.categories(this.diamond).get(0)));
      }

      this.scroll = (double)0.0F;
      this.status = Text.empty();
   }

   private void initHome() {
      List<Section> big = Arrays.stream(Section.values()).filter((sxx) -> sxx.big).toList();
      int gap = 10;
      int cw = Math.max(54, Math.min(104, (this.width - 40 - gap * (big.size() - 1)) / big.size()));
      int ch = Math.min(cw, Math.max(64, this.height * 3 / 10));
      int rowW = big.size() * cw + (big.size() - 1) * gap;
      int bx = (this.width - rowW) / 2;
      int by = Math.max(this.height * 36 / 100, 76);

      for(int i = 0; i < big.size(); ++i) {
         Section s = (Section)big.get(i);
         this.buttons.add(new Btn(PackEditorScreen.Kind.SECTION, bx + i * (cw + gap), by, cw, ch, s.label, (Group)null, (Category)null, () -> this.openSection(s)));
      }

      int sw = 58;
      int sh = 40;
      int sg = 8;
      int sy = by + ch + 16;
      boolean site = !"https://awerial.awerial.workers.dev".isEmpty();
      int n = site ? 3 : 2;
      int sx = (this.width - (n * sw + (n - 1) * sg)) / 2;
      this.buttons.add(new Btn(PackEditorScreen.Kind.SMALL, sx, sy, sw, sh, "Configs", (Group)null, (Category)null, () -> this.openSection(Section.CONFIGS)));
      this.buttons.add(new Btn(PackEditorScreen.Kind.SMALL, sx + sw + sg, sy, sw, sh, "My Packs", (Group)null, (Category)null, () -> this.openSection(Section.MY_PACKS)));
      if (site) {
         this.buttons.add(new Btn(PackEditorScreen.Kind.SMALL, sx + 2 * (sw + sg), sy, sw, sh, "Website", (Group)null, (Category)null, () -> ConfirmLinkScreen.open(this, "https://awerial.awerial.workers.dev")));
      }

      String newer = Announcements.newerVersion();
      String title = Announcements.latestTitle();
      if (title != null) {
         String label = newer != null ? "✦ Update " + newer + " is out: " + title + " — click to download" : "✦ What's new: " + title;
         int bw = Math.min(this.width - 40, this.textRenderer.getWidth(label) + 20);
         int byy = sy + sh + 12 + 16 <= this.height - 6 ? sy + sh + 12 : 6;
         String url = newer != null ? "https://awerial.awerial.workers.dev/#download" : "https://awerial.awerial.workers.dev/#news";
         this.buttons.add(new Btn(newer != null ? PackEditorScreen.Kind.PRIMARY : PackEditorScreen.Kind.BUTTON, (this.width - bw) / 2, byy, bw, 16, this.textRenderer.trimToWidth(label, bw - 12), (Group)null, (Category)null, () -> ConfirmLinkScreen.open(this, url)));
      }

   }

   private void renderHome(DrawContext ctx, int mx, int my) {
      Text title = Text.literal("Awerial");
      float scale = Math.max(2.0F, Math.min(4.0F, (float)this.width / 120.0F));
      int ty = Math.max(this.height * 36 / 100, 76) - (int)(scale * 8.0F) - 22;
      ctx.getMatrices().pushMatrix();
      ctx.getMatrices().translate((float)this.width / 2.0F - (float)this.textRenderer.getWidth(title) * scale / 2.0F, (float)ty);
      ctx.getMatrices().scale(scale, scale);
      ctx.drawText(this.textRenderer, title, 0, 0, -1, false);
      ctx.getMatrices().popMatrix();
      Text sub = (Text)(this.status.getString().isEmpty() ? Text.literal("Pack Editor").formatted(Formatting.GRAY) : this.status);
      ctx.drawCenteredTextWithShadow(this.textRenderer, sub, this.width / 2, ty + (int)(scale * 8.0F) + 4, -1);
      int unsaved = this.unsavedChanges();

      for(Btn b : this.buttons) {
         if (b.kind != PackEditorScreen.Kind.PRIMARY && b.kind != PackEditorScreen.Kind.BUTTON) {
            if (b.kind == PackEditorScreen.Kind.SECTION || b.kind == PackEditorScreen.Kind.SMALL) {
               boolean hover = b.contains((double)mx, (double)my);
               boolean isApply = b.label.equals("Apply");
               int fill = isApply && unsaved > 0 ? withAlpha(-6456321, hover ? 192 : 144) : (hover ? 1358954495 : 822083583);
               round(ctx, b.x, b.y, b.w, b.h, fill);
               outline(ctx, b.x, b.y, b.w, b.h, hover ? -1593835521 : 1627389951);
               if (b.kind == PackEditorScreen.Kind.SECTION) {
                  Section s = Section.byLabel(b.label);
                  int iconSpace = b.h - 30;
                  float is = Math.max(1.0F, Math.min(2.5F, Math.min((float)b.w / 40.0F, (float)iconSpace / 17.0F)));
                  ctx.getMatrices().pushMatrix();
                  ctx.getMatrices().translate((float)b.x + (float)b.w / 2.0F - 8.0F * is, (float)(b.y + 5) + ((float)iconSpace - 16.0F * is) / 2.0F);
                  ctx.getMatrices().scale(is, is);
                  ctx.drawItem(new ItemStack(s.icon()), 0, 0);
                  ctx.getMatrices().popMatrix();
                  int ly = b.y + b.h - 22;
                  ctx.drawCenteredTextWithShadow(this.textRenderer, b.label, b.x + b.w / 2, ly, -1);
                  ctx.fill(b.x + b.w / 2 - 7, ly + 12, b.x + b.w / 2 + 7, ly + 13, -1593835521);
                  if (s.groups.stream().anyMatch(this::groupChanged)) {
                     round(ctx, b.x + b.w - 9, b.y + 5, 4, 4, -6456321);
                  }
               } else {
                  Section s = Section.byLabel(b.label);
                  if (s != null) {
                     ctx.drawItem(new ItemStack(s.icon()), b.x + b.w / 2 - 8, b.y + 6);
                  } else if (b.label.equals("Website")) {
                     ctx.drawItem(new ItemStack(Items.COMPASS), b.x + b.w / 2 - 8, b.y + 6);
                  } else {
                     String glyph = isApply ? "✔" : "✕";
                     Text g = Text.literal(glyph).formatted(Formatting.BOLD);
                     ctx.drawText(this.textRenderer, g, b.x + b.w / 2 - this.textRenderer.getWidth(g) / 2, b.y + 10, -1, false);
                  }

                  String label = isApply && unsaved > 0 ? "Apply (" + unsaved + ")" : b.label;
                  ctx.drawCenteredTextWithShadow(this.textRenderer, label, b.x + b.w / 2, b.y + b.h - 13, -1);
               }
            }
         } else {
            boolean hover = b.contains((double)mx, (double)my);
            boolean update = b.kind == PackEditorScreen.Kind.PRIMARY;
            round(ctx, b.x, b.y, b.w, b.h, update ? withAlpha(-6456321, hover ? 224 : 176) : (hover ? 1358954495 : 822083583));
            outline(ctx, b.x, b.y, b.w, b.h, update ? -1056964609 : (hover ? -1593835521 : 1627389951));
            ctx.drawCenteredTextWithShadow(this.textRenderer, b.label, b.x + b.w / 2, b.y + (b.h - 8) / 2, -1);
         }
      }

   }

   private Profiles.Setup currentSetup() {
      return new Profiles.Setup(new EnumMap(this.pending), new EnumMap(this.toggles), this.shieldColor, this.crosshairSize, this.disabledShieldColor, this.cobwebOpacity);
   }

   private int loadSetup(Profiles.Setup s) {
      this.pending.clear();
      int missing = 0;

      for(Map.Entry<Category, String> e : s.selections().entrySet()) {
         if (VariantRegistry.find((List)this.variants.get(e.getKey()), (String)e.getValue()) != null) {
            this.pending.put((Category)e.getKey(), (String)e.getValue());
         } else {
            ++missing;
         }
      }

      this.toggles.clear();
      this.toggles.putAll(s.toggles());
      this.shieldColor = s.shieldColor();
      if (this.shieldColor >= 0) {
         this.setHsvFrom(this.shieldColor);
      }

      this.crosshairSize = s.crosshairSize();
      this.disabledShieldColor = s.disabledShieldColor();
      this.cobwebOpacity = s.cobwebOpacity();
      this.editingDisabled = false;
      return missing;
   }

   private void switchTo(Profiles.Setup s, String what, String activeName) {
      int missing = this.loadSetup(s);
      PackEditorConfig.setActiveConfig(activeName);
      if (this.equip()) {
         String msg = "✔ " + what + " equipped!";
         if (missing > 0) {
            msg = msg + " (" + missing + " need" + (missing == 1 ? "s a pack" : " packs") + " you don't have)";
         }

         this.status = Text.literal(msg).setStyle(Style.EMPTY.withColor(missing > 0 ? 16762967 : 7332768));
      }
   }

   private void saveConfig() {
      String name = this.nameText.trim();
      if (name.isEmpty()) {
         this.status = Text.literal("Type a name for your config first").formatted(Formatting.RED);
      } else {
         boolean existed = Profiles.names().contains(name);
         Profiles.save(name, this.currentSetup());
         PackEditorConfig.setActiveConfig(name);
         this.equip();
         this.nameText = "";
         this.status = Text.literal("✔ " + (existed ? "Updated" : "Saved") + " '" + name + "'").setStyle(Style.EMPTY.withColor(7332768));
      }
   }

   private void copyCode(Profiles.Setup s, String what) {
      this.withLookCode(s, (code, withLook) -> {
         this.client.keyboard.setClipboard(code);
         this.lastCode = code;
         this.status = Text.literal("✔ Copied " + what + " code" + (withLook ? " — friends get the same look, no packs needed" : "")).setStyle(Style.EMPTY.withColor(7332768));
      });
   }

   private void withLookCode(Profiles.Setup s, BiConsumer<String, Boolean> then) {
      String code = Profiles.shortCode(s);
      if (!"https://awerial.awerial.workers.dev".isEmpty() && SharedLooks.anyNeedFiles(s.selections())) {
         this.status = Text.literal("Uploading your textures so friends don't need your packs...").formatted(Formatting.GRAY);
         SharedLooks.upload(s.selections(), this.variants).whenComplete((id, err) -> this.client.execute(() -> {
               if (err == null && id != null) {
                  then.accept(code + "~" + id, true);
               } else {
                  String why = err == null ? "" : (err.getCause() != null ? err.getCause().getMessage() : err.getMessage());
                  then.accept(code, false);
                  if (err != null) {
                     this.status = Text.literal("✔ Copied code without textures (" + why + ") — friends need the same packs").formatted(Formatting.GOLD);
                  }

               }
            }));
      } else {
         then.accept(code, false);
      }
   }

   private void importCode() {
      String clip = (this.codeText.isEmpty() ? this.client.keyboard.getClipboard() : this.codeText).trim();
      this.codeText = "";
      int sep = clip.indexOf(126);

      Profiles.Setup setup;
      try {
         setup = Profiles.decode(sep < 0 ? clip : clip.substring(0, sep), this.variants);
      } catch (IllegalArgumentException e) {
         this.status = Text.literal(e.getMessage() + " — copy the whole code first").formatted(Formatting.RED);
         return;
      }

      if (sep < 0) {
         this.switchTo(setup, "Friend's code", (String)null);
      } else {
         this.status = Text.literal("Downloading your friend's look...").formatted(Formatting.GRAY);
         SharedLooks.download(clip.substring(sep + 1).trim()).whenComplete((picks, err) -> this.client.execute(() -> {
               if (err != null) {
                  this.switchTo(setup, "Friend's code", (String)null);
                  String why = err.getCause() != null ? err.getCause().getMessage() : err.getMessage();
                  this.status = Text.literal("Equipped, but couldn't get their textures (" + why + ")").formatted(Formatting.GOLD);
               } else {
                  this.rescan();
                  Map<Category, String> sel = new EnumMap(Category.class);
                  sel.putAll(setup.selections());
                  sel.putAll(picks);
                  this.switchTo(new Profiles.Setup(sel, setup.toggles(), setup.shieldColor(), setup.crosshairSize(), setup.disabledShieldColor(), setup.cobwebOpacity()), "Friend's look", (String)null);
               }
            }));
      }
   }

   private static List<String> configNames() {
      List<String> names = new ArrayList();

      for(String p : Profiles.PRESETS.keySet()) {
         if (Profiles.showsPreset(p)) {
            names.add(p);
         }
      }

      names.addAll(Profiles.names());
      return names;
   }

   private void deleteConfig(String name) {
      if (!name.equals(this.confirmDelete)) {
         this.confirmDelete = name;
         this.status = Text.literal("Click Sure? to delete '" + name + "'").formatted(Formatting.GOLD);
      } else {
         Profiles.delete(name);
         this.confirmDelete = null;
         this.status = Text.literal("Deleted '" + name + "'").formatted(Formatting.GRAY);
      }
   }

   private void initConfigs(int cx) {
      this.cfgX = cx;
      this.cfgW = this.x1 - 10 - cx;
      int y = this.y0 + 38;
      this.cfgSaveY = y;
      String var10000 = Stats.enabled() ? "ON" : "OFF";
      String statsLabel = "Anonymous player count: " + var10000;
      int sbw = this.textRenderer.getWidth(statsLabel) + 12;
      this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, cx + this.cfgW - sbw, y + 34 - 3, sbw, 13, statsLabel, (Group)null, (Category)null, () -> Stats.setEnabled(!Stats.enabled())));
      this.nameField = new TextFieldWidget(this.textRenderer, cx + 1, y + 12, this.cfgW - 66, 14, Text.literal("Config name"));
      this.nameField.setMaxLength(32);
      this.nameField.setText(this.nameText);
      this.nameField.setChangedListener((s) -> this.nameText = s);
      this.nameField.setPlaceholder(Text.literal("Name this setup...").formatted(Formatting.DARK_GRAY));
      this.addSelectableChild(this.nameField);
      this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, cx + this.cfgW - 60, y + 11, 60, 16, "Save", (Group)null, (Category)null, this::saveConfig));
      this.cfgCodeY = y + 34;
      int cy = this.cfgCodeY + 11;
      boolean site = !"https://awerial.awerial.workers.dev".isEmpty();
      int third = site ? (this.cfgW - 8) / 3 : (this.cfgW - 4) / 2;
      this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, cx, cy, third, 18, third >= 100 ? "⎘ Copy My Code" : "Copy My Code", (Group)null, (Category)null, () -> this.copyCode(this.currentSetup(), "your")));
      this.buttons.add(new Btn(PackEditorScreen.Kind.PRIMARY, cx + third + 4, cy, site ? third : this.cfgW - third - 4, 18, third >= 134 ? "↓ Paste Friend's Code" : "↓ Paste Friend's", (Group)null, (Category)null, this::importCode));
      if (site) {
         this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, cx + 2 * (third + 4), cy, this.cfgW - 2 * (third + 4), 18, "Share Online", (Group)null, (Category)null, () -> this.withLookCode(this.currentSetup(), (code, withLook) -> {
               this.client.keyboard.setClipboard(code);
               this.lastCode = code;
               ConfirmLinkScreen.open(this, "https://awerial.awerial.workers.dev/#share=" + code);
            })));
      }

      this.codeField = null;
      this.cfgListY = this.cfgCodeY + 46;
      int rowsTop = this.cfgListY + 12;
      this.cfgRows = Math.max(1, (this.y1 - 32 - rowsTop) / 19);
      if (Profiles.anyPresetHidden()) {
         String rl = "Restore presets";
         int rw = this.textRenderer.getWidth(rl) + 12;
         this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, cx + this.cfgW - rw, this.cfgListY - 3, rw, 13, rl, (Group)null, (Category)null, () -> {
            Profiles.restorePresets();
            this.status = Text.literal("Built-in presets are back").formatted(Formatting.GRAY);
         }));
      }

      List<String> names = configNames();
      this.profileScroll = Math.max(0, Math.min(this.profileScroll, Math.max(0, names.size() - this.cfgRows)));
      this.configRows.clear();

      for(int i = 0; i < this.cfgRows && this.profileScroll + i < names.size(); ++i) {
         String name = (String)names.get(this.profileScroll + i);
         int ry = rowsTop + i * 19;
         this.configRows.add(new Object[]{name, ry});
         int bx = cx + this.cfgW - 4;
         bx -= 46;
         this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, bx, ry + 1, 46, 15, name.equals(this.confirmDelete) ? "Sure?" : "Delete", (Group)null, (Category)null, () -> this.deleteConfig(name)));
         bx -= 44;
         this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, bx, ry + 1, 42, 15, "Code", (Group)null, (Category)null, () -> this.copyCode(Profiles.get(name), "'" + name + "'")));
         bx -= 44;
         boolean active = name.equals(PackEditorConfig.activeConfig());
         this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, bx, ry + 1, 42, 15, active ? "Reload" : "Use", (Group)null, (Category)null, () -> {
            Profiles.Setup s = Profiles.get(name);
            if (s != null) {
               this.switchTo(s, "'" + name + "'", name);
            }

         }));
      }

   }

   private void renderConfigs(DrawContext ctx, int mx, int my, float delta, double time) {
      ctx.drawText(this.textRenderer, Text.literal("Save current setup").formatted(Formatting.BOLD), this.cfgX, this.cfgSaveY + 1, -1, true);
      ctx.drawText(this.textRenderer, "textures, switches & shield colour", this.cfgX + 4 + this.textRenderer.getWidth(Text.literal("Save current setup").formatted(Formatting.BOLD)), this.cfgSaveY + 1, -4935484, false);
      round(ctx, this.cfgX - 1, this.cfgSaveY + 11, this.cfgW - 64, 16, 1342177280);
      outline(ctx, this.cfgX - 1, this.cfgSaveY + 11, this.cfgW - 64, 16, this.nameField.isFocused() ? accent(time * 0.2) : 1358954495);
      this.nameField.render(ctx, mx, my, delta);
      ctx.drawText(this.textRenderer, Text.literal("Share codes").formatted(Formatting.BOLD), this.cfgX, this.cfgCodeY + 1, -1, true);
      if (this.lastCode != null) {
         Text copied = Text.literal("Copied: ").formatted(Formatting.GRAY).append(Text.literal(this.lastCode).setStyle(Style.EMPTY.withColor(accent(time * 0.15) & 16777215)));
         ctx.drawText(this.textRenderer, copied, this.cfgX, this.cfgCodeY + 33, -1, false);
      } else {
         ctx.drawText(this.textRenderer, "Copy a friend's code, then press Paste — it's equipped instantly", this.cfgX, this.cfgCodeY + 33, -4935484, false);
      }

      List<String> names = configNames();
      TextRenderer var10001 = this.textRenderer;
      MutableText var10002 = Text.literal("Configs").formatted(Formatting.BOLD);
      int var10003 = names.size() - Profiles.names().size();
      ctx.drawText(var10001, var10002.append(Text.literal("  " + var10003 + " presets · " + Profiles.names().size() + " saved").formatted(Formatting.DARK_GRAY)), this.cfgX, this.cfgListY + 1, -1, true);
      if (names.isEmpty()) {
         ctx.drawText(this.textRenderer, "Nothing saved yet — name your setup above and hit Save.", this.cfgX + 4, this.cfgListY + 17, -4935484, false);
         ctx.drawText(this.textRenderer, "Save a few setups, then press Use to switch between them.", this.cfgX + 4, this.cfgListY + 29, -4935484, false);
      }

      for(Object[] row : this.configRows) {
         String name = (String)row[0];
         int ry = (Integer)row[1];
         boolean hover = mx >= this.cfgX && mx < this.cfgX + this.cfgW && my >= ry && my < ry + 17;
         round(ctx, this.cfgX, ry, this.cfgW, 17, hover ? 1358954495 : 822083583);
         outline(ctx, this.cfgX, ry, this.cfgW, 17, hover ? withAlpha(-5018625, 176) : 1358954495);
         boolean active = name.equals(PackEditorConfig.activeConfig());
         if (active) {
            outline(ctx, this.cfgX, ry, this.cfgW, 17, -14505888);
         }

         ctx.fill(this.cfgX + 1, ry + 3, this.cfgX + 3, ry + 14, active ? -14505888 : accent(time * 0.2 + (double)name.hashCode() * 0.001));
         ctx.drawText(this.textRenderer, this.textRenderer.trimToWidth(name, this.cfgW - 200), this.cfgX + 8, ry + 5, -1, false);
         if (Profiles.isPreset(name) && !active) {
            int tx = this.cfgX + 12 + this.textRenderer.getWidth(this.textRenderer.trimToWidth(name, this.cfgW - 200));
            round(ctx, tx, ry + 4, this.textRenderer.getWidth("PRESET") + 6, 10, withAlpha(-6456321, 80));
            ctx.drawText(this.textRenderer, "PRESET", tx + 3, ry + 5, -3557121, false);
         }

         if (active) {
            int tx = this.cfgX + 12 + this.textRenderer.getWidth(this.textRenderer.trimToWidth(name, this.cfgW - 200));
            round(ctx, tx, ry + 4, this.textRenderer.getWidth("ACTIVE") + 6, 10, withAlpha(-14505888, 80));
            ctx.drawText(this.textRenderer, "ACTIVE", tx + 3, ry + 5, -9444448, false);
         }
      }

      if (names.size() > this.cfgRows) {
         int var10000 = this.profileScroll + 1;
         String more = var10000 + "-" + Math.min(names.size(), this.profileScroll + this.cfgRows) + " of " + names.size() + "  (scroll)";
         ctx.drawText(this.textRenderer, more, this.cfgX + this.cfgW - this.textRenderer.getWidth(more), this.cfgListY + 1, -4935484, false);
      }

   }

   private void rescan() {
      VariantRegistry.invalidate();
      this.variants = VariantRegistry.get();
      this.uploadedPacks = null;
      this.pending.entrySet().removeIf((e) -> VariantRegistry.find((List)this.variants.get(e.getKey()), (String)e.getValue()) == null);
   }

   private String summaryOf(String fileName) {
      List<String> tabs = new ArrayList();

      for(Category c : Category.values()) {
         for(Variant v : (List<Variant>)this.variants.getOrDefault(c, List.of())) {
            if (v instanceof Variant.PackVariant pv) {
               if (pv.pack.userAdded && pv.pack.fileName.equals(fileName) && !tabs.contains(c.group.displayName)) {
                  tabs.add(c.group.displayName);
               }
            }
         }
      }

      return tabs.isEmpty() ? "nothing this mod uses" : String.join(", ", tabs);
   }

   private void addPacks(List<Path> paths) {
      List<String> added = new ArrayList();
      String error = null;

      for(Path p : paths) {
         try {
            added.add(UserPacks.add(p));
         } catch (IOException e) {
            error = e.getMessage();
         }
      }

      if (!added.isEmpty()) {
         this.rescan();
         String first = (String)added.get(0);
         String summary = this.summaryOf(first);
         int tabs = summary.startsWith("nothing") ? 0 : summary.split(", ").length;
         String var10001 = added.size() == 1 ? "'" + first.replaceAll("(?i)\\.zip$", "") + "'" : added.size() + " packs";
         this.status = Text.literal("✔ Added " + var10001 + (added.size() == 1 ? " to " + tabs + " tab" + (tabs == 1 ? "" : "s") : "")).setStyle(Style.EMPTY.withColor(tabs <= 0 && added.size() <= 1 ? 16762967 : 7332768));
      } else if (error != null) {
         this.status = Text.literal(error).formatted(Formatting.RED);
      }

      this.clearAndInit();
   }

   public void onFilesDropped(List<Path> paths) {
      List<Path> images = paths.stream().filter(ImageColours::isImage).toList();
      if (!images.isEmpty()) {
         this.copyColours((Path)images.get(0));
         List<Path> rest = paths.stream().filter((p) -> !images.contains(p)).toList();
         if (rest.isEmpty()) {
            return;
         }

         paths = rest;
      }

      this.addPacks(paths);
   }

   private boolean takesPictures() {
      return this.section != null && !this.group.isPanel() && (this.current.drawable() && this.current != Category.CROSSHAIR || this.current == Category.SKY);
   }

   private String tabThing() {
      return this.current.material() == null ? this.current.displayName : (this.diamond ? "Diamond " : "Netherite ") + this.current.displayName;
   }

   private void renderDropBox(DrawContext ctx, double time) {
      if (this.dropY >= 0) {
         int bx = this.gridX;
         int bw = this.x1 - 10 - this.gridX;
         int bh = 16;
         round(ctx, bx, this.dropY, bw, bh, 806489882);

         for(int x = bx; x < bx + bw; x += 6) {
            ctx.fill(x, this.dropY, Math.min(x + 3, bx + bw), this.dropY + 1, withAlpha(accent(time), 160));
            ctx.fill(x, this.dropY + bh - 1, Math.min(x + 3, bx + bw), this.dropY + bh, withAlpha(accent(time), 160));
         }

         for(int y = this.dropY; y < this.dropY + bh; y += 6) {
            ctx.fill(bx, y, bx + 1, Math.min(y + 3, this.dropY + bh), withAlpha(accent(time), 160));
            ctx.fill(bx + bw - 1, y, bx + bw, Math.min(y + 3, this.dropY + bh), withAlpha(accent(time), 160));
         }

         int buttonX = this.buttons.stream().filter((b) -> b.label.equals("Choose…")).mapToInt(Btn::x).findFirst().orElse(bx + bw);
         int room = buttonX - bx - 10;
         String text = null;
         String[] wordings = this.current == Category.SKY ? new String[]{"⬇ Drop any Minecraft picture (e.g. an item png) to make a sky in its colour", "⬇ Drop any Minecraft picture to make a sky in its colour", "⬇ Drop a picture → sky colour", "⬇ Drop a picture here"} : new String[]{"⬇ Drop a screenshot here to copy its colours onto " + this.tabThing(), "⬇ Drop a screenshot to recolour " + this.tabThing(), "⬇ Drop screenshot → " + this.tabThing(), "⬇ Drop a screenshot here"};

         for(String s : wordings) {
            if (this.textRenderer.getWidth(s) <= room) {
               text = s;
               break;
            }
         }

         if (text == null) {
            text = this.textRenderer.trimToWidth("⬇ Drop a screenshot here", room);
         }

         ctx.drawText(this.textRenderer, text, bx + 5, this.dropY + 4, -1645585, false);
         this.dropHover = this.mouseInDrop(bx, bw, buttonX);
      }
   }

   private boolean mouseInDrop(int bx, int bw, int buttonX) {
      double mx = this.client.mouse.getScaledX(this.client.getWindow());
      double my = this.client.mouse.getScaledY(this.client.getWindow());
      return mx >= (double)bx && mx < (double)buttonX && my >= (double)this.dropY && my < (double)(this.dropY + 16);
   }

   private List<Text> dropTooltip() {
      return this.current == Category.SKY ? List.of(Text.literal("Sky colour from a picture").formatted(Formatting.BOLD), Text.literal("Drag any Minecraft png (an item like an end").formatted(Formatting.GRAY), Text.literal("crystal, a block, a screenshot...) onto this").formatted(Formatting.GRAY), Text.literal("window, or press Choose…. Its main colour").formatted(Formatting.GRAY), Text.literal("becomes your sky's Custom colour.").formatted(Formatting.GRAY)) : List.of(Text.literal("Copy colours from a screenshot").formatted(Formatting.BOLD), Text.literal("Drag a screenshot (png or jpg) of any pack's").formatted(Formatting.GRAY), Text.literal(this.tabThing() + " from your files onto this window,").formatted(Formatting.GRAY), Text.literal("or press Choose…. Only " + this.tabThing() + " changes:").formatted(Formatting.GRAY), Text.literal("the look you picked gets its colours.").formatted(Formatting.GRAY));
   }

   private void choosePicture() {
      Category forTab = this.current;
      this.status = Text.literal("Choose a picture in the window that opened...").formatted(Formatting.GRAY);
      Thread t = new Thread(() -> {
         String chosen;
         try {
            MemoryStack stack = MemoryStack.stackPush();

            try {
               PointerBuffer filters = stack.mallocPointer(5);

               for(String ext : new String[]{"*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"}) {
                  filters.put(stack.UTF8(ext));
               }

               filters.flip();
               chosen = TinyFileDialogs.tinyfd_openFileDialog("Choose a screenshot to copy colours from", pickerStart(), filters, "Pictures", false);
            } catch (Throwable var10) {
               if (stack != null) {
                  try {
                     stack.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (stack != null) {
               stack.close();
            }
         } catch (Throwable e) {
            chosen = null;
            PackEditorClient.LOGGER.warn("Picture picker failed", e);
         }

         String picked = chosen;
         this.client.execute(() -> {
            if (picked == null) {
               this.status = Text.literal("No picture chosen").formatted(Formatting.GRAY);
            } else if (this.current != forTab) {
               this.status = Text.literal("You switched tabs — open " + forTab.displayName + " and try again").formatted(Formatting.GOLD);
            } else {
               this.copyColours(Path.of(picked));
            }

            this.clearAndInit();
         });
      }, "APE picture picker");
      t.setDaemon(true);
      t.start();
   }

   private void copyColours(Path image) {
      if (this.section != null && this.current == Category.SKY) {
         String file = image.getFileName().toString().replaceAll("\\.[^.]+$", "");

         try {
            int colour = ImageColours.mainColour(image);
            this.customPreviews.put(Category.SKY, colour);
            this.pending.put(Category.SKY, (new Variant.RecolourVariant(colour)).id);
            this.status = Text.literal("✔ Sky now uses the colour of '" + file + "' — press Apply").setStyle(Style.EMPTY.withColor(7332768));
         } catch (Exception e) {
            PackEditorClient.LOGGER.warn("Couldn't take a sky colour from {}", image, e);
            this.status = Text.literal("Couldn't use that picture: " + e.getMessage()).formatted(Formatting.RED);
         }

      } else if (this.section != null && !this.group.isPanel() && this.current.drawable() && this.current != Category.CROSSHAIR) {
         String file = image.getFileName().toString().replaceAll("\\.[^.]+$", "");

         try {
            int[] palette = ImageColours.palette(image);
            Variant base = VariantRegistry.find(this.currentList(), (String)this.pending.getOrDefault(this.current, "vanilla"));
            if (base == null || base instanceof Variant.SizeVariant) {
               base = new Variant.Vanilla();
            }

            String name = this.uniqueDrawingName(Drawings.cleanName("Colours " + file));
            if (this.current == Category.RESPAWN_ANCHOR) {
               for(String[] part : AnchorParts.PARTS) {
                  Category.Target t = (Category.Target)this.current.targets.stream().filter((x) -> x.path().equals(part[0])).findFirst().orElse(null);
                  if (t != null) {
                     NativeImage img = recoloured(base, t, palette);

                     label95: {
                        try {
                           if (img != null) {
                              if (part[1] == null) {
                                 Drawings.save(this.current, name, img);
                              } else {
                                 Drawings.savePart(this.current, name, part[1], img);
                              }
                              break label95;
                           }
                        } catch (Throwable var15) {
                           if (img != null) {
                              try {
                                 img.close();
                              } catch (Throwable var14) {
                                 var15.addSuppressed(var14);
                              }
                           }

                           throw var15;
                        }

                        if (img != null) {
                           img.close();
                        }
                        continue;
                     }

                     if (img != null) {
                        img.close();
                     }
                  }
               }
            } else {
               Category.Target main = (Category.Target)this.current.targets.get(0);
               NativeImage img = recoloured(base, main, palette);

               try {
                  if (img == null) {
                     throw new IOException("This look has no picture to recolour");
                  }

                  Drawings.save(this.current, name, img);
                  int ticks = frameTime(base.loadMeta(main));
                  if (img.getHeight() > img.getWidth() && img.getHeight() % img.getWidth() == 0) {
                     Drawings.saveFrameTime(this.current, name, Math.max(1, ticks));
                  }
               } catch (Throwable var16) {
                  if (img != null) {
                     try {
                        img.close();
                     } catch (Throwable var13) {
                        var16.addSuppressed(var13);
                     }
                  }

                  throw var16;
               }

               if (img != null) {
                  img.close();
               }
            }

            this.rescan();
            this.pending.put(this.current, "drawn:" + name);
            String var10001 = this.tabThing();
            this.status = Text.literal("✔ " + var10001 + " now uses the colours from '" + file + "' — press Apply").setStyle(Style.EMPTY.withColor(7332768));
         } catch (Exception e) {
            PackEditorClient.LOGGER.warn("Couldn't copy colours from {}", image, e);
            this.status = Text.literal("Couldn't use that picture: " + e.getMessage()).formatted(Formatting.RED);
         }

      } else {
         this.status = Text.literal("Open a texture tab (e.g. Blocks › Fire), then drop the picture on this window").formatted(Formatting.GOLD);
      }
   }

   private static NativeImage recoloured(Variant base, Category.Target t, int[] palette) throws IOException {
      NativeImage src = base.loadImage(t);
      if (src == null) {
         src = (new Variant.Vanilla()).loadImage(t);
      }

      if (src == null) {
         return null;
      } else {
         int[] px = WornArmour.matchColours(WornArmour.pixels(src), palette);
         NativeImage out = WornArmour.toImage(px, src.getWidth(), src.getHeight());
         src.close();
         return out;
      }
   }

   private static int frameTime(byte[] meta) {
      if (meta == null) {
         return 0;
      } else {
         Matcher m = Pattern.compile("\"frametime\"\\s*:\\s*(\\d+)").matcher(new String(meta, StandardCharsets.UTF_8));
         return m.find() ? Integer.parseInt(m.group(1)) : 1;
      }
   }

   private String uniqueDrawingName(String wanted) {
      String base = wanted.isEmpty() ? "Colours" : wanted;
      List<String> taken = Drawings.list(this.current);
      if (!taken.contains(base)) {
         return base;
      } else {
         int i = 2;

         while(true) {
            String var10000 = base.substring(0, Math.min(base.length(), 21));
            String n = Drawings.cleanName(var10000 + " " + i);
            if (!taken.contains(n)) {
               return n;
            }

            ++i;
         }
      }
   }

   private static String pickerStart() {
      Path home = Path.of(System.getProperty("user.home"));
      Path downloads = home.resolve("Downloads");
      Path start = Files.isDirectory(downloads, new LinkOption[0]) ? downloads : home;
      String var10000 = String.valueOf(start.toAbsolutePath());
      return var10000 + File.separator;
   }

   private void uploadFolder() {
      this.status = Text.literal("Choose the pack's folder in the window that opened...").formatted(Formatting.GRAY);
      Thread t = new Thread(() -> {
         String chosen;
         try {
            chosen = TinyFileDialogs.tinyfd_selectFolderDialog("Choose your extracted resource pack folder", pickerStart());
         } catch (Throwable e) {
            chosen = null;
            PackEditorClient.LOGGER.warn("Folder picker failed", e);
         }

         String picked = chosen;
         this.client.execute(() -> {
            if (picked == null) {
               this.status = Text.literal("No folder chosen").formatted(Formatting.GRAY);
            } else {
               this.addPacks(List.of(Path.of(picked)));
            }

         });
      }, "APE folder picker");
      t.setDaemon(true);
      t.start();
   }

   private void uploadPack() {
      this.status = Text.literal("Choose a pack in the window that opened...").formatted(Formatting.GRAY);
      Thread t = new Thread(() -> {
         String chosen;
         try {
            MemoryStack stack = MemoryStack.stackPush();

            try {
               PointerBuffer filters = stack.mallocPointer(1);
               filters.put(stack.UTF8("*.zip"));
               filters.flip();
               chosen = TinyFileDialogs.tinyfd_openFileDialog("Upload a resource pack", pickerStart(), filters, "Resource packs (*.zip)", false);
            } catch (Throwable var6) {
               if (stack != null) {
                  try {
                     stack.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (stack != null) {
               stack.close();
            }
         } catch (Throwable e) {
            chosen = null;
            PackEditorClient.LOGGER.warn("File picker failed", e);
         }

         String picked = chosen;
         this.client.execute(() -> {
            if (picked == null) {
               this.status = Text.literal("No pack chosen — you can also drag a .zip onto this window").formatted(Formatting.GRAY);
            } else {
               this.addPacks(List.of(Path.of(picked)));
            }

         });
      }, "APE file picker");
      t.setDaemon(true);
      t.start();
   }

   private void removePack(String fileName) {
      if (!fileName.equals(this.confirmRemove)) {
         this.confirmRemove = fileName;
         this.status = Text.literal("Click Sure? to remove it").formatted(Formatting.GOLD);
      } else {
         this.confirmRemove = null;

         try {
            UserPacks.remove(fileName);
            this.rescan();
            this.status = Text.literal("Removed '" + fileName.replaceAll("(?i)\\.zip$", "") + "'").formatted(Formatting.GRAY);
         } catch (IOException e) {
            this.status = Text.literal("Couldn't remove it: " + e.getMessage()).formatted(Formatting.RED);
         }

      }
   }

   private void initMyPacks(int cx) {
      this.mpX = cx;
      this.mpW = this.x1 - 10 - cx;
      int y = this.y0 + 38;
      int ubw = Math.min(140, (this.mpW - 96) / 2);
      this.buttons.add(new Btn(PackEditorScreen.Kind.PRIMARY, cx, y + 12, ubw, 18, "+ Upload .zip", (Group)null, (Category)null, this::uploadPack));
      this.buttons.add(new Btn(PackEditorScreen.Kind.PRIMARY, cx + ubw + 4, y + 12, ubw, 18, "+ Upload Folder", (Group)null, (Category)null, this::uploadFolder));
      this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, cx + 2 * (ubw + 4), y + 12, 84, 18, "Open Folder", (Group)null, (Category)null, () -> {
         try {
            Files.createDirectories(UserPacks.dir());
         } catch (IOException var1) {
         }

         Util.getOperatingSystem().open(UserPacks.dir());
      }));
      this.mpDropY = y + 36;
      this.mpListY = this.mpDropY + 40;
      int deleted = PackEditorConfig.deletedCount();
      this.restoreW = 0;
      if (deleted > 0) {
         String label = "Restore deleted (" + deleted + ")";
         this.restoreW = this.textRenderer.getWidth(label) + 12;
         this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, cx + this.mpW - this.restoreW, this.mpListY - 3, this.restoreW, 14, label, (Group)null, (Category)null, () -> {
            PackEditorConfig.restoreDeleted();
            this.rescan();
            this.status = Text.literal("✔ Brought back " + deleted + " deleted texture" + (deleted == 1 ? "" : "s")).setStyle(Style.EMPTY.withColor(7332768));
         }));
      }

      int rowsTop = this.mpListY + 12;
      this.mpRows = Math.max(1, (this.y1 - 32 - rowsTop) / 19);
      if (this.uploadedPacks == null) {
         this.uploadedPacks = UserPacks.scan();
      }

      List<ExternalPack> uploaded = this.uploadedPacks;
      this.packScroll = Math.max(0, Math.min(this.packScroll, Math.max(0, uploaded.size() - this.mpRows)));
      this.packRows.clear();

      for(int i = 0; i < this.mpRows && this.packScroll + i < uploaded.size(); ++i) {
         ExternalPack p = (ExternalPack)uploaded.get(this.packScroll + i);
         int ry = rowsTop + i * 19;
         this.packRows.add(new Object[]{p.fileName, p.displayName, this.summaryOf(p.fileName), ry});
         String file = p.fileName;
         this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, cx + this.mpW - 50, ry + 1, 46, 15, file.equals(this.confirmRemove) ? "Sure?" : "Remove", (Group)null, (Category)null, () -> this.removePack(file)));
      }

   }

   private void renderMyPacks(DrawContext ctx, int mx, int my, double time) {
      int y = this.y0 + 38;
      Text title = Text.literal("Your own packs").formatted(Formatting.BOLD);
      ctx.drawText(this.textRenderer, title, this.mpX, y + 1, -1, true);
      int subX = this.mpX + this.textRenderer.getWidth(title) + 6;
      ctx.drawText(this.textRenderer, this.textRenderer.trimToWidth("any pack works — its textures go into every tab", this.mpX + this.mpW - subX), subX, y + 1, -4935484, false);
      int dz = this.mpDropY;
      int dh = 32;
      round(ctx, this.mpX, dz, this.mpW, dh, 1074925338);
      int phase = 0;

      for(int x = this.mpX + phase; x < this.mpX + this.mpW; x += 6) {
         int c = accent(time * 0.2 + (double)(x - this.mpX) / (double)this.mpW);
         ctx.fill(x, dz, Math.min(x + 3, this.mpX + this.mpW), dz + 1, c);
         ctx.fill(x, dz + dh - 1, Math.min(x + 3, this.mpX + this.mpW), dz + dh, c);
      }

      for(int yy = dz + phase; yy < dz + dh; yy += 6) {
         ctx.fill(this.mpX, yy, this.mpX + 1, Math.min(yy + 3, dz + dh), accent(time * 0.2));
         ctx.fill(this.mpX + this.mpW - 1, yy, this.mpX + this.mpW, Math.min(yy + 3, dz + dh), accent(time * 0.2 + (double)0.5F));
      }

      String drop = "⬇  Drag & drop pack .zip files (or folders) onto this window";
      ctx.drawCenteredTextWithShadow(this.textRenderer, drop, this.mpX + this.mpW / 2, dz + 7, -1);
      ctx.drawCenteredTextWithShadow(this.textRenderer, "previews, hotbar icons & armour are made automatically", this.mpX + this.mpW / 2, dz + 19, -4935484);
      List<ExternalPack> uploaded = this.uploadedPacks != null ? this.uploadedPacks : List.of();
      ctx.drawText(this.textRenderer, Text.literal("Uploaded").formatted(Formatting.BOLD).append(Text.literal("  (" + uploaded.size() + ")").formatted(Formatting.DARK_GRAY)), this.mpX, this.mpListY + 1, -1, true);
      if (uploaded.isEmpty()) {
         int ty = this.mpListY + 17;

         for(OrderedText line : this.textRenderer.wrapLines(Text.literal("No packs yet. Uploaded packs show up with a YOURS tag in each tab they change."), Math.max(60, this.mpW - 8))) {
            ctx.drawText(this.textRenderer, line, this.mpX + 4, ty, -4935484, false);
            ty += 10;
         }
      }

      for(Object[] row : this.packRows) {
         int ry = (Integer)row[3];
         boolean hover = mx >= this.mpX && mx < this.mpX + this.mpW && my >= ry && my < ry + 17;
         round(ctx, this.mpX, ry, this.mpW, 17, hover ? 1358954495 : 822083583);
         outline(ctx, this.mpX, ry, this.mpW, 17, hover ? withAlpha(-37931, 176) : 1358954495);
         ctx.fill(this.mpX + 1, ry + 3, this.mpX + 3, ry + 14, -37931);
         String name = this.textRenderer.trimToWidth((String)row[1], Math.max(40, this.mpW / 3));
         ctx.drawText(this.textRenderer, name, this.mpX + 8, ry + 5, -1, false);
         int sx = this.mpX + 14 + this.textRenderer.getWidth(name);
         ctx.drawText(this.textRenderer, this.textRenderer.trimToWidth("→ " + String.valueOf(row[2]), this.mpX + this.mpW - 56 - sx), sx, ry + 5, -4935484, false);
      }

      if (uploaded.size() > this.mpRows) {
         int var10000 = this.packScroll + 1;
         String more = var10000 + "-" + Math.min(uploaded.size(), this.packScroll + this.mpRows) + " of " + uploaded.size() + "  (scroll)";
         ctx.drawText(this.textRenderer, more, this.mpX + this.mpW - this.restoreW - (this.restoreW > 0 ? 6 : 0) - this.textRenderer.getWidth(more), this.mpListY + 1, -4935484, false);
      }

   }

   private void filterItems() {
      String q = this.searchText.trim().toLowerCase(Locale.ROOT);
      this.itemList = ItemTextures.all().stream().filter((e) -> {
         if (this.onlyDrawn && !ItemTextures.isDrawn(e.id())) {
            return false;
         } else if (q.isEmpty()) {
            return true;
         } else {
            return ItemTextures.searchName(e).contains(q) || e.id().replace('_', ' ').contains(q) || e.id().contains(q);
         }
      }).toList();
      this.clampItemScroll();
   }

   private void clampItemScroll() {
      int total = (this.itemList.size() + Math.max(1, this.itCols) - 1) / Math.max(1, this.itCols);
      this.itScroll = Math.max(0, Math.min(this.itScroll, total - this.itRows));
   }

   private void initItems(int cx) {
      this.itX = cx;
      this.itW = this.x1 - 10 - cx;
      int y = this.y0 + 38;
      int fw = this.textRenderer.getWidth("Show: Drawn") + 14;
      this.searchField = new TextFieldWidget(this.textRenderer, cx + 1, y + 1, this.itW - fw - 6, 14, Text.literal("Search items"));
      this.searchField.setMaxLength(40);
      this.searchField.setText(this.searchText);
      this.searchField.setChangedListener((s) -> {
         this.searchText = s;
         this.itScroll = 0;
         this.filterItems();
      });
      this.searchField.setPlaceholder(Text.literal("Search items... (e.g. sword, apple)").formatted(Formatting.DARK_GRAY));
      this.addSelectableChild(this.searchField);
      this.setFocused(this.searchField);
      this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, cx + this.itW - fw, y, fw, 16, this.onlyDrawn ? "Show: Drawn" : "Show: All", (Group)null, (Category)null, () -> {
         this.onlyDrawn = !this.onlyDrawn;
         this.itScroll = 0;
      }));
      this.itGridY = y + 32;
      this.itCols = Math.max(1, (this.itW - 6) / 22);
      this.itRows = Math.max(1, (this.y1 - 32 - this.itGridY) / 22);
      this.filterItems();
   }

   private ItemTextures.Entry itemAt(double mx, double my) {
      if (!(mx < (double)this.itX) && !(my < (double)this.itGridY)) {
         int col = (int)((mx - (double)this.itX) / (double)22.0F);
         int row = (int)((my - (double)this.itGridY) / (double)22.0F);
         if (col < this.itCols && row < this.itRows) {
            int i = (this.itScroll + row) * this.itCols + col;
            return i < this.itemList.size() ? (ItemTextures.Entry)this.itemList.get(i) : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private ItemTextures.Entry renderItems(DrawContext ctx, int mx, int my, float delta, double time) {
      round(ctx, this.itX - 1, this.y0 + 38, this.searchField.getWidth() + 2, 16, 1342177280);
      outline(ctx, this.itX - 1, this.y0 + 38, this.searchField.getWidth() + 2, 16, this.searchField.isFocused() ? accent(time * 0.2) : 1358954495);
      this.searchField.render(ctx, mx, my, delta);
      int drawnCount = ItemTextures.drawn().size();
      int var10000 = this.itemList.size();
      String info = var10000 + " items" + (drawnCount > 0 ? " · " + drawnCount + " drawn by you" : "") + " — click one to draw it";
      ctx.drawText(this.textRenderer, this.textRenderer.trimToWidth(info, this.itW), this.itX, this.itGridY - 12, -4935484, false);
      ItemTextures.Entry hovered = this.itemAt((double)mx, (double)my);

      for(int r = 0; r < this.itRows; ++r) {
         for(int c = 0; c < this.itCols; ++c) {
            int i = (this.itScroll + r) * this.itCols + c;
            if (i >= this.itemList.size()) {
               break;
            }

            ItemTextures.Entry e = (ItemTextures.Entry)this.itemList.get(i);
            int sx = this.itX + c * 22;
            int sy = this.itGridY + r * 22;
            boolean hover = e == hovered;
            boolean drawn = ItemTextures.isDrawn(e.id());
            round(ctx, sx, sy, 20, 20, hover ? 1358954495 : 822083583);
            outline(ctx, sx, sy, 20, 20, hover ? accent(time * 0.2) : (drawn ? withAlpha(-24765, 192) : 1358954495));
            ctx.drawItem(new ItemStack(e.item()), sx + 2, sy + 2);
            if (drawn) {
               ctx.fill(sx + 22 - 7, sy + 2, sx + 22 - 4, sy + 5, -24765);
            }
         }
      }

      if (this.itemList.isEmpty()) {
         ctx.drawText(this.textRenderer, this.onlyDrawn && this.searchText.isBlank() ? "You haven't drawn any items yet." : "No items match '" + this.searchText + "'", this.itX + 4, this.itGridY + 6, -4935484, false);
      }

      int total = (this.itemList.size() + this.itCols - 1) / this.itCols;
      if (total > this.itRows) {
         int gh = this.itRows * 22 - 2;
         int barH = Math.max(14, gh * this.itRows / total);
         int barY = this.itGridY + (gh - barH) * this.itScroll / Math.max(1, total - this.itRows);
         round(ctx, this.itX + this.itCols * 22 + 2, this.itGridY, 3, gh, 822083583);
         round(ctx, this.itX + this.itCols * 22 + 2, barY, 3, barH, accent(time * 0.2));
      }

      return hovered;
   }

   private void openItemDraw(ItemTextures.Entry e) {
      boolean custom = ItemTextures.isDrawn(e.id());
      NativeImage start = null;

      try {
         start = custom ? ItemTextures.load(e.id()) : null;
         boolean animated = custom && start != null && ItemTextures.frameTime(e.id()) > 0;
         if (start == null) {
            start = ItemTextures.loadVanilla(e);
         }

         if (start != null && !animated) {
            start = PreviewImages.firstFrame(start);
         }
      } catch (Exception ex) {
         PackEditorClient.LOGGER.warn("Couldn't load {} to draw on", e.id(), ex);
      }

      this.client.setScreen(DrawScreen.forItem(this, e, start, custom));
   }

   void onItemDrawn(ItemTextures.Entry e, boolean saved) {
      this.itemsDirty = true;
      String name = (new ItemStack(e.item())).getName().getString();
      this.status = saved ? Text.literal("✔ Saved your " + name + " — press Apply to equip").setStyle(Style.EMPTY.withColor(7332768)) : Text.literal(name + " is back to normal after Apply").formatted(Formatting.GRAY);
   }

   private boolean groupChanged(Group g) {
      if (g == Group.ITEMS) {
         return !ItemTextures.drawn().isEmpty();
      } else if (g == Group.TOTEMS && this.customized(Category.TOTEM_SIZE)) {
         return true;
      } else {
         return g == Group.COBWEB && this.cobwebOpacity < 100 ? true : g.categories().stream().anyMatch(this::customized);
      }
   }

   private void openDraw() {
      Category.Target main = (Category.Target)this.current.targets.get(0);
      Variant sel = VariantRegistry.find(this.currentList(), (String)this.pending.getOrDefault(this.current, "vanilla"));
      if (this.current == Category.END_CRYSTAL) {
         NativeImage ent = null;

         try {
            if (sel != null) {
               ent = sel.loadImage(main);
            }

            if (ent != null && (ent.getWidth() % 64 != 0 || ent.getWidth() > 256 || ent.getHeight() * 2 != ent.getWidth())) {
               ent.close();
               ent = null;
            }

            if (ent == null) {
               ent = (new Variant.Vanilla()).loadImage(main);
            }
         } catch (Exception e) {
            PackEditorClient.LOGGER.warn("Couldn't load crystal to draw on", e);
         }

         if (ent == null) {
            this.status = Text.literal("Couldn't load the crystal texture").formatted(Formatting.RED);
         } else {
            MinecraftClient var20 = this.client;
            String var21;
            if (sel instanceof Variant.DrawnVariant) {
               Variant.DrawnVariant d = (Variant.DrawnVariant)sel;
               var21 = d.drawingName;
            } else {
               var21 = null;
            }

            var20.setScreen(DrawScreen.forCrystal(this, ent, var21));
         }
      } else if (this.current == Category.RESPAWN_ANCHOR) {
         List<String[]> parts = AnchorParts.PARTS;
         NativeImage[] starts = new NativeImage[parts.size()];

         for(int i = 0; i < parts.size(); ++i) {
            String path = ((String[])parts.get(i))[0];
            Category.Target t = (Category.Target)this.current.targets.stream().filter((x) -> x.path().equals(path)).findFirst().orElse(main);
            starts[i] = editableStart(sel, t);
         }

         MinecraftClient var19 = this.client;
         String var10003;
         if (sel instanceof Variant.DrawnVariant) {
            Variant.DrawnVariant d = (Variant.DrawnVariant)sel;
            var10003 = d.drawingName;
         } else {
            var10003 = null;
         }

         var19.setScreen(DrawScreen.forAnchor(this, starts, var10003));
      } else {
         NativeImage start = null;

         try {
            if (sel != null && !(sel instanceof Variant.SizeVariant)) {
               start = sel.loadImage(main);
            }

            boolean var10000;
            label107: {
               if (sel instanceof Variant.DrawnVariant) {
                  Variant.DrawnVariant dv = (Variant.DrawnVariant)sel;
                  if (Drawings.frameTime(this.current, dv.drawingName) > 0) {
                     var10000 = true;
                     break label107;
                  }
               }

               var10000 = false;
            }

            boolean animated = var10000;
            if (start != null && !animated) {
               start = PreviewImages.firstFrame(start);
            }

            if (start != null && (start.getWidth() > 32 || !animated && start.getHeight() > 32)) {
               start.close();
               start = null;
            }

            if (start == null) {
               start = (new Variant.Vanilla()).loadImage(main);
               if (start != null) {
                  start = PreviewImages.firstFrame(start);
               }
            }
         } catch (Exception e) {
            PackEditorClient.LOGGER.warn("Couldn't load texture to draw on", e);
         }

         String var18;
         if (sel instanceof Variant.DrawnVariant) {
            Variant.DrawnVariant d = (Variant.DrawnVariant)sel;
            var18 = d.drawingName;
         } else {
            var18 = null;
         }

         String editing = var18;
         this.client.setScreen(new DrawScreen(this, this.current, start, editing));
      }
   }

   private static NativeImage editableStart(Variant sel, Category.Target t) {
      NativeImage img = null;

      try {
         if (sel != null) {
            img = sel.loadImage(t);
         }

         if (img != null) {
            img = PreviewImages.firstFrame(img);
         }

         if (img != null && (img.getWidth() > 32 || img.getHeight() > 32)) {
            img.close();
            img = null;
         }

         if (img == null) {
            img = (new Variant.Vanilla()).loadImage(t);
            if (img != null) {
               img = PreviewImages.firstFrame(img);
            }
         }
      } catch (Exception e) {
         PackEditorClient.LOGGER.warn("Couldn't load {} to draw on", t.path(), e);
      }

      return img;
   }

   void onDrawingSaved(Category c, String name) {
      this.rescan();
      this.pending.put(c, "drawn:" + name);
      this.status = Text.literal("✔ Saved drawing '" + name + "' — press Apply to equip").setStyle(Style.EMPTY.withColor(7332768));
   }

   void onDrawingDeleted(Category c, String name) {
      this.rescan();
      this.status = Text.literal("Deleted drawing '" + name + "'").formatted(Formatting.GRAY);
   }

   private static void drawPencil(DrawContext ctx, int x, int y, int body) {
      for(int i = 0; i < 7; ++i) {
         ctx.fill(x + 3 + i, y + 8 - i, x + 5 + i, y + 10 - i, body);
      }

      ctx.fill(x + 2, y + 9, x + 4, y + 11, -1521248);
      ctx.fill(x + 1, y + 11, x + 2, y + 12, -14013910);
      ctx.fill(x + 10, y + 1, x + 12, y + 3, -37931);
   }

   private Category matchMaterial(Category c) {
      boolean isDiamond = "diamond".equals(c.material());
      return c.material() != null && isDiamond != this.diamond ? c.counterpart() : c;
   }

   private boolean isOn(Toggle t) {
      return (Boolean)this.toggles.getOrDefault(t, false);
   }

   private boolean shownOn(Toggle t) {
      return this.isOn(t) != t.inverted;
   }

   private Toggle toggleFor(Btn b) {
      for(Toggle t : Toggle.values()) {
         if (t.label.equals(b.label) && (t.group == null || t.group == this.group)) {
            return t;
         }
      }

      for(Toggle t : Toggle.values()) {
         if (t.label.equals(b.label)) {
            return t;
         }
      }

      return null;
   }

   public PackEditorScreen(Screen parent) {
      super(Text.literal("Awerial's Pack Editor"));
      this.group = Group.END_CRYSTALS;
      this.current = Category.END_CRYSTAL;
      this.status = Text.empty();
      this.customPreviews = new EnumMap(Category.class);
      this.cobwebOpacity = 100;
      this.lastGroup = new EnumMap(Section.class);
      this.nameText = "";
      this.codeText = "";
      this.configRows = new ArrayList();
      this.packRows = new ArrayList();
      this.dropY = -1;
      this.searchText = "";
      this.itemList = List.of();
      this.toggles = new EnumMap(Toggle.class);
      this.shieldColor = -1;
      this.hue = 0.52F;
      this.sat = 1.0F;
      this.val = 1.0F;
      this.disabledShieldColor = -1;
      this.parent = parent;
   }

   protected void init() {
      if (this.effects == null) {
         this.effects = new EffectsPanel(this.textRenderer);
      }

      if (this.variants == null) {
         this.variants = VariantRegistry.get();
         this.pending.putAll(PackEditorConfig.all());
         this.toggles.putAll(PackEditorConfig.toggles());
         this.shieldColor = PackEditorConfig.shieldColor();
         this.disabledShieldColor = PackEditorConfig.disabledShieldColor();
         this.cobwebOpacity = PackEditorConfig.cobwebOpacity();
         if (this.shieldColor >= 0) {
            this.setHsvFrom(this.shieldColor);
         }

         this.crosshairSize = PackEditorConfig.crosshairSize();
      }

      this.buttons.clear();
      this.x0 = 6;
      this.y0 = 6;
      this.x1 = this.width - 6;
      this.y1 = this.height - 6;
      if (this.section == null) {
         this.initHome();
      } else {
         this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, this.x0 + 8, this.y0 + 8, 52, 20, "‹ Back", (Group)null, (Category)null, () -> this.section = null));
         List<Group> groups = this.section.groups;
         boolean sidebar = groups.size() > 1;
         int sideTop = this.y0 + 38;
         this.tabH = Math.max(14, Math.min(24, (this.y1 - 30 - sideTop) / Math.max(1, groups.size())));
         if (sidebar) {
            for(int i = 0; i < groups.size(); ++i) {
               Group g = (Group)groups.get(i);
               this.buttons.add(new Btn(PackEditorScreen.Kind.TAB, this.x0 + 6, sideTop + i * this.tabH, 116, this.tabH - 2, g.displayName, g, (Category)null, () -> {
                  this.group = g;
                  this.lastGroup.put(this.section, g);
                  this.confirmDelete = null;
                  this.confirmRemove = null;
                  if (!g.isPanel()) {
                     this.current = this.matchMaterial((Category)this.lastPiece.getOrDefault(g, (Category)g.categories(this.diamond).get(0)));
                  }

                  this.scroll = (double)0.0F;
               }));
            }
         }

         int contentX = sidebar ? this.x0 + 6 + 116 + 10 : this.x0 + 12;
         int y = this.y0 + 38;
         List<Category> pieces = this.group.categories(this.diamond);
         if (pieces.size() > 1) {
            int px = contentX;
            int switchW = this.group.hasMaterials() ? this.textRenderer.getWidth("Netherite") + this.textRenderer.getWidth("Diamond") + 52 + 8 : 0;
            int room = this.x1 - 10 - switchW - contentX;
            int full = 0;

            for(Category c : pieces) {
               full += 30 + this.textRenderer.getWidth(c.displayName) + 4;
            }

            boolean iconsOnly = full > room;

            for(Category c : pieces) {
               int w = iconsOnly ? 22 : 30 + this.textRenderer.getWidth(c.displayName);
               this.buttons.add(new Btn(PackEditorScreen.Kind.PIECE, px, y, w, 18, iconsOnly ? "" : c.displayName, (Group)null, c, () -> {
                  this.current = c;
                  this.lastPiece.put(this.group, c);
                  this.scroll = (double)0.0F;
               }));
               px += w + 4;
            }

            y += 22;
         }

         this.headlineY = y + 1;
         this.gridX = contentX;
         this.gridY = y + 14;
         this.gridW = this.x1 - 8 - this.gridX;
         this.dropY = -1;
         if (this.takesPictures()) {
            this.dropY = this.gridY - 1;
            this.gridY += 18;
            int bw = this.textRenderer.getWidth("Choose…") + 10;
            this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, this.x1 - 10 - bw, this.dropY + 2, bw, 12, "Choose…", (Group)null, (Category)null, this::choosePicture));
         }

         this.gridH = this.y1 - 30 - this.gridY;
         if (this.hasBar()) {
            this.initBar();
         }

         this.cols = Math.max(1, (this.gridW + 5) / 63);
         this.clampScroll();
         if (this.group == Group.SHIELDS) {
            this.pkX = this.gridX;
            this.pkY = this.gridY + 72 + 10;
            this.pkW = Math.min(this.gridW - 8, 310);
            int avail = this.y1 - 34 - this.pkY;
            this.svH = Math.max(18, Math.min(56, avail - 56));
            this.svX = this.pkX + 6;
            this.svY = this.pkY + 22;
            this.svW = this.pkW - 12;
            this.hueY = this.svY + this.svH + 5;
            this.presetY = this.hueY + 11;
            this.pkH = this.presetY + 12 + 6 - this.pkY;
            this.defX = this.pkX + this.pkW - 46;
            this.defY = this.presetY - 1;
         }

         if (this.group.hasMaterials()) {
            int nw = this.textRenderer.getWidth("Netherite") + 26;
            int dw = this.textRenderer.getWidth("Diamond") + 26;
            int mx = this.x1 - 10 - nw - dw;
            int my = this.y0 + 38;
            this.buttons.add(new Btn(PackEditorScreen.Kind.MATERIAL, mx, my, dw, 18, "Diamond", (Group)null, (Category)null, () -> {
               this.diamond = true;
               this.current = this.matchMaterial(this.current);
               this.lastPiece.put(this.group, this.current);
            }));
            this.buttons.add(new Btn(PackEditorScreen.Kind.MATERIAL, mx + dw, my, nw, 18, "Netherite", (Group)null, (Category)null, () -> {
               this.diamond = false;
               this.current = this.matchMaterial(this.current);
               this.lastPiece.put(this.group, this.current);
            }));
         }

         if (this.group == Group.CONFIGS) {
            this.initConfigs(contentX);
         }

         if (this.group == Group.MY_PACKS) {
            this.initMyPacks(contentX);
         }

         if (this.group == Group.ITEMS) {
            this.initItems(contentX);
         }

         if (this.group.isEffects()) {
            this.effects.layout(this.group, contentX, this.y0 + 38, this.x1 - 10 - contentX, this.y1 - 32);
         }

         int rightEdge = this.x1 - 10;
         int barX = this.gridX;

         for(Toggle t : Toggle.values()) {
            if (t.group == this.group) {
               int w = this.textRenderer.getWidth(t.label) + 40;
               int ty;
               int tx;
               if (this.togglesInBar()) {
                  tx = barX;
                  ty = this.barY + 4;
                  barX += w + 18;
               } else {
                  tx = rightEdge - w;
                  ty = this.headlineY - 3;
                  rightEdge -= w + 10;
               }

               this.buttons.add(new Btn(PackEditorScreen.Kind.TOGGLE, tx, ty, w, 14, t.label, (Group)null, (Category)null, () -> {
                  this.toggles.put(t, !this.isOn(t));
                  if (t == Toggle.LEGACY_GLINT && this.isOn(t) && !Variant.legacyGlintAvailable()) {
                     LegacyGlint.prepare();
                     this.status = Text.literal("Getting the 1.19 glint from Minecraft's servers...").formatted(Formatting.GRAY);
                     this.waitingForLegacyGlint = true;
                  }

               }));
            }
         }

         if (rightEdge != this.x1 - 10) {
            rightEdge += 4;
         }

         if (!this.group.isPanel() && this.current.drawable()) {
            this.buttons.add(new Btn(PackEditorScreen.Kind.PENCIL, rightEdge - 22, this.headlineY - 4, 22, 16, "Draw your own", (Group)null, (Category)null, this::openDraw));
         }

         int by = this.y1 - 23;
         int bx = this.x1 - 6;
         bx -= 88;
         this.buttons.add(new Btn(PackEditorScreen.Kind.PRIMARY, bx, by, 88, 18, "✔ Apply", (Group)null, (Category)null, this::apply));
         bx -= 56;
         this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, bx, by, 52, 18, "Close", (Group)null, (Category)null, this::close));
         if (this.group != Group.ITEMS && !this.group.isEffects()) {
            bx -= 60;
            this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, bx, by, 56, 18, "Reset", (Group)null, (Category)null, () -> this.pending.put(this.current, "vanilla")));
         }

         if (!this.group.isPanel()) {
            String var10000 = this.current.id;
            boolean armed = (var10000 + "|" + (String)this.pending.getOrDefault(this.current, "vanilla")).equals(this.confirmDeleteTexture);
            bx -= 56;
            this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, bx, by, 52, 18, armed ? "Sure?" : "Delete", (Group)null, (Category)null, this::deleteTexture));
         }

         if (this.group != Group.MY_PACKS) {
            bx -= 82;
            this.buttons.add(new Btn(PackEditorScreen.Kind.BUTTON, bx, by, 78, 18, "Packs Folder", (Group)null, (Category)null, () -> Util.getOperatingSystem().open(this.client.getResourcePackDir())));
         }

      }
   }

   private void apply() {
      if (this.equip()) {
         if (this.client.player != null) {
            this.client.inGameHud.setOverlayMessage(Text.literal("✦ Awerial's Pack Editor — look equipped!").setStyle(Style.EMPTY.withColor(11758591)), false);
         }

         this.close();
      }
   }

   private boolean equip() {
      try {
         PackEditorConfig.save(this.pending, this.toggles, this.shieldColor, this.crosshairSize, this.disabledShieldColor, this.cobwebOpacity);
         int n = PackBuilder.apply(this.pending, this.variants, this.toggles, this.shieldColor, this.disabledShieldColor, this.cobwebOpacity);
         PackEditorClient.LOGGER.info("Applied {} files", n);
         this.itemsDirty = false;
         return true;
      } catch (Exception e) {
         PackEditorClient.LOGGER.error("Failed to build pack", e);
         this.status = Text.literal("Couldn't apply: " + e.getMessage()).formatted(Formatting.RED);
         return false;
      }
   }

   private List<Variant> currentList() {
      return (List)this.variants.get(this.current);
   }

   private int contentHeight() {
      int rows = (this.currentList().size() + this.cols - 1) / this.cols;
      return rows * 77;
   }

   private void clampScroll() {
      this.scroll = Math.max((double)0.0F, Math.min((double)Math.max(0, this.contentHeight() - this.gridH), this.scroll));
   }

   private boolean customized(Category c) {
      return !((String)this.pending.getOrDefault(c, "vanilla")).equals("vanilla");
   }

   private int unsavedChanges() {
      int n = 0;

      for(Category c : Category.values()) {
         if (!((String)this.pending.getOrDefault(c, "vanilla")).equals(PackEditorConfig.get(c))) {
            ++n;
         }
      }

      for(Toggle t : Toggle.values()) {
         if (this.isOn(t) != PackEditorConfig.get(t)) {
            ++n;
         }
      }

      if (this.shieldColor != PackEditorConfig.shieldColor()) {
         ++n;
      }

      if (this.disabledShieldColor != PackEditorConfig.disabledShieldColor()) {
         ++n;
      }

      if (this.cobwebOpacity != PackEditorConfig.cobwebOpacity()) {
         ++n;
      }

      if (this.crosshairSize != PackEditorConfig.crosshairSize()) {
         ++n;
      }

      if (this.itemsDirty) {
         ++n;
      }

      return n;
   }

   public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
      if (this.waitingForLegacyGlint && Variant.legacyGlintAvailable()) {
         this.waitingForLegacyGlint = false;
         this.status = Text.literal("✔ Legacy Glint ready — press Apply").setStyle(Style.EMPTY.withColor(7332768));
      }

      super.render(ctx, mouseX, mouseY, delta);
      double time = (double)Util.getMeasuringTimeMs() / (double)1000.0F;
      if (this.section == null) {
         this.renderHome(ctx, mouseX, mouseY);
      } else {
         round(ctx, this.x0, this.y0, this.x1 - this.x0, this.y1 - this.y0, 1880100888);
         outline(ctx, this.x0, this.y0, this.x1 - this.x0, this.y1 - this.y0, 1090519039);
         this.renderHeader(ctx, time);
         this.renderSidebar(ctx, mouseX, mouseY, time);
         Variant hovered = null;
         ItemTextures.Entry hoveredItem = null;
         this.effectsTip = null;
         if (this.group == Group.CONFIGS) {
            this.renderConfigs(ctx, mouseX, mouseY, delta, time);
         } else if (this.group == Group.MY_PACKS) {
            this.renderMyPacks(ctx, mouseX, mouseY, time);
         } else if (this.group == Group.ITEMS) {
            hoveredItem = this.renderItems(ctx, mouseX, mouseY, delta, time);
         } else if (this.group.isEffects()) {
            this.effectsTip = this.effects.render(ctx, mouseX, mouseY, time);
         } else {
            this.renderPieces(ctx, mouseX, mouseY, time);
            this.renderHeadline(ctx, time);
            this.renderDropBox(ctx, time);
            this.renderToggles(ctx, mouseX, mouseY, time);
            hovered = this.renderGrid(ctx, mouseX, mouseY, time);
         }

         if (this.group == Group.SHIELDS) {
            this.renderShieldColor(ctx, mouseX, mouseY, time);
         }

         if (this.hasBar() && !this.group.isPanel()) {
            this.renderBar(ctx, mouseX, mouseY, time);
         }

         this.renderFooter(ctx, mouseX, mouseY, time);
         if (this.colourPopup != null && (this.popupCategory != this.current || this.group.isPanel())) {
            this.colourPopup = null;
         }

         if (this.colourPopup != null) {
            this.colourPopup.render(ctx, this.textRenderer, time);
            if (this.colourPopup.contains((double)mouseX, (double)mouseY)) {
               hovered = null;
            }
         }

         if (this.effectsTip != null) {
            ctx.drawTooltip(this.textRenderer, this.effectsTip, mouseX, mouseY);
         }

         if (this.dropY >= 0 && this.dropHover && !this.group.isPanel()) {
            ctx.drawTooltip(this.textRenderer, this.dropTooltip(), mouseX, mouseY);
         }

         if (hoveredItem != null) {
            List<Text> tip = new ArrayList();
            tip.add((new ItemStack(hoveredItem.item())).getName().copy().formatted(new Formatting[]{Formatting.WHITE, Formatting.BOLD}));
            tip.add(Text.literal("minecraft:" + hoveredItem.id()).formatted(Formatting.DARK_GRAY));
            tip.add(ItemTextures.isDrawn(hoveredItem.id()) ? Text.literal("✎ Drawn by you — click to edit").setStyle(Style.EMPTY.withColor(16752451)) : Text.literal("Click to draw your own").formatted(Formatting.GRAY));
            if (WornArmour.wornTarget(hoveredItem.id()) != null) {
               tip.add(Text.literal("Also changes the armour when worn").formatted(Formatting.GRAY));
            }

            ctx.drawTooltip(this.textRenderer, tip, mouseX, mouseY);
         }

         if (hovered != null) {
            List<Text> tip = new ArrayList();
            tip.add(Text.literal(hovered.name).formatted(new Formatting[]{Formatting.WHITE, Formatting.BOLD}));
            tip.add(Text.literal(hovered.source).formatted(Formatting.GRAY));
            if (hovered instanceof Variant.RecolourVariant) {
               tip.add(Text.literal("Pick any colour").formatted(Formatting.GRAY));
               if (this.current.drawable() && this.current != Category.CROSSHAIR) {
                  tip.add(Text.literal("Tip: drop a screenshot of a pack on this window").formatted(Formatting.DARK_GRAY));
                  tip.add(Text.literal("to copy its exact colours").formatted(Formatting.DARK_GRAY));
               }
            }

            if (hovered.id.equals(PackEditorConfig.get(this.current))) {
               tip.add(Text.literal("✔ Equipped").formatted(Formatting.GREEN));
            } else {
               tip.add(Text.literal("Click to select, then press Apply").formatted(Formatting.GRAY));
            }

            ctx.drawTooltip(this.textRenderer, tip, mouseX, mouseY);
         }

      }
   }

   private void renderHeader(DrawContext ctx, double time) {
      int lx = this.x0 + 68;
      ctx.drawItem(new ItemStack(this.section.icon()), lx, this.y0 + 10);
      Text title = Text.literal(this.section.label).formatted(Formatting.BOLD);
      int tx = lx + 22 + this.textRenderer.getWidth(title);
      ctx.drawText(this.textRenderer, title, lx + 22, this.y0 + 10, -1, false);
      ctx.drawText(this.textRenderer, this.group.isEffects() ? "Changes apply instantly" : "Pick a look, then press Apply", lx + 22, this.y0 + 20, -4935484, false);
      Text right = this.status;
      if (right.getString().isEmpty()) {
         int n = this.unsavedChanges();
         right = n == 0 ? Text.literal("✔ Everything applied").setStyle(Style.EMPTY.withColor(7332768)) : Text.literal(n + " change" + (n == 1 ? "" : "s") + " not applied yet").setStyle(Style.EMPTY.withColor(16762967));
      }

      int maxW = this.x1 - 8 - (tx + 12);
      OrderedText fitted = Language.getInstance().reorder(this.textRenderer.trimToWidth(right, Math.max(20, maxW)));
      ctx.drawText(this.textRenderer, fitted, this.x1 - 8 - this.textRenderer.getWidth(fitted), this.y0 + 9, -1, false);
      ctx.fill(this.x0 + 6, this.y0 + 32, this.x1 - 6, this.y0 + 33, 1358954495);
   }

   private void renderSidebar(DrawContext ctx, int mx, int my, double time) {
      for(Btn b : this.buttons) {
         if (b.kind == PackEditorScreen.Kind.TAB) {
            boolean sel = b.group == this.group;
            boolean hover = b.contains((double)mx, (double)my);
            if (sel) {
               round(ctx, b.x, b.y, b.w, b.h, 1090519039);
               outline(ctx, b.x, b.y, b.w, b.h, 1895825407);
            } else if (hover) {
               round(ctx, b.x, b.y, b.w, b.h, 553648127);
            }

            int iy = b.y + (b.h - 16) / 2;
            if (b.h >= 17) {
               ctx.drawItem(new ItemStack(b.group.icon()), b.x + 5, iy);
            } else {
               float s = (float)(b.h - 1) / 16.0F;
               ctx.getMatrices().pushMatrix();
               ctx.getMatrices().translate((float)(b.x + 5) + (16.0F - 16.0F * s) / 2.0F, (float)b.y + ((float)b.h - 16.0F * s) / 2.0F);
               ctx.getMatrices().scale(s, s);
               ctx.drawItem(new ItemStack(b.group.icon()), 0, 0);
               ctx.getMatrices().popMatrix();
               iy = b.y;
            }

            int color = sel ? -1 : (hover ? -1646350 : -1645585);
            ctx.drawText(this.textRenderer, b.label, b.x + 25, b.y + (b.h - 8) / 2, color, false);
            if (this.groupChanged(b.group)) {
               ctx.fill(b.x + 18, iy - 1, b.x + 22, iy + 3, -15922667);
               ctx.fill(b.x + 19, iy, b.x + 21, iy + 2, accent(time * 0.2 + 0.3));
            }
         }
      }

   }

   private void renderPieces(DrawContext ctx, int mx, int my, double time) {
      for(Btn b : this.buttons) {
         if (b.kind == PackEditorScreen.Kind.PIECE) {
            boolean sel = b.piece == this.current;
            boolean hover = b.contains((double)mx, (double)my);
            if (sel) {
               ctx.fillGradient(b.x + 1, b.y, b.x + b.w - 1, b.y + b.h, withAlpha(accent(time * 0.15), 112), withAlpha(accent(time * 0.15 + 0.3), 112));
               outline(ctx, b.x, b.y, b.w, b.h, accent(time * 0.15));
            } else {
               round(ctx, b.x, b.y, b.w, b.h, hover ? 1358954495 : 822083583);
               outline(ctx, b.x, b.y, b.w, b.h, hover ? withAlpha(-5018625, 170) : 1358954495);
            }

            Item item = (Item)Registries.ITEM.get(Identifier.ofVanilla(b.piece.id));
            ctx.drawItem(new ItemStack(item), b.x + 3, b.y + 1);
            ctx.drawText(this.textRenderer, b.label, b.x + 21, b.y + 5, sel ? -1 : -1645585, sel);
            if (this.customized(b.piece)) {
               ctx.fill(b.x + b.w - 6, b.y + 8, b.x + b.w - 3, b.y + 11, -37931);
            }

            if (hover && b.label.isEmpty()) {
               ctx.drawTooltip(this.textRenderer, Text.literal(b.piece.displayName), mx, my);
            }
         }
      }

   }

   private void renderHeadline(DrawContext ctx, double time) {
      Variant selected = VariantRegistry.find(this.currentList(), (String)this.pending.getOrDefault(this.current, "vanilla"));
      String piece = this.current.material() == null ? this.current.displayName : (this.diamond ? "Diamond " : "Netherite ") + this.current.displayName;
      String name = this.group.categories().size() > 1 ? this.group.displayName + " › " + piece : this.group.displayName;
      int room = this.buttons.stream().filter((b) -> (b.kind == PackEditorScreen.Kind.TOGGLE || b.kind == PackEditorScreen.Kind.PENCIL || b.kind == PackEditorScreen.Kind.MATERIAL) && b.y <= this.headlineY + 8 && b.y + b.h >= this.headlineY).mapToInt(Btn::x).min().orElse(this.x1 - 8) - 6 - this.gridX;
      Text head = null;

      for(String sep : new String[]{"   ·  selected: ", " · ", null}) {
         Text t = Text.literal(name).formatted(new Formatting[]{Formatting.WHITE, Formatting.BOLD});
         if (sep != null) {
            t = ((MutableText)t).append(Text.literal(sep).setStyle(Style.EMPTY.withBold(false).withColor(6972288))).append(Text.literal(selected != null ? selected.name : "Vanilla").setStyle(Style.EMPTY.withBold(false).withColor(accent(time * 0.15) & 16777215)));
         }

         head = t;
         if (this.textRenderer.getWidth(t) <= room) {
            break;
         }
      }

      ctx.drawText(this.textRenderer, Language.getInstance().reorder(this.textRenderer.trimToWidth(head, Math.max(20, room))), this.gridX, this.headlineY, -1, true);
   }

   private int editColor() {
      return this.editingDisabled ? this.disabledShieldColor : this.shieldColor;
   }

   private void setEditColor(int c) {
      if (this.editingDisabled) {
         this.disabledShieldColor = c;
      } else {
         this.shieldColor = c;
      }

   }

   private int[][] shieldTabs() {
      int a = this.textRenderer.getWidth("Shield Colour") + 24;
      int b = this.textRenderer.getWidth(this.disabledLabel()) + 24;
      return new int[][]{{this.pkX + 4, a}, {this.pkX + 4 + a + 3, b}};
   }

   private String disabledLabel() {
      int room = this.pkW - 8 - (this.textRenderer.getWidth("Shield Colour") + 24) - 3;
      return this.textRenderer.getWidth("Disabled Shield Colour") + 24 <= room ? "Disabled Shield Colour" : "Disabled";
   }

   private void renderShieldColor(DrawContext ctx, int mx, int my, double time) {
      round(ctx, this.pkX, this.pkY, this.pkW, this.pkH, -15593702);
      outline(ctx, this.pkX, this.pkY, this.pkW, this.pkH, 1358954495);
      int[][] tabs = this.shieldTabs();
      String[] labels = new String[]{"Shield Colour", this.disabledLabel()};
      int[] colors = new int[]{this.shieldColor < 0 ? 8743230 : this.shieldColor, this.disabledShieldColor};

      for(int i = 0; i < 2; ++i) {
         int tx = tabs[i][0];
         int tw = tabs[i][1];
         boolean sel = i == 1 == this.editingDisabled;
         boolean hov = mx >= tx && mx < tx + tw && my >= this.pkY + 3 && my < this.pkY + 18;
         if (sel) {
            ctx.fillGradient(tx + 1, this.pkY + 3, tx + tw - 1, this.pkY + 18, withAlpha(accent(time * 0.15), 144), withAlpha(accent(time * 0.15 + 0.3), 96));
            outline(ctx, tx, this.pkY + 3, tw, 15, accent(time * 0.15));
         } else {
            round(ctx, tx, this.pkY + 3, tw, 15, hov ? 1358954495 : 822083583);
            outline(ctx, tx, this.pkY + 3, tw, 15, hov ? withAlpha(-5018625, 170) : 1358954495);
         }

         if (colors[i] >= 0) {
            round(ctx, tx + 4, this.pkY + 6, 11, 9, -16777216 | colors[i]);
         } else {
            ctx.fill(tx + 4, this.pkY + 6, tx + 15, this.pkY + 15, -14015434);
            ctx.fill(tx + 5, this.pkY + 10, tx + 14, this.pkY + 11, -7502429);
         }

         outline(ctx, tx + 4, this.pkY + 6, 11, 9, -1056964609);
         ctx.drawText(this.textRenderer, labels[i], tx + 19, this.pkY + 7, sel ? -1 : -1645585, sel);
         if (hov && i == 1) {
            ctx.drawTooltip(this.textRenderer, List.of(Text.literal("Disabled Shield Colour").formatted(Formatting.BOLD), Text.literal("Shields turn this colour while disabled (hit by an axe),").formatted(Formatting.GRAY), Text.literal("yours and your opponents', then change back").formatted(Formatting.GRAY)), mx, my);
         }
      }

      int current = this.editColor();
      boolean defHover = mx >= this.defX && mx < this.defX + 40 && my >= this.defY && my < this.defY + 13;
      round(ctx, this.defX, this.defY, 40, 13, current < 0 ? withAlpha(-5018625, 112) : (defHover ? 1358954495 : 822083583));
      outline(ctx, this.defX, this.defY, 40, 13, defHover ? accent(time * 0.2) : 1358954495);
      ctx.drawText(this.textRenderer, "Default", this.defX + (40 - this.textRenderer.getWidth("Default")) / 2, this.defY + 3, -1, false);
      if (defHover) {
         ctx.drawTooltip(this.textRenderer, Text.literal(this.editingDisabled ? "No colour change when disabled" : "Normal shield colour"), mx, my);
      }

      for(int i = 0; i < this.svW; ++i) {
         int top = -16777216 | hsv(this.hue, (float)i / (float)(this.svW - 1), 1.0F);
         ctx.fillGradient(this.svX + i, this.svY, this.svX + i + 1, this.svY + this.svH, top, -16777216);
      }

      outline(ctx, this.svX - 1, this.svY - 1, this.svW + 2, this.svH + 2, 1358954495);
      if (current >= 0) {
         int cx = this.svX + Math.round(this.sat * (float)(this.svW - 1));
         int cy = this.svY + Math.round((1.0F - this.val) * (float)(this.svH - 1));
         outline(ctx, cx - 3, cy - 3, 7, 7, -16777216);
         outline(ctx, cx - 2, cy - 2, 5, 5, -1);
      }

      for(int i = 0; i < this.svW; ++i) {
         ctx.fill(this.svX + i, this.hueY, this.svX + i + 1, this.hueY + 6, -16777216 | hsv((float)i / (float)(this.svW - 1), 1.0F, 1.0F));
      }

      int hxk = this.svX + Math.round(this.hue * (float)(this.svW - 1));
      round(ctx, hxk - 2, this.hueY - 2, 5, 10, -1);
      ctx.fill(hxk - 1, this.hueY - 1, hxk + 2, this.hueY + 7, -16777216 | hsv(this.hue, 1.0F, 1.0F));

      for(int i = 0; i < PRESETS.length; ++i) {
         int px = this.svX + i * 15;
         if (px + 12 > this.defX - 4) {
            break;
         }

         boolean sel = PRESETS[i] == current;
         boolean hov = mx >= px && mx < px + 12 && my >= this.presetY && my < this.presetY + 12;
         round(ctx, px, this.presetY, 12, 12, -16777216 | PRESETS[i]);
         outline(ctx, px, this.presetY, 12, 12, sel ? -1 : (hov ? accent(time * 0.2) : 1627389951));
      }

   }

   private boolean pickerClick(double mx, double my) {
      if (this.group != Group.SHIELDS) {
         return false;
      } else {
         int[][] tabs = this.shieldTabs();

         for(int i = 0; i < 2; ++i) {
            if (mx >= (double)tabs[i][0] && mx < (double)(tabs[i][0] + tabs[i][1]) && my >= (double)(this.pkY + 3) && my < (double)(this.pkY + 18)) {
               this.editingDisabled = i == 1;
               if (this.editColor() >= 0) {
                  this.setHsvFrom(this.editColor());
               }

               return true;
            }
         }

         if (mx >= (double)this.defX && mx < (double)(this.defX + 40) && my >= (double)this.defY && my < (double)(this.defY + 13)) {
            this.setEditColor(-1);
            return true;
         } else if (mx >= (double)this.svX && mx < (double)(this.svX + this.svW) && my >= (double)this.svY && my < (double)(this.svY + this.svH)) {
            this.drag = 1;
            this.pickerDrag(mx, my);
            return true;
         } else if (mx >= (double)this.svX && mx < (double)(this.svX + this.svW) && my >= (double)(this.hueY - 2) && my < (double)(this.hueY + 8)) {
            this.drag = 2;
            this.pickerDrag(mx, my);
            return true;
         } else {
            for(int i = 0; i < PRESETS.length; ++i) {
               int px = this.svX + i * 15;
               if (px + 12 > this.defX - 4) {
                  break;
               }

               if (mx >= (double)px && mx < (double)(px + 12) && my >= (double)this.presetY && my < (double)(this.presetY + 12)) {
                  this.setEditColor(PRESETS[i]);
                  this.setHsvFrom(PRESETS[i]);
                  return true;
               }
            }

            return false;
         }
      }
   }

   private void pickerDrag(double mx, double my) {
      if (this.drag == 1) {
         this.sat = (float)Math.max((double)0.0F, Math.min((double)1.0F, (mx - (double)this.svX) / (double)(this.svW - 1)));
         this.val = (float)Math.max((double)0.0F, Math.min((double)1.0F, (double)1.0F - (my - (double)this.svY) / (double)(this.svH - 1)));
      } else {
         if (this.drag != 2) {
            return;
         }

         this.hue = (float)Math.max((double)0.0F, Math.min((double)1.0F, (mx - (double)this.svX) / (double)(this.svW - 1)));
      }

      this.setEditColor(hsv(this.hue, this.sat, this.val));
   }

   public boolean mouseDragged(Click click, double dx, double dy) {
      if (this.colourPopup != null && this.colourPopup.dragging()) {
         this.colourPopup.move(click.x(), click.y());
         return true;
      } else if (this.drag == 3) {
         this.cobwebSlide(click.x());
         return true;
      } else if (this.drag != 0) {
         this.pickerDrag(click.x(), click.y());
         return true;
      } else {
         return this.group.isEffects() && this.effects.drag(click.x(), click.y()) ? true : super.mouseDragged(click, dx, dy);
      }
   }

   public boolean mouseReleased(Click click) {
      this.drag = 0;
      this.effects.release();
      if (this.colourPopup != null && this.colourPopup.dragging()) {
         this.colourPopup.release();
         String now = (String)this.pending.getOrDefault(this.current, "");
         Variant.RecolourVariant rv = now.startsWith("recolour:") ? Variant.RecolourVariant.of(now) : null;
         if (rv != null) {
            this.customPreviews.put(this.current, rv.color);
         }
      }

      return super.mouseReleased(click);
   }

   private void cobwebSlide(double mx) {
      int v = (int)Math.round(Math.max((double)0.0F, Math.min((double)1.0F, (mx - (double)this.cwX) / (double)this.cwW)) * (double)100.0F);
      this.cobwebOpacity = v >= 97 ? 100 : v;
   }

   private static int brighten(int rgb) {
      float k = 1.275F;
      int r = Math.min(255, Math.round((float)(rgb >> 16 & 255) * k));
      int g = Math.min(255, Math.round((float)(rgb >> 8 & 255) * k));
      int b = Math.min(255, Math.round((float)(rgb & 255) * k));
      return r << 16 | g << 8 | b;
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

   private void setHsvFrom(int rgb) {
      float r = (float)(rgb >> 16 & 255) / 255.0F;
      float g = (float)(rgb >> 8 & 255) / 255.0F;
      float b = (float)(rgb & 255) / 255.0F;
      float max = Math.max(r, Math.max(g, b));
      float min = Math.min(r, Math.min(g, b));
      float d = max - min;
      float h;
      if (d == 0.0F) {
         h = this.hue;
      } else if (max == r) {
         h = (g - b) / d % 6.0F / 6.0F;
      } else if (max == g) {
         h = ((b - r) / d + 2.0F) / 6.0F;
      } else {
         h = ((r - g) / d + 4.0F) / 6.0F;
      }

      this.hue = h < 0.0F ? h + 1.0F : h;
      this.sat = max == 0.0F ? 0.0F : d / max;
      this.val = max;
   }

   private void renderToggles(DrawContext ctx, int mx, int my, double time) {
      for(Btn b : this.buttons) {
         if (b.kind == PackEditorScreen.Kind.PENCIL) {
            boolean hover = b.contains((double)mx, (double)my);
            if (hover) {
               ctx.fillGradient(b.x, b.y, b.x + b.w, b.y + b.h, withAlpha(accent(time * 0.3), 192), withAlpha(accent(time * 0.3 + 0.3), 192));
            } else {
               round(ctx, b.x, b.y, b.w, b.h, 822083583);
            }

            outline(ctx, b.x, b.y, b.w, b.h, hover ? -1 : withAlpha(accent(time * 0.2), 192));
            drawPencil(ctx, b.x + 4, b.y + 2, hover ? -1 : -14249);
            if (hover) {
               ctx.drawTooltip(this.textRenderer, List.of(Text.literal("Draw your own").formatted(Formatting.LIGHT_PURPLE), Text.literal("or drop a screenshot on this window to copy its colours").formatted(Formatting.GRAY)), mx, my);
            }
         }
      }

      for(Btn b : this.buttons) {
         if (b.kind == PackEditorScreen.Kind.MATERIAL) {
            boolean isDiamond = b.label.equals("Diamond");
            boolean sel = isDiamond == this.diamond;
            boolean hover = b.contains((double)mx, (double)my);
            int base = isDiamond ? -12590888 : -9741714;
            if (sel) {
               ctx.fillGradient(b.x, b.y, b.x + b.w, b.y + b.h, withAlpha(base, 192), withAlpha(base, 96));
               outline(ctx, b.x, b.y, b.w, b.h, isDiamond ? -6291464 : -4610116);
            } else {
               round(ctx, b.x, b.y, b.w, b.h, hover ? 1358954495 : 822083583);
               outline(ctx, b.x, b.y, b.w, b.h, hover ? withAlpha(base, 255) : 1358954495);
            }

            ItemStack icon = new ItemStack(isDiamond ? Items.DIAMOND : Items.NETHERITE_INGOT);
            ctx.drawItem(icon, b.x + 3, b.y + 1);
            ctx.drawText(this.textRenderer, b.label, b.x + 21, b.y + 5, sel ? -1 : -1645585, sel);
         }
      }

      for(Btn b : this.buttons) {
         if (b.kind == PackEditorScreen.Kind.TOGGLE) {
            boolean on = this.shownOn(this.toggleFor(b));
            boolean hover = b.contains((double)mx, (double)my);
            ctx.drawText(this.textRenderer, b.label, b.x, b.y + 3, hover ? -1 : -1645585, true);
            int sx = b.x + b.w - 30;
            int sy = b.y + 1;
            if (on) {
               ctx.fillGradient(sx + 1, sy, sx + 29, sy + 12, accent(time * 0.2), accent(time * 0.2 + 0.3));
               ctx.fillGradient(sx, sy + 1, sx + 30, sy + 11, accent(time * 0.2), accent(time * 0.2 + 0.3));
            } else {
               round(ctx, sx, sy, 30, 12, -14015434);
            }

            outline(ctx, sx, sy, 30, 12, hover ? -1 : (on ? withAlpha(16777215, 128) : 1358954495));
            int knob = on ? sx + 19 : sx + 2;
            round(ctx, knob, sy + 2, 9, 8, -1);
            ctx.drawText(this.textRenderer, on ? "ON" : "OFF", on ? sx + 4 : sx + 12, sy + 2, on ? -1 : -4935484, false);
         }
      }

   }

   private Variant renderGrid(DrawContext ctx, int mouseX, int mouseY, double time) {
      String selectedId = (String)this.pending.getOrDefault(this.current, "vanilla");
      String appliedId = PackEditorConfig.get(this.current);
      Variant hovered = null;
      List<Variant> list = this.currentList();
      ctx.enableScissor(this.gridX - 2, this.gridY - 2, this.gridX + this.gridW + 2, this.gridY + this.gridH + 2);

      for(int i = 0; i < list.size(); ++i) {
         Variant v = (Variant)list.get(i);
         int x = this.gridX + i % this.cols * 63;
         int y = this.gridY + i / this.cols * 77 - (int)this.scroll;
         if (y + 72 >= this.gridY - 2 && y <= this.gridY + this.gridH + 2) {
            boolean custom = v instanceof Variant.RecolourVariant;
            boolean isSel = custom ? selectedId.startsWith("recolour:") : v.id.equals(selectedId);
            boolean hover = mouseX >= x && mouseX < x + 58 && mouseY >= y && mouseY < y + 72 && mouseY >= this.gridY && mouseY <= this.gridY + this.gridH;
            if (hover) {
               hovered = v;
               --y;
            }

            round(ctx, x, y, 58, 72, hover ? 1358954495 : 822083583);
            if (isSel) {
               int glow = accent(time * (double)0.25F + (double)i * 0.04);
               outline(ctx, x - 1, y - 1, 60, 74, withAlpha(glow, 128));
               outline(ctx, x, y, 58, 72, glow);
            } else {
               outline(ctx, x, y, 58, 72, hover ? withAlpha(-5018625, 176) : 1358954495);
            }

            int px = x + 9;
            int py = y + 6;
            round(ctx, px - 2, py - 2, 44, 44, 1342177280);
            Preview p = v instanceof Variant.SizeVariant ? null : this.preview((Variant)(custom ? new Variant.RecolourVariant(this.customCardColour()) : v));
            if (v instanceof Variant.SizeVariant) {
               Variant.SizeVariant size = (Variant.SizeVariant)v;
               float s = 2.5F * size.previewScale;
               ctx.getMatrices().pushMatrix();
               ctx.getMatrices().translate((float)px + 20.0F - 8.0F * s, (float)(py + 40) - 16.0F * s - 2.0F);
               ctx.getMatrices().scale(s, s);
               ctx.drawItem(new ItemStack(Items.TOTEM_OF_UNDYING), 0, 0);
               ctx.getMatrices().popMatrix();
            } else if (p == LOADING) {
               for(int d = 0; d < 3; ++d) {
                  boolean lit = (int)(time * (double)3.0F) % 3 == d;
                  ctx.fill(px + 20 - 7 + d * 6, py + 20 - 1, px + 20 - 5 + d * 6, py + 20 + 1, lit ? -1056964609 : 1358954495);
               }
            } else if (p != null) {
               float scale = Math.min(40.0F / (float)p.w, 40.0F / (float)p.h);
               int dw = Math.max(1, Math.round((float)p.w * scale));
               int dh = Math.max(1, Math.round((float)p.h * scale));
               int tint = -1;
               if (this.current == Category.COBWEB) {
                  tint = Math.max(32, this.cobwebOpacity * 255 / 100) << 24 | 16777215;
               }

               if (this.current == Category.SHIELD) {
                  if (this.shieldColor >= 0) {
                     tint = -16777216 | brighten(this.shieldColor);
                  } else if (v instanceof Variant.PackVariant) {
                     tint = -16777216 | brighten(8743230);
                  }
               }

               ctx.drawTexture(RenderPipelines.GUI_TEXTURED, p.id, px + (40 - dw) / 2, py + (40 - dh) / 2, 0.0F, 0.0F, dw, dh, p.w, p.h, p.w, p.h, tint);
            } else {
               ctx.drawCenteredTextWithShadow(this.textRenderer, "?", px + 20, py + 20 - 4, -4935484);
            }

            boolean isSize = v instanceof Variant.SizeVariant;
            if (isSize) {
               String size = v.name.substring(0, v.name.lastIndexOf(32));
               ctx.drawCenteredTextWithShadow(this.textRenderer, size, x + 29, y + 50, -1);
               ctx.drawCenteredTextWithShadow(this.textRenderer, "Totem", x + 29, y + 61, -37931);
               if (v.id.equals(appliedId)) {
                  round(ctx, x + 58 - 12, y + 2, 10, 10, -14505888);
                  ctx.drawText(this.textRenderer, "✔", x + 58 - 10, y + 3, -1, false);
               }
            } else {
               boolean var10000;
               label162: {
                  ctx.drawCenteredTextWithShadow(this.textRenderer, this.textRenderer.trimToWidth(v.name, 54), x + 29, y + 50, -1);
                  if (v instanceof Variant.PackVariant) {
                     Variant.PackVariant pv = (Variant.PackVariant)v;
                     if (pv.pack.userAdded) {
                        var10000 = true;
                        break label162;
                     }
                  }

                  var10000 = false;
               }

               boolean yours = var10000;
               boolean drawn = v instanceof Variant.DrawnVariant;
               boolean preset = v instanceof CrosshairPresets.PresetVariant;
               String tag = isSize ? "SIZE" : (drawn ? "DRAWN" : (yours ? "YOURS" : (preset ? "PRESET" : (custom ? "PICK" : (v.isVanilla() ? "VANILLA" : (v.id.startsWith("pack:") ? "PACK" : "STYLE"))))));
               int tagColor = drawn ? -24765 : (preset ? -4875009 : (!isSize && !yours ? (custom ? -16777216 | Math.max(4210752, this.customCardColour()) : (v.isVanilla() ? -4869950 : (v.id.startsWith("pack:") ? -11541505 : -14249))) : -37931));
               int tw = this.textRenderer.getWidth(tag) + 6;
               round(ctx, x + (58 - tw) / 2, y + 60, tw, 10, withAlpha(tagColor, 48));
               ctx.drawText(this.textRenderer, tag, x + (58 - tw) / 2 + 3, y + 61, tagColor, false);
               if (custom) {
                  if (!appliedId.startsWith("recolour:")) {
                     continue;
                  }
               } else if (!v.id.equals(appliedId)) {
                  continue;
               }

               round(ctx, x + 58 - 12, y + 2, 10, 10, -14505888);
               ctx.drawText(this.textRenderer, "✔", x + 58 - 10, y + 3, -1, false);
            }
         }
      }

      ctx.disableScissor();
      int content = this.contentHeight();
      if (content > this.gridH) {
         int barH = Math.max(14, this.gridH * this.gridH / content);
         int barY = this.gridY + (int)((double)(this.gridH - barH) * (this.scroll / (double)(content - this.gridH)));
         round(ctx, this.gridX + this.gridW + 3, this.gridY, 3, this.gridH, 822083583);
         round(ctx, this.gridX + this.gridW + 3, barY, 3, barH, accent(time * 0.2));
      }

      return hovered;
   }

   private void renderFooter(DrawContext ctx, int mx, int my, double time) {
      ctx.fill(this.x0 + 6, this.y1 - 28, this.x1 - 6, this.y1 - 27, 1358954495);
      String version = (String)FabricLoader.getInstance().getModContainer("awerialspackeditor").map((m) -> m.getMetadata().getVersion().getFriendlyString()).orElse("");
      Text credit = Text.literal("by ").formatted(Formatting.DARK_GRAY).append(Text.literal("Awerial").setStyle(Style.EMPTY.withColor(10320895))).append(Text.literal("  v" + version).formatted(Formatting.DARK_GRAY));
      int firstButton = this.buttons.stream().filter((bx) -> bx.y == this.y1 - 23).mapToInt(Btn::x).min().orElse(this.x1);
      if (this.x0 + 10 + this.textRenderer.getWidth(credit) + 6 <= firstButton) {
         ctx.drawText(this.textRenderer, credit, this.x0 + 10, this.y1 - 17, -1, false);
      }

      int unsaved = this.unsavedChanges();

      for(Btn b : this.buttons) {
         if (b.kind == PackEditorScreen.Kind.BUTTON || b.kind == PackEditorScreen.Kind.PRIMARY) {
            boolean hover = b.contains((double)mx, (double)my);
            if (b.kind == PackEditorScreen.Kind.PRIMARY) {
               round(ctx, b.x, b.y, b.w, b.h, hover ? -4875009 : -6456321);
               String label = b.label.equals("✔ Apply") && unsaved > 0 ? "✔ Apply (" + unsaved + ")" : b.label;
               Text t = Text.literal(label).formatted(Formatting.BOLD);
               ctx.drawText(this.textRenderer, t, b.x + (b.w - this.textRenderer.getWidth(t)) / 2, b.y + (b.h - 8) / 2 + 1, -1, false);
            } else {
               round(ctx, b.x, b.y, b.w, b.h, hover ? 1358954495 : 822083583);
               outline(ctx, b.x, b.y, b.w, b.h, hover ? accent(time * 0.2) : 1358954495);
               ctx.drawText(this.textRenderer, b.label, b.x + (b.w - this.textRenderer.getWidth(b.label)) / 2, b.y + (b.h - 8) / 2 + 1, hover ? -1 : -1645585, false);
            }
         }
      }

   }

   public boolean mouseClicked(Click click, boolean doubled) {
      if (click.button() != 0) {
         return super.mouseClicked(click, doubled);
      } else {
         double mx = click.x();
         double my = click.y();
         if (this.colourPopup != null) {
            if (this.colourPopup.click(mx, my)) {
               return true;
            }

            this.colourPopup = null;
         }

         if (this.group == Group.COBWEB && mx >= (double)(this.cwX - 3) && mx <= (double)(this.cwX + this.cwW + 3) && my >= (double)(this.barY + 2) && my < (double)(this.barY + 18)) {
            this.drag = 3;
            this.cobwebSlide(mx);
            return true;
         } else if (this.group.isEffects() && this.effects.click(mx, my)) {
            this.playClick();
            return true;
         } else if (this.pickerClick(mx, my)) {
            if (this.drag == 0) {
               this.playClick();
            }

            return true;
         } else {
            for(Btn b : this.buttons) {
               if (b.contains(mx, my)) {
                  this.playClick();
                  b.action.run();
                  if (this.client.currentScreen == this) {
                     this.clearAndInit();
                  }

                  return true;
               }
            }

            if (this.group == Group.ITEMS) {
               ItemTextures.Entry e = this.itemAt(mx, my);
               if (e != null) {
                  this.playClick();
                  this.openItemDraw(e);
                  return true;
               }
            }

            if (!this.group.isPanel() && mx >= (double)this.gridX && mx < (double)(this.gridX + this.gridW) && my >= (double)this.gridY && my <= (double)(this.gridY + this.gridH)) {
               int col = (int)((mx - (double)this.gridX) / (double)63.0F);
               int row = (int)((my - (double)this.gridY + this.scroll) / (double)77.0F);
               int inX = (int)((mx - (double)this.gridX) % (double)63.0F);
               int inY = (int)((my - (double)this.gridY + this.scroll) % (double)77.0F);
               int idx = row * this.cols + col;
               List<Variant> list = this.currentList();
               if (col < this.cols && inX < 58 && inY < 72 && idx < list.size()) {
                  Variant picked = (Variant)list.get(idx);
                  if (!(picked instanceof Variant.RecolourVariant)) {
                     this.pending.put(this.current, picked.id);
                     this.status = Text.empty();
                     this.playClick();
                     return true;
                  }

                  String now = (String)this.pending.getOrDefault(this.current, "");
                  int c = now.startsWith("recolour:") && Variant.RecolourVariant.of(now) != null ? Variant.RecolourVariant.of(now).color : this.customPreviewFor(this.current);
                  this.pending.put(this.current, (new Variant.RecolourVariant(c)).id);
                  this.customPreviews.put(this.current, c);
                  int cardX = this.gridX + col * 63;
                  int cardY = this.gridY + row * 77 - (int)this.scroll;
                  Category forCat = this.current;
                  this.colourPopup = new ColourPopup("Custom colour", c, cardX + 58 + 4, cardY, this.gridX, this.x1 - 8, this.gridY, this.y1 - 30, (chosen) -> {
                     if (this.current == forCat) {
                        this.pending.put(forCat, (new Variant.RecolourVariant(chosen)).id);
                     }

                     if (!this.colourPopup.dragging()) {
                        this.customPreviews.put(forCat, chosen);
                     }

                  });
                  this.popupCategory = this.current;
                  this.status = Text.empty();
                  this.playClick();
                  return true;
               }
            }

            return super.mouseClicked(click, doubled);
         }
      }
   }

   private void playClick() {
      this.client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      if (this.group == Group.CONFIGS) {
         this.profileScroll = Math.max(0, this.profileScroll - (int)Math.signum(vertical));
         this.clearAndInit();
         return true;
      } else if (this.group == Group.MY_PACKS) {
         this.packScroll = Math.max(0, this.packScroll - (int)Math.signum(vertical));
         this.clearAndInit();
         return true;
      } else if (this.group == Group.ITEMS) {
         this.itScroll -= (int)Math.signum(vertical) * 2;
         this.clampItemScroll();
         return true;
      } else {
         this.scroll -= vertical * (double)24.0F;
         this.clampScroll();
         return true;
      }
   }

   static int accent(double phase) {
      return -6456321;
   }

   private static int lerp(int a, int b, float f) {
      int r = (int)((float)(a >> 16 & 255) + (float)((b >> 16 & 255) - (a >> 16 & 255)) * f);
      int g = (int)((float)(a >> 8 & 255) + (float)((b >> 8 & 255) - (a >> 8 & 255)) * f);
      int bl = (int)((float)(a & 255) + (float)((b & 255) - (a & 255)) * f);
      return -16777216 | r << 16 | g << 8 | bl;
   }

   static int withAlpha(int color, int alpha) {
      return alpha << 24 | color & 16777215;
   }

   private static double frac(double d) {
      return d - Math.floor(d);
   }

   static void round(DrawContext c, int x, int y, int w, int h, int color) {
      if (w >= 10 && h >= 10) {
         c.fill(x + 2, y, x + w - 2, y + 1, color);
         c.fill(x + 1, y + 1, x + w - 1, y + 2, color);
         c.fill(x, y + 2, x + w, y + h - 2, color);
         c.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, color);
         c.fill(x + 2, y + h - 1, x + w - 2, y + h, color);
      } else {
         c.fill(x + 1, y, x + w - 1, y + h, color);
         c.fill(x, y + 1, x + 1, y + h - 1, color);
         c.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
      }
   }

   static void outline(DrawContext c, int x, int y, int w, int h, int color) {
      if (w >= 10 && h >= 10) {
         c.fill(x + 2, y, x + w - 2, y + 1, color);
         c.fill(x + 2, y + h - 1, x + w - 2, y + h, color);
         c.fill(x, y + 2, x + 1, y + h - 2, color);
         c.fill(x + w - 1, y + 2, x + w, y + h - 2, color);
         c.fill(x + 1, y + 1, x + 2, y + 2, color);
         c.fill(x + w - 2, y + 1, x + w - 1, y + 2, color);
         c.fill(x + 1, y + h - 2, x + 2, y + h - 1, color);
         c.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, color);
      } else {
         c.fill(x + 1, y, x + w - 1, y + 1, color);
         c.fill(x + 1, y + h - 1, x + w - 1, y + h, color);
         c.fill(x, y + 1, x + 1, y + h - 1, color);
         c.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
      }
   }

   public boolean keyPressed(KeyInput input) {
      if (this.group != null && this.group.isEffects() && this.section != null && this.effects.keyPressed(input)) {
         return true;
      } else if (input.isEscape() && this.section != null) {
         this.setFocused((Element)null);
         this.section = null;
         this.clearAndInit();
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   private static void gradientBar(DrawContext c, int x, int y, int w, int h, double phase) {
      for(int i = 0; i < w; i += 2) {
         c.fill(x + i, y, Math.min(x + w, x + i + 2), y + h, accent(phase + (double)i / (double)w));
      }

   }

   private Preview preview(Variant v) {
      int gen = VariantRegistry.generation();
      if (gen != previewGeneration) {
         this.clearPreviews();
         previewGeneration = gen;
      }

      Category cat = this.current;
      boolean low = cat == Category.FIRE && this.isOn(Toggle.LOW_FIRE);
      boolean legacy = cat == Category.GLINT && this.isOn(Toggle.LEGACY_GLINT) && Variant.legacyGlintAvailable();
      String key = cat.id + "|" + v.id + (low ? "|low" : "") + (legacy ? "|legacy" : "");
      Preview done = (Preview)PREVIEWS.get(key);
      if (done == null && !PREVIEWS.containsKey(key)) {
         NativeImage img = null;
         if (cat == Category.GLINT) {
            Variant.legacyGlint = legacy;
            img = makePreview(cat, v, false);
         } else {
            Future<NativeImage> f = (Future)MAKING.get(key);
            if (f == null) {
               MAKING.put(key, PREVIEW_THREAD.submit(() -> makePreview(cat, v, low)));
               return LOADING;
            }

            if (!f.isDone()) {
               return LOADING;
            }

            MAKING.remove(key);

            try {
               img = (NativeImage)f.get();
            } catch (Exception e) {
               PackEditorClient.LOGGER.warn("Preview failed for {}: {}", v.name, e.toString());
            }
         }

         Preview result = null;
         if (img != null) {
            int var10001 = textureCounter++;
            Identifier id = Identifier.of("awerialspackeditor", "preview/" + var10001);
            int w = img.getWidth();
            int h = img.getHeight();
            this.client.getTextureManager().registerTexture(id, new NativeImageBackedTexture(() -> "awerialspackeditor preview", img));
            result = new Preview(id, w, h);
         }

         PREVIEWS.put(key, result);
         return result;
      } else {
         return done;
      }
   }

   private static NativeImage makePreview(Category cat, Variant v, boolean low) {
      try {
         NativeImage img = null;
         if (cat == Category.SKY) {
            img = SkyTint.preview(SkyTint.opFor(v));
         }

         for(Category.Target t : cat == Category.GLINT ? cat.targets.reversed() : cat.targets) {
            img = v.loadImage(t);
            if (img != null) {
               break;
            }
         }

         if (img == null) {
            return null;
         } else {
            img = cat == Category.END_CRYSTAL ? PreviewImages.crystalIcon(img) : (cat == Category.SHIELD ? PreviewImages.shieldFront(img) : PreviewImages.firstFrame(img));
            if (low) {
               NativeImage squashed = PackBuilder.lowFire(img);
               img.close();
               img = squashed;
            }

            return img;
         }
      } catch (Exception e) {
         PackEditorClient.LOGGER.warn("Preview failed for {}: {}", v.name, e.toString());
         return null;
      }
   }

   private void clearPreviews() {
      for(Preview p : PREVIEWS.values()) {
         if (p != null) {
            this.client.getTextureManager().destroyTexture(p.id);
         }
      }

      PREVIEWS.clear();
      MAKING.values().forEach((f) -> f.cancel(false));
      MAKING.clear();
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   private static record Preview(Identifier id, int w, int h) {
   }

   private static enum Kind {
      TAB,
      PIECE,
      BUTTON,
      PRIMARY,
      TOGGLE,
      MATERIAL,
      PENCIL,
      SECTION,
      SMALL,
      TOTEM_STOP,
      CROSSHAIR_SIZE;

   }

   private static record Btn(Kind kind, int x, int y, int w, int h, String label, Group group, Category piece, Runnable action) {
      boolean contains(double mx, double my) {
         return mx >= (double)this.x && mx < (double)(this.x + this.w) && my >= (double)this.y && my < (double)(this.y + this.h);
      }
   }
}
