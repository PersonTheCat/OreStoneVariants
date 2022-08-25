package personthecat.osv.compat.collector.create.forge;

import com.simibubi.create.foundation.worldgen.ConfigDrivenPlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import personthecat.osv.compat.collector.create.CreateClusterPlacementCollector;
import personthecat.osv.preset.data.FlexiblePlacementSettings.FlexiblePlacementSettingsBuilder;
import personthecat.osv.world.providers.SimpleCountProvider;
import personthecat.osv.world.providers.SimpleHeightProvider;

import java.util.Optional;

public class CreateClusterPlacementCollectorImpl extends CreateClusterPlacementCollector {

    private static final CreateClusterPlacementCollector INSTANCE = new CreateClusterPlacementCollectorImpl();

    private CreateClusterPlacementCollectorImpl() {}

    public static Optional<CreateClusterPlacementCollector> getInstance() {
        return Optional.of(INSTANCE);
    }

    @Override
    public boolean isPlacementSupported(final PlacementModifier modifier) {
        return modifier instanceof ConfigDrivenPlacement;
    }

    @Override
    public void collectPlacement(final FlexiblePlacementSettingsBuilder builder, final PlacementModifier modifier) {
        if (modifier instanceof ConfigDrivenPlacement create) {
            builder.count(new SimpleCountProvider(0, (int) create.getFrequency()));
            builder.height(new SimpleHeightProvider(create.getMinY(), create.getMaxY()));
        }
    }
}
