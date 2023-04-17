package personthecat.osv.preset.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import personthecat.osv.preset.reader.CommonHeightAccessor;
import personthecat.osv.preset.reader.HeightProviderReader;
import personthecat.osv.preset.reader.IntProviderReader;
import personthecat.osv.world.placement.PlacementProvider;
import personthecat.osv.world.placement.FlexiblePlacementModifier;
import personthecat.osv.world.providers.SimpleCount;

import java.util.List;

import static personthecat.catlib.serialization.codec.CodecUtils.codecOf;
import static personthecat.catlib.serialization.codec.CodecUtils.easyList;
import static personthecat.catlib.serialization.codec.FieldDescriptor.defaulted;

@Value
@Builder
@FieldNameConstants
public class FlexiblePlacementSettings implements PlacementProvider<FlexiblePlacementSettings> {

    public static final FlexiblePlacementSettings DEFAULTS = FlexiblePlacementSettings.builder().build();
    public static final HeightProvider DEFAULT_HEIGHT =
        UniformHeight.of(VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(128));

    @Default int spread = 0;
    @Default double chance = 1.0;
    @Default int bias = 0;
    @Default int plateau = Integer.MIN_VALUE;
    @Default IntProvider count = new SimpleCount(2, 2);
    @Default HeightProvider height = DEFAULT_HEIGHT;
    @Singular List<PlacementModifier> modifiers;

    public static final Codec<FlexiblePlacementSettings> CODEC = codecOf(
        defaulted(Codec.INT, Fields.spread, 0, FlexiblePlacementSettings::getSpread),
        defaulted(Codec.DOUBLE, Fields.chance, 1.0, FlexiblePlacementSettings::getChance),
        defaulted(Codec.INT, Fields.bias, 0, FlexiblePlacementSettings::getBias),
        defaulted(Codec.INT, Fields.plateau, Integer.MIN_VALUE, FlexiblePlacementSettings::getPlateau),
        defaulted(IntProviderReader.CODEC, Fields.count, new SimpleCount(2, 2), FlexiblePlacementSettings::getCount),
        defaulted(HeightProviderReader.CODEC, Fields.height, DEFAULT_HEIGHT, FlexiblePlacementSettings::getHeight),
        defaulted(easyList(PlacementModifier.CODEC), Fields.modifiers, List.of(), FlexiblePlacementSettings::getModifiers),
        FlexiblePlacementSettings::new
    );

    @Override
    public List<PlacementModifier> createModifiers() {
        final ImmutableList.Builder<PlacementModifier> modifiers = ImmutableList.builder();
        modifiers.add(new FlexiblePlacementModifier(this.count, this.buildHeightProvider(), this.bias, this.chance));
        modifiers.addAll(this.modifiers);
        return modifiers.build();
    }

    private HeightProvider buildHeightProvider() {
        if (this.plateau != Integer.MIN_VALUE && this.height instanceof CommonHeightAccessor a) {
            return TrapezoidHeight.of(a.getMaxInclusive(), a.getMaxInclusive(), this.plateau);
        }
        return this.height;
    }

    @Override
    public Codec<FlexiblePlacementSettings> codec() {
        return CODEC;
    }
}
