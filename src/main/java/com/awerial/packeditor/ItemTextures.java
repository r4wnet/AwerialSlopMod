package com.awerial.packeditor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public final class ItemTextures {
   private static List<Entry> all;
   private static final Map<String, Entry> BY_ID = new HashMap();
   private static Set<String> drawn;
   private static final Map<String, String> NAMES = new ConcurrentHashMap();

   private ItemTextures() {
   }

   public static synchronized List<Entry> all() {
      if (all != null) {
         return all;
      } else {
         List<Entry> list = new ArrayList();

         for(Item item : Registries.ITEM) {
            Identifier id = Registries.ITEM.getId(item);
            if (item != Items.AIR && id.getNamespace().equals("minecraft")) {
               String p = id.getPath();
               if (categoryFor(p) == null) {
                  for(String tex : List.of("item/" + p, "block/" + p, "item/" + p + "_standby", "block/" + p + "_front", "block/" + p + "_side", "block/" + p + "_top")) {
                     if (exists(tex + ".png")) {
                        Entry e = new Entry(p, tex + ".png", item);
                        list.add(e);
                        BY_ID.put(p, e);
                        break;
                     }
                  }
               }
            }
         }

         all = List.copyOf(list);
         return all;
      }
   }

   public static String searchName(Entry e) {
      return (String)NAMES.computeIfAbsent(e.id(), (k) -> (new ItemStack(e.item())).getName().getString().toLowerCase(Locale.ROOT));
   }

   static void searchNames() {
      for(Entry e : all()) {
         searchName(e);
      }

   }

   public static Entry get(String id) {
      all();
      return (Entry)BY_ID.get(id);
   }

   private static boolean exists(String texture) {
      return MinecraftClient.getInstance().getDefaultResourcePack().open(ResourceType.CLIENT_RESOURCES, Identifier.ofVanilla("textures/" + texture)) != null;
   }

   public static NativeImage loadVanilla(Entry e) throws IOException {
      byte[] b = Variant.readVanillaAsset("textures/" + e.texture());
      return b == null ? null : NativeImage.read(b);
   }

   private static Path dir() {
      return FabricLoader.getInstance().getConfigDir().resolve("awerialspackeditor").resolve("drawings").resolve("items");
   }

   public static Category categoryFor(String itemId) {
      for(Category c : Category.values()) {
         for(Category.Target t : c.targets) {
            String p = t.path();
            if (p.equals("item/" + itemId + ".png") || p.equals("block/" + itemId + ".png") || p.startsWith("block/" + itemId + "_side") || p.startsWith("block/" + itemId + "_top")) {
               return c;
            }
         }
      }

      return null;
   }

   static void moveToOwnTabs() {
      if (Files.isDirectory(dir(), new LinkOption[0])) {
         List<String> moved = new ArrayList();

         try {
            Stream<Path> files = Files.list(dir());

            try {
               for(Path f : files.toList()) {
                  String n = f.getFileName().toString();
                  if (n.endsWith(".png") && !n.endsWith(".worn.png")) {
                     String id = n.substring(0, n.length() - 4);
                     Category c = categoryFor(id);
                     if (c != null) {
                        String main = ((Category.Target)c.targets.get(0)).path();
                        if (main.equals("item/" + id + ".png") || main.equals("block/" + id + ".png")) {
                           String name = Drawings.cleanName("From Items");

                           for(int i = 2; Drawings.list(c).contains(name); ++i) {
                              name = Drawings.cleanName("From Items " + i);
                           }

                           Path to = Drawings.dir(c);
                           Files.createDirectories(to);
                           Files.move(f, to.resolve(name + ".png"));
                           Path worn = dir().resolve(id + ".worn.png");
                           Path anim = dir().resolve(id + ".anim");
                           if (Files.exists(worn, new LinkOption[0])) {
                              Files.move(worn, to.resolve(name + ".worn.png"));
                           }

                           if (Files.exists(anim, new LinkOption[0])) {
                              Files.move(anim, to.resolve(name + ".anim"));
                           }

                           PackEditorConfig.select(c, "drawn:" + name);
                           moved.add(id + " -> " + c.displayName);
                        }
                     }
                  }
               }
            } catch (Throwable var13) {
               if (files != null) {
                  try {
                     files.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }
               }

               throw var13;
            }

            if (files != null) {
               files.close();
            }
         } catch (IOException e) {
            PackEditorClient.LOGGER.warn("Could not move item drawings to their tabs", e);
         }

         if (!moved.isEmpty()) {
            drawn = null;
            PackEditorClient.LOGGER.info("Moved item drawings to their own tabs: {}", moved);
         }

      }
   }

   public static Set<String> drawn() {
      if (drawn != null) {
         return drawn;
      } else {
         Set<String> ids = new LinkedHashSet();
         if (Files.isDirectory(dir(), new LinkOption[0])) {
            try {
               Stream<Path> files = Files.list(dir());

               try {
                  for(Path f : files.sorted().toList()) {
                     String n = f.getFileName().toString();
                     if (n.endsWith(".png") && !n.endsWith(".worn.png") && categoryFor(n.substring(0, n.length() - 4)) == null) {
                        ids.add(n.substring(0, n.length() - 4));
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
            } catch (IOException e) {
               PackEditorClient.LOGGER.warn("Could not list item drawings", e);
            }
         }

         drawn = ids;
         return ids;
      }
   }

   public static boolean isDrawn(String id) {
      return drawn().contains(id);
   }

   static Path file(String id) {
      return dir().resolve(id + ".png");
   }

   public static NativeImage load(String id) throws IOException {
      Path f = file(id);
      return Files.isRegularFile(f, new LinkOption[0]) ? NativeImage.read(Files.readAllBytes(f)) : null;
   }

   public static NativeImage loadWorn(String id) throws IOException {
      Path f = dir().resolve(id + ".worn.png");
      return Files.isRegularFile(f, new LinkOption[0]) ? NativeImage.read(Files.readAllBytes(f)) : null;
   }

   public static void save(String id, NativeImage img, NativeImage worn) throws IOException {
      Files.createDirectories(dir());
      img.writeTo(file(id));
      Path wf = dir().resolve(id + ".worn.png");
      if (worn != null) {
         worn.writeTo(wf);
      } else {
         Files.deleteIfExists(wf);
      }

      drawn = null;
   }

   public static int frameTime(String id) {
      return Drawings.readFrameTime(dir().resolve(id + ".anim"));
   }

   public static void saveFrameTime(String id, int ticks) throws IOException {
      Drawings.writeFrameTime(dir().resolve(id + ".anim"), ticks);
   }

   public static void delete(String id) throws IOException {
      Files.deleteIfExists(file(id));
      Files.deleteIfExists(dir().resolve(id + ".anim"));
      Files.deleteIfExists(dir().resolve(id + ".worn.png"));
      drawn = null;
   }

   static Variant wornVariant(final String itemId, final Category.Target worn) {
      return new Variant("item:" + itemId, itemId, "Your drawing") {
         public NativeImage loadImage(Category.Target target) throws IOException {
            return WornArmour.wornImage(worn, ItemTextures.loadWorn(itemId), ItemTextures.load(itemId));
         }
      };
   }

   public static record Entry(String id, String texture, Item item) {
   }
}
