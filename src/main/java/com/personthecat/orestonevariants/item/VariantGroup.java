package com.personthecat.orestonevariants.item;

import com.personthecat.orestonevariants.init.LazyRegistries;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import static com.personthecat.orestonevariants.util.CommonMethods.find;

@MethodsReturnNonnullByDefault
public class VariantGroup extends ItemGroup {
    public static VariantGroup GROUP = new VariantGroup();

    private VariantGroup() {
        super("variants");
    }

    @Override
    public ItemStack createIcon() {
        final Block block = find(LazyRegistries.BLOCKS, b -> b.getRegistryName().getPath().equals("coal_ore_diorite"))
            .orElseGet(() -> LazyRegistries.BLOCKS.iterator().next());
        return new ItemStack(block);
    }
}
