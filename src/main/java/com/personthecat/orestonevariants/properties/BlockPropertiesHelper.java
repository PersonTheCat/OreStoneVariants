package com.personthecat.orestonevariants.properties;

import lombok.extern.log4j.Log4j2;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;
import org.hjson.JsonObject;
import personthecat.fresult.Result;

import java.util.function.Function;
import java.util.function.ToIntFunction;

import static com.personthecat.orestonevariants.util.CommonMethods.avg;
import static com.personthecat.orestonevariants.util.CommonMethods.getMax;
import static com.personthecat.orestonevariants.util.HjsonTools.getBoolOr;
import static com.personthecat.orestonevariants.util.HjsonTools.getFloatOr;
import static com.personthecat.orestonevariants.util.HjsonTools.getIntOr;
import static com.personthecat.orestonevariants.util.HjsonTools.getLocationOr;
import static com.personthecat.orestonevariants.util.HjsonTools.getMaterialOr;
import static com.personthecat.orestonevariants.util.HjsonTools.getSoundTypeOr;
import static com.personthecat.orestonevariants.util.HjsonTools.getStringOr;

/**
 * The block properties object has really bad setters and no getters (minus Forge's values).
 * This class provides a wrapper for conveniently handling that using access transformers.
 */
@Log4j2
@SuppressWarnings("WeakerAccess")
public class BlockPropertiesHelper {

