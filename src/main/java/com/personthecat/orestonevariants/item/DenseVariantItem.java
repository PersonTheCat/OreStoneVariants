package com.personthecat.orestonevariants.item;

import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class DenseVariantItem extends VariantItem {
    public DenseVariantItem(Block block) {
        super(block, getProperties(), osvLocation("dense_" + block.getRegistryName().getPath()));
    }

    private static Item.Properties getProperties() {
        return Cfg.denseOres.get() ? new Item.Properties().group(DenseVariantGroup.GROUP)
            : new Item.Properties();
    }

    @Override
    protected BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return getBlock().getDefaultState().with(BaseOreVariant.DENSE, true);
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return new TranslationTextComponent("osv.denseKey")
            .appendText(" ")
            .appendSibling(super.getDisplayName(stack));
    }
}