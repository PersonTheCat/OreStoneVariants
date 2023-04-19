package personthecat.osv.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.Value;
import net.minecraft.resources.ResourceLocation;
import personthecat.catlib.data.collections.MultiValueHashMap;
import personthecat.catlib.data.collections.MultiValueMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static personthecat.catlib.util.Shorthand.f;

@Value
public class Group {

    public static final String DEFAULT = "default";
    public static final String ALL = "all";

    String name;
    Set<String> entries;
    boolean implicitNamespace;
    boolean synthetic;

    public Group(final String name, final String... entries) {
        this(name, ImmutableSet.copyOf(entries), false, false);
    }

    public Group(final String name, final Collection<String> entries) {
        this(name, ImmutableSet.copyOf(entries), false, false);
    }

    public Group(final String name, final String entry) {
        this(name, Collections.singleton(entry), false, false);
    }

    private Group(final String name, final Set<String> entries, final boolean implicitNamespace, final boolean synthetic) {
        this.name = name;
        this.entries = entries;
        this.implicitNamespace = implicitNamespace;
        this.synthetic = synthetic;
    }

    public int size() {
        return this.entries.size();
    }

    public boolean isMetaGroup() {
        return this.name.equals(ALL) || this.name.equals(DEFAULT);
    }

    public ResourceLocation asId() {
        return new ResourceLocation(this.name);
    }

    public Collection<String> filenames() {
        if ("minecraft".equals(this.name)) {
            return this.map(e -> f("{}/{}.xjs", this.name, e));
        }
        return this.map(e -> f("{}/{}_{}.xjs", this.name, this.name, e));
    }

    public Collection<ResourceLocation> ids() {
        if (this.implicitNamespace) {
            return this.map(value -> new ResourceLocation(this.name, value));
        }
        return this.map(ResourceLocation::new);
    }

    public Collection<String> formatted() {
        if ("minecraft".equals(this.name)) {
            return this.entries;
        }
        return this.map(e -> f("{}_{}", this.name, e));
    }

    public <T> List<T> map(final Function<String, T> mapper) {
        return this.entries.stream().map(mapper).collect(ImmutableList.toImmutableList());
    }

    public Group implicitNamespace() {
        return new Group(this.name, this.entries, true, this.synthetic);
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof Group) {
            return this.name.equals(((Group) o).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return this.name + Arrays.toString(this.entries.toArray());
    }

    public static MultiValueMap<String, String> toIdMap(final Group... groups) {
        final MultiValueMap<String, String> map = new MultiValueHashMap<>();
        for (final Group group : groups) {
            map.put(group.name, group.ids().stream().map(ResourceLocation::toString).collect(Collectors.toList()));
        }
        return map;
    }

    public static PartialGroup named(final String name) {
        return new PartialGroup(name);
    }

    public static Group synthetic(final String entry) {
        return new Group(entry, Collections.singleton(entry), false, true);
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

        public final Group withNamespacedEntries(final String... entries) {
            return new Group(this.name, Stream.of(entries).map(e -> this.name + ":" + e).collect(Collectors.toSet()));
        }

        public final Group withEntries(final Collection<String> entries) {
            return new Group(this.name, entries);
        }
    }
}
