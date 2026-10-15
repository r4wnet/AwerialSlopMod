package com.awerial.packeditor.mixin;

import com.awerial.packeditor.Effects;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({ParticleManager.class})
public class ParticleManagerMixin {
   @ModifyReturnValue(
      method = {"addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;"},
      at = {@At("RETURN")}
   )
   private Particle awerialspackeditor$recolour(Particle particle, ParticleEffect effect) {
      if (!(particle instanceof BillboardParticle b)) {
         return particle;
      } else {
         ParticleType type = effect.getType();
         int c = -1;
         if (!Effects.hitParticles || type != ParticleTypes.CRIT && type != ParticleTypes.ENCHANTED_HIT && type != ParticleTypes.SWEEP_ATTACK) {
            if (Effects.totemParticles && type == ParticleTypes.TOTEM_OF_UNDYING) {
               float k = 0.75F + (float)Math.random() * 0.25F;
               c = scale(Effects.totemColor, k);
            }
         } else {
            c = Effects.hitColor;
         }

         if (c >= 0) {
            b.setColor((float)(c >> 16 & 255) / 255.0F, (float)(c >> 8 & 255) / 255.0F, (float)(c & 255) / 255.0F);
         }

         return particle;
      }
   }

   private static int scale(int rgb, float k) {
      return Math.round((float)(rgb >> 16 & 255) * k) << 16 | Math.round((float)(rgb >> 8 & 255) * k) << 8 | Math.round((float)(rgb & 255) * k);
   }
}
