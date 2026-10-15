package com.awerial.packeditor;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

public final class ShieldDisabled {
   public static final SpriteIdentifier SPRITE = sprite("entity/shield/awerial_disabled.png");
   public static final SpriteIdentifier SPRITE_NOPATTERN = sprite("entity/shield/awerial_disabled_nopattern.png");
   private static final int MARKER = -4280389;
   private static final int DISABLE_TICKS = 100;
   private static final Map<Integer, Long> OTHERS = new HashMap();

   private ShieldDisabled() {
   }

   private static SpriteIdentifier sprite(String texture) {
      return new SpriteIdentifier(TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, Identifier.ofVanilla(texture.substring(0, texture.length() - ".png".length())));
   }

   public static boolean appliesTo(HeldItemContext holder, ItemStack stack) {
      if (PackEditorConfig.disabledShieldColor() >= 0 && holder != null) {
         MinecraftClient mc = MinecraftClient.getInstance();
         LivingEntity e = holder.getEntity();
         if (e != null && mc.world != null) {
            if (e == mc.player) {
               return mc.player.getItemCooldownManager().isCoolingDown(stack);
            } else {
               Long until = (Long)OTHERS.get(e.getId());
               if (until == null) {
                  return false;
               } else if (mc.world.getTime() >= until) {
                  OTHERS.remove(e.getId());
                  return false;
               } else {
                  return true;
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static void onSound(RegistryEntry<SoundEvent> sound, double x, double y, double z) {
      if (((SoundEvent)sound.value()).id().equals(((SoundEvent)SoundEvents.ITEM_SHIELD_BREAK.value()).id())) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.world != null) {
            LivingEntity nearest = null;
            double best = (double)4.0F;
            Box area = new Box(x - (double)2.0F, y - (double)2.0F, z - (double)2.0F, x + (double)2.0F, y + (double)2.0F, z + (double)2.0F);

            for(LivingEntity p : mc.world.getEntitiesByClass(LivingEntity.class, area, (l) -> l != mc.player)) {
               double d = p.squaredDistanceTo(x, y, z);
               if (d < best) {
                  best = d;
                  nearest = p;
               }
            }

            if (nearest != null) {
               OTHERS.put(nearest.getId(), mc.world.getTime() + 100L);
            }

         }
      }
   }

   public static ComponentMap mark(ComponentMap data) {
      return ComponentMap.builder().addAll(data).add(DataComponentTypes.REPAIR_COST, -4280389).build();
   }

   public static boolean isMarked(ComponentMap data) {
      Integer v = (Integer)data.get(DataComponentTypes.REPAIR_COST);
      return v != null && v == -4280389;
   }
}
