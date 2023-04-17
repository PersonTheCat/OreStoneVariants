package personthecat.osv.preset.reader;

import com.mojang.serialization.Codec;
import net.minecraft.util.valueproviders.IntProvider;
import personthecat.catlib.serialization.codec.SimpleEitherCodec;
import personthecat.osv.world.providers.SimpleCount;

public final class IntProviderReader {

    public static final Codec<IntProvider> CODEC =
        new SimpleEitherCodec<>(SimpleCount.CODEC, IntProvider.CODEC)
            .withEncoder(a -> a instanceof SimpleCount ? SimpleCount.CODEC : IntProvider.CODEC);

    private IntProviderReader() {}
}
