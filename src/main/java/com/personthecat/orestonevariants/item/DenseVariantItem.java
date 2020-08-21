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

import static com.personthecat.orestonevariants.util.CommonMethods.osvLocation;

public class DenseVariantItem extends VariantItem {
    public DenseVariantItem(BaseOreVariant block) {
        super(block, getProperties(), osvLocation("dense_" + block.getRegistryName().getPath()));
    }

    @Override
    public BlockState getOre() {
        return getBlock().getDefaultState().with(BaseOreVariant.DENSE, true);
    }

    private static Item.Properties getProperties() {
        return Cfg.denseOres.get() ? new Item.Properties().group(DenseVariantGroup.GROUP)
            : new Item.Properties();
    }

    @Override
    protected BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return getOre();
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return new TranslationTextComponent("osv.denseKey")
            .func_240702_b_(" ")
            .func_230529_a_(super.getDisplayName(stack));
    }
}