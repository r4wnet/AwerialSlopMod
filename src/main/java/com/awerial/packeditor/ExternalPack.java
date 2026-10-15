package com.awerial.packeditor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.fabricmc.loader.api.FabricLoader;

public final class ExternalPack {
   public final String fileName;
   public final String displayName;
   public final String key;
   private final Path path;
   private final String prefix;
   private final Map<String, String> zipEntries;
   public final boolean bundled;
   public boolean userAdded;
   private static final Map<Path, Cached> ZIP_CACHE = new ConcurrentHashMap();

   private ExternalPack(Path path, String prefix, Map<String, String> zipEntries) {
      this(path, path.getFileName().toString(), prefix, zipEntries, false);
   }

   private ExternalPack(Path path, String name, String prefix, Map<String, String> zipEntries, boolean bundled) {
      this.bundled = bundled;
      this.path = path;
      this.fileName = name;
      this.displayName = cleanName(this.fileName);
      this.key = key(this.fileName);
      this.prefix = prefix;
      this.zipEntries = zipEntries;
   }

   public static String key(String name) {
      String n = name.replaceAll("[§õ].", "").toLowerCase(Locale.ROOT).trim();
      if (n.endsWith(".zip")) {
         n = n.substring(0, n.length() - 4);
      }

      StringBuilder sb = new StringBuilder();
      IntStream var10000 = n.codePoints().filter(Character::isLetterOrDigit);
      Objects.requireNonNull(sb);
      var10000.forEach(sb::appendCodePoint);
      return sb.toString();
   }

   public static List<ExternalPack> scan(Path dir) {
      List<ExternalPack> packs = new ArrayList();
      if (!Files.isDirectory(dir, new LinkOption[0])) {
         return packs;
      } else {
         try {
            Stream<Path> children = Files.list(dir);

            try {
               for(Path child : children.sorted().toList()) {
                  String name = child.getFileName().toString();
                  if (!name.equals("AwerialsPackEditor")) {
                     try {
                        if (Files.isDirectory(child, new LinkOption[0]) && Files.isRegularFile(child.resolve("pack.mcmeta"), new LinkOption[0])) {
                           packs.add(new ExternalPack(child, "", (Map)null));
                        } else if (Files.isRegularFile(child, new LinkOption[0]) && name.toLowerCase().endsWith(".zip")) {
                           ExternalPack p = openZip(child);
                           if (p != null) {
                              packs.add(p);
                           }
                        }
                     } catch (Exception e) {
                        PackEditorClient.LOGGER.warn("Skipping unreadable pack {}: {}", name, e.toString());
                     }
                  }
               }
            } catch (Throwable var9) {
               if (children != null) {
                  try {
                     children.close();
                  } catch (Throwable var7) {
                     var9.addSuppressed(var7);
                  }
               }

               throw var9;
            }

            if (children != null) {
               children.close();
            }
         } catch (IOException e) {
            PackEditorClient.LOGGER.warn("Could not list resource packs", e);
         }

         return packs;
      }
   }

