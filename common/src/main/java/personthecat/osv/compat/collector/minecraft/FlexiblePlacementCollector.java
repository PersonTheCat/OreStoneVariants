package personthecat.osv.compat.collector.minecraft;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.VeryBiasedToBottomHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import personthecat.osv.compat.collector.PlacementCollector;
import personthecat.osv.config.Cfg;
import personthecat.osv.mixin.CountPlacementAccessor;
import personthecat.osv.mixin.HeightRangePlacementAccessor;
import personthecat.osv.mixin.RarityFilterAccessor;
import personthecat.osv.mixin.TrapezoidHeightAccessor;
import personthecat.osv.preset.data.FlexiblePlacementSettings;
import personthecat.osv.preset.data.FlexiblePlacementSettings.FlexiblePlacementSettingsBuilder;
import personthecat.osv.world.placement.FlexiblePlacementModifier;
import personthecat.osv.world.providers.SimpleCount;

import java.util.Set;

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
            final IntProvider count = ((CountPlacementAccessor) modifier).getCount();
            builder.count(Cfg.highAccuracy() ? count : toOsvCount(count));
        } else if (modifier instanceof RarityFilter) {
            final int chance = ((RarityFilterAccessor) modifier).getChance();
            builder.chance(1.0 / (double) chance);
        } else if (modifier instanceof HeightRangePlacement) {
            final HeightProvider height = ((HeightRangePlacementAccessor) modifier).getHeight();
            builder.height(height);
            if (height instanceof BiasedToBottomHeight) {
                builder.bias(1);
            } else if (height instanceof VeryBiasedToBottomHeight) {
                builder.bias(2);
            } else if (height instanceof TrapezoidHeight) {
                builder.plateau(((TrapezoidHeightAccessor) height).getPlateau());
            }
        } else if (this.isSupportedMiscellaneousType(modifier)) {
            builder.modifier(modifier);
        }
    }

    protected boolean isSupportedMiscellaneousType(final PlacementModifier m) {
        return !(m instanceof BiomeFilter || m instanceof InSquarePlacement || m instanceof FlexiblePlacementModifier);
    }

    protected IntProvider toOsvCount(final IntProvider count) {
        return new SimpleCount(count.getMinValue(), count.getMaxValue());
    }
}
