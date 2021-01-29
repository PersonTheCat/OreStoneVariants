package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.InvertableSet;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.MultiValueMap;
import com.personthecat.orestonevariants.util.Range;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.experimental.FieldDefaults;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.getBiome;
import static com.personthecat.orestonevariants.util.CommonMethods.getBiomes;
import static com.personthecat.orestonevariants.util.CommonMethods.getBiomeType;
import static com.personthecat.orestonevariants.util.HjsonTools.getBoolOr;
import static com.personthecat.orestonevariants.util.HjsonTools.getFloat;
import static com.personthecat.orestonevariants.util.HjsonTools.getInt;
import static com.personthecat.orestonevariants.util.HjsonTools.getObjectOrNew;
import static com.personthecat.orestonevariants.util.HjsonTools.getRange;
import static com.personthecat.orestonevariants.util.HjsonTools.getStage;
import static com.personthecat.orestonevariants.util.HjsonTools.getStringArrayOrEmpty;
import static com.personthecat.orestonevariants.util.HjsonTools.noBiomeNamed;

@EqualsAndHashCode
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class WorldGenProperties {

    /** The ratio of dense:normal variants. */
    @Default double denseRatio = Cfg.denseChance.get();

    /** The vein 'count' to spawn, according to WorldGenMinable. */
    @Default int size = 8;

    /** The chance that any try will succeed. */
    @Default double chance = 1.0f;

    /** The number of tries per chunk to spawn veins. */
    @Default Range count = new Range(2);

    /** A range of acceptable heights for this ore to spawn. */
    @Default Range height = new Range(0, 32);

    /** When this ore should get placed underground. */
    @Default Decoration stage = Decoration.UNDERGROUND_ORES;

    /** A list of biomes for this ore to spawn in, lazily initialized.*/
    @Exclude Lazy<InvertableSet<Biome>> biomes;

    private static WorldGenProperties from(JsonObject json) {
        final JsonObject biomes = getObjectOrNew(json, "biomes");
        final List<String> names = getStringArrayOrEmpty(biomes, "names");
        final List<String> types = getStringArrayOrEmpty(biomes, "types");
        final boolean blacklist = getBoolOr(json, "blacklistBiomes", false);

        final WorldGenPropertiesBuilder builder = builder()
            .biomes(new Lazy<>(() -> getAllBiomes(names, types, blacklist)));
        getFloat(json, "denseChance", builder::denseRatio);
        getInt(json, "size", builder::size);
        getFloat(json, "chance", builder::chance);
        getRange(json, "count", builder::count);
        getRange(json, "height", builder::height);
        getStage(json, "stage", builder::stage);
        return builder.build();
    }

    /** Returns a set containing every biome specified by type and name. */
    private static InvertableSet<Biome> getAllBiomes(Collection<String> names, Collection<String> types, boolean blacklist) {
        final Set<Biome> biomes = new HashSet<>();
        names.forEach(name -> biomes.add(getBiome(name)
            .orElseThrow(() -> noBiomeNamed(name))));
        types.forEach(type -> biomes.addAll(Arrays.asList(getBiomes(getBiomeType(type)))));
        if (!Cfg.biomeSpecific.get() || biomes.isEmpty()) {
            ForgeRegistries.BIOMES.forEach(biomes::add);
        }
        return InvertableSet.wrap(biomes).setBlacklist(blacklist);
    }

    /** Converts an array of JsonObjects to a List of WorldGenProperties. */
    public static List<WorldGenProperties> list(JsonArray array) {
        final List<WorldGenProperties> list = new ArrayList<>();
        for (JsonValue value : array) {
            list.add(WorldGenProperties.from(value.asObject()));
        }
        return list;
    }

    /** Converts these properties into a simplified JSON object. */
    JsonObject toJson() {
        final JsonObject json = new JsonObject();
        final InvertableSet<Biome> reconstructed = reconstruct(biomes.get());
        // Only add this if it's different.
        if (denseRatio != Cfg.denseChance.get()) {
            json.set("denseChance", denseRatio);
        }
        return json.set("height", toJson(height))
            .set("count", toJson(count))
            .set("size", size)
            .set("chance", chance)
            .set("stage", stage.name())
            .set("blacklistBiomes", reconstructed.isBlacklist())
            .set("biomes", getJsonBiomes(reconstructed));
    }

    /** Converts a set of biomes into arrays of type and name. */
    private JsonObject getJsonBiomes(Set<Biome> biomes) {
        final MultiValueMap<Biome.Category, Biome> categories = getCategories(biomes);
        final JsonArray names = new JsonArray();
        final JsonArray types = new JsonArray();
        // Compress as many names as possible into categories.
        for (Map.Entry<Biome.Category, List<Biome>> entry : categories.entrySet()) {
            final Biome[] possible = getBiomes(entry.getKey());
            if (possible.length == entry.getValue().size()) {
                // We have every possible biome in this category.
                types.add(entry.getKey().getName().toUpperCase());
            } else {
                for (Biome b : entry.getValue()) {
                    names.add(b.getRegistryName().toString());
                }
            }
        }
        final JsonObject json = new JsonObject();
        if (names.size() > 0) {
            json.set("names", names);
        }
        if (types.size() > 0) {
            json.set("types", types);
        }
        return json;
    }

    /** Reconstructs the set to be as compact as possible. */
    private static InvertableSet<Biome> reconstruct(InvertableSet<Biome> biomes) {
        final IForgeRegistry<Biome> allBiomes = ForgeRegistries.BIOMES;
        final Set<Biome> expanded = expandRegistered(biomes, allBiomes);
        // Use a blacklist if we have more than 50% of the biomes.
        if (expanded.size() > allBiomes.getValues().size() / 2) {
            final Set<Biome> inverted = new HashSet<>(allBiomes.getValues());
            inverted.removeAll(expanded);
            return InvertableSet.wrap(inverted).setBlacklist(true);
        }
        return InvertableSet.wrap(expanded);
    }

    /** Maps every biome to the category in which it spawns. */
    private static MultiValueMap<Biome.Category, Biome> getCategories(Set<Biome> biomes) {
        final MultiValueMap<Biome.Category, Biome> categories = new MultiValueMap<>();
        for (Biome b : biomes) {
            categories.add(b.getCategory(), b);
        }
        return categories;
    }

    /** Expands a blacklist / whitelist into a whitelist containing only contents from the source. */
    private static Set<Biome> expandRegistered(InvertableSet<Biome> biomes, IForgeRegistry<Biome> source) {
        final Set<Biome> expanded = new HashSet<>();
        for (Biome b : biomes) {
            expanded.add(source.getValue(b.getRegistryName()));
        }
        if (biomes.isBlacklist()) {
            final Set<Biome> copy = new HashSet<>(source.getValues());
            copy.removeAll(expanded);
            return copy;
        }
        return expanded;
    }

    /** Converts a Range into a JSON primitive or array. Todo: move */
    private static JsonValue toJson(Range range) {
        if (range.min == range.max) {
            return JsonValue.valueOf(range.min);
        }
        return new JsonArray().add(range.min).add(range.max).setCondensed(true);
    }
}