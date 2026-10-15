package com.awerial.packeditor.mixin;

import com.awerial.packeditor.Badges;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({PlayerListHud.class})
public class PlayerListHudMixin {
   @ModifyReturnValue(
      method = {"getPlayerName"},
      at = {@At("RETURN")}
   )
   private Text awerialspackeditor$logo(Text name, @Local(argsOnly = true) PlayerListEntry entry) {
      return Badges.withLogo(entry.getProfile().id(), name);
   }
}
