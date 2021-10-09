package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import personthecat.osv.world.decorator.DecoratorProvider;

public class EmptyDecoratorSettings implements DecoratorProvider<EmptyDecoratorSettings> {

    public static final EmptyDecoratorSettings INSTANCE = new EmptyDecoratorSettings();

    public static final Codec<EmptyDecoratorSettings> CODEC = Codec.unit(INSTANCE);

    private EmptyDecoratorSettings() {};

    @Override
    public ConfiguredFeature<?, ?> decorate(final ConfiguredFeature<?, ?> feature) {
        return feature;
    }

    @Override
    public Codec<EmptyDecoratorSettings> codec() {
        return CODEC;
    }
}
