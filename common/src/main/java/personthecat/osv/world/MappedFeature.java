package personthecat.osv.world;

import lombok.Value;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import personthecat.catlib.data.BiomePredicate;

@Value
public class MappedFeature {
    BiomePredicate biomes;
    PlacedFeature feature;
}
