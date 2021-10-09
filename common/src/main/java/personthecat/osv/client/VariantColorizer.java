package personthecat.osv.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import lombok.extern.log4j.Log4j2;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.ModRegistries;
import personthecat.osv.block.SharedStateBlock;
import personthecat.osv.item.VariantItem;

@Log4j2
@Environment(EnvType.CLIENT)
public class VariantColorizer {

    public static void colorizeAll() {
        final BlockColors blockColors = getBlockColors();
        final ItemColors itemColors = getItemColors();

        ModRegistries.VARIANTS.forEach(variant -> colorizeBlock(blockColors, variant));
        ModRegistries.ITEMS.forEach(variant -> colorizeItem(itemColors, variant));
    }

    @ExpectPlatform
    private static ItemColors getItemColors() {
        throw new AssertionError();
    }

    private static BlockColors getBlockColors() {
        return Minecraft.getInstance().getBlockColors();
    }

    private static void colorizeBlock(final BlockColors registry, final SharedStateBlock variant) {
        Block source = variant.getBg();
        BlockColor color = getColor(registry, source);
        if (color == null) {
            source = variant.getFg();
            color = getColor(registry, source);
        }
        if (color != null) {
            log.info("Copying block colors from {} to {}", source, variant);
            registry.register(color, variant);
        }
    }

    private static void colorizeItem(final ItemColors registry, final VariantItem variant) {
        Item source = variant.getBg().asItem();
        ItemColor color = getColor(registry, source);
        if (color == null) {
            source = variant.getFg().asItem();
            color = getColor(registry, source);
        }
        if (color != null) {
            log.info("Copying item colors from {} to {}", source, variant);
            registry.register(color, variant);
        }
    }

    @Nullable
    @ExpectPlatform
    private static BlockColor getColor(final BlockColors registry, final Block block) {
        throw new AssertionError();
    }

    @Nullable
    @ExpectPlatform
    private static ItemColor getColor(final ItemColors registry, final Item item) {
        throw new AssertionError();
    }
}
