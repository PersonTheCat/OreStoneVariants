package personthecat.osv.world.decorator;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import personthecat.catlib.data.Range;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;

public class FlexibleDecoratorConfig implements DecoratorConfiguration {

    public static final Codec<FlexibleDecoratorConfig> CODEC = codecOf(
        defaulted(Range.CODEC, "count", Range.of(8, 8), c -> c.count),
        defaulted(Range.CODEC, "height", Range.of(0, 32), c -> c.height),
        defaulted(Codec.DOUBLE, "chance", 1.0, c -> c.chance),
        FlexibleDecoratorConfig::new
    );

    final Range count;
    final Range height;
    final double chance;

    public FlexibleDecoratorConfig(final Range count, final Range height, final double chance) {
        this.count = count;
        this.height = height;
        this.chance = chance;
    }
}
