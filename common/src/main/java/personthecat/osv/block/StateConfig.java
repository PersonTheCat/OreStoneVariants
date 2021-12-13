package personthecat.osv.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public class StateConfig {
    final Block bg;
    final Block fg;
    final Property<?>[] mixins;

    public StateConfig(final Block bg, final Block fg, final Property<?>... mixins) {
        this.bg = bg;
        this.fg = fg;
        this.mixins = mixins;
    }
}
