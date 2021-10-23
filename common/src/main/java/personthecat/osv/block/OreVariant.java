package personthecat.osv.block;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.ModRegistries;
import personthecat.osv.item.VariantItem;
import personthecat.osv.mixin.UseOnContextAccessor;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.world.interceptor.InterceptorAccessor;
import personthecat.osv.world.interceptor.InterceptorDispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OreVariant extends SharedStateBlock {

    protected final OrePreset preset;
    protected final Block bg;
    protected final Block fg;

    private final Map<BlockState, VariantItem> itemMap = new HashMap<>();

    public OreVariant(final OrePreset preset, final Properties properties, final StateConfig config) {
        super(properties, config);
        this.preset = preset;
        this.bg = config.bg;
        this.fg = config.fg;
    }

    @ExpectPlatform
    @SuppressWarnings("unused")
    public static OreVariant createPlatformVariant(final OrePreset preset, final Properties properties, final StateConfig config) {
        throw new AssertionError();
    }

    public Block getBg() {
        return this.bg;
    }

    public Block getFg() {
        return this.fg;
    }

    public BlockState asBg(final BlockState me) {
        return copyInto(this.bg.defaultBlockState(), me);
    }

    public BlockState asFg(final BlockState me) {
        return copyInto(this.fg.defaultBlockState(), me);
    }

    public VariantItem asItem(final BlockState me) {
        return this.itemMap.computeIfAbsent(me, s ->
            ModRegistries.ITEMS.findByValue(i -> me.equals(i.getState()))
                .orElseGet(() -> (VariantItem) this.asItem()));
    }

    protected <L extends LevelAccessor> L prime(final L level, final Block in) {
        return InterceptorDispatcher.prime(level).intercept(this, in).getInterceptor();
    }

    // Todo: the interceptor needs to *just* take the ore variant and dynamically match bg / fg
    protected <L extends LevelAccessor> L primeRestricted(final L level, final Block in, final BlockPos pos) {
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
    public boolean propagatesSkylightDown(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bg.propagatesSkylightDown(state, getter, pos);
    }

    @Override
    public void destroy(final LevelAccessor level, final BlockPos pos, final BlockState state) {
        final LevelAccessor interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            this.bg.destroy(interceptor, pos, state);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    public void wasExploded(final Level level, final BlockPos pos, final Explosion explosion) {
        final Level interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            this.bg.wasExploded(interceptor, pos, explosion);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    public void stepOn(final Level level, final BlockPos pos, final Entity entity) {
        Level interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            this.bg.stepOn(interceptor, pos, entity);

            interceptor = this.prime(level, this.fg);
            this.fg.stepOn(interceptor, pos, entity);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext ctx) {
        Level interceptor = this.primeRestricted(ctx.getLevel(), this.bg, ctx.getClickedPos());
        try {
            ((UseOnContextAccessor) ctx).setLevel(interceptor);
            final BlockState bgState = this.bg.getStateForPlacement(ctx);

            this.prime(ctx.getLevel(), this.fg);
            final BlockState fgState = this.fg.getStateForPlacement(ctx);

            return copyInto(this.defaultBlockState(), fgState, bgState);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity entity, ItemStack stack) {
        final Level interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            this.bg.playerDestroy(interceptor, player, pos, this.asBg(state), entity, stack);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        final Level interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            this.bg.setPlacedBy(interceptor, pos, this.asBg(state), entity, stack);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    public void fallOn(final Level level, final BlockPos pos, final Entity entity, final float f) {
        final Level interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            this.bg.fallOn(interceptor, pos, entity, f);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    public void updateEntityAfterFallOn(final BlockGetter getter, final Entity entity) {
        this.bg.updateEntityAfterFallOn(getter, entity);
    }

    @Override
    public void playerWillDestroy(final Level level, final BlockPos pos, final BlockState state, final Player player) {
        final Level interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            this.bg.playerWillDestroy(interceptor, pos, this.asBg(state), player);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    public boolean dropFromExplosion(final Explosion explosion) {
        return this.bg.dropFromExplosion(explosion);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter getter, List<Component> list, TooltipFlag flag) {
        final int size = list.size();
        this.bg.appendHoverText(stack, getter, list, flag);

        if (size == list.size()) {
            this.fg.appendHoverText(stack, getter, list, flag);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction dir, BlockState facing, LevelAccessor level, BlockPos from, BlockPos to) {
        final LevelAccessor interceptor = this.primeRestricted(level, this.bg, from);
        try {
            if (facing.getBlock() instanceof OreVariant && ((OreVariant) facing.getBlock()).bg.equals(this.bg)) {
                facing = this.asBg(facing);
            }
            return this.fromOther(this.bg.updateShape(this.asBg(state), dir, facing, level, from, to));
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block facing, BlockPos at, boolean bl) {
        final Level interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            if (facing instanceof OreVariant && ((OreVariant) facing).bg.equals(this.bg)) {
                facing = this.bg;
            }
            this.bg.neighborChanged(this.asBg(state), level, pos, facing, at, bl);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState old, final boolean bl) {
        // Todo: debug what "old" actually is.
        Level interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            final boolean same = this == old.getBlock();
            final BlockState bgOld = same ? this.asBg(old) : old;
            this.bg.onPlace(this.asBg(state), interceptor, pos, bgOld, bl);

            interceptor = this.prime(level, this.fg);
            final BlockState fgOld = same ? this.asFg(old) : old;
            this.fg.onPlace(this.asFg(state), interceptor, pos, fgOld, bl);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(final BlockState state, final Level level, final BlockPos pos, final BlockState old, final boolean bl) {
        // todo: debug what "old" actually is.
        Level interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            final boolean same = this == old.getBlock();
            final BlockState bgOld = same ? this.asBg(old) : old;
            this.bg.onRemove(this.asBg(state), interceptor, pos, bgOld, bl);

            interceptor = this.prime(level, this.fg);
            final BlockState fgOld = same ? this.asFg(old) : old;
            this.fg.onRemove(this.asFg(state), interceptor, pos, fgOld, bl);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Level interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            final InteractionResult bgr = this.bg.use(this.asBg(state), interceptor, pos, player, hand, hit);

            interceptor = this.prime(level, this.fg);
            final InteractionResult fgr = this.fg.use(this.asFg(state), interceptor, pos, player, hand, hit);

            return bgr == InteractionResult.FAIL ? InteractionResult.FAIL : fgr;
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean triggerEvent(final BlockState state, final Level level, final BlockPos pos, final int i, final int j) {
        Level interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            final boolean bge = this.bg.triggerEvent(this.asBg(state), level, pos, i, j);

            interceptor = this.prime(level, this.fg);
            final boolean fge = this.fg.triggerEvent(this.asFg(state), level, pos, i, j);
            return bge || fge;
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(final BlockState state) {
        return this.bg.getRenderShape(this.asBg(state));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean useShapeForLightOcclusion(final BlockState state) {
        return this.bg.useShapeForLightOcclusion(this.asBg(state));
    }

    @Override
    @SuppressWarnings("deprecation")
    public PushReaction getPistonPushReaction(final BlockState state) {
        // There's a special exemption in PistonBlock.
        if (this.bg.equals(Blocks.OBSIDIAN)) {
            return PushReaction.BLOCK;
        }
        return this.bg.getPistonPushReaction(this.asBg(state));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSignalSource(final BlockState state) {
        return this.bg.isSignalSource(this.asBg(state));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return this.bg.hasAnalogOutputSignal(this.asBg(state));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(final BlockState state, final Rotation rotation) {
        return this.fromOther(this.bg.rotate(this.asBg(state), rotation));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(final BlockState state, final Mirror mirror) {
        return this.fromOther(this.bg.mirror(this.asBg(state), mirror));
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public VoxelShape getOcclusionShape(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bg.getOcclusionShape(this.asBg(state), getter, pos);
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(final BlockState state, final BlockGetter getter, final BlockPos pos, final CollisionContext ctx) {
        return this.bg.getShape(this.asBg(state), getter, pos, ctx);
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public VoxelShape getBlockSupportShape(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bg.getBlockSupportShape(this.asBg(state), getter, pos);
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public VoxelShape getInteractionShape(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bg.getInteractionShape(this.asBg(state), getter, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightBlock(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bg.getLightBlock(this.asBg(state), getter, pos);
    }

    @Nullable
    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public MenuProvider getMenuProvider(final BlockState state, final Level level, final BlockPos pos) {
        return this.bg.getMenuProvider(this.asBg(state), level, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        return this.bg.canSurvive(this.asBg(state), level, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getShadeBrightness(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bg.getShadeBrightness(this.asBg(state), getter, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getAnalogOutputSignal(final BlockState state, final Level level, final BlockPos pos) {
        return this.bg.getAnalogOutputSignal(this.asBg(state), level, pos);
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        return this.bg.getCollisionShape(this.asBg(state), getter, pos, ctx);
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public VoxelShape getVisualShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        return this.bg.getVisualShape(this.asBg(state), getter, pos, ctx);
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getDestroyProgress(final BlockState state, final Player player, final BlockGetter getter, final BlockPos pos) {
        return this.fg.getDestroyProgress(this.asFg(state), player, getter, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void spawnAfterBreak(final BlockState state, final ServerLevel server, final BlockPos pos, final ItemStack stack) {
        ServerLevel interceptor = this.primeRestricted(server, this.bg, pos);
        try {
            this.bg.spawnAfterBreak(this.asBg(state), interceptor, pos, stack);

            interceptor = this.prime(server, this.fg);
            this.fg.spawnAfterBreak(this.asFg(state), interceptor, pos, stack);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void attack(final BlockState state, final Level level, final BlockPos pos, final Player player) {
        Level interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            this.bg.attack(this.asBg(state), interceptor, pos, player);

            interceptor = this.prime(level, this.fg);
            this.fg.attack(this.asFg(state), interceptor, pos, player);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getSignal(final BlockState state, final BlockGetter getter, final BlockPos pos, final Direction dir) {
        final int bgs = this.bg.getSignal(this.asBg(state), getter, pos, dir);
        final int fgs = this.fg.getSignal(this.asFg(state), getter, pos, dir);
        return Math.max(bgs, fgs);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(final BlockState state, final Level level, final BlockPos pos, final Entity entity) {
        Level interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            this.bg.entityInside(this.asBg(state), interceptor, pos, entity);

            interceptor = this.prime(level, this.fg);
            this.fg.entityInside(this.asFg(state), interceptor, pos, entity);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getDirectSignal(final BlockState state, final BlockGetter getter, final BlockPos pos, final Direction dir) {
        final int bgs = this.bg.getDirectSignal(this.asBg(state), getter, pos, dir);
        final int fgs = this.fg.getDirectSignal(this.asFg(state), getter, pos, dir);
        return Math.max(bgs, fgs);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onProjectileHit(final Level level, final BlockState state, final BlockHitResult hit, final Projectile projectile) {
        this.bg.onProjectileHit(level, this.asBg(state), hit, projectile);
        this.fg.onProjectileHit(level, this.asFg(state), hit, projectile);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(final BlockState state, final ServerLevel server, final BlockPos pos, final Random rand) {
        ServerLevel interceptor = this.primeRestricted(server, this.bg, pos);
        try {
            this.bg.tick(this.asBg(state), interceptor, pos, rand);

            interceptor = this.prime(server, this.fg);
            this.fg.tick(this.asFg(state), interceptor, pos, rand);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(final BlockState state, final ServerLevel server, final BlockPos pos, final Random rand) {
        ServerLevel interceptor = this.primeRestricted(server, this.bg, pos);
        try {
            this.bg.randomTick(this.asBg(state), interceptor, pos, rand);

            interceptor = this.prime(server, this.fg);
            this.fg.randomTick(this.asFg(state), interceptor, pos, rand);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void animateTick(final BlockState state, final Level level, final BlockPos pos, final Random rand) {
        Level interceptor = this.primeRestricted(level, this.bg, pos);
        try {
            this.bg.animateTick(this.asBg(state), interceptor, pos, rand);

            interceptor = this.prime(level, this.fg);
            this.fg.animateTick(this.asFg(state), interceptor, pos, rand);
        } finally {
            InterceptorAccessor.dispose(interceptor);
        }
    }
}
