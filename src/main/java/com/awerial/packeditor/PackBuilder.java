package com.awerial.packeditor;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourcePackManager;

public final class PackBuilder {
   public static final String FOLDER = "AwerialsPackEditor";
   public static final int VANILLA_SHIELD_COLOR = 8743230;
   private static final String PACK_ID = "file/AwerialsPackEditor";
   public static final String DISABLED_SHIELD = "entity/shield/awerial_disabled.png";
   public static final String DISABLED_SHIELD_NOPATTERN = "entity/shield/awerial_disabled_nopattern.png";
   public static final float LOW_FIRE_HEIGHT = 0.2F;

   private PackBuilder() {
   }

   public static int apply(Map<Category, String> selections, Map<Category, List<Variant>> variants, Map<Toggle, Boolean> toggles, int shieldColor, int disabledShieldColor, int cobwebOpacity) throws IOException {
      MinecraftClient client = MinecraftClient.getInstance();
      Path root = client.getResourcePackDir().resolve("AwerialsPackEditor");
      deleteRecursively(root.resolve("assets"));
      Files.createDirectories(root);
      writeMcmeta(root);
      writeIcon(root);
      Path textures = root.resolve("assets/minecraft/textures");
      int written = 0;
      Map<String, List<RegionJob>> regionJobs = new LinkedHashMap();
      boolean legacy = (Boolean)toggles.getOrDefault(Toggle.LEGACY_GLINT, false) && (Variant.legacyGlintAvailable() || LegacyGlint.await(8000L) != null);
      Variant.legacyGlint = legacy;

      for(Category c : Category.values()) {
         String id = (String)selections.getOrDefault(c, "vanilla");
         Variant v = VariantRegistry.find((List)variants.get(c), id);
         boolean legacyVanilla = legacy && c == Category.GLINT && (v == null || v.isVanilla());
         if (legacyVanilla) {
            v = new Variant.Vanilla();
         }

         if (v != null && (!v.isVanilla() || legacyVanilla)) {
            for(Category.Target t : c.targets) {
               if (t.region() != null) {
                  ((List)regionJobs.computeIfAbsent(t.path(), (k) -> new ArrayList())).add(new RegionJob(v, t));
               } else {
                  boolean still = c == Category.ENDER_PEARL && (Boolean)toggles.getOrDefault(Toggle.STILL_PEARLS, false) || c == Category.FIRE && (Boolean)toggles.getOrDefault(Toggle.STILL_FIRE, false);

                  try {
                     NativeImage img = still ? firstFrame(v.loadImage(t)) : v.loadImage(t);

                     label246: {
                        try {
                           if (img != null) {
                              Path dest = textures.resolve(t.path());
                              Files.createDirectories(dest.getParent());
                              img.writeTo(dest);
                              byte[] meta = still ? null : v.loadMeta(t);
                              if (meta != null) {
                                 Files.write(dest.resolveSibling(String.valueOf(dest.getFileName()) + ".mcmeta"), meta, new OpenOption[0]);
                              }

                              ++written;
                              break label246;
                           }
                        } catch (Throwable var32) {
                           if (img != null) {
                              try {
                                 img.close();
                              } catch (Throwable var31) {
                                 var32.addSuppressed(var31);
                              }
                           }

                           throw var32;
                        }

                        if (img != null) {
                           img.close();
                        }
                        continue;
                     }

                     if (img != null) {
                        img.close();
                     }
                  } catch (IOException e) {
                     PackEditorClient.LOGGER.warn("Failed to write {} for {}: {}", new Object[]{t.path(), v.name, e.toString()});
                  }
               }
            }

            for(String model : c.models) {
               try {
                  byte[] bytes = withParticle(v.loadModel(model));
                  if (bytes != null) {
                     Path dest = root.resolve("assets/minecraft").resolve(model);
                     Files.createDirectories(dest.getParent());
                     Files.write(dest, bytes, new OpenOption[0]);
                     ++written;
                  }
               } catch (IOException e) {
                  PackEditorClient.LOGGER.warn("Failed to write {} for {}: {}", new Object[]{model, v.name, e.toString()});
               }
            }
         }
      }

      Variant shield = VariantRegistry.find((List)variants.get(Category.SHIELD), (String)selections.getOrDefault(Category.SHIELD, "vanilla"));
      boolean packShield = shield instanceof Variant.PackVariant;
      int tint = shieldColor >= 0 ? shieldColor : (packShield ? 8743230 : -1);
      Variant shieldLook = (Variant)(shield != null ? shield : new Variant.Vanilla());
      if (tint >= 0) {
         written += writeTintedShield(textures, shieldLook, tint, (String[])null);
      }

      if (disabledShieldColor >= 0) {
         written += writeTintedShield(textures, shieldLook, disabledShieldColor, new String[]{"entity/shield/awerial_disabled_nopattern.png", "entity/shield/awerial_disabled.png"});
      }

      boolean stillFire = (Boolean)toggles.getOrDefault(Toggle.STILL_FIRE, false);
      if ((Boolean)toggles.getOrDefault(Toggle.LOW_FIRE, false) || stillFire) {
         Variant fire = VariantRegistry.find((List)variants.get(Category.FIRE), (String)selections.getOrDefault(Category.FIRE, "vanilla"));
         written += writeLowFire(textures, (Variant)(fire != null ? fire : new Variant.Vanilla()), (Boolean)toggles.getOrDefault(Toggle.LOW_FIRE, false), stillFire);
      }

      Variant glowstone = VariantRegistry.find((List)variants.get(Category.GLOWSTONE), (String)selections.getOrDefault(Category.GLOWSTONE, "vanilla"));
      if (glowstone != null && !glowstone.isVanilla()) {
         written += writeGlowstoneDust(textures, glowstone);
      }

      if ((Boolean)toggles.getOrDefault(Toggle.NO_EXPLOSION_PARTICLES, false)) {
         written += writeNoExplosionParticles(textures);
      }

      if (cobwebOpacity < 100) {
         Variant web = VariantRegistry.find((List)variants.get(Category.COBWEB), (String)selections.getOrDefault(Category.COBWEB, "vanilla"));
         written += writeCobwebOpacity(textures, (Variant)(web != null ? web : new Variant.Vanilla()), cobwebOpacity);
      }

      Variant sky = VariantRegistry.find((List)variants.get(Category.SKY), (String)selections.getOrDefault(Category.SKY, "vanilla"));
      IntUnaryOperator skyOp = SkyTint.opFor(sky);
      if (skyOp != null) {
         written += SkyTint.write(root, skyOp);
      }

      try {
         byte[] vsh = Variant.readVanillaAsset("shaders/core/entity.vsh");
         String patched = DamageTint.patchShader(vsh == null ? null : new String(vsh, StandardCharsets.UTF_8));
         if (patched != null) {
            Path dest = root.resolve("assets/minecraft/shaders/core/entity.vsh");
            Files.createDirectories(dest.getParent());
            Files.writeString(dest, patched);
            ++written;
         }
      } catch (IOException e) {
         PackEditorClient.LOGGER.warn("Couldn't write the gradient shader: {}", e.toString());
      }

      Path itemModels = root.resolve("assets/minecraft/models/item");
      writeGappleModels(itemModels, (Boolean)toggles.getOrDefault(Toggle.SMALLER_GAPS, false));
      if ((Boolean)toggles.getOrDefault(Toggle.SMALLER_PEARLS, false)) {
         written += writeSmallPearl(itemModels);
      }

      writeShieldModels(itemModels, (Boolean)toggles.getOrDefault(Toggle.SMALL_SHIELD, false), packShield);
      written += 4;

      for(String id : ItemTextures.drawn()) {
         ItemTextures.Entry item = ItemTextures.get(id);
         if (item != null) {
            try {
               Path dest = textures.resolve(item.texture());
               Files.createDirectories(dest.getParent());
               Files.copy(ItemTextures.file(id), dest, StandardCopyOption.REPLACE_EXISTING);
               Path meta = dest.resolveSibling(String.valueOf(dest.getFileName()) + ".mcmeta");
               int frameTime = ItemTextures.frameTime(id);
               if (frameTime > 0) {
                  Files.write(meta, Drawings.animationMeta(frameTime), new OpenOption[0]);
               } else {
                  Files.deleteIfExists(meta);
               }

               ++written;
            } catch (IOException e) {
               PackEditorClient.LOGGER.warn("Failed to write drawn item {}: {}", id, e.toString());
            }

            Category.Target worn = WornArmour.wornTarget(id);
            if (worn != null) {
               ((List)regionJobs.computeIfAbsent(worn.path(), (k) -> new ArrayList())).add(new RegionJob(ItemTextures.wornVariant(id, worn), worn));
            }
         }
      }

      for(Map.Entry<String, List<RegionJob>> e : regionJobs.entrySet()) {
         try {
            if (writeComposite(textures.resolve((String)e.getKey()), (String)e.getKey(), (List)e.getValue())) {
               ++written;
            }
         } catch (IOException ex) {
            PackEditorClient.LOGGER.warn("Failed to build {}: {}", e.getKey(), ex.toString());
         }
      }

      enableAndReload(client);
      return written;
   }

