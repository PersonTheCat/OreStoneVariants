package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
@EventBusSubscriber
public class RegistryHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerBlocks(final RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(Main.BLOCKS.toArray(new Block[0]));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerItems(final RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(Main.ITEMS.toArray(new Item[0]));
    }

    @SubscribeEvent
    public static void registerModels(final ModelRegistryEvent event) {
        for (BaseOreVariant block : Main.BLOCKS) {
            block.registerModels();
        }
    }
}