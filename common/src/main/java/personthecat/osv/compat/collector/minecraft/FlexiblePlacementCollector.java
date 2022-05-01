package personthecat.osv.compat.collector.minecraft;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.VeryBiasedToBottomHeight;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import personthecat.osv.compat.collector.PlacementCollector;
import personthecat.osv.config.Cfg;
import personthecat.osv.mixin.CountPlacementAccessor;
import personthecat.osv.mixin.HeightRangePlacementAccessor;
import personthecat.osv.mixin.RarityFilterAccessor;
import personthecat.osv.preset.data.FlexiblePlacementSettings;
import personthecat.osv.preset.data.FlexiblePlacementSettings.FlexiblePlacementSettingsBuilder;
import personthecat.osv.preset.reader.CommonHeightAccessor;
import personthecat.osv.world.providers.OffsetHeightProvider;
import personthecat.osv.world.providers.SimpleCountProvider;
import personthecat.osv.world.providers.SimpleHeightProvider;

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
            builder.height(Cfg.highAccuracy() ? height : toOsvHeight(height));
            if (height instanceof BiasedToBottomHeight) {
                builder.bias(1);
            } else if (height instanceof VeryBiasedToBottomHeight) {
                builder.bias(2);
            }
        } else {
            builder.modifier(modifier);
        }
    }

    protected IntProvider toOsvCount(final IntProvider count) {
        return new SimpleCountProvider(count.getMinValue(), count.getMaxValue());
    }

    protected HeightProvider toOsvHeight(final HeightProvider height) {
        if (height instanceof CommonHeightAccessor accessor) {
            final VerticalAnchor min = accessor.getMinInclusive();
            final VerticalAnchor max = accessor.getMaxInclusive();
            if (min instanceof VerticalAnchor.Absolute aMin && max instanceof VerticalAnchor.Absolute aMax) {
                return new SimpleHeightProvider(aMin.y(), aMax.y());
            }
            return new OffsetHeightProvider(
                this.getOffset(accessor.getMinInclusive()), this.getOffset(accessor.getMaxInclusive()));
        }
        return height;
    }

    protected int getOffset(final VerticalAnchor anchor) {
        if (anchor instanceof VerticalAnchor.AboveBottom bottom) {
            return bottom.offset();
        } else if (anchor instanceof VerticalAnchor.BelowTop top) {
            return Math.max(-1, -top.offset());
        }
        return 0;
    }
}
