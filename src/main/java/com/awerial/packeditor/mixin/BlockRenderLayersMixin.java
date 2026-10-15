package com.awerial.packeditor.mixin;

import com.awerial.packeditor.PackEditorConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BlockRenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({BlockRenderLayers.class})
public class BlockRenderLayersMixin {
   @ModifyReturnValue(
      method = {"getBlockLayer"},
      at = {@At("RETURN")}
   )
   private static BlockRenderLayer awerialspackeditor$seeThroughCobwebs(BlockRenderLayer original, BlockState state) {
      return PackEditorConfig.cobwebOpacity() < 100 && state.isOf(Blocks.COBWEB) ? BlockRenderLayer.TRANSLUCENT : original;
   }
}
