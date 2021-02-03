package com.personthecat.orestonevariants.world;

import com.personthecat.orestonevariants.util.WriteOnce;
import lombok.Builder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockReader;
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

import static com.personthecat.orestonevariants.util.CommonMethods.info;
import static com.personthecat.orestonevariants.util.CommonMethods.runEx;

/**
 * This class is designed for intercepting calls to any kind of {@link World} object. It provides
 * functionality for replacing {@link BlockState} parameters and return values with those of some
 * other block. When it is finished, it will be able to replicate a regular world object with
 * exact parity by replacing any non-intercepted methods with calls to the world being wrapped.
 *
 * WIP
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorldInterceptor extends ServerWorld {

    private static final WriteOnce<WorldInterceptor> INSTANCE = new WriteOnce<>();

    private final TickInterceptor tickInterceptor;
    // Todo: still researching and testing thread safety.
    private WeakReference<World> currentWorld;
    private WeakReference<IBlockReader> currentReader;
    private Block from;
    private Block to;
    private BlockPos pos;

    @Builder
    private WorldInterceptor(ServerWorld target, MinecraftServer server, Executor executor, LevelSave saves,
            IServerWorldInfo info, RegistryKey<World> dim, DimensionType dimType, IChunkStatusListener chunkListener,
            ChunkGenerator chunkGenerator, boolean isDebug, long seed, List<ISpecialSpawner> spawner, boolean unknown) {
        super(server, executor, saves, info, dim, dimType, chunkListener,
            chunkGenerator, isDebug, seed, spawner, unknown);
        this.currentWorld = new WeakReference<>(target);
        this.currentReader = new WeakReference<>(target);
        this.tickInterceptor = new TickInterceptor(target);
    }

    /**
     * Returns the only single instance of this interceptor. <em>The caller must guarantee that
     * the world object being intercepted is updated with each use to ensure that calls are
     * forwarded correctly.
     */
    public static WorldInterceptor getInstance() {
        return INSTANCE.get();
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
     * Primes the interceptor to forward calls to and from a new {@link World} object.
     *
     * @param world The current world being wrapped.
     * @return <code>this</code>, for method chaining.
     */
    public WorldInterceptor inWorld(World world) {
        this.currentWorld = new WeakReference<>(world);
        this.currentReader = new WeakReference<>(world);
        tickInterceptor.wrapping(world.getPendingBlockTicks());
        return this;
    }

    /**
     * Primes the interceptor to read blocks from a new {@link IBlockReader}. This can be used
     * whenever it is unknown whether the world object is a regular instance of {@link World}.
     *
     * @param reader The object providing {@link BlockState}s.
     * @return <code>this</code>, for method chaining.
     */
    public WorldInterceptor readingFrom(IBlockReader reader) {
        if (reader instanceof World) {
            final World world = (World) reader;
            this.currentWorld = new WeakReference<>(world);
            tickInterceptor.wrapping(world.getPendingBlockTicks());
        }
        this.currentReader = new WeakReference<>(reader);
        return this;
    }

    /**
     * Primes the interceptor to detect scheduled ticks and returned or set block states and
     * replace them with a different block.
     *
     * Todo: Add a function for mapping the input block instead of using just from and to.
     *
     * @param from The block we're expecting to intercept.
     * @param to The block we're replacing it with.
     * @param pos The current block pos being operated on.
     * @return <code>this</code>, for method chaining.
     */
    public WorldInterceptor intercepting(Block from, Block to, BlockPos pos) {
        this.from = from;
        this.to = to;
        this.pos = pos;
        tickInterceptor.listenFor(from.getBlock(), to.getBlock());
        return this;
    }

    /**
     * Nullifies all references being stored by the wrapper. When used correctly, this guarantees
     * that no unexpected operations will occur. In other words, we'll never attempt to make any
     * calls to the wrong world or maintain references to a world after it has closed down.
     */
    public void clear() {
        this.from = null;
        this.to = null;
        this.pos = null;
        this.currentWorld = null;
        this.currentReader = null;
        this.tickInterceptor.clear();
    }

    private static WorldInterceptor create(ServerWorld world) {
        return builder()
            .target(world)
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

    private static LevelSave getDummySave() {
        try {
            return SaveFormat.create(Paths.get("dummy")).getLevelSave("dummy");
        } catch (IOException e) {
            throw runEx("Error creating dummy save file.");
        }
    }

    private World getCurrentWorld() {
        return Objects.requireNonNull(currentWorld.get(), "World reference has been culled.");
    }

    private IBlockReader getCurrentReader() {
        return Objects.requireNonNull(currentReader.get(), "Reader reference has been culled.");
    }

    @Override
    protected void initCapabilities() {}

    @Override
    public void calculateInitialSkylight() {}

    @Override
    protected void calculateInitialWeather() {}

    @Override
    public ServerTickList<Block> getPendingBlockTicks() {
        return tickInterceptor;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (pos.equals(this.pos)) {
            // Todo: use mapper
            return to.getDefaultState();
        }
        return getCurrentReader().getBlockState(pos);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        final World world = getCurrentWorld();
        if (state.getBlock().equals(this.from)) {
            // Todo: use mapper
            state = this.to.getDefaultState();
        }
        return world.setBlockState(pos, state, flags, recursionLeft);
    }
}
