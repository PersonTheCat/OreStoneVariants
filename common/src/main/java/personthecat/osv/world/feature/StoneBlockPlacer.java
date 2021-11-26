package personthecat.osv.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import personthecat.catlib.serialization.EasyStateCodec;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.preset.reader.RuleTestReader;
import personthecat.osv.world.rule.BlockSetRuleTest;

import java.util.Random;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;
import static personthecat.catlib.serialization.FieldDescriptor.field;

public class StoneBlockPlacer implements BlockPlacer {

    public static final Codec<StoneBlockPlacer> CODEC = codecOf(
        field(EasyStateCodec.INSTANCE, "state", c -> c.state),
        defaulted(RuleTestReader.CODEC, "source", BlockSetRuleTest.STONE_ONLY, c -> c.source),
        StoneBlockPlacer::new
    );

    final BlockState state;
    final RuleTest source;

    public StoneBlockPlacer(final StonePreset preset) {
        this(preset.getStone(), preset.getSource());
    }

    public StoneBlockPlacer(final BlockState state, final RuleTest source) {
        this.state = state;
        this.source = source;
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
}
