package com.awerial.packeditor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.fabricmc.loader.api.FabricLoader;

public final class Profiles {
   public static final String CODE_PREFIX = "APE1:";
   public static final int MAX_NAME = 32;
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private static final Map<String, JsonObject> SAVED = new LinkedHashMap();
   private static boolean loaded;
   public static final String SHORT_PREFIX = "APE-";
   private static final int FORMAT = 2;
   private static final String[] CODE_IDS = new String[]{null, "end_crystal", "respawn_anchor", "glowstone", "totem", "totem_size", "obsidian", "ender_pearl", "golden_apple", "fire", "shield", "netherite_sword", "netherite_pickaxe", "netherite_axe", "netherite_shovel", "netherite_helmet", "netherite_chestplate", "netherite_leggings", "netherite_boots", "diamond_sword", "diamond_pickaxe", "diamond_axe", "diamond_shovel", "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots", "crosshair", "netherite_spear", "diamond_spear", "mace", "glint", "sky", "crosshair_size", "more_toggles", "disabled_shield_r", "disabled_shield_g", "disabled_shield_b", "cobweb", "cobweb_opacity"};
   private static final String[] SIZE_IDS = new String[]{"vanilla", "size:small", "size:very_small"};
   public static final Map<String, Setup> PRESETS = new LinkedHashMap();
   private static final Set<String> HIDDEN_PRESETS;

   private Profiles() {
   }

   public static JsonObject toJson(Setup s) {
      JsonObject sel = new JsonObject();
      s.selections().forEach((c, id) -> {
         if (!"vanilla".equals(id)) {
            sel.addProperty(c.id, id);
         }

      });
      JsonObject tg = new JsonObject();
      s.toggles().forEach((t, on) -> {
         if (on) {
            tg.addProperty(t.id, true);
         }

      });
      JsonObject root = new JsonObject();
      root.add("selections", sel);
      root.add("toggles", tg);
      if (s.shieldColor() >= 0) {
         root.addProperty("shield_color", s.shieldColor());
      }

      if (s.crosshairSize() > 0) {
         root.addProperty("crosshair_size", s.crosshairSize());
      }

      if (s.disabledShieldColor() >= 0) {
         root.addProperty("disabled_shield_color", s.disabledShieldColor());
      }

      if (s.cobwebOpacity() < 100) {
         root.addProperty("cobweb_opacity", s.cobwebOpacity());
      }

      return root;
   }

   public static Setup fromJson(JsonObject root) {
      Map<Category, String> sel = new EnumMap(Category.class);
      if (root.has("selections")) {
         JsonObject o = root.getAsJsonObject("selections");

         for(String k : o.keySet()) {
            Category c = Category.byId(k);
            if (c != null) {
               sel.put(c, o.get(k).getAsString());
            }
         }
      }

      Map<Toggle, Boolean> tg = new EnumMap(Toggle.class);

      for(Toggle t : Toggle.values()) {
         tg.put(t, false);
      }

      if (root.has("toggles")) {
         JsonObject o = root.getAsJsonObject("toggles");

         for(Toggle t : Toggle.values()) {
            if (o.has(t.id)) {
               tg.put(t, o.get(t.id).getAsBoolean());
            }
         }
      }

      int color = root.has("shield_color") ? root.get("shield_color").getAsInt() & 16777215 : -1;
      if (root.has("shield_color") && root.get("shield_color").getAsInt() < 0) {
         color = -1;
      }

      if (root.has("toggles") && root.getAsJsonObject("toggles").has("shield_colour") && !root.getAsJsonObject("toggles").get("shield_colour").getAsBoolean()) {
         color = -1;
      }

      int size = root.has("crosshair_size") ? Math.max(0, Math.min(4, root.get("crosshair_size").getAsInt())) : 0;
      int disabled = root.has("disabled_shield_color") ? root.get("disabled_shield_color").getAsInt() : -1;
      int cobweb = root.has("cobweb_opacity") ? Math.max(0, Math.min(100, root.get("cobweb_opacity").getAsInt())) : 100;
      return new Setup(sel, tg, color, size, disabled < 0 ? -1 : disabled & 16777215, cobweb);
   }

