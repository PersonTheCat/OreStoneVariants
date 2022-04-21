package personthecat.osv;

import net.minecraft.resources.ResourceLocation;
import personthecat.catlib.data.collections.LazyRegistry;
import personthecat.osv.block.OreVariant;
import personthecat.osv.config.BlockEntry;
import personthecat.osv.config.BlockList;
import personthecat.osv.config.VariantDescriptor;
import personthecat.osv.init.GroupSerializer;
import personthecat.osv.init.PresetLoadingContext;
import personthecat.osv.init.VariantLoadingContext;
import personthecat.osv.item.VariantItem;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.util.Group;

import java.util.List;

import static personthecat.catlib.util.Shorthand.f;

public class ModRegistries {

    public static final LazyRegistry<BlockEntry, List<VariantDescriptor>> BLOCK_LIST =
        LazyRegistry.of(BlockList::loadEntries)
            .respondsWith(entry -> f("Entry was not loaded on block list event: {}", entry))
            .canBeReset(true);

    public static final LazyRegistry<String, OrePreset> ORE_PRESETS =
        LazyRegistry.of(PresetLoadingContext::getOres)
            .respondsWith(name -> f("No ore preset was found with name: {}", name))
            .canBeReset(true);

    public static final LazyRegistry<String, StonePreset> STONE_PRESETS =
        LazyRegistry.of(PresetLoadingContext::getStones)
            .respondsWith(name -> f("No stone preset was found with name: {}", name))
            .canBeReset(true);

    public static final LazyRegistry<String, Group> PROPERTY_GROUPS =
        LazyRegistry.of(GroupSerializer::loadPropertyGroups)
            .respondsWith(name -> f("Ore group is undefined: {}", name))
            .canBeReset(true);

    public static final LazyRegistry<String, Group> BLOCK_GROUPS =
        LazyRegistry.of(GroupSerializer::loadBlockGroups)
            .respondsWith(name -> f("Block group is undefined: {}", name))
            .canBeReset(true);

    public static final LazyRegistry<ResourceLocation, OreVariant> VARIANTS =
        LazyRegistry.of(VariantLoadingContext::getVariants)
            .respondsWith(id -> f("Block loaded after variants were drained: {}", id));

    public static final LazyRegistry<ResourceLocation, VariantItem> ITEMS =
        LazyRegistry.of(VariantLoadingContext::getItems)
            .respondsWith(id -> f("No item created for variant: {}", id));

    public static void resetAll() {
        LazyRegistry.resetAll(BLOCK_LIST, ORE_PRESETS, STONE_PRESETS, PROPERTY_GROUPS, BLOCK_GROUPS);
    }

    public static void deepReload() {
        PresetLoadingContext.reloadOres();
        PresetLoadingContext.reloadStones();
        LazyRegistry.reloadAll(BLOCK_LIST, ORE_PRESETS, STONE_PRESETS, PROPERTY_GROUPS, BLOCK_GROUPS);
    }
}
