package personthecat.osv.world.placer;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import lombok.extern.log4j.Log4j2;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.serialization.EasyStateCodec;
import personthecat.osv.ModRegistries;
import personthecat.osv.block.AdditionalProperties;
import personthecat.osv.block.OreVariant;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.DecoratedFeatureSettings;
import personthecat.osv.preset.data.NestedSettings;

import java.util.*;
import java.util.function.Function;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.CodecUtils.easySet;
import static personthecat.catlib.serialization.FieldDescriptor.field;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;

@Log4j2
public class BlockMatchingSpawnConfig {

    private static final Codec<BlockMatchingSpawnConfig> OBJECT_CODEC = codecOf(
        field(EasyStateCodec.INSTANCE, "block", c -> c.block),
        defaulted(Codec.doubleRange(0.0, 1.0), "chance", 1.0, c -> c.chance),
        BlockMatchingSpawnConfig::new
    );

    public static final Codec<BlockMatchingSpawnConfig> CODEC = Codec.either(EasyStateCodec.INSTANCE, OBJECT_CODEC).xmap(
        either -> either.map(s -> new BlockMatchingSpawnConfig(s, 1.0), Function.identity()),
        config -> config.chance == 1.0 ? Either.left(config.block) : Either.right(config)
    );

    public static final Codec<Map<BlockState, Set<BlockMatchingSpawnConfig>>> MAP_CODEC =
        Codec.unboundedMap(EasyStateCodec.INSTANCE, easySet(CODEC));

    final BlockState block;
    final double chance;

    public BlockMatchingSpawnConfig(final BlockState block, final double chance) {
        this.block = block;
        this.chance = chance;
    }

    public static Map<BlockState, Set<BlockMatchingSpawnConfig>> createMap(
            final DecoratedFeatureSettings<?, ?> cfg, final OrePreset preset) {

        final Map<BlockState, Set<BlockMatchingSpawnConfig>> blocks = new HashMap<>();
        for (final OreVariant ore : ModRegistries.VARIANTS) {
            if (ore.getPreset() == preset) {
                blocks.put(ore.getBg().defaultBlockState(), createConfigs(ore, cfg, preset));
            }
        }
        return blocks;
    }

    private static Set<BlockMatchingSpawnConfig> createConfigs(
            final OreVariant ore, final DecoratedFeatureSettings<?, ?> cfg, final OrePreset preset) {

        final Set<BlockMatchingSpawnConfig> configs = new HashSet<>();
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

    public static int hash(final Map<BlockState, Set<BlockMatchingSpawnConfig>> map) {
        int hash = 31;
        for (final Set<BlockMatchingSpawnConfig> blocks : map.values()) {
            for (final BlockMatchingSpawnConfig block : blocks) {
                hash += 31 * Block.getId(block.block);
            }
        }
        return hash;
    }

    public static boolean tryPlace(final Map<BlockState, Set<BlockMatchingSpawnConfig>> map, final Random rand,
                                   final WorldGenLevel level, final BlockPos pos) {
        final BlockState bg = level.getBlockState(pos);
        final Set<BlockMatchingSpawnConfig> blocks = map.get(bg);
        if (blocks != null) {
            for (final BlockMatchingSpawnConfig block : blocks) {
                if (rand.nextDouble() <= block.chance) {
                    level.setBlock(pos, block.block, 2);
                    return true;
                }
            }
        }
        return false;
    }
}
