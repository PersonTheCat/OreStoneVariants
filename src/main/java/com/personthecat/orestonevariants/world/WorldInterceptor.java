package com.personthecat.orestonevariants.world;

import com.personthecat.orestonevariants.util.WriteOnce;
import lombok.Builder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.ISpecialSpawner;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.SaveFormat.LevelSave;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static com.personthecat.orestonevariants.util.CommonMethods.info;
import static com.personthecat.orestonevariants.util.CommonMethods.runEx;

/**
 * This class is designed for intercepting calls to any kind of {@link World} object. It provides
 * functionality for replacing {@link BlockState} parameters and return values with those of some
 * other block. When it is finished, it will be able to replicate a regular world object with
 * exact parity by replacing any non-intercepted methods with calls to the world being wrapped.
 *
 * Todo: implement AutoCloseable for a cleaner syntax.
 *
 * WIP
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorldInterceptor extends ServerWorld {

    /**
     * The single instance of this interceptor. Constructing fake worlds is expensive even when
     * they are filled with mock data. This avoids unnecessary load time and allows the instance
     * to be constructed only when the server is initially starting.
     */
    private static final WriteOnce<WorldInterceptor> INSTANCE = new WriteOnce<>();

    /**
     * The interceptor has a different set of data in each thread. This avoids conflicts between
     * The integrated server and client threads when running locally.
     */
    private final ThreadLocal<Data> data = ThreadLocal.withInitial(Data::new);

    @Builder
    private WorldInterceptor(MinecraftServer server, Executor executor, LevelSave saves,
            IServerWorldInfo info, RegistryKey<World> dim, DimensionType dimType,
            IChunkStatusListener chunkListener, ChunkGenerator chunkGenerator, boolean isDebug,
            long seed, List<ISpecialSpawner> spawner, boolean unknown) {
        super(server, executor, saves, info, dim, dimType, chunkListener,
            chunkGenerator, isDebug, seed, spawner, unknown);
    }

    /**
     * Called whenever the singleton should be instantiated for the first time, on {@link
     * FMLServerStartingEvent}. Any subsequent calls will be ignored.
     *
     * @param world Any {@link ServerWorld} as it is loading. These provide the most functionality.
     */
    public static void init(ServerWorld world) {
        if (!INSTANCE.isSet()) {
            info("Loading world interceptor.");
            INSTANCE.set(create(world));
        }
    }

    /**
     * Returns a handle on the current world interceptor ensuring that an up to date {@link World}
     * is provided. It is safe to call this function with any type of world, as access checks will
     * be handled by the interceptor to provide as much functionality as possible at any time.
     *
     * @param world The object providing {@link BlockState}s.
     * @return The thread-local data for this interceptor in a builder style syntax.
     */
    public static Data inWorld(IBlockReader world) {
        return INSTANCE.get().data.get().inWorld(world);
    }

    /**
     * Nullifies all references being stored by the wrapper. When used correctly, this guarantees
     * that no unexpected operations will occur. In other words, we'll never attempt to make any
     * calls to the wrong world or maintain references to a world after it has closed down.
     */
    public void clear() {
        this.data.get().clear();
    }

    /**
     * Builds a new WorldInterceptor using fake data where possible, otherwise copying from
     * <code>world</code> when necessary.
     *
     * @param world Any current server world providing dummy info for this wrapper.
     * @return A new interceptor which can be adapted to any world or world interface.
     */
    private static WorldInterceptor create(ServerWorld world) {
        return builder()
            .server(world.getServer())
            .executor(Runnable::run)
            .saves(getDummySave())
            .info((IServerWorldInfo) world.getWorldInfo())
            .dim(world.getDimensionKey())
            .dimType(world.getDimensionType())
            .seed(world.getSeed())
            .isDebug(world.isDebug())
            .spawner(Collections.emptyList())
            .chunkGenerator(world.getChunkProvider().getChunkGenerator())
            .build();
    }

    /**
     * A new save directory which will not be written to. It is required by the super constructor,
     * which will use it to create relative directories.
     *
     * @return A new {@link LevelSave} entitled <code>dummy</code>.
     */
    private static LevelSave getDummySave() {
        try {
            return SaveFormat.create(Paths.get("dummy")).getLevelSave("dummy");
        } catch (IOException e) {
            throw runEx("Error creating dummy save file.");
        }
    }

    @Override
    protected void initCapabilities() {}

    @Override
    public void calculateInitialSkylight() {}

    @Override
    protected void calculateInitialWeather() {}

    @Override
    public ServerTickList<Block> getPendingBlockTicks() {
        return data.get().tickInterceptor;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        final Data data = this.data.get();
        final BlockState actual = data.getCurrentReader().getBlockState(pos);
        if (actual.getBlock().equals(data.to)) {
            // We're expecting the actual block, but want to return the block being wrapped.
            return data.mapTo.apply(actual);
        }
        return actual;
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        final Data data = this.data.get();
        final IWorld world = data.getCurrentWorld();
        if (state.getBlock().equals(data.from)) {
            // We're expecting the block being wrapped, but want to return the actual block.
            state = data.mapFrom.apply(state);
        }
        return world.setBlockState(pos, state, flags, recursionLeft);
    }

    // Todo: look for a better way to guarantee tickInterceptor is never null.
    public class Data {
        // We store the highest level of access that we have and
        // only use the lowest level needed at any time.
        // Todo: consider using only one world type and instanceof checking at the call site.
        private WeakReference<IWorld> currentWorld = new WeakReference<>(null);
        private WeakReference<IBlockReader> currentReader = new WeakReference<>(null);
        private Block from = Blocks.AIR;
        private Block to = Blocks.AIR;

        private final TickInterceptor tickInterceptor = new TickInterceptor(WorldInterceptor.this);
        private Function<BlockState, BlockState> mapFrom = from -> from;
        private Function<BlockState, BlockState> mapTo = to -> to;

        /**
         * Primes the interceptor to read blocks from a new {@link IBlockReader}. This can be used
         * whenever it is unknown whether the world object is a regular instance of {@link World}.
         *
         * @param reader The object providing {@link BlockState}s.
         * @return <code>this</code>, for method chaining.
         */
        private Data inWorld(IBlockReader reader) {
            if (reader instanceof IWorld) {
                final IWorld world = (IWorld) reader;
                this.currentWorld = new WeakReference<>(world);
                this.tickInterceptor.wrapping(world.getPendingBlockTicks());
            }
            this.currentReader = new WeakReference<>(reader);
            return this;
        }

        /**
         * Primes the interceptor to detect scheduled ticks and returned or set block states and
         * replace them with a different block.
         *
         * @param from The block we're expecting to intercept.
         * @param to The block we're replacing it with.
         * @return <code>this</code>, for method chaining.
         */
        public Data intercepting(Block from, Block to) {
            this.from = from;
            this.to = to;
            tickInterceptor.listenFor(from, to);
            return this;
        }

        /**
         * Primes the interceptor to convert the expected block state into the desired state.
         *
         * @param fromMapper A mapper converting from -> to
         * @return <code>this</code>, for method chaining.
         */
        public Data mappingFrom(Function<BlockState, BlockState> fromMapper) {
            this.mapFrom = fromMapper;
            return this;
        }

        /**
         * Primes the interceptor to convert the actual block state into the state expected by
         * the wrapper.
         *
         * @param toMapper A mapper converting to -> from
         * @return <code>this</code>, for method chaining.
         */
        public Data mappingTo(Function<BlockState, BlockState> toMapper) {
            this.mapTo = toMapper;
            return this;
        }

        /**
         * Provides the interceptor back to the call site after being primed for intercepting
         * any expected calls. The caller must ensure that these data are cleared after being
         * passed into the receiver.
         *
         * @return The parent object to be used as a regular world.
         */
        public WorldInterceptor getWorld() {
            return WorldInterceptor.this;
        }

        private IWorld getCurrentWorld() {
            return Objects.requireNonNull(currentWorld.get(), "World reference has been culled.");
        }

        private IBlockReader getCurrentReader() {
            return Objects.requireNonNull(currentReader.get(), "Reader reference has been culled.");
        }

        private void clear() {
            this.from = null;
            this.to = null;
            this.currentWorld = null;
            this.currentReader = null;
            this.tickInterceptor.clear();
        }
    }
}
