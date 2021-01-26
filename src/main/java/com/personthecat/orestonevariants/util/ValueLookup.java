package com.personthecat.orestonevariants.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import java.util.Optional;

import static com.personthecat.orestonevariants.util.CommonMethods.nullable;

/**
 * Used for serialization of non-enum static types. This could be avoided
 * via reflection, but in general, I prefer to avoid that.
 */
public class ValueLookup {

    /** A map of every vanilla material to its name. */
    private static final BiMap<String, Material> MATERIAL_MAP = ImmutableBiMap.<String, Material>builder()
        .put("AIR", Material.AIR)
        .put("STRUCTURE_VOID", Material.STRUCTURE_VOID)
        .put("PORTAL", Material.PORTAL)
        .put("CARPET", Material.CARPET)
        .put("PLANTS", Material.PLANTS)
        .put("OCEAN_PLANT", Material.OCEAN_PLANT)
        .put("TALL_PLANTS", Material.TALL_PLANTS)
        .put("NETHER_PLANTS", Material.NETHER_PLANTS)
        .put("SEA_GRASS", Material.SEA_GRASS)
        .put("WATER", Material.WATER)
        .put("BUBBLE_COLUMN", Material.BUBBLE_COLUMN)
        .put("LAVA", Material.LAVA)
        .put("SNOW", Material.SNOW)
        .put("FIRE", Material.FIRE)
        .put("MISCELLANEOUS", Material.MISCELLANEOUS)
        .put("WEB", Material.WEB)
        .put("REDSTONE_LIGHT", Material.REDSTONE_LIGHT)
        .put("CLAY", Material.CLAY)
        .put("EARTH", Material.EARTH)
        .put("ORGANIC", Material.ORGANIC)
        .put("PACKED_ICE", Material.PACKED_ICE)
        .put("SAND", Material.SAND)
        .put("SPONGE", Material.SPONGE)
        .put("SHULKER", Material.SHULKER)
        .put("WOOD", Material.WOOD)
        .put("NETHER_WOOD", Material.NETHER_WOOD)
        .put("BAMBOO_SAPLING", Material.BAMBOO_SAPLING)
        .put("BAMBOO", Material.BAMBOO)
        .put("WOOL", Material.WOOL)
        .put("TNT", Material.TNT)
        .put("LEAVES", Material.LEAVES)
        .put("GLASS", Material.GLASS)
        .put("ICE", Material.ICE)
        .put("CACTUS", Material.CACTUS)
        .put("ROCK", Material.ROCK)
        .put("IRON", Material.IRON)
        .put("SNOW_BLOCK", Material.SNOW_BLOCK)
        .put("ANVIL", Material.ANVIL)
        .put("BARRIER", Material.BARRIER)
        .put("PISTON", Material.PISTON)
        .put("CORAL", Material.CORAL)
        .put("GOURD", Material.GOURD)
        .put("DRAGON_EGG", Material.DRAGON_EGG)
        .put("CAKE", Material.CAKE)
        .build();

    /** A map of every vanilla sound type to its name */
    private static final BiMap<String, SoundType> SOUND_MAP = ImmutableBiMap.<String, SoundType>builder()
        .put("WOOD", SoundType.WOOD)
        .put("GROUND", SoundType.GROUND)
        .put("PLANT", SoundType.PLANT)
        .put("LILY_PADS", SoundType.LILY_PADS)
        .put("STONE", SoundType.STONE)
        .put("METAL", SoundType.METAL)
        .put("GLASS", SoundType.GLASS)
        .put("CLOTH", SoundType.CLOTH)
        .put("SAND", SoundType.SAND)
        .put("SNOW", SoundType.SNOW)
        .put("LADDER", SoundType.LADDER)
        .put("ANVIL", SoundType.ANVIL)
        .put("SLIME", SoundType.SLIME)
        .put("HONEY", SoundType.HONEY)
        .put("WET_GRASS", SoundType.WET_GRASS)
        .put("CORAL", SoundType.CORAL)
        .put("BAMBOO", SoundType.BAMBOO)
        .put("BAMBOO_SAPLING", SoundType.BAMBOO_SAPLING)
        .put("SCAFFOLDING", SoundType.SCAFFOLDING)
        .put("SWEET_BERRY_BUSH", SoundType.SWEET_BERRY_BUSH)
        .put("CROP", SoundType.CROP)
        .put("STEM", SoundType.STEM)
        .put("VINE", SoundType.VINE)
        .put("NETHER_WART", SoundType.NETHER_WART)
        .put("LANTERN", SoundType.LANTERN)
        .put("HYPHAE", SoundType.HYPHAE)
        .put("NYLIUM", SoundType.NYLIUM)
        .put("FUNGUS", SoundType.FUNGUS)
        .put("ROOT", SoundType.ROOT)
        .put("SHROOMLIGHT", SoundType.SHROOMLIGHT)
        .put("NETHER_VINE", SoundType.NETHER_VINE)
        .put("NETHER_VINE_LOWER_PITCH", SoundType.NETHER_VINE_LOWER_PITCH)
        .put("SOUL_SAND", SoundType.SOUL_SAND)
        .put("SOUL_SOIL", SoundType.SOUL_SOIL)
        .put("BASALT", SoundType.BASALT)
        .put("WART", SoundType.NETHERRACK)
        .put("NETHER_BRICK", SoundType.NETHER_BRICK)
        .put("NETHER_SPROUT", SoundType.NETHER_SPROUT)
        .put("NETHER_ORE", SoundType.NETHER_ORE)
        .put("BONE", SoundType.BONE)
        .put("NETHERITE", SoundType.NETHERITE)
        .put("ANCIENT_DEBRIS", SoundType.ANCIENT_DEBRIS)
        .put("LODESTONE", SoundType.LODESTONE)
        .put("CHAIN", SoundType.CHAIN)
        .put("NETHER_GOLD", SoundType.NETHER_GOLD)
        .put("GILDED_BLACKSTONE", SoundType.GILDED_BLACKSTONE)
        .build();

    /** Attempts to retrieve a material from the map. */
    public static Optional<Material> getMaterial(String key) {
        return nullable(MATERIAL_MAP.get(key.toUpperCase()));
    }

    /** Attempts to retrieve a sound type from the map. */
    public static Optional<SoundType> getSoundType(String key) {
        return nullable(SOUND_MAP.get(key.toUpperCase()));
    }

    /** Converts the input material to a string. */
    public static Optional<String> serialize(Material value) {
        return nullable(MATERIAL_MAP.inverse().get(value));
    }

    /** Converts the input sound type to a string. */
    public static Optional<String> serialize(SoundType value) {
        return nullable(SOUND_MAP.inverse().get(value));
    }
}