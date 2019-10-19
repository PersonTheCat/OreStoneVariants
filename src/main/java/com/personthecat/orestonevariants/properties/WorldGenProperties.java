package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.Range;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.registries.ForgeRegistries;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.*;
import java.util.stream.Collectors;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

public class WorldGenProperties {
    /** The ratio of dense:normal variants. */
    public final double denseRatio;
    /** The vein 'count' to spawn, according to WorldGenMinable. */
    public final int count;
    /** The number of tries per chunk to spawn veins. */
    public final int frequency;
    /** The chance that any try will succeed. */
    public final double chance;
    /** A range of acceptable heights for this ore to spawn. */
    public final Range height;

    // To-do: replace these with Predicates.
    /** A list of biomes for this ore to spawn in, lazily initialized. */
    public final Lazy<Set<Biome>> biomes;

    /** Allows WorldGenProperties to be retrieved from JsonObjects. */
    public WorldGenProperties(JsonObject json) {
        this(json, getObjectOrNew(json, "biomes"));
    }

    /** Separates the `biomes` object out of `main` to retrieve multiple elements from it. */
    private WorldGenProperties(JsonObject main, JsonObject biomes) {
        this(
            getFloatOr(main, "denseRatio", 0.3f),
            getIntOr(main, "count", 8),
            getIntOr(main, "frequency", 2),
            getFloatOr(main, "chance", 100.0f),
            getRangeOr(main, "height", new Range(0, 32)),
            getStringArrayOrEmpty(biomes, "names"),
            getStringArrayOrEmpty(biomes, "types")
        );
    }

    /** Primary constructor. */
    public WorldGenProperties(
        double denseRatio,
        int count,
        int frequency,
        double chance,
        Range height,
        List<String> biomes,
        List<String> biomeTypes
    ) {
        this.denseRatio = denseRatio;
        this.count = count;
        this.frequency = frequency;
        this.chance = chance;
        this.height = height;
        this.biomes = new Lazy<>(() -> getAllBiomes(biomes, biomeTypes));
    }

    private static Set<Biome> getAllBiomes(List<String> names, List<String> types) {
        final Set<Biome> biomes = new HashSet<>();
        names.forEach(name -> biomes.add(getBiome(name)
            .orElseThrow(() -> noBiomeNamed(name))));
        types.forEach(type -> biomes.addAll(Arrays.asList(getBiomes(getBiomeType(type)))));
        if (biomes.isEmpty()) {
            biomes.addAll(ForgeRegistries.BIOMES.getValues());
        }
        return biomes;
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