package com.personthecat.orestonevariants.properties;

import net.minecraft.client.resources.I18n;
import org.hjson.JsonObject;

import static com.personthecat.orestonevariants.util.HjsonTools.*;

/** A more convenient variant of Block.Properties. */
public class BlockProperties {
    private final String languageKey;
    private final String background;
    private final String texture;
    private final float hardness;
    private final float lightLevel;
    private final int level;

    /** Allows BlockProperties to be retrieved from JsonObjects. */
    public BlockProperties(JsonObject json) {
        this(
            getStringOr(json, "languageKey", "oreAnonymous"),
            getStringOr(json, "background", "assets/minecraft/blocks/stone"),
            getStringOr(json, "texture", "assets/minecraft/blocks/coal_ore"),
            getFloatOr(json, "hardness", 3.0f),
            getFloatOr(json, "lightLevel", 0.0f),
            getIntOr(json, "level", 0)
        );
    }

    /** Primary constructor. */
    public BlockProperties(
        String languageKey,
        String background,
        String texture,
        float hardness,
        float lightLevel,
        int level
    ) {
        this.languageKey = languageKey;
        this.background = background;
        this.texture = texture;
        this.hardness = hardness;
        this.lightLevel = lightLevel;
        this.level = level;
    }

    /** Returns a path to these properties' original texture. */
    public String getOriginalTexture() {
        return texture;
    }

    /** Returns the texture used for extracting an overlay. */
    public String getBackground() {
        return background;
    }

    /** Returns the language key of the original ore. */
    public String getLanguageKey() {
        return languageKey;
    }

    /** Returns the hardness value of the original ore. */
    public float getHardness() {
        return hardness;
    }

    /** Returns the light level of the original ore. */
    public float getLightLevel() {
        return lightLevel;
    }

    /** Returns the mining level of the original ore. */
    public int getLevel() {
        return level;
    }

    /** Returns a translated string matching the original ore's display. */
    public String localizedName() {
        return I18n.format(getLanguageKey());
    }
}