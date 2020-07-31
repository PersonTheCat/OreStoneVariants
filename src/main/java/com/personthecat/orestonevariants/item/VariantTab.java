package com.personthecat.orestonevariants.item;

import com.personthecat.orestonevariants.Main;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import static com.personthecat.orestonevariants.util.CommonMethods.find;

public class VariantTab extends CreativeTabs {
    public static VariantTab TAB = new VariantTab();

    private VariantTab() {
        super("variants");
    }

    @Override
    public ItemStack createIcon() {
        final Block block = find(Main.BLOCKS, b ->
                b.getRegistryName().getPath().equals("coal_ore_3"))
            .orElseGet(() -> Main.BLOCKS.iterator().next());
        return new ItemStack(block);
    }
}
