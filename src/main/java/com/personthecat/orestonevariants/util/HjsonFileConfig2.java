package com.personthecat.orestonevariants.util;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.readJson;
import static com.personthecat.orestonevariants.util.HjsonTools.writeJson;

public class HjsonFileConfig2 implements CommentedFileConfig  {
    private final File file;
    private final Map<String, Object> map;
    private final Map<String, String> comments;
    private boolean writing, closed;

    public HjsonFileConfig2(String path) {
        this(new File(path));
    }

    public HjsonFileConfig2(File file) {
        this(file, getContainer(file, readJson(file).orElse(new JsonObject())));
    }

    private HjsonFileConfig2(File file, Container container) {
        this.file = file;
        this.map = container.map;
        this.comments = container.comments;
    }

    private JsonObject toHjson() {
        JsonObject json = new JsonObject();
        for (String key : map.keySet()) {
            final Object value = map.get(key);
            final JsonValue jsonValue = value instanceof HjsonFileConfig2 ?
                ((HjsonFileConfig2) value).toHjson() :
                JsonValue.valueOf(value);
            safeGet(comments, key).ifPresent(jsonValue::setComment);
            json.set(key, jsonValue);
        }
        return json;
    }

    private static Container getContainer(File file, JsonObject json) {
        final Container container = new Container();
        for (JsonObject.Member member : json) {
            final String name = member.getName();
            final JsonValue value = member.getValue();
            if (value.isObject()) {
                container.map.put(name, new HjsonFileConfig2(file, getContainer(file, value.asObject())));
            } else {
                container.map.put(name, value.asRaw());
                container.comments.put(name, value.getBOLComment());
            }
        }
        return container;
    }

    private HjsonFileConfig2 getLastConfig(List<String> path) {
        Object val = this;
        for (int i = 0; i < path.size() - 1; i++) {
            val = ((HjsonFileConfig2) val).map.get(path.get(i));
        }
        return (HjsonFileConfig2) val;
    }

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
            if (o instanceof HjsonFileConfig2) {
                ((HjsonFileConfig2) o).clearComments();
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
        final HjsonFileConfig2 config = getLastConfig(path);
        final String endOfPath = endOfPath(path);
        if (!config.map.containsKey(endOfPath)) {
            config.map.put(endOfPath, value);
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
        HjsonFileConfig2 config = this;
        final int lastIndex = path.size() - 1;
        for (int i = 0; i < lastIndex; i++) {
            final String s = path.get(i);
            if (!config.map.containsKey(s)) {
                return false;
            }
            config = (HjsonFileConfig2) config.map.get(s);
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

    @Override
    public ConfigFormat<?> configFormat() {
        return null;
    }

    @Override
    public CommentedConfig createSubConfig() {
        return new HjsonFileConfig2(file.getPath());
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
    public void save() {
        if (closed) {
            throw new IllegalStateException("Cannot save a closed file config.");
        }
        writing = true;
        writeJson(toHjson(), file).handleIfPresent(e -> {
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
                final JsonObject json = readJson(file).orElse(new JsonObject());
                final Container container = getContainer(file, json);
                map.putAll(container.map);
                comments.putAll(container.comments);
            }
        }
    }

    @Override
    public void close() {
        closed = true;
    }

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