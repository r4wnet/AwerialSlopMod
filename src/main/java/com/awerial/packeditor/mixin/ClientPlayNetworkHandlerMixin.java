package com.awerial.packeditor.mixin;

import com.awerial.packeditor.CombatTracker;
import com.awerial.packeditor.ShieldDisabled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public class ClientPlayNetworkHandlerMixin {
   @Inject(
      method = {"onPlaySound"},
      at = {@At("RETURN")}
   )
   private void awerialspackeditor$sound(PlaySoundS2CPacket packet, CallbackInfo ci) {
      ShieldDisabled.onSound(packet.getSound(), packet.getX(), packet.getY(), packet.getZ());
      CombatTracker.onSound(packet.getSound(), packet.getX(), packet.getY(), packet.getZ());
   }

   @Inject(
      method = {"onEntityStatus"},
      at = {@At("RETURN")}
   )
   private void awerialspackeditor$status(EntityStatusS2CPacket packet, CallbackInfo ci) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world != null) {
         CombatTracker.onStatus(packet.getEntity(mc.world), packet.getStatus());
      }

   }
}
