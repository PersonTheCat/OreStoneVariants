package personthecat.osv.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import personthecat.osv.preset.OrePreset;

public class ItemPropertiesHelper {

    public static Item.Properties create(final OrePreset preset, final Item base) {
        return new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS);
    }
}
