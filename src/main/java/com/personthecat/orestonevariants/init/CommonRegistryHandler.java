package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.models.ModelConstructor;
import com.personthecat.orestonevariants.world.VariantFeature;
import com.personthecat.orestonevariants.world.VariantPlacement;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.Item;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.*;

import java.util.function.Consumer;

import static com.personthecat.orestonevariants.util.CommonMethods.runExF;
import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

@Log4j2
@SuppressWarnings("unused")
@EventBusSubscriber(bus = Bus.MOD)
public class CommonRegistryHandler {

    public static boolean MODELS_LOADING = false;

    @SubscribeEvent(priority = LOWEST)
    public static void deferredRegistries(final RegistryEvent.Register<Biome> helper) {
        // Capitalizing on this event being later than the others. We'll be using it
        // for now to guarantee that most other mods have loaded their blocks.
        // If you have a better solution, *please* submit an issue on GitHub. Thank you!
        runDeferred(ForgeRegistries.BLOCKS, CommonRegistryHandler::registerBlocks);
        runDeferred(ForgeRegistries.ITEMS, CommonRegistryHandler::registerItems);
    }

    private static void registerBlocks(final IForgeRegistry<Block> registry) {
        log.info("Registering blocks. Models may still be generating...");
        MODELS_LOADING = true;
        LazyRegistries.BLOCKS.forEach(b -> {
            registry.register(b);
            if (FMLEnvironment.dist == Dist.CLIENT) {
                RenderTypeLookup.setRenderLayer(b, b::canRenderInLayer);
                ModelConstructor.generateOreModels(b);
            }
        });
        MODELS_LOADING = false;
        log.info("Done registering blocks. Models are done generating.");
    }

    private static void registerItems(final IForgeRegistry<Item> registry) {
        LazyRegistries.ITEMS.forEach(registry::register);
    }

    private static <T extends IForgeRegistryEntry<T>> void runDeferred(IForgeRegistry<T> r, Consumer<IForgeRegistry<T>> f) {
        if (!(r instanceof ForgeRegistry)) {
            throw runExF("Attempted to add listener for unsupported type: {}", r.getClass());
        }
        log.info("Running deferred registry event for {}", r.getRegistryName());
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
}