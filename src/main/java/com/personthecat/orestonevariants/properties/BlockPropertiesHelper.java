package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.util.unsafe.ReflectionTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;
import org.hjson.JsonObject;
import personthecat.fresult.Result;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

/**
 * All of the heinous, unadulterated, unsafe code necessary for interacting
 * with an object that has poorly-designed setters and no getters.
 */
@SuppressWarnings({"unchecked", "WeakerAccess"})
public class BlockPropertiesHelper {
    /** The underlying DTO contained within this object. */
    public final Block.Properties properties;

    /** All of the private fields that cannot be easily set in Block.Properties. */
    private static final Field material = reflect("material", 0);
    private static final Field mapColor = reflect("field_235800_b_", 1);
    private static final Field blocksMovement = reflect("blocksMovement", 2);
    private static final Field soundType = reflect("soundType", 3);
    private static final Field lightValue = reflect("field_235803_e_", 4);
    private static final Field resistance = reflect("resistance", 5);
    private static final Field hardness = reflect("hardness", 6);
    private static final Field unknown1 = reflect("field_235806_h_", 7);
    private static final Field ticksRandomly = reflect("ticksRandomly", 8);
    private static final Field slipperiness = reflect("slipperiness", 9);
    private static final Field speedFactor = reflect("speedFactor", 10);
    private static final Field jumpFactor = reflect("jumpFactor", 11);
    private static final Field lootTable = reflect("lootTable", 12);
    private static final Field isSolid = reflect("isSolid", 13);
    private static final Field unknown2 = reflect("field_235813_o_", 14);
    private static final Field harvestLevel = reflect("harvestLevel", 15);
    private static final Field harvestTool = reflect("harvestTool", 16);
    private static final Field variableOpacity = reflect("variableOpacity", 24);

    /** Convenience constructor. */
    public BlockPropertiesHelper() {
        this(Block.Properties.create(Material.ROCK));
    }

    /** Primary Constructor. */
    public BlockPropertiesHelper(Block.Properties properties) {
        this.properties = properties;
    }

    public static Block.Properties from(JsonObject json) {
        final int lightCalc = getIntOr(json, "light", 0);
        return new BlockPropertiesHelper()
            .setMaterial(getMaterialOr(json, "material", Material.ROCK))
            // map color?
            .setBlocksMovement(getBoolOr(json, "blocksMovement", true))
            .setSoundType(getSoundTypeOr(json, "soundType", SoundType.STONE))
            .setLightValue(s -> lightCalc)
            .setResistance(getFloatOr(json, "resistance", 15))
            .setHardness(getFloatOr(json, "hardness", 3.0F))
            .setUnknown1(getBoolOr(json, "unknown1", false))
            .setTicksRandomly(getBoolOr(json, "ticksRandomly", false))
            .setSlipperiness(getFloatOr(json, "slipperiness", 0.6F))
            .setSpeedFactor(getFloatOr(json, "speedFactor", 1.0F))
            .setJumpFactor(getFloatOr(json, "jumpFactor", 1.0F))
            // Wrong location. Not used here anyway. Remove?
            .setLootTable(getLocationOr(json, "loot", Blocks.COAL_ORE.getLootTable()))
            .setIsSolid(getBoolOr(json, "isSolid", true))
            .setUnknown2(getBoolOr(json, "unknown2", false))
            .setHarvestLevel(getIntOr(json, "level", 1))
            .setHarvestTool(ToolType.get(getStringOr(json, "tool", "pickaxe")))
            .setVariableOpacity(getBoolOr(json, "variableOpacity", false))
            .properties;
    }

    /** Merges the properties from two blocks. */
    public static Block.Properties merge(Block.Properties ore, BlockState bg) {
        return merge(ore, Block.Properties.from(bg.getBlock()), bg);
    }

