package personthecat.osv.compat;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import personthecat.osv.compat.collector.DecoratorCollector;
import personthecat.osv.compat.collector.FeatureCollector;
import personthecat.osv.compat.collector.minecraft.ClusterFeatureCollector;
import personthecat.osv.compat.collector.minecraft.FlexibleDecoratorCollector;
import personthecat.osv.config.Cfg;
import personthecat.osv.config.DefaultOres;
import personthecat.osv.preset.data.FlexibleDecoratorSettings;
import personthecat.osv.util.Group;

import java.util.ArrayList;
import java.util.List;
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

    private static final List<ResourceLocation> DEFAULT_ORES = getDefaultOres();

    public static ResourceLocation getRandomOreId() {
        return DEFAULT_ORES.get((int) (Math.random() * ((double) DEFAULT_ORES.size() - 1.0)));
    }

    private static List<ResourceLocation> getDefaultOres() {
        final List<ResourceLocation> defaultOres = new ArrayList<>();
        addLoaded(defaultOres, DefaultOres.LISTED);
        addLoaded(defaultOres, DefaultOres.UNLISTED);
        return defaultOres;
    }

    private static void addLoaded(final List<ResourceLocation> ores, final Group... groups) {
        for (final Group group : groups) {
            if (Cfg.modEnabled(group.getName())) {
                ores.addAll(group.ids());
            }
        }
    }
}