   private static int codeOf(Category c) {
      for(int i = 1; i < CODE_IDS.length; ++i) {
         if (c.id.equals(CODE_IDS[i])) {
            return i;
         }
      }

      return -1;
   }

   static int packHash(String packKey) {
      CRC32 crc = new CRC32();
      crc.update(packKey.getBytes(StandardCharsets.UTF_8));
      return (int)(crc.getValue() & 65535L);
   }

   public static String shortCode(Setup s) {
      ByteArrayOutputStream b = new ByteArrayOutputStream();
      b.write(2);
      int flags = 0;

      for(Toggle t : Toggle.values()) {
         if (t.inCodeFlags() && (Boolean)s.toggles().getOrDefault(t, false)) {
            flags |= 1 << t.ordinal();
         }
      }

      boolean colour = s.shieldColor() >= 0;
      if (colour) {
         flags |= 128;
      }

      b.write(flags);
      if (colour) {
         b.write(s.shieldColor() >> 16 & 255);
         b.write(s.shieldColor() >> 8 & 255);
         b.write(s.shieldColor() & 255);
      }

      for(Map.Entry<Category, String> e : s.selections().entrySet()) {
         int code = codeOf((Category)e.getKey());
         String id = (String)e.getValue();
         if (code >= 0 && id != null && !id.equals("vanilla")) {
            if (id.startsWith("pack:")) {
               int h = packHash(ExternalPack.key(id.substring(5)));
               b.write(code | 128);
               b.write(h >> 8);
               b.write(h & 255);
            } else {
               int n = -1;
               if (id.startsWith("recolour:")) {
                  Variant.RecolourVariant rv = Variant.RecolourVariant.of(id);
                  if (rv != null) {
                     n = 128 | (rv.color >> 22 & 3) << 5 | (rv.color >> 13 & 7) << 2 | rv.color >> 6 & 3;
                  }
               } else if (id.startsWith("style:")) {
                  for(int i = 0; i < Styles.ALL.size(); ++i) {
                     if (id.equals("style:" + ((Styles.Style)Styles.ALL.get(i)).id())) {
                        n = i + 1;
                     }
                  }
               } else if (id.startsWith("preset:")) {
                  try {
                     int p = Integer.parseInt(id.substring(7));
                     if (p >= 0 && p < 64) {
                        n = 64 | p;
                     }
                  } catch (NumberFormatException var10) {
                  }
               } else {
                  n = Arrays.asList(SIZE_IDS).indexOf(id);
               }

               if (n > 0) {
                  b.write(code);
                  b.write(n);
               }
            }
         }
      }

      if (s.crosshairSize() > 0) {
         writeExtra(b, "crosshair_size", s.crosshairSize());
      }

      int more = 0;

      for(Toggle t : Toggle.values()) {
         if (!t.inCodeFlags() && (Boolean)s.toggles().getOrDefault(t, false)) {
            more |= 1 << t.ordinal() - 7;
         }
      }

      if (more != 0) {
         writeExtra(b, "more_toggles", more);
      }

      if (s.cobwebOpacity() < 100) {
         writeExtra(b, "cobweb_opacity", s.cobwebOpacity());
      }

      if (s.disabledShieldColor() >= 0) {
         writeExtra(b, "disabled_shield_r", s.disabledShieldColor() >> 16 & 255);
         writeExtra(b, "disabled_shield_g", s.disabledShieldColor() >> 8 & 255);
         writeExtra(b, "disabled_shield_b", s.disabledShieldColor() & 255);
      }

      return "APE-" + Base64.getUrlEncoder().withoutPadding().encodeToString(b.toByteArray());
   }

   private static void writeExtra(ByteArrayOutputStream b, String name, int value) {
      b.write(Arrays.asList(CODE_IDS).indexOf(name));
      b.write(value & 255);
   }

