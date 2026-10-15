package com.awerial.packeditor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntUnaryOperator;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;

public final class SkyTint {
   private static final Pattern SKY = Pattern.compile("^assets/[^/]+/(optifine/sky|mcpatcher/sky|fabricskyboxes|celestial|skyboxify)/.+\\.png$", 2);
   private static final List<String> VANILLA = List.of("environment/end_sky.png", "environment/celestial/sun.png", "environment/celestial/moon/full_moon.png", "environment/celestial/moon/waning_gibbous.png", "environment/celestial/moon/third_quarter.png", "environment/celestial/moon/waning_crescent.png", "environment/celestial/moon/new_moon.png", "environment/celestial/moon/waxing_crescent.png", "environment/celestial/moon/first_quarter.png", "environment/celestial/moon/waxing_gibbous.png");
   private static final int PREVIEW = 48;
   private static int[] previewBase;
   private static List<String> previewPacks;

   private SkyTint() {
   }

   public static IntUnaryOperator activeOp() {
      String id = PackEditorConfig.get(Category.SKY);
      if (id == null) {
         return null;
      } else if (id.startsWith("recolour:")) {
         return opFor(Variant.RecolourVariant.of(id));
      } else if (!id.startsWith("style:")) {
         return null;
      } else {
         Styles.Style s = Styles.byId(id.substring(6));
         return s == null ? null : s.op();
      }
   }

   public static IntUnaryOperator opFor(Variant v) {
      if (v instanceof Variant.StyleVariant sv) {
         return sv.style.op();
      } else if (v instanceof Variant.RecolourVariant rv) {
         int target = rv.color;
         return (c) -> Variant.RecolourVariant.recolour(c, target);
      } else {
         return null;
      }
   }

   private static List<ExternalPack> enabledPacks() {
      MinecraftClient client = MinecraftClient.getInstance();
      Map<String, ExternalPack> byName = new LinkedHashMap();

      for(ExternalPack p : ExternalPack.scan(client.getResourcePackDir())) {
         byName.put(p.fileName, p);
      }

      return client.options.resourcePacks.stream().filter((id) -> id.startsWith("file/") && !id.equals("file/AwerialsPackEditor")).map((id) -> (ExternalPack)byName.get(id.substring(5))).filter(Objects::nonNull).toList();
   }

   public static Map<String, ExternalPack> skyFiles() {
      Map<String, ExternalPack> files = new LinkedHashMap();

      for(ExternalPack p : enabledPacks()) {
         for(String rel : p.list((r) -> SKY.matcher(r).matches() || isVanillaSky(r))) {
            files.put(rel, p);
         }
      }

      return files;
   }

   private static boolean isVanillaSky(String rel) {
      return rel.startsWith("assets/minecraft/textures/") && VANILLA.contains(rel.substring("assets/minecraft/textures/".length()));
   }

   public static int write(Path packRoot, IntUnaryOperator op) {
      Map<String, ExternalPack> files = skyFiles();
      int n = 0;

      for(Map.Entry<String, ExternalPack> e : files.entrySet()) {
         try {
            byte[] b = ((ExternalPack)e.getValue()).readFile((String)e.getKey());
            if (b != null && writeTinted(packRoot.resolve((String)e.getKey()), b, op)) {
               ++n;
            }
         } catch (Exception ex) {
            PackEditorClient.LOGGER.warn("Couldn't recolour sky image {}: {}", e.getKey(), ex.toString());
         }
      }

      for(String v : VANILLA) {
         String rel = "assets/minecraft/textures/" + v;
         if (!files.containsKey(rel)) {
            try {
               byte[] b = Variant.readVanilla(v);
               if (b != null && writeTinted(packRoot.resolve(rel), b, op)) {
                  ++n;
               }
            } catch (Exception ex) {
               PackEditorClient.LOGGER.warn("Couldn't recolour {}: {}", v, ex.toString());
            }
         }
      }

      return n;
   }

   private static boolean writeTinted(Path dest, byte[] png, IntUnaryOperator op) throws IOException {
      NativeImage img = NativeImage.read(png);

      boolean var4;
      try {
         tint(img, op);
         Files.createDirectories(dest.getParent());
         img.writeTo(dest);
         var4 = true;
      } catch (Throwable var7) {
         if (img != null) {
            try {
               img.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (img != null) {
         img.close();
      }

      return var4;
   }

   static void tint(NativeImage img, IntUnaryOperator op) {
      for(int y = 0; y < img.getHeight(); ++y) {
         for(int x = 0; x < img.getWidth(); ++x) {
            int c = img.getColorArgb(x, y);
            if (c >>> 24 != 0) {
               img.setColorArgb(x, y, op.applyAsInt(c));
            }
         }
      }

   }

   public static synchronized NativeImage preview(IntUnaryOperator op) {
      List<String> packs = List.copyOf(MinecraftClient.getInstance().options.resourcePacks);
      if (previewBase == null || !packs.equals(previewPacks)) {
         previewBase = sample();
         previewPacks = packs;
      }

      NativeImage out = new NativeImage(48, 48, true);

      for(int y = 0; y < 48; ++y) {
         for(int x = 0; x < 48; ++x) {
            int c = previewBase[y * 48 + x];
            out.setColorArgb(x, y, op != null ? op.applyAsInt(c) : c);
         }
      }

      return out;
   }

   private static int[] sample() {
      int s = 48;
      int[] px = new int[s * s];
      NativeImage src = null;

      try {
         for(Map.Entry<String, ExternalPack> e : skyFiles().entrySet()) {
            if (SKY.matcher((CharSequence)e.getKey()).matches()) {
               byte[] b = ((ExternalPack)e.getValue()).readFile((String)e.getKey());
               if (b != null) {
                  src = NativeImage.read(b);
                  break;
               }
            }
         }
      } catch (Exception var7) {
      }

      for(int y = 0; y < s; ++y) {
         for(int x = 0; x < s; ++x) {
            if (src != null) {
               int fw = Math.max(1, src.getWidth() / 3);
               int fh = Math.max(1, src.getHeight() / 2);
               px[y * s + x] = src.getColorArgb(x * fw / s, y * fh / s) | -16777216;
            } else {
               px[y * s + x] = lerp(-8869889, -4138753, (float)y / (float)s);
            }
         }
      }

      if (src != null) {
         src.close();
      }

      return px;
   }

   private static int lerp(int a, int b, float f) {
      int r = (int)((float)(a >> 16 & 255) + (float)((b >> 16 & 255) - (a >> 16 & 255)) * f);
      int g = (int)((float)(a >> 8 & 255) + (float)((b >> 8 & 255) - (a >> 8 & 255)) * f);
      int bl = (int)((float)(a & 255) + (float)((b & 255) - (a & 255)) * f);
      return -16777216 | r << 16 | g << 8 | bl;
   }
}
