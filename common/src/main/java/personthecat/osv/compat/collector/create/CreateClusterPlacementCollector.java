package personthecat.osv.compat.collector.create;

import dev.architectury.injectables.annotations.ExpectPlatform;
import personthecat.osv.compat.collector.PlacementCollector;
import personthecat.osv.preset.data.FlexiblePlacementSettings;
import personthecat.osv.preset.data.FlexiblePlacementSettings.FlexiblePlacementSettingsBuilder;

import java.util.Optional;

public abstract class CreateClusterPlacementCollector extends PlacementCollector<FlexiblePlacementSettings, FlexiblePlacementSettingsBuilder> {

    protected CreateClusterPlacementCollector() {
        super(FlexiblePlacementSettings::builder, FlexiblePlacementSettingsBuilder::build);
    }

    @ExpectPlatform
    public static Optional<CreateClusterPlacementCollector> getInstance() {
        throw new AssertionError();
    }
}
