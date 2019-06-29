package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.Range;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.Dimension;
import org.hjson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

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
    public final List<Lazy<Biome>> biomes = new ArrayList<>();
    /** A list of dimensions for this ore to spawn in. */
    public final List<Dimension> dimensions = new ArrayList<>();

    /** Allows WorldGenProperties to be retrieved from JsonObjects. */
    public WorldGenProperties(JsonObject json) {
        this(
            getFloatOr(json, "denseRatio", 0.3f),
            getIntOr(json, "count", 8),
            getIntOr(json, "frequency", 2),
            getFloatOr(json, "chance", 100.0f),
            getRangeOr(json, "height", new Range(0, 32))
            // To-do: biomes and dimensions.
        );
    }

    /** Primary constructor. */
    public WorldGenProperties(
        double denseRatio,
        int count,
        int frequency,
        double chance,
        Range height
    ) {
        this.denseRatio = denseRatio;
        this.count = count;
        this.frequency = frequency;
        this.chance = chance;
        this.height = height;
    }

    /** Additional constructor used for specifying biomes and dimensions. */
    public WorldGenProperties(
        double denseRatio,
        int count,
        int frequency,
        double chance,
        Range height,
        List<String> biomes,
        List<Dimension> dimensions
    ) {
        this(denseRatio, count, frequency, chance, height);
        this.dimensions.addAll(dimensions);
        biomes.forEach(b -> this.biomes.add(
            new Lazy<>(() -> getBiome(b)
                .orElseThrow(() -> runExF("No biome was found named \"{}\"", b))
        )));
    }
}