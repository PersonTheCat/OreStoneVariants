package personthecat.osv.compat;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import personthecat.osv.compat.collector.minecraft.ClusterFeatureCollector;
import personthecat.osv.compat.collector.minecraft.FlexibleDecoratorCollector;
import personthecat.osv.preset.collector.DecoratorCollector;
import personthecat.osv.preset.collector.FeatureCollector;
import personthecat.osv.preset.data.FlexibleDecoratorSettings;

import java.util.Set;

public class ModCompat {

    public static final Set<FeatureCollector<?, ?>> FEATURE_COLLECTORS =
        ImmutableSet.<FeatureCollector<?, ?>>builder()
            .add(new ClusterFeatureCollector())
            .build();

    public static final Multimap<Class<?>, DecoratorCollector<?, ?>> DECORATOR_COLLECTORS =
        ImmutableMultimap.<Class<?>, DecoratorCollector<?, ?>>builder()
            .put(FlexibleDecoratorSettings.class, new FlexibleDecoratorCollector())
            .build();
}
