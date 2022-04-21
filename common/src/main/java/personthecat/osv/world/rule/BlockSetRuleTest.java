package personthecat.osv.world.rule;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import personthecat.catlib.data.collections.InvertibleSet;
import personthecat.catlib.serialization.codec.EasyStateCodec;
import personthecat.osv.util.Reference;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

import static personthecat.catlib.serialization.codec.CodecUtils.easySet;

@ParametersAreNonnullByDefault
public class BlockSetRuleTest extends RuleTest {

    public static final Codec<BlockSetRuleTest> CODEC = easySet(EasyStateCodec.INSTANCE)
        .xmap(BlockSetRuleTest::new, rule -> rule.blocks);

    public static final RuleTestType<BlockSetRuleTest> INSTANCE =
        Registry.register(Registry.RULE_TEST, Reference.MOD_ID + ":block_set_rule_test", () -> CODEC);

    public static final BlockSetRuleTest STONE_ONLY =
        new BlockSetRuleTest(Collections.singleton(Blocks.STONE.defaultBlockState()));

    private final Set<BlockState> blocks;

    public BlockSetRuleTest(final Set<BlockState> blocks) {
        this.blocks = new InvertibleSet<>(blocks, false).optimize(Collections.emptySet());
    }

    @Override
    public boolean test(final BlockState state, final Random rand) {
        return this.blocks.contains(state);
    }

    @Override
    protected RuleTestType<?> getType() {
        return INSTANCE;
    }
}
