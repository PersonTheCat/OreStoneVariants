package com.personthecat.orestonevariants.util;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.properties.OreProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.item.Item;
import net.minecraft.state.Property;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.*;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;
import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("WeakerAccess")
public class CommonMethods {

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

    public static <K, V> Optional<V> find(Map<K, V> map, Predicate<V> by) {
        return find(map.values(), by);
    }

    public static <T> List<T> findAll(Collection<T> values, Predicate<T> by) {
        final List<T> all = new ArrayList<>();
        for (T val : values) {
            if (by.test(val)) {
                all.add(val);
            }
        }
        return all;
    }

    public static <K, V> List<V> findAll(Map<K, V> map, Predicate<V> by) {
        return findAll(map.values(), by);
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

    /** Safely retrieves a value from the input map. */
    public static <K, V> Optional<V> safeGet(Map<K, V> map, K key) {
        return Optional.ofNullable(map.get(key));
    }

    /** Safely retrieves a value from the input array. */
    public static <T> Optional<T> safeGet(T[] array, int index) {
        return index >= 0 && index < array.length ? full(array[index]) : empty();
    }

    @SafeVarargs
    public static <T> List<T> list(T... values) {
        final List<T> list = new ArrayList<>();
        Collections.addAll(list, values);
        return list;
    }

    /** Determines the extension of the input `file`. */
    public static String extension(final File file) {
        final String name = file.getName();
        return name.substring(name.lastIndexOf(".") + 1);
    }

    /** Gets the name of the file, minus the extension. */
    public static String noExtension(final File file) {
        final String name = file.getName();
        return name.substring(0, name.indexOf("."));
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
                sb.append(s, begin, si);
                sb.append(args[oi++]);
                begin = si = si + 2;
            } else {
                break;
            }
        }
        sb.append(s.substring(begin));
        return sb.toString();
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

    public static float avg(float a, float b) {
        return (a + b) / 2.0F;
    }

    /** Returns a random number between the input bounds, inclusive. */
    public static int numBetween(Random rand, int min, int max) {
        return min == max ? min : rand.nextInt(max - min + 1) + min;
    }

    /** Variant of Arrays#sort which returns the array. */
    public static int[] sort(int[] array) {
        Arrays.sort(array);
        return array;
    }

    public static boolean isModLoaded(String mod) {
        return ModList.get().isLoaded(mod);
    }

    public static File getOSVDir() {
        return new File(FMLLoader.getGamePath() + "/config/" + Main.MOD_ID);
    }

    public static String randomId() {
        final StringBuilder sb = new StringBuilder("osv:");
        final Random rand = new Random();
        for (int i = 0; i < 20; i++) {
            sb.append((char) rand.nextInt(26) + 'a');
        }
        return sb.toString();
    }

    public static ITag<Item> getOrCreateItemTag(String id) {
        if (id.startsWith("#")) {
            id = id.substring(1);
        }
        return getOrCreateItemTag(new ResourceLocation(id));
    }

    public static ITag<Item> getOrCreateItemTag(ResourceLocation id) {
        return nullable(ItemTags.getCollection().get(id))
            .orElse(ItemTags.createOptional(id));
    }

    public static ITag<Block> getOrCreateBlockTag(String id) {
        if (id.startsWith("#")) {
            id = id.substring(1);
        }
        return getOrCreateBlockTag(new ResourceLocation(id));
    }

    public static ITag<Block> getOrCreateBlockTag(ResourceLocation id) {
        return nullable(BlockTags.getCollection().get(id))
            .orElse(BlockTags.createOptional(id));
    }

    /**
     * Used for retrieving a Biome from either a registry name
     * or unique ID. Returns an Optional<Biome> to ensure that
     * null checks are propagated elsewhere.
     */
    public static Optional<Biome> getBiome(String biomeName) {
        final ResourceLocation location = new ResourceLocation(biomeName);
        return nullable(ForgeRegistries.BIOMES.getValue(location));
    }

    public static Biome[] getBiomes(Biome.Category category) {
        return Stream.of(ForgeRegistries.BIOMES)
            .flatMap(reg -> reg.getValues().stream())
            .filter(b -> b.getCategory().equals(category))
            .toArray(Biome[]::new);
    }

    public static Biome.Category getBiomeType(String name) {
        return Stream.of(Biome.Category.values())
            .filter(c -> c.name().equalsIgnoreCase(name) || c.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseThrow(() -> noBiomeTypeNamed(name));
    }

    /** Shorthand for creating a new ResourceLocation with OSV as the namespace. */
    public static ResourceLocation osvLocation(String name) {
        return new ResourceLocation(Main.MOD_ID, name);
    }

    /** Reads a parsed variant string (k=v) to determine if it is valid for this block. */
    public static boolean stateHasVariant(BlockState state, String k, String v) {
        for (Map.Entry<Property<?>, ?> value : state.getValues().entrySet()) {
            if (k.equals(value.getKey().getName())) {
                return v.equals(value.getValue().toString());
            }
        }
        return false;
    }

    public static BlockState getGuaranteedState(String fullName) {
        return getBlockState(fullName).orElseThrow(() -> runExF("There is no state called {}",  fullName));
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

    public static ResourceLocation createName(OreProperties props, Block block) {
        final ResourceLocation registry = nullable(block.getRegistryName())
            .orElseThrow(() -> runExF("Block not registered in time: {}.", block));
        return createName(props, registry);
    }

    public static ResourceLocation createName(OreProperties props, ResourceLocation block) {
        final String bgFormat = formatId(block);
        final String fgFormat = props.name;

        final StringBuilder sb = new StringBuilder(fgFormat);
        if (bgFormat.length() > 0) {
            sb.append('_');
            sb.append(bgFormat);
        }
        return osvLocation(sb.toString());
    }

    /** Produces a formatted identifier from `state`'s registry name. */
    public static String formatBlock(Block block) {
        final ResourceLocation registry = nullable(block.getRegistryName())
            .orElseThrow(() -> runExF("Block not registered in time: {}.", block));
        return formatId(registry);
    }

    private static String formatId(ResourceLocation registry) {
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

    private static RuntimeException noBiomeTypeNamed(String name) {
        return runExF("{} is not valid. The following are valid categories: {}", name,
            Arrays.toString(Biome.Category.values()));
    }
}