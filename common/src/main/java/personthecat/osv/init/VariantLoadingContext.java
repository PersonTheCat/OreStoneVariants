package personthecat.osv.init;

import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j2;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.event.registry.CommonRegistries;
import personthecat.catlib.event.registry.RegistryAddedEvent;
import personthecat.catlib.event.registry.RegistryHandle;
import personthecat.catlib.util.McUtils;
import personthecat.osv.ModRegistries;
import personthecat.osv.block.SharedStateBlock;
import personthecat.osv.client.VariantRenderDispatcher;
import personthecat.osv.config.VariantDescriptor;
import personthecat.osv.exception.VariantLoadException;
import personthecat.osv.client.model.ModelHandler;
import personthecat.osv.util.Reference;

import java.util.*;

import static personthecat.catlib.util.Shorthand.drain;

@Log4j2
public final class VariantLoadingContext {

    private static final Context CTX = new Context();

    private VariantLoadingContext() {}

    public static void startLoading() {
        init();

        RegistryAddedEvent.get(Registry.BLOCK_REGISTRY).register((registry, id, block) -> {
            if (!CTX.unloaded.isEmpty()) {
                synchronized (CTX) {
                    drain(CTX.unloaded, descriptor -> descriptor.canLoad(registry, id))
                        .forEach(descriptor -> generateVariant(registry, descriptor));
                }
            }
        });
    }

    private static void init() {
        synchronized (CTX) {
            ModRegistries.BLOCK_LIST.forEach(descriptors -> descriptors.forEach(descriptor -> {
                if (descriptor.canLoad(CommonRegistries.BLOCKS)) {
                    generateVariant(CommonRegistries.BLOCKS, descriptor);
                } else {
                    CTX.unloaded.add(descriptor);
                }
            }));
        }
    }

    private static void generateVariant(final RegistryHandle<Block> registry, final VariantDescriptor descriptor) {
        final ResourceLocation id = descriptor.getId();
        try {
            final SharedStateBlock variant = descriptor.generateBlock(registry);
            CTX.output.put(id, (SharedStateBlock) registry.register(id, variant));
            onVariantLoaded(variant, descriptor);
        } catch (final RuntimeException e) {
            LibErrorContext.registerSingle(Reference.MOD_NAME, new VariantLoadException(id, e));
        }
    }

    private static void onVariantLoaded(final SharedStateBlock variant, final VariantDescriptor descriptor) {
        if (McUtils.isClientSide()) {
            // Todo: return whether model was generated. If late, handle reload scenario.
            ModelHandler.generateModel(descriptor);
            VariantRenderDispatcher.setupRenderLayer(variant);
        }
        log.debug("Loaded {} -> {} ({})", descriptor.getForeground(), descriptor.getBackground(), descriptor.getPath());
    }

    public static Map<ResourceLocation, SharedStateBlock> getVariants() {
        return ImmutableMap.copyOf(CTX.output);
    }

    private static class Context {
        final Map<ResourceLocation, SharedStateBlock> output = new HashMap<>();
        final Set<VariantDescriptor> unloaded = new HashSet<>();
    }
}