package com.awerial.packeditor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;

public final class VariantRegistry {
   private static Map<Category, List<Variant>> cached;
   private static String cachedSignature;
   private static int generation;

   private VariantRegistry() {
   }

   public static synchronized Map<Category, List<Variant>> get() {
      String sig = signature();
      if (cached == null || !sig.equals(cachedSignature)) {
         cached = scan();
         cachedSignature = sig;
         ++generation;
      }

      return cached;
   }

   public static synchronized void invalidate() {
      cached = null;
   }

   public static synchronized int generation() {
      return generation;
   }

   private static String signature() {
      StringBuilder sb = new StringBuilder();

      for(Path dir : List.of(MinecraftClient.getInstance().getResourcePackDir(), UserPacks.dir())) {
         if (Files.isDirectory(dir, new LinkOption[0])) {
            try {
               Stream<Path> s = Files.list(dir);

               try {
                  for(Path p : s.sorted().toList()) {
                     String name = p.getFileName().toString();
                     if (!name.equals("AwerialsPackEditor")) {
                        sb.append(name).append(':').append(Files.getLastModifiedTime(p).toMillis()).append(':').append(Files.isRegularFile(p, new LinkOption[0]) ? Files.size(p) : 0L).append('|');
                     }
                  }
               } catch (Throwable var8) {
                  if (s != null) {
                     try {
                        s.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (s != null) {
                  s.close();
               }
            } catch (IOException var9) {
               sb.append("?");
            }
         }
      }

      return sb.toString();
   }

   public static void warmUp() {
      Thread t = new Thread(() -> {
         try {
            get();
            ItemTextures.all();
            ItemTextures.searchNames();
         } catch (Throwable e) {
            PackEditorClient.LOGGER.warn("Editor warm-up failed: {}", e.toString());
         }

      }, "APE warm-up");
      t.setDaemon(true);
      t.setPriority(1);
      t.start();
   }

   public static Map<Category, List<Variant>> scan() {
      List<ExternalPack> packs = new ArrayList(ExternalPack.bundled());
      packs.addAll(UserPacks.scan());
      packs.addAll(ExternalPack.scan(MinecraftClient.getInstance().getResourcePackDir()));
      Variant.classicGlintPack = (ExternalPack)packs.stream().filter((px) -> px.key.equals(ExternalPack.key("Classic Glint"))).findFirst().orElse(null);
      ExternalPack vanillaPlus = (ExternalPack)packs.stream().filter((px) -> px.key.equals(ExternalPack.key("Vanilla+ 3D Crystal 1.21"))).findFirst().orElse(null);
      Map<Category, List<Variant>> map = new EnumMap(Category.class);

      for(Category c : Category.values()) {
         if (c == Category.TOTEM_SIZE) {
            map.put(c, Variant.SizeVariant.all());
         } else if (c == Category.SKY) {
            List<Variant> sky = new ArrayList();
            sky.add(new Variant.Vanilla("Original"));
            sky.add(new Variant.RecolourVariant(10320895));

            for(Styles.Style s : Styles.ALL) {
               sky.add(new Variant.StyleVariant(s));
            }

            map.put(c, sky);
         } else {
            List<Variant> list = new ArrayList();
            list.add(c == Category.GLINT ? new Variant.Vanilla("Vanilla Glint") : new Variant.Vanilla());
            if (c == Category.CROSSHAIR) {
               for(CrosshairPresets.Preset p : CrosshairPresets.list()) {
                  list.add(new CrosshairPresets.PresetVariant(p));
               }
            }

            List<Variant> packVariants = new ArrayList();
            Set<String> seenKeys = new HashSet();

            for(ExternalPack p : packs) {
               Variant.PackVariant v = Variant.PackVariant.of(p, c);
               if (v != null && (p.userAdded || !PackEditorConfig.isHidden(c, v)) && seenKeys.add(p.key)) {
                  if (c == Category.END_CRYSTAL && vanillaPlus != null && p.key.equals(ExternalPack.key("Aqua+"))) {
                     v.useIconFrom(vanillaPlus, Styles::purpleToCyan);
                  } else if (c == Category.END_CRYSTAL && p.find(List.of("item/end_crystal.png", "items/end_crystal.png")) == null) {
                     v.deriveCrystalIcon();
                  }

                  packVariants.add(v);
               }
            }

            packVariants.sort(Comparator.comparingInt((vx) -> PackEditorConfig.sortKey(c, vx)));
            list.addAll(packVariants);
            if (c.drawable()) {
               for(String name : Drawings.list(c)) {
                  list.add(new Variant.DrawnVariant(c, name));
               }
            }

            if (c != Category.SHIELD) {
               list.add(new Variant.RecolourVariant(10320895));

               for(Styles.Style s : Styles.ALL) {
                  list.add(new Variant.StyleVariant(s));
               }
            }

            list.removeIf((vx) -> !vx.isVanilla() && PackEditorConfig.isDeleted(c, vx.id));
            map.put(c, list);
         }
      }

      return map;
   }

   public static Variant find(List<Variant> list, String id) {
      if (list != null && id != null) {
         for(Variant v : list) {
            if (v.id.equals(id)) {
               return v;
            }
         }

         if (id.startsWith("recolour:") && list.stream().anyMatch((vx) -> vx instanceof Variant.RecolourVariant)) {
            return Variant.RecolourVariant.of(id);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }
}
