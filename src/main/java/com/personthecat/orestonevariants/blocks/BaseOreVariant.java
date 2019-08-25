package com.personthecat.orestonevariants.blocks;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.BlockPropertiesHelper;
import com.personthecat.orestonevariants.properties.OreProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;

import java.util.List;

public class BaseOreVariant extends Block {
    /** Contains the standard block properties and any additional values, if necessary. */
    private final OreProperties properties;

    /** Primary constructor. */
    public BaseOreVariant(OreProperties properties, BlockState bgBlock) {
        super(createProperties(properties.getBlock(), bgBlock));
        this.properties = properties;
    }

    /** Decides whether to merge block properties for this ore. */
    private static Block.Properties createProperties(Block.Properties ore, BlockState bgBlock) {
        return Cfg.bgImitation.get() ? BlockPropertiesHelper.merge(ore, bgBlock) : ore;
    }

    /** Returns a stack containing this block. */
    private ItemStack getStack() {
        return new ItemStack(this);
    }

    /** Returns a stack containing the background ore block represented by this block. */
    private ItemStack getBackgroundStack() {
        return new ItemStack(properties.getOre().getBlock());
    }

    /** Substitutes drops from the lookup loot table with those of a raw table, if applicable. */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        final LootContext ctx = builder
            .withParameter(LootParameters.BLOCK_STATE, state)
            .build(LootParameterSets.BLOCK);
        final List<ItemStack> items = properties.getDrops()
            .map(loot -> loot.generate(ctx))
            .orElse(super.getDrops(state, builder));
        return handleSilkTouch(items);
    }

    /** Replaces the original silk touch drop with this block, if applicable. */
    private List<ItemStack> handleSilkTouch(List<ItemStack> items) {
        items.replaceAll(item -> {
            if (item.isItemEqual(getBackgroundStack())) {
                return getStack();
            } else {
                return item;
            }
        });
        return items;
    }
}