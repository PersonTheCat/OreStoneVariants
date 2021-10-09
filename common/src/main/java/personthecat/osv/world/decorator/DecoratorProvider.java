package personthecat.osv.world.decorator;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import personthecat.osv.preset.data.DynamicSerializable;

public interface DecoratorProvider<T> extends DynamicSerializable<T> {
    ConfiguredFeature<?, ?> decorate(final ConfiguredFeature<?, ?> feature);
}
