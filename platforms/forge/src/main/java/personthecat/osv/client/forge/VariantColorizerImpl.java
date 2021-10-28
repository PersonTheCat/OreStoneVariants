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

    public static ItemColors getItemColors() {
        return Minecraft.getInstance().getItemColors();
    }

    @Nullable
    public static BlockColor getColor(final BlockColors registry, final Block block) {
        return ((BlockColorsAccessor) registry).getBlockColors().get(block.delegate);
    }

    @Nullable
    public static ItemColor getColor(final ItemColors registry, final Item item) {
        return ((ItemColorsAccessor) registry).getItemColors().get(item.delegate);
    }
}
