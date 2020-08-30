package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.item.VariantItem;
import com.personthecat.orestonevariants.world.VariantFeature;
import com.personthecat.orestonevariants.world.VariantPlacement;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

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
    public static void registerOreFeatures(final RegistryEvent.Register<Feature<?>> event) {
        Registry.register(Registry.FEATURE, "osv:variant_feature", VariantFeature.INSTANCE);
    }

    @SubscribeEvent
    public static void registerOrePlacement(final RegistryEvent.Register<Placement<?>> event) {
        Registry.register(Registry.DECORATOR, "osv:variant_placement", VariantPlacement.INSTANCE);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void colorizeVariants(final ColorHandlerEvent.Item event) {
        Main.ITEMS.forEach(i -> copyColor(i, event.getBlockColors(), event.getItemColors()));
    }

    private static void copyColor(VariantItem item, BlockColors blockColors, ItemColors itemColors) {
        final int blockColor = blockColors.getColor(item.getBg(), null, null, 0);
        blockColors.register((state, reader, pos, tint) -> blockColor, item.getBlock());
        final ItemStack bgStack = toStack(item.getBg());
        final int itemColor = itemColors.getColor(bgStack, 0);
        itemColors.register((state, tint) -> itemColor, item);
    }
}