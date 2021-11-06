package personthecat.osv;

import net.minecraft.resources.ResourceLocation;
import personthecat.catlib.data.SafeRegistry;
import personthecat.osv.block.OreVariant;
import personthecat.osv.config.BlockEntry;
import personthecat.osv.config.BlockList;
import personthecat.osv.config.VariantDescriptor;
import personthecat.osv.init.*;
import personthecat.osv.item.VariantItem;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.util.Group;

import java.util.List;

import static personthecat.catlib.util.Shorthand.f;

public class ModRegistries {

    public static final SafeRegistry<BlockEntry, List<VariantDescriptor>> BLOCK_LIST =
        SafeRegistry.of(BlockList::loadEntries)
            .respondsWith(entry -> f("Entry was not loaded on block list event: {}", entry))
            .canBeReset(true);

    public static final SafeRegistry<String, OrePreset> ORE_PRESETS =
        SafeRegistry.of(PresetLoadingContext::getOres)
            .respondsWith(name -> f("No ore preset was found with name: {}", name))
            .canBeReset(true);

    public static final SafeRegistry<String, StonePreset> STONE_PRESETS =
        SafeRegistry.of(PresetLoadingContext::getStones)
            .respondsWith(name -> f("No stone preset was found with name: {}", name))
            .canBeReset(true);

    public static final SafeRegistry<String, Group> PROPERTY_GROUPS =
        SafeRegistry.of(GroupSerializer::loadPropertyGroups)
            .respondsWith(name -> f("Ore group is undefined: {}", name))
            .canBeReset(true);

    public static final SafeRegistry<String, Group> BLOCK_GROUPS =
        SafeRegistry.of(GroupSerializer::loadBlockGroups)
            .respondsWith(name -> f("Block group is undefined: {}", name))
            .canBeReset(true);

    public static final SafeRegistry<Integer, ResourceLocation> DISABLED_FEATURES =
        SafeRegistry.enumerated(DisabledFeatureSupport::loadDisabledFeatures)
            .respondsWith(id -> f("No such feature in registry: {}", id))
            .canBeReset(true);

    public static final SafeRegistry<ResourceLocation, OreVariant> VARIANTS =
        SafeRegistry.of(VariantLoadingContext::getVariants)
            .respondsWith(id -> f("Block loaded after variants were drained: {}", id));

    public static final SafeRegistry<ResourceLocation, VariantItem> ITEMS =
        SafeRegistry.of(VariantLoadingContext::getItems)
            .respondsWith(id -> f("No item created for variant: {}", id));

    public static void resetAll() {
        SafeRegistry.resetAll(BLOCK_LIST, ORE_PRESETS, STONE_PRESETS, PROPERTY_GROUPS, BLOCK_GROUPS, DISABLED_FEATURES);
    }

    public static void deepReload() {
        PresetLoadingContext.reloadOres();
        PresetLoadingContext.reloadStones();
        SafeRegistry.reloadAll(BLOCK_LIST, ORE_PRESETS, STONE_PRESETS, PROPERTY_GROUPS, BLOCK_GROUPS, DISABLED_FEATURES);
    }
}
