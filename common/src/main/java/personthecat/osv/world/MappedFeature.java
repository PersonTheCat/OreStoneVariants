package personthecat.osv.world;

import lombok.Value;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import personthecat.catlib.data.BiomePredicate;
import personthecat.osv.config.Cfg;

@Value
public class MappedFeature {
    BiomePredicate biomes;
    PlacedFeature feature;

    public boolean canSpawn(final Biome biome) {
        return !Cfg.biomeSpecific() || this.biomes.test(biome);
    }
}
