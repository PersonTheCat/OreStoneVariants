package personthecat.osv.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.ClusterSettings;
import personthecat.osv.preset.data.DecoratedFeatureSettings;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;
import static personthecat.catlib.serialization.FieldDescriptor.field;

public class ClusterConfig implements FeatureConfiguration {

    public static final Codec<ClusterConfig> CODEC = codecOf(
        defaulted(Codec.INT, ClusterSettings.Fields.size, 8, c -> c.size),
        field(BlockPlacer.EITHER_CODEC, "placer", c -> c.placer),
        ClusterConfig::new
    );

    final int size;
    final BlockPlacer placer;

    public ClusterConfig(final int size, final BlockPlacer placer) {
        this.size = size;
        this.placer = placer;
    }

    public ClusterConfig(final int size, final DecoratedFeatureSettings<?, ?> cfg, final OrePreset preset) {
        this(size, new VariantBlockPlacer(cfg, preset));
    }
}
