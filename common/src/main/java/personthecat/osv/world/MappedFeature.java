package personthecat.osv.world;

import lombok.Value;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import personthecat.catlib.data.BiomePredicate;

@Value
public class MappedFeature {
    BiomePredicate biomes;
    ConfiguredFeature<?, ?> feature;
}
