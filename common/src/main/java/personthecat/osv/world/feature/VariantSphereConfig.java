package personthecat.osv.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import personthecat.catlib.data.Range;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.DecoratedFeatureSettings;
import personthecat.osv.preset.data.SphereSettings;

import java.util.Map;
import java.util.Set;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;
import static personthecat.catlib.serialization.FieldDescriptor.field;

public class VariantSphereConfig implements FeatureConfiguration {

    private static final Codec<VariantSphereConfig> UNVALIDATED = codecOf(
        defaulted(Range.CODEC, SphereSettings.Fields.radiusX, Range.of(3, 5), c -> c.radiusX),
        defaulted(Range.CODEC, SphereSettings.Fields.radiusY, Range.of(2, 4), c -> c.radiusY),
        defaulted(Range.CODEC, SphereSettings.Fields.radiusZ, Range.of(3, 5), c -> c.radiusZ),
        defaulted(Codec.DOUBLE, SphereSettings.Fields.integrity, 1.0, c -> c.integrity),
        field(BlockMatchingSpawnConfig.MAP_CODEC, "blocks", c -> c.blocks),
        VariantSphereConfig::new
    );

    public static final Codec<VariantSphereConfig> CODEC =
        UNVALIDATED.flatXmap(c -> SphereSettings.validate(c, c.radiusX, c.radiusY, c.radiusZ),
            c -> SphereSettings.validate(c, c.radiusX, c.radiusY, c.radiusZ));

    final Range radiusX;
    final Range radiusY;
    final Range radiusZ;
    final double integrity;
    final Map<BlockState, Set<BlockMatchingSpawnConfig>> blocks;

    public VariantSphereConfig(final Range radiusX, final Range radiusY, final Range radiusZ, final double integrity,
                               final Map<BlockState, Set<BlockMatchingSpawnConfig>> blocks) {
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
        this.integrity = integrity;
        this.blocks = blocks;
    }

    public VariantSphereConfig(final Range radiusX, final Range radiusY, final Range radiusZ, final double integrity,
                               final DecoratedFeatureSettings<?, ?> cfg, final OrePreset preset) {
        this(radiusX, radiusY, radiusZ, integrity, BlockMatchingSpawnConfig.createMap(cfg, preset));
    }
}
