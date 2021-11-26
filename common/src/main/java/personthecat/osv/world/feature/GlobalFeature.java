package personthecat.osv.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import java.util.Collection;

public abstract class GlobalFeature<FC extends FeatureConfiguration> extends Feature<FC> {

    public GlobalFeature(final Codec<FC> codec) {
        super(codec);
    }

    public abstract ConfiguredFeature<?, ?> configured(final Collection<FeatureStem> configs);
}
