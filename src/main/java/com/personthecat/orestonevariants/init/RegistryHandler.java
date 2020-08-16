package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.util.CommonMethods;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@SuppressWarnings("unused")
@EventBusSubscriber(bus = Bus.MOD)
public class RegistryHandler {
    @SubscribeEvent
    public static void registerBlocks(final RegistryEvent.Register<Block> event) {
        Main.BLOCKS.forEach(b -> {
            event.getRegistry().register(b);
            b.updatePostRegister();
        });
    }

    @SubscribeEvent
    public static void registerItems(final RegistryEvent.Register<Item> event) {
        Main.ITEMS.forEach(i -> event.getRegistry().register(i));
    }

    @SubscribeEvent
    public static void colorizeBlocks(final ColorHandlerEvent.Block event) {
        CommonMethods.info("Running colorizer.");
        Main.BLOCKS.forEach(b -> copyColor(b, event.getBlockColors()));
    }

    private static void copyColor(BaseOreVariant ore, BlockColors colors) {
        final int bgColor = colors.getColor(ore.bgBlock, null, null, 0);
        colors.register((state, reader, pos, tint) -> bgColor, ore);
    }
}