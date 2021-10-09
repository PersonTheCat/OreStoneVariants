package personthecat.osv.world.feature;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.preset.data.DynamicSerializable;

public interface FeatureProvider<T> extends DynamicSerializable<T> {
    ConfiguredFeature<?, ?> createOreFeature(final OrePreset ore);
    ConfiguredFeature<?, ?> createStoneFeature(final StonePreset stone);
}
