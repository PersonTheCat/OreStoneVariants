package personthecat.osv.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import personthecat.catlib.exception.JsonFormatException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StateMap<T> {

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

    public void put(final String variant, final T t) {
        this.map.put(variant, t);
    }

    public T get(final String variant) {
        return this.map.get(variant);
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

    public Function<BlockState, T> createFunction() {
        return s -> { throw new UnsupportedOperationException(); };
    }

    public Map<BlockState, T> forBlock(final Block b) {
        final Map<BlockState, T> remapped = new HashMap<>();
        final T t = this.map.get("");
        if (t != null) {
            for (final BlockState state : b.getStateDefinition().getPossibleStates()) {
                remapped.put(state, t);
            }
        } else {
            for (final Map.Entry<String, T> entry : this.map.entrySet()) {
                remapped.put(getState(b, entry.getKey()), entry.getValue());
            }
        }
        return remapped;
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
}
