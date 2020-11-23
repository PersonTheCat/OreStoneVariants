
package com.personthecat.orestonevariants.world;

import net.minecraft.world.biome.Biome;

/** Used as a wildcard to map generators with no biome entries. */
public class WildcardBiome extends Biome {
    public WildcardBiome() {
        super(new BiomeProperties("WILDCARD"));
    }
}
