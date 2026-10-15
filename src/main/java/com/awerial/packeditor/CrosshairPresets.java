package com.awerial.packeditor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.NativeImage;

public final class CrosshairPresets {
   private static List<Preset> cached;
   private static volatile boolean refreshing;

   private CrosshairPresets() {
   }

   private static Path dir() {
      return FabricLoader.getInstance().getConfigDir().resolve("awerialspackeditor").resolve("presets").resolve("crosshair");
   }

   public static synchronized List<Preset> list() {
      if (cached != null) {
         return cached;
      } else {
         List<Preset> out = new ArrayList();

         try {
            Path index = dir().resolve("index.json");
            if (Files.isRegularFile(index, new LinkOption[0])) {
               for(JsonElement e : JsonParser.parseString(Files.readString(index, StandardCharsets.UTF_8)).getAsJsonArray()) {
                  JsonObject o = e.getAsJsonObject();
                  Preset p = new Preset(o.get("id").getAsInt(), o.get("name").getAsString(), o.get("updated").getAsLong());
                  if (Files.isRegularFile(image(p.id), new LinkOption[0])) {
                     out.add(p);
                  }
               }
            }
         } catch (Exception e) {
            PackEditorClient.LOGGER.warn("Couldn't read crosshair presets: {}", e.toString());
         }

         cached = out;
         return out;
      }
   }

   static Path image(int id) {
      return dir().resolve(id + ".png");
   }

   public static void refresh() {
      if (!"https://awerial.awerial.workers.dev".isEmpty() && !refreshing) {
         refreshing = true;
         Thread t = new Thread(() -> {
            try {
               HttpRequest req = HttpRequest.newBuilder(URI.create("https://awerial.awerial.workers.dev/api/crosshairs")).timeout(Duration.ofSeconds(10L)).build();
               HttpResponse<String> r = Stats.HTTP.send(req, BodyHandlers.ofString());
               if (r.statusCode() == 200) {
                  JsonArray arr = JsonParser.parseString((String)r.body()).getAsJsonArray();
                  Files.createDirectories(dir());
                  Set<Integer> keep = new HashSet();
                  List<Preset> old = list();
                  boolean changed = old.size() != arr.size();

                  for(JsonElement e : arr) {
                     JsonObject o = e.getAsJsonObject();
                     Preset p = new Preset(o.get("id").getAsInt(), o.get("name").getAsString(), o.get("updated").getAsLong());
                     keep.add(p.id);
                     if (!old.contains(p) || !Files.isRegularFile(image(p.id), new LinkOption[0])) {
                        HttpRequest img = HttpRequest.newBuilder(URI.create("https://awerial.awerial.workers.dev/api/crosshairs/img/" + p.id)).timeout(Duration.ofSeconds(10L)).build();
                        byte[] png = (byte[])Stats.HTTP.send(img, BodyHandlers.ofByteArray()).body();
                        NativeImage check = NativeImage.read(png);

                        label219: {
                           try {
                              if (check.getWidth() <= 64 && check.getHeight() <= 64) {
                                 break label219;
                              }
                           } catch (Throwable var28) {
                              if (check != null) {
                                 try {
                                    check.close();
                                 } catch (Throwable var25) {
                                    var28.addSuppressed(var25);
                                 }
                              }

                              throw var28;
                           }

                           if (check != null) {
                              check.close();
                           }
                           continue;
                        }

                        if (check != null) {
                           check.close();
                        }

                        Files.write(image(p.id), png, new OpenOption[0]);
                        changed = true;
                     }
                  }

                  Stream<Path> files = Files.list(dir());

                  try {
                     for(Path f : files.toList()) {
                        String n = f.getFileName().toString();
                        if (n.endsWith(".png") && !keep.contains(Integer.parseInt(n.substring(0, n.length() - 4)))) {
                           Files.delete(f);
                           changed = true;
                        }
                     }
                  } catch (Throwable var27) {
                     if (files != null) {
                        try {
                           files.close();
                        } catch (Throwable var24) {
                           var27.addSuppressed(var24);
                        }
                     }

                     throw var27;
                  }

                  if (files != null) {
                     files.close();
                  }

                  Files.writeString(dir().resolve("index.json"), arr.toString(), StandardCharsets.UTF_8);
                  if (changed) {
                     synchronized(CrosshairPresets.class) {
                        cached = null;
                     }

                     VariantRegistry.invalidate();
                     PackEditorClient.LOGGER.info("Crosshair presets updated ({})", arr.size());
                  }

                  return;
               }
            } catch (Exception e) {
               PackEditorClient.LOGGER.debug("Crosshair presets: {}", e.toString());
               return;
            } finally {
               refreshing = false;
            }

         }, "APE crosshair presets");
         t.setDaemon(true);
         t.start();
      }
   }

   public static record Preset(int id, String name, long updated) {
   }

   public static final class PresetVariant extends Variant {
      private final int presetId;

      public PresetVariant(Preset p) {
         super("preset:" + p.id(), p.name(), "Awerial's preset");
         this.presetId = p.id();
      }

      public NativeImage loadImage(Category.Target target) throws IOException {
         Path f = CrosshairPresets.image(this.presetId);
         return Files.isRegularFile(f, new LinkOption[0]) ? NativeImage.read(Files.readAllBytes(f)) : null;
      }

      public byte[] loadMeta(Category.Target target) {
         return null;
      }
   }
}
