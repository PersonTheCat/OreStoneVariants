package personthecat.osv.preset.reader;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import personthecat.catlib.serialization.codec.SimpleAnyCodec;
import personthecat.osv.world.providers.OffsetHeightProvider;
import personthecat.osv.world.providers.SimpleHeightProvider;

public final class HeightProviderReader {

    public static final Codec<HeightProvider> CODEC =
        new SimpleAnyCodec<>(SimpleHeightProvider.CODEC, OffsetHeightProvider.CODEC, HeightProvider.CODEC)
            .withEncoder(a -> {
                if (a instanceof SimpleHeightProvider) {
                    return SimpleHeightProvider.CODEC;
                } else if (a instanceof OffsetHeightProvider) {
                    return OffsetHeightProvider.CODEC;
                }
                return HeightProvider.CODEC;
            });

    private HeightProviderReader() {}
}
