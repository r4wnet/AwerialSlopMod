package com.awerial.packeditor.gui;

import com.awerial.packeditor.Group;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

enum Section {
   PVP("PvP", () -> Items.END_CRYSTAL, true, new Group[]{Group.END_CRYSTALS, Group.RESPAWN_ANCHORS, Group.TOTEMS, Group.ENDER_PEARLS, Group.GOLDEN_APPLES, Group.SHIELDS, Group.ITEMS}),
   BLOCKS("Blocks", () -> Items.OBSIDIAN, true, new Group[]{Group.OBSIDIAN, Group.GLOWSTONE, Group.COBWEB, Group.FIRE}),
   GEAR("Gear", () -> Items.NETHERITE_CHESTPLATE, true, new Group[]{Group.NETHERITE_TOOLS, Group.NETHERITE_ARMOUR}),
   HUD("HUD", () -> Items.TARGET, true, new Group[]{Group.CROSSHAIR}),
   MISC("Misc", () -> Items.ENCHANTED_BOOK, true, new Group[]{Group.GLINT, Group.SKY}),
   EFFECTS("Effects", () -> Items.FIREWORK_ROCKET, true, new Group[]{Group.EFFECTS_COMBAT, Group.EFFECTS_WEAPONS, Group.EFFECTS_TOTEMS, Group.EFFECTS_HANDS}),
   CONFIGS("Configs", () -> Items.WRITABLE_BOOK, false, new Group[]{Group.CONFIGS}),
   MY_PACKS("My Packs", () -> Items.CHEST, false, new Group[]{Group.MY_PACKS});

   final String label;
   private final Supplier<Item> icon;
   final boolean big;
   final List<Group> groups;

   private Section(String label, Supplier<Item> icon, boolean big, Group... groups) {
      this.label = label;
      this.icon = icon;
      this.big = big;
      this.groups = List.of(groups);
   }

   Item icon() {
      return (Item)this.icon.get();
   }

   static Section of(Group g) {
      for(Section s : values()) {
         if (s.groups.contains(g)) {
            return s;
         }
      }

      return PVP;
   }

   static Section byLabel(String label) {
      for(Section s : values()) {
         if (s.label.equals(label)) {
            return s;
         }
      }

      return null;
   }

}
