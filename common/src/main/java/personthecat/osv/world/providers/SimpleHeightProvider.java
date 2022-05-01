package personthecat.osv.world.providers;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import personthecat.catlib.data.Range;
import personthecat.catlib.serialization.codec.CodecUtils;
import personthecat.catlib.util.Shorthand;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
public class SimpleHeightProvider extends HeightProvider {

    public static final Codec<SimpleHeightProvider> CODEC =
        Range.CODEC.xmap(r -> new SimpleHeightProvider(r.min, r.max), p -> new Range(p.min, p.max));

    public static final HeightProviderType<?> TYPE = () -> CodecUtils.asParent(CODEC);

    public final int min;
    public final int max;

    public SimpleHeightProvider(final int min, final int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public int sample(final Random rand, final WorldGenerationContext ctx) {
        return Shorthand.numBetween(rand, this.min, this.max);
    }

    @Override
    public HeightProviderType<?> getType() {
        return TYPE;
    }
}
