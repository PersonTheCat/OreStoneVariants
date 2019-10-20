package com.personthecat.orestonevariants.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Temporary variant of BlockItem that can be used for individual
 * BlockStates. This is necessary for now due to ItemStacks no
 * longer being able to hold meta values / different states.
 * Seriously not a fan.
 *
 * Unused: delete me
 */
public class BlockStateItem extends BlockItem {
    private final String bgKey = getBackgroundKey();

    public BlockStateItem(Block block, Item.Properties properties) {
        super(block, properties);
        setRegistryName(block.getRegistryName());
    }

    @Override
    protected BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return getState(ctx.getItem());
    }

    private BlockState getState(ItemStack stack) {
        try {
            return new BlockStateParser(getReader(stack), false).parse(true).getState();
        } catch (CommandSyntaxException e) {
            return getBlock().getDefaultState();
        }
    }

    private StringReader getReader(ItemStack stack) {
        final CompoundNBT tag = stack.getTag();
        final String id = tag == null ? "" : getId(tag);
        //info("got this id \"{}\"", id);
        return new StringReader(getRegistryName().toString() + id);
    }

    private static String getId(CompoundNBT tag) {
        return tag.toString()
            .replace('{', '[')
            .replace('}', ']')
            .replace(':', '=')
            .replace("\"", "")
            .replace(" ", "");
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        ITextComponent name = super.getDisplayName(stack);
        if (getState(stack).get(BaseOreVariant.DENSE)) {
            name = new StringTextComponent("Dense ").appendSibling(name);
        }
        return name.appendText(" (")
            .appendSibling(new TranslationTextComponent(bgKey))
            .appendText(")");
    }

    private String getBackgroundKey() {
        return ((BaseOreVariant) getBlock()).bgBlock.getBlock().getTranslationKey();
    }
}