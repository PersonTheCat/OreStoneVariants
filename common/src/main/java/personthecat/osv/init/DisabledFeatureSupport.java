package personthecat.osv.init;

import lombok.extern.log4j.Log4j2;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import personthecat.catlib.event.registry.DynamicRegistries;
import personthecat.osv.config.Cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Log4j2
public class DisabledFeatureSupport {

    /**
     * Generates a list of {@link ConfiguredFeature} resource locations to be disabled by the
     * mod. This list is also immutable so that the the holding registry is still safe.
     *
     * @return A list of disabled configured features.
     */
    public static List<ConfiguredFeature<?, ?>> loadDisabledFeatures() {
        final List<ConfiguredFeature<?, ?>> disabledFeatures = new ArrayList<>();
        for (final String id : Cfg.disabledFeatures()) {
            final ResourceLocation key = new ResourceLocation(id);
            final Feature<?> feature = Registry.FEATURE.get(key);
            if (feature != null) {
                for (final ConfiguredFeature<?, ?> cf : DynamicRegistries.CONFIGURED_FEATURES) {
                    if (cf.getFeatures().anyMatch(f -> feature.equals(f.feature))) {
                        disabledFeatures.add(cf);
                    }
                }
            } else if (DynamicRegistries.CONFIGURED_FEATURES.isRegistered(key)) {
                final ConfiguredFeature<?, ?> cf = DynamicRegistries.CONFIGURED_FEATURES.lookup(key);
                disabledFeatures.add(Objects.requireNonNull(cf));
            } else {
                log.error("Invalid feature id. Cannot disable: {}", id);
            }
        }
        return disabledFeatures;
    }
}
