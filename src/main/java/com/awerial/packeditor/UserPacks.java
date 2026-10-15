package com.awerial.packeditor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipFile;
import net.fabricmc.loader.api.FabricLoader;

public final class UserPacks {
   private UserPacks() {
   }

   public static Path dir() {
      return FabricLoader.getInstance().getConfigDir().resolve("awerialspackeditor").resolve("packs");
   }

   public static List<ExternalPack> scan() {
      List<ExternalPack> packs = ExternalPack.scan(dir());
      packs.forEach((p) -> p.userAdded = true);
      return packs;
   }

   public static String add(Path source) throws IOException {
      if (Files.isDirectory(source, new LinkOption[0]) && !Files.isRegularFile(source.resolve("pack.mcmeta"), new LinkOption[0])) {
         Stream<Path> kids = Files.list(source);

         try {
            List<Path> packs = kids.filter((k) -> Files.isRegularFile(k.resolve("pack.mcmeta"), new LinkOption[0])).toList();
            if (packs.size() == 1) {
               source = (Path)packs.get(0);
            }
         } catch (Throwable var13) {
            if (kids != null) {
               try {
                  kids.close();
               } catch (Throwable var9) {
                  var13.addSuppressed(var9);
               }
            }

            throw var13;
         }

         if (kids != null) {
            kids.close();
         }
      }

      String name = source.getFileName().toString();
      Files.createDirectories(dir());
      Path dest = dir().resolve(name);
      if (Files.isDirectory(source, new LinkOption[0])) {
         if (!Files.isRegularFile(source.resolve("pack.mcmeta"), new LinkOption[0])) {
            throw new IOException("'" + name + "' isn't a resource pack (no pack.mcmeta inside)");
         } else {
            deleteRecursively(dest);
            Stream<Path> walk = Files.walk(source);

            try {
               for(Path p : walk.toList()) {
                  Path target = dest.resolve(source.relativize(p).toString());
                  if (Files.isDirectory(p, new LinkOption[0])) {
                     Files.createDirectories(target);
                  } else {
                     Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING);
                  }
               }
            } catch (Throwable var10) {
               if (walk != null) {
                  try {
                     walk.close();
                  } catch (Throwable var8) {
                     var10.addSuppressed(var8);
                  }
               }

               throw var10;
            }

            if (walk != null) {
               walk.close();
            }

            return name;
         }
      } else if (!name.toLowerCase().endsWith(".zip")) {
         throw new IOException("'" + name + "' isn't a .zip resource pack");
      } else {
         try {
            ZipFile zip = new ZipFile(source.toFile());

            try {
               boolean hasMeta = zip.stream().anyMatch((ex) -> ex.getName().replace('\\', '/').endsWith("pack.mcmeta"));
               if (!hasMeta) {
                  throw new IOException("'" + name + "' isn't a resource pack (no pack.mcmeta)");
               }
            } catch (Throwable var11) {
               try {
                  zip.close();
               } catch (Throwable var7) {
                  var11.addSuppressed(var7);
               }

               throw var11;
            }

            zip.close();
         } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("'")) {
               throw e;
            }

            throw new IOException("'" + name + "' couldn't be opened as a .zip");
         }

         Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
         return name;
      }
   }

   public static void remove(String fileName) throws IOException {
      deleteRecursively(dir().resolve(fileName));
   }

   private static void deleteRecursively(Path p) throws IOException {
      if (Files.exists(p, new LinkOption[0])) {
         Stream<Path> walk = Files.walk(p);

         try {
            for(Path q : walk.sorted(Comparator.reverseOrder()).toList()) {
               Files.delete(q);
            }
         } catch (Throwable var5) {
            if (walk != null) {
               try {
                  walk.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (walk != null) {
            walk.close();
         }

      }
   }
}
