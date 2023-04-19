package personthecat.osv.block.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ToolAction;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.data.Range;
import personthecat.osv.block.AdditionalProperties;
import personthecat.osv.block.OreVariant;
import personthecat.osv.block.StateConfig;
import personthecat.osv.preset.OrePreset;

public class ForgeVariant extends OreVariant {

    public ForgeVariant(final OrePreset preset, final Properties properties, final StateConfig config) {
        super(preset, properties, config);
    }

    @Override
    public boolean isLadder(final BlockState state, final LevelReader level, final BlockPos pos, final LivingEntity entity) {
        return this.bg.isLadder(this.asBg(state), level, pos, entity);
    }

    @Override
    public boolean isBurning(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bg.isBurning(this.asBg(state), getter, pos);
    }

//    @Override
//    public boolean hasTileEntity(final BlockState state) {
//        return this.bg.hasTileEntity(this.asBg(state));
//    }
//
//    @Nullable
//    @Override
//    public BlockEntity createTileEntity(final BlockState state, final BlockGetter getter) {
//        return this.bg.createTileEntity(this.asBg(state), getter);
//    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter getter, BlockPos pos, Player player) {
        final ItemStack actual = new ItemStack(this.asItem(state));
        if (player.isCreative() || player.getInventory().contains(actual)) {
            return actual;
        }
        final ItemStack ore = new ItemStack(this.fg);
        if (player.getInventory().contains(ore)) {
            return ore;
        }
        final ItemStack block = new ItemStack(this.bg);
        if (player.getInventory().contains(block)) {
            return block;
        }
        return actual;
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int count) {
        final boolean bge = this.bg.addLandingEffects(this.asBg(state1), level, pos, state2, entity, count);
        final boolean fge = this.fg.addLandingEffects(this.asFg(state1), level, pos, state2, entity, count);
        return bge || fge;
    }

    @Override
    public boolean addRunningEffects(final BlockState state, final Level level, final BlockPos pos, final Entity entity) {
        final boolean bge = this.bg.addRunningEffects(this.asBg(state), level, pos, entity);
        final boolean fge = this.fg.addRunningEffects(this.asFg(state), level, pos, entity);
        return bge || fge;
    }

//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public boolean addHitEffects(final BlockState state, final Level level, final HitResult target, final ParticleEngine manager) {
//        this.bg.land
//        final boolean bge = this.bg.addHitEffects(this.asBg(state), level, target, manager);
//        final boolean fge = this.fg.addHitEffects(this.asFg(state), level, target, manager);
//        return bge || fge;
//    }

//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public boolean addDestroyEffects(final BlockState state, final Level level, final BlockPos pos, final ParticleEngine manager) {
//        final boolean bge = this.bg.addDestroyEffects(this.asBg(state), level, pos, manager);this.bg.destro
//        final boolean fge = this.fg.addDestroyEffects(this.asFg(state), level, pos, manager);
//        return bge || fge;
//    }

//    @Override
//    public void onPlantGrow(final BlockState state, final LevelAccessor level, final BlockPos pos, final BlockPos source) {
//        this.bg.onPlantGrow(this.asBg(state), level, pos, source);
//    }

    @Override
    public boolean isFertile(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bg.isFertile(this.asBg(state), getter, pos);
    }

    @Override
    public boolean isConduitFrame(final BlockState state, final LevelReader level, final BlockPos pos, final BlockPos conduit) {
        return this.bg.isConduitFrame(this.asBg(state), level, pos, conduit);
    }

    @Override
    public boolean isPortalFrame(final BlockState state, final BlockGetter getter, final BlockPos pos) {
        return this.bg.isPortalFrame(this.asBg(state), getter, pos);
    }

    @Override
    public int getExpDrop(final BlockState state, final LevelReader level, final BlockPos pos, final int fortune, final int silkTouch) {
        final int count;
        final Range xp = this.preset.getVariant().getXp();
        if (xp != null && level instanceof Level) {
            count = xp.rand(((Level) level).getRandom());
        } else {
            final int bgx = this.bg.getExpDrop(this.asBg(state), level, pos, fortune, silkTouch);
            final int fgx = this.fg.getExpDrop(this.asFg(state), level, pos, fortune, silkTouch);
            count = bgx + fgx;
        }
        return AdditionalProperties.isDense(state) ? count * 2 : count;
    }

    @Override
    public float getEnchantPowerBonus(final BlockState state, final LevelReader level, final BlockPos pos) {
        final float bgp = this.bg.getEnchantPowerBonus(this.asBg(state), level, pos);
        final float fgp = this.fg.getEnchantPowerBonus(this.asFg(state), level, pos);
        return bgp + fgp;
    }

