package com.awerial.packeditor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

public final class Stats {
   public static final String SITE = "https://awerial.awerial.workers.dev";
   private static final long INTERVAL_MS = 600000L;
   static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).build();
   private static boolean enabled = true;
   private static String badgeSecret;
   private static String id;
   private static long lastPing;

   private Stats() {
   }

   private static Path file() {
      return FabricLoader.getInstance().getConfigDir().resolve("awerialspackeditor_stats.json");
   }

   public static void load() {
      try {
         if (Files.isRegularFile(file(), new LinkOption[0])) {
            JsonObject o = JsonParser.parseString(Files.readString(file(), StandardCharsets.UTF_8)).getAsJsonObject();
            if (o.has("share_player_count")) {
               enabled = o.get("share_player_count").getAsBoolean();
            }

            if (o.has("id")) {
               id = o.get("id").getAsString();
            }

            if (o.has("badge_secret")) {
               badgeSecret = o.get("badge_secret").getAsString();
            }
         }
      } catch (Exception e) {
         PackEditorClient.LOGGER.warn("Could not read stats settings", e);
      }

      if (id == null || badgeSecret == null) {
         if (id == null) {
            id = UUID.randomUUID().toString();
         }

         if (badgeSecret == null) {
            String var10000 = UUID.randomUUID().toString().replace("-", "");
            badgeSecret = var10000 + UUID.randomUUID().toString().replace("-", "");
         }

         save();
      }

   }

   private static void save() {
      JsonObject o = new JsonObject();
      o.addProperty("share_player_count", enabled);
      o.addProperty("id", id);
      o.addProperty("badge_secret", badgeSecret);

      try {
         Files.createDirectories(file().getParent());
         Files.writeString(file(), o.toString(), StandardCharsets.UTF_8);
      } catch (Exception e) {
         PackEditorClient.LOGGER.warn("Could not save stats settings", e);
      }

   }

   public static boolean enabled() {
      return enabled;
   }

   public static void setEnabled(boolean on) {
      enabled = on;
      save();
   }

   static String badgeSecret() {
      return badgeSecret;
   }

   public static boolean badgeEnabled() {
      return true;
   }

   static String publicServer(MinecraftClient client) {
      ServerInfo entry = client.getCurrentServerEntry();
      if (entry != null && !entry.isLocal() && !client.isInSingleplayer()) {
         String a = entry.address.trim().toLowerCase(Locale.ROOT);
         if (a.endsWith(":25565")) {
            a = a.substring(0, a.length() - 6);
         }

         String host = a.replaceAll(":\\d+$", "");
         if (host.contains(".") && !host.equals("localhost") && !host.endsWith(".local") && !host.matches("^(10|127)\\..*|^192\\.168\\..*|^172\\.(1[6-9]|2\\d|3[01])\\..*|^169\\.254\\..*")) {
            return a.length() > 80 ? a.substring(0, 80) : a;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static void tick(MinecraftClient client) {
      if (!"https://awerial.awerial.workers.dev".isEmpty() && enabled && client.player != null && !FabricLoader.getInstance().isDevelopmentEnvironment()) {
         long now = System.currentTimeMillis();
         if (now - lastPing >= 600000L) {
            lastPing = now;
            String version = (String)FabricLoader.getInstance().getModContainer("awerialspackeditor").map((m) -> m.getMetadata().getVersion().getFriendlyString()).orElse("?");
            JsonObject body = new JsonObject();
            body.addProperty("id", id);
            body.addProperty("version", version);
            String server = publicServer(client);
            if (server != null) {
               body.addProperty("server", server);
               body.addProperty("logo", Badges.logoOn());
               body.addProperty("logosSeen", Badges.logosSeen());
            }

            HttpRequest req = HttpRequest.newBuilder(URI.create("https://awerial.awerial.workers.dev/api/ping")).timeout(Duration.ofSeconds(10L)).header("content-type", "application/json").POST(BodyPublishers.ofString(body.toString())).build();
            HTTP.sendAsync(req, BodyHandlers.discarding()).exceptionally((e) -> null);
         }
      }
   }
}
