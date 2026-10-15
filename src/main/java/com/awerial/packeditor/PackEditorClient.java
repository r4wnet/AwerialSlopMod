package com.awerial.packeditor;

import com.awerial.packeditor.gui.PackEditorScreen;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.WindChargeEntityRenderer;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackEditorClient implements ClientModInitializer {
   public static final String MOD_ID = "awerialspackeditor";
   public static final Logger LOGGER = LoggerFactory.getLogger("Awerial's Pack Editor");
   private static KeyBinding openKey;
   private static KeyBinding resetPopsKey;
   private static int announceIn;
   private static final String UPDATE_PREFIX = "✦ Pack Editor ";

   public static KeyBinding resetPopsKey() {
      return resetPopsKey;
   }

   private static void addUpdateButton(Screen screen) {
      String v = Announcements.newerVersion();
      if (v != null) {
         List<ClickableWidget> buttons = Screens.getButtons(screen);
         if (!buttons.stream().anyMatch((b) -> b.getMessage().getString().startsWith("✦ Pack Editor "))) {
            Text label = Text.literal("✦ Pack Editor " + v + " is out — Download").formatted(Formatting.LIGHT_PURPLE);
            MinecraftClient client = MinecraftClient.getInstance();
            int w = Math.min(screen.width - 8, client.textRenderer.getWidth(label) + 16);
            ButtonWidget button = ButtonWidget.builder(label, (b) -> ConfirmLinkScreen.open(screen, "https://awerial.awerial.workers.dev/#download")).dimensions(4, 4, w, 20).tooltip(Tooltip.of(Text.literal(Announcements.latestTitle() + "\nOpens the download on the website"))).build();
            buttons.add(button);
         }
      }
   }

   public void onInitializeClient() {
      PackEditorConfig.load();
      Stats.load();
      ItemTextures.moveToOwnTabs();
      KeyBinding.Category category = Category.create(Identifier.of("awerialspackeditor", "main"));
      openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.awerialspackeditor.open", Type.KEYSYM, 79, category));
      Effects.load();
      resetPopsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.awerialspackeditor.reset_pops", Type.KEYSYM, -1, category));
      EntityRendererRegistry.register(EntityType.WIND_CHARGE, (ctx) -> (EntityRenderer)(ItemTextures.isDrawn("wind_charge") ? new DrawnWindChargeRenderer(ctx) : new WindChargeEntityRenderer(ctx)));
      CrosshairPresets.refresh();
      ScreenEvents.AFTER_INIT.register((ScreenEvents.AfterInit)(client, screen, w, h) -> {
         if (screen instanceof TitleScreen) {
            addUpdateButton(screen);
         }

      });
      Announcements.fetch(MinecraftClient.getInstance(), () -> {
         Screen screen = MinecraftClient.getInstance().currentScreen;
         if (screen instanceof TitleScreen) {
            addUpdateButton(screen);
         }

      });
      if ((Boolean)PackEditorConfig.toggles().getOrDefault(Toggle.LEGACY_GLINT, false)) {
         LegacyGlint.prepare();
      }

      ClientPlayConnectionEvents.JOIN.register((ClientPlayConnectionEvents.Join)(handler, sender, client) -> {
         VariantRegistry.warmUp();
         CombatTracker.clear();
         announceIn = 60;
      });
      ClientTickEvents.END_CLIENT_TICK.register((ClientTickEvents.EndTick)(client) -> {
         if (announceIn > 0 && client.player != null && --announceIn == 0) {
            Announcements.check(client);
         }

         Stats.tick(client);
         Badges.tick(client);
         CombatTracker.tick(client);
         DamageTint.refresh();

         while(resetPopsKey.wasPressed()) {
            CombatTracker.resetPops();
            if (client.player != null) {
               client.inGameHud.setOverlayMessage(Text.literal("Totem pop counts reset"), false);
            }
         }

         while(openKey.wasPressed()) {
            if (client.currentScreen == null) {
               client.setScreen(new PackEditorScreen((Screen)null));
            }
         }

      });
   }
}
