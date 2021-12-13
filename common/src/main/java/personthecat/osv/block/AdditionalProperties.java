package personthecat.osv.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class AdditionalProperties {
    public static final BooleanProperty DENSE = BooleanProperty.create("dense");

    public static boolean isDense(final BlockState state) {
        return state.hasProperty(DENSE) && state.getValue(DENSE);
    }

    public static BlockState nonDense(final BlockState state) {
        return state.hasProperty(DENSE) ? state.setValue(DENSE, false) : state;
    }

    public static BlockState applyDefaults(final BlockState state) {
        return state.hasProperty(DENSE) ? state.setValue(DENSE, false) : state;
    }
}
