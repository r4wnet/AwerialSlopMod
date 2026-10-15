package com.awerial.packeditor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class Effects {
   public static boolean popCounter = true;
   public static int popColor = 16767291;
   public static boolean popColorByCount = true;
   public static boolean damageTint;
   public static int tintColor = 10320895;
   public static boolean tintGradient;
   public static int tintColor2 = 59903;
   public static int tintOpacity = 30;
   public static TintTarget tintTarget;
   public static boolean hitParticles;
   public static int hitColor;
   public static KillEffect killEffect;
   public static int killColor;
   public static boolean crystalOutline;
   public static int outlineColor;
   public static boolean maceRing;
   public static int maceColor;
   public static boolean maceBoom;
   public static boolean spearCharge;
   public static boolean totemParticles;
   public static int totemColor;
   public static PopAnimation popAnimation;
   public static boolean durabilityWarning;
   public static float mainX;
   public static float mainY;
   public static float mainZ;
   public static float offX;
   public static float offY;
   public static float offZ;
   public static float mainSize;
   public static float offSize;
   private static final Gson GSON;

   private Effects() {
   }

   private static Path file() {
      return FabricLoader.getInstance().getConfigDir().resolve("awerialspackeditor_effects.json");
   }

   public static void load() {
      Path f = file();
      if (Files.isRegularFile(f, new LinkOption[0])) {
         try {
            JsonObject o = (JsonObject)GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), JsonObject.class);
            if (o == null) {
               return;
            }

            popCounter = bool(o, "pop_counter", popCounter);
            popColor = color(o, "pop_color", popColor);
            popColorByCount = bool(o, "pop_color_by_count", popColorByCount);
            tintOpacity = Math.max(0, Math.min(100, (int)num(o, "tint_opacity", (float)tintOpacity)));
            tintTarget = o.has("tint_target") ? (TintTarget)enumOr(TintTarget.class, o.get("tint_target").getAsString(), tintTarget) : tintTarget;
            damageTint = bool(o, "damage_tint", damageTint);
            tintColor = color(o, "tint_color", tintColor);
            tintGradient = bool(o, "tint_gradient", tintGradient);
            tintColor2 = color(o, "tint_color_2", tintColor2);
            hitParticles = bool(o, "hit_particles", hitParticles);
            hitColor = color(o, "hit_color", hitColor);
            killEffect = o.has("kill_effect") ? (KillEffect)enumOr(KillEffect.class, o.get("kill_effect").getAsString(), killEffect) : killEffect;
            killColor = color(o, "kill_color", killColor);
            crystalOutline = bool(o, "crystal_outline", crystalOutline);
            outlineColor = color(o, "outline_color", outlineColor);
            maceRing = bool(o, "mace_ring", maceRing);
            maceColor = color(o, "mace_color", maceColor);
            maceBoom = bool(o, "mace_boom", maceBoom);
            spearCharge = bool(o, "spear_charge", spearCharge);
            totemParticles = bool(o, "totem_particles", totemParticles);
            totemColor = color(o, "totem_color", totemColor);
            popAnimation = o.has("pop_animation") ? (PopAnimation)enumOr(PopAnimation.class, o.get("pop_animation").getAsString(), popAnimation) : popAnimation;
            durabilityWarning = bool(o, "durability_warning", durabilityWarning);
            mainX = num(o, "main_x", 0.0F);
            mainY = num(o, "main_y", 0.0F);
            mainZ = num(o, "main_z", 0.0F);
            offX = num(o, "off_x", 0.0F);
            offY = num(o, "off_y", 0.0F);
            offZ = num(o, "off_z", 0.0F);
            mainSize = num(o, "main_size", 1.0F);
            offSize = num(o, "off_size", 1.0F);
         } catch (Exception e) {
            PackEditorClient.LOGGER.warn("Could not read effects settings", e);
         }

      }
   }

   public static void save() {
      JsonObject o = new JsonObject();
      o.addProperty("pop_counter", popCounter);
      o.addProperty("pop_color", popColor);
      o.addProperty("pop_color_by_count", popColorByCount);
      o.addProperty("tint_opacity", tintOpacity);
      o.addProperty("tint_target", tintTarget.name());
      o.addProperty("damage_tint", damageTint);
      o.addProperty("tint_color", tintColor);
      o.addProperty("tint_gradient", tintGradient);
      o.addProperty("tint_color_2", tintColor2);
      o.addProperty("hit_particles", hitParticles);
      o.addProperty("hit_color", hitColor);
      o.addProperty("kill_effect", killEffect.name());
      o.addProperty("kill_color", killColor);
      o.addProperty("crystal_outline", crystalOutline);
      o.addProperty("outline_color", outlineColor);
      o.addProperty("mace_ring", maceRing);
      o.addProperty("mace_color", maceColor);
      o.addProperty("mace_boom", maceBoom);
      o.addProperty("spear_charge", spearCharge);
      o.addProperty("totem_particles", totemParticles);
      o.addProperty("totem_color", totemColor);
      o.addProperty("pop_animation", popAnimation.name());
      o.addProperty("durability_warning", durabilityWarning);
      o.addProperty("main_x", mainX);
      o.addProperty("main_y", mainY);
      o.addProperty("main_z", mainZ);
      o.addProperty("off_x", offX);
      o.addProperty("off_y", offY);
      o.addProperty("off_z", offZ);
      o.addProperty("main_size", mainSize);
      o.addProperty("off_size", offSize);

      try {
         Files.createDirectories(file().getParent());
         Files.writeString(file(), GSON.toJson(o), StandardCharsets.UTF_8);
      } catch (Exception e) {
         PackEditorClient.LOGGER.error("Could not save effects settings", e);
      }

   }

   private static boolean bool(JsonObject o, String k, boolean def) {
      return o.has(k) ? o.get(k).getAsBoolean() : def;
   }

   private static int color(JsonObject o, String k, int def) {
      return o.has(k) ? o.get(k).getAsInt() & 16777215 : def;
   }

   private static float num(JsonObject o, String k, float def) {
      return o.has(k) ? o.get(k).getAsFloat() : def;
   }

   private static <E extends Enum<E>> E enumOr(Class<E> type, String name, E def) {
      try {
         return (E)Enum.valueOf(type, name);
      } catch (IllegalArgumentException var4) {
         return def;
      }
   }

   public static int popColorFor(int pops) {
      if (!popColorByCount) {
         return popColor;
      } else if (pops <= 2) {
         return 5635925;
      } else if (pops <= 4) {
         return 43520;
      } else if (pops <= 6) {
         return 16777045;
      } else {
         return pops <= 8 ? 16755200 : 16733525;
      }
   }

   public static int mix(int a, int b, float t) {
      int r = Math.round((float)(a >> 16 & 255) + (float)((b >> 16 & 255) - (a >> 16 & 255)) * t);
      int g = Math.round((float)(a >> 8 & 255) + (float)((b >> 8 & 255) - (a >> 8 & 255)) * t);
      int bl = Math.round((float)(a & 255) + (float)((b & 255) - (a & 255)) * t);
      return r << 16 | g << 8 | bl;
   }

   static {
      tintTarget = Effects.TintTarget.PLAYER;
      hitColor = 16739285;
      killEffect = Effects.KillEffect.OFF;
      killColor = 10320895;
      outlineColor = 14236671;
      maceRing = true;
      maceColor = 59903;
      spearCharge = true;
      totemColor = 10320895;
      popAnimation = Effects.PopAnimation.NORMAL;
      durabilityWarning = true;
      mainSize = 1.0F;
      offSize = 1.0F;
      GSON = (new GsonBuilder()).setPrettyPrinting().create();
   }

   public static enum KillEffect {
      OFF("Off"),
      LIGHTNING("Lightning"),
      FIREWORK("Firework"),
      BOTH("Both");

      public final String label;

      private KillEffect(String label) {
         this.label = label;
      }

   }

   public static enum TintTarget {
      PLAYER("Player"),
      ARMOUR("Armour"),
      BOTH("Both");

      public final String label;

      private TintTarget(String label) {
         this.label = label;
      }

   }

   public static enum PopAnimation {
      NORMAL("Normal"),
      SHORT("Short"),
      NONE("None");

      public final String label;

      private PopAnimation(String label) {
         this.label = label;
      }

   }
}
