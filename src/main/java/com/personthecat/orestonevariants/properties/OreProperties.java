package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.Range;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.loading.FMLLoader;
import org.hjson.JsonArray;
import org.hjson.JsonObject;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;
import static com.personthecat.orestonevariants.io.SafeFileIO.*;

/**
 * The primary data holder containing all of the information needed for
 * multiple ores to share properties.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class OreProperties {
    /** An identifier for these properties. */
    public final ResourceLocation location;
    /** A reference to the original BlockState represented by these properties. */
    public final Lazy<BlockState> ore;
    /** Standard block properties to be applied when creating new variants. */
    public final Block.Properties block;
    /** Information regarding this ore's texture sprites. */
    public final TextureProperties texture;
    /** Information regarding this ore's world generation variables. */
    public final List<WorldGenProperties> gen;
    /** Information regarding this ore's drop overrides, if any. */
    public final Optional<LootTable> drops;
    /** Information regarding this ore's smelting recipe. */
    public final Optional<RecipeProperties> recipe;
    /** The amount of experience to drop for this ore. Better location? */
    public final Optional<Range> xp;

    /** The name of the directory containing all of the presets. */
    private static final String FOLDER = "/config/" + Main.MODID + "/presets/";
    /** The path leading to the folder. */
    private static final File DIR = new File(FMLLoader.getGamePath() + FOLDER);

    /** Helps organize the categories inside of the root object. Needs work? */
    private OreProperties(ResourceLocation location, JsonObject root, JsonObject block, JsonObject texture, JsonArray gen) {
        this(
            location,
            getStringOr(block, "location", "air"),
            BlockPropertiesHelper.from(block),
            TextureProperties.from(location, texture),
            WorldGenProperties.list(gen),
            getLootTable(root, "loot"),
            getObject(root, "recipe").map(RecipeProperties::new),
            getRange(block, "xp")
        );
    }

    /** Primary constructor */
    public OreProperties(
        ResourceLocation location,
        String oreLookup,
        Block.Properties block,
        TextureProperties texture,
        List<WorldGenProperties> gen,
        Optional<LootTable> drops,
        Optional<RecipeProperties> recipe,
        Optional<Range> xp
    ) {
        this.location = location;
        this.ore = new Lazy<>(() -> getBlockState(oreLookup).orElseThrow(() -> noBlockNamed(oreLookup)));
        this.block = block;
        this.texture = texture;
        this.gen = gen;
        this.drops = drops;
        this.recipe = recipe;
        this.xp = xp;
    }

    /** Generates a new OreProperties object from the input file. */
    public static OreProperties fromFile(File f) {
        final JsonObject root = readJson(f).orElseThrow(() -> runEx("Invalid hjson file."));
        final String mod = getStringOr(root, "mod", "custom");
        final String name = noExtension(f);
        final ResourceLocation location = new ResourceLocation(mod, name);
        final JsonObject block = getObjectOrNew(root, "block");
        final JsonObject texture = getObjectOrNew(root, "texture");
        final JsonArray gen = getArrayOrNew(root, "gen");
        return new OreProperties(location, root, block, texture, gen);
    }

    /** Generates properties for all of the presets inside of the directory. */
    public static Set<OreProperties> setupOreProperties() {
        final Set<OreProperties> properties = new HashSet<>();
        for (File f : safeListFiles(DIR)) {
            properties.add(fromFile(f));
        }
        return properties;
    }

    /** Locates the OreProperties corresponding to `name`. */
    public static Optional<OreProperties> of(String name) {
        return find(Main.ORE_PROPERTIES, props -> props.location.getPath().equals(name));
    }

    /** Locates the OreProperties corresponding to each entry in the list. */
    public static Set<OreProperties> of(List<String> names) {
        return names.stream()
            .map(name -> of(name)
                .orElseThrow(() -> runExF("There are no properties named \"{}.\" Fix your property group.", name)))
            .collect(Collectors.toSet());
    }
}