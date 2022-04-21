package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.world.feature.ClusterConfig;
import personthecat.osv.world.feature.ClusterFeature;
import personthecat.osv.world.feature.FeatureProvider;
import personthecat.osv.world.placer.StoneBlockPlacer;
import personthecat.osv.world.placer.VariantBlockPlacer;

import static personthecat.catlib.serialization.codec.CodecUtils.codecOf;
import static personthecat.catlib.serialization.codec.FieldDescriptor.defaulted;

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
    public ConfiguredFeature<?, ?> createOreFeature(final OrePreset ore, final PlacedFeatureSettings<?, ?> cfg) {
        return new ConfiguredFeature<>(ClusterFeature.INSTANCE, new ClusterConfig(this.size, new VariantBlockPlacer(cfg, ore)));
    }

    @Override
    public ConfiguredFeature<?, ?> createStoneFeature(final StonePreset stone, final PlacedFeatureSettings<?, ?> cfg) {
        return new ConfiguredFeature<>(ClusterFeature.INSTANCE, new ClusterConfig(this.size, new StoneBlockPlacer(stone)));
    }

    @Override
    public Codec<ClusterSettings> codec() {
        return CODEC;
    }
}
