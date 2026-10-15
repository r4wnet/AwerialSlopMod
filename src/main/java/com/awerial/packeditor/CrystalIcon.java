package com.awerial.packeditor;

import net.minecraft.client.texture.NativeImage;

final class CrystalIcon {
   static final String MODEL;

   private CrystalIcon() {
   }

   static NativeImage itemTexture(NativeImage entity) {
      int s = Math.max(1, entity.getWidth() / 64);
      int w = 32 * s;
      NativeImage out = new NativeImage(w, w, true);
      out.fillRect(0, 0, w, w, 0);

      for(int y = 0; y < 16 * s; ++y) {
         for(int x = 0; x < 32 * s; ++x) {
            out.setColorArgb(x, y, entity.getColorArgb(x, y));
            out.setColorArgb(x, 16 * s + y, entity.getColorArgb(32 * s + x, y));
         }
      }

      return out;
   }

   private static String faces(int v) {
      return "{\"north\": {\"uv\": [4, " + (v + 4) + ", 8, " + (v + 8) + "], \"texture\": \"#all\"}, \"east\": {\"uv\": [0, " + (v + 4) + ", 4, " + (v + 8) + "], \"texture\": \"#all\"}, \"south\": {\"uv\": [12, " + (v + 4) + ", 16, " + (v + 8) + "], \"texture\": \"#all\"}, \"west\": {\"uv\": [8, " + (v + 4) + ", 12, " + (v + 8) + "], \"texture\": \"#all\"}, \"up\": {\"uv\": [4, " + v + ", 8, " + (v + 4) + "], \"texture\": \"#all\"}, \"down\": {\"uv\": [8, " + v + ", 12, " + (v + 4) + "], \"texture\": \"#all\"}}";
   }

   static {
      String var10000 = faces(8);
      MODEL = "{\n  \"textures\": { \"all\": \"minecraft:item/end_crystal\", \"particle\": \"minecraft:item/end_crystal\" },\n  \"elements\": [\n    { \"from\": [3, 3, 3], \"to\": [13, 13, 13], \"faces\": " + var10000 + " },\n    { \"from\": [0, 0, 0], \"to\": [16, 16, 16], \"rotation\": { \"angle\": -45, \"axis\": \"x\", \"origin\": [8, 8, 8] }, \"faces\": " + faces(0) + " },\n    { \"from\": [-0.5, -0.5, -0.5], \"to\": [16.5, 16.5, 16.5], \"rotation\": { \"angle\": -45, \"axis\": \"z\", \"origin\": [8, 8, 8] }, \"faces\": " + faces(0) + " }\n  ],\n  \"display\": {\n    \"gui\": { \"rotation\": [30, 45, 0], \"scale\": [0.55, 0.55, 0.55] },\n    \"ground\": { \"translation\": [0, 3, 0], \"scale\": [0.25, 0.25, 0.25] },\n    \"fixed\": { \"rotation\": [0, 180, 0], \"scale\": [0.45, 0.45, 0.45] },\n    \"thirdperson_righthand\": { \"rotation\": [75, 45, 0], \"translation\": [0, 2.5, 0], \"scale\": [0.3, 0.3, 0.3] },\n    \"thirdperson_lefthand\": { \"rotation\": [75, 45, 0], \"translation\": [0, 2.5, 0], \"scale\": [0.3, 0.3, 0.3] },\n    \"firstperson_righthand\": { \"rotation\": [0, 45, 0], \"translation\": [1, 2, 0], \"scale\": [0.17, 0.17, 0.17] },\n    \"firstperson_lefthand\": { \"rotation\": [0, 45, 0], \"translation\": [1, 2, 0], \"scale\": [0.17, 0.17, 0.17] }\n  }\n}\n";
   }
}
