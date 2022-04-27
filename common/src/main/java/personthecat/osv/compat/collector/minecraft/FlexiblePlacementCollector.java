package personthecat.osv.compat.collector.minecraft;

import com.google.common.collect.ImmutableSet;
import lombok.extern.log4j.Log4j2;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import personthecat.catlib.data.Range;
import personthecat.osv.compat.collector.PlacementCollector;
import personthecat.osv.mixin.CountPlacementAccessor;
import personthecat.osv.mixin.HeightRangePlacementAccessor;
import personthecat.osv.mixin.RarityFilterAccessor;
import personthecat.osv.preset.data.FlexiblePlacementSettings;
import personthecat.osv.preset.data.FlexiblePlacementSettings.FlexiblePlacementSettingsBuilder;

import java.util.Set;

@Log4j2
public class FlexiblePlacementCollector extends PlacementCollector<FlexiblePlacementSettings, FlexiblePlacementSettingsBuilder> {

    private static final Set<Class<? extends PlacementModifier>> SUPPORTED_MODIFIERS =
        ImmutableSet.<Class<? extends PlacementModifier>>builder()
            .add(CountPlacement.class)
            .add(RarityFilter.class)
            .add(HeightRangePlacement.class)
            .build();

    public FlexiblePlacementCollector() {
        super(FlexiblePlacementSettings::builder, FlexiblePlacementSettingsBuilder::build);
    }

    @Override
    public boolean isPlacementSupported(final PlacementModifier modifier) {
        return SUPPORTED_MODIFIERS.contains(modifier.getClass());
    }

    @Override
    public void collectPlacement(final FlexiblePlacementSettingsBuilder builder, final PlacementModifier modifier) {
        if (modifier instanceof CountPlacement) {
            final IntProvider provider = ((CountPlacementAccessor) modifier).getCount();
            builder.count(Range.of(provider.getMinValue(), provider.getMaxValue()));
        } else if (modifier instanceof RarityFilter) {
            final int chance = ((RarityFilterAccessor) modifier).getChance();
            builder.chance(1.0 / (double) chance);
        } else if (modifier instanceof HeightRangePlacement) {
            final HeightProvider height = ((HeightRangePlacementAccessor) modifier).getHeight();
            // Todo: support height: { relative: <int | [int,int]> }
            log.info("Cannot collect height (unimplemented): {}", height);
        }
    }
}
