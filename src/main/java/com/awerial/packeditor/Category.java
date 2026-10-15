package com.awerial.packeditor;

import java.util.List;
import java.util.stream.Stream;

public enum Category {
   END_CRYSTAL("end_crystal", "End Crystals", Group.END_CRYSTALS, List.of("models/item/end_crystal.json"), new Target[]{t("entity/end_crystal/end_crystal.png", "entity/endercrystal/endercrystal.png"), t("entity/end_crystal/end_crystal_beam.png", "entity/endercrystal/endercrystal_beam.png"), t("item/end_crystal.png", "items/end_crystal.png")}),
   RESPAWN_ANCHOR("respawn_anchor", "Respawn Anchors", Group.RESPAWN_ANCHORS, List.of(), new Target[]{t("block/respawn_anchor_side4.png"), t("block/respawn_anchor_side0.png"), t("block/respawn_anchor_side1.png"), t("block/respawn_anchor_side2.png"), t("block/respawn_anchor_side3.png"), t("block/respawn_anchor_top.png"), t("block/respawn_anchor_top_off.png"), t("block/respawn_anchor_bottom.png")}),
   GLOWSTONE("glowstone", "Glowstone", Group.GLOWSTONE, List.of(), new Target[]{t("block/glowstone.png", "blocks/glowstone.png")}),
   COBWEB("cobweb", "Cobwebs", Group.COBWEB, List.of(), new Target[]{t("block/cobweb.png", "blocks/web.png")}),
   TOTEM("totem", "Totems of Undying", Group.TOTEMS, List.of(), new Target[]{t("item/totem_of_undying.png", "items/totem_of_undying.png", "items/totem.png")}),
   TOTEM_SIZE("totem_size", "Totem Sizes", Group.TOTEM_SIZES, List.of("models/item/totem_of_undying.json"), new Target[0]),
   OBSIDIAN("obsidian", "Obsidian", Group.OBSIDIAN, List.of(), new Target[]{t("block/obsidian.png", "blocks/obsidian.png")}),
   ENDER_PEARL("ender_pearl", "Ender Pearls", Group.ENDER_PEARLS, List.of(), new Target[]{t("item/ender_pearl.png", "items/ender_pearl.png")}),
   GOLDEN_APPLE("golden_apple", "Golden Apples", Group.GOLDEN_APPLES, List.of(), new Target[]{t("item/golden_apple.png", "items/apple_golden.png", "items/golden_apple.png")}),
   FIRE("fire", "Fire", Group.FIRE, List.of(), new Target[]{t("block/fire_0.png", "blocks/fire_layer_0.png"), t("block/fire_1.png", "blocks/fire_layer_1.png")}),
   GLINT("glint", "Enchant Glint", Group.GLINT, List.of(), new Target[]{t("misc/enchanted_glint_item.png", "misc/enchanted_glint_armor.png", "misc/enchanted_item_glint.png"), t("misc/enchanted_glint_armor.png", "misc/enchanted_item_glint.png")}),
   SKY("sky", "Sky", Group.SKY, List.of(), new Target[0]),
   CROSSHAIR("crosshair", "Crosshair", Group.CROSSHAIR, List.of(), new Target[]{t("gui/sprites/hud/crosshair.png")}),
   SHIELD("shield", "Shields", Group.SHIELDS, List.of(), new Target[]{t("entity/shield_base_nopattern.png"), t("entity/shield_base.png")}),
   NETHERITE_SWORD("netherite_sword", "Sword", Group.NETHERITE_TOOLS, List.of(), new Target[]{t("item/netherite_sword.png", "items/netherite_sword.png")}),
   NETHERITE_PICKAXE("netherite_pickaxe", "Pickaxe", Group.NETHERITE_TOOLS, List.of(), new Target[]{t("item/netherite_pickaxe.png", "items/netherite_pickaxe.png")}),
   NETHERITE_AXE("netherite_axe", "Axe", Group.NETHERITE_TOOLS, List.of(), new Target[]{t("item/netherite_axe.png", "items/netherite_axe.png")}),
   NETHERITE_SHOVEL("netherite_shovel", "Shovel", Group.NETHERITE_TOOLS, List.of(), new Target[]{t("item/netherite_shovel.png", "items/netherite_shovel.png")}),
   NETHERITE_SPEAR("netherite_spear", "Spear", Group.NETHERITE_TOOLS, List.of("models/item/netherite_spear_in_hand.json"), new Target[]{t("item/netherite_spear.png"), t("item/netherite_spear_in_hand.png")}),
   MACE("mace", "Mace", Group.NETHERITE_TOOLS, List.of("models/item/handheld_mace.json"), new Target[]{t("item/mace.png")}),
   NETHERITE_HELMET("netherite_helmet", "Helmet", Group.NETHERITE_ARMOUR, List.of(), new Target[]{t("item/netherite_helmet.png", "items/netherite_helmet.png"), region(0, 0, 64, 16, "entity/equipment/humanoid/netherite.png", "models/armor/netherite_layer_1.png")}),
   NETHERITE_CHESTPLATE("netherite_chestplate", "Chestplate", Group.NETHERITE_ARMOUR, List.of(), new Target[]{t("item/netherite_chestplate.png", "items/netherite_chestplate.png"), region(16, 16, 40, 16, "entity/equipment/humanoid/netherite.png", "models/armor/netherite_layer_1.png")}),
   NETHERITE_LEGGINGS("netherite_leggings", "Leggings", Group.NETHERITE_ARMOUR, List.of(), new Target[]{t("item/netherite_leggings.png", "items/netherite_leggings.png"), t("entity/equipment/humanoid_leggings/netherite.png", "models/armor/netherite_layer_2.png")}),
   NETHERITE_BOOTS("netherite_boots", "Boots", Group.NETHERITE_ARMOUR, List.of(), new Target[]{t("item/netherite_boots.png", "items/netherite_boots.png"), region(0, 16, 16, 16, "entity/equipment/humanoid/netherite.png", "models/armor/netherite_layer_1.png")}),
   DIAMOND_SWORD("diamond_sword", "Sword", Group.NETHERITE_TOOLS, List.of(), new Target[]{t("item/diamond_sword.png", "items/diamond_sword.png")}),
   DIAMOND_PICKAXE("diamond_pickaxe", "Pickaxe", Group.NETHERITE_TOOLS, List.of(), new Target[]{t("item/diamond_pickaxe.png", "items/diamond_pickaxe.png")}),
   DIAMOND_AXE("diamond_axe", "Axe", Group.NETHERITE_TOOLS, List.of(), new Target[]{t("item/diamond_axe.png", "items/diamond_axe.png")}),
   DIAMOND_SHOVEL("diamond_shovel", "Shovel", Group.NETHERITE_TOOLS, List.of(), new Target[]{t("item/diamond_shovel.png", "items/diamond_shovel.png")}),
   DIAMOND_SPEAR("diamond_spear", "Spear", Group.NETHERITE_TOOLS, List.of("models/item/diamond_spear_in_hand.json"), new Target[]{t("item/diamond_spear.png"), t("item/diamond_spear_in_hand.png")}),
   DIAMOND_HELMET("diamond_helmet", "Helmet", Group.NETHERITE_ARMOUR, List.of(), new Target[]{t("item/diamond_helmet.png", "items/diamond_helmet.png"), region(0, 0, 64, 16, "entity/equipment/humanoid/diamond.png", "models/armor/diamond_layer_1.png")}),
   DIAMOND_CHESTPLATE("diamond_chestplate", "Chestplate", Group.NETHERITE_ARMOUR, List.of(), new Target[]{t("item/diamond_chestplate.png", "items/diamond_chestplate.png"), region(16, 16, 40, 16, "entity/equipment/humanoid/diamond.png", "models/armor/diamond_layer_1.png")}),
   DIAMOND_LEGGINGS("diamond_leggings", "Leggings", Group.NETHERITE_ARMOUR, List.of(), new Target[]{t("item/diamond_leggings.png", "items/diamond_leggings.png"), t("entity/equipment/humanoid_leggings/diamond.png", "models/armor/diamond_layer_2.png")}),
   DIAMOND_BOOTS("diamond_boots", "Boots", Group.NETHERITE_ARMOUR, List.of(), new Target[]{t("item/diamond_boots.png", "items/diamond_boots.png"), region(0, 16, 16, 16, "entity/equipment/humanoid/diamond.png", "models/armor/diamond_layer_1.png")});

