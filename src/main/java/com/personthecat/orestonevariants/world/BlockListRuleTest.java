package com.personthecat.orestonevariants.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.template.IRuleTestType;
import net.minecraft.world.gen.feature.template.RuleTest;
import org.hjson.JsonArray;
import org.hjson.JsonValue;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.personthecat.orestonevariants.util.CommonMethods.getGuaranteedState;

@AllArgsConstructor
@ParametersAreNonnullByDefault
public class BlockListRuleTest extends RuleTest {

    private static final Codec<BlockListRuleTest> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(Codec.list(BlockState.CODEC).fieldOf("source").forGetter(rule -> rule.blocks))
            .apply(instance, BlockListRuleTest::new));

    private static final IRuleTestType<BlockListRuleTest> INSTANCE =
        Registry.register(Registry.RULE_TEST, "osv:block_list_rule_test", () -> CODEC);

    private final List<BlockState> blocks;

    public static BlockListRuleTest from(JsonArray array) {
        final List<BlockState> states = new ArrayList<>();
        for (JsonValue value : array) {
            states.add(getGuaranteedState(value.asString()));
        }
        if (states.isEmpty()) { // Default value
            states.add(Blocks.STONE.getDefaultState());
        }
        return new BlockListRuleTest(states);
    }

    @Override
    public boolean test(BlockState state, Random rand) {
        return blocks.contains(state);
    }

    @NotNull
    @Override
    protected IRuleTestType<?> getType() {
        return INSTANCE;
    }
}
