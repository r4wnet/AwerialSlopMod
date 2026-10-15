package com.awerial.packeditor.mixin;

import com.awerial.packeditor.Effects;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({MinecraftClient.class})
public class MinecraftClientMixin {
   @ModifyReturnValue(
      method = {"hasOutline"},
      at = {@At("RETURN")}
   )
   private boolean awerialspackeditor$crystalOutline(boolean original, Entity entity) {
      return original || Effects.crystalOutline && entity instanceof EndCrystalEntity;
   }
}
