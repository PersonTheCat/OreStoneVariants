package personthecat.osv.preset.reader;

import net.minecraft.world.level.levelgen.VerticalAnchor;

public interface CommonHeightAccessor {
    VerticalAnchor getMinInclusive();
    VerticalAnchor getMaxInclusive();

    default int getPlateau() {
        return -1;
    }
}
