package com.personthecat.orestonevariants.util;

import com.google.gson.Gson;
import com.personthecat.orestonevariants.util.unsafe.ReflectionTools;
import com.personthecat.orestonevariants.util.unsafe.Result;
import com.personthecat.orestonevariants.util.unsafe.Void;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.ForgeHooks;
import org.hjson.*;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

import static com.personthecat.orestonevariants.io.SafeFileIO.getResource;
import static com.personthecat.orestonevariants.util.CommonMethods.*;

/**
 * A set of tools for interacting with Hjson objects unique to this name.
 * See github.com/PersonTheCat/hjson-java to find the specific build of
 * hjson used for this mod.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class HjsonTools {

    /** The settings to be used when outputting JsonObjects to the disk. */
    private static final HjsonOptions FORMATTER = new HjsonOptions()
        .setAllowCondense(true)
        .setAllowMultiVal(true)
        .setCommentSpace(1)
        .setSpace(2)
        .setBracesSameLine(true)
        .setOutputComments(true);

    public static Optional<JsonObject> readJson(File file) {
        return Result.of(() -> JsonObject.readHjson(new FileReader(file), FORMATTER).asObject())
            .get(Result::IGNORE);
    }

    public static Optional<JsonObject> readJson(InputStream is) {
        return Result.of(() -> JsonObject.readHjson(new InputStreamReader(is), FORMATTER).asObject())
            .get(Result::IGNORE);
    }

    /** Writes the JsonObject to the disk. */
    public static Result<Void, IOException> writeJson(JsonObject json, File file) {
        return Result.with(() -> new FileWriter(file))
            .of(tw -> {
                if (extension(file).equals("json")) { // Write as json.
                    json.writeTo(tw, Stringify.FORMATTED);
                } else { // Write as hjson.
                    json.writeTo(tw, FORMATTER);
                }
            });
    }

    /** Variant of setOrAdd() used for boolean values. */
    public static JsonObject setOrAdd(JsonObject json, String field, boolean value) {
        return setOrAdd(json, field, JsonValue.valueOf(value));
    }

    /** Variant of setOrAdd() used for integer values. */
    public static JsonObject setOrAdd(JsonObject json, String field, int value) {
        return setOrAdd(json, field, JsonValue.valueOf(value));
    }

    /** Variant of setOrAdd() used for floating point values. */
    public static JsonObject setOrAdd(JsonObject json, String field, float value) {
        return setOrAdd(json, field, JsonValue.valueOf(value));
    }

    /** Variant of setOrAdd() used for string values. */
    public static JsonObject setOrAdd(JsonObject json, String field, String value) {
        return setOrAdd(json, field, JsonValue.valueOf(value));
    }

    /** Modifies or adds a field with the input value. Avoids duplicate fields. */
    public static JsonObject setOrAdd(JsonObject json, String field, JsonValue value) {
        if (json.get(field) != null) {
            return json.set(field, value);
        } else {
            return json.add(field, value);
        }
    }

    /** Safely retrieves a boolean from the input object. */
    public static Optional<Boolean> getBool(JsonObject json, String field) {
        return Optional.ofNullable(json.get(field))
            .map(JsonValue::asBoolean);
    }

    /** Shorthand for getBool(json, field).ifPresent(ifPresent). */
    public static void getBool(JsonObject json, String field, Consumer<Boolean> ifPresent) {
        JsonValue value = json.get(field);
        if (value != null) {
            ifPresent.accept(value.asBoolean());
        }
    }

    /** Retrieves a boolean from the input object. Returns `or` if nothing is found. */
    public static boolean getBoolOr(JsonObject json, String field, boolean orElse) {
        return getBool(json, field).orElse(orElse);
    }

    /** Safely retrieves an integer from the input json. */
    public static Optional<Integer> getInt(JsonObject json, String field) {
        return Optional.ofNullable(json.get(field))
            .map(JsonValue::asInt);
    }

    /** Shorthand for getInt(). */
    public static void getInt(JsonObject json, String field, Consumer<Integer> ifPresent) {
        JsonValue value = json.get(field);
        if (value != null) {
            ifPresent.accept(value.asInt());
        }
    }

    /** Retrieves an integer from the input object. Returns `or` if nothing is found. */
    public static int getIntOr(JsonObject json, String field, int orElse) {
        return getInt(json, field).orElse(orElse);
    }

    /** Retrieves a range of integers from the input object. */
    public static Optional<Range> getRange(JsonObject json, String field) {
        return getValue(json, field)
            .map(HjsonTools::asOrToArray)
            .map(HjsonTools::toIntArray)
            .map(CommonMethods::sort)
            .map(a -> a.length == 1 ? new Range(a[0]) : new Range(a[0], a[a.length - 1]));
    }

    /** Retrieves a range of integers from the input object. Returns `orElse` if nothing is found. */
    public static Range getRangeOr(JsonObject json, String field, Range orElse) {
        return getRange(json, field).orElse(orElse);
    }

    /** Safely retrieves a boolean from the input json. */
    public static Optional<Float> getFloat(JsonObject json, String field) {
        return Optional.ofNullable(json.get(field))
            .map(JsonValue::asFloat);
    }

    /** Shorthand for getFloat(). */
    public static void getFloat(JsonObject json, String field, Consumer<Float> ifPresent) {
        JsonValue value = json.get(field);
        if (value != null) {
            ifPresent.accept(value.asFloat());
        }
    }

    /** Retrieves a float from the input object. Returns `or` if nothing is found. */
    public static float getFloatOr(JsonObject json, String field, float orElse) {
        return getFloat(json, field).orElse(orElse);
    }

    /** Safely retrieves a string from the input json. */
    public static Optional<String> getString(JsonObject json, String field) {
        return Optional.ofNullable(json.get(field))
            .map(JsonValue::asString);
    }

    /** Shorthand for getString(). */
    public static void getString(JsonObject json, String field, Consumer<String> ifPresent) {
        JsonValue value = json.get(field);
        if (value != null) {
            ifPresent.accept(value.asString());
        }
    }

    /** Retrieves a string from the input object. Returns `or` if nothing is found. */
    public static String getStringOr(JsonObject json, String field, String orElse) {
        return getString(json, field).orElse(orElse);
    }

    /** Safely retrieves a JsonArray from the input json. */
    public static Optional<JsonArray> getArray(JsonObject json, String field) {
        return Optional.ofNullable(json.get(field))
            .map(HjsonTools::asOrToArray);
    }

    /** Retrieves an array from the input object. Returns `or` if nothing is found. */
    public static JsonArray getArrayOr(JsonObject json, String field, JsonArray orElse) {
        return getArray(json, field).orElse(orElse);
    }

    /** Retrieves an object from the input object. Returns an empty array, if nothing is found. */
    public static JsonArray getArrayOrNew(JsonObject json, String field) {
        return getArray(json, field).orElse(new JsonArray());
    }

    /** Casts or converts a JsonValue to a JsonArray.*/
    private static JsonArray asOrToArray(JsonValue value) {
        return value.isArray() ? value.asArray() : new JsonArray().add(value);
    }

    /** Shorthand for getArray().*/
    public static void getArray(JsonObject json, String field, Consumer<JsonArray> ifPresent) {
        JsonValue value = json.get(field);
        if (value != null) {
            ifPresent.accept(value.asArray());
        }
    }

    /** Safely retrieves a boolean from the input json. */
    public static Optional<JsonObject> getObject(JsonObject json, String field) {
        return Optional.ofNullable(json.get(field))
            .map(JsonValue::asObject);
    }

    /** Retrieves an object from the input object. Returns `or` if nothing is found. */
    public static JsonObject getObjectOr(JsonObject json, String field, JsonObject orElse) {
        return getObject(json, field).orElse(orElse);
    }

    /** Retrieves an object from the input object. Returns an empty object, if nothing is found. */
    public static JsonObject getObjectOrNew(JsonObject json, String field) {
        return getObject(json, field).orElse(new JsonObject());
    }

    /** Shorthand for getObject(). */
    public static void getObject(JsonObject json, String field, Consumer<JsonObject> ifPresent) {
        JsonValue value = json.get(field);
        if (value != null) {
            ifPresent.accept(value.asObject());
        }
    }

    /** Safely retrieves a JsonValue from the input object. */
    public static Optional<JsonValue> getValue(JsonObject json, String field) {
        return Optional.ofNullable(json.get(field));
    }

    public static void getValue(JsonObject json, String field, Consumer<JsonValue> ifPresent) {
        getValue(json, field).ifPresent(ifPresent);
    }

    /**
     * Safely retrieves an array of JsonObjects from the input json.
     * To-do: Be more consistent and use Optional, instead.
     */
    public static List<JsonObject> getObjectArray(JsonObject json, String field) {
        List<JsonObject> array = new ArrayList<>();
        getValue(json, field).map(HjsonTools::asOrToArray)
            .ifPresent(a -> a.forEach(e -> {
                // This is assumed to be an object. If it isn't,
                // The user should be informed (i.e. crash).
                array.add(e.asObject());
            }));
        return array;
    }

    /** Safely retrieves an int array from the input json. */
    public static Optional<int[]> getIntArray(JsonObject json, String field) {
        return Optional.ofNullable(json.get(field))
            .map((v) -> toIntArray(v.asArray()));
    }

    /** Shorthand for getIntArray */
    public static void getIntArray(JsonObject json, String field, Consumer<int[]> ifPresent) {
        JsonValue value = json.get(field);
        if (value != null) {
            ifPresent.accept(toIntArray(value.asArray()));
        }
    }

    /** Retrieves an array of integers from the input object. Returns `or` if nothing is found. */
    public static int[] getIntArrayOr(JsonObject json, String field, int[] orElse) {
        return getIntArray(json, field).orElse(orElse);
    }

    public static int[] getIntArrayOrEmpty(JsonObject json, String field) {
        return getIntArrayOr(json, field, new int[0]);
    }

    /** Converts a JsonArray into an array of ints. */
    public static int[] toIntArray(JsonArray array) {
        // Create a List of Integer objects.
        List<Integer> ints = new ArrayList<>();
        // Iterate through the array, adding to the list.
        Iterator<JsonValue> iter = array.iterator();
        while (iter.hasNext()) {
            ints.add(iter.next().asInt());
        }
        // Convert the Integer objects into
        // their primitive counterparts.
        return ints.stream()
            .mapToInt((i) -> i)
            .toArray();
    }

    /** Safely retrieves a List of Strings from the input json. */
    public static Optional<List<String>> getStringArray(JsonObject json, String field) {
        return Optional.ofNullable(json.get(field))
            .map((v) -> toStringArray(asOrToArray(v)));
    }

    /** Retrieves either an array from the input json or an empty list, if absent. */
    public static List<String> getStringArrayOrEmpty(JsonObject json, String field) {
        return getStringArray(json, field).orElse(Collections.emptyList());
    }

    /** Shorthand for getStringArray(). */
    public static void getStringArray(JsonObject json, String field, Consumer<List<String>> ifPresent) {
        JsonValue value = json.get(field);
        if (value != null) {
            ifPresent.accept(toStringArray(asOrToArray(value)));
        }
    }

    /** Converts a JsonArray into a List of Strings. */
    public static List<String> toStringArray(JsonArray array) {
        List<String> strings = new ArrayList<>();
        Iterator<JsonValue> iter = array.iterator();
        while (iter.hasNext()) {
            strings.add(iter.next().asString());
        }
        return strings;
    }

    /**
     * Gets the required "state" field which must exist in many objects.
     * Throws an exception when no block is found with the input name.
     */
    public static IBlockState getGuranteedState(JsonObject json, String requiredFor) {
        String stateName = getString(json, "state")
            .orElseThrow(() -> runExF("Each %s object must contain the field \"state.\"", requiredFor));
        return getBlockState(stateName)
            .orElseThrow(() -> noBlockNamed(stateName));
    }

    /**
     * Gets the required "states" field which must exist in many objects.
     * Throws an exception when any block cannot be found.
     */
    public static IBlockState[] getGuranteedStates(JsonObject json, String requiredFor) {
        JsonArray stateNames = getArray(json, "states")
            .orElseThrow(() -> runExF("Each %s object must contain the field \"states.\"", requiredFor));
        // Handles crashing when no block is found.
        return toBlocks(stateNames);
    }

    /** Retrieves a single BlockState from the input json. */
    public static Optional<IBlockState> getBlock(JsonObject json, String field) {
        return getString(json, field)
            .map(s -> getBlockState(s)
                .orElseThrow(() -> noBlockNamed(s)));
    }

    /**
     * Retrives an BlockState from the input json, returning `orElse`
     * if no object is found.
     */
    public static IBlockState getBlockOr(JsonObject json, String field, IBlockState orElse) {
        return getBlock(json, field).orElse(orElse);
    }

    /** Safely retrieves an array of blocks from the input json. */
    public static Optional<IBlockState[]> getBlocks(JsonObject json, String field) {
        return getArray(json, field).map(HjsonTools::toBlocks);
    }

    /** Shorthand for getBlocks(). */
    public static void getBlocks(JsonObject json, String field, Consumer<IBlockState[]> ifPresent) {
        getBlocks(json, field).ifPresent(ifPresent);
    }

    /** Converts each element in the array into an BlockState. */
    public static IBlockState[] toBlocks(JsonArray array) {
        List<IBlockState> blocks = new ArrayList<>();
        for (String s : toStringArray(array)) {
            IBlockState state = getBlockState(s).orElseThrow(() -> noBlockNamed(s));
            blocks.add(state);
        }
        return blocks.toArray(new IBlockState[0]);
    }

    /**
     * Retrieves an array of BlockStates from the input json, substituting
     * `orElse` if no object is found.
     */
    public static IBlockState[] getBlocksOr(JsonObject json, String field, IBlockState... orElse) {
        return getBlocks(json, field).orElse(orElse);
    }

    /** Safely retrieves an Item from the input object. */
    public static Optional<Item> getItem(JsonObject json, String field) {
        return getString(json, field).map(s -> CommonMethods.getItem(s)
            .orElseThrow(() -> noItemNamed(s)));
    }

    /**
     * Retrieves an Item from the input object, substituting `orElse` if
     * no object is found.
     */
    public static Item getItemOr(JsonObject json, String field, Item orElse) {
        return getItem(json, field).orElse(orElse);
    }

    public static Item getGuaranteedItem(JsonObject json, String field) {
        return getItem(json, field)
            .orElseThrow(() -> runExF("The field {} must be defined.", field));
    }

    public static Optional<ItemStack> getStack(JsonObject json, String field) {
        return getString(json, field).map(s -> CommonMethods.getStack(s)
             .orElseThrow(() -> noItemNamed(s)));
    }

    public static ItemStack getStackOr(JsonObject json, String field, ItemStack orElse) {
        return getStack(json, field).orElse(orElse);
    }

    public static ItemStack getGuaranteedStack(JsonObject json, String field) {
        return getStack(json, field)
            .orElseThrow(() -> runExF("The field {} must be defined.", field));
    }

    /** Safely retrieves a BlockPos from the input object. */
    public static Optional<BlockPos> getPosition(JsonObject json, String field) {
        return getArray(json, field).map(HjsonTools::toPosition);
    }

    /**
     * Retrieves a BlockPos from the input json, returning `orElse`
     * if no object can be found.
     */
    public static BlockPos getPositionOr(JsonObject json, String field, BlockPos orElse) {
        return getPosition(json, field).orElse(orElse);
    }

    /** Safely retrieves an array of type BlockPos from the input json. */
    public static Optional<BlockPos[]> getPositions(JsonObject json, String field) {
        return getArray(json, field).map(a -> {
            List<BlockPos> positions = new ArrayList<>();
            for (JsonValue v : a) {
                positions.add(toPosition(v.asArray()));
            }
            return toArray(positions, BlockPos.class);
        });
    }

    /** Shorthand for getPositions(). */
    public static void getPositions(JsonObject json, String field, Consumer<BlockPos[]> ifPresent) {
        getPositions(json, field).ifPresent(ifPresent);
    }

    /**
     * Retrieves an array of type `BlockPos` from the input object,
     * returning `orElse` if no object is found.
     */
    public static BlockPos[] getPositionsOr(JsonObject json, String field, BlockPos... orElse) {
        return getPositions(json, field).orElse(orElse);
    }

    /** Converts the input JsonArray into a BlockPos object. */
    public static BlockPos toPosition(JsonArray coordinates) {
        // Expect exactly 3 elements.
        if (coordinates.size() != 3) {
            throw runEx("Relative coordinates must be specified in an array of 3 elements, e.g. [0, 0, 0].");
        }
        // Convert the array into a BlockPos object.
        return new BlockPos(
            coordinates.get(0).asInt(),
            coordinates.get(1).asInt(),
            coordinates.get(2).asInt()
        );
    }

    /** For the biome object at the top level. */
    public static Biome[] getAllBiomes(JsonObject json) {
        List<Biome> biomes = new ArrayList<>();
        // Get biomes by registry name.
        getStringArray(json, "names").ifPresent((a) -> {
            for (String s : a) {
                Biome biome = getBiome(s).orElseThrow(() -> noBiomeNamed(s));
                biomes.add(biome);
            }
        });
        // Get biomes by type.
        getBiomeTypes(json, "types").ifPresent((a) -> {
            for (BiomeDictionary.Type t : a) {
                Collections.addAll(biomes, getBiomes(t));
            }
        });
        return toArray(biomes, Biome.class);
    }

    /** Safely retrieves a List of BiomeTypes from the input json. */
    public static Optional<List<BiomeDictionary.Type>> getBiomeTypes(JsonObject json, String field) {
        return Optional.ofNullable(json.get(field))
            .map(v -> toBiomeTypes(v.asArray()));
    }

    /** Converts a JsonArray in to a list of BiomeTypes. */
    public static List<BiomeDictionary.Type> toBiomeTypes(JsonArray array) {
        List<BiomeDictionary.Type> types = new ArrayList<>();
        Iterator<JsonValue> iter = array.iterator();
        while (iter.hasNext()) {
            types.add(getBiomeType(iter.next().asString()));
        }
        return types;
    }

    /** Safely retrieves a material from the input json. */
    public static Optional<Material> getMaterial(JsonObject json, String field) {
        return getString(json, field).flatMap(ValueLookup::getMaterial);
    }

    /**
     * Retrieves a material from the input json, returning `orElse`
     * if no object can be found.
     */
    public static Material getMaterialOr(JsonObject json, String field, Material orElse) {
        return getMaterial(json, field).orElse(orElse);
    }

    public static Optional<MapColor> getMapColor(JsonObject json, String field) {
        return getString(json, field).flatMap(ValueLookup::getMapColor);
    }

    public static MapColor getMapColorOr(JsonObject json, String field, MapColor orElse) {
        return getMapColor(json, field).orElse(orElse);
    }

    /** Retrieves a resource location from the input json. */
    public static Optional<ResourceLocation> getLocation(JsonObject json, String field) {
        return getString(json, field).map(ResourceLocation::new);
    }

    /**
     * Retrieves a resource location from the input json, returning
     * `orElse` if no value is found.
     */
    public static ResourceLocation getLocationOr(JsonObject json, String field, ResourceLocation orElse) {
        return getLocation(json, field).orElse(orElse);
    }

    /** Retrieves a sound type from the input json. */
    public static Optional<SoundType> getSoundType(JsonObject json, String field) {
        return getString(json, field).flatMap(ValueLookup::getSoundType);
    }

    /**
     * Retrieves a sound type from the input json, returning
     * `orElse` if no value is found.
     */
    public static SoundType getSoundTypeOr(JsonObject json, String field, SoundType orElse) {
        return getSoundType(json, field).orElse(orElse);
    }

