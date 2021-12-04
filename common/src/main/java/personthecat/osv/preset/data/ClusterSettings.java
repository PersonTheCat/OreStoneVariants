package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.world.feature.ClusterConfig;
import personthecat.osv.world.feature.ClusterFeature;
import personthecat.osv.world.feature.FeatureProvider;
import personthecat.osv.world.placer.VariantBlockPlacer;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;

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
        return ClusterFeature.INSTANCE.configured(new ClusterConfig(this.size, new VariantBlockPlacer(cfg, ore)));
    }

    @Override
    public ConfiguredFeature<?, ?> createStoneFeature(final StonePreset stone, final DecoratedFeatureSettings<?, ?> cfg) {
        return Feature.ORE.configured(new OreConfiguration(stone.getSource(), stone.getStone(), this.size));
    }

    @Override
    public Codec<ClusterSettings> codec() {
        return CODEC;
    }
}
