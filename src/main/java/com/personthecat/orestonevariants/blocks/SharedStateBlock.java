package com.personthecat.orestonevariants.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import org.jetbrains.annotations.NotNull;

/*
 * This class is hopefully temporary and won't make it to release.
 * It is essentially a fix for a bug surrounding the imitation
 * handler in BaseOreVariant, which will ultimately need to be
 * removed in 1.16.
 *
 * Update: On second thought, as much as this is intended to
 * circumvent language design and years of convention, it would
 * be extremely useful in letting variants actually take on all
 * of the states contained by their original ores / backgrounds.
 * Ores created in oak_log would simultaneously and automatically
 * support all possible orientations. I'm leaving this here in the
 * event that something good does come from it, bearing in mind
 * that it does work as it stands.
 */
public class SharedStateBlock extends Block {
    private static ThreadLocal<Block> shared = new ThreadLocal<>();

    SharedStateBlock(Block b, Properties properties) {
        super(updateBlock(b, properties));
        setDefaultState(b.getDefaultState());
    }

    private static synchronized Properties updateBlock(Block b, Properties properties) {
        shared.set(b);
        return properties;
    }

    @Override
    protected synchronized void fillStateContainer(@NotNull StateContainer.Builder<Block, BlockState> builder) {
        shared.get().getStateContainer().getProperties().forEach(builder::add);
    }
}