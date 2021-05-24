package com.personthecat.orestonevariants.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.LazyRegistries;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.Range;
import com.personthecat.orestonevariants.util.Reference;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.loot.LootTable;
import net.minecraftforge.fml.loading.FMLLoader;
import org.hjson.JsonObject;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.personthecat.orestonevariants.io.SafeFileIO.safeListFiles;
import static com.personthecat.orestonevariants.util.CommonMethods.empty;
import static com.personthecat.orestonevariants.util.CommonMethods.extension;
import static com.personthecat.orestonevariants.util.CommonMethods.find;
import static com.personthecat.orestonevariants.util.CommonMethods.full;
import static com.personthecat.orestonevariants.util.CommonMethods.getGuaranteedState;
import static com.personthecat.orestonevariants.util.CommonMethods.noExtension;
import static com.personthecat.orestonevariants.util.CommonMethods.runEx;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;
import static com.personthecat.orestonevariants.util.HjsonTools.getArrayOrNew;
import static com.personthecat.orestonevariants.util.HjsonTools.getLootTable;
import static com.personthecat.orestonevariants.util.HjsonTools.getObjectOrNew;
import static com.personthecat.orestonevariants.util.HjsonTools.getRange;
import static com.personthecat.orestonevariants.util.HjsonTools.getString;
import static com.personthecat.orestonevariants.util.HjsonTools.getStringOr;
import static com.personthecat.orestonevariants.util.HjsonTools.readJson;

/**
 * The primary data holder containing all of the information needed for
 * multiple ores to share properties.
 */
@Log4j2
@Builder
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class OreProperties {

    /** An identifier for these properties. */
    public final String name;

    /** A reference to the original BlockState represented by these properties. */
    public final Lazy<BlockState> ore;

    /** Standard block properties to be applied when creating new variants. */
    public final Block.Properties block;

    /** Information regarding this ore's texture sprites. */
    public final TextureProperties texture;

    /** Information regarding this ore's world generation variables. */
    public final List<WorldGenProperties> gen;

    /** Information regarding this ore's drop overrides, if any. */
    public final Lazy<Optional<LootTable>> drops;

    /** Information regarding this ore's smelting recipe. Verified later.*/
    public final RecipeProperties.Unchecked recipe;

    /** The amount of experience to drop for this ore. Better location? */
    public final Optional<Range> xp;

    /** The translation key to return for this ore type. */
    public final Optional<String> translationKey;

    /** The name of the directory containing all of the presets. */
    private static final String FOLDER = "/config/" + Main.MODID + "/ores/";

    /** The path leading to the folder. */
    public static final File DIR = new File(FMLLoader.getGamePath() + FOLDER);

    /** Enables serialization via vanilla configs. */
    private static final Encoder<OreProperties> ENCODER = Codec.STRING
        .comap(properties -> properties.name);

    /** Enables deserialization in vanilla configs. */
    private static final Decoder<OreProperties> DECODER = Codec.STRING
        .map(s -> of(s).orElseThrow(() -> runExF("Undefined OreProperties: {}", s)));

    /** Required because of this class' use in world generation. */
    public static final Codec<OreProperties> CODEC = Codec.of(ENCODER, DECODER);

    private static OreProperties fromJson(JsonObject json) {
        final String name = getString(json, "name").orElseThrow(() -> runEx("missing name"));
        final JsonObject block = getObjectOrNew(json, "block");
        final String lookup = getStringOr(block, "location", "air");

        return builder()
            .ore(new Lazy<>(() -> getGuaranteedState(lookup)))
            .drops(new Lazy<>(() -> getLootTable(json, "loot")))
            .block(BlockPropertiesHelper.from(block))
            .texture(TextureProperties.from(getObjectOrNew(json, "texture")))
            .recipe(RecipeProperties.from(getObjectOrNew(json, "recipe")))
            .gen(WorldGenProperties.list(getArrayOrNew(json, "gen")))
            .translationKey(getString(block, "translationKey"))
            .xp(getRange(block, "xp"))
            .name(name)
            .build();
    }

    /** Generates a new OreProperties object from the input file. */
    private static Optional<OreProperties> fromFile(File f) {
        log.info("Checking: {}", f.getName());
        final JsonObject root = readJson(f).orElseThrow(() -> runExF("Invalid hjson file: {}.", f.getPath()));
        final String mod = getStringOr(root, "mod", "custom");
        final String name = getString(root, "name")
            .orElseGet(() -> noExtension(f))
            .toLowerCase();
        if (!Cfg.oreEnabled(name) || Cfg.modFamiliar(mod) && !Cfg.modEnabled(mod)) {
            log.info("Skipping {}. It is supported, but not enabled", name);
            return empty();
        } else {
            log.info("Loading new ore properties: {}", name);
        }
        return full(fromJson(root));
    }

    /** Generates properties for all of the presets inside of the directory. */
    public static Set<OreProperties> setupOreProperties() {
        final Set<OreProperties> properties = new HashSet<>();
        for (File f : safeListFiles(DIR)) {
            if (Reference.VALID_EXTENSIONS.contains(extension(f))) {
                if (!"TUTORIAL.hjson".equals(f.getName())) {
                    fromFile(f).ifPresent(properties::add);
                }
            }
        }
        return properties;
    }

    /** Locates the OreProperties corresponding to `name`. */
    public static Optional<OreProperties> of(String name) {
        return find(LazyRegistries.ORE_PROPERTIES, props -> props.name.equals(name));
    }

    /** Locates the OreProperties corresponding to each entry in the list. */
    public static Set<OreProperties> of(List<String> names) {
        return names.stream()
            .map(name -> of(name)
                .orElseThrow(() -> runExF("There are no properties named \"{}.\" Fix your property group.", name)))
            .collect(Collectors.toSet());
    }
}