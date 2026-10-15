package com.awerial.packeditor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;

public final class PackEditorConfig {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private static final Map<Category, String> SELECTIONS = new EnumMap(Category.class);
   private static final Map<Toggle, Boolean> TOGGLES = new EnumMap(Toggle.class);
   private static int shieldColor = -1;
   private static String activeConfig;
   private static int crosshairSize;
   private static int disabledShieldColor = -1;
   private static int cobwebOpacity = 100;
   private static final Map<String, Set<String>> DELETED = new HashMap();
   private static final Map<String, List<String>> HIDDEN = readBuiltIn("default_hidden.json");
   private static final Map<String, List<String>> KEEP = readBuiltIn("default_keep.json");

   public static String activeConfig() {
      return activeConfig;
   }

   public static void setActiveConfig(String name) {
      activeConfig = name;
   }

   private PackEditorConfig() {
   }

   private static Path file() {
      return FabricLoader.getInstance().getConfigDir().resolve("awerialspackeditor.json");
   }

   public static void load() {
      SELECTIONS.clear();
      TOGGLES.clear();
      shieldColor = -1;
      activeConfig = null;
      crosshairSize = 0;
      disabledShieldColor = -1;
      cobwebOpacity = 100;
      DELETED.clear();
      Path f = file();
      if (Files.isRegularFile(f, new LinkOption[0])) {
         try {
            JsonObject root = (JsonObject)GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), JsonObject.class);
            if (root != null && root.has("shield_color")) {
               shieldColor = root.get("shield_color").getAsInt();
            }

            if (root != null && root.has("crosshair_size")) {
               crosshairSize = Math.max(0, Math.min(4, root.get("crosshair_size").getAsInt()));
            }

            if (root != null && root.has("deleted")) {
               JsonObject del = root.getAsJsonObject("deleted");

               for(String k : del.keySet()) {
                  Set<String> ids = new LinkedHashSet();

                  for(JsonElement e : del.getAsJsonArray(k)) {
                     ids.add(e.getAsString());
                  }

                  DELETED.put(k, ids);
               }
            }

            if (root != null && root.has("disabled_shield_color")) {
               disabledShieldColor = root.get("disabled_shield_color").getAsInt();
            }

            if (root != null && root.has("cobweb_opacity")) {
               cobwebOpacity = Math.max(0, Math.min(100, root.get("cobweb_opacity").getAsInt()));
            }

            if (root != null && root.has("toggles") && root.getAsJsonObject("toggles").has("shield_colour") && !root.getAsJsonObject("toggles").get("shield_colour").getAsBoolean()) {
               shieldColor = -1;
            }

            if (root != null && root.has("active_config")) {
               activeConfig = root.get("active_config").getAsString();
            }

            if (root != null && root.has("smaller_gaps")) {
               TOGGLES.put(Toggle.SMALLER_GAPS, root.get("smaller_gaps").getAsBoolean());
            }

            if (root != null && root.has("toggles")) {
               JsonObject tg = root.getAsJsonObject("toggles");

               for(Toggle t : Toggle.values()) {
                  if (tg.has(t.id)) {
                     TOGGLES.put(t, tg.get(t.id).getAsBoolean());
                  }
               }
            }

            if (root != null && root.has("selections")) {
               JsonObject sel = root.getAsJsonObject("selections");

               for(String k : sel.keySet()) {
                  Category c = Category.byId(k);
                  if (c != null) {
                     SELECTIONS.put(c, sel.get(k).getAsString());
                  }
               }

               if ("pack:Awerial Overlays".equals(SELECTIONS.get(Category.SHIELD))) {
                  SELECTIONS.put(Category.SHIELD, "pack:PigWhite");
               }
            }
         } catch (Exception e) {
            PackEditorClient.LOGGER.warn("Could not read config, using defaults", e);
         }

      }
   }

   private static Map<String, List<String>> readBuiltIn(String fileName) {
      Map<String, List<String>> into = new HashMap();

      try {
         InputStream in = PackEditorConfig.class.getResourceAsStream("/assets/awerialspackeditor/" + fileName);

         Object var12;
         label74: {
            try {
               if (in == null) {
                  var12 = into;
                  break label74;
               }

               JsonObject obj = (JsonObject)GSON.fromJson(new String(in.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);

               for(String k : obj.keySet()) {
                  List<String> names = new ArrayList();

                  for(JsonElement e : obj.getAsJsonArray(k)) {
                     names.add(e.getAsString());
                  }

                  into.put(k, List.copyOf(names));
               }
            } catch (Throwable var10) {
               if (in != null) {
                  try {
                     in.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (in != null) {
               in.close();
            }

            return into;
         }

         if (in != null) {
            in.close();
         }

         return (Map<String, List<String>>)var12;
      } catch (Exception e) {
         PackEditorClient.LOGGER.warn("Could not read {}", fileName, e);
         return into;
      }
   }

   public static void save(Map<Category, String> selections, Map<Toggle, Boolean> toggles, int shieldTint, int crosshair, int disabledShieldTint, int cobweb) {
      SELECTIONS.clear();
      SELECTIONS.putAll(selections);
      TOGGLES.clear();
      TOGGLES.putAll(toggles);
      shieldColor = shieldTint;
      crosshairSize = crosshair;
      disabledShieldColor = disabledShieldTint;
      cobwebOpacity = cobweb;
      write();
   }

   private static void write() {
      JsonObject sel = new JsonObject();
      SELECTIONS.forEach((c, id) -> sel.addProperty(c.id, id));
      JsonObject tg = new JsonObject();
      TOGGLES.forEach((t, on) -> tg.addProperty(t.id, on));
      JsonObject root = new JsonObject();
      root.add("selections", sel);
      root.add("toggles", tg);
      if (shieldColor >= 0) {
         root.addProperty("shield_color", shieldColor);
      }

      if (activeConfig != null) {
         root.addProperty("active_config", activeConfig);
      }

      if (crosshairSize > 0) {
         root.addProperty("crosshair_size", crosshairSize);
      }

      if (disabledShieldColor >= 0) {
         root.addProperty("disabled_shield_color", disabledShieldColor);
      }

      if (cobwebOpacity < 100) {
         root.addProperty("cobweb_opacity", cobwebOpacity);
      }

      if (!DELETED.isEmpty()) {
         JsonObject del = new JsonObject();
         DELETED.forEach((k, ids) -> {
            JsonArray a = new JsonArray();
            Objects.requireNonNull(a);
            ids.forEach(a::add);
            del.add(k, a);
         });
         root.add("deleted", del);
      }

      try {
         Files.createDirectories(file().getParent());
         Files.writeString(file(), GSON.toJson(root), StandardCharsets.UTF_8);
      } catch (IOException e) {
         PackEditorClient.LOGGER.error("Could not save config", e);
      }

   }

   public static int shieldColor() {
      return shieldColor;
   }

   public static int crosshairSize() {
      return crosshairSize;
   }

   public static int disabledShieldColor() {
      return disabledShieldColor;
   }

   public static int cobwebOpacity() {
      return cobwebOpacity;
   }

   public static void delete(Category c, String variantId) {
      ((Set)DELETED.computeIfAbsent(c.id, (k) -> new LinkedHashSet())).add(variantId);
      write();
   }

   public static boolean isDeleted(Category c, String variantId) {
      return ((Set)DELETED.getOrDefault(c.id, Set.of())).contains(variantId);
   }

   public static int deletedCount() {
      return DELETED.values().stream().mapToInt(Set::size).sum();
   }

   public static void restoreDeleted() {
      DELETED.clear();
      write();
   }

   public static boolean get(Toggle t) {
      return (Boolean)TOGGLES.getOrDefault(t, false);
   }

   public static Map<Toggle, Boolean> toggles() {
      Map<Toggle, Boolean> copy = new EnumMap(Toggle.class);

      for(Toggle t : Toggle.values()) {
         copy.put(t, get(t));
      }

      return copy;
   }

   public static String get(Category c) {
      return (String)SELECTIONS.getOrDefault(c, "vanilla");
   }

   public static void select(Category c, String variantId) {
      SELECTIONS.put(c, variantId);
      write();
   }

   public static Map<Category, String> all() {
      Map<Category, String> copy = new EnumMap(Category.class);
      copy.putAll(SELECTIONS);
      return copy;
   }

   private static int indexOf(List<String> names, ExternalPack pack) {
      for(int i = 0; i < names.size(); ++i) {
         if (ExternalPack.key((String)names.get(i)).equals(pack.key)) {
            return i;
         }
      }

      return -1;
   }

   private static List<String> keepFor(Category c) {
      List<String> own = (List)KEEP.get(c.id);
      if (own == null && "diamond".equals(c.material())) {
         own = (List)KEEP.get(c.counterpart().id);
      }

      return own != null ? own : (List)KEEP.get(c.listKey());
   }

   public static boolean isHidden(Category c, Variant v) {
      if (v instanceof Variant.PackVariant pv) {
         List<String> keep = keepFor(c);
         if (keep != null) {
            return indexOf(keep, pv.pack) < 0;
         } else {
            return indexOf((List)HIDDEN.getOrDefault(c.listKey(), List.of()), pv.pack) >= 0;
         }
      } else {
         return false;
      }
   }

   public static int sortKey(Category c, Variant v) {
      if (v instanceof Variant.PackVariant pv) {
         List<String> keep = keepFor(c);
         if (keep == null) {
            return 0;
         } else {
            int i = indexOf(keep, pv.pack);
            return i >= 0 ? i : 10000;
         }
      } else {
         return 0;
      }
   }
}
