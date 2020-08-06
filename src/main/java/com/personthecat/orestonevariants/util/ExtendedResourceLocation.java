package com.personthecat.orestonevariants.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

/** Variant of ResourceLocation which includes meta values. */
public class ExtendedResourceLocation extends ResourceLocation {
    private final int meta;

    public ExtendedResourceLocation(String name, int meta) {
        super(name);
        this.meta = meta;
    }

    public static ExtendedResourceLocation complete(String name) {
        // View the components of this name separately.
        final String[] split = name.split(":");

        // Ensure the number of segments to be valid.
        if (!(split.length > 0 && split.length < 4)) {
            throw runExF("Syntax error: could not determine blockstate from {}", name);
        }
        // Use the end section to determine the format.
        final String end = split[split.length - 1];

        // If the end of the string is numeric, is must be the metadata.
        if (StringUtils.isNumeric(end)) {
            final int meta = Integer.parseInt(end);
            final String updated = name.replace(":" + end, "");
            return new ExtendedResourceLocation(updated, meta);
        }
        // The end isn't numeric, so the name is in the standard format.
        return new ExtendedResourceLocation(name, 0);
    }

    public static ExtendedResourceLocation fromState(IBlockState state) {
        final int meta = state.getBlock().getMetaFromState(state);
        final String name = nullable(state.getBlock().getRegistryName())
            .orElseThrow(() -> runEx("Invalid block state registry."))
            .toString();
        return new ExtendedResourceLocation(name, meta);
    }

    public static ExtendedResourceLocation fromStack(ItemStack stack) {
        final int meta = stack.getMetadata();
        final String name = nullable(stack.getItem().getRegistryName())
            .orElseThrow(() -> runEx("Invalid item stack registry."))
            .toString();
        return new ExtendedResourceLocation(name, meta);
    }

    /** Strips away the meta value. This could be necessary for registry lookups. (?) */
    public ResourceLocation strip() {
        return new ResourceLocation(namespace, path);
    }

    public int getMeta() {
        return meta;
    }

    @Override
    public String toString() {
        final String main = super.toString();
        return meta > 0 ? main + ":" + meta : main;
    }
}
