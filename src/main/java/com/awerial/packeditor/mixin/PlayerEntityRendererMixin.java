package com.awerial.packeditor.mixin;

import com.awerial.packeditor.Badges;
import com.awerial.packeditor.CombatTracker;
import com.awerial.packeditor.Effects;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerEntityRenderer.class})
public class PlayerEntityRendererMixin {
   @Inject(
      method = {"updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V"},
      at = {@At("TAIL")}
   )
   private void awerialspackeditor$nameExtras(PlayerLikeEntity player, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
      if (state.displayName != null) {
         int pops = Effects.popCounter ? CombatTracker.pops(player.getUuid()) : 0;
         if (pops > 0) {
            state.displayName = state.displayName.copy().append(Text.literal("  ×" + pops).setStyle(Style.EMPTY.withColor(Effects.popColorFor(pops)).withBold(true)));
         }

         state.displayName = Badges.withLogo(player.getUuid(), state.displayName);
      }
   }
}
