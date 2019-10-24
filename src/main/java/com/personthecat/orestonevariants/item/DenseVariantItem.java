package com.personthecat.orestonevariants.item;

import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class DenseVariantItem extends VariantItem {
    public DenseVariantItem(Block block) {
        super(block, new Item.Properties().group(DenseVariantGroup.GROUP), osvLocation("dense_" + block.getRegistryName().getPath()));
    }

    @Override
    protected BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return getBlock().getDefaultState().with(BaseOreVariant.DENSE, true);
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return new StringTextComponent("Dense ")
            .appendSibling(super.getDisplayName(stack));
    }
}