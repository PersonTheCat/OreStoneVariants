package com.personthecat.orestonevariants.item;

import com.personthecat.orestonevariants.blocks.OreVariant;
import mcp.MethodsReturnNonnullByDefault;
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
import org.jetbrains.annotations.NotNull;

@MethodsReturnNonnullByDefault
public class VariantItem extends BlockItem {
    private final String bgKey = getBackgroundKey();

    public VariantItem(OreVariant block) {
        this(block, new Item.Properties().group(VariantGroup.GROUP), block.getRegistryName());
    }

    VariantItem(OreVariant block, Item.Properties properties, ResourceLocation name) {
        super(block, properties);
        setRegistryName(name);
    }

    public BlockState getVariant() {
        return getBlock().getDefaultState();
    }

    public BlockState getBg() {
        return ((OreVariant) getBlock()).bgState;
    }

    public boolean isDense() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName(@NotNull ItemStack stack) {
        return asFormattable(super.getDisplayName(stack))
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
        return ((OreVariant) getBlock()).bgState.getBlock().getTranslationKey();
    }

    @Override
    public void fillItemGroup(@NotNull ItemGroup group, @NotNull NonNullList<ItemStack> items) {
        // Manually doing this so that items get filled with the correct alternatives.
        if (isInGroup(group)) {
            items.add(new ItemStack(this));
        }
    }
}