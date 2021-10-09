package personthecat.osv.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.Value;
import net.minecraft.resources.ResourceLocation;
import personthecat.catlib.data.MultiValueHashMap;
import personthecat.catlib.data.MultiValueMap;

import java.util.*;
import java.util.function.Function;

import static personthecat.catlib.util.Shorthand.f;

@Value
public class Group {

    public static final String DEFAULT = "default";
    public static final String ALL = "all";

    String name;
    Set<String> entries;

    public Group(final String name, final String... entries) {
        this.name = name;
        this.entries = ImmutableSet.copyOf(entries);
    }

    public Group(final String name, final Collection<String> entries) {
        this.name = name;
        this.entries = ImmutableSet.copyOf(entries);
    }

    public Group(final String name, final String entry) {
        this.name = name;
        this.entries = Collections.singleton(entry);
    }

    public int size() {
        return this.entries.size();
    }

    public Collection<String> filenames() {
        if ("minecraft".equals(this.name)) {
            return this.map(e -> f("{}/{}.hjson", this.name, e));
        }
        return this.map(e -> f("{}/{}_{}.hjson", this.name, this.name, e));
    }

    public Collection<ResourceLocation> ids() {
        return this.map(ResourceLocation::new);
    }

    public Collection<String> formatted() {
        if ("minecraft".equals(this.name)) {
            return this.entries;
        }
        return this.map(e -> f("{}_{}", this.name, e));
    }

    @SuppressWarnings("UnstableApiUsage")
    public <T> List<T> map(final Function<String, T> mapper) {
        return this.entries.stream().map(mapper).collect(ImmutableList.toImmutableList());
    }

    public static MultiValueMap<String, String> toFormattedMap(final Group... groups) {
        final MultiValueMap<String, String> map = new MultiValueHashMap<>();
        for (final Group group : groups) {
            map.put(group.name, new ArrayList<>(group.formatted()));
        }
        return map;
    }

    public static PartialGroup named(final String name) {
        return new PartialGroup(name);
    }

    public static Group collect(final Group... groups) {
        return collect("", groups);
    }

    public static Group collect(final String name, final Group... groups) {
        final Set<String> entries = new HashSet<>();
        for (final Group group : groups) {
            entries.addAll(group.entries);
        }
        return new Group(name, entries);
    }

    public static Group collect(final String name, final Iterable<Group> groups) {
        final Set<String> entries = new HashSet<>();
        for (final Group group : groups) {
            entries.addAll(group.entries);
        }
        return new Group(name, entries);
    }

    public static class PartialGroup {
        private final String name;

        private PartialGroup(final String name) {
            this.name = name;
        }

        public final Group withEntries(final String... entries) {
            return new Group(this.name, entries);
        }

        public final Group withEntries(final Collection<String> entries) {
            return new Group(this.name, entries);
        }
    }
}
