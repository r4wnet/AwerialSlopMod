package com.awerial.packeditor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.SystemToast.Type;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class Announcements {
   private static final long REMIND_EVERY = 21600000L;
   private static long lastSeenPost = -1L;
   private static long lastReminder;
   private static boolean loaded;
   private static volatile JsonObject latest;
   public static final String DOWNLOAD = "https://awerial.awerial.workers.dev/#download";
   public static final String NEWS = "https://awerial.awerial.workers.dev/#news";

   private Announcements() {
   }

   private static Path file() {
      return FabricLoader.getInstance().getConfigDir().resolve("awerialspackeditor_news.json");
   }

   private static void load() {
      if (!loaded) {
         loaded = true;

         try {
            if (Files.isRegularFile(file(), new LinkOption[0])) {
               JsonObject o = JsonParser.parseString(Files.readString(file(), StandardCharsets.UTF_8)).getAsJsonObject();
               if (o.has("last_post")) {
                  lastSeenPost = o.get("last_post").getAsLong();
               }

               if (o.has("last_reminder")) {
                  lastReminder = o.get("last_reminder").getAsLong();
               }
            }
         } catch (Exception e) {
            PackEditorClient.LOGGER.warn("Could not read news settings: {}", e.toString());
         }

      }
   }

   private static void save() {
      JsonObject o = new JsonObject();
      o.addProperty("last_post", lastSeenPost);
      o.addProperty("last_reminder", lastReminder);

      try {
         Files.writeString(file(), o.toString(), StandardCharsets.UTF_8);
      } catch (Exception var2) {
      }

   }

   public static void fetch(MinecraftClient client, Runnable then) {
      if (!"https://awerial.awerial.workers.dev".isEmpty()) {
         HttpRequest req = HttpRequest.newBuilder(URI.create("https://awerial.awerial.workers.dev/api/posts")).timeout(Duration.ofSeconds(10L)).build();
         Stats.HTTP.sendAsync(req, BodyHandlers.ofString()).thenAccept((r) -> {
            if (r.statusCode() == 200) {
               JsonArray posts = JsonParser.parseString((String)r.body()).getAsJsonArray();
               if (!posts.isEmpty()) {
                  latest = posts.get(0).getAsJsonObject();
                  if (then != null) {
                     client.execute(then);
                  }

               }
            }
         }).exceptionally((e) -> null);
      }
   }

   public static void check(MinecraftClient client) {
      fetch(client, () -> {
         if (latest != null) {
            show(client, latest);
         }

      });
   }

   public static String newerVersion() {
      JsonObject p = latest;
      String v = p == null ? null : versionOf(p);
      return v != null && compare(v, installedVersion()) > 0 ? v : null;
   }

   public static String latestTitle() {
      JsonObject p = latest;
      return p == null ? null : p.get("title").getAsString();
   }

   private static String versionOf(JsonObject post) {
      return post.has("version") && !post.get("version").isJsonNull() ? post.get("version").getAsString().replaceFirst("^[vV]", "") : null;
   }

   private static void show(MinecraftClient client, JsonObject post) {
      if (client.player != null) {
         load();
         long id = post.get("id").getAsLong();
         String title = post.get("title").getAsString();
         String version = versionOf(post);
         boolean newer = version != null && compare(version, installedVersion()) > 0;
         long now = System.currentTimeMillis();
         boolean firstTime = lastSeenPost < 0L;
         boolean unseen = id > lastSeenPost;
         lastSeenPost = Math.max(lastSeenPost, id);
         if (newer) {
            if (!unseen && now - lastReminder < 21600000L) {
               save();
               return;
            }

            lastReminder = now;
         } else if (!unseen || firstTime || version != null) {
            save();
            return;
         }

         save();
         MutableText msg = Text.literal("✦ ").formatted(Formatting.LIGHT_PURPLE).append(Text.literal("Awerial's Pack Editor").formatted(new Formatting[]{Formatting.LIGHT_PURPLE, Formatting.BOLD})).append(Text.literal(newer ? "  " + version + " is out! " : "  ").formatted(Formatting.WHITE)).append(Text.literal(title).formatted(Formatting.GRAY));
         msg.append(Text.literal("  "));
         if (newer) {
            msg.append(link("[Download]", "https://awerial.awerial.workers.dev/#download", "Download " + version + " from the website", Formatting.GREEN)).append(Text.literal(" "));
         }

         msg.append(link("[What's new]", "https://awerial.awerial.workers.dev/#news", "Read the full update on the website", Formatting.AQUA));
         client.inGameHud.getChatHud().addMessage(msg);
         SystemToast.add(client.getToastManager(), Type.PERIODIC_NOTIFICATION, Text.literal(newer ? "Pack Editor " + version + " is out!" : "Awerial's Pack Editor").formatted(Formatting.LIGHT_PURPLE), Text.literal(newer ? "Click [Download] in chat" : title));
      }
   }

   private static Text link(String label, String url, String hover, Formatting colour) {
      return Text.literal(label).setStyle(Style.EMPTY.withColor(colour).withUnderline(true).withClickEvent(new ClickEvent.OpenUrl(URI.create(url))).withHoverEvent(new HoverEvent.ShowText(Text.literal(hover))));
   }

   private static String installedVersion() {
      return (String)FabricLoader.getInstance().getModContainer("awerialspackeditor").map((m) -> m.getMetadata().getVersion().getFriendlyString()).orElse("0");
   }

   static int compare(String a, String b) {
      String[] x = a.split("[.\\-+]");
      String[] y = b.split("[.\\-+]");

      for(int i = 0; i < Math.max(x.length, y.length); ++i) {
         int p = i < x.length ? num(x[i]) : 0;
         int q = i < y.length ? num(y[i]) : 0;
         if (p != q) {
            return Integer.compare(p, q);
         }
      }

      return 0;
   }

   private static int num(String s) {
      try {
         return Integer.parseInt(s.replaceAll("\\D.*$", ""));
      } catch (NumberFormatException var2) {
         return 0;
      }
   }
}
