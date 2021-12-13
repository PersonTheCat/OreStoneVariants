package personthecat.osv.world.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import personthecat.catlib.data.Range;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static personthecat.catlib.serialization.CodecUtils.easySet;

public class GiantClusterCollection implements CarverConfiguration {

    public static final Codec<GiantClusterCollection> CODEC =
        easySet(GiantClusterConfig.CODEC).xmap(GiantClusterCollection::new, c -> c.configs);

    final Set<GiantClusterConfig> configs;
    final Range bounds;

    public GiantClusterCollection(final Collection<GiantClusterConfig> configs) {
        this.configs = ImmutableSet.copyOf(configs);
        this.bounds = getBounds(configs);
    }

    private static Range getBounds(final Collection<GiantClusterConfig> configs) {
        if (configs.size() == 0) return Range.empty();

        final Iterator<GiantClusterConfig> iterator = configs.iterator();
        final GiantClusterConfig first = iterator.next();
        int min = first.height.min;
        int max = first.height.max;
        int rad = (int) (((double) first.radiusX.max) * (1.0 + first.amplitude));

        while (iterator.hasNext()) {
            final GiantClusterConfig next = iterator.next();
            min = Math.min(min, next.height.min);
            max = Math.max(max, next.height.max);
            rad = Math.max(rad, (int) (((double) next.radiusX.max) * (1.0 + next.amplitude)));
        }
        return new Range(Math.max(0, min - rad), Math.min(255, max + rad));
    }
}
