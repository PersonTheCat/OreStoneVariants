package personthecat.osv.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.ClusterSettings;
import personthecat.osv.preset.data.DecoratedFeatureSettings;
import java.util.List;
import java.util.Map;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;
import static personthecat.catlib.serialization.FieldDescriptor.field;

public class VariantClusterConfig implements FeatureConfiguration {

    public static final Codec<VariantClusterConfig> CODEC = codecOf(
        defaulted(Codec.INT, ClusterSettings.Fields.size, 8, c -> c.size),
        field(BlockMatchingSpawnConfig.MAP_CODEC, "blocks", c -> c.blocks),
        VariantClusterConfig::new
    );

    final int size;
    final Map<BlockState, List<BlockMatchingSpawnConfig>> blocks;

    public VariantClusterConfig(final int size, final Map<BlockState, List<BlockMatchingSpawnConfig>> blocks) {
        this.size = size;
        this.blocks = blocks;
    }

    public VariantClusterConfig(final int size, final DecoratedFeatureSettings<?, ?> cfg, final OrePreset preset) {
        this(size, BlockMatchingSpawnConfig.createMap(cfg, preset));
    }
}
