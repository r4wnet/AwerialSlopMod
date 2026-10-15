package com.awerial.packeditor;

public enum Toggle {
   SMALLER_GAPS("smaller_gaps", "Smaller GAPS", Group.GOLDEN_APPLES),
   SMALL_SHIELD("small_shield", "Small shield", Group.SHIELDS),
   LOW_FIRE("low_fire", "Low fire", Group.FIRE),
   INVERT_CROSSHAIR("invert_crosshair", "Invert colours", Group.CROSSHAIR),
   LEGACY_GLINT("legacy_glint", "Legacy Glint", Group.GLINT),
   STILL_PEARLS("still_pearls", "Animations", Group.ENDER_PEARLS, true),
   NO_EXPLOSION_PARTICLES("no_explosion_particles", "Explosion Particles", Group.END_CRYSTALS),
   SMALLER_PEARLS("smaller_pearls", "Smaller Pearls", Group.ENDER_PEARLS),
   NO_GLINT("no_glint", "Enchant glint", Group.GLINT, true),
   STILL_FIRE("still_fire", "Animations", Group.FIRE, true);

   public final String id;
   public final String label;
   public final Group group;
   public final boolean inverted;

   private Toggle(String id, String label, Group group) {
      this(id, label, group, false);
   }

   private Toggle(String id, String label, Group group, boolean inverted) {
      this.id = id;
      this.label = label;
      this.group = group;
      this.inverted = inverted;
   }

   public boolean inCodeFlags() {
      return this.ordinal() < 7;
   }

}
