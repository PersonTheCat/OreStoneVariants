package personthecat.osv.item;

import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.event.registry.CommonRegistries;
import personthecat.osv.block.AdditionalProperties;
import personthecat.osv.mixin.ItemAccessor;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.ItemSettings;
@Log4j2
public class ItemPropertiesHelper {

    public static Item.Properties create(final OrePreset preset, final Item base, final BlockState state) {
        final Item.Properties properties = new Item.Properties();
        final ItemSettings settings = preset.getItem();

        if (getIsFireResistant(settings, base)) properties.fireResistant();
        properties.stacksTo(getStackSize(settings, base));
        properties.rarity(getRarity(settings, base));
        properties.craftRemainder(getCraftRemaining(settings, base));
        properties.food(getFoodProperties(settings, base));

        final CreativeModeTab tab = AdditionalProperties.isDense(state)
            ? DenseVariantTab.INSTANCE : VariantTab.INSTANCE;

        return properties.tab(tab);
    }

    private static boolean getIsFireResistant(final ItemSettings settings, final Item base) {
        final Boolean configured = settings.getIsFireResistant();
        if (configured != null) return configured;
        return base.isFireResistant();
    }

    private static int getStackSize(final ItemSettings settings, final Item base) {
        final Integer configured = settings.getMaxStackSize();
        if (configured != null) return configured;
        return base.getMaxStackSize();
    }

    private static Rarity getRarity(final ItemSettings settings, final Item base) {
        final Rarity configured = settings.getRarity();
        if (configured != null) return configured;
        return ((ItemAccessor) base).getRarity();
    }

    @Nullable
    private static Item getCraftRemaining(final ItemSettings settings, final Item base) {
        final ResourceLocation configured = settings.getCraftRemainingItem();
        if (configured != null) return CommonRegistries.ITEMS.lookup(configured);
        return base.getCraftingRemainingItem();
    }

    @Nullable
    private static FoodProperties getFoodProperties(final ItemSettings settings, final Item base) {
        final FoodProperties configured = settings.getFoodProperties();
        if (configured != null) return configured;
        return base.getFoodProperties();
    }
}
