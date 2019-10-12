package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.PathTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.loading.FMLLoader;
import org.hjson.JsonArray;
import org.hjson.JsonObject;

import java.io.File;
import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;
import static com.personthecat.orestonevariants.util.SafeFileIO.*;

/**
 * The primary data holder containing all of the information needed for
 * multiple ores to share properties.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class OreProperties {
    private final String name;
    private final String mod;
    private final String originalTexture;
    private final Lazy<BlockState> ore;
    private final Block.Properties block;
    private final Optional<LootTable> drops;
    private final List<WorldGenProperties> gen;
    private final Optional<RecipeProperties> recipe;
    private final boolean builtinTexture;
    private final boolean shade;

    /** The name of the directory containing all of the presets. */
    private static final String FOLDER = "/config/" + Main.MODID + "/presets/";
    /** The path leading to the folder. */
    private static final File DIR = new File(FMLLoader.getGamePath() + FOLDER);
    /** The default background texture used for generating overlays. */
    private static final String DEFAULT_MATCHER = "/assets/minecraft/textures/block/stone.png";

    /** From Json */
    public OreProperties(String name, JsonObject json) {
        this(name, json, getObjectOr(json, "block", new JsonObject()));
    }

    /** Helps organize the categories inside of the root object. Needs work. */
    private OreProperties(String name, JsonObject root, JsonObject block) {
        this(
            name,
            getStringOr(root, "mod", "custom"),
            getStringOr(block, "texture", "/assets/minecraft/textures/block/string"),
            getStringOr(block, "location", "air"),
            BlockPropertiesHelper.from(block),
            getLootTable(root, "loot"),
            WorldGenProperties.list(getArrayOr(root, "gen", new JsonArray())),
            getObject(root, "recipe").map(o -> new RecipeProperties(name, o)),
            getBoolOr(block, "builtInTexture", false),
            getBoolOr(block, "shade", true)
        );
    }

    public OreProperties(
        String name,
        String mod,
        String originalTexture,
        String oreLookup,
        Block.Properties block,
        Optional<LootTable> drops,
        List<WorldGenProperties> gen,
        Optional<RecipeProperties> recipe,
        boolean builtinTexture,
        boolean shade
    ) {
        this.name = name;
        this.mod = mod;
        this.originalTexture = originalTexture;
        this.ore = new Lazy<>(() -> getBlockState(oreLookup).orElseThrow(() -> noBlockNamed(oreLookup)));
        this.block = block;
        this.drops = drops;
        this.gen = gen;
        this.recipe = recipe;
        this.builtinTexture = builtinTexture;
        this.shade = shade;
    }

    /** Generates properties for all of the presets inside of the directory. */
    public static Set<OreProperties> setupOreProperties() {
        final Set<OreProperties> properties = new HashSet<>();
        for (File f : safeListFiles(DIR)) {
            info("Attempting to load a preset from {}", f.getName());
            properties.add(new OreProperties(noExtension(f), readJson(f).get()));
        }
        return properties;
    }

    /** Returns the string identifier for these properties. */
    public String getName() {
        return name;
    }

    /** Returns whether these properties have a built-in overlay in the jar. */
    public boolean hasBuiltInTextures() {
        return builtinTexture;
    }

    /** Returns whether to use a fully shaded overlay for this ore. */
    public boolean shade() {
        return shade;
    }

    /** Returns a reference to the original BlockState represented by these properties. */
    public BlockState getOre() {
        return ore.get();
    }

    public Block.Properties getBlock() {
        return block;
    }

    /** Returns information regarding this ore's drop overrides, if any. */
    public Optional<LootTable> getDrops() {
        return drops;
    }

    /** Returns information regarding this ore's smelting recipe. */
    public Optional<RecipeProperties> getRecipe() {
        return recipe;
    }

    /** Returns information regarding this ore's world generation variables. */
    public List<WorldGenProperties> getGenProps() {
        return gen;
    }

    /** Locates the OreProperties corresponding to `name`. */
    public static Optional<OreProperties> of(String name) {
        return find(Main.ORE_PROPERTIES, props -> props.name.equals(name));
    }

    /** Returns the file name associated with these properties' overlay sprite. */
    public String getFileName() {
        final String fileName = f("{}/{}_overlay", mod, name);
        if (shade) {
            return PathTools.ensureShaded(fileName);
        }
        return fileName;
    }

    public String getBackgroundMatcher() {
        return DEFAULT_MATCHER;
    }

    public String getOriginalTexture() {
        return originalTexture;
    }

    /** Generates a path to these properties' overlay sprite. */
    public String getOverlayPath() {
        return f("assets/{}/textures/block/{}", Main.MODID, getFileName());
    }

    /** Generates a ResourceLocation representing these properties' overlay sprite. */
    public ResourceLocation getOverlayResourceLocation() {
        return osvLocation("block/" + getFileName());
    }
}