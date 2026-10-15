package com.awerial.packeditor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;

public final class HudExtras {
   private static final EquipmentSlot[] ARMOUR;

   private HudExtras() {
   }

   public static void render(DrawContext ctx, float tickDelta) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null && !mc.options.hudHidden) {
         if (Effects.durabilityWarning) {
            durability(ctx, mc.player);
         }

         if (Effects.spearCharge) {
            lunge(ctx, mc.player, tickDelta);
         }

      }
   }

   private static void durability(DrawContext ctx, PlayerEntity p) {
      boolean lit = Util.getMeasuringTimeMs() / 400L % 2L == 0L;
      int x = ctx.getScaledWindowWidth() / 2 + 96;
      int y = ctx.getScaledWindowHeight() - 20;

      for(EquipmentSlot slot : ARMOUR) {
         ItemStack s = p.getEquippedStack(slot);
         if (!s.isEmpty() && s.isDamageable()) {
            int left = s.getMaxDamage() - s.getDamage();
            if (left * 10 < s.getMaxDamage()) {
               ctx.fill(x - 1, y - 1, x + 17, y + 17, lit ? -1593892832 : 1614807040);
               ctx.drawItem(s, x, y);
               int var10000 = Math.max(0, left * 100 / s.getMaxDamage());
               String pct = var10000 + "%";
               ctx.drawText(mc().textRenderer, pct, x + 8 - mc().textRenderer.getWidth(pct) / 2, y - 10, lit ? -43691 : -1, true);
               x += 22;
            }
         }
      }

   }

   private static void lunge(DrawContext ctx, PlayerEntity p, float tickDelta) {
      ItemStack s = p.getMainHandStack();
      if (s.get(DataComponentTypes.PIERCING_WEAPON) != null && hasLunge(s)) {
         float needed = (Float)s.getOrDefault(DataComponentTypes.MINIMUM_ATTACK_CHARGE, 0.0F);
         if (needed <= 0.0F) {
            needed = 1.0F;
         }

         float t = Math.min(1.0F, p.getAttackCooldownProgress(tickDelta) / needed);
         float gui = (float)MinecraftClient.getInstance().getWindow().getScaleFactor();
         float cx = (float)ctx.getScaledWindowWidth() / 2.0F;
         float cy = (float)ctx.getScaledWindowHeight() / 2.0F + 19.0F;
         ctx.getMatrices().pushMatrix();
         ctx.getMatrices().translate(cx, cy);
         ctx.getMatrices().scale(1.0F / gui, 1.0F / gui);
         float r = 5.5F * gui;
         int points = Math.max(32, Math.round(r * 7.0F));

         for(int i = 0; i < points; ++i) {
            double a = (Math.PI * 2D) * (double)i / (double)points;
            boolean filled = t >= 1.0F || (float)i / (float)points < t;
            int c = t >= 1.0F ? -251658241 : (filled ? (int)(112.0F + 128.0F * t) << 24 | 16777215 : 956301311);

            for(float rr = r - Math.max(1.0F, gui * 0.6F); rr <= r; ++rr) {
               int x = Math.round((float)Math.sin(a) * rr);
               int y = Math.round((float)(-Math.cos(a)) * rr);
               ctx.fill(x, y, x + 1, y + 1, c);
            }
         }

         if (t >= 1.0F) {
            int d = Math.max(1, Math.round(gui * 0.75F));
            ctx.fill(-d, -d, d, d, -251658241);
         }

         ctx.getMatrices().popMatrix();
      }
   }

   private static boolean hasLunge(ItemStack s) {
      for(RegistryEntry<Enchantment> e : s.getEnchantments().getEnchantments()) {
         if (e.matchesKey(Enchantments.LUNGE)) {
            return true;
         }
      }

      return false;
   }

   private static MinecraftClient mc() {
      return MinecraftClient.getInstance();
   }

   static {
      ARMOUR = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
   }
}
