package personthecat.osv.preset.reader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Encoder;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import personthecat.catlib.serialization.codec.SimpleAnyCodec;
import personthecat.osv.preset.data.FlexibleHeightSettings;
import personthecat.osv.world.providers.SimpleHeight;

public final class HeightProviderReader {

    public static final Codec<HeightProvider> CODEC =
        new SimpleAnyCodec<>(SimpleHeight.CODEC, FlexibleHeightSettings.HEIGHT_PROVIDER_CODEC, HeightProvider.CODEC)
            .withEncoder(HeightProviderReader::selectEncoder);

    private static Encoder<? extends HeightProvider> selectEncoder(final HeightProvider provider) {
        if (provider instanceof SimpleHeight) {
            return SimpleHeight.CODEC;
        } else if (FlexibleHeightSettings.isSupportedProvider(provider)) {
            return FlexibleHeightSettings.HEIGHT_PROVIDER_CODEC;
        }
        return HeightProvider.CODEC;
    }

    private HeightProviderReader() {}
}