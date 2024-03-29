package personthecat.osv.world.placer;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import personthecat.catlib.serialization.codec.EasyStateCodec;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.preset.reader.RuleTestReader;
import personthecat.osv.world.rule.BlockSetRuleTest;

import java.util.Random;

import static personthecat.catlib.serialization.codec.CodecUtils.codecOf;
import static personthecat.catlib.serialization.codec.FieldDescriptor.defaulted;
import static personthecat.catlib.serialization.codec.FieldDescriptor.field;

public class StoneBlockPlacer implements BlockPlacer {

    public static final Codec<StoneBlockPlacer> CODEC = codecOf(
        field(EasyStateCodec.INSTANCE, "state", c -> c.state),
        defaulted(RuleTestReader.CODEC, "source", BlockSetRuleTest.STONE_ONLY, c -> c.source),
        StoneBlockPlacer::new
    );

    final BlockState state;
    final RuleTest source;
    final int id;

    public StoneBlockPlacer(final StonePreset preset) {
        this(preset.getStone(), preset.getSource());
    }

    public StoneBlockPlacer(final BlockState state, final RuleTest source) {
        this.state = state;
        this.source = source;
        this.id = Block.getId(state);
    }

    @Override
    public boolean place(final WorldGenLevel level, final Random rand, final BlockPos pos) {
        final BlockState bg = level.getBlockState(pos);
        if (this.source.test(bg, rand)) {
            level.setBlock(pos, this.state, 2);
            return true;
        }
        return false;
    }

    @Override
    public boolean placeUnchecked(final ChunkAccess chunk, final Random rand, int x, int y, int z) {
        final int i = y >> 4;
        final LevelChunkSection section = chunk.getSections()[i];
        if (section == null) {
            return false;
        }
        x &= 15;
        y &= 15;
        z &= 15;
        final BlockState bg = section.getBlockState(x, y, z);
        if (this.source.test(bg, rand)) {
            section.setBlockState(x, y, z, this.state, false);
        }
        return false;
    }

    @Override
    public int getId() {
        return this.id;
    }
}
