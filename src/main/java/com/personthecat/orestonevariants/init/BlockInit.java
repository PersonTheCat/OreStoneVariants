package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.blocks.BlockEntry;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.util.ExtendedResourceLocation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class BlockInit {
    public static Set<BaseOreVariant> setupBlocks() {
        Set<BaseOreVariant> variants = new HashSet<>();
        for (BlockEntry entry : Main.BLOCK_ENTRIES) {
            for (IBlockState block : entry.blocks.blocks.get()) {
                for (OreProperties props : entry.properties.properties) {
                    if (filterUBCVariants(props)) {
                        variants.add(BaseOreVariant.of(props, block));
                    }
                }
            }
        }
        return variants;
    }

    private static boolean filterUBCVariants(OreProperties properties) {
        if (Cfg.WorldCat.avoidDuplicateUBCVariants && Cfg.modEnabled("undergroundbiomes")) {
            final ExtendedResourceLocation location = ExtendedResourceLocation.complete(properties.oreLookup);
            return !doesUBCSupport(location.getNamespace());
        }
        return true;
    }

    private static boolean doesUBCSupport(String mod) {
        final List<ResourceLocation> ubc = findAll(ForgeRegistries.BLOCKS.getKeys(),
            l -> l.getNamespace().equals("undergroundbiomes")
        );
        for (ResourceLocation location : ubc) {
            // Old way of doing this. Not updating it yet.
            if (location.getPath().contains(mod)) {
                return true;
            }
        }
        return false;
    }
}