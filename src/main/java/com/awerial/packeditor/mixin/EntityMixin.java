package com.awerial.packeditor.mixin;

import com.awerial.packeditor.Effects;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({Entity.class})
public class EntityMixin {
   @ModifyReturnValue(
      method = {"getTeamColorValue"},
      at = {@At("RETURN")}
   )
   private int awerialspackeditor$crystalOutlineColour(int original) {
      return Effects.crystalOutline && (Object)this instanceof EndCrystalEntity ? Effects.outlineColor : original;
   }
}
