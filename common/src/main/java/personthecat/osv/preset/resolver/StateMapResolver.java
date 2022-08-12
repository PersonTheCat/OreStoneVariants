package personthecat.osv.preset.resolver;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.StateArgumentPredicate;
import net.minecraft.world.level.block.state.BlockBehaviour.StatePredicate;
import net.minecraft.world.level.material.MaterialColor;
import personthecat.catlib.registry.CommonRegistries;
import personthecat.osv.util.StateMap;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;


public final class StateMapResolver {

    public static StateMap<MaterialColor> resolveMaterialColor(
            final Function<BlockState, MaterialColor> function,
            final Block block) {
        final StateMap<MaterialColor> map = new StateMap<>();
        for (final BlockState state : block.getStateDefinition().getPossibleStates()) {
            map.put(StateMap.toString(state), function.apply(state));
        }
        return map;
    }

    public static StateMap<Integer> resolveInteger(
            final ToIntFunction<BlockState> function,
            final Block block) {
        final StateMap<Integer> map = new StateMap<>();
        for (final BlockState state : block.getStateDefinition().getPossibleStates()) {
            map.put(StateMap.toString(state), function.applyAsInt(state));
        }
        return map;
    }

    public static StateMap<Set<EntityType<?>>> resolveEntityTypes(
            final StateArgumentPredicate<EntityType<?>> function,
            final Level level,
            final BlockPos pos,
            final Block block) {
        final StateMap<Set<EntityType<?>>> map = new StateMap<>();
        for (final BlockState state : block.getStateDefinition().getPossibleStates()) {
            final Set<EntityType<?>> types = new HashSet<>();
            for (final EntityType<?> type : CommonRegistries.ENTITIES) {
                if (function.test(state, level, pos, type)) {
                    types.add(type);
                }
            }
            map.put(StateMap.toString(state), types);
        }
        return map;
    }

    public static StateMap<Boolean> resolveBoolean(
            final StatePredicate function,
            final Level level,
            final BlockPos pos,
            final Block block) {
        final StateMap<Boolean> map = new StateMap<>();
        for (final BlockState state : block.getStateDefinition().getPossibleStates()) {
            map.put(StateMap.toString(state), function.test(state, level, pos));
        }
        return map;
    }
}
