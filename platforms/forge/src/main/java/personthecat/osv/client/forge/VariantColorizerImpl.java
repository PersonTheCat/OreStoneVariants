package personthecat.osv.client.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.mixin.BlockColorsAccessor;
import personthecat.osv.mixin.ItemColorsAccessor;

public class VariantColorizerImpl {

    private static final int MAX_REGISTRY_SIZE = 4096;

    public static ItemColors getItemColors() {
        return Minecraft.getInstance().getItemColors();
    }

    private static boolean canRegister(final BlockColors registry) {
        return ((BlockColorsAccessor) registry).getBlockColors().size() < MAX_REGISTRY_SIZE;
    }

    @Nullable
    public static BlockColor getColor(final BlockColors registry, final Block block) {
        return ((BlockColorsAccessor) registry).getBlockColors().get(block.delegate);
    }

    private static boolean canRegister(final ItemColors registry) {
        return ((ItemColorsAccessor) registry).getItemColors().size() < MAX_REGISTRY_SIZE;
    }

    @Nullable
    public static ItemColor getColor(final ItemColors registry, final Item item) {
        return ((ItemColorsAccessor) registry).getItemColors().get(item.delegate);
    }
}
