package personthecat.osv.block;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.StateArgumentPredicate;
import net.minecraft.world.level.block.state.BlockBehaviour.StatePredicate;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import personthecat.osv.mixin.BlockPropertiesAccessor;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.BlockSettings;
import personthecat.osv.preset.data.VariantSettings;
import personthecat.osv.preset.data.StateSettings;

import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class BlockPropertiesHelper {

    public static Properties merge(final OrePreset preset, final Block bg, final Block fg) {
        final Properties merged = Properties.of(Material.STONE);
        final Context ctx = new Context(preset, bg, fg);

        final BlockPropertiesAccessor accessor = (BlockPropertiesAccessor) merged;
        accessor.setMaterial(ctx.material());
        accessor.setHasCollision(ctx.hasCollision());
        accessor.setSoundType(ctx.soundType());
        accessor.setExplosionResistance(ctx.explosionResistance());
        accessor.setDestroyTime(ctx.destroyTime());
        accessor.setRequiresCorrectToolForDrops(ctx.requiresCorrectToolForDrops());
        accessor.setIsRandomlyTicking(ctx.isRandomlyTicking());
        accessor.setFriction(ctx.friction());
        accessor.setSpeedFactor(ctx.speedFactor());
        accessor.setJumpFactor(ctx.jumpFactor());
        accessor.setCanOcclude(ctx.canOcclude());
        accessor.setIsAir(ctx.isAir());
        accessor.setDynamicShape(ctx.dynamicShape());
        accessor.setMaterialColor(ctx.materialColor());
        accessor.setLightEmission(ctx.lightEmission());
        accessor.setIsValidSpawn(ctx.isValidSpawn());
        accessor.setIsSuffocating(ctx.isSuffocating());
        accessor.setIsViewBlocking(ctx.isViewBlocking());
        accessor.setHasPostProcess(ctx.hasPostProcess());
        accessor.setEmissiveRendering(ctx.emissiveRendering());

        preset.getPlatform().apply(merged);

        return merged;
    }

    private static class Context {
        final BlockPropertiesAccessor bgp;
        final BlockPropertiesAccessor fgp;
        final VariantSettings ore;
        final BlockSettings block;
        final StateSettings state;
        final Block bg;
        final Block fg;
        final boolean sameMaterial;

        Context(final OrePreset preset, final Block bg, final Block fg) {
            this.bgp = (BlockPropertiesAccessor) Properties.copy(bg);
            this.fgp = (BlockPropertiesAccessor) Properties.copy(fg);
            this.ore = preset.getVariant();
            this.block = preset.getBlock();
            this.state = preset.getState();
            this.bg = bg;
            this.fg = fg;
            this.sameMaterial = bgp.getMaterial().equals(this.fgp.getMaterial());
        }

        Material material() {
            if (this.block.getMaterial() != null) {
                return this.block.getMaterial();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getMaterial();
            }
            return this.fgp.getMaterial();
        }

        boolean hasCollision() {
            if (this.block.getHasCollision() != null) {
                return this.block.getHasCollision();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getHasCollision();
            }
            return this.fgp.getHasCollision();
        }

        SoundType soundType() {
            if (this.block.getSoundType() != null) {
                return this.block.getSoundType();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getSoundType();
            }
            return this.fgp.getSoundType();
        }

        float explosionResistance() {
            if (this.block.getExplosionResistance() != null) {
                return this.block.getExplosionResistance();
            } else if (this.ore.isBgImitation()) {
                return Math.max(this.bgp.getExplosionResistance(), this.fgp.getExplosionResistance());
            }
            return this.fgp.getExplosionResistance();
        }

        float destroyTime() {
            if (this.block.getDestroyTime() != null) {
                return this.block.getDestroyTime();
            } else if (this.ore.isBgImitation() && !this.sameMaterial) {
                final float bgTime = this.bgp.getDestroyTime();
                final float fgTime = this.fgp.getDestroyTime();
                return bgTime < 0 ? -1.0F : Math.max(bgTime + fgTime - 1.5F, 0.0F);
            }
            return this.fgp.getDestroyTime();
        }

        boolean requiresCorrectToolForDrops() {
            if (this.block.getRequiresCorrectToolForDrops() != null) {
                return this.block.getRequiresCorrectToolForDrops();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getRequiresCorrectToolForDrops() || this.fgp.getRequiresCorrectToolForDrops();
            }
            return this.fgp.getRequiresCorrectToolForDrops();
        }

        boolean isRandomlyTicking() {
            if (this.block.getIsRandomlyTicking() != null) {
                return this.block.getIsRandomlyTicking();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getIsRandomlyTicking() || this.fgp.getIsRandomlyTicking();
            }
            return this.fgp.getIsRandomlyTicking();
        }

        float friction() {
            if (this.block.getFriction() != null) {
                return this.block.getFriction();
            } else if (this.ore.isBgImitation() && !this.sameMaterial) {
                final float bgFriction = this.bgp.getFriction();
                final float fgFriction = this.fgp.getFriction();
                return (bgFriction + fgFriction) / 2.0F;
            }
            return this.fgp.getFriction();
        }

        float speedFactor() {
            if (this.block.getSpeedFactor() != null) {
                return this.block.getSpeedFactor();
            } else if (this.ore.isBgImitation() && !this.sameMaterial) {
                final float bgFactor = this.bgp.getSpeedFactor();
                final float fgFactor = this.fgp.getSpeedFactor();
                return (bgFactor + fgFactor + fgFactor) / 3.0F;
            }
            return this.fgp.getSpeedFactor();
        }

        float jumpFactor() {
            if (this.block.getJumpFactor() != null) {
                return this.block.getJumpFactor();
            } else if (this.ore.isBgImitation() && !this.sameMaterial) {
                final float bgFactor = this.bgp.getJumpFactor();
                final float fgFactor = this.fgp.getJumpFactor();
                return (bgFactor + fgFactor + fgFactor) / 3.0F;
            }
            return this.fgp.getJumpFactor();
        }

        boolean canOcclude() {
            if (this.block.getCanOcclude() != null) {
                return this.block.getCanOcclude();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getCanOcclude() && this.fgp.getCanOcclude();
            }
            return this.fgp.getCanOcclude();
        }

        boolean isAir() {
            if (this.block.getIsAir() != null) {
                return this.block.getIsAir();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getIsAir() || this.fgp.getIsAir();
            }
            return this.fgp.getIsAir();
        }

        boolean dynamicShape() {
            if (this.block.getDynamicShape() != null) {
                return this.block.getDynamicShape();
            } else if (this.ore.isBgImitation()) {
                return this.bgp.getDynamicShape() || this.fgp.getDynamicShape();
            }
            return this.fgp.getDynamicShape();
        }

        Function<BlockState, MaterialColor> materialColor() {
            if (this.state.getMaterialColor() != null) {
                return this.state.getMaterialColor().createFunction();
            } else if (this.ore.isBgImitation()) {
                return translateFunction(this.bgp.getMaterialColor(), this.bg);
            }
            return translateFunction(this.fgp.getMaterialColor(), this.fg);
        }

        ToIntFunction<BlockState> lightEmission() {
            if (this.state.getLightEmission() != null) {
                return this.state.getLightEmission().createFunction()::apply;
            } else if (this.ore.isBgImitation()) {
                return translateToInt(this.bgp.getLightEmission(), this.bg);
            }
            return translateToInt(this.fgp.getLightEmission(), this.fg);
        }

        StateArgumentPredicate<EntityType<?>> isValidSpawn() {
            if (this.state.getIsValidSpawn() != null) {
                final Function<BlockState, Set<EntityType<?>>> entities = this.state.getIsValidSpawn().createFunction();
                return (state, block, pos, entity) -> entities.apply(state).contains(entity);
            } else if (this.ore.isBgImitation()) {
                return translateArgument(this.bgp.getIsValidSpawn(), this.bg);
            }
            return translateArgument(this.fgp.getIsValidSpawn(), this.fg);
        }

        StatePredicate isSuffocating() {
            if (this.state.getIsSuffocating() != null) {
                final Function<BlockState, Boolean> predicate = this.state.getIsSuffocating().createFunction();
                return (state, block, pos) -> predicate.apply(state);
            } else if (this.ore.isBgImitation()) {
                return translatePredicate(this.bgp.getIsSuffocating(), this.bg);
            }
            return translatePredicate(this.fgp.getIsSuffocating(), this.fg);
        }

        StatePredicate isViewBlocking() {
            if (this.state.getIsViewBlocking() != null) {
                final Function<BlockState, Boolean> predicate = this.state.getIsViewBlocking().createFunction();
                return (state, block, pos) -> predicate.apply(state);
            } else if (this.ore.isBgImitation()) {
                return translatePredicate(this.bgp.getIsViewBlocking(), this.bg);
            }
            return translatePredicate(this.fgp.getIsViewBlocking(), this.fg);
        }

        StatePredicate hasPostProcess() {
            if (this.state.getHasPostProcess() != null) {
                final Function<BlockState, Boolean> predicate = this.state.getHasPostProcess().createFunction();
                return (state, block, pos) -> predicate.apply(state);
            }
            return translatePredicate(this.fgp.getHasPostProcess(), this.fg);
        }

        StatePredicate emissiveRendering() {
            if (this.state.getEmissiveRendering() != null) {
                final Function<BlockState, Boolean> predicate = this.state.getEmissiveRendering().createFunction();
                return (state, block, pos) -> predicate.apply(state);
            }
            return translatePredicate(this.fgp.getEmissiveRendering(), this.fg);
        }

        static <T> Function<BlockState, T> translateFunction(final Function<BlockState, T> function, final Block to) {
            return state -> function.apply(translateState(state, to));
        }

        static <T> StateArgumentPredicate<T> translateArgument(final StateArgumentPredicate<T> predicate, final Block to) {
            return (state, block, pos, t) -> predicate.test(translateState(state, to), block, pos, t);
        }

        static ToIntFunction<BlockState> translateToInt(final ToIntFunction<BlockState> toInt, final Block to) {
            return state -> toInt.applyAsInt(translateState(state, to));
        }

        static StatePredicate translatePredicate(final StatePredicate predicate, final Block to) {
            return (state, block, pos) -> predicate.test(translateState(state, to), block, pos);
        }

        static BlockState translateState(final BlockState actual, final Block to) {
            final Block b = actual.getBlock();
            if (!(b instanceof SharedStateBlock)) {
                throw new IllegalStateException("Unexpected block: " + b);
            }
            final SharedStateBlock shared = (SharedStateBlock) b;
            return shared.toEither(actual, to);
        }
    }
}
