package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.world.VariantFeature;
import com.personthecat.orestonevariants.world.VariantPlacement;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.*;
import java.util.function.Consumer;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static net.minecraftforge.eventbus.api.EventPriority.*;

@SuppressWarnings("unused")
@EventBusSubscriber(bus = Bus.MOD)
public class RegistryHandler {

    @SubscribeEvent(priority = LOWEST)
    public static void deferredRegistries(final RegistryEvent.Register<Biome> helper) {
        // Capitalizing on this event being later than the others. We'll be using it
        // for now to guarantee that most other mods have loaded their blocks.
        // If you have a better solution, *please* submit an issue on GitHub. Thank you!
        runDeferred(ForgeRegistries.BLOCKS, RegistryHandler::registerBlocks);
        runDeferred(ForgeRegistries.ITEMS, RegistryHandler::registerItems);
    }

    private static void registerBlocks(final IForgeRegistry<Block> registry) {
        Main.BLOCKS.forEach(b -> {
            registry.register(b);
            b.updatePostRegister();
        });
    }

    private static void registerItems(final IForgeRegistry<Item> registry) {
        Main.ITEMS.forEach(registry::register);
    }

    private static <T extends IForgeRegistryEntry<T>> void runDeferred(IForgeRegistry<T> r, Consumer<IForgeRegistry<T>> f) {
        if (!(r instanceof ForgeRegistry)) {
            throw runExF("Attempted to add listener for unsupported type: {}", r.getClass());
        }
        final ForgeRegistry<T> registry = (ForgeRegistry<T>) r;
        registry.unfreeze();
        f.accept(registry);
        registry.freeze();
    }

    @SubscribeEvent
    public static void registerOreFeatures(final RegistryEvent.Register<Feature<?>> event) {
        event.getRegistry().register(VariantFeature.INSTANCE);
    }

    @SubscribeEvent
    public static void registerOrePlacement(final RegistryEvent.Register<Placement<?>> event) {
        event.getRegistry().register(VariantPlacement.INSTANCE);
    }

    // Todo: may need to also push these events back.
//    @OnlyIn(Dist.CLIENT)
//    @SubscribeEvent(priority = LOWEST)
//    public static void colorizeVariants(final ColorHandlerEvent.Item event) {
//        Main.ITEMS.forEach(i -> copyColor(i, event.getBlockColors(), event.getItemColors()));
//    }
//
//    private static void copyColor(VariantItem item, BlockColors blockColors, ItemColors itemColors) {
//        final int blockColor = blockColors.getColor(item.getBg(), null, null, 0);
//        blockColors.register((state, reader, pos, tint) -> blockColor, item.getBlock());
//        final ItemStack bgStack = toStack(item.getBg());
//        final int itemColor = itemColors.getColor(bgStack, 0);
//        itemColors.register((state, tint) -> itemColor, item);
//    }
}