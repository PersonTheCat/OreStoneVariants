package personthecat.osv.world.placement;

import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

public interface NoneDecoratorProvider<T> extends PlacementProvider<T> {

    @Override
    default List<PlacementModifier> getModifiers() {
        return List.of();
    }
}