   public static Setup decode(String code, Map<Category, List<Variant>> variants) {
      String c = code == null ? "" : code.replaceAll("\\s+", "");
      if (c.startsWith("APE1:")) {
         return decode(c);
      } else if (!c.startsWith("APE-")) {
         throw new IllegalArgumentException("That isn't an Awerial's Pack Editor code");
      } else {
         byte[] raw;
         try {
            raw = Base64.getUrlDecoder().decode(c.substring("APE-".length()));
         } catch (IllegalArgumentException var22) {
            throw new IllegalArgumentException("That code is broken or incomplete");
         }

         if (raw.length >= 2 && raw[0] == 2) {
            int i = 1;
            int flags = raw[i++] & 255;
            Map<Toggle, Boolean> tg = new EnumMap(Toggle.class);

            for(Toggle t : Toggle.values()) {
               if (t.inCodeFlags()) {
                  tg.put(t, (flags & 1 << t.ordinal()) != 0);
               }
            }

            int color = -1;
            if ((flags & 128) != 0) {
               if (raw.length < i + 3) {
                  throw new IllegalArgumentException("That code is broken or incomplete");
               }

               color = (raw[i] & 255) << 16 | (raw[i + 1] & 255) << 8 | raw[i + 2] & 255;
               i += 3;
            }

            int crosshairSize = 0;
            int[] disabledRgb = new int[]{-1, -1, -1};
            int cobwebOpacity = 100;
            Map<Category, String> sel = new EnumMap(Category.class);

            while(i < raw.length) {
               int head = raw[i++] & 255;
               boolean pack = (head & 128) != 0;
               int catCode = head & 127;
               Category cat = catCode < CODE_IDS.length && CODE_IDS[catCode] != null ? Category.byId(CODE_IDS[catCode]) : null;
               if (pack) {
                  if (i + 2 > raw.length) {
                     throw new IllegalArgumentException("That code is broken or incomplete");
                  }

                  int h = (raw[i] & 255) << 8 | raw[i + 1] & 255;
                  i += 2;
                  if (cat != null) {
                     String found = "pack:?";

                     for(Variant v : (List<Variant>)variants.getOrDefault(cat, List.of())) {
                        if (v instanceof Variant.PackVariant) {
                           Variant.PackVariant pv = (Variant.PackVariant)v;
                           if (packHash(pv.pack.key) == h) {
                              found = v.id;
                           }
                        }
                     }

                     sel.put(cat, found);
                  }
               } else {
                  if (i >= raw.length) {
                     throw new IllegalArgumentException("That code is broken or incomplete");
                  }

                  int n = raw[i++] & 255;
                  String extra = catCode < CODE_IDS.length ? CODE_IDS[catCode] : null;
                  if ("crosshair_size".equals(extra)) {
                     crosshairSize = Math.min(4, n);
                  }

                  if ("more_toggles".equals(extra)) {
                     for(Toggle t : Toggle.values()) {
                        if (!t.inCodeFlags()) {
                           tg.put(t, (n & 1 << t.ordinal() - 7) != 0);
                        }
                     }
                  }

                  if ("cobweb_opacity".equals(extra)) {
                     cobwebOpacity = Math.min(100, n);
                  }

                  if ("disabled_shield_r".equals(extra)) {
                     disabledRgb[0] = n;
                  }

                  if ("disabled_shield_g".equals(extra)) {
                     disabledRgb[1] = n;
                  }

                  if ("disabled_shield_b".equals(extra)) {
                     disabledRgb[2] = n;
                  }

                  if (cat != null) {
                     if (cat == Category.TOTEM_SIZE) {
                        if (n < SIZE_IDS.length) {
                           sel.put(cat, SIZE_IDS[n]);
                        }
                     } else if (n >= 128) {
                        int r = (n >> 5 & 3) * 255 / 3;
                        int g = (n >> 2 & 7) * 255 / 7;
                        int b = (n & 3) * 255 / 3;
                        Object[] var10003 = new Object[]{r << 16 | g << 8 | b};
                        sel.put(cat, "recolour:" + String.format("%06x", var10003));
                     } else if (n >= 64) {
                        sel.put(cat, "preset:" + (n & 63));
                     } else if (n >= 1 && n <= Styles.ALL.size()) {
                        sel.put(cat, "style:" + ((Styles.Style)Styles.ALL.get(n - 1)).id());
                     }
                  }
               }
            }

            int disabled = disabledRgb[0] >= 0 && disabledRgb[1] >= 0 && disabledRgb[2] >= 0 ? disabledRgb[0] << 16 | disabledRgb[1] << 8 | disabledRgb[2] : -1;
            return new Setup(sel, tg, color, crosshairSize, disabled, cobwebOpacity);
         } else {
            throw new IllegalArgumentException("That code is broken or from a newer version");
         }
      }
   }