   private static void writeGappleModels(Path dir, boolean small) throws IOException {
      String display = !small ? "" : ",\n  \"display\": {\n    \"firstperson_righthand\": { \"rotation\": [0, -90, 25], \"translation\": [1.13, 2, 1.13], \"scale\": [0.4, 0.4, 0.4] },\n    \"firstperson_lefthand\": { \"rotation\": [0, 90, -25], \"translation\": [1.13, 2, 1.13], \"scale\": [0.4, 0.4, 0.4] }\n  }";
      String json = "{\n  \"parent\": \"minecraft:item/generated\",\n  \"textures\": { \"layer0\": \"minecraft:item/golden_apple\" }" + display + "\n}\n";
      Files.createDirectories(dir);
      Files.writeString(dir.resolve("golden_apple.json"), json);
      Files.writeString(dir.resolve("enchanted_golden_apple.json"), json);
   }

   private static int writeSmallPearl(Path dir) throws IOException {
      String json = "{\n  \"parent\": \"minecraft:item/generated\",\n  \"textures\": { \"layer0\": \"minecraft:item/ender_pearl\" },\n  \"display\": {\n    \"firstperson_righthand\": { \"rotation\": [0, -90, 25], \"translation\": [1.13, 2, 1.13], \"scale\": [0.4, 0.4, 0.4] },\n    \"firstperson_lefthand\": { \"rotation\": [0, 90, -25], \"translation\": [1.13, 2, 1.13], \"scale\": [0.4, 0.4, 0.4] }\n  }\n}\n";
      Files.createDirectories(dir);
      Files.writeString(dir.resolve("ender_pearl.json"), json);
      return 1;
   }

