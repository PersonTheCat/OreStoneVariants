package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.Range;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

/** Contains all of the properties needed to determine which items to drop. */
public class DropProperties {
    /** A range representing how many items to drop. */
    public final Range quantity;
    /** A range representing how much xp to drop. */
    public final Range xp;
    /** The given chance for this item to drop. */
    public final double chance;
    /** The advancement associated with this drop. */
    public final ResourceLocation advancement;
    /** The actual item to drop. */
    public final Lazy<Item> item;
    /** The original ore block. */
    public final Lazy<BlockState> block;
    /** Whether the item dropped is a block. */
    public final Lazy<Boolean> dropsBlock;
    /** Whether the relevant block can drop itself. */
    public final Lazy<Boolean> canDropSelf;

    /** Primary constructor. */
    public DropProperties(
        Range quantity,
        Range xp,
        double chance,
        String advancement,
        String itemLookup,
        String blockLookup
    ) {
        this.quantity = quantity;
        this.xp = xp;
        this.chance = chance;
        this.advancement = new ResourceLocation(advancement);
        this.item = new Lazy<>(() -> getItem(itemLookup).get());
        this.block = new Lazy<>(() -> getBlockState(blockLookup).get());
        this.dropsBlock = new Lazy<>(this::_dropsBlock);
        this.canDropSelf = new Lazy<>(this::_canDropSelf);
    }

    /** The internal method used to determine whether this drop is a block. */
    private boolean _dropsBlock() {
        return isBlock(item.get().getRegistryName());
    }

    /** The internal method used to determine whether the relevant block can drop itself. */
    private boolean _canDropSelf() {
        if (dropsBlock.get()) {
            final Block dropBlock = Block.getBlockFromItem(item.get());
            final Block dropSilkTouch = block.get().getBlock();
            return dropBlock.equals(dropSilkTouch);
        }
        return false;
    }
}