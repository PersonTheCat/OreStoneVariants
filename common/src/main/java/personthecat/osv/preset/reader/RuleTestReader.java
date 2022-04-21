package personthecat.osv.preset.reader;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import personthecat.osv.world.rule.BlockSetRuleTest;

import static personthecat.catlib.serialization.codec.CodecUtils.asParent;
import static personthecat.catlib.serialization.codec.CodecUtils.simpleEither;

public class RuleTestReader {
    public static final Codec<RuleTest> CODEC = simpleEither(asParent(BlockSetRuleTest.CODEC), RuleTest.CODEC)
        .withEncoder(rule -> rule instanceof BlockSetRuleTest ? asParent(BlockSetRuleTest.CODEC) : RuleTest.CODEC);
}
