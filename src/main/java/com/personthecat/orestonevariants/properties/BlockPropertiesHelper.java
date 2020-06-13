package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.util.unsafe.ReflectionTools;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import org.hjson.JsonObject;

import java.lang.reflect.Field;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

/**
 * A note about porting this to 1.12 from 1.14: this code is highly unnecessary in
 * 1.12; however, it proved to help organize the code inside of BaseOreVariant so
 * much that it was kept almost in its original form.
 */
public class BlockPropertiesHelper {
    /** The underlying DTO contained within this object. */
    public final Block properties;

    /** All of the protected fields that cannot be easily retrieved in Block. */
    private static final Field material = reflect("material", 18); // Ignored in 1.12?
    private static final Field mapColor = reflect("blockMapColor", 19);
    private static final Field soundType = reflect("blockSoundType", 16);
    private static final Field lightValue = reflect("lightValue", 9);
    private static final Field resistance = reflect("blockResistance", 12);
    private static final Field hardness = reflect("blockHardness", 11);
    private static final Field ticksRandomly = reflect("needsRandomTick", 14);
    private static final Field slipperiness = reflect("slipperiness", 20);

    /** Convenience constructor. */
    public BlockPropertiesHelper(Material material, MapColor color) {
        this(new Block(material, color));
    }

    /** Primary Constructor. */
    public BlockPropertiesHelper(Block properties) {
        this.properties = properties;
    }

    public static BlockPropertiesHelper from(JsonObject json) {
        final Material material = getMaterialOr(json, "material", Material.ROCK);
        final MapColor color = getMapColorOr(json, "mapColor", MapColor.STONE);
        return new BlockPropertiesHelper(material, color)
            .setSoundType(getSoundTypeOr(json, "soundType", SoundType.STONE))
            .setLightValue(getIntOr(json, "light", 0))
            .setResistance(getFloatOr(json, "resistance", 15))
            .setHardness(getFloatOr(json, "hardness", 3.0F))
            .setTicksRandomly(getBoolOr(json, "ticksRandomly", false))
            .setSlipperiness(getFloatOr(json, "slipperiness", 0.6F))
            .setHarvestLevel(getIntOr(json, "level", 1))
            .setHarvestTool(getStringOr(json, "tool", "pickaxe"));
    }

    /** Merges the properties from two blocks. */
    public static BlockPropertiesHelper merge(Block ore, IBlockState bg) {
        return merge(ore, bg.getBlock());
    }

    /** Merges the properties from two blocks. */
    public static BlockPropertiesHelper merge(Block oreProps, Block bgProps) {
        final BlockPropertiesHelper ore = new BlockPropertiesHelper(oreProps);
        final BlockPropertiesHelper bg = new BlockPropertiesHelper(bgProps);
        final Material material = bg.getMaterial();
        final MapColor color = bg.getMapColor();
        return new BlockPropertiesHelper(material, color)
            .setSoundType(bg.getSoundType())
            .setLightValue(getMax(ore.getLightValue(), bg.getLightValue()))
            .setResistance(getMax(ore.getResistance(), bg.getResistance()))
            .setHardness(mergeHardness(ore.getHardness(), bg.getHardness()))
            .setTicksRandomly(ore.getTicksRandomly() || bg.getTicksRandomly())
            .setSlipperiness(avg(ore.getSlipperiness(), bg.getSlipperiness()))
            .setHarvestLevel(getMax(ore.getHarvestLevel(), bg.getHarvestLevel()))
            .setHarvestTool(bg.getHarvestTool());
    }

    /** Clones all of this block's properties into another block. */
    public void cloneInto(Block other) {
        new BlockPropertiesHelper(other)
            .setSoundType(getSoundType())
            .setLightValue(getLightValue())
            .setResistance(getResistance())
            .setHardness(getHardness())
            .setTicksRandomly(getTicksRandomly())
            .setSlipperiness(getSlipperiness())
            .setHarvestLevel(getHarvestLevel())
            .setHarvestTool(getHarvestTool());
    }

    /** Reflectively gets the Material from these properties. */
    public Material getMaterial() {
        return (Material) get(material);
    }