//    /**
//     * Attempts to retrieve a loot table from the specified field.
//     * Accepts either a resource location or a raw loot table object.
//     */
//    public static Optional<LootTable> getLootTable(JsonObject json, String field) {
//        final Optional<JsonValue> value = getValue(json, field);
//        if (!value.isPresent()) {
//            return empty();
//        }
//        final ResourceLocation location;
//        final com.google.gson.JsonObject gson;
//        if (value.get().isString()) {
//            final String name = value.get().asString();
//            location = new ResourceLocation(name);
//            gson = gsonFromLocation(location, name)
//                .orElseThrow(() -> runExF("\"{}\" points to an invalid Json object (syntax error).", name));
//        } else if (value.get().isObject()) {
//            final JsonObject object = value.get().asObject();
//            location = osvLocation("dynamic_loot/");
//            gson = parseGson(new StringReader(object.toString(Stringify.PLAIN)))
//                .orElseThrow(() -> runExF("The object named \"{}\" is an invalid loot table."));
//        } else {
//            return empty();
//        }
//        final String data = gson.toString();
//        return full(ForgeHooks.loadLootTable(LOOT_TABLE_CTX, location, data, true, new LootTableManager(null)));
//    }

    /** Parses a Gson json object from a ResourceLocation. */
    private static Optional<com.google.gson.JsonObject> gsonFromLocation(ResourceLocation location, String name) {
        final String path = f("/assets/{}/loot_tables/{}.json", location.getNamespace(), location.getPath());
        final InputStream stream = getResource(path)
            .orElseThrow(() -> noTableNamed(name));
        return parseGson(new InputStreamReader(stream));
    }

    /** Reads a Gson json object neatly, using Result#get. */
    private static Optional<com.google.gson.JsonObject> parseGson(Reader reader) {
        return Result.of(() -> new com.google.gson.JsonParser().parse(reader).getAsJsonObject()).get(Result::WARN);
    }

    /** Informs the user that they have entered an invalid biome name. */
    public static RuntimeException noBiomeNamed(String name) {
        return runExF("There is no biome named \"{}.\"", name);
    }

    /** Informs the user that they have entered an invalid dimension name. */
    public static RuntimeException noDimensionNamed(String name) {
        return runExF("There is no dimension named \"{}.\"", name);
    }

    /** Informs the user that they have entered an invalid block name. */
    public static RuntimeException noBlockNamed(String name) {
        return runExF("There is no block named \"{}.\"", name);
    }

    /** Informs the user that they have entered an invalid item name. */
    public static RuntimeException noItemNamed(String name) {
        return runExF("There is no item named \"{}.\"", name);
    }

    /** Informs the user that they have entered an invalid setMaterial name. */
    public static RuntimeException noMaterialNamed(String name) {
        return runExF("There is no (supported) setMaterial named \"{}.\"", name);
    }

    /** Informs the user that they have entered an invalid loot table location. */
    public static RuntimeException noTableNamed(String name) {
        return runExF("There is no loot table located at \"{}.\"", name);
    }
}