   private static int writeTintedShield(Path textures, Variant shield, int color, String[] destPaths) {
      int r = color >> 16 & 255;
      int g = color >> 8 & 255;
      int b = color & 255;
      int n = 0;

      for(int ti = 0; ti < Category.SHIELD.targets.size(); ++ti) {
         Category.Target t = (Category.Target)Category.SHIELD.targets.get(ti);
         String destPath = destPaths != null ? destPaths[ti] : t.path();

         try {
            NativeImage img = shield.loadImage(t);

            label88: {
               try {
                  if (img == null) {
                     break label88;
                  }

                  float maxL = 1.0F;

                  for(int y = 0; y < img.getHeight(); ++y) {
                     for(int x = 0; x < img.getWidth(); ++x) {
                        int c = img.getColorArgb(x, y);
                        if (c >>> 24 != 0) {
                           maxL = Math.max(maxL, luminance(c));
                        }
                     }
                  }

                  for(int y = 0; y < img.getHeight(); ++y) {
                     for(int x = 0; x < img.getWidth(); ++x) {
                        int c = img.getColorArgb(x, y);
                        float k = luminance(c) / maxL;
                        int nr = Math.min(255, Math.round((float)r * k));
                        int ng = Math.min(255, Math.round((float)g * k));
                        int nb = Math.min(255, Math.round((float)b * k));
                        img.setColorArgb(x, y, c & -16777216 | nr << 16 | ng << 8 | nb);
                     }
                  }

                  Path dest = textures.resolve(destPath);
                  Files.createDirectories(dest.getParent());
                  img.writeTo(dest);
                  ++n;
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
               continue;
            }

            if (img != null) {
               img.close();
            }
         } catch (IOException e) {
            PackEditorClient.LOGGER.warn("Failed to tint {}: {}", destPath, e.toString());
         }
      }

      return n;
   }

   static byte[] withParticle(byte[] model) {
      if (model == null) {
         return null;
      } else {
         try {
            JsonElement root = JsonParser.parseString(new String(model, StandardCharsets.UTF_8));
            if (!root.isJsonObject() || root.getAsJsonObject().has("parent")) {
               return model;
            }

            JsonObject textures = root.getAsJsonObject().getAsJsonObject("textures");
            if (textures == null || textures.has("particle") || textures.isEmpty()) {
               return model;
            }

            for(Map.Entry<String, JsonElement> e : textures.entrySet()) {
               if (((JsonElement)e.getValue()).isJsonPrimitive() && !((JsonElement)e.getValue()).getAsString().startsWith("#")) {
                  textures.add("particle", (JsonElement)e.getValue());
                  return root.toString().getBytes(StandardCharsets.UTF_8);
               }
            }
         } catch (RuntimeException var5) {
         }

         return model;
      }
   }

   static NativeImage firstFrame(NativeImage img) {
      if (img == null) {
         return null;
      } else {
         int w = img.getWidth();
         int h = img.getHeight();
         if (h > w && h % w == 0) {
            NativeImage out = firstFrameCopy(img);
            img.close();
            return out;
         } else {
            return img;
         }
      }
   }

   static NativeImage firstFrameCopy(NativeImage img) {
      int w = img.getWidth();
      int h = img.getHeight();
      int fh = h > w && h % w == 0 ? w : h;
      NativeImage out = new NativeImage(w, fh, true);

      for(int y = 0; y < fh; ++y) {
         for(int x = 0; x < w; ++x) {
            out.setColorArgb(x, y, img.getColorArgb(x, y));
         }
      }

      return out;
   }

   private static int writeGlowstoneDust(Path textures, Variant glowstone) {
      try {
         NativeImage block = firstFrame(glowstone.loadImage((Category.Target)Category.GLOWSTONE.targets.get(0)));

         byte var17;
         label90: {
            try {
               byte[] dustBytes = Variant.readVanilla("item/glowstone_dust.png");
               if (block != null && dustBytes != null) {
                  NativeImage dust = NativeImage.read(dustBytes);

                  try {
                     int[] px = WornArmour.matchColours(WornArmour.pixels(dust), WornArmour.pixels(block));
                     NativeImage out = WornArmour.toImage(px, dust.getWidth(), dust.getHeight());

                     try {
                        Path dest = textures.resolve("item/glowstone_dust.png");
                        Files.createDirectories(dest.getParent());
                        out.writeTo(dest);
                     } catch (Throwable var12) {
                        if (out != null) {
                           try {
                              out.close();
                           } catch (Throwable var11) {
                              var12.addSuppressed(var11);
                           }
                        }

                        throw var12;
                     }

                     if (out != null) {
                        out.close();
                     }
                  } catch (Throwable var13) {
                     if (dust != null) {
                        try {
                           dust.close();
                        } catch (Throwable var10) {
                           var13.addSuppressed(var10);
                        }
                     }

                     throw var13;
                  }

                  if (dust != null) {
                     dust.close();
                  }

                  var17 = 1;
                  break label90;
               }

               var17 = 0;
            } catch (Throwable var14) {
               if (block != null) {
                  try {
                     block.close();
                  } catch (Throwable var9) {
                     var14.addSuppressed(var9);
                  }
               }

               throw var14;
            }

            if (block != null) {
               block.close();
            }

            return var17;
         }

         if (block != null) {
            block.close();
         }

         return var17;
      } catch (IOException e) {
         PackEditorClient.LOGGER.warn("Failed to colour glowstone dust: {}", e.toString());
         return 0;
      }
   }

   private static int writeCobwebOpacity(Path textures, Variant web, int opacity) {
      Category.Target t = (Category.Target)Category.COBWEB.targets.get(0);

      try {
         NativeImage img = web.loadImage(t);

         byte var13;
         label71: {
            int c;
            try {
               if (img == null) {
                  var13 = 0;
                  break label71;
               }

               for(int y = 0; y < img.getHeight(); ++y) {
                  for(int x = 0; x < img.getWidth(); ++x) {
                     c = img.getColorArgb(x, y);
                     int a = (c >>> 24) * opacity / 100;
                     img.setColorArgb(x, y, a << 24 | c & 16777215);
                  }
               }

               Path dest = textures.resolve(t.path());
               Files.createDirectories(dest.getParent());
               img.writeTo(dest);
               byte[] meta = web.loadMeta(t);
               if (meta != null) {
                  Files.write(dest.resolveSibling(String.valueOf(dest.getFileName()) + ".mcmeta"), meta, new OpenOption[0]);
               }

               c = 1;
            } catch (Throwable var10) {
               if (img != null) {
                  try {
                     img.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (img != null) {
               img.close();
            }

            return c;
         }

         if (img != null) {
            img.close();
         }

         return var13;
      } catch (IOException e) {
         PackEditorClient.LOGGER.warn("Failed to write see-through cobweb: {}", e.toString());
         return 0;
      }
   }

   private static int writeNoExplosionParticles(Path textures) {
      int n = 0;

      for(int i = 0; i < 16; ++i) {
         String path = "particle/explosion_" + i + ".png";

         try {
            byte[] vanilla = Variant.readVanilla(path);
            if (vanilla != null) {
               NativeImage v = NativeImage.read(vanilla);

               int w;
               int h;
               try {
                  w = v.getWidth();
                  h = v.getHeight();
               } catch (Throwable var13) {
                  if (v != null) {
                     try {
                        v.close();
                     } catch (Throwable var11) {
                        var13.addSuppressed(var11);
                     }
                  }

                  throw var13;
               }

               if (v != null) {
                  v.close();
               }

               v = new NativeImage(w, h, true);

               try {
                  v.fillRect(0, 0, w, h, 0);
                  Path dest = textures.resolve(path);
                  Files.createDirectories(dest.getParent());
                  v.writeTo(dest);
               } catch (Throwable var12) {
                  try {
                     v.close();
                  } catch (Throwable var10) {
                     var12.addSuppressed(var10);
                  }

                  throw var12;
               }

               v.close();
               ++n;
            }
         } catch (IOException e) {
            PackEditorClient.LOGGER.warn("Failed to write {}: {}", path, e.toString());
         }
      }

      return n;
   }

   private static int writeLowFire(Path textures, Variant fire, boolean low, boolean still) {
      int n = 0;

      for(Category.Target t : Category.FIRE.targets) {
         try {
            NativeImage src = fire.loadImage(t);

            label116: {
               try {
                  if (src != null) {
                     NativeImage lowered = low ? lowFire(src) : null;

                     try {
                        NativeImage base = lowered != null ? lowered : src;
                        NativeImage frame = still ? firstFrameCopy(base) : null;

                        try {
                           Path dest = textures.resolve(t.path());
                           Files.createDirectories(dest.getParent());
                           (frame != null ? frame : base).writeTo(dest);
                        } catch (Throwable var16) {
                           if (frame != null) {
                              try {
                                 frame.close();
                              } catch (Throwable var15) {
                                 var16.addSuppressed(var15);
                              }
                           }

                           throw var16;
                        }

                        if (frame != null) {
                           frame.close();
                        }
                     } catch (Throwable var17) {
                        if (lowered != null) {
                           try {
                              lowered.close();
                           } catch (Throwable var14) {
                              var17.addSuppressed(var14);
                           }
                        }

                        throw var17;
                     }

                     if (lowered != null) {
                        lowered.close();
                     }

                     Path metaFile = textures.resolve(t.path() + ".mcmeta");
                     byte[] meta = still ? null : fire.loadMeta(t);
                     if (meta != null) {
                        Files.write(metaFile, meta, new OpenOption[0]);
                     } else {
                        Files.deleteIfExists(metaFile);
                     }

                     ++n;
                     break label116;
                  }
               } catch (Throwable var18) {
                  if (src != null) {
                     try {
                        src.close();
                     } catch (Throwable var13) {
                        var18.addSuppressed(var13);
                     }
                  }

                  throw var18;
               }

               if (src != null) {
                  src.close();
               }
               continue;
            }

            if (src != null) {
               src.close();
            }
         } catch (IOException e) {
            PackEditorClient.LOGGER.warn("Failed to make low fire {}: {}", t.path(), e.toString());
         }
      }

      return n;
   }

   public static NativeImage lowFire(NativeImage src) {
      int w = src.getWidth();
      int h = src.getHeight();
      int frames = Math.max(1, h / w);
      int size = h / frames;
      int top = size;

      for(int f = 0; f < frames; ++f) {
         for(int y = 0; y < size && y < top; ++y) {
            for(int x = 0; x < w; ++x) {
               if (src.getColorArgb(x, f * size + y) >>> 24 != 0) {
                  top = y;
                  break;
               }
            }
         }
      }

      int flame = size - top;
      int target = Math.max(1, Math.round((float)size * 0.2F));
      NativeImage out = new NativeImage(w, h, true);
      out.fillRect(0, 0, w, h, 0);

      for(int f = 0; f < frames; ++f) {
         int base = f * size;
         if (flame <= target) {
            for(int y = 0; y < size; ++y) {
               for(int x = 0; x < w; ++x) {
                  out.setColorArgb(x, base + y, src.getColorArgb(x, base + y));
               }
            }
         } else {
            for(int y = 0; y < target; ++y) {
               int sy = top + y * flame / target;

               for(int x = 0; x < w; ++x) {
                  out.setColorArgb(x, base + size - target + y, src.getColorArgb(x, base + sy));
               }
            }
         }
      }

      return out;
   }

   private static float luminance(int c) {
      return (float)(c >> 16 & 255) * 0.299F + (float)(c >> 8 & 255) * 0.587F + (float)(c & 255) * 0.114F;
   }

   private static void writeShieldModels(Path dir, boolean small, boolean flatIcon) throws IOException {
      Files.createDirectories(dir);
      writeShieldModel(dir, "shield", small, flatIcon, new String[][]{{"thirdperson_righthand", "0, 90, 0", "10, 6, -4", "1"}, {"thirdperson_lefthand", "0, 90, 0", "10, 6, 12", "1"}, {"firstperson_righthand", "0, 180, 5", "-10.5, -2, -10", "1"}, {"firstperson_lefthand", "0, 180, 5", "10.5, -3.6, -10", "1"}});
      writeShieldModel(dir, "shield_blocking", small, flatIcon, new String[][]{{"thirdperson_righthand", "45, 135, 0", "3.51, 11, -2", "1"}, {"thirdperson_lefthand", "45, 135, 0", "13.51, 3, 5", "1"}, {"firstperson_righthand", "0, 180, -5", "-15, -1, -11", "1"}, {"firstperson_lefthand", "0, 180, -5", "5, 0.9, -11", "1"}});
   }

   private static void writeShieldModel(Path dir, String name, boolean small, boolean flatIcon, String[][] awerial) throws IOException {
      byte[] vanilla = Variant.readVanillaAsset("models/item/" + name + ".json");
      if (vanilla != null) {
         JsonObject model = JsonParser.parseString(new String(vanilla, StandardCharsets.UTF_8)).getAsJsonObject();
         JsonObject display = model.getAsJsonObject("display");
         if (small) {
            for(String[] d : awerial) {
               display.add(d[0], JsonParser.parseString("{\"rotation\": [" + d[1] + "], \"translation\": [" + d[2] + "], \"scale\": [" + d[3] + ", " + d[3] + ", " + d[3] + "]}"));
            }
         }

         if (flatIcon) {
            display.add("gui", JsonParser.parseString("{\"rotation\": [0, 0, 0], \"translation\": [4.9, 5.3, 0], \"scale\": [0.65, 0.65, 0.65]}"));
         }

         Files.writeString(dir.resolve(name + ".json"), (new GsonBuilder()).setPrettyPrinting().create().toJson(model));
      }
   }

   private static boolean writeComposite(Path dest, String path, List<RegionJob> jobs) throws IOException {
      byte[] vanillaBytes = Variant.readVanilla(path);
      if (vanillaBytes == null) {
         return false;
      } else {
         List<NativeImage> sources = new ArrayList();

         try {
            NativeImage base = NativeImage.read(vanillaBytes);

            try {
               int w = base.getWidth();

               for(RegionJob job : jobs) {
                  NativeImage src = job.variant().loadImage(job.target());
                  sources.add(src);
                  if (src != null) {
                     w = Math.max(w, src.getWidth());
                  }
               }

               int h = w * base.getHeight() / base.getWidth();
               NativeImage out = new NativeImage(w, h, true);

               try {
                  blit(base, out, 0, 0, w, h);

                  for(int i = 0; i < jobs.size(); ++i) {
                     NativeImage src = (NativeImage)sources.get(i);
                     if (src != null) {
                        int[] r = ((RegionJob)jobs.get(i)).target().region();
                        int sx = r[0] * w / 64;
                        int sy = r[1] * h / 32;
                        int sw = r[2] * w / 64;
                        int sh = r[3] * h / 32;
                        blit(src, out, sx, sy, sw, sh);
                     }
                  }

                  Files.createDirectories(dest.getParent());
                  out.writeTo(dest);
               } catch (Throwable var26) {
                  try {
                     out.close();
                  } catch (Throwable var25) {
                     var26.addSuppressed(var25);
                  }

                  throw var26;
               }

               out.close();
            } catch (Throwable var27) {
               if (base != null) {
                  try {
                     base.close();
                  } catch (Throwable var24) {
                     var27.addSuppressed(var24);
                  }
               }

               throw var27;
            }

            if (base != null) {
               base.close();
            }
         } finally {
            for(NativeImage s : sources) {
               if (s != null) {
                  s.close();
               }
            }
         }

         return true;
      }
   }

   private static void blit(NativeImage src, NativeImage out, int x, int y, int w, int h) {
      for(int py = y; py < y + h; ++py) {
         for(int px = x; px < x + w; ++px) {
            int sx = px * src.getWidth() / out.getWidth();
            int sy = py * src.getHeight() / out.getHeight();
            out.setColorArgb(px, py, src.getColorArgb(sx, sy));
         }
      }

   }

   private static void enableAndReload(MinecraftClient client) {
      ResourcePackManager manager = client.getResourcePackManager();
      manager.scanPacks();
      List<String> before = new ArrayList(client.options.resourcePacks);
      manager.disable("file/AwerialsPackEditor");
      manager.enable("file/AwerialsPackEditor");
      client.options.refreshResourcePacks(manager);
      if (before.equals(client.options.resourcePacks)) {
         client.reloadResources();
      }

   }

   private static void writeMcmeta(Path root) throws IOException {
      int f = 75;
      String json = "{\n  \"pack\": {\n    \"description\": \"Generated by Awerial's Pack Editor\",\n    \"pack_format\": " + f + ",\n    \"min_format\": " + f + ",\n    \"max_format\": " + f + "\n  }\n}\n";
      Files.writeString(root.resolve("pack.mcmeta"), json);
   }

   private static void writeIcon(Path root) {
      try {
         InputStream in = PackBuilder.class.getResourceAsStream("/assets/awerialspackeditor/icon.png");

         try {
            if (in != null) {
               Files.write(root.resolve("pack.png"), in.readAllBytes(), new OpenOption[0]);
            }
         } catch (Throwable var5) {
            if (in != null) {
               try {
                  in.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (in != null) {
            in.close();
         }
      } catch (IOException var6) {
      }

   }

   private static void deleteRecursively(Path dir) throws IOException {
      if (Files.exists(dir, new LinkOption[0])) {
         Stream<Path> walk = Files.walk(dir);

         try {
            for(Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
               Files.delete(p);
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

   private static record RegionJob(Variant variant, Category.Target target) {
   }
}
