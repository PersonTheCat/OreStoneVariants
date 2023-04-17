package personthecat.osv.preset.reader;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import personthecat.catlib.serialization.codec.SimpleAnyCodec;
import personthecat.osv.preset.data.SimpleHeightSettings;
import personthecat.osv.world.providers.SimpleHeightProvider;

public final class HeightProviderReader {

    public static final Codec<HeightProvider> CODEC =
        new SimpleAnyCodec<>(SimpleHeightProvider.CODEC, SimpleHeightSettings.HEIGHT_PROVIDER_CODEC, HeightProvider.CODEC)
            .withEncoder(SimpleHeightSettings.HEIGHT_PROVIDER_CODEC);

    private HeightProviderReader() {}
}
