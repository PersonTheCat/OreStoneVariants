package personthecat.osv.init;

import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import personthecat.osv.block.OreVariant;
import personthecat.osv.config.VariantDescriptor;
import personthecat.osv.item.VariantItem;
import personthecat.osv.util.StateMap;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class ItemGenerator {

    public static Map<ResourceLocation, VariantItem> createItems(final VariantDescriptor descriptor, final OreVariant variant) {
        final Map<ResourceLocation, VariantItem> items = new HashMap<>();
        final ResourceLocation id = descriptor.getId();

        descriptor.getForeground().getItemVariants().forEach((state, affix) -> {
            final BlockState resolved = StateMap.resolve(variant, state);
            if (resolved == null) {
                log.warn("Could not resolve state {} in {}", state, variant);
            } else if (affix.isEmpty()) {
                log.info("Overriding default variant item to {} for {}", state, descriptor);
                items.put(id, descriptor.generateItem(state, resolved));
            } else {
                final ResourceLocation nid = new ResourceLocation(id.getNamespace(), affix + "_" + id.getPath());
                items.put(nid, descriptor.generateItem(state, resolved));
            }
        });
        if (!items.containsKey(id)) {
            items.put(id, descriptor.generateItem("", variant.defaultBlockState()));
        }
        return items;
    }
}
