package com.personthecat.orestonevariants.util;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.personthecat.orestonevariants.Main;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.Dimension;
import net.minecraft.world.biome.*;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@SuppressWarnings({"WeakerAccess", "unused"})
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

    /** Accesses the mod's main instance to send a fatal error using its LOGGER. */
    public static void fatal(String x, Object... args) {
        Main.LOGGER.fatal(x, args);
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

    /** Converts any standard exception into a RuntimeException. */
    public static RuntimeException runEx(Throwable e) {
        return new RuntimeException(e);
    }

    /** Shorthand for RuntimeException::new(String, Throwable). */
    public static RuntimeException runEx(String x, Throwable e) {
        return new RuntimeException(x, e);
    }

    /** Shorthand for a RuntimeException using String#format. */
    public static RuntimeException runExF(String x, Object... args) {
        return new RuntimeException(f(x, args));
    }

    /** Shorthand for a simple CommandSyntaxException. */
    public static CommandSyntaxException cmdEx(StringReader reader, String msg) {
        final int cursor = reader.getCursor();
        final String input = reader.getString().substring(0, cursor);
        final Message m = new LiteralMessage(msg);
        return new CommandSyntaxException(new SimpleCommandExceptionType(m), m, input, cursor);
    }

    /** Returns a CommandSyntaxException with formatted String via #f. */
    public static CommandSyntaxException cmdExF(StringReader reader, String msg, Object... args) {
        return cmdEx(reader, f(msg, args));
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

    public static <T> List<T> findAll(Collection<T> values, Predicate<T> by) {
        final List<T> list = new ArrayList<>();
        for (T val : values) {
            if (by.test(val)) {
                list.add(val);
            }
        }
        return list;
    }

    /** Determines whether any value in the collection matches the predicate. */
    public static <T> boolean anyMatches(Collection<T> values, Predicate<T> by) {
        for (T val : values) {
            if (by.test(val)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean anyMatches(T[] values, Predicate<T> by) {
        for (T val : values) {
            if (by.test(val)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a generic List into its standard array counterpart.
     * Unsafe. Should not be used for any primitive data type. In
     * Most cases where this method is used, storing the data in a
     * primitive array probably offers little or no benefit. As a
     * result, I may try to remove this sometime in the near future.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(List<T> list, Class<T> clazz) {
        return list.toArray((T[]) Array.newInstance(clazz, 0));
    }

    /** Safely retrieves a value from the input map. */
    public static <K, V> Optional<V> safeGet(Map<K, V> map, K key) {
        return Optional.ofNullable(map.get(key));
    }

    /** Safely retrieves a value from the input array. */
    public static <T> Optional<T> safeGet(T[] array, int index) {
        return index >= 0 && index < array.length ? full(array[index]) : empty();
    }

    public static <K, V> Optional<V> biLookup(Map<K, V> map, BiPredicate<K, V> by) {
        return map.entrySet().stream()
            .filter(e -> by.test(e.getKey(), e.getValue()))
            .findFirst()
            .map(Map.Entry::getValue);
    }

    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> cloneList(ArrayList<T> list) {
        return (ArrayList<T>) list.clone();
    }

    public static <T> List<T> list(T... values) {
        final List<T> list = new ArrayList<>();
        Collections.addAll(list, values);
        return list;
    }

    /** Determines the extension of the input `file`. */
    public static String extension(final File file) {
        String[] split = file.getName().split(Pattern.quote("."));
        return split[split.length - 1];
    }

    /** Gets the name of the file, minus the extension. */
    public static String noExtension(final File file) {
        String[] split = file.getName().split(Pattern.quote("."));
        return split[0];
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
        sb.append(s.substring(begin));
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

    /** Shorthand for calling Optional#ofNullable. */
    public static <T> Optional<T> nullable(T val) {
        return Optional.ofNullable(val);
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

    public static double getMin(double a, double b) {
        return a < b ? a : b;
    }

    public static double getMax(double a, double b) {
        return a > b ? a : b;
    }

    public static float avg(float a, float b) {
        return (a + b) / 2.0F;
    }

    /** Returns a random number between the input bounds. */
    public static int numBetween(Random rand, int min, int max) {
        return min == max ? min : rand.nextInt(max - min + 1) + min;
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

    public static File getConfigDir() {
        return new File(FMLLoader.getGamePath() + "/config");
    }

    public static File getOSVDir() {
        return new File(FMLLoader.getGamePath() + "/config/" + Main.MODID);
    }

    /**
     * Used for retrieving a Biome from either a registry name
     * or unique ID. Returns an Optional<Biome> to ensure that
     * null checks are propagated elsewhere.
     */
    public static Optional<Biome> getBiome(String biomeName) {
        final ResourceLocation location = new ResourceLocation(biomeName);
        final RegistryKey<Biome> key = RegistryKey.func_240903_a_(Registry.BIOME_KEY, location);
        return WorldGenRegistries.field_243657_i.func_243575_c(key);
    }

    public static Biome[] getBiomes(Biome.Category category) {
        return WorldGenRegistries.field_243657_i.stream()
            .filter(b -> b.getCategory().equals(category))
            .toArray(Biome[]::new);
    }

    public static Biome.Category getBiomeType(String name) {
        return Biome.Category.valueOf(name.toUpperCase());
    }

    /** Shorthand for creating a new ResourceLocation with OSV as the namespace. */
    public static ResourceLocation osvLocation(String name) {
        return new ResourceLocation(Main.MODID, name);
    }

    /** Shorthand for ModelResourceLocation#new. */
    public static ModelResourceLocation mrl(ResourceLocation location, String id) {
        return new ModelResourceLocation(location, id);
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

    public static Optional<ItemStack> getStack(String fullName) {
        return getItem(fullName).map(ItemStack::new);
    }

    public static ItemStack toStack(BlockState from) {
        final Block block = from.getBlock();
        return new ItemStack(block, 1);
    }

    /** Produces a formatted identifier from a foreground and background state. */
    public static String formatStates(BlockState fg, BlockState bg) {
        final StringBuilder sb = new StringBuilder(formatState(fg));
        if (sb.length() > 0) {
            sb.append('_');
        }
        sb.append(formatState(bg));
        return sb.toString();
    }

    /** Produces a formatted identifier from `state`'s registry name. */
    public static String formatState(BlockState state) {
        final ResourceLocation registry = nullable(state.getBlock().getRegistryName())
            .orElseThrow(() -> runExF("Block not registered in time: {}.", state));
        return formatBlock(registry);
    }

    private static String formatBlock(ResourceLocation registry) {
        final String mod = registry.getNamespace();
        final String block = registry.getPath();

        final StringBuilder sb = new StringBuilder();
        if (!mod.equals("minecraft")) {
            sb.append(mod);
        }
        if (!block.equals("stone")) {
            appendSegment(sb, block);
        }
        return sb.toString();
    }

    private static void appendSegment(StringBuilder sb, String segment) {
        final int length = sb.length();
        // !sb.endsWith("_")
        if (length > 0 && !sb.substring(length - 1).equals("_")) {
            sb.append("_");
        }
        sb.append(segment);
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