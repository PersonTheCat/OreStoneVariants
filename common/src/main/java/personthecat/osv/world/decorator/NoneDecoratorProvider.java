package personthecat.osv.world.decorator;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public interface NoneDecoratorProvider<T> extends DecoratorProvider<T> {

    @Override
    default ConfiguredFeature<?, ?> decorate(final ConfiguredFeature<?, ?> feature) {
        return feature;
    }
}
