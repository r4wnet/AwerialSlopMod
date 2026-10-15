package com.awerial.packeditor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public abstract class Variant {
   public static final String VANILLA_ID = "vanilla";
   public final String id;
   public final String name;
   public final String source;
   public static ExternalPack classicGlintPack;
   public static boolean legacyGlint;

   protected Variant(String id, String name, String source) {
      this.id = id;
      this.name = name;
      this.source = source;
   }

   public abstract NativeImage loadImage(Category.Target var1) throws IOException;

   public byte[] loadMeta(Category.Target target) throws IOException {
      return readVanilla(target.path() + ".mcmeta");
   }

   public byte[] loadModel(String assetPath) throws IOException {
      return null;
   }

   public boolean isVanilla() {
      return false;
   }

   public static boolean legacyGlintAvailable() {
      return LegacyGlint.ready() || classicGlintPack != null;
   }

   static byte[] readVanilla(String texturePath) throws IOException {
      boolean glint = texturePath.startsWith("misc/enchanted_") && texturePath.endsWith(".png");
      if (legacyGlint && glint) {
         byte[] old = LegacyGlint.get();
         if (old != null) {
            return old;
         }
      }

      if (legacyGlint && classicGlintPack != null && glint) {
         String p = classicGlintPack.find(List.of(texturePath, "misc/enchanted_glint_armor.png", "misc/enchanted_glint_item.png", "misc/enchanted_item_glint.png"));
         if (p != null) {
            return classicGlintPack.readTexture(p);
         }
      }

      return readVanillaAsset("textures/" + texturePath);
   }

   static byte[] readVanillaAsset(String assetPath) throws IOException {
      InputSupplier<InputStream> in = MinecraftClient.getInstance().getDefaultResourcePack().open(ResourceType.CLIENT_RESOURCES, Identifier.ofVanilla(assetPath));
      if (in == null) {
         return null;
      } else {
         InputStream s = (InputStream)in.get();

         byte[] var3;
         try {
            var3 = s.readAllBytes();
         } catch (Throwable var6) {
            if (s != null) {
               try {
                  s.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (s != null) {
            s.close();
         }

         return var3;
      }
   }

   public static final class Vanilla extends Variant {
      public Vanilla() {
         this("Vanilla");
      }

      public Vanilla(String name) {
         super("vanilla", name, "Default Minecraft");
      }

      public NativeImage loadImage(Category.Target target) throws IOException {
         byte[] b = readVanilla(target.path());
         return b == null ? null : NativeImage.read(b);
      }

      public boolean isVanilla() {
         return true;
      }
   }

   public static final class SizeVariant extends Variant {
      public final float previewScale;
      private final String json;

      public SizeVariant(String id, String name, float scale, float height, float previewScale) {
         super(id, name, "Held totem size • small pop");
         this.previewScale = previewScale;
         String s = String.format(Locale.ROOT, "[%.2f, %.2f, %.2f]", scale, scale, scale);
         String t = String.format(Locale.ROOT, "[1.13, %.2f, 1.13]", height);
         this.json = "{\n  \"parent\": \"minecraft:item/generated\",\n  \"textures\": { \"layer0\": \"minecraft:item/totem_of_undying\" },\n  \"display\": {\n    \"firstperson_righthand\": { \"rotation\": [0, -90, 25], \"translation\": " + t + ", \"scale\": " + s + " },\n    \"firstperson_lefthand\": { \"rotation\": [0, 90, -25], \"translation\": " + t + ", \"scale\": " + s + " },\n    \"fixed\": { \"rotation\": [0, 180, 0], \"translation\": [0, -4, 0], \"scale\": [0.3, 0.3, 0.3] }\n  }\n}\n";
      }

      public static List<Variant> all() {
         return List.of(new SizeVariant("vanilla", "Big Totem", 0.68F, 3.2F, 1.0F), new SizeVariant("size:small", "Small Totem", 0.4F, 2.0F, 0.62F), new SizeVariant("size:very_small", "Very Small Totem", 0.25F, 1.5F, 0.4F));
      }

      public NativeImage loadImage(Category.Target target) {
         return null;
      }

      public byte[] loadModel(String assetPath) {
         return this.json.getBytes(StandardCharsets.UTF_8);
      }
   }

   public static final class DrawnVariant extends Variant {
      public final Category category;
      public final String drawingName;

      public DrawnVariant(Category category, String drawingName) {
         super("drawn:" + drawingName, drawingName, "Your drawing");
         this.category = category;
         this.drawingName = drawingName;
      }

      public NativeImage loadImage(Category.Target target) throws IOException {
         if (this.category == Category.END_CRYSTAL) {
            if (target.path().equals("entity/end_crystal/end_crystal.png")) {
               return Drawings.load(this.category, this.drawingName);
            } else if (target.path().equals("item/end_crystal.png")) {
               NativeImage entity = Drawings.load(this.category, this.drawingName);

               NativeImage var3;
               try {
                  var3 = entity == null ? null : CrystalIcon.itemTexture(entity);
               } catch (Throwable var6) {
                  if (entity != null) {
                     try {
                        entity.close();
                     } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                     }
                  }

                  throw var6;
               }

               if (entity != null) {
                  entity.close();
               }

               return var3;
            } else {
               return null;
            }
         } else if (this.category == Category.RESPAWN_ANCHOR) {
            return AnchorParts.load(this.drawingName, target.path());
         } else {
            Category.Target worn = this.category.wornTarget();
            if (target == worn) {
               return WornArmour.wornImage(worn, Drawings.loadWorn(this.category, this.drawingName), Drawings.load(this.category, this.drawingName));
            } else {
               return target != this.category.targets.get(0) && this.category != Category.FIRE ? null : Drawings.load(this.category, this.drawingName);
            }
         }
      }

      public byte[] loadModel(String assetPath) {
         return this.category == Category.END_CRYSTAL && assetPath.equals("models/item/end_crystal.json") ? CrystalIcon.MODEL.getBytes(StandardCharsets.UTF_8) : null;
      }

      public byte[] loadMeta(Category.Target target) {
         int ticks = target != this.category.targets.get(0) && this.category != Category.FIRE ? 0 : Drawings.frameTime(this.category, this.drawingName);
         return ticks > 0 ? Drawings.animationMeta(ticks) : null;
      }
   }

   public static final class RecolourVariant extends Variant {
      public static final String PREFIX = "recolour:";
      public final int color;

      public RecolourVariant(int color) {
         super("recolour:" + String.format("%06x", color & 16777215), "Custom", "Your colour");
         this.color = color & 16777215;
      }

      public static RecolourVariant of(String id) {
         try {
            return new RecolourVariant(Integer.parseInt(id.substring("recolour:".length()), 16));
         } catch (RuntimeException var2) {
            return null;
         }
      }

      public static int recolour(int argb, int target) {
         float tr = (float)(target >> 16 & 255) / 255.0F;
         float tg = (float)(target >> 8 & 255) / 255.0F;
         float tb = (float)(target & 255) / 255.0F;
         float tmax = Math.max(tr, Math.max(tg, tb));
         float tmin = Math.min(tr, Math.min(tg, tb));
         float th = 0.0F;
         float ts = tmax == 0.0F ? 0.0F : (tmax - tmin) / tmax;
         if (tmax > tmin) {
            if (tmax == tr) {
               th = (tg - tb) / (tmax - tmin) / 6.0F;
            } else if (tmax == tg) {
               th = ((tb - tr) / (tmax - tmin) + 2.0F) / 6.0F;
            } else {
               th = ((tr - tg) / (tmax - tmin) + 4.0F) / 6.0F;
            }

            if (th < 0.0F) {
               ++th;
            }
         }

         float r = (float)(argb >> 16 & 255) / 255.0F;
         float g = (float)(argb >> 8 & 255) / 255.0F;
         float b = (float)(argb & 255) / 255.0F;
         float max = Math.max(r, Math.max(g, b));
         float min = Math.min(r, Math.min(g, b));
         float s = max == 0.0F ? 0.0F : (max - min) / max;
         float ns = ts * (0.35F + 0.65F * s);
         float v = max * (0.55F + 0.45F * tmax);
         int rgb = MathHelper.hsvToRgb(th, Math.min(1.0F, ns), Math.min(1.0F, v));
         return argb & -16777216 | rgb & 16777215;
      }

      public NativeImage loadImage(Category.Target target) throws IOException {
         byte[] b = readVanilla(target.path());
         if (b == null) {
            return null;
         } else {
            NativeImage img = NativeImage.read(b);

            for(int y = 0; y < img.getHeight(); ++y) {
               for(int x = 0; x < img.getWidth(); ++x) {
                  int c = img.getColorArgb(x, y);
                  if (c >>> 24 != 0) {
                     img.setColorArgb(x, y, recolour(c, this.color));
                  }
               }
            }

            return img;
         }
      }
   }

   public static final class StyleVariant extends Variant {
      public final Styles.Style style;

      public StyleVariant(Styles.Style style) {
         super("style:" + style.id(), style.name(), "Built-in style");
         this.style = style;
      }

      public NativeImage loadImage(Category.Target target) throws IOException {
         byte[] b = readVanilla(target.path());
         if (b == null) {
            return null;
         } else {
            NativeImage img = NativeImage.read(b);

            for(int y = 0; y < img.getHeight(); ++y) {
               for(int x = 0; x < img.getWidth(); ++x) {
                  int c = img.getColorArgb(x, y);
                  if (c >>> 24 != 0) {
                     img.setColorArgb(x, y, this.style.op().applyAsInt(c));
                  }
               }
            }

            return img;
         }
      }
   }

   public static final class PackVariant extends Variant {
      public final ExternalPack pack;
      private final Map<String, String> resolved = new HashMap();
      private ExternalPack iconSource;
      private IntUnaryOperator iconRecolor;
      private boolean crystalIconFromEntity;

      private PackVariant(ExternalPack pack) {
         super("pack:" + pack.fileName, pack.displayName, pack.bundled ? "Built into the mod" : (pack.userAdded ? "Your uploaded pack" : "From pack: " + pack.fileName));
         this.pack = pack;
      }

      public static PackVariant of(ExternalPack pack, Category category) {
         PackVariant v = new PackVariant(pack);

         for(Category.Target t : category.targets) {
            String found = pack.find(t.allPaths());
            if (found != null) {
               v.resolved.put(t.path(), found);
            }
         }

         return v.resolved.isEmpty() ? null : v;
      }

      public void useIconFrom(ExternalPack source, IntUnaryOperator recolor) {
         this.iconSource = source;
         this.iconRecolor = recolor;
      }

      public void deriveCrystalIcon() {
         this.crystalIconFromEntity = true;
      }

      private boolean isIcon(Category.Target target) {
         return this.iconSource != null && target.path().startsWith("item/");
      }

      public NativeImage loadImage(Category.Target target) throws IOException {
         if (this.crystalIconFromEntity && target.path().equals("item/end_crystal.png")) {
            String p = (String)this.resolved.get("entity/end_crystal/end_crystal.png");
            byte[] b = p == null ? null : this.pack.readTexture(p);
            if (b == null) {
               return null;
            } else {
               NativeImage entity = NativeImage.read(b);

               NativeImage var15;
               try {
                  var15 = CrystalIcon.itemTexture(entity);
               } catch (Throwable var9) {
                  if (entity != null) {
                     try {
                        entity.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (entity != null) {
                  entity.close();
               }

               return var15;
            }
         } else if (!this.isIcon(target)) {
            String p = (String)this.resolved.get(target.path());
            if (p == null) {
               return null;
            } else {
               byte[] b = this.pack.readTexture(p);
               return b == null ? null : NativeImage.read(b);
            }
         } else {
            String p = this.iconSource.find(target.allPaths());
            byte[] b = p == null ? null : this.iconSource.readTexture(p);
            if (b == null) {
               return null;
            } else {
               NativeImage img = NativeImage.read(b);

               for(int y = 0; y < img.getHeight(); ++y) {
                  for(int x = 0; x < img.getWidth(); ++x) {
                     int c = img.getColorArgb(x, y);
                     if (c >>> 24 != 0) {
                        img.setColorArgb(x, y, this.iconRecolor.applyAsInt(c));
                     }
                  }
               }

               return img;
            }
         }
      }

      public byte[] loadMeta(Category.Target target) throws IOException {
         if (this.isIcon(target)) {
            String p = this.iconSource.find(target.allPaths());
            return p == null ? null : this.iconSource.readTextureMeta(p);
         } else {
            String p = (String)this.resolved.get(target.path());
            if (p == null) {
               return null;
            } else {
               byte[] meta = this.pack.readTextureMeta(p);
               return meta != null ? meta : super.loadMeta(target);
            }
         }
      }

      public byte[] loadModel(String assetPath) throws IOException {
         return this.crystalIconFromEntity && assetPath.equals("models/item/end_crystal.json") ? CrystalIcon.MODEL.getBytes(StandardCharsets.UTF_8) : (this.iconSource != null ? this.iconSource : this.pack).readAsset(assetPath);
      }
   }
}
