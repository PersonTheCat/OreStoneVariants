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
public class OffsetHeightProvider extends HeightProvider {

    public static final Codec<OffsetHeightProvider> CODEC =
        Range.CODEC.xmap(r -> new OffsetHeightProvider(r.min, r.max), p -> new Range(p.bottom, p.top))
            .fieldOf("offset")
            .codec();

    public static final HeightProviderType<?> TYPE = () -> CodecUtils.asParent(CODEC);

    public final int bottom;
    public final int top;

    public OffsetHeightProvider(final int min, final int max) {
        this.bottom = min;
        this.top = max;
    }

    @Override
    public int sample(final Random rand, final WorldGenerationContext ctx) {
        final int min = ctx.getMinGenY();
        final int max = ctx.getGenDepth() + min - 1;
        return min + Shorthand.numBetween(rand,
            getOffset(this.bottom, min, max) - min, getOffset(this.top, min, max)) - min;
    }

    private static int getOffset(final int offset, final int min, final int max) {
        return offset < 0 ? max + offset : min + offset;
    }

    @Override
    public HeightProviderType<?> getType() {
        return TYPE;
    }
}
