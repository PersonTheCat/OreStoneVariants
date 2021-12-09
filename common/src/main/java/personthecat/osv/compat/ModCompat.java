package personthecat.osv.compat;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import personthecat.catlib.util.McUtils;
import personthecat.osv.compat.collector.DecoratorCollector;
import personthecat.osv.compat.collector.FeatureCollector;
import personthecat.osv.compat.collector.create.CreateClusterCollector;
import personthecat.osv.compat.collector.create.CreateClusterDecoratorCollector;
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

    public static final String CREATE_MOD = "create";

    public static final Set<FeatureCollector<?, ?>> FEATURE_COLLECTORS = getPossibleFeatures();
    public static final Multimap<Class<?>, DecoratorCollector<?, ?>> DECORATOR_COLLECTORS = getPossibleDecorators();
    private static final List<ResourceLocation> DEFAULT_ORES = getDefaultOres();

    public static ResourceLocation getRandomOreId() {
        return DEFAULT_ORES.get((int) (Math.random() * ((double) DEFAULT_ORES.size() - 1.0)));
    }

    private static Set<FeatureCollector<?, ?>> getPossibleFeatures() {
        final ImmutableSet.Builder<FeatureCollector<?, ?>> features = ImmutableSet.builder();
        features.add(new ClusterFeatureCollector());
        if (McUtils.isModLoaded(CREATE_MOD)) {
            CreateClusterCollector.getInstance().ifPresent(features::add);
        }
        return features.build();
    }

    private static Multimap<Class<?>, DecoratorCollector<?, ?>> getPossibleDecorators() {
        final ImmutableMultimap.Builder<Class<?>, DecoratorCollector<?, ?>> decorators = ImmutableMultimap.builder();
        decorators.put(FlexibleDecoratorSettings.class, new FlexibleDecoratorCollector());
        if (McUtils.isModLoaded(CREATE_MOD)) {
            CreateClusterDecoratorCollector.getInstance()
                .ifPresent(d -> decorators.put(FlexibleDecoratorCollector.class, d));
        }
        return decorators.build();
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
