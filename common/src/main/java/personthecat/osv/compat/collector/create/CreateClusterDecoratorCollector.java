package personthecat.osv.compat.collector.create;

import dev.architectury.injectables.annotations.ExpectPlatform;
import personthecat.osv.compat.collector.DecoratorCollector;
import personthecat.osv.preset.data.FlexibleDecoratorSettings;
import personthecat.osv.preset.data.FlexibleDecoratorSettings.FlexibleDecoratorSettingsBuilder;

import java.util.Optional;

public abstract class CreateClusterDecoratorCollector extends DecoratorCollector<FlexibleDecoratorSettings, FlexibleDecoratorSettingsBuilder> {

    protected CreateClusterDecoratorCollector() {
        super(FlexibleDecoratorSettings::builder, FlexibleDecoratorSettingsBuilder::build);
    }

    @ExpectPlatform
    public static Optional<CreateClusterDecoratorCollector> getInstance() {
        throw new AssertionError();
    }
}