    /** Merges the properties from two blocks. */
    public static Block.Properties merge(Block.Properties oreProps, Block.Properties bgProps, BlockState bgState) {
        BlockPropertiesHelper ore = new BlockPropertiesHelper(oreProps);
        BlockPropertiesHelper bg = new BlockPropertiesHelper(bgProps);
        return new BlockPropertiesHelper()
            .setMaterial(bg.getMaterial())
            .setMapColor(bg.getMapColor())
            .setBlocksMovement(ore.getBlocksMovement() || bg.getBlocksMovement())
            .setSoundType(bg.getSoundType())
            .setLightValue(s -> getMax(ore.getLightValue().applyAsInt(s), bg.getLightValue().applyAsInt(s)))
            .setResistance(getMax(ore.getResistance(), bg.getResistance()))
            .setHardness(mergeHardness(ore.getHardness(), bg.getHardness()))
            .setUnknown1(ore.getUnknown1() || bg.getUnknown1())
            .setTicksRandomly(ore.getTicksRandomly() || bg.getTicksRandomly())
            .setSlipperiness(avg(ore.getSlipperiness(), bg.getSlipperiness()))
            .setSpeedFactor(avg(ore.getSpeedFactor(), bg.getSpeedFactor()))
            .setJumpFactor(avg(ore.getJumpFactor(), bg.getJumpFactor()))
            .setLootTable(ore.getLootTable())
            .setIsSolid(ore.getIsSolid()) // Good choice?
            .setUnknown2(ore.getUnknown2() || bg.getUnknown2())
            .setHarvestLevel(getMax(ore.getHarvestLevel(), bg.getHarvestLevel()))
            .setHarvestTool(bg.getHarvestTool())
            .setVariableOpacity(ore.getVariableOpacity() || bg.getVariableOpacity())
            .wrapMapColor(bgState)
            .wrapLightValue(bgState)
            .properties;
    }

    /** Tests for and works around IllegalArgumentExceptions. */
    public BlockPropertiesHelper wrapMapColor(BlockState def) {
        final Function<BlockState, MaterialColor> getter = getMapColor();
        setMapColor(s ->
            Result.<MaterialColor, IllegalArgumentException>of(() -> getter.apply(s))
                .ifErr(Result::WARN)
                .orElseGet(() -> getter.apply(def))
        );
        return this;
    }

    /** Tests for and works around IllegalArgumentExceptions. */
    public BlockPropertiesHelper wrapLightValue(BlockState def) {
        final ToIntFunction<BlockState> getter = getLightValue();
        setLightValue(s ->
            Result.<Integer, IllegalArgumentException>of(() -> getter.applyAsInt(s))
                .ifErr(Result::WARN)
                .orElseGet(() -> getter.applyAsInt(def))
        );
        return this;
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
    public BlockPropertiesHelper setMapColor(Function<BlockState, MaterialColor> func) {
        set(mapColor, func);
        return this;
    }

    /** Reflectively gets the map color from these properties. */
    public Function<BlockState, MaterialColor> getMapColor() {
        return (Function<BlockState, MaterialColor>) get(mapColor);
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
    public BlockPropertiesHelper setLightValue(ToIntFunction<BlockState> func) {
        properties.setLightLevel(func);
        return this;
    }

    /** Reflectively gets the light value from these properties. */
    public ToIntFunction<BlockState> getLightValue() {
        return (ToIntFunction<BlockState>) get(lightValue);
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

    public BlockPropertiesHelper setUnknown1(boolean b) {
        set(unknown1, b);
        return this;
    }

    public boolean getUnknown1() {
        return (boolean) get(unknown1);
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

    /** Reflectively sets the speed factor from these properties. */
    public BlockPropertiesHelper setSpeedFactor(float factor) {
        set(speedFactor, factor);
        return this;
    }

    /** Reflectively gets the speed factor from these properties. */
    public float getSpeedFactor() {
        return (float) get(speedFactor);
    }

    /** Reflectively sets the jump factor from these properties. */
    public BlockPropertiesHelper setJumpFactor(float factor) {
        set(jumpFactor, factor);
        return this;
    }

    /** Reflectively gets the jump factor from these properties. */
    public float getJumpFactor() {
        return (float) get(jumpFactor);
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

    /** Reflectively sets whether the block is solid from these properties. */
    public BlockPropertiesHelper setIsSolid(boolean solid) {
        set(isSolid, solid);
        return this;
    }

    /** Reflectively gets whether the block is solid from these properties. */
    public boolean getIsSolid() {
        return (boolean) get(isSolid);
    }

    public BlockPropertiesHelper setUnknown2(boolean b) {
        set(unknown2, b);
        return this;
    }

    public boolean getUnknown2() {
        return (boolean) get(unknown2);
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

    /** Reflectively sets whether the underlying properties have a variable opacity. */
    public BlockPropertiesHelper setVariableOpacity(boolean variable) {
        set(variableOpacity, variable);
        return this;
    }

    /** Reflectively gets whether these properties have variable opacity. */
    public boolean getVariableOpacity() {
        return (boolean) get(variableOpacity);
    }

    /** Locates a field from Block.Properties., marking it as accessible. */
    private static Field reflect(String name, int index) {
        return ReflectionTools.getField(Block.Properties.class, name, index);
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