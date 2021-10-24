package personthecat.osv.init;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j2;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.event.registry.CommonRegistries;
import personthecat.catlib.event.registry.RegistryAddedEvent;
import personthecat.catlib.util.McUtils;
import personthecat.osv.ModRegistries;
import personthecat.osv.block.OreVariant;
import personthecat.osv.client.VariantRenderDispatcher;
import personthecat.osv.config.VariantDescriptor;
import personthecat.osv.exception.VariantLoadException;
import personthecat.osv.client.model.ModelHandler;
import personthecat.osv.item.VariantItem;
import personthecat.osv.util.Reference;

import java.util.*;

import static personthecat.catlib.util.Shorthand.drain;

@Log4j2
public final class VariantLoadingContext {

    private static final Context CTX = new Context();

    private VariantLoadingContext() {}

    public static void startLoading() {
        log.info("Starting early block setup.");

        final Stopwatch sw = Stopwatch.createStarted();
        init();

        log.info("Early block setup completed in {}. Loaded {} variants.", sw, CTX.output.size());

        if (CTX.unloaded.isEmpty()) {
            log.info("All dependencies loaded in time. OSV variant setup complete.");
            return;
        } else {
            log.info("Still have {} variants to load. Adding listener.", CTX.unloaded.size());
        }

        RegistryAddedEvent.get(Registry.BLOCK_REGISTRY).register((registry, id, block) -> {
            if (!CTX.unloaded.isEmpty()) {
                synchronized (CTX) {
                    drain(CTX.unloaded, descriptor -> descriptor.canLoad(registry, id))
                        .forEach(VariantLoadingContext::generateVariant);
                }
            }
        });
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
            final OreVariant variant = descriptor.generateBlock();
            CTX.output.put(id, CommonRegistries.BLOCKS.register(id, variant));

            ItemGenerator.createItems(descriptor, variant).forEach((nid, item) ->
                CTX.items.put(nid, CommonRegistries.ITEMS.register(nid, item)));

            onVariantLoaded(variant, descriptor);
        } catch (final RuntimeException e) {
            LibErrorContext.registerSingle(Reference.MOD_NAME, new VariantLoadException(id, e));
        }
    }

    private static void onVariantLoaded(final OreVariant variant, final VariantDescriptor descriptor) {
        if (McUtils.isClientSide()) {
            // Todo: return whether model was generated. If late, handle reload scenario.
            ModelHandler.generateModels(descriptor);
            VariantRenderDispatcher.setupRenderLayer(descriptor, variant);
        }
        log.debug("Loaded {} -> {} ({})", descriptor.getForeground(), descriptor.getBackground(), descriptor.getPath());
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
    }
}
