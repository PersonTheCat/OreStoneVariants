package personthecat.osv.preset.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import personthecat.osv.preset.reader.HeightProviderReader;
import personthecat.osv.preset.reader.IntProviderReader;
import personthecat.osv.world.placement.PlacementProvider;
import personthecat.osv.world.placement.FlexiblePlacementModifier;
import personthecat.osv.world.providers.OffsetHeightProvider;
import personthecat.osv.world.providers.SimpleCountProvider;

import java.util.List;

import static personthecat.catlib.serialization.codec.CodecUtils.codecOf;
import static personthecat.catlib.serialization.codec.CodecUtils.easyList;
import static personthecat.catlib.serialization.codec.FieldDescriptor.defaulted;

@Value
@Builder
@FieldNameConstants
public class FlexiblePlacementSettings implements PlacementProvider<FlexiblePlacementSettings> {

    public static final FlexiblePlacementSettings DEFAULTS = FlexiblePlacementSettings.builder().build();

    @Default int spread = 0;
    @Default double chance = 1.0;
    @Default int bias = 0;
    @Default IntProvider count = new SimpleCountProvider(2, 2);
    @Default HeightProvider height = new OffsetHeightProvider(0, 128);
    @Singular List<PlacementModifier> modifiers;

    public static final Codec<FlexiblePlacementSettings> CODEC = codecOf(
        defaulted(Codec.INT, Fields.spread, 0, FlexiblePlacementSettings::getSpread),
        defaulted(Codec.DOUBLE, Fields.chance, 1.0, FlexiblePlacementSettings::getChance),
        defaulted(Codec.INT, Fields.bias, 0, FlexiblePlacementSettings::getBias),
        defaulted(IntProviderReader.CODEC, Fields.count, new SimpleCountProvider(2, 2), FlexiblePlacementSettings::getCount),
        defaulted(HeightProviderReader.CODEC, Fields.height, new OffsetHeightProvider(0, 416), FlexiblePlacementSettings::getHeight),
        defaulted(easyList(PlacementModifier.CODEC), Fields.modifiers, List.of(), FlexiblePlacementSettings::getModifiers),
        FlexiblePlacementSettings::new
    );

    @Override
    public List<PlacementModifier> createModifiers() {
        final ImmutableList.Builder<PlacementModifier> modifiers = ImmutableList.builder();
        modifiers.add(new FlexiblePlacementModifier(this.count, this.height, this.bias, this.chance));
        modifiers.addAll(this.modifiers);
        return modifiers.build();
    }

    @Override
    public Codec<FlexiblePlacementSettings> codec() {
        return CODEC;
    }
}
