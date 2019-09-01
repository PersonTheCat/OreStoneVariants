package com.personthecat.orestonevariants.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

/**
 * Contains all of the necessary procedures for handling a Forge-friendly configuration
 * based on Hjson, a human-friendly variant of JSON. The sole utility of this class for
 * Ore Stone Variants is that string arrays can be stored without any commas or quotes,
 * unlike the TOML format used by Forge. This should theoretically improve the config
 * file's usability for the end user by being easier to read and write, specifically the
 * block registry.
 */
public class HjsonFileConfig implements CommentedFileConfig  {
    /** The main file which stores all data represented by this config. */
    private final File file;
    /** The primary collection of values. */
    private final Map<String, Object> map;
    /** Comments are stored separately to be consistent with CommentedFileConfig's spec. */
    private final Map<String, String> comments;
    /** Data about this config's state for concurrency. */
    private boolean writing, closed;

    /** Constructs a new instance solely from the path to this config file. */
    public HjsonFileConfig(String path) {
        this(new File(path));
    }

    /** Constructs a new instance from the object representing this config file. */
    public HjsonFileConfig(File file) {
        this(file, loadContainer(file));
    }

    /** Constructs a new instance with data that have been previously loaded. */
    private HjsonFileConfig(File file, Container container) {
        this.file = file;
        this.map = container.map;
        this.comments = container.comments;
    }

    /** Converts all of this config's data into an Hjson object. */
    private JsonObject toHjson() {
        JsonObject json = new JsonObject();
        for (String key : map.keySet()) {
            final JsonValue value = toHjson(map.get(key));
            safeGet(comments, key).ifPresent(value::setComment);
            json.set(key, value);
        }
        return json;
    }

    /** Converts a raw value into an Hjson value. */
    private static JsonValue toHjson(Object o) {
        return o instanceof HjsonFileConfig
            ? ((HjsonFileConfig) o).toHjson()
            : JsonValue.valueOf(o);
    }

    /** Reads the json from the disk and parses its data into an electronwill-friendly format. */
    private static Container loadContainer(File file) {
        return getContainer(file, readJson(file).orElse(new JsonObject()));
    }

    /** Converts the input Hjson data into an electronwill-friendly format. */
    private static Container getContainer(File file, JsonObject json) {
        final Container container = new Container();
        for (JsonObject.Member member : json) {
            put(file, container, member.getName(), member.getValue());
        }
        return container;
    }

    /** Puts the JsonObject's raw value and comments into the container. */
    private static void put(File file, Container container, String key, JsonValue value) {
        container.map.put(key, toRaw(file, value));
        container.comments.put(key, value.getBOLComment());
    }

    /** Converts an Hjson value into its raw counterpart. */
    private static Object toRaw(File file, JsonValue value) {
        return value.isObject()
            ? toConfig(file, value.asObject())
            : value.asRaw();
    }

    /** Converts the input Hjson object into a configuration. */
    private static HjsonFileConfig toConfig(File file, JsonObject object) {
        return new HjsonFileConfig(file, getContainer(file, object));
    }

    /** Returns the second to last object in the path, asserting that it is a configuration. */
    private HjsonFileConfig getLastConfig(List<String> path) {
        Object val = this;
        for (int i = 0; i < path.size() - 1; i++) {
            val = ((HjsonFileConfig) val).map.get(path.get(i));
        }
        return (HjsonFileConfig) val;
    }

    /** Returns the last element of the input array. */
    private static String endOfPath(List<String> path) {
        return path.get(path.size() - 1);
    }

    @Override
    public String setComment(List<String> path, String comment) {
        return getLastConfig(path).comments.put(endOfPath(path), comment);
    }

    @Override
    public String removeComment(List<String> path) {
        return getLastConfig(path).comments.remove(endOfPath(path));
    }

    @Override
    public void clearComments() {
        comments.clear();
        for (Object o : map.values()) {
            if (o instanceof HjsonFileConfig) {
                ((HjsonFileConfig) o).clearComments();
            }
        }
    }

    @Override
    public String getComment(List<String> path) {
        return getLastConfig(path).comments.get(endOfPath(path));
    }

    @Override
    public boolean containsComment(List<String> path) {
        return getLastConfig(path).comments.containsKey(endOfPath(path));
    }

    @Override
    public Map<String, String> commentMap() {
        return comments;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T set(List<String> path, Object value) {
        return (T) getLastConfig(path).map.put(endOfPath(path), value);
    }

    @Override
    public boolean add(List<String> path, Object value) {
        return add(getLastConfig(path), endOfPath(path), value);
    }

    /** Adds a value directly to the config, if it does not already exist. */
    private static boolean add(HjsonFileConfig config, String key, Object value) {
        if (!config.map.containsKey(key)) {
            config.map.put(key, value);
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T remove(List<String> path) {
        return (T) getLastConfig(path).map.remove(endOfPath(path));
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getRaw(List<String> path) {
        return (T) getLastConfig(path).map.get(endOfPath(path));
    }

    @Override
    public boolean contains(List<String> path) {
        HjsonFileConfig config = this;
        final int lastIndex = path.size() - 1;
        for (int i = 0; i < lastIndex; i++) {
            final String s = path.get(i);
            if (!config.map.containsKey(s)) {
                return false;
            }
            config = (HjsonFileConfig) config.map.get(s);
        }
        return config.map.containsKey(path.get(lastIndex));
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Map<String, Object> valueMap() {
        return map;
    }

    @Override
    public Set<? extends CommentedConfig.Entry> entrySet() {
        return null;
    }

    /** No proper implementation. Doing so would require me to write an entire library. */
    @Override public ConfigFormat<CommentedFileConfig> configFormat() {
        return null;
    }

    @Override
    public CommentedConfig createSubConfig() {
        return new HjsonFileConfig(file.getPath());
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public Path getNioPath() {
        return file.toPath();
    }

    @Override
    public synchronized void save() {
        if (closed) {
            throw new IllegalStateException("Cannot save a closed file config.");
        }
        writing = true;
        writeJson(toHjson(), file).handle(e -> {
            info("handling an error...");
            throw runExF("Error writing config file: {}", e.getMessage());
        });
        writing = false;
    }

    @Override
    public void load() {
        if (!writing) {
            synchronized (this) {
                if (closed) {
                    throw new IllegalStateException("Cannot (re)load a closed FileConfig");
                }
                map.clear();
                comments.clear();
                final Container container = loadContainer(file);
                map.putAll(container.map);
                comments.putAll(container.comments);
            }
        }
    }

    @Override
    public void close() {
        closed = true;
    }

    /** A DTO holding both the value map and comments. */
    private static class Container {
        final Map<String, Object> map;
        final Map<String, String> comments;

        Container(Map<String, Object> map, Map<String, String> comments) {
            this.map = map;
            this.comments = comments;
        }

        Container() {
            this(new HashMap<>(), new HashMap<>());
        }
    }
}