package com.awerial.packeditor;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.FlyingItemEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class DrawnWindChargeRenderer extends EntityRenderer<AbstractWindChargeEntity, FlyingItemEntityRenderState> {
   private static final ItemStack WIND_CHARGE;
   private final ItemModelManager itemModelManager;

   public DrawnWindChargeRenderer(EntityRendererFactory.Context ctx) {
      super(ctx);
      this.itemModelManager = ctx.getItemModelManager();
   }

   public FlyingItemEntityRenderState createRenderState() {
      return new FlyingItemEntityRenderState();
   }

   public void updateRenderState(AbstractWindChargeEntity entity, FlyingItemEntityRenderState state, float tickDelta) {
      super.updateRenderState(entity, state, tickDelta);
      this.itemModelManager.updateForNonLivingEntity(state.itemRenderState, WIND_CHARGE, ItemDisplayContext.GROUND, entity);
   }

   public void render(FlyingItemEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState camera) {
      matrices.push();
      matrices.multiply(camera.orientation);
      state.itemRenderState.render(matrices, queue, state.light, OverlayTexture.DEFAULT_UV, state.outlineColor);
      matrices.pop();
      super.render(state, matrices, queue, camera);
   }

   static {
      WIND_CHARGE = new ItemStack(Items.WIND_CHARGE);
   }
}
