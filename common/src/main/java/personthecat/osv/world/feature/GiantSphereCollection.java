package personthecat.osv.world.feature;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.Collection;
import java.util.Set;

import static personthecat.catlib.serialization.CodecUtils.easySet;

public class GiantSphereCollection implements FeatureConfiguration {

    public static final Codec<GiantSphereCollection> CODEC =
        easySet(GiantSphereConfig.CODEC).xmap(GiantSphereCollection::new, c -> c.configs);

    final Set<GiantSphereConfig> configs;

    public GiantSphereCollection(final Collection<GiantSphereConfig> configs) {
        this.configs = ImmutableSet.copyOf(configs);
    }
}
