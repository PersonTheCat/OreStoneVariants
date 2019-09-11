package com.personthecat.orestonevariants.properties;

import com.google.common.collect.ImmutableSet;
import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.PathTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;

import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

/**
 * The primary data holder containing all of the information needed for
 * multiple ores to share properties.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class OreProperties {
    private final String name;
    private final String mod;
    private final Lazy<BlockState> ore;
    private final Block.Properties block;
    private final Optional<LootTable> drops;
    private final List<WorldGenProperties> gen;
    private final RecipeProperties recipe;
    private final boolean builtinTexture;
    private final boolean shade;

    public OreProperties(
        String name,
        String mod,
        String oreLookup,
        Block.Properties block,
        Optional<LootTable> drops,
        List<WorldGenProperties> gen,
        RecipeProperties recipe,
        boolean builtinTexture,
        boolean shade
    ) {
        this.name = name;
        this.mod = mod;
        this.ore = new Lazy<>(() -> getBlockState(oreLookup).get());
        this.block = block;
        this.drops = drops;
        this.gen = gen;
        this.recipe = recipe;
        this.builtinTexture = builtinTexture;
        this.shade = shade;
    }

    public static ImmutableSet<OreProperties> setupOreProperties() {
        return ImmutableSet.of();
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
    public RecipeProperties getRecipe() {
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

    /** Returns the filename associated with these properties' overlay sprite. */
    public String getFileName() {
        final String fileName = f("{}/{}_overlay", mod, name);
        if (shade) {
            return PathTools.ensureShaded(fileName);
        }
        return fileName;
    }

    /** Generates a path to these properties' overlay sprite. */
    public String getOverlayPath() {
        return f("assets/{}/textures/blocks/{}", Main.MODID, getFileName());
    }

    /** Generates a ResourceLocation representing these properties' overlay sprite. */
    public ResourceLocation getOverlayResourceLocation() {
        return osvLocation("blocks/" + getFileName());
    }
}