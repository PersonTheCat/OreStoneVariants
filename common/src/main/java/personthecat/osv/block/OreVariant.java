package personthecat.osv.block;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import personthecat.osv.world.interceptor.InterceptorAccessor;
import personthecat.osv.world.interceptor.InterceptorDispatcher;

import java.util.Collection;
import java.util.Random;

public class OreVariant extends SharedStateBlock {

    private final Block bg;
    private final Block fg;

    public OreVariant(final Properties properties, final StateConfig config) {
        super(properties, config);
        this.bg = config.bg;
        this.fg = config.fg;
    }

    @ExpectPlatform
    public static OreVariant createPlatformVariant(final Properties properties, final StateConfig config) {
        throw new AssertionError();
    }

    public Block getBg() {
        return this.bg;
    }

    public Block getFg() {
        return this.fg;
    }

    public BlockState toEither(final BlockState me, final Block other) {
        return copyInto(other.defaultBlockState(), me);
    }

    public BlockState fromEither(final BlockState other) {
        return copyInto(this.defaultBlockState(), other);
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private static BlockState copyInto(BlockState base, final BlockState... sources) {
        final Collection<Property<?>> validProperties = base.getProperties();
        for (final BlockState source : sources) {
            for (final Property property : source.getValues().keySet()) {
                if (validProperties.contains(property)) {
                    base = base.setValue(property, source.getValue(property));
                }
            }
        }
        return base;
    }

    private <L extends LevelAccessor> L prime(final L level, final Block in) {
        return InterceptorDispatcher.prime(level).intercept(this, in).getInterceptor();
    }

    // Todo: the interceptor needs to *just* take the ore variant and dynamically match bg / fg
    private <L extends LevelAccessor> L primeRestricted(final L level, final Block in, final BlockPos pos) {
        return InterceptorDispatcher.prime(level).intercept(this, in).at(pos).getInterceptor();
    }

    @Override
    public boolean is(final Tag<Block> tag) {
        return super.is(tag) || this.fg.is(tag) || this.bg.is(tag);
    }

    @Override
    public boolean is(final Block block) {
        return super.is(block) || this.fg.is(block) || this.bg.is(block);
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext ctx) {
        return this.bg.getShape(copyInto(this.bg.defaultBlockState(), state), level, pos, ctx);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void animateTick(final BlockState state, final Level level, final BlockPos pos, final Random rand) {
        Level interceptor = this.primeRestricted(level, this.fg, pos);
        try {
            this.fg.animateTick(copyInto(this.fg.defaultBlockState(), state), interceptor, pos, rand);

            interceptor = this.prime(level, this.bg);
            this.bg.animateTick(copyInto(this.bg.defaultBlockState(), state), interceptor, pos, rand);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }
}
