package personthecat.osv.world.providers;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import personthecat.catlib.data.Range;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.serialization.codec.CodecUtils;
import personthecat.catlib.util.Shorthand;
import personthecat.osv.exception.OffsetOutOfBoundsException;
import personthecat.osv.util.Reference;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
public class OffsetHeightProvider extends HeightProvider {

    public static final String FIELD = "offset";

    public static final Codec<OffsetHeightProvider> CODEC =
        Range.CODEC.xmap(r -> new OffsetHeightProvider(r.min, r.max), p -> new Range(p.bottom, p.top))
            .fieldOf(FIELD)
            .codec();

    public static final HeightProviderType<?> TYPE = () -> CodecUtils.asParent(CODEC);

    public final int bottom;
    public final int top;
    private volatile boolean warned;

    public OffsetHeightProvider(final int min, final int max) {
        this.bottom = min;
        this.top = max;
        this.warned = false;
    }

    @Override
    public int sample(final Random rand, final WorldGenerationContext ctx) {
        final int min = ctx.getMinGenY();
        final int max = ctx.getGenDepth() + min - 1;
        final int a = getBound(this.bottom, min, max);
        final int b = getBound(this.top, min, max);
        return a < b ? this.getFromBounds(rand, min, max, a, b) : this.getFromBounds(rand, min, max, b, a);
    }

    private static int getBound(final int offset, final int min, final int max) {
        return offset < 0 ? max + offset : min + offset;
    }

    private int getFromBounds(final Random rand, final int min, final int max, final int lower, final int upper) {
        if (lower < min || upper > max) {
            if (!this.warned) {
                LibErrorContext.warn(Reference.MOD, new OffsetOutOfBoundsException(this, min, max, lower, upper));
                this.warned = true;
            }
            return 0;
        }
        return Shorthand.numBetween(rand, lower - min, upper - min) + min;
    }

    @Override
    public HeightProviderType<?> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "OffsetHeight[" + this.bottom + "," + this.top + "]";
    }
}
