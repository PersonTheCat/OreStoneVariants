package com.personthecat.orestonevariants.properties;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.hjson.JsonObject;

import java.lang.reflect.Field;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

/**
 * All of the heinous, unadulterated, unsafe code necessary for interacting
 * with an object that has poorly-designed setters and no getters.
 */
public class BlockPropertiesHelper {
    /** The underlying DTO contained within this object. */
    public final Block.Properties properties;

    /** All of the private fields that cannot be easily set in Block.Properties. */
    private static final Field material = reflect("material");
    private static final Field mapColor = reflect("mapColor");
    private static final Field blocksMovement = reflect("blocksMovement");
    private static final Field soundType = reflect("soundType");
    private static final Field lightValue = reflect("lightValue");
    private static final Field resistance = reflect("resistance");
    private static final Field hardness = reflect("hardness");
    private static final Field ticksRandomly = reflect("ticksRandomly");
    private static final Field slipperiness = reflect("slipperiness");
    private static final Field lootTable = reflect("lootTable");
    private static final Field variableOpacity = reflect("variableOpacity");
    private static final Field harvestLevel = reflect("harvestLevel");
    private static final Field harvestTool = reflect("harvestTool");

    /** Convenience constructor. */
    public BlockPropertiesHelper() {
        this(Block.Properties.create(Material.ROCK));
    }

    /** Primary Constructor. */
    public BlockPropertiesHelper(Block.Properties properties) {
        this.properties = properties;
    }

    public static Block.Properties from(JsonObject json) {
        return new BlockPropertiesHelper()
            .setMaterial(getMaterialOr(json, "material", Material.ROCK))
            .setBlocksMovement(getBoolOr(json, "blocksMovement", true))
            .setSoundType(getSoundTypeOr(json, "soundType", SoundType.STONE))
            .setLightValue(getIntOr(json, "lightValue", 0))
            .setResistance(getFloatOr(json, "resistance", 15))
            .setHardness(getFloatOr(json, "hardness", 3.0F))
            .setTicksRandomly(getBoolOr(json, "ticksRandomly", false))
            .setSlipperiness(getFloatOr(json, "slipperiness", 0F))
            .setLootTable(getLocationOr(json, "lootTable", Blocks.COAL_ORE.getLootTable()))
            .setVariableOpacity(getBoolOr(json, "variableOpacity", false))
            .setHarvestLevel(getIntOr(json, "harvestLevel", 1))
            .setHarvestTool(ToolType.get(getStringOr(json, "harvestTool", "pickaxe")))
            .properties;
    }

    /** Merges the properties from two blocks. */
    public static Block.Properties merge(Block.Properties ore, BlockState bg) {
        return merge(ore, Block.Properties.from(bg.getBlock()));
    }

    /** Merges the properties from two blocks. */
    public static Block.Properties merge(Block.Properties oreProps, Block.Properties bgProps) {
        BlockPropertiesHelper ore = new BlockPropertiesHelper(oreProps);
        BlockPropertiesHelper bg = new BlockPropertiesHelper(bgProps);
        return new BlockPropertiesHelper()
            .setMaterial(bg.getMaterial())
            .setMapColor(bg.getMapColor())
            .setBlocksMovement(ore.getBlocksMovement() || bg.getBlocksMovement())
            .setSoundType(bg.getSoundType())
            .setLightValue(getMax(ore.getLightValue(), bg.getLightValue()))
            .setResistance(getMax(ore.getResistance(), bg.getResistance()))
            .setHardness(mergeHardness(ore.getHardness(), bg.getHardness()))
            .setTicksRandomly(ore.getTicksRandomly() || bg.getTicksRandomly())
            .setSlipperiness(avg(ore.getSlipperiness(), bg.getSlipperiness()))
            .setLootTable(ore.getLootTable())
            .setVariableOpacity(ore.getVariableOpacity() || bg.getVariableOpacity())
            .setHarvestLevel(getMax(ore.getHarvestLevel(), bg.getHarvestLevel()))
            .setHarvestTool(bg.getHarvestTool())
            .properties;
    }

    /** Reflectively sets the material for these properties. */
    public BlockPropertiesHelper setMaterial(Material mat) {
        set(material, mat);
        return this;
    }

    /** Reflectively gets the Material from these properties. */
    public Material getMaterial() {
        return (Material) get(material);
    }

    /** Reflectively sets the map color for these properties. */
    public BlockPropertiesHelper setMapColor(MaterialColor color) {
        set(mapColor, color);
        return this;
    }

    /** Reflectively gets the map color from these properties. */
    public MaterialColor getMapColor() {
        return (MaterialColor) get(mapColor);
    }

    /** Reflectively sets whether these properties block movement. */
    public BlockPropertiesHelper setBlocksMovement(boolean blocks) {
        set(blocksMovement, blocks);
        return this;
    }

    /** Reflectively gets whether these properties block movement. */
    public boolean getBlocksMovement() {
        return (boolean) get(blocksMovement);
    }

    /** Forwards a new sound type to the underlying DTO. */
    public BlockPropertiesHelper setSoundType(SoundType sound) {
        properties.sound(sound);
        return this;
    }

    /** Reflectively gets the sound type from these properties. */
    public SoundType getSoundType() {
        return (SoundType) get(soundType);
    }

    /** Forwards a new light value to the underlying DTO. */
    public BlockPropertiesHelper setLightValue(int value) {
        properties.lightValue(value);
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
        properties.slipperiness(slip);
        return this;
    }

    /** Reflectively gets the slipperiness from these properties. */
    public float getSlipperiness() {
        return (float) get(slipperiness);
    }

    /** Reflectively sets the loot table for tese properties. */
    public BlockPropertiesHelper setLootTable(ResourceLocation location) {
        set(lootTable, location);
        return this;
    }

    /** Reflectively gets the loot table location from these properties. */
    public ResourceLocation getLootTable() {
        return (ResourceLocation) get(lootTable);
    }

    /** Reflectively sets whether the underlying properties have a variable opacity. */
    public BlockPropertiesHelper setVariableOpacity(boolean variable) {
        set(variableOpacity, variable);
        return this;
    }

    /** Reflectively gets whether these properties have variable opacity. */
    public boolean getVariableOpacity() {
        return (boolean) get(variableOpacity);
    }

    /** Forwards a new harvest level to the underlying DTO. */
    public BlockPropertiesHelper setHarvestLevel(int level) {
        properties.harvestLevel(level);
        return this;
    }

    /** Reflectively gets the harvest level from these properties. */
    public int getHarvestLevel() {
        return (int) get(harvestLevel);
    }

    /** Forwards a new harvest tool to the underlying DTO. */
    public BlockPropertiesHelper setHarvestTool(ToolType type) {
        properties.harvestTool(type);
        return this;
    }

    /** Reflectively gets the harvest tool type from these properties. */
    public ToolType getHarvestTool() {
        return (ToolType) get(harvestTool);
    }

    /** Locates a field from Block.Properties., marking it as accessible. */
    private static Field reflect(String name) {
        return ObfuscationReflectionHelper.findField(Block.Properties.class, name);
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