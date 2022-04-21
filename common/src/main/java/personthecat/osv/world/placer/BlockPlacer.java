package personthecat.osv.world.placer;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Random;

import static personthecat.catlib.serialization.codec.CodecUtils.asParent;
import static personthecat.catlib.serialization.codec.CodecUtils.simpleEither;

public interface BlockPlacer {

    Codec<BlockPlacer> EITHER_CODEC =
        simpleEither(asParent(StoneBlockPlacer.CODEC), asParent(VariantBlockPlacer.CODEC))
            .withEncoder(p -> p instanceof StoneBlockPlacer ? asParent(StoneBlockPlacer.CODEC)
                : asParent(VariantBlockPlacer.CODEC));

    boolean place(final WorldGenLevel level, final Random rand, final BlockPos pos);
    boolean placeUnchecked(final ChunkAccess chunk, final Random rand, final int x, final int y, final int z);
    int getId();
}
