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
        Main.BLOCKS.forEach(b -> {
            event.getRegistry().register(b);
            b.updatePostRegister();
        });
    }

    @SubscribeEvent
    public static void registerItems(final RegistryEvent.Register<Item> event) {
        Main.ITEMS.forEach(i -> event.getRegistry().register(i));
    }
}