   public final String id;
   public final String displayName;
   public final Group group;
   public final List<String> models;
   public final List<Target> targets;

   private Category(String id, String displayName, Group group, List<String> models, Target... targets) {
      this.id = id;
      this.displayName = displayName;
      this.group = group;
      this.models = models;
      this.targets = List.of(targets);
   }

   public String listKey() {
      String var10000;
      switch (this.group) {
         case NETHERITE_TOOLS -> var10000 = "netherite_tools";
         case NETHERITE_ARMOUR -> var10000 = "netherite_armor";
         default -> var10000 = this.id;
      }

      return var10000;
   }

   public boolean drawable() {
      boolean var10000;
      switch (this.ordinal()) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
         case 6:
         case 7:
         case 8:
         case 9:
         case 12:
         case 19:
            var10000 = true;
            break;
         case 5:
         case 10:
         case 11:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         default:
            var10000 = this.material() != null;
      }

      return var10000;
   }

   public Target wornTarget() {
      for(Target t : this.targets) {
         if (t.path().startsWith("entity/equipment/")) {
            return t;
         }
      }

      return null;
   }

   public String material() {
      if (this.id.startsWith("diamond_")) {
         return "diamond";
      } else {
         return this.id.startsWith("netherite_") ? "netherite" : null;
      }
   }

   public Category counterpart() {
      String m = this.material();
      if (m == null) {
         return this;
      } else {
         String var10000 = m.equals("diamond") ? "netherite_" : "diamond_";
         Category other = byId(var10000 + this.id.substring(this.id.indexOf(95) + 1));
         return other != null ? other : this;
      }
   }

   public static Category byId(String id) {
      for(Category c : values()) {
         if (c.id.equals(id)) {
            return c;
         }
      }

      return null;
   }

   private static Target t(String path, String... aliases) {
      return new Target(path, List.of(aliases), (int[])null);
   }

   private static Target region(int x, int y, int w, int h, String path, String... aliases) {
      return new Target(path, List.of(aliases), new int[]{x, y, w, h});
   }

   public static record Target(String path, List<String> aliases, int[] region) {
      public List<String> allPaths() {
         return Stream.concat(Stream.of(this.path), this.aliases.stream()).toList();
      }
   }
}