//    @Override
//    public boolean isToolEffective(final BlockState state, final ToolType tool) {
//        if (((PlatformBlockSettingsImpl) this.preset.getPlatform()).getHarvestTool() != null) {
//            return super.isToolEffective(state, tool);this.bg.getToolModifiedState(\)
//        }
//
//        final boolean bgt = this.bg.isToolEffective(this.asBg(state), tool);
//        final boolean fgt = this.fg.isToolEffective(this.asFg(state), tool);
//        return bgt && fgt;
//    }

    @Override
    public boolean isSlimeBlock(final BlockState state) {
        return this.bg.isSlimeBlock(this.asBg(state));
    }

    @Override
    public boolean isStickyBlock(final BlockState state) {
        return this.bg.isStickyBlock(this.asBg(state));
    }

    @Override
    public boolean canStickTo(final BlockState state, final BlockState other) {
        return this.bg.canStickTo(this.asBg(state), other);
    }

    @Override
    public int getFlammability(final BlockState state, final BlockGetter getter, final BlockPos pos, final Direction dir) {
        return this.bg.getFlammability(this.asBg(state), getter, pos, dir);
    }

    @Override
    public boolean isFlammable(final BlockState state, final BlockGetter getter, final BlockPos pos, final Direction dir) {
        return this.bg.isFlammable(this.asBg(state), getter, pos, dir);
    }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction dir, @Nullable LivingEntity igniter) {
        this.bg.onCaughtFire(this.asBg(state), level, pos, dir, igniter);
    }

    @Override
    public int getFireSpreadSpeed(final BlockState state, final BlockGetter getter, final BlockPos pos, final Direction dir) {
        return this.bg.getFireSpreadSpeed(this.asBg(state), getter, pos, dir);
    }

    @Override
    public boolean isFireSource(final BlockState state, final LevelReader level, final BlockPos pos, final Direction dir) {
        return this.bg.isFireSource(this.asBg(state), level, pos, dir);
    }

    @Override
    public boolean canEntityDestroy(final BlockState state, final BlockGetter getter, final BlockPos pos, final Entity entity) {
        final boolean bgd = this.bg.canEntityDestroy(this.asBg(state), getter, pos, entity);
        final boolean fgd = this.fg.canEntityDestroy(this.asFg(state), getter, pos, entity);
        return bgd && fgd;
    }

    @Override
    public boolean canDropFromExplosion(final BlockState state, final BlockGetter getter, final BlockPos pos, final Explosion explosion) {
        final boolean bgd = this.bg.canDropFromExplosion(this.asBg(state), getter, pos, explosion);
        final boolean fgd = this.fg.canDropFromExplosion(this.asFg(state), getter, pos, explosion);
        return bgd && fgd;
    }

    @Override
    public void onBlockExploded(final BlockState state, final Level level, final BlockPos pos, final Explosion explosion) {
        this.bg.onBlockExploded(this.asBg(state), level, pos, explosion);
        this.fg.onBlockExploded(this.asFg(state), level, pos, explosion);
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter level, BlockPos pos, FluidState fluid) {
        return this.bg.shouldDisplayFluidOverlay(state, level, pos, fluid);
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, Level level, BlockPos pos, Player player, ItemStack stack, ToolAction toolAction) {
        return copyInto(
            this.defaultBlockState(),
            this.bg.getToolModifiedState(this.asBg(state), level, pos, player, stack, toolAction),
            this.fg.getToolModifiedState(this.asFg(state), level, pos, player, stack, toolAction));
    }

    @Override
    public boolean collisionExtendsVertically(BlockState state, BlockGetter getter, BlockPos pos, Entity colliding) {
        return this.bg.collisionExtendsVertically(this.asBg(state), getter, pos, colliding);
    }

    @Override
    public boolean isScaffolding(final BlockState state, final LevelReader level, final BlockPos pos, final LivingEntity entity) {
        return this.bg.isScaffolding(this.asBg(state), level, pos, entity);
    }
}
