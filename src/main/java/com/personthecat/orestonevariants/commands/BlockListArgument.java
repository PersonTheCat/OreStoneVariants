package com.personthecat.orestonevariants.commands;

import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;

import javax.annotation.Nullable;

public class BlockListArgument extends ListArgument<BlockStateInput, BlockStateArgument> {

    public static void register() {
        ArgumentTypes.register("osv:blocks_in", BlockListArgument.class, new ArgumentSerializer<>(BlockListArgument::blocksInArgument));
    }

    public BlockListArgument(@Nullable String terminator) {
        super(BlockStateArgument.blockState(), terminator);
    }

    public static BlockListArgument blocksInArgument() {
        return new BlockListArgument("in");
    }
}
