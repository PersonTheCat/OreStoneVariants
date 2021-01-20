package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.InvertableSet;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.Range;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
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
    public final Lazy<InvertableSet<Biome>> biomes;

    /** Allows WorldGenProperties to be retrieved from JsonObjects. */
    public WorldGenProperties(JsonObject json) {
        this(json, getObjectOrNew(json, "biomes"));
    }

    /** Separates the `biomes` object out of `main` to retrieve multiple elements from it. */
    private WorldGenProperties(JsonObject main, JsonObject biomes) {
        this(
            getFloatOr(main, "denseChance", Cfg.denseChance.get().floatValue()),
            getIntOr(main, "size", 8),
            getIntOr(main, "count", 2),
            getFloatOr(main, "chance", 1.0f),
            getRangeOr(main, "height", new Range(0, 32)),
            getStringArrayOrEmpty(biomes, "names"),
            getStringArrayOrEmpty(biomes, "types"),
            getBoolOr(main, "blacklistBiomes", false)
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
        boolean blacklistBiomes
    ) {
        this(denseRatio, size, count, chance, height, new Lazy<>(() -> getAllBiomes(biomes, biomeTypes, blacklistBiomes)));
    }

    public WorldGenProperties(
        double denseRatio,
        int size,
        int count,
        double chance,
        Range height,
        Lazy<InvertableSet<Biome>> biomes
    ) {
        this.denseRatio = denseRatio;
        this.size = size;
        this.count = count;
        this.chance = chance;
        this.height = height;
        this.biomes = biomes;
    }

    private static InvertableSet<Biome> getAllBiomes(Collection<String> names, Collection<String> types, boolean blacklist) {
        final Set<Biome> biomes = new HashSet<>();
        names.forEach(name -> biomes.add(getBiome(name)
            .orElseThrow(() -> noBiomeNamed(name))));
        types.forEach(type -> biomes.addAll(Arrays.asList(getBiomes(getBiomeType(type)))));
        if (!Cfg.biomeSpecific.get() || biomes.isEmpty()) {
            WorldGenRegistries.BIOME.forEach(biomes::add);
        }
        return InvertableSet.wrap(biomes).setBlacklist(blacklist);
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