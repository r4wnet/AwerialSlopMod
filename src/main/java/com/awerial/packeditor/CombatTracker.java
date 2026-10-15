package com.awerial.packeditor;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworkExplosionComponent.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class CombatTracker {
   private static final Map<UUID, Integer> POPS = new HashMap();
   private static int lastHitId = -1;
   private static long lastHitTime = -1000L;
   private static long lastCrystalHitTime = -1000L;
   private static final List<double[]> RINGS = new ArrayList();

   private CombatTracker() {
   }

   public static int pops(UUID player) {
      return (Integer)POPS.getOrDefault(player, 0);
   }

   public static void resetPops() {
      POPS.clear();
   }

   public static void onStatus(Entity e, byte status) {
      if (e != null) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (status == 35 && e instanceof PlayerEntity) {
            PlayerEntity p = (PlayerEntity)e;
            if (Effects.popCounter) {
               POPS.merge(p.getUuid(), 1, Integer::sum);
               return;
            }
         }

         if (status == 3 && e instanceof PlayerEntity) {
            PlayerEntity p = (PlayerEntity)e;
            POPS.remove(p.getUuid());
            if (p != mc.player && mc.world != null && killedByYou(p, mc.world.getTime())) {
               killEffect(p);
            }
         }

      }
   }

   private static boolean killedByYou(PlayerEntity p, long now) {
      if (p.getId() == lastHitId && now - lastHitTime <= 100L) {
         return true;
      } else {
         MinecraftClient mc = MinecraftClient.getInstance();
         return now - lastCrystalHitTime <= 40L && mc.player != null && p.squaredDistanceTo(mc.player) < (double)256.0F;
      }
   }

   public static void onAttack(Entity target) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world != null) {
         if (target instanceof EndCrystalEntity) {
            lastCrystalHitTime = mc.world.getTime();
         } else {
            lastHitId = target.getId();
            lastHitTime = mc.world.getTime();
         }

      }
   }

   private static void killEffect(PlayerEntity p) {
      Effects.KillEffect k = Effects.killEffect;
      MinecraftClient mc = MinecraftClient.getInstance();
      if (k != Effects.KillEffect.OFF && mc.world != null) {
         Vec3d pos = new Vec3d(p.getX(), p.getY(), p.getZ());
         if (k == Effects.KillEffect.LIGHTNING || k == Effects.KillEffect.BOTH) {
            LightningEntity bolt = (LightningEntity)EntityType.LIGHTNING_BOLT.create(mc.world, SpawnReason.TRIGGERED);
            if (bolt != null) {
               bolt.refreshPositionAfterTeleport(pos);
               bolt.setCosmetic(true);
               mc.world.addEntity(bolt);
            }
         }

         if (k == Effects.KillEffect.FIREWORK || k == Effects.KillEffect.BOTH) {
            int c = Effects.killColor;
            FireworkExplosionComponent burst = new FireworkExplosionComponent(Type.LARGE_BALL, IntList.of(c), IntList.of(16777215), true, true);
            mc.world.addFireworkParticle(pos.x, pos.y + (double)1.5F, pos.z, (double)0.0F, (double)0.0F, (double)0.0F, List.of(burst));
         }

      }
   }

   public static void onSound(RegistryEntry<SoundEvent> sound, double x, double y, double z) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world != null) {
         Identifier id = ((SoundEvent)sound.value()).id();
         boolean heavy = id.equals(SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY.id());
         if (heavy || id.equals(SoundEvents.ITEM_MACE_SMASH_GROUND.id())) {
            if (Effects.maceRing) {
               RINGS.add(new double[]{x, y, z, (double)0.0F, heavy ? (double)1.0F : (double)0.0F});
            }

            if (heavy && Effects.maceBoom) {
               mc.world.playSoundClient(x, y, z, (SoundEvent)SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.7F, 1.6F, false);
               mc.world.playSoundClient(x, y, z, SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY, SoundCategory.PLAYERS, 1.2F, 0.7F, false);
            }

         }
      }
   }

   public static void tick(MinecraftClient mc) {
      if (mc.world == null) {
         RINGS.clear();
      } else {
         RINGS.removeIf((r) -> {
            double var10003 = r[3];
            double var10000 = r[3];
            r[3] = var10003 + (double)1.0F;
            int t = (int)var10000;
            int steps = r[4] > (double)0.0F ? 7 : 5;
            if (t >= steps) {
               return true;
            } else {
               double radius = (double)0.5F + (double)t * 0.55;
               int points = 14 + t * 6;
               DustParticleEffect dust = new DustParticleEffect(Effects.maceColor, 1.4F - (float)t * 0.1F);

               for(int i = 0; i < points; ++i) {
                  double a = (Math.PI * 2D) * (double)i / (double)points;
                  mc.world.addParticleClient(dust, r[0] + Math.cos(a) * radius, r[1] + 0.15, r[2] + Math.sin(a) * radius, (double)0.0F, 0.02, (double)0.0F);
               }

               return false;
            }
         });
      }
   }

   public static void clear() {
      POPS.clear();
      RINGS.clear();
      lastHitId = -1;
   }
}
