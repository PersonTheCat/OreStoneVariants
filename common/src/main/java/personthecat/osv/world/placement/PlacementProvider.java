package personthecat.osv.world.placement;

import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import personthecat.osv.preset.data.DynamicSerializable;

import java.util.List;

public interface PlacementProvider<T> extends DynamicSerializable<T> {
    List<PlacementModifier> getModifiers();
}
