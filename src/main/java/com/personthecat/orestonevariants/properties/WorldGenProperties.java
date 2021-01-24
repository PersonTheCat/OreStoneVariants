package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.InvertableSet;
import com.personthecat.orestonevariants.util.Lazy;
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
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

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

    JsonObject toJson() {
        final JsonObject json = new JsonObject();
        // Only add this if it's different.
        if (denseRatio != Cfg.denseChance.get()) {
            json.set("denseChance", denseRatio);
        }
        return json.set("size", size)
            .set("count", toJson(count))
            .set("chance", chance)
            .set("height", toJson(height))
            .set("stage", stage.name())
            .set("blacklistBiomes", biomes.get().isBlacklist())
            .set("biomes", getJsonBiomes());
    }

    // Todo: Reconstruct biomes and types.
    private JsonObject getJsonBiomes() {
        final JsonArray names = new JsonArray();
        for (Biome b : biomes.get()) {
            names.add(b.getRegistryName().toString());
        }
        return new JsonObject().set("names", names);
    }

    private static JsonValue toJson(Range range) {
        if (range.min == range.max) {
            return JsonValue.valueOf(range.min);
        }
        return new JsonArray().add(range.min).add(range.max).setCondensed(true);
    }
}