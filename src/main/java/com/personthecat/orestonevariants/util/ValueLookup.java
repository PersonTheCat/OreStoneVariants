package com.personthecat.orestonevariants.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

import java.util.Optional;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

/**
 * Used for serialization of non-enum static types. This could be avoided
 * via reflection, but in general, I prefer to avoid that.
 */
public class ValueLookup {

    /** A map of every vanilla material to its name. */
    private static final BiMap<String, Material> MATERIAL_MAP = ImmutableBiMap.<String, Material>builder()
        .put("AIR", Material.AIR)
        .put("GRASS", Material.GRASS)
        .put("GROUND", Material.GROUND)
        .put("WOOD", Material.WOOD)
        .put("ROCK", Material.ROCK)
        .put("IRON", Material.IRON)
        .put("ANVIL", Material.ANVIL)
        .put("WATER", Material.WATER)
        .put("LAVA", Material.LAVA)
        .put("LEAVES", Material.LEAVES)
        .put("PLANTS", Material.PLANTS)
        .put("VINE", Material.VINE)
        .put("SPONGE", Material.SPONGE)
        .put("CLOTH", Material.CLOTH)
        .put("SAND", Material.SAND)
        .put("CARPET", Material.CARPET)
        .put("GLASS", Material.GLASS)
        .put("REDSTONE_LIGHT", Material.REDSTONE_LIGHT)
        .put("TNT", Material.TNT)
        .put("CORAL", Material.CORAL)
        .put("ICE", Material.ICE)
        .put("PACKED_ICE", Material.PACKED_ICE)
        .put("SNOW", Material.SNOW)
        .put("CRAFTED_SNOW", Material.CRAFTED_SNOW)
        .put("CACTUS", Material.CACTUS)
        .put("CLAY", Material.CLAY)
        .put("GOURD", Material.GOURD)
        .put("DRAGON_EGG", Material.DRAGON_EGG)
        .put("PORTAL", Material.PORTAL)
        .put("CAKE", Material.CAKE)
        .put("WEB", Material.WEB)
        .put("PISTON", Material.PISTON)
        .put("BARRIER", Material.BARRIER)
        .build();

