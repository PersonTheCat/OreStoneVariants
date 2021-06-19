package com.personthecat.orestonevariants.item;

import com.personthecat.orestonevariants.init.LazyRegistries;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static com.personthecat.orestonevariants.util.CommonMethods.osvLocation;

@MethodsReturnNonnullByDefault
public class VariantGroup extends ItemGroup {

    private static final ResourceLocation DEFAULT_BLOCK = osvLocation("coal_ore_diorite");

    public static VariantGroup GROUP = new VariantGroup();

    private VariantGroup() {
        super("variants");
    }

    @Override
    public ItemStack createIcon() {
        final Block block = LazyRegistries.BLOCKS.getOptional(DEFAULT_BLOCK)
            .orElseGet(() -> LazyRegistries.BLOCKS.iterator().next());
        return new ItemStack(block);
    }
}