    /** The underlying DTO contained within this object. */
    public final Block.Properties properties;

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
        // This is hardcoded to support redstone. Todo: make it configurable.
        final ToIntFunction<BlockState> lightGetter = state -> {
            if (state.getProperties().contains(RedstoneOreBlock.LIT)) {
                return state.get(RedstoneOreBlock.LIT) ? lightCalc : 0;
            }
            return lightCalc;
        };
        return new BlockPropertiesHelper()
            .setMaterial(getMaterialOr(json, "material", Material.ROCK))
            // map color?
            .setBlocksMovement(getBoolOr(json, "blocksMovement", true))
            .setSoundType(getSoundTypeOr(json, "soundType", SoundType.STONE))
            .setLightValue(lightGetter)
            .setResistance(getFloatOr(json, "resistance", 3.0F))
            .setHardness(getFloatOr(json, "hardness", 3.0F))
            .setRequiresTool(getBoolOr(json, "requiresTool", false))
            .setTicksRandomly(getBoolOr(json, "ticksRandomly", false))
            .setSlipperiness(getFloatOr(json, "slipperiness", 0.6F))
            .setSpeedFactor(getFloatOr(json, "speedFactor", 1.0F))
            .setJumpFactor(getFloatOr(json, "jumpFactor", 1.0F))
            // Wrong location. Not used here anyway. Remove?
            .setLootTable(getLocationOr(json, "loot", Blocks.COAL_ORE.getLootTable()))
            .setIsSolid(getBoolOr(json, "isSolid", true))
            .setIsAir(getBoolOr(json, "isAir", false))
            .setHarvestLevel(getIntOr(json, "level", 1))
            .setHarvestTool(ToolType.get(getStringOr(json, "tool", "pickaxe")))
            .setVariableOpacity(getBoolOr(json, "variableOpacity", false))
            .properties;
    }

    /** Merges the properties from two blocks. */
    public static Block.Properties merge(Block.Properties ore, Block bg) {
        return merge(ore, Block.Properties.from(bg), bg.getDefaultState());
    }

    /** Merges the properties from two blocks. */
    public static Block.Properties merge(Block.Properties oreProps, Block.Properties bgProps, BlockState bgState) {
        BlockPropertiesHelper ore = new BlockPropertiesHelper(oreProps);
        BlockPropertiesHelper bg = new BlockPropertiesHelper(bgProps);
        ToolType tool = bg.getHarvestTool();
        if (tool == null) {
            log.warn("Harvest tool for bg block {} is null. Defaulting to fg block.", bgState);
            tool = ore.getHarvestTool();
        }
        return new BlockPropertiesHelper()
            .setMaterial(bg.getMaterial())
            .setMapColor(bg.getMapColor())
            .setBlocksMovement(ore.getBlocksMovement() || bg.getBlocksMovement())
            .setSoundType(bg.getSoundType())
            .setLightValue(s -> getMax(ore.getLightValue().applyAsInt(s), bg.getLightValue().applyAsInt(s)))
            .setResistance(getMax(ore.getResistance(), bg.getResistance()))
            .setHardness(mergeHardness(ore.getHardness(), bg.getHardness()))
            .setRequiresTool(ore.getRequiresTool() || bg.getRequiresTool())
            .setTicksRandomly(ore.getTicksRandomly() || bg.getTicksRandomly())
            .setSlipperiness(avg(ore.getSlipperiness(), bg.getSlipperiness()))
            .setSpeedFactor(avg(ore.getSpeedFactor(), bg.getSpeedFactor()))
            .setJumpFactor(avg(ore.getJumpFactor(), bg.getJumpFactor()))
            .setLootTable(ore.getLootTable())
            .setIsSolid(ore.getIsSolid()) // Good choice?
            .setIsAir(ore.getIsAir() || bg.getIsAir())
            .setHarvestLevel(getMax(ore.getHarvestLevel(), bg.getHarvestLevel()))
            .setHarvestTool(tool)
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

    public BlockPropertiesHelper setMaterial(Material mat) {
        this.properties.material = mat;
        return this;
    }

    public Material getMaterial() {
        return this.properties.material;
    }

    public BlockPropertiesHelper setMapColor(Function<BlockState, MaterialColor> func) {
        this.properties.blockColors = func;
        return this;
    }

    public Function<BlockState, MaterialColor> getMapColor() {
        return this.properties.blockColors;
    }

    public BlockPropertiesHelper setBlocksMovement(boolean blocks) {
        this.properties.blocksMovement = blocks;
        return this;
    }

    public boolean getBlocksMovement() {
        return this.properties.blocksMovement;
    }

    public BlockPropertiesHelper setSoundType(SoundType sound) {
        this.properties.soundType = sound;
        return this;
    }

    public SoundType getSoundType() {
        return this.properties.soundType;
    }

    public BlockPropertiesHelper setLightValue(ToIntFunction<BlockState> func) {
        this.properties.lightLevel = func;
        return this;
    }

    public ToIntFunction<BlockState> getLightValue() {
        return this.properties.lightLevel;
    }

    /** Reflectively sets the resistance for the properties. */
    public BlockPropertiesHelper setResistance(float r) {
        this.properties.resistance = r;
        return this;
    }

    public float getResistance() {
        return this.properties.resistance;
    }

    public BlockPropertiesHelper setHardness(float h) {
        this.properties.hardness = h;
        return this;
    }

    public float getHardness() {
        return this.properties.hardness;
    }

    /** Combines the two hardness values, accounting for unbreakable background blocks. */
    private static float mergeHardness(float ore, float bg) {
        return bg < 0 ? -1 : getMax(ore + bg - 1.5F, 0F);
    }

    public BlockPropertiesHelper setRequiresTool(boolean b) {
        this.properties.requiresTool = b;
        return this;
    }

    public boolean getRequiresTool() {
        return this.properties.requiresTool;
    }

    public BlockPropertiesHelper setTicksRandomly(boolean ticks) {
        this.properties.ticksRandomly = ticks;
        return this;
    }

    public boolean getTicksRandomly() {
        return this.properties.ticksRandomly;
    }

    public BlockPropertiesHelper setSlipperiness(float slip) {
        this.properties.slipperiness = slip;
        return this;
    }

    public float getSlipperiness() {
        return this.properties.slipperiness;
    }

    public BlockPropertiesHelper setSpeedFactor(float factor) {
        this.properties.speedFactor = factor;
        return this;
    }

    public float getSpeedFactor() {
        return this.properties.speedFactor;
    }

    public BlockPropertiesHelper setJumpFactor(float factor) {
        this.properties.jumpFactor = factor;
        return this;
    }

    public float getJumpFactor() {
        return this.properties.jumpFactor;
    }

    public BlockPropertiesHelper setLootTable(ResourceLocation location) {
        this.properties.lootTable = location;
        return this;
    }

    public ResourceLocation getLootTable() {
        return this.properties.lootTable;
    }

    public BlockPropertiesHelper setIsSolid(boolean solid) {
        this.properties.isSolid = solid;
        return this;
    }

    public boolean getIsSolid() {
        return this.properties.isSolid;
    }

    public BlockPropertiesHelper setIsAir(boolean b) {
        this.properties.isAir = b;
        return this;
    }

    public boolean getIsAir() {
        return this.properties.isAir;
    }

    public BlockPropertiesHelper setHarvestLevel(int level) {
        this.properties.harvestLevel(level);
        return this;
    }

    public int getHarvestLevel() {
        return this.properties.getHarvestLevel();
    }

    public BlockPropertiesHelper setHarvestTool(ToolType type) {
        this.properties.harvestTool(type);
        return this;
    }

    public ToolType getHarvestTool() {
        return this.properties.getHarvestTool();
    }

    public BlockPropertiesHelper setVariableOpacity(boolean variable) {
        this.properties.variableOpacity = variable;
        return this;
    }

    public boolean getVariableOpacity() {
        return this.properties.variableOpacity;
    }
}