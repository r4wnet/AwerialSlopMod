package com.awerial.packeditor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.texture.NativeImage;

public final class SharedLooks {
   public static final char SEPARATOR = '~';
   private static final long MAX_BYTES = 8000000L;
   private static final Map<Integer, String> UPLOADED = new ConcurrentHashMap();

   private SharedLooks() {
   }

   public static boolean needsFiles(String variantId) {
      return variantId != null && (variantId.startsWith("pack:") || variantId.startsWith("drawn:"));
   }

   public static boolean anyNeedFiles(Map<Category, String> selections) {
      return selections.values().stream().anyMatch(SharedLooks::needsFiles);
   }

   public static CompletableFuture<String> upload(Map<Category, String> selections, Map<Category, List<Variant>> variants) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Map<String, List<Category>> bySource = new LinkedHashMap();

            for(Map.Entry<Category, String> e : selections.entrySet()) {
               if (needsFiles((String)e.getValue()) && VariantRegistry.find((List)variants.get(e.getKey()), (String)e.getValue()) != null) {
                  ((List)bySource.computeIfAbsent(String.valueOf(e.getKey()) + "|" + (String)e.getValue(), (k) -> new ArrayList())).add((Category)e.getKey());
               }
            }

            Map<String, List<Category>> groups = new LinkedHashMap();

            for(Map.Entry<String, List<Category>> e : bySource.entrySet()) {
               String id = ((String)e.getKey()).substring(((String)e.getKey()).indexOf(124) + 1);
               ((List)groups.computeIfAbsent(id.startsWith("pack:") ? id : (String)e.getKey(), (k) -> new ArrayList())).addAll((Collection)e.getValue());
            }

            if (groups.isEmpty()) {
               return null;
            } else {
               JsonArray out = new JsonArray();
               long total = 0L;

               for(List<Category> cats : groups.values()) {
                  JsonObject group = new JsonObject();
                  JsonArray catIds = new JsonArray();
                  JsonObject files = new JsonObject();

                  for(Category c : cats) {
                     catIds.add(c.id);
                     Variant v = VariantRegistry.find((List)variants.get(c), (String)selections.get(c));

                     for(Category.Target t : c.targets) {
                        NativeImage img = v.loadImage(t);

                        label145: {
                           try {
                              if (img == null) {
                                 break label145;
                              }

                              byte[] png = png(img);
                              total += (long)png.length;
                              files.addProperty("assets/minecraft/textures/" + t.path(), Base64.getEncoder().encodeToString(png));
                           } catch (Throwable var21) {
                              if (img != null) {
                                 try {
                                    img.close();
                                 } catch (Throwable var20) {
                                    var21.addSuppressed(var20);
                                 }
                              }

                              throw var21;
                           }

                           if (img != null) {
                              img.close();
                           }

                           byte[] meta = v.loadMeta(t);
                           if (meta != null) {
                              files.addProperty("assets/minecraft/textures/" + t.path() + ".mcmeta", Base64.getEncoder().encodeToString(meta));
                           }
                           continue;
                        }

                        if (img != null) {
                           img.close();
                        }
                     }

                     for(String m : c.models) {
                        byte[] model = PackBuilder.withParticle(v.loadModel(m));
                        if (model != null) {
                           files.addProperty("assets/minecraft/" + m, Base64.getEncoder().encodeToString(model));
                        }
                     }
                  }

                  if (total > 8000000L) {
                     throw new IOException("that look is too big to share (over 8 MB of textures)");
                  }

                  group.add("categories", catIds);
                  group.add("files", files);
                  out.add(group);
               }

               JsonObject body = new JsonObject();
               body.add("groups", out);
               String json = body.toString();
               String known = (String)UPLOADED.get(json.hashCode());
               if (known != null) {
                  return known;
               } else {
                  HttpRequest req = HttpRequest.newBuilder(URI.create("https://awerial.awerial.workers.dev/api/looks")).timeout(Duration.ofSeconds(60L)).header("content-type", "application/json").POST(BodyPublishers.ofString(json)).build();
                  HttpResponse<String> r = Stats.HTTP.send(req, BodyHandlers.ofString());
                  JsonObject res = JsonParser.parseString((String)r.body()).getAsJsonObject();
                  if (r.statusCode() == 200 && res.has("id")) {
                     String id = res.get("id").getAsString();
                     UPLOADED.put(json.hashCode(), id);
                     return id;
                  } else {
                     throw new IOException(res.has("error") ? res.get("error").getAsString() : "the website said " + r.statusCode());
                  }
               }
            }
         } catch (IOException e) {
            throw new CompletionException(e);
         } catch (Exception e) {
            throw new CompletionException(new IOException("couldn't reach the website (" + e.getClass().getSimpleName() + ")"));
         }
      });
   }

   private static byte[] png(NativeImage img) throws IOException {
      Path tmp = Files.createTempFile("ape-look", ".png");

      byte[] var2;
      try {
         img.writeTo(tmp);
         var2 = Files.readAllBytes(tmp);
      } finally {
         Files.deleteIfExists(tmp);
      }

      return var2;
   }

   public static CompletableFuture<Map<Category, String>> download(String id) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            if (!id.matches("[a-z0-9]{6,16}")) {
               throw new IOException("that code's look id is broken");
            } else {
               HttpRequest req = HttpRequest.newBuilder(URI.create("https://awerial.awerial.workers.dev/api/looks/" + id)).timeout(Duration.ofSeconds(60L)).build();
               HttpResponse<String> r = Stats.HTTP.send(req, BodyHandlers.ofString());
               if (r.statusCode() == 404) {
                  throw new IOException("that shared look isn't on the website any more");
               } else if (r.statusCode() != 200) {
                  throw new IOException("the website said " + r.statusCode());
               } else {
                  JsonArray groups = JsonParser.parseString((String)r.body()).getAsJsonObject().getAsJsonArray("groups");
                  Map<Category, String> picks = new EnumMap(Category.class);

                  for(int i = 0; i < groups.size(); ++i) {
                     JsonObject g = groups.get(i).getAsJsonObject();
                     String name = "Shared look " + id + (groups.size() > 1 ? " " + (i + 1) : "");
                     Path root = UserPacks.dir().resolve(name);
                     Files.createDirectories(root);
                     Files.writeString(root.resolve("pack.mcmeta"), "{\"pack\":{\"pack_format\":75,\"description\":\"A look shared with an Awerial's Pack Editor code\"}}", StandardCharsets.UTF_8);

                     for(Map.Entry<String, JsonElement> f : g.getAsJsonObject("files").entrySet()) {
                        String path = (String)f.getKey();
                        if (path.matches("assets/minecraft/(textures|models)/[a-z0-9_./-]+\\.(png|json|mcmeta)") && !path.contains("..")) {
                           Path dest = root.resolve(path);
                           Files.createDirectories(dest.getParent());
                           Files.write(dest, Base64.getDecoder().decode(((JsonElement)f.getValue()).getAsString()), new OpenOption[0]);
                        }
                     }

                     for(JsonElement c : g.getAsJsonArray("categories")) {
                        Category cat = Category.byId(c.getAsString());
                        if (cat != null) {
                           picks.put(cat, "pack:" + name);
                        }
                     }
                  }

                  VariantRegistry.invalidate();
                  return picks;
               }
            }
         } catch (IOException e) {
            throw new CompletionException(e);
         } catch (Exception e) {
            throw new CompletionException(new IOException("couldn't download the look (" + e.getClass().getSimpleName() + ")"));
         }
      });
   }
}
