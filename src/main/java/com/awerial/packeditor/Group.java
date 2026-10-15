package com.awerial.packeditor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum Group {
   END_CRYSTALS("End Crystals", () -> Items.END_CRYSTAL),
   RESPAWN_ANCHORS("Respawn Anchors", () -> Items.RESPAWN_ANCHOR),
   GLOWSTONE("Glowstone", () -> Items.GLOWSTONE),
   COBWEB("Cobwebs", () -> Items.COBWEB),
   TOTEMS("Totems", () -> Items.TOTEM_OF_UNDYING),
   TOTEM_SIZES("Totem Sizes", () -> Items.TOTEM_OF_UNDYING),
   OBSIDIAN("Obsidian", () -> Items.OBSIDIAN),
   ENDER_PEARLS("Ender Pearls", () -> Items.ENDER_PEARL),
   GOLDEN_APPLES("Golden Apples", () -> Items.GOLDEN_APPLE),
   FIRE("Fire", () -> Items.FLINT_AND_STEEL),
   CROSSHAIR("Crosshair", () -> Items.TARGET),
   GLINT("Enchant Glint", () -> Items.ENCHANTED_BOOK),
   SKY("Sky", () -> Items.LIGHT_BLUE_STAINED_GLASS),
   SHIELDS("Shields", () -> Items.SHIELD),
   NETHERITE_TOOLS("Tools", () -> Items.NETHERITE_SWORD),
   NETHERITE_ARMOUR("Armour", () -> Items.NETHERITE_CHESTPLATE),
   CONFIGS("Configs", () -> Items.WRITABLE_BOOK),
   MY_PACKS("My Packs", () -> Items.CHEST),
   ITEMS("Items", () -> Items.ITEM_FRAME),
   EFFECTS_COMBAT("Combat", () -> Items.NETHERITE_SWORD),
   EFFECTS_WEAPONS("Mace & Spear", () -> Items.MACE),
   EFFECTS_TOTEMS("Totems & HUD", () -> Items.TOTEM_OF_UNDYING),
   EFFECTS_HANDS("Hands", () -> Items.GOLDEN_APPLE);

   public final String displayName;
   private final Supplier<Item> icon;

   public boolean isPanel() {
      return this == CONFIGS || this == MY_PACKS || this == ITEMS || this.isEffects();
   }

   public boolean isEffects() {
      return this == EFFECTS_COMBAT || this == EFFECTS_WEAPONS || this == EFFECTS_TOTEMS || this == EFFECTS_HANDS;
   }

   private Group(String displayName, Supplier<Item> icon) {
      this.displayName = displayName;
      this.icon = icon;
   }

   public Item icon() {
      return (Item)this.icon.get();
   }

   public List<Category> categories() {
      return Arrays.stream(Category.values()).filter((c) -> c.group == this).toList();
   }

   public boolean hasMaterials() {
      return this == NETHERITE_TOOLS || this == NETHERITE_ARMOUR;
   }

   public List<Category> categories(boolean diamond) {
      if (!this.hasMaterials()) {
         return this.categories();
      } else {
         String m = diamond ? "diamond" : "netherite";
         return this.categories().stream().filter((c) -> c.material() == null || m.equals(c.material())).toList();
      }
   }

}