   public static String encode(Setup s) {
      try {
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         GZIPOutputStream gz = new GZIPOutputStream(bytes);

         try {
            gz.write((new Gson()).toJson(toJson(s)).getBytes(StandardCharsets.UTF_8));
         } catch (Throwable var6) {
            try {
               gz.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         gz.close();
         return "APE1:" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.toByteArray());
      } catch (IOException e) {
         throw new IllegalStateException(e);
      }
   }

   public static Setup decode(String code) {
      String c = code == null ? "" : code.replaceAll("\\s+", "");
      if (!c.startsWith("APE1:")) {
         throw new IllegalArgumentException("That isn't an Awerial's Pack Editor code");
      } else {
         try {
            byte[] raw = Base64.getUrlDecoder().decode(c.substring("APE1:".length()));
            GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(raw));

            Setup var5;
            try {
               String json = new String(gz.readAllBytes(), StandardCharsets.UTF_8);
               var5 = fromJson((JsonObject)GSON.fromJson(json, JsonObject.class));
            } catch (Throwable var7) {
               try {
                  gz.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            gz.close();
            return var5;
         } catch (Exception var8) {
            throw new IllegalArgumentException("That code is broken or incomplete");
         }
      }
   }

   private static Path file() {
      return FabricLoader.getInstance().getConfigDir().resolve("awerialspackeditor_configs.json");
   }

   private static void ensureLoaded() {
      if (!loaded) {
         loaded = true;
         Path f = file();
         if (Files.isRegularFile(f, new LinkOption[0])) {
            try {
               JsonObject root = (JsonObject)GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), JsonObject.class);
               if (root != null && root.has("configs")) {
                  JsonObject all = root.getAsJsonObject("configs");

                  for(String name : all.keySet()) {
                     JsonElement e = all.get(name);
                     if (e.isJsonObject()) {
                        SAVED.put(name, e.getAsJsonObject());
                     }
                  }
               }
            } catch (Exception e) {
               PackEditorClient.LOGGER.warn("Could not read saved configs", e);
            }

         }
      }
   }

   private static void write() {
      JsonObject all = new JsonObject();
      SAVED.forEach(all::add);
      JsonObject root = new JsonObject();
      root.add("configs", all);
      if (!HIDDEN_PRESETS.isEmpty()) {
         JsonArray hidden = new JsonArray();
         HIDDEN_PRESETS.forEach(hidden::add);
         root.add("hidden_presets", hidden);
      }

      try {
         Files.createDirectories(file().getParent());
         Files.writeString(file(), GSON.toJson(root), StandardCharsets.UTF_8);
      } catch (IOException e) {
         PackEditorClient.LOGGER.error("Could not save configs", e);
      }

   }

   public static List<String> names() {
      ensureLoaded();
      return new ArrayList(SAVED.keySet());
   }

   public static Setup get(String name) {
      ensureLoaded();
      JsonObject o = (JsonObject)SAVED.get(name);
      if (o == null && PRESETS.containsKey(name)) {
         return (Setup)PRESETS.get(name);
      } else {
         return o == null ? null : fromJson(o);
      }
   }

