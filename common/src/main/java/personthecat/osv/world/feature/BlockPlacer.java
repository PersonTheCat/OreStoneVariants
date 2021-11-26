package personthecat.osv.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

import java.util.Random;

import static personthecat.catlib.serialization.CodecUtils.asParent;
import static personthecat.catlib.serialization.CodecUtils.simpleEither;

public interface BlockPlacer {

    Codec<BlockPlacer> EITHER_CODEC =
        simpleEither(asParent(StoneBlockPlacer.CODEC), asParent(VariantBlockPlacer.CODEC))
            .withEncoder(p -> p instanceof StoneBlockPlacer ? asParent(StoneBlockPlacer.CODEC)
                : asParent(VariantBlockPlacer.CODEC));

    boolean place(final WorldGenLevel level, final Random rand, final BlockPos pos);
    int getId();
}
