package com.personthecat.orestonevariants.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;

/*
 * This class is hopefully temporary and won't make it to release.
 * It is essentially a fix for a bug surrounding the imitation
 * handler in BaseOreVariant, which will ultimately need to be
 * removed in 1.16.
 */
public class SharedStateBlock extends Block {
    private static ThreadLocal<Block> hack = new ThreadLocal<>();

    public SharedStateBlock(Block b, Properties properties) {
        super(updateBlock(b, properties));
        setDefaultState(b.getDefaultState());
    }

    private static synchronized Properties updateBlock(Block b, Properties properties) {
        hack.set(b);
        return properties;
    }

    @Override
    protected synchronized void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        hack.get().getStateContainer().getProperties().forEach(builder::add);
    }
}