   private static String[] styledPicks(String[] categories, String style, String... extra) {
      List<String> out = new ArrayList();

      for(String c : categories) {
         out.add(c);
         out.add(style);
      }

      out.addAll(List.of(extra));
      return (String[])out.toArray(new String[0]);
   }

   private static void preset(String name, int shieldColor, int crosshairSize, int disabledColor, String[] picks, Toggle... on) {
      Map<Category, String> sel = new EnumMap(Category.class);

      for(int i = 0; i + 1 < picks.length; i += 2) {
         Category c = Category.byId(picks[i]);
         if (c != null) {
            sel.put(c, picks[i + 1]);
         }
      }

      Map<Toggle, Boolean> tg = new EnumMap(Toggle.class);

      for(Toggle t : Toggle.values()) {
         tg.put(t, false);
      }

      for(Toggle t : on) {
         tg.put(t, true);
      }

      PRESETS.put(name, new Setup(sel, tg, shieldColor, crosshairSize, disabledColor, 100));
   }

   public static boolean isPreset(String name) {
      return PRESETS.containsKey(name) && !SAVED.containsKey(name);
   }

   public static boolean showsPreset(String name) {
      ensureLoaded();
      return isPreset(name) && !HIDDEN_PRESETS.contains(name);
   }

   public static boolean anyPresetHidden() {
      ensureLoaded();
      return !HIDDEN_PRESETS.isEmpty();
   }

   public static void restorePresets() {
      ensureLoaded();
      HIDDEN_PRESETS.clear();
      write();
   }

   public static void save(String name, Setup s) {
      ensureLoaded();
      SAVED.remove(name);
      SAVED.put(name, toJson(s));
      write();
   }

   public static void delete(String name) {
      ensureLoaded();
      if (SAVED.remove(name) != null) {
         write();
      } else if (PRESETS.containsKey(name) && HIDDEN_PRESETS.add(name)) {
         write();
      }

   }

   static {
      preset("Awerial's Setup", 65297, 2, 16711680, new String[]{"totem_size", "size:very_small", "fire", "pack:Vanilla+ 3D Crystal 1.21.zip", "sky", "style:royal", "shield", "pack:PigWhite"}, Toggle.SMALLER_GAPS, Toggle.SMALL_SHIELD, Toggle.LEGACY_GLINT, Toggle.SMALLER_PEARLS);
      preset("Clean Crystal PvP", -1, 2, 16719904, new String[]{"totem_size", "size:small"}, Toggle.SMALLER_GAPS, Toggle.SMALL_SHIELD, Toggle.LOW_FIRE, Toggle.SMALLER_PEARLS, Toggle.NO_EXPLOSION_PARTICLES);
      String[] styled = new String[]{"end_crystal", "respawn_anchor", "glowstone", "totem", "obsidian", "ender_pearl", "golden_apple", "crosshair", "netherite_sword", "netherite_axe", "netherite_helmet", "netherite_chestplate", "netherite_leggings", "netherite_boots", "diamond_sword", "diamond_axe", "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots"};
      preset("Neon", 59903, 0, 16719904, styledPicks(styled, "style:neon", "sky", "style:midnight"), Toggle.SMALLER_GAPS, Toggle.SMALL_SHIELD, Toggle.LOW_FIRE);
      preset("Frost", 3909631, 0, 16777215, styledPicks(styled, "style:frost", "sky", "style:frost"), Toggle.SMALLER_GAPS, Toggle.SMALL_SHIELD, Toggle.LOW_FIRE, Toggle.SMALLER_PEARLS);
      HIDDEN_PRESETS = new LinkedHashSet();
   }

   public static record Setup(Map<Category, String> selections, Map<Toggle, Boolean> toggles, int shieldColor, int crosshairSize, int disabledShieldColor, int cobwebOpacity) {
   }
}
