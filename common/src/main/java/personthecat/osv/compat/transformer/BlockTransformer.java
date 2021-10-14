package personthecat.osv.compat.transformer;

import personthecat.catlib.util.JsonTransformer;
import personthecat.catlib.util.JsonTransformer.ObjectResolver;
import personthecat.osv.preset.data.BlockSettings;
import personthecat.osv.preset.data.OreSettings;

public class BlockTransformer {
    public static final ObjectResolver INSTANCE =
        JsonTransformer.withPath(OreSettings.Fields.block)
            .history("blocksMovement", BlockSettings.Fields.hasCollision)
            .history("resistance", BlockSettings.Fields.explosionResistance)
            .history("hardness", BlockSettings.Fields.destroyTime)
            .history("requiresTool", BlockSettings.Fields.requiresCorrectToolForDrops)
            .history("ticksRandomly", BlockSettings.Fields.isRandomlyTicking)
            .history("slipperiness", BlockSettings.Fields.friction)
            .history("isSolid", BlockSettings.Fields.canOcclude)
            .freeze();
}
