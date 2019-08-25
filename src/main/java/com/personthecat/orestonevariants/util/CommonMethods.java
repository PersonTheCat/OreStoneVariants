package com.personthecat.orestonevariants.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.personthecat.orestonevariants.Main;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class CommonMethods {
    /** Standard System.out.println() call, but less ugly. */
    public static void println(String x) {
        System.out.println(x);
    }

    /** Accesses the mod's main instance to send a message using its LOGGER. */
    public static void info(String x, Object... args) {
        Main.LOGGER.info(x, args);
    }

    /** Accesses the mod's main instance to debug using its LOGGER. */
    public static void debug(String x, Object... args) {
        Main.LOGGER.debug(x, args);
    }

    /** Accesses the mod's main instance to send a warning using its LOGGER. */
    public static void warn(String x, Object... args) {
        Main.LOGGER.warn(x, args);
    }

    /** Accesses the mod's main instance to send an error using its LOGGER. */
    public static void error(String x, Object... args) {
        Main.LOGGER.error(x, args);
    }

    /** Accesses the mod's main instance to log information using its LOGGER. */
    public static void log(Level level, String x, Object... args) {
        if (level.equals(Level.FATAL)) {
            throw runExF(x, args);
        }
        Main.LOGGER.log(level, x, args);
    }

    /** Returns a clean-looking, general-purpose RuntimeException. */
    public static RuntimeException runEx(String x) {
        return new RuntimeException(x);
    }

    /** Shorthand for a RuntimeException using String#format. */
    public static RuntimeException runExF(String x, Object... args) {
        return new RuntimeException(f(x, args));
    }

    /**
     * Uses a linear search algorithm to locate a value in an array,
     * matching the predicate `by`. Shorthand for Stream#findFirst.
     *
     * Example:
     *  // Find x by x.name
     *  Object[] vars = getObjectsWithNames();
     *  Optional<Object> var = find(vars, (x) -> x.name.equals("Cat"));
     *  // You can then get the value -> NPE
     *  Object result = var.get()
     *  // Or use an alternative. Standard java.util.Optional. -> no NPE
     *  Object result = var.orElse(new Object("Cat"))
     */
    public static <T> Optional<T> find(T[] values, Predicate<T> by) {
        for (T val : values) {
            if (by.test(val)) {
                return full(val);
            }
        }
        return empty();
    }

    public static <T> Optional<T> find(Collection<T> values, Predicate<T> by) {
        for (T val : values) {
            if (by.test(val)) {
                return full(val);
            }
        }
        return empty();
    }

    /**
     * Converts a generic List into its standard array counterpart.
     * Unsafe. Should not be used for any primitive data type. In
     * Most cases where this method is used, storing the data in a
     * primitive array probably offers little or no benefit. As a
     * result, I may try to remove this sometime in the near future.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> T[] toArray(List<T> list, Class<T> clazz) {
        return list.toArray((T[]) Array.newInstance(clazz, 0));
    }

    /** Safely retrieves a value from the input map. */
    public static <K, V> Optional<V> safeGet(Map<K, V> map, K key) {
        return Optional.ofNullable(map.get(key));
    }

    /** Determines the extension of the input `file`. */
    public static String extension(final File file) {
        String[] split = file.getName().split(Pattern.quote("."));
        return split[split.length - 1];
    }

    /** Equivalent to multiple String#startsWith calls joined by OR operators. */
    public static boolean startsWithAny(String s, String... match) {
        for (String s2 : match) {
            if (s.startsWith(s2)) {
                return true;
            }
        }
        return false;
    }

    /** A neater way to interpolate strings. */
    public static String f(String s, Object... args) {
        int begin = 0, si = 0, oi = 0;
        StringBuilder sb = new StringBuilder();
        while (true) {
            si = s.indexOf("{}", si);
            if (si >= 0) {
                sb.append(s.substring(begin, si));
                sb.append(args[oi++]);
                begin = si = si + 2;
            } else {
                break;
            }
        }
        return sb.toString();
    }

    /** Shorthand for LogManager#getLogger. */
    public static Logger logger(String name) {
        return LogManager.getLogger(name);
    }

    /** Shorthand for calling Optional#empty. */
    public static <T> Optional<T> empty() {
        return Optional.empty();
    }

    /**
     * Shorthand for calling Optional#of, matching the existing syntax of
     * `empty`, while being more clear than `of` alone.
     */
    public static <T> Optional<T> full(T val) {
        return Optional.of(val);
    }

    public static int getMin(int a, int b) {
        return a < b ? a : b;
    }

    public static int getMax(int a, int b) {
        return a > b ? a : b;
    }

    public static float getMin(float a, float b) {
        return a < b ? a : b;
    }

    public static float getMax(float a, float b) {
        return a > b ? a : b;
    }

    public static float avg(float a, float b) {
        return (a + b) / 2.0F;
    }

    /** Returns a random number between the input bounds. */
    public static int numBetween(Random rand, int min, int max) {
        return min == max ? min : rand.nextInt(max - min) + min;
    }

    /** Divides 1 / `value` without any divide by zero errors or unsightly casting. */
    public static int invert(double value) {
        return value == 0 ? Integer.MAX_VALUE : (int) (1 / value);
    }

    /** Variant of Arrays#sort which returns the array. */
    public static int[] sort(int[] array) {
        Arrays.sort(array);
        return array;
    }

    public static boolean isModLoaded(String mod) {
        return ModList.get().isLoaded(mod);
    }

    /**
     * Used for retrieving a Biome from either a registry name
     * or unique ID. Returns an Optional<Biome> to ensure that
     * null checks are propagated elsewhere.
     */
    public static Optional<Biome> getBiome(String biomeName) {
        return Optional.ofNullable(ForgeRegistries.BIOMES.getValue(new ResourceLocation(biomeName)));
    }

    public static Biome[] getBiomes(BiomeDictionary.Type biomeType) {
        return BiomeDictionary.getBiomes(biomeType).toArray(new Biome[0]);
    }

    public static BiomeDictionary.Type getBiomeType(String name) {
        return BiomeDictionary.Type.getType(name);
    }

    /** Determines whether the input location refers to a block. */
    public static boolean isBlock(String fullName) {
        return isBlock(new ResourceLocation(ignoreData(fullName)));
    }

    /** Variant of #isBlock which directly accepts the ResourceLocation. */
    public static boolean isBlock(ResourceLocation location) {
        return ForgeRegistries.BLOCKS.containsKey(location);
    }

    /** Shorthand for using Mojang's built-in BlockStateParser. */
    public static Optional<BlockState> getBlockState(String fullName) {
        BlockStateParser parser = new BlockStateParser(new StringReader(fullName), true);
        try { // Ignoring tile entities.
            return full(parser.parse(false).getState());
        } catch (CommandSyntaxException e) {
            return empty();
        }
    }

    /** Shorthand for using Mojang's built-in ItemParser. */
    public static Optional<Item> getItem(String fullName) {
        ItemParser parser = new ItemParser(new StringReader(fullName), true);
        try {
            return full(parser.parse().getItem());
        } catch (CommandSyntaxException e) {
            return empty();
        }
    }

    /** Removes any NBT data from the input string. */
    public static String ignoreData(String s) {
        int dataIndex = s.indexOf('{');
        if (dataIndex < 0) {
            dataIndex = s.length();
        }
        return s.substring(0, dataIndex);
    }
}