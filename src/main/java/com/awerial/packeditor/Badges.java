package com.awerial.packeditor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.session.Session;
import net.minecraft.network.encryption.PlayerKeyPair;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class Badges {
   private static final Text LOGO;
   private static final long REGISTER_EVERY = 1800000L;
   private static final long LOOKUP_AGAIN = 1800000L;
   private static final long LOOKUP_GAP = 60000L;
   private static final long BACK_OFF = 300000L;
   private static volatile long backOffUntil;
   private static final Map<UUID, Known> KNOWN;
   private static volatile UUID registeredFor;
   private static long lastRegister;
   private static long lastLookup;
   private static volatile UUID triedFor;
   private static volatile boolean busy;

   private Badges() {
   }

   public static boolean has(UUID id) {
      if (id == null) {
         return false;
      } else {
         MinecraftClient c = MinecraftClient.getInstance();
         if (id.equals(c.getSession().getUuidOrNull())) {
            return id.equals(registeredFor) && Stats.badgeEnabled();
         } else {
            Known k = (Known)KNOWN.get(id);
            return k != null && k.has;
         }
      }
   }

   public static boolean logoOn() {
      UUID me = MinecraftClient.getInstance().getSession().getUuidOrNull();
      return Stats.badgeEnabled() && me != null && me.equals(registeredFor);
   }

   public static int logosSeen() {
      ClientPlayNetworkHandler net = MinecraftClient.getInstance().getNetworkHandler();
      if (net == null) {
         return 0;
      } else {
         int n = 0;

         for(PlayerListEntry e : net.getPlayerList()) {
            Known k = (Known)KNOWN.get(e.getProfile().id());
            if (k != null && k.has) {
               ++n;
            }
         }

         return n;
      }
   }

   public static Text withLogo(UUID id, Text name) {
      if (name != null && has(id)) {
         MutableText t = Text.empty().append(LOGO).append(Text.literal(" "));
         return t.append(name);
      } else {
         return name;
      }
   }

   public static void tick(MinecraftClient client) {
      if (!"https://awerial.awerial.workers.dev".isEmpty()) {
         ClientPlayNetworkHandler net = client.getNetworkHandler();
         if (net != null && client.player != null) {
            long now = System.currentTimeMillis();
            UUID me = client.getSession().getUuidOrNull();
            boolean switched = me != null && !me.equals(registeredFor) && !me.equals(triedFor);
            if (me != null && Stats.badgeEnabled() && (switched || !me.equals(registeredFor) && now - lastRegister > 300000L || now - lastRegister > 1800000L)) {
               triedFor = me;
               lastRegister = now;
               register(client.getSession(), false);
            }

            if (!busy && now - lastLookup >= 60000L && now >= backOffUntil) {
               lastLookup = now;
               UUID self = client.getSession().getUuidOrNull();
               List<String> ask = new ArrayList();

               for(PlayerListEntry e : net.getPlayerList()) {
                  UUID id = e.getProfile().id();
                  if (id != null && !id.equals(self)) {
                     Known k = (Known)KNOWN.get(id);
                     if (k == null || now - k.at > 1800000L) {
                        ask.add(id.toString());
                     }

                     if (ask.size() >= 100) {
                        break;
                     }
                  }
               }

               if (!ask.isEmpty()) {
                  lookup(ask, now);
               }

            }
         }
      }
   }

   private static void lookup(List<String> ids, long now) {
      busy = true;
      JsonObject body = new JsonObject();
      JsonArray arr = new JsonArray();
      Objects.requireNonNull(arr);
      ids.forEach(arr::add);
      body.add("ids", arr);
      HttpRequest req = HttpRequest.newBuilder(URI.create("https://awerial.awerial.workers.dev/api/badge/lookup")).timeout(Duration.ofSeconds(10L)).header("content-type", "application/json").POST(BodyPublishers.ofString(body.toString())).build();
      Stats.HTTP.sendAsync(req, BodyHandlers.ofString()).thenAccept((r) -> {
         if (r.statusCode() != 200) {
            backOffUntil = System.currentTimeMillis() + 300000L;
         } else {
            Set<String> have = new HashSet();

            for(JsonElement e : JsonParser.parseString((String)r.body()).getAsJsonObject().getAsJsonArray("have")) {
               have.add(e.getAsString());
            }

            for(String id : ids) {
               KNOWN.put(UUID.fromString(id), new Known(have.contains(id), now));
            }

         }
      }).whenComplete((v, e) -> {
         if (e != null) {
            backOffUntil = System.currentTimeMillis() + 300000L;
         }

         busy = false;
      });
   }

   private static void register(Session session, boolean remove) {
      if (!FabricLoader.getInstance().isDevelopmentEnvironment() && session.getUuidOrNull() != null) {
         Thread t = new Thread(() -> {
            String uuid = session.getUuidOrNull().toString();
            JsonObject body = new JsonObject();
            body.addProperty("uuid", uuid);
            body.addProperty("secret", Stats.badgeSecret());
            body.addProperty("remove", remove);

            try {
               Optional<PlayerKeyPair> keys = (Optional)MinecraftClient.getInstance().getProfileKeys().fetchKeyPair().get(5L, TimeUnit.SECONDS);
               if (keys.isPresent()) {
                  PlayerPublicKey.PublicKeyData data = ((PlayerKeyPair)keys.get()).publicKey().data();
                  String payload = "awerial-badge|" + uuid + "|" + System.currentTimeMillis() + "|" + (remove ? "remove" : "add");
                  Signature sig = Signature.getInstance("SHA256withRSA");
                  sig.initSign(((PlayerKeyPair)keys.get()).privateKey());
                  sig.update(payload.getBytes(StandardCharsets.UTF_8));
                  Base64.Encoder enc = Base64.getEncoder();
                  JsonObject proof = new JsonObject();
                  proof.addProperty("payload", payload);
                  proof.addProperty("expiresAt", data.expiresAt().toEpochMilli());
                  proof.addProperty("publicKey", enc.encodeToString(data.key().getEncoded()));
                  proof.addProperty("keySignature", enc.encodeToString(data.keySignature()));
                  proof.addProperty("signature", enc.encodeToString(sig.sign()));
                  body.add("proof", proof);
               }
            } catch (Exception var11) {
            }

            try {
               HttpRequest req = HttpRequest.newBuilder(URI.create("https://awerial.awerial.workers.dev/api/badge/register")).timeout(Duration.ofSeconds(15L)).header("content-type", "application/json").POST(BodyPublishers.ofString(body.toString())).build();
               HttpResponse<String> r = Stats.HTTP.send(req, BodyHandlers.ofString());
               if (r.statusCode() == 200) {
                  registeredFor = remove ? null : session.getUuidOrNull();
                  PackEditorClient.LOGGER.info("Logo by my name: {} ({})", remove ? "removed" : "on", body.has("proof") ? "confirmed with Mojang's key" : "install code");
               } else {
                  PackEditorClient.LOGGER.info("Logo by my name: the website said {} {}", r.statusCode(), r.body());
               }
            } catch (Exception e) {
               PackEditorClient.LOGGER.info("Logo by my name: couldn't reach the website ({})", e.toString());
            }

         }, "APE badge");
         t.setDaemon(true);
         t.start();
      }
   }

   static {
      LOGO = Text.literal("\ue000").setStyle(Style.EMPTY.withFont(new StyleSpriteSource.Font(Identifier.of("awerialspackeditor", "badge"))));
      KNOWN = new ConcurrentHashMap();
      lastRegister = -1800000L;
   }

   private static record Known(boolean has, long at) {
   }
}
