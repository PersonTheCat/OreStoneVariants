package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import personthecat.osv.world.placement.PlacementProvider;

import java.util.Collections;

public class EmptyDecoratorSettings implements PlacementProvider<EmptyDecoratorSettings> {

    public static final EmptyDecoratorSettings INSTANCE = new EmptyDecoratorSettings();

    public static final Codec<EmptyDecoratorSettings> CODEC = Codec.unit(INSTANCE);

    private EmptyDecoratorSettings() {};

    @Override
    public PlacedFeature place(final ConfiguredFeature<?, ?> feature) {
        return new PlacedFeature(Holder.direct(feature), Collections.emptyList());
    }

    @Override
    public Codec<EmptyDecoratorSettings> codec() {
        return CODEC;
    }
}
