package personthecat.osv.world.feature;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import lombok.extern.log4j.Log4j2;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.ModRegistries;
import personthecat.osv.block.AdditionalProperties;
import personthecat.osv.block.OreVariant;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.DecoratedFeatureSettings;
import personthecat.osv.preset.data.NestedSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.CodecUtils.easyList;
import static personthecat.catlib.serialization.FieldDescriptor.field;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;

@Log4j2
public class BlockMatchingSpawnConfig {

    private static final Codec<BlockMatchingSpawnConfig> OBJECT_CODEC = codecOf(
        field(BlockState.CODEC, "block", c -> c.block),
        defaulted(Codec.doubleRange(0.0, 1.0), "chance", 1.0, c -> c.chance),
        BlockMatchingSpawnConfig::new
    );

    public static final Codec<BlockMatchingSpawnConfig> CODEC = Codec.either(BlockState.CODEC, OBJECT_CODEC).xmap(
        either -> either.map(s -> new BlockMatchingSpawnConfig(s, 1.0), Function.identity()),
        config -> config.chance == 1.0 ? Either.left(config.block) : Either.right(config)
    );

    // Todo: create set codec and use set instead (faster iteration)
    public static final Codec<Map<BlockState, List<BlockMatchingSpawnConfig>>> MAP_CODEC =
        Codec.unboundedMap(BlockState.CODEC, easyList(CODEC));

    final BlockState block;
    final double chance;

    public BlockMatchingSpawnConfig(final BlockState block, final double chance) {
        this.block = block;
        this.chance = chance;
    }

    public static Map<BlockState, List<BlockMatchingSpawnConfig>> createMap(
            final DecoratedFeatureSettings<?, ?> cfg, final OrePreset preset) {

        final Map<BlockState, List<BlockMatchingSpawnConfig>> blocks = new HashMap<>();
        for (final OreVariant ore : ModRegistries.VARIANTS) {
            if (ore.getPreset() == preset) {
                blocks.put(ore.getBg().defaultBlockState(), createConfigs(ore, cfg, preset));
            }
        }
        return blocks;
    }

    private static List<BlockMatchingSpawnConfig> createConfigs(
            final OreVariant ore, final DecoratedFeatureSettings<?, ?> cfg, final OrePreset preset) {

        final List<BlockMatchingSpawnConfig> configs = new ArrayList<>();
        if (preset.canBeDense()) {
            final BlockState dense = ore.defaultBlockState().setValue(AdditionalProperties.DENSE, true);
            configs.add(new BlockMatchingSpawnConfig(dense, cfg.getDenseRatio()));
        }
        final List<NestedSettings> containers = cfg.getNested();
        if (containers != null) {
            for (final NestedSettings container : containers) {
                final OreVariant nested = getNestedVariant(container.getType(), ore.getBg());
                if (nested != null) {
                    configs.add(new BlockMatchingSpawnConfig(nested.defaultBlockState(), container.getChance()));
                }
            }
        }
        configs.add(new BlockMatchingSpawnConfig(ore.defaultBlockState(), 1.0));
        return configs;
    }

    @Nullable
    private static OreVariant getNestedVariant(final String type, final Block bg) {
        for (final OreVariant ore : ModRegistries.VARIANTS) {
            if (ore.getBg() == bg && ore.getPreset().getName().equals(type)) {
                return ore;
            }
        }
        log.warn("No nested variants found for type: {}. Ignoring...", type);
        return null;
    }
}