    /** Reflectively gets the map color from these properties. */
    public MapColor getMapColor() {
        return (MapColor) get(mapColor);
    }

    /** Forwards a new sound type to the underlying DTO. */
    public BlockPropertiesHelper setSoundType(SoundType sound) {
        set(soundType, sound);
        return this;
    }

    /** Reflectively gets the sound type from these properties. */
    public SoundType getSoundType() {
        return (SoundType) get(soundType);
    }

    /** Forwards a new light value to the underlying DTO. */
    public BlockPropertiesHelper setLightValue(int value) {
        set(lightValue, value);
        return this;
    }

    /** Reflectively gets the light value from these properties. */
    public int getLightValue() {
        return (int) get(lightValue);
    }

    /** Reflectively sets the resistance for the properties. */
    public BlockPropertiesHelper setResistance(float r) {
        set(resistance, r);
        return this;
    }

    /** Reflectively gets the resistance from these properties. */
    public float getResistance() {
        return (float) get(resistance);
    }

    /** Reflectively sets the hardness for these properties. */
    public BlockPropertiesHelper setHardness(float h) {
        set(hardness, h);
        return this;
    }

    /** Reflectively gets the hardness from these properties. */
    public float getHardness() {
        return (float) get(hardness);
    }

    /** Combines the two hardness values, accounting for unbreakable background blocks. */
    private static float mergeHardness(float ore, float bg) {
        return bg < 0 ? -1 : getMax(ore + bg - 1.5F, 0F);
    }

    /** Reflectively sets whether these properties tick randomly. */
    public BlockPropertiesHelper setTicksRandomly(boolean ticks) {
        set(ticksRandomly, ticks);
        return this;
    }

    /** Reflectively gets whether these properties tick randomly. */
    public boolean getTicksRandomly() {
        return (boolean) get(ticksRandomly);
    }

    /** Forwards a new slipperiness value to the underlying DTO. */
    public BlockPropertiesHelper setSlipperiness(float slip) {
        set(slipperiness, slip);
        return this;
    }

    /** Reflectively gets the slipperiness from these properties. */
    public float getSlipperiness() {
        return (float) get(slipperiness);
    }

    /** Forwards a new harvest level to the underlying DTO. */
    public BlockPropertiesHelper setHarvestLevel(int level) {
        final IBlockState defaultState = properties.getDefaultState();
        final String harvestTool = nullable(properties.getHarvestTool(defaultState))
            .orElse("pickaxe");
        properties.setHarvestLevel(harvestTool, level);
        return this;
    }

    /** Reflectively gets the harvest level from these properties. */
    public int getHarvestLevel() {
        return properties.getHarvestLevel(properties.getDefaultState());
    }

    /** Forwards a new harvest tool to the underlying DTO. */
    public BlockPropertiesHelper setHarvestTool(String type) {
        final IBlockState defaultState = properties.getDefaultState();
        final int harvestLevel = properties.getHarvestLevel(defaultState);
        properties.setHarvestLevel(type, harvestLevel);
        return this;
    }

    /** Reflectively gets the harvest tool type from these properties. */
    public String getHarvestTool() {
        return properties.getHarvestTool(properties.getDefaultState());
    }

    /** Locates a field from Block.Properties., marking it as accessible. */
    private static Field reflect(String name, int index) {
        return ReflectionTools.getField(Block.class, name, index);
    }

    /** Reflectively sets a field in the underlying Object. Frustrating that this is necessary. */
    private void set(Field f, Object o) {
        try {
            f.set(properties, o);
        } catch (IllegalAccessException e) {
            throw invalidFieldError(f);
        }
    }

    /** Reflectively gets the value of a Field from the underlying Object. */
    private Object get(Field f) {
        try {
            return f.get(properties);
        } catch (IllegalAccessException e) {
            throw invalidFieldError(f);
        }
    }

    /** An error indicating that reflection was handled incorrectly. */
    private RuntimeException invalidFieldError(Field f) {
        return runExF("Build error: field `{}` registered incorrectly.", f.getName());
    }
}