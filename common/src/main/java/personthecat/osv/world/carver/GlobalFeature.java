package personthecat.osv.world.carver;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import java.util.Collection;
import java.util.Random;

public abstract class GlobalFeature<C extends CarverConfiguration> extends WorldCarver<C> {

    public GlobalFeature(final Codec<C> codec, int height) {
        super(codec, height);
    }

    public abstract ConfiguredWorldCarver<?> configured(final Collection<FeatureStem> configs);

    @Override
    public boolean isStartChunk(Random random, int i, int j, C cfg) {
        return true;
    }

    @Override
    protected boolean skip(double d, double e, double f, int i) {
        return false;
    }
}
