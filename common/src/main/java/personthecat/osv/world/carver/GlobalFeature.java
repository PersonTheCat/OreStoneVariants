package personthecat.osv.world.carver;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Random;

@ParametersAreNonnullByDefault
public abstract class GlobalFeature<C extends CarverConfiguration> extends WorldCarver<C> {

    public GlobalFeature(final Codec<C> codec) {
        super(codec);
    }

    public abstract ConfiguredWorldCarver<?> configured(final Collection<FeatureStem> configs);

    @Override
    public boolean isStartChunk(C cfg, Random rand) {
        return true;
    }
}
