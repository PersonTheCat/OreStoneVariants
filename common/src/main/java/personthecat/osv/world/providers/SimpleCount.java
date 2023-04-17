package personthecat.osv.world.providers;

import com.mojang.serialization.Codec;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;
import personthecat.catlib.data.Range;
import personthecat.catlib.serialization.codec.CodecUtils;
import personthecat.catlib.util.Shorthand;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
public class SimpleCount extends IntProvider {

    public static final Codec<SimpleCount> CODEC =
        Range.CODEC.xmap(r -> new SimpleCount(r.min, r.max), p -> new Range(p.min, p.max));

    public static final IntProviderType<?> TYPE = () -> CodecUtils.asParent(CODEC);

    public final int min;
    public final int max;

    public SimpleCount(final int min, final int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public int sample(final Random rand) {
        return Shorthand.numBetween(rand, this.min, this.max);
    }

    @Override
    public int getMinValue() {
        return this.min;
    }

    @Override
    public int getMaxValue() {
        return this.max;
    }

    @Override
    public IntProviderType<?> getType() {
        return TYPE;
    }
}
