package com.personthecat.orestonevariants.world;

import com.personthecat.orestonevariants.blocks.SharedStateBlock;
import com.personthecat.orestonevariants.util.unsafe.ReflectionTools;
import com.personthecat.orestonevariants.util.unsafe.UnsafeUtil;
import io.netty.util.internal.EmptyPriorityQueue;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import lombok.extern.log4j.Log4j2;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.ISpecialSpawner;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraft.world.storage.SaveFormat.LevelSave;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *   This class is designed for intercepting calls to any kind of {@link World} object. It provides
 * functionality for replacing {@link BlockState} parameters and return values with those of some
 * other block. When it is finished, it will be able to replicate a regular world object with
 * exact parity by replacing any non-intercepted methods with calls to the world being wrapped.
 *
 * Todo: Still investigating how we can generate this class.
 */
@Log4j2
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorldInterceptor extends ServerWorld {

    /**
     * Used for updating any mob entities that erroneously get added to our world.
     */
    private static final Method CREATE_NAVIGATOR =
        ReflectionTools.getMethod(MobEntity.class, "func_175447_b", World.class);

    /**
     * Also used for updating mob entities after their path tracking is calculated for this
     * fake world.
     */
    private static final Method REGISTER_GOALS =
        ReflectionTools.getMethod(MobEntity.class, "func_184651_r");

    /**
     * A map containing every interceptor instance for the current game. It is imperative that
     * this map be cleared every time the user leaves a world.
     */
    private static final Map<Integer, WorldInterceptor> INSTANCE_MAP = new ConcurrentHashMap<>();

    /**
     * The underlying world object of any kind being intercepted. This allows world references
     * to persist anywhere they may be used. While this is allowed, careful attention has been
     * paid to make sure this never happens with any vanilla world types.
     */
    private WeakReference<IBlockReader> wrapped;

    /**
     * The interceptor has a different set of data in each thread. This avoids conflicts between
     * The integrated server and client threads when running locally.
     */
    private ThreadLocal<Data> data;

    /**
     * This constructor only exists so that the compiler will consider it a valid object. It is
     * not intended to be used in any way.
     */
    private WorldInterceptor(MinecraftServer server, Executor executor, LevelSave saves,
            IServerWorldInfo info, RegistryKey<World> dim, DimensionType dimType,
            IChunkStatusListener chunkListener, ChunkGenerator chunkGenerator, boolean isDebug,
            long seed, List<ISpecialSpawner> spawner, boolean unknown) {
        super(server, executor, saves, info, dim, dimType, chunkListener,
            chunkGenerator, isDebug, seed, spawner, unknown);

        // Make sure this constructor is never used.
        throw new UnsupportedOperationException("Illegal constructor access");
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
        final int id = System.identityHashCode(world);
        WorldInterceptor interceptor = INSTANCE_MAP.get(id);
        if (interceptor == null) {
            INSTANCE_MAP.put(id, (interceptor = create(world)));
        }
        return interceptor.getData().inWorld(world);
    }

    /**
     * Nullifies all of the instructions for which values to intercept. When used correctly, this
     * guarantees that no unintended side effects may occur outside of {@link SharedStateBlock}.
     */
    public static void resetThread() {
        INSTANCE_MAP.forEach((c, i) -> i.getData().reset());
    }

    /**
     * Removes all interceptors from the cache, freeing memory and ensuring that no invalid
     * references could potentially be used.
     */
    public static void clearAll() {
        log.info("Clearing interceptor cache.");
        INSTANCE_MAP.clear();
    }

    /**
     * Allocates a new <code>WorldInterceptor</code> when provided no data. The ability for this
     * class to operate successfully depends on enough overrides being present and fields being
     * manually initialized.
     *
     * @return A new interceptor which can be adapted to any world or world interface.
     */
    private static WorldInterceptor create(IBlockReader reader) {
        WorldInterceptor interceptor = UnsafeUtil.allocate(WorldInterceptor.class);
        if (reader instanceof ServerWorld) {
            interceptor.copyWorldData((ServerWorld) reader);
            interceptor.copyServerData((ServerWorld) reader);
        } else if (reader instanceof World) {
            interceptor.copyWorldData((World) reader);
            interceptor.generateServerData();
        } else {
            interceptor.generateWorldData();
            interceptor.generateServerData();
        }
        interceptor.disableLevelSaving = true;
        interceptor.profiler = () -> EmptyProfiler.INSTANCE;
        interceptor.wrapped = new WeakReference<>(reader);
        interceptor.data = ThreadLocal.withInitial(() -> new Data(interceptor));
        return interceptor;
    }

    /**
     * This function copies as much information as possible from another {@link World} object. It
     * is designed to enable this interceptor to disguise itself using more than just functions.
     *
     * @param world Any regular world object to be copied from.
     */
    private void copyWorldData(World world) {
        this.capturedBlockSnapshots = world.capturedBlockSnapshots;
        this.addedTileEntityList = world.addedTileEntityList;
        this.tickableTileEntities = world.tickableTileEntities;
        this.loadedTileEntityList = world.loadedTileEntityList;
        this.rand = world.rand;
        this.mainThread = world.mainThread;
        this.isDebug = world.isDebug;
        this.isRemote = world.isRemote;
        this.worldBorder = world.worldBorder;
        this.dimension = world.dimension;
        this.dimensionType = world.dimensionType;
        this.biomeManager = world.biomeManager;
    }

    /**
     * In addition to {@link #copyWorldData}, this function copies any information that is unique
     * to a {@link ServerWorld}, which this class extends from due to the amount of coverage that
     * provides.
     *
     * @param world Any regular {@link ServerWorld} or a child of this class.
     */
    private void copyServerData(ServerWorld world) {
        this.worldInfo = world.worldInfo;
        this.entitiesById = world.entitiesById;
        this.entitiesByUuid = world.entitiesByUuid;
        this.entitiesToAdd = world.entitiesToAdd;
        this.players = world.players;
        this.field_241102_C_ = world.field_241102_C_;
        this.server = world.server;
        this.worldTeleporter = world.worldTeleporter;
        this.pendingBlockTicks = world.pendingBlockTicks;
        this.pendingFluidTicks = world.pendingFluidTicks;
        this.navigations = world.navigations;
        this.field_241105_O_ = world.field_241105_O_;
        this.field_241106_P_ = world.field_241106_P_;
    }

    /**
     * This function is called to provide as much dummy information as possible when intercepting
     * methods from a non-world class. This is purely intended to prevent as many NPEs as possible
     * for any class that may unknowingly cast this object to an instance of {@link ServerWorld}
     * and thus gain access to information which it should not have.
     */
    private void generateWorldData() {
        this.addedTileEntityList = Collections.emptyList();
        this.loadedTileEntityList = Collections.emptyList();
        this.tickableTileEntities = Collections.emptyList();
        this.capturedBlockSnapshots = new ArrayList<>();
        this.rand = new Random();
        this.mainThread = Thread.currentThread();
        this.isDebug = true;
        this.isRemote = false;
        this.worldBorder = new WorldBorder();
        this.dimension = World.OVERWORLD;
    }

    /**
     * This is a variant of {@link #generateWorldData} which provides information specific to a
     * {@link ServerWorld} world type. Much of this information--for example, a Minecraft server
     * --cannot be stubbed out and thus will still return null.
     */
    private void generateServerData() {
        this.entitiesByUuid = Collections.emptyMap();
        this.entitiesById = Int2ObjectMaps.emptyMap();
        this.entitiesToAdd = EmptyPriorityQueue.instance();
        this.players = Collections.emptyList();
        this.pendingBlockTicks =
            new ServerTickList<>(this, b -> true, IForgeRegistryEntry::getRegistryName, e -> {});
        this.pendingFluidTicks =
            new ServerTickList<>(this, f -> true, IForgeRegistryEntry::getRegistryName, e -> {});
        this.navigations = Collections.emptySet();
    }

    /**
     * Returns the original world being wrapped by the interceptor after verifying that the
     * reference is still valid.
     */
    private IBlockReader getWrappedWorld() {
        return Objects.requireNonNull(wrapped.get(), "World reference has been culled.");
    }

    /**
     * Returns the thread-local data defining everything being currently intercepted. While
     * the return value of this function is guaranteed to be non-null, it is still imperative
     * that callers verify the <em>members</em> of this value before using them.
     */
    private Data getData() {
        return Objects.requireNonNull(this.data.get(), "Initial data supplier was missing.");
    }

    @Override
    protected void initCapabilities() {}

    @Override
    public void calculateInitialSkylight() {}

    @Override
    protected void calculateInitialWeather() {}

    @Override
    public ServerTickList<Block> getPendingBlockTicks() {
        final IBlockReader reader = this.getWrappedWorld();
        final Data data = this.getData();
        if (data.isPrimed) {
            return data.tickInterceptor;
        } else if (reader instanceof ServerWorld) {
            return ((ServerWorld) reader).getPendingBlockTicks();
        }
        return super.getPendingBlockTicks();
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        final Data data = this.getData();
        final BlockState actual = this.getWrappedWorld().getBlockState(pos);
        if (data.checkPos(pos) && actual.getBlock().equals(data.to)) {
            // We're expecting the actual block, but want to return the block being wrapped.
            return data.mapTo.apply(actual);
        }
        return actual;
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        final Data data = this.getData();
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof IWorld) {
            if (data.checkPos(pos) && state.getBlock().equals(data.from)) {
                // We're expecting the block being wrapped, but want to return the actual block.
                state = data.mapFrom.apply(state);
            }
            return ((IWorld) reader).setBlockState(pos, state, flags, recursionLeft);
        }
        return false;
    }

    @Override
    public void addBlockEvent(BlockPos pos, Block block, int eventID, int eventParam) {
        final Data data = this.getData();
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            if (data.checkPos(pos) && block.equals(data.from)) {
                block = data.to;
            }
            ((World) reader).addBlockEvent(pos, block, eventID, eventParam);
        }
    }

    @Override
    public BlockPos getBlockRandomPos(int x, int y, int z, int yMask) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            return ((World) reader).getBlockRandomPos(x, y, z, yMask);
        }
        return BlockPos.ZERO;
    }

    @Override
    public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        final Data data = this.getData();
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            if (data.checkPos(pos)) {
                if (oldState.getBlock().equals(data.from)) {
                    oldState = data.mapFrom.apply(oldState);
                }
                if (newState.getBlock().equals(data.from)) {
                    newState = data.mapFrom.apply(newState);
                }
            }
            ((World) reader).notifyBlockUpdate(pos, oldState, newState, flags);
        }
    }

    @Override
    public void neighborChanged(BlockPos pos, Block block, BlockPos fromPos) {
        final Data data = this.getData();
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            if (data.checkPos(pos) && block.equals(data.from)) {
                block = data.to;
            }
            ((World) reader).neighborChanged(pos, block, fromPos);
        }
    }

    @Override
    public void onBlockStateChange(BlockPos pos, BlockState oldState, BlockState newState) {
        final Data data = this.getData();
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            if (data.checkPos(pos)) {
                if (oldState.getBlock().equals(data.from)) {
                    oldState = data.mapFrom.apply(oldState);
                }
                if (newState.getBlock().equals(data.from)) {
                    newState = data.mapFrom.apply(newState);
                }
            }
            ((World) reader).onBlockStateChange(pos, oldState, newState);
        }
    }

    @Override
    public ServerTickList<Fluid> getPendingFluidTicks() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof ServerWorld) {
            return ((ServerWorld) reader).getPendingFluidTicks();
        }
        return super.getPendingFluidTicks();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getWrappedWorld().getFluidState(pos);
    }

    @Override
    public void updateComparatorOutputLevel(BlockPos pos, Block block) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            final Data data = this.getData();
            if (data.checkPos(pos) && block.equals(data.from)) {
                block = data.to;
            }
            ((World) reader).updateComparatorOutputLevel(pos, block);
        }
    }

    @Override
    public boolean addEntity(Entity entity) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            final World world = (World) reader;
            entity.setWorld(world);

            if (entity instanceof FallingBlockEntity) {
                final Data data = this.getData();
                final FallingBlockEntity fbe = (FallingBlockEntity) entity;
                if (fbe.fallTile.getBlock().equals(data.from)) {
                    fbe.fallTile = data.mapFrom.apply(fbe.fallTile);
                }
            } else if (entity instanceof MobEntity) {
                // Mob entities will keep references to the
                // current world and thus must be recreated.
                final MobEntity mob = (MobEntity) entity;
                mob.navigator = ReflectionTools.get(CREATE_NAVIGATOR, mob, world);
                if (!world.isRemote()) {
                    ReflectionTools.invoke(REGISTER_GOALS, mob);
                }
            }
            return world.addEntity(entity);
        }
        return false;
    }

    @Override
    public void setEntityState(Entity entity, byte state) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            ((World) reader).setEntityState(entity, state);
        }
    }

    @Override
    public boolean addTileEntity(TileEntity tile) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            return ((World) reader).addTileEntity(tile);
        }
        return false;
    }

    @Override
    @Nullable
    public TileEntity getTileEntity(BlockPos pos) {
        return this.getWrappedWorld().getTileEntity(pos);
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntity) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            ((World) reader).setTileEntity(pos, tileEntity);
        }
    }

    @Override
    public void removeTileEntity(BlockPos pos) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            ((World) reader).removeTileEntity(pos);
        }
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            return ((World) reader).destroyBlock(pos, dropBlock, entity, recursionLeft);
        }
        return false;
    }

    @Override
    public void tickBlockEntities() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            ((World) reader).tickBlockEntities();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof ClientWorld) {
            ((ClientWorld) reader).addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addParticle(IParticleData particleData, boolean forceAlwaysRender, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof ClientWorld) {
            ((ClientWorld) reader).addParticle(particleData, forceAlwaysRender, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addOptionalParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof ClientWorld) {
            ((ClientWorld) reader).addOptionalParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addOptionalParticle(IParticleData particleData, boolean ignoreRange, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof ClientWorld) {
            ((ClientWorld) reader).addOptionalParticle(particleData, ignoreRange, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            ((World) reader).playSound(player, x, y, z, sound, category, volume, pitch);
        }
    }

    @Override
    public void playMovingSound(@Nullable PlayerEntity player, Entity entity, SoundEvent event, SoundCategory category, float volume, float pitch) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            ((World) reader).playMovingSound(player, entity, event, category, volume, pitch);
        }
    }

    @Override
    public boolean isRemote() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            return ((World) reader).isRemote();
        }
        return false;
    }

    @Override
    public Biome getNoiseBiomeRaw(int x, int y, int z) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof IWorldReader) {
            return ((IWorldReader) reader).getNoiseBiomeRaw(x, y, z);
        }
        return super.getNoiseBiomeRaw(x, y, z);
    }

    @Override
    public BiomeManager getBiomeManager() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof IWorldReader) {
            return ((IWorldReader) reader).getBiomeManager();
        }
        return super.getBiomeManager();
    }

    @Override
    public Optional<RegistryKey<Biome>> func_242406_i(BlockPos pos) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof IBiomeReader) {
            return ((IBiomeReader) reader).func_242406_i(pos);
        }
        return Optional.empty();
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof IWorldReader) {
            return ((IWorldReader) reader).getBiome(pos);
        }
        return super.getBiome(pos);
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof IBiomeReader) {
            ((IBiomeReader) reader).getNoiseBiome(x, y, z);
        }
        return super.getNoiseBiome(x, y, z);
    }

    @Override
    public int getSeaLevel() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            ((World) reader).getSeaLevel();
        }
        return 64;
    }

    @Override
    public IWorldInfo getWorldInfo() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof IWorld) {
            return ((IWorld) reader).getWorldInfo();
        }
        return super.getWorldInfo();
    }

    @Override
    public GameRules getGameRules() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            return ((World) reader).getGameRules();
        }
        return super.getGameRules();
    }

    @Override
    public ServerScoreboard getScoreboard() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof ServerWorld) {
            return ((ServerWorld) reader).getScoreboard();
        }
        return super.getScoreboard();
    }

    @Override
    public Teleporter getDefaultTeleporter() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof ServerWorld) {
            return ((ServerWorld) reader).getDefaultTeleporter();
        }
        throw new IllegalStateException("Caller needs to check World#isRemote");
    }

    @Override
    public Supplier<IProfiler> getWorldProfiler() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            return ((World) reader).getWorldProfiler();
        }
        return super.getWorldProfiler();
    }

    @Override
    public RegistryKey<World> getDimensionKey() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            return ((World) reader).getDimensionKey();
        }
        return super.getDimensionKey();
    }

    @Override
    public DimensionType getDimensionType() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof IWorldReader) {
            return ((IWorldReader) reader).getDimensionType();
        }
        return super.getDimensionType();
    }

    @Override
    public ServerChunkProvider getChunkProvider() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof ServerWorld) {
            return ((ServerWorld) reader).getChunkProvider();
        }
        return super.getChunkProvider();
    }

    @Override
    public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof IWorldReader) {
            return ((IWorldReader) reader).getChunk(x, z, requiredStatus, nonnull);
        }
        return null;
    }

    @Override
    public float getRainStrength(float delta) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            return ((World) reader).getRainStrength(delta);
        }
        return 0F;
    }

    @Override
    public float getThunderStrength(float delta) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            return ((World) reader).getThunderStrength(delta);
        }
        return 0F;
    }

    @Override
    public boolean isDaytime() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            return ((World) reader).isDaytime();
        }
        return true;
    }

    @Override
    public boolean isNightTime() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            return ((World) reader).isNightTime();
        }
        return false;
    }

    @Override
    public WorldLightManager getLightManager() {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof World) {
            return ((World) reader).getLightManager();
        }
        return super.getLightManager();
    }

    @Override
    public int getLightFor(LightType type, BlockPos pos) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof IBlockDisplayReader) {
            return ((IBlockDisplayReader) reader).getLightFor(type, pos);
        }
        return 0;
    }

    @Override
    public int getLightSubtracted(BlockPos pos, int amount) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof IBlockDisplayReader) {
            return ((IBlockDisplayReader) reader).getLightSubtracted(pos, amount);
        }
        return 0;
    }

    @Override
    public int getLightValue(BlockPos pos) {
        return this.getWrappedWorld().getLightValue(pos);
    }

    @Override
    public int getMaxLightLevel() {
        return this.getWrappedWorld().getMaxLightLevel();
    }

    @Override
    public int getLight(BlockPos pos) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof IWorldReader) {
            return ((IWorldReader) reader).getLight(pos);
        }
        return 0;
    }

    @Override
    public int getNeighborAwareLightSubtracted(BlockPos pos, int amount) {
        final IBlockReader reader = this.getWrappedWorld();
        if (reader instanceof IWorldReader) {
            return ((IWorldReader) reader).getNeighborAwareLightSubtracted(pos, amount);
        }
        return 0;
    }

    @Override
    public String toString() {
        return "WorldInterceptor[" + wrapped.get() + "]";
    }

    /**
     * Contains all of the data about which blocks are being replaced and in which world in the
     * current thread.
     */
    public static class Data {
        private final WorldInterceptor interceptor;
        private Block from = Blocks.AIR;
        private Block to = Blocks.AIR;
        private BlockPos pos = null;

        private final TickInterceptor tickInterceptor;
        private Function<BlockState, BlockState> mapFrom = from -> from;
        private Function<BlockState, BlockState> mapTo = to -> to;
        private boolean isPrimed = false;

        private Data(WorldInterceptor interceptor) {
            this.interceptor = interceptor;
            this.tickInterceptor = new TickInterceptor(interceptor);
        }

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
                this.tickInterceptor.wrapping(world.getPendingBlockTicks());
            }
            this.isPrimed = true;
            return this;
        }

        /**
         * Determines whether we are primed to intercept the current position. We will intercept this
         * position if the data do not specify a position or if the two positions match.
         *
         * @param pos The current position being intercepted.
         * @return Whether to intercept this position at all.
         */
        private boolean checkPos(BlockPos pos) {
            return this.isPrimed && (this.pos == null || this.pos == pos);
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
         * Primes the interceptor to only intercept block updates at the current location.
         * This field is nullable and thus this function is optional.
         *
         * @param pos The current block position being intercepted.
         * @return <code>this</code>, for method chaining.
         */
        public Data onlyAt(BlockPos pos) {
            this.pos = pos;
            this.tickInterceptor.onlyAt(pos);
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
            return interceptor;
        }

        /**
         * Prevents the interceptor from replacing any unintended blocks.
         */
        private void reset() {
            this.from = Blocks.AIR;
            this.to = Blocks.AIR;
            this.tickInterceptor.reset();
            this.mapFrom = s -> s;
            this.mapTo = s -> s;
            this.isPrimed = false;
        }
    }
}
