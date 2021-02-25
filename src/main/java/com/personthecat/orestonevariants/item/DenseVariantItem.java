package com.personthecat.orestonevariants.item;

import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.config.Cfg;
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

    @Override
    public boolean isDense() {
        return true;
    }

    private static Item.Properties getProperties() {
        return Cfg.denseOres.get() ? new Item.Properties().group(DenseVariantGroup.GROUP)
            : new Item.Properties();
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return new TranslationTextComponent("osv.denseKey")
            .appendString(" ")
            .append(super.getDisplayName(stack));
    }
}