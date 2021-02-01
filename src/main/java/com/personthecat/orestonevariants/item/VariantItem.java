package com.personthecat.orestonevariants.item;

import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class VariantItem extends BlockItem {
    private final String bgKey = getBackgroundKey();

    public VariantItem(BaseOreVariant block) {
        this(block, new Item.Properties().group(VariantGroup.GROUP), block.getRegistryName());
    }

    VariantItem(BaseOreVariant block, Item.Properties properties, ResourceLocation name) {
        super(block, properties);
        setRegistryName(name);
    }

    public BlockState getOre() {
        return getBlock().getDefaultState();
    }

    public BlockState getBg() {
        return ((BaseOreVariant) getBlock()).bgState;
    }

    public boolean isDense() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        ITextComponent name = super.getDisplayName(stack);

        return asFormattable(name)
            .appendString(" (")
            .append(new TranslationTextComponent(bgKey))
            .appendString(")");
    }

    private IFormattableTextComponent asFormattable(ITextComponent text) {
        if (text instanceof IFormattableTextComponent) {
            return (IFormattableTextComponent) text;
        }
        return new StringTextComponent(text.getString());
    }

    private String getBackgroundKey() {
        return ((BaseOreVariant) getBlock()).bgState.getBlock().getTranslationKey();
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        // Manually doing this so that items get filled with the correct alternatives.
        if (isInGroup(group)) {
            items.add(new ItemStack(this));
        }
    }
}