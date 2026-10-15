package com.awerial.packeditor.mixin;

import com.awerial.packeditor.PackEditorConfig;
import com.awerial.packeditor.Toggle;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({ItemStack.class})
public class ItemStackMixin {
   @ModifyReturnValue(
      method = {"hasGlint"},
      at = {@At("RETURN")}
   )
   private boolean awerialspackeditor$noGlint(boolean original) {
      return original && !PackEditorConfig.get(Toggle.NO_GLINT);
   }
}
