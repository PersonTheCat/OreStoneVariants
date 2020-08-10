package com.personthecat.orestonevariants.item;

import com.personthecat.orestonevariants.Main;
import net.minecraft.block.Block;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class VariantGroup extends ItemGroup {
    public static VariantGroup GROUP = new VariantGroup();

    private VariantGroup() {
        super("variants");
    }

    @Override
    public ItemStack createIcon() {
        final Block block = find(Main.BLOCKS, b -> b.getRegistryName().getPath().equals("coal_ore_diorite"))
            .orElseGet(() -> Main.BLOCKS.iterator().next());
        return new ItemStack(block);
    }
}
