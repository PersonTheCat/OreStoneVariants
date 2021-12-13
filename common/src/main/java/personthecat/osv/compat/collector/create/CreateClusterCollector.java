package personthecat.osv.compat.collector.create;

import dev.architectury.injectables.annotations.ExpectPlatform;
import personthecat.osv.compat.collector.FeatureCollector;
import personthecat.osv.preset.data.ClusterSettings;
import personthecat.osv.preset.data.ClusterSettings.ClusterSettingsBuilder;

import java.util.Optional;

public abstract class CreateClusterCollector extends FeatureCollector<ClusterSettings, ClusterSettingsBuilder> {

    protected CreateClusterCollector() {
        super(ClusterSettings::builder, ClusterSettingsBuilder::build);
    }

    @ExpectPlatform
    public static Optional<CreateClusterCollector> getInstance() {
        throw new AssertionError();
    }
}
