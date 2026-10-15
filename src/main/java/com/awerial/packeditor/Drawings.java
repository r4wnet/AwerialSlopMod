package com.awerial.packeditor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.NativeImage;

public final class Drawings {
   public static final int MAX_NAME = 24;

   private Drawings() {
   }

   static Path dir(Category c) {
      return FabricLoader.getInstance().getConfigDir().resolve("awerialspackeditor").resolve("drawings").resolve(c.id);
   }

   public static String cleanName(String name) {
      String n = name.replaceAll("[^A-Za-z0-9 _-]", "").trim();
      return n.length() > 24 ? n.substring(0, 24).trim() : n;
   }

   public static List<String> list(Category c) {
      List<String> names = new ArrayList();
      Path d = dir(c);
      if (!Files.isDirectory(d, new LinkOption[0])) {
         return names;
      } else {
         try {
            Stream<Path> files = Files.list(d);

            try {
               for(Path f : files.sorted().toList()) {
                  String n = f.getFileName().toString();
                  String base = n.endsWith(".png") ? n.substring(0, n.length() - 4) : null;
                  if (base != null && !base.contains(".")) {
                     names.add(base);
                  }
               }
            } catch (Throwable var9) {
               if (files != null) {
                  try {
                     files.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }
               }

               throw var9;
            }

            if (files != null) {
               files.close();
            }
         } catch (IOException e) {
            PackEditorClient.LOGGER.warn("Could not list drawings", e);
         }

         return names;
      }
   }

   public static NativeImage load(Category c, String name) throws IOException {
      Path f = dir(c).resolve(name + ".png");
      return Files.isRegularFile(f, new LinkOption[0]) ? NativeImage.read(Files.readAllBytes(f)) : null;
   }

   public static void save(Category c, String name, NativeImage img) throws IOException {
      Files.createDirectories(dir(c));
      img.writeTo(dir(c).resolve(name + ".png"));
   }

   public static NativeImage loadWorn(Category c, String name) throws IOException {
      Path f = dir(c).resolve(name + ".worn.png");
      return Files.isRegularFile(f, new LinkOption[0]) ? NativeImage.read(Files.readAllBytes(f)) : null;
   }

   public static void saveWorn(Category c, String name, NativeImage img) throws IOException {
      Path f = dir(c).resolve(name + ".worn.png");
      if (img == null) {
         Files.deleteIfExists(f);
      } else {
         Files.createDirectories(dir(c));
         img.writeTo(f);
      }
   }

   public static int frameTime(Category c, String name) {
      return readFrameTime(dir(c).resolve(name + ".anim"));
   }

   public static void saveFrameTime(Category c, String name, int ticks) throws IOException {
      writeFrameTime(dir(c).resolve(name + ".anim"), ticks);
   }

   static int readFrameTime(Path f) {
      try {
         return Files.isRegularFile(f, new LinkOption[0]) ? Math.max(0, Integer.parseInt(Files.readString(f).trim())) : 0;
      } catch (Exception var2) {
         return 0;
      }
   }

   static void writeFrameTime(Path f, int ticks) throws IOException {
      if (ticks <= 0) {
         Files.deleteIfExists(f);
      } else {
         Files.createDirectories(f.getParent());
         Files.writeString(f, Integer.toString(ticks));
      }
   }

   public static byte[] animationMeta(int ticks) {
      return ("{\n  \"animation\": { \"frametime\": " + ticks + " }\n}\n").getBytes(StandardCharsets.UTF_8);
   }

   public static NativeImage loadPart(Category c, String name, String part) throws IOException {
      Path f = dir(c).resolve(name + "." + part + ".png");
      return Files.isRegularFile(f, new LinkOption[0]) ? NativeImage.read(Files.readAllBytes(f)) : null;
   }

   public static void savePart(Category c, String name, String part, NativeImage img) throws IOException {
      Files.createDirectories(dir(c));
      img.writeTo(dir(c).resolve(name + "." + part + ".png"));
   }

   public static void delete(Category c, String name) throws IOException {
      Files.deleteIfExists(dir(c).resolve(name + ".png"));
      if (Files.isDirectory(dir(c), new LinkOption[0])) {
         Stream<Path> files = Files.list(dir(c));

         try {
            for(Path f : files.toList()) {
               if (f.getFileName().toString().startsWith(name + ".")) {
                  Files.deleteIfExists(f);
               }
            }
         } catch (Throwable var6) {
            if (files != null) {
               try {
                  files.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (files != null) {
            files.close();
         }

      }
   }
}
