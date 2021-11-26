package personthecat.osv.world.feature;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import personthecat.catlib.data.Range;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static personthecat.catlib.serialization.CodecUtils.easySet;

public class GiantSphereCollection implements FeatureConfiguration {

    public static final Codec<GiantSphereCollection> CODEC =
        easySet(GiantSphereConfig.CODEC).xmap(GiantSphereCollection::new, c -> c.configs);

    final Set<GiantSphereConfig> configs;
    final Range bounds;

    public GiantSphereCollection(final Collection<GiantSphereConfig> configs) {
        this.configs = ImmutableSet.copyOf(configs);
        this.bounds = getBounds(configs);
    }

    private static Range getBounds(final Collection<GiantSphereConfig> configs) {
        if (configs.size() == 0) return Range.empty();

        final Iterator<GiantSphereConfig> iterator = configs.iterator();
        final GiantSphereConfig first = iterator.next();
        int min = first.height.min;
        int max = first.height.max;
        int rad = first.radiusY.max;

        while (iterator.hasNext()) {
            final GiantSphereConfig next = iterator.next();
            min = Math.min(min, next.height.min);
            max = Math.max(max, next.height.max);
            rad = Math.max(rad, next.radiusY.max);
        }
        return new Range(Math.max(0, min - rad), Math.min(255, max + rad));
    }
}