   public static List<ExternalPack> bundled() {
      List<ExternalPack> packs = new ArrayList();
      FabricLoader.getInstance().getModContainer("awerialspackeditor").flatMap((mod) -> mod.findPath("assets/awerialspackeditor/bundled")).ifPresent((root) -> {
         try {
            Stream<Path> children = Files.list(root);

            try {
               for(Path dir : children.sorted().toList()) {
                  if (Files.isDirectory(dir, new LinkOption[0])) {
                     String name = dir.getFileName().toString().replace("/", "");
                     packs.add(new ExternalPack(dir, name, "", (Map)null, true));
                  }
               }
            } catch (Throwable var7) {
               if (children != null) {
                  try {
                     children.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (children != null) {
               children.close();
            }
         } catch (IOException e) {
            PackEditorClient.LOGGER.warn("Could not list bundled packs", e);
         }

      });
      return packs;
   }

   private static ExternalPack openZip(Path zip) throws IOException {
      long modified = Files.getLastModifiedTime(zip).toMillis();
      long size = Files.size(zip);
      Cached c = (Cached)ZIP_CACHE.get(zip);
      if (c != null && c.modified == modified && c.size == size) {
         return c.pack;
      } else {
         ExternalPack p = readZip(zip);
         ZIP_CACHE.put(zip, new Cached(modified, size, p));
         return p;
      }
   }

   private static ExternalPack readZip(Path zip) throws IOException {
      Map<String, String> entries = new HashMap();
      String prefix = null;
      ZipFile zf = new ZipFile(zip.toFile());

      try {
         Enumeration<? extends ZipEntry> en = zf.entries();

         while(en.hasMoreElements()) {
            String raw = ((ZipEntry)en.nextElement()).getName();
            String n = raw.replace('\\', '/');
            entries.put(n, raw);
            if (n.endsWith("pack.mcmeta") && (n.equals("pack.mcmeta") || n.endsWith("/pack.mcmeta"))) {
               String pre = n.substring(0, n.length() - "pack.mcmeta".length());
               if (prefix == null || pre.length() < prefix.length()) {
                  prefix = pre;
               }
            }
         }
      } catch (Throwable var9) {
         try {
            zf.close();
         } catch (Throwable var8) {
            var9.addSuppressed(var8);
         }

         throw var9;
      }

      zf.close();
      return prefix == null ? null : new ExternalPack(zip, prefix, entries);
   }

   public String find(List<String> texturePaths) {
      for(String p : texturePaths) {
         if (this.has(rel(p))) {
            return p;
         }
      }

      return null;
   }

   public byte[] readTexture(String texturePath) throws IOException {
      return this.read(rel(texturePath));
   }

   public byte[] readTextureMeta(String texturePath) throws IOException {
      String r = rel(texturePath) + ".mcmeta";
      return this.has(r) ? this.read(r) : null;
   }

   public List<String> list(Predicate<String> match) {
      List<String> out = new ArrayList();
      if (this.zipEntries != null) {
         for(String n : this.zipEntries.keySet()) {
            if (n.startsWith(this.prefix) && !n.endsWith("/")) {
               String rel = n.substring(this.prefix.length());
               if (match.test(rel)) {
                  out.add(rel);
               }
            }
         }

         return out;
      } else {
         try {
            Stream<Path> walk = Files.walk(this.path);

            try {
               for(Path p : walk.filter((x$0) -> Files.isRegularFile(x$0, new LinkOption[0])).toList()) {
                  String rel = this.path.relativize(p).toString().replace('\\', '/');
                  if (match.test(rel)) {
                     out.add(rel);
                  }
               }
            } catch (Throwable var8) {
               if (walk != null) {
                  try {
                     walk.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (walk != null) {
               walk.close();
            }
         } catch (IOException e) {
            PackEditorClient.LOGGER.warn("Could not list {}", this.fileName, e);
         }

         return out;
      }
   }

   public byte[] readFile(String rel) throws IOException {
      return this.has(rel) ? this.read(rel) : null;
   }

   public byte[] readAsset(String assetPath) throws IOException {
      String r = "assets/minecraft/" + assetPath;
      return this.has(r) ? this.read(r) : null;
   }

   private static String rel(String texturePath) {
      return "assets/minecraft/textures/" + texturePath;
   }

   private boolean has(String rel) {
      return this.zipEntries != null ? this.zipEntries.containsKey(this.prefix + rel) : Files.isRegularFile(this.path.resolve(rel), new LinkOption[0]);
   }

   private byte[] read(String rel) throws IOException {
      if (this.zipEntries == null) {
         return Files.readAllBytes(this.path.resolve(rel));
      } else {
         try (ZipFile zf = new ZipFile(this.path.toFile())) {
            String raw = this.zipEntries.get(this.prefix + rel);
            ZipEntry e = raw == null ? null : zf.getEntry(raw);
            if (e == null) {
               return null;
            }

            try (InputStream in = zf.getInputStream(e)) {
               return in.readAllBytes();
            }
         }
      }
   }

   private static String cleanName(String fileName) {
      String n = fileName;
      if (fileName.toLowerCase().endsWith(".zip")) {
         n = fileName.substring(0, fileName.length() - 4);
      }

      for(n = n.replaceAll("[§õ].", "").replace('_', ' ').trim(); n.startsWith("!"); n = n.substring(1).trim()) {
      }

      return n.isEmpty() ? fileName : n;
   }

   private static record Cached(long modified, long size, ExternalPack pack) {
   }
}
