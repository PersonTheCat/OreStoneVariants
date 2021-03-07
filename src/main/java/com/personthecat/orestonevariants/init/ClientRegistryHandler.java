package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.item.VariantItem;
import com.personthecat.orestonevariants.util.unsafe.ReflectionTools;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.IRegistryDelegate;

import java.lang.reflect.Field;
import java.util.Map;

@Log4j2
public class ClientRegistryHandler {

    /** Used for copying block colors from background blocks. */
    private static final Field BLOCK_COLORS = ReflectionTools.getField(BlockColors.class, "field_186725_a");

    /** Used for copying item colors based on original background blocks. */
    private static final Field ITEM_COLORS = ReflectionTools.getField(ItemColors.class, "field_186732_a");

    @SuppressWarnings("unused")
    public static void clientLoadComplete(final FMLLoadCompleteEvent event) {
        colorizeVariants(); // Using this event for compat with Quark.
    }

    private static void colorizeVariants() {
        final Minecraft minecraft = Minecraft.getInstance();
        final BlockColors blockColors = minecraft.getBlockColors();
        final ItemColors itemColors = minecraft.getItemColors();
        LazyRegistries.ITEMS.forEach(i -> copyColor(i, blockColors, itemColors));
    }

    private static void copyColor(VariantItem item, BlockColors blockColors, ItemColors itemColors) {
        final Map<IRegistryDelegate<Block>, IBlockColor> blockGetter = ReflectionTools.getValue(BLOCK_COLORS, blockColors);
        final Map<IRegistryDelegate<Item>, IItemColor> itemGetter = ReflectionTools.getValue(ITEM_COLORS, itemColors);

        final IBlockColor blockColor = blockGetter.get(item.getBg().getBlock().delegate);
        if (blockColor != null) {
            log.info("Copying block colors from: {} -> {}", item.getBg(), item.getBlock());
            blockColors.register(blockColor, item.getBlock());
        }

        final ItemStack bgStack = new ItemStack(item.getBg().getBlock());
        final IItemColor itemColor = itemGetter.get(bgStack.getItem().delegate);
        if (itemColor != null) {
            log.info("Copying item colors from: {} -> {}", item.getBg(), item.getBlock());
            itemColors.register(itemColor, item);
        }
    }
}
