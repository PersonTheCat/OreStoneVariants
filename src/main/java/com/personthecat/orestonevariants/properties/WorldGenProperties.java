package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.Range;
import com.personthecat.orestonevariants.world.WildcardBiome;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

public class WorldGenProperties {
    /** The ratio of dense:normal variants. */
    public final double denseRatio;
    /** The vein 'count' to spawn, according to WorldGenMinable. */
    public final int size;
    /** The number of tries per chunk to spawn veins. */
    public final int count;
    /** The chance that any try will succeed. */
    public final double chance;
    /** A range of acceptable heights for this ore to spawn. */
    public final Range height;

    // To-do: replace these with Predicates.
    /** A list of biomes for this ore to spawn in, lazily initialized. */
    public final Lazy<Set<Biome>> biomes;
    /** A list of all dimensions for this ore to spawn in. */
    public final int[] dimensions;

    /** A wildcard dimension number. Empty lists will match all dimensions. */
    public static final Integer DIM_WILDCARD = Integer.MAX_VALUE;
    /** A wildcard biome. Empty lists will match all biomes. */
    public static final Biome BIOME_WILDCARD = new WildcardBiome();

    /** Allows WorldGenProperties to be retrieved from JsonObjects. */
    public WorldGenProperties(JsonObject json) {
        this(json, getObjectOrNew(json, "biomes"));
    }

    /** Separates the `biomes` object out of `main` to retrieve multiple elements from it. */
    private WorldGenProperties(JsonObject main, JsonObject biomes) {
        this(
            getFloatOr(main, "denseChance", (float) Cfg.DenseCat.chance),
            getIntOr(main, "size", 8),
            getIntOr(main, "count", 2),
            getFloatOr(main, "chance", 1.0F),
            getRangeOr(main, "height", new Range(0, 32)),
            getStringArrayOrEmpty(biomes, "names"),
            getStringArrayOrEmpty(biomes, "types"),
            getIntArrayOrEmpty(main, "dimensions")
        );
    }

    /** Primary constructor. */
    public WorldGenProperties(
        double denseRatio,
        int size,
        int count,
        double chance,
        Range height,
        List<String> biomes,
        List<String> biomeTypes,
        int[] dimensions
    ) {
        this.denseRatio = denseRatio;
        this.size = size;
        this.count = count;
        this.chance = chance;
        this.height = height;
        this.biomes = new Lazy<>(() -> getAllBiomes(biomes, biomeTypes));
        this.dimensions = getAllDimensions(dimensions);
    }

    private static Set<Biome> getAllBiomes(List<String> names, List<String> types) {
        final Set<Biome> biomes = new HashSet<>();
        names.forEach(name -> biomes.add(getBiome(name)
            .orElseThrow(() -> noBiomeNamed(name))));
        types.forEach(type -> biomes.addAll(Arrays.asList(getBiomes(getBiomeType(type)))));
        if (!Cfg.WorldCat.biomeSpecific || biomes.isEmpty()) {
            biomes.add(BIOME_WILDCARD);
        }
        return biomes;
    }

    private static int[] getAllDimensions(int[] dimensions) {
        // For now, don't worry about blacklists.
        return dimensions.length > 0 ? dimensions : new int[] { DIM_WILDCARD };
    }

    /** Converts an array of JsonObjects to a List of WorldGenProperties. */
    public static List<WorldGenProperties> list(JsonArray array) {
        final List<WorldGenProperties> list = new ArrayList<>();
        for (JsonValue value : array) {
            list.add(new WorldGenProperties(value.asObject()));
        }
        return list;
    }
}