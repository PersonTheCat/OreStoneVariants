package personthecat.osv.init;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j2;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.registry.CommonRegistries;
import personthecat.catlib.event.registry.RegistryAddedCallback;
import personthecat.catlib.event.registry.RegistryAddedEvent;
import personthecat.catlib.registry.RegistryHandle;
import personthecat.catlib.util.McUtils;
import personthecat.osv.ModRegistries;
import personthecat.osv.block.OreVariant;
import personthecat.osv.client.VariantRenderDispatcher;
import personthecat.osv.client.model.ModelHandler;
import personthecat.osv.config.Cfg;
import personthecat.osv.config.VariantDescriptor;
import personthecat.osv.exception.UnloadedVariantsException;
import personthecat.osv.exception.VariantLoadException;
import personthecat.osv.item.VariantItem;
import personthecat.osv.mixin.InfestedBlockAccessor;
import personthecat.osv.util.Reference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static personthecat.catlib.util.Shorthand.drain;

@Log4j2
public final class VariantLoadingContext {

    private static final Context CTX = new Context();

    private VariantLoadingContext() {}

    public static void startLoading() {
        log.info("Starting early block setup.");
        final Stopwatch sw = Stopwatch.createStarted();

        if (Cfg.smartUpdatePresets() && Reference.VERSION_CACHE.isUpgraded()) {
            log.info("Detected version upgrade. Running smart transformations.");
            PresetLoadingContext.runTransformations();
            log.info("All presets transformed in {}.", sw);
            sw.reset().start();
        }

        init();
        log.info("Early block setup completed in {}. Loaded {} variants.", sw, CTX.output.size());

        if (CTX.unloaded.isEmpty()) {
            log.info("All dependencies loaded in time. OSV variant setup complete.");
        } else {
            log.info("Still have {} variants to load. Adding listener.", CTX.unloaded.size());
            BlockRegistryListener.startListening();
        }
    }

    private static void init() {
        synchronized (CTX) {
            ModRegistries.BLOCK_LIST.forEach(descriptors -> descriptors.forEach(descriptor -> {
                if (descriptor.canLoad(CommonRegistries.BLOCKS)) {
                    generateVariant(descriptor);
                } else {
                    CTX.unloaded.add(descriptor);
                }
            }));
        }
    }

    private static void generateVariant(final VariantDescriptor descriptor) {
        final ResourceLocation id = descriptor.getId();
        try {
            if (CommonRegistries.BLOCKS.isRegistered(id)) {
                log.warn("Variant is already registered. Ignoring... ({})", id);
                return;
            }
            final OreVariant variant = descriptor.generateBlock();
            CTX.output.put(id, CommonRegistries.BLOCKS.register(id, variant));

            ItemGenerator.createItems(descriptor, variant).forEach((nid, item) ->
                CTX.items.put(nid, CommonRegistries.ITEMS.register(nid, item)));

            onVariantLoaded(variant, descriptor);
        } catch (final RuntimeException e) {
            LibErrorContext.error(Reference.MOD, new VariantLoadException(id, e));
        }
    }

    private static void onVariantLoaded(final OreVariant variant, final VariantDescriptor descriptor) {
        if (McUtils.isClientSide()) {
            // Todo: return whether model was generated. If late, handle reload scenario.
            ModelHandler.generateModels(descriptor);
            VariantRenderDispatcher.setupRenderLayer(descriptor, variant);
        }
        if (Cfg.mapInfestedVariants() && variant.getFg() instanceof InfestedBlock) {
            InfestedBlockAccessor.getBlockByHostMap().put(variant.getBg(), variant);
        }
        log.debug("Loaded {} -> {} ({})", descriptor.getForeground(), descriptor.getBackground(), descriptor.getPath());
    }

    public static void stopLoading() {
        if (CTX.listener != null) {
            CTX.listener.destroy();

            if (!CTX.unloaded.isEmpty()) {
                log.error("{} variants were unable to load in time.", CTX.unloaded.size());
                LibErrorContext.error(Reference.MOD, new UnloadedVariantsException(CTX.unloaded));
            }
        }
    }

    public static Map<ResourceLocation, OreVariant> getVariants() {
        return ImmutableMap.copyOf(CTX.output);
    }

    public static Map<ResourceLocation, VariantItem> getItems() {
        return ImmutableMap.copyOf(CTX.items);
    }

    private static class Context {
        final Map<ResourceLocation, OreVariant> output = new HashMap<>();
        final Map<ResourceLocation, VariantItem> items = new HashMap<>();
        final Set<VariantDescriptor> unloaded = new HashSet<>();
        volatile BlockRegistryListener listener = null;
    }

    private static class BlockRegistryListener implements RegistryAddedCallback<Block> {

        private static void startListening() {
            synchronized (CTX) {
                RegistryAddedEvent.get(Registry.BLOCK_REGISTRY)
                    .register(CTX.listener = new BlockRegistryListener());
            }
        }

        @Override
        public void onRegistryAdded(final RegistryHandle<Block> handle, final ResourceLocation id, final Block block) {
            if (CTX.unloaded.isEmpty()) {
                this.destroy();
                return;
            }
            synchronized (CTX) {
                drain(CTX.unloaded, descriptor -> descriptor.canLoad(handle, id))
                    .forEach(VariantLoadingContext::generateVariant);
            }
        }

        private void destroy() {
            synchronized (CTX) {
                RegistryAddedEvent.get(Registry.BLOCK_REGISTRY).deregister(this);
                CTX.listener = null;
            }
        }
    }
}
