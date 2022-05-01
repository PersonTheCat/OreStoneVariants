package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import personthecat.catlib.data.Range;
import personthecat.osv.world.placement.NoneDecoratorProvider;

import static personthecat.catlib.serialization.codec.CodecUtils.codecOf;
import static personthecat.catlib.serialization.codec.FieldDescriptor.defaulted;

@Value
@Builder
@FieldNameConstants
public class SimplePlacementSettings implements NoneDecoratorProvider<SimplePlacementSettings> {

    @Default double chance = 0.025;
    @Default int count = 1;
    @Default Range height = new Range(15, 40);

    public static final Codec<SimplePlacementSettings> CODEC = codecOf(
        defaulted(Codec.DOUBLE, Fields.chance,  0.025, SimplePlacementSettings::getChance),
        defaulted(Codec.INT, Fields.count, 1, SimplePlacementSettings::getCount),
        defaulted(Range.CODEC, Fields.height, new Range(15, 40), SimplePlacementSettings::getHeight),
        SimplePlacementSettings::new
    );

    @Override
    public Codec<SimplePlacementSettings> codec() {
        return CODEC;
    }
}
