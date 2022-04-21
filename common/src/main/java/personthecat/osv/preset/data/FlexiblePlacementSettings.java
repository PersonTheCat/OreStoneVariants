package personthecat.osv.preset.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import personthecat.catlib.data.Range;
import personthecat.osv.world.placement.PlacementProvider;
import personthecat.osv.world.placement.FlexiblePlacementModifier;

import static personthecat.catlib.serialization.codec.CodecUtils.codecOf;
import static personthecat.catlib.serialization.codec.FieldDescriptor.defaulted;

@Value
@Builder
@FieldNameConstants
public class FlexiblePlacementSettings implements PlacementProvider<FlexiblePlacementSettings> {

    public static final FlexiblePlacementSettings DEFAULTS = FlexiblePlacementSettings.builder().build();

    @Default int spread = 0;
    @Default double chance = 1.0;
    @Default int bias = 0;
    @Default Range count = new Range(2);
    @Default Range height = new Range(0, 32);

    public static final Codec<FlexiblePlacementSettings> CODEC = codecOf(
        defaulted(Codec.INT, Fields.spread, 0, FlexiblePlacementSettings::getSpread),
        defaulted(Codec.DOUBLE, Fields.chance, 1.0, FlexiblePlacementSettings::getChance),
        defaulted(Codec.INT, Fields.bias, 0, FlexiblePlacementSettings::getBias),
        defaulted(Range.CODEC, Fields.count, new Range(2), FlexiblePlacementSettings::getCount),
        defaulted(Range.CODEC, Fields.height, new Range(0, 32), FlexiblePlacementSettings::getHeight),
        FlexiblePlacementSettings::new
    );

    @Override
    public PlacedFeature place(final ConfiguredFeature<?, ?> feature) {
        // Todo: This feature is old now. Needs to be updated.
        final ImmutableList.Builder<PlacementModifier> modifiers = ImmutableList.builder();

        modifiers.add(new FlexiblePlacementModifier(this.count, this.height, this.bias, this.chance));
        return new PlacedFeature(Holder.direct(feature), modifiers.build());
    }

    @Override
    public Codec<FlexiblePlacementSettings> codec() {
        return CODEC;
    }
}
