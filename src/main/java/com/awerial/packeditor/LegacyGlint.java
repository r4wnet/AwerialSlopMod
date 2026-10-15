package com.awerial.packeditor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

public final class LegacyGlint {
   private static final String ENTRY = "assets/minecraft/textures/misc/enchanted_item_glint.png";
   private static final String CLIENT_JAR = "https://piston-data.mojang.com/v1/objects/055b30d860ead928cba3849ba920c88b6950b654/client.jar";
   private static final String[] LOCAL_VERSIONS = new String[]{"1.19.2", "1.19.1", "1.19", "1.19.3", "1.18.2"};
   private static volatile byte[] png;
   private static volatile Thread loading;
   private static final HttpClient HTTP;

   private LegacyGlint() {
   }

   private static Path cacheFile() {
      return FabricLoader.getInstance().getConfigDir().resolve("awerialspackeditor").resolve("legacy_glint.png");
   }

   public static byte[] get() {
      byte[] b = png;
      if (b != null) {
         return b;
      } else {
         try {
            Path cache = cacheFile();
            if (Files.isRegularFile(cache, new LinkOption[0])) {
               return png = Files.readAllBytes(cache);
            }
         } catch (IOException var2) {
         }

         b = fromLocalJar();
         if (b != null) {
            save(b);
         }

         return b;
      }
   }

   public static boolean ready() {
      return get() != null;
   }

   public static synchronized void prepare() {
      if (!ready() && (loading == null || !loading.isAlive())) {
         loading = new Thread(LegacyGlint::download, "APE legacy glint");
         loading.setDaemon(true);
         loading.start();
      }
   }

   public static byte[] await(long ms) {
      prepare();
      Thread t = loading;
      if (t != null) {
         try {
            t.join(ms);
         } catch (InterruptedException var4) {
            Thread.currentThread().interrupt();
         }
      }

      return png;
   }

   private static byte[] fromLocalJar() {
      Path versions = MinecraftClient.getInstance().runDirectory.toPath().resolve("versions");

      for(String v : LOCAL_VERSIONS) {
         Path jar = versions.resolve(v).resolve(v + ".jar");
         if (Files.isRegularFile(jar, new LinkOption[0])) {
            try {
               ZipFile zip = new ZipFile(jar.toFile());

               byte[] var9;
               label63: {
                  try {
                     ZipEntry e = zip.getEntry("assets/minecraft/textures/misc/enchanted_item_glint.png");
                     if (e != null) {
                        InputStream in = zip.getInputStream(e);

                        try {
                           var9 = in.readAllBytes();
                        } catch (Throwable var13) {
                           if (in != null) {
                              try {
                                 in.close();
                              } catch (Throwable var12) {
                                 var13.addSuppressed(var12);
                              }
                           }

                           throw var13;
                        }

                        if (in != null) {
                           in.close();
                        }
                        break label63;
                     }
                  } catch (Throwable var14) {
                     try {
                        zip.close();
                     } catch (Throwable var11) {
                        var14.addSuppressed(var11);
                     }

                     throw var14;
                  }

                  zip.close();
                  continue;
               }

               zip.close();
               return var9;
            } catch (IOException var15) {
            }
         }
      }

      return null;
   }

   private static void save(byte[] b) {
      png = b;

      try {
         Files.createDirectories(cacheFile().getParent());
         Files.write(cacheFile(), b, new OpenOption[0]);
      } catch (IOException e) {
         PackEditorClient.LOGGER.warn("Couldn't save the legacy glint: {}", e.toString());
      }

   }

   private static void download() {
      try {
         byte[] b = fetchEntry("https://piston-data.mojang.com/v1/objects/055b30d860ead928cba3849ba920c88b6950b654/client.jar", "assets/minecraft/textures/misc/enchanted_item_glint.png");
         if (b == null || b.length < 8 || b[1] != 80 || b[2] != 78 || b[3] != 71) {
            throw new IOException("not a png");
         }

         save(b);
         PackEditorClient.LOGGER.info("Got the 1.19 enchantment glint from Mojang ({} bytes)", b.length);
      } catch (Exception e) {
         PackEditorClient.LOGGER.warn("Couldn't get the 1.19 glint: {}", e.toString());
      }

   }

   private static byte[] range(String url, String range) throws IOException, InterruptedException {
      HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30L)).header("Range", "bytes=" + range).build();
      HttpResponse<byte[]> r = HTTP.send(req, BodyHandlers.ofByteArray());
      if (r.statusCode() != 206) {
         throw new IOException("HTTP " + r.statusCode());
      } else {
         return (byte[])r.body();
      }
   }

   private static byte[] fetchEntry(String url, String name) throws IOException, InterruptedException {
      byte[] tail = range(url, "-65557");
      ByteBuffer t = ByteBuffer.wrap(tail).order(ByteOrder.LITTLE_ENDIAN);
      int eocd = -1;

      for(int i = tail.length - 22; i >= 0; --i) {
         if (t.getInt(i) == 101010256) {
            eocd = i;
            break;
         }
      }

      if (eocd < 0) {
         throw new IOException("no zip directory");
      } else {
         long cdSize = Integer.toUnsignedLong(t.getInt(eocd + 12));
         long cdOffset = Integer.toUnsignedLong(t.getInt(eocd + 16));
         ByteBuffer cd = ByteBuffer.wrap(range(url, cdOffset + "-" + (cdOffset + cdSize - 1L))).order(ByteOrder.LITTLE_ENDIAN);
         byte[] want = name.getBytes(StandardCharsets.UTF_8);

         int nameLen;
         int extraLen;
         int commentLen;
         for(int p = 0; p + 46 <= cd.limit() && cd.getInt(p) == 33639248; p += 46 + nameLen + extraLen + commentLen) {
            int method = Short.toUnsignedInt(cd.getShort(p + 10));
            long compSize = Integer.toUnsignedLong(cd.getInt(p + 20));
            long size = Integer.toUnsignedLong(cd.getInt(p + 24));
            nameLen = Short.toUnsignedInt(cd.getShort(p + 28));
            extraLen = Short.toUnsignedInt(cd.getShort(p + 30));
            commentLen = Short.toUnsignedInt(cd.getShort(p + 32));
            long local = Integer.toUnsignedLong(cd.getInt(p + 42));
            if (nameLen == want.length && Arrays.equals(cd.array(), p + 46, p + 46 + nameLen, want, 0, want.length)) {
               ByteBuffer lh = ByteBuffer.wrap(range(url, local + "-" + (local + 29L))).order(ByteOrder.LITTLE_ENDIAN);
               long data = local + 30L + (long)Short.toUnsignedInt(lh.getShort(26)) + (long)Short.toUnsignedInt(lh.getShort(28));
               byte[] raw = range(url, data + "-" + (data + compSize - 1L));
               if (method == 0) {
                  return raw;
               }

               Inflater inf = new Inflater(true);
               inf.setInput(raw);
               ByteArrayOutputStream out = new ByteArrayOutputStream((int)size);
               byte[] buf = new byte[8192];

               try {
                  while(!inf.finished()) {
                     int n = inf.inflate(buf);
                     if (n == 0 && (inf.needsInput() || inf.needsDictionary())) {
                        break;
                     }

                     out.write(buf, 0, n);
                  }
               } catch (DataFormatException e) {
                  throw new IOException(e);
               } finally {
                  inf.end();
               }

               return out.toByteArray();
            }
         }

         throw new IOException(name + " isn't in that jar");
      }
   }

   static {
      HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).followRedirects(Redirect.NORMAL).build();
   }
}
