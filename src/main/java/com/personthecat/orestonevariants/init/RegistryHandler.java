package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.Main;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@SuppressWarnings("unused")
@EventBusSubscriber(bus = Bus.MOD)
public class RegistryHandler {
    @SubscribeEvent
    public static void registerBlocks(final RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(Main.BLOCKS.toArray(new Block[0]));
    }

    @SubscribeEvent
    public static void registerItems(final RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(Main.ITEMS.toArray(new Item[0]));
    }
}