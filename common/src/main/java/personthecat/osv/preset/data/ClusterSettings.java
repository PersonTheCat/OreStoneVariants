package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.world.feature.FeatureProvider;
import personthecat.osv.world.feature.VariantClusterConfig;
import personthecat.osv.world.feature.VariantClusterFeature;

import static personthecat.catlib.serialization.FieldDescriptor.defaulted;
import static personthecat.catlib.serialization.CodecUtils.codecOf;

@Value
@Builder
@FieldNameConstants
public class ClusterSettings implements FeatureProvider<ClusterSettings> {

    @Default int size = 8;

    public static final Codec<ClusterSettings> CODEC = codecOf(
        defaulted(Codec.INT, Fields.size, 8, ClusterSettings::getSize),
        ClusterSettings::new
    );

    @Override
    public ConfiguredFeature<?, ?> createOreFeature(final OrePreset ore, final DecoratedFeatureSettings<?, ?> cfg) {
        return VariantClusterFeature.INSTANCE.configured(new VariantClusterConfig(this.size, cfg, ore));
    }

    @Override
    public ConfiguredFeature<?, ?> createStoneFeature(final StonePreset stone, final DecoratedFeatureSettings<?, ?> cfg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Codec<ClusterSettings> codec() {
        return CODEC;
    }
}
