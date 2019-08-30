package com.personthecat.orestonevariants.util;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import org.hjson.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

public class HjsonFileConfig implements CommentedFileConfig {
    private final File file;
    private final Writer tw;
    private JsonObject config;
    private boolean writing, closed;

    public HjsonFileConfig(Path path) {
        this.file = path.toFile();
        this.config = readJson(file).orElse(new JsonObject());
        this.tw = getWriter(file);
    }

    public HjsonFileConfig(String path) {
        this(Paths.get(path));
    }

    private static Writer getWriter(File file) {
        try {
            return new FileWriter(file);
        } catch (IOException e) {
            throw runExF("Error creating file writer: {}", e.getMessage());
        }
    }

    private JsonObject getLastObject(List<String> path) {
        JsonValue val = config.get(path.get(0));
        final int lastIndex = path.size() - 1;
        for (int i = 1; i < lastIndex; i++) {
            val = val.asObject().get(path.get(i));
        }
        return val.asObject();
    }

    private static String endOfPath(List<String> path) {
        return path.get(path.size() - 1);
    }

    private JsonValue getValue(List<String> path) {
        return getLastObject(path).get(endOfPath(path));
    }

    private static Map<String, Object> getMap(JsonObject object) {
        final Map<String, Object> map = new HashMap<>();
        for (JsonObject.Member member : object) {
            map.put(member.getName(), member.getValue().asRaw());
        }
        return map;
    }

    @Override
    public String setComment(List<String> path, String comment) {
        final JsonValue value = getValue(path);
        final String old = value.getBOLComment();
        value.setComment(comment);
        return old;
    }

    @Override
    public String removeComment(List<String> path) {
        final JsonValue value = getValue(path);
        final String old = value.getBOLComment();
        value.setComment("");
        return old;
    }

    @Override
    public void clearComments() {
        clearComments(config);
    }

    private static void clearComments(JsonObject object) {
        for (JsonObject.Member member : object) {
            if (member.getValue().isObject()) {
                clearComments(member.getValue().asObject());
            } else {
                member.getValue().setComment("");
            }
        }
    }

    @Override
    public String getComment(List<String> path) {
        return getValue(path).getBOLComment();
    }

    @Override
    public boolean containsComment(List<String> path) {
        return getValue(path).hasComments();
    }

    @Override
    public Map<String, String> commentMap() {
        return null;
    }

    @Override
    public <T> T set(List<String> path, Object value) {
        final JsonObject lastObject = getLastObject(path);
        final String endOfPath = endOfPath(path);
        final JsonValue original = lastObject.get(endOfPath);
        lastObject.set(endOfPath, JsonValue.valueOf(value));
        return original.asRaw();
    }

    @Override
    public boolean add(List<String> path, Object value) {
        JsonObject object = config;
        for (int i = 0; i < path.size() - 1; i++) {
            final String s = path.get(i);
            if (!object.has(s)) {
                object.add(s, new JsonObject());
            }
            object = object.get(s).asObject();
        }
        final String endOfPath = endOfPath(path);
        if (!object.has(endOfPath)) {
            object.add(endOfPath, JsonValue.valueOf(value));
            return true;
        }
        return false;
    }

    @Override
    public <T> T remove(List<String> path) {
        final JsonObject lastObject = getLastObject(path);
        final String endOfPath = endOfPath(path);
        final JsonValue original = lastObject.get(endOfPath);
        lastObject.remove(endOfPath);
        return original.asRaw();
    }

    @Override
    public void clear() {
        for (JsonObject.Member member : config) {
            config.remove(member.getName());
        }
    }

    @Override
    public <T> T getRaw(List<String> path) {
        return getValue(path).asRaw();
    }

    @Override
    public boolean contains(List<String> path) {
        return getValue(path) != null;
    }

    @Override
    public int size() {
        return config.size();
    }

    @Override
    public Map<String, Object> valueMap() {
        return null;
    }

    @Override
    public Set<? extends CommentedConfig.Entry> entrySet() {
        return null;
    }

    @Override
    public ConfigFormat<?> configFormat() {
        return null;
    }

    @Override
    public CommentedConfig createSubConfig() {
        return null;
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
        if (writing) {
            throw new IllegalStateException("Cannot save a closed file config.");
        }
        writing = true;
        writeJson(config, file).handleIfPresent(e -> {
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
                config = readJson(file).orElse(new JsonObject());
            }
        }
    }

    @Override
    public void close() {
        closed = true;
    }
}