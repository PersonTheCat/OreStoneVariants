package personthecat.osv.block;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SharedStateBlock extends Block {

    private final Block bg;
    private final Block fg;

    public SharedStateBlock(final Block bg, final Block fg, final Properties properties) {
        super(properties);
        this.bg = bg;
        this.fg = fg;
    }

    @ExpectPlatform
    public static SharedStateBlock createPlatformVariant(final Block bg, final Block fg, final Properties properties) {
        throw new AssertionError();
    }

    public BlockState translate(final BlockState me, final Block other) {
//        throw new UnsupportedOperationException();
        return other.defaultBlockState();
    }

    public Block getBg() {
        return this.bg;
    }

    public Block getFg() {
        return this.fg;
    }
}
