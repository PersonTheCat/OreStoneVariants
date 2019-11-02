package personthecat.mod.compat;

import java.util.Collections;
import java.util.Map;

import com.oitsjustjose.geolosys.common.api.GeolosysAPI;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import personthecat.mod.properties.WorldGenProperties;
import personthecat.mod.world.gen.WorldGenCustomOres;

public class GeolosysCompat
{
    /**
     * Does not support dense variants.
     */
    public static void enableGeolosysVeinGeneration()
    {
        for (WorldGenProperties genProp : WorldGenProperties.getWorldGenPropertyRegistry())
        {
            if (!genProp.isValidProperty()) continue;

            Map<IBlockState, IBlockState> worldGenMap = WorldGenCustomOres.getWorldGenMap(genProp.getName());

            for (Map.Entry<IBlockState, IBlockState> worldGenEntry : worldGenMap.entrySet())
            {
                if (!genProp.getOreProperties().inUse())
                {
                    continue; //Placement?
                }

                int newBlockCount = genProp.getBlockCount() * 15;

                GeolosysAPI.registerMineralDeposit(
                    worldGenEntry.getValue(),
                    Blocks.AIR.getDefaultState(),
                    genProp.getMinHeight(),
                    genProp.getMaxHeight(),
                    newBlockCount > 70 ? 70 : newBlockCount,
                    genProp.getFrequency() * 10,
                    genProp.getDimensionListAsBlackList(),
                    Collections.singletonList(worldGenEntry.getKey()),
                    1.0F
                );

                GeolosysAPI.oreConverterBlacklist.add(worldGenEntry.getValue());
            }
        }
    }
}