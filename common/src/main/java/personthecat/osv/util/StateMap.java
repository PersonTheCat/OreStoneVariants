package personthecat.osv.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.exception.JsonFormatException;
import personthecat.fresult.Result;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class StateMap<T> {

    private static final StateMap<Object> EMPTY = new StateMap<>(Collections.emptyMap());

    private final Map<String, T> map;

    public StateMap() {
        this(new HashMap<>());
    }

    public StateMap(final Map<String, T> map) {
        this.map = map;
    }

    public static <T> StateMap<T> all(final T value) {
        return new StateMap<>(Collections.singletonMap("", value));
    }

    public static <T> StateMap<T> singleton(final String key, final T value) {
        return new StateMap<>(Collections.singletonMap(key, value));
    }

    public static <T> StateMap<List<T>> singletonList(final String key, final T value) {
        return new StateMap<>(Collections.singletonMap(key, Collections.singletonList(value)));
    }

    @SuppressWarnings("unchecked")
    public static <T> StateMap<T> empty() {
        return (StateMap<T>) EMPTY;
    }

    public void put(final String variant, final T t) {
        this.map.put(variant, t);
    }

    public T get(final String variant) {
        final T get = this.map.get(variant);
        return get != null ? get : this.map.get("");
    }

    public int size() {
        return this.map.size();
    }

    public boolean isSimple() {
        return this.size() == 1 && this.get("") != null;
    }

    public Map<String, T> asRaw() {
        return this.map;
    }

    public Set<String> keys() {
        return this.map.keySet();
    }

    public Collection<T> values() {
        return this.map.values();
    }

    public <U> void biConsume(final StateMap<U> other, final BiConsumer<T, U> fn) {
        assert size() == other.size() : "Cannot compare maps with different state keys";
        for (final Map.Entry<String, T> entry : this.map.entrySet()) {
            final U u = Objects.requireNonNull(other.get(entry.getKey()), "expected " + entry.getKey());
            fn.accept(entry.getValue(), u);
        }
    }

    public <U> StateMap<U> mapTo(final Function<T, U> mapper) {
        final StateMap<U> map = new StateMap<>();
        for (final Map.Entry<String, T> entry : this.map.entrySet()) {
            map.put(entry.getKey(), mapper.apply(entry.getValue()));
        }
        return map;
    }

    public <U> StateMap<Pair<T, U>> with(final StateMap<U> other) {
        final StateMap<Pair<T, U>> map = new StateMap<>();
        for (final Map.Entry<String, T> tEntry : this.map.entrySet()) {
            for (final Map.Entry<String, U> uEntry : other.map.entrySet()) {
                final Pair<T, U> pair = Pair.of(tEntry.getValue(), uEntry.getValue());
                map.put(addVariant(tEntry.getKey(), uEntry.getKey()), pair);
            }
        }
        return map;
    }

    public static String addVariant(final String variants, final String kv) {
        if (variants.isEmpty()) {
            return kv;
        } else if (kv.isEmpty()) {
            return variants;
        }
        return variants + "," + kv;
    }

    public StateMap<T> without(final String k, final String v) {
        final Map<String, T> updated = new HashMap<>();
        final Map<String, T> partial = new HashMap<>();

        for (final Map.Entry<String, T> entry : this.map.entrySet()) {
            if (!contains(entry.getKey(), k, v)) {
                updated.put(entry.getKey(), entry.getValue());
            } else if (!containsPartial(entry.getKey(), k)) {
                partial.put(entry.getKey(), entry.getValue());
            }
        }
        return new StateMap<>(addPartial(updated, partial));
    }

    private static <T> Map<String, T> addPartial(final Map<String, T> map, final Map<String, T> partial) {
        for (final Map.Entry<String, T> partialEntry : partial.entrySet()) {
            boolean anyContains = false;
            for (final Map.Entry<String, T> entry : map.entrySet()) {
                anyContains |= contains(entry.getKey(), partialEntry.getKey());
            }
            if (!anyContains) {
                map.put(partialEntry.getKey(), partialEntry.getValue());
            }
        }
        return map;
    }

    public void forEach(final BiConsumer<String, T> fn) {
        this.map.forEach(fn);
    }

    public static <T> void forEachInner(final StateMap<? extends Collection<? extends T>> map, final BiConsumer<String, T> fn) {
        map.forEach((key, ts) -> ts.forEach(t -> fn.accept(key, t)));
    }

    public static <T, U> void forEachPair(final StateMap<Pair<T, U>> map, final TriConsumer<String, T, U> fn) {
        map.forEach((key, pair) -> fn.accept(key, pair.getLeft(), pair.getRight()));
    }

    public Function<BlockState, T> createFunction() {
        return new StateFunction();
    }

    public static boolean contains(final String variant, final String k, final String v) {
        for (final String entry : variant.split(",")) {
            final String[] kv = entry.split("=");
            if (kv.length == 2 && kv[0].equals(k) && kv[1].equals(v)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(final String variant, final String kv) {
        for (final String entry : variant.split(",")) {
            if (entry.equals(kv)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsPartial(final String variant, final String k) {
        for (final String entry : variant.split(",")) {
            if (entry.split("=")[0].equals(k)) {
                return true;
            }
        }
        return false;
    }

    public static boolean is(final String variant, final String k, final String v) {
        return variant.equals(k + "=" + v);
    }

    public static boolean isPartial(final String variant, final String k) {
        return variant.startsWith(k + "=");
    }

    @Nullable
    public static BlockState resolve(final Block b, final String variants) {
        return Result.suppress(() -> getState(b, variants)).orElse(null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState getState(final Block b, final String variants) {
        final StateDefinition<Block, BlockState> definitions = b.getStateDefinition();
        BlockState state = b.defaultBlockState();

        for (final String variant : variants.split(",")) {
            final String[] kv = variant.split("=");
            assert kv.length == 2 : "Unchecked variant string: " + variant;

            final Property<?> property = definitions.getProperty(kv[0]);
            if (property == null) throw new JsonFormatException(b + " does not contain: " + kv[0]);

            final Comparable<?> value = property.getValue(kv[1]).orElse(null);
            if (value == null) throw new JsonFormatException(property + " does not contain: " + kv[1]);

            state = state.setValue((Property) property, (Comparable) value);
        }
        return state;
    }

    private class StateFunction implements Function<BlockState, T> {

        @Override
        public T apply(final BlockState state) {
            for (final Map.Entry<String, T> entry : map.entrySet()) {
                if (checkState(state, entry.getKey())) {
                    return entry.getValue();
                }
            }
            return map.get("");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean checkState(final BlockState state, final String variants) {
        final StateDefinition<Block, BlockState> definition = state.getBlock().getStateDefinition();
        for (final String variant : variants.split(",")) {
            final String[] kv = variant.split("=");
            assert kv.length == 2 : "Unchecked kv: " + variant;

            final Property property = definition.getProperty(kv[0]);
            final Comparable comparable = state.getValue(property);
            if (comparable != null && comparable.toString().equals(kv[1])) {
                return true;
            }
        }
        return false;
    }
}
