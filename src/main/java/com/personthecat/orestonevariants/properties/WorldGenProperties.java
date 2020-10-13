package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.Range;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
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
            getStringArrayOrEmpty(biomes, "types")
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
        List<String> biomeTypes
    ) {
        this(denseRatio, size, count, chance, height, new Lazy<>(() -> getAllBiomes(biomes, biomeTypes)));
    }

    public WorldGenProperties(
        double denseRatio,
        int size,
        int count,
        double chance,
        Range height,
        Lazy<Set<Biome>> biomes
    ) {
        this.denseRatio = denseRatio;
        this.size = size;
        this.count = count;
        this.chance = chance;
        this.height = height;
        this.biomes = biomes;
    }

    private static Set<Biome> getAllBiomes(Collection<String> names, Collection<String> types) {
        final Set<Biome> biomes = new HashSet<>();
        names.forEach(name -> biomes.add(getBiome(name)
            .orElseThrow(() -> noBiomeNamed(name))));
        types.forEach(type -> biomes.addAll(Arrays.asList(getBiomes(getBiomeType(type)))));
        if (!Cfg.biomeSpecific.get() || biomes.isEmpty()) {
            WorldGenRegistries.field_243657_i.forEach(biomes::add);
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

    public static Builder builder() {
        return new Builder();
    }

    // API
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static class Builder {
        private double denseRatio = Cfg.denseChance.get().floatValue();
        private int size = 8;
        private int count = 2;
        private double chance = 1.0;
        private Range height = new Range(0, 32);
        private Lazy<Set<Biome>> biomes = new Lazy<>(Collections::emptySet);
        private Set<String> biomeNames = new HashSet<>();
        private Set<String> biomeTypes = new HashSet<>();

        private Builder() {}

        public WorldGenProperties build() {
            return new WorldGenProperties(denseRatio, size, count, chance, height, new Lazy<>(this::getBiomes));
        }

        private Set<Biome> getBiomes() {
            final Set<Biome> all = new HashSet<>();
            all.addAll(biomes.get());
            all.addAll(getAllBiomes(biomeNames, biomeTypes));
            return all;
        }

        public Builder denseRatio(double ratio) {
            this.denseRatio = ratio;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder count(int count) {
            this.count = count;
            return this;
        }

        public Builder height(Range height) {
            this.height = height;
            return this;
        }

        public Builder height(int min, int max) {
            return height(Range.of(min, max));
        }

        public Builder biomes(Lazy<Set<Biome>> biomes) {
            this.biomes = biomes;
            return this;
        }

        public Builder biomes(Set<Biome> biomes) {
            return biomes(new Lazy<>(biomes));
        }

        public Builder biomeNames(Set<String> biomeNames) {
            this.biomeNames = biomeNames;
            return this;
        }

        public Builder biomeNames(String... biomeNames) {
            return biomeNames(new HashSet<>(Arrays.asList(biomeNames)));
        }

        public Builder biomeTypes(Set<String> biomeTypes) {
            this.biomeTypes = biomeTypes;
            return this;
        }

        public Builder biomeTypes(String... biomeTypes) {
            return biomeTypes(new HashSet<>(Arrays.asList(biomeTypes)));
        }
    }
}