    /** A map of every vanilla map color to its name. */
    private static final BiMap<String, MapColor> COLOR_MAP = ImmutableBiMap.<String, MapColor>builder()
        .put("AIR", MapColor.AIR)
        .put("GRASS", MapColor.GRASS)
        .put("SAND", MapColor.SAND)
        .put("CLOTH", MapColor.CLOTH)
        .put("TNT", MapColor.TNT)
        .put("ICE", MapColor.ICE)
        .put("IRON", MapColor.IRON)
        .put("FOLIAGE", MapColor.FOLIAGE)
        .put("SNOW", MapColor.SNOW)
        .put("CLAY", MapColor.CLAY)
        .put("DIRT", MapColor.DIRT)
        .put("STONE", MapColor.STONE)
        .put("WATER", MapColor.WATER)
        .put("WOOD", MapColor.WOOD)
        .put("QUARTZ", MapColor.QUARTZ)
        .put("ADOBE", MapColor.ADOBE)
        .put("MAGENTA", MapColor.MAGENTA)
        .put("LIGHT_BLUE", MapColor.LIGHT_BLUE)
        .put("YELLOW", MapColor.YELLOW)
        .put("LIME", MapColor.LIME)
        .put("PINK", MapColor.PINK)
        .put("GRAY", MapColor.GRAY)
        .put("SILVER", MapColor.SILVER)
        .put("CYAN", MapColor.CYAN)
        .put("PURPLE", MapColor.PURPLE)
        .put("BLUE", MapColor.BLUE)
        .put("BROWN", MapColor.BROWN)
        .put("GREEN", MapColor.GREEN)
        .put("RED", MapColor.RED)
        .put("BLACK", MapColor.BLACK)
        .put("GOLD", MapColor.GOLD)
        .put("DIAMOND", MapColor.DIAMOND)
        .put("LAPIS", MapColor.LAPIS)
        .put("EMERALD", MapColor.EMERALD)
        .put("OBSIDIAN", MapColor.OBSIDIAN)
        .put("NETHERRACK", MapColor.NETHERRACK)
        .put("WHITE_STAINED_HARDENED_CLAY", MapColor.WHITE_STAINED_HARDENED_CLAY)
        .put("ORANGE_STAINED_HARDENED_CLAY", MapColor.ORANGE_STAINED_HARDENED_CLAY)
        .put("MAGENTA_STAINED_HARDENED_CLAY", MapColor.MAGENTA_STAINED_HARDENED_CLAY)
        .put("LIGHT_BLUE_STAINED_HARDENED_CLAY", MapColor.LIGHT_BLUE_STAINED_HARDENED_CLAY)
        .put("YELLOW_STAINED_HARDENED_CLAY", MapColor.YELLOW_STAINED_HARDENED_CLAY)
        .put("LIME_STAINED_HARDENED_CLAY", MapColor.LIME_STAINED_HARDENED_CLAY)
        .put("PINK_STAINED_HARDENED_CLAY", MapColor.PINK_STAINED_HARDENED_CLAY)
        .put("GRAY_STAINED_HARDENED_CLAY", MapColor.GRAY_STAINED_HARDENED_CLAY)
        .put("SILVER_STAINED_HARDENED_CLAY", MapColor.SILVER_STAINED_HARDENED_CLAY)
        .put("CYAN_STAINED_HARDENED_CLAY", MapColor.CYAN_STAINED_HARDENED_CLAY)
        .put("PURPLE_STAINED_HARDENED_CLAY", MapColor.PURPLE_STAINED_HARDENED_CLAY)
        .put("BLUE_STAINED_HARDENED_CLAY", MapColor.BLUE_STAINED_HARDENED_CLAY)
        .put("BROWN_STAINED_HARDENED_CLAY", MapColor.BROWN_STAINED_HARDENED_CLAY)
        .put("GREEN_STAINED_HARDENED_CLAY", MapColor.GREEN_STAINED_HARDENED_CLAY)
        .put("RED_STAINED_HARDENED_CLAY", MapColor.RED_STAINED_HARDENED_CLAY)
        .put("BLACK_STAINED_HARDENED_CLAY", MapColor.BLACK_STAINED_HARDENED_CLAY)
        .build();

    /** A map of every vanilla sound type to its name */
    private static final BiMap<String, SoundType> SOUND_MAP = ImmutableBiMap.<String, SoundType>builder()
        .put("WOOD", SoundType.WOOD)
        .put("GROUND", SoundType.GROUND)
        .put("PLANT", SoundType.PLANT)
        .put("STONE", SoundType.STONE)
        .put("METAL", SoundType.METAL)
        .put("GLASS", SoundType.GLASS)
        .put("CLOTH", SoundType.CLOTH)
        .put("SAND", SoundType.SAND)
        .put("SNOW", SoundType.SNOW)
        .put("LADDER", SoundType.LADDER)
        .put("ANVIL", SoundType.ANVIL)
        .put("SLIME", SoundType.SLIME)
        .build();

    /** Attempts to retrieve a material from the map. */
    public static Optional<Material> getMaterial(String key) {
        return nullable(MATERIAL_MAP.get(key.toUpperCase()));
    }

    /** Attempts to retrieve a map color from the map. */
    public static Optional<MapColor> getMapColor(String key) {
        return nullable(COLOR_MAP.get(key.toUpperCase()));
    }

    /** Attempts to retrieve a sound type from the map. */
    public static Optional<SoundType> getSoundType(String key) {
        return nullable(SOUND_MAP.get(key.toUpperCase()));
    }

    /** Converts the input material to a string. */
    public static Optional<String> serialize(Material value) {
        return nullable(MATERIAL_MAP.inverse().get(value));
    }

    /** Converts the input map color to a string. */
    public static Optional<String> serialize(MapColor value) {
        return nullable(COLOR_MAP.inverse().get(value));
    }

    /** Converts the input sound type to a string. */
    public static Optional<String> serialize(SoundType value) {
        return nullable(SOUND_MAP.inverse().get(value));
